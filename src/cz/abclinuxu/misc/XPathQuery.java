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
package cz.abclinuxu.misc;

import cz.abclinuxu.persistence.impl.MySqlPersistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.data.XMLHandler;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Utility class that dumps to output result of XPath query
 * for selected rows.
 * @author literakl
 * @since 30.12.2005
 */
public class XPathQuery {

    public static void main(String[] args) throws Exception {
        System.out.println("Input parameters:");
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            System.out.println("arg " + i + " = " + arg);
        }

        if (args.length < 2) {
            System.out.println("Usage: XPathQuery \"sql query\" \"xpath query\"");
            System.out.println("Sql query must return column with well formed XML in the first column,");
            System.out.println("additional columns in SQL result will be dumped to output.");
            System.out.println("Xpath query contains your query, only matching nodes will be printed.");
            System.out.println();
            System.exit(1);
        }

        String sql = args[0], xpath = args[1], tmp;
        Document doc;
        List nodes;
        Node node;
        ResultSetMetaData metaData;
        int columns, i;

        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = persistance.getSQLConnection();
        Statement statement = con.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            tmp = resultSet.getString(1);
            tmp = MySqlPersistence.insertEncoding(tmp);
            doc = new XMLHandler(tmp).getData();
            nodes = doc.selectNodes(xpath);
            if (nodes.size() > 0) {
                for (Iterator iter = nodes.iterator(); iter.hasNext();) {
                    node = (Node) iter.next();
                    System.out.print(node.getText());
                    System.out.print(';');
                }

                metaData = resultSet.getMetaData();
                columns = metaData.getColumnCount();
                for (i=2; i<=columns; i++) {
                    System.out.print(resultSet.getObject(i));
                    System.out.print(';');
                }
                System.out.println();
            }
        }
    }
}
