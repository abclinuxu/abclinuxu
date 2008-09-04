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

/**
 *
 * @author lubos
 */
public abstract class Thumbnailer {
    static final Thumbnailer youtubeThumbnailer = new YouTubeThumbnailer();
    static final Thumbnailer googleVideoThumbnailer = new GoogleVideoThumbnailer();
    
    public static Thumbnailer getInstance(String server) {
        if ("youtube".equals(server))
            return youtubeThumbnailer;
        else if ("googlevideo".equals(server))
            return googleVideoThumbnailer;
        else
            return null;
    }
    /**
     * Retrieves the URL of a thumbnail attached to the specified video
     * @param url Video URL
     * @return Thumbnail URL
     */
    public abstract String getThumbnailUrl(String url);
}
