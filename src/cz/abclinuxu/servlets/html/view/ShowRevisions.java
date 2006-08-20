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
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.versioning.VersioningFactory;
import cz.abclinuxu.persistence.versioning.Versioning;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;

/**
 * Shows revision history for selected document.
 * User: literakl
 * Date: 11.7.2005
 */
public class ShowRevisions implements AbcAction {
    public static final String PARAM_RELATION = "rid";
    public static final String PARAM_PREFIX = "prefix";
    public static final String PARAM_REVISION = "revize";

    /** list of VersionInfos */
    public static final String VAR_HISTORY = "HISTORY";
    public static final String VAR_RELATION = "RELATION";
    /** URL of object */
    public static final String VAR_URL = "OBJECT_URL";
    /** value of revision parameter to be appended after URL, then revision value shall be added. */
    public static final String VAR_REVISION_PARAM = "REVISION_PARAM";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Parametr rid je prázdný!");
        Tools.sync(relation);
        env.put(VAR_RELATION, relation);

        Versioning versioning = VersioningFactory.getVersioning();
        List history = versioning.getHistory(Integer.toString(relation.getId()));
        env.put(VAR_HISTORY, history);

        String url = relation.getUrl(), revisionParam = "?" + PARAM_REVISION + "=";
        if (url==null) {
            String prefix = (String) params.get(PARAM_PREFIX);
            url = prefix+"/show/"+relation.getId();
        }
        env.put(VAR_URL, url);
        env.put(VAR_REVISION_PARAM, revisionParam);

        return FMTemplateSelector.select("ShowRevisions", "view", env, request);
    }
}
