package cz.abclinuxu.migrate;

import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.impl.MySqlPersistence;
import cz.abclinuxu.data.User;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.exceptions.PersistenceException;
import cz.finesoft.socd.analyzer.DiacriticRemover;

import java.util.*;
import java.text.ParseException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.regexp.RE;
import org.dom4j.Element;

/**
 * Tool for findg users which has conflict in login / nickname with other users,
 * when case insensitive search is performed.
 * User: literakl
 * Date: 18.2.2007
 */
public class FindCollidingUsers {
    static DiacriticRemover diacriticTool = DiacriticRemover.getInstance();
    static RE reLoginInvalid = new RE("[^a-zA-Z0-9_.\\-]");
    static SQLTool sqlTool = SQLTool.getInstance();
    static Persistence persistence = PersistenceFactory.getPersistence();

    public static void main(String[] args) throws Exception {
        boolean print = false, mail = false, fix = false;
        if (args == null || args.length == 0)
            print = true;
        else {
            if ("print".equalsIgnoreCase(args[0]))
                print = true;
            else if ("mail".equalsIgnoreCase(args[0]))
                mail = true;
            else if ("fix".equalsIgnoreCase(args[0]))
                fix = true;
        }

        int max = sqlTool.getMaximumUserId();
        Map<String, List<UserInfo>> logins = new HashMap<String, List<UserInfo>>(max, 0.99f);
        Map<String, List<UserInfo>> nicknames = new HashMap<String, List<UserInfo>>(max, 0.99f);

        User user;
        for (int i = 1; i < max; i++) {
            try {
                user = (User) persistence.findById(new User(i));
            } catch (Exception e) {
                continue;
            }

            String login = user.getLogin().toLowerCase();
            login = diacriticTool.removeDiacritics(login);
            List<UserInfo> list = logins.get(login);
            if (list == null) {
                list = new ArrayList<UserInfo>(3);
                logins.put(login, list);
            }
            list.add(new UserInfo(user));

            String nick = user.getNick();
            if (nick != null) {
                nick = nick.toLowerCase();
                nick = diacriticTool.removeDiacritics(nick);
                list = nicknames.get(nick);
                if (list == null) {
                    list = new ArrayList<UserInfo>(3);
                    nicknames.put(nick, list);
                }
                list.add(new UserInfo(user));
            }
        }

        if (print)
            printConflicts(logins, nicknames);
        if (mail)
            mailConflicts(logins, nicknames);
        if (fix)
            fixConflicts(logins, nicknames);
    }

    private static void fixConflicts(Map<String, List<UserInfo>> logins, Map<String, List<UserInfo>> nicknames) throws Exception {
        System.out.println("id; original login; new login");
        for (List<UserInfo> users : logins.values()) {
            setNewLogin(users, true);
            for (UserInfo user : users) {
                if (user.getNewLogin() != null)
                    System.out.println(user.getId() + "; " + user.getLogin() + "; " + user.getNewLogin());
            }
        }

        System.out.println();
        System.out.println("id; original nick; new nick");
        for (List<UserInfo> users : nicknames.values()) {
            setNewNick(users, true);
            for (UserInfo user : users) {
                if (user.getNewNick() != null)
                    System.out.println(user.getId() + "; " + user.getNick() + "; " + user.getNewNick());
            }
        }
    }

    private static void mailConflicts(Map<String, List<UserInfo>> logins, Map<String, List<UserInfo>> nicknames) throws Exception {
        Map<Integer, UserInfo> users = new HashMap<Integer, UserInfo>();
        for (List<UserInfo> list : logins.values()) {
            if (list.size() == 1 && ! list.get(0).isIllegalLogin())
                continue;

            setNewLogin(list, false);
            for (UserInfo user : list) {
                user.setLoginConflict(list.size() > 1);
                users.put(user.getId(), user);
            }
        }

        for (List<UserInfo> list : nicknames.values()) {
            setNewNick(list, false);
            for (UserInfo user : list) {
                if (list.size() == 1)
                    continue;

                UserInfo stored = (UserInfo) users.get(user.getId());
                if (stored == null) {
                    stored = user;
                } else {
                    stored.setNewNick(user.getNewNick());
                }
                stored.setNickConflict(true);
                users.put(user.getId(), stored);
            }
        }

        Map env = new HashMap();
        env.put(EmailSender.KEY_SUBJECT, "info o vasem ucte na abclinuxu");
        env.put(EmailSender.KEY_FROM, "robot@abclinuxu.cz");
        env.put(EmailSender.KEY_TEMPLATE, "/mail/kolize.ftl");

        for (UserInfo user : users.values()) {
            env.put(EmailSender.KEY_TO, user.getEmail()); // if
            env.put("USER", user);
            System.out.println(FMUtils.executeTemplate("/mail/kolize.ftl", env));
        }
    }

    private static void printConflicts(Map<String, List<UserInfo>> logins, Map<String, List<UserInfo>> nicknames) {
        System.out.println("Conflicting and illegal logins:");
        for (Iterator<String> iter = logins.keySet().iterator(); iter.hasNext();) {
            String s = iter.next();
            List<UserInfo> list = logins.get(s);
            if (list.size() > 1 || list.get(0).isIllegalLogin()) {
                Collections.sort(list);
                System.out.println(s + " -> " + list);
            }
        }
        System.out.println("\n\n");

        System.out.println("Conflicting nicknames:");
        for (Iterator<String> iter = nicknames.keySet().iterator(); iter.hasNext();) {
            String s = iter.next();
            List list = nicknames.get(s);
            if (list.size() > 1) {
                Collections.sort(list);
                System.out.println(s + " -> " + list);
            }
        }
    }

    static void setNewLogin(List<UserInfo> logins, boolean updateUser) throws Exception {
        Collections.sort(logins);
        int i = 2;
        boolean first = true;
        for (UserInfo user : logins) {
            String newLogin = null;
            if (user.isIllegalLogin()) {
                newLogin = diacriticTool.removeDiacritics(user.getLogin());
                newLogin = reLoginInvalid.subst(newLogin, "");
                if (newLogin.length() == 2)
                    newLogin += i++;
                while (!sqlTool.findUsersWithLogin(newLogin, null).isEmpty()) {
                    newLogin = newLogin + i++;
                }
                user.setNewLogin(newLogin);
            }

            if (first) {
                if (user.getNewLogin() != null && updateUser)
                    updateLogin(user.getId(), user.getNewLogin());
                first = false;
                continue;
            }

            if (newLogin == null) {
                newLogin = user.getLogin() + i++;
                while (!sqlTool.findUsersWithLogin(newLogin, null).isEmpty()) {
                    newLogin = user.getLogin() + i++;
                }
                user.setNewLogin(newLogin);
            }
            if (updateUser)
                updateLogin(user.getId(), user.getNewLogin());
        }
    }

    static void setNewNick(List<UserInfo> nicks, boolean updateUser) throws Exception {
        Collections.sort(nicks);
        int i = 2;
        boolean first = true;
        for (UserInfo user : nicks) {
            if (first) {
                first = false;
                continue;
            }

            String newNick = user.getNick() + i++;
            while (! sqlTool.findUsersWithNick(newNick, null).isEmpty()) {
                newNick = user.getNick() + i++;
            }
            user.setNewNick(newNick);
            if (updateUser)
                updateNick(user.getId(), user.getNewNick());
        }
    }

    private static void updateLogin(int id, String login) throws Exception {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement("update uzivatel set login=? where cislo=?");
            statement.setString(1, login);
            statement.setInt(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Chyba v SQL!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, null);
        }
    }

    private static void updateNick(int id, String nick) throws Exception {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement("update uzivatel set prezdivka=? where cislo=?");
            statement.setString(1, nick);
            statement.setInt(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Chyba v SQL!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, null);
        }
    }

    public static class UserInfo implements Comparable {
        int id;
        String login, nick, newLogin, newNick, email, name;
        Date lastLogin;
        boolean illegalLogin, loginConflict, nickConflict;

        public UserInfo(User user) throws ParseException {
            id = user.getId();
            login = user.getLogin();
            illegalLogin = reLoginInvalid.match(login);
            nick = user.getNick();
            name = user.getName();

            Element element = (Element) user.getData().selectSingleNode("/data/communication/email[@valid='no']");
            if (element == null )
                email = user.getEmail();

            String lastLoginStr = Tools.xpath(user, "/data/system/last_login_date");
            if (lastLoginStr == null)
                lastLogin =  new Date(0);
            else
                lastLogin = Constants.isoFormat.parse(lastLoginStr);
        }

        public String toString() {
            return Integer.toString(id) + " - " + login + " - " + nick;
//            return Integer.toString(id);
//            return "" + id + " (" + lastLoginStr+")";
        }

        public Date getLastLogin() {
            return lastLogin;
        }

        public int getId() {
            return id;
        }

        public String getLogin() {
            return login;
        }

        public String getNick() {
            return nick;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }

        public boolean isIllegalLogin() {
            return illegalLogin;
        }

        public String getNewLogin() {
            return newLogin;
        }

        public void setNewLogin(String newLogin) {
            this.newLogin = newLogin;
        }

        public String getNewNick() {
            return newNick;
        }

        public void setNewNick(String newNick) {
            this.newNick = newNick;
        }

        public boolean isLoginConflict() {
            return loginConflict;
        }

        public void setLoginConflict(boolean loginConflict) {
            this.loginConflict = loginConflict;
        }

        public boolean isNickConflict() {
            return nickConflict;
        }

        public void setNickConflict(boolean nickConflict) {
            this.nickConflict = nickConflict;
        }

        public int compareTo(Object o) {
            UserInfo second = (UserInfo) o;
            Calendar time1 = Calendar.getInstance();
            time1.setTime(getLastLogin());
            Calendar time2 = Calendar.getInstance();
            time2.setTime(second.getLastLogin());

            if (time1.get(Calendar.YEAR) == 2007) {
                if (time2.get(Calendar.YEAR) == 2007)
                    return getId() - second.getId(); // both are active
//                System.out.println("a"+getId()+","+second.getId());
                return -1; // smaller - the first has precedence
            }
            if (time2.get(Calendar.YEAR) == 2007) {
//                System.out.println("b" + getId() + "," + second.getId());
                return 1; // greater - the second has precedence
            }
            return getId() - second.getId(); // both are inactive
        }
    }
}
