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
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.freemarker.Tools;
import org.dom4j.Document;
import org.dom4j.Element;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
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
    //static Map definitions;
    static UpdateLinks instance;

    static {
        instance = new UpdateLinks();
        ConfigurationManager.getConfigurator().configureAndRememberMe(instance);
    }

    // todo: link will be independant object, no relations, parent Category object.
    // just some category identifier inside the row. there will be soem database
    // table, which will contain information about RSS channels that shall be
    // fetched and the refresh period.

    /**
     * constructor
     */
    private UpdateLinks() {
        System.setProperty ("sun.net.client.defaultReadTimeout", "7000");
        System.setProperty ("sun.net.client.defaultConnectTimeout", "7000");
    }

    /**
     * Periodically invoked task. Its purpose is to synchronize links with external definitions.
     */
    public void run() {
        if (log.isDebugEnabled()) log.debug("Starting task "+getJobName());
        try {
            Persistence persistence = PersistenceFactory.getPersistence();
            SQLTool sqlTool = SQLTool.getInstance();
            List<Server> servers = sqlTool.getValidServers();
            
            for (Iterator iter = servers.iterator(); iter.hasNext();) {
                Server server = (Server) iter.next();
                
                try {
                    synchronize(server, 0, server.getRssUrl(), persistence);
                } catch (Exception e) {
                    log.warn("Cannot update links for server "+server+"!", e);
                }
            }
            

            List remove = new ArrayList();
            GenericObject child;
            Relation relation;
            Element feed;
            int rid = 0;
            Item dynamicRss = (Item) persistence.findById(new Item(Constants.ITEM_DYNAMIC_CONFIGURATION));
            Document doc = (Document) dynamicRss.getData().clone();
            for (Iterator iter = doc.selectNodes("//feeds/feed").iterator(); iter.hasNext();) {
                feed = (Element) iter.next();
                try {
                    rid = Misc.parseInt(feed.attributeValue("relation"), -1);
                    relation = (Relation) persistence.findById(new Relation(rid));
                    child = persistence.findById(relation.getChild());
                    
                    try {
                        synchronize(child, relation.getId(), feed.getText(), persistence);
                    } catch (Exception e) {
                        log.warn("Cannot update links for url " + feed.getText() + ", parent relation is "+rid+"!", e);
                    }
                } catch (Exception e) {
                    remove.add(rid);
                }
            }

            if (remove.size() > 0) { // remove feeds for objects purged from persistence
                dynamicRss = (Item) persistence.findById(new Item(Constants.ITEM_DYNAMIC_CONFIGURATION));
                dynamicRss = (Item) dynamicRss.clone();
                doc = dynamicRss.getData();
                for (Iterator iter = remove.iterator(); iter.hasNext();) {
                    Integer id = (Integer) iter.next();
                    feed = (Element) doc.selectSingleNode("//feed[@relation="+id+"]");
                    if (feed != null)
                        feed.detach();
                }
                persistence.update(dynamicRss);
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
    protected void synchronize(GenericObject parent, int parentRelation, String rssUrl, Persistence persistence) {
        List storedLinks = getLinks(parent, persistence);
        List downloaded = parseRSS(rssUrl);
        int updated = 0;
        
        if (downloaded == null)
            return;

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
                persistence.update(existingLink);
            } else {
                link.setOwner(1);
                link.setServer(parent.getId());
                link.setFixed(false);
                persistence.create(link);

                Relation relation = new Relation(parent, link, parentRelation);
                persistence.create(relation);
                parent.addChildRelation(relation);
            }
            updated++;
        }
        if ( log.isDebugEnabled() && updated > 0 )
            log.debug("Updated "+updated+" links for "+parent.getClass()+", id="+parent.getId());
    }

    /**
     * Returns all links that are children of given object.
     * @param object initialized GenericObject
     * @return list of initialized Links
     */
    private List getLinks(GenericObject object, Persistence persistence) {
        List result = new ArrayList(linksPerFeed);
        Relation relation;
        GenericObject obj;
        Link link;
        for (Iterator iter = object.getChildren().iterator(); iter.hasNext();) {
            relation =  (Relation) iter.next();
            relation = (Relation) persistence.findById(relation);
            obj = persistence.findById(relation.getChild());
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
    protected List parseRSS(String rssUrl) {
        List result = new ArrayList();
        SyndFeedInput input = new SyndFeedInput();

        try {
            HttpClient client = new HttpClient();
            GetMethod method = new GetMethod(rssUrl);

            method.setFollowRedirects(true);
            method.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 3000);

            int status = client.executeMethod(method);
            if (status != HttpStatus.SC_OK)
                throw new IOException("Failed to get " + rssUrl + ", status: " + status);

            SyndFeed feed = input.build(new InputStreamReader(method.getResponseBodyAsStream()));
            List items = feed.getEntries();
            if ( items==null ) return result;
            for (Iterator iter = items.iterator(); iter.hasNext();) {
                SyndEntry entry = (SyndEntry) iter.next();
                String title = entry.getTitle();
                title = Tools.encodeSpecial(title);
                if ( title.length() > maxTitleLength )
                    title = title.substring(0, maxTitleLength);
//                String url = Tools.fixAmpersand(entry.getLink()); // encodovani se bude delat per request v sablone
                String url = entry.getLink();

                Link link = new Link();
                link.setUrl(url);
                link.setText(title);

                Date published = entry.getPublishedDate(); // http://tremulous.net/rss_feed.xml has invalid feed time
                if (published != null && published.getTime() < System.currentTimeMillis() + Constants.DAY_DURATION)
                    link.setUpdated(published);
                
                if (link.getUpdated() == null || link.getUpdated().before(new Date(0))) {
                    link.setUpdated(new Date());
                }

                result.add(link);
            }
        } catch (ParsingFeedException e) {
            log.warn("Invalid content in feed "+rssUrl+": "+e.getMessage());
            return null;
        } catch (IOException e) {
            log.warn("IO problems for "+rssUrl+": "+e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            log.warn("Cannot read " + rssUrl + ": " + e.getMessage());
            return null;
        }  catch (Exception e) {
            log.error("Cannot parse links from "+rssUrl, e);
            return null;
        }

        return result;
    }

    /**
     * Searches list of links for link with same text. If it doesn't find it,
     * it returns false. If the search is succesfull, link is removed from
     * list and method returns true.
     */
    private boolean removeLink(List list, Link link) {
        String tmp;
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
    public static List<Integer> getMaintainedServers() {
        SQLTool sqlTool = SQLTool.getInstance();
        List<Relation> servers = sqlTool.findServerRelationsInCategory(Constants.CAT_LINKS);
        List<Integer> numbers = new ArrayList(servers.size());
        
        for (Iterator iter = servers.iterator(); iter.hasNext();) {
            Relation rel = (Relation) iter.next();
            numbers.add(rel.getChild().getId());
        }
        
        return numbers;
    }

    /**
     * Finds fresh list of links of maintained feeds.
     * @return list of Servers, their children are Links
     */
    public static Map<Server, List<Link>> getMaintainedFeeds() {
        return getFeeds(Constants.CAT_LINKS);
    }
    
    public static Map<Server, List<Link>> getFeeds(int cat) {
        SQLTool sqlTool = SQLTool.getInstance();
        List<Relation> servers = sqlTool.findServerRelationsInCategory(cat);
        
        Tools.syncList(servers);

        Map<Server, List<Link>> result = new HashMap(servers.size());
        List<Link> allLinks = new ArrayList<Link>(servers.size() * 5);
        for (Iterator iter = servers.iterator(); iter.hasNext();) {
            Relation rel = (Relation) iter.next();
            Server server = (Server) rel.getChild();
            List children = server.getChildren();
            List links = new ArrayList(children.size());
            
            for (Iterator iter2 = children.iterator(); iter2.hasNext();) {
                Relation relation = (Relation) iter2.next();
                Link link = (Link) relation.getChild();
                links.add(link);
                allLinks.add(link);
            }
            result.put(server, links);
        }
        Tools.syncList(allLinks);
        return result;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        maxTitleLength = prefs.getInt(PREF_MAX_TITLE_LENGTH, 80);
        linksPerFeed = prefs.getInt(PREF_MAX_LINKS_PER_FEED, 5);
    }

    public static void main(String[] args) {
        org.apache.log4j.BasicConfigurator.configure();
        UpdateLinks updater = new UpdateLinks();
        updater.run();
    }
}
