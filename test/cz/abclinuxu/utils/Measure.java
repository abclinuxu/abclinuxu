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
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * This class works as template for speed measurement.
 */
public class Measure {

    public static void main(String[] args) throws Exception {
//        DOMConfigurator.configure("conf/log4j.xml");
        LogManager.getRootLogger().setLevel(Level.OFF);
        int i=0,j=0;
        long l = 0;

        // place initilizaton here
        Document doc = DocumentHelper.createDocument();
        Element monitor = doc.addElement("monitor","monitor");
        for(i=0;i<100;i++)
            monitor.addElement("id").setText(new Integer(i).toString());
        Element cloned = monitor.createCopy();

        long start = System.currentTimeMillis();
        for (i=0; i<4000; i++) {
            //place your code to measure here
            cloned = monitor.createCopy();
        }
        long end = System.currentTimeMillis();

        // place clean up here

        float avg = (end-start)/(float)i;
        System.out.println("celkem = "+(end-start)+" ms ,prumer = "+avg+ " ms.");
        LogManager.getRootLogger().setLevel(Level.ALL);
    }
}
