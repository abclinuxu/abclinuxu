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
    protected int server;
    /**
     * if false, then this link is periodically checked and may be replaced
     * with newer link (e.g. links to articles at news servers.
     */
    protected boolean fixed;


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
    public int getServer() {
        return server;
    }

    /**
     * sets server, where this link belongs to
     */
    public void setServer(int server) {
        this.server = server;
    }

    /**
     * if false, then this link is periodically checked and may be replaced
     * with newer link (e.g. links to articles at news servers.
     */
    public boolean isFixed() {
        return fixed;
    }

    /**
     * if false, then this link is periodically checked and may be replaced
     * with newer link (e.g. links to articles at news servers.
     */
    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Link: id=");
        sb.append(id);
        if ( text!=null ) sb.append(",text="+text);
        if ( url!=null ) sb.append(",url="+url);
        if ( server!=0 ) sb.append(",server="+server);
        return sb.toString();
    }

    public boolean equals(Object o) {
        if ( !( o instanceof Link) ) return false;
        Link p = (Link)o;
        if ( id==p.id && text.equals(p.text) && url.equals(p.url) &&
             server==p.server && fixed==p.fixed ) return true;
        return false;
    }
}
