/*
 * User: literakl
 * Date: Jan 23, 2002
 * Time: 3:19:31 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.data;

import cz.abclinuxu.exceptions.InvalidDataException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This class defines methods to work with DOM4J - conversions to/from String.
 * Other classes may use object adapter pattern with this class.
 */
public class XMLHandler implements Cloneable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XMLHandler.class);

    /** XML parsed into DOM4J tree */
    protected Document data;
    /** original XML, it is here to allow lazy instantiation */
    protected String stringData;


    /**
     * Creates empty XMLHandler. You must set <code>data</code> afterwards!
     */
    public XMLHandler() {
    }

    /**
     * Creates XMLHandler, which is not initialized.
     */
    public XMLHandler(String s) {
        stringData = s;
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
        if ( data==null && stringData!=null )
            lazyInit();
        return data;
    }

    /**
     * @return XML data in String format
     */
    public String getDataAsString() {
        try {
            if ( data==null ) {
                return (stringData==null)? "":stringData;
            }

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
    public void setData(String data) {
        stringData = data;
    }

    /**
     * Provides lazy initialization
     */
    protected void lazyInit() {
        try {
            this.data = DocumentHelper.parseText(stringData);
            stringData = null;
        } catch (DocumentException e) {
            throw new InvalidDataException("Chyba v XML: \n"+stringData,e);
        }
    }

    public Object clone() {
        if ( data!=null )
            return new XMLHandler(data);
        else
            return new XMLHandler(stringData);
    }

    public String toString() {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("ISO-8859-2");
        format.setSuppressDeclaration(true);

        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            XMLWriter writer = new XMLWriter(os, format);
            writer.write(data);
            return os.toString();
        } catch (IOException e) {
            log.warn(e);
            return "Error while serializing xml: ".concat(e.getMessage());
        }
    }
}
