/*
 * User: literakl
 * Date: Jan 15, 2002
 * Time: 9:24:05 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.servlets.view.SelectRelation;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.Persistance;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

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

    public static final String ACTION_LINK = "add";
    public static final String ACTION_LINK_STEP2 = "add2";
    public static final String ACTION_REMOVE = "rm";
    public static final String ACTION_REMOVE_STEP2 = "rm2";


    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        String action = (String) params.get(AbcServlet.PARAM_ACTION);

        String tmp = (String) params.get(EditRelation.PARAM_RELATION);
        int relationId = Integer.parseInt(tmp);
        Relation relation = (Relation) PersistanceFactory.getPersistance().findById(new Relation(relationId));
        ctx.put(EditRelation.VAR_CURRENT,relation);

        if ( action==null || action.equals(EditRelation.ACTION_LINK) ) {
            int rights = checkAccess(relation.getChild(),AbcServlet.METHOD_ADD,ctx);
            switch (rights) {
                case AbcServlet.LOGIN_REQUIRED: return getTemplate("login.vm");
                case AbcServlet.USER_INSUFFICIENT_RIGHTS: addErrorMessage(AbcServlet.GENERIC_ERROR,"Vase prava nejsou dostatecna pro tuto operaci!",ctx);
                default: return getTemplate("add/relation.vm");
            }

        } else if ( action.equals(EditCategory.ACTION_ADD_STEP2) ) {
            int rights = checkAccess(relation.getChild(),AbcServlet.METHOD_ADD,ctx);
            switch (rights) {
                case AbcServlet.LOGIN_REQUIRED: return getTemplate("login.vm");
                case AbcServlet.USER_INSUFFICIENT_RIGHTS: {
                    addErrorMessage(AbcServlet.GENERIC_ERROR,"Vase prava nejsou dostatecna pro tuto operaci!",ctx);
                    return getTemplate("add/relation.vm");
                }
                default: return actionLinkStep2(request,response,ctx);
            }
        }
        return getTemplate("add/category.vm");
    }

    protected Template actionLinkStep2(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        String tmp = (String) params.get(EditRelation.PARAM_RELATION);
        int relationId = Integer.parseInt(tmp);
        Relation parent = (Relation) persistance.findById(new Relation(relationId));

        tmp = (String) params.get(SelectRelation.PARAM_SELECTED);
        relationId = Integer.parseInt(tmp);
        Relation child = (Relation) persistance.findById(new Relation(relationId));

        Relation relation = new Relation();
        relation.setParent(parent.getChild());
        relation.setChild(child.getChild());
        relation.setUpper(parent.getId());

        tmp = (String) params.get(EditRelation.PARAM_NAME);
        if ( tmp!=null && tmp.length()>0 ) relation.setName(tmp);
        persistance.create(relation);

        String prefix = (String)params.get(EditRelation.PARAM_PREFIX);
        ctx.put(AbcServlet.VAR_URL_UTILS,new UrlUtils(prefix, response));
        redirect("/ViewRelation?relationId="+parent.getId(),response,ctx);
        return null;
    }
}
