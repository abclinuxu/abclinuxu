package cz.abclinuxu.servlets.html.edit;

import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.AbcAutoAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.view.ViewUser;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.email.monitor.MonitorTool;
import cz.abclinuxu.utils.freemarker.Tools;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * This class is used to manipulate with monitors that user has set up.
 */
public class EditMonitor extends AbcAutoAction {
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_REDIRECT = "redirect";
    public static final String PARAM_USER = "uid";
    
    public static final String VAR_RELATION = "RELATION";

    public static final String ACTION_START_MONITOR = "start";
    public static final String ACTION_STOP_MONITOR = "stop";
    public static final String ACTION_REMOVE_ALL_MONITORS = "removeAll";


    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);

        if (ServletUtils.handleMaintainance(request, env)) {
            response.sendRedirect(response.encodeRedirectURL("/"));
            return null;
        }

        // check permissions
        User user = (User) env.get(Constants.VAR_USER);
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if (ACTION_START_MONITOR.equals(action)) {
            ActionProtector.ensureContract(request, EditMonitor.class, false, false, false, true);
            return actionToggleMonitor(true, request, response, env);
        }

        if (ACTION_STOP_MONITOR.equals(action)) {
            ActionProtector.ensureContract(request, EditMonitor.class, false, false, false, true);
            return actionToggleMonitor(false, request, response, env);
        }

        if (ACTION_REMOVE_ALL_MONITORS.equals(action)) {
            ActionProtector.ensureContract(request, ViewUser.class, true, true, true, false);
            return actionRemoveAllMonitors(request, response, env);
        }

        throw new MissingArgumentException("Chybí parametr action!");
    }

    private String actionRemoveAllMonitors(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);

        int managed = Misc.parseInt((String) params.get(PARAM_USER), -1);
        if (managed == -1)
            throw new MissingArgumentException("Chybí číslo uživatele!");

        if (!(user.getId() == managed || user.hasRole(Roles.USER_ADMIN)))
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        MonitorTool.removeAllMonitors(new User(managed));

        ServletUtils.addError(Constants.ERROR_GENERIC, "Všechny monitory byly odstraněny.", env, request.getSession());

        if (user.getId() != managed)
            AdminLogger.logEvent(user, "smazal monitory uzivatele " + managed);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Profile/" + managed + "?action=monitors");
        return null;
    }

    public String actionToggleMonitor(boolean start, HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Chybí parametr rid!");
        else {
            Tools.sync(relation);
            env.put(VAR_RELATION, relation);
        }

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        if (start && ! Misc.hasValidEmail(user)) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Nemáte validní email. Buď jste jej nezadali, neaktivovali jej " +
                    "nebo byl zablokován kvůli nedoručeným emailům.", env, request.getSession());
            urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
            return null;
        }

        if (start)
            MonitorTool.startMonitor((GenericDataObject) relation.getChild(), user);
        else
            MonitorTool.stopMonitor((GenericDataObject) relation.getChild(), user);

        String redirectUrl = (String) params.get(PARAM_REDIRECT);
        if (Misc.empty(redirectUrl))
            redirectUrl = urlUtils.getRelationUrl(relation);

        urlUtils.redirect(response, redirectUrl);
        return null;
    }
}
