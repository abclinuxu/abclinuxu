/*
 * Created by IntelliJ IDEA.
 * User: literakl
 * Date: Jan 3, 2002
 * Time: 8:14:00 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.Persistance;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import java.util.List;

/**
 * Servlet, which loads Relation specified by parameter <code>relationId</code>
 * and redirects execution to servlet handling one of relation's GenericObjects.<p>
 * <u>Context variables introduced by AbcServlet</u>
 * <dl>
 * <dt><code>VAR_RELATION</code></dt>
 * <dd>instance of Relation.</dd>
 * <dt><code>VAR_PARENTS</code></dt>
 * <dd>List of parental relations. Last element is current relation.</dd>
 * </dl>
 * <u>Parameters used by ViewRelation</u>
 * <dl>
 * <dt>relationId</dt>
 * <dd>PK of asked relation, number.</dd>
 * </dl>
 */
public class ViewRelation extends AbcServlet {
    public static final String PARAM_RELATION_ID = "relationId";
    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_PARENTS = "PARENTS";

    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        validateUserSession(request,response,ctx);

        int pk = Integer.parseInt(request.getParameter(PARAM_RELATION_ID));
        Relation relation = new Relation(pk);
        Persistance persistance = PersistanceFactory.getPersistance();
        relation = (Relation) persistance.findById(relation);
        ctx.put(VAR_RELATION,relation);

        List parents = persistance.findParents(relation);
        ctx.put(VAR_PARENTS,parents);

        if ( relation.getParent() instanceof Item ) return null;// redirect to item
        if ( relation.getParent() instanceof Category ) {
            if ( relation.getChild() instanceof Item ) return null;// redirect to item
            RequestDispatcher dispatcher = request.getRequestDispatcher("/ViewCategory");
            dispatcher.forward(request,response);
        }
        return null;
    }
}
