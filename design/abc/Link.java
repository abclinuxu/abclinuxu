/*
 * Copyright Leos Literak 2001
 */
package abc;

import java.util.Date;

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
    /** identifier of owner of this object */
    protected int owner;
    /** creation date or last update of this object */
    protected Date updated;

}
