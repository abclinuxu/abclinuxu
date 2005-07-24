/*
 * User: literakl
 * Date: Feb 25, 2002
 * Time: 7:45:21 AM
 */
package cz.abclinuxu.servlets.html.edit;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.data.view.Comment;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;
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
import cz.abclinuxu.exceptions.PersistanceException;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.scheduler.VariableFetcher;

import org.dom4j.*;
import org.htmlparser.util.ParserException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

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

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_DISCUSSION = "DISCUSSION";
    public static final String VAR_THREAD = "THREAD";
    public static final String VAR_PREVIEW = "PREVIEW";
    public static final String VAR_SEARCH_PERFORMED = "QUESTION_OK";
    public static final String VAR_SEARCH_INVALID = "QUESTION_KO";

    public static final String ACTION_ADD_DISCUSSION = "addDiz";
    public static final String ACTION_ADD_QUESTION = "addQuez";
    public static final String ACTION_ADD_QUESTION_STEP2 = "addQuez2";
    public static final String ACTION_ADD_COMMENT = "add";
    public static final String ACTION_ADD_COMMENT_STEP2 = "add2";
    public static final String ACTION_CENSORE_COMMENT = "censore";
    public static final String ACTION_CENSORE_COMMENT_STEP2 = "censore2";
    public static final String ACTION_EDIT_COMMENT = "edit";
    public static final String ACTION_EDIT_COMMENT_STEP2 = "edit2";
    public static final String ACTION_ALTER_MONITOR = "monitor";
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


    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        String action = (String) params.get(PARAM_ACTION);

        if ( relation!=null ) {
            relation = (Relation) persistance.findById(relation);
            persistance.synchronize(relation.getChild());
            env.put(VAR_RELATION,relation);
        } else if ( ! ACTION_ADD_QUESTION.equals(action) )
            throw new MissingArgumentException("Chybí parametr relationId!");

        if ( ACTION_ADD_DISCUSSION.equals(action) )
            return actionAddDiscussion(request,env);

        if ( ACTION_ADD_COMMENT.equals(action) )
            return actionAddComment(request,env);

        if ( ACTION_ADD_COMMENT_STEP2.equals(action) )
            return  actionAddComment2(request,response,env);

        if ( ACTION_ADD_QUESTION.equals(action) )
            return FMTemplateSelector.select("EditDiscussion","ask",env,request);

        if ( ACTION_ADD_QUESTION_STEP2.equals(action) )
            return actionAddQuestion2(request, response, env);

        // check permissions
        User user = (User) env.get(Constants.VAR_USER);
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if ( ACTION_ALTER_MONITOR.equals(action) )
            return actionAlterMonitor(request, response, env);
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
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(VAR_RELATION);
        Relation relChild = InstanceUtils.findFirstChildItemOfType(relation.getChild(),Item.DISCUSSION);
        Item discussion = null;

        if ( relChild==null ) {
            relChild = createEmptyDiscussion(relation, user, persistance);
            discussion = (Item) relChild.getChild();
        } else {
            discussion = (Item) relChild.getChild();
        }

        env.put(VAR_RELATION,relChild);
        env.put(VAR_DISCUSSION,discussion);
        return FMTemplateSelector.select("EditDiscussion","reply",env,request);
    }

    /**
     * Creates and persists empty discussion.
     * @param relation parent relation
     * @param user user that created this discussion (may be empty).
     * @param persistance
     * @return relation between created discussion and its parent
     */
    public static Relation createEmptyDiscussion(Relation relation, User user, Persistance persistance) {
        Item discussion = new Item(0, Item.DISCUSSION);
        Document document = DocumentHelper.createDocument();
        document.addElement("data").addElement("comments").setText("0");
        discussion.setData(document);
        if ( user!=null )
            discussion.setOwner(user.getId());

        persistance.create(discussion);
        Relation relChild = new Relation(relation.getChild(), discussion, relation.getId());
        persistance.create(relChild);
        relChild.getParent().addChildRelation(relChild);
        return relChild;
    }

    /**
     * last step - either shows preview of question or saves new discussion
     */
    protected String actionAddQuestion2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(VAR_RELATION);
        Item discussion = new Item(0,Item.DISCUSSION);

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        root.addElement("comments").setText("0");
        discussion.setData(document);

        boolean canContinue = true;
        canContinue &= setTitle(params, root, env);
        canContinue &= setText(params, root, env);
        canContinue &= setItemAuthor(params, user, root, discussion, env);

        if ( !canContinue || params.get(PARAM_PREVIEW)!=null ) {
            Comment comment = new Comment(root,new Date(),new Integer(0),null,user);
            env.put(VAR_PREVIEW,comment);
            return FMTemplateSelector.select("EditDiscussion","ask_confirm",env,request);
        }

        persistance.create(discussion);
        Relation rel2 = new Relation(relation.getChild(),discussion,relation.getId());
        persistance.create(rel2);
        rel2.getParent().addChildRelation(rel2);

        // run email forum and refresh RSS
        ForumPool.submitComment(rel2, discussion.getId(), 0, 0);
        FeedGenerator.updateForum();
        VariableFetcher.getInstance().refreshQuestions();

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/show/"+rel2.getId());
        return null;
    }

    /**
     * Displays add comment dialog
     */
    protected String actionAddComment(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params, request);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");
        persistance.synchronize(discussion);
        env.put(VAR_DISCUSSION, discussion);

        String xpath = "/data/frozen";
        Element element = (Element) discussion.getData().selectSingleNode(xpath);
        if ( element!=null )
            return ServletUtils.showErrorPage("Diskuse byla zmrazena - není mo¾né pøidat dal¹í komentáø!", env, request);

        // display discussed comment, only if it has title
        Comment thread = getDiscussedComment(params, discussion, persistance);
        if ( Tools.xpath(thread.getData(),"title")!=null )
            env.put(VAR_THREAD,thread);

        return FMTemplateSelector.select("EditDiscussion","reply",env,request);
    }

    /**
     * Adds new comment to selected discussion.
     */
    protected String actionAddComment2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        User user = (User) env.get(Constants.VAR_USER);

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params, request);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");
        persistance.synchronize(discussion);

        String xpath = "/data/frozen";
        Element element = (Element) discussion.getData().selectSingleNode(xpath);
        if ( element!=null )
            return ServletUtils.showErrorPage("Diskuse byla zmrazena - není mo¾né pøidat dal¹í komentáø!", env, request);

        Record record = null; Element root = null, comment = null;
        List children = discussion.getChildren();
        if ( children.size()>0 ) {
            record = (Record) ((Relation)children.get(0)).getChild();
            persistance.synchronize(record);
            root = record.getData().getRootElement();
        } else {
            record = new Record(0,Record.DISCUSSION);
            Document document = DocumentHelper.createDocument();
            record.setData(document);
            root = document.addElement("data");
        }
        comment = DocumentHelper.createElement("comment");

        // We can use root to synchronize threads, because Document is not cloned,
        // so it is shared. Each discussion has only single Document in memory.
        // The reason for this synchronization is, that if this code would run concurrently
        // on same discussion, data would be corrupted.
        synchronized(root) {

            boolean canContinue = true;
            canContinue &= setId(root, comment);
            canContinue &= setCreated(comment);
            canContinue &= setParent(params, comment);
            canContinue &= setCommentAuthor(params, user, comment, env);
            canContinue &= setTitle(params, comment, env);
            canContinue &= setText(params, comment, env);

            if ( !canContinue || params.get(PARAM_PREVIEW)!=null ) {
                env.put(VAR_DISCUSSION, discussion);
                if ( canContinue ) {
                    Comment previewComment = new Comment(comment);
                    env.put(VAR_PREVIEW, previewComment);
                }
                // display discussed comment, only if it has title
                Comment thread = getDiscussedComment(params, discussion, persistance);
                if ( Tools.xpath(thread.getData(), "title")!=null )
                    env.put(VAR_THREAD, thread);

                return FMTemplateSelector.select("EditDiscussion", "reply", env, request);
            }

            // now it is safe to modify XML Document, because data were validated
            root.add(comment);

            if ( record.getId()==0 ) {
                persistance.create(record);
                Relation rel = new Relation(discussion, record, 0); // todo set parent relation
                persistance.create(rel);
                rel.getParent().addChildRelation(rel);
            } else {
                persistance.update(record);
            }
        }
        persistance.synchronize(discussion);
        Element itemRoot = discussion.getData().getRootElement();
        synchronized (itemRoot) {
            setCommentsCount(itemRoot, root);
            persistance.update(discussion);
        }

        // run monitor
        String url = relation.getUrl();
        if (url==null)
            url = "http://www.abclinuxu.cz"+urlUtils.getPrefix()+"/show/"+relation.getId();
        url += "#" + comment.attributeValue("id");

        MonitorAction action = null;
        if (user!=null)
            action = new MonitorAction(user, UserAction.ADD, ObjectType.DISCUSSION, discussion, url);
        else {
            String author = (String) params.get(PARAM_AUTHOR);
            action = new MonitorAction(author, UserAction.ADD, ObjectType.DISCUSSION, discussion, url);
        }
        String title = comment.elementText("title");
        action.setProperty(DiscussionDecorator.PROPERTY_NAME, title);
        String content = comment.elementText("text");
        action.setProperty(DiscussionDecorator.PROPERTY_CONTENT, content);
        MonitorPool.scheduleMonitorAction(action);

        int commentId = Misc.parseInt(comment.attributeValue("id"), 0);
        // run email forum and update RSS
        if (relation.getParent() instanceof Category) {
            Category parent = (Category) persistance.findById(relation.getParent());
            if (parent.getType() == Category.FORUM) {
                ForumPool.submitComment(relation, discussion.getId(), record.getId(), commentId);
                FeedGenerator.updateForum();
                VariableFetcher.getInstance().refreshQuestions();
            }
        }

        url = (String) params.get(PARAM_URL);
        if (url==null)
            url = relation.getUrl();
        if (url==null)
            url = urlUtils.getPrefix()+"/show/"+relation.getId();
        url += "#"+commentId;
        urlUtils.redirect(response, url, false);
        return null;
    }

    protected String actionSolved(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        User user = (User) env.get(Constants.VAR_USER);

        SQLTool sqlTool = SQLTool.getInstance();
        Item diz = (Item) persistance.findById(relation.getChild());
        boolean canContinue = setSolved(params, diz.getData().getRootElement(), sqlTool, relation.getId(), user);
        if (canContinue) {
            Date updated = diz.getUpdated();
            persistance.update(diz);
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
    protected String actionCensore(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params, request);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");
        persistance.synchronize(discussion);

        Relation relation;
        String thread = (String) params.get(PARAM_THREAD);
        List children = discussion.getChildren();
        if ("0".equals(thread) || children.size()==0) {
            ServletUtils.addError(Constants.ERROR_GENERIC,"Nejde cenzurovat otázku!",env,request.getSession());
            relation = (Relation) env.get(VAR_RELATION);
            urlUtils.redirect(response, "/show/"+relation.getId());
        }

        relation = (Relation) children.get(0);
        Record record = (Record) relation.getChild();
        persistance.synchronize(record);
        String xpath = "//comment[@id='"+thread+"']";

        relation = (Relation) env.get(VAR_RELATION);

        Element element = (Element) record.getData().selectSingleNode(xpath);
        if (element!=null) {
            Node node = element.selectSingleNode("censored");
            if (node!=null) {
                node.detach();
                AdminLogger.logEvent(user,"odstranena cenzura na vlakno "+thread+" diskuse "+discussion.getId()+", relace "+relation.getId());
            } else {
                String action = (String) params.get(PARAM_ACTION);
                if ( ACTION_CENSORE_COMMENT_STEP2.equals(action) ) {
                    Element censored = element.addElement("censored");
                    censored.addAttribute("admin",new Integer(user.getId()).toString());
                    censored.setText((String) params.get(PARAM_TEXT));

                    // run monitor
                    String url = "http://www.abclinuxu.cz"+urlUtils.getPrefix()+"/show/"+relation.getId();
                    MonitorAction monitor = new MonitorAction(user, UserAction.CENSORE, ObjectType.DISCUSSION, discussion, url);
                    String title = element.selectSingleNode("title").getText();
                    monitor.setProperty(DiscussionDecorator.PROPERTY_NAME, title);
                    MonitorPool.scheduleMonitorAction(monitor);

                    AdminLogger.logEvent(user, "uvalil cenzuru na vlakno "+thread+" diskuse "+discussion.getId()+", relace "+relation.getId());
                } else {
                    env.put(VAR_THREAD, new Comment(element));
                    return FMTemplateSelector.select("EditDiscussion", "censore", env, request);
                }
            }
        }
        persistance.update(record);

        String url = relation.getUrl();
        if (url == null)
            url = urlUtils.getPrefix() + "/show/" + relation.getId();
        urlUtils.redirect(response, url, false);
        return null;
    }

    /**
     * Displays add comment dialog
     */
    protected String actionEditComment(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params, request);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");
        persistance.synchronize(discussion);

        Comment thread = null;
        int threadId = Misc.parseInt((String) params.get(PARAM_THREAD),0);
        if (threadId==0)
            thread = new Comment(discussion);
        else {
            Relation relation = (Relation) discussion.getChildren().get(0);
            Record record = (Record) relation.getChild();
            persistance.synchronize(record);
            String xpath = "//comment[@id='"+threadId+"']";
            Element element = (Element) record.getData().selectSingleNode(xpath);
            thread = new Comment(element);
        }

        params.put(PARAM_TITLE,thread.getData().selectSingleNode("title").getText());
        params.put(PARAM_TEXT,thread.getData().selectSingleNode("text").getText());

        User author = thread.getAuthor();
        if (author!=null)
            params.put(PARAM_AUTHOR_ID, new Integer(author.getId()));
        else {
            String authorName = thread.getData().elementText("author");
            params.put(PARAM_AUTHOR, authorName);
        }

        return FMTemplateSelector.select("EditDiscussion", "edit", env, request);
    }

    /**
     * Adds new comment to selected discussion.
     */
    protected String actionEditComment2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params, request);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");
        persistance.synchronize(discussion);

        Element comment = null; Record record = null;
        int threadId = Misc.parseInt((String) params.get(PARAM_THREAD), 0);
        if ( threadId==0 )
            comment = discussion.getData().getRootElement();
        else {
            Relation relation = (Relation) discussion.getChildren().get(0);
            record = (Record) relation.getChild();
            persistance.synchronize(record);
            String xpath = "//comment[@id='"+threadId+"']";
            comment = (Element) record.getData().selectSingleNode(xpath);
        }

        boolean canContinue = true;
        canContinue &= setTitle(params, comment, env);
        canContinue &= setText(params, comment, env);
        if ( threadId==0 )
            canContinue &= setItemAuthor(params, null, comment, discussion, env);
        else
            canContinue &= setCommentAuthor(params, null, comment, env);

        if ( !canContinue || params.get(PARAM_PREVIEW)!=null ) {
            if ( canContinue ) {
                Comment previewComment = new Comment(comment);
                env.put(VAR_PREVIEW, previewComment);
            }
            return FMTemplateSelector.select("EditDiscussion", "edit", env, request);
        }

        if ( threadId==0 ) {
            Date updated = discussion.getUpdated();
            persistance.update(discussion);
            SQLTool.getInstance().setUpdatedTimestamp(discussion,updated);
        } else
            persistance.update(record);

        User user = (User) env.get(Constants.VAR_USER);
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
        Persistance persistance = PersistanceFactory.getPersistance();

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params, request);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");

        int threadId = Misc.parseInt((String) params.get(PARAM_THREAD), 0);
        if ( threadId==0 )
            throw new MissingArgumentException("Chybí parametr threadId!");

        persistance.synchronize(discussion);

        Relation relation = (Relation) discussion.getChildren().get(0);
        Record record = (Record) relation.getChild();
        persistance.synchronize(record);
        String xpath = "//comment[@id='"+threadId+"']";
        Element element = (Element) record.getData().selectSingleNode(xpath);
        env.put(VAR_THREAD, new Comment(element));

        return FMTemplateSelector.select("EditDiscussion", "remove", env, request);
    }

    /**
     * Removes selected comment.
     */
    protected String actionRemoveComment2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);
        Relation mainRelation = (Relation) env.get(VAR_RELATION);

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params, request);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");

        int threadId = Misc.parseInt((String) params.get(PARAM_THREAD), 0);
        if ( threadId==0 )
            throw new MissingArgumentException("Chybí parametr threadId!");

        persistance.synchronize(discussion);

        Relation relation = (Relation) discussion.getChildren().get(0);
        Record record = (Record) relation.getChild();
        persistance.synchronize(record);
        Document recordData = record.getData();

        List stack = new ArrayList();
        stack.add(new Integer(threadId));

        String xpath;
        Element element;
        List children;
        String title = null, content = null;

        while(stack.size()>0) {
            threadId = ((Integer)stack.remove(0)).intValue();
            xpath = "//comment[@id='"+threadId+"']";
            element = (Element) recordData.selectSingleNode(xpath);
            element.detach();
            title = element.elementText("title");
            content = element.elementText("text");
            AdminLogger.logEvent(user, "smazal vlakno "+threadId+", relace "+mainRelation.getId());

            xpath = "//comment[parent/text()='"+threadId+"']";
            children = recordData.selectNodes(xpath);
            if (children==null || children.size()==0)
                continue;
            for ( Iterator iter = children.iterator(); iter.hasNext(); ) {
                element = (Element) iter.next();
                threadId = Misc.parseInt(element.attributeValue("id"),0);
                stack.add(new Integer(threadId));
            }
        }
        persistance.update(record);

        List commentList = recordData.getRootElement().selectNodes("comment");
        int comments = commentList.size();
        DocumentHelper.makeElement(discussion.getData().getRootElement(), "comments").setText(""+comments);
        Date lastUpdate = discussion.getCreated();
        if (comments>0) {
            element = (Element) commentList.get(comments-1);
            synchronized (Constants.isoFormat) {
                lastUpdate = Constants.isoFormat.parse(element.elementText("created"));
            }
        }
        persistance.update(discussion);
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
            url = urlUtils.getPrefix() + "/show/" + relation.getId();
        urlUtils.redirect(response, url, false);
        return null;
    }

    /**
     * Reverts current monitor state for the user on this driver.
     */
    protected String actionAlterMonitor(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item discussion = (Item) persistance.findById(relation.getChild());
        User user = (User) env.get(Constants.VAR_USER);

        alterDiscussionMonitor(discussion, user, persistance);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String url = (String) params.get(PARAM_URL);
        if (url == null)
            url = relation.getUrl();
        if (url==null)
            url = urlUtils.getPrefix() + "/show/"+relation.getId();
        urlUtils.redirect(response, url, false);
        return null;
    }

    /**
     * Reverts the current state of monitor on specified discussion.
     */
    public static void alterDiscussionMonitor(Item discussion, User user, Persistance persistance) {
        Date originalUpdated = discussion.getUpdated();
        MonitorTools.alterMonitor(discussion.getData().getRootElement(), user);
        persistance.update(discussion);
        SQLTool.getInstance().setUpdatedTimestamp(discussion, originalUpdated);
    }

    /**
     * Reverts current state of frozen attribute.
     */
    protected String actionAlterFreeze(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item discussion = (Item) persistance.findById(relation.getChild());

        String xpath = "/data/frozen";
        Document data = discussion.getData();
        Element element = (Element) data.selectSingleNode(xpath);
        if ( element!=null )
            element.detach();
        else
            DocumentHelper.makeElement(data,xpath);

        Date originalUpdated = discussion.getUpdated();
        persistance.update(discussion);
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
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params, request);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");
        Tools.sync(discussion);
        env.put(VAR_DISCUSSION, discussion);

        return FMTemplateSelector.select("EditDiscussion", "move", env, request);
    }

    protected String actionMoveThreadStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item discussion = (Item) persistance.findById(relation.getChild());

        Map params = (Map) env.get(Constants.VAR_PARAMS);
        int threadId = Misc.parseInt((String) params.get(PARAM_THREAD), 0);
        if ( threadId==0 )
            throw new MissingArgumentException("Chybí parametr threadId!");
        int parentId = Misc.parseInt((String) params.get(PARAM_PARENT_THREAD), -1);
        if ( parentId==-1 )
            throw new MissingArgumentException("Chybí parametr parentId!");
        if ( parentId==threadId ) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Pokou¹íte se vytvoøit smyèku ve stromì!", env, null);
            return actionMoveThread(request, env);
        }

        Record record = (Record) ((Relation) discussion.getChildren().get(0)).getChild();
        Tools.sync(record);
        Document data = record.getData();

        Element thread = (Element) data.selectSingleNode("//comment[@id='"+threadId+"']");
        if (parentId!=0) {
            Element threadParent = (Element) data.selectSingleNode("//comment[@id='"+parentId+"']");
            if ( threadParent==null ) {
                ServletUtils.addError(Constants.ERROR_GENERIC, "Takový pøedek neexistuje!", env, null);
                return actionMoveThread(request, env);
            }
        }

        Element parent = thread.element("parent");
        String originalParentId = parent.getText();
        parent.setText(Integer.toString(parentId));
        persistance.update(record);
        AdminLogger.logEvent(user, "presunul vlakno "+threadId+", puvodni predek="+originalParentId+", novy predek="+parentId+", relace "+relation.getId());

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/show/"+relation.getId());
        return null;
    }

    /**
     * Moves selected thread one level up. Top level threads are not changed.
     */
    protected String actionDecreaseThreadLevel(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item discussion = (Item) persistance.findById(relation.getChild());

        Map params = (Map) env.get(Constants.VAR_PARAMS);
        int threadId = Misc.parseInt((String) params.get(PARAM_THREAD), 0);
        if ( threadId==0 )
            throw new MissingArgumentException("Chybí parametr threadId!");

        Record record = (Record) ((Relation)discussion.getChildren().get(0)).getChild();
        Tools.sync(record);
        Document data = record.getData();

        Element thread = (Element) data.selectSingleNode("//comment[@id='"+threadId+"']");
        Element threadParent = thread.element("parent");
        int parentId = Misc.parseInt(threadParent.getText(), 0);
        if (parentId!=0) {
            Element newParent = (Element) data.selectSingleNode("//comment[@id='"+parentId+"']/parent");
            threadParent.setText(newParent.getText());
            persistance.update(record);
            AdminLogger.logEvent(user, "presunul vlakno "+threadId+" o uroven vys, relace "+relation.getId());
        }

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/show/"+relation.getId());
        return null;
    }

    protected String actionToNewDiscussion1(HttpServletRequest request, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item discussion = (Item) persistance.findById(relation.getChild());

        Map params = (Map) env.get(Constants.VAR_PARAMS);
        int threadId = Misc.parseInt((String) params.get(PARAM_THREAD), 0);
        if ( threadId==0 )
            throw new MissingArgumentException("Chybí parametr threadId!");

        Record record = (Record) ((Relation) discussion.getChildren().get(0)).getChild();
        Tools.sync(record);
        Document data = record.getData();
        Element thread = (Element) data.selectSingleNode("//comment[@id='"+threadId+"']");

        params.put(PARAM_TITLE, thread.elementText("title"));
        params.put(PARAM_TEXT, "<a href=\"/"+urlUtils.getPrefix()+"/show/"+relation.getId()+"\">pùvodní diskuse</a>");

        return FMTemplateSelector.select("EditDiscussion", "toQuestion", env, request);
    }

    /**
     * Extracts selected thread in discussion to new separate discussion.
     */
    protected String actionToNewDiscussion2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);

        Relation currentDizRelation = (Relation) env.get(VAR_RELATION), newDizRelation;
        Item currentDiz = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params, request);
        if ( currentDiz==null )
            throw new MissingArgumentException("Chybí parametr dizId!");
        Tools.sync(currentDiz);
        Element currentItemRoot = currentDiz.getData().getRootElement();

        List children = currentDiz.getChildren();
        Record currentRecord = (Record) ((Relation) children.get(0)).getChild();
        Tools.sync(currentRecord);
        Element currentRecordRoot = currentRecord.getData().getRootElement();
        String thread = (String) params.get(PARAM_THREAD);
        String xpath = "//comment[@id='"+thread+"']";

        // vytvorit novou prazdnou diskusi
        Item newDiz = new Item(0, Item.DISCUSSION);
        Document newItemDoc = DocumentHelper.createDocument();
        Element newItemRoot = newItemDoc.addElement("data");
        newItemRoot.addElement("comments").setText("0");
        newDiz.setData(newItemDoc);

        Record newRecord = new Record(0, Record.DISCUSSION);
        Document newRecordDoc = DocumentHelper.createDocument();
        newRecord.setData(newRecordDoc);
        Element newRecordRoot = newRecordDoc.addElement("data");

        synchronized(currentRecordRoot) {
            Element element = (Element) currentRecordRoot.selectSingleNode(xpath);

            // otazka nove diskuse
            Map newParams = new HashMap();
            newParams.put(PARAM_TITLE, element.elementText("title"));
            // specifikuj jmeno diskuse, pozor na diskuse ke clankum
            String url = urlUtils.getPrefix()+"/show/"+currentDizRelation.getId();
            newParams.put(PARAM_TEXT, "<p class=\"threadMoved\">Diskuse vznikla z vlákna <a href=\""+url+"\">této</a> diskuse.</p>");
            setTitle(newParams, newItemRoot, env);
            setTextNoHTMLCheck(newParams, newItemRoot, env);
            setItemAuthor(newParams, user, newItemRoot, newDiz, env);

            // extraktovany komentar jako prvni odpoved
            Element newElement = element.createCopy();
            setParent(newParams, newElement);
            newRecordRoot.add(newElement);

            // presun vsechny potomky (cele vlakno)
            boolean found = false;
            String id = null, parent;
            Set parents = new HashSet();
            parents.add(thread); // root prenaseneho vlakna
            List comments = currentRecordRoot.elements("comment");
            for ( Iterator iter = comments.iterator(); iter.hasNext(); ) {
                Element comment = (Element) iter.next();
                if ( !found ) {
                    if ( comment.equals(element) )
                        found = true;
                    continue;
                }

                parent = comment.elementText("parent");
                if ( !parents.contains(parent) )
                    continue; // nepatri do threadu

                id = comment.attributeValue("id");
                parents.add(id);
                comment.detach(); // smazat z puvodniho stromu
                newRecordRoot.add(comment); // vlozit do noveho stromu
            }

            // nastavit hlavicku nove diskuse
            List commentList = newRecordRoot.selectNodes("comment");
            int commentsCount = commentList.size();
            DocumentHelper.makeElement(newItemRoot, "comments").setText(""+commentsCount);
            Element lastElement = (Element) commentList.get(commentsCount-1);
            Date lastUpdate = null;
            synchronized (Constants.isoFormat) {
                lastUpdate = Constants.isoFormat.parse(lastElement.elementText("created"));
            }

            persistance.create(newDiz);
            SQLTool.getInstance().setUpdatedTimestamp(newDiz, lastUpdate);

            // opravit hlavicku puvodni diskuse
            commentList = currentRecordRoot.selectNodes("comment");
            commentsCount = commentList.size();
            DocumentHelper.makeElement(currentItemRoot, "comments").setText(""+commentsCount);
            lastElement = (Element) commentList.get(commentsCount-1);
            synchronized (Constants.isoFormat) {
                lastUpdate = Constants.isoFormat.parse(lastElement.elementText("created"));
            }

            persistance.update(currentDiz);
            SQLTool.getInstance().setUpdatedTimestamp(currentDiz, lastUpdate);

            // check whether new relation has correct parent. where to put article discussions?
            newDizRelation = new Relation(currentDizRelation.getParent(), newDiz, currentDizRelation.getUpper());
            persistance.create(newDizRelation);
            newDizRelation.getParent().addChildRelation(newDizRelation);

            persistance.create(newRecord);
            Relation newRecordRelation = new Relation(newDiz, newRecord, newDizRelation.getId());
            persistance.create(newRecordRelation);
            newRecordRelation.getParent().addChildRelation(newRecordRelation);

            // v puvodni diskusi ponechat vysvetlujici text
            url = urlUtils.getPrefix()+"/show/"+newDizRelation.getId();
            newParams.put(PARAM_TEXT, "<p class=\"threadMoved\">Vlákno bylo pøesunuto do <a href=\""+url+"\">samostatné</a> diskuse.</p>");
            setTextNoHTMLCheck(newParams, element, env);
            persistance.update(currentRecord);
        }

        AdminLogger.logEvent(user, "presunul vlakno "+thread+" diskuse rid="+currentDizRelation.getId()+" do nove diskuse rid="+newDizRelation);

        urlUtils.redirect(response, "/show/"+newDizRelation.getId());
        return null;
    }


    /* ******** setters ********* */

    /**
     * Finds thread given by PARAM_THREAD key or makes facade around given Item.
     * @return Comment to be displayed
     * @throws PersistanceException if PARAM_THREAD points to nonexisting record
     */
    public static Comment getDiscussedComment(Map params, Item discussion, Persistance persistance) {
        String thread = (String) params.get(PARAM_THREAD);
        if ( thread!=null && thread.length()>0 && ! "0".equals(thread) ) {
            Relation relation = (Relation) discussion.getChildren().get(0);
            Record record = (Record) relation.getChild();
            persistance.synchronize(record);
            String xpath = "//comment[@id='"+thread+"']";
            Element element = (Element) record.getData().selectSingleNode(xpath);
            if ( element!=null )
                return new Comment(element);
        }
        return new Comment(discussion);
    }

    /**
     * Updates title from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element of discussion to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    static boolean setTitle(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_TITLE);
        if ( tmp!=null && tmp.length()>0 ) {
            if ( tmp.indexOf("<")!=-1 ) {
                params.put(PARAM_TITLE, "");
                ServletUtils.addError(PARAM_TITLE, "Pou¾ití HTML znaèek je zakázáno!", env, null);
                return false;
            }
            if (tmp.indexOf('\n')!=-1) {
                tmp = tmp.replace('\n', ' ');
            }
            DocumentHelper.makeElement(root,"title").setText(tmp);
        } else {
            ServletUtils.addError(PARAM_TITLE, "Zadejte titulek va¹eho dotazu!", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates text of comment from parameters. Changes are not synchronized with persistance.
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
            element.setText(tmp);
            Format format = FormatDetector.detect(tmp);
            element.addAttribute("format", Integer.toString(format.getId()));
        } else {
            ServletUtils.addError(PARAM_TEXT, "Zadejte text va¹eho dotazu!", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates text of comment from parameters. HTML is not checked for validity.
     * Changes are not synchronized with persistance.
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
            ServletUtils.addError(PARAM_TEXT, "Zadejte text va¹eho dotazu!", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates author of comment from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element of discussion to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    static private boolean setItemAuthor(Map params, User user, Element root, Item diz, Map env) {
        diz.setOwner(0);
        Element previousAuthor = root.element("author");
        if ( previousAuthor!=null )
            previousAuthor.detach();

        if ( user!=null ) {
            diz.setOwner(user.getId());
        } else {
            String tmp = (String) params.get(PARAM_AUTHOR_ID);
            if (tmp!=null && tmp.length()>0) {
                diz.setOwner(Integer.parseInt(tmp));
                return true;
            }

            tmp = (String) params.get(PARAM_AUTHOR);
            if ( tmp!=null && tmp.length()>0 ) {
                DocumentHelper.makeElement(root, "author").setText(tmp);
            } else {
                ServletUtils.addError(PARAM_AUTHOR, "Zadejte prosím své jméno.", env, null);
                return false;
            }
        }
        return true;
    }

    /**
     * Updates author of comment from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element of discussion to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    static boolean setCommentAuthor(Map params, User user, Element root, Map env) {
        Element previousAuthor = root.element("author_id");
        if ( previousAuthor!=null )
            previousAuthor.detach();
        previousAuthor = root.element("author");
        if ( previousAuthor!=null )
            previousAuthor.detach();

        if ( user!=null ) {
            DocumentHelper.makeElement(root, "author_id").setText(Integer.toString(user.getId()));
        } else {
            String tmp = (String) params.get(PARAM_AUTHOR_ID);
            if ( tmp!=null && tmp.length()>0 ) {
                int authorId = Integer.parseInt(tmp);
                DocumentHelper.makeElement(root, "author_id").setText(Integer.toString(authorId));
                return true;
            }

            tmp = (String) params.get(PARAM_AUTHOR);
            if ( tmp!=null && tmp.length()>0 ) {
                if (tmp.indexOf("<")!=-1) {
                    params.put(PARAM_AUTHOR,"");
                    ServletUtils.addError(PARAM_AUTHOR, "Pou¾ití HTML znaèek je zakázáno!", env, null);
                    return false;
                }
                DocumentHelper.makeElement(root, "author").setText(tmp);
            } else {
                ServletUtils.addError(PARAM_AUTHOR, "Zadejte prosím své jméno.", env, null);
                return false;
            }
        }
        return true;
    }

    /**
     * Updates parent of this comment from parameters. Changes are not synchronized with persistance.
     * todo when thread is extracted, threadId may point to nonexistent thread. check it.
     * @param params map holding request's parameters
     * @param root root element of discussion to be updated
     * @return false, if there is a major error.
     */
    static boolean setParent(Map params, Element root) {
        String tmp = (String) params.get(PARAM_THREAD);
        if ( tmp==null || tmp.length()==0 )
            tmp = "0";
        DocumentHelper.makeElement(root, "parent").setText(tmp);
        return true;
    }

    /**
     * Updates parent of this comment from parameters. Changes are not synchronized with persistance.
     * @param root root element of discussion to be updated
     * @return false, if there is a major error.
     */
    static boolean setCreated(Element root) {
        String date;
        synchronized (Constants.isoFormat) {
            date = Constants.isoFormat.format(new Date());
        }
        DocumentHelper.makeElement(root, "created").setText(date);
        return true;
    }

    /**
     * Updates id of this comment. Id must be bigger than ids of all existing comments.
     * Because we store comments serially in XML, it is enough to increment id of last
     * comment.
     * Changes are not synchronized with persistance.
     * @param root root element of discussion
     * @param comment element of comment to be updated
     * @return false, if there is a major error.
     */
    static boolean setId(Element root, Element comment) {
        int last = 0;
        List comments = root.elements("comment");
        if ( comments!=null && comments.size()>0) {
            Element element = (Element) comments.get(comments.size()-1);
            String tmp = element.attributeValue("id");
            last = Integer.parseInt(tmp);
        }

        last++;
        comment.addAttribute("id",Integer.toString(last));
        return true;
    }

    /**
     * Updates number of comments in discussion. Changes are not synchronized with persistance.
     * @param itemRoot root element of item to be updated.
     * @param recordRoot root element of record.
     * @return false, if there is a major error.
     */
    static boolean setCommentsCount(Element itemRoot, Element recordRoot) {
        List comments = recordRoot.selectNodes("comment");
        DocumentHelper.makeElement(itemRoot,"comments").setText(""+comments.size());
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
}
