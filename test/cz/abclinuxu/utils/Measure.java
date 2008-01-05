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
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.cache.TagCache;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.search.CreateIndex;
import cz.abclinuxu.scheduler.UpdateLinks;
import cz.finesoft.socd.analyzer.DiacriticRemover;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.Random;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.io.File;

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
        Date d = null;

        char ch;
        Random random = new Random();
        Tag tag;
        List tags = null;

        // place initilizaton here
        ConcurrentHashMap map = new ConcurrentHashMap(1001, 1.0f, 1);
        for (i = 0; i<1000;i++) {
            ch = (char)('a' + random.nextInt(26));
            s = ch + Integer.toString(i);
            tag = new Tag(s, s);
            tag.setCreated(new Date(107+random.nextInt(4), random.nextInt(11), random.nextInt(28), 2, 2));
            tag.setUsage(random.nextInt(500));
            map.put(s, tag);
        }

        long start = System.currentTimeMillis();
        for (i = 0; i < 100; i++) {
            //place your code to measure here
            tags = new ArrayList(map.values());
//            Collections.sort(tags, new TagCache.TitleComparator(true));
        }
        long end = System.currentTimeMillis();

        // place clean up here
        for (Iterator iterator = tags.iterator(); iterator.hasNext();) {
            Tag tag1 = (Tag) iterator.next();
            System.out.println(tag1.getTitle() + "\t\t" + tag1.getUsage() + "\t\t" + tag1.getCreated() );
        }

        float avg = (end-start)/(float)i;
        System.out.println("celkem = "+(end-start)+" ms, prumer = "+avg+ " ms.");
        LogManager.getRootLogger().setLevel(Level.ALL);
    }
}
