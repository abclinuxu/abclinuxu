/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

/**
 * Class containing basic user data
 */
public class User extends GenericObject {

    /** login name of the user */
    private String login;
    /** real name of the user */
    private String name;
    /** email of the user */
    private String email;
    /** (noncrypted) password */
    private String password;


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

    public String toString() {
        StringBuffer sb = new StringBuffer("User: id=");
        sb.append(id);
        if ( login!=null ) sb.append(",login="+login);
        if ( name!=null ) sb.append(",name="+name);
        if ( email!=null ) sb.append(",email="+email);
        if ( password!=null ) sb.append(",password="+password);
        return sb.toString();
    }

    public boolean equals(Object o) {
        if ( !( o instanceof User) ) return false;
        User p = (User) o;
        if ( id==p.id && login.equals(p.login) && name.equals(p.name) &&
             email.equals(p.email) && password.equals(p.password) ) return true;
        return false;
    }
}
