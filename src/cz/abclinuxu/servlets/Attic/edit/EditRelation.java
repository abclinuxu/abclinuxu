/*
 * User: literakl
 * Date: Jan 15, 2002
 * Time: 9:24:05 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.persistance.PersistanceFactory;
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
 * <dt><code>PARAM_LINKED</code></dt>
 * <dd>Id of relation, whose child will be linked as children of PARAM_RELATION.getChild().</dd>
 * </dl>
 */
public class EditRelation extends AbcServlet {
    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_LINKED = "linkedId";

    public static final String ACTION_LINK = "add";
    public static final String ACTION_LINK_STEP2 = "add2";
    public static final String ACTION_LINK_STEP3 = "add3";
    public static final String ACTION_REMOVE = "rm";
    public static final String ACTION_REMOVE_STEP2 = "rm2";


    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        String action = (String) params.get(AbcServlet.PARAM_ACTION);
        String tmp = (String) params.get(EditRelation.PARAM_RELATION);
        int relationId = Integer.parseInt(tmp);
        Relation relation = (Relation) PersistanceFactory.getPersistance().findById(new Relation(relationId));

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
        return null;
    }
}
