/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

import cz.abclinuxu.utils.InstanceUtils;

import java.util.Date;

/**
 * Category is a node of the tree (not leaf)
 */
public class Category extends GenericDataObject {

    /** normal category, only admin can insert content */
    public static final int CLOSED_CATEGORY = 0;
    /** normal category, every logged user can insert content */
    public static final int OPEN_CATEGORY = 1;


    public Category() {
        super();
    }

    public Category(int id) {
        super(id);
    }

    /**
     * @return whether normal users may add content to this category
     */
    public boolean isOpen() {
        return type==OPEN_CATEGORY;
    }

    /**
     * sets whether normal users may add content to this category
     */
    public void setOpen(boolean open) {
        type = (open)? OPEN_CATEGORY:CLOSED_CATEGORY;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Category: id="+id);
        sb.append(", "+content.size()+" children");
        if ( owner!=0 ) sb.append(",owner="+owner);
        if ( documentHandler!=null ) sb.append(",data="+getDataAsString());
//        if ( updated!=null ) sb.append(",updated="+updated);
        if ( isOpen() ) sb.append(", otevrena"); else sb.append(", uzavrena");
        return sb.toString();
    }

    public boolean preciseEquals(Object o) {
        if ( !( o instanceof Category) ) return false;
        if ( id!=((GenericObject)o).getId() ) return false;
        if ( type!=((GenericDataObject)o).type ) return false;
        if ( owner!=((GenericDataObject)o).owner ) return false;
        if ( ! InstanceUtils.same(getDataAsString(),((GenericDataObject)o).getDataAsString()) ) return false;
        return true;
    }

    public int hashCode() {
        String tmp = "Category"+id;
        return tmp.hashCode();
    }
}
