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
package cz.abclinuxu.utils;

import org.apache.regexp.RE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Comparation of speed of jakarta ORO and java regexps.
 * Jakarta ORO - 156 000 loops per second with 4 matches
 * Java regexps - 58 000 loops per second with 4 matches
 */
public class Regexps {
    public static final String URL = "http://www.abclinuxu.cz/hardware/ViewRelation";
    public static final String URL2 = "http://abclinuxu.cz/hardware/ViewRelation?rid=1234";
    public static final String URL3 = "http://abclinuxu.cz/Profile";
    public static final String URL4 = "http://abclinuxu.cz/Index";

    public static void main(String[] args) throws Exception {
        int i = 0;
        boolean b;

        Pattern pattern = Pattern.compile(".*ViewRelation");
        Matcher matcher = pattern.matcher(URL);
        System.out.println(matcher.matches());

//        RE re = new RE("ViewRelation");
//        System.out.println(re.match(URL));

        long start = System.currentTimeMillis();
        for ( i = 0; i<58000; i++ ) {
            matcher = pattern.matcher(URL);
            b = matcher.matches();
            matcher = pattern.matcher(URL2);
            b = matcher.matches();
            matcher = pattern.matcher(URL3);
            b = matcher.matches();
            matcher = pattern.matcher(URL4);
            b = matcher.matches();

//            b = re.match(URL);
//            b = re.match(URL2);
//            b = re.match(URL3);
//            b = re.match(URL4);
        }
        long end = System.currentTimeMillis();

        float avg = (end-start)/(float) i;
        System.out.println("celkem = "+(end-start)+" ms ,prumer = "+avg+" ms.");
    }
}
