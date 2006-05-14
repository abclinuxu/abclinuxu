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

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import cz.abclinuxu.data.*;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.extra.Qualifier;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.scheduler.UpdateLinks;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Map;

/**
 * This class works as template for speed measurement.
 */
public class Measure {

    public static void main(String[] args) throws Exception {
//        DOMConfigurator.configure("conf/log4j.xml");
        LogManager.getRootLogger().setLevel(Level.OFF);
        int i=0,j=0,k=0;
        long l = 0;
        String s,t;

        // place initilizaton here
        Persistance persistance = PersistanceFactory.getPersistance();
        s = "\u0000qwert\u001Btyuiopa\n\tsdfghjklzxc\u0018vvbnm,./';[\\]=-0987654321"; t = s;
        System.out.println(s.length()+": "+s);

        long start = System.currentTimeMillis();
        for (i=0; i<41000; i++) {
            //place your code to measure here
            t = s.replaceAll("[\\x00-\\x08\\x0B-\\x0C\\x0E-\\x1f]", "");
        }
        long end = System.currentTimeMillis();
        System.out.println(t.length() + ": " + t);

        // place clean up here

        float avg = (end-start)/(float)i;
        System.out.println("celkem = "+(end-start)+" ms, prumer = "+avg+ " ms.");
        LogManager.getRootLogger().setLevel(Level.ALL);
    }
}
