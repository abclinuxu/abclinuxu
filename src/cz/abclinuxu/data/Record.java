/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

import java.util.Date;

public class Record extends GenericDataObject {

    public Record() {
        super();
    }

    public Record(int id) {
        super(id);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Record: id=");
        sb.append(id);
        if ( owner!=0 ) sb.append(",owner="+owner);
        if ( data!=null ) sb.append(",data="+getDataAsString());
        if ( updated!=null ) sb.append(",updated="+updated);
        return sb.toString();
    }

    public boolean equals(Object o) {
        if ( !( o instanceof Record) ) return false;
        return super.equals(o);
    }
}
