/*
 * User: Leos Literak
 * Date: Oct 30, 2002
 * Time: 8:03:50 AM
 */
package cz.abclinuxu.persistance;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import java.util.List;

/**
 * Tests, how fast can DOM4J build tree.
 * Result: long Article 10381 takes 9 ms to be parsed (106 iterations per second)
 * Result: short question 10323 takes 6 ms (156 iterations per second)
 * Conclusion: syncing category with 20 discussions (each having 5 responses) takes at least 720 ms
 */
public class TestDOM4JSpeed {

    public static void main(String[] args) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();

        List objects = persistance.findByCommand("select data from zaznam where cislo=6715"); // R: 10381
//        List objects = persistance.findByCommand("select data from polozka where cislo=2999"); // R: 10323
        String str = (String) ((Object[])objects.get(0))[0];

        Document document = DocumentHelper.parseText(str); // initialize all DOM4J internal stuff

        int i = 0;
        long start = System.currentTimeMillis();
        for (i=0; i<106; i++) {
            //place your code to measure here
            document = DocumentHelper.parseText(str);
        }

        long end = System.currentTimeMillis();
        float avg = (end-start)/(float)i;
        System.out.println("celkem = "+(end-start)+" ms ,prumer = "+avg+ " ms.");
    }
}
