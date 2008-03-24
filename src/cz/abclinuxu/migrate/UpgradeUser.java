/*
 *  Copyright (C) 2005 Leos Literak
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
package cz.abclinuxu.migrate;

import cz.abclinuxu.data.User;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.ldap.LdapUserManager;
import cz.abclinuxu.utils.freemarker.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * This utility will upgrade users
 */
public class UpgradeUser {

    /**
     * It does all work. First it finds relevant users, load them into memory
     * and migrates their data property to new XML.
     */
    public static void main(String[] args) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        SQLTool sqlTool = SQLTool.getInstance();
        LdapUserManager ldapMgr = LdapUserManager.getInstance();
        int max = sqlTool.getMaximumUserId(), l = 0;
        String tmp;
        List<User> users = new ArrayList<User>(50);
        Map<String, String> changes = new HashMap<String, String>();
        System.out.println("Found " + max + " users");
        System.out.print("\n" + l + "\t\t");
        long start = System.currentTimeMillis();
        for (int i = 0; i < max; i += 50) {
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_ID, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, 50)};
            List<Integer> uids = sqlTool.findUsers(qualifiers);
            users.clear();
            for (Integer id : uids) {
                users.add(new User(id));
            }
            persistence.synchronizeList(users);

            for (User user : users) {
                changes.clear();
                try {
                    ldapMgr.registerUser(user.getLogin(), user.getPassword(), null, user.getName(), "www.abclinuxu.cz");

                    tmp = Tools.xpath(user, "/data/personal/city");
                    if (tmp != null)
                        changes.put(LdapUserManager.ATTRIB_CITY, tmp);
                    tmp = Tools.xpath(user, "/data/personal/country");
                    if (tmp != null)
                        changes.put(LdapUserManager.ATTRIB_COUNTRY, tmp);
                    tmp = Tools.xpath(user, "/data/profile/home_page");
                    if (tmp != null)
                        changes.put(LdapUserManager.ATTRIB_HOME_PAGE_URL, tmp);
                    tmp = Tools.xpath(user, "/data/system/registration_date");
                    if (tmp == null)
                        tmp = "2003-07-12 00:01";
                    changes.put(LdapUserManager.ATTRIB_REGISTRATION_DATE, tmp);
                    changes.put(LdapUserManager.ATTRIB_REGISTRATION_PORTAL, "www.abclinuxu.cz");
                    changes.put(LdapUserManager.ATTRIB_VISITED_PORTAL, "www.abclinuxu.cz");
                    tmp = Tools.xpath(user, "/data/communication/email[@valid]");
                    if ("no".equals(tmp))
                        changes.put(LdapUserManager.ATTRIB_EMAIL_BLOCKED, "true");
                    else
                        changes.put(LdapUserManager.ATTRIB_EMAIL_BLOCKED, "false");
                    changes.put(LdapUserManager.ATTRIB_EMAIL_VERIFIED, "true");
                    tmp = Tools.xpath(user, "/data/personal/sex");
                    changes.put(LdapUserManager.ATTRIB_SEX, tmp);

                    ldapMgr.updateUser(user.getLogin(), changes);
                    if (true) //todo remove
                        System.exit(1);

                    System.out.print("#");
                    if (++l % 50 == 0)
                        System.out.print("\n" + l + "\t\t");
                } catch (Exception e) {
                    System.err.println("Migration of user " + user.getId() + " failed. Reason: " + e.getMessage());
                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("\n\nMigrated " + l + " users in " + (end-start)/1000 + " seconds");
    }
}
