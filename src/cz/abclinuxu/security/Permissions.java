/*
 * Copyright (C) 2005 Leos Literak
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; see the file COPYING. If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package cz.abclinuxu.security;

import java.util.Arrays;
import java.util.List;

/**
 * Immutable permissions object. Represents only a part of permissions, such as
 * permissions for owner group or others. Total permission object than consist
 * of one integer value, which can be mapped to three permission objects.
 * <p/>
 * <p/>
 * Contains factory methods to simplify creation.
 *
 * @author lubos
 * @author kapy
 */
public class Permissions {

    /**
     * GenericDataObject: the right to modify its contents Category: the right
     * to create/delete subcategories and modify the category in question
     */
    public static final int PERMISSION_MODIFY = 1;

    /**
     * GenericDataObject: the right to delete the object Category: no special
     * right, but it's propagated to child items
     */
    public static final int PERMISSION_DELETE = 2;

    /**
     * GenericDataObject: the right to derive content Category: the right to
     * create a child GenericDataObject
     */
    public static final int PERMISSION_CREATE = 4;

    /**
     * Position of owner permissions bits
     */
    public static final int PERMISSIONS_OWNER_SHIFT = 16;

    /**
     * Position of group permissions bits
     */
    public static final int PERMISSIONS_GROUP_SHIFT = 8;

    /**
     * Position of others permissions bits
     */
    public static final int PERMISSIONS_OTHERS_SHIFT = 0;

    /**
     * Which permissions don't apply to categrories
     */
    public static final int PERMISSIONS_CATEGORY_MASK = (PERMISSION_MODIFY << PERMISSIONS_OTHERS_SHIFT);

    /**
     * The value to use if the user is a root.
     */
    public static final Permissions PERMISSIONS_ROOT = new Permissions(0xff);

    public static final Permissions NO_PERMISSIONS = new Permissions(0x00);

    private final int permissions;

    /**
     * Constructs permissions according to given value This is permission object
     * for one distinct category {owner, group, others}
     *
     * @param perms Integer value to set permissions
     */
    public Permissions(int perms) {
        this.permissions = perms;
    }

    /**
     * Applies mask at permission object
     *
     * @param mask Mask to be applied
     * @return Permissions with applied mask
     */
    public Permissions applyMask(int mask) {
        return new Permissions(permissions & ~mask);
    }

    /**
     * Tests whether there is modification right
     *
     * @return {@code true} if so, {@code false} otherwise
     */
    public boolean canModify() {
        return (permissions & PERMISSION_MODIFY) != 0;
    }

    /**
     * Tests whether there is creation right
     *
     * @return {@code true} if so, {@code false} otherwise
     */
    public boolean canCreate() {
        return (permissions & PERMISSION_CREATE) != 0;
    }

    /**
     * Tests whether there is deletion right
     *
     * @return {@code true} if so, {@code false} otherwise
     */
    public boolean canDelete() {
        return (permissions & PERMISSION_DELETE) != 0;
    }

    /**
     * Combines three permission object into one integer value which can be stored in
     * persistence storage
     *
     * @param owner  Owner permissions
     * @param group  Group permissions
     * @param others Others permissions
     * @return Integer value representing all three permissions
     */
    public static int computePermissions(Permissions owner, Permissions group, Permissions others) {
        return (owner.permissions << PERMISSIONS_OWNER_SHIFT) + (group.permissions << PERMISSIONS_GROUP_SHIFT) + others.permissions;
    }

    /**
     * Returns owner permissions from total integral value
     *
     * @param perms Total integral value
     * @return Permissions for owner
     * @see computePermissions
     */
    public static Permissions extractOwner(int perms) {
        return new Permissions((perms >> PERMISSIONS_OWNER_SHIFT) & 0xff);
    }

    /**
     * Returns group permissions from total integral value
     *
     * @param perms Total integral value
     * @return Permissions for group
     * @see computePermissions
     */
    public static Permissions extractGroup(int perms) {
        return new Permissions((perms >> PERMISSIONS_OWNER_SHIFT) & 0xff);
    }

    /**
     * Returns others permissions from total integral value
     *
     * @param perms Total integral value
     * @return Permissions for others
     * @see computePermissions
     */
    public static Permissions extractOthers(int perms) {
        return new Permissions((perms >> PERMISSIONS_OWNER_SHIFT) & 0xff);
    }

    /**
     * Constructs one permission object combining multiple of them.
     * If there is right to do something in one of input permissions, there is the same right in
     * combined permission
     *
     * @param perms Multiple permissions objects
     * @return Combined permission
     */
    public static Permissions combine(Permissions... perms) {
        return combine(Arrays.asList(perms));
    }

    /**
     * Constructs one permission object combining multiple of them.
     * If there is right to do something in one of input permissions, there is the same right in
     * combined permission
     *
     * @param perms List of permission objects
     * @return Combined permission
     */
    public static Permissions combine(List<Permissions> perms) {
        int permissions = 0x00;
        for (Permissions p : perms)
            permissions |= p.permissions;

        return new Permissions(permissions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(canCreate() ? 'c' : '-');
        sb.append(canDelete() ? 'd' : '-');
        sb.append(canModify() ? 'm' : '-');
        return sb.toString();
    }
}
