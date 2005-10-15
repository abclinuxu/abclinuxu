/*
 * User: literakl
 * Date: 18.9.2004
 * Time: 11:55:07
 */
package cz.abclinuxu.scheduler;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.utils.freemarker.Tools;

import java.util.prefs.Preferences;
import java.util.*;
import java.io.*;
import java.net.URL;

import org.apache.log4j.Logger;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndEntry;

/**
 * Fetches Unixshop RSS.
 */
public class OKSystemFetcher extends TimerTask implements Configurable {
    static Logger log = Logger.getLogger(OKSystemFetcher.class);

    static final String PREF_FILE = "file";
    static final String PREF_URI = "uri";
    static final String PREF_MAX_ITEMS = "max";

    String fileName, uri;
    int max;

    public void run() {
        log.debug("Fetching OKSystem RSS starts ..");
        try {
            ArrayList result = new ArrayList();
            SyndFeedInput input = new SyndFeedInput();
            String title;
            RssItem rssItem;

            SyndFeed feed = input.build(new XmlReader(new URL(uri)));
            List items = feed.getEntries();
            if (items != null) {
                int i = 0;
                for (Iterator iter = items.iterator(); iter.hasNext() && i < max; i++) {
                    SyndEntry entry = (SyndEntry) iter.next();
                    title = entry.getTitle();
                    title = Tools.encodeSpecial(title);

                    rssItem = new RssItem();
                    rssItem.setUrl(entry.getLink());
                    rssItem.setTitle(title);
                    rssItem.setDescription(entry.getDescription().getValue());

                    result.add(rssItem);
                }
            }

            Map env = new HashMap();
            env.put("ITEMS", result);
            String file = AbcConfig.calculateDeployedPath(fileName);
            FMUtils.executeTemplate("/include/misc/generate_oksystem.ftl", env, new File(file));
            log.debug("OKSystem include file generated");
        } catch (IOException e) {
            log.error("IO problems for " + uri + ": " + e.getMessage());
        } catch (Exception e) {
            log.error("Cannot parse links from " + uri, e);
        }
    }

    public OKSystemFetcher() {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(this);
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        uri = prefs.get(PREF_URI, null);
        fileName = prefs.get(PREF_FILE, null);
        max = prefs.getInt(PREF_MAX_ITEMS, 3);
    }

    public static void main(String[] args) throws Exception {
        new OKSystemFetcher().run();
    }

    public static class RssItem {
        String url, title, description;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
