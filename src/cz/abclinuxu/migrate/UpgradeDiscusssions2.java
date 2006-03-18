/*
 *  Copyright (C) 2006 Leos Literak
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

import cz.abclinuxu.data.Record;
import cz.abclinuxu.data.view.DiscussionRecord;
import cz.abclinuxu.data.view.RowComment;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.impl.MySqlPersistance;
import cz.abclinuxu.persistance.cache.EmptyCache;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.Misc;
import org.dom4j.Attribute;
import org.dom4j.Element;

import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Script to upgrade comments - move them to separate table
 */
public class UpgradeDiscusssions2 {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UpgradeDiscusssions2.class);

    static Persistance persistance;
    static SQLTool sqlTool;
    static Connection con;
    static PreparedStatement updateUpper;
    static PreparedStatement selectRelations;
    static int counter = 0;
    static {
        persistance = PersistanceFactory.getPersistance(EmptyCache.class);
        sqlTool = SQLTool.getInstance();
        con = ((MySqlPersistance) persistance).getSQLConnection();
    }

    public static void main(String[] args) throws Exception {
        log.info("User "+System.getProperty("user.name")+" started upgrade of comments.");
        selectRelations = con.prepareStatement("select D.cislo,Z.cislo from relace D, relace Z where Z.predchozi=0 and Z.typ_potomka='Z' and Z.potomek=? and D.typ_potomka='P' and D.potomek=Z.predek");
        updateUpper = con.prepareStatement("update relace set predchozi=? where cislo=?");

        Record record;
        List records;
        int total = sqlTool.countRecordsWithType(Record.DISCUSSION), i = 0;
        long startTime = System.currentTimeMillis();
        while (i < total) {
            records = sqlTool.findRecordsWithType(Record.DISCUSSION, i, 50);
            for (Iterator iter = records.iterator(); iter.hasNext();) {
                record = (Record) iter.next();
                upgradeComments(record);
                i++;
            }
            records.clear();
            printHash();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Upgrade of "+total+" discussion records to new version took "+(endTime-startTime)/1000+" seconds");

        selectRelations.close();
        updateUpper.close();
        con.close();
    }

    static void upgradeComments(Record record) throws Exception {
        try {
            DiscussionRecord diz = new DiscussionRecord();
            record.setCustom(diz);
            Element oldRoot = record.getData().getRootElement();
            List oldComments = oldRoot.elements("comment");
            Map comments = new HashMap(oldComments.size()+1, 1.0f);
            List alone = new ArrayList();

            Element oldComment, element;
            Attribute attribute;
            RowComment comment, upper;
            Date when;
            int parent, user, id;
            for (Iterator iter = oldComments.iterator(); iter.hasNext();) {
                oldComment = (Element) iter.next();
                oldComment.detach();
                oldComment.setName("data");

                attribute = oldComment.attribute("id");
                attribute.detach();
                id = Misc.parseInt(attribute.getText(), 0);

                element = oldComment.element("created");
                element.detach();
                when = Constants.isoFormat.parse(element.getTextTrim());

                element = oldComment.element("parent");
                element.detach();
                parent = Misc.parseInt(element.getTextTrim(), 0);

                element = oldComment.element("author_id");
                if (element != null) {
                    element.detach();
                    user = Misc.parseInt(element.getTextTrim(), 0);
                } else
                    user = 0;

                comment = new RowComment(oldComment);
                comment.setId(id);
                comment.setCreated(when);
                if (user > 0)
                    comment.setAuthor(user);
                if (parent != 0)
                    comment.setParent(parent);
                comments.put(new Integer(comment.getId()), comment);

                if (comment.getParent() != null) {
                    upper = (RowComment) comments.get(comment.getParent());
                    if (upper != null)
                        upper.addChild(comment);
                    else
                        alone.add(comment);
                } else
                    diz.addThread(comment);
            }

            if (alone.size() > 0) {
                for (Iterator iter = alone.iterator(); iter.hasNext();) {
                    comment = (RowComment) iter.next();
                    upper = (RowComment) comments.get(comment.getParent());
                    if (upper != null)
                        upper.addChild(comment);
                    else {
                        comment.setParent(null);
                        diz.addThread(comment);
                        System.out.println("Nenalezen pøedek pro komentáø " + comment.getRowId() + "!");
                    }
                }
            }

            comments.clear();
            alone.clear();
            persistance.update(record);
            setUpper(record);
        } catch (Exception e) {
            System.err.println("Error upgrading record "+record.getId());
            e.printStackTrace();
        }
    }

    static void setUpper(Record record) throws Exception {
        selectRelations.setInt(1, record.getId());
        ResultSet resultSet = selectRelations.executeQuery();
        if (! resultSet.next())
            return;
        updateUpper.setInt(1, resultSet.getInt(1));
        updateUpper.setInt(2, resultSet.getInt(2));
        updateUpper.executeUpdate();
        resultSet.close();
    }

    static void printHash() {
        System.out.print("#");
        if (counter % 80 == 79)
            System.out.println();
        counter++;
    }
}
