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
import cz.abclinuxu.data.Data;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;

import java.util.Date;
import java.util.List;
import java.util.Iterator;

import org.dom4j.Element;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

/**
 * @author literakl
 * @since 22.1.2008
 */
public class MigrateAttachments {
    public static void main(String[] args) {
        SQLTool sqlTool = SQLTool.getInstance();
//
//        System.out.print("Starting to search for Bazaar items ..");
//        int size = sqlTool.countItemsWithType(Item.BAZAAR);
//        System.out.println(" found " + size + " items");
//
//        for (int i = 0, j = 50; i < size; i += j) {
//            if (i + j > size) j = size - i;
//            List<Item> items = sqlTool.findItemsWithType(Item.BAZAAR, i, j);
//            for (Item item : items) {
//                upgradeAttachment(item, true);
//            }
//        }
//        System.out.println();

        System.out.print("Starting to search for personality items ..");
        int size = sqlTool.countItemsWithType(Item.PERSONALITY);
        System.out.println(" found " + size + " items");

        for (int i = 0, j = 50; i < size; i += j) {
            if (i + j > size) j = size - i;
            List<Item> items = sqlTool.findItemsWithType(Item.PERSONALITY, i, j);
            for (Item item : items) {
                upgradeAttachment(item, true);
            }
        }
        System.out.println();
//
//        System.out.print("Starting to search for Blog items ..");
//        size = sqlTool.countItemsWithType(Item.BLOG);
//        System.out.println(" found " + size + " items");
//
//        for (int i = 0, j = 50; i < size; i += j) {
//            if (i + j > size) j = size - i;
//            List<Item> items = sqlTool.findItemsWithType(Item.BLOG, i, j);
//            for (Item item : items) {
//                upgradeAttachment(item, true);
//            }
//        }
//        System.out.println();
//
//        System.out.print("Starting to search for unpublished Blog items ..");
//        size = sqlTool.countItemsWithType(Item.UNPUBLISHED_BLOG);
//        System.out.println(" found " + size + " items");
//
//        for (int i = 0, j = 50; i < size; i += j) {
//            if (i + j > size) j = size - i;
//            List<Item> items = sqlTool.findItemsWithType(Item.UNPUBLISHED_BLOG, i, j);
//            for (Item item : items) {
//                upgradeAttachment(item, true);
//            }
//        }
//        System.out.println();
//
//        System.out.print("Starting to search for Hardware items ..");
//        size = sqlTool.countItemsWithType(Item.HARDWARE);
//        System.out.println(" found " + size + " items");
//
//        for (int i = 0, j = 50; i < size; i += j) {
//            if (i + j > size) j = size - i;
//            List<Item> items = sqlTool.findItemsWithType(Item.HARDWARE, i, j);
//            for (Item item : items) {
//                upgradeAttachment(item, false);
//            }
//        }
//        System.out.println();
//
//        System.out.print("Starting to search for Software items ..");
//        size = sqlTool.countItemsWithType(Item.SOFTWARE);
//        System.out.println(" found " + size + " items");
//
//        for (int i = 0, j = 50; i < size; i += j) {
//            if (i + j > size) j = size - i;
//            List<Item> items = sqlTool.findItemsWithType(Item.SOFTWARE, i, j);
//            for (Item item : items) {
//                upgradeAttachment(item, false);
//            }
//        }
//        System.out.println();
//
//        System.out.print("Starting to search for Desktop items ..");
//        size = sqlTool.countItemsWithType(Item.DESKTOP);
//        System.out.println(" found " + size + " items");
//
//        for (int i = 0, j = 50; i < size; i += j) {
//            if (i + j > size) j = size - i;
//            List<Item> items = sqlTool.findItemsWithType(Item.DESKTOP, i, j);
//            for (Item item : items) {
//                upgradeScreenshot(item);
//            }
//        }
//        System.out.println();
    }

    private static void upgradeAttachment(Item item, boolean setOwner) {
        Element inset = (Element) item.getData().selectSingleNode("/data/inset");
        if (inset == null)
            return;

        Persistence persistence = PersistenceFactory.getPersistence();
        SQLTool sqlTool = SQLTool.getInstance();
        Date updated;

        List images = inset.selectNodes("images/image");
        for (Iterator iter = images.iterator(); iter.hasNext();) {
            Element elementImage = (Element) iter.next();
            String path = elementImage.getText();
            String thumbnailPath = elementImage.attributeValue("thumbnail");

            Data data = new Data();
            data.setType(Data.IMAGE);
            if (setOwner)
                data.setOwner(item.getOwner());
            Document document = DocumentHelper.createDocument();
            data.setData(document);
            Element root = document.addElement("data");
            Element elementScreenshot = root.addElement("object").addAttribute("path", path);
            if (thumbnailPath != null) {
                elementScreenshot.addElement("thumbnail").addAttribute("path", thumbnailPath);
            }

            persistence.create(data);
            Relation dataRelation = new Relation(item, data, 0);
            persistence.create(dataRelation);
        }
        inset.detach();

        updated = item.getUpdated();
        persistence.update(item);
        sqlTool.setUpdatedTimestamp(item, updated);
        System.out.print('#');
    }

    private static void upgradeScreenshot(Item item) {
        Persistence persistence = PersistenceFactory.getPersistence();
        SQLTool sqlTool = SQLTool.getInstance();
        Date updated;

        Element root = item.getData().getRootElement();
        Element element = root.element("image");
        String pathImage = element.getText();
        element.detach();
        element = root.element("listingThumbnail");
        String pathListing = element.getText();
        element.detach();
        element = root.element("detailThumbnail");
        String pathDetail = element.getText();
        element.detach();

        Data data = new Data();
        data.setType(Data.IMAGE);
        data.setOwner(item.getOwner());
        Document document = DocumentHelper.createDocument();
        data.setData(document);

        root = document.addElement("data");
        Element elementScreenshot = root.addElement("object").addAttribute("path", pathImage);
        elementScreenshot.addElement("thumbnail").addAttribute("path", pathListing).addAttribute("useType", "listing");
        elementScreenshot.addElement("thumbnail").addAttribute("path", pathDetail).addAttribute("useType", "detail");

        persistence.create(data);
        Relation dataRelation = new Relation(item, data, 0);
        persistence.create(dataRelation);

        updated = item.getUpdated();
        persistence.update(item);
        sqlTool.setUpdatedTimestamp(item, updated);
        System.out.print('#');
    }
}
