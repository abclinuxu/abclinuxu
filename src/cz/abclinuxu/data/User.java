/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

import org.dom4j.Document;
import cz.abclinuxu.AbcException;

import java.util.Iterator;

/**
 * Class containing basic user data
 */
public class User extends GenericObject implements XMLContainer {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(User.class);

    /** login name of the user */
    private String login;
    /** real name of the user */
    private String name;
    /** email of the user */
    private String email;
    /** (noncrypted) password */
    private String password;
    /** XML with data or this object */
    protected XMLHandler documentHandler;


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
        documentHandler = new XMLHandler(data);
    }

    /**
     * sets XML data of this object in String format
     */
    public void setData(String data) {
        documentHandler = new XMLHandler();
        documentHandler.setData(data);
    }

    /**
     * @return True, if user is an administrator
     */
    public boolean isAdmin() {
        for (Iterator iter = getContent().iterator(); iter.hasNext();) {
            GenericObject obj = (GenericObject) ((Relation) iter.next()).getChild();
            if ( obj instanceof AccessRights ) {
                return ((AccessRights)obj).isAdmin();
            }
        }
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
        if ( login!=null ) sb.append(",login="+login);
        if ( name!=null ) sb.append(",name="+name);
        if ( email!=null ) sb.append(",email="+email);
        if ( password!=null ) sb.append(",password="+password);
        if ( documentHandler!=null ) sb.append(",data="+getDataAsString());
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
