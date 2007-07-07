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

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.edit.EditAuthor;
import cz.abclinuxu.servlets.html.view.ShowObject;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MigrateArticles {
    private Persistence persistence = PersistenceFactory.getPersistence();
    private SQLTool sqlTool = SQLTool.getInstance();
    private Map authors;
    private User admin = new User(1);

    /**
     * Migrates all article and royalty objects in database
     */
    void run() throws Exception {
        int total, i;
        Item item;
        authors = new HashMap();
        total = sqlTool.countItemsWithType(Item.ARTICLE);
        for (i = 0; i < total;) {
            List data = sqlTool.findItemsWithType(Item.ARTICLE, i, 100);
            i += data.size();

            for (Iterator iter2 = data.iterator(); iter2.hasNext();) {
                item = (Item) iter2.next();
                processArticle(item);
            }
        }
        System.out.println("Migrated "+ total + " articles");

        total = sqlTool.countItemsWithType(Item.ROYALTIES);
        for (i = 0; i < total;) {
            List data = sqlTool.findItemsWithType(Item.ROYALTIES, i, 100);
            i += data.size();

            for (Iterator iter2 = data.iterator(); iter2.hasNext();) {
                item = (Item) iter2.next();
                processRoyalty(item);
            }
        }
        System.out.println("Migrated " + total + " royalties");
    }

    /**
     * Migrates article object
     */
    protected void processArticle(Item article) throws Exception {
        article = (Item) persistence.findById(article);
        Element root = article.getData().getRootElement();
        Integer userId;
        Relation rel;


        Node userNode = root.selectSingleNode("/data/author");
        userId = new Integer(userNode.getText());
        userNode.detach();

        rel = (Relation) authors.get(userId);
        if (rel == null) {
            rel = createAuthor(new User(userId.intValue()));
            authors.put(userId, rel);
        }

        article.addProperty(Constants.PROPERTY_AUTHOR, Integer.toString(rel.getId()));
        persistence.update(article);
    }

    /**
     * Migrates royalty object
     */
    protected void processRoyalty(Item royalty) throws Exception {
        int id = royalty.getOwner();
        Integer userId = new Integer(id);
        Relation rel = (Relation) authors.get(userId);
        if (rel == null) {
            rel = createAuthor(new User(id));
            authors.put(userId, rel);
        }

        royalty.setOwner(rel.getChild().getId());
        persistence.update(royalty);
    }

    /**
     * Creates Author object from the User object
     */
    protected Relation createAuthor(User user) throws Exception {
        user = (User) persistence.findById(user);
        String name = user.getName();
        String surname = name, firstname = null;

        int position = name.lastIndexOf(' ');
        if (position > 0) {
            firstname = name.substring(0, position);
            surname = name.substring(position + 1, name.length());
        }

        Map map = new HashMap();
        Map params = new HashMap();
        map.put(Constants.VAR_PARAMS, params);
        map.put(Constants.VAR_USER, admin);

        params.put(EditAuthor.PARAM_SURNAME, surname);
        params.put(EditAuthor.PARAM_NAME, firstname);
        params.put(EditAuthor.PARAM_NICKNAME, user.getNick());
        params.put(EditAuthor.PARAM_EMAIL, user.getEmail());
        params.put(EditAuthor.PARAM_UID, Integer.toString(user.getId()));

        EditAuthor servlet = new EditAuthor();
        servlet.actionAddStep2(null, null, map, false);
        Relation created = (Relation) map.get(ShowObject.VAR_RELATION);
        return created;
    }

    public static void main(String[] args) throws Exception {
        MigrateArticles task = new MigrateArticles();
        task.run();
    }
}
