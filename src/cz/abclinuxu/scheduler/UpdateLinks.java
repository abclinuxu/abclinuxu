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
package cz.abclinuxu.scheduler;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Link;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Server;
import cz.abclinuxu.exceptions.PersistanceException;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.init.AbcInit;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.freemarker.Tools;
import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;
import org.apache.regexp.RESyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

/**
 * Updates Links from other servers.
 */
public class UpdateLinks extends TimerTask {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UpdateLinks.class);

    /** how many links we have per server */
    static final int LINKS_PER_SERVER = 5;
    /** maximum length of one link's text */
    static final int TEXT_LENGTH = 80;

    /** ids of servers */
    public static final int ROOT = 1;
    public static final int LW = 2;
    public static final int SW = 3;
    public static final int UG = 4;
    public static final int PENGUIN = 5;
    public static final int WS = 6;
    public static final int ABCLINUXU = 7;
    public static final int REBOOT = 8;
    public static final int LINUXZONE = 9;
    public static final int LINUXSK = 10;
    public static final int LDAP = 11;
    public static final int MANDRAKE = 12;
    public static final int CZILLA = 13;
    public static final int OPENOFFICE = 14;
    public static final int SLASHDOT = 15;
    public static final int LINUXBIZ = 16;
    public static final int SUSEPORTAL = 17;
    public static final int CHIP = 18;
    public static final int SVETHARDWARE = 19;
    /** id of last server, maximum id */
    public static final int LAST_SERVER = SVETHARDWARE;

    static REProgram ampersand;

    static {
	try {
        ampersand = new RECompiler().compile("&");
	} catch (RESyntaxException e) {
	        log.error("Regexp cannot be compiled!", e);
	}
    }

    Category category = new Category(Constants.CAT_LINKS);

    /** contains definition of locations and preferences for all servers */
    Map definitions;

    // todo: link will be independant object, no relations, parent Category object.
    // just some category identifier inside the row. there will be soem database
    // table, which will contain information about RSS channels that shall be
    // fetched and the refresh period.

    /**
     * constructor
     */
    public UpdateLinks(boolean debug) {
        definitions = new HashMap();

        if (!debug) {
            definitions.put(new Server(ROOT), new ServerInfo("http://www.root.cz/rss/", null, ServerInfo.RSS));
//            definitions.put(new Server(LW), new ServerInfo("http://www.linuxworld.cz/lw.rss", "Windows-1250", ServerInfo.RSS));
            definitions.put(new Server(SW), new ServerInfo("http://www.scienceworld.cz/sw.rss", "Windows-1250", ServerInfo.RSS));
//            definitions.put(new Server(UG), new ServerInfo("http://underground.cz/backend.php", null, ServerInfo.RSS));
            definitions.put(new Server(PENGUIN), new ServerInfo("http://www.penguin.cz/rss.php", null, ServerInfo.RSS));
            definitions.put(new Server(ABCLINUXU), new ServerInfo("http://localhost:8080/auto/abc.rss", null, ServerInfo.RSS));
            definitions.put(new Server(MANDRAKE), new ServerInfo("http://www.mandrake.cz/titles_abc.php", "Windows-1250", ServerInfo.TRAFIKA));
            definitions.put(new Server(CZILLA), new ServerInfo("http://www.czilla.cz/rss/rss.html", null, ServerInfo.RSS));
            definitions.put(new Server(OPENOFFICE), new ServerInfo("http://www.openoffice.cz/rss.xml", null, ServerInfo.RSS));
            definitions.put(new Server(SLASHDOT), new ServerInfo("http://slashdot.org/index.rss", null, ServerInfo.RSS));
            definitions.put(new Server(LINUXBIZ), new ServerInfo("http://www.lbw.cz/rss/lbw.xml", null, ServerInfo.RSS));
            definitions.put(new Server(SUSEPORTAL), new ServerInfo("http://portal.suse.cz/node/feed", null, ServerInfo.RSS));
            definitions.put(new Server(CHIP), new ServerInfo("http://www.chip.cz/texty/feed.rss", null, ServerInfo.RSS));
            definitions.put(new Server(SVETHARDWARE), new ServerInfo("http://www.svethardware.cz/export.jsp?format=rss", null, ServerInfo.RSS));
        } else {
//        definitions.put(new Server(ROOT),new ServerInfo("file:///home/literakl/abc/data/titulky/ttitles.txt"));
//        definitions.put(new Server(LW),new ServerInfo("file:///home/literakl/abc/data/titulky/lw.dat","Windows-1250",ServerInfo.TRAFIKA));
//        definitions.put(new Server(SW),new ServerInfo("file:///home/literakl/abc/data/titulky/sw.dat","Windows-1250",ServerInfo.TRAFIKA));
//        definitions.put(new Server(UG),new ServerInfo("file:///home/literakl/abc/data/titulky/czech.txt"));
//        definitions.put(new Server(PENGUIN),new ServerInfo("file:///home/literakl/abc/data/titulky/trafika.php3"));
//        definitions.put(new Server(WS),new ServerInfo("file:///home/literakl/abc/data/titulky/ws.dat"));
//        definitions.put(new Server(KECZY),new ServerInfo("file:///home/literakl/abc/data/titulky/headline.php3"));
//        definitions.put(new Server(REBOOT),new ServerInfo("file:///home/literakl/abc/data/titulky/reboot_lh.phtml"));
//        definitions.put(new Server(LINUXZONE),new ServerInfo("file:///home/literakl/abc/data/titulky/last10.phtml"));
//        definitions.put(new Server(LINUXSK),new ServerInfo("file:///home/literakl/abc/data/titulky/backend.php",null,ServerInfo.RSS));
//        definitions.put(new Server(LDAP),new ServerInfo("file:///home/literakl/abc/data/titulky/backend-ldap.php",null,ServerInfo.RSS));
//        definitions.put(new Server(MANDRAKE), new ServerInfo("file:///home/literakl/abc/data/titulky/titles_abc.php"));
//        definitions.put(new Server(CZILLA), new ServerInfo("file:///home/literakl/abc/data/titulky/mozilla_rss.html", null, ServerInfo.RSS));
//        definitions.put(new Server(LW), new ServerInfo("http://www.linuxworld.cz/lw.rss", "Windows-1250", ServerInfo.RSS));
//        definitions.put(new Server(SW), new ServerInfo("http://www.scienceworld.cz/sw.rss", "Windows-1250", ServerInfo.RSS));
//            definitions.put(new Server(ABCLINUXU), new ServerInfo("http://www.abclinuxu.cz/auto/abc.dat"));
        }
    }

    /**
     * Periodically invoked task. Its purpose is to synchronize links with external definitions.
     */
    public void run() {
        log.debug("Starting task "+getJobName());
        Persistance persistance = PersistanceFactory.getPersistance();
        category = (Category) persistance.findById(category);
        Map serverLinks = groupLinks(category,persistance);

        for (Iterator iter = definitions.keySet().iterator(); iter.hasNext();) {
            Server server = (Server) persistance.findById((Server) iter.next());
            try {
                synchronize(server, category, serverLinks, persistance);
            } catch (Exception e) {
                log.warn("Cannot update links for server "+server+"!", e);
	        }
        }
        log.debug("Finishing task "+getJobName());
        try {
            AbcInit.setServerLinksAstSharedVariables();
        } catch (Exception e) {
            log.error("Cannot set links as shared variables!", e);
        }
    }

    public String getJobName() {
        return "UpdateLinks";
    }

    /**
     * Synchronizes links from external server with internal records.
     * @param server Server to be processed
     * @param category Category, where to put new Links
     * @param serverLinks Map, where key is Server and value is list of its links.
     */
    protected void synchronize(Server server, Category category, Map serverLinks, Persistance persistance) throws PersistanceException {
        ServerInfo definition = (ServerInfo) definitions.get(server);
        List stored = loadStoredLinks(server,serverLinks);
        List downloaded = null;
        int updated = 0;

        if ( definition.format==ServerInfo.RSS ) {
            downloaded = parseRSS(definition);
        } else {
            downloaded = parseTrafika(definition);
        }

        if ( downloaded.size()>LINKS_PER_SERVER )
            downloaded = downloaded.subList(0,LINKS_PER_SERVER);
        // remove from downloaded list links, that were already downloaded
        for (Iterator iter = downloaded.iterator(); iter.hasNext();) {
            if ( removeLink(stored, (Link) iter.next()) )
                iter.remove();
        }

        for (Iterator iter = downloaded.iterator(); iter.hasNext();) {
            Link link = (Link) iter.next();

            if ( stored.size()>0 ) {
                Link existingLink = (Link) stored.remove(0);
                existingLink.setText(link.getText());
                existingLink.setUrl(link.getUrl());
                persistance.update(existingLink);
            } else {
                link.setOwner(1);
                link.setServer(server.getId());
                link.setFixed(false);
                persistance.create(link);
                Relation relation = new Relation(category,link,0);
                persistance.create(relation);
            }
            updated++;
        }
        if ( log.isDebugEnabled() ) log.debug("Updated "+updated+" links for server "+server);
    }

    /**
     * Loads and parses links in Trafika format from selected server.
     * @return List of Links sorted by time in descending order.
     */
    protected List parseTrafika(ServerInfo definition) {
        List result = new ArrayList();

        try {
            InputStream stream = new URL(definition.url).openStream();
            BufferedReader in = null;
            if ( definition.encoding!=null ) {
                in = new BufferedReader(new InputStreamReader(stream,definition.encoding));
            } else {
                in = new BufferedReader(new InputStreamReader(stream));
            }

            String line = in.readLine(); // delete first line with date
            while ( (line = in.readLine())!=null ) {
                int where = line.indexOf("|\\");
                if ( where==-1 ) continue;
                String url = fixAmpersand(line.substring(0,where));
                String title = line.substring(where+2);
                title = Tools.encodeSpecial(title);
                if ( title.length()>TEXT_LENGTH )
                    title = title.substring(0,TEXT_LENGTH);

                Link link = new Link();
                link.setUrl(url);
                link.setText(title);

                result.add(link);
            }
            in.close();
        } catch (IOException e) {
            log.error("IO problems for "+definition.url+": "+e.getMessage());
        } catch (Exception e) {
            log.error("Cannot parse links from "+definition.url, e);
        }
        return result;
    }

    /**
     * Loads and parses links in netscape's RSS format from selected server.
     * @return List of Links sorted by time in descending order.
     */
    protected List parseRSS(ServerInfo definition) {
        List result = new ArrayList();
        SyndFeedInput input = new SyndFeedInput();

        try {
            SyndFeed feed = input.build(new XmlReader(new URL(definition.url)));
            List items = feed.getEntries();
            if ( items==null ) return result;
            for (Iterator iter = items.iterator(); iter.hasNext();) {
                SyndEntry entry = (SyndEntry) iter.next();
                String title = entry.getTitle();
                title = Tools.encodeSpecial(title);
                if ( title.length()>TEXT_LENGTH )
                    title = title.substring(0,TEXT_LENGTH);
                String url = fixAmpersand(entry.getLink());

                Link link = new Link();
                link.setUrl(url);
                link.setText(title);

                result.add(link);
            }
        } catch (IOException e) {
            log.error("IO problems for "+definition.url+": "+e.getMessage());
        }  catch (Exception e) {
            log.error("Cannot parse links from "+definition.url, e);
        }

        return result;
    }

    /**
     * Loads links for selected server from persistant storage and
     * sorts them by time in descending order.
     */
    protected List loadStoredLinks(Server server, Map serverLinks) {
        List links = (List) serverLinks.get(server.getName());
        if ( links==null )
            return new LinkedList();

        Sorters2.byDate(links,"DESCENDING");
        return links;
    }

    /**
     * Searches list of links for link with same text. If it doesn't find it,
     * it returns false. If the search is succesfull, link is removed from
     * list and method returns true.
     */
    private boolean removeLink(List list, Link link) {
        String tmp = null;

        for (Iterator iter = list.iterator(); iter.hasNext();) {
            tmp = ((Link) iter.next()).getText();
            if ( link.getText().equals(tmp) ) {
                iter.remove();
                return true;
            }
        }

        return false;
    }

    /**
     * Groups links by server. The server name will be key in map and value will be
     * list of its links. Fixed links will be discarded.
     * @throws PersistanceException if something goes wrong
     */
    public static Map groupLinks(Category category, Persistance persistance) {
        List servers = new ArrayList(LAST_SERVER);
        for (int i = 1; i <= LAST_SERVER; i++)
            servers.add(new Server(i));
        Tools.syncList(servers);
        Tools.syncList(category.getChildren());

        List[] links = new List[LAST_SERVER+1];
        for ( int i=0; i<=LAST_SERVER; i++ )
            links[i] = new ArrayList();

        for (Iterator iter = category.getChildren().iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
                Link link = (Link) persistance.findById(relation.getChild());
                if ( !link.isFixed() )
                    links[link.getServer()].add(link);
        }

        Map result = new HashMap();
        for (int i = 1; i <= LAST_SERVER; i++) {
            Server server = (Server) persistance.findById(new Server(i));
            result.put(server.getName(), links[i]);
        }
        return result;
    }

    public static void main(String[] args) {
        org.apache.log4j.BasicConfigurator.configure();
        String debug = (args==null || args.length==0)? "true":args[0];
        UpdateLinks updater = new UpdateLinks(Boolean.valueOf(debug).booleanValue());
        updater.run();
    }

    public static String fixAmpersand(String url) {
        if (url==null || url.length()==0)
	        return url;
	    return new RE(ampersand).subst(url,"&amp;",RE.REPLACE_ALL);
    }

    static class ServerInfo {
        static final int TRAFIKA = 1;
        static final int RSS = 2;

        /** where to download new links */
        String url;
        /** null fore default, otherwise valid encoding name of text */
        String encoding;
        /** in which format data are stored */
        int format = TRAFIKA;

        public ServerInfo(String url) {
            this.url = url;
        }

        public ServerInfo(String url, String encoding, int format) {
            this.url = url;
            this.encoding = encoding;
            this.format = format;
        }

        public String toString() {
            return url;
        }
    }
}
