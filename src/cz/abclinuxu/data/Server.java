/*
 *  Copyright (C) 2005 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
package cz.abclinuxu.data;

import cz.abclinuxu.utils.Misc;

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

    /**
     * Initialize this object with values from <code>obj</code>, if
     * this.getClass.equals(obj.getClass()).
     */
    public void synchronizeWith(GenericObject obj) {
        if ( ! (obj instanceof Server) ) return;
        if ( obj==this ) return;
        super.synchronizeWith(obj);
        Server b = (Server) obj;
        name = b.getName();
        url = b.getUrl();
        contact = b.getContact();
    }

    /**
     * Compares content fields of this and that GenericObject. The argument
     * must be instance of same class and have same content properties.
     * @param obj compared class
     * @return true if both instances have same content
     */
    public boolean contentEquals(GenericObject obj) {
        if (obj == this)
            return true;
        if (! super.contentEquals(obj))
            return false;
        Server p = (Server) obj;
        if (! Misc.same(name, p.name))
            return false;
        return Misc.same(url, p.url);
    }

    public int hashCode() {
        String tmp = "Server"+id;
        return tmp.hashCode();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Server: id=");
        sb.append(id);
        if ( name!=null ) sb.append(",name="+name);
        if ( url!=null ) sb.append(",title="+url);
        if ( contact!=null ) sb.append(",contact="+contact);
        return sb.toString();
    }
}
