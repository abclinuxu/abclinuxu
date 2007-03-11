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
package cz.abclinuxu.servlets.html.edit;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.data.view.Comment;
import cz.abclinuxu.data.view.RowComment;
import cz.abclinuxu.data.view.ItemComment;
import cz.abclinuxu.data.view.DiscussionRecord;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.parser.safehtml.SafeHTMLGuard;
import cz.abclinuxu.utils.email.monitor.*;
import cz.abclinuxu.utils.email.forum.ForumPool;
import cz.abclinuxu.utils.format.Format;
import cz.abclinuxu.utils.format.FormatDetector;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.exceptions.PersistenceException;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.scheduler.VariableFetcher;

import org.dom4j.*;
import org.htmlparser.util.ParserException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import java.util.*;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

/**
 * This class is responsible for adding new
 * new discussion.<p>
 */
public class EditDiscussion implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditDiscussion.class);

    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_DISCUSSION = "dizId";
    public static final String PARAM_THREAD = "threadId";
    public static final String PARAM_PARENT_THREAD = "parentId";
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_TEXT = "text";
    public static final String PARAM_AUTHOR = "author";
    public static final String PARAM_AUTHOR_ID = "author_id";
    public static final String PARAM_PREVIEW = "preview";
    public static final String PARAM_URL = "url";
    public static final String PARAM_SOLVED = "solved";
    public static final String PARAM_ANTISPAM = "antispam";

    public static final String COOKIE_USER_VERIFIED = "usrVrfd";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_DISCUSSION = "DISCUSSION";
    public static final String VAR_THREAD = "THREAD";
    public static final String VAR_PREVIEW = "PREVIEW";
    public static final String VAR_PARENT_TITLE = "PARENT_TITLE";
    public static final String VAR_FORUM_QUESTION = "FORUM_QUESTION";
    public static final String VAR_USER_VERIFIED = "USER_VERIFIED";

    public static final String ACTION_ADD_DISCUSSION = "addDiz";
    public static final String ACTION_ADD_QUESTION = "addQuez";
    public static final String ACTION_ADD_QUESTION_STEP2 = "addQuez2";
    public static final String ACTION_ADD_COMMENT = "add";
    public static final String ACTION_ADD_COMMENT_STEP2 = "add2";
    public static final String ACTION_CENSORE_COMMENT = "censore";
    public static final String ACTION_CENSORE_COMMENT_STEP2 = "censore2";
    public static final String ACTION_EDIT_COMMENT = "edit";
    public static final String ACTION_EDIT_COMMENT_STEP2 = "edit2";
    public static final String ACTION_REMOVE_COMMENT = "rm";
    public static final String ACTION_REMOVE_COMMENT_STEP2 = "rm2";
    public static final String ACTION_MOVE_COMMENT = "move";
    public static final String ACTION_MOVE_COMMENT_STEP2 = "move2";
    public static final String ACTION_FREEZE_DISCUSSION = "freeze";
    public static final String ACTION_DECREASE_LEVEL = "moveUp";
    public static final String ACTION_RATE_COMMENT = "rate";
    public static final String ACTION_THREAD_TO_DIZ = "toQuestion";
    public static final String ACTION_THREAD_TO_DIZ_STEP2 = "toQuestion2";
    public static final String ACTION_SOLVED = "solved";

//    private static final String LOGIN_REQUIRED = "Litujeme, ale bez registrace je možné komentovat jen otázky v diskusním fóru, " +
//                        "kde se řeší problémy. U ostatních diskusí (zprávičky, články, blogy) je nutné se nejdříve přihlásit. " +
//                        "Toto opatření jsme zavedli z důvodu zvýšené aktivity spambotů a trollů.";

// prepsat a overit kazdou jednotlivou funkci
    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        String action = Misc.getString(params, PARAM_ACTION);

        if (ServletUtils.handleMaintainance(request, env))
            response.sendRedirect(response.encodeRedirectURL("/"));

        if ( relation!=null ) {
            relation = (Relation) persistence.findById(relation);
            persistence.synchronize(relation.getChild());
            env.put(VAR_RELATION,relation);
        } else if ( ! ACTION_ADD_QUESTION.equals(action) )
            throw new MissingArgumentException("Chybí parametr relationId!");

        if ( ACTION_ADD_DISCUSSION.equals(action) )
            return actionAddDiscussion(request,env);

        if ( ACTION_ADD_COMMENT.equals(action) )
            return actionAddComment(request,env);

        if ( ACTION_ADD_COMMENT_STEP2.equals(action) )
            return  actionAddComment2(request,response,env, true);

        if ( ACTION_ADD_QUESTION.equals(action) )
            return actionAddQuestion(request, env);

        if ( ACTION_ADD_QUESTION_STEP2.equals(action) )
            return actionAddQuestion2(request, response, env, true);

        // check permissions
        User user = (User) env.get(Constants.VAR_USER);
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if ( ACTION_SOLVED.equals(action) )
            return actionSolved(request, response, env);

        // check permissions
        if ( !user.hasRole(Roles.DISCUSSION_ADMIN) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if ( ACTION_CENSORE_COMMENT.equals(action) )
            return actionCensore(request, response, env);

        if ( ACTION_CENSORE_COMMENT_STEP2.equals(action) )
            return actionCensore(request, response, env);

        if ( ACTION_EDIT_COMMENT.equals(action) )
            return actionEditComment(request, env);

        if ( ACTION_EDIT_COMMENT_STEP2.equals(action) )
            return actionEditComment2(request, response, env);

        if ( ACTION_REMOVE_COMMENT.equals(action) )
            return actionRemoveComment(request, env);

        if ( ACTION_REMOVE_COMMENT_STEP2.equals(action) )
            return actionRemoveComment2(request, response, env);

        if ( ACTION_MOVE_COMMENT.equals(action) )
            return actionMoveThread(request, env);

        if ( ACTION_MOVE_COMMENT_STEP2.equals(action) )
            return actionMoveThreadStep2(request, response, env);

        if ( ACTION_FREEZE_DISCUSSION.equals(action) )
            return actionAlterFreeze(request, response, env);

        if ( ACTION_DECREASE_LEVEL.equals(action) )
            return actionDecreaseThreadLevel(request, response, env);

        if ( ACTION_THREAD_TO_DIZ.equals(action) )
            return actionToNewDiscussion1(request, env);

        if ( ACTION_THREAD_TO_DIZ_STEP2.equals(action) )
            return actionToNewDiscussion2(request, response, env);

        throw new MissingArgumentException("Chybí parametr action!");
    }

    /**
     * adds new discussion to selected object, if it is not defined meanwhile.
     * then opens form for adding new reaction.
     */
    protected String actionAddDiscussion(HttpServletRequest request, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(VAR_RELATION);
        Relation relChild = InstanceUtils.findFirstChildItemOfType(relation.getChild(),Item.DISCUSSION);
        if ( relChild==null )
            relChild = createEmptyDiscussion(relation, user, persistence);
        Item discussion = (Item) relChild.getChild();

        env.put(VAR_PARENT_TITLE, getTitleFromParent(relation));
        env.put(VAR_RELATION,relChild);
        env.put(VAR_DISCUSSION,discussion);
        params.put(PARAM_AUTHOR, detectSpambotCookie(request, env, user));
        return FMTemplateSelector.select("EditDiscussion","reply",env,request);
    }

    /**
     * Creates and persists empty discussion.
     * @param relation parent relation
     * @param user user that created this discussion (may be empty).
     * @param persistence
     * @return relation between created discussion and its parent
     */
    public static Relation createEmptyDiscussion(Relation relation, User user, Persistence persistence) {
        Item discussion = new Item(0, Item.DISCUSSION);
        Document document = DocumentHelper.createDocument();
        document.addElement("data").addElement("comments").setText("0");
        discussion.setData(document);
        if ( user!=null )
            discussion.setOwner(user.getId());

        persistence.create(discussion);
        Relation relChild = new Relation(relation.getChild(), discussion, relation.getId());
        String url = relation.getUrl();
        if (url!=null) {
            if (url.charAt(url.length()-1)!='/') // zadne url by nemelo koncit na /
                url = url+'/';
            url += "diskuse";
            url = URLManager.protectFromDuplicates(url);
            relChild.setUrl(url);
        }

        persistence.create(relChild);
        relChild.getParent().addChildRelation(relChild);
        return relChild;
    }

    /**
     * Starts add question wizzard
     */
    protected String actionAddQuestion(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        params.put(PARAM_AUTHOR, detectSpambotCookie(request, env, user));
        return FMTemplateSelector.select("EditDiscussion", "ask", env, request);
    }

    /**
     * last step - either shows preview of question or saves new discussion
     */
    public String actionAddQuestion2(HttpServletRequest request, HttpServletResponse response, Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(VAR_RELATION);
        Item discussion = new Item(0,Item.DISCUSSION);

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        root.addElement("comments").setText("0");
        discussion.setData(document);
        ItemComment comment = new ItemComment(discussion);

        boolean canContinue = true;
        canContinue &= setTitle(params, root, env);
        canContinue &= setText(params, root, env);
        canContinue &= setCommentAuthor(params, user, comment, root, env);
        canContinue &= setUserIPAddress(root, request);
        canContinue &= checkSpambot(request, response, params, env, user);

        if ( !canContinue || params.get(PARAM_PREVIEW)!=null ) {
            comment.setCreated(new Date());
            if (user != null)
                comment.setAuthor(new Integer(user.getId()));
            env.put(VAR_PREVIEW, comment);
            return FMTemplateSelector.select("EditDiscussion","ask_confirm",env,request);
        }

        persistence.create(discussion);
        Relation rel2 = new Relation(relation.getChild(),discussion,relation.getId());
        persistence.create(rel2);
        rel2.getParent().addChildRelation(rel2);

        // run email forum and refresh RSS
        ForumPool.submitComment(rel2, discussion.getId(), 0, 0);
        FeedGenerator.updateForum();
        VariableFetcher.getInstance().refreshQuestions();

        if (redirect) {
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/show/"+rel2.getId());
        } else
            env.put(VAR_RELATION, rel2);

        return null;
    }

    /**
     * Displays add comment dialog
     */
    protected String actionAddComment(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        User user = (User) env.get(Constants.VAR_USER);

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params, request);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");
        discussion = (Item) persistence.findById(discussion);
        env.put(VAR_DISCUSSION, discussion);

        String xpath = "/data/frozen";
        Element element = (Element) discussion.getData().selectSingleNode(xpath);
        if ( element!=null )
            return ServletUtils.showErrorPage("Diskuse byla zmrazena - není možné přidat další komentář!", env, request);

        // display discussed comment, only if it has title
        Comment parentThread = getDiscussedComment(params, discussion, persistence);
        if ( parentThread.getTitle() != null )
            env.put(VAR_THREAD, parentThread);
        else {
            if (relation.getParent() instanceof Category) {
                Category category = (Category) relation.getParent();
                if (category.getType()!=Category.FORUM)
                    relation = new Relation(relation.getUpper());
            } else
                relation = new Relation(relation.getUpper());
            env.put(VAR_PARENT_TITLE, getTitleFromParent(relation));
        }

        params.put(PARAM_AUTHOR, detectSpambotCookie(request, env, user));
        return FMTemplateSelector.select("EditDiscussion","reply",env,request);
    }

    /**
     * Adds new comment to selected discussion.
     */
    public synchronized String actionAddComment2(HttpServletRequest request, HttpServletResponse response,
                                                 Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        User user = (User) env.get(Constants.VAR_USER);

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params, request);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");
        discussion = (Item) persistence.findById(discussion).clone();

        String xpath = "/data/frozen";
        Element element = (Element) discussion.getData().selectSingleNode(xpath);
        if ( element!=null )
            return ServletUtils.showErrorPage("Diskuse byla zmrazena - není možné přidat další komentář!", env, request);

        Record record = null;
        DiscussionRecord dizRecord = null;
        Element root = DocumentHelper.createElement("data");
        RowComment comment = new RowComment(root);
        List children = discussion.getChildren();
        if ( children.size()>0 ) {
            record = (Record) ((Relation)children.get(0)).getChild();
            record = (Record) persistence.findById(record).clone();
            dizRecord = (DiscussionRecord) record.getCustom();
        } else {
            record = new Record(0, Record.DISCUSSION);
            Document document = DocumentHelper.createDocument();
            record.setData(document);
            document.addElement("data");
            dizRecord = new DiscussionRecord();
            record.setCustom(dizRecord);
        }

        boolean canContinue = true;
        canContinue &= setId(dizRecord, comment);
        canContinue &= setCreated(comment);
        canContinue &= setParent(params, comment);
        canContinue &= setCommentAuthor(params, user, comment, root, env);
        canContinue &= setTitle(params, root, env);
        canContinue &= setText(params, root, env);
        canContinue &= setUserIPAddress(root, request);
        canContinue &= checkSpambot(request, response, params, env, user);
//        canContinue &= testAnonymCanPostComments(user, env);

        if (!canContinue || params.get(PARAM_PREVIEW) != null) {
            env.put(VAR_DISCUSSION, discussion);
            if (canContinue)
                env.put(VAR_PREVIEW, comment);

            // display discussed comment, only if it has title
            Comment thread = getDiscussedComment(params, discussion, persistence);
            if (thread.getTitle() != null)
                env.put(VAR_THREAD, thread);

            return FMTemplateSelector.select("EditDiscussion", "reply", env, request);
        }

        // now it is safe to modify XML Document, because data were validated
        boolean duplicate = false;
        if (comment.getParent() == null) {
            duplicate = dizRecord.findComment(comment) != null;
            dizRecord.addThread(comment);
        } else {
            Comment parent = dizRecord.getComment(comment.getParent().intValue());
            duplicate = parent.findComment(comment) != null;
            parent.addChild(comment);
        }

        if (duplicate)
            return ServletUtils.showErrorPage("Systém detekoval vícenásobné odeslání totožného komentáře.", env, request);  

        dizRecord.calculateCommentStatistics();

        if (record.getId() == 0) {
            persistence.create(record);
            Relation rel = new Relation(discussion, record, relation.getId());
            persistence.create(rel);
            rel.getParent().addChildRelation(rel);
        } else {
            persistence.update(record);
        }

        Element itemRoot = discussion.getData().getRootElement();
        setCommentsCount(itemRoot, dizRecord);
        persistence.update(discussion);

        // run monitor
        String url = relation.getUrl();
        if (url==null)
            url = "http://www.abclinuxu.cz"+urlUtils.getPrefix()+"/show/"+relation.getId();
        else
            url = "http://www.abclinuxu.cz" + url;
        url += "#" + comment.getId();

        MonitorAction action = null;
        if (user!=null)
            action = new MonitorAction(user, UserAction.ADD, ObjectType.DISCUSSION, discussion, url);
        else {
            String author = (String) params.get(PARAM_AUTHOR);
            action = new MonitorAction(author, UserAction.ADD, ObjectType.DISCUSSION, discussion, url);
        }
        action.setProperty(DiscussionDecorator.PROPERTY_NAME, comment.getTitle());
        String content = root.elementText("text");
        action.setProperty(DiscussionDecorator.PROPERTY_CONTENT, content);
        MonitorPool.scheduleMonitorAction(action);

        // run email forum and update RSS
        if (relation.getParent() instanceof Category) {
            Category parent = (Category) persistence.findById(relation.getParent());
            if (parent.getType() == Category.FORUM) {
                ForumPool.submitComment(relation, discussion.getId(), record.getId(), comment.getId());
                FeedGenerator.updateForum();
                VariableFetcher.getInstance().refreshQuestions();
            }
        }

        if (redirect) {
            url = (String) params.get(PARAM_URL);
            if (url==null)
                url = relation.getUrl();
            if (url==null)
                url = urlUtils.getPrefix()+"/show/"+relation.getId();
            url += "#"+comment.getId();
            urlUtils.redirect(response, url, false);
        }
        return null;
    }

    protected String actionSolved(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        User user = (User) env.get(Constants.VAR_USER);

        SQLTool sqlTool = SQLTool.getInstance();
        Item diz = (Item) persistence.findById(relation.getChild()).clone();
        boolean canContinue = setSolved(params, diz.getData().getRootElement(), sqlTool, relation.getId(), user);
        if (canContinue) {
            Date updated = diz.getUpdated();
            persistence.update(diz);
            SQLTool.getInstance().setUpdatedTimestamp(diz, updated);
        } else
            ServletUtils.addError(Constants.ERROR_GENERIC, "Tuto akci smíte provést jen jednou.", env, request.getSession());

        String url = relation.getUrl();
        if (url == null)
            url = urlUtils.getPrefix() + "/show/" + relation.getId();
        urlUtils.redirect(response, url, false);
        return null;
    }

    /**
     * Changes censore flag on given thread.
     */
    protected synchronized String actionCensore(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params, request);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");
        discussion = (Item) persistence.findById(discussion).clone();

        Relation relation;
        int id = Misc.parseInt((String) params.get(PARAM_THREAD), 0);
        List children = discussion.getChildren();
        if (id == 0 || children.size() == 0) {
            ServletUtils.addError(Constants.ERROR_GENERIC,"Nejde cenzurovat otázku!",env,request.getSession());
            relation = (Relation) env.get(VAR_RELATION);
            urlUtils.redirect(response, "/show/"+relation.getId());
        }

        relation = (Relation) children.get(0);
        Record record = (Record) persistence.findById(relation.getChild());
        relation = (Relation) env.get(VAR_RELATION);
        DiscussionRecord dizRecord = (DiscussionRecord) record.getCustom();
        RowComment comment = (RowComment) dizRecord.getComment(id);
        if (comment != null) {
            Element root = comment.getData().getRootElement();
            Node node = root.selectSingleNode("censored");
            if (node!=null) {
                node.detach();
                comment.set_dirty(true);
                AdminLogger.logEvent(user,"odstranena cenzura na vlakno "+id+" diskuse "+discussion.getId()+", relace "+relation.getId());
            } else {
                String action = (String) params.get(PARAM_ACTION);
                if ( ACTION_CENSORE_COMMENT_STEP2.equals(action) ) {
                    Element censored = root.addElement("censored");
                    censored.addAttribute("admin", Integer.toString(user.getId()));
                    censored.setText((String) params.get(PARAM_TEXT));
                    comment.set_dirty(true);

                    // run monitor
                    String url = "http://www.abclinuxu.cz"+urlUtils.getPrefix()+"/show/"+relation.getId();
                    MonitorAction monitor = new MonitorAction(user, UserAction.CENSORE, ObjectType.DISCUSSION, discussion, url);
                    monitor.setProperty(DiscussionDecorator.PROPERTY_NAME, comment.getTitle());
                    MonitorPool.scheduleMonitorAction(monitor);

                    AdminLogger.logEvent(user, "uvalil cenzuru na vlakno "+id+" diskuse "+discussion.getId()+", relace "+relation.getId());
                } else {
                    env.put(VAR_THREAD, getUnthreadedComment(comment));
                    return FMTemplateSelector.select("EditDiscussion", "censore", env, request);
                }
            }
        }
        persistence.update(record);

        String url = relation.getUrl();
        if (url == null)
            url = urlUtils.getPrefix() + "/show/" + relation.getId();
        urlUtils.redirect(response, url, false);
        return null;
    }

    /**
     * Displays edit comment dialog
     */
    protected String actionEditComment(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params, request);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");
        discussion = (Item) persistence.findById(discussion);
        Comment thread = getDiscussedComment(params, discussion, persistence);

        params.put(PARAM_TITLE, thread.getTitle());
        params.put(PARAM_TEXT,thread.getData().selectSingleNode("//text").getText());

        Integer author = thread.getAuthor();
        if (author!=null)
            params.put(PARAM_AUTHOR_ID, author);
        else
            params.put(PARAM_AUTHOR, thread.getAnonymName());

        return FMTemplateSelector.select("EditDiscussion", "edit", env, request);
    }

    /**
     * Adds new comment to selected discussion.
     */
    protected synchronized String actionEditComment2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params, request);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");
        discussion = (Item) persistence.findById(discussion).clone();

        Comment comment = null; Record record = null;
        DiscussionRecord dizRecord = null; Element root;
        int threadId = Misc.parseInt((String) params.get(PARAM_THREAD), 0);
        if ( threadId==0 ) {
            comment = new ItemComment(discussion);
        } else {
            Relation relation = (Relation) discussion.getChildren().get(0);
            record = (Record) persistence.findById(relation.getChild()).clone();
            dizRecord = (DiscussionRecord) record.getCustom();
            comment = dizRecord.getComment(threadId);
            ((RowComment)comment).set_dirty(true);
        }
        root = comment.getData().getRootElement();

        boolean canContinue = true;
        canContinue &= setTitle(params, root, env);
        canContinue &= setText(params, root, env);
        canContinue &= setCommentAuthor(params, null, comment, root, env);

        if ( !canContinue || params.get(PARAM_PREVIEW)!=null ) {
            if ( canContinue ) {
                env.put(VAR_PREVIEW, getUnthreadedComment(comment));
            }
            return FMTemplateSelector.select("EditDiscussion", "edit", env, request);
        }

        if ( threadId==0 ) {
            Date updated = discussion.getUpdated();
            persistence.update(discussion);
            SQLTool.getInstance().setUpdatedTimestamp(discussion,updated);
        } else
            persistence.update(record);

        Relation relation = (Relation) env.get(VAR_RELATION);
        AdminLogger.logEvent(user, "upravil vlakno "+threadId+" diskuse "+discussion.getId()+", relace "+relation.getId());

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        String url = relation.getUrl();
        if (url == null)
            url = urlUtils.getPrefix() + "/show/" + relation.getId();
        urlUtils.redirect(response, url, false);
        return null;
    }

    /**
     * Displays remove comment dialog
     */
    protected String actionRemoveComment(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params, request);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");
        discussion = (Item) persistence.findById(discussion);

        int threadId = Misc.parseInt((String) params.get(PARAM_THREAD), 0);
        if ( threadId==0 )
            throw new MissingArgumentException("Chybí parametr threadId!");

        Comment comment = getDiscussedComment(params, discussion, persistence);
        env.put(VAR_THREAD, comment);

        return FMTemplateSelector.select("EditDiscussion", "remove", env, request);
    }

    /**
     * Removes selected comment.
     */
    protected synchronized String actionRemoveComment2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);
        Relation mainRelation = (Relation) env.get(VAR_RELATION);

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params, request);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");

        int threadId = Misc.parseInt((String) params.get(PARAM_THREAD), 0);
        if ( threadId==0 )
            throw new MissingArgumentException("Chybí parametr threadId!");

        discussion = (Item) persistence.findById(discussion).clone();

        Relation relation = (Relation) discussion.getChildren().get(0);
        Record record = (Record) persistence.findById(relation.getChild()).clone();
        DiscussionRecord dizRecord = (DiscussionRecord) record.getCustom();

        RowComment comment = (RowComment) dizRecord.getComment(threadId);
        String title = comment.getTitle();
        String content = comment.getData().getRootElement().elementText("text");

        boolean removed = false;
        if (comment.getParent() != null) {
            Comment parent = (Comment) dizRecord.getComment(comment.getParent().intValue());
            if (parent != null)
                removed = parent.removeChild(comment, dizRecord);
        }
        if (!removed)
            dizRecord.removeThread(comment, true);

        dizRecord.calculateCommentStatistics();
        persistence.update(record);
        AdminLogger.logEvent(user, "smazal vlakno " + threadId + ", relace " + mainRelation.getId());

        setCommentsCount(discussion.getData().getRootElement(), dizRecord);
        Date lastUpdate = discussion.getCreated();
        if (dizRecord.getTotalComments() > 0) {
            comment = (RowComment) dizRecord.getLastComment();
            lastUpdate = comment.getCreated();
        }
        persistence.update(discussion);
        SQLTool.getInstance().setUpdatedTimestamp(discussion, lastUpdate);

        // run monitor
        String url = mainRelation.getUrl();
        if (url == null)
            url = "http://www.abclinuxu.cz" + urlUtils.getPrefix() + "/show/" + mainRelation.getId();
        MonitorAction action = new MonitorAction(user, UserAction.REMOVE, ObjectType.DISCUSSION, discussion, url);
        action.setProperty(DiscussionDecorator.PROPERTY_NAME, title);
        action.setProperty(DiscussionDecorator.PROPERTY_CONTENT, content);
        MonitorPool.scheduleMonitorAction(action);

        url = mainRelation.getUrl();
        if (url == null)
            url = urlUtils.getPrefix() + "/show/" + mainRelation.getId();
        urlUtils.redirect(response, url, false);
        return null;
    }

    /**
     * Reverts the current state of monitor on specified discussion.
     */
    public static void alterDiscussionMonitor(Item discussion, User user, Persistence persistence) {
        Date originalUpdated = discussion.getUpdated();
        MonitorTools.alterMonitor(discussion.getData().getRootElement(), user);
        persistence.update(discussion);
        SQLTool.getInstance().setUpdatedTimestamp(discussion, originalUpdated);
    }

    /**
     * Reverts current state of frozen attribute.
     */
    protected synchronized String actionAlterFreeze(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item discussion = (Item) persistence.findById(relation.getChild()).clone();

        String xpath = "/data/frozen";
        Document data = discussion.getData();
        Element element = (Element) data.selectSingleNode(xpath);
        if ( element!=null )
            element.detach();
        else
            DocumentHelper.makeElement(data,xpath);

        Date originalUpdated = discussion.getUpdated();
        persistence.update(discussion);
        SQLTool.getInstance().setUpdatedTimestamp(discussion, originalUpdated);

        User user = (User) env.get(Constants.VAR_USER);
        AdminLogger.logEvent(user, "zmrazil diskusi "+discussion.getId()+", relace "+relation.getId());

        String url = relation.getUrl();
        if (url == null)
            url = "/show/" + relation.getId();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, url);
        return null;
    }

    protected String actionMoveThread(HttpServletRequest request, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params, request);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");
        discussion = (Item) persistence.findById(discussion);
        env.put(VAR_DISCUSSION, discussion);

        return FMTemplateSelector.select("EditDiscussion", "move", env, request);
    }

    protected synchronized String actionMoveThreadStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item discussion = (Item) persistence.findById(relation.getChild()).clone();

        Map params = (Map) env.get(Constants.VAR_PARAMS);
        int threadId = Misc.parseInt((String) params.get(PARAM_THREAD), 0);
        if ( threadId==0 )
            throw new MissingArgumentException("Chybí parametr threadId!");
        int parentId = Misc.parseInt((String) params.get(PARAM_PARENT_THREAD), -1);
        if ( parentId==-1 )
            throw new MissingArgumentException("Chybí parametr parentId!");
        if ( parentId==threadId ) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Pokoušíte se vytvořit smyčku ve stromě!", env, null);
            return actionMoveThread(request, env);
        }

        Record record = (Record) ((Relation) discussion.getChildren().get(0)).getChild();
        record = (Record) persistence.findById(record).clone();
        DiscussionRecord dizRecord = (DiscussionRecord) record.getCustom();

        RowComment comment = (RowComment) dizRecord.getComment(threadId);
        int originalParentId = comment.getParent() == null ? 0 : comment.getParent().intValue();
        if (comment.getParent() != null) {
            Comment parent = (Comment) dizRecord.getComment(originalParentId);
            parent.removeChild(comment, null);
        } else
            dizRecord.removeThread(comment, false);

        if (parentId != 0) {
            Comment parentComment = dizRecord.getComment(parentId);
            if ( parentComment==null ) {
                ServletUtils.addError(Constants.ERROR_GENERIC, "Takový předek neexistuje!", env, null);
                return actionMoveThread(request, env);
            }
            comment.setParent(new Integer(parentComment.getId()));
            parentComment.addChild(comment);
            parentComment.sortChildren();
        } else {
            comment.setParent(null);
            dizRecord.addThread(comment);
            dizRecord.sortThreads();
        }

        comment.set_dirty(true);
        persistence.update(record);
        AdminLogger.logEvent(user, "presunul vlakno "+threadId+", puvodni predek="+originalParentId+", novy predek="+parentId+", relace "+relation.getId());

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/show/"+relation.getId());
        return null;
    }

    /**
     * Moves selected thread one level up. Top level threads are not changed.
     */
    protected synchronized String actionDecreaseThreadLevel(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item discussion = (Item) persistence.findById(relation.getChild());

        Map params = (Map) env.get(Constants.VAR_PARAMS);
        int threadId = Misc.parseInt((String) params.get(PARAM_THREAD), 0);
        if ( threadId==0 )
            throw new MissingArgumentException("Chybí parametr threadId!");

        Record record = (Record) ((Relation) discussion.getChildren().get(0)).getChild();
        record = (Record) persistence.findById(record).clone();
        DiscussionRecord dizRecord = (DiscussionRecord) record.getCustom();

        RowComment comment = (RowComment) dizRecord.getComment(threadId);
        if (comment.getParent() != null) {
            Comment parentComment = dizRecord.getComment(comment.getParent().intValue());
            parentComment.removeChild(comment, null);
            int parentId = parentComment.getParent() == null ? 0 : parentComment.getParent().intValue();
            if (parentId == 0) {
                comment.setParent(null);
                dizRecord.addThread(comment);
                dizRecord.sortThreads();
            } else {
                parentComment = dizRecord.getComment(parentId);
                comment.setParent(new Integer(parentComment.getId()));
                parentComment.addChild(comment);
                parentComment.sortChildren();
            }

            comment.set_dirty(true);
            persistence.update(record);
            AdminLogger.logEvent(user, "presunul vlakno "+threadId+" o uroven vys, relace "+relation.getId());
        }

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/show/"+relation.getId());
        return null;
    }

    protected String actionToNewDiscussion1(HttpServletRequest request, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item discussion = (Item) persistence.findById(relation.getChild());
        Record record = (Record) ((Relation) discussion.getChildren().get(0)).getChild();
        record = (Record) persistence.findById(record);
        DiscussionRecord dizRecord = (DiscussionRecord) record.getCustom();

        Map params = (Map) env.get(Constants.VAR_PARAMS);
        int threadId = Misc.parseInt((String) params.get(PARAM_THREAD), 0);
        if ( threadId==0 )
            throw new MissingArgumentException("Chybí parametr threadId!");

        Comment comment = dizRecord.getComment(threadId);
        params.put(PARAM_TITLE, comment.getTitle());
        params.put(PARAM_TEXT, "<a href=\"/"+urlUtils.getPrefix()+"/show/"+relation.getId()+"\">původní diskuse</a>");

        return FMTemplateSelector.select("EditDiscussion", "toQuestion", env, request);
    }

    /**
     * Extracts selected thread in discussion to new separate discussion.
     */
    protected synchronized String actionToNewDiscussion2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);

        Relation currentDizRelation = (Relation) env.get(VAR_RELATION), newDizRelation;
        Item currentDiz = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params, request);
        if ( currentDiz==null )
            throw new MissingArgumentException("Chybí parametr dizId!");
        currentDiz = (Item) persistence.findById(currentDiz).clone();
        Element currentItemRoot = currentDiz.getData().getRootElement();

        Record currentRecord = (Record) ((Relation) currentDiz.getChildren().get(0)).getChild();
        currentRecord = (Record) persistence.findById(currentRecord).clone();
        DiscussionRecord currentDizRecord = (DiscussionRecord) currentRecord.getCustom();

        int threadId = Misc.parseInt((String) params.get(PARAM_THREAD), 0);
        if (threadId == 0)
            throw new MissingArgumentException("Chybí parametr threadId!");
        RowComment originalComment = (RowComment) currentDizRecord.getComment(threadId);
        RowComment movedComment = (RowComment) originalComment.clone();

        // vytvorit novou prazdnou diskusi
        Item newDiz = new Item(0, Item.DISCUSSION);
        Document newItemDoc = DocumentHelper.createDocument();
        Element newItemRoot = newItemDoc.addElement("data");
        newDiz.setData(newItemDoc);
        Comment topComment = new ItemComment(newDiz);

        Record newRecord = new Record(0, Record.DISCUSSION);
        Document newRecordDoc = DocumentHelper.createDocument();
        newRecordDoc.addElement("data");
        newRecord.setData(newRecordDoc);
        DiscussionRecord newDizRecord = new DiscussionRecord();
        newRecord.setCustom(newDizRecord);

        // otazka nove diskuse
        Map newParams = new HashMap();
        newParams.put(PARAM_TITLE, movedComment.getTitle());
        String url = currentDizRelation.getUrl();
        if (url == null)
            url = urlUtils.getPrefix()+"/show/"+currentDizRelation.getId();
        newParams.put(PARAM_TEXT, "<p class=\"threadMoved\">Diskuse vznikla z vlákna <a href=\""+url+"\">této</a> diskuse.</p>");
        setTitle(newParams, newItemRoot, env);
        setTextNoHTMLCheck(newParams, newItemRoot, env);
        setCommentAuthor(newParams, user, topComment, newItemRoot, env);

        // presunout extraktovany komentar a nastavit jej jako prvni odpoved
        movedComment.setParent(null);
        movedComment.setCreated(new Date());
        newDizRecord.addThread(movedComment);
        newDizRecord.calculateCommentStatistics();
        originalComment.removeAllChildren(currentDizRecord);
        currentDizRecord.calculateCommentStatistics();

        // nastavit hlavicku nove diskuse
        setCommentsCount(newItemRoot, newDizRecord);
        Comment lastComment = newDizRecord.getLastComment();

        // ulozit hlavicku nove diskuse
        persistence.create(newDiz);
        SQLTool.getInstance().setUpdatedTimestamp(newDiz, lastComment.getCreated());

        // presun vsechny potomky (cele vlakno)
        LinkedList stack = new LinkedList();
        stack.add(movedComment);
        while (stack.size() > 0) {
            RowComment comment = (RowComment) stack.removeFirst();
            comment.setRowId(0);
            comment.setRecord(0);
            stack.addAll(comment.getChildren());
        }

        // opravit hlavicku puvodni diskuse
        setCommentsCount(currentItemRoot, currentDizRecord);
        lastComment = currentDizRecord.getLastComment();
        persistence.update(currentDiz);
        SQLTool.getInstance().setUpdatedTimestamp(currentDiz, lastComment.getCreated());

        // todo check whether new relation has correct parent. where to put article discussions?
        newDizRelation = new Relation(currentDizRelation.getParent(), newDiz, currentDizRelation.getUpper());
        persistence.create(newDizRelation);
        newDizRelation.getParent().addChildRelation(newDizRelation);

        persistence.create(newRecord);
        Relation newRecordRelation = new Relation(newDiz, newRecord, newDizRelation.getId());
        persistence.create(newRecordRelation);
        newRecordRelation.getParent().addChildRelation(newRecordRelation);

        // v puvodni diskusi ponechat vysvetlujici text
        url = urlUtils.getPrefix()+"/show/"+newDizRelation.getId();
        newParams.put(PARAM_TEXT, "<p class=\"threadMoved\">Vlákno bylo přesunuto do <a href=\""+url+"\">samostatné</a> diskuse.</p>");
        setTextNoHTMLCheck(newParams, originalComment.getData().getRootElement(), env);
        originalComment.set_dirty(true);
        persistence.update(currentRecord);

        AdminLogger.logEvent(user, "presunul vlakno "+threadId+" diskuse rid="+currentDizRelation.getId()+" do nove diskuse rid="+newDizRelation);

        urlUtils.redirect(response, "/show/"+newDizRelation.getId());
        return null;
    }


    /* ******** setters ********* */

    /**
     * Finds thread given by PARAM_THREAD key or makes facade around given Item.
     * @return Comment to be displayed, without any child comments
     * @throws PersistenceException if PARAM_THREAD points to nonexisting record
     */
    public static Comment getDiscussedComment(Map params, Item discussion, Persistence persistence) {
        int id = Misc.parseInt((String) params.get(PARAM_THREAD), 0);
        if ( id != 0 ) {
            Relation relation = (Relation) discussion.getChildren().get(0);
            Record record = (Record) persistence.findById(relation.getChild());
            DiscussionRecord dizRecord = (DiscussionRecord) record.getCustom();
            Comment comment = dizRecord.getComment(id);
            if (comment != null)
                return getUnthreadedComment(comment);
        }
        return new ItemComment(discussion);
    }

    /**
     * Creates copy of comment, that has no children.
     * @param comment
     * @return clone without children
     */
    public static Comment getUnthreadedComment(Comment comment) {
        Comment clone = (Comment) comment.clone();
        clone.removeAllChildren(null);
        return clone;
    }

    /**
     * Finds title in specified relation.
     * @param relation relation. It may be not initialized.
     * @return title of this relation
     */
    private String getTitleFromParent(Relation relation) {
        Tools.sync(relation);
        String title = Tools.childName(relation);
        return title;
    }

    /**
     * Updates title from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root root element of discussion to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    static boolean setTitle(Map params, Element root, Map env) {
        String tmp = tmp = Misc.getString(params, PARAM_TITLE);
        if ( tmp != null && tmp.length() > 0 ) {
            if ( tmp.indexOf("<") != -1 ) {
                params.put(PARAM_TITLE, "");
                ServletUtils.addError(PARAM_TITLE, "Použití HTML značek je zakázáno!", env, null);
                return false;
            }
            if (tmp.indexOf('\n') != -1)
                tmp = tmp.replace('\n', ' ');
            tmp = Misc.filterDangerousCharacters(tmp);
            DocumentHelper.makeElement(root,"title").setText(tmp);
        } else {
            ServletUtils.addError(PARAM_TITLE, "Zadejte titulek!", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates text of comment from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root root element of discussion to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    static boolean setText(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_TEXT);
        if ( tmp!=null && tmp.length()>0 ) {
            try {
                SafeHTMLGuard.check(tmp);
            } catch (ParserException e) {
                log.error("ParseException on '"+tmp+"'", e);
                ServletUtils.addError(PARAM_TEXT, e.getMessage(), env, null);
                return false;
            } catch (Exception e) {
                ServletUtils.addError(PARAM_TEXT, e.getMessage(), env, null);
                return false;
            }
            Element element = DocumentHelper.makeElement(root,"text");
            tmp = Misc.filterDangerousCharacters(tmp);
            element.setText(tmp);
            Format format = FormatDetector.detect(tmp);
            element.addAttribute("format", Integer.toString(format.getId()));
        } else {
            ServletUtils.addError(PARAM_TEXT, "Zadejte text vašeho dotazu.", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates text of comment from parameters. HTML is not checked for validity.
     * Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root root element of discussion to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    static boolean setTextNoHTMLCheck(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_TEXT);
        if ( tmp!=null && tmp.length()>0 ) {
            Element element = DocumentHelper.makeElement(root,"text");
            element.setText(tmp);
            Format format = FormatDetector.detect(tmp);
            element.addAttribute("format", Integer.toString(format.getId()));
        } else {
            ServletUtils.addError(PARAM_TEXT, "Zadejte text vašeho dotazu.", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates author of comment from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root root element of discussion to be updated
     * @param comment comment to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    static boolean setCommentAuthor(Map params, User user, Comment comment, Element root, Map env) {
        comment.setAuthor(null);
        Element previousAuthor = root.element("author");
        if ( previousAuthor!=null )
            previousAuthor.detach();

        if ( user!=null ) {
            comment.setAuthor(new Integer(user.getId()));
        } else {
            String tmp = Misc.getString(params, PARAM_ACTION);
            if ( tmp != null && tmp.length() > 0 ) {
                Integer authorId = new Integer(tmp);
                comment.setAuthor(authorId);
                return true;
            }

            tmp = Misc.getString(params, PARAM_ACTION);
            if ( tmp != null && tmp.length() > 0 ) {
                if (tmp.indexOf("<") != -1) {
                    params.put(PARAM_AUTHOR,"");
                    ServletUtils.addError(PARAM_AUTHOR, "Použití HTML značek je zakázáno!", env, null);
                    return false;
                }
                tmp = Misc.filterDangerousCharacters(tmp);
                DocumentHelper.makeElement(root, "author").setText(tmp);
            } else {
                ServletUtils.addError(PARAM_AUTHOR, "Zadejte prosím své jméno.", env, null);
                return false;
            }
        }
        return true;
    }

//    /**
//     * Anonymous post allowance test. The method setForumQuestionFlag() must be called prior this method.
//     * @return false, if user is not logged in and anonymous posts are prohibited
//     */
//    static boolean testAnonymCanPostComments(User user, Relation relation, Map env) {
//        if (user != null)
//            return true;
//
//        if (isQuestionInForum(relation))
//            return true;
//
//        ServletUtils.addError(ServletUtils.PARAM_LOG_USER, "Zadejte prosím své přihlašovací údaje.", env, null);
//        return false;
//    }

    /**
     * Sets client's IP address. Changes are not synchronized with persistence.
     * @param root   root element of comment to be updated
     * @param request HTTP request
     * @return true
     */
    static boolean setUserIPAddress(Element root, HttpServletRequest request) {
        if (request == null)
            return true;
        String ip = ServletUtils.getClientIPAddress(request);
        DocumentHelper.makeElement(root, "author_ip").setText(ip);
        return true;
    }

    /**
     * Updates parent of this comment from parameters. Changes are not synchronized with persistence.
     * todo when thread is extracted, threadId may point to nonexistent thread. check it.
     * @param params map holding request's parameters
     * @param comment comment to be updated
     * @return false, if there is a major error.
     */
    static boolean setParent(Map params, Comment comment) {
        String tmp = (String) params.get(PARAM_THREAD);
        int parent = Misc.parseInt(tmp, 0);
        if (parent != 0)
            comment.setParent(new Integer(parent));
        return true;
    }

    /**
     * Updates parent of this comment from parameters. Changes are not synchronized with persistence.
     * @param comment comment to be updated
     * @return false, if there is a major error.
     */
    static boolean setCreated(Comment comment) {
        comment.setCreated(new Date());
        return true;
    }

    /**
     * Updates id of this comment. Id must be bigger than ids of all existing comments.
     * Changes are not synchronized with persistence.
     * @param dizRecord discussion
     * @param comment element of comment to be updated
     * @return false, if there is a major error.
     */
    static boolean setId(DiscussionRecord dizRecord, Comment comment) {
        int id = dizRecord.getMaxCommentId();
        comment.setId(++id);
        dizRecord.setMaxCommentId(id);
        return true;
    }

    /**
     * Updates number of comments in discussion. It is assumed that
     * dizRecord.calculateCommentStatistics() was already called.
     * Changes are not synchronized with persistence.
     * @param itemRoot root element of item to be updated.
     * @param dizRecord discussion record
     * @return false, if there is a major error.
     */
    static boolean setCommentsCount(Element itemRoot, DiscussionRecord dizRecord) {
        String max = Integer.toString(dizRecord.getMaxCommentId());
        String total = Integer.toString(dizRecord.getTotalComments());
        DocumentHelper.makeElement(itemRoot, "last_id").setText(max);
        DocumentHelper.makeElement(itemRoot, "comments").setText(total);
        return true;
    }

    /**
     * Updates solved from parameters. User may vote only once for given choice,
     * but he may changed his decision by voting to opposite (this will undo his
     * previous choice).
     * @param params map holding request's parameters
     * @param root root element of discussion to be updated
     * @return false, if there is a major error.
     */
    static boolean setSolved(Map params, Element root, SQLTool sqlTool, int rid, User user) {
        String tmp = (String) params.get(PARAM_SOLVED);
        boolean solved = Boolean.valueOf(tmp).booleanValue();
        String type = (solved)? "solved" : "notsolved";

        Date voted = sqlTool.getUserAction(user.getId(), rid, type);
        if (voted!=null)
            return false;

        sqlTool.insertUserAction(user.getId(), rid, type);

        type = (solved) ? "notsolved" : "solved";
        voted = sqlTool.getUserAction(user.getId(), rid, type);
        if (voted!=null)
            sqlTool.removeUserAction(user.getId(), rid, type);

        Element element = DocumentHelper.makeElement(root, "solved");
        Attribute attribSolved = element.attribute("yes");
        Attribute attribNotSolved = element.attribute("no");
        if (solved) {
            if (attribSolved==null)
                element.addAttribute("yes","1");
            else {
                int count = Misc.parseInt(attribSolved.getText(), 0);
                attribSolved.setText(Integer.toString(count + 1));
            }
            if (voted!=null && attribNotSolved!=null) {
                int count = Misc.parseInt(attribNotSolved.getText(), 1);
                attribNotSolved.setText(Integer.toString(count - 1));
            }
        } else {
            if (attribNotSolved == null)
                element.addAttribute("no", "1");
            else {
                int count = Misc.parseInt(attribNotSolved.getText(), 0);
                attribNotSolved.setText(Integer.toString(count + 1));
            }
            if (voted != null && attribSolved != null) {
                int count = Misc.parseInt(attribSolved.getText(), 1);
                attribSolved.setText(Integer.toString(count - 1));
            }
        }
        return true;
    }

//    static boolean isQuestionInForum(Relation relation) {
//        return true;
//        Persistance persistence = PersistanceFactory.getPersistance();
//        GenericObject parent = persistence.findById(relation.getParent());
//        return (parent instanceof Category && ((Category)parent).getType()==Category.FORUM);
//    }

    /**
     * This method detects if there is cookie COOKIE_USER_VERIFIED. In such case
     * the boolean VAR_USER_VERIFIED is set in environment and cookie value
     * is returned.
     */
    public static String detectSpambotCookie(HttpServletRequest request, Map env, User user) {
        if (request == null)
            return null;
        if (user != null)
            return null;
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0)
            return null;
        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if (! COOKIE_USER_VERIFIED.equals(cookie.getName()))
                continue;
            env.put(VAR_USER_VERIFIED, Boolean.TRUE);
            String name = cookie.getValue();
            if (name != null && name.length() > 0) {
                try {
                    name = URLDecoder.decode(name, "UTF-8");
                    return name;
                } catch (UnsupportedEncodingException e) {
                    log.warn(e.getMessage(), e);
                }
            }
        }
        return null;
    }

    /**
     * Performs anti-spambot detection. Logged in user or already verified user is automatically allowed
     * to submit comment. Other users must enter current year. In such case new cookie is created, which
     * will hold their name and its existence will show that anti-spambot detection was already successfully
     * performed for this user.
     * @return false if anti-spambot rules were not satisfied
     */
    public static boolean checkSpambot(HttpServletRequest request, HttpServletResponse response, Map params, Map env, User user) {
        if (request == null || response == null)
            return true;
        if (user != null)
            return true;

        String storedName = detectSpambotCookie(request, env, user);
        boolean alreadyVerified = (env.containsKey(VAR_USER_VERIFIED));

        if (! alreadyVerified) {
            int year = Calendar.getInstance().get(Calendar.YEAR);
            String s = (String) params.get(PARAM_ANTISPAM);
            if (! String.valueOf(year).equals(s)) {
                ServletUtils.addError(PARAM_ANTISPAM, "Zadejte prosím letošní rok.", env, null);
                return false;
            }
            env.put(VAR_USER_VERIFIED, Boolean.TRUE);
        }

        String name = (String) params.get(PARAM_AUTHOR);
        if (name == null)
            name = "";
        else
            try {
                name = URLEncoder.encode(name, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.warn(e.getMessage(), e);
            }

        // if user has been verified earlier and either he didn't enter any name or
        // did not modified already stored name, do nothing
        if (alreadyVerified && (name.length() == 0 || name.equals(storedName)))
            return true;

        Cookie cookie = new Cookie(COOKIE_USER_VERIFIED, name);
        cookie.setPath("/");
        cookie.setMaxAge(5 * 365 * 24 * 3600);
        response.addCookie(cookie);

        return true;
    }
}
