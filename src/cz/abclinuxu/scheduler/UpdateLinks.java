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
import com.sun.syndication.io.ParsingFeedException;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Link;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Server;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.freemarker.Tools;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimerTask;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
    static Pattern ampersand;
    static {
        instance = new UpdateLinks();
        ConfigurationManager.getConfigurator().configureAndRememberMe(instance);
        try {
            ampersand = Pattern.compile("&");
        } catch (PatternSyntaxException e) {
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
            ServerInfo definition;
            Server server;
            List servers = getMaintainedServers();
            for (Iterator iter = servers.iterator(); iter.hasNext();) {
                Integer id = (Integer) iter.next();
                definition = (ServerInfo) definitions.get(id);
                server = (Server) persistance.findById(new Server(id.intValue()));
                try {
                    synchronize(server, 0, definition, persistance);
                } catch (Exception e) {
                    log.warn("Cannot update links for server "+server+"!", e);
                }
            }

            List remove = new ArrayList();
            GenericObject child;
            Relation relation;
            Element feed;
            int rid = 0;
            Item dynamicRss = (Item) persistance.findById(new Item(Constants.ITEM_DYNAMIC_CONFIGURATION));
            Document doc = (Document) dynamicRss.getData().clone();
            for (Iterator iter = doc.selectNodes("//feeds/feed").iterator(); iter.hasNext();) {
                feed = (Element) iter.next();
                try {
                    rid = Misc.parseInt(feed.attributeValue("relation"), -1);
                    relation = (Relation) persistance.findById(new Relation(rid));
                    child = persistance.findById(relation.getChild());
                    definition = new ServerInfo(feed.getText());
                    try {
                        synchronize(child, relation.getId(), definition, persistance);
                    } catch (Exception e) {
                        log.warn("Cannot update links for url " + definition.url + ", parent relation is "+rid+"!", e);
                    }
                } catch (Exception e) {
                    remove.add(new Integer(rid));
                }
            }

            if (remove.size() > 0) { // remove feeds for objects purged from persistance
                dynamicRss = (Item) persistance.findById(new Item(Constants.ITEM_DYNAMIC_CONFIGURATION));
                dynamicRss = (Item) dynamicRss.clone();
                doc = dynamicRss.getData();
                for (Iterator iter = remove.iterator(); iter.hasNext();) {
                    Integer id = (Integer) iter.next();
                    feed = (Element) doc.selectSingleNode("//feed[@relation="+id+"]");
                    if (feed != null)
                        feed.detach();
                }
                persistance.update(dynamicRss);
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
     * Synchronizes links from given feed with links under given object.
     * @param parent initialized parent, where the links are stored.
     * @param parentRelation if of parent relation or 0.
     * @param definition definition of the feed
     */
    protected void synchronize(GenericObject parent, int parentRelation, ServerInfo definition, Persistance persistance) {
        List storedLinks = getLinks(parent, persistance);
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
                existingLink.setUpdated(link.getUpdated());
                persistance.update(existingLink);
            } else {
                link.setOwner(1);
                link.setServer(parent.getId());
                link.setFixed(false);
                persistance.create(link);

                Relation relation = new Relation(parent, link, parentRelation);
                persistance.create(relation);
                parent.addChildRelation(relation);
            }
            updated++;
        }
        if ( log.isDebugEnabled() ) log.debug("Updated "+updated+" links for "+parent);
    }

    /**
     * Returns all links that are children of given object.
     * @param object initialized GenericObject
     * @return list of initialized Links
     */
    private List getLinks(GenericObject object, Persistance persistance) {
        List result = new ArrayList(linksPerFeed);
        Relation relation;
        GenericObject obj;
        Link link;
        for (Iterator iter = object.getChildren().iterator(); iter.hasNext();) {
            relation =  (Relation) iter.next();
            relation = (Relation) persistance.findById(relation);
            obj = persistance.findById(relation.getChild());
            if (! (obj instanceof Link))
                continue;

            link = (Link) obj;
            if (link.isFixed())
                continue;
            result.add(link);
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
                if ( title.length() > maxTitleLength )
                    title = title.substring(0, maxTitleLength);
                String url = fixAmpersand(entry.getLink());

                Link link = new Link();
                link.setUrl(url);
                link.setText(title);
                link.setUpdated(entry.getPublishedDate());

                result.add(link);
            }
        } catch (ParsingFeedException e) {
            log.warn("Invalid content in feed "+definition.url+": "+e.getMessage());
        } catch (IOException e) {
            log.warn("IO problems for "+definition.url+": "+e.getMessage());
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
     * @return list of Servers, their children are Links
     */
    public static Map getMaintainedFeeds() {
        List servers = new ArrayList(definitions.size());
        for (Iterator iter = definitions.keySet().iterator(); iter.hasNext();) {
            Integer id = (Integer) iter.next();
            servers.add(new Server(id.intValue()));
        }
        servers = Tools.syncList(servers);

        Map result = new HashMap(definitions.size() + 1, 1.0f);
        for (Iterator iter = servers.iterator(); iter.hasNext();) {
            Server server = (Server) iter.next();
            List children = Tools.syncList(server.getChildren());
            List links = new ArrayList(children.size());
            for (Iterator iter2 = children.iterator(); iter2.hasNext();) {
                Relation relation = (Relation) iter2.next();
                links.add(relation.getChild());
            }
            result.put(server, links);
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
        Matcher matcher = ampersand.matcher(url);
	    return matcher.replaceAll("&amp;");
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
