/*
 * Copyright Leos Literak 2001
 */
package abc;


/**
 * Class containing basic user data
 */
public class User extends GenericObject {

    /** login name of the user */
    protected String login;
    /** real name of the user */
    protected String name;
    /** email of the user */
    protected String email;
    /** (noncrypted) password */
    protected String password;
    /** XML with data or this object */
    protected Document data;

    /**
     * verifies supplied password
     * @return true, if supplied password is valid
     */
    public boolean validatePassword(String pass) {
        return password.equals(pass);
    }

    /**
     * @return XML data in String format
     */
    public String getDataAsString() {
            return "";
    }

}