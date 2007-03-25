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

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.versioning.VersionedDocument;
import cz.abclinuxu.persistence.versioning.Versioning;
import cz.abclinuxu.persistence.versioning.VersioningFactory;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.edit.EditDictionary;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.paging.Paging;
import org.dom4j.Document;
import org.dom4j.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Displays dictionary
 */
public class ShowDictionary implements AbcAction {
    public static final String PARAM_RELATION_ID_SHORT = "rid";
    public static final String PARAM_PREFIX = "prefix";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_FOUND = "FOUND";
    public static final String VAR_CURRENT_PREFIX = "CURRENT_PREFIX";
    public static final String VAR_ITEM = "ITEM";
    public static final String VAR_NEXT = "NEXT";
    public static final String VAR_PREVIOUS = "PREV";

    private final int PREFIX_LENGTH = UrlUtils.PREFIX_DICTIONARY.length() + 1;

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_ID_SHORT, Relation.class, params, request);

        if ( relation == null ) {
            String url = ServletUtils.combinePaths(request.getServletPath(), request.getPathInfo());
            if (url.length() > PREFIX_LENGTH) {
                // copied from URLMapper
                String newUrl = null;
                Object redirect = SQLTool.getInstance().findNewAddress(url);
                if (redirect instanceof String)
                    newUrl = (String) redirect;
                else if (redirect instanceof Relation) {
                    relation = (Relation) redirect;
                    newUrl = relation.getUrl();
                }

                if (newUrl != null) {
                    UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
                    urlUtils.redirect(response, newUrl);
                    return null;
                }

                String name = url.substring(PREFIX_LENGTH);
                params.put(EditDictionary.PARAM_NAME, name);
                ServletUtils.addMessage("Tento pojem nebyl ještě popsán. V tomto formuláři jej můžete vysvětlit jako první.", env, null);
                return FMTemplateSelector.select("Dictionary", "add", env, request);
            }
        }

        Tools.sync(relation);
        env.put(VAR_RELATION, relation);

        List parents = persistence.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        if (relation.getId() == Constants.REL_DICTIONARY)
            return processList(request, env);
        else
            return processDefinition(request, relation, env);
    }

    /**
     * Shows single dictionary definition.
     */
    static String processDefinition(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Item item = (Item) relation.getChild();
        env.put(VAR_ITEM, item);

        int revision = Misc.parseInt((String) params.get(ShowRevisions.PARAM_REVISION), -1);
        if (revision != -1) {
            Versioning versioning = VersioningFactory.getVersioning();
            VersionedDocument version = versioning.load(relation.getId(), revision);
            Document document = item.getData();
            Element monitor = (Element) document.selectSingleNode("/data/monitor");
            item.setData(version.getDocument());
            item.setUpdated(version.getCommited());
            item.setOwner(version.getUser());
            if (monitor != null) {
                monitor = monitor.createCopy();
                Element element = (Element) document.selectSingleNode("/data/monitor");
                if (element != null)
                    element.detach();
                document.getRootElement().add(monitor);
            }
        }

        SQLTool sqlTool = SQLTool.getInstance();
        List siblings = sqlTool.getNeighbourDictionaryItemRelations(item.getSubType(), true, 3);
        env.put(VAR_PREVIOUS, Tools.syncList(siblings));
        siblings = sqlTool.getNeighbourDictionaryItemRelations(item.getSubType(), false, 3);
        env.put(VAR_NEXT, Tools.syncList(siblings));
        return FMTemplateSelector.select("Dictionary", "show", env, request);
    }

    /**
     * Shows page with list of dictionary definitions.
     */
    static String processList(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        SQLTool sqlTool = SQLTool.getInstance();

        String prefix = (String) params.get(PARAM_PREFIX);
        prefix = sqlTool.protectSQLParameter(prefix);
        if (Misc.empty(prefix))
            prefix = "a";

        Qualifier[] qualifiers = new Qualifier[]{new CompareCondition(Field.SUBTYPE, Operation.LIKE, prefix + "%")};
        List data = sqlTool.findItemRelationsWithType(Item.DICTIONARY, qualifiers);
        Tools.syncList(data);
        Sorters2.byName(data);

        Paging found = new Paging(data, 0, data.size(), data.size(), qualifiers);
        env.put(VAR_FOUND, found);
        env.put(VAR_CURRENT_PREFIX, prefix);
        return FMTemplateSelector.select("Dictionary", "showList", env, request);
    }
}