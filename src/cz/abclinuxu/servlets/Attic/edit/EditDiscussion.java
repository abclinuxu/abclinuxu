/*
 * User: literakl
 * Date: Feb 25, 2002
 * Time: 7:45:21 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Tools;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.exceptions.PersistanceException;

import org.dom4j.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * This class is responsible for adding new
 * new discussion.<p>
 * <u>ACTIONS</u>
 * <dl>
 * <dt><code>ACTION_ADD_DISCUSSION</code></dt>
 * <dd>Adds new discussion to any GenericObject.</dd>
 * <dt><code>ACTION_ADD_QUESTION</code></dt>
 * <dd>Adds new discussion with initial question to support forum.</dd>
 * <dt><code>ACTION_ADD_REACTION</code></dt>
 * <dd>Adds one response or answear to selected discussion.</dd>
 * </dl>
 */
public class EditDiscussion extends AbcFMServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditDiscussion.class);

    public static final String PARAM_RELATION = "relationId";
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

    public static final String ACTION_ADD_DISCUSSION = "addDiz";
    public static final String ACTION_ADD_QUESTION = "addQuez";
    public static final String ACTION_ADD_QUESTION_STEP2 = "addQuez2";
    public static final String ACTION_ADD_COMMENT = "add";
    public static final String ACTION_ADD_COMMENT_STEP2 = "add2";

    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION,Relation.class,params);
        String action = (String) params.get(PARAM_ACTION);

        if ( relation!=null ) {
            relation = (Relation) persistance.findById(relation);
            persistance.synchronize(relation.getChild());
            env.put(VAR_RELATION,relation);
        } else if ( !action.equals(ACTION_ADD_QUESTION) )
            throw new MissingArgumentException("Chybí parametr relationId!");

        if ( ACTION_ADD_DISCUSSION.equals(action) ) {
            return actionAddDiscussion(request,env);

        } else if ( ACTION_ADD_COMMENT.equals(action) ) {
            return actionAddComment(request,env);

        } else if ( ACTION_ADD_COMMENT_STEP2.equals(action) ) {
            return  actionAddComment2(request,response,env);

        } else if ( ACTION_ADD_QUESTION.equals(action) ) {
            return FMTemplateSelector.select("EditDiscussion","ask",env,request);

        } else if ( ACTION_ADD_QUESTION_STEP2.equals(action) ) {
            return actionAddQuestion2(request,response,env);
        }

        return actionAddComment(request,env);
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
            document.addElement("data");
            discussion.setData(document);
            if ( user!=null ) discussion.setOwner(user.getId());

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
     * creates question
     */
    protected String actionAddQuestion2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(VAR_RELATION);
        Item discussion = new Item(0,Item.DISCUSSION);

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        discussion.setData(document);

        boolean error = false;
        String tmp = (String) params.get(PARAM_AUTHOR);
        if ( user!=null ) {
            discussion.setOwner(user.getId());
        } else {
            if ( tmp!=null && tmp.length()>0 ) {
                root.addElement("author").setText(tmp);
            } else {
                ServletUtils.addError(PARAM_AUTHOR,"Slu¹ností je se pøedstavit!",env,null);
                error = true;
            }
        }

        tmp = (String) params.get(PARAM_TITLE);
        if ( tmp!=null && tmp.length()>0 ) {
            root.addElement("title").setText(tmp);
        } else {
            ServletUtils.addError(PARAM_TITLE,"Zadejte titulek va¹eho dotazu!",env,null);
            error = true;
        }

        tmp = (String) params.get(PARAM_TEXT);
        if ( tmp!=null && tmp.length()>0 ) {
            root.addElement("text").setText(tmp);
        } else {
            ServletUtils.addError(PARAM_TEXT,"Zadejte text va¹eho dotazu!",env,null);
            error = true;
        }

        if ( error || params.get(PARAM_PREVIEW)!=null ) {
            discussion.setInitialized(true);
            discussion.setUpdated(new Date());
            env.put(VAR_PREVIEW,discussion);
            return FMTemplateSelector.select("EditDiscussion","ask_confirm",env,request);
        }

        persistance.create(discussion);
        Relation rel2 = new Relation(relation.getChild(),discussion,relation.getId());
        persistance.create(rel2);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/ViewRelation?relationId="+relation.getId());
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
        env.put(VAR_DISCUSSION,discussion);

        Record record = getDiscussion(params,discussion,persistance);
        // display reaction, that is discussed, only if it has title
        // (article discussions doesn't have it)
        if ( Tools.xpath(record,"data/title")!=null )
            env.put(VAR_THREAD,record);

        return FMTemplateSelector.select("EditDiscussion","reply",env,request);
    }

    /**
     * Adds new comment to selected discussion.
     */
    protected String actionAddComment2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);

        Item discussion = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION,Item.class,params);
        if ( discussion==null )
            throw new MissingArgumentException("Chybí parametr dizId!");

        persistance.synchronize(discussion);
        Relation relation = (Relation) env.get(VAR_RELATION);

        Record reaction = new Record(0,Record.DISCUSSION);
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        reaction.setData(document);

        boolean error = false;
        String tmp = (String) params.get(PARAM_AUTHOR);
        if ( user!=null ) {
            reaction.setOwner(user.getId());
        } else {
            if ( tmp!=null && tmp.length()>0 ) {
                root.addElement("author").setText(tmp);
            } else {
                ServletUtils.addError(PARAM_AUTHOR,"Slu¹ností je se pøedstavit!",env,null);
                error = true;
            }
        }

        tmp = (String) params.get(PARAM_TITLE);
        if ( tmp!=null && tmp.length()>0 ) {
            root.addElement("title").setText(tmp);
        } else {
            ServletUtils.addError(PARAM_TITLE,"Zadejte titulek va¹eho pøíspìvku!",env,null);
            error = true;
        }

        tmp = (String) params.get(PARAM_TEXT);
        if ( tmp!=null && tmp.length()>0 ) {
            root.addElement("text").setText(tmp);
        } else {
            ServletUtils.addError(PARAM_TEXT,"Zadejte text va¹eho pøíspìvku!",env,null);
            error = true;
        }

        tmp = (String) params.get(PARAM_THREAD);
        if ( tmp!=null && tmp.length()>0 )
            root.addElement("thread").setText(tmp);

        if ( error ) {
            env.put(VAR_DISCUSSION,discussion);
            Record record = getDiscussion(params,discussion,persistance);
            if ( Tools.xpath(record,"data/title")!=null )
                env.put(VAR_THREAD,record);
            return FMTemplateSelector.select("EditDiscussion","reply",env,request);
        }

        if ( params.get(PARAM_PREVIEW)!=null ) {
            env.put(VAR_DISCUSSION,discussion);
            reaction.setInitialized(true);
            reaction.setUpdated(new Date());
            env.put(VAR_PREVIEW,reaction);
            Record record = getDiscussion(params,discussion,persistance);
            if ( Tools.xpath(record,"data/title")!=null )
                env.put(VAR_THREAD,record);
            return FMTemplateSelector.select("EditDiscussion","reply",env,request);
        }

        persistance.create(reaction);
        Relation rel = new Relation(discussion,reaction,0);
        persistance.create(rel);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/ViewRelation?relationId="+relation.getId());
        return null;
    }

    /**
     * Finds Record of type Discussion from PARAM_THREAD key in params or makes facade
     * of given Item.
     * @return initialized record
     * @throws PersistanceException if PARAM_THREAD points to nonexisting record
     */
    private Record getDiscussion(Map params, Item discussion, Persistance persistance) {
        Record record = (Record) InstanceUtils.instantiateParam(PARAM_THREAD,Record.class,params);
        if ( record!=null && record.getId()!=0 )
            record = (Record) persistance.findById(record);
        else {
            // Item.Discussion to Record.Discussion facade
            record = new Record();
            record.setData(discussion.getData());
            record.setUpdated(discussion.getUpdated());
            record.setOwner(discussion.getOwner());
            record.setInitialized(true);
        }
        return record;
    }
}
