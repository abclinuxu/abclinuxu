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

    public TOC(Element root) {
        Element element = root.element("node");
        String ridValue = element.attributeValue("rid");
        int rid = Misc.parseInt(ridValue, -1);
        Chapter parent = new Chapter(null, rid);
        allChapters.put(new Integer(rid), parent);

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
     * Single chapter in the content hierarchy.
     * <pre>
     *          A
     *     B    C     D
     *   E  F  G H  I  J
     * </pre>
     * Using getLeftChapter() and getRightChapter() you can walk previous hierarchy this way:
     * A B E F C G H D I J
     */
    public static class Chapter {
        Chapter parent;
        List chapters;
        int rid;

        public Chapter(Chapter parent, int rid) {
            this.parent = parent;
            this.rid = rid;
        }

        void addChapter(Chapter chapter) {
            if (chapters==null)
                chapters = new ArrayList();
            chapters.add(chapter);
        }

        List getChapters() {
            if (chapters==null)
                return Collections.EMPTY_LIST;
            return chapters;
        }

        /**
         * @return parent chapter or null, if this is top level chapter
         */
        public Chapter getParent() {
            return parent;
        }

        /**
         * @return previous chapter on same level or null, if this is the first chapter.
         */
        public Chapter getPreviousChapter() {
            if (parent == null)
                return null;
            List siblings = parent.getChapters();
            int position = siblings.indexOf(this);
            if (position < 1)
                return null;
            return (Chapter) siblings.get(position-1);
        }

        /**
         * @return next chapter on same level or null, if this is the last chapter.
         */
        public Chapter getNextChapter() {
            if (parent==null)
                return null;
            List siblings = parent.getChapters();
            int position = siblings.indexOf(this);
            if (position+1 == siblings.size() || position == -1)
                return null;
            return (Chapter) siblings.get(position + 1);
        }

        /**
         * Finds most right child of previous chapter on same level or parent, if there is no previous chapter in this level.
         * @return found chapter
         */
        public Chapter getLeftChapter() {
            Chapter left = getPreviousChapter();
            if (left==null)
                return parent;

            Chapter current = left;
            List children;
            do {
                children = current.getChapters();
                if (children.size()==0)
                    return current;
                current = (Chapter) children.get(children.size()-1);
            } while (current!=null);

            return null;
        }

        /**
         * Finds next chapter on same level or parent's next chapter, if there is no next chapter
         * in its level and so on.
         * @return found chapter
         */
        public Chapter getRightChapter() {
            List children = getChapters();
            if (children.size()!=0)
                return (Chapter) children.get(0);

            Chapter current = this, right;
            do {
                right = current.getNextChapter();
                if (right!=null)
                    return right;
                current = current.getParent();
            } while (current!=null);

            return null;
        }

        /**
         * @return relation id for this chapter
         */
        public int getRid() {
            return rid;
        }
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
