/*
 *  Copyright (C) 2007 Leos Literak
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

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.view.ViewTag;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Tag;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.utils.TagTool;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.finesoft.socd.analyzer.DiacriticRemover;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.prefs.Preferences;

/**
 * Tag manipulation class.
 * @author literakl
 * @since 5.1.2008
 */
public class EditTag implements AbcAction, Configurable {
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_ID = "id";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";
    public static final String ACTION_REMOVE_STEP2 = "rm2";

    public static final String PREF_INVALID_TITLE_REGEXP = "regexp.invalid.title";
    private static Pattern reInvalidTitle;

    static {
        ConfigurationManager.getConfigurator().configureAndRememberMe(new EditTag());
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        String tmp = prefs.get(PREF_INVALID_TITLE_REGEXP, null);
        reInvalidTitle = Pattern.compile(tmp);
    }

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        if (ServletUtils.handleMaintainance(request, env))
            response.sendRedirect(response.encodeRedirectURL("/"));

        if (ACTION_ADD.equals(action))
            return actionAddStep1(request, env);

        if (ACTION_ADD_STEP2.equals(action)) {
            ActionProtector.ensureContract(request, EditTag.class, false, true, true, false);
            return actionAddStep2(request, response, env, true);
        }

        // check permissions
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        if (! user.hasRole(Roles.TAG_ADMIN))
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (ACTION_EDIT.equals(action))
            return actionEdit(request, env);

        if (ACTION_EDIT_STEP2.equals(action)) {
            ActionProtector.ensureContract(request, EditTag.class, true, true, true, false);
            return actionEditStep2(request, response, env);
        }

        if (ACTION_REMOVE_STEP2.equals(action)) {
            ActionProtector.ensureContract(request, ViewTag.class, true, true, false, true);
            return actionRemoveStep2(request, response, env);
        }

        throw new MissingArgumentException("Chybí parametr action!");
    }

    private String actionAddStep1(HttpServletRequest request, Map env) throws Exception {
        return FMTemplateSelector.select("Tags", "add", env, request);
    }

    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String ipAddress = ServletUtils.getClientIPAddress(request);

        Tag tag = new Tag();
        boolean canContinue = setTitleAndId(params, tag, false, env);
        if (! canContinue)
            return FMTemplateSelector.select("Tags", "add", env, request);

        TagTool.create(tag, user, ipAddress);
        String url = UrlUtils.PREFIX_TAGS + "/" + tag.getId();

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, url);
        return null;
    }

    protected String actionEdit(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String id = (String) params.get(PARAM_ID);
        Tag tag = TagTool.getById(id);
        if (tag == null)
            throw new NotFoundException("Štítek '" + id + "' nebyl nalezen!");

        params.put(PARAM_TITLE, tag.getTitle());
        return FMTemplateSelector.select("Tags", "edit", env, request);
    }

    protected String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String ipAddress = ServletUtils.getClientIPAddress(request);

        String id = (String) params.get(PARAM_ID);
        Tag tag = (Tag) TagTool.getById(id).clone();
        if (tag == null)
            throw new NotFoundException("Štítek '" + id + "' nebyl nalezen!");

        boolean canContinue = setTitleAndId(params, tag, true, env);
        if (!canContinue)
            return FMTemplateSelector.select("Tags", "edit", env, request);

        TagTool.update(tag, user, ipAddress);
        String url = UrlUtils.PREFIX_TAGS + "/" + tag.getId();

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, url);
        return null;
    }

    /**
     * Validates input values and if it is OK, that it updates the driver and displays it.
     * @return page to be rendered
     */
    protected String actionRemoveStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String ipAddress = ServletUtils.getClientIPAddress(request);

        String id = (String) params.get(PARAM_ID);
        Tag tag = TagTool.getById(id);
        if (tag == null)
            throw new NotFoundException("Štítek '" + id + "' nebyl nalezen!");

        TagTool.remove(tag, user, ipAddress);
        String url = UrlUtils.PREFIX_TAGS;

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, url);
        return null;
    }


    /* ******* setters ********* */


    /**
     * Updates name of tag from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param tag a tag to be updated
     * @param setOnlyTitle whether only title shall be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setTitleAndId(Map params, Tag tag, boolean setOnlyTitle, Map env) {
        String title = (String) params.get(PARAM_TITLE);
        if (title != null)
            title = title.trim();
        if (title == null || title.length() == 0) {
            ServletUtils.addError(PARAM_TITLE, "Zadejte název štítku!", env, null);
            return false;
        }

        String id = DiacriticRemover.getInstance().removeDiacritics(title.toLowerCase());
        Matcher matcher = reInvalidTitle.matcher(id);
        if (matcher.find()) {
            ServletUtils.addError(PARAM_TITLE, "Název štítku obsahuje nepovolené znaky!", env, null);
            return false;
        }
        id = URLManager.enforceRelativeURL(id);
        if ("edit".equals(id)) {
            ServletUtils.addError(PARAM_TITLE, "Zakázaný název štítku!", env, null);
            return false;
        }

        tag.setTitle(title);

        if (! setOnlyTitle) {
            Tag existingTag = TagTool.getById(id);
            if (existingTag != null) {
                ServletUtils.addError(PARAM_TITLE, "Zadaný název koliduje s existujícím štítkem " + existingTag.getTitle() + "!", env, null);
                return false;
            }
            tag.setId(id);
        }

        return true;
    }
}