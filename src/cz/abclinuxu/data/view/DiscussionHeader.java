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
    public int lastCommentId;
    /**
     * When the discussion was modified for the last time.
     * If there were no responses, than creation time of the dicsussion.
     */
    public Date updated;
    public Date created;
    public String title;
    public String url;

    public DiscussionHeader(Item discussion) {
        this.discussion = discussion;
    }

    public Item getDiscussion() {
        return discussion;
    }

    public int getResponseCount() {
        return responseCount;
    }

    public Date getUpdated() {
        return updated;
    }

    public Date getCreated() {
        return created;
    }

    public int getRelationId() {
        return relationId;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public int getLastCommentId() {
        return lastCommentId;
    }

    public String toString() {
        return "discussion item " + discussion.getId();
    }
}
