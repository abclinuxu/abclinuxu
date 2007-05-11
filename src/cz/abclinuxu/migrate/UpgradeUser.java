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
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.edit.EditUser;

import java.util.List;
import java.util.Collections;

/**
 * This utility will upgrade users
 */
public class UpgradeUser {

    /**
     * It does all work. First it finds relevant users, load them into memory
     * and migrates their data property to new XML.
     */
    public static void main(String[] args) throws Exception {
        SQLTool sqlTool = SQLTool.getInstance();
        System.out.print("Starting to search for users ..");
        int max = sqlTool.getMaximumUserId();
        for (int i = 0; i < max; i += 50) {
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_ID, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, 50)};
            List<Integer> users = sqlTool.findUsers(qualifiers);
            for (Integer id : users) {
                String ticket = EditUser.generateTicket(id);
                sqlTool.setProperty(new User(id), Constants.PROPERTY_TICKET, Collections.singleton(ticket));
            }
        }
        System.out.println(" done");
    }
}
