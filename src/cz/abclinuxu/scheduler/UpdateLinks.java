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
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.freemarker.Tools;
import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;
import org.apache.regexp.RESyntaxException;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * Updates Links from other servers.
 */
public class UpdateLinks extends TimerTask implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UpdateLinks.class);
    public static final String PREF_MAX_LINKS_PER_FEED = "maxLinksPerFeed";
    public static final String PREF_MAX_TITLE_LENGTH = "maxLength";
    public static final String PREF_FEEDS = "feeds";
    public static final String PREF_FEED = "feedUri";

    /** how many links we have per server */
    static int linksPerFeed = 5;
    /** maximum length of one link's text */
    static int maxTitleLength = 80;
    /** id (integer) of server is key, value is ServerInfo instance */
    static Map definitions;
    static UpdateLinks instance;
    static REProgram ampersand;
    static {
        instance = new UpdateLinks();
        ConfigurationManager.getConfigurator().configureAndRememberMe(instance);
        try {
            ampersand = new RECompiler().compile("&");
        } catch (RESyntaxException e) {
                log.error("Regexp cannot be compiled!", e);
        }
    }

    // todo: link will be independant object, no relations, parent Category object.
    // just some category identifier inside the row. there will be soem database
    // table, which will contain information about RSS channels that shall be
    // fetched and the refresh period.

    /**
     * constructor
     */
    private UpdateLinks() {
    }

    /**
     * Periodically invoked task. Its purpose is to synchronize links with external definitions.
     */
    public void run() {
        if (log.isDebugEnabled()) log.debug("Starting task "+getJobName());
        try {
            Persistance persistance = PersistanceFactory.getPersistance();
            Category category = new Category(Constants.CAT_LINKS);
            category = (Category) persistance.findById(category);
            Map serverLinks = groupLinks(category,persistance);

            for (Iterator iter = serverLinks.keySet().iterator(); iter.hasNext();) {
                Server server = (Server) iter.next();
                try {
                    synchronize(server, (List)serverLinks.get(server), category, persistance);
                } catch (Exception e) {
                    log.warn("Cannot update links for server "+server+"!", e);
                }
            }
            if (log.isDebugEnabled()) log.debug("Finishing task " + getJobName());
        } catch (Exception e) {
            log.error("Task "+getJobName()+" failed!", e);
        }
    }

    public String getJobName() {
        return "UpdateLinks";
    }

    /**
     * Synchronizes links from external server with internal records.
     * @param server Server to be processed
     * @param storedLinks list of Links stored in persistance storage
     * @param category Category, where to put new Links
     */
    protected void synchronize(Server server, List storedLinks, Category category, Persistance persistance) throws PersistanceException {
        ServerInfo definition = (ServerInfo) definitions.get(new Integer(server.getId()));
        List downloaded = parseRSS(definition);
        int updated = 0;

        if ( downloaded.size() > linksPerFeed )
            downloaded = downloaded.subList(0, linksPerFeed);

        // remove from downloaded list links, that were already downloaded
        for (Iterator iter = downloaded.iterator(); iter.hasNext();) {
            if ( removeLink(storedLinks, (Link) iter.next()) )
                iter.remove();
        }

        for (Iterator iter = downloaded.iterator(); iter.hasNext();) {
            Link link = (Link) iter.next();
            if ( storedLinks.size() > 0 ) {
                Link existingLink = (Link) storedLinks.remove(0);
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
                category.addChildRelation(relation);
            }
            updated++;
        }
        if ( log.isDebugEnabled() ) log.debug("Updated "+updated+" links for server "+server);
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
                if ( title.length() > maxTitleLength )
                    title = title.substring(0, maxTitleLength);
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

    public static UpdateLinks getInstance() {
        return instance;
    }

    /**
     * @return list of ids of servers (integer) whose feeds are regularly fetched
     */
    public static List getMaintainedServers() {
        return new ArrayList(definitions.keySet());
    }

    /**
     * Finds fresh list of links of maintained feeds.
     * @return map where id is server and value is list of Links.
     */
    public static Map getMaintainedFeedLinks() {
        Persistance persistance = PersistanceFactory.getPersistance();
        Category category = new Category(Constants.CAT_LINKS);
        category = (Category) persistance.findById(category);
        return groupLinks(category, persistance);
    }

    /**
     * Groups links by server. Initialized server will be key in map and value will be
     * list of its links sorted by date in descending order. Fixed links will be discarded.
     * @throws PersistanceException if something goes wrong
     */
    static Map groupLinks(Category category, Persistance persistance) {
        int size = definitions.size();
        List servers = new ArrayList(size);
        for (Iterator iter = definitions.keySet().iterator(); iter.hasNext();) {
            Integer id = (Integer) iter.next();
            servers.add(new Server(id.intValue()));
        }

        Tools.syncList(servers);
        Tools.syncList(category.getChildren());

        Map result = new HashMap();
        Server server;
        for (Iterator iter = servers.iterator(); iter.hasNext();) {
            server = (Server) iter.next();
            result.put(server, new ArrayList());
        }

        List links;
        for (Iterator iter = category.getChildren().iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            Link link = (Link) persistance.findById(relation.getChild());
            if ( !link.isFixed() ) {
                server = new Server(link.getServer());
                links = (List) result.get(server);
                if ( links == null)
                    continue; // unmaintained server
                links.add(link);
            }
        }

        for (Iterator iter = result.values().iterator(); iter.hasNext();) {
            links = (List) iter.next();
            Sorters2.byDate(links, Sorters2.DESCENDING);
        }

        return result;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        maxTitleLength = prefs.getInt(PREF_MAX_TITLE_LENGTH, 80);
        linksPerFeed = prefs.getInt(PREF_MAX_LINKS_PER_FEED, 5);
        String feeds = prefs.get(PREF_FEEDS, "");

        Map newDefinitions = new HashMap();
        StringTokenizer stk = new StringTokenizer(feeds, ",");
        String feed, tmp;
        Integer id;
        while (stk.hasMoreTokens()) {
            tmp =  stk.nextToken();
            id = new Integer(tmp);
            feed = prefs.get(PREF_FEED + id, null);
            if (feed == null) {
                log.warn("Missing feed url for server "+id);
                continue;
            }
            log.info("Added feed url '"+feed+"' for server "+id);
            newDefinitions.put(id, new ServerInfo(feed));
        }
        definitions = newDefinitions;
    }

    public static void main(String[] args) {
        org.apache.log4j.BasicConfigurator.configure();
        UpdateLinks updater = new UpdateLinks();
        updater.run();
    }

    public static String fixAmpersand(String url) {
        if (url==null || url.length()==0)
	        return url;
	    return new RE(ampersand).subst(url,"&amp;",RE.REPLACE_ALL);
    }

    static class ServerInfo {
        static final int RSS = 2;

        /** where to download new links */
        String url;
        /** null fore default, otherwise valid encoding name of text */
        String encoding;
        /** in which format data are stored */
        int format = RSS;

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
