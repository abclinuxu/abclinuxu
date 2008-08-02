package cz.abclinuxu.servlets.html.edit;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.data.*;
import cz.abclinuxu.utils.email.monitor.MonitorTools;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.security.ActionProtector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Date;

/**
 * This class is used to manipulate with monitors that user has set up.
 */
public class EditMonitor implements AbcAction {
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_REDIRECT = "redirect";
    
    public static final String VAR_RELATION = "RELATION";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Chybí parametr rid!");
        else {
            Tools.sync(relation);
            env.put(VAR_RELATION, relation);
        }

        if (ServletUtils.handleMaintainance(request, env)) {
            response.sendRedirect(response.encodeRedirectURL("/"));
            return null;
        }

        // check permissions
        User user = (User) env.get(Constants.VAR_USER);
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        ActionProtector.ensureContract(request, EditMonitor.class, false, false, false, true);
        return actionAlterMonitor(request, response, env);
    }

    /**
     * Reverts current monitor state for the user on this document.
     */
    protected String actionAlterMonitor(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        Persistence persistence = PersistenceFactory.getPersistence();
        Relation relation = (Relation) env.get(VAR_RELATION);
        GenericDataObject content = (GenericDataObject) persistence.findById(relation.getChild());

        User user = (User) env.get(Constants.VAR_USER);
        if (! Misc.hasValidEmail(user)) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Nemáte validní email. Buď jste jej nezadali, neaktivovali jej " +
                                  "nebo byl zablokován kvůli nedoručeným emailům.", env, request.getSession());
            urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
            return null;
        }

        Date originalUpdated = content.getUpdated();
        MonitorTools.alterMonitor(content.getData().getRootElement(), user);
        persistence.update(content);
        SQLTool.getInstance().setUpdatedTimestamp(content, originalUpdated);
        
        String redirectUrl = (String) params.get(PARAM_REDIRECT);
        if (Misc.empty(redirectUrl))
            redirectUrl = urlUtils.getRelationUrl(relation);

        urlUtils.redirect(response, redirectUrl);
        return null;
    }
}
