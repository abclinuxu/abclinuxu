/*
 * User: literakl
 * Date: Jan 15, 2002
 * Time: 9:24:05 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.VariantTool;
import cz.abclinuxu.servlets.view.SelectRelation;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.security.Guard;
import cz.abclinuxu.utils.InstanceUtils;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.dom4j.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;

/**
 * Class for removing relations or creating links.<p>
 * <u>Parameters used by EditRelation</u>
 * <dl>
 * <dt><code>PARAM_RELATION</code></dt>
 * <dd>Id of base relation for this operation.</dd>
 * <dt><code>PARAM_NAME</code></dt>
 * <dd>Name used in new relation, overrides default.</dd>
 * <dt><code>PARAM_PREFIX</code></dt>
 * <dd>Prefix of URL.</dd>
 * </dl>
 */
public class EditRelation extends AbcServlet {
    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_PREFIX = "prefix";

    public static final String VAR_CURRENT = "CURRENT";
    public static final String VAR_PARENTS = "PARENTS";

    public static final String ACTION_LINK = "add";
    public static final String ACTION_LINK_STEP2 = "add2";
    public static final String ACTION_REMOVE = "remove";
    public static final String ACTION_REMOVE_STEP2 = "remove2";
    public static final String ACTION_MOVE = "move";


    protected String process(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        String action = (String) params.get(AbcServlet.PARAM_ACTION);

        Relation relation = (Relation) InstanceUtils.instantiateParam(EditRelation.PARAM_RELATION,Relation.class,params);
        if ( relation!=null ) {
            relation = (Relation) PersistanceFactory.getPersistance().findById(relation);
            ctx.put(EditRelation.VAR_CURRENT,relation);
        }

        if ( action==null || action.equals(ACTION_LINK) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation.getChild(),Guard.OPERATION_ADD,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return VariantTool.selectTemplate(request,ctx,"EditUser","login");
                case Guard.ACCESS_DENIED: ServletUtils.addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx, null);
                default: return VariantTool.selectTemplate(request,ctx,"EditRelation","add");
            }

        } else if ( action.equals(ACTION_LINK_STEP2) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation.getChild(),Guard.OPERATION_ADD,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return VariantTool.selectTemplate(request,ctx,"EditUser","login");
                case Guard.ACCESS_DENIED: {
                    ServletUtils.addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx, null);
                    return VariantTool.selectTemplate(request,ctx,"EditRelation","add");
                }
                default: return actionLinkStep2(request,response,ctx);
            }

        } else if ( action.equals(ACTION_REMOVE) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation,Guard.OPERATION_REMOVE,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return VariantTool.selectTemplate(request,ctx,"EditUser","login");
                case Guard.ACCESS_DENIED: {
                    ServletUtils.addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx, null);
                    return VariantTool.selectTemplate(request,ctx,"EditRelation","remove");
                }
                default: return actionRemove1(request,ctx);
            }

        } else if ( action.equals(ACTION_REMOVE_STEP2) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation,Guard.OPERATION_REMOVE,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return VariantTool.selectTemplate(request,ctx,"EditUser","login");
                case Guard.ACCESS_DENIED: {
                    ServletUtils.addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx, null);
                    return VariantTool.selectTemplate(request,ctx,"EditRelation","remove");
                }
                default: return actionRemove2(request,response,ctx);
            }

        } else if ( action.equals(ACTION_MOVE) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation,Guard.OPERATION_EDIT,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return VariantTool.selectTemplate(request,ctx,"EditUser","login");
                case Guard.ACCESS_DENIED: {
                    ServletUtils.addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx, null);
                    UrlUtils.redirect("/Index",response,ctx);
                    return null;
                }
                default: return actionMove(request,response,ctx);
            }

        }

        UrlUtils.redirect("/Index",response,ctx);
        return null;
    }

    protected String actionLinkStep2(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Relation parent = (Relation) ctx.get(EditRelation.VAR_CURRENT);
        Relation child = (Relation) InstanceUtils.instantiateParam(SelectRelation.PARAM_SELECTED,Relation.class,params);
        persistance.synchronize(child);

        Relation relation = new Relation();
        relation.setParent(parent.getChild());
        relation.setChild(child.getChild());
        relation.setUpper(parent.getId());

        String tmp = (String) params.get(EditRelation.PARAM_NAME);
        if ( tmp!=null && tmp.length()>0 ) {
            Document document = DocumentHelper.createDocument();
            Element root = document.addElement("data");
            root.addElement("name").addText(tmp);
            relation.setData(document);
        }

        persistance.create(relation);

        String prefix = (String)params.get(EditRelation.PARAM_PREFIX);
        ctx.put(AbcServlet.VAR_URL_UTILS,new UrlUtils(prefix, response));
        UrlUtils.redirect("/ViewRelation?relationId="+parent.getId(),response,ctx);
        return null;
    }

    protected String actionRemove1(HttpServletRequest request, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) ctx.get(VAR_CURRENT);

        Relation[] parents = persistance.findByExample(new Relation(null,relation.getChild(),0));
        ctx.put(VAR_PARENTS,parents);
        return VariantTool.selectTemplate(request,ctx,"EditRelation","remove");
    }

    protected String actionRemove2(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) ctx.get(VAR_CURRENT);

        persistance.remove(relation);
        String url = null;
        String prefix = (String) params.get(PARAM_PREFIX);
        if ( prefix!=null ) {
            url = prefix.concat("/ViewRelation?relationId="+relation.getUpper());
        } else url = "/Index";

        UrlUtils.redirect(url,response,ctx);
        return null;
    }

    /**
     * Called, when user selects destination in SelectRelation. It replaces parent in relation with child
     * in destination.
     */
    protected String actionMove(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) ctx.get(VAR_CURRENT);
        Relation destination = (Relation) InstanceUtils.instantiateParam(SelectRelation.PARAM_SELECTED,Relation.class,params);

        persistance.synchronize(destination);
        relation.setParent(destination.getChild());
        relation.setUpper(destination.getId());
        persistance.update(relation);

        String url = null;
        String prefix = (String) params.get(PARAM_PREFIX);
        if ( prefix!=null ) {
            url = prefix.concat("/ViewRelation?relationId="+relation.getUpper());
        } else url = "/Index";

        UrlUtils.redirect(url,response,ctx);
        return null;
    }
}
