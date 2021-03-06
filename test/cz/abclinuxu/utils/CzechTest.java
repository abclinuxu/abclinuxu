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
package cz.abclinuxu.utils;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.io.ByteArrayOutputStream;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * Purpose of this class is test, whether the environment
 * soesn't alter some national characters.
 */
public class CzechTest extends TestCase {
    static {
        try { Class.forName("org.gjt.mm.mysql.Driver"); } catch (Exception e) { e.printStackTrace(); }
    }


    /** tests plain string operation */
    public void testString() throws Exception {
        String zcaronString = "ž";
        char zcaronChar = 'ž';
        String tmp = "This includes " + zcaronString + " character.";
        String tmp2 = "This includes " + zcaronChar + " character.";

        assertEquals(tmp,tmp2);

        assertEquals(tmp.charAt(14),zcaronChar);
        assertEquals(tmp2.charAt(14),zcaronChar);

        assertEquals(tmp.charAt(14),zcaronString.charAt(0));
        assertEquals(zcaronChar,zcaronString.charAt(0));

        byte[] bytes = tmp.getBytes();
        byte[] bytesZcaron = new byte[1];
        bytesZcaron[0] = bytes[14];
        String reverse = new String(bytesZcaron);
        assertEquals(zcaronString,reverse);

        bytes = tmp.getBytes("ISO-8859-2");
        bytesZcaron = new byte[1];
        bytesZcaron[0] = bytes[14];
        reverse = new String(bytesZcaron,"ISO-8859-2");
        assertEquals(zcaronString,reverse); // Here it fails
    }

    /** tests raw sql communication */
    public void testMySQL() throws Exception {
//        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/abc?user=literakl");
//
//        String testString = "živočišný";
//        Statement statement = connection.createStatement();
//        statement.executeUpdate("create temporary table test(str varchar(20), str2 text)");
//
//        PreparedStatement st = connection.prepareStatement("insert into test values(?,?)");
//        st.setString(1,testString);
//        st.setBytes(2,testString.getBytes());
//        st.execute();
//
//        st = connection.prepareStatement("select * from test");
//        ResultSet resultSet = st.executeQuery();
//        resultSet.next();
//
//        String foundStr = resultSet.getString(1);
//        String foundStr2 = new String(resultSet.getBytes(2));
//
//        connection.close();
//
//        assertEquals(testString,foundStr);
//        assertEquals(testString,foundStr2);
    }

    /** tests DOM4J manipulation */
    public void testDom4j() throws Exception {
        String testString = "živočišný";

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        root.setText(testString);

        String converted = getDataAsString(document);
        System.out.println("converted = " + converted);
        assertTrue(converted.indexOf(testString)!=-1);

        Document back = DocumentHelper.parseText(insertEncoding(converted));
        back = null;
    }

    private String insertEncoding(String xml) {
        if ( xml==null || xml.startsWith("<?xml") ) return xml;
        return "<?xml version=\"1.0\" encoding=\"ISO-8859-2\" ?>\n"+xml;
    }

    /**
     * @return XML data in String format
     */
    public String getDataAsString(Document data) throws Exception {
        if ( data==null ) return "";
        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        OutputFormat format = new OutputFormat(null,false);
        OutputFormat format = new OutputFormat(null,false,"ISO-8859-2");
        format.setSuppressDeclaration(true);
        XMLWriter writer = new XMLWriter(os,format);

        writer.write(data);
        String result = os.toString();
        return result;
    }

    public CzechTest(String s) {
        super(s);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(CzechTest.class);
        return suite;
    }

    public static void main(String[] args) {
        TestRunner.run(CzechTest.suite());
    }
}


