/*
 * User: Leos Literak
 * Date: Dec 8, 2002
 * Time: 11:38:58 AM
 */
package cz.abclinuxu.utils;

import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.util.*;


/**
 * This class is used to analyze survey's output.
 */
public class AnalyzeSurveyXML {
    static String TOTAL = "TOTAL";

    Map data = new HashMap(50);
    SAXReader reader = new SAXReader();
    /** contains names of already processed options in this file */
    List processedOptions = new ArrayList(30);

    public static void main(String[] args) throws Exception {
        AnalyzeSurveyXML analyzer = new AnalyzeSurveyXML();
        List files = analyzer.getFiles();
        System.out.println("Starting to process "+files.size()+" files.");
        for (Iterator iter = files.iterator(); iter.hasNext();) {
            File file = (File) iter.next();
            analyzer.processFiles(file);
        }
        System.out.println("Finished processing.");
        analyzer.generateOutput();
        System.out.println("Output written.");
    }

    /**
     * Processes one xml file, affecting data map.
     */
    void processFiles(File f) throws Exception {
        Document d = reader.read(f);
        processedOptions.clear();
        List nodes = d.selectNodes("/*/*/*");

        for (Iterator iter = nodes.iterator(); iter.hasNext();) {
            Node node = (Node) iter.next();
            String name = node.getName();
            String value = node.getText();
            if ( isOption(name) ) {
                processOption(name,value);
            } else {
                processText(name,value);
            }
        }
    }

    /**
     * Processes option tag. It stores tag's value to map
     * as key incrementing its counter. If tag hasn't been processed
     * yet, it also increments its TOTAL counter.
     */
    void processOption(String name, String value) {
        Map options = (Map) data.get(name);
        if ( options==null ) {
            options = new HashMap(10);
            data.put(name,options);
            options.put(TOTAL,new Integer(0));
        }

        if ( ! processedOptions.contains(name) ) {
            Integer counter = (Integer) options.get(TOTAL);
            counter = new Integer(counter.intValue()+1);
            options.put(TOTAL,counter);
            processedOptions.add(name);
        }

        Integer counter = (Integer) options.get(value);
        if ( counter==null ) {
            counter = new Integer(1);
        } else {
            counter = new Integer(counter.intValue()+1);
        }
        options.put(value,counter);
    }

    /**
     * Processes text tag. It adds tag's value to
     * list stored under tag's name.
     */
    void processText(String name, String value) {
        List values = (List) data.get(name);
        if ( values==null ) {
            values = new ArrayList();
            data.put(name,values);
        }
        values.add(value);
    }

    /**
     * @return true, if this tag is option (has fixed value).
     */
    boolean isOption(String name) {
        if ( "vzkaz".equals(name) )
            return false;
        if ( "sluzby_chybi".equals(name) )
            return false;
        return true;
    }

    /**
     * @return list of files to be processed
     */
    List getFiles() {
//        List list = new ArrayList();
//        list.add(new File("/home/literakl/tmp/new/abc1/data/answear_27929.xml"));
//        list.add(new File("/home/literakl/tmp/new/abc1/data/answear_27926.xml"));
        File dir = new File("/home/literakl/tmp/new/abc1/data");
        File[] files = dir.listFiles();
        List list = Arrays.asList(files);
        return list;
    }

    /**
     * Prints statistics.
     */
    void generateOutput() throws Exception {
        PrintStream psOption = new PrintStream(new FileOutputStream("options.csv"));
        PrintStream psTexts = new PrintStream(new FileOutputStream("texts.txt"));

        for (Iterator iter = data.keySet().iterator(); iter.hasNext();) {
            String tag = (String) iter.next();
            if ( isOption(tag) ) {
                Map values = (Map) data.get(tag);
                List counters = new ArrayList();
                List keys = new ArrayList(values.keySet());
                Collections.sort(keys, new TotalComparator());

                psOption.println(tag);
                for (Iterator iter2 = keys.iterator(); iter2.hasNext();) {
                    String s = (String) iter2.next();
                    psOption.print(s+" ; ");
                    counters.add(values.get(s));
                }
                psOption.println();
                for (Iterator iter2 = counters.iterator(); iter2.hasNext();) {
                    Integer i = (Integer) iter2.next();
                    psOption.print(i+" ; ");
                }
                psOption.println("\n\n");
            } else {
                List values = (List) data.get(tag);
                psTexts.println("<"+tag+">");
                for (Iterator iter2 = values.iterator(); iter2.hasNext();) {
                    String s = (String) iter2.next();
                    psTexts.println(s);
                    if ( iter2.hasNext() )
                        psTexts.println("-----");
                }
                psTexts.println("</"+tag+">\n");
            }
        }
        psOption.close();
        psTexts.close();
    }

    /**
     * Sorts lexicografically with one exception: Total is always smallest.
     */
    class TotalComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            if ( TOTAL.equals(o1) ) return -1;
            if ( TOTAL.equals(o2) ) return 1;
            return ((String)o1).compareTo(o2);
        }
    }
}
