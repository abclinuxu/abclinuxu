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
import cz.abclinuxu.utils.freemarker.Tools;

import java.util.List;

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
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation dizRel = (Relation) persistance.findById(new Relation(62819));
        Item diz = (Item) dizRel.getChild();
        Tools tools = new Tools();
        tools.sync(diz);
        tools.createDiscussionTree(diz);

        long start = System.currentTimeMillis();
        for (i=0; i<4000; i++) {
            //place your code to measure here
            tools.createDiscussionTree(diz);
        }
        long end = System.currentTimeMillis();

        // place clean up here

        float avg = (end-start)/(float)i;
        System.out.println("celkem = "+(end-start)+" ms, prumer = "+avg+ " ms.");
        LogManager.getRootLogger().setLevel(Level.ALL);
    }
}
