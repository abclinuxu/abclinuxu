/*
 * Created by IntelliJ IDEA.
 * User: literakl
 * Date: Jan 3, 2002
 * Time: 8:14:00 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcVelocityServlet;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.AbcException;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import java.util.List;
import java.util.Map;

/**
 * Servlet, which loads Relation specified by parameter <code>relationId</code>
 * and redirects execution to servlet handling one of relation's GenericObjects.<p>
 * <u>Context variables introduced by AbcVelocityServlet</u>
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
public class ViewRelation extends AbcVelocityServlet {
    public static final String PARAM_RELATION_ID = "relationId";
    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_PARENTS = "PARENTS";

    protected String process(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Persistance persistance = PersistanceFactory.getPersistance();
        Map params = (Map) request.getAttribute(AbcVelocityServlet.ATTRIB_PARAMS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_ID,Relation.class,params);
        persistance.synchronize(relation);
        ctx.put(VAR_RELATION,relation);

        List parents = null;
        if ( relation==null ) {
            throw new AbcException("Relation is null!",AbcException.MISSING_ARGUMENT);
        }

        parents = persistance.findParents(relation);
        ctx.put(VAR_PARENTS,parents);

        if ( relation.getParent() instanceof Item ) {
            UrlUtils.dispatch(request, response, "/ViewItem");
            return null;
        }
        if ( relation.getParent() instanceof Category ) {
            if ( relation.getChild() instanceof Item ) {
                UrlUtils.dispatch(request, response, "/ViewItem");
                return null;
            }
            // redirect to category otherwise
            UrlUtils.dispatch(request, response, "/ViewCategory");
        }
        return null;
    }
}
