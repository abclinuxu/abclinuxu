/*
 * User: literakl
 * Date: 31.1.2004
 * Time: 21:19:24
 */
package cz.abclinuxu.utils.email.forum;

import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.data.Record;
import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.User;
import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.servlets.Constants;

import java.util.Map;
import java.util.HashMap;
import java.util.Date;

import org.dom4j.Element;

/**
 * Loads a comment and creates environment from it.
 */
public class CommentDecorator {
    public static final String VAR_TITLE = "TITLE";
    public static final String VAR_PUBLISHED = "PUBLISHED";
    public static final String VAR_AUTHOR = "AUTHOR";
    public static final String VAR_CONTENT = "CONTENT";
    public static final String VAR_RELATION_ID = "RELATION_ID";
    public static final String VAR_DISCUSSION_ID = "DISCUSSION_ID";
    public static final String VAR_THREAD_ID = "THREAD_ID";

    /**
     * Creates environment for given Comment. This
     * environment will be used by template engine to
     * render the email.
     * @return environment
     */
    public static Map getEnvironment(Comment comment) {
        HashMap env = new HashMap();
        Persistance persistance = PersistanceFactory.getPersistance();
        GenericDataObject gdo = null;
        Element root;
        String authorName = null;

        if (comment.recordId==0) {
            gdo = (GenericDataObject) persistance.findById(new Item(comment.discussionId));
            root = gdo.getData().getRootElement();
            env.put(VAR_PUBLISHED, gdo.getCreated());
            if ( gdo.getOwner()!=0 ) {
                User author = (User) persistance.findById(new User(gdo.getOwner()));
                authorName = author.getName();
            } else
                authorName = root.elementText("author");
        } else {
            gdo = (GenericDataObject) persistance.findById(new Record(comment.recordId));
            String xpath = "//comment[@id='"+comment.threadId+"']";
            root = (Element) gdo.getData().selectSingleNode(xpath);
            Date created = Misc.parseDate(root.elementText("created"), Constants.isoFormat);
            env.put(VAR_PUBLISHED, created);

            String tmp = root.elementText("author_id");
            if (tmp!=null) {
                int id = Misc.parseInt(tmp, 0);
                authorName = ((User) persistance.findById(new User(id))).getName();
            } else
                authorName = root.elementText("author");
        }
        env.put(VAR_AUTHOR, authorName);

        String title = root.elementText("title");
        env.put(VAR_TITLE, title);
        String text = root.elementText("text");
        text = Tools.removeTags(text);
        env.put(VAR_CONTENT, text);
        env.put(VAR_RELATION_ID, Integer.toString(comment.relationId));
        env.put(VAR_DISCUSSION_ID, Integer.toString(comment.relationId));
        env.put(VAR_THREAD_ID, Integer.toString(comment.threadId));

        String subject = title+" ["+comment.relationId+","+comment.discussionId+","+comment.threadId+"]";
        env.put(EmailSender.KEY_SUBJECT, subject);
        env.put(EmailSender.KEY_FROM, "diskuse@abclinuxu.cz");
        env.put(EmailSender.KEY_REPLYTO, "bounce@abclinuxu.cz");
        env.put(EmailSender.KEY_TEMPLATE, "/mail/forum/comment.ftl");

        return env;
    }
}
