/*
 * User: literakl
 * Date: Jan 15, 2002
 * Time: 9:24:05 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.view.ViewUser;
import cz.abclinuxu.servlets.select.SelectRelation;
import cz.abclinuxu.servlets.select.SelectUser;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.data.view.ACL;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Tools;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.monitor.*;
import cz.abclinuxu.exceptions.MissingArgumentException;

import org.dom4j.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * Class for removing relations or creating links.
 */
public class EditRelation extends AbcFMServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditRelation.class);

    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_PREFIX = "prefix";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_SELECTED = SelectRelation.PARAM_SELECTED;
    public static final String PARAM_ACL_ID = "id";
    public static final String PARAM_ACL_TYPE = "right";
    public static final String PARAM_ACL_VALUE = "value";
    public static final String PARAM_ACL_WHO = "who";
    public static final String PARAM_USER = ViewUser.PARAM_USER_SHORT;
    public static final String PARAM_GROUP = EditGroup.PARAM_GROUP;

    public static final String VAR_CURRENT = "CURRENT";
    public static final String VAR_SELECTED = "SELECTED";
    public static final String VAR_PARENTS = "PARENTS";
    public static final String VAR_ACL = "ACL";
    public static final String VAR_GROUPS = EditGroup.VAR_GROUPS;

    public static final String VALUE_DISCUSSIONS = "discussions";
    public static final String VALUE_MAKES = "makes";
    public static final String VALUE_ARTICLES = "articles";
    public static final String VALUE_CATEGORIES = "categories";

    public static final String ACTION_LINK = "add";
    public static final String ACTION_LINK_STEP2 = "add2";
    public static final String ACTION_REMOVE = "remove";
    public static final String ACTION_REMOVE_STEP2 = "remove2";
    public static final String ACTION_MOVE = "move";
    public static final String ACTION_MOVE_ALL = "moveAll";
    public static final String ACTION_MOVE_ALL_STEP2 = "moveAll2";
    public static final String ACTION_SHOW_ACL = "showACL";
    public static final String ACTION_ADD_ACL = "addACL";
    public static final String ACTION_ADD_ACL_STEP2 = "addACL2";
    public static final String ACTION_ADD_ACL_STEP3 = "addACL3";
    public static final String ACTION_REMOVE_ACL = "removeACL";


    // todo move permission checks to called methods from here.
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

        if ( action.equals(ACTION_MOVE_ALL) ) {
            if ( !user.hasRole(Roles.CATEGORY_ADMIN) )
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return FMTemplateSelector.select("EditRelation", "moveAll", env, request);
        }

        if ( action.equals(ACTION_MOVE_ALL_STEP2) ) {
            if ( !user.hasRole(Roles.CATEGORY_ADMIN) )
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            return actionMoveAll(request, response, env);
        }

        if ( action.equals(ACTION_SHOW_ACL) )
            return actionShowACL(request,env);

        if ( action.equals(ACTION_ADD_ACL) )
            return actionAddACLStep1(request,env);

        if ( action.equals(ACTION_ADD_ACL_STEP2) )
            return actionAddACLStep2(request,response,env);

        if ( action.equals(ACTION_ADD_ACL_STEP3) )
            return actionAddACLStep3(request,env);

        if ( action.equals(ACTION_REMOVE_ACL) )
            return actionRemoveACL(request, env);

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

        runMonitor(relation,user);
        persistance.remove(relation);
        AdminLogger.logEvent(user, "remove | relation "+relation.getId()+" | "+Tools.childName(relation));

        String url = null;
        String prefix = (String) params.get(PARAM_PREFIX);
        if ( prefix!=null ) {
            url = prefix.concat("/ViewRelation?rid="+relation.getUpper());
        } else
            url = "/Index";

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
     * Called, when user selects destination in SelectRelation. It replaces parent in relation with child
     * in destination.
     */
    protected String actionMoveAll(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);
        String type = (String) params.get(PARAM_TYPE);

        Relation relation = (Relation) env.get(VAR_CURRENT);
        persistance.synchronize(relation.getChild());
        Relation destination = (Relation) InstanceUtils.instantiateParam(PARAM_SELECTED, Relation.class, params);
        persistance.synchronize(destination);

        for ( Iterator iter = relation.getChild().getContent().iterator(); iter.hasNext(); ) {
            Relation childRelation = (Relation) iter.next();
            GenericObject child = childRelation.getChild();
            persistance.synchronize(child);
            boolean move = false;
            if (child instanceof Item) {
                if (VALUE_ARTICLES.equals(type) && ((Item)child).getType()==Item.ARTICLE)
                    move = true;
                if (VALUE_DISCUSSIONS.equals(type) && ((Item)child).getType()==Item.DISCUSSION)
                    move = true;
                if (VALUE_MAKES.equals(type) && ((Item)child).getType()==Item.MAKE)
                    move = true;
            } else if ( VALUE_CATEGORIES.equals(type) && child instanceof Category)
                move = true;

            if (move) {
                childRelation.setParent(destination.getChild());
                childRelation.setUpper(destination.getId());
                persistance.update(childRelation);

                AdminLogger.logEvent(user, "  move | relation "+childRelation.getId()+" | from "+relation.getId()+" | to "+destination.getId());
            }
        }

        String url = null;
        String prefix = (String) params.get(PARAM_PREFIX);
        url = (prefix!=null) ? prefix.concat("/ViewRelation?rid="+relation.getId()) : "/Index";

        UrlUtils urlUtils = new UrlUtils("", response);
        urlUtils.redirect(response, url);
        return null;
    }

    /**
     * Shows ACL for given relation.
     */
    private String actionShowACL(HttpServletRequest request, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        if ( !user.hasRole(Roles.ROOT) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        Relation relation = (Relation) env.get(VAR_CURRENT);
        if (relation.getData()!=null) {
            List nodes = relation.getData().selectNodes("/data/acl");
            List acls = new ArrayList(nodes.size());
            for ( Iterator iter = nodes.iterator(); iter.hasNext(); ) {
                Element element = (Element) iter.next();
                ACL acl = getACL(element);
                acls.add(acl);
            }
            if (acls.size()>0)
                env.put(VAR_ACL,acls);
        }

        return FMTemplateSelector.select("EditRelation", "showACL", env, request);
    }

    /**
     * Adds new ACL for given relation.
     */
    private String actionAddACLStep1(HttpServletRequest request, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        if ( !user.hasRole(Roles.ROOT) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        List items = SQLTool.getInstance().findItemsWithType(Item.GROUP, 0, EditGroup.DEFAULT_MAX_NUMBER_OF_GROUPS);
        env.put(VAR_GROUPS, items);

        return FMTemplateSelector.select("EditRelation", "addACL", env, request);
    }

    /**
     * Adds new ACL for given relation.
     */
    private String actionAddACLStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        if ( !user.hasRole(Roles.ROOT) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String who = (String) params.remove(PARAM_ACL_WHO);
        if ( "user".equals(who) ) {
            String url = "/SelectUser";
            params.put(SelectUser.PARAM_ACTION,SelectUser.ACTION_SHOW_FORM);
            params.put(SelectUser.PARAM_URL, "/EditRelation");
            params.put(PARAM_ACTION, ACTION_ADD_ACL_STEP3);
            request.getSession().setAttribute(Constants.VAR_PARAMS, params);

            ((UrlUtils) env.get(Constants.VAR_URL_UTILS)).redirect(response, url);
            return null;
        } else
            return actionAddACLStep3(request,env);
    }

    /**
     * Adds new ACL for given relation.
     */
    private String actionAddACLStep3(HttpServletRequest request, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        if ( !user.hasRole(Roles.ROOT) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_CURRENT);
        Persistance persistance = PersistanceFactory.getPersistance();

        boolean canContinue = setACL(params,relation,env);
        if (canContinue) {
            persistance.update(relation);
            AdminLogger.logEvent(user, "vytvoril nove ACL pro relaci "+relation.getId());
        }

        return actionShowACL(request,env);
    }

    /**
     * Removes selected ACLs.
     */
    protected String actionRemoveACL(HttpServletRequest request, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        if ( !user.hasRole(Roles.ROOT) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_CURRENT);
        Persistance persistance = PersistanceFactory.getPersistance();
        Document document = relation.getData();

        List acls = null;
        Object tmp = params.get(PARAM_ACL_ID);
        if ( tmp instanceof String ) {
            acls = new ArrayList(1);
            acls.add(tmp);
        } else
            acls = (List) tmp;

        for ( Iterator iter = acls.iterator(); iter.hasNext(); ) {
            String id = (String) iter.next();
            Element element = (Element) document.selectSingleNode("/data/acl[@id='"+id+"']");
            if ( element==null )
                continue;
            element.detach();
            AdminLogger.logEvent(user, "zrusil ACL "+id+" z relace "+relation.getId());
        }
        persistance.update(relation);

        return actionShowACL(request, env);
    }

    ///////////////////////////////////////////////////////////////////////////
    //                          Setters                                      //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Creates ACL from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param relation relation to be updated
     * @return false, if there is a major error.
     */
    private boolean setACL(Map params, Relation relation, Map env) {
        String right = (String) params.get(PARAM_ACL_TYPE);
        String value = (String) params.get(PARAM_ACL_VALUE);
        int gid = Misc.parseInt((String) params.get(PARAM_GROUP),0);
        int uid = Misc.parseInt((String) params.get(PARAM_USER),0);

        Document document = relation.getData();
        if ( document==null ) {
            document = DocumentHelper.createDocument();
            relation.setData(document);
        }

        int id = 0;
        List acls = document.selectNodes("/data/acl");
        for ( Iterator iter = acls.iterator(); iter.hasNext(); ) {
            Element element = (Element) iter.next();
            int tmp = Misc.parseInt(element.attributeValue("id"),0);
            if (tmp>=id) id = tmp+1;
        }

        Element data = document.getRootElement();
        if (data==null) {
            document.setRootElement(DocumentHelper.createElement("data"));
            data = document.getRootElement();
        }

        Element acl = data.addElement("acl");
        acl.addAttribute("id",new Integer(id).toString());
        acl.addAttribute("right",right);
        acl.addAttribute("value",value);
        if (uid>0)
            acl.addAttribute("uid", new Integer(uid).toString());
        else if (gid>0)
            acl.addAttribute("gid", new Integer(gid).toString());
        else {
            ServletUtils.addError(Constants.ERROR_GENERIC,"Musíte zadat èíslo skupiny nebo u¾ivatele!",env,null);
            return false;
        }

        return true;
    }

    /**
     * Converts DOM4J Element to ACL.
     */
    public static ACL getACL(Element element) {
        Persistance persistance = PersistanceFactory.getPersistance();

        int id = Misc.parseInt(element.attributeValue("id"),0);
        int gid = Misc.parseInt(element.attributeValue("gid"),0);
        int uid = Misc.parseInt(element.attributeValue("uid"),0);

        ACL acl;
        if (uid!=0)
            acl = new ACL(id,(User) persistance.findById(new User(uid)));
        else
            acl = new ACL(id, (Item) persistance.findById(new Item(gid)));

        boolean value = "yes".equals(element.attributeValue("value"));
        acl.setRight(element.attributeValue("right"),value);

        return acl;
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

    /**
     * Runs monitor, if deleted object was driver, make or discussion (question).
     */
    private void runMonitor(Relation relation, User user) {
        MonitorAction action = null;

        GenericObject child = relation.getChild();
        if ( ! (child instanceof Item) )
            return;

        Item item = (Item) child;
        if (item.getType()==Item.DRIVER) {
            action = new MonitorAction(user, UserAction.REMOVE, ObjectType.DRIVER, item, null);
            String name = item.getData().selectSingleNode("/data/name").getText();
            action.setProperty(Decorator.PROPERTY_NAME, name);
        } else if (item.getType()==Item.MAKE) {
            action = new MonitorAction(user, UserAction.REMOVE, ObjectType.ITEM, item, null);
            String name = item.getData().selectSingleNode("/data/name").getText();
            action.setProperty(Decorator.PROPERTY_NAME, name);
        } else if (item.getType()==Item.DISCUSSION) {
            action = new MonitorAction(user, UserAction.REMOVE, ObjectType.DISCUSSION, item, null);
            Element title = (Element) item.getData().selectSingleNode("/data/title");
            if (title==null)
                return;
            action.setProperty(Decorator.PROPERTY_NAME, title.getText());
        }

        MonitorPool.scheduleMonitorAction(action);
    }
}
