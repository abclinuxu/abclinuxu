/*
 * User: literakl
 * Date: Dec 10, 2001
 * Time: 8:22:50 AM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.utils;

import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.data.*;
import cz.abclinuxu.servlets.utils.VelocityHelper;
import org.apache.log4j.xml.DOMConfigurator;

import java.util.List;

/**
 * This class works as template for speed measurement.
 */
public class Measure {

    public static void main(String[] args) throws Exception {
        DOMConfigurator.configure("conf/log4j.xml");
        org.apache.log4j.Category.getDefaultHierarchy().disableAll();
        Persistance persistance = PersistanceFactory.getPersistance();
        int i=0,j=0;
        long l = 0;

        // place initilizaton here
        List list = persistance.findByCommand("select cislo from zaznam where typ=1 order by kdy desc limit 3");

        long start = System.currentTimeMillis();
        for (i=0; i<100; i++) {
            //place your code to measure here
            list = persistance.findByCommand("select cislo from zaznam where typ=1 order by kdy desc limit 3");
        }
        long end = System.currentTimeMillis();

        // place clean up here

        float avg = (end-start)/(float)i;
        System.out.println("celkem = "+(end-start)+" ,prumer = "+avg);
        org.apache.log4j.Category.getDefaultHierarchy().enableAll();
    }
}
