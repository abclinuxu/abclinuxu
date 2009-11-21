/*
 *  Copyright (C) 2008 Leos Literak
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

package cz.abclinuxu.utils.video;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.net.URL;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser of thumbnail image for video from stream.cz
 * @author leos
 */
public class StreamThumbnailer extends Thumbnailer implements Configurable {
    static Logger log = Logger.getLogger(StreamThumbnailer.class);
    public static final String PREF_PATTERN = "RE_ID_PATTERN";

    static Pattern urlPattern;
    static {
        ConfigurationManager.getConfigurator().configureAndRememberMe(new StreamThumbnailer());
    }

    public String getThumbnailUrl(String url) {
        Matcher matcher = urlPattern.matcher(url);
        if (! matcher.find()) {
            log.error("Unsupported URL passed: "+url);
            return null;
        }

        try {
            String urlFeed = "http://flash.stream.cz/get_info/"+matcher.group(1);
            InputStream is = new URL(urlFeed).openStream();

            SAXReader reader = new SAXReader();
            Document document = reader.read(is);
            Element elem = (Element) document.selectSingleNode("/streamcz/video[@thumbnail]");
            if (elem == null) {
                log.error("Failed to find the XML attribute with thumbnail for "+url);
                return null;
            }

            return elem.attributeValue("thumbnail");
        } catch (Exception e) {
            log.error(url + ": " + e);
            return null;
        }

    }

    public void configure(Preferences prefs) throws ConfigurationException {
        String regexp = prefs.get(PREF_PATTERN, null);
        urlPattern = Pattern.compile(regexp);
    }
}