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
package cz.abclinuxu.persistence.ldap;

import junit.framework.TestCase;
import static cz.abclinuxu.persistence.ldap.LdapUserManager.*;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.data.User;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author literakl
 * @since 16.8.2008
 */
public class TestLdapManager extends TestCase {
    String[] SHORT_PROPERTIES = new String[] {ATTRIB_LOGIN, ATTRIB_EMAIL_ADRESS};

    Persistence persistence = PersistenceFactory.getPersistence();
    LdapUserManager mgr = LdapUserManager.getInstance();
    SQLTool sqlTool = SQLTool.getInstance();

    public void testLoad() throws Exception {
        final int userCount = 100;
        final int threads = 5;

        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_ID, Qualifier.ORDER_ASCENDING, new LimitQualifier(0, userCount)};
        List<Integer> uids = sqlTool.findUsers(qualifiers);
        List<User> users = new ArrayList<User>(userCount);
        for (Integer id : uids) {
            users.add(new User(id));
        }
        persistence.synchronizeList(users);

        final List<String> logins = new ArrayList<String>(userCount);
        for (User user : users) {
            logins.add(user.getLogin());
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < threads; i++) {
            new Thread(new Runnable() {
                public void run() {
                    List<String> emails = new ArrayList<String>(userCount);
                    for (String login : logins) {
                        Map<String, String> found = mgr.getUserInformation(login, SHORT_PROPERTIES);
                        emails.add(found.get(ATTRIB_EMAIL_ADRESS));
                    }
                }
            }).run();
        }
        long end = System.currentTimeMillis();
        System.out.println("\n\nFetch of " + userCount + " users in " +threads + " took " + (end - start) + " milliseconds");
    }
}
