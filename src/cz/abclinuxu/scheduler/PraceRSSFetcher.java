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
import java.util.*;
import java.io.*;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * Fetches prace.abclinuxu.cz RSS.
 */
public class PraceRSSFetcher extends TimerTask implements Configurable {
    static Logger log = Logger.getLogger(PraceRSSFetcher.class);

    static final String PREF_FILE_TEMPLATE = "file_template";
    static final String PREF_FILE_INDEX = "file_index";
    static final String PREF_COUNT_TEMPLATE = "count_template";
    static final String PREF_COUNT_INDEX = "count_index";
    static final String PREF_URI = "uri";

    String fileTemplate, fileIndex, uri;
    int countTemplate, countIndex;

    public void run() {
        try {
            URL url = new URL(uri);
            InputStream is = url.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            ArrayList list = new ArrayList();
            Item item;
            int pos1;

            String line = reader.readLine();
            while(line!=null) {
                pos1 = line.indexOf('|');
                item = new Item();
                item.setName(line.substring(0,pos1));
                item.setUrl(UpdateLinks.fixAmpersand(line.substring(pos1+1)));
                list.add(item);
                line = reader.readLine();
            }

            Map env = new HashMap();
            env.put("ITEMS", list.subList(0, countIndex));
            String file = AbcConfig.calculateDeployedPath(fileIndex);
            FMUtils.executeTemplate("/include/misc/generate_prace.ftl", env, new File(file));

            Collections.reverse(list);
            env.put("ITEMS", list.subList(0, countTemplate));
            file = AbcConfig.calculateDeployedPath(fileTemplate);
            FMUtils.executeTemplate("/include/misc/generate_prace.ftl", env, new File(file));
        } catch (Exception e) {
            log.error("Job failed", e);
        }
    }

    public PraceRSSFetcher() {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(this);
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        uri = prefs.get(PREF_URI, null);
        fileIndex = prefs.get(PREF_FILE_INDEX, null);
        fileTemplate = prefs.get(PREF_FILE_TEMPLATE, null);
        countIndex = prefs.getInt(PREF_COUNT_INDEX, 5);
        countTemplate = prefs.getInt(PREF_COUNT_TEMPLATE, 10);
    }

    public static void main(String[] args) throws Exception {
        new PraceRSSFetcher().run();
    }

    public static class Item {
        String url, name;

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
    }
}
