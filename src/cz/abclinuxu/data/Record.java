/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

import cz.abclinuxu.utils.Misc;

import java.util.Collections;

public class Record extends GenericDataObject {

    public static final int HARDWARE = 1;
    public static final int SOFTWARE = 2;
    /** part of the article, each article is consisted from article header and at least one record */
    public static final int ARTICLE = 3;
    /** one reaction in Discussion */
    public static final int DISCUSSION = 4;

    /** constants to be used in Data for HARDWARE record */
    public static final String VAL_HW_DRIVER_KERNEL = "kernel";
    public static final String VAL_HW_DRIVER_XFREE = "xfree";
    public static final String VAL_HW_DRIVER_MAKER = "maker";
    public static final String VAL_HW_DRIVER_OTHER = "other";
    public static final String VAL_HW_DRIVER_NONE = "none";

    public static final String VAL_HW_PRICE_VERYLOW = "verylow";
    public static final String VAL_HW_PRICE_LOW = "low";
    public static final String VAL_HW_PRICE_GOOD = "good";
    public static final String VAL_HW_PRICE_HIGH = "high";
    public static final String VAL_HW_PRICE_TOOHIGH = "toohigh";


    public Record() {
        super();
        content = Collections.EMPTY_LIST;
    }

    public Record(int id) {
        super(id);
        content = Collections.EMPTY_LIST;
    }

    public Record(int id, int type) {
        super(id);
        this.type = type;
        content = Collections.EMPTY_LIST;
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
        sb.append(": id="+id);
        sb.append(", "+content.size()+" children");
        if ( owner!=0 ) sb.append(",owner="+owner);
        if ( documentHandler!=null ) sb.append(",data="+getDataAsString());
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
