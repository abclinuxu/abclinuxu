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

import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.servlets.Constants;

import java.util.List;

import org.dom4j.Element;

/**
 * Moves user id from XML to property.
 * @author literakl
 * @since 28.3.2007
 */
public class MigrateAuthors {
    public static void main(String[] args) {
        SQLTool sqlTool = SQLTool.getInstance();
        Persistence persistence = PersistenceFactory.getPersistance();
        List<Relation> authors = sqlTool.findItemRelationsWithType(Item.AUTHOR, null);
        Tools.syncList(authors);
        for (Relation relation : authors) {
            Item item = (Item) relation.getChild();
            Element element = (Element) item.getData().selectSingleNode("/data/uid");
            if (element == null)
                continue;
            String uid = element.getText();
            item.addProperty(Constants.PROPERTY_USER, uid);
            element.detach();
            persistence.update(item);
        }
    }
}
