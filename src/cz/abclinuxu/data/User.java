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
package cz.abclinuxu.data;

import cz.abclinuxu.persistence.SQLTool;
import org.dom4j.Document;
import org.dom4j.Node;

import java.util.*;

import cz.abclinuxu.security.Roles;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.edit.EditGroup;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.LRUMap;
import cz.abclinuxu.utils.config.impl.AbcConfig;

/**
 * Class containing basic user data
 */
public class User extends CommonObject {
    /** login name of the user */
    private String login;
    /** openid login */
    private String openId;
    /** real name of the user */
    private String name;
    /** nickname of the user */
    private String nick;
    /** email of the user */
    private String email;
    /** (noncrypted) password, not persisted */
    private String password;
    /** time of last synchronization with LDAP (optional) */
    private Date lastSynced;
    private boolean virtual; // brainstorming

    /** cache of granted user roles */
    private Map roles;
    /** map where key is discussion id and value is id of last seen comment */
    private Map<Integer, Integer> lastSeenDiscussions;


    public User() {
        super();
    }

    public User(int id) {
        super(id);
    }

    /**
     * @return login name of the user
     */
    public String getLogin() {
        return login;
    }

    /**
     * sets login name of the user
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * @return real name of the user
     */
    public String getName() {
        return name;
    }

    /**
     * @return openid
     */
    public String getOpenId() {
        return openId;
    }

    /**
     * Sets openid
     * @param openId valid openid url
     */
    public void setOpenId(String openId) {
        this.openId = openId;
    }

    /**
     * sets real name of the user
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *  @return nickname of the user
     */
    public String getNick() {
        return nick;
    }

    /**
     *  Set nickname of the user
     */
    public void setNick(String nick) {
        this.nick = nick;
    }

    /**
     * @return email of the user
     */
    public String getEmail() {
        return email;
    }

    /**
     * sets email of the user
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * User password. It is not persisted in database since migration to LDAP.
     * @return (noncrypted) password
     */
    public String getPassword() {
        return password;
    }

    /**
     * (noncrypted) password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get time of last synchronization with LDAP
     * @return last synchronization time or null
     */
    public Date getLastSynced() {
        return lastSynced;
    }

    /**
     * Sets time of last synchronization with LDAP
     * @param lastSynced time
     */
    public void setLastSynced(Date lastSynced) {
        this.lastSynced = lastSynced;
    }

    /**
     * Finds out, whether user is in given role.
     * @param role name of role.
     * @return true, if user is in give role.
     * @see cz.abclinuxu.security.Roles
     */
    public boolean hasRole(String role) {
        if ( id==1 ) return true;
        if ( role==null || role.length()==0 ) return false;

        if (roles==null) {
            Document data = getData();
            if ( data==null ) {
                roles = Collections.EMPTY_MAP;
                return false;
            }

            List nodes = data.selectNodes("/data/roles/role");
            if ( nodes==null || nodes.size()==0 ) {
                roles = Collections.EMPTY_MAP;
                return false;
            }

            roles = new HashMap(nodes.size(),1.0f);
            for ( Iterator iter = nodes.iterator(); iter.hasNext(); ) {
                String value = ((Node) iter.next()).getText();
                roles.put(value,value);
            }
        }

        if ( roles.containsKey(Roles.ROOT) )
            return true;
        if ( roles.containsKey(role) )
            return true;
        return false;
    }

    /**
     * @return true, if user is member of specified group.
     */
    public boolean isMemberOf(int group) {
		return getData().selectSingleNode("/data/system/group[text()='"+group+"']")!=null;
    }

	/**
     * @return true, if user is member of specified group.
     */
	public boolean isMemberOf(String groupName) {
		List<Item> items = SQLTool.getInstance().findItemsWithType(Item.GROUP, 0, EditGroup.DEFAULT_MAX_NUMBER_OF_GROUPS);

		for (Item item : items) {
			if (item.getTitle().equals(groupName))
				return true;
		}

		return false;
	}

	public boolean isRoot() {
		return isMemberOf(Constants.GROUP_ADMINI);
	}

    /**
     * Fills last seen comments for this user. This is cache to be filled from persistent storage,
     * changes are not propagated to persistence.
     * @param map map to be copied, key is discussion id (integer), value is last seen comment id (integer).
     */
    public void fillLastSeenComments(Map<Integer, Integer> map) {
        createLastSeenCommentsMap();
        lastSeenDiscussions.putAll(map);
    }

    /**
     * Creates map where cache of last seen comments will be stored.
     */
    private void createLastSeenCommentsMap() {
        lastSeenDiscussions = new LRUMap(AbcConfig.getMaxWatchedDiscussionLimit());
    }

    /**
     * Stores id of last seen comment for given discussion (not persistent).
     * @param discussion
     * @param lastComment
     */
    public void storeLastSeenComment(int discussion, int lastComment) {
        if (lastSeenDiscussions == null)
            createLastSeenCommentsMap();
        lastSeenDiscussions.put(discussion, lastComment);
    }

    /**
     * Finds id of comment that this user has seen as last.
     * @param discussion id of discussion
     * @return id of last seen comment or null, if he has not opened that dicussion yet
     */
    public Integer getLastSeenComment(int discussion) {
        if (lastSeenDiscussions == null)
            return null;
        return (Integer) lastSeenDiscussions.get(discussion);
    }

    /**
     * sets XML data of this object
     */
    public void setData(Document data) {
        super.setData(data);
        roles = null;
    }

    /**
     * sets XML data of this object in String format
     */
    public void setData(String data) {
        super.setData(data);
        roles = null;
    }

    /**
     * Initialize this object with values from <code>obj</code>, if
     * this.getClass.equals(obj.getClass()).
     */
    public void synchronizeWith(GenericObject obj) {
        if ( ! (obj instanceof User) ) return;
        if ( obj==this ) return;
        super.synchronizeWith(obj);

        User b = (User) obj;
        name = b.getName();
        nick = b.getNick();
        login = b.getLogin();
        email = b.getEmail();
        password = b.getPassword();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("User: id=");
        sb.append(id);
        if ( name!=null ) sb.append(",name=").append(name);
        return sb.toString();
    }

    public boolean preciseEquals(Object o) {
        if ( !( o instanceof User) ) return false;
        User p = (User) o;
        if ( id!=p.id )
            return false;
        if (!Misc.same(login,p.login))
            return false;
        if (!Misc.same(name,p.name))
            return false;
        if (!Misc.same(nick,p.nick))
            return false;
        if (!Misc.same(email,p.email))
            return false;
        if (!Misc.same(password,p.password))
            return false;
        if (!Misc.sameXml(getDataAsString(),p.getDataAsString()))
            return false;
        return true;
    }

    public int hashCode() {
        String tmp = "User"+id;
        return tmp.hashCode();
    }

    /**
     * Compares content fields of this and that GenericObject. The argument
     * must be instance of same class and have same content properties.
     * @param obj compared class
     * @return true if both instances have same content
     */
    public boolean contentEquals(GenericObject obj) {
        if (obj == this)
            return true;
        if (! super.contentEquals(obj))
            return false;
        User p = (User) obj;
        if (! Misc.same(login, p.login))
            return false;
        if (! Misc.same(name, p.name))
            return false;
        if (! Misc.same(nick, p.nick))
            return false;
        return Misc.same(email, p.email);
    }
}
