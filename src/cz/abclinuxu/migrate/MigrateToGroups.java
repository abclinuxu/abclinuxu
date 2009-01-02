/*
 *  Copyright (C) 2008 Leos Literak
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

package cz.abclinuxu.migrate;

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.security.Permissions;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.freemarker.Tools;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 *
 * @author lubos
 */
public class MigrateToGroups {
	private Persistence persistence = PersistenceFactory.getPersistence();
    private SQLTool sqlTool = SQLTool.getInstance();
	private List<User> priviledged;
    
    public static final String[] OBSOLETE_ROLES = {
        "bazaar admin", "article admin", "content admin", "games admin",
        "requests admin", "attachment admin", "category admin", "dictionary admin",
        "software admin", "move relation", "derive content", "remove relation"
    };
	
	void run() throws Exception {
		int gid, perms;
		
		List<Integer> users =  sqlTool.findUsersWithRoles(null);
		priviledged = new ArrayList<User>(users.size());
		
		for (Integer uid : users) {
			User user = Tools.createUser(uid);
			priviledged.add(user);
		}
		
		gid = createGroup("article admins", "article admin");
        perms = (Permissions.PERMISSION_CREATE | Permissions.PERMISSION_DELETE | Permissions.PERMISSION_MODIFY) << Permissions.PERMISSIONS_GROUP_SHIFT;
        
		assignGIDToNotCachedCategories(Category.SECTION, gid, perms);
		assignGID(Constants.CAT_ARTICLES, gid, perms);
		assignGID(Constants.CAT_ARTICLES_POOL, gid, perms);
		assignGID(Constants.CAT_AUTHORS, gid, perms);
        assignGIDToItems(Item.AUTHOR, gid, perms);
		assignGIDToItems(Item.ARTICLE, gid, perms);
		assignGIDToItems(Item.SERIES, gid, perms);
		assignGIDToItems(Item.ROYALTIES, gid, perms);
		addUsers("article admin", gid);
        
		gid = createGroup("games admins", "games admin");
		assignGID(Constants.CAT_TRIVIA, gid, perms);
		assignGIDToItems(Item.TRIVIA, gid, perms);
		assignGIDToItems(Item.HANGMAN, gid, perms);
		addUsers("games admin", gid);
		
        perms |= Permissions.PERMISSION_CREATE;
        
        gid = createGroup("requests admins", "requests admin");
		assignGID(Constants.CAT_REQUESTS, gid, perms);
		assignGIDToItems(Item.REQUEST, gid, perms);
		addUsers("requests admin", gid);
        
        gid = createGroup("bazaar admins", "bazaar admin");
		assignGID(Constants.CAT_BAZAAR, gid, perms);
		assignGIDToItems(Item.BAZAAR, gid, perms);
		addUsers("bazaar admin", gid);
        
        gid = createGroup("desktop admins", null);
		assignGID(Constants.CAT_DESKTOPS, gid, perms);
		assignGIDToItems(Item.DESKTOP, gid, perms);
        
        perms |= Permissions.PERMISSION_MODIFY;
        
		gid = createGroup("dictionary admins", "dictionary admin");
		assignGID(Constants.CAT_DICTIONARY, gid, perms);
		assignGIDToItems(Item.DICTIONARY, gid, perms);
		addUsers("dictionary admin", gid);
        
        // previously non-existent admin rights
		gid = createGroup("FAQ admins", null);
		assignGID(Constants.CAT_FAQ, gid, perms);
		assignGIDToNotCachedCategories(Category.FAQ, gid, perms);
		assignGIDToItems(Item.FAQ, gid, perms);
		
		gid = createGroup("hardware admins", null);
		assignGIDToNotCachedCategories(Category.HARDWARE_SECTION, gid,
                (Permissions.PERMISSION_CREATE | Permissions.PERMISSION_DELETE | Permissions.PERMISSION_MODIFY)
                    << Permissions.PERMISSIONS_GROUP_SHIFT);
		assignGIDToItems(Item.HARDWARE, gid, perms);
		
		gid = createGroup("personality admins", null);
		assignGID(Constants.CAT_PERSONALITIES, gid, perms);
		assignGIDToItems(Item.PERSONALITY, gid, perms);
		
		gid = createGroup("driver admins", null);
		assignGID(Constants.CAT_DRIVERS, gid, perms);
		assignGIDToItems(Item.DRIVER, gid, perms);
		
        // reset "others" rights
        perms = (Permissions.PERMISSION_CREATE | Permissions.PERMISSION_DELETE | Permissions.PERMISSION_MODIFY) << Permissions.PERMISSIONS_GROUP_SHIFT;
		gid = createGroup("content admins", "content admin");
		assignGID(Constants.CAT_DOCUMENTS, gid, perms);
		assignGIDToItems(Item.CONTENT, gid, perms);
		addUsers("content admin", gid);
        
        gid = createGroup("event admin", null);
        assignGID(Constants.CAT_EVENTS, gid, perms | (Permissions.PERMISSION_CREATE << Permissions.PERMISSIONS_OTHERS_SHIFT));
		
		gid = createGroup("software admins", "software admin");
        assignGIDToNotCachedCategories(Category.SOFTWARE_SECTION, gid, perms);
        
        perms |= (Permissions.PERMISSION_MODIFY | Permissions.PERMISSION_CREATE) << Permissions.PERMISSIONS_OTHERS_SHIFT;
		assignGIDToItems(Item.SOFTWARE, gid, perms);
		addUsers("software admin", gid);
        
        // remove particular roles
        for (User user : priviledged) {
            removeRoles(user);
            persistence.update(user);
        }
	}
	
	void assignGID(int category, int gid, int perms) {
		Category cat = new Category(category);
		Tools.sync(cat);
		
		assignGID(cat, gid, perms);
	}
	
	void assignGID(Category cat, int gid, int perms) {
		List<Relation> children = Tools.syncList(cat.getChildren());
		
		cat.setGroup(gid);
        
        if (!isCategoryOpen(cat))
            cat.setPermissions(perms);
        else
            cat.setPermissions(perms |
                    (Permissions.PERMISSION_CREATE | Permissions.PERMISSION_MODIFY) << Permissions.PERMISSIONS_OTHERS_SHIFT);
		
		//System.out.println(">>> Walking through "+cat.getTitle());
		
		for (Relation r : children) {
			GenericObject child = r.getChild();
			if (child instanceof Category)
				assignGID((Category) child, gid, perms);
		}
		
        Date originalUpdated = cat.getUpdated();
		persistence.update(cat);
        sqlTool.setUpdatedTimestamp(cat, originalUpdated);
	}
    
    public boolean isCategoryOpen(Category cat) {
        Document document = cat.getData();
        if (document == null)
            return false;
        Element element = (Element) document.selectSingleNode("/data/writeable");
        return element != null && Boolean.valueOf(element.getText()).booleanValue();
    }
	
	void assignGIDToNotCachedCategories(int categoryType, int gid, int perms) {
		int count = sqlTool.countCategoryRelationsWithType(categoryType);
		for (int i=0; i<count; i += 50) {
			List<Relation> cats = sqlTool.findCategoryRelationsWithType(categoryType, new Qualifier[]{new LimitQualifier(i, i+50)});
			Tools.syncList(cats);
			
			for (Relation r : cats) {
				Category c = (Category) r.getChild();
				Tools.sync(c);
				
				c.setGroup(gid);
                
                if (!isCategoryOpen(c))
                    c.setPermissions(perms);
                else
                    c.setPermissions(perms |
                            (Permissions.PERMISSION_CREATE | Permissions.PERMISSION_MODIFY) << Permissions.PERMISSIONS_OTHERS_SHIFT);
                
                Date originalUpdated = c.getUpdated();
				persistence.update(c);
                sqlTool.setUpdatedTimestamp(c, originalUpdated);
			}
		}
	}
	/*
	void assignGIDToPolls(int gid) {
		int count = sqlTool.countStandalonePollRelations();
		for (int i=0; i<count; i += 50) {
			List<Relation> polls = sqlTool.findStandalonePollRelations(new Qualifier[]{new LimitQualifier(i, i+50)});
			Tools.syncList(polls);
			
			for (Relation rel : polls) {
				Poll poll = (Poll) rel.getChild();
				poll.setGroup(gid);
				System.out.println("*** Would modify GDO "+poll.getId()+"; "+poll.getText());
				persistence.update(poll);
			}
		}
	}
	*/
	void assignGIDToItems(int itemType, int gid, int perms) {
		int count = sqlTool.countItemsWithType(itemType);
		
		for (int i=0; i<count; i += 50) {
			List<Item> faqs = sqlTool.findItemsWithType(itemType, i, 50);
			Tools.syncList(faqs);
			
			for (Item item : faqs) {
				item.setGroup(gid);
                
                if ("public".equals(item.getSubType()))
                    item.setPermissions(perms | (Permissions.PERMISSION_MODIFY) << Permissions.PERMISSIONS_OTHERS_SHIFT);
                else
                    item.setPermissions(perms);
				//System.out.println("*** Would modify GDO "+item.getId()+"; "+item.getTitle());
                
                Date originalUpdated = item.getUpdated();
				persistence.update(item);
                sqlTool.setUpdatedTimestamp(item, originalUpdated);
			}
		}
		
		
	}
	
	void addUsers(String formerRole, int gid) {
		String group = Integer.toString(gid);
		int users = 0;
		
		for (User user : priviledged) {
			if(user.hasRole(formerRole) && (!user.hasRole(Roles.ROOT) || formerRole.equals(Roles.ROOT))) {
				Element system = (Element) user.getData().selectSingleNode("/data/system");
				System.out.println("Adding user "+user.getId()+" to GID "+gid);
				
				if (!user.isMemberOf(group))
					system.addElement("group").setText(group);
				
				users++;
			}
		}
		
		System.out.println("Migrated "+users+" that have the role "+formerRole);
	}
	
	int createGroup(String name, String role) {
		Item group = new Item(0, Item.GROUP);
        group.setData(DocumentHelper.createDocument());
		
		if (role != null)
			System.out.println("Creating a group \"" + name + "\" as a replacement for the role \""+role+"\"");
		else
			System.out.println("Creating a brand new group \"" + name + "\"");

		group.setTitle(name);
		Node node = DocumentHelper.makeElement(group.getData(), "/data/desc");
		
		if (role != null)
			node.setText("Converted from role "+role);
		else
			node.setText("A new group of "+name);
		
        group.setCreated(new Date());

        persistence.create(group);
		
		return group.getId();
	}
    
    void removeRoles(User user) {
        for (int i=0;i<OBSOLETE_ROLES.length;i++) {
            if (user.hasRole(OBSOLETE_ROLES[i])) {
                Node node = user.getData().selectSingleNode("//roles/role[text()='" + OBSOLETE_ROLES[i] + "']");
                
                if (node != null)
                    node.detach();
            }
        }
    }
	
	public static void main(String[] args) throws Exception {
        MigrateToGroups task = new MigrateToGroups();
        task.run();
    }
}
