/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

import cz.abclinuxu.utils.InstanceUtils;

import java.util.Date;

public class Item extends GenericDataObject {

    /** Leaf of the category. It contains at least one hardware or software record. */
    public static final int MAKE = 1;
    /** Article header. The article is consisted from this header and at least one Article record. */
    public static final int ARTICLE = 2;
    /** Discussion may contain one DiscussionQuestion and many DiscussionItems. */
    public static final int DISCUSSION = 3;
    /** User's request to administrators. */
    public static final int REQUEST = 4;

    /** Specifies type of Item. You must set it, before you stores it with Persistance! */
    int type = 0;


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
        if ( ! (obj instanceof Item) ) return;
        super.synchronizeWith(obj);
        type = ((Item)obj).getType();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        switch ( type ) {
            case 1: sb.append("Make");break;
            case 2: sb.append("Article");break;
            case 3: sb.append("Discussion");break;
            case 4: sb.append("Request");break;
            default: sb.append("Unknown Item");
        }
        sb.append(": id="+id);
        sb.append(", "+content.size()+" children");
        if ( owner!=0 ) sb.append(",owner="+owner);
        if ( data!=null ) sb.append(",data="+getDataAsString());
        return sb.toString();
    }

    public boolean preciseEquals(Object o) {
        if ( !( o instanceof Item) ) return false;
        if ( id!=((GenericObject)o).getId() ) return false;
        if ( type!=((Item)o).getType() ) return false;
        if ( owner!=((GenericDataObject)o).getOwner() ) return false;
        if ( ! InstanceUtils.same(getDataAsString(),((GenericDataObject)o).getDataAsString()) ) return false;
        return true;
    }

    public int hashCode() {
        String tmp = "Item"+id;
        return tmp.hashCode();
    }
}
