/*
 * User: literakl
 * Date: Feb 3, 2002
 * Time: 7:09:42 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.transfer;

import java.sql.*;

public class FixRecords {

    static {
        try { Class.forName("org.gjt.mm.mysql.Driver"); } catch (Exception e) { e.printStackTrace(); }
    }

    public static void main(String[] args) throws SQLException {
        Connection conAbc = DriverManager.getConnection("jdbc:mysql://localhost/abc?user=literakl&password=lkaretil&useUnicode=true&characterEncoding=ISO-8859-2");
        Statement statement = conAbc.createStatement();
        ResultSet set = statement.executeQuery("select * from zaznam");

        while ( set.next() ) {
            String data = new String(set.getBytes(3));
            String result = fixLines(data);
            if (data!=result) {
                set.updateBytes(3,result.getBytes());
                set.updateRow();
            }
        }
    }

    public static String fixLines(String str) {
        if ( str==null ) return null;
        if ( (str.indexOf("<")!=-1)&&(str.indexOf(">")!=-1) ) return str;

        StringBuffer sb = new StringBuffer(str);
        for ( int i=0; i<sb.length(); i++ ) {
            char c = sb.charAt(i);
            if ( c=='\n' ) {
                sb.insert(i,"<BR>");
                i += 4;
            }
        }
        return sb.toString();
    }
}
