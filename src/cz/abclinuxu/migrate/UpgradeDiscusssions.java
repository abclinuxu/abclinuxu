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
package cz.abclinuxu.migrate;

import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.impl.MySqlPersistence;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Record;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.servlets.Constants;

import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.sql.*;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * Script to upgrade discussions from VERSION_2_2
 */
public class UpgradeDiscusssions {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UpgradeDiscusssions.class);

    static Persistence persistence;
    static {
        persistence = PersistenceFactory.getPersistence();
    }
    static int counter = 0;

    public static void main(String[] args) throws Exception {
        log.info("User "+System.getProperty("user.name")+" started upgrade of discussions.");
        List list = getDiscussions();
        log.info(list.size()+ " discussions found.");
        for ( Iterator iter = list.iterator(); iter.hasNext(); ) {
            Integer key = (Integer) iter.next();
            Item item = (Item) persistence.findById(new Item(key.intValue()));
            if ( removeEmptyDiscussion(item) ) {
                log.info("Removed empty discussion with id "+item.getId());
                printHash();
                continue;
            }
            migrateDiscussion(item);
            printHash();
        }
        log.info("finished");
    }

    private static List getDiscussions() throws Exception {
        MySqlPersistence mp = (MySqlPersistence) persistence;
        Connection con = null; Statement statement = null; ResultSet resultSet = null;
        List list = new ArrayList(6500);
        try {
            con = mp.getSQLConnection();
            statement = con.createStatement();
            resultSet = statement.executeQuery("select cislo from polozka where typ=3 order by cislo asc");

            while ( resultSet.next() ) {
                list.add(new Integer(resultSet.getInt(1)));
            }
            return list;
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * If discussion is not question (it has no title) and it is empty
     * (no comments), remove it.
     * @return true, if discussion was empty and thus removed
     * @throws Exception
     */
    private static boolean removeEmptyDiscussion(Item item) throws Exception {
        if (item.getData().selectSingleNode("/data/title")==null && item.getChildren().size()==0) {
            MySqlPersistence mp = (MySqlPersistence) persistence;
            mp.remove(item);
            Connection con = null; PreparedStatement statement = null;
            try {
                con = mp.getSQLConnection();
                statement = con.prepareStatement("delete from relace where typ_potomka='P' and potomek=?");
                statement.setInt(1,item.getId());
                statement.executeUpdate();
                return true;
            } finally {
                PersistenceFactory.releaseSQLResources(con, statement, null);
            }
        }
        return false;
    }

    /**
     * Migrates discussion to new schema, removing any unused objects.
     * @param item discussion to be upgraded
     */
    private static void migrateDiscussion(Item item) throws Exception {
        Element data = DocumentHelper.createDocument().addElement("data");
        boolean isFirst = true;
        Record firstRecord = null;
        Date lastResponse = item.getUpdated();
        int comments = 0;

        Tools.syncList(item.getChildren());
        Sorters2.byId(item.getChildren()); // fucntionality has been changed slightly

        for ( Iterator iter = item.getChildren().iterator(); iter.hasNext(); ) {
            Relation child = (Relation) iter.next();
            Record record = (Record) child.getChild();

            Element comment = data.addElement("comment");
            comment.addAttribute("id",""+record.getId());
            comment.addElement("created").setText(Constants.isoFormat.format(record.getCreated()));

            Node node = record.getData().selectSingleNode("/data/author");
            if (node!=null) {
                comment.addElement("author").setText(node.getText());
            } else {
                comment.addElement("author_id").setText(""+record.getOwner());
            }

            node = record.getData().selectSingleNode("/data/thread");
            if (node!=null) {
                comment.addElement("parent").setText(node.getText());
            } else {
                comment.addElement("parent").setText("0");
            }

            node = record.getData().selectSingleNode("/data/title");
            comment.addElement("title").setText(node.getText());

            node = record.getData().selectSingleNode("/data/text");
            comment.addElement("text").setText(node.getText());

            if ( lastResponse.before(record.getCreated()) )
                lastResponse = record.getCreated();
            comments++;
            if (isFirst) {
                firstRecord = record;
                isFirst = false;
            } else {
                persistence.remove(child);
            }
        }

        if (firstRecord!=null) {
            firstRecord.setData(data.getDocument());
            persistence.update(firstRecord);
        }
        DocumentHelper.makeElement(item.getData(),"/data/comments").setText(""+comments);
        persistence.update(item);
        SQLTool.getInstance().setUpdatedTimestamp(item,lastResponse);
    }

    static void printHash() {
        System.out.print("#");
        if (counter%80==79)
            System.out.println();
        counter++;
    }
}
