/*
 * User: literakl
 * Date: Dec 10, 2001
 * Time: 8:22:50 AM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.utils;

import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import java.util.Calendar;

/**
 * This class works as template for speed measurement.
 */
public class Measure {

    public static void main(String[] args) throws Exception {
//        DOMConfigurator.configure("conf/log4j.xml");
        LogManager.getRootLogger().setLevel(Level.OFF);
        Persistance persistance = PersistanceFactory.getPersistance();
        int i=0,j=0;
        long l = 0;
        String line = null;

        // place initilizaton here
        Calendar profileLastRun = Calendar.getInstance();
        profileLastRun.add(Calendar.DAY_OF_MONTH,-1);

        long start = System.currentTimeMillis();
        for (i=0; i<250000; i++) {
            //place your code to measure here
            Calendar calendar = Calendar.getInstance();
            int r = calendar.get(Calendar.DAY_OF_MONTH);
            int z = profileLastRun.get(Calendar.DAY_OF_MONTH);
        }
        long end = System.currentTimeMillis();

        // place clean up here

        float avg = (end-start)/(float)i;
        System.out.println("celkem = "+(end-start)+" ms ,prumer = "+avg+ " ms.");
        LogManager.getRootLogger().setLevel(Level.ALL);
    }
}
