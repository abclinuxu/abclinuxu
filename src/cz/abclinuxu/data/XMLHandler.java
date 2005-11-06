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
