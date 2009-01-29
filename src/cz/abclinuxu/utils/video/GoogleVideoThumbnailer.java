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

import cz.abclinuxu.data.view.VideoServer;
import cz.abclinuxu.servlets.html.edit.EditVideo;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import org.apache.regexp.RE;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import org.apache.log4j.Logger;

/**
 *
 * @author lubos
 */
public class GoogleVideoThumbnailer extends Thumbnailer {
    static Logger log = Logger.getLogger(GoogleVideoThumbnailer.class);
    
    public String getThumbnailUrl(String url) {
        VideoServer server = EditVideo.videoServers.get("googlevideo");
        RE regexp = new RE(server.getUrlMatcher(), RE.MATCH_SINGLELINE);
            
        if (!regexp.match(url)) {
            log.error("Unsupported URL passed: "+url);
            return null;
        }

        try {
            String urlFeed = "http://video.google.com/videofeed?docid="+regexp.getParen(1);

            InputStream is = new URL(urlFeed).openStream();
            
            SAXReader reader = new SAXReader();
            //reader.setEncoding("iso-8859-1"); // former hack that works on certain configurations
            Document document = reader.read(changeEncoding(is));
            
            Element elem = (Element) document.selectSingleNode("//media:thumbnail[@url]");
            
            if (elem == null) {
                log.error("Failed to find the XML attribute for "+url);
                return null;
            }
            
            return elem.attributeValue("url");
        } catch (Exception e) {
            log.error(url + ": " + e);
            return null;
        }
        
    }

    public static String streamToString(InputStream in) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;)
            out.append(new String(b, 0, n));

        return out.toString();
    }

    /**
     * This is an ugly hack to workaround a problem in Google's feed.
     */
    private static Reader changeEncoding(InputStream is) throws IOException {
        String ss = streamToString(is);
        ss = ss.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");
        return new StringReader(ss);
    }
}
