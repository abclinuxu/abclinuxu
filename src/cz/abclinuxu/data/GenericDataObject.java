/*
 * User: literakl
 * Date: Dec 11, 2001
 * Time: 7:53:13 AM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.data;

import java.util.Date;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.dom4j.*;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;
import cz.abclinuxu.AbcException;
import cz.abclinuxu.persistance.MySqlPersistance;

/**
 * This class serves as base class for Item, Category and Record,
 * which have very similar functionality and usage.
 */
public abstract class GenericDataObject extends GenericObject {

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(GenericDataObject.class);

    /** identifier of owner of this object */
    protected int owner;
    /** creation date or last update of this object */
    protected Date updated;
    /** XML with data or this object */
    protected Document data;
    /**
     * Helper (non-persistant) String for findByExample(),
     * which works as argument to search in <code>data</code>.
     **/
    protected String searchString;


    public GenericDataObject() {
    }

    public GenericDataObject(int id) {
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
     * @return XML data of this object
     */
    public Document getData() {
        return data;
    }

    /**
     * @return XML data in String format
     */
    public String getDataAsString() {
        try {
            if ( data==null ) return "";
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            OutputFormat format = new OutputFormat(null,false,"ISO-8859-2");
            format.setSuppressDeclaration(true);
            XMLWriter writer = new XMLWriter(os,format);
            writer.write(data);
            return os.toString();
        } catch (Exception e) {
            log.error("Nemohu prevest XML data na string! "+data.toString(),e);
            return "";
        }
    }

    /**
     * sets XML data of this object
     */
    public void setData(Document data) {
        this.data = data;
    }

    /**
     * sets XML data of this object in String format
     */
    public void setData(String data) throws AbcException {
        try {
            this.data = DocumentHelper.parseText(data);
        } catch (DocumentException e) {
            log.warn("Nemuzu konvertovat data do XML! Exception: "+e.getMessage()+" ("+data+")");
            throw new AbcException("Nemuzu konvertovat data do XML!",AbcException.WRONG_DATA,data,e);
        }
    }

    /**
     * Initialize this object with values from <code>obj</code>, if
     * this.getClass.equals(obj.getClass()).
     */
    public void synchronizeWith(GenericObject obj) {
        if ( ! (obj instanceof GenericDataObject) ) return;
        GenericDataObject b = (GenericDataObject) obj;
        content = b.getContent();
        data = b.getData();
        owner = b.getOwner();
        updated = b.getUpdated();
    }

    /**
     * @return Helper (non-persistant) String for findByExample(), which
     * works as argument to search in <code>data</code>.
     **/
    public String getSearchString() {
        return searchString;
    }

    /**
     * Sets elper (non-persistant) String for findByExample(), which
     * works as argument to search in <code>data</code>.
     **/
    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public boolean equals(Object o) {
        if ( !( o instanceof GenericDataObject) ) return false;
        GenericDataObject p = (GenericDataObject) o;
        if ( id==p.id && owner==p.owner && getDataAsString().equals(p.getDataAsString()) ) return true;
        return false;
    }
}
