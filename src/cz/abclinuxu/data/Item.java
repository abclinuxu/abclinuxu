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
package cz.abclinuxu.data;

import cz.abclinuxu.utils.Misc;

public class Item extends GenericDataObject {

    /** Hardware */
    public static final int HARDWARE = 1;
    /**
     * Article header. The article is consisted from this header and at least
     * one Article record. Created field has meaning of Published dated.
     **/
    public static final int ARTICLE = 2;
    /** Discussion defines one discussion. It may contain initial question. */
    public static final int DISCUSSION = 3;
    /** User's request to administrators. */
    public static final int REQUEST = 4;
    /** driver information */
    public static final int DRIVER = 5;
    /** data for survey */
    public static final int SURVEY = 6;
    /** news */
    public static final int NEWS = 7;
    /** group */
    public static final int GROUP = 8;
    /** royalties for articles */
    public static final int ROYALTIES = 9;
    /** dictionary */
    public static final int DICTIONARY = 10;
    /** unspecified content to be served */
    public static final int CONTENT = 11;
    /** one story of the blog */
    public static final int BLOG = 12;
    /** one entry in guestbook */
    public static final int GUESTBOOK = 13;
    /** one frequently asked question */
    public static final int FAQ = 14;
    /** blog story waiting for publishing */
    public static final int UNPUBLISHED_BLOG = 15;
    /** table of contents for content hierarchy */
    public static final int TOC = 16;
    /** software item **/
    public static final int SOFTWARE = 17;
    /** advertisement in bazaar */
    public static final int BAZAAR = 18;
    /** simple trivia game */
    public static final int TRIVIA = 20;
    /** simple trivia game */
    public static final int HANGMAN = 21;

    public Item() {
        super();
    }

    public Item(int id) {
        super(id);
    }

    public Item(int id, int type) {
        super(id);
        this.type = type;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        switch ( type ) {
            case HARDWARE: sb.append("Hardware");break;
            case ARTICLE: sb.append("Article");break;
            case DISCUSSION: sb.append("Discussion");break;
            case REQUEST: sb.append("Request");break;
            case DRIVER: sb.append("Driver");break;
            case SURVEY: sb.append("Survey");break;
            case NEWS: sb.append("News");break;
            case GROUP: sb.append("Group");break;
            case ROYALTIES: sb.append("Royalties");break;
            case DICTIONARY: sb.append("Dictionary");break;
            case CONTENT: sb.append("Content");break;
            case BLOG:
            case UNPUBLISHED_BLOG: sb.append("Blog");break;
            case FAQ: sb.append("FAQ");break;
            case TOC: sb.append("TOC");break;
            case SOFTWARE: sb.append("Software");break;
            case BAZAAR: sb.append("Bazaar");break;
            default: sb.append("Unknown Item");
        }
        sb.append(": id=").append(id);
        if ( owner!=0 ) sb.append(",owner=").append(owner);
        if ( subType!=null ) sb.append(",subtype=").append(subType);
        if ( documentHandler!=null ) sb.append(",data=").append(getDataAsString());
        return sb.toString();
    }

    public boolean preciseEquals(Object o) {
        if ( !( o instanceof Item) ) return false;
        if ( id!=((GenericObject)o).getId() ) return false;
        if ( type!=((GenericDataObject)o).type ) return false;
        if ( owner!=((GenericDataObject)o).owner ) return false;
        return Misc.same(getDataAsString(), ((GenericDataObject) o).getDataAsString());
    }

    public int hashCode() {
        String tmp = "Item"+id;
        return tmp.hashCode();
    }
}
