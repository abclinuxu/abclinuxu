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
    private int id, cacheSize = 100;
    private float cacheFactor = 0.95f;
    private boolean loadDescriptions = true, loadSizes = true, loadLastItem = false;

    /**
     * Creates new tree cache for some category with given prefix for urls.
     * @param urlPrefix prefix for relations with constructed urls
     * @param id category id
     */
    public SectionTreeCache(String urlPrefix, int id) {
        this.urlPrefix = urlPrefix;
        this.id = id;
        children = new ArrayList<SectionNode>();
        mapById = Collections.emptyMap();
        mapByRelation = Collections.emptyMap();
    }

    /**
     * Sets default size of caches
     * @param cacheSize default value
     */
    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    /**
     * Sets default map load factor
     * @param cacheFactor default value
     */
    public void setCacheFactor(float cacheFactor) {
        this.cacheFactor = cacheFactor;
    }

    /**
     * Whether category descriptions will be fetched for SectionNodes
     * @param loadDescriptions true means to load descriptions
     */
    public void setLoadDescriptions(boolean loadDescriptions) {
        this.loadDescriptions = loadDescriptions;
    }

    /**
     * Whether number of child items shall be fetched for SectionNodes
     * @param loadSizes true means to load sizes
     */
    public void setLoadSizes(boolean loadSizes) {
        this.loadSizes = loadSizes;
    }

    /**
     * Whether last child item shall be fetched for SectionNodes
     * @param loadLastItem true means to load last item
     */
    public void setLoadLastItem(boolean loadLastItem) {
        this.loadLastItem = loadLastItem;
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
     * Loads complete section tree for relation identified by relationId.
     */
    public void initialize() {
        try {
            Persistence persistence = PersistenceFactory.getPersistance();
            log.debug("SectionTree cache (" + urlPrefix + ", " + id + ") initialization started");
            Category category = (Category) persistence.findById(new Category(id));

            Map<Integer, SectionNode> directAccessMap = new HashMap<Integer, SectionNode>(cacheSize, cacheFactor);
            SectionNode root = new SectionNode(null, category.getId(), 0);
            List subSections = Tools.syncList(category.getChildren());
            for (Iterator iter = subSections.iterator(); iter.hasNext();) {
                scanRelation(root, (Relation) iter.next(), directAccessMap);
            }

            loadNames(directAccessMap);
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
            log.debug("SectionTree cache (" + urlPrefix + ", " + id + ") refresh started");
            SQLTool sqlTool = SQLTool.getInstance();
            if (loadSizes && ! loadLastItem) {
                Map<Integer, Integer> result = sqlTool.getItemsCountInSections(new ArrayList(mapById.keySet()));
                for (Integer id : result.keySet()) {
                    SectionNode section = mapById.get(id);
                    section.setItemsCount(result.get(id));
                }
            }
            if (loadLastItem) {
                Map<Integer, Integer[]> result = sqlTool.getLastItemAndItemsCountInSections(new ArrayList(mapById.keySet()));
                for (Integer id : result.keySet()) {
                    SectionNode section = mapById.get(id);
                    Integer[] data = result.get(id);
                    section.setItemsCount(data[0]);
                    section.setLastItem(data[1]);
                }
            }
            log.debug("SectionTree cache (" + urlPrefix + ", " + id + ") refresh completed");
        } catch (Exception e) {
            log.error("SectionTree cache (" + urlPrefix + ", " + id + ") refresh failed", e);
        }
    }

    private void setChildren(List<SectionNode> children) {
        Map<Integer, SectionNode> mapById = new HashMap<Integer, SectionNode>(cacheSize, cacheFactor);
        Map<Integer, SectionNode> mapByRelation = new HashMap<Integer, SectionNode>(cacheSize, cacheFactor);
        Collections.sort(children);

        List<SectionNode> stack = new ArrayList<SectionNode>();
        stack.addAll(children);
        SectionNode section;
        while (stack.size() > 0) {
            section = stack.remove(0);
            mapById.put(section.getId(), section);
            mapByRelation.put(section.getRelationId(), section);
            stack.addAll(section.getChildren());
            Collections.sort(section.getChildren());
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
        SectionNode section = directAccessMap.get(id);
        if (section != null) // there are multiple relations to this category
            return;

        section = new SectionNode(url, id, relationId);
        parent.addSubsection(section);
        directAccessMap.put(id, section);

        Qualifier qualifierType = new CompareCondition(Field.PARENT_TYPE, Operation.EQUAL,
                                                       PersistenceMapping.getGenericObjectType(category));
        Qualifier qualifierParent = new CompareCondition(Field.PARENT, Operation.EQUAL, id);
        SQLTool sqlTool = SQLTool.getInstance();
        List relations = sqlTool.findCategoriesRelations(new Qualifier[]{qualifierType, qualifierParent});
        for (Object relation1 : relations) {
            Relation child = (Relation) relation1;
            scanRelation(section, child, directAccessMap);
        }
    }

    /**
     * Loads name and description for all sections.
     * @param directAccessMap
     */
    private void loadNames(Map<Integer, SectionNode> directAccessMap) {
        List categories = new ArrayList(directAccessMap.size());
        for (Integer id : directAccessMap.keySet())
            categories.add(new Category(id));

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
            if (loadDescriptions) {
                element = (Element) document.selectSingleNode("/data/note");
                if (element != null)
                    section.setDescription(element.getText());
            }
        }
    }

    private void print() {
        List stack = new ArrayList();
        stack.addAll(children);
        while (stack.size() > 0) {
            SectionNode node = (SectionNode) stack.remove(0);
            stack.addAll(0, node.getChildren());
            System.out.println(node.getUrl()+" "+node.getName()+", size="+node.getSize()+" , last="+node.getLastItem());
        }
        System.out.println("\n\n");
    }

    public static void main(String[] args) {
        SectionTreeCache cache;
        cache = new SectionTreeCache("/forum", Constants.CAT_FORUM);
        cache.initialize();
        cache.setLoadLastItem(true);
        long before = System.currentTimeMillis();
        cache.refresh();
//        cache.print();
//        cache = new SectionTreeCache("/faq", Constants.CAT_FAQ);
//        cache.initialize();
//        cache.refresh();
//        cache.print();
//        cache = new SectionTreeCache("/software", Constants.CAT_SOFTWARE);
//        cache.initialize();
//        cache.refresh();
//        cache.print();
//        cache = new SectionTreeCache("/hardware", Constants.CAT_386);
//        cache.initialize();
//        cache.refresh();
//        cache.print();
//        cache = new SectionTreeCache("/clanky", Constants.CAT_ARTICLES);
//        cache.initialize();
//        cache.refresh();
        long end = System.currentTimeMillis();
        cache.print();
        System.out.println("total = " + (end-before));
    }
}
