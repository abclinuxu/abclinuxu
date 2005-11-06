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


    public Link() {
        super();
    }

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

    /**
     * @return owner's id
     */
    public int getOwner() {
        return owner;
    }

    /**
     * sets owner's id
     */
    public void setOwner(int owner) {
        this.owner = owner;
    }

    /**
     * @return last updated (or creation) date
     */
    public Date getUpdated() {
        return updated;
    }

    /**
     * sets last updated (or creation) date
     */
    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    /**
     * Initialize this object with values from <code>obj</code>, if
     * this.getClass.equals(obj.getClass()).
     */
    public void synchronizeWith(GenericObject obj) {
        if ( ! (obj instanceof Link) ) return;
        if ( obj==this ) return;
        super.synchronizeWith(obj);
        Link b = (Link) obj;
        text = b.getText();
        url = b.getUrl();
        owner = b.getOwner();
        updated = b.getUpdated();
        server = b.getServer();
        fixed = b.isFixed();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Link: id=");
        sb.append(id);
        if ( text!=null ) sb.append(",text="+text);
        if ( url!=null ) sb.append(",url="+url);
        if ( server!=0 ) sb.append(",server="+server);
        return sb.toString();
    }

    public boolean preciseEquals(Object o) {
        if ( !( o instanceof Link) ) return false;
        Link p = (Link)o;
        if ( id==p.id && text.equals(p.text) && url.equals(p.url) &&
             server==p.server && fixed==p.fixed && owner==p.owner ) return true;
        return false;
    }

    public int hashCode() {
        String tmp = "Link"+id;
        return tmp.hashCode();
    }
}
