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

/**
 * Class for storage various binary objects like images,
 * sounds or video
 */
public class Data extends GenericObject {
    /** owner of this object */
    protected int owner;
    /** data of this object */
    protected byte[] data;
    /** MIME type or "URL", if <code>data</code> contains URL */
    protected String format;


    public Data() {
        super();
    }

    public Data(int id) {
        super(id);
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
     * @return binary data or URL
     */
    public byte[] getData() {
        return data;
    }

    /**
     * sets binary data or URL
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * @return MIME type or "URL", if <code>data</code> contains URL
     */
    public String getFormat() {
        return format;
    }

    /**
     * sets MIME type. Use "URL", if <code>data</code> contains URL
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Initialize this object with values from <code>obj</code>, if
     * this.getClass.equals(obj.getClass()).
     */
    public void synchronizeWith(GenericObject obj) {
        if ( ! (obj instanceof Data) ) return;
        if ( obj==this ) return;
        super.synchronizeWith(obj);
        Data b = (Data) obj;
        data = b.getData();
        owner = b.getOwner();
        format = b.getFormat();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Data: id=");
        sb.append(id);
        if ( owner!=0 ) sb.append(",owner="+owner);
        if ( data!=null ) sb.append(",data="+data);
        if ( format!=null ) sb.append(",format="+format);
        return sb.toString();
    }

    public boolean preciseEquals(Object o) {
        if ( !( o instanceof Data) ) return false;
        Data p = (Data)o;
        if ( id==p.id && owner==p.owner && data.equals(p.data) && format.equals(p.format) ) return true;
        return false;
    }

    public int hashCode() {
        String tmp = "Data"+id;
        return tmp.hashCode();
    }
}
