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
import cz.abclinuxu.data.Category;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.impl.MySqlPersistence;
import cz.abclinuxu.utils.TagTool;
import cz.abclinuxu.utils.freemarker.Tools;

import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 * Detects tags on existing data.
 * @author literakl
 * @since 17.2.2008
 */
public class SetTags {
    static Persistence persistence = PersistenceFactory.getPersistence();
    static SQLTool sqlTool = SQLTool.getInstance();

    public static void main(String[] args) throws Exception {
        TagTool.init();
        System.out.print("Tagging categories");
        setOnCategory();
        System.out.println(" done");
        System.out.print("Tagging articles");
        setOnItem(Item.ARTICLE);
        System.out.println(" done");
        System.out.print("Tagging bazaar");
        setOnItem(Item.BAZAAR);
        System.out.println(" done");
        System.out.print("Tagging blogs");
        setOnItem(Item.BLOG);
        System.out.println(" done");
        System.out.print("Tagging wiki documents");
        setOnItem(Item.CONTENT);
        System.out.println(" done");
        System.out.print("Tagging dictionaries");
        setOnItem(Item.DICTIONARY);
        System.out.println(" done");
        System.out.print("Tagging discussions");
        setOnItem(Item.DISCUSSION);
        System.out.println(" done");
        System.out.print("Tagging drivers");
        setOnItem(Item.DRIVER);
        System.out.println(" done");
        System.out.print("Tagging faq");
        setOnItem(Item.FAQ);
        System.out.println(" done");
        System.out.print("Tagging hardware");
        setOnItem(Item.HARDWARE);
        System.out.println(" done");
        System.out.print("Tagging news");
        setOnItem(Item.NEWS);
        System.out.println(" done");
        System.out.print("Tagging personalities");
        setOnItem(Item.PERSONALITY);
        System.out.println(" done");
        System.out.print("Tagging screenshots");
        setOnItem(Item.SCREENSHOT);
        System.out.println(" done");
        System.out.print("Tagging series");
        setOnItem(Item.SERIES);
        System.out.println(" done");
        System.out.print("Tagging software");
        setOnItem(Item.SOFTWARE);
        System.out.println(" done");
    }

    private static void setOnItem(int type) {
        int total = sqlTool.countItemsWithType(type);
        for (int i = 0; i < total;) {
            List<Item> data = sqlTool.findItemsWithType(type, i, 50);
            Tools.syncList(data);
            i += data.size();

            for (Item item : data) {
                TagTool.assignDetectedTags(item, null);
            }
        }
    }

    private static void setOnCategory() throws Exception {
        MySqlPersistence persistance = (MySqlPersistence) persistence;
        Connection con = persistance.getSQLConnection();
        Statement statement = con.createStatement();
        ResultSet resultSet = null;
        List<Category> foundCategories = new ArrayList<Category>(1000);
        try {
            resultSet = statement.executeQuery("select cislo from kategorie where typ!=3");
            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                foundCategories.add(new Category(id));
            }
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }

        List<Category> categories = new ArrayList<Category>(50);
        while (foundCategories.size() > 0) {
            for (int i = 0; i< 50 &&  ! foundCategories.isEmpty(); i++)
                categories.add(foundCategories.remove(0));

            Tools.syncList(categories);
            for (Category category : categories) {
                TagTool.assignDetectedTags(category, null);
            }
        }
    }
}
