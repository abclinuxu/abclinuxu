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
import cz.abclinuxu.persistence.extra.JobOfferManager;
import cz.abclinuxu.data.Record;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.*;
import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.utils.format.HtmlToTextFormatter;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.finesoft.socd.analyzer.DiacriticRemover;

import java.util.Map;
import java.util.HashMap;
import java.util.prefs.Preferences;
import java.io.UnsupportedEncodingException;

import org.dom4j.Element;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

/**
 * Loads a comment and creates environment from it.
 */
public class CommentDecorator implements Configurable {
    public static final String PREF_SENDER = "sender.address";

    public static final String VAR_CONTENT = "CONTENT";
    public static final String VAR_RELATION_ID = "RELATION_ID";
    public static final String VAR_DISCUSSION_ID = "DISCUSSION_ID";
    public static final String VAR_THREAD_ID = "THREAD_ID";
    public static final String VAR_JOB_OFFER = "JOB";

    static int counter;
    static String sender;

    static {
        ConfigurationManager.getConfigurator().configureAndRememberMe(new CommentDecorator());
    }


    /**
     * Creates environment for given Comment. This
     * environment will be used by template engine to
     * render the email.
     * @return environment
     */
    public static Map getEnvironment(Comment comment) {
        HashMap env = new HashMap();
        Persistence persistence = PersistenceFactory.getPersistence();
        HtmlToTextFormatter formatter = new HtmlToTextFormatter();
        Element root;
        String authorName = null;
        cz.abclinuxu.data.view.Comment dizComment;

        if (comment.recordId == 0) {
            Item item = (Item) persistence.findById(new Item(comment.discussionId));
            dizComment = new ItemComment(item);
            root = item.getData().getRootElement();
        } else {
            Record record = (Record) persistence.findById(new Record(comment.recordId));
            DiscussionRecord dizRecord = (DiscussionRecord) record.getCustom();
            dizComment = dizRecord.getComment(comment.threadId);
            root = (Element) dizComment.getData().getRootElement();
        }

        Integer parent = dizComment.getParent();
        if (parent == null)
            parent = new Integer(0);
        String text = root.elementText("text");
        text = formatter.format(text);

        authorName = dizComment.getAnonymName();
        if (authorName == null) {
            User user = (User) persistence.findById(new User(dizComment.getAuthor().intValue()));
            authorName = user.getNick();
            if (authorName == null)
                authorName = user.getName();
        }
        authorName = DiacriticRemover.getInstance().removeDiacritics(authorName);

        env.put(VAR_CONTENT, text);
        env.put(VAR_RELATION_ID, Integer.toString(comment.relationId));
        env.put(VAR_DISCUSSION_ID, Integer.toString(comment.discussionId));
        env.put(VAR_THREAD_ID, Integer.toString(comment.threadId));
        env.put(EmailSender.KEY_SUBJECT, dizComment.getTitle());
        try {
            Address from = new InternetAddress(sender, authorName);
            env.put(EmailSender.KEY_FROM, from);
        } catch (UnsupportedEncodingException e) {
            env.put(EmailSender.KEY_FROM, sender);
        }
        env.put(EmailSender.KEY_TEMPLATE, "/mail/forum/comment.ftl");
        env.put(EmailSender.KEY_SENT_DATE, dizComment.getCreated());
        env.put(EmailSender.KEY_MESSAGE_ID, ""+comment.discussionId+"."+comment.threadId+"@abclinuxu.cz");
        env.put(EmailSender.KEY_REFERENCES, ""+comment.discussionId+"."+parent+"@abclinuxu.cz");

        JobOffer offer = JobOfferManager.getOffer(counter++);
        env.put(VAR_JOB_OFFER, offer);

        return env;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        sender = prefs.get(PREF_SENDER, null);
    }
}
