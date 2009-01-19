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
import org.apache.regexp.RE;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 *
 * @author lubos
 */
public class GoogleVideoThumbnailer extends Thumbnailer {
    public String getThumbnailUrl(String url) {
        VideoServer server = EditVideo.videoServers.get("googlevideo");
        RE regexp = new RE(server.getUrlMatcher(), RE.MATCH_SINGLELINE);
            
        if (!regexp.match(url))
            return null;
        
        try {
            String urlFeed = "http://video.google.com/videofeed?docid="+regexp.getParen(1);
            
            SAXReader reader = new SAXReader();
            reader.setEncoding("iso-8859-1");
            Document document = reader.read(urlFeed);
            
            Element elem = (Element) document.selectSingleNode("//media:thumbnail[@url]");
            
            if (elem == null)
                return null;
            
            return elem.attributeValue("url");
        } catch (Exception e) {
            return null;
        }
        
    }
}
