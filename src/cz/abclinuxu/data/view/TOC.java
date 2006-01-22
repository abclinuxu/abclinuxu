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
package cz.abclinuxu.data.view;

import org.dom4j.Element;

import java.util.*;

import cz.abclinuxu.utils.Misc;

/**
 * Table of contents for content hierarchy.
 * @author literakl
 * @since 21.1.2006
 */
public class TOC {
    // all chapters in the hierarchy
    Map allChapters = new HashMap();
    Chapter top;

    public TOC(Element root) {
        Element element = root.element("node");
        String ridValue = element.attributeValue("rid");
        int rid = Misc.parseInt(ridValue, -1);
        Chapter parent = new Chapter(null, rid);
        allChapters.put(new Integer(rid), parent);
        top = parent;

        List queue = new ArrayList(), children;
        QueueItem queueItem = new QueueItem(parent, element);
        queue.add(queueItem);
        Chapter chapter;
        while (queue.size()>0) {
            queueItem = (QueueItem) queue.remove(0);
            parent = queueItem.parent;
            element = queueItem.element;

            children = element.elements("node");
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                element = (Element) iter.next();
                ridValue = element.attributeValue("rid");
                rid = Misc.parseInt(ridValue, -1);
                chapter = new Chapter(parent, rid);

                parent.addChapter(chapter);
                allChapters.put(new Integer(rid), chapter);
                queue.add(new QueueItem(chapter, element));
            }
        }
    }

    /**
     * Finds chapter in the hierarchy.
     * @param rid relation id of the searched content
     * @return the chapter or null
     */
    public Chapter getChapter(int rid) {
        return (Chapter) allChapters.get(new Integer(rid));
    }

    /**
     * @return top-level chapter
     */
    public Chapter getFirstChapter() {
        return top;
    }

    /**
     * @return number of all chapters
     */
    public int getChapterCount() {
        return allChapters.size();
    }

    static class QueueItem {
        Chapter parent;
        Element element;

        public QueueItem(Chapter parent, Element element) {
            this.parent = parent;
            this.element = element;
        }
    }
}
