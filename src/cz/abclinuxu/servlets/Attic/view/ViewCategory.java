/*
 * Created by IntelliJ IDEA.
 * User: literakl
 * Date: Jan 3, 2002
 * Time: 8:42:06 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Tools;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.exceptions.MissingArgumentException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;


/**
 * Servlet, which loads Category specified by parameter <code>categoryId</code> (or
 * by relation.getChild() from Context) and displays the result.<p>
 * <u>Context variables introduced by AbcVelocityServlet</u>
 * <dl>
 * <dt><code>VAR_CATEGORY</code></dt>
 * <dd>instance of Category.</dd>
 * </dl>
 * <u>Parameters used by ViewRelation</u>
 * <dl>
 * <dt>PARAM_CATEGORY_ID</dt>
 * <dd>PK of asked Category, number.</dd>
 * <dt>PARAM_FROM</dt>
 * <dd>used by clanky.vm. Defines range of shown objects.</dd>
 * </dl>
 */
public class ViewCategory extends AbcFMServlet {
    /** if set, it indicates to display parent in the relation of two categories */
    public static final String PARAM_PARENT = "parent";
    public static final String PARAM_RELATION_ID = "relationId";
    public static final String PARAM_RELATION_ID_SHORT = "rid";
    /** holds category to be displayed */
    public static final String VAR_CATEGORY = "CATEGORY";

    static Persistance persistance = PersistanceFactory.getPersistance();

    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_ID_SHORT,PARAM_RELATION_ID,Relation.class,params);
        if ( relation==null ) {
            throw new MissingArgumentException("Parametr relationId je prázdný!");
        }

        Tools.sync(relation);
        env.put(ViewRelation.VAR_RELATION,relation);
        List parents = persistance.findParents(relation);
        env.put(ViewRelation.VAR_PARENTS,parents);

        return processCategory(request,response,env,relation,parents);
    }

    /**
     * processes given category
     * @return template to be rendered
     */
    public static String processCategory(HttpServletRequest request, HttpServletResponse response, Map env, Relation relation, List parents) throws Exception {
        if ( parents!=null ) parents.add(relation);
        Category category = null;

        String tmp = (String) ((Map)env.get(Constants.VAR_PARAMS)).get(PARAM_PARENT);
        GenericObject obj;
        if ( Misc.same(tmp,"yes") )
            obj = relation.getParent();
        else
            obj = relation.getChild();
        if ( !(obj instanceof Category) ) {
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/ViewRelation?rid="+relation.getId());
            return null;
        } else
            category = (Category) obj;

        Tools.sync(category);
        Tools.sync(category.getContent());
        env.put(VAR_CATEGORY,category);

        if ( Misc.same(tmp,"yes") ) {
            switch ( category.getId() ) {
                case Constants.CAT_ARTICLES:
                case Constants.CAT_ABC: return FMTemplateSelector.select("ViewCategory","rubriky",env, request);
            }
        } else {
            switch (relation.getId()) {
                case Constants.REL_POLLS: return FMTemplateSelector.select("ViewCategory","ankety",env, request);
                case Constants.REL_DRIVERS: return FMTemplateSelector.select("ViewCategory","drivers",env, request);
                case Constants.REL_NEWS_POOL: return FMTemplateSelector.select("ViewCategory","news",env, request);
                case Constants.REL_REQUESTS: return FMTemplateSelector.select("EditRequest","view",env, request);
            }
        }

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        tmp = urlUtils.getPrefix();
        if ( Misc.same(tmp,UrlUtils.PREFIX_CLANKY) )
            return FMTemplateSelector.select("ViewCategory","rubrika",env, request);
        else
            return FMTemplateSelector.select("ViewCategory","sekce",env, request);
    }
}
