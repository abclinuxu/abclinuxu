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
package cz.abclinuxu.utils.offline;

import cz.abclinuxu.data.*;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.cache.LRUCache;
import cz.abclinuxu.persistence.extra.*;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.view.ShowObject;
import cz.abclinuxu.servlets.html.view.ViewCategory;
import cz.abclinuxu.servlets.html.view.ViewFaq;
import cz.abclinuxu.servlets.html.view.ShowArticle;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.template.TemplateSelector;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.DateTool;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.data.view.NewsCategories;
import cz.abclinuxu.utils.paging.Paging;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.ext.dom.NodeModel;

import java.io.File;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.*;
import java.util.prefs.Preferences;

import org.dom4j.io.DOMWriter;
import org.dom4j.Element;
import org.dom4j.Document;

/**
 * This class is responsible for dumping all
 * objects to hard disc. Objects are stored
 * in files, where directory is computed as
 * relationId modulo 26 represented as ascii
 * character (27%26=1 => 'a') a filename
 * consists of string "relace" and relationId
 * padded to 5 digits.
 */
public class Dump implements Configurable {
    static final String VAR_ONLINE_URL = "ONLINE";
    static final String VAR_DATA = "RESULT";

    public static final String PORTAL_URL = "http://www.abclinuxu.cz";
    public static final String LOCAL_PATH = "../..";
    public static final String IMAGES_LOCAL_PATH = "../../..";

    Persistence persistence;
    SQLTool sqlTool;
    DecimalFormat df;
    Configuration config;
    Map indexed = new HashMap(120000);
    private final int PAGE_SIZE = 30;

    public static void main(String[] args) throws Exception {
        Dump dumper = new Dump();
        dumper.execute();
    }

    public Dump() throws Exception {
        ConfigurationManager.getConfigurator().configureMe(this);
        persistence = PersistenceFactory.getPersistence();
        persistence.setCache(new LRUCache(5000));
        sqlTool = SQLTool.getInstance();
        String templateURI = AbcConfig.calculateDeployedPath("WEB-INF/conf/templates.xml");
        TemplateSelector.initialize(templateURI);

        df = new DecimalFormat("#####");
        df.setDecimalSeparatorAlwaysShown(false);
        df.setMinimumIntegerDigits(6);
        df.setMaximumIntegerDigits(6);

        config = FMUtils.getConfiguration();
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        config.setTemplateUpdateDelay(1000);
        config.setSharedVariable(Constants.VAR_TOOL,new Tools());
        config.setSharedVariable(Constants.VAR_DATE_TOOL,new DateTool());
        config.setSharedVariable(Constants.VAR_SORTER,new Sorters2());
        config.setSharedVariable(Constants.VAR_NEWS_CATEGORIES, NewsCategories.getInstance());
        config.setSharedVariable("DUMP",this);
    }

    void execute() throws Exception {
        File dirRoot = new File("objects");
        dirRoot.mkdirs();

        Relation hardware = (Relation) persistence.findById(new Relation(Constants.REL_HARDWARE));
        Relation drivers = (Relation) persistence.findById(new Relation(Constants.REL_DRIVERS));
        Relation articles = (Relation) persistence.findById(new Relation(Constants.REL_ARTICLES));
//        Relation news = new Relation(Constants.REL_NEWS);

        long start = System.currentTimeMillis();
        dumpIndex(dirRoot);
        dumpArticles(dirRoot, articles);
//        dumpAllNews(dirRoot, news);
        dumpTree(drivers, dirRoot, UrlUtils.PREFIX_DRIVERS);
        dumpTree(hardware, dirRoot, UrlUtils.PREFIX_HARDWARE);
        dumpForums(dirRoot);
        dumpFaqs(dirRoot);
        dumpDictionary(dirRoot);
        long end = System.currentTimeMillis();
        System.out.println("Dumping of "+indexed.size()+" documents took "+(end-start)/1000+" seconds.");
    }

    private void dumpIndex(File dirRoot) throws Exception {
        Map env = new HashMap();
        env.put("HARDWARE", new Integer(Constants.REL_HARDWARE));
        env.put("DRIVERS", new Integer(Constants.REL_DRIVERS));
        env.put("ARTICLES", new Integer(Constants.REL_ARTICLES));
//        env.put("NEWS", new Integer(Constants.REL_NEWS));
        env.put("FORUM", new Integer(Constants.REL_FORUM));
        env.put("FAQ", new Integer(Constants.REL_FAQ));
        env.put("DICTIONARY", new Integer(Constants.REL_DICTIONARY));

        String name = FMTemplateSelector.select("ViewIndex", "show", env, "offline");
        File file = new File(dirRoot, "index.html");
        FMUtils.executeTemplate(name, env, file);
        return;
    }

    /**
     * Recursively dumps relation and all its objects.
     */
    void dumpTree(Relation relation, File currentDir, String prefix) throws Exception {
        if (hasBeenIndexed(relation))
            return;

        Tools.sync(relation);
        File file = getFileName(relation,currentDir);
        GenericObject obj = relation.getChild();
        if ( obj instanceof Item ) {
            dumpItem(relation, (Item) obj, file, null, prefix);
        } else if ( obj instanceof Category ) {
            dumpCategory(relation, (Category) obj, file, prefix);
        }

        for (Iterator iter = obj.getChildren().iterator(); iter.hasNext();) {
            dumpTree( (Relation)iter.next(), currentDir, prefix);
        }
    }

    /**
     * dumps article into html file.
     */
    void dumpItem(Relation relation, Item item, File file, List parents, String prefix) throws Exception {
        if (hasBeenIndexed(relation))
            return;
        setIndexed(relation);

        Map env = new HashMap();
        env.put(ShowObject.VAR_RELATION, relation);
        env.put(VAR_ONLINE_URL, PORTAL_URL+prefix+"/show/"+relation.getId());

        if (parents==null) {
            parents = persistence.findParents(relation);
        } else {
            parents = new ArrayList(parents);
            parents.add(relation);
        }
        env.put(ShowObject.VAR_PARENTS,parents);

        Tools.sync(item);
        env.put(ShowObject.VAR_ITEM,item);
        String name = null;

        if ( item.getType()==Item.DISCUSSION ) {
            name = FMTemplateSelector.select("ShowObject", "discussion", env, "offline");
            FMUtils.executeTemplate(name,env,file);
            return;
        }
        if ( item.getType()==Item.FAQ ) {
            env.put(VAR_ONLINE_URL, PORTAL_URL + relation.getUrl());
            name = FMTemplateSelector.select("ViewFaq", "view", env, "offline");
            FMUtils.executeTemplate(name, env, file);
            return;
        }
        if ( item.getType()==Item.DRIVER ) {
            name = FMTemplateSelector.select("ShowObject", "driver", env, "offline");
            FMUtils.executeTemplate(name, env, file);
            return;
        }

        Map children = Tools.groupByType(item.getChildren());
        env.put(ShowObject.VAR_CHILDREN_MAP,children);

        if ( item.getType()==Item.ARTICLE ) {
            setArticleRelatedResources(env);
            name = FMTemplateSelector.select("ShowObject", "article", env, "offline");
            FMUtils.executeTemplate(name, env, file);
            return;
        }

        if ( item.getType()==Item.DICTIONARY ) {
            env.put(VAR_ONLINE_URL, PORTAL_URL + prefix + "/" + item.getSubType());
            name = FMTemplateSelector.select("Dictionary", "show", env, "offline");
            FMUtils.executeTemplate(name, env, file);
            return;
        }

        if ( item.getType()==Item.HARDWARE ) {
            List records = (List) children.get(Constants.TYPE_RECORD);
            Record record = null;
            if ( records!=null && records.size()>0 )
                record = (Record) ((Relation)records.get(0)).getChild();
            else
                return;
            if ( ! record.isInitialized() )
                persistence.synchronize(record);

            if ( record.getType()== Record.HARDWARE )
                name = FMTemplateSelector.select("ShowObject", "hardware", env, "offline");
            else
                return;

            FMUtils.executeTemplate(name,env,file);
        }
    }

    private void setArticleRelatedResources(Map env) {
        Map children = (Map) env.get(ShowObject.VAR_CHILDREN_MAP);
        List list = (List) children.get(Constants.TYPE_RECORD);
        Record record = (Record) ((Relation) list.get(0)).getChild();
        Document document = record.getData();

        List nodes = document.selectNodes("/data/related/link");
        if (nodes != null && nodes.size() > 0) {
            List articles = new ArrayList(nodes.size());
            for (Iterator iter = nodes.iterator(); iter.hasNext();) {
                Element element = (Element) iter.next();
                ShowArticle.Link link = new ShowArticle.Link(element.getText(), element.attributeValue("url"), element.attributeValue("description"));
                articles.add(link);
            }
            env.put(ShowArticle.VAR_RELATED_ARTICLES, articles);
        }

        nodes = document.selectNodes("/data/resources/link");
        if (nodes != null && nodes.size() > 0) {
            List resources = new ArrayList(nodes.size());
            for (Iterator iter = nodes.iterator(); iter.hasNext();) {
                Element element = (Element) iter.next();
                ShowArticle.Link link = new ShowArticle.Link(element.getText(), element.attributeValue("url"), element.attributeValue("description"));
                resources.add(link);
            }
            env.put(ShowArticle.VAR_RELATED_RESOURCES, resources);
        }
    }

    /**
     * dumps category into html file.
     */
    void dumpCategory(Relation relation, Category category, File file, String prefix) throws Exception {
        if (hasBeenIndexed(relation))
            return;
        setIndexed(relation);

        Map env = new HashMap();
        env.put(ShowObject.VAR_RELATION,relation);
        env.put(VAR_ONLINE_URL, PORTAL_URL+prefix+"/dir/"+relation.getId());

        List parents = persistence.findParents(relation);
        env.put(ShowObject.VAR_PARENTS,parents);

        Tools.sync(category);
        env.put(ViewCategory.VAR_CATEGORY,category);
        Map children = Tools.groupByType(category.getChildren());
        env.put(ShowObject.VAR_CHILDREN_MAP, children);

        String name = FMTemplateSelector.select("ViewCategory","sekce", env, "offline");
        FMUtils.executeTemplate(name,env,file);
    }

    /**
     * Dumps articles.
     */
    void dumpArticles(File currentDir, Relation articles) throws Exception {
        File file = getFileName(articles, currentDir, 0);
        // todo ignorovat ve vypisu rubrik Ke stahnuti
        dumpCategory(articles, (Category) articles.getChild(), file, UrlUtils.PREFIX_CLANKY);

        List sections = articles.getChild().getChildren();
        int total, i, count = PAGE_SIZE;

        for (Iterator iter = sections.iterator(); iter.hasNext();) {
            Relation sectionRelation = (Relation) iter.next();
            if (hasBeenIndexed(sectionRelation))
                continue;
            setIndexed(sectionRelation);

            Map env = new HashMap();
            env.put(ShowObject.VAR_RELATION, sectionRelation);
            env.put(VAR_ONLINE_URL, PORTAL_URL + "/clanky/dir/" + sectionRelation.getId());
            env.put(ViewCategory.VAR_CATEGORY, sectionRelation.getChild());

            List parents = persistence.findParents(sectionRelation);
            env.put(ShowObject.VAR_PARENTS, parents);

            int sectionId = sectionRelation.getChild().getId();
            total = sqlTool.countArticleRelations(sectionId);

            for (i = 0; i < total;) {
                Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(i, count)};
                List data = sqlTool.findArticleRelations(qualifiers, sectionId);
                Tools.syncList(data);
                Paging paging = new Paging(data, i, count, total);
                env.put(VAR_DATA, paging);
                i += data.size();

                String template = FMTemplateSelector.select("ViewCategory", "rubrika", env, "offline");
                file = getFileName(sectionRelation, currentDir, paging.getPageIndex().intValue());
                FMUtils.executeTemplate(template, env, file);

                for (Iterator iter2 = data.iterator(); iter2.hasNext();) {
                    Relation article = (Relation) iter2.next();
                    file = getFileName(article, currentDir);
                    dumpItem(article, (Item) article.getChild(), file, parents, UrlUtils.PREFIX_CLANKY);
                }
            }
        }
    }

    /**
     * Dumps all news.
     */
    void dumpAllNews(File currentDir, Relation news_section) throws Exception {
        int total = sqlTool.countNewsRelations(), i, count = PAGE_SIZE;
        Relation relation, sectionNews = (Relation) persistence.findById(new Relation(Constants.REL_NEWS));

        for (i = 0; i < total;) {
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(i, count)};
            List data = sqlTool.findNewsRelations(qualifiers);
            Tools.syncList(data);
            Paging paging = new Paging(data, i, count, total);

            Map env = new HashMap();
            env.put(VAR_DATA, paging);
            env.put(ShowObject.VAR_RELATION, news_section);
            env.put(VAR_ONLINE_URL, PORTAL_URL + UrlUtils.PREFIX_NEWS);
            i += data.size();

            String template = FMTemplateSelector.select("ViewCategory", "news", env, "offline");
            File file = getFileName(news_section, currentDir, paging.getPageIndex().intValue());
            FMUtils.executeTemplate(template, env, file);
            setIndexed(news_section);

            for (Iterator iter2 = data.iterator(); iter2.hasNext();) {
                relation = (Relation) iter2.next();
                file = getFileName(relation, currentDir);
                dumpNewsItem(relation, (Item) relation.getChild(), sectionNews, file);
            }
        }
    }

    void dumpNewsItem(Relation relation, Item item, Relation sectionNews, File file) throws Exception {
        if (hasBeenIndexed(relation))
            return;
        setIndexed(relation);

        Map env = new HashMap();
        env.put(ShowObject.VAR_RELATION, relation);
        env.put(VAR_ONLINE_URL, PORTAL_URL + relation.getUrl());

        List parents = new ArrayList(2);
        parents.add(sectionNews);
        parents.add(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        env.put(ShowObject.VAR_ITEM, item);
        env.put(ShowObject.VAR_CHILDREN_MAP, Tools.groupByType(item.getChildren()));

        String name = FMTemplateSelector.select("ShowObject", "news", env, "offline");
        FMUtils.executeTemplate(name, env, file);
    }

    /**
     * Dumps all discussions.
     * @param currentDir
     * @throws Exception
     */
    void dumpForums(File currentDir) throws Exception {
        List forums = sqlTool.findCategoryRelationsWithType(Category.FORUM, null);
        Tools.syncList(forums);

        Relation forum = (Relation) Tools.sync(new Relation(Constants.REL_FORUM));
        File file = getFileName(forum, currentDir, 0);
        dumpForum(forum, (Category) forum.getChild(), file);

        int total, i, count = PAGE_SIZE;
        Relation relation, relation2;
        for (Iterator iter = forums.iterator(); iter.hasNext();) {
            relation = (Relation) iter.next();
            if (hasBeenIndexed(relation))
                continue;
            setIndexed(relation);

            Map env = new HashMap();
            env.put(ShowObject.VAR_RELATION, relation);
            env.put(VAR_ONLINE_URL, PORTAL_URL + "/forum/dir/" + relation.getId());
            env.put(ViewCategory.VAR_CATEGORY, relation.getChild());

            List parents = persistence.findParents(relation);
            env.put(ShowObject.VAR_PARENTS, parents);

            total = sqlTool.countDiscussionRelationsWithParent(relation.getId());
            for (i = 0; i < total;) {
                Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(i, count)};
                List data = sqlTool.findDiscussionRelationsWithParent(relation.getId(), qualifiers);
                Tools.syncList(data);
                Paging paging = new Paging(data, i, count, total);
                env.put(VAR_DATA, paging);
                i += data.size();

                String template = FMTemplateSelector.select("ShowForum", "show", env, "offline");
                file = getFileName(relation, currentDir, paging.getPageIndex().intValue());
                FMUtils.executeTemplate(template, env, file);

                for (Iterator iter2 = data.iterator(); iter2.hasNext();) {
                    relation2 = (Relation) iter2.next();
                    file = getFileName(relation2, currentDir);
                    dumpItem(relation2, (Item) relation2.getChild(), file, parents, UrlUtils.PREFIX_FORUM);
                }
            }
        }
    }

    /**
     * dumps all forums page into html file.
     */
    void dumpForum(Relation relation, Category category, File file) throws Exception {
        if (hasBeenIndexed(relation))
            return;
        setIndexed(relation);

        Map env = new HashMap();
        env.put(ShowObject.VAR_RELATION, relation);
        env.put(VAR_ONLINE_URL, PORTAL_URL + "/poradna");

        List parents = persistence.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        Tools.sync(category);
        env.put(ViewCategory.VAR_CATEGORY, category);

        String name = FMTemplateSelector.select("ShowForum", "main", env, "offline");
        FMUtils.executeTemplate(name, env, file);
    }

    /**
     * Dumps all FAQ.
     * @param currentDir
     * @throws Exception
     */
    void dumpFaqs(File currentDir) throws Exception {
        Relation mainSection = (Relation) Tools.sync(new Relation(Constants.REL_FAQ));
        File file = getFileName(mainSection, currentDir, 0);
        Category category = (Category) mainSection.getChild();
        List sections = category.getChildren();
        Tools.syncList(sections);
        dumpFaqIntro(mainSection, category, file);

        int total, i, count = PAGE_SIZE;
        Relation relation, relation2;
        for (Iterator iter = sections.iterator(); iter.hasNext();) {
            relation = (Relation) iter.next();
            if (hasBeenIndexed(relation))
                continue;
            setIndexed(relation);

            Map env = new HashMap();
            env.put(ShowObject.VAR_RELATION, relation);
            env.put(VAR_ONLINE_URL, PORTAL_URL + relation.getUrl());
            env.put(ViewCategory.VAR_CATEGORY, relation.getChild());

            List parents = new ArrayList();
            parents.add(mainSection);
            parents.add(relation);
            env.put(ShowObject.VAR_PARENTS, parents);

            List qualifiers = new ArrayList();
            qualifiers.add(new CompareCondition(Field.UPPER, Operation.EQUAL, new Integer(relation.getId())));
            Qualifier[] qa = new Qualifier[qualifiers.size()];
            total = sqlTool.countItemRelationsWithType(Item.FAQ, (Qualifier[]) qualifiers.toArray(qa));
            qualifiers.add(Qualifier.SORT_BY_CREATED);
            qualifiers.add(Qualifier.ORDER_DESCENDING);

            for (i = 0; i < total;) {
                List tmpQualifiers = new ArrayList(qualifiers);
                tmpQualifiers.add(new LimitQualifier(i, count));
                qa = new Qualifier[tmpQualifiers.size()];

                List data = sqlTool.findItemRelationsWithType(Item.FAQ, (Qualifier[]) tmpQualifiers.toArray(qa));
                Tools.syncList(data);

                Paging paging = new Paging(data, i, count, total);
                env.put(VAR_DATA, paging);
                i += data.size();

                String template = FMTemplateSelector.select("ViewFaq", "list", env, "offline");
                file = getFileName(relation, currentDir, paging.getPageIndex().intValue());
                FMUtils.executeTemplate(template, env, file);

                for (Iterator iter2 = data.iterator(); iter2.hasNext();) {
                    relation2 = (Relation) iter2.next();
                    file = getFileName(relation2, currentDir);
                    dumpItem(relation2, (Item) relation2.getChild(), file, parents, UrlUtils.PREFIX_FAQ);
                }
            }
        }
    }

    /**
     * dumps all faqs page into html file.
     */
    void dumpFaqIntro(Relation relation, Category category, File file) throws Exception {
        if (hasBeenIndexed(relation))
            return;
        setIndexed(relation);

        Map env = new HashMap();
        env.put(ShowObject.VAR_RELATION, relation);
        env.put(VAR_ONLINE_URL, PORTAL_URL + "/faq");
        List parents = new ArrayList();
        parents.add(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        Tools.sync(category);
        env.put(ViewCategory.VAR_CATEGORY, category);

        String name = FMTemplateSelector.select("ViewFaq", "start", env, "offline");
        FMUtils.executeTemplate(name, env, file);
    }

    /**
     * Dumps all dictionary items.
     * @param currentDir
     * @throws Exception
     */
    void dumpDictionary(File currentDir) throws Exception {
        Relation relation = (Relation) Tools.sync(new Relation(Constants.REL_DICTIONARY));
        if (hasBeenIndexed(relation))
            return;
        setIndexed(relation);

        Map env = new HashMap();
        env.put(ShowObject.VAR_RELATION, relation);
        env.put(VAR_ONLINE_URL, PORTAL_URL + "/slovnik");
        env.put(ViewCategory.VAR_CATEGORY, relation.getChild());

        List parents = new ArrayList();
        parents.add(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        int total = sqlTool.countItemRelationsWithType(Item.FAQ, null);
        List data = sqlTool.findItemRelationsWithType(Item.DICTIONARY, null);
        Tools.syncList(data);
        Sorters2.byName(data);

        Paging paging = new Paging(data, 0, total, total);
        env.put(VAR_DATA, paging);

        String template = FMTemplateSelector.select("Dictionary", "showList", env, "offline");
        File file = getFileName(relation, currentDir, paging.getPageIndex().intValue());
        FMUtils.executeTemplate(template, env, file);

        for (Iterator iter2 = data.iterator(); iter2.hasNext();) {
            Relation relation2 = (Relation) iter2.next();
            file = getFileName(relation2, currentDir);
            dumpItem(relation2, (Item) relation2.getChild(), file, parents, UrlUtils.PREFIX_DICTIONARY);
        }
    }

    /**
     * Calculates file name including directories for relation.
     * File name is equal to current directory plus computed file name.
     */
    File getFileName(Relation relation, File currentDir) {
        return getFileName(relation, currentDir, 0);
    }

    File getFileName(Relation relation, File currentDir, int page) {
        int id = relation.getId();
        StringBuffer sb = new StringBuffer();
        insertDirectory(sb, id);
        File dir = new File(currentDir, sb.toString());
        dir.mkdirs();

        sb.setLength(0);
        insertFileName(sb, id, page);
        File file = new File(dir, sb.toString());
        return file;
    }

    /**
     * Calculates file name including directories for relation.
     */
    public String getFile(int relationId, Number page) {
        StringBuffer sb = new StringBuffer();
        insertDirectory(sb, relationId);
        sb.append(File.separatorChar);
        int pageIndex = 0;
        if (page!=null)
            pageIndex = page.intValue();
        insertFileName(sb, relationId, pageIndex);
        return sb.toString();
    }

    /**
     * Calculates file name including directories for relation.
     */
    public String getFile(int relationId) {
        return getFile(relationId, new Integer(0));
    }

    private void insertDirectory(StringBuffer sb, int id) {
        sb.append((char) ('a' + id % 23));
        sb.append('/');
        sb.append((char) ('a' + id % 26));
    }

    private void insertFileName(StringBuffer sb, int id, int page) {
        df.format(id, sb, new FieldPosition(0));
        if (page>0)
            sb.append("_"+page);
        sb.append(".html");
    }

    /**
     * Tests, whether child has been already indexed. If it has not been,
     * its empty clone is stored to mark child as indexed.
     */
    boolean hasBeenIndexed(Relation relation) {
        Integer id = new Integer(relation.getId());
        if (indexed.containsKey(id)) {
//            System.out.println(id);
            return true;
        }
        return false;
    }

    /**
     * Mark relation as indexed.
     * @param relation
     */
    void setIndexed(Relation relation) {
        indexed.put(new Integer(relation.getId()), Boolean.TRUE);
    }

    /**
     * Force initialization of subsystems.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
    }

    /*
       uprava dat:
       smazat relaci http://www.abclinuxu.cz/clanky/dir/4731
       smazat prazdne FAQ sekce
       smazat clanky Udalo se ..
       select R.cislo from relace R,polozka P where R.typ_potomka='P' and P.cislo=R.potomek and typ=2 and P.data like '%<name>Událo%';
       prevest URL na lokalni
       prevest textova URL na ciselna
       prevest ciselna URL na offline (ideal - jen kdyz se indexuji, jinak smerovat na internet)
    */
}
