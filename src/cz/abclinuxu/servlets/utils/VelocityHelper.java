/*
 * User: literakl
 * Date: Jan 15, 2002
 * Time: 7:39:39 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.utils;

import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.PersistanceException;
import org.dom4j.Document;
import org.dom4j.Node;

/**
 * This class provides several methods, that
 * make velocity developer's life easier.
 */
public class VelocityHelper {

    /**
     * Get name of this child in this relation context. Default name
     * from getChild may be overriden by Name attribute of relation.
     * If child doesn't have any name, it return class name.
     */
    public String getChildName(Relation relation) throws PersistanceException {
        if ( relation==null || relation.getChild()==null ) return null;
        String name = relation.getName();
        if ( name!=null && name.length()>0) return name;

        GenericObject child = relation.getChild();
        if ( !child.isInitialized() ) PersistanceFactory.getPersistance().synchronize(child);

        if ( child instanceof GenericDataObject ) {
            Document data = ((GenericDataObject)child).getData();
            if ( data!=null ) {
                Node node = data.selectSingleNode("data/name");
                if ( node!=null ) name = node.getText();
            }

        } else if ( child instanceof Link ) {
            name = ((Link)child).getText();

        } else if ( child instanceof Poll ) {
            name = ((Poll)child).getText();

        } else if ( child instanceof User ) {
            name = ((User)child).getName();

        } else if ( child instanceof Server ) {
            name = ((Server)child).getName();
        }

        if ( name==null || name.length()==0) {
            name = child.getClass().getName().substring(name.lastIndexOf('.')+1);
        }
        return normalizeEncoding(name);
    }

    /**
     * Gets text value of node selected by xpath expression for GenericObject.
     * If <code>obj</code> doesn't contain <code>Document data</code> field
     * or xpath element doesn't exist, null is returned.
     */
    public String getXPath(GenericObject obj, String xpath) throws PersistanceException {
        if ( obj==null ) return null;
        if ( !obj.isInitialized() ) PersistanceFactory.getPersistance().synchronize(obj);
        Document doc = null;
        String value = null;

        if ( obj instanceof GenericDataObject ) {
            doc = ((GenericDataObject)obj).getData();
        } else if ( obj instanceof User ) {
            doc = ((User)obj).getData();
        }
        if ( doc==null ) return null;

        Node node = doc.selectSingleNode(xpath);
        if ( node!=null ) value = node.getText();

        return normalizeEncoding(value);
    }

    /**
     * Converts string to encoding, which is best for servlets
     */
    private String normalizeEncoding(String in) {
        return in;
    }
}
