package cz.abclinuxu.migrate;

import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.data.User;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.servlets.Constants;

import java.util.*;
import java.text.ParseException;

/**
 * Tool for findg users which has conflict in login / nickname with other users,
 * when case insensitive search is performed.
 * User: literakl
 * Date: 18.2.2007
 */
public class FindCollidingUsers {
    public static void main(String[] args) throws Exception {
        SQLTool sqlTool = SQLTool.getInstance();
        Persistence persistence = PersistenceFactory.getPersistence();

        int max = sqlTool.getMaximumUserId();
        Map<String, List> logins = new HashMap<String, List>(max, 0.99f);
        Map<String, List> nicknames = new HashMap<String, List>(max, 0.99f);

        User user;
        for (int i = 1; i < max; i++) {
            try {
                user = (User) persistence.findById(new User(i));
            } catch (Exception e) {
                continue;
            }

            String login = user.getLogin().toLowerCase();
            List list = logins.get(login);
            if (list != null) {
                if (! (list instanceof ArrayList)) {
                    list = new ArrayList(list);
                    logins.put(login, list);
                }

                list.add(new UserInfo(user));
            } else
                logins.put(login, Collections.singletonList(new UserInfo(user)));

            String nick = user.getNick();
            if (nick != null) {
                nick = nick.toLowerCase();
                list = nicknames.get(nick);
                if (list != null) {
                    if (! (list instanceof ArrayList)) {
                        list = new ArrayList(list);
                        nicknames.put(nick, list);
                    }

                    list.add(new UserInfo(user));
                } else
                    nicknames.put(nick, Collections.singletonList(new UserInfo(user)));

            }
        }

        System.out.println("Conflicting logins:");
        for (Iterator<String> iter = logins.keySet().iterator(); iter.hasNext();) {
            String s = iter.next();
            List list = logins.get(s);
            if (list.size() > 1) {
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

    private static class UserInfo implements Comparable {
        int id;
        String lastLoginStr;
        Date lastLogin;

        public UserInfo(User user) {
            id = user.getId();
            lastLoginStr = Tools.xpath(user, "/data/system/last_login_date");
        }

        public String toString() {
            return Integer.toString(id);
//            return "" + id + " (" + lastLoginStr+")";
        }

        public Date getLastLogin() throws ParseException {
            if (lastLoginStr == null)
                return new Date(0);
            lastLogin = Constants.isoFormat.parse(lastLoginStr);
            return lastLogin;
        }

        public int compareTo(Object o) {
            UserInfo second = (UserInfo) o;
            try {
                return -1 * getLastLogin().compareTo(second.getLastLogin());
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        }
    }
}
