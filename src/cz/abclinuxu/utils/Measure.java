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

/**
 * This class works as template for speed measurement.
 */
public class Measure {

    public static void main(String[] args) throws Exception {
        DOMConfigurator.configure("conf/log4j.xml");
        org.apache.log4j.Category.getDefaultHierarchy().disableAll();
        Persistance persistance = PersistanceFactory.getPersistance("jdbc:mysql://localhost/unit?user=literakl");
        int i=0,j=0;
        long l = 0;
        String str = "92032";

        // place initilizaton here
        User user = new User();
        user.setId(1);
        user.setData("<data><name>Leos Literak</name></data>");
        user.setInitialized(true);

        str = VelocityHelper.getXPath(user,"data/name"); // to load all libraries
        long start = System.currentTimeMillis();
        for (i=0; i<3000; i++) {
            //place your code to measure here
            str = user.getData().selectSingleNode("data/name").getText(); // 0.311 second
            str = VelocityHelper.getXPath(user,"data/name"); // 0.314 second
        }
        long end = System.currentTimeMillis();

        // place clean up here
        System.out.println("str = " + str);

        float avg = (end-start)/(float)i;
        System.out.println("celkem = "+(end-start)+" ,prumer = "+avg);
        org.apache.log4j.Category.getDefaultHierarchy().enableAll();
    }
}
