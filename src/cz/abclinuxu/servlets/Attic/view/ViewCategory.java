/*
 * Created by IntelliJ IDEA.
 * User: literakl
 * Date: Jan 3, 2002
 * Time: 8:42:06 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;
import java.util.List;

/**
 * Servlet, which loads Category specified by parameter <code>categoryId</code> (or
 * by relation.getChild() from Context) and displays the result.<p>
 * <u>Context variables introduced by AbcServlet</u>
 * <dl>
 * <dt><code>VAR_CATEGORY</code></dt>
 * <dd>instance of Category.</dd>
 * </dl>
 * <u>Parameters used by ViewRelation</u>
 * <dl>
 * <dt>PARAM_CATEGORY_ID</dt>
 * <dd>PK of asked Category, number.</dd>
 * </dl>
 */
public class ViewCategory extends AbcServlet {
    public static final String VAR_CATEGORY = "CATEGORY";
    public static final String PARAM_CATEGORY_ID = "categoryId";

    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        // find category and store it into Context
        Category category = null;
        Relation relation = (Relation) ctx.get(ViewRelation.VAR_RELATION);
        List parents = (List) ctx.get(ViewRelation.VAR_PARENTS);
        if ( parents!=null && relation!=null ) parents.add(relation);

        Persistance persistance = PersistanceFactory.getPersistance();
        String tmp = request.getParameter(ViewCategory.PARAM_CATEGORY_ID);
        if ( tmp!=null ) {
            category = new Category(Integer.parseInt(tmp));
        } else {
            if ( relation==null ) {
                addError(AbcServlet.GENERIC_ERROR,"Nebyla vybrána ¾ádná kategorie!",ctx, request.getSession());
                response.sendRedirect("/");
                return null;
            }
            category = (Category) relation.getChild();
        }
        category = (Category) persistance.findById(category);
        ctx.put(VAR_CATEGORY,category);

        UrlUtils urlUtils = (UrlUtils) ctx.get(AbcServlet.VAR_URL_UTILS);
        tmp = urlUtils.getPrefix();

        if ( UrlUtils.PREFIX_CLANKY.equals(tmp) ) {
            return getTemplate("view/clanky.vm");
        } else {
            return getTemplate("view/category.vm");
        }
    }
}
