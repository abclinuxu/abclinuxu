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
import org.apache.log4j.BasicConfigurator;

public class GenerateLinks implements Task {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(GenerateLinks.class);

    static String fileName_trafika = "abc.dat";
    static String fileName_anneca = "abc2.dat";
    static String fileName_rss = "abc.rss";

    static RE lineBreak;

    static {
        try {
            lineBreak = new RE("[\r\n$]+",RE.MATCH_MULTILINE);
        } catch (RESyntaxException e) {
            log.error("Cannot create regexp to find line breaks!", e);
        }
    }

    LinksGenerator[] generators;


    public GenerateLinks() {
        generators = new LinksGenerator[3];
        generators[0] = new Trafika();
        generators[1] = new Anneca();
        generators[2] = new RSS();
    }

    public void runJob() {
        try {
            Persistance persistance = PersistanceFactory.getPersistance();
            VelocityHelper helper = new VelocityHelper();
            Category actual = (Category) persistance.findById(new Category(Constants.CAT_ACTUAL_ARTICLES));
            helper.sync(actual.getContent());
            List list = helper.sortByDateDescending(actual.getContent());

            for (int j = 0; j < generators.length; j++) {
                generators[j].generateHeader();
            }

            String url,title,desc;
            for(int i=0; i<4 && i<list.size(); i++ ) {
                Relation relation = (Relation) list.get(i);
                Item item = (Item) relation.getChild();

                url = "http://AbcLinuxu.cz/clanky/ViewRelation?relationId="+relation.getId();
                title = helper.getXPath(item,"data/name");
                desc = removeNewLines(helper.getXPath(item,"data/perex"));

                for (int j = 0; j < generators.length; j++) {
                    generators[j].generateLink(title,desc,url);
                }
            }

            list = persistance.findByCommand("select cislo from zaznam where typ=1 order by kdy desc limit 2");
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Object[] objects = (Object[]) iter.next();
                Relation child = new Relation(null,new Record(((Integer)objects[0]).intValue(),Record.HARDWARE),0);
                Relation found = persistance.findByExample(child)[0];
                Item item = (Item) found.getParent();
                persistance.synchronize(item);

                url = "http://AbcLinuxu.cz/hardware/ViewRelation?relationId="+found.getId();
                title = "H "+helper.getXPath(item,"data/name");

                for (int j = 0; j < generators.length; j++) {
                    generators[j].generateLink(title,"",url);
                }
            }

            list = persistance.findByCommand("select cislo from zaznam where typ=2 order by kdy desc limit 2");
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Object[] objects = (Object[]) iter.next();
                Relation child = new Relation(null,new Record(((Integer)objects[0]).intValue(),Record.SOFTWARE),0);
                Relation found = persistance.findByExample(child)[0];
                Item item = (Item) found.getParent();
                persistance.synchronize(item);

                url = "http://AbcLinuxu.cz/software/ViewRelation?relationId="+found.getId();
                title = "S "+helper.getXPath(item,"data/name");

                for (int j = 0; j < generators.length; j++) {
                    generators[j].generateLink(title,"",url);
                }
            }

            list = persistance.findByCommand("select cislo from polozka where typ=5 order by kdy desc limit 2");
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Object[] objects = (Object[]) iter.next();
                Relation child = new Relation(null,new Item(((Integer)objects[0]).intValue(),Item.DRIVER),0);
                Relation found = persistance.findByExample(child)[0];
                Item item = (Item) found.getChild();
                persistance.synchronize(item);

                url = "http://AbcLinuxu.cz/drivers/ViewRelation?relationId="+found.getId();
                title = "O "+helper.getXPath(item,"data/name");

                for (int j = 0; j < generators.length; j++) {
                    generators[j].generateLink(title,"",url);
                }
            }

            for (int j = 0; j < generators.length; j++) {
                generators[j].generateBottom();
            }
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
    public static void setFileNameTrafika(String name) {
        fileName_trafika = name;
    }

    /**
     * Sets default file name.
     */
    public static void setFileNameAnneca(String name) {
        fileName_anneca = name;
    }

    /**
     * Sets RSS file name.
     */
    public static void setFileNameRSS(String name) {
        fileName_rss = name;
    }

    /**
     * Converts new line characters to spaces.
     */
    String removeNewLines(String text) {
        return lineBreak.subst(text," ");
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        GenerateLinks links = new GenerateLinks();
        links.runJob();
    }

    /**
     * For event based generation of files with links.
     */
    interface LinksGenerator {
        /** called at start */
        public void generateHeader() throws IOException;
        /** called at end */
        public void generateBottom() throws IOException;
        /** called for each article */
        public void generateLink(String title, String desc, String url) throws IOException;
    }

    /**
     * classical trafika format
     */
    class Trafika implements LinksGenerator {
        FileWriter writer = null;

        public void generateHeader() throws IOException {
            writer = new FileWriter(fileName_trafika);
            writer.write(Constants.isoFormat.format(new Date()));
            writer.write('\n');
        }

        public void generateBottom() throws IOException {
            writer.close();
        }

        public void generateLink(String title, String desc, String url) throws IOException {
            writer.write(url+"|\\"+title+"\n");
        }
    }

    /**
     * Slightly modified and enhanced Trafika format
     */
    class Anneca implements LinksGenerator {
        FileWriter writer = null;

        public void generateHeader() throws IOException {
            writer = new FileWriter(fileName_anneca);
        }

        public void generateLink(String title, String desc, String url) throws IOException {
            writer.write(url+"|"+title+"|"+desc+"\n");
        }

        public void generateBottom() throws IOException {
            writer.close();
        }
    }

    /**
     * Standard RSS
     */
    class RSS implements LinksGenerator {
        FileWriter writer = null;

        public void generateHeader() throws IOException {
            writer = new FileWriter(fileName_rss);
            writer.write("<?xml version=\"1.0\" encoding=\"iso-8859-2\" ?>\n");
            writer.write("<!DOCTYPE rss PUBLIC \"-//Netscape Communications//DTD RSS 0.91//EN\" \"http://my.netscape.com/publish/formats/rss-0.91.dtd\">\n");
            writer.write("<rss version=\"0.91\">\n");
            writer.write("<channel>\n");
            writer.write("<title>AbcLinuxu.cz - tady je tuèòákùm hej!</title>\n");
            writer.write("<link>http://AbcLinuxu.cz</link>\n");
            writer.write("<description>Centrála pro výmìnu rad, zku¹eností a návodù pod Linuxem.</description>\n");
        }

        public void generateLink(String title, String desc, String url) throws IOException {
            writer.write("<item>\n");
            writer.write("<title>"+title+"</title>\n");
            writer.write("<link>"+url+"</link>\n");
            writer.write("</item>\n");
        }

        public void generateBottom() throws IOException {
            writer.write("<textinput>\n");
            writer.write("<title>Prohledej AbcLinuxu.cz</title>\n");
            writer.write("<description>Hledej výraz v návodech, èláncích èi diskusích.</description>\n");
            writer.write("<name>query</name>\n");
            writer.write("<link>http://AbcLinuxu.cz/Search</link>\n");
            writer.write("</textinput>\n");
            writer.write("</channel>\n");
            writer.write("</rss>\n");
            writer.close();
        }
    }
}
