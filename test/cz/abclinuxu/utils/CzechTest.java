/*
 * User: literakl
 * Date: Jan 16, 2002
 * Time: 10:56:18 PM
 * (c)2001-2002 Tinnio
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
        String zcaronString = "¾";
        char zcaronChar = '¾';
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
//        String testString = "¾ivoèi¹ný";
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
        String testString = "¾ivoèi¹ný";

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


