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

import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Comment;
import cz.abclinuxu.data.view.RowComment;
import cz.abclinuxu.utils.email.monitor.MonitorAction;
import cz.abclinuxu.utils.email.monitor.ObjectType;
import cz.abclinuxu.utils.email.monitor.UserAction;

import java.util.List;

/**
 * Conatins basic information about comment, that was made.
 */
public class CommentNotification extends MonitorAction {
    protected int relationId, discussionId, recordId, threadId;
    protected boolean forum;

    public CommentNotification(String author, Relation relation, Comment comment, String url, boolean forum) {
        super(author, UserAction.REPLY, ObjectType.COMMENT, relation, url);
        setValues(relation, comment, forum);
    }

    public CommentNotification(User author, Relation relation, Comment comment, String url, boolean forum) {
        super(author, UserAction.REPLY, ObjectType.COMMENT, relation, url);
        setValues(relation, comment, forum);
    }

    @Override
    public void gatherRecipients() {
        if (forum) {
            List<Subscription> subscriptions = SubscribedUsers.getInstance().getSubscriptions();
            if (!subscriptions.isEmpty()) {
                for (Subscription subscription : subscriptions) {
                    recipients.add(subscription.getId());
                }
            }
        }
        super.gatherRecipients();
    }

    private void setValues(Relation relation, Comment comment, boolean forum) {
        this.relationId = relation.getId();
        this.discussionId = relation.getChild().getId();
        this.threadId = comment.getId();
        this.forum = forum;
        if (comment instanceof RowComment)
            this.recordId = ((RowComment) comment).getRecord();
    }
}
