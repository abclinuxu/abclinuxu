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
package cz.abclinuxu.utils.email.forum;

import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Category;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Contains comments to be sent.
 */
public class ForumPool {
    static List pool;
    static {
        pool = Collections.synchronizedList(new ArrayList());
    }

    /**
     * If the discussion is contained in some discussion forum,
     * than store its identification in pool, so it will be
     * sent by email.
     */
    public static void submitComment(Relation relation, int discussionId, int recordId, int threadId) {
        if (! (relation.getParent() instanceof Category) )
            return;
        Persistence persistence = PersistenceFactory.getPersistance();
        Category parent = (Category) persistence.findById(relation.getParent());
        if (parent.getType()!=Category.FORUM)
            return;
        Comment comment = new Comment(relation.getId(), discussionId, recordId, threadId);
        pool.add(comment);
    }

    /**
     * Finds out, whether the pool is empty.
     * @return true, if the pool doesn't contain any unprocessed Comments.
     */
    public static boolean isEmpty() {
        return pool.size()==0;
    }

    /**
     * Extracts the first Comment from the queue.
     * @return first MonitorAction
     * @throws java.lang.IndexOutOfBoundsException If there is no element in the pool.
     */
    public static Comment removeFirst() {
        return (Comment) pool.remove(0);
    }
}
