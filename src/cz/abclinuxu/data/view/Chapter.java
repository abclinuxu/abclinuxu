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

import cz.abclinuxu.data.Relation;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

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
public class Chapter {
    Chapter parent;
    List chapters;
    int rid;
    Relation relation;

    public Chapter(Chapter parent, int rid) {
        this.parent = parent;
        this.rid = rid;
    }

    void addChapter(Chapter chapter) {
        if (chapters==null)
            chapters = new ArrayList();
        chapters.add(chapter);
    }

    public List getChapters() {
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

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }
}
