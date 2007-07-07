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
package cz.abclinuxu.persistence;

import org.dom4j.Document;
import org.dom4j.Node;

import java.util.List;
import java.util.ArrayList;

import cz.abclinuxu.data.User;

/**
 * Tests speed of DOM4J's selectSingleNode() method.
 * 14.8.2003: DOM4J 1.4 - testing first 1000 users took 213 ms,
 *            one invocation of selectSingleNode took 0.213 ms.
 * Conclusion: it makes no sense to cache XPath execution result.
 */
public class TestXPathSpeed {
    public static void main(String[] args) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = null; Document data = null; Node node = null; int total = 1000;
        List users = new ArrayList(total);
        for (int i=1; users.size()<total; i++) {
            try {
                user = (User) persistence.findById(new User(i));
                users.add(user);
                data = user.getData(); // forces lazy init to happen now
            } catch (Exception e) { /* user doesn't exist, skip it */}
        }
        node = data.selectSingleNode("/data/settings/emoticons");

        int i = 0;
        long start = System.currentTimeMillis();
        for ( i = 0; i<total; i++ ) {
            //place your code to measure here
            data = ((User) users.get(i)).getData();
            node = data.selectSingleNode("/data/settings/emoticons");
        }
        long end = System.currentTimeMillis();

        if (node!=null)
            System.out.println("Value="+node.getText());

        float avg = (end-start)/(float) i;
        System.out.println("celkem = "+(end-start)+" ms ,prumer = "+avg+" ms.");
    }
}
