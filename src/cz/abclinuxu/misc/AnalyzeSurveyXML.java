/*
 *  Copyright (C) 2005 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
package cz.abclinuxu.misc;

import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.*;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.utils.freemarker.FMUtils;


/**
 * This class is used to analyze survey's output.
 */
public class AnalyzeSurveyXML {
    static String TOTAL = "TOTAL";
    SAXReader reader = new SAXReader();

    static Map options = new HashMap(30);

    Map data = new HashMap(20);

    public static void main(String[] args) throws Exception {
        if ( args.length!=1 ) {
            System.err.println("Chybi parametr - cislo polozky s anketou!");
            System.exit(1);
        }
        int id = Integer.parseInt(args[0]);
        Item survey = (Item) PersistenceFactory.getPersistence().findById(new Item(id));
        if (survey.getType()!=Item.SURVEY) {
            System.err.println("Tato polozka neni anketou!");
            System.exit(1);
        }

        List tagDumps = survey.getData().selectNodes("//dump");
        if ( tagDumps==null || tagDumps.size()==0 )  {
            System.err.println("Anketa neobsahuje zadnou znacku dump!");
            System.exit(1);
        }

        List tagOptions = survey.getData().selectNodes("//choice");
        if ( tagOptions!=null && tagOptions.size()>0 ) {
            for ( Iterator iter = tagOptions.iterator(); iter.hasNext(); ) {
                String option = ((Node) iter.next()).getText();
                options.put(option, option);
            }
        }

        AnalyzeSurveyXML analyzer = new AnalyzeSurveyXML();
        for ( Iterator iter = tagDumps.iterator(); iter.hasNext(); ) {
            Element dump = (Element) iter.next();
            String screenId = dump.getParent().attributeValue("id");
            analyzer.processDump(dump, screenId);
        }
    }

    /**
     * Processes given dump.
     */
    void processDump(Element dump, String id) throws Exception {
        String dir = dump.element("dir").getTextTrim();
        String prefix = dump.element("prefix").getTextTrim();
        File directory = new File(FMUtils.getTemplatesDir(), "web/ankety/" + dir);
        List files = getFiles(directory, prefix);

        data.clear();
        System.out.println("Processing dump of screen "+id+", "+files.size()+" files.");
        for ( Iterator iter = files.iterator(); iter.hasNext(); ) {
            File file = (File) iter.next();
            processFile(file);
        }
        System.out.println("Finished processing.");
        generateOutput(prefix);
        System.out.println("Output written.");
    }

    /**
     * @return list of files to be processed
     */
    List getFiles(File directory, String prefix) {
        FilenameFilter filter = new PrefixFileNameFilter(prefix);
        File[] files = directory.listFiles(filter);
        List list = Arrays.asList(files);
        return list;
    }

    /**
     * Processes one xml file, affecting data map.
     */
    void processFile(File f) throws Exception {
        Document d = null;
        try {
            d = reader.read(f);
        } catch (DocumentException e) {
            System.out.println(f.getName());
            throw e;
        }
        List processedOptions = new ArrayList(30);
        List nodes = d.selectNodes("/*/*/*");

        for (Iterator iter = nodes.iterator(); iter.hasNext();) {
            Node node = (Node) iter.next();
            String name = node.getName();
            String value = node.getText();
            if ( isOption(name) ) {
                processOption(name,value, processedOptions);
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
    void processOption(String name, String value, List processedOptions) {
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
        return options.get(name)!=null;
    }

    /**
     * Prints statistics.
     */
    void generateOutput(String prefix) throws Exception {
        PrintStream psOption = new PrintStream(new FileOutputStream(prefix+"options.csv"));
        PrintStream psTexts = new PrintStream(new FileOutputStream(prefix+"texts.txt"));

        for (Iterator iter = data.keySet().iterator(); iter.hasNext();) {
            String tag = (String) iter.next();
            if ( isOption(tag) ) {
                Map values = (Map) data.get(tag);
                List keys = new ArrayList(values.keySet());
                Collections.sort(keys, new TotalComparator());

                psOption.println(tag);
                for (Iterator iter2 = keys.iterator(); iter2.hasNext();) {
                    String s = (String) iter2.next();
                    psOption.print(s);
                    psOption.print(" ; ");
                    psOption.print(values.get(s));
                    psOption.println();
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
            return ((String)o1).compareTo((String) o2);
        }
    }

    /**
     * Selects only files, whose names start with given prefix.
     */
    class PrefixFileNameFilter implements FilenameFilter {
        String prefix;

        public PrefixFileNameFilter(String prefix) {
            this.prefix = prefix;
        }

        public boolean accept(File dir, String name) {
            return name.startsWith(prefix);
        }
    }
}
