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

public class GenerateLinks implements Task {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(GenerateLinks.class);
    static String fileName = "abc.dat";

    public void runJob() {
        try {
            FileWriter writer = new FileWriter(fileName);
            Persistance persistance = PersistanceFactory.getPersistance();
            VelocityHelper helper = new VelocityHelper();

            writer.write(Constants.isoFormat.format(new Date()));
            writer.write('\n');

            Category actual = (Category) persistance.findById(new Category(Constants.CAT_ACTUAL_ARTICLES));
            helper.sync(actual.getContent());
            List list = helper.sortByDateDescending(actual.getContent());
            for(int i=0; i<2 && i<list.size(); i++ ) {
                Relation relation = (Relation) list.get(i);
                Item item = (Item) relation.getChild();

                writer.write("http://AbcLinuxu.cz/clanky/ViewRelation?relationId="+relation.getId());
                writer.write("|\\");
                writer.write(helper.getXPath(item,"data/name"));
                writer.write('\n');
            }

            list = persistance.findByCommand("select cislo from zaznam where typ=1 order by kdy desc limit 2");
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Object[] objects = (Object[]) iter.next();
                Relation child = new Relation(null,new Record(((Integer)objects[0]).intValue(),Record.HARDWARE),0);
                Relation found = persistance.findByExample(child)[0];

                Item item = (Item) found.getParent();
                persistance.synchronize(item);
                writer.write("http://AbcLinuxu.cz/hardware/ViewRelation?relationId="+found.getId());
                writer.write("|\\");
                writer.write(helper.getXPath(item,"data/name"));
                writer.write('\n');
            }

            list = persistance.findByCommand("select cislo from zaznam where typ=2 order by kdy desc limit 2");
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Object[] objects = (Object[]) iter.next();
                Relation child = new Relation(null,new Record(((Integer)objects[0]).intValue(),Record.SOFTWARE),0);
                Relation found = persistance.findByExample(child)[0];

                Item item = (Item) found.getParent();
                persistance.synchronize(item);
                writer.write("http://AbcLinuxu.cz/software/ViewRelation?relationId="+found.getId());
                writer.write("|\\");
                writer.write(helper.getXPath(item,"data/name"));
                writer.write('\n');
            }

            list = persistance.findByCommand("select cislo from polozka where typ=5 order by kdy desc limit 2");
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Object[] objects = (Object[]) iter.next();
                Relation child = new Relation(null,new Item(((Integer)objects[0]).intValue(),Item.DRIVER),0);
                Relation found = persistance.findByExample(child)[0];

                Item item = (Item) found.getChild();
                persistance.synchronize(item);
                writer.write("http://AbcLinuxu.cz/drivers/ViewRelation?relationId="+found.getId());
                writer.write("|\\");
                writer.write(helper.getXPath(item,"data/name"));
                writer.write('\n');
            }

            writer.close();
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
    public static void setFile(String name) {
        fileName = name;
    }

    /**
     * @return File, where links are stored.
     */
    public static String getFileName() {
        return fileName;
    }

    public static void main(String[] args) {
        GenerateLinks links = new GenerateLinks();
        links.runJob();
    }
}
