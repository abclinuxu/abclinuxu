/*
 * User: literakl
 * Date: Feb 18, 2002
 * Time: 9:03:52 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.scheduler;

import cz.abclinuxu.persistance.*;
import cz.abclinuxu.data.*;
import cz.abclinuxu.servlets.utils.VelocityHelper;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.view.ShowOlder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.apache.log4j.BasicConfigurator;

public class GenerateLinks extends TimerTask {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GenerateLinks.class);

    static String fileName_trafika = "abc.dat";
    static String fileName_anneca = "abc2.dat";
    static String fileName_rss = "abc.rss";
    static String fileName_szm = "abc_szm_sk.dat";

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
        generators = new LinksGenerator[4];
        generators[0] = new Trafika();
        generators[1] = new Anneca();
        generators[2] = new RSS();
        generators[3] = new Szm();
    }

    public void run() {
        try {
            Persistance persistance = PersistanceFactory.getPersistance();
            VelocityHelper helper = new VelocityHelper();
            Category actual = (Category) persistance.findById(new Category(Constants.CAT_ACTUAL_ARTICLES));
            helper.sync(actual.getContent());
            List list = helper.sortByDateDescending(actual.getContent());

            for (int j = 0; j < generators.length; j++) {
                generators[j].generateHeader();
            }

            String url,title,desc,content;
            Record record = null;
            for(int i=0; i<6 && i<list.size(); i++ ) {
                Relation relation = (Relation) list.get(i);
                Item item = (Item) relation.getChild();

                url = "http://www.abclinuxu.cz/clanky/ViewRelation?relationId="+relation.getId();
                title = helper.getXPath(item,"data/name");
                desc = removeNewLines(helper.getXPath(item,"data/perex"));

                content = null; record = null;
                for (Iterator iter = item.getContent().iterator(); iter.hasNext();) {
                    Relation child = (Relation) iter.next();
                    if ( child.getChild() instanceof Record ) {
                        record = (Record) child.getChild();
                        helper.sync(record);
                        if ( record.getType()==Record.ARTICLE )
                            break;
                        record = null;
                    }
                }
                if ( record!=null )
                    content = removeNewLines(helper.getXPath(record,"data/content"));

                for (int j = 0; j < generators.length; j++) {
                    generators[j].generateLink(title, url, desc, content);
                }
            }

            list = persistance.findByCommand(ShowOlder.SQL_HARDWARE+" limit "+3);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Object[] objects = (Object[]) iter.next();
                int id = ((Integer)objects[0]).intValue();
                Relation found = (Relation) persistance.findById(new Relation(id));
                Item item = (Item) found.getParent();
                persistance.synchronize(item);

                url = "http://www.abclinuxu.cz/hardware/ViewRelation?relationId="+found.getId();
                title = "H "+helper.getXPath(item,"data/name");

                for (int j = 0; j < generators.length; j++) {
                    generators[j].generateLink(title, url, "", null);
                }
            }

//            list = persistance.findByCommand(ShowOlder.SQL_SOFTWARE+" limit "+2);
//            for (Iterator iter = list.iterator(); iter.hasNext();) {
//                Object[] objects = (Object[]) iter.next();
//                int id = ((Integer)objects[0]).intValue();
//                Relation found = (Relation) persistance.findById(new Relation(id));
//                Item item = (Item) found.getParent();
//                persistance.synchronize(item);
//
//                url = "http://AbcLinuxu.cz/software/ViewRelation?relationId="+found.getId();
//                title = "S "+helper.getXPath(item,"data/name");
//
//                for (int j = 0; j < generators.length; j++) {
//                    generators[j].generateLink(title,"",url);
//                }
//            }

            list = persistance.findByCommand(ShowOlder.SQL_DRIVERS+" limit "+1);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Object[] objects = (Object[]) iter.next();
                int id = ((Integer)objects[0]).intValue();
                Relation found = (Relation) persistance.findById(new Relation(id));
                Item item = (Item) found.getChild();
                persistance.synchronize(item);

                url = "http://www.abclinuxu.cz/drivers/ViewRelation?relationId="+found.getId();
                title = "O "+helper.getXPath(item,"data/name");

                for (int j = 0; j < generators.length; j++) {
                    generators[j].generateLink(title, url, "", null);
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
     * Sets file name of Anneca feed.
     */
    public static void setFileNameAnneca(String name) {
        fileName_anneca = name;
    }

    /**
     * Sets file name of szm feed.
     */
    public static void setFileNameSzm(String name) {
        fileName_szm = name;
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
        links.run();
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
        public void generateLink(String title, String url, String desc, String content) throws IOException;
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

        public void generateLink(String title, String url, String desc, String content) throws IOException {
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

        public void generateLink(String title, String url, String desc, String content) throws IOException {
            writer.write(url+"|"+title+"|"+desc+"\n");
        }

        public void generateBottom() throws IOException {
            writer.close();
        }
    }

    /**
     * Full version used by szm.sk.
     */
    class Szm implements LinksGenerator {
        FileWriter writer = null;

        public void generateHeader() throws IOException {
            writer = new FileWriter(fileName_szm);
            writer.write("// Tento soubor je urcen pro szm.sk.\n");
            writer.write("// Obsah clanku smi byt indexovan, ale nesmi byt zverejnen.\n");
            writer.write(Constants.isoFormat.format(new Date()));
            writer.write('\n');
        }

        public void generateLink(String title, String url, String desc, String content) throws IOException {
            writer.write(url+"|"+title+"|"+desc+"|"+content+"\n");
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
            writer.write("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns=\"http://purl.org/rss/1.0/\">\n");
//            writer.write("<!DOCTYPE rss PUBLIC \"-//Netscape Communications//DTD RSS 0.91//EN\" \"http://my.netscape.com/publish/formats/rss-0.91.dtd\">\n");
            writer.write("\t<channel rdf:about=\"http://www.abclinuxu.cz\">\n");
            writer.write("\t\t<title>AbcLinuxu.cz - tady je tu���k�m hej!</title>\n");
            writer.write("\t\t<link>http://www.abclinuxu.cz</link>\n");
            writer.write("\t\t<description>Centr�la pro v�m�nu rad, zku�enost� a n�vod� pod Linuxem.</description>\n");
            writer.write("\t\t<image rdf:resource=\"http://www.abclinuxu.cz/images/site/logo2.png\" />\n");
            writer.write("\t</channel>\n");
        }

        public void generateLink(String title, String url, String desc, String content) throws IOException {
            writer.write("\t<item rdf:about=\""+url+"\">\n");
            writer.write("\t\t<title>"+title+"</title>\n");
            writer.write("\t\t<link>"+url+"</link>\n");
            writer.write("\t\t<description>"+desc+"</description>\n");
            writer.write("\t</item>\n");
        }

        public void generateBottom() throws IOException {
            writer.write("\t<textinput>\n");
            writer.write("\t\t<title>Prohledej AbcLinuxu.cz</title>\n");
            writer.write("\t\t<description>Hledej v�raz v n�vodech, �l�nc�ch �i diskus�ch.</description>\n");
            writer.write("\t\t<name>query</name>\n");
            writer.write("\t\t<link>http://AbcLinuxu.cz/Search</link>\n");
            writer.write("\t</textinput>\n");
            writer.write("</rdf:RDF>\n");
            writer.close();
        }
    }
}
