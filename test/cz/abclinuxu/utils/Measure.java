/*
 * User: literakl
 * Date: Dec 10, 2001
 * Time: 8:22:50 AM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
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

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Date;

/**
 * This class works as template for speed measurement.
 */
public class Measure {

    public static void main(String[] args) throws Exception {
//        DOMConfigurator.configure("conf/log4j.xml");
        LogManager.getRootLogger().setLevel(Level.OFF);
        int i=0,j=0,k=0;
        long l = 0;

        // place initilizaton here
        Persistance persistance = PersistanceFactory.getPersistance();
        List list = new ArrayList(20);
        for(i=0; i<20; i++)
            list.add(new Relation(i));
        List subList;
        j = 5;

        long start = System.currentTimeMillis();
        for (i=0; i<1700000; i++) {
            //place your code to measure here
            subList = new ArrayList(j);
            for (k = 0; k<j; k++)
                subList.add(list.get(k));
        }
        long end = System.currentTimeMillis();

        // place clean up here

        float avg = (end-start)/(float)i;
        System.out.println("celkem = "+(end-start)+" ms, prumer = "+avg+ " ms.");
        LogManager.getRootLogger().setLevel(Level.ALL);
    }
}
