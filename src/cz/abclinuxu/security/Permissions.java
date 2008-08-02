/*
 *  Copyright (C) 2005 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */

package cz.abclinuxu.security;

/**
 *
 * @author lubos
 */
public class Permissions {
	/**
	 * GenericDataObject: the right to modify its contents
	 * Category: the right to create/delete subcategories and modify the category in question
	 */
	public static final int PERMISSION_MODIFY = 1;
	/**
	 * GenericDataObject: the right to delete the object
	 * Category: no special right, but it's propagated to child items
	 */
	public static final int PERMISSION_DELETE = 2;
	/**
	 * GenericDataObject: the right to derive content
	 * Category: the right to create a child GenericDataObject
	 */
	public static final int PERMISSION_CREATE = 4;
	
	public static final int PERMISSIONS_GROUP_SHIFT = 8;
	public static final int PERMISSIONS_OTHERS_SHIFT = 0;
	
	/** Which permissions don't apply to categrories */
	public static final int PERMISSIONS_CATEGORY_MASK = (PERMISSION_MODIFY << PERMISSIONS_OTHERS_SHIFT);
	
	/** The value to use if the user is a root. */
	public static final Permissions PERMISSIONS_ROOT = new Permissions(0xff);
	
	private int permissions;
	
	public Permissions(int perms) {
		this.permissions = perms;
	}
	
	public int getPermissions() {
		return permissions;
	}
	
	public void setPermissions(int permissions) {
		this.permissions = permissions;
	}
	
	public boolean canModify() {
		return (permissions & PERMISSION_MODIFY) != 0;
	}
	
	public boolean canCreate() {
		return (permissions & PERMISSION_CREATE) != 0;
	}
	
	public boolean canDelete() {
		return (permissions & PERMISSION_DELETE) != 0;
	}
}
