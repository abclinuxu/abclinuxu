/*
 *  Copyright (C) 2007 Leos Literak
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
package cz.abclinuxu.data.view;

import cz.abclinuxu.data.Relation;

import cz.abclinuxu.servlets.Constants;
import java.text.Collator;
import java.util.Comparator;
import java.util.Date;

/**
 * User's bookmark item.
 * @author literakl
 * @since 7.8.2007
 */
public class Bookmark {
    String title, prefix, type, url;
    Relation relation;
    Date modified;

    public Bookmark(Relation relation, String title, String prefix, String type) {
        this.relation = relation;
        this.title = title;
        this.prefix = prefix;
        this.type = type;
    }
    
    public Bookmark(String url, String title) {
        this.type = Constants.TYPE_EXTERNAL_DOCUMENT;
        this.title = title;
        this.url = url;
    }

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation r) {
        this.relation = r;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public Date getModified() {
        return modified;
    }
    
    public void setModified(Date modified) {
        this.modified = modified;
    }
    
    public String getNiceType() {
        if (Constants.TYPE_ARTICLE.equals(type))
            return "článek";
        else if (Constants.TYPE_CONTENT.equals(type))
            return "dokument";
        else if (Constants.TYPE_DICTIONARY.equals(type))
            return "pojem";
        else if (Constants.TYPE_DISCUSSION.equals(type))
            return "diskuse";
        else if (Constants.TYPE_DRIVER.equals(type))
            return "ovladač";
        else if (Constants.TYPE_FAQ.equals(type))
            return "FAQ";
        else if (Constants.TYPE_HARDWARE.equals(type))
            return "hardware";
        else if (Constants.TYPE_NEWS.equals(type))
            return "zprávička";
        else if (Constants.TYPE_OTHER.equals(type))
            return "ostatní";
        else if (Constants.TYPE_POLL.equals(type))
            return "anketa";
        else if (Constants.TYPE_SECTION.equals(type))
            return "sekce";
        else if (Constants.TYPE_SOFTWARE.equals(type))
            return "software";
        else if (Constants.TYPE_STORY.equals(type))
            return "blog";
        else if (Constants.TYPE_AUTHOR.equals(type))
            return "autor";
        else if (Constants.TYPE_SERIES.equals(type))
            return "seriál";
        else if (Constants.TYPE_EVENT.equals(type))
            return "akce";
        else if (Constants.TYPE_EXTERNAL_DOCUMENT.equals(type))
            return "externí odkaz";
        else if (type != null)
            return type;
        else
            return "";
    }
    
    public static class TitleComparator implements Comparator<Bookmark> {
        public int compare(Bookmark a, Bookmark b) {
            return Collator.getInstance().compare(a.title, b.title);
        }
    }
    
    public static class ModifiedComparator implements Comparator<Bookmark> {
        public int compare(Bookmark a, Bookmark b) {
            if (a.modified == null && b.modified == null)
                return 0;
            if (a.modified == null)
                return 1;
            else if (b.modified == null)
                return -1;
            else
                return a.modified.compareTo(b.modified);
        }
    }
    
    public static class TypeComparator implements Comparator<Bookmark> {
        public int compare(Bookmark a, Bookmark b) {
            return Collator.getInstance().compare(a.getNiceType(), b.getNiceType());
        }
    }
}
