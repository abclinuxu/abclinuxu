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
import org.apache.log4j.xml.DOMConfigurator;

/**
 * This class works as template for speed measurement.
 */
public class Measure {

    public static void main(String[] args) throws Exception {
        DOMConfigurator.configure("conf/log4j.xml");
        Persistance persistance = PersistanceFactory.getPersistance();
        int i=0,j=0;
        long l = 0;
        String str = "92032";

        // place initilizaton here
        User user = new User();
        user.setId(1);

        long start = System.currentTimeMillis();
        for (i=0; i<1000000; i++) {
            //place your code to measure here
            l = System.currentTimeMillis();
        }
        long end = System.currentTimeMillis();

        // place clean up here
        System.out.println("l = " + l);

        float avg = (end-start)/(float)i;
        System.out.println("celkem = "+(end-start)+" ,prumer = "+avg);
    }
}
