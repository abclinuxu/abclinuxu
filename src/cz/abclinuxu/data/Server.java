/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

/**
 * Contains information about server, from which we download
 * links to their resources
 */
public class Server extends GenericObject {
    /** display name of the server */
    protected String name;
    /** URL of the start page of the server */
    protected String url;
    /** email of our contact to this server */
    protected String contact;


    public Server() {
        super();
    }

    public Server(int id) {
        super(id);
    }

    /**
     * @return display name of the server
     */
    public String getName() {
        return name;
    }

    /**
     * sets display name of the server
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return URL of the start page of the server
     */
    public String getUrl() {
        return url;
    }

    /**
     * sets URL of the start page of the server
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return email of our contact to this server
     */
    public String getContact() {
        return contact;
    }

    /**
     * sets email of our contact to this server
     */
    public void setContact(String contact) {
        this.contact = contact;
    }
}
