/*
 *  Copyright (C) 2006 Leos Literak
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

import cz.abclinuxu.utils.LRUMap;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.view.Search;
import cz.abclinuxu.exceptions.PersistenceException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Date;

/**
 * Reads search log and stores its content into database table.
 * @author literakl
 * @since 14.10.2006
 */
public class MigrateSearchLog {
    final static long MINIMUM_LIMIT = 1000 * 60 * 5; // 5 minutes

    public static void main(String[] args) throws Exception {
        LRUMap map = new LRUMap(20);
        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        String line, query;
        Date date, previous;
        int position, lineNo = 0;
        while ((line = reader.readLine()) != null) {
            position = line.indexOf('|');
            if (position == -1)
                continue;

            query = line.substring(position + 2);
            date = Constants.isoFormat.parse(line.substring(0, position - 5));
            previous = (Date) map.get(query);
            map.put(query, date);
            if ( previous != null && (date.getTime() - previous.getTime() < MINIMUM_LIMIT))
                continue; // user probably clicked on next page of results

            try {
                Search.logSearch(query);
            } catch (PersistenceException e) {
                System.err.println("chyba na radce "+lineNo+": "+line);
            }
            lineNo++;
        }
    }
}
