/*
 * User: literakl
 * Date: 23.11.2003
 * Time: 20:46:02
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.view.ViewUser;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.utils.config.impl.AbcConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.Element;

/**
 * Code for manipulation of user groups.
 */
public class EditGroup implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditGroup.class);

    public static final String PARAM_NAME = "name";
    public static final String PARAM_DESCRIPTION = "desc";
    public static final String PARAM_GROUP = "gid";
    public static final String PARAM_FROM = "from";

    public static final String VAR_GROUP = "GROUP";
    public static final String VAR_GROUPS = "GROUPS";
    public static final String VAR_MEMBERS = "MEMBERS";

    public static final String ACTION_CREATE_GROUP = "add";
    public static final String ACTION_CREATE_GROUP_STEP2 = "add2";
    public static final String ACTION_EDIT_GROUP = "edit";
    public static final String ACTION_EDIT_GROUP_STEP2 = "edit2";
    public static final String ACTION_SHOW = "show";
    public static final String ACTION_SHOW_USERS = "members";
    public static final String ACTION_REMOVE_GROUP_MEMBERS = "removeMembers";

    /** number of groups in system shall be smaller than this value */
    public static final int DEFAULT_MAX_NUMBER_OF_GROUPS = 20;

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);
        User user = (User) env.get(Constants.VAR_USER);

        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        if ( !user.hasRole(Roles.USER_ADMIN) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);


        if ( ACTION_CREATE_GROUP.equals(action) )
            return FMTemplateSelector.select("EditGroup", "create", env, request);

        if ( ACTION_CREATE_GROUP_STEP2.equals(action) )
            return actionCreateGroup(request, response, env);

        if ( ACTION_EDIT_GROUP.equals(action) )
            return actionEditGroupStep1(request, env);

        if ( ACTION_EDIT_GROUP_STEP2.equals(action) )
            return actionEditGroupStep2(request, response, env);

        if ( ACTION_SHOW.equals(action) )
            return actionShow(request, env);

        if ( ACTION_SHOW_USERS.equals(action) )
            return actionShowUsers(request, env);

        if ( ACTION_REMOVE_GROUP_MEMBERS.equals(action) )
            return actionRemoveMembers(request, response, env);

        return actionShow(request, env);
    }


    /**
     * Creates new group.
     */
    protected String actionCreateGroup(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        Persistance persistance = PersistanceFactory.getPersistance();

        Item group = new Item(0, Item.GROUP);
        group.setData(DocumentHelper.createDocument());

        boolean canContinue = true;
        canContinue &= setGroupName(params, group, env);
        canContinue &= setGroupDescription(params, group, env);

        if ( !canContinue )
            return FMTemplateSelector.select("EditGroup", "create", env, request);

        group.setOwner(user.getId());
        group.setCreated(new Date());

        persistance.create(group);
        AdminLogger.logEvent(user, "vytvoril novou skupinu "+group.getId());

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Group");
        return null;
    }

    /**
     * Edits the group.
     */
    protected String actionEditGroupStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Item group = (Item) InstanceUtils.instantiateParam(PARAM_GROUP, Item.class, params, request);
        if ( group==null )
            return ServletUtils.showErrorPage("Zadejte skupinu!", env, request);
        persistance.synchronize(group);

        if ( group.getType()!=Item.GROUP )
            return ServletUtils.showErrorPage("Toto není skupina!", env, request);

        params.put(PARAM_NAME, group.getData().selectSingleNode("/data/name").getText());
        params.put(PARAM_DESCRIPTION, group.getData().selectSingleNode("/data/desc").getText());

        return FMTemplateSelector.select("EditGroup", "create", env, request);
    }

    /**
     * Edits the group.
     */
    protected String actionEditGroupStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        Persistance persistance = PersistanceFactory.getPersistance();

        Item group = (Item) InstanceUtils.instantiateParam(PARAM_GROUP, Item.class, params, request);
        if ( group==null )
            return ServletUtils.showErrorPage("Zadejte skupinu!", env, request);
        persistance.synchronize(group);

        if ( group.getType()!=Item.GROUP )
            return ServletUtils.showErrorPage("Toto není skupina!", env, request);

        boolean canContinue = true;
        canContinue &= setGroupName(params, group, env);
        canContinue &= setGroupDescription(params, group, env);

        if ( !canContinue )
            return FMTemplateSelector.select("EditGroup", "create", env, request);

        persistance.update(group);
        AdminLogger.logEvent(user, "upravil skupinu "+group.getId());

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Group");
        return null;
    }

    /**
     * Shows existing groups.
     */
    protected String actionShow(HttpServletRequest request, Map env) throws Exception {
        List items = SQLTool.getInstance().findItemsWithType(Item.GROUP, 0, DEFAULT_MAX_NUMBER_OF_GROUPS);
        env.put(VAR_GROUPS, items);
        return FMTemplateSelector.select("EditGroup", "showGroups", env, request);
    }

    /**
     * Shows members of the group.
     */
    protected String actionShowUsers(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Item group = (Item) InstanceUtils.instantiateParam(PARAM_GROUP, Item.class, params, request);
        persistance.synchronize(group);
        if ( group.getType()!=Item.GROUP )
            return ServletUtils.showErrorPage("Toto není skupina!", env, request);
        env.put(VAR_GROUP,group);

        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int pageSize = AbcConfig.getViewUserPageSize();
        SQLTool sqlTool = SQLTool.getInstance();
        List keys = sqlTool.findUsersInGroup(group.getId(), new Qualifier[] {Qualifier.SORT_BY_ID,new LimitQualifier(from, pageSize)});
        List users = new ArrayList(keys.size());
        for ( Iterator iter = keys.iterator(); iter.hasNext(); ) {
            Integer key = (Integer) iter.next();
            users.add(persistance.findById(new User(key.intValue())));
        }

        int total = sqlTool.countUsersInGroup(group.getId());
        Paging paging = new Paging(users, from, pageSize, total);
        env.put(VAR_MEMBERS,paging);

        return FMTemplateSelector.select("EditGroup", "showMembers", env, request);
    }

    /**
     * Removes selected users from the group.
     */
    protected String actionRemoveMembers(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User admin = (User) env.get(Constants.VAR_USER);
        Persistance persistance = PersistanceFactory.getPersistance();

        int group = Misc.parseInt((String) params.get(EditGroup.PARAM_GROUP), 0);
        if ( group==0 )
            return ServletUtils.showErrorPage("Chybí èíslo skupiny!", env, request);

        List users = null;
        Object tmp = params.get(ViewUser.PARAM_USER_SHORT);
        if (tmp instanceof String) {
            users = new ArrayList(1);
            users.add(tmp);
        } else
            users = (List) tmp;

        int id;
        for ( Iterator iter = users.iterator(); iter.hasNext(); ) {
            id = Misc.parseInt((String) iter.next(),0);
            if (id==0)
                continue;
            User user = (User) persistance.findById(new User(id));
            Element element = (Element) user.getData().selectSingleNode("/data/system/group[text()='"+group+"']");
            if (element==null)
                continue;
            element.detach();
            persistance.update(user);
            AdminLogger.logEvent(admin, "vyradil uzivatele "+user.getId()+" ze skupiny "+group);
        }

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Group?action=members&gid="+group);
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////
    //                          Setters                                      //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Updates name of group from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param group group to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setGroupName(Map params, Item group, Map env) {
        String name = (String) params.get(PARAM_NAME);
        if ( name==null || name.trim().length()==0 ) {
            ServletUtils.addError(PARAM_NAME, "Zadejte jméno skupiny!", env, null);
            return false;
        }
        Node node = DocumentHelper.makeElement(group.getData(), "/data/name");
        node.setText(name);
        return true;
    }

    /**
     * Updates description of group from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param group group to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setGroupDescription(Map params, Item group, Map env) {
        String desc = (String) params.get(PARAM_DESCRIPTION);
        if ( desc==null || desc.trim().length()==0 ) {
            ServletUtils.addError(PARAM_DESCRIPTION, "Zadejte popis skupiny!", env, null);
            return false;
        }
        Node node = DocumentHelper.makeElement(group.getData(), "/data/desc");
        node.setText(desc);
        return true;
    }
}
