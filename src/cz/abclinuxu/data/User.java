/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

import org.dom4j.Document;
import org.dom4j.Node;

import java.util.*;

import cz.abclinuxu.security.Roles;

/**
 * Class containing basic user data
 */
public class User extends GenericObject implements XMLContainer {
    /** login name of the user */
    private String login;
    /** real name of the user */
    private String name;
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
        login = b.getLogin();
        email = b.getEmail();
        password = b.getPassword();
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
        if ( id==p.id && login.equals(p.login) && name.equals(p.name) &&
             email.equals(p.email) && password.equals(p.password) &&
             getDataAsString().equals(p.getDataAsString()) ) return true;
        return false;
    }

    public int hashCode() {
        String tmp = "User"+id;
        return tmp.hashCode();
    }
}
