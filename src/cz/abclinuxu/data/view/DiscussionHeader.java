/*
* User: literakl
* Date: Mar 20, 2002
* Time: 6:43:25 PM
* (c)2001-2002 Tinnio
*/
package cz.abclinuxu.data.view;

import cz.abclinuxu.data.Item;

import java.util.Date;

/**
 * used to hold discussion statistics
 */
public class DiscussionHeader {
    public Item discussion;
    public int relationId;
    /** count of responses in this discussion */
    public int responseCount;
    /**
     * When the discussion was modified for the last time.
     * If there were no responses, than creation time of the dicsussion.
     */
    public Date lastUpdate;

    public DiscussionHeader(Item discussion) {
        this.discussion = discussion;
    }

    public Item getDiscussion() {
        return discussion;
    }

    public int getResponseCount() {
        return responseCount;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public int getRelationId() {
        return relationId;
    }
}
