/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

import java.io.ByteArrayOutputStream;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import cz.abclinuxu.AbcException;

/**
 * Class containing basic user data
 */
public class User extends GenericObject {

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(User.class);

    /** login name of the user */
    private String login;
    /** real name of the user */
    private String name;
    /** email of the user */
    private String email;
    /** (noncrypted) password */
    private String password;
    /** XML with data or this object */
    protected Document data;


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
        return password.equals(pass);
    }

    /**
     * @return XML data of this object
     */
    public Document getData() {
        return data;
    }

    /**
     * @return XML data in String format
     */
    public String getDataAsString() {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            OutputFormat format = new OutputFormat(null,false,"ISO-8859-2");
            format.setSuppressDeclaration(true);
            XMLWriter writer = new XMLWriter(os,format);
            writer.write(data);
            return os.toString();
        } catch (Exception e) {
            log.error("Nemohu prevest XML data na string! "+data.toString(),e);
            return "";
        }
    }

    /**
     * sets XML data of this object
     */
    public void setData(Document data) {
        this.data = data;
    }

    /**
     * sets XML data of this object in String format
     */
    public void setData(String data) throws AbcException {
        try {
            this.data = DocumentHelper.parseText(data);
        } catch (DocumentException e) {
            log.warn("Nemuzu konvertovat data do XML! Exception: "+e.getMessage()+" ("+data+")");
            throw new AbcException("Nemuzu konvertovat data do XML!",AbcException.WRONG_DATA,data,e);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("User: id=");
        sb.append(id);
        if ( login!=null ) sb.append(",login="+login);
        if ( name!=null ) sb.append(",name="+name);
        if ( email!=null ) sb.append(",email="+email);
        if ( password!=null ) sb.append(",password="+password);
        if ( data!=null ) sb.append(",data="+getDataAsString());
        return sb.toString();
    }

    public boolean equals(Object o) {
        if ( !( o instanceof User) ) return false;
        User p = (User) o;
        if ( id==p.id && login.equals(p.login) && name.equals(p.name) &&
             email.equals(p.email) && password.equals(p.password) &&
             getDataAsString().equals(p.getDataAsString()) ) return true;
        return false;
    }
}
