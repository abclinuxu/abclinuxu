/*
 * User: Leos Literak
 * Date: Nov 16, 2002
 * Time: 9:52:45 AM
 */
package cz.abclinuxu.data;

import org.dom4j.Document;

/**
 * Marker, that class uses XMLHandler
 */
public interface XMLContainer {

    /**
     * sets XML data of this object
     */
    public void setData(Document data);

    /**
     * sets XML data of this object in String format
     */
    public void setData(String data);

    /**
     * @return XML data of this object
     */
    public Document getData();
}
