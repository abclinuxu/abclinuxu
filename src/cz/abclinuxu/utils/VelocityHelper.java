/*
 * User: literakl
 * Date: Jan 15, 2002
 * Time: 7:39:39 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.utils;

import cz.abclinuxu.data.*;
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
    public String getChildName(Relation relation) {
        if ( relation==null || relation.getChild()==null ) return null;
        String name = relation.getName();
        if ( name!=null && name.length()>0) return name;

        GenericObject child = relation.getChild();

        if ( child instanceof GenericDataObject ) {
            Document data = ((GenericDataObject)child).getData();
            if ( data!=null ) {
                Node node = data.selectSingleNode("data/name");
                if ( node!=null ) name = node.getText();
                if ( name!=null && name.length()>0) return name;
            }

        } else if ( child instanceof Link ) {
            name = ((Link)child).getText();
            if ( name!=null && name.length()>0) return name;

        } else if ( child instanceof Poll ) {
            name = ((Poll)child).getText();
            if ( name!=null && name.length()>0) return name;

        } else if ( child instanceof User ) {
            name = ((User)child).getName();
            if ( name!=null && name.length()>0) return name;

        } else if ( child instanceof Server ) {
            name = ((Server)child).getName();
            if ( name!=null && name.length()>0) return name;
        }

        name = child.getClass().getName();
        return name.substring(name.lastIndexOf('.')+1);
    }
}
