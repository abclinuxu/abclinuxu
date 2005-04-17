/*
 * User: literakl
 * Date: Dec 26, 2001
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.data;

import org.apache.log4j.Logger;
import org.dom4j.Document;

/**
 * This class contains one relation between two GenericObjects.
 */
public final class Relation extends GenericObject implements XMLContainer {
    static Logger log = Logger.getLogger(Relation.class);

    /** Upper relation. Similar to .. in filesystem. */
    int upper = 0;
    GenericObject parent;
    GenericObject child;
    /** URL of this relation. It shall not end with slash. */
    String url;
    /** XML with data of this object. There may be tags name and icon, which override default settings */
    protected XMLHandler documentHandler;


    public Relation() {
        super();
    }

    public Relation(int id) {
        super(id);
    }

    /**
     * Copy constructor.
     * @param copy object to be cloned.
     */
    public Relation(Relation copy) {
        id = copy.id;
        initialized = copy.initialized;
        upper = copy.upper;
        parent = copy.parent;
        child = copy.child;
        url = copy.url;
        documentHandler = copy.documentHandler;
    }

    /**
     * Constructs new Relation between <code>parent</code> and <code>child</code> (in this order).
     * If this relation is not top-level (e.g. it has its parent), you shall set <code>upper</code>
     * to <code>id</code> of this upper-level relation, otherwise set it to 0.
     */
    public Relation(GenericObject parent, GenericObject child, int upper) {
        this.parent = parent;
        this.child = child;
        this.upper = upper;
    }

    public GenericObject getChild() {
        return child;
    }

    public void setChild(GenericObject child) {
        this.child = child;
    }

    public GenericObject getParent() {
        return parent;
    }

    public void setParent(GenericObject parent) {
        this.parent = parent;
    }

    /**
     * @return Upper relation. Similar to .. in filesystem.
     */
    public int getUpper() {
        return upper;
    }

    /**
     * Sets upper relation. Similar to .. in filesystem.
     */
    public void setUpper(int upper) {
        this.upper = upper;
    }

    /**
     * @return URL for this relation.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets url for this relation. It must start with slash.
     * It must contain only characters [a-zA-Z0-9_/-] Last character
     * cannot be slash (it is removed).
     * todo once URLManager is commonly used, remove the check
     */
    public void setUrl(String url) {
        if (url!=null && url.endsWith("/"))
            url = url.substring(0, url.length()-1);
        this.url = url;
    }

    /**
     * @return XML data of this object
     */
    public Document getData() {
        return (documentHandler!=null)? documentHandler.getData():null;
    }

    /**
     * @return XML data in String format
     */
    public String getDataAsString() {
        return (documentHandler!=null)? documentHandler.getDataAsString():null;
    }

    /**
     * sets XML data of this object
     */
    public void setData(Document data) {
        documentHandler = new XMLHandler(data);
    }

    /**
     * sets XML data of this object in String format
     */
    public void setData(String data) {
        documentHandler = new XMLHandler();
        documentHandler.setData(data);
    }

    public void synchronizeWith(GenericObject obj) {
        if ( !(obj instanceof Relation) || obj==this ) return;

        Relation r = (Relation) obj;
        id = r.getId();
        initialized = r.isInitialized();
        upper = r.getUpper();
        documentHandler = r.documentHandler;
        parent = r.getParent();
        child = r.getChild();
        url = r.url;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Relation "+id+",upper="+upper);
        if ( parent!=null ) sb.append(",parent="+parent.getId());
        if ( child!=null ) sb.append(",child="+child.getId());
        return sb.toString();
    }

    public boolean preciseEquals(Object obj) {
        if ( ! (obj instanceof Relation ) ) return false;
        Relation o = (Relation) obj;
        if ( upper==o.getUpper() && parent==o.getParent() && child==o.getChild() ) return true;
        return false;
    }

    public int hashCode() {
        String tmp = "Relation"+id;
        return tmp.hashCode();
    }
}
