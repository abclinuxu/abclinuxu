/*
 * User: literakl
 * Date: Feb 6, 2002
 * Time: 6:32:36 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.data.*;
import cz.abclinuxu.security.Guard;
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.utils.InstanceUtils;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.dom4j.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.io.IOException;

/**
 * @todo archive drivers replaced by newer version
 */
public class EditDriver extends AbcServlet {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EditDriver.class);

    public static final String PARAM_NAME = "name";
    public static final String PARAM_VERSION = "version";
    public static final String PARAM_URL = "url";
    public static final String PARAM_NOTE = "note";
    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_DRIVER = "driverId";

    public static final String VAR_DRIVER = "driver";
    public static final String VAR_RELATION = "relation";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";

    protected String process(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        String action = (String) params.get(AbcServlet.PARAM_ACTION);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION,Relation.class,params);

        if ( relation!=null ) {
            persistance.synchronize(relation);
            persistance.synchronize(relation.getChild());
            ctx.put(VAR_RELATION,relation);
        } else throw new Exception("Chybí parametr relationId!");

        if ( action==null || action.equals(ACTION_ADD) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation.getChild(),Guard.OPERATION_ADD,Item.class);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return VariantTool.selectTemplate(request,ctx,"EditUser","login");
                case Guard.ACCESS_DENIED: return VariantTool.selectTemplate(request,ctx,"EditUser","forbidden");
                default: return actionAddStep(request,ctx);
            }

        } else if ( action.equals(ACTION_ADD_STEP2) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation.getChild(),Guard.OPERATION_ADD,Item.class);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return VariantTool.selectTemplate(request,ctx,"EditUser","login");
                case Guard.ACCESS_DENIED: return VariantTool.selectTemplate(request,ctx,"EditUser","forbidden");
                default: return actionAddStep2(request,response,ctx);
            }
        }

        return VariantTool.selectTemplate(request,ctx,"EditDriver","add");
    }

    protected String actionAddStep(HttpServletRequest request, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        VelocityHelper helper = (VelocityHelper) ctx.get(AbcServlet.VAR_HELPER);
        Persistance persistance = PersistanceFactory.getPersistance();

        Item driver = (Item) InstanceUtils.instantiateParam(PARAM_DRIVER,Item.class,params);

        if ( driver!=null ) {
            try {
                persistance.synchronize(driver);
            } catch (PersistanceException e) {
                log.warn("Driver doesn't exist, creating new one.",e);
                params.remove(PARAM_DRIVER);
                driver = null;
            }
        }

        if ( driver!=null ) {
            Document document = driver.getData();
            Node node = document.selectSingleNode("data/name");
            if ( node!=null ) params.put(PARAM_NAME,node.getText());
            node = document.selectSingleNode("data/version");
            if ( node!=null ) params.put(PARAM_VERSION,node.getText());
            node = document.selectSingleNode("data/url");
            if ( node!=null ) params.put(PARAM_URL,node.getText());
            node = document.selectSingleNode("data/note");
            if ( node!=null ) params.put(PARAM_NOTE,helper.encodeSpecial(node.getText()));
        }

        return VariantTool.selectTemplate(request,ctx,"EditDriver","add");
    }

    /**
     * add: if driver exists, its content is replaced by newer version. otherwise it is created.
     */
    protected String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Relation upper = (Relation) ctx.get(VAR_RELATION);
        User user = (User) ctx.get(AbcServlet.VAR_USER);

        boolean error = false;
        String name = (String) params.get(PARAM_NAME);
        if ( name==null || name.length()==0 ) {
            ServletUtils.addError(PARAM_NAME,"Nevyplnil jste název ovladaèe!",ctx,null);
            error = true;
        }
        String version = (String) params.get(PARAM_VERSION);
        if ( version==null || version.length()==0 ) {
            ServletUtils.addError(PARAM_VERSION,"Nevyplnil jste verzi ovladaèe!",ctx,null);
            error = true;
        }
        String url = (String) params.get(PARAM_URL);
        if ( url==null || url.length()==0 ) {
            ServletUtils.addError(PARAM_URL,"Nevyplnil jste adresu ovladaèe!",ctx,null);
            error = true;
        } else if ( url.indexOf("tp://")==-1 || url.length()<12 ) {
            ServletUtils.addError(PARAM_URL,"Neplatná adresa ovladaèe!",ctx,null);
            error = true;
        }
        String note = (String) params.get(PARAM_NOTE);

        if ( error ) {
            return VariantTool.selectTemplate(request,ctx,"EditDriver","add");
        }

        boolean created = true;
        Item driver = (Item) InstanceUtils.instantiateParam(PARAM_DRIVER,Item.class,params);
        Document document = null;

        if ( driver!=null ) {
            try {
                persistance.synchronize(driver);
                document = driver.getData();
                created = false;
            } catch (PersistanceException e) {
                log.warn("Driver doesn't exist, creating new one.",e);
                driver = null;
            }
        }

        if ( driver==null ) {
            driver = new Item(0,Item.DRIVER);
            document = DocumentHelper.createDocument();
        }

        DocumentHelper.makeElement(document,"data/name").setText(name);
        DocumentHelper.makeElement(document,"data/version").setText(version);
        DocumentHelper.makeElement(document,"data/url").setText(url);
        if ( note!=null && note.length()>0 ) {
            DocumentHelper.makeElement(document,"data/note").setText(note);
        }

        driver.setData(document);
        driver.setOwner(user.getId());

        try {
            if ( created ) {
                persistance.create(driver);
                Relation relation = new Relation(upper.getChild(),driver,upper.getId());
                persistance.create(relation);
            } else {
                persistance.update(driver);
            }
        } catch (PersistanceException e) {
            ServletUtils.addError(AbcServlet.GENERIC_ERROR,e.getMessage(),ctx, null);
            return VariantTool.selectTemplate(request,ctx,"EditDriver","add");
        }

        UrlUtils.redirect("/ViewRelation?relationId="+Constants.REL_DRIVERS,response,ctx);
        return null;
    }
}
