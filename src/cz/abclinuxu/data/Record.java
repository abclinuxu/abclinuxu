/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

import java.util.Date;

public class Record extends GenericObject {
    /** identifier of owner of this object */
    protected int owner;
    /** creation date or last update of this object */
    protected Date updated;
    /** XML with data or this object */
    protected String data;


    public Record() {
        super();
    }

    public Record(int id) {
        super(id);
    }

    /**
     * @return owner's id
     */
    public int getOwner() {
        return owner;
    }

    /**
     * sets owner's id
     */
    public void setOwner(int owner) {
        this.owner = owner;
    }

    /**
     * @return last updated (or creation) date
     */
    public Date getUpdated() {
        return updated;
    }

    /**
     * sets last updated (or creation) date
     */
    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    /**
     * @return data of this object in XML
     */
    public String getData() {
        return data;
    }

    /**
     * sets data of this object in XML
     */
    public void setData(String data) {
        this.data = data;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Item: id=");
        sb.append(id);
        if ( owner!=0 ) sb.append(",owner="+owner);
        if ( data!=null ) sb.append(",data="+data);
        if ( updated!=null ) sb.append(",updated="+updated);
        return sb.toString();
    }

    public boolean equals(Object o) {
        if ( !( o instanceof Record) ) return false;
        Record p = (Record) o;
        if ( id==p.id && owner==p.owner && data.equals(p.data) ) return true;
        return false;
    }
}
