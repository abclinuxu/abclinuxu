/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

/**
 * This class contains link to external URL
 */
public class Link extends GenericObject {
    /** description of link */
    protected String text;
    /** URL */
    protected String url;
    /** server, where this link belongs to */
    protected Server server;


    public Link(int id) {
        super(id);
    }

    /**
     * @return description of link
     */
    public String getText() {
        return text;
    }

    /**
     * sets description of link
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * sets URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return server, where this link belongs to
     */
    public Server getServer() {
        return server;
    }

    /**
     * sets server, where this link belongs to
     */
    public void setServer(Server server) {
        this.server = server;
    }
}
