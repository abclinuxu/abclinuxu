/*
 * User: literakl
 * Date: Dec 10, 2001
 * Time: 7:34:34 AM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.data;

public class AccessRights extends GenericObject {

    /** whether the user is admin */
    protected boolean admin = false;

    public AccessRights() {
        super();
    }

    public AccessRights(int id) {
        super(id);
    }

    /**
     * @return Whether the user is admin
     */
    public boolean isAdmin() {
        return admin;
    }

    /**
     * sets, whether the user is admin
     */
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
