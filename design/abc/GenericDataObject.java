/*
 * User: literakl
 * Date: Dec 11, 2001
 * Time: 7:53:13 AM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package abc;

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

    /** identifier of owner of this object */
    protected int owner;
    /** creation date or last update of this object */
    protected Date updated;
    /** XML with data or this object */
    protected Document data;

    /**
     * @return XML data in String format
     */
    public String getDataAsString() {
    }
}
