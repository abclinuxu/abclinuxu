/*
 * User: literakl
 * Date: 29.11.2003
 * Time: 17:34:16
 */
package cz.abclinuxu.data.view;

import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Item;

/**
 * Item of Access Control List
 */
public class ACL {
    private int id;
    private User user;
    private Item group;
    private String right;
    private boolean value;

    public ACL(int id, User user) {
        this.id = id;
        this.user = user;
    }

    public ACL(int id, Item group) {
        this.id = id;
        this.group = group;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Item getGroup() {
        return group;
    }

    public void setGroup(Item group) {
        this.group = group;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }

    public void setRight(String right, boolean value) {
        this.right = right;
        this.value = value;
    }

    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public String toString() {
        return "ACL ("+id+") "+right+": "+value;
    }
}
