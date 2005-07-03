/*
 * User: literakl
 * Date: Jan 3, 2002
 * Time: 8:42:06 AM
 */
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.data.*;
import cz.abclinuxu.data.view.ACL;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.extra.Qualifier;
import cz.abclinuxu.persistance.extra.LimitQualifier;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.html.edit.EditRelation;
import cz.abclinuxu.servlets.html.edit.EditRequest;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.security.Roles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;


/**
 * Servlet, which loads Category specified by parameter <code>categoryId</code> (or
 * by relation.getChild() from Context) and displays the result.<p>
 * <u>Context variables introduced by AbcVelocityServlet</u>
 * <dl>
 * <dt><code>VAR_CATEGORY</code></dt>
 * <dd>instance of Category.</dd>
 * </dl>
 * <u>Parameters used by ShowObject</u>
 * <dl>
 * <dt>PARAM_CATEGORY_ID</dt>
 * <dd>PK of asked Category, number.</dd>
 * <dt>PARAM_FROM</dt>
 * <dd>used by clanky.vm. Defines range of shown objects.</dd>
 * </dl>
 */
public class ViewCategory implements AbcAction {
    /** if set, it indicates to display parent in the relation of two categories */
    public static final String PARAM_PARENT = "parent";
    public static final String PARAM_RELATION_ID = "relationId";
    public static final String PARAM_RELATION_ID_SHORT = "rid";
    /** n-th oldest object, where to display from */
    public static final String PARAM_FROM = "from";
    /** how many object to display */
    public static final String PARAM_COUNT = "count";
    /** holds category to be displayed */
    public static final String VAR_CATEGORY = "CATEGORY";
    public static final String VAR_CHILDREN = "CHILDREN";
    /** holds list of articles */
    public static final String VAR_ARTICLES = "ARTICLES";

    static Persistance persistance = PersistanceFactory.getPersistance();

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_ID_SHORT, Relation.class, params, request);
        if ( relation==null ) {
            throw new MissingArgumentException("Parametr relationId je prázdný!");
        }

        Tools.sync(relation);
        env.put(ShowObject.VAR_RELATION,relation);
        List parents = persistance.findParents(relation);
        env.put(ShowObject.VAR_PARENTS,parents);

        // check ACL
        Document document = relation.getData();
        if ( document!=null && document.selectSingleNode("/data/acl")!=null ) {
            User user = (User) env.get(Constants.VAR_USER);
            if ( user==null )
                return FMTemplateSelector.select("ViewUser", "login", env, request);

            if ( !user.hasRole(Roles.ROOT) ) {
                List elements = document.selectNodes("/data/acl");
                List acls = new ArrayList(elements.size());
                for ( Iterator iter = elements.iterator(); iter.hasNext(); ) {
                    ACL acl = EditRelation.getACL((Element) iter.next());
                    acls.add(acl);
                }
                if ( !ACL.isGranted(user, ACL.RIGHT_READ, acls) )
                    return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            }
        }

        return processCategory(request,response,env,relation);
    }

    /**
     * processes given category
     * @return template to be rendered
     */
    public static String processCategory(HttpServletRequest request, HttpServletResponse response, Map env, Relation relation) throws Exception {
        String tmp = (String) ((Map)env.get(Constants.VAR_PARAMS)).get(PARAM_PARENT);
        GenericObject obj;
        if ( Misc.same(tmp,"yes") )
            obj = relation.getParent();
        else
            obj = relation.getChild();

        Category category = null;
        if ( !(obj instanceof Category) ) {
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/show/"+relation.getId());
            return null;
        } else
            category = (Category) obj;

        Tools.sync(category);
        env.put(VAR_CATEGORY, category);

        if (category.getType()==Category.SECTION)
            return processArticleSection(request, env, relation);

        List children = Tools.syncList(category.getChildren());
        env.put(VAR_CHILDREN, children);

        switch ( relation.getId() ) {
            case Constants.REL_DRIVERS:
                return FMTemplateSelector.select("ViewCategory", "drivers", env, request);
            case Constants.REL_NEWS_POOL:
                return FMTemplateSelector.select("ViewCategory", "waiting_news", env, request);
            case Constants.REL_REQUESTS: {
                env.put(EditRequest.VAR_CATEGORIES, EditRequest.categories);
                return FMTemplateSelector.select("EditRequest", "view", env, request);
            }
            case Constants.REL_DOCUMENTS:
                return FMTemplateSelector.select("ViewCategory", "documents", env, request);
        }
        if ( category.getId()==Constants.CAT_ARTICLES )
                return FMTemplateSelector.select("ViewCategory","rubriky",env, request);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        tmp = urlUtils.getPrefix();
        if ( Misc.same(tmp,UrlUtils.PREFIX_CLANKY) ) {
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                Relation relation1 = (Relation) iter.next();
                Tools.sync(relation1);
                if (!(relation1.getChild() instanceof Item)) {
                    iter.remove();
                    continue;
                }
                if (((Item)relation1.getChild()).getType()!=Item.ARTICLE)
                    iter.remove();
            }
            Paging paging = new Paging(children, 0, children.size(), children.size());
            env.put(VAR_ARTICLES, paging);
            return FMTemplateSelector.select("ViewCategory","rubrika",env, request);
        } else
            return FMTemplateSelector.select("ViewCategory","sekce",env, request);
    }

    public static String processArticleSection(HttpServletRequest request, Map env, Relation relation) throws Exception {
        Category section = (Category) relation.getChild();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int count = AbcConfig.getSectionArticleCount();

        SQLTool sqlTool = SQLTool.getInstance();
        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(from, count)};
        List articles = sqlTool.findArticleRelations(qualifiers, section.getId());
        int total = sqlTool.countArticleRelations(section.getId());
        Tools.syncList(articles);

        Paging paging = new Paging(articles, from, count, total);
        env.put(VAR_ARTICLES, paging);

        return FMTemplateSelector.select("ViewCategory", "rubrika", env, request);
    }
}
