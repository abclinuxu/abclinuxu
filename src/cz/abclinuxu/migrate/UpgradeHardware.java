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

import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.impl.MySqlPersistance;
import cz.abclinuxu.persistance.versioning.VersioningFactory;
import cz.abclinuxu.persistance.versioning.Versioning;
import cz.abclinuxu.persistance.versioning.VersionInfo;
import cz.abclinuxu.persistance.extra.Qualifier;
import cz.abclinuxu.persistance.extra.LimitQualifier;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Record;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.format.Format;
import cz.abclinuxu.utils.format.SimpleFormatRenderer;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.exceptions.PersistanceException;

import java.util.List;
import java.util.Iterator;
import java.util.Date;
import java.util.Collections;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.SQLException;

import org.dom4j.Element;

/**
 * Tool that migrates from original system of one item with many records to single item.
 * If there are multiple records, they are stored as revisions and merged together.
 * @author literakl
 * @since 27.11.2005
 */
public class UpgradeHardware {
    MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
    Versioning versioning = VersioningFactory.getVersioning();
    SQLTool sqlTool = SQLTool.getInstance();

    void run() throws Exception {
        int count = sqlTool.countItemRelationsWithType(Item.HARDWARE, new Qualifier[]{});
        long start = System.currentTimeMillis();
        int i = 0;
        while (i<count) {
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_ID, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, 50)};
            List items = sqlTool.findItemRelationsWithType(Item.HARDWARE, qualifiers);
            Tools.syncList(items);
            i += items.size();

            for (Iterator iter = items.iterator(); iter.hasNext();) {
                Relation relation = (Relation) iter.next();
                migrateItem(relation);
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("Migrace "+i+" hardwarovych polozek trvala "+(end-start)+" ms.");
    }

    void migrateItem(Relation relation) throws Exception {
        Item item = (Item) relation.getChild();
        List children = item.getChildren();
        Tools.syncList(children);

        String path = Integer.toString(relation.getId());
        String price, driver, tmp;
        Element setup, params, identification, note, element;
        price = driver = null;
        setup = params = identification = note = null;
        Record record = null;
        boolean merged = false;
        VersionInfo versionInfo = null;

        for (Iterator iter = children.iterator(); iter.hasNext();) {
            Relation child = (Relation) iter.next();
            if (!child.getChild().isInitialized())
                iter.remove(); // probably its link was already migrated
        }

        if (children.size()==0)
            return;
        if (children.size()==1) {
            Relation child = (Relation) children.get(0);
            record = (Record) child.getChild();
            item.setOwner(record.getOwner());

            Element root = record.getData().getRootElement();
            element = root.element("price");
            if (element!=null) {
                price = element.getText();
                if ("unknown".equals(price))
                    price = null;
            }

            element = root.element("driver");
            if (element!=null) {
                driver = element.getText();
                if ("unknown".equals(driver))
                    driver = null;
            }

            setup = root.element("setup");
            params = root.element("params");
            identification = root.element("identification");
            note = root.element("note");

            persistance.remove(child);
        } else {
            merged = true;
            item.setOwner(Constants.USER_REDAKCE);
            children = Sorters2.byDate(children, Sorters2.ASCENDING);
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                Relation child = (Relation) iter.next();
                record = (Record) child.getChild();
                Element root = record.getData().getRootElement();
                Element originalDoc = item.getData().getRootElement().createCopy();

                // the most new value wins
                element = root.element("price");
                if (element != null) {
                    tmp = element.getText();
                    if (!"unknown".equals(tmp))
                        price = tmp;
                    originalDoc.add(element.createCopy());
                }

                element = root.element("driver");
                if (element != null) {
                    tmp = element.getText();
                    if (!"unknown".equals(tmp))
                        driver = tmp;
                    originalDoc.add(element.createCopy());
                }

                // merge if there are multiple texts
                element = root.element("setup");
                if (element != null) {
                    originalDoc.add(element.createCopy());
                    if (setup==null)
                        setup = element;
                    else {
                        appendElement(setup, element);
                    }
                }

                element = root.element("params");
                if (element!=null) {
                    originalDoc.add(element.createCopy());
                    if (params == null)
                        params = element;
                    else {
                        appendElement(params, element);
                    }
                }

                element = root.element("identification");
                if (element != null) {
                    originalDoc.add(element.createCopy());
                    if (identification == null)
                        identification = element;
                    else {
                        appendElement(identification, element);
                    }
                }

                element = root.element("note");
                if (element != null) {
                    originalDoc.add(element.createCopy());
                    if (note ==null)
                        note = element;
                    else {
                        appendElement(note, element);
                    }
                }

                String userId = Integer.toString(record.getOwner());
                versionInfo = versioning.commit(originalDoc.asXML(), path, userId);
                fixVersionDate(versionInfo, path, record.getUpdated());

                persistance.remove(child);
            }

        }

        Element root = item.getData().getRootElement();
        element = root.element("icon");
        if (element!=null)
            element.detach();
        element = root.element("comments");
        if (element!=null)
            element.detach();

        if (price!=null)
            root.addElement("price").setText(price);
        if (driver!=null)
            root.addElement("driver").setText(driver);
        if (setup!=null)
            root.add(setup.createCopy());
        if (params!=null)
            root.add(params.createCopy());
        if (identification!=null)
            root.add(identification.createCopy());
        if (note!=null)
            root.add(note.createCopy());

        // commit new version
        String userId = Integer.toString(item.getOwner());
        versionInfo = versioning.commit(item.getData().asXML(), path, userId);

        persistance.update(item);
        if (!merged) {
            fixVersionDate(versionInfo, path, record.getUpdated());
            sqlTool.setUpdatedTimestamp(item, record.getUpdated());
        }
    }

    /**
     * Merges content of two elements with texts and maintains
     * format logic. If both elements are in simple format,
     * both texts are merged together by concatenation.
     * If one of element is in HTML format, the other is
     * converted to HTML format too and merged, the result
     * is in HTML format. Texts are divided by HR element.
     * @param first first element, to which second will be appended. Its format may be changed from simple to html.
     * @param second the element, whose text is to be appended to the first element.
     */
    void appendElement(Element first, Element second) {
        boolean firstInHtml = Format.HTML.getId() == Misc.parseInt(first.attributeValue("format"), 0);
        boolean secondInHtml = Format.HTML.getId() == Misc.parseInt(second.attributeValue("format"), 0);
        if (! (firstInHtml || secondInHtml)) {
            StringBuffer sb = new StringBuffer(first.getText());
            sb.append("\n\n<hr><!-- merged -->\n\n");
            sb.append(second.getText());
            first.setText(sb.toString());
            return;
        }

        if (!firstInHtml) {
            first.attribute("format").setText(Integer.toString(Format.HTML.getId()));
            String firstText = convertToHtml(first.getText());
            StringBuffer sb = new StringBuffer(firstText);
            sb.append("\n\n<p/><hr><!-- merged --><p/>\n\n");
            sb.append(second.getText());
            first.setText(sb.toString());
            return;
        }

        String secondText = convertToHtml(second.getText());
        StringBuffer sb = new StringBuffer(first.getText());
        sb.append("\n<p/><hr><p/>\n");
        sb.append(secondText);
        first.setText(sb.toString());
        return;
    }

    /**
     * Convert text in Format.SIMPLE to Format.HTML.
     * @param text text in simple format
     * @return text in HTML format.
     */
    String convertToHtml(String text) {
        return SimpleFormatRenderer.getInstance().render(text, Collections.EMPTY_MAP);
    }

    /**
     * Sets version create date to specified date.
     * @param versionInfo info about version
     * @param path path identifier
     * @param modified time when version shall be created
     */
    void fixVersionDate(VersionInfo versionInfo, String path, Date modified) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = persistance.getSQLConnection();
            String sql = "update verze set kdy=? where cesta=? and verze=?";
            statement = con.prepareStatement(sql);
            statement.setTimestamp(1, new Timestamp(modified.getTime()));
            statement.setString(2, path);
            statement.setString(3, versionInfo.getVersion());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pri nastavovani!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, null);
        }
    }

    public static void main(String[] args) throws Exception {
        UpgradeHardware task = new UpgradeHardware();
        task.run();
    }
}
