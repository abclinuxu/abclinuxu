/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

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

    public String toString() {
        StringBuffer sb = new StringBuffer("Category: id=");
        sb.append(id);
        if ( owner!=0 ) sb.append(",owner="+owner);
        if ( data!=null ) sb.append(",data="+getDataAsString());
        if ( updated!=null ) sb.append(",updated="+updated);
        if ( open ) sb.append(", otevrena"); else sb.append(", uzavrena");
        return sb.toString();
    }

    public boolean equals(Object o) {
        if ( !( o instanceof Category) ) return false;
        return super.equals(o) && ((Category)o).isOpen();
    }
}
