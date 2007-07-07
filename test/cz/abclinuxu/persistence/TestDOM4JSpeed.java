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
import org.dom4j.DocumentHelper;

import java.util.List;

/**
 * Tests, how fast can DOM4J build tree.
 * Result: long Article 10381 takes 9 ms to be parsed (106 iterations per second)
 * Result: short question 10323 takes 6 ms (156 iterations per second)
 * Conclusion: syncing category with 20 discussions (each having 5 responses) takes at least 720 ms
 */
public class TestDOM4JSpeed {

    public static void main(String[] args) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();

        List objects = persistence.findByCommand("select data from zaznam where cislo=6715"); // R: 10381
//        List objects = persistence.findByCommand("select data from polozka where cislo=2999"); // R: 10323
        String str = (String) ((Object[])objects.get(0))[0];

        Document document = DocumentHelper.parseText(str); // initialize all DOM4J internal stuff

        int i = 0;
        long start = System.currentTimeMillis();
        for (i=0; i<106; i++) {
            //place your code to measure here
            document = DocumentHelper.parseText(str);
        }

        long end = System.currentTimeMillis();
        float avg = (end-start)/(float)i;
        System.out.println("celkem = "+(end-start)+" ms ,prumer = "+avg+ " ms.");
    }
}
