/*
 * User: literakl
 * Date: Jan 3, 2002
 * Time: 8:14:00 AM
 */
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.html.edit.EditNews;
import cz.abclinuxu.servlets.html.edit.EditRelation;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.data.view.Comment;
import cz.abclinuxu.data.view.ACL;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.news.NewsCategories;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.security.Roles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Element;
import org.dom4j.Document;

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
 * <u>Parameters used by ShowObject</u>
 * <dl>
 * <dt>relationId</dt>
 * <dd>PK of asked relation, number.</dd>
 * </dl>
 */
public class ShowObject implements AbcAction {
    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_DISCUSSION = "dizId";
    public static final String PARAM_THREAD = "threadId";

    public static final String ACTION_SHOW_CENSORED = "censored";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_PARENTS = "PARENTS";
    public static final String VAR_ITEM = "ITEM";
    /** Relation upper to selected relation Item-Record */
    public static final String VAR_UPPER = "REL_ITEM";
    /** children relation of Item, grouped by their type */
    public static final String VAR_CHILDREN_MAP = "CHILDREN";
    public static final String VAR_THREAD = "THREAD";

    Persistance persistance = PersistanceFactory.getPersistance();

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);

        if ( ACTION_SHOW_CENSORED.equals(action))
            return processCensored(request,env);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if ( relation==null ) {
            throw new MissingArgumentException("Parametr relationId je pr�zdn�!");
        }

        Tools.sync(relation);
        env.put(VAR_RELATION,relation);

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

        List parents = persistance.findParents(relation);
        env.put(VAR_PARENTS, parents);

        if ( (relation.getParent() instanceof Item) || ( relation.getChild() instanceof Item ) )
            return processItem(env, relation, request, response);
        else if ( relation.getChild() instanceof Poll )
            return ViewPolls.processPoll(env, relation, request, response);
        else if ( relation.getParent() instanceof Category ) {
            return ViewCategory.processCategory(request,response,env,relation);
        }
        return null; // todo log object
    }

    /**
     * Processes item - like article, discussion, driver etc.
     * @return template to be rendered
     */
    String processItem(Map env, Relation relation, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Item item = null;
        Record record = null;
        Relation upper = null;

        if ( relation.getChild() instanceof Item ) {
            item = (Item) relation.getChild();
            upper = relation;
        } else if ( relation.getParent() instanceof Item ) {
            item = (Item) relation.getParent();
            upper = new Relation(relation.getUpper());
            record = (Record) relation.getChild();
        }

        Tools.sync(item);
        env.put(VAR_ITEM, item);
        Map children = Tools.groupByType(item.getChildren());
        env.put(VAR_CHILDREN_MAP, children);
        Tools.sync(upper);
        env.put(VAR_UPPER, upper);

        if ( item.getType()==Item.ARTICLE )
            return ShowArticle.show(env, item, request);

        if ( item.getType()==Item.NEWS ) {
            env.put(EditNews.VAR_CATEGORY, NewsCategories.get(item.getSubType()));
            return FMTemplateSelector.select("ShowObject", "news", env, request);
        }

        if ( item.getType()==Item.DISCUSSION )
            return FMTemplateSelector.select("ShowObject","discussion",env, request);

        if ( item.getType()==Item.CONTENT )
            return ViewContent.show(request, response, env);

        if ( item.getType()==Item.DRIVER )
            return FMTemplateSelector.select("ShowObject","driver",env, request);

        if ( record==null ) {
            List records = (List) children.get(Constants.TYPE_RECORD);
            if ( records!=null && records.size()>0 )
                record = (Record) ((Relation)records.get(0)).getChild();
        }

        if ( item.getType()==Item.MAKE && record!=null ) {
            if ( ! record.isInitialized() )
                persistance.synchronize(record);
            switch ( record.getType() ) {
                case Record.HARDWARE: return FMTemplateSelector.select("ShowObject","hardware",env, request);
                case Record.SOFTWARE: return FMTemplateSelector.select("ShowObject","software",env, request);
            }
        }
        return null;
    }

    /**
     * Displays thread in discussion, that was marked as censored.
     */
    String processCensored(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Item diz = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params, request);
        if (diz==null)
            throw new MissingArgumentException("Chyb� parametr "+PARAM_DISCUSSION+"!");
        diz = (Item) persistance.findById(diz);

        List children = diz.getChildren();
        Record record = (Record) ((Relation)children.get(0)).getChild();
        persistance.synchronize(record);

        String thread = (String) params.get(PARAM_THREAD);
        if (thread==null)
            throw new MissingArgumentException("Chyb� parametr "+PARAM_THREAD+"!");

        String xpath = "//comment[@id='"+thread+"']";
        Element element = (Element) record.getData().selectSingleNode(xpath);
        env.put(VAR_THREAD, new Comment(element));

        return FMTemplateSelector.select("ShowObject", "censored", env, request);
    }
}
