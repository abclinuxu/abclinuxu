/*
 * User: literakl
 * Date: Apr 6, 2002
 * Time: 5:10:03 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.scheduler.jobs;

import cz.abclinuxu.persistance.*;
import cz.abclinuxu.data.*;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.Sorters;
import cz.abclinuxu.scheduler.Task;

import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;

import org.dom4j.io.SAXReader;
import org.dom4j.*;

public class UpdateLinks2 implements Task {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(UpdateLinks2.class);

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
    public static final int KECZY = 7;
    public static final int REBOOT = 8;
    public static final int LINUXZONE = 9;
    public static final int LINUXSK = 10;
    public static final int LDAP = 11;
    /** id of last server, maximum id */
    public static final int LAST_SERVER = LDAP;

    Persistance persistance;
    Category category = new Category(Constants.CAT_LINKS);

    /** contains definition of locations and preferences for all servers */
    Map definitions;

    class ServerInfo {
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
    }

    /**
     * Default constructor
     */
    public UpdateLinks2() {
        persistance = PersistanceFactory.getPersistance();
        definitions = new HashMap();

        definitions.put(new Server(ROOT),new ServerInfo("http://www.root.cz/share/ttitles.txt"));
        definitions.put(new Server(LW),new ServerInfo("http://www.linuxworld.cz/lw.dat","Windows-1250",ServerInfo.TRAFIKA));
        definitions.put(new Server(SW),new ServerInfo("http://www.scienceworld.cz/sw.dat","Windows-1250",ServerInfo.TRAFIKA));
        definitions.put(new Server(UG),new ServerInfo("http://underground.cz/backend/czech.txt"));
        definitions.put(new Server(PENGUIN),new ServerInfo("http://www.penguin.cz/trafika.php3"));
        definitions.put(new Server(WS),new ServerInfo("http://www.awdesign.cz/ws/ws.dat"));
        definitions.put(new Server(KECZY),new ServerInfo("http://www.keczy.cz/latin2/headline.php3"));
        definitions.put(new Server(REBOOT),new ServerInfo("http://www.reboot.cz/reboot_lh.phtml"));
        definitions.put(new Server(LINUXZONE),new ServerInfo("http://www.linuxzone.cz/export/last10.phtml"));
        definitions.put(new Server(LINUXSK),new ServerInfo("http://www.linux.sk/backend.php",null,ServerInfo.RSS));
        definitions.put(new Server(LDAP),new ServerInfo("http://www.ldap-obsession.sk/backend.php",null,ServerInfo.RSS));
    }

    /**
     * Constructor for test
     */
    public UpdateLinks2(boolean nic) {
        persistance = PersistanceFactory.getPersistance();
        definitions = new HashMap();

        definitions.put(new Server(ROOT),new ServerInfo("file:///home/literakl/java/abc/bussiness/obsahy/ttitles.txt"));
        definitions.put(new Server(LW),new ServerInfo("file:///home/literakl/java/abc/bussiness/obsahy/lw.dat","Windows-1250",ServerInfo.TRAFIKA));
        definitions.put(new Server(SW),new ServerInfo("file:///home/literakl/java/abc/bussiness/obsahy/sw.dat","Windows-1250",ServerInfo.TRAFIKA));
        definitions.put(new Server(UG),new ServerInfo("file:///home/literakl/java/abc/bussiness/obsahy/czech.txt"));
        definitions.put(new Server(PENGUIN),new ServerInfo("file:///home/literakl/java/abc/bussiness/obsahy/trafika.php3"));
        definitions.put(new Server(WS),new ServerInfo("file:///home/literakl/java/abc/bussiness/obsahy/ws.dat"));
        definitions.put(new Server(KECZY),new ServerInfo("file:///home/literakl/java/abc/bussiness/obsahy/headline.php3"));
        definitions.put(new Server(REBOOT),new ServerInfo("file:///home/literakl/java/abc/bussiness/obsahy/reboot_lh.phtml"));
        definitions.put(new Server(LINUXZONE),new ServerInfo("file:///home/literakl/java/abc/bussiness/obsahy/last10.phtml"));
        definitions.put(new Server(LINUXSK),new ServerInfo("file:///home/literakl/java/abc/bussiness/obsahy/backend.php",null,ServerInfo.RSS));
        definitions.put(new Server(LDAP),new ServerInfo("file:///home/literakl/java/abc/bussiness/obsahy/backend-ldap.php",null,ServerInfo.RSS));
    }

    /**
     * Periodically invoked task. Its purpose is to synchronize links with external definitions.
     */
    public void runJob() {
        log.debug("Starting task "+getJobName());

        try {
            category = (Category) persistance.findById(category);
        } catch (PersistanceException e) {
            log.error("Cannot load category "+category+"! Aborting ..", e);
            return;
        }
        Map serverLinks = UpdateLinks.groupLinks(category,persistance);

        for (Iterator iter = definitions.keySet().iterator(); iter.hasNext();) {
            Server server = (Server) iter.next();
            ServerInfo definition = (ServerInfo) definitions.get(server);
            try {
                synchronize(server,category,serverLinks);
            } catch (PersistanceException e) {
                log.error("Cannot update links for server "+server+"!", e);
            }
        }
        log.debug("Finishing task "+getJobName());
    }

    public String getJobName() {
        return "UpdateLinks2";
    }

    /**
     * Synchronizes links from external server with internal records.
     * @param server Server to be processed
     * @param category Category, where to put new Links
     * @param serverLinks Map, where key is Server and value is list of its links.
     */
    protected void synchronize(Server server, Category category, Map serverLinks) throws PersistanceException {
        ServerInfo definition = (ServerInfo) definitions.get(server);
        List stored = loadStoredLinks(server,serverLinks);
        List downloaded = null;
        int updated = 0;

        if ( definition.format==ServerInfo.RSS ) {
            downloaded = parseRSS(definition);
        } else {
            downloaded = parseTrafika(definition);
        }

        if ( downloaded.size()>LINKS_PER_SERVER ) downloaded = downloaded.subList(0,LINKS_PER_SERVER);
        // remove from downloaded list links, that were already downloaded
        for (Iterator iter = downloaded.iterator(); iter.hasNext();) {
            if ( removeLink(stored, (Link) iter.next()) ) iter.remove();
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
                String url = line.substring(0,where);
                String title = line.substring(where+2);
                if ( title.length()>TEXT_LENGTH ) title = title.substring(0,TEXT_LENGTH);

                Link link = new Link();
                link.setUrl(url);
                link.setText(title);

                result.add(link);
            }
            in.close();
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
        SAXReader reader = new SAXReader();

        try {
            Document doc = reader.read(new URL(definition.url));
            List items = doc.selectNodes("rss/channel/item");
            if ( items==null ) return result;
            for (Iterator iter = items.iterator(); iter.hasNext();) {
                Node node = (Node) iter.next();
                String title = node.selectSingleNode("title").getText();
                if ( title.length()>TEXT_LENGTH ) title = title.substring(0,TEXT_LENGTH);
                String url = node.selectSingleNode("link").getText();

                Link link = new Link();
                link.setUrl(url);
                link.setText(title);

                result.add(link);
            }
        } catch (Exception e) {
            log.error("Cannot parse links from "+definition.url, e);
        }

        return result;
    }

    /**
     * Loads links for selected server from persistant storage and
     * sorts them by time in descending order.
     */
    protected List loadStoredLinks(Server server, Map serverLinks) {
        List links = (List) serverLinks.get(server);
        if ( links==null ) return new LinkedList();
        Sorters.sortByDate(links,false);
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

    public static void main(String[] args) {
        org.apache.log4j.BasicConfigurator.configure();
        UpdateLinks2 updater = new UpdateLinks2();
        updater.runJob();
    }
}
