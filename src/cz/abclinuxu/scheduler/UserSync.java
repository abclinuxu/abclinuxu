/*
 *  Copyright (C) 2008 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
package cz.abclinuxu.scheduler;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.persistence.ldap.LdapUserManager;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.data.User;
import static cz.abclinuxu.persistence.ldap.LdapUserManager.*;

import java.util.TimerTask;
import java.util.Map;
import java.util.List;
import java.util.Date;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.prefs.Preferences;
import java.io.File;
import java.text.ParseException;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;

/**
 * This class is responsible in finding all externally modified users and merging changes into database.
 * It starts to search from last modification time of run file or one day back, if the file is missing.
 * Once finished, it
 * @author literakl
 * @since 16.8.2008
 */
public class UserSync extends TimerTask implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserSync.class);

    public static final String PREF_PATH = "path";
    public static final String PREF_LAST_RUN_NAME = "last.run.file";
    private final String LAST_RUN_FILE = "ldap_sync.txt";

    public static final String SF_USER_ALL_ATTRIBUTES[] = {
        ATTRIB_CITY, ATTRIB_COUNTRY, ATTRIB_EMAIL_ADRESS, ATTRIB_EMAIL_BLOCKED, ATTRIB_EMAIL_VERIFIED,
        ATTRIB_HOME_PAGE_URL, ATTRIB_LAST_CHANGE_DATE, ATTRIB_LOGIN, ATTRIB_NAME, ATTRIB_OPEN_ID, ATTRIB_SEX
    };

    static String runFilePath, lastRunFilename;
    static {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(new UserSync());
    }

    public void run() {
        if (log.isDebugEnabled()) log.debug("Starting task " + getJobName());

        try {
            Date syncSince;
            File file = new File(runFilePath, lastRunFilename);
            if (!file.exists()) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                syncSince = calendar.getTime();
            } else
                syncSince = new Date(file.lastModified());

            String time, login;
            synchronized(Constants.isoLongFormat) {
                time = Constants.isoLongFormat.format(syncSince);
            }

            SQLTool sqlTool = SQLTool.getInstance();
            LdapUserManager mgr = LdapUserManager.getInstance();
//            List<Map<String, String>> users = mgr.search("(modifytimestamp>=" + time + ")", SF_USER_ALL_ATTRIBUTES);
            String query = "(" + ATTRIB_LAST_CHANGE_DATE + "=" + time + ")";
            List<Map<String, String>> ldapUsers = mgr.search(query, SF_USER_ALL_ATTRIBUTES);
            long syncTime = System.currentTimeMillis();
            while (! ldapUsers.isEmpty()) {
                int i = 0;
                Map<String, Map<String, String>> batchUsers = new HashMap<String, Map<String, String>>();
                List<String> dbUsers = new ArrayList<String>();
                for (Iterator<Map<String, String>> iter = ldapUsers.iterator(); i < 50 && iter.hasNext(); i++) {
                    Map<String, String> map = iter.next();
                    iter.remove();
                    login = map.get(ATTRIB_LOGIN);
                    batchUsers.put(login, map);
                    dbUsers.add(login);
                }

                List<Integer> userIds = sqlTool.findUsersByLogins(dbUsers);
                List<User> users = Tools.createUsers(userIds);
                for (User user : users) {
                    Map<String, String> ldapAttributes = batchUsers.get(user.getLogin());
                    if ( ! isSyncObsolete(user, ldapAttributes))
                        continue;
                    syncUser(user, ldapAttributes);
                }
            }

            Misc.touchFile(file, syncTime);
        } catch (Exception e) {
            log.warn("UserSync failed", e);
        }

        if (log.isDebugEnabled()) log.debug("Task " + getJobName() + " has finished its job.");
    }

    /**
     * Updates user to match values of LDAP properties. It is mandatory to use SF_USER_ALL_ATTRIBUTES
     * when fetching data otherwise missing attributes will be considered having empty value.
     * @param user initialized user
     * @param ldapAttributes attributes
     */
    public static void syncUser(User user, Map<String, String> ldapAttributes) {
        Document doc = user.getData();
        Element elPersonal = DocumentHelper.makeElement(doc, "/data/personal");
//        "sex"
    }

    /**
     * @return true, if user has no lastSynced property or its value is older than ATTRIB_LAST_CHANGE_DATE.
     */
    private boolean isSyncObsolete(User user, Map<String, String> ldapAttributes) {
        Date lastSynced = user.getLastSynced();
        if (lastSynced == null)
            return true;

        String sDate = ldapAttributes.get(ATTRIB_LAST_CHANGE_DATE);
        if (sDate == null) // this shall not happen
            return false;
        Date lastChanged;
        try {
            synchronized (Constants.isoLongFormat) {
                lastChanged = Constants.isoLongFormat.parse(sDate);
            }
        } catch (ParseException e) {
            log.warn("Failed to parse lastChangeDate attribute with value '" + sDate + "' to date!");
            return false;
        }

        return (lastSynced.compareTo(lastChanged) < 0);
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        runFilePath = prefs.get(PREF_PATH, null);
        lastRunFilename = prefs.get(PREF_LAST_RUN_NAME, LAST_RUN_FILE);
    }

    public static void main(String[] args) {
        UserSync instance = new UserSync();
        instance.run();
    }

    public String getJobName() {
        return "UserLdapSync";
    }
}
