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

import java.util.prefs.Preferences;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.TimerTask;
import java.io.*;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * Fetches Unixshop RSS.
 */
public class UnixshopFetcher extends TimerTask implements Configurable {
    static Logger log = Logger.getLogger(UnixshopFetcher.class);

    static final String PREF_FILE = "file";
    static final String PREF_URI = "uri";

    String fileName, uri;

    public void run() {
        try {
            URL url = new URL(uri);
            InputStream is = url.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            ArrayList list = new ArrayList();
            Item item;
            int pos1,pos2;
            String tmp;

            String line = reader.readLine();
            while(line!=null) {
                pos1 = line.indexOf('|');
                pos2 = line.indexOf('|', pos1+1);
                item = new Item();
                item.setUrl(line.substring(0,pos1));
                item.setName(line.substring(pos1+1,pos2));
                tmp = line.substring(pos2+1);
                item.setPrice(Integer.parseInt(tmp));
                list.add(item);
                line = reader.readLine();
            }

            Map env = new HashMap();
            env.put("ITEMS", list);
            String file = AbcConfig.calculateDeployedPath(fileName);
            FMUtils.executeTemplate("/include/misc/generate_unixshop.ftl", env, new File(file));
        } catch (Exception e) {
            log.error("Job failed", e);
        }
    }

    public UnixshopFetcher() {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(this);
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        uri = prefs.get(PREF_URI, null);
        fileName = prefs.get(PREF_FILE, null);
    }

    public static void main(String[] args) throws Exception {
        new UnixshopFetcher().run();
    }

    public static class Item {
        String url, name;
        int price;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }
    }
}
