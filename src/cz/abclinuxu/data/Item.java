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

import cz.abclinuxu.servlets.Constants;

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
    /** message */
    public static final int MESSAGE = 13;
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
    /** author of articles **/
    public static final int AUTHOR = 19;
    /** simple trivia game */
    public static final int TRIVIA = 20;
    /** simple trivia game */
    public static final int HANGMAN = 21;
    /** article series */
    public static final int SERIES = 22;
    /** personality */
    public static final int PERSONALITY = 23;
    /** screenshot */
    public static final int DESKTOP = 24;
    /** generic type for imported data, its content depends on data source */
    public static final int IMPORT = 25;
    /** an event in the calendar of Linux events */
    public static final int EVENT = 26;
    /** unpublished event */
    public static final int UNPUBLISHED_EVENT = 27;
    /** video items, standalone or as attachments */
    public static final int VIDEO = 28;
    /** advertisement (banner) object */
    public static final int ADVERTISEMENT = 29;
    /** contract  template */
    public static final int CONTRACT_TEMPLATE = 30;
    /** contract with concrete author */
    public static final int SIGNED_CONTRACT = 31;
    /** topic for author's work */
    public static final int TOPIC = 32;


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
            case MESSAGE: sb.append("Message");break;
            case BLOG:
            case UNPUBLISHED_BLOG: sb.append("Blog");break;
            case FAQ: sb.append("FAQ");break;
            case TOC: sb.append("TOC");break;
            case SOFTWARE: sb.append("Software");break;
            case BAZAAR: sb.append("Bazaar");break;
            case AUTHOR: sb.append("Author");break;
            case TRIVIA: sb.append("Trivia");break;
            case SERIES: sb.append("Series");break;
            case PERSONALITY: sb.append("Personality");break;
            case DESKTOP: sb.append("Desktop");break;
            case IMPORT: sb.append("Import");break;
            case UNPUBLISHED_EVENT:
            case EVENT: sb.append("Event"); break;
            case VIDEO: sb.append("Video"); break;
            case CONTRACT_TEMPLATE: sb.append("Contract template"); break;
            case SIGNED_CONTRACT: sb.append("Signed contract"); break;
            case TOPIC: sb.append("Topic"); break;
            
            default: sb.append("Unknown Item");
        }
        sb.append(": id=").append(id);
        if (title != null)
            sb.append(", title=").append(title);
        return sb.toString();
    }

    public int hashCode() {
        String tmp = "Item"+id;
        return tmp.hashCode();
    }

    public String getTypeString() {
        switch (this.type) {
            case ARTICLE: return Constants.TYPE_ARTICLE;
            case AUTHOR: return Constants.TYPE_AUTHOR;
            case BLOG: return Constants.TYPE_STORY;
            case CONTENT: return Constants.TYPE_CONTENT;
            case DICTIONARY: return Constants.TYPE_DICTIONARY;
            case DISCUSSION: return Constants.TYPE_DISCUSSION;
            case DRIVER: return Constants.TYPE_DRIVER;
            case FAQ: return Constants.TYPE_FAQ;
            case HARDWARE: return Constants.TYPE_HARDWARE;
            case NEWS: return Constants.TYPE_NEWS;
            case PERSONALITY: return Constants.TYPE_PERSONALITY;
            case SERIES: return Constants.TYPE_SERIES;
            case SOFTWARE: return Constants.TYPE_SOFTWARE;
            case DESKTOP: return Constants.TYPE_SCREENSHOT;
            case EVENT: return Constants.TYPE_EVENT;
            case VIDEO: return Constants.TYPE_VIDEO;
            case CONTRACT_TEMPLATE: return Constants.TYPE_CONTRACT;
            case TOPIC: return Constants.TYPE_TOPIC;
            default: return Constants.TYPE_OTHER;
        }
    }
}
