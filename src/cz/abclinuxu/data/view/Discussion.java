/*
 * User: literakl
 * Date: 2.11.2003
 * Time: 18:52:29
 */
package cz.abclinuxu.data.view;

import java.util.AbstractList;
import java.util.Arrays;

/**
 * Discussion is an container for comments. Though it is
 * derived from Container, it has special behaviour.
 * It can store only Comments in append-only fashion.
 */
public class Discussion extends  AbstractList {
    private Comment[] comments;
    private int size = 0;

    public Discussion() {
        comments = new Comment[5];
    }

    public Discussion(int size) {
        comments = new Comment[size];
    }

    public Object get(int index) {
        return comments[index];
    }

    public int size() {
        return size;
    }

    public Object set(int index, Object element) {
        Object previous = comments[index];
        comments[index] = (Comment) element;
        return previous;
    }

    public void add(int index, Object element) {
        int length = comments.length;
        if ( index>length || index<0 )
            throw new ArrayIndexOutOfBoundsException("Index is "+index+", size is "+length);
        else if (index==length) {
            Comment[] copy = new Comment[2*index];
            System.arraycopy(comments,0,copy,0,index);
            Arrays.fill(comments,null);
            comments = copy;
        }

        System.arraycopy(comments,index,comments,index+1,size-index);
        comments[index] = (Comment) element;
        size++;
    }
}
