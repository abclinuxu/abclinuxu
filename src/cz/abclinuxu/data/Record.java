/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

import cz.abclinuxu.utils.InstanceUtils;

import java.util.Date;

public class Record extends GenericDataObject {

    public static final int HARDWARE = 1;
    public static final int SOFTWARE = 2;
    /** part of the article, each article is consisted from article header and at least one record */
    public static final int ARTICLE = 3;
    /** initial question in the discussion */
    public static final int DISCUSSION_QUESTION = 4;
    /** one reaction in Discussion */
    public static final int DISCUSSION_ITEM = 5;

    /** Specifies type of record. You must set it, before you stores it with Persistance! */
    int type = 0;

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

    /**
     * @return Type of Record
     */
    public int getType() {
        return type;
    }

    /**
     * Sets type of Record
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Initialize this object with values from <code>obj</code>, if
     * this.getClass.equals(obj.getClass()).
     */
    public void synchronizeWith(GenericObject obj) {
        if ( ! (obj instanceof Record) ) return;
        super.synchronizeWith(obj);
        type = ((Record)obj).getType();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        switch ( type ) {
            case 1: sb.append("HardwareRecord");break;
            case 2: sb.append("SoftwareRecord");break;
            case 3: sb.append("ArticleRecord");break;
            case 4: sb.append("DiscussionQuestion");break;
            case 5: sb.append("DiscussionItem");break;
            default: sb.append("Unknown Record");
        }
        sb.append(": id="+id);
        if ( owner!=0 ) sb.append(",owner="+owner);
        if ( data!=null ) sb.append(",data="+getDataAsString());
        if ( updated!=null ) sb.append(",updated="+updated);
        return sb.toString();
    }

    public boolean preciseEquals(Object o) {
        if ( !( o instanceof Record) ) return false;
        if ( id!=((GenericObject)o).getId() ) return false;
        if ( type!=((Record)o).getType() ) return false;
        if ( owner!=((GenericDataObject)o).getOwner() ) return false;
        if ( ! InstanceUtils.same(getDataAsString(),((GenericDataObject)o).getDataAsString()) ) return false;
        return true;
    }

    public int hashCode() {
        String tmp = "Record"+id;
        return tmp.hashCode();
    }
}
