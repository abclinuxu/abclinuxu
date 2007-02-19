/*
 *  Copyright (C) 2006 Yin, Leos Literak
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

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.view.SectionTreeCache;
import cz.abclinuxu.data.view.SectionNode;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.versioning.VersionedDocument;
import cz.abclinuxu.persistence.versioning.Versioning;
import cz.abclinuxu.persistence.versioning.VersioningFactory;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.ReadRecorder;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.scheduler.VariableFetcher;
import org.dom4j.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URLDecoder;

/**
 * User: literakl
 * Date: 17.7.2005
 */
public class ViewSoftware implements AbcAction {
    /**
     * if set, it indicates to display parent in the relation of two categories
     */
    public static final String PARAM_PARENT = "parent";
    public static final String PARAM_RELATION_ID = "rid";
    /** n-th oldest object, where display from */
    public static final String PARAM_FROM = "from";
    /** how many object to display */
    public static final String PARAM_COUNT = "count";
    public static final String PARAM_ACTION = "action";
    public static final String PARAM_FILTER_UITYPE = "ui";
    public static final String PARAM_FILTER_LICENSES = "license";
    public static final String PARAM_NAME = "name";

    public static final String ACTION_FILTER = "filter";

    public static final String VAR_FILTERS = "FILTERS";
    public static final String VAR_PARENTS = "PARENTS";
    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_ITEMS = "ITEMS";
    public static final String VAR_ITEM = "ITEM";
    public static final String VAR_CATEGORY = "CATEGORY";
    public static final String VAR_CATEGORIES = "CATEGORIES";
    public static final String VAR_LINKS = "FEED_LINKS";
    public static final String VAR_CHILDREN_MAP = "CHILDREN";
    public static final String VAR_TREE_DEPTH = "DEPTH";
    public static final String VAR_SOFTWARE_NAME = "SOFTWARE";
    public static final String VAR_ALTERNATIVES = "ALTERNATIVES";

    static Pattern reAlternatives;
    static {
        reAlternatives = Pattern.compile("/software/alternativy(/(.*))?");
    }

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);
        HttpSession session = request.getSession();

        String uri = (String) env.get(Constants.VAR_REQUEST_URI);
        Matcher matcher = reAlternatives.matcher(uri);
        if (matcher.find()) {
            String name = matcher.group(2);
            if (Misc.empty(name))
                return processAlternatives(request, env);
            else
                return processAlternative(request, name, env);
        }

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_ID, Relation.class, params, request);
        if (relation == null)
            throw new NotFoundException("Stránka nebyla nalezena.");
        Tools.sync(relation);
        env.put(ShowObject.VAR_RELATION, relation);

        if (relation.getChild() instanceof Item) {
            Item item = (Item) relation.getChild();
            if (item.getType() != Item.SOFTWARE)
                return ShowObject.processItem(request, env, relation);
        } else if (relation.getChild() instanceof Category) {
            Category category = (Category) relation.getChild();
            if (category.getType() != Category.SOFTWARE_SECTION)
                return ViewCategory.processCategory(request, response, env, relation);
        }

        if (ACTION_FILTER.equals(action)) {
            Map filters = new HashMap();
            Object filterValue = params.get(PARAM_FILTER_UITYPE);
            if (filterValue != null)
                filters.put(Constants.PROPERTY_USER_INTERFACE, Tools.asSet(filterValue));
            filterValue = params.get(PARAM_FILTER_LICENSES);
            if (filterValue != null)
                filters.put(Constants.PROPERTY_LICENSE, Tools.asSet(filterValue));

            session.setAttribute(VAR_FILTERS, filters);
        }

        if (relation.getChild() instanceof Category) {
            return processSection(request, relation, env);
        } else
            return processItem(request, relation, env);
    }

    private String processAlternatives(HttpServletRequest request, Map env) {
        SQLTool sqlTool = SQLTool.getInstance();
        List values = sqlTool.getPropertyValues(Constants.PROPERTY_ALTERNATIVE_SOFTWARE);
        Collections.sort(values, String.CASE_INSENSITIVE_ORDER);
        env.put(VAR_ALTERNATIVES, values);
        return FMTemplateSelector.select("ViewSoftware", "alternatives", env, request);
    }

    private String processAlternative(HttpServletRequest request, String name, Map env) throws Exception {
        SQLTool sqlTool = SQLTool.getInstance();
        Map filters = new HashMap();
        name = URLDecoder.decode(name, "UTF-8");
        filters.put(Constants.PROPERTY_ALTERNATIVE_SOFTWARE, Tools.asSet(name));
        List items = sqlTool.findItemRelationsWithTypeWithFilters(Item.SOFTWARE, null, filters);
        if (items.size() > 0)
            env.put(VAR_ITEMS, Tools.syncList(items));
        env.put(VAR_SOFTWARE_NAME, name);
        return FMTemplateSelector.select("ViewSoftware", "alternative", env, request);
    }

    /**
     * Processes section with software items.
     */
    public static String processSection(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance();
        SQLTool sqlTool = SQLTool.getInstance();

        Map filters = (Map) request.getSession().getAttribute(VAR_FILTERS);
        if (filters == null)
            filters = Collections.EMPTY_MAP;
        env.put(VAR_FILTERS, filters);

        SectionTreeCache softwareTree = VariableFetcher.getInstance().getSoftwareTree();
        List<SectionNode> categories = null;
        int depth = 0;
        if (relation.getId() == Constants.REL_SOFTWARE) {
            categories = softwareTree.getChildren();
            depth = 3;
        } else {
            SectionNode sectionNode = softwareTree.getByRelation(relation.getId());
            if (sectionNode != null) {
                categories = sectionNode.getChildren();
                depth = sectionNode.getDepth();
            }
        }
        env.put(VAR_CATEGORIES, categories);
        env.put(VAR_TREE_DEPTH, depth);

        List qualifiers = new ArrayList();
        qualifiers.add(new CompareCondition(Field.UPPER, Operation.EQUAL, relation.getId()));
        Qualifier[] qa = new Qualifier[qualifiers.size()];
//        qualifiers.add(new LimitQualifier(from, count));

        List items = sqlTool.findItemRelationsWithTypeWithFilters(Item.SOFTWARE, (Qualifier[]) qualifiers.toArray(qa), filters);
        if (items.size() > 0)
            env.put(VAR_ITEMS, Tools.syncList(items));

        List parents = persistence.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);
        env.put(VAR_CATEGORY, Tools.sync(relation.getChild()));

        return FMTemplateSelector.select("ViewSoftware", "swsekce", env, request);
    }

    /**
     * Processes one software item.
     */
    public static String processItem(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Item item = (Item) relation.getChild();

        Tools.sync(item);
        env.put(VAR_ITEM, item);
        ReadRecorder.log(item, Constants.COUNTER_READ, env);

        List parents = persistence.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        Map children = Tools.groupByType(item.getChildren());
        List links = (List) children.get(Constants.TYPE_LINK);
        if (links != null) {
            Sorters2.byDate(links, Sorters2.DESCENDING);
            env.put(VAR_LINKS, links);
        }

        // todo tohle take proverit
        String revision = (String) params.get(ShowRevisions.PARAM_REVISION);
        if (revision != null) {
            Versioning versioning = VersioningFactory.getVersioning();
            VersionedDocument version = versioning.load(Integer.toString(relation.getId()), revision);
            Element monitor = (Element) item.getData().selectSingleNode("/data/monitor");
            item.setData(version.getDocument());
            item.setUpdated(version.getCommited());
            item.setOwner(Integer.parseInt(version.getUser()));
            if (monitor != null) {
                monitor = monitor.createCopy();
                Element element = (Element) item.getData().selectSingleNode("/data/monitor");
                if (element != null)
                    element.detach();
                item.getData().getRootElement().add(monitor);
            }
        }

        return FMTemplateSelector.select("ViewSoftware", "software", env, request);
    }
}
