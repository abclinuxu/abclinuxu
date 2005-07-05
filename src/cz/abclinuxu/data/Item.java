/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

import cz.abclinuxu.utils.Misc;

public class Item extends GenericDataObject {

    /** Leaf of the category. It contains at least one hardware or software record. */
    public static final int MAKE = 1;
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
            case 1: sb.append("Make");break;
            case 2: sb.append("Article");break;
            case 3: sb.append("Discussion");break;
            case 4: sb.append("Request");break;
            case 5: sb.append("Driver");break;
            case 6: sb.append("Survey");break;
            case 7: sb.append("News");break;
            case 8: sb.append("Group");break;
            default: sb.append("Unknown Item");
        }
        sb.append(": id="+id);
        if ( owner!=0 ) sb.append(",owner="+owner);
        if ( subType!=null ) sb.append(",subtype="+subType);
        if ( documentHandler!=null ) sb.append(",data="+getDataAsString());
        return sb.toString();
    }

    public boolean preciseEquals(Object o) {
        if ( !( o instanceof Item) ) return false;
        if ( id!=((GenericObject)o).getId() ) return false;
        if ( type!=((GenericDataObject)o).type ) return false;
        if ( owner!=((GenericDataObject)o).owner ) return false;
        if ( ! Misc.same(getDataAsString(),((GenericDataObject)o).getDataAsString()) ) return false;
        return true;
    }

    public int hashCode() {
        String tmp = "Item"+id;
        return tmp.hashCode();
    }
}
