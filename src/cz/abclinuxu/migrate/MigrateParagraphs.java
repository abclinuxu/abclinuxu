/*
 *  Copyright (C) 2008 Leos Literak
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
package cz.abclinuxu.migrate;

import cz.abclinuxu.persistence.impl.MySqlPersistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.utils.parser.clean.HtmlPurifier;
import cz.abclinuxu.data.XMLHandler;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.DocumentException;
import org.dom4j.Element;

/**
 * @author literakl
 * @since 11.10.2008
 */
public class MigrateParagraphs {

    public static void main(String[] args) throws SQLException {
        convertParagraphs("zaznam", 50);
        convertParagraphs("kategorie", 50);
        convertParagraphs("polozka", 80000);
        convertParagraphs("komentar", 800000);
    }

    public static void convertParagraphs(String table, int initial) throws SQLException {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getUncachedPersistence();
        Connection con = null;
        Statement statement = null;
        PreparedStatement updateStatement = null;
        ResultSet rs = null;
        int items = 0, strings = 0, id, result;
        List<Integer> ids = new ArrayList<Integer>(initial), interval;
        long started = System.currentTimeMillis();
        String data, content, converted, repaired;
        StringBuilder sb;
        Document doc;
        List nodes;
        try {
            con = persistance.getSQLConnection();
            updateStatement = con.prepareStatement("update " + table + " set data=? where cislo=?");
            statement = con.createStatement();
            rs = statement.executeQuery("select cislo from " + table + " where data like '%format=\"0\">%'");
            while (rs.next())
                ids.add(rs.getInt(1));

            statement.close(); // flush it, it could hold megabytes
            statement = con.createStatement();

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
                    try {
                        doc = DocumentHelper.parseText(data);
                    } catch (DocumentException e) {
                        System.err.println("\nFailed to parse XML from table " + table + " with id " + id + "!");
                        e.printStackTrace();
                        continue;
                    }

                    nodes = doc.selectNodes("//*[@format='0']");
                    for (Iterator iter = nodes.iterator(); iter.hasNext();) {
                        Element element = (Element) iter.next();
                        content = element.getText();
                        converted = HtmlPurifier.clean(content);
                        element.setText(converted);
                        element.attribute("format").detach();
                        strings++;
                    }

                    repaired = XMLHandler.getDocumentAsString(doc);
                    updateStatement.setString(1, repaired);
                    updateStatement.setInt(2, id);
                    result = updateStatement.executeUpdate();
                    if (result != 1) {
                        System.err.println("\nFailed to update table " + table + " with id " + id + "!");
                        continue;
                    }

                    hash(items++);
                }
            }

            System.out.println();
            System.out.println();
            System.out.println("Table " + table + ": modified " + items + " rows, " + strings + " strings, " +
                                (System.currentTimeMillis() - started) / 1000 + " seconds");
            System.out.println();
            System.out.println();
        } finally {
            PersistenceFactory.releaseSQLResources(con, new Statement[] {statement, updateStatement}, new ResultSet[] {rs});
        }
    }

    static void hash(int column) {
        System.out.print('#');
        if (column % 50 == 49) {
            System.out.println(" " + (column + 1));
            System.out.flush();
        }
    }
}
