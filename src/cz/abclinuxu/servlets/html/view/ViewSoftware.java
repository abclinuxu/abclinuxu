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
import cz.abclinuxu.data.User;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.extra.CompareCondition;
import cz.abclinuxu.persistance.extra.Field;
import cz.abclinuxu.persistance.extra.LimitQualifier;
import cz.abclinuxu.persistance.extra.Operation;
import cz.abclinuxu.persistance.extra.Qualifier;
import cz.abclinuxu.persistance.versioning.VersionedDocument;
import cz.abclinuxu.persistance.versioning.Versioning;
import cz.abclinuxu.persistance.versioning.VersioningFactory;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.ReadRecorder;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.security.Roles;
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
    public static final String ACTION_SEARCH = "search";

    public static final String VAR_FILTERS = "FILTERS";
    public static final String VAR_PARENTS = "PARENTS";
    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_ITEMS = "ITEMS";
    public static final String VAR_ITEM = "ITEM";
    public static final String VAR_CATEGORY = "CATEGORY";
    public static final String VAR_CATEGORIES = "CATEGORIES";
    public static final String VAR_LINKS = "FEED_LINKS";
    public static final String VAR_CHILDREN_MAP = "CHILDREN";
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

        // temporary: check permissions TODO remove when released officially
        User user = (User) env.get(Constants.VAR_USER);
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        if (!user.hasRole(Roles.SOFTWARE_ADMIN))
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

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

        Map filters = (Map) session.getAttribute(VAR_FILTERS);
        if (filters != null)
            env.put(VAR_FILTERS, filters);
        else
            env.put(VAR_FILTERS, Collections.EMPTY_MAP);

        if (ACTION_SEARCH.equals(action))
            return processSearch(request, env);

        if (relation.getChild() instanceof Category) {
            return processSection(request, relation, env);
        } else
            return processItem(request, relation, env);
    }

    private String processAlternatives(HttpServletRequest request, Map env) {
        SQLTool sqlTool = SQLTool.getInstance();
        List values = sqlTool.getPropertyValues(Constants.PROPERTY_ALTERNATIVE_SOFTWARE);
        // todo Case insensitive comparator, mozna v jakarta commons
        Collections.sort(values);
        env.put(VAR_ALTERNATIVES, values);
        return FMTemplateSelector.select("ViewSoftware", "alternatives", env, request);
    }

    private String processAlternative(HttpServletRequest request, String name, Map env) throws Exception {
        SQLTool sqlTool = SQLTool.getInstance();
        Map filters = new HashMap();
        name = URLDecoder.decode(name, "ISO-8859-2");
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
    private String processSection(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        SQLTool sqlTool = SQLTool.getInstance();

        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int count = getPageSize(params);

        List qualifiers = new ArrayList();
        qualifiers.add(new CompareCondition(Field.UPPER, Operation.EQUAL, new Integer(relation.getId())));
        Qualifier[] qa = new Qualifier[qualifiers.size()];

        // todo tohle jde prece porovnat bez SQL
        List categories = sqlTool.findCategoriesRelationsWithType(Category.SOFTWARE_SECTION, (Qualifier[]) qualifiers.toArray(qa));
        if (categories.size() > 0)
            env.put(VAR_CATEGORIES, Tools.syncList(categories));

        // todo proc sortovat podle datumu vytvoreni?
        qualifiers.add(Qualifier.SORT_BY_CREATED);
        qualifiers.add(Qualifier.ORDER_DESCENDING);
        qualifiers.add(new LimitQualifier(from, count));
        qa = new Qualifier[qualifiers.size()];

        Map filters = (Map) env.get(VAR_FILTERS);
        List items = sqlTool.findItemRelationsWithTypeWithFilters(Item.SOFTWARE, (Qualifier[]) qualifiers.toArray(qa), filters);
        if (items.size() > 0)
            env.put(VAR_ITEMS, Tools.syncList(items));

        List parents = persistance.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);
        env.put(VAR_CATEGORY, Tools.sync(relation.getChild()));

        return FMTemplateSelector.select("ViewSoftware", "swsekce", env, request);
    }

    /**
     * Processes search for given software
     */
    private String processSearch(HttpServletRequest request, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        SQLTool sqlTool = SQLTool.getInstance();

        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String name = (String) params.get(PARAM_NAME);

        List qualifiers = new ArrayList();
        qualifiers.add(new CompareCondition(Field.DATA, Operation.LIKE, "%<name>%"+name+"%</name>%"));
        Qualifier[] qa = new Qualifier[qualifiers.size()];

        List items = sqlTool.findItemRelationsWithType(Item.SOFTWARE, (Qualifier[]) qualifiers.toArray(qa));
        if (items.size() > 0)
            env.put(VAR_ITEMS, Tools.syncList(items));

        Relation relation = (Relation) env.get(VAR_RELATION);
        List parents = persistance.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);
        env.put(VAR_CATEGORY, Tools.sync(relation.getChild()));

        return FMTemplateSelector.select("ViewSoftware", "swsekce", env, request);
    }

    /**
     * Processes one software item.
     */
    private String processItem(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Item item = null;
        Relation upper = null;

        // todo ma tohle smysl? neni to jen slepe zkopirovano z ShowObject, kde byl vztah Item - Record?
        if (relation.getChild() instanceof Item) {
            item = (Item) relation.getChild();
            upper = relation;
        } else if (relation.getParent() instanceof Item) {
            item = (Item) relation.getParent();
            upper = new Relation(relation.getUpper());
        }

        Tools.sync(item);
        env.put(VAR_ITEM, item);
        ReadRecorder.log(item, Constants.COUNTER_READ, env);

        List parents = persistance.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        Map children = Tools.groupByType(item.getChildren());
        env.put(VAR_LINKS, children.get(Constants.TYPE_LINK));
        Tools.sync(upper);

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

    /**
     * Gets page size for found questions. Parameters take precendence over user settings.
     *
     * @return page size for found documents.
     */
    private int getPageSize(Map params) {
        int count = -1;
        String str = (String) params.get(PARAM_COUNT);
        if (str != null && str.length() > 0)
            count = Misc.parseInt(str, -1);
        if (count == -1)
            return AbcConfig.getFaqSectionCount();
        else
            return Misc.limit(count, 5, 50);
    }
}
