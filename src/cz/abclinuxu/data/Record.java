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

public class Record extends GenericDataObject {
    /** deprecated */
    public static final int HARDWARE = 1;
    /** deprecated */
    public static final int SOFTWARE = 2;
    /** part of the article, each article is consisted from article header and at least one record */
    public static final int ARTICLE = 3;
    /** one reaction in Discussion */
    public static final int DISCUSSION = 4;
    /** deprecated */
    public static final int DICTIONARY = 5;

    public Record() {
        super();
    }

    public Record(int id) {
        super(id);
    }

    public Record(int id, int type) {
        super(id);
        this.type = type;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        switch ( type ) {
            case 1: sb.append("HardwareRecord");break;
            case 2: sb.append("SoftwareRecord");break;
            case 3: sb.append("ArticleRecord");break;
            case 4: sb.append("DiscussionRecord");break;
            default: sb.append("Unknown Record");
        }
        sb.append(": id=").append(id);
        if ( owner!=0 ) sb.append(",owner=").append(owner);
//        if ( documentHandler!=null ) sb.append(",data=").append(getDataAsString());
        return sb.toString();
    }

    public boolean preciseEquals(Object o) {
        if ( !( o instanceof Record) ) return false;
        if ( id!=((GenericObject)o).getId() ) return false;
        if ( type!=((GenericDataObject)o).type ) return false;
        if ( owner!=((GenericDataObject)o).owner ) return false;
        if ( ! Misc.same(getDataAsString(),((GenericDataObject)o).getDataAsString()) ) return false;
        return true;
    }

    public int hashCode() {
        String tmp = "Record"+id;
        return tmp.hashCode();
    }
}
