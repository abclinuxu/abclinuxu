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

package cz.abclinuxu.utils.forms;

import cz.abclinuxu.security.Permissions;
import cz.abclinuxu.utils.freemarker.Tools;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author lubos
 */
public class PermissionsSet extends ListMultipleChoice<PermissionsSet.Permission> {
	private List<String> allValues;
	
	public PermissionsSet(Object param) {
		allValues = new ArrayList<String>();
		
		allValues.add("modify");
		allValues.add("delete");
		allValues.add("create");
		
		List<String> checked = (List<String>) Tools.asList(param);

        for (String perm : allValues) {
            Permission js;
            
            if (checked.contains(perm)) {
                js = new Permission(perm, true);
                selected++;
            } else {
                js = new Permission(perm, false);
            }
            choices.add(js);
        }
	}
	
	public PermissionsSet(Permissions perms) {
		allValues = new ArrayList<String>();
		
		allValues.add("modify");
		allValues.add("delete");
		allValues.add("create");
		
		choices.add(new Permission("modify", perms.canModify()));
		choices.add(new Permission("delete", perms.canDelete()));
		choices.add(new Permission("create", perms.canCreate()));
	}
	
	public boolean isEverythingSelected() {
		return selected == allValues.size();
	}
	
	@Override
	public Collection<String> selectedSet() {
        
        List<String> list = new ArrayList<String>();
        for (Permission element : choices) {
            // check whether element is checked
            if (element != null && element.isSet())
                list.add(element.getPermission());
        }
        return list;
    }
	
	public int getPermissions() {
		Collection<String> set = selectedSet();
		int perms = 0;
		
		if (set.contains("modify"))
			perms |= Permissions.PERMISSION_MODIFY;
		if (set.contains("delete"))
			perms |= Permissions.PERMISSION_DELETE;
		if (set.contains("create"))
			perms |= Permissions.PERMISSION_CREATE;
		
		return perms;
	}
	
	public static class Permission implements Selectable {
        
        private boolean set;
        private String permission;

        public Permission(String permission, boolean set) {
            this.set = set;
            this.permission = permission;
        }

        public boolean isSet() {
            return set;
        }

        public void setSet(boolean set) {
            this.set = set;
        }

        public String getPermission() {
            return permission;
        }
        
    }
}

