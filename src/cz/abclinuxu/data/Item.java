/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

import java.util.Date;

public class Item extends GenericDataObject {

    public Item() {
        super();
    }

    public Item(int id) {
        super(id);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Item: id=");
        sb.append(id);
        if ( owner!=0 ) sb.append(",owner="+owner);
        if ( data!=null ) sb.append(",data="+getDataAsString());
        if ( updated!=null ) sb.append(",updated="+updated);
        return sb.toString();
    }

    public boolean equals(Object o) {
        if ( !( o instanceof Item) ) return false;
        return super.equals(o);
    }
}
