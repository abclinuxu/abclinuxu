/*
 *  Copyright (C) 2008 Leos Literak
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
package cz.abclinuxu.data.view;

import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Data;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.exceptions.InvalidDataException;

import java.util.Map;
import java.util.List;

import org.dom4j.Document;

/**
 * View object around Item.SCREENSHOT.
 * @author literakl
 * @since 22.1.2008
 */
public class Screenshot {
    String url;
    String title;
    String imageUrl;
    String thumbnailListingUrl;
    String thumbnailDetailUrl;

    /**
     * Constructs object from fully initialized relation (including child).
     * @param relation
     */
    public Screenshot(Relation relation) {
        url = relation.getUrl();
        Item item = (Item) relation.getChild();
        title = item.getData().selectSingleNode("/data/title").getText();
        Map children = Tools.groupByType(item.getChildren(), "Data");
        List list = (List) children.get(Constants.TYPE_DATA);
        if (list == null)
            throw new InvalidDataException("Screenshot " + item.getId() + " neobsahuje objekt Data!");

        Relation dataRelation = (Relation) list.get(0);
        Data data = (Data) dataRelation.getChild();
        Document doc = data.getData();
        imageUrl = doc.selectSingleNode("/data/object/@path").getText();
        thumbnailDetailUrl = doc.selectSingleNode("/data/object/thumbnail[@useType='detail']/@path").getText();
        thumbnailListingUrl = doc.selectSingleNode("/data/object/thumbnail[@useType='listing']/@path").getText();
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getThumbnailListingUrl() {
        return thumbnailListingUrl;
    }

    public String getThumbnailDetailUrl() {
        return thumbnailDetailUrl;
    }
}
