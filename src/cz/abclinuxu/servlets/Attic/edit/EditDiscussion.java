/*
 * User: literakl
 * Date: Feb 25, 2002
 * Time: 7:45:21 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.view.Search;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.data.view.Comment;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Tools;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.monitor.*;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.exceptions.PersistanceException;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.security.AdminLogger;

import org.dom4j.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * This class is responsible for adding new
 * new discussion.<p>
 */
public class EditDiscussion extends AbcFMServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditDiscussion.class);

    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_DISCUSSION = "dizId";
    public static final String PARAM_THREAD = "threadId";
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_TEXT = "text";
    public static final String PARAM_AUTHOR = "author";
    public static final String PARAM_PREVIEW = "preview";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_DISCUSSION = "DISCUSSION";
    public static final String VAR_THREAD = "THREAD";
    public static final String VAR_PREVIEW = "PREVIEW";
    public static final String VAR_SEARCH_PERFORMED = "QUESTION_OK";
    public static final String VAR_SEARCH_INVALID = "QUESTION_KO";

    public static final String ACTION_ADD_DISCUSSION = "addDiz";
    public static final String ACTION_ADD_QUESTION = "addQuez";
    public static final String ACTION_ADD_QUESTION_STEP2 = "addQuez2";
    public static final String ACTION_ADD_QUESTION_STEP3 = "addQuez3";
    public static final String ACTION_ADD_QUESTION_STEP4 = "addQuez4";
    public static final String ACTION_ADD_COMMENT = "add";
    public static final String ACTION_ADD_COMMENT_STEP2 = "add2";
    public static final String ACTION_CENSORE_COMMENT = "censore";
    public static final String ACTION_CENSORE_COMMENT_STEP2 = "censore2";
    public static final String ACTION_EDIT_COMMENT = "edit";
    public static final String ACTION_EDIT_COMMENT_STEP2 = "edit2";
    public static final String ACTION_ALTER_MONITOR = "monitor";
    public static final String ACTION_REMOVE_COMMENT = "rm";
    public static final String ACTION_REMOVE_COMMENT_STEP2 = "rm2";
    public static final String ACTION_FREEZE_DISCUSSION = "freeze";


    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT,PARAM_RELATION,Relation.class,params);
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
            return actionAddQuestion2(request,env);

        if ( ACTION_ADD_QUESTION_STEP3.equals(action) )
            return FMTemplateSelector.select("EditDiscussion", "ask_form", env, request);

        if ( ACTION_ADD_QUESTION_STEP4.equals(action) )
            return actionAddQuestion4(request,response,env);

        // check permissions
        User user = (User) env.get(Constants.VAR_USER);
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if ( ACTION_ALTER_MONITOR.equals(action) )
            return actionAlterMonitor(request, response, env);

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

        if ( ACTION_FREEZE_DISCUSSION.equals(action) )
            return actionAlterFreeze(request, response, env);

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
            discussion = new Item(0,Item.DISCUSSION);
            Document document = DocumentHelper.createDocument();
            document.addElement("data").addElement("comments").setText("0");
            discussion.setData(document);
            if ( user!=null )
                discussion.setOwner(user.getId());

            persistance.create(discussion);
            relChild = new Relation(relation.getChild(),discussion,relation.getId());
            persistance.create(relChild);
        } else {
            discussion = (Item) relChild.getChild();
        }

        env.put(VAR_RELATION,relChild);
        env.put(VAR_DISCUSSION,discussion);
        return FMTemplateSelector.select("EditDiscussion","reply",env,request);
    }

    /**
     * The second step is to display results of search. User can proceed to third
     * page, where he can fill the form with question.
     */
    protected String actionAddQuestion2(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String query = (String) params.get(Search.PARAM_QUERY);
        if (query!=null && query.trim().length()>0)
            env.put(VAR_SEARCH_PERFORMED,Boolean.TRUE);
        else
            env.put(VAR_SEARCH_INVALID, Boolean.TRUE);
        return Search.performSearch(request,env);
    }

    /**
     * last step - either shows preview of question or saves new discussion
     */
    protected String actionAddQuestion4(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
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
            Comment comment = new Comment(root,new Date(),null,null,user);
            env.put(VAR_PREVIEW,comment);
            return FMTemplateSelector.select("EditDiscussion","ask_confirm",env,request);
        }

        persistance.create(discussion);
        Relation rel2 = new Relation(relation.getChild(),discussion,relation.getId());
        persistance.create(rel2);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/ViewRelation?rid="+rel2.getId());
        return null;
    }

    /**
     * Displays add comment dialog
     */
    protected String actionAddComment(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION,Item.class,params);
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

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION,Item.class,params);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");
        persistance.synchronize(discussion);

        String xpath = "/data/frozen";
        Element element = (Element) discussion.getData().selectSingleNode(xpath);
        if ( element!=null )
            return ServletUtils.showErrorPage("Diskuse byla zmrazena - není mo¾né pøidat dal¹í komentáø!", env, request);

        Record record = null; Element root = null, comment = null;
        if ( discussion.getContent().size()>0 ) {
            record = (Record) ((Relation)discussion.getContent().get(0)).getChild();
            persistance.synchronize(record);
            root = (Element) record.getData().selectSingleNode("/data");
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
                Relation rel = new Relation(discussion, record, 0);
                persistance.create(rel);
            } else {
                persistance.update(record);
            }

            persistance.synchronize(discussion);
            setCommentsCount(discussion.getData().getRootElement(), root);
            persistance.update(discussion);
        }

        // run monitor
        String url = "http://www.abclinuxu.cz"+urlUtils.getPrefix()+"/ViewRelation?rid="+relation.getId();
        MonitorAction action = null;
        if (user!=null)
            action = new MonitorAction(user, UserAction.ADD, ObjectType.DISCUSSION, discussion, url);
        else {
            String author = (String) params.get(PARAM_AUTHOR);
            action = new MonitorAction(author, UserAction.ADD, ObjectType.DISCUSSION, discussion, url);
        }
        String title = comment.selectSingleNode("title").getText();
        action.setProperty(DiscussionDecorator.PROPERTY_NAME, title);
        MonitorPool.scheduleMonitorAction(action);

        urlUtils.redirect(response, "/ViewRelation?rid="+relation.getId());
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

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");
        persistance.synchronize(discussion);

        Relation relation;
        String thread = (String) params.get(PARAM_THREAD);
        if ("0".equals(thread) || discussion.getContent().size()==0) {
            ServletUtils.addError(Constants.ERROR_GENERIC,"Nejde cenzurovat otázku!",env,request.getSession());
            relation = (Relation) env.get(VAR_RELATION);
            urlUtils.redirect(response, "/ViewRelation?rid="+relation.getId());
        }

        relation = (Relation) discussion.getContent().get(0);
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
                    String url = "http://www.abclinuxu.cz"+urlUtils.getPrefix()+"/ViewRelation?rid="+relation.getId();
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

        urlUtils.redirect(response, "/ViewRelation?rid="+relation.getId());
        return null;
    }

    /**
     * Displays add comment dialog
     */
    protected String actionEditComment(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");
        persistance.synchronize(discussion);

        Comment thread = null;
        int threadId = Misc.parseInt((String) params.get(PARAM_THREAD),0);
        if (threadId==0)
            thread = new Comment(discussion);
        else {
            Relation relation = (Relation) discussion.getContent().get(0);
            Record record = (Record) relation.getChild();
            persistance.synchronize(record);
            String xpath = "//comment[@id='"+threadId+"']";
            Element element = (Element) record.getData().selectSingleNode(xpath);
            thread = new Comment(element);
        }

        params.put(PARAM_TITLE,thread.getData().selectSingleNode("title").getText());
        params.put(PARAM_TEXT,thread.getData().selectSingleNode("text").getText());

        return FMTemplateSelector.select("EditDiscussion", "edit", env, request);
    }

    /**
     * Adds new comment to selected discussion.
     */
    protected String actionEditComment2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");
        persistance.synchronize(discussion);

        Element comment = null; Record record = null;
        int threadId = Misc.parseInt((String) params.get(PARAM_THREAD), 0);
        if ( threadId==0 )
            comment = discussion.getData().getRootElement();
        else {
            Relation relation = (Relation) discussion.getContent().get(0);
            record = (Record) relation.getChild();
            persistance.synchronize(record);
            String xpath = "//comment[@id='"+threadId+"']";
            comment = (Element) record.getData().selectSingleNode(xpath);
        }

        boolean canContinue = true;
        canContinue &= setTitle(params, comment, env);
        canContinue &= setText(params, comment, env);

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
        urlUtils.redirect(response, "/ViewRelation?rid="+relation.getId());
        return null;
    }

    /**
     * Displays remove comment dialog
     */
    protected String actionRemoveComment(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");

        int threadId = Misc.parseInt((String) params.get(PARAM_THREAD), 0);
        if ( threadId==0 )
            throw new MissingArgumentException("Chybí parametr threadId!");

        persistance.synchronize(discussion);

        Relation relation = (Relation) discussion.getContent().get(0);
        Record record = (Record) relation.getChild();
        persistance.synchronize(record);
        String xpath = "//comment[@id='"+threadId+"']";
        Element element = (Element) record.getData().selectSingleNode(xpath);
        env.put(VAR_THREAD, new Comment(element));

        xpath = "//comment[parent/text()='"+threadId+"']";
        element = (Element) record.getData().selectSingleNode(xpath);
        if (element!=null)
            ServletUtils.addError(Constants.ERROR_GENERIC,"Komentáø nesmí obsahovat ¾ádné reakce!",env,null);

        return FMTemplateSelector.select("EditDiscussion", "remove", env, request);
    }

    /**
     * Removes selected comment.
     */
    protected String actionRemoveComment2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");

        int threadId = Misc.parseInt((String) params.get(PARAM_THREAD), 0);
        if ( threadId==0 )
            throw new MissingArgumentException("Chybí parametr threadId!");

        persistance.synchronize(discussion);

        Relation relation = (Relation) discussion.getContent().get(0);
        Record record = (Record) relation.getChild();
        persistance.synchronize(record);

        String xpath = "//comment[parent/text()='"+threadId+"']";
        Element element = (Element) record.getData().selectSingleNode(xpath);
        if ( element!=null ) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Komentáø nesmí obsahovat ¾ádné reakce!", env, null);
            return FMTemplateSelector.select("EditDiscussion", "remove", env, request);
        }

        xpath = "//comment[@id='"+threadId+"']";
        element = (Element) record.getData().selectSingleNode(xpath);
        element.detach();
        persistance.update(record);

        User user = (User) env.get(Constants.VAR_USER);
        relation = (Relation) env.get(VAR_RELATION);
        AdminLogger.logEvent(user, "smazal vlakno "+threadId+" diskuse "+discussion.getId()+", relace "+relation.getId());

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/ViewRelation?rid="+relation.getId());
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

        Date originalUpdated = discussion.getUpdated();
        MonitorTools.alterMonitor(discussion.getData().getRootElement(), user);
        persistance.update(discussion);
        SQLTool.getInstance().setUpdatedTimestamp(discussion, originalUpdated);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/ViewRelation?rid="+relation.getId());
        return null;
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

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/ViewRelation?rid="+relation.getId());
        return null;
    }

    /* ******** setters ********* */

    /**
     * Finds thread given by PARAM_THREAD key or makes facade around given Item.
     * @return Comment to be displayed
     * @throws PersistanceException if PARAM_THREAD points to nonexisting record
     */
    private Comment getDiscussedComment(Map params, Item discussion, Persistance persistance) {
        String thread = (String) params.get(PARAM_THREAD);
        if ( thread!=null && thread.length()>0 && ! "0".equals(thread) ) {
            Relation relation = (Relation) discussion.getContent().get(0);
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
    private boolean setTitle(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_TITLE);
        if ( tmp!=null && tmp.length()>0 ) {
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
    private boolean setText(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_TEXT);
        if ( tmp!=null && tmp.length()>0 ) {
            DocumentHelper.makeElement(root,"text").setText(tmp);
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
    private boolean setItemAuthor(Map params, User user, Element root, Item diz, Map env) {
        if ( user!=null ) {
            diz.setOwner(user.getId());
        } else {
            String tmp = (String) params.get(PARAM_AUTHOR);
            if ( tmp!=null && tmp.length()>0 ) {
                DocumentHelper.makeElement(root, "author").setText(tmp);
            } else {
                ServletUtils.addError(PARAM_AUTHOR, "Slu¹ností je se pøedstavit!", env, null);
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
    private boolean setCommentAuthor(Map params, User user, Element root, Map env) {
        if ( user!=null ) {
            DocumentHelper.makeElement(root, "author_id").setText(""+user.getId());
        } else {
            String tmp = (String) params.get(PARAM_AUTHOR);
            if ( tmp!=null && tmp.length()>0 ) {
                if (tmp.indexOf("<")!=-1) {
                    params.put(PARAM_AUTHOR,"");
                    ServletUtils.addError(PARAM_AUTHOR, "Pou¾ití HTML znaèek je zakázáno!", env, null);
                    return false;
                }
                DocumentHelper.makeElement(root, "author").setText(tmp);
            } else {
                ServletUtils.addError(PARAM_AUTHOR, "Slu¹ností je se pøedstavit!", env, null);
                return false;
            }
        }
        return true;
    }

    /**
     * Updates parent of this comment from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element of discussion to be updated
     * @return false, if there is a major error.
     */
    private boolean setParent(Map params, Element root) {
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
    private boolean setCreated(Element root) {
        DocumentHelper.makeElement(root, "created").setText(Constants.isoFormat.format(new Date()));
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
    private boolean setId(Element root, Element comment) {
        int last = 0;
        List comments = root.elements("comment");
        if ( comments!=null && comments.size()>0) {
            Element element = (Element) comments.get(comments.size()-1);
            String tmp = element.attributeValue("id");
            last = Integer.parseInt(tmp);
        }

        last++;
        comment.addAttribute("id",""+last);
        return true;
    }

    /**
     * Updates number of comments in discussion. Changes are not synchronized with persistance.
     * @param itemRoot root element of item to be updated.
     * @param recordRoot root element of record.
     * @return false, if there is a major error.
     */
    private boolean setCommentsCount(Element itemRoot, Element recordRoot) {
        List comments = recordRoot.selectNodes("comment");
        DocumentHelper.makeElement(itemRoot,"comments").setText(""+comments.size());
        return true;
    }
}
