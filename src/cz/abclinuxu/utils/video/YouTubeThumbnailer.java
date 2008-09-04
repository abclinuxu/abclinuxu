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

/**
 *
 * @author lubos
 */
public class YouTubeThumbnailer extends Thumbnailer {
    public String getThumbnailUrl(String url) {
        VideoServer server = EditVideo.videoServers.get("youtube");
        RE regexp = new RE(server.getUrlMatcher(), RE.MATCH_SINGLELINE);
            
        if (!regexp.match(url))
            return null;
        
        return "http://img.youtube.com/vi/" + regexp.getParen(1) + "/default.jpg";
    }
}
