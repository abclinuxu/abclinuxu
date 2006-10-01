package cz.abclinuxu.servlets.html.edit;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.data.*;
import cz.abclinuxu.utils.email.monitor.MonitorTools;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.exceptions.MissingArgumentException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Date;

/**
 * This class is used to manipulate with monitors that user has set up.
 */
public class EditMonitor implements AbcAction {
    public static final String PARAM_RELATION_SHORT = "rid";
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

        // check permissions
        User user = (User) env.get(Constants.VAR_USER);
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        return actionAlterMonitor(request, response, env);
    }

    /**
     * Reverts current monitor state for the user on this document.
     */
    protected String actionAlterMonitor(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        GenericDataObject content = (GenericDataObject) persistence.findById(relation.getChild());
        User user = (User) env.get(Constants.VAR_USER);

        Date originalUpdated = content.getUpdated();
        MonitorTools.alterMonitor(content.getData().getRootElement(), user);
        persistence.update(content);
        SQLTool.getInstance().setUpdatedTimestamp(content, originalUpdated);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation, true));
        return null;
    }
}
