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

import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Record;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Poll;
import cz.abclinuxu.data.view.Discussion;
import cz.abclinuxu.data.view.Comment;
import cz.abclinuxu.data.view.RowComment;
import cz.abclinuxu.exceptions.DuplicateKeyException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.impl.MySqlPersistence;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.freemarker.Tools;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.Element;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * This class has single purpose - fill database for developers with some data,
 * so they can run small abclinuxu on their computers.
 * @author literakl
 * @since 7.11.2005
 */
public class GenerateDevelDatabase {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GenerateDevelDatabase.class);

    static Persistence persistence = PersistenceFactory.getPersistence();
    static Persistence persistenceDevel = PersistenceFactory.getSpecificPersistence(PersistenceFactory.defaultDevelUrl);
    Set copied = new HashSet(500);

    public static void main(String[] args) throws Exception {
        GenerateDevelDatabase app = new GenerateDevelDatabase();
        app.dump(new Relation(185423)); // discussion in forum
        app.dump(new Relation(184502)); // discussion in forum
        app.dump(new Relation(185123)); // news
        app.dump(new Relation(185127)); // news
        app.dump(new Relation(182452)); // article
        app.dump(new Relation(165259)); // series
        app.dump(new Relation(95028)); // faq
        app.dump(new Relation(175493)); // faq
        app.dump(new Relation(107524)); // hardware
        app.dump(new Relation(117428)); // hardware
        app.dump(new Relation(136384)); // software
        app.dump(new Relation(139058)); // software
        app.dump(new Relation(60064)); // dictionary
        app.dump(new Relation(68799)); // dictionary
        app.dump(new Relation(100430)); // dictionary
        app.dump(new Relation(3618)); // driver
        app.dump(new Relation(8471)); // driver
        app.dump(new Relation(157149)); // trivia
        app.dump(new Relation(175886)); // advertisement in bazaar
        app.dump(new Relation(72131)); // blog
        app.dump(new Relation(110144)); // blog story
        app.dump(new Relation(175086)); // blog story
        app.dump(new Relation(184944)); // blog story

        Relation relation = (Relation) persistence.findById(new Relation(184564));
        Poll poll = (Poll) relation.getChild();
        poll.setClosed(false);
        app.dump(relation);

        List<User> wikiAuthors = new ArrayList<User>(100);

        MySqlPersistence persistance = (MySqlPersistence) persistenceDevel;
        Connection con = persistance.getSQLConnection();
        Statement statement = con.createStatement();
        try {
            /* prenest vsechny sekce krome blogu */
            statement.execute("insert into devel.kategorie select * from abc.kategorie where typ!=3");
            statement.execute("insert into devel.relace select R.* from abc.relace R, abc.kategorie K where typ_potomka='K' and potomek=K.cislo and typ!=3");

            /* prenest servery vcetne jejich odkazu */
            statement.execute("insert into devel.server (cislo,jmeno,url) select cislo,jmeno,url from abc.server");
            statement.execute("update devel.server set kontakt=''");
            statement.execute("insert into devel.odkaz select * from abc.odkaz");
            statement.execute("insert into devel.relace select * from abc.relace where typ_predka='S'");

            /* dynamic RSS polozka */
            statement.execute("insert into devel.polozka values(59516,0,NULL,'<data><title>Dynamicka konfigurace</title></data>',1,now(),NULL)");

            /* konstanty data objektu */
            statement.execute("insert into devel.konstanty select * from abc.konstanty");

            /* prenest historii wiki dokumentu */
            statement.execute("insert into devel.verze select V.* from abc.verze V, devel.relace R, devel.polozka P where R.typ_potomka='P' and R.potomek=P.cislo and R.cislo=V.relace");
            ResultSet resultSet = statement.executeQuery("select kdo from devel.verze");
            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                wikiAuthors.add(new User(id));
            }
            persistance.releaseSQLResources(con, statement, resultSet);
        } catch (SQLException e) {
            log.error(e, e);
        }

        for (User user : wikiAuthors) {
            app.dump(user);
        }
    }

    private void dump(Relation relation) {
        Tools.sync(relation);
        save(relation);
        GenericObject child = relation.getChild();
        if (child instanceof Item)
            dump((Item) child);
        else if (child instanceof Record)
            dump((Record) child);
        else if (child instanceof Category)
            dump((Category) child);
        else if (child instanceof Poll)
            dump((Poll) child);
    }

    private void dump(Item item) {
        Tools.sync(item);
        if (item.getType() == Item.DISCUSSION)
            dumpDiscussion(item);
        else if (item.getType() == Item.ARTICLE)
            dumpArticle(item);
        else if (item.getType() == Item.SERIES)
            dumpSeries(item);
        else if (item.getType() == Item.ROYALTIES)
            dumpRoyalty(item);
        save(item);
        dumpOwner(item.getOwner());
        dumpChildren(item.getChildren());
    }

    private void dump(Record record) {
        Tools.sync(record);
        dumpOwner(record.getOwner());
        save(record);
    }

    private void dump(User user) {
        Tools.sync(user);
        Element element = (Element) user.getData().selectSingleNode("/data/settings/blog");
        if (element != null) {
            int id = Integer.parseInt(element.getText());
            dump(new Category(id));
        }
        save(user);
    }

    private void dump(Category category) {
        Tools.sync(category);
        save(category);
    }

    private void dump(Poll poll) {
        Tools.sync(poll);
        save(poll);
        dumpChildren(poll.getChildren());
    }

    private void dumpArticle(Item item) {
        Set<String> authors = item.getProperty(Constants.PROPERTY_AUTHOR);
        for (String author : authors) {
            int rid = Integer.parseInt(author);
            dump(new Relation(rid));
        }
    }

    private void dumpSeries(Item item) {
        List list = item.getData().getRootElement().elements("article");
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Element element = (Element) iter.next();
            int rid = Integer.parseInt(element.getText());
            dump(new Relation(rid));
        }
    }

    private void dumpRoyalty(Item item) {
        item.getData().selectSingleNode("/data/amount").setText("333");
    }

    private void dumpDiscussion(Item item) {
        Discussion discussion = Tools.createDiscussionTree(item, null, 0, false);
        List<Comment> stack = new ArrayList<Comment>();
        stack.addAll(discussion.getThreads());
        if (Tools.isQuestion(item))
            stack.add(Tools.createComment(item));

        while (! stack.isEmpty()) {
            Comment comment = stack.remove(0);
            stack.addAll(comment.getChildren());
            if (comment instanceof RowComment)
                ((RowComment)comment).set_dirty(true);
            Integer author = comment.getAuthor();
            if (author != null)
                dump(new User(author));
        }
    }

    private void dumpChildren(List<Relation> children) {
        for (Relation relation : children) {
            dump(relation);
        }
    }

    private void dumpOwner(int uid) {
        if (uid > 0)
            dump(new User(uid));
    }

    /**
     * Saves specified object into devel database.
     * @param obj object to be copied
     */
    private void save(GenericObject obj) {
        if (copied.contains(obj))
            return;

        filterObject(obj);
        try {
            persistenceDevel.create(obj);
            copied.add(obj.makeLightClone());
            if (copied.size() % 50 != 0)
                System.out.print("#");
            else
                System.out.println();
        } catch (DuplicateKeyException e) {
            // ignore, it is already there
        }
    }

    /**
     * Filter purpose is to remove any privacy and other information that shall not be transfered
     * to devel database.
     * @param obj object to be altered
     */
    private void filterObject(GenericObject obj) {
        if (obj instanceof Item)
            filterObject((Item) obj);
        else if (obj instanceof User)
            filterObject((User) obj);
    }

    /**
     * Filter purpose is to remove any privacy and other information that shall not be transfered
     * to devel database. Here we remove private information from authors.
     * @param item item to be altered
     */
    private void filterObject(Item item) {
        if (item.getType() == Item.AUTHOR) {
            Document data = item.getData();
            removeNode(data, "/data/birthNumber");
            removeNode(data, "/data/accountNumber");
            removeNode(data, "/data/email");
            removeNode(data, "/data/phone");
            removeNode(data, "/data/address");
        }
    }

    /**
     * Filter purpose is to remove any privacy and other information that shall not be transfered
     * to devel database. Here we remove email, password and ticket.
     * @param user user to be altered
     */
    private void filterObject(User user) {
        user.setEmail("x@x.com");
        user.setPassword("xxx");
        user.setProperty(Constants.PROPERTY_TICKET, Collections.singleton("xxx"));
        user.setProperty(Constants.PROPERTY_SCORE, Collections.singleton("33"));
    }

    /**
     * Removes node specified by xpath in given document.
     * @param document document to be updated
     * @param xpath xpath identifying the node
     */
    private void removeNode(Document document, String xpath) {
        Node node = document.selectSingleNode(xpath);
        if (node != null)
            node.detach();
    }
}
