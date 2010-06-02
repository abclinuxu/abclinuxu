package cz.abclinuxu.misc;

import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.impl.MySqlPersistence;
import cz.abclinuxu.persistence.cache.EmptyCache;
import cz.abclinuxu.data.XMLHandler;

import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import org.dom4j.DocumentHelper;
import org.dom4j.DocumentException;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 * This class finds comments containing spam and deletes them.
 * User: literakl
 * Date: 2.6.2010
 */
public class DeleteSpams {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DeleteSpams.class);

    static Persistence persistence;
    static SQLTool sqlTool;
    static Connection con;
    static PreparedStatement selectData, selectCount, fixHeader;
    static int counter = 0;

    static {
        persistence = PersistenceFactory.getPersistance(EmptyCache.class);
        sqlTool = SQLTool.getInstance();
        con = ((MySqlPersistence) persistence).getSQLConnection();
    }


    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            // query must have form "select zaznam from komentar K where condition"
            System.out.println("Usage: DeleteSpams \"condition\"");
            System.out.println("It will be used in queries like \"select zaznam from komentar K where $condition\"");
            System.exit(1);
        }

        String matchSpamsCondition = args[0];
        log.info("User " + System.getProperty("user.name") + " started deletion of comments with query\n" + matchSpamsCondition);

        try {
            long startTime = System.currentTimeMillis();
            selectData = con.prepareStatement("SELECT data from polozka where cislo=?");
            selectCount = con.prepareStatement("SELECT count(*),max(id) from komentar where zaznam=?");
            fixHeader = con.prepareStatement("UPDATE polozka set data=? where cislo=?");

            List<Integer[]> items = findDiscussionItems(matchSpamsCondition);
            deleteSpams(matchSpamsCondition);
            for (Integer[] ids : items) {
                fixDiscussionHeader(ids);
            }

            long endTime = System.currentTimeMillis();
            System.out.println(counter + " spams deleted in " + (endTime - startTime) / 1000 + " seconds");
        } catch (Exception e) {
            e.printStackTrace();
            PersistenceFactory.releaseSQLResources(con, new Statement[]{selectData, selectCount, fixHeader}, null);
        }
    }

    private static void fixDiscussionHeader(Integer[] ids) throws Exception {
        ResultSet rsData = null, rsCount = null;
        try {
            Integer itemId = ids[0];
            Integer recordId = ids[1];
            selectData.setInt(1, itemId);
            rsData = selectData.executeQuery();
            if (! rsData.next())
                return;

            selectCount.setInt(1, recordId);
            rsCount = selectCount.executeQuery();
            if (!rsCount.next())
                return;

            int count = rsCount.getInt(1);
            int max = rsCount.getInt(2);

            String data = MySqlPersistence.insertEncoding(rsData.getString(1));
            Document doc;
            try {
                doc = DocumentHelper.parseText(data);
            } catch (DocumentException e) {
                System.err.println("Failed to parse XML from discussion with id " + itemId + "!");
                e.printStackTrace();
                return;
            }

            Element elComments = (Element) doc.selectSingleNode("/data/comments");
            elComments.setText(Integer.toString(count));
            Element elMaxId = (Element) doc.selectSingleNode("/data/last_id");
            elMaxId.setText(Integer.toString(max));

            String repaired = XMLHandler.getDocumentAsString(doc);
            fixHeader.setString(1, repaired);
            fixHeader.setInt(2, itemId);
            fixHeader.executeUpdate();
        } catch (SQLException e) {
            PersistenceFactory.releaseSQLResources(null, null, new ResultSet[] {rsCount, rsData});
            throw e;
        }
    }

    /**
     * @return id of discussion header items
     */
    private static List<Integer[]> findDiscussionItems(String query) throws Exception {
        Statement statement = null;
        List<Integer[]> found = new ArrayList<Integer[]>();
        try {
            String findQuery = "SELECT DISTINCT R.predek,R.potomek FROM komentar K JOIN relace R ON K.zaznam = R.potomek AND " +
                    "R.typ_potomka = 'Z' AND R.typ_predka = 'P' WHERE " + query;
            statement = con.createStatement();
            ResultSet rs = statement.executeQuery(findQuery);
            while(rs.next()) {
                found.add(new Integer[]{rs.getInt(1), rs.getInt(2)});
            }
            return found;
        } catch (SQLException e) {
            PersistenceFactory.releaseSQLResources(null, statement, null);
            throw e;
        }
    }

    private static void deleteSpams(String query) throws Exception {
        Statement statementDelete = null, statementSelect = null;
        ResultSet rs = null;
        try {
            StringBuilder deleteQuery = new StringBuilder("DELETE FROM komentar WHERE cislo IN (");

            statementSelect = con.createStatement();
            rs = statementSelect.executeQuery("SELECT cislo FROM komentar K WHERE " + query);
            while (rs.next()) {
                deleteQuery.append(rs.getInt(1)).append(',');
            }
            deleteQuery.setCharAt(deleteQuery.length() - 1, ')');

            statementDelete = con.createStatement();
            counter = statementDelete.executeUpdate(deleteQuery.toString());
        } catch (SQLException e) {
            PersistenceFactory.releaseSQLResources(null, new Statement[]{statementDelete, statementSelect}, new ResultSet[] {rs});
            throw e;
        }
    }
}
