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
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.impl.AbcConfig;

import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.*;
import java.util.prefs.Preferences;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.apache.log4j.BasicConfigurator;

public class GenerateLinks extends TimerTask implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GenerateLinks.class);

    public static final String PREF_TRAFIKA = "trafika";
    public static final String DEFAULT_TRAFIKA = "abc.dat";
    public static final String PREF_RSS = "rss";
    public static final String DEFAULT_RSS = "abc.rss";
    public static final String PREF_ANNECA = "anneca";
    public static final String DEFAULT_ANNECA = "abc2.dat";
    public static final String PREF_SZM = "szm";
    public static final String DEFAULT_SZM = "abc_szm_sk.xml";

    String trafika, anneca, rss, szm;

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

        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(this);
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        trafika = prefs.get(PREF_TRAFIKA,DEFAULT_TRAFIKA);
        anneca = prefs.get(PREF_ANNECA,DEFAULT_ANNECA);
        rss = prefs.get(PREF_RSS,DEFAULT_RSS);
        szm = prefs.get(PREF_SZM,DEFAULT_SZM);
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
                    content = helper.encodeSpecial(helper.getXPath(record,"data/content"));

                for (int j = 0; j < generators.length; j++) {
                    generators[j].generateLink(title, url, desc, content);
                }
            }

            list = SQLTool.getInstance().findHardwareRelationsByUpdated(0,3);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Relation found = (Relation) iter.next();
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

            list = SQLTool.getInstance().findDriverRelationsByUpdated(0,1);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Relation found = (Relation) iter.next();
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
            String file = AbcConfig.calculateDeployedPath(trafika);
            writer = new FileWriter(file);
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
            String file = AbcConfig.calculateDeployedPath(anneca);
            writer = new FileWriter(file);
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
            String file = AbcConfig.calculateDeployedPath(szm);
            writer = new FileWriter(file);
            writer.write("<?xml version=\"1.0\" encoding=\"iso-8859-2\" ?>\n");
            writer.write("<root generated=\""+Constants.isoFormat.format(new Date())+"\">\n");
            writer.write("<copyright>AbcLinuxu s.r.o.</copyright>\n");
            writer.write("<note>Tento soubor je urcen pro szm.sk.");
            writer.write(" Obsah clanku smi byt indexovan, ale nesmi byt zverejnen.</note>\n");
        }

        public void generateLink(String title, String url, String desc, String content) throws IOException {
            writer.write("<item>\n");
            writer.write("\t<title>"+title+"</title>\n");
            writer.write("\t<link>"+url+"</link>\n");
            writer.write("\t<description>"+desc+"</description>\n");
            writer.write("\t<content>"+content+"</content>\n");
            writer.write("</item>\n\n");
        }

        public void generateBottom() throws IOException {
            writer.write("</root>\n");
            writer.close();
        }
    }

    /**
     * Standard RSS
     */
    class RSS implements LinksGenerator {
        FileWriter writer = null;

        public void generateHeader() throws IOException {
            String file = AbcConfig.calculateDeployedPath(rss);
            writer = new FileWriter(file);
            writer.write("<?xml version=\"1.0\" encoding=\"iso-8859-2\" ?>\n");
            writer.write("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns=\"http://purl.org/rss/1.0/\">\n");
            writer.write("\t<channel rdf:about=\"http://www.abclinuxu.cz\">\n");
            writer.write("\t\t<title>AbcLinuxu.cz - tady je tuèòákùm hej!</title>\n");
            writer.write("\t\t<link>http://www.abclinuxu.cz</link>\n");
            writer.write("\t\t<description>Centrála pro výmìnu rad, zku¹eností a návodù pod Linuxem.</description>\n");
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
            writer.write("\t\t<description>Hledej výraz v návodech, èláncích èi diskusích.</description>\n");
            writer.write("\t\t<name>query</name>\n");
            writer.write("\t\t<link>http://AbcLinuxu.cz/Search</link>\n");
            writer.write("\t</textinput>\n");
            writer.write("</rdf:RDF>\n");
            writer.close();
        }
    }
}
