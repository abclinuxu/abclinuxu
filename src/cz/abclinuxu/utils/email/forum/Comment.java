/*
 * User: literakl
 * Date: 31.1.2004
 * Time: 20:14:36
 */
package cz.abclinuxu.utils.email.forum;

/**
 * Conatins basic information about comment, that was made.
 */
public class Comment {
    public int relationId, discussionId, recordId, threadId;

    public Comment(int relationId, int discussionId, int recordId, int threadId) {
        this.relationId = relationId;
        this.discussionId = discussionId;
        this.threadId = threadId;
        this.recordId = recordId;
    }
}
