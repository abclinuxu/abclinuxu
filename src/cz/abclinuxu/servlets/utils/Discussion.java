/*
 * User: literakl
 * Date: Feb 21, 2002
 * Time: 8:42:42 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.utils;

import cz.abclinuxu.data.Record;

import java.util.List;
import java.util.ArrayList;

/**
 * Discussion is data class, which simplifies
 * display of Records with type set to Discussion.
 * It is not supported by persistance in any mean.
 * <p>
 * Requirements:<br>
 * <code>record</code> must not be empty and record.getType==Record.Discussion, it must be initialized already.<br>
 * <code>list</code> may be empty or it contains at least one Discussion.
 * It cannot contain objects of any other class than Discussion.
 */
public class Discussion {

    Record record;
    List list;

    /**
     * Creates new instance of Discussion. If record is not initialized or its type is not Discussion,
     * it throws an exception.
     * @throws java.lang.IllegalArgumentException
     */
    public Discussion(Record record) {
        if ( record.getType()!=Record.DISCUSSION ) throw new IllegalArgumentException("Record is not discussion!");
        this.record = record;
    }

    /**
     * Adds Discussion to <code>list</code>. You create a new branch with this method.
     */
    public void add(Discussion discussion) {
        if ( list==null ) list = new ArrayList(4);
        list.add(discussion);
    }

    /**
     * Adds new record as Discussion to <code>list</code>. You create a new branch with this method.
     * @throws java.lang.IllegalArgumentException
     */
    public Discussion add(Record record) {
        Discussion discussion = new Discussion(record);
        if ( list==null ) list = new ArrayList(4);
        list.add(discussion);
        return discussion;
    }

    /**
     * @return Record associated with this node in the tree.
     */
    public Record getRecord() {
        return record;
    }

    /**
     * @return Children of this node in the tree.
     */
    public List getList() {
        return list;
    }

    public int hashCode() {
        return record.getId();
    }

    public boolean equals(Object obj) {
        if ( !(obj instanceof Discussion) ) return false;
        return record.getId()==((Discussion)obj).record.getId();
    }
}
