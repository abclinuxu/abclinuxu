/*
 *  Copyright (C) 2005 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
package cz.abclinuxu.servlets.html.edit;

import org.dom4j.Element;
import org.dom4j.Document;

import java.util.Map;
import java.util.Date;
import java.util.prefs.Preferences;

import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.data.User;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.security.ActionProtector;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utility class for manipulation with ratings.
 */
public class EditRating implements AbcAction, Configurable {
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_VALUE = "rvalue";
    public static final String PARAM_RETURN = "return";

    // todo jina persistence. Nehodi se to mit v XML, ale v nejakem sloupecku ci tabulce, bug #623
    public static final int VALUE_MIN = 0;
    public static final int VALUE_MAX = 3;
    public static final String USER_ACTION_RATING = "rating";

    public static final String PREF_MESSAGE_OK = "msg.ok";
    public static final String PREF_MESSAGE_NOT_LOGGED = "msg.not.logged";
    public static final String PREF_MESSAGE_MISSING_DATA = "msg.data.missing";
    public static final String PREF_MESSAGE_ALREADY_RATED = "msg.already.rated";

    static String msgOK, msgMissingData, msgAlreadyRated, msgNotLogged;
    static {
        ConfigurationManager.getConfigurator().configureAndRememberMe(new EditRating());
    }

    /**
     * Rates object identified by relation id and redirects back.
     */
    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);

        if (AbcConfig.isMaintainanceMode()) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Systém je v režimu údržby.", env, null);
            return "/print/misc/rating_result.ftl";
        }

        if ( relation!=null ) {
            relation = (Relation) persistence.findById(relation);
            persistence.synchronize(relation.getChild());
        } else
            throw new MissingArgumentException("Chybí parametr relationId!");

        User user = (User) env.get(Constants.VAR_USER);
        if (user==null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, msgNotLogged, env, null);
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        }

        ActionProtector.ensureContract(request, EditRating.class, false, false, false, true);

        GenericDataObject object = ((GenericDataObject)relation.getChild());
        Date originalUpdated = object.getUpdated();
        Document data = object.getData();
        synchronized (data.getRootElement()) {
            boolean result = rate(user, relation.getId(), data.getRootElement(), params, env, request.getSession());
            if ( result ) {
                ServletUtils.addError(Constants.ERROR_GENERIC, msgOK, env, null);
                persistence.update(object);
            }
        }
        SQLTool.getInstance().setUpdatedTimestamp(object, originalUpdated);

        if (params.containsKey(PARAM_RETURN)) {
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
            return null;
        }
        return "/print/misc/rating_result.ftl";
    }

    /**
     * Adds user vote to rating of this object. This method
     * doesn't care of either synchronization or persistence.
     * This shall be handled by caller.
     * @param object root element for the rated object.
     * @param params parameters
     * @return true if rating was successfull
     */
    public static boolean rate(User user, int relationId, Element object, Map params, Map env, HttpSession session) {
        int value = Misc.parseInt((String) params.get(PARAM_VALUE), 0);
        if (value != VALUE_MIN && value != VALUE_MAX) {
            ServletUtils.addError(Constants.ERROR_GENERIC, msgMissingData, env, null);
            return false;
        }

        SQLTool sqlTool = SQLTool.getInstance();
        if (sqlTool.getUserAction(user.getId(), relationId, USER_ACTION_RATING)!=null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, msgAlreadyRated, env, null);
            return false;
        }
        sqlTool.insertUserAction(user.getId(), relationId, USER_ACTION_RATING);

        int sum = 0, count = 0;
        Element rating = (Element) object.selectSingleNode("rating");
        if (rating==null) {
            rating = object.addElement("rating");
            rating.addElement("sum");
            rating.addElement("count");
        } else {
            sum = Misc.parseInt(rating.elementText("sum"), 0);
            count = Misc.parseInt(rating.elementText("count"), 0);
        }

        rating.element("sum").setText(Integer.toString(sum+value));
        rating.element("count").setText(Integer.toString(count+1));
        return true;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        msgOK = prefs.get(PREF_MESSAGE_OK, "");
        msgMissingData = prefs.get(PREF_MESSAGE_MISSING_DATA, "");
        msgAlreadyRated = prefs.get(PREF_MESSAGE_ALREADY_RATED, "");
        msgNotLogged = prefs.get(PREF_MESSAGE_NOT_LOGGED, "");
    }
}
