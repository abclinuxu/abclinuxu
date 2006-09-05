/*
 *  Copyright (C) 2006 Leos Literak
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
package cz.abclinuxu.data.view;

import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.PersistenceMapping;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.servlets.Constants;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;

import org.dom4j.Element;
import org.dom4j.Document;

/**
 * Cache for some tree of sections. It may contain additional information
 * like number of items within each section. It consists of list of SectionNodes.
 * @author literakl
 * @since 3.9.2006
 */
public class SectionTreeCache {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SectionTreeCache.class);

    private List<SectionNode> children;
    Map<Integer, SectionNode> mapById, mapByRelation;
    private String urlPrefix;
    private int id;

    public SectionTreeCache(String urlPrefix, int relationId) {
        this.urlPrefix = urlPrefix;
        this.id = relationId;
        children = new ArrayList<SectionNode>();
        mapById = Collections.emptyMap();
        mapByRelation = Collections.emptyMap();
    }

    /**
     * @return root SectionNodes
     */
    public List<SectionNode> getChildren() {
        return children;
    }

    /**
     * @param id category id
     * @return SectionNode with given id or null, if not found
     */
    public SectionNode getById(int id) {
        return mapById.get(id);
    }

    /**
     * @param rid parent relation id
     * @return SectionNode with given relation id or null, if not found
     */
    public SectionNode getByRelation(int rid) {
        return mapByRelation.get(rid);
    }

    /**
     * Loads complete section tree for relation identified by relationId
     * and initializes its properties (like number of items).
     */
    public void initialize() {
        try {
            Persistence persistence = PersistenceFactory.getPersistance();
            log.debug("SectionTree cache (" + urlPrefix + ", " + id + ") initialization started");
            Category category = (Category) persistence.findById(new Category(id));

            Map<Integer, SectionNode> directAccessMap = new HashMap<Integer, SectionNode>(100);
            SectionNode root = new SectionNode(null, category.getId(), 0);
            List subSections = Tools.syncList(category.getChildren());
            for (Iterator iter = subSections.iterator(); iter.hasNext();) {
                scanRelation(root, (Relation) iter.next(), directAccessMap);
            }

            loadNames(directAccessMap);
            refresh();
            setChildren(root.getChildren());

            log.debug("SectionTree cache (" + urlPrefix + ", " + id + ") initialization completed");
        } catch (Exception e) {
            log.error("SectionTree cache (" + urlPrefix + ", " + id + ") initialization failed", e);
        }
    }

    /**
     * Refreshes properties of every SectionNode within the tree (like number of items).
     */
    public void refresh() {
        try {
            SQLTool sqlTool = SQLTool.getInstance();
            Map<Integer, Integer> result = sqlTool.getItemsCountInSections(new ArrayList(mapById.keySet()));
            for (Iterator<Integer> iter = result.keySet().iterator(); iter.hasNext();) {
                Integer id = iter.next();
                SectionNode section = mapById.get(id);
                section.setItemsCount(result.get(id));
            }
        } catch (Exception e) {
            log.error("SectionTree cache (" + urlPrefix + ", " + id + ") refresh failed", e);
        }
    }

    private void setChildren(List<SectionNode> children) {
        Map<Integer, SectionNode> mapById = new HashMap<Integer, SectionNode>(100);
        Map<Integer, SectionNode> mapByRelation = new HashMap<Integer, SectionNode>(100);

        List<SectionNode> stack = new ArrayList<SectionNode>();
        stack.addAll(children);
        SectionNode node;
        while (stack.size() > 0) {
            node = stack.remove(0);
            mapById.put(node.getId(), node);
            mapByRelation.put(node.getRelationId(), node);
            stack.addAll(node.getChildren());
            Collections.sort(node.getChildren());
        }

        this.children = children;
        this.mapById = mapById;
        this.mapByRelation = mapByRelation;
    }

    /**
     * Adds specified section relation into given parent.
     * @param parent parent section
     * @param relation initialized relation containing Category as child
     * @param directAccessMap map of section id to SectionNode
     */
    private void scanRelation(SectionNode parent, Relation relation, Map<Integer, SectionNode> directAccessMap) {
        String url = relation.getUrl();
        int relationId = relation.getId();
        if (url == null)
            url = urlPrefix + "/show/" + relationId;

        Category category = (Category) relation.getChild();
        int id = category.getId();
        SectionNode section = new SectionNode(url, id, relationId);
        parent.addSubsection(section);
        directAccessMap.put(id, section);

        Qualifier qualifierType = new CompareCondition(Field.PARENT_TYPE, Operation.EQUAL,
                                                       PersistenceMapping.getGenericObjectType(category));
        Qualifier qualifierParent = new CompareCondition(Field.PARENT, Operation.EQUAL, id);
        SQLTool sqlTool = SQLTool.getInstance();
        List relations = sqlTool.findCategoriesRelations(new Qualifier[]{qualifierType, qualifierParent});
        for (Iterator iter = relations.iterator(); iter.hasNext();) {
            Relation child = (Relation) iter.next();
            scanRelation(section, child, directAccessMap);
        }
    }

    /**
     * Loads name and description for all sections.
     * @param directAccessMap
     */
    private void loadNames(Map<Integer, SectionNode> directAccessMap) {
        List categories = new ArrayList(directAccessMap.size());
        for (Iterator<Integer> iter = directAccessMap.keySet().iterator(); iter.hasNext();) {
            Integer id = iter.next();
            categories.add(new Category(id));
        }

        Tools.syncList(categories);
        Category category;
        Document document;
        Element element;
        SectionNode section;
        for (Iterator iter = categories.iterator(); iter.hasNext();) {
            category = (Category) iter.next();
            section = directAccessMap.get(category.getId());
            document = category.getData();
            element = (Element) document.selectSingleNode("/data/name");
            section.setName(element.getText());
            element = (Element) document.selectSingleNode("/data/note");
            if (element != null)
                section.setDescription(element.getText());
        }
    }

    private void print() {
        List stack = new ArrayList();
        stack.addAll(children);
        while (stack.size() > 0) {
            SectionNode node = (SectionNode) stack.remove(0);
            stack.addAll(0, node.getChildren());
            System.out.println(node.getUrl()+" "+node.getName()+" "+node.getSize());
        }
        System.out.println("\n\n");
    }

    public static void main(String[] args) {
        SectionTreeCache cache = new SectionTreeCache("/forum", Constants.CAT_FORUM);
        cache.initialize();
        cache.print();
        cache = new SectionTreeCache("/faq", Constants.CAT_FAQ);
        cache.initialize();
        cache.print();
        cache = new SectionTreeCache("/software", Constants.CAT_SOFTWARE);
        cache.initialize();
        cache.print();
    }
}
