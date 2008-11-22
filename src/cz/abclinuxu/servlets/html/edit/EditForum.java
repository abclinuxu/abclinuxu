/*
 *  Copyright (C) 2008 Leos Literak
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

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.view.ShowObject;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.parser.clean.HtmlPurifier;
import cz.abclinuxu.utils.parser.clean.HtmlChecker;
import cz.abclinuxu.utils.parser.clean.Rules;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 *
 * @author lubos
 */
public class EditForum implements AbcAction {
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_NOTE = "note";
    public static final String PARAM_RULES = "rules";

    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditForum.class);

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        if (ServletUtils.handleMaintainance(request, env)) {
            response.sendRedirect(response.encodeRedirectURL("/"));
            return null;
        }

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Chybí parametr relationId!");

        Tools.sync(relation);

        env.put(ShowObject.VAR_RELATION, relation);

        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        if ( ! Tools.permissionsFor(user, relation).canModify() ||
                ! Tools.permissionsFor(user, relation.getUpper()).canModify()) {

            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
        }

        if (ACTION_EDIT.equals(action))
            return actionEditStep1(request, response, env);

        if (ACTION_EDIT_STEP2.equals(action)) {
            ActionProtector.ensureContract(request, EditForum.class, true, true, true, false);
            return actionEditStep2(request, response, env);
        }

        throw new MissingArgumentException("Chybí parametr action!");
    }

    protected String actionEditStep1(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Category category = (Category) relation.getChild();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Document document = category.getData();

        params.put(PARAM_NAME, category.getTitle());
        Node node = document.selectSingleNode("data/note");
        if (node != null)
            params.put(PARAM_NOTE, node.getText());

        node = document.selectSingleNode("data/rules");
        if (node != null)
            params.put(PARAM_RULES, node.getText());

        return FMTemplateSelector.select("EditForum", "edit", env, request);
    }

    protected String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Relation relation = (Relation) env.get(ShowObject.VAR_RELATION);
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Category category = (Category) relation.getChild();
        Document document = category.getData();

        boolean canContinue = setDescription(params, document, env);
        canContinue &= setRules(params, document, env);
        canContinue &= setName(params, category, env);

        if (!canContinue)
            FMTemplateSelector.select("EditForum", "edit", env, request);

        persistence.update(category);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
        return null;
    }

    private boolean setDescription(Map params, Document document, Map env) {
        String tmp = (String) params.get(PARAM_NOTE);
        tmp = Misc.filterDangerousCharacters(tmp);
        if (tmp != null && tmp.length() > 0) {
            try {
                tmp = HtmlPurifier.clean(tmp);
                HtmlChecker.check(Rules.WIKI, tmp);
            } catch (Exception e) {
                ServletUtils.addError(PARAM_NOTE, e.getMessage(), env, null);
                return false;
            }
            Element element = DocumentHelper.makeElement(document, "data/note");
            element.setText(tmp);
        } else {
            Element element = document.getRootElement().element("note");
            if (element != null)
                element.detach();
        }
        return true;
    }

    private boolean setRules(Map params, Document document, Map env) {
        String tmp = (String) params.get(PARAM_RULES);
        tmp = Misc.filterDangerousCharacters(tmp);
        if (tmp != null && tmp.length() > 0) {
            try {
                tmp = HtmlPurifier.clean(tmp);
                HtmlChecker.check(Rules.WIKI, tmp);
            } catch (Exception e) {
                ServletUtils.addError(PARAM_RULES, e.getMessage(), env, null);
                return false;
            }
            Element element = DocumentHelper.makeElement(document, "data/rules");
            element.setText(tmp);
        } else {
            Element element = document.getRootElement().element("rules");
            if (element != null)
                element.detach();
        }
        return true;
    }

    private boolean setName(Map params, Category category, Map env) {
        String tmp = (String) params.get(PARAM_NAME);
        tmp = Misc.filterDangerousCharacters(tmp);
        if (Misc.empty(tmp)) {
            ServletUtils.addError(PARAM_NAME, "Zadejte jméno sekce!", env, null);
            return false;
        }
        category.setTitle(tmp);
        return true;
    }
}
