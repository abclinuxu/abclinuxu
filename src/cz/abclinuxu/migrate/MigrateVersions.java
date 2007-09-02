/*
 *  Copyright (C) 2007 Leos Literak
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
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.impl.MySqlPersistence;
import cz.abclinuxu.persistence.versioning.VersionInfo;
import cz.abclinuxu.persistence.versioning.Versioning;
import cz.abclinuxu.persistence.versioning.VersioningFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author literakl
 * @since 1.9.2007
 */
public class MigrateVersions {
    static MySqlPersistence persistence = (MySqlPersistence) PersistenceFactory.getPersistence();
    static SQLTool sqlTool = SQLTool.getInstance();
    static Versioning versioning = VersioningFactory.getVersioning();

    public static void main(String[] args) throws Exception {
        long started = System.currentTimeMillis();
//        fixItems(Item.CONTENT);
//        fixItems(Item.DICTIONARY);
//        fixItems(Item.DRIVER);
//        fixItems(Item.FAQ);
//        fixItems(Item.HARDWARE);
//        fixItems(Item.PERSONALITY);
        fixItems(Item.SOFTWARE);
        System.out.println("Migration took " + (System.currentTimeMillis() - started) + " ms.");
    }

    public static void fixItems(int type) throws Exception {
        int total = sqlTool.countItemsWithType(type), i;
        for (i = 0; i < total;) {
            List<Item> data = sqlTool.findItemsWithType(type, i, 50);
            i += data.size();
            for (Item item : data) {
                migrateWikiDocument(item);
            }
        }
        System.out.println("Migrated " + i + " items of type " + type);
    }

    public static void migrateWikiDocument(Item item) throws Exception {
        List<VersionInfo> history = versioning.getHistory(item);
        if (history == null || history.isEmpty()) {
            System.err.println("Chybi historie pro polozku " + item.getId() + " typu " + item.getType());
            return;
        }

        VersionInfo first = history.remove(history.size() - 1);
        VersionInfo last = null;
        if (! history.isEmpty())
            last = history.remove(0);
        Set<String> users = new LinkedHashSet<String>(5, 1.0f);
        for (VersionInfo info : history) {
            if (info.getUser() == last.getUser())
                continue;
            users.add(Integer.toString(info.getUser()));
            if (users.size() == 2)
                break;
        }

        Element info = (Element) item.getData().selectSingleNode("/data/versioning/revisions");
        if (info != null)
            info.detach();
        info = DocumentHelper.makeElement(item.getData(), "/data/versioning/revisions");
        info.addAttribute("last", Integer.toString((last != null) ? last.getVersion() : first.getVersion()));
        Element committers = info.addElement("committers");
        committers.addElement("creator").setText(Integer.toString(first.getUser()));
        if (last != null)
            committers.addElement("last").setText(Integer.toString(last.getUser()));
        for (String s : users) {
            committers.addElement("committer").setText(s);
        }

        Date lastModified = item.getUpdated();
        persistence.update(item);
        sqlTool.setUpdatedTimestamp(item, lastModified);
    }
}
