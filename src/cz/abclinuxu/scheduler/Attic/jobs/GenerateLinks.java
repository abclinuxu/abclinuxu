/*
 * User: literakl
 * Date: Feb 18, 2002
 * Time: 9:03:52 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.scheduler.jobs;

import cz.abclinuxu.scheduler.Task;
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.data.*;
import cz.abclinuxu.servlets.utils.VelocityHelper;
import cz.abclinuxu.servlets.Constants;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

public class GenerateLinks implements Task {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(GenerateLinks.class);
    static String fileName = "abc.dat";
    static String fileName_anneca = "abc2.dat";
    static RE lineBreak;

    static {
        try {
            lineBreak = new RE("[\r\n$]+",RE.MATCH_MULTILINE);
        } catch (RESyntaxException e) {
            log.error("Cannot create regexp to find line breaks!", e);
        }
    }

    public void runJob() {
        try {
            FileWriter writer = new FileWriter(fileName);
            FileWriter writer2 = new FileWriter(fileName_anneca);
            Persistance persistance = PersistanceFactory.getPersistance();
            VelocityHelper helper = new VelocityHelper();

            writer.write(Constants.isoFormat.format(new Date()));
            writer.write('\n');

            Category actual = (Category) persistance.findById(new Category(Constants.CAT_ACTUAL_ARTICLES));
            helper.sync(actual.getContent());
            List list = helper.sortByDateDescending(actual.getContent());
            for(int i=0; i<4 && i<list.size(); i++ ) {
                Relation relation = (Relation) list.get(i);
                Item item = (Item) relation.getChild();

                writer.write("http://AbcLinuxu.cz/clanky/ViewRelation?relationId="+relation.getId());
                writer2.write("http://AbcLinuxu.cz/clanky/ViewRelation?relationId="+relation.getId());
                writer.write("|\\"+helper.getXPath(item,"data/name")+"\n");
                writer2.write("|"+helper.getXPath(item,"data/name")+"|"+removeNewLines(helper.getXPath(item,"data/perex"))+"\n");
            }

            list = persistance.findByCommand("select cislo from zaznam where typ=1 order by kdy desc limit 2");
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Object[] objects = (Object[]) iter.next();
                Relation child = new Relation(null,new Record(((Integer)objects[0]).intValue(),Record.HARDWARE),0);
                Relation found = persistance.findByExample(child)[0];

                Item item = (Item) found.getParent();
                persistance.synchronize(item);
                writer.write("http://AbcLinuxu.cz/hardware/ViewRelation?relationId="+found.getId());
                writer2.write("http://AbcLinuxu.cz/hardware/ViewRelation?relationId="+found.getId());
                writer.write("|\\H "+helper.getXPath(item,"data/name")+"\n");
                writer2.write("|H "+helper.getXPath(item,"data/name")+"|\n");
            }

            list = persistance.findByCommand("select cislo from zaznam where typ=2 order by kdy desc limit 2");
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Object[] objects = (Object[]) iter.next();
                Relation child = new Relation(null,new Record(((Integer)objects[0]).intValue(),Record.SOFTWARE),0);
                Relation found = persistance.findByExample(child)[0];

                Item item = (Item) found.getParent();
                persistance.synchronize(item);
                writer.write("http://AbcLinuxu.cz/software/ViewRelation?relationId="+found.getId());
                writer2.write("http://AbcLinuxu.cz/software/ViewRelation?relationId="+found.getId());
                writer.write("|\\S "+helper.getXPath(item,"data/name")+"\n");
                writer2.write("|S "+helper.getXPath(item,"data/name")+"|\n");
            }

            list = persistance.findByCommand("select cislo from polozka where typ=5 order by kdy desc limit 2");
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Object[] objects = (Object[]) iter.next();
                Relation child = new Relation(null,new Item(((Integer)objects[0]).intValue(),Item.DRIVER),0);
                Relation found = persistance.findByExample(child)[0];

                Item item = (Item) found.getChild();
                persistance.synchronize(item);
                writer.write("http://AbcLinuxu.cz/drivers/ViewRelation?relationId="+found.getId());
                writer2.write("http://AbcLinuxu.cz/drivers/ViewRelation?relationId="+found.getId());
                writer.write("|\\O "+helper.getXPath(item,"data/name")+"\n");
                writer2.write("|O "+helper.getXPath(item,"data/name")+"|\n");
            }

            writer.close();
            writer2.close();
        } catch (Exception e) {
            log.error("Cannot generate links",e);
        }
    }

    public String getJobName() {
        return "GenerateLinks";
    }

    /**
     * Sets default file name.
     */
    public static void setFileName(String name) {
        fileName = name;
    }

    /**
     * Sets default file name.
     */
    public static void setFileNameAnneca(String name) {
        fileName_anneca = name;
    }

    /**
     * Converts new line characters to spaces.
     */
    String removeNewLines(String text) {
        return lineBreak.subst(text," ");
    }

    public static void main(String[] args) {
        GenerateLinks links = new GenerateLinks();
        links.runJob();
    }
}
