/*
 * User: literakl
 * Date: Jan 15, 2002
 * Time: 9:24:05 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.view.SelectRelation;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Tools;
import cz.abclinuxu.exceptions.MissingArgumentException;

import org.dom4j.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Class for removing relations or creating links.
 */
public class EditRelation extends AbcFMServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditRelation.class);

    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_PREFIX = "prefix";
    public static final String PARAM_SELECTED = SelectRelation.PARAM_SELECTED;

    public static final String VAR_CURRENT = "CURRENT";
    public static final String VAR_SELECTED = "SELECTED";
    public static final String VAR_PARENTS = "PARENTS";

    public static final String ACTION_LINK = "add";
    public static final String ACTION_LINK_STEP2 = "add2";
    public static final String ACTION_REMOVE = "remove";
    public static final String ACTION_REMOVE_STEP2 = "remove2";
    public static final String ACTION_MOVE = "move";


    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        String action = (String) params.get(PARAM_ACTION);
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT,PARAM_RELATION,Relation.class,params);
        relation = (Relation) persistance.findById(relation);
        env.put(VAR_CURRENT, relation);

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if ( action.equals(ACTION_LINK) ) {
            if ( !user.hasRole(Roles.CATEGORY_ADMIN) )
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return actionLinkStep1(request,env);
        }

        if ( action.equals(ACTION_LINK_STEP2) ) {
            if ( !user.hasRole(Roles.CATEGORY_ADMIN) )
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return actionLinkStep2(request, response, env);
        }

        GenericObject child = relation.getChild();
        persistance.synchronize(child);

        if ( action.equals(ACTION_MOVE) ) {
            // check permissions
            boolean canMove = false;
            canMove |= user.hasRole(Roles.CAN_MOVE_RELATION);
            if (child instanceof Category )
                canMove |= user.hasRole(Roles.CATEGORY_ADMIN);
            if (child instanceof Item ) {
                switch ( ((Item)child).getType() ) {
                    case Item.DISCUSSION:
                        canMove |= user.hasRole(Roles.DISCUSSION_ADMIN); break;
                    case Item.ARTICLE:
                        canMove |= user.hasRole(Roles.ARTICLE_ADMIN); break;
                }
            }

            if ( !canMove )
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

            return actionMove(request, response, env);
        }

        // check permissions
        boolean canRemove = false;
        canRemove |= user.hasRole(Roles.CAN_REMOVE_RELATION);
        if ( child instanceof Category )
            canRemove |= user.hasRole(Roles.CATEGORY_ADMIN);
        if ( child instanceof Item ) {
            switch ( ((Item) child).getType() ) {
                case Item.DISCUSSION:
                    canRemove |= user.hasRole(Roles.DISCUSSION_ADMIN); break;
                case Item.ARTICLE:
                    canRemove |= user.hasRole(Roles.ARTICLE_ADMIN); break;
                case Item.SURVEY:
                    canRemove |= user.hasRole(Roles.SURVEY_ADMIN); break;
            }
        }
        if ( child instanceof Poll )
            canRemove |= user.hasRole(Roles.POLL_ADMIN);

        if ( !canRemove )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if ( action.equals(ACTION_REMOVE) ) {
            return actionRemove1(request, env);

        } else if ( action.equals(ACTION_REMOVE_STEP2) ) {
            return actionRemove2(request, response, env);
        }

        throw new MissingArgumentException("Chybí parametr action!");
    }

    protected String actionLinkStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_SELECTED,Relation.class,params);
        if ( relation!=null ) {
            relation = (Relation) persistance.findById(relation);
            env.put(VAR_SELECTED,relation);
        }
        return FMTemplateSelector.select("EditRelation","add",env,request);
    }

    protected String actionLinkStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Relation parent = (Relation) env.get(VAR_CURRENT);
        Relation child = (Relation) InstanceUtils.instantiateParam(PARAM_SELECTED,Relation.class,params);
        persistance.synchronize(child);

        Relation relation = new Relation();
        relation.setParent(parent.getChild());
        relation.setChild(child.getChild());
        relation.setUpper(parent.getId());

        String tmp = (String) params.get(PARAM_NAME);
        if ( tmp!=null && tmp.length()>0 ) {
            Document document = DocumentHelper.createDocument();
            Element root = document.addElement("data");
            root.addElement("name").addText(tmp);
            relation.setData(document);
        }

        persistance.create(relation);

        String prefix = (String)params.get(PARAM_PREFIX);
        UrlUtils urlUtils = new UrlUtils(prefix, response);
        urlUtils.redirect(response, "/ViewRelation?rid="+parent.getId());
        return null;
    }

    protected String actionRemove1(HttpServletRequest request, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_CURRENT);

        Relation[] parents = persistance.findByExample(new Relation(null,relation.getChild(),0));
        env.put(VAR_PARENTS,parents);
        return FMTemplateSelector.select("EditRelation","remove",env,request);
    }

    protected String actionRemove2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_CURRENT);
        User user = (User) env.get(Constants.VAR_USER);

        persistance.synchronize(relation);
        persistance.synchronize(relation.getChild());
        AdminLogger.logEvent(user,"remove | relation "+relation.getId()+" | "+Tools.childName(relation));
        persistance.remove(relation);

        String url = null;
        String prefix = (String) params.get(PARAM_PREFIX);
        if ( prefix!=null ) {
            url = prefix.concat("/ViewRelation?rid="+relation.getUpper());
        } else url = "/Index";

        UrlUtils urlUtils = new UrlUtils(prefix, response);
        urlUtils.redirect(response, url);
        return null;
    }

    /**
     * Called, when user selects destination in SelectRelation. It replaces parent in relation with child
     * in destination.
     */
    protected String actionMove(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(VAR_CURRENT);
        int originalUpper = relation.getUpper();
        Relation destination = (Relation) InstanceUtils.instantiateParam(PARAM_SELECTED,Relation.class,params);
        persistance.synchronize(destination);

        relation.setParent(destination.getChild());
        relation.setUpper(destination.getId());
        persistance.update(relation);

        AdminLogger.logEvent(user, "  move | relation "+relation.getId()+" | from "+originalUpper+" | to "+destination.getId());

        String url = null;
        String prefix = (String) params.get(PARAM_PREFIX);
        if ( prefix!=null ) {
            if ( originalUpper==Constants.REL_FORUM && returnBackToForum(user) )
                url = "/diskuse.jsp";
            else
                url = prefix.concat("/ViewRelation?rid="+relation.getUpper());
        } else url = "/Index";

        UrlUtils urlUtils = new UrlUtils("", response);
        urlUtils.redirect(response, url);
        return null;
    }

    /**
     * Whether user wishes to be redirected after move of discussions
     * from discussion forum back to the forum.
     * @param user
     * @return
     */
    private static boolean returnBackToForum(User user) {
        Node node = user.getData().selectSingleNode("/data/settings/return_to_forum");
        if ( node!=null )
            return "yes".equals(node.getText());
        return false;
    }
}
