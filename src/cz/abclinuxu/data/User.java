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

import org.dom4j.Document;
import org.dom4j.Node;

import java.util.*;

import cz.abclinuxu.security.Roles;
import cz.abclinuxu.utils.Misc;

/**
 * Class containing basic user data
 */
public class User extends GenericObject implements XMLContainer {
    /** login name of the user */
    private String login;
    /** real name of the user */
    private String name;
    /** nickname of the user */
    private String nick;
    /** email of the user */
    private String email;
    /** (noncrypted) password */
    private String password;
    /** XML with data or this object */
    private XMLHandler documentHandler;

    /** cache of granted user roles */
    private Map roles = null;


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
     * verifies supplied password
     * @return true, if supplied password is valid
     */
    public boolean validatePassword(String pass) {
        if ( pass==null ) return false;
        return password.equals(pass);
    }

    /**
     * @return XML data of this object
     */
    public Document getData() {
        return (documentHandler!=null)? documentHandler.getData():null;
    }

    /**
     * @return XML data in String format
     */
    public String getDataAsString() {
        return (documentHandler!=null)? documentHandler.getDataAsString():null;
    }

    /**
     * sets XML data of this object
     */
    public void setData(Document data) {
        roles = null;
        documentHandler = new XMLHandler(data);
    }

    /**
     * sets XML data of this object in String format
     */
    public void setData(String data) {
        roles = null;
        documentHandler = new XMLHandler(data);
    }

    /**
     * @return True, if user is an administrator
     */
    public boolean isAdmin() {
        throw new RuntimeException("Deprecated method isAdmin called!");
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
     * Initialize this object with values from <code>obj</code>, if
     * this.getClass.equals(obj.getClass()).
     */
    public void synchronizeWith(GenericObject obj) {
        if ( ! (obj instanceof User) ) return;
        if ( obj==this ) return;
        super.synchronizeWith(obj);
        User b = (User) obj;
        documentHandler = b.documentHandler;
        name = b.getName();
        nick = b.getNick();
        login = b.getLogin();
        email = b.getEmail();
        password = b.getPassword();
    }

    public Object clone() {
        User clone = (User) super.clone();
        if (documentHandler != null)
            clone.documentHandler = (XMLHandler) documentHandler.clone();
        return clone;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("User: id=");
        sb.append(id);
        if ( name!=null ) sb.append(",name="+name);
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
        if (!Misc.same(getDataAsString(),p.getDataAsString()))
            return false;
        return true;
    }

    public int hashCode() {
        String tmp = "User"+id;
        return tmp.hashCode();
    }
}
