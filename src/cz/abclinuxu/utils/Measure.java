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
        DOMConfigurator.configure("WEB-INF/log4j.xml");
        Persistance persistance = PersistanceFactory.getPersistance();
        int  i=0,j=0;

        // place initilizaton here
        User user = new User();
        user.setId(1);
        Record record = new HardwareRecord();
        record.setOwner(1);
        boolean admin=false;

        long start = System.currentTimeMillis();
        for (i=0; i<30000; i++) {
            //place your code to measure here
            admin = record.isManagedBy(user);
        }
        long end = System.currentTimeMillis();

        // place clean up here
        System.out.println("admin = " + admin);

        float avg = (end-start)/(float)i;
        System.out.println("celkem = "+(end-start)+" ,prumer = "+avg);
    }
}
