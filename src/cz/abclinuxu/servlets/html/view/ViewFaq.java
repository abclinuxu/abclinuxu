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
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.persistance.extra.*;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.versioning.Versioning;
import cz.abclinuxu.persistance.versioning.VersioningFactory;
import cz.abclinuxu.persistance.versioning.VersionedDocument;
import cz.abclinuxu.exceptions.NotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.dom4j.Element;
import org.dom4j.io.DOMWriter;
import freemarker.ext.dom.NodeModel;

/**
 * User: literakl
 * Date: 17.7.2005
 */
public class ViewFaq implements AbcAction {
    public static final String PARAM_RELATION_ID = "rid";
    /** n-th oldest object, where display from */
    public static final String PARAM_FROM = "from";
    /** how many object to display */
    public static final String PARAM_COUNT = "count";

    public static final String VAR_QUESTIONS = "QUESTIONS";
    public static final String VAR_ITEM = "ITEM";
    public static final String VAR_SECTION_SIZES = "SIZES";
    public static final String VAR_FAQ_XML = "XML";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_ID, Relation.class, params, request);
        if (relation==null)
            throw new NotFoundException("Stránka nebyla nalezena.");
        Tools.sync(relation);
        env.put(ShowObject.VAR_RELATION, relation);

        if (relation.getChild() instanceof Category) {
            if (relation.getId()==Constants.REL_FAQ)
                return processStart(request, env);
            else
                return processSection(request, relation, env);
        } else
            return processQuestion(request, relation, env);
    }

    /**
     * Processes start page with list of all FAQ sections.
     */
    private String processStart(HttpServletRequest request, Map env) throws Exception {
        SQLTool sqlTool = SQLTool.getInstance();
        Map sizes = sqlTool.getFaqSectionsSize();
        env.put(VAR_SECTION_SIZES, sizes);
        return FMTemplateSelector.select("ViewFaq", "start", env, request);
    }

    /**
     * Processes section with FAQ.
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
        int total = sqlTool.countItemRelationsWithType(Item.FAQ, (Qualifier[]) qualifiers.toArray(qa));

        qualifiers.add(Qualifier.SORT_BY_CREATED);
        qualifiers.add(Qualifier.ORDER_DESCENDING);
        qualifiers.add(new LimitQualifier(from, count));
        qa = new Qualifier[qualifiers.size()];

        List questions = sqlTool.findItemRelationsWithType(Item.FAQ, (Qualifier[]) qualifiers.toArray(qa));
        Tools.syncList(questions);

        Paging paging = new Paging(questions, from, count, total);
        env.put(VAR_QUESTIONS, paging);

        List parents = persistance.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        return FMTemplateSelector.select("ViewFaq", "list", env, request);
    }

    /**
     * Processes one frequently asked question.
     * todo zobrazit odkazy na nekolik dalsich otazek
     */
    private String processQuestion(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        List parents = persistance.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        Item item = (Item) relation.getChild();
        env.put(VAR_ITEM, item);
        env.put(VAR_FAQ_XML, NodeModel.wrap((new DOMWriter().write(item.getData()))));

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

        return FMTemplateSelector.select("ViewFaq", "view", env, request);
    }

    /**
     * Gets page size for found questions. Parameters take precendence over user settings.
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
