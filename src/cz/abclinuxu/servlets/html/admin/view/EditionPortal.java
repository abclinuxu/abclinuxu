package cz.abclinuxu.servlets.html.admin.view;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import cz.abclinuxu.data.User;
import cz.abclinuxu.data.EditionRole;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.view.Author;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.data.view.Topic;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.OrderByQualifier;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.PageNavigation;
import cz.abclinuxu.servlets.utils.url.PwdNavigator;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.BeanFetcher;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.BeanFetcher.FetchType;

/**
 * Handles both editor's and authors portal
 *
 * @author kapy
 */
public class EditionPortal implements AbcAction {
    public static final String VAR_AUTHOR = "AUTHOR";
    public static final String VAR_TOPICS = "TOPICS";
    public static final String VAR_ROLE = "ROLE";
    public static final String VAR_IS_EDITOR = "IS_EDITOR";
    public static final String VAR_IS_EDITOR_IN_CHIEF = "IS_EDITOR_IN_CHIEF";
    public static final String VAR_UNSIGNED_CONTRACT = "UNSIGNED_CONTRACT";

    public static final String PARAM_DESIRED_ROLE = "desiredRole";

    public static final String ACTION_SWITCH_ROLE = "switch";
    public static final String ACTION_SWITCH_ROLE_STEP2 = "switch2";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);
        User user = (User) env.get(Constants.VAR_USER);

        // check permissions
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        EditionRole role = ServletUtils.getEditionRole(user,  request);
        if (role == EditionRole.NONE)
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        PwdNavigator navigator = new PwdNavigator(env, PageNavigation.EDITION_PORTAL);

        Author author = Tools.getAuthor(user.getId());
        env.put(VAR_AUTHOR, author);

        if (ACTION_SWITCH_ROLE.equals(action))
            return actionSwitchRole(request, env, navigator);
        if (ACTION_SWITCH_ROLE_STEP2.equals(action))
            return actionSwitchRoleStep2(request, response, env, navigator);

        switch (role) {
            case AUTHOR:
                return actionShowAuthorsDashboard(request, env, navigator);

            case EDITOR:
            case EDITOR_IN_CHIEF:
                env.put(VAR_ROLE, role);
                return actionShowEditorsDashboard(request, env, navigator);
        }

        return null;
    }

    private String actionShowAuthorsDashboard(HttpServletRequest request, Map env, PwdNavigator navigator) {
        List<Link> parents = navigator.navigate();
        env.put(Constants.VAR_PARENTS, parents);

        SQLTool sqlTool = SQLTool.getInstance();
        Author author = (Author) env.get(VAR_AUTHOR);
        Qualifier[] qualifiers = new Qualifier[] {
                new CompareCondition(new Field(Field.NUMERIC1, "P"), Operation.EQUAL, author.getRelationId()),
                new CompareCondition(new Field(Field.DATE1, "P"), Operation.IS_NOT_NULL, null),
                new CompareCondition(new Field(Field.NUMERIC3, "P"), Operation.IS_NULL, null),
                new OrderByQualifier(Qualifier.SORT_BY_DATE1, "P"), Qualifier.ORDER_ASCENDING};

        List<Relation> relations = sqlTool.getTopics(qualifiers);
        Tools.syncList(relations);
        List<Topic> topics = BeanFetcher.fetchTopics(relations, FetchType.EAGER);
        if (!Misc.empty(topics))
            env.put(VAR_TOPICS, topics);

        Relation relation = sqlTool.findUnsignedContractRelation(author.getUid());
        env.put(VAR_UNSIGNED_CONTRACT, relation != null);

        return FMTemplateSelector.select("EditionPortal", "authorsDashboard", env, request);
    }

    private String actionShowEditorsDashboard(HttpServletRequest request, Map env, PwdNavigator navigator) {
        List<Link> parents = navigator.navigate();
        env.put(Constants.VAR_PARENTS, parents);

        return FMTemplateSelector.select("EditionPortal", "editorsDashboard", env, request);
    }

    private String actionSwitchRole(HttpServletRequest request, Map env, PwdNavigator navigator) {
        User user = (User) env.get(Constants.VAR_USER);
        env.put(VAR_IS_EDITOR, user.isMemberOf(Constants.GROUP_EDITORS));
        env.put(VAR_IS_EDITOR_IN_CHIEF, user.isMemberOf(Constants.GROUP_EDITORS_IN_CHIEF));
        List<Link> parents = navigator.navigate();
        env.put(Constants.VAR_PARENTS, parents);

        return FMTemplateSelector.select("EditionPortal", "switchRole", env, request);
    }

    private String actionSwitchRoleStep2(HttpServletRequest request, HttpServletResponse response, Map env, PwdNavigator navigator) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);

        EditionRole role;
        String desiredRole = (String) params.get(PARAM_DESIRED_ROLE);

        if ("editor".equals(desiredRole)) {
            if (! user.isMemberOf(Constants.GROUP_EDITORS) && ! user.isMemberOf(Constants.GROUP_EDITORS_IN_CHIEF))
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

            role = EditionRole.EDITOR;
        } else if ("editorInChief".equals(desiredRole)) {
            if (! user.isMemberOf(Constants.GROUP_EDITORS_IN_CHIEF))
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

            role = EditionRole.EDITOR_IN_CHIEF;
        } else
            role = EditionRole.AUTHOR;

        HttpSession session = request.getSession();
        session.setAttribute(Constants.VAR_EDITION_ROLE, role);

        List<Link> parents = navigator.navigate();
        env.put(Constants.VAR_PARENTS, parents);

        UrlUtils urlUtils = navigator.getUrlUtils();
        urlUtils.redirect(response, urlUtils.make(UrlUtils.PREFIX_ADMINISTRATION + "/redakce/"));
        return null;
    }
}