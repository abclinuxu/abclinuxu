/*
 * User: literakl
 * Date: 31.1.2004
 * Time: 20:05:43
 */
package cz.abclinuxu.utils.email.forum;

import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
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
        Persistance persistance = PersistanceFactory.getPersistance();
        Category parent = (Category) persistance.findById(relation.getParent());
        if (parent.getType()!=Category.SECTION_FORUM)
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
