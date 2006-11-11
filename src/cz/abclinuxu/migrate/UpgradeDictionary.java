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

import cz.abclinuxu.persistence.impl.MySqlPersistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.versioning.Versioning;
import cz.abclinuxu.persistence.versioning.VersioningFactory;
import cz.abclinuxu.persistence.versioning.VersionInfo;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Record;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.url.CustomURLCache;
import cz.finesoft.socd.analyzer.DiacriticRemover;

import java.util.List;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Upgrades dictionary from Item-Record to wiki format (publicly editable Item).
 * @author literakl
 * @since 10.11.2006
 */
public class UpgradeDictionary {
    static MySqlPersistence persistence = (MySqlPersistence) PersistenceFactory.getPersistance();
    static Versioning versioning = VersioningFactory.getVersioning();
    static SQLTool sqlTool = SQLTool.getInstance();
    static DiacriticRemover instance = DiacriticRemover.getInstance();

    public static void main(String[] args) {
//        System.out.println(UrlUtils.PREFIX_DICTIONARY + "/" + URLManager.enforceRelativeURL("køí¾enì¹"));
//        if (true) return;
        long start = System.currentTimeMillis();
        List items = sqlTool.findItemRelationsWithType(Item.DICTIONARY, null);
        Tools.syncList(items);

        for (Iterator iter = items.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            migrateItem(relation);
        }
        long end = System.currentTimeMillis();
        System.out.println("Upgrade " + items.size() + " pojmu trval " + (end - start) + " ms.");
    }

    /**
     * Loads item, its record, moves description from record to item, deletes record and its relation,
     * stores item revision and calculates new relation url. If it is different, then old url will be
     * stored in translation table and new url set to relation.
     * @param relation
     */
    private static void migrateItem(Relation relation) {
        Item item = (Item) persistence.findById(relation.getChild());
        Relation child = (Relation) persistence.findById(item.getChildren().get(0));
        Record record = (Record) persistence.findById(child.getChild());

        Document document = item.getData();
        Element root = document.getRootElement();
        String name = root.elementText("name");
        String normalizedName = instance.removeDiacritics(name);
        normalizedName = normalizedName.toLowerCase();
        item.setSubType(normalizedName);
        String desc = record.getData().getRootElement().elementText("description");
        root.addElement("description").setText(desc);
        item.setCreated(record.getUpdated());

        persistence.update(item);
        sqlTool.setUpdatedTimestamp(item, record.getUpdated());
        persistence.remove(child);

        String userId = Integer.toString(record.getOwner());
        String path = Integer.toString(relation.getId());
        VersionInfo versionInfo = versioning.commit(document.asXML(), path, userId);
        UpgradeHardware.fixVersionDate(versionInfo, path, record.getUpdated(), persistence);

        String originalUrl = relation.getUrl();
        String url = UrlUtils.PREFIX_DICTIONARY + "/" + URLManager.enforceRelativeURL(name);
        if (! url.equals(originalUrl)) {
            CustomURLCache.getInstance().remove(originalUrl);
            sqlTool.insertOldAddress(originalUrl, null, new Integer(relation.getId()));
            relation.setUrl(url);
            persistence.update(relation);
        }
    }
}
