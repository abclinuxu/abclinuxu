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
import java.io.*;

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
        String line = null;

        // place initilizaton here
        BufferedReader in = new BufferedReader(new FileReader("/home/literakl/test.txt"));
        while ( (line=in.readLine())!=null );

        long start = System.currentTimeMillis();
        for (i=0; i<2450; i++) {
            //place your code to measure here
            in = new BufferedReader(new FileReader("/home/literakl/test.txt"));
            while ( (line=in.readLine())!=null );
        }
        long end = System.currentTimeMillis();

        // place clean up here

        float avg = (end-start)/(float)i;
        System.out.println("celkem = "+(end-start)+" ms ,prumer = "+avg+ " ms.");
        org.apache.log4j.Category.getDefaultHierarchy().enableAll();
    }
}
