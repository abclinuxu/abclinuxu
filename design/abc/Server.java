/*
 * Copyright Leos Literak 2001
 */
package abc;

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
    private Link lnkLink;
}