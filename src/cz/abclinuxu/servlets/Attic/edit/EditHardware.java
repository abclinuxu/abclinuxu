/*
 * User: literakl
 * Date: Jan 30, 2002
 * Time: 8:24:00 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.servlets.view.SelectIcon;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.security.Guard;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.dom4j.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * This class is responsible for adding and
 * editing of hardware items and records.
 */
public class EditHardware extends AbcServlet {
    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_ICON = SelectIcon.PARAM_ICON;
    public static final String PARAM_DRIVER = "driver";
    public static final String PARAM_PRICE = "price";
    public static final String PARAM_SETUP = "setup";
    public static final String PARAM_TECHPARAM = "params";
    public static final String PARAM_IDENTIFICATION = "identification";
    public static final String PARAM_NOTE = "note";

    public static final String VAR_RELATION = "RELATION";

    public static final String ACTION_ADD_ITEM = "addItem";
    public static final String ACTION_ADD_ITEM_STEP2 = "addItem2";
    public static final String ACTION_ADD_ITEM_STEP3 = "addItem3";
    public static final String ACTION_ADD_RECORD = "addRecord";
    public static final String ACTION_ADD_RECORD_STEP2 = "addRecord2";
    public static final String ACTION_EDIT_ITEM = "editItem";
    public static final String ACTION_EDIT_RECORD = "editRecord";


    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Relation relation = null;
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        String action = (String) params.get(AbcServlet.PARAM_ACTION);

        relation = (Relation) instantiateParam(PARAM_RELATION,Relation.class,params);
        if ( relation!=null ) {
            relation = (Relation) persistance.findById(relation);
            persistance.synchronize(relation.getChild());
            ctx.put(VAR_RELATION,relation);
        } else throw new Exception("Chyb� parametr relationId!");

        if ( action==null || action.equals(ACTION_ADD_ITEM) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation.getChild(),Guard.OPERATION_ADD,Item.class);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return getTemplate("login.vm");
                case Guard.ACCESS_DENIED: addError(AbcServlet.GENERIC_ERROR,"Va�e pr�va nejsou dostate�n� pro tuto operaci!",ctx, null);
                default: return getTemplate("add/item.vm");
            }

        } else if ( action.equals(ACTION_ADD_ITEM_STEP2) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation.getChild(),Guard.OPERATION_ADD,Item.class);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return getTemplate("login.vm");
                case Guard.ACCESS_DENIED: {
                    addError(AbcServlet.GENERIC_ERROR,"Va�e pr�va nejsou dostate�n� pro tuto operaci!",ctx, null);
                    return getTemplate("add/item.vm");
                }
                default: return actionAddStep2(request,ctx);
            }

        } else if ( action.equals(ACTION_ADD_ITEM_STEP3) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation.getChild(),Guard.OPERATION_ADD,Item.class);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return getTemplate("login.vm");
                case Guard.ACCESS_DENIED: {
                    addError(AbcServlet.GENERIC_ERROR,"Va�e pr�va nejsou dostate�n� pro tuto operaci!",ctx, null);
                    return getTemplate("add/item.vm");
                }
                default: return actionAddStep3(request,response,ctx);
            }

        } else if ( action.equals(ACTION_ADD_RECORD) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation.getChild(),Guard.OPERATION_ADD,Record.class);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return getTemplate("login.vm");
                case Guard.ACCESS_DENIED: addError(AbcServlet.GENERIC_ERROR,"Va�e pr�va nejsou dostate�n� pro tuto operaci!",ctx, null);
                default: {
                    params.put(PARAM_ACTION,ACTION_ADD_RECORD_STEP2);
                    return getTemplate("add/hwrecord.vm");
                }
            }

        } else if ( action.equals(ACTION_ADD_RECORD_STEP2) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation.getChild(),Guard.OPERATION_ADD,Record.class);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return getTemplate("login.vm");
                case Guard.ACCESS_DENIED: addError(AbcServlet.GENERIC_ERROR,"Va�e pr�va nejsou dostate�n� pro tuto operaci!",ctx, null);
                default: return actionAddRecord(request,response,ctx);
            }
        }

        return getTemplate("add/item.vm");
    }

    protected Template actionAddStep2(HttpServletRequest request, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);

        String name = (String) params.get(PARAM_NAME);
        if ( name==null || name.length()==0 ) {
            addError(PARAM_NAME,"Nevyplnil jste n�zev druhu!",ctx,null);
            return getTemplate("add/item.vm");
        }
        return getTemplate("add/hwrecord.vm");
    }

    protected Template actionAddStep3(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation upper = (Relation) ctx.get(VAR_RELATION);
        User user = (User) ctx.get(AbcServlet.VAR_USER);

        String name = (String) params.get(PARAM_NAME);
        if ( name==null || name.length()==0 ) {
            addError(PARAM_NAME,"Nevyplnil jste n�zev druhu!",ctx,null);
            return getTemplate("add/item.vm");
        }
        String icon = (String) params.get(PARAM_ICON);

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        root.addElement("name").addText(name);
        if ( icon!=null && icon.length()>0 ) root.addElement("icon").addText(icon);

        Item item = new Item(0,Item.MAKE);
        item.setData(document);
        item.setOwner(user.getId());

        String driver = (String) params.get(PARAM_DRIVER);
        String price = (String) params.get(PARAM_PRICE);
        String setup = (String) params.get(PARAM_SETUP);
        String tech = (String) params.get(PARAM_TECHPARAM);
        String note = (String) params.get(PARAM_NOTE);
        String identification = (String) params.get(PARAM_IDENTIFICATION);

        if ( (note==null || note.length()==0) && (setup==null || setup.length()==0) ) {
            addError(PARAM_SETUP,"Vypl�te postup zprovozn�n� nebo pozn�mku!",ctx,null);
            return getTemplate("add/hwrecord.vm");
        }

        document = DocumentHelper.createDocument();
        root = document.addElement("data");
        if ( price!=null && price.length()>0 ) root.addElement("price").addText(price);
        if ( driver!=null && driver.length()>0 ) root.addElement("driver").addText(driver);
        if ( setup!=null && setup.length()>0 ) root.addElement("setup").addText(setup);
        if ( tech!=null && tech.length()>0 ) root.addElement("params").addText(tech);
        if ( identification!=null && identification.length()>0 ) root.addElement("identification").addText(identification);
        if ( note!=null && note.length()>0 ) root.addElement("note").addText(note);

        Record record = new Record(0,Record.HARDWARE);
        record.setData(document);
        record.setOwner(user.getId());

        try {
            persistance.create(item);
            Relation relation = new Relation(upper.getChild(),item,upper.getId());
            persistance.create(relation);
            persistance.create(record);
            persistance.create(new Relation(item,record,relation.getId()));

            redirect("/ViewRelation?relationId="+relation.getId(),response,ctx);
            return null;
        } catch (PersistanceException e) {
            addError(AbcServlet.GENERIC_ERROR,e.getMessage(),ctx, null);
            return getTemplate("add/hwrecord.vm");
        }
    }

    protected Template actionAddRecord(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation upper = (Relation) ctx.get(VAR_RELATION);
        User user = (User) ctx.get(AbcServlet.VAR_USER);

        String driver = (String) params.get(PARAM_DRIVER);
        String price = (String) params.get(PARAM_PRICE);
        String setup = (String) params.get(PARAM_SETUP);
        String tech = (String) params.get(PARAM_TECHPARAM);
        String note = (String) params.get(PARAM_NOTE);
        String identification = (String) params.get(PARAM_IDENTIFICATION);

        if ( (note==null || note.length()==0) && (setup==null || setup.length()==0) ) {
            addError(PARAM_SETUP,"Vypl�te postup zprovozn�n� nebo pozn�mku!",ctx,null);
            return getTemplate("add/hwrecord.vm");
        }

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        if ( price!=null && price.length()>0 ) root.addElement("price").addText(price);
        if ( driver!=null && driver.length()>0 ) root.addElement("driver").addText(driver);
        if ( setup!=null && setup.length()>0 ) root.addElement("setup").addText(setup);
        if ( tech!=null && tech.length()>0 ) root.addElement("params").addText(tech);
        if ( identification!=null && identification.length()>0 ) root.addElement("identification").addText(identification);
        if ( note!=null && note.length()>0 ) root.addElement("note").addText(note);

        Record record = new Record(0,Record.HARDWARE);
        record.setData(document);
        record.setOwner(user.getId());

        try {
            persistance.create(record);
            Relation relation = new Relation(upper.getChild(),record,upper.getId());
            persistance.create(relation);

            redirect("/ViewRelation?relationId="+relation.getId(),response,ctx);
            return null;
        } catch (PersistanceException e) {
            addError(AbcServlet.GENERIC_ERROR,e.getMessage(),ctx, null);
            return getTemplate("add/hwrecord.vm");
        }
    }
}
