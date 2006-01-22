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
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.view.TOC;
import cz.abclinuxu.data.view.Chapter;
import cz.abclinuxu.utils.freemarker.Tools;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Displays table of content for content with the hierarchy.
 * @author literakl
 * @since 22.1.2006
 */
public class ViewTOC {
    public static final String VAR_TOC = ViewContent.VAR_TOC;

    public static String show(HttpServletRequest request, Map env) throws Exception {
        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Item item = (Item) relation.getChild();

        TOC toc = new TOC(item.getData().getRootElement());
        Chapter chapter = toc.getFirstChapter();
        List queue = new ArrayList(toc.getChapterCount());
        queue.add(chapter);
        while (queue.size() > 0) {
            chapter = (Chapter) queue.remove(0);
            relation = new Relation(chapter.getRid());
            Tools.sync(relation);
            chapter.setRelation(relation);
            queue.addAll(chapter.getChapters());
        }

        env.put(VAR_TOC, toc);
        return FMTemplateSelector.select("ViewContent", "toc", env, request);
    }
}
