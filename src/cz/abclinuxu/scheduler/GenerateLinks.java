/*
 * User: literakl
 * Date: Feb 18, 2002
 * Time: 9:03:52 AM
 */
package cz.abclinuxu.scheduler;

import cz.abclinuxu.persistance.*;
import cz.abclinuxu.persistance.extra.LimitQualifier;
import cz.abclinuxu.persistance.extra.Qualifier;
import cz.abclinuxu.data.*;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.data.view.DiscussionHeader;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.InstanceUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.prefs.Preferences;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.apache.log4j.BasicConfigurator;

public class GenerateLinks extends TimerTask implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GenerateLinks.class);

    public static final String PREF_TRAFIKA = "trafika";
    public static final String PREF_RSS = "rss";
    public static final String PREF_ANNECA = "anneca";
    public static final String PREF_SZM = "szm";
    public static final String PREF_DISCUSSIONS = "diskuse.rss";

    String trafika, anneca, rss, szm, discussionsRSS;

    static RE lineBreak;

    static {
        try {
            lineBreak = new RE("[\r\n$]+",RE.MATCH_MULTILINE);
        } catch (RESyntaxException e) {
            log.error("Cannot create regexp to find line breaks!", e);
        }
    }

    LinksGenerator[] generators;
    LinksGenerator forumGenerator;


    public GenerateLinks() {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureAndRememberMe(this);

        generators = new LinksGenerator[4];
        generators[0] = new Trafika(trafika);
        generators[1] = new Anneca(anneca);
        generators[2] = new RSS(rss);
        generators[3] = new Szm(szm);
        forumGenerator = new RSS(discussionsRSS);
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        trafika = prefs.get(PREF_TRAFIKA, null);
        anneca = prefs.get(PREF_ANNECA, null);
        rss = prefs.get(PREF_RSS, null);
        szm = prefs.get(PREF_SZM, null);
        discussionsRSS = prefs.get(PREF_DISCUSSIONS, null);
    }

    public void run() {
        try {
            Persistance persistance = PersistanceFactory.getPersistance();
            Tools tools = new Tools();
            String url,title,desc,content;
            Record record = null;
            List links = new ArrayList(15);

            Category actual = (Category) persistance.findById(new Category(Constants.CAT_ACTUAL_ARTICLES));
            List children = actual.getChildren();
            tools.syncList(children);
            List list = Sorters2.byDate(children, Sorters2.DESCENDING);
            for(int i=0; i<6 && i<list.size(); i++ ) {
                Relation relation = (Relation) list.get(i);
                Item item = (Item) relation.getChild();

                url = "http://www.abclinuxu.cz/clanky/show/"+relation.getId();
                title = tools.xpath(item,"data/name");
                desc = removeNewLines(tools.xpath(item,"data/perex"));
                desc = tools.encodeSpecial(desc);
                record = (Record) InstanceUtils.findFirstChildRecordOfType(item,Record.ARTICLE).getChild();
                content = tools.encodeSpecial(tools.xpath(record,"data/content"));

                links.add(new Link(title, url, desc, content));
            }

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED,Qualifier.ORDER_DESCENDING, new LimitQualifier(0,3)};
            list = SQLTool.getInstance().findRecordRelationsWithType(Record.HARDWARE, qualifiers);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Relation found = (Relation) iter.next();
                Item item = (Item) found.getParent();
                persistance.synchronize(item);

                url = "http://www.abclinuxu.cz/hardware/show/"+found.getId();
                title = "H "+tools.xpath(item,"data/name");
                links.add(new Link(title, url, "", null));
            }

            qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, 1)};
            list = SQLTool.getInstance().findItemRelationsWithType(Item.DRIVER, qualifiers);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Relation found = (Relation) iter.next();
                Item item = (Item) found.getChild();
                persistance.synchronize(item);

                url = "http://www.abclinuxu.cz/drivers/show/"+found.getId();
                title = "O "+tools.xpath(item,"data/name");
                links.add(new Link(title, url, "", null));
            }

            for (int j = 0; j < generators.length; j++)
                generators[j].generate(links);

            // generate RSS feed for discussion forum
            links.clear();

            qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, 20)};
            list = SQLTool.getInstance().findDiscussionRelations(qualifiers);
            Tools.syncList(list);
            List discussions = tools.analyzeDiscussions(list);
            for ( Iterator iter = discussions.iterator(); iter.hasNext(); ) {
                DiscussionHeader discussion = (DiscussionHeader) iter.next();
                url = "http://www.abclinuxu.cz/forum/show/"+discussion.getRelationId();
                title = tools.xpath(discussion.getDiscussion(), "data/title");
                title = title.concat(", odpovìdí: "+discussion.getResponseCount());
                title = tools.encodeSpecial(title);
                desc = tools.xpath(discussion.getDiscussion(), "data/text");
                desc = tools.removeTags(desc);
                desc = tools.encodeSpecial(desc);
                links.add(new Link(title,url,desc,null));
            }

            forumGenerator.generate(links);

        } catch (Exception e) {
            log.error("Cannot generate links",e);
        }
    }

    private static class Link {
        String title, url, desc, content;

        public Link(String title, String url, String desc, String content) {
            this.title = title;
            this.url = url;
            this.desc = desc;
            this.content = content;
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
    private interface LinksGenerator {
        /** generates content of the feed */
        public void generate(List links) throws IOException;
    }

    /**
     * classical trafika format
     */
    private static class Trafika implements LinksGenerator {
        FileWriter writer = null;
        String path;

        public Trafika(String path) {
            this.path = path;
        }

        public void generate(List links) throws IOException {
            String file = AbcConfig.calculateDeployedPath(path);
            writer = new FileWriter(file);
            writer.write(Constants.isoFormat.format(new Date()));
            writer.write('\n');

            for ( Iterator iter = links.iterator(); iter.hasNext(); ) {
                Link link = (Link) iter.next();
                writer.write(link.url+"|\\"+link.title+"\n");
            }

            writer.close();
        }
    }

    /**
     * Slightly modified and enhanced Trafika format
     */
    private static class Anneca implements LinksGenerator {
        FileWriter writer = null;
        String path;

        public Anneca(String path) {
            this.path = path;
        }

        public void generate(List links) throws IOException {
            String file = AbcConfig.calculateDeployedPath(path);
            writer = new FileWriter(file);

            for ( Iterator iter = links.iterator(); iter.hasNext(); ) {
                Link link = (Link) iter.next();
                writer.write(link.url+"|"+link.title+"|"+link.desc+"\n");
            }

            writer.close();
        }
    }

    /**
     * Full version used by szm.sk.
     */
    private static class Szm implements LinksGenerator {
        FileWriter writer = null;
        String path;

        public Szm(String path) {
            this.path = path;
        }

        public void generate(List links) throws IOException {
            String file = AbcConfig.calculateDeployedPath(path);
            writer = new FileWriter(file);
            writer.write("<?xml version=\"1.0\" encoding=\"iso-8859-2\" ?>\n");
            writer.write("<root generated=\""+Constants.isoFormat.format(new Date())+"\">\n");
            writer.write("<copyright>AbcLinuxu s.r.o.</copyright>\n");
            writer.write("<note>Tento soubor je urcen pro szm.sk.");
            writer.write(" Obsah clanku smi byt indexovan, ale nesmi byt zverejnen.</note>\n");

            for ( Iterator iter = links.iterator(); iter.hasNext(); ) {
                Link link = (Link) iter.next();
                writer.write("<item>\n");
                writer.write("\t<title>"+link.title+"</title>\n");
                writer.write("\t<link>"+link.url+"</link>\n");
                writer.write("\t<description>"+link.desc+"</description>\n");
                writer.write("\t<content>"+link.content+"</content>\n");
                writer.write("</item>\n\n");
            }

            writer.write("</root>\n");
            writer.close();
        }
    }

    /**
     * Standard RSS
     */
    private static class RSS implements LinksGenerator {
        FileWriter writer = null;
        String path;

        public RSS(String path) {
            this.path = path;
        }

        public void generate(List links) throws IOException {
            String file = AbcConfig.calculateDeployedPath(path);
            writer = new FileWriter(file);
            writer.write("<?xml version=\"1.0\" encoding=\"iso-8859-2\" ?>\n");
            writer.write("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns=\"http://purl.org/rss/1.0/\">\n");
            writer.write("\t<channel rdf:about=\"http://www.abclinuxu.cz\">\n");
            writer.write("\t\t<title>AbcLinuxu.cz - tady je tuèòákùm hej!</title>\n");
            writer.write("\t\t<link>http://www.abclinuxu.cz</link>\n");
            writer.write("\t\t<description>Centrála pro výmìnu rad, zku¹eností a návodù pod Linuxem.</description>\n");
            writer.write("\t\t<image rdf:resource=\"http://www.abclinuxu.cz/images/site/logo2.png\" />\n");
            writer.write("\t\t<items>\n");
            writer.write("\t\t\t<rdf:Seq>\n");

            for ( Iterator iter = links.iterator(); iter.hasNext(); ) {
                Link link = (Link) iter.next();
                writer.write("\t\t\t\t<rdf:li resource=\""+link.url+"\" />\n");
            }

            writer.write("\t\t\t</rdf:Seq>\n");
            writer.write("\t\t</items>\n");
            writer.write("\t</channel>\n");

            for ( Iterator iter = links.iterator(); iter.hasNext(); ) {
                Link link = (Link) iter.next();
                writer.write("\t<item rdf:about=\""+link.url+"\">\n");
                writer.write("\t\t<title>"+link.title+"</title>\n");
                writer.write("\t\t<link>"+link.url+"</link>\n");
                writer.write("\t\t<description>"+link.desc+"</description>\n");
                writer.write("\t</item>\n");
            }

            writer.write("</rdf:RDF>\n");
            writer.close();
        }
    }
}
