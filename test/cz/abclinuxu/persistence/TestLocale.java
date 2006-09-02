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
package cz.abclinuxu.persistence;

import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.data.Record;
import cz.abclinuxu.data.Item;
import org.dom4j.Element;

import java.util.Date;

/**
 * Helper tool for locale troubleshooting. You can use it to test
 * various locale settings to verify that database is not corrupted
 * by invoking some command (e.g. during migration).
 * @author literakl
 * @since 2.9.2006
 */
public class TestLocale {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: type (record or item) id xpath");
            System.out.println("Load specified object from database, selects give element " +
                    "and appends space to it. Finally the object is persisted to database again.");
            System.exit(1);
        }

        boolean typeRecord = "record".equalsIgnoreCase(args[0]);
        int id = Misc.parseInt(args[1], 0);
        String  xpath = args[2];

        Persistence persistence = PersistenceFactory.getPersistance();
        SQLTool sqlTool = SQLTool.getInstance();

        GenericDataObject gdo = (typeRecord)? (GenericDataObject) new Record(id) : new Item(id);
        gdo = (GenericDataObject) persistence.findById(gdo);
        Element element = (Element) gdo.getData().selectSingleNode(xpath);
        if (element == null) {
            System.out.println("XPath '"+xpath+"' nic nenasla v "+(typeRecord? "zaznamu":"polozce")+" cislo "+id);
            System.exit(1);
        }
        element.setText(element.getText()+" ");

        Date modified = gdo.getUpdated();
        persistence.update(gdo);
        sqlTool.setUpdatedTimestamp(gdo, modified);
    }
}
