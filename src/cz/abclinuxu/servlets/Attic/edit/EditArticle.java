/*
 * User: literakl
 * Date: Feb 1, 2002
 * Time: 4:48:29 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.AbcVelocityServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.view.SelectRelation;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.security.Guard;
import cz.abclinuxu.utils.InstanceUtils;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.dom4j.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Class for manipulation of articles.
 * @todo implement flow of checks: Author enters article, revisor corrects grammar, editor approves article and selects publish date.
 */
public class EditArticle extends AbcVelocityServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditArticle.class);

    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_PEREX = "perex";
    public static final String PARAM_CONTENT = "content";
    public static final String PARAM_PUBLISHED = "published";
    public static final String PARAM_AUTHOR_ID = SelectRelation.PARAM_SELECTED;

    public static final String VAR_RELATION = "RELATION";

    public static final String ACTION_ADD_ITEM = "add";
    public static final String ACTION_ADD_ITEM_STEP2 = "add2";
    public static final String ACTION_EDIT_ITEM = "edit";
    public static final String ACTION_EDIT_ITEM_STEP2 = "edit2";


    protected String process(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Relation relation = null;
        Map params = (Map) request.getAttribute(AbcVelocityServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        String action = (String) params.get(AbcVelocityServlet.PARAM_ACTION);

        relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION,Relation.class,params);
        if ( relation!=null ) {
            persistance.synchronize(relation);
            persistance.synchronize(relation.getChild());
            ctx.put(VAR_RELATION,relation);
        } else throw new Exception("Chybí parametr relationId!");

        if ( action==null || action.equals(ACTION_ADD_ITEM) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation.getChild(),Guard.OPERATION_ADD,Item.class);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return VelocityTemplateSelector.selectTemplate(request,ctx,"EditUser","login");
                case Guard.ACCESS_DENIED: return VelocityTemplateSelector.selectTemplate(request,ctx,"EditUser","forbidden");
                default: {
                    params.put(PARAM_PUBLISHED,Constants.isoFormat.format(new Date()));
                    return VelocityTemplateSelector.selectTemplate(request,ctx,"EditArticle","add");
                }
            }

        } else if ( action.equals(ACTION_ADD_ITEM_STEP2) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation.getChild(),Guard.OPERATION_ADD,Item.class);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return VelocityTemplateSelector.selectTemplate(request,ctx,"EditUser","login");
                case Guard.ACCESS_DENIED: return VelocityTemplateSelector.selectTemplate(request,ctx,"EditUser","forbidden");
                default: return actionAddStep2(request,response,ctx);
            }

        } else if ( action.equals(ACTION_EDIT_ITEM) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation.getChild(),Guard.OPERATION_EDIT,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return VelocityTemplateSelector.selectTemplate(request,ctx,"EditUser","login");
                case Guard.ACCESS_DENIED: return VelocityTemplateSelector.selectTemplate(request,ctx,"EditUser","forbidden");
                default: return actionEditItem(request,ctx);
            }

        } else if ( action.equals(ACTION_EDIT_ITEM_STEP2) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation.getChild(),Guard.OPERATION_EDIT,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return VelocityTemplateSelector.selectTemplate(request,ctx,"EditUser","login");
                case Guard.ACCESS_DENIED: return VelocityTemplateSelector.selectTemplate(request,ctx,"EditUser","forbidden");
                default: return actionEditItem2(request,response,ctx);
            }

        }
        return VelocityTemplateSelector.selectTemplate(request,ctx,"EditArticle","add");
    }

    protected String  actionAddStep2(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcVelocityServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation upper = (Relation) ctx.get(VAR_RELATION);
        User user = (User) ctx.get(AbcVelocityServlet.VAR_USER);

        boolean error = false;
        Date publish = null;

        String name = (String) params.get(PARAM_TITLE);
        if ( name==null || name.length()==0 ) {
            ServletUtils.addError(PARAM_TITLE,"Nevyplnil jste titulek èlánku!",ctx,null); error = true;
        }

        String perex = (String) params.get(PARAM_PEREX);
        if ( perex==null || perex.length()==0 ) {
            ServletUtils.addError(PARAM_PEREX,"Nevyplnil jste popis èlánku!",ctx,null); error = true;
        }

        String content = (String) params.get(PARAM_CONTENT);
        if ( content==null || content.length()==0 ) {
            ServletUtils.addError(PARAM_CONTENT,"Nevyplnil jste obsah èlánku!",ctx,null); error = true;
        }

        String str = (String) params.get(PARAM_PUBLISHED);
        if ( str==null || str.length()<12 ) {
            ServletUtils.addError(PARAM_PUBLISHED,"Správný formát je 2002-02-10 06:22",ctx,null); error = true;
        } else {
            try {
                publish = Constants.isoFormat.parse(str);
            } catch (ParseException e) {
                ServletUtils.addError(PARAM_PUBLISHED,"Správný formát je 2002-02-10 06:22",ctx,null); error = true;
            }
        }

        if ( error ) {
            return VelocityTemplateSelector.selectTemplate(request,ctx,"EditArticle","add");
        }

        /** todo: support for author not listed in section Authors */
        Relation tmp = (Relation) InstanceUtils.instantiateParam(PARAM_AUTHOR_ID,Relation.class,params);
        persistance.synchronize(tmp);
        User author = (User) tmp.getChild();

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        root.addElement("name").addText(name);
        root.addElement("author").addText(""+author.getId());
        root.addElement("editor").addText(""+user.getId());
        root.addElement("revisor").addText(""+user.getId());
        root.addElement("perex").addText(perex);


        Item item = new Item(0,Item.ARTICLE);
        item.setData(document);
        item.setOwner(user.getId());
        item.setCreated(publish);

        document = DocumentHelper.createDocument();
        root = document.addElement("data");
        root.addElement("content").addText(content);
        root.addElement("part").addText("1");

        Record record = new Record(0,Record.ARTICLE);
        record.setData(document);
        record.setOwner(user.getId());

        try {
            persistance.create(item);
            Relation relation = new Relation(upper.getChild(),item,upper.getId());
            persistance.create(relation);
            persistance.create(record);
            persistance.create(new Relation(item,record,relation.getId()));

            UrlUtils.redirect(response, "/ViewRelation?relationId="+relation.getId());
            return null;
        } catch (PersistanceException e) {
            ServletUtils.addError(AbcVelocityServlet.GENERIC_ERROR,e.getMessage(),ctx, null);
            return VelocityTemplateSelector.selectTemplate(request,ctx,"EditArticle","add");
        }
    }

    protected String  actionEditItem(HttpServletRequest request, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcVelocityServlet.ATTRIB_PARAMS);
        VelocityHelper helper = (VelocityHelper) ctx.get(AbcVelocityServlet.VAR_HELPER);

        Relation relation = (Relation) ctx.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        Document document = item.getData();

        Node node = document.selectSingleNode("data/name");
        params.put(PARAM_TITLE,node.getText());
        node = document.selectSingleNode("data/perex");
        if ( node!=null ) params.put(PARAM_PEREX,helper.encodeSpecial(node.getText()));
        params.put(PARAM_PUBLISHED,item.getCreated());

        Record record = null;
        for (Iterator iter = item.getContent().iterator(); iter.hasNext();) {
            Relation rel = (Relation) iter.next();
            PersistanceFactory.getPersistance().synchronize(rel.getChild());
            if ( rel.getChild() instanceof Record ) {
                record = (Record) rel.getChild();
                if ( record.getType()==Record.ARTICLE ) {
                    document = record.getData();
                    node = document.selectSingleNode("data/content");
                    params.put(PARAM_CONTENT,helper.encodeSpecial(node.getText()));
                    break;
                }
            }
        }

        return VelocityTemplateSelector.selectTemplate(request,ctx,"EditArticle","edit");
    }

    protected String actionEditItem2(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcVelocityServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation upper = (Relation) ctx.get(VAR_RELATION);

        boolean error = false;
        Date publish = null;

        String name = (String) params.get(PARAM_TITLE);
        if ( name==null || name.length()==0 ) {
            ServletUtils.addError(PARAM_TITLE,"Nevyplnil jste titulek èlánku!",ctx,null); error = true;
        }

        String perex = (String) params.get(PARAM_PEREX);
        if ( perex==null || perex.length()==0 ) {
            ServletUtils.addError(PARAM_PEREX,"Nevyplnil jste popis èlánku!",ctx,null); error = true;
        }

        String content = (String) params.get(PARAM_CONTENT);
        if ( content==null || content.length()==0 ) {
            ServletUtils.addError(PARAM_CONTENT,"Nevyplnil jste obsah èlánku!",ctx,null); error = true;
        }

        String str = (String) params.get(PARAM_PUBLISHED);
        if ( str==null || str.length()<12 ) {
            ServletUtils.addError(PARAM_PUBLISHED,"Správný formát je 2002-02-10 06:22",ctx,null); error = true;
        } else {
            try {
                publish = Constants.isoFormat.parse(str);
            } catch (ParseException e) {
                ServletUtils.addError(PARAM_PUBLISHED,"Správný formát je 2002-02-10 06:22",ctx,null); error = true;
            }
        }

        if ( error ) {
            return VelocityTemplateSelector.selectTemplate(request,ctx,"EditArticle","edit");
        }

        Item item = (Item) upper.getChild();
        item.setCreated(publish);

        Document document = item.getData();
        DocumentHelper.makeElement(document,"data/name").setText(name);
        DocumentHelper.makeElement(document,"data/perex").setText(perex);
        persistance.update(item);

        Record record = null;
        for (Iterator iter = item.getContent().iterator(); iter.hasNext();) {
            Relation rel = (Relation) iter.next();
            PersistanceFactory.getPersistance().synchronize(rel.getChild());
            if ( rel.getChild() instanceof Record ) {
                record = (Record) rel.getChild();
                if ( record.getType()==Record.ARTICLE ) {
                    document = record.getData();
                    DocumentHelper.makeElement(document,"data/content").setText(content);
                    persistance.update(record);
                    break;
                }
            }
        }

        UrlUtils.redirect(response, "/ViewRelation?relationId="+upper.getId());
        return null;
    }
}
