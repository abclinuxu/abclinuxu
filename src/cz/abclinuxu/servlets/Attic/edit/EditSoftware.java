/*
 * User: literakl
 * Date: Feb 1, 2002
 * Time: 3:00:28 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.view.SelectIcon;
import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.security.Guard;
import cz.abclinuxu.utils.InstanceUtils;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.dom4j.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class EditSoftware extends AbcServlet {
    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_ICON = SelectIcon.PARAM_ICON;
    public static final String PARAM_URL = "url";
    public static final String PARAM_TEXT = "text";
    public static final String PARAM_VERSION = "version";
    public static final String PARAM_RECORD_ID = "recordId";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_RECORD = "RECORD";

    public static final String ACTION_ADD_ITEM = "addItem";
    public static final String ACTION_ADD_ITEM_STEP2 = "addItem2";
    public static final String ACTION_ADD_ITEM_STEP3 = "addItem3";
    public static final String ACTION_ADD_RECORD = "addRecord";
    public static final String ACTION_ADD_RECORD_STEP2 = "addRecord2";
    public static final String ACTION_EDIT_ITEM = "editItem";
    public static final String ACTION_EDIT_ITEM_STEP2 = "editItem2";
    public static final String ACTION_EDIT_RECORD = "editRecord";
    public static final String ACTION_EDIT_RECORD_STEP2 = "editRecord2";


    protected String process(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Relation relation = null;
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        String action = (String) params.get(AbcServlet.PARAM_ACTION);

        relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION,Relation.class,params);
        if ( relation!=null ) {
            persistance.synchronize(relation);
            persistance.synchronize(relation.getChild());
            ctx.put(VAR_RELATION,relation);
        } else throw new Exception("Chybí parametr relationId!");

        if ( action==null || action.equals(ACTION_ADD_ITEM) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation.getChild(),Guard.OPERATION_ADD,Item.class);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return VariantTool.selectTemplate(request,ctx,"EditUser","login");
                case Guard.ACCESS_DENIED: ServletUtils.addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx, null);
                default: return VariantTool.selectTemplate(request,ctx,"EditSoftware","add_item");
            }

        } else if ( action.equals(ACTION_ADD_ITEM_STEP2) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation.getChild(),Guard.OPERATION_ADD,Item.class);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return VariantTool.selectTemplate(request,ctx,"EditUser","login");
                case Guard.ACCESS_DENIED: {
                    ServletUtils.addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx, null);
                    return VariantTool.selectTemplate(request,ctx,"EditSoftware","add_item");
                }
                default: return actionAddStep2(request,ctx);
            }

        } else if ( action.equals(ACTION_ADD_ITEM_STEP3) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation.getChild(),Guard.OPERATION_ADD,Item.class);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return VariantTool.selectTemplate(request,ctx,"EditUser","login");
                case Guard.ACCESS_DENIED: {
                    ServletUtils.addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx, null);
                    return VariantTool.selectTemplate(request,ctx,"EditSoftware","add_item");
                }
                default: return actionAddStep3(request,response,ctx);
            }

        } else if ( action.equals(ACTION_ADD_RECORD) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation.getChild(),Guard.OPERATION_ADD,Record.class);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return VariantTool.selectTemplate(request,ctx,"EditUser","login");
                case Guard.ACCESS_DENIED: ServletUtils.addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx, null);
                default: {
                    params.put(PARAM_ACTION,ACTION_ADD_RECORD_STEP2);
                    return VariantTool.selectTemplate(request,ctx,"EditSoftware","add_record");
                }
            }

        } else if ( action.equals(ACTION_ADD_RECORD_STEP2) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation.getChild(),Guard.OPERATION_ADD,Record.class);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return VariantTool.selectTemplate(request,ctx,"EditUser","login");
                case Guard.ACCESS_DENIED: ServletUtils.addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx, null);
                default: return actionAddRecord(request,response,ctx);
            }

        } else if ( action.equals(ACTION_EDIT_RECORD) ) {
            Record record = (Record) InstanceUtils.instantiateParam(PARAM_RECORD_ID,Record.class,params);
            persistance.synchronize(record);
            ctx.put(VAR_RECORD,record);

            int rights = Guard.check((User)ctx.get(VAR_USER),record,Guard.OPERATION_EDIT,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return VariantTool.selectTemplate(request,ctx,"EditUser","login");
                case Guard.ACCESS_DENIED: ServletUtils.addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx, null);
                default: return actionEditRecord(request,ctx);
            }

        } else if ( action.equals(ACTION_EDIT_RECORD_STEP2) ) {
            Record record = (Record) InstanceUtils.instantiateParam(PARAM_RECORD_ID,Record.class,params);
            persistance.synchronize(record);
            ctx.put(VAR_RECORD,record);

            int rights = Guard.check((User)ctx.get(VAR_USER),record,Guard.OPERATION_EDIT,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return VariantTool.selectTemplate(request,ctx,"EditUser","login");
                case Guard.ACCESS_OK: return actionEditRecord2(request,response,ctx);
                case Guard.ACCESS_DENIED: ServletUtils.addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx, null);
                default: return actionEditRecord(request,ctx);
            }

        } else if ( action.equals(ACTION_EDIT_ITEM) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation.getChild(),Guard.OPERATION_EDIT,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return VariantTool.selectTemplate(request,ctx,"EditUser","login");
                case Guard.ACCESS_DENIED: ServletUtils.addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx, null);
                default: return actionEditItem(request,ctx);
            }

        } else if ( action.equals(ACTION_EDIT_ITEM_STEP2) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation.getChild(),Guard.OPERATION_EDIT,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return VariantTool.selectTemplate(request,ctx,"EditUser","login");
                case Guard.ACCESS_OK: return actionEditItem2(request,response,ctx);
                case Guard.ACCESS_DENIED: ServletUtils.addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx, null);
                default: return actionEditItem(request,ctx);
            }

        }

        return VariantTool.selectTemplate(request,ctx,"EditSoftware","add_item");
    }

    protected String actionAddStep2(HttpServletRequest request, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);

        String name = (String) params.get(PARAM_NAME);
        if ( name==null || name.length()==0 ) {
            ServletUtils.addError(PARAM_NAME,"Nevyplnil jste název druhu!",ctx,null);
            return VariantTool.selectTemplate(request,ctx,"EditSoftware","add_item");
        }
        return VariantTool.selectTemplate(request,ctx,"EditSoftware","add_record");
    }

    protected String actionAddStep3(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation upper = (Relation) ctx.get(VAR_RELATION);
        User user = (User) ctx.get(AbcServlet.VAR_USER);

        String name = (String) params.get(PARAM_NAME);
        if ( name==null || name.length()==0 ) {
            ServletUtils.addError(PARAM_NAME,"Nevyplnil jste název druhu!",ctx,null);
            return VariantTool.selectTemplate(request,ctx,"EditSoftware","add_item");
        }
        String icon = (String) params.get(PARAM_ICON);

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        root.addElement("name").addText(name);
        if ( icon!=null && icon.length()>0 ) root.addElement("icon").addText(icon);

        Item item = new Item(0,Item.MAKE);
        item.setData(document);
        item.setOwner(user.getId());

        String url = (String) params.get(PARAM_URL);
        String text = (String) params.get(PARAM_TEXT);
        String version = (String) params.get(PARAM_VERSION);

        if ( text==null || text.length()==0 ) {
            ServletUtils.addError(PARAM_TEXT,"Vyplòte návod!",ctx,null);
            return VariantTool.selectTemplate(request,ctx,"EditSoftware","add_record");
        }

        document = DocumentHelper.createDocument();
        root = document.addElement("data");
        root.addElement("text").addText(text);
        if ( url!=null && url.length()>0 ) root.addElement("url").addText(url);
        if ( version!=null && version.length()>0 ) root.addElement("version").addText(version);

        Record record = new Record(0,Record.SOFTWARE);
        record.setData(document);
        record.setOwner(user.getId());

        try {
            persistance.create(item);
            Relation relation = new Relation(upper.getChild(),item,upper.getId());
            persistance.create(relation);
            persistance.create(record);
            persistance.create(new Relation(item,record,relation.getId()));

            UrlUtils.redirect("/ViewRelation?relationId="+relation.getId(),response,ctx);
            return null;
        } catch (PersistanceException e) {
            ServletUtils.addError(AbcServlet.GENERIC_ERROR,e.getMessage(),ctx, null);
            return VariantTool.selectTemplate(request,ctx,"EditSoftware","add_record");
        }
    }

    protected String actionAddRecord(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation upper = (Relation) ctx.get(VAR_RELATION);
        User user = (User) ctx.get(AbcServlet.VAR_USER);

        String url = (String) params.get(PARAM_URL);
        String text = (String) params.get(PARAM_TEXT);
        String version = (String) params.get(PARAM_VERSION);

        if ( text==null || text.length()==0 ) {
            ServletUtils.addError(PARAM_TEXT,"Vyplòte návod!",ctx,null);
            return VariantTool.selectTemplate(request,ctx,"EditSoftware","add_record");
        }

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        root.addElement("text").addText(text);
        if ( url!=null && url.length()>0 ) root.addElement("url").addText(url);
        if ( version!=null && version.length()>0 ) root.addElement("version").addText(version);

        Record record = new Record(0,Record.SOFTWARE);
        record.setData(document);
        record.setOwner(user.getId());

        try {
            persistance.create(record);
            Relation relation = new Relation(upper.getChild(),record,upper.getId());
            persistance.create(relation);

            UrlUtils.redirect("/ViewRelation?relationId="+relation.getId(),response,ctx);
            return null;
        } catch (PersistanceException e) {
            ServletUtils.addError(AbcServlet.GENERIC_ERROR,e.getMessage(),ctx, null);
            return VariantTool.selectTemplate(request,ctx,"EditSoftware","add_record");
        }
    }

    protected String actionEditRecord(HttpServletRequest request, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        VelocityHelper helper = (VelocityHelper) ctx.get(AbcServlet.VAR_HELPER);
        Persistance persistance = PersistanceFactory.getPersistance();

        Relation upper = (Relation) ctx.get(VAR_RELATION);
        Record record = (Record) ctx.get(VAR_RECORD);

        Document document = record.getData();
        Node node = document.selectSingleNode("data/url");
        if ( node!=null ) params.put(PARAM_URL,node.getText());
        node = document.selectSingleNode("data/version");
        if ( node!=null ) params.put(PARAM_VERSION,node.getText());
        node = document.selectSingleNode("data/text");
        if ( node!=null ) params.put(PARAM_TEXT,helper.encodeSpecial(node.getText()));

        params.put(PARAM_ACTION,ACTION_EDIT_RECORD_STEP2);
        return VariantTool.selectTemplate(request,ctx,"EditSoftware","edit_record");
    }

    protected String actionEditRecord2(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Relation relation = (Relation) ctx.get(VAR_RELATION);
        Record record = (Record) ctx.get(VAR_RECORD);
        Document document = record.getData();

        String url = (String) params.get(PARAM_URL);
        String text = (String) params.get(PARAM_TEXT);
        String version = (String) params.get(PARAM_VERSION);

        if ( text==null || text.length()==0 ) {
            ServletUtils.addError(PARAM_TEXT,"Vyplòte návod!",ctx,null);
            return VariantTool.selectTemplate(request,ctx,"EditSoftware","edit_record");
        }

        DocumentHelper.makeElement(document,"data/text").setText(text);
        if ( url!=null ) DocumentHelper.makeElement(document,"data/url").setText(url);
        if ( version!=null ) DocumentHelper.makeElement(document,"data/version").setText(version);

        persistance.update(record);

        UrlUtils.redirect("/ViewRelation?relationId="+relation.getId(),response,ctx);
        return null;
    }

    protected String actionEditItem(HttpServletRequest request, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);

        Relation relation = (Relation) ctx.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        Document document = item.getData();

        Node node = document.selectSingleNode("data/name");
        params.put(PARAM_NAME,node.getText());
        node = document.selectSingleNode("data/icon");
        if ( node!=null ) params.put(PARAM_ICON,node.getText());

        return VariantTool.selectTemplate(request,ctx,"EditSoftware","edit_item");
    }

    protected String actionEditItem2(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) ctx.get(VAR_RELATION);

        Item item = (Item) relation.getChild();
        Document document = item.getData();
        Node node = document.selectSingleNode("data/name");

        String tmp = (String) params.get(PARAM_NAME);
        if ( tmp==null || tmp.length()==0 ) {
            ServletUtils.addError(PARAM_NAME,"Nevyplnil jste název druhu!",ctx,null);
            return VariantTool.selectTemplate(request,ctx,"EditSoftware","edit_item");
        }

        node.setText(tmp);
        persistance.update(item);

        UrlUtils.redirect("/ViewRelation?relationId="+relation.getUpper(),response,ctx);
        return null;
    }
}
