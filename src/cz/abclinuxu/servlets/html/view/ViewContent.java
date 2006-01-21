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
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.view.TOC;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

import org.dom4j.Element;

/**
 * Displays content (Item.type==11)
 */
public class ViewContent {
    public static final String VAR_CONTENT = ShowObject.VAR_ITEM;
    public static final String VAR_TOC = "TOC";

    /**
     * Env must contain VAR_CONTENT already.
     */
    public static String show(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) relation.getChild();

        Element element = (Element) item.getData().selectSingleNode("/data/toc");
        if (element != null) {
            int id = Misc.parseInt(element.getText(), -1);
            Item tocItem = (Item) persistance.findById(new Item(id));

            Relation up = null, left = null, right = null;
            TOC toc = new TOC(tocItem.getData().getRootElement());
            TOC.Chapter thisChapter = toc.getChapter(relation.getId());
            TOC.Chapter chapter = thisChapter.getParent();
            if (chapter!=null)
                up = (Relation) Tools.sync(new Relation(chapter.getRid()));
            chapter = thisChapter.getLeftChapter();
            if (chapter!=null)
                left = (Relation) Tools.sync(new Relation(chapter.getRid()));
            chapter = thisChapter.getRightChapter();
            if (chapter!=null)
                right = (Relation) Tools.sync(new Relation(chapter.getRid()));

            Map map = new HashMap();
            map.put("toc", toc);
            map.put("left", left);
            map.put("right", right);
            map.put("up", up);
            env.put(VAR_TOC, map);
        }
        return FMTemplateSelector.select("ViewContent", "view", env, request);
    }
}
