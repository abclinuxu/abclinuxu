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

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.impl.MySqlPersistence;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.exceptions.NotFoundException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * New table for common properties of GenericDataObject has been created.
 * Titles must be populated with this script.
 * @author literakl
 * @since 30.3.2008
 */
public class MigrateTitles {
    static int column = 0;

    public static void main(String[] args) throws SQLException, DocumentException {
        if (args == null || args.length != 1 || ! "generate".equalsIgnoreCase(args[0])) {
            System.err.println("Usage: MigrateTitles [generate|clean]");
            System.exit(1);
        }

        System.out.println("Starting ..\n");
        long start = System.currentTimeMillis();

        MySqlPersistence persistence = (MySqlPersistence) PersistenceFactory.getUncachedPersistence();
        Connection con = persistence.getSQLConnection();
        PreparedStatement selectStatement, updateStatement;
//        PreparedStatement updateXmlStatement;
        ResultSet resultSet;

//        updateXmlStatement = con.prepareStatement("update kategorie set data=?, zmeneno=zmeneno where cislo=?");
        updateStatement = con.prepareStatement("update spolecne set jmeno=?, zmeneno=zmeneno where typ=? and cislo=?");
        updateStatement.setString(2, "K");
        selectStatement = con.prepareStatement("select cislo,typ,data from kategorie order by cislo limit ?,50");
        int position = 0;
        while (true) {
            selectStatement.setInt(1, position);
            resultSet = selectStatement.executeQuery();
            boolean found = false;
            while (resultSet.next()) {
                found = true;
                int id = resultSet.getInt(1);
                int type = resultSet.getInt(2);
                String tmp = MySqlPersistence.insertEncoding(resultSet.getString(3));

                String title;
                Element element;
                Document document = DocumentHelper.parseText(tmp);
                if (type == Category.BLOG)
                    element = (Element) document.selectSingleNode("/data/custom/title");
                else
                    element = (Element) document.selectSingleNode("/data/name");

                if (element != null) {
                    title = element.getText();
//                    element.detach();
//                    updateXmlStatement.setBytes(1, XMLHandler.getDocumentAsString(document).getBytes());
//                    updateXmlStatement.setInt(2, id);
//                    updateXmlStatement.executeUpdate();
                } else
                    title = null;

                updateStatement.setString(1, title);
                updateStatement.setInt(3, id);
                updateStatement.executeUpdate();
                hash();
            }
            resultSet.close();
            if (!found)
                break;
            position += 50;
        }
        selectStatement.close();
//        updateXmlStatement.close();

        resetHash();

//        updateXmlStatement = con.prepareStatement("update polozka set data=?, zmeneno=zmeneno where cislo=?");
        updateStatement.setString(2, "P");
        selectStatement = con.prepareStatement("select P.cislo,P.typ,P.data,R.predchozi from polozka P, relace R where typ_potomka='P' and potomek=P.cislo order by cislo limit ?,50");
        position = 0;
        while (true) {
            selectStatement.setInt(1, position);
            resultSet = selectStatement.executeQuery();
            boolean found = false;
            while (resultSet.next()) {
                found = true;
                int id = resultSet.getInt(1);
                int type = resultSet.getInt(2);
                String tmp = MySqlPersistence.insertEncoding(resultSet.getString(3));
                int upper = resultSet.getInt(4);

                String title = null;
                Element element;
                Document document = DocumentHelper.parseText(tmp);
                element = (Element) document.selectSingleNode("/data/name");
                if (element == null)
                    element = (Element) document.selectSingleNode("/data/title");
                if (element == null)
                    element = (Element) document.selectSingleNode("/anketa/title");
                if (element == null) {
                    switch(type) {
                        case Item.AUTHOR:
                        case Item.PERSONALITY: {
                            StringBuffer sb = new StringBuffer();
                            String name = document.getRootElement().elementTextTrim("firstname");
                            if (name != null)
                                sb.append(name).append(' ');
                            sb.append(document.getRootElement().elementTextTrim("surname"));
                            title = sb.toString();
                            break;
                        }
                        case Item.NEWS: {
                            String content = document.selectSingleNode("/data/content").getText();
                            String withoutTags = Tools.removeTags(content);
                            title = Tools.limitWords(withoutTags, 6, "");
                            break;
                        }
                        case Item.DISCUSSION: {
                            try {
                                Relation relation = (Relation) persistence.findById(new Relation(upper));
                                GenericObject child = persistence.findById(relation.getChild());
                                title = Tools.childName(child) + " (diskuse)";
                            } catch (NotFoundException e) {
                                title = "Diskuse";
                            }
                        }
                    }
                }

                if (title == null && element != null) {
                    title = element.getText();
//                    element.detach();
//                    updateXmlStatement.setBytes(1, XMLHandler.getDocumentAsString(document).getBytes());
//                    updateXmlStatement.setInt(2, id);
//                    updateXmlStatement.executeUpdate();
                }

                updateStatement.setString(1, title);
                updateStatement.setInt(3, id);
                updateStatement.executeUpdate();
                hash();
            }
            resultSet.close();
            if (!found)
                break;
            position += 50;
        }
        PersistenceFactory.releaseSQLResources(con, new Statement[] {selectStatement, updateStatement}, null);

        resetHash();
        int seconds = (int) (System.currentTimeMillis() - start) / 1000;
        System.out.println("finished (" + seconds + " seconds)");
    }

    static void hash() {
        if (column == 40) {
            column = 0;
            System.out.print('\n');
            System.out.flush();
        }
        System.out.print('#');
        column++;
    }

    static void resetHash() {
        if (column > 0)
            System.out.print('\n');
        column = 0;
    }
}
