/*
 * User: literakl
 * Date: Jan 23, 2002
 * Time: 3:19:31 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.data;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.ByteArrayOutputStream;

import cz.abclinuxu.AbcException;

/**
 * This class defines methods to work with DOM4J - conversions to/from String.
 * Other classes may use object adapter pattern with this class.
 */
public class XMLHandler {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XMLHandler.class);

    protected Document data;

    /**
     * Creates empty XMLHandler. You must set <code>data</code> afterwards!
     */
    public XMLHandler() {
    }

    /**
     * Creates XMLHandler initialized with <code>data</code> parameter.
     */
    public XMLHandler(Document data) {
        this.data = data;
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
            String result = os.toString();
            return result;
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

}
