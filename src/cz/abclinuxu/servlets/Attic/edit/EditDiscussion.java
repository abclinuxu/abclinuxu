/*
 * User: literakl
 * Date: Feb 25, 2002
 * Time: 7:45:21 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.servlets.utils.VelocityHelper;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
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
public class EditDiscussion extends AbcServlet {
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
    public static final String VAR_CONTINUE = "CONTINUE";

    public static final String ACTION_ADD_DISCUSSION = "addDiz";
    public static final String ACTION_ADD_QUESTION = "addQuez";
    public static final String ACTION_ADD_QUESTION_STEP2 = "addQuez2";
    public static final String ACTION_ADD_COMMENT = "add";
    public static final String ACTION_ADD_COMMENT_STEP2 = "add2";

    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) instantiateParam(PARAM_RELATION,Relation.class,params);
        String action = (String) params.get(AbcServlet.PARAM_ACTION);

        if ( relation!=null ) {
            relation = (Relation) persistance.findById(relation);
            persistance.synchronize(relation.getChild());
            ctx.put(VAR_RELATION,relation);
        } else if ( !action.equals(ACTION_ADD_QUESTION) ) throw new Exception("Chybí parametr relationId!");

        if ( action==null || action.equals(ACTION_ADD_DISCUSSION) ) {
            return actionAddDiscussion(request,ctx);

        } else if ( action.equals(ACTION_ADD_COMMENT) ) {
            return actionAddComment(request,ctx);

        } else if ( action.equals(ACTION_ADD_COMMENT_STEP2) ) {
            return  actionAddComment2(request,response,ctx);

        } else if ( action.equals(ACTION_ADD_QUESTION) ) {
            return getTemplate("add/question.vm");
        } else if ( action.equals(ACTION_ADD_QUESTION_STEP2) ) {
            return actionAddQuestion2(request,response,ctx);
        }

        return getTemplate("add/response.vm");
    }

    /**
     * adds new discussion to selected object, if it is not defined meanwhile.
     * then opens form for adding new reaction.
     */
    protected Template actionAddDiscussion(HttpServletRequest request, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Relation relation = (Relation) ctx.get(VAR_RELATION);
        User user = (User) ctx.get(AbcServlet.VAR_USER);
        GenericObject child = relation.getChild();
        Item discussion = null;

        for (Iterator iter = child.getContent().iterator(); iter.hasNext();) {
            Relation rel = (Relation) iter.next();
            persistance.synchronize(rel.getChild());
            if ( rel.getChild() instanceof Item ) {
                Item item = (Item) rel.getChild();
                if ( item.getType()==Item.DISCUSSION ) {
                    discussion = item;
                    break;
                }
            }
        }

        if ( discussion==null ) {
            discussion = new Item(0,Item.DISCUSSION);

            Document document = DocumentHelper.createDocument();
            Element root = document.addElement("data");
            discussion.setData(document);

            if ( user!=null ) discussion.setOwner(user.getId());
            persistance.create(discussion);

            Relation tmp = new Relation(relation.getChild(),discussion,relation.getId());
            persistance.create(tmp);
        }

        ctx.put(VAR_DISCUSSION,discussion);
        return getTemplate("add/response.vm");
    }

    /**
     * Displays add comment dialog
     */
    protected Template actionAddComment(HttpServletRequest request, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Item discussion = (Item) instantiateParam(PARAM_DISCUSSION,Item.class,params);
        if ( discussion==null ) throw new Exception("Chybí parametr dizId!");
        persistance.synchronize(discussion);
        ctx.put(VAR_DISCUSSION,discussion);

        Record record = new Record();
        record.setData(discussion.getData());
        record.setUpdated(discussion.getUpdated());
        record.setOwner(discussion.getOwner());
        record.setInitialized(true);

        String tmp = (String) params.get(PARAM_THREAD);
        if ( tmp!=null && tmp.length()>0 ) {
            int upper = Integer.parseInt(tmp);
            if ( upper!=0 ) {
                record = (Record) persistance.findById(new Record(upper));
            }
        }
        if ( VelocityHelper.getXPath(record,"data/title")!=null ) ctx.put(VAR_THREAD,record);

        return getTemplate("add/response.vm");
    }

    /**
     * Adds new comment to selected discussion.
     */
    protected Template actionAddComment2(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Item discussion = (Item) instantiateParam(PARAM_DISCUSSION,Item.class,params);
        if ( discussion==null ) throw new Exception("Chybí parametr dizId!");
        persistance.synchronize(discussion);

        Relation relation = (Relation) ctx.get(VAR_RELATION);
        User user = (User) ctx.get(AbcServlet.VAR_USER);

        Record reaction = new Record(0,Record.DISCUSSION);
        boolean error = false;

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        reaction.setData(document);

        String tmp = (String) params.get(PARAM_AUTHOR);
        if ( user!=null ) {
            reaction.setOwner(user.getId());
        } else {
            if ( tmp!=null && tmp.length()>0 ) {
                root.addElement("author").setText(tmp);
            } else {
                addError(PARAM_AUTHOR,"Slu¹ností je se pøedstavit!",ctx,null);
                error = true;
            }
        }

        tmp = (String) params.get(PARAM_TITLE);
        if ( tmp!=null && tmp.length()>0 ) {
            root.addElement("title").setText(tmp);
        } else {
            addError(PARAM_TITLE,"Zadejte titulek va¹eho pøíspìvku!",ctx,null);
            error = true;
        }

        tmp = (String) params.get(PARAM_TEXT);
        if ( tmp!=null && tmp.length()>0 ) {
            tmp = VelocityHelper.fixLines(tmp);
            root.addElement("text").setText(tmp);
        } else {
            addError(PARAM_TEXT,"Zadejte text va¹eho pøíspìvku!",ctx,null);
            error = true;
        }

        tmp = (String) params.get(PARAM_THREAD);
        int upper = 0;
        if ( tmp!=null && tmp.length()>0 ) {
            root.addElement("thread").setText(tmp);
            upper = Integer.parseInt(tmp);
        }

        if ( error ) {
            ctx.put(VAR_DISCUSSION,discussion);
            if ( upper!=0 ) {
                Record record = (Record) persistance.findById(new Record(upper));
                ctx.put(VAR_THREAD,record);
            }
            return getTemplate("add/response.vm");
        }

        ctx.put(VAR_CONTINUE,new Boolean(true));

        if ( params.get(PARAM_PREVIEW)!=null ) {
            ctx.put(VAR_DISCUSSION,discussion);
            reaction.setInitialized(true);
            reaction.setUpdated(new Date());
            ctx.put(VAR_PREVIEW,reaction);

            if ( upper!=0 ) {
                Record record = (Record) persistance.findById(new Record(upper));
                ctx.put(VAR_THREAD,record);
            }
            return getTemplate("add/response.vm");
        }

        persistance.create(reaction);
        Relation rel = new Relation(discussion,reaction,0);
        persistance.create(rel);

        redirect("/ViewRelation?relationId="+relation.getId(),response,ctx);
        return null;
    }

    /**
     * creates question
     */
    protected Template actionAddQuestion2(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Relation relation = (Relation) ctx.get(VAR_RELATION);
        User user = (User) ctx.get(AbcServlet.VAR_USER);
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
                addError(PARAM_AUTHOR,"Slu¹ností je se pøedstavit!",ctx,null);
                error = true;
            }
        }

        tmp = (String) params.get(PARAM_TITLE);
        if ( tmp!=null && tmp.length()>0 ) {
            root.addElement("title").setText(tmp);
        } else {
            addError(PARAM_TITLE,"Zadejte titulek va¹eho dotazu!",ctx,null);
            error = true;
        }

        tmp = (String) params.get(PARAM_TEXT);
        if ( tmp!=null && tmp.length()>0 ) {
            tmp = VelocityHelper.fixLines(tmp);
            root.addElement("text").setText(tmp);
        } else {
            addError(PARAM_TEXT,"Zadejte text va¹eho dotazu!",ctx,null);
            error = true;
        }

        if ( error || params.get(PARAM_PREVIEW)!=null ) {
            discussion.setInitialized(true);
            discussion.setUpdated(new Date());
            ctx.put(VAR_THREAD,discussion);
            return getTemplate("add/question2.vm");
        }

        persistance.create(discussion);
        Relation rel2 = new Relation(relation.getChild(),discussion,relation.getId());
        persistance.create(rel2);

        redirect("/ViewRelation?relationId="+relation.getId(),response,ctx);
        return null;
    }
}
