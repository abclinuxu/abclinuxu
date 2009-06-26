package cz.abclinuxu.migrate;

import cz.abclinuxu.persistence.impl.MySqlPersistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.PersistenceMapping;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.data.XMLHandler;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.DocumentHelper;
import org.dom4j.DocumentException;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Migrate monitors from XML to table monitor.
 * User: literakl
 * Date: 14.2.2009
 */
public class MigrateMonitor extends Migration {
    static MySqlPersistence persistence = (MySqlPersistence) PersistenceFactory.getUncachedPersistence();

    public static void main(String[] args) throws Exception {
        findMonitoredObjects(false);
        findMonitoredObjects(true);
    }

    public static void findMonitoredObjects(boolean items) throws Exception {
        Connection con = null;
        Statement statement = null;
        PreparedStatement insertStatement = null, updateStatement = null;
        ResultSet rs = null;
        List<Integer> ids = new ArrayList<Integer>(),interval;
        String table = (items) ? "polozka" : "kategorie";
        StringBuilder sb;
        String data;
        int id;

        try {
            con = persistence.getSQLConnection();
            statement = con.createStatement();
            rs = statement.executeQuery("select cislo from " + table + " where data like '%<monitor>%'");
            while (rs.next())
                ids.add(rs.getInt(1));

            resetCounter();

            PersistenceFactory.releaseSQLResources(null, statement, rs);
            statement = con.createStatement();
            insertStatement = con.prepareStatement("INSERT INTO monitor (typ,cislo,uzivatel) VALUES (?,?,?)");
            updateStatement = con.prepareStatement("update " + table + " set data=? where cislo=?");

            int size = ids.size();
            for (int i = 0, j = 50; i < size; i += j) {
                if (i + j > size)
                    j = size - i;
                interval = ids.subList(i, i + j);

                sb = new StringBuilder();
                sb.append("select cislo,data from ").append(table).append(" where cislo in (");
                for (Integer key : interval) {
                    sb.append(key).append(',');
                }
                sb.setCharAt(sb.length() - 1, ')');

                rs = statement.executeQuery(sb.toString());
                while (rs.next()) {
                    id = rs.getInt(1);
                    data = MySqlPersistence.insertEncoding(rs.getString(2));
                    fixObject(id, data, table, insertStatement, updateStatement);
                    hash();
                }
            }
        } finally {
            PersistenceFactory.releaseSQLResources(con, new Statement[] {statement,  insertStatement, updateStatement}, new ResultSet[]{rs});
        }
    }

    public static void fixObject(int id, String data, String table, PreparedStatement insertStatement, PreparedStatement updateStatement) throws Exception {
        Document doc;
        int result;
        try {
            doc = DocumentHelper.parseText(data);
            Element monitor = doc.getRootElement().element("monitor");
            if (monitor == null)
                return;

            List users = monitor.elements("id");
            for (Iterator iter = users.iterator(); iter.hasNext();) {
                Element element = (Element) iter.next();
                int uid = Misc.parseInt(element.getText(), -1);
                if (uid == -1)
                    return;

                insertStatement.setString(1, table.equals("polozka") ? PersistenceMapping.TREE_ITEM : PersistenceMapping.TREE_CATEGORY);
                insertStatement.setInt(2, id);
                insertStatement.setInt(3, uid);
                result = insertStatement.executeUpdate();
                if (result != 1) {
                    System.err.println("\nFailed to insert user " + uid + " into " + table + " with id " + id + "!");
                }
            }

            monitor.detach();
            String modified = XMLHandler.getDocumentAsString(doc);
            updateStatement.setString(1, modified);
            updateStatement.setInt(2, id);
            result = updateStatement.executeUpdate();
            if (result != 1) {
                System.err.println("\nFailed to update table " + table + " with id " + id + "!");
            }
        } catch (DocumentException e) {
            System.err.println("\nFailed to parse XML from table " + table + " with id " + id + "!");
            e.printStackTrace();
        }

    }
}
