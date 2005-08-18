/*
 * User: literakl
 * Date: 29.11.2003
 * Time: 17:34:16
 */
package cz.abclinuxu.data.view;

import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Item;

import java.util.List;
import java.util.Iterator;

/**
 * Item of Access Control List
 */
public class ACL {
    public static final String RIGHT_READ = "read";

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

    /**
     * Finds out, whether the user has specified right. User has the right,
     * if right is not specified in acls, or if he is explicitely allowed
     * for this right or if he is member of group, that is allowed for this
     * right. If the right is specified and he or his group is not explicitely
     * specified, the access is not granted.
     * @param user initialized user.
     * @param right name of right.
     * @param acls list of ACL instances
     * @return
     */
    public static boolean isGranted(User user, String right, List acls) {
        boolean rightFound = false;
        for ( Iterator iter = acls.iterator(); iter.hasNext(); ) {
            ACL acl = (ACL) iter.next();
            if (!right.equals(acl.right))
                continue;
            rightFound = true;

            if (acl.user!=null && acl.user.getId()==user.getId()) {
                return acl.value;
            }

            if (acl.group!=null && user.isMemberOf(acl.group.getId())) {
                return acl.value;
            }
        }
        return !rightFound;
    }

    /**
     * Finds out, whether anonymous user has specified right. User has the right,
     * if right is not specified in acls or if he is member of group, that is
     * allowed for this right. If neither is true, access is not granted.
     * @param right name of right.
     * @param acls list of ACL instances
     * @return true, if access is granted
     */
    public static boolean isGranted(String right, List acls) {
        boolean rightFound = false;
        for ( Iterator iter = acls.iterator(); iter.hasNext(); ) {
            ACL acl = (ACL) iter.next();
            if (!right.equals(acl.right))
                continue;
            rightFound = true;
        }
        return !rightFound;
    }

    public String toString() {
        return "ACL ("+id+") "+right+": "+value;
    }
}
