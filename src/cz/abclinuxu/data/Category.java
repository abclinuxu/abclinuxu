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
    /** tells, whether normal users (non-administrators) can add items to this category */
    protected boolean open;


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
        return open;
    }

    /**
     * sets whether normal users may add content to this category
     */
    public void setOpen(boolean open) {
        this.open = open;
    }

    /**
     * Initialize this object with values from <code>obj</code>, if
     * this.getClass.equals(obj.getClass()).
     */
    public void synchronizeWith(GenericObject obj) {
        if ( ! (obj instanceof Category) ) return;
        if ( obj==this ) return;
        super.synchronizeWith(obj);
        open = ((Category)obj).isOpen();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Category: id="+id);
        sb.append(", "+content.size()+" children");
        if ( owner!=0 ) sb.append(",owner="+owner);
        if ( data!=null ) sb.append(",data="+getDataAsString());
//        if ( updated!=null ) sb.append(",updated="+updated);
        if ( open ) sb.append(", otevrena"); else sb.append(", uzavrena");
        return sb.toString();
    }

    public boolean preciseEquals(Object o) {
        if ( !( o instanceof Category) ) return false;
        if ( id!=((GenericObject)o).getId() ) return false;
        if ( open!=((Category)o).isOpen() ) return false;
        if ( owner!=((GenericDataObject)o).getOwner() ) return false;
        if ( ! InstanceUtils.same(getDataAsString(),((GenericDataObject)o).getDataAsString()) ) return false;
        return true;
    }

    public int hashCode() {
        String tmp = "Category"+id;
        return tmp.hashCode();
    }
}
