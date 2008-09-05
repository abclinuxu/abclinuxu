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
package cz.abclinuxu.utils.search;

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Poll;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.view.ParsedDocument;
import cz.abclinuxu.data.view.NewsCategory;
import cz.abclinuxu.data.view.NewsCategories;
import cz.abclinuxu.misc.DocumentParser;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.SpecialValue;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.edit.EditArticle;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.freemarker.Tools;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.dom4j.Element;
import org.dom4j.Node;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * This class is responsible for creating and maintaining Lucene's index.
 */
public class CreateIndex implements Configurable {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CreateIndex.class);

    public static final String PREF_PATH = "path";
    public static final String PREF_LAST_RUN_NAME = "last.run.file";
    public static final String PREF_MERGE_FACTOR = "merge.factor";
    public static final String PREF_MAX_MERGE_DOCS = "max.merge.docs";
    public static final String PREF_MAX_FIELD_LENGTH = "max.field.length";
    public static final String PREF_INDEXING_PAGE_SIZE = "indexing.page.size";
    public static final String PREF_BOOST_SOFTWARE = "boost.hardware";
    public static final String PREF_BOOST_HARDWARE = "boost.software";
    public static final String PREF_BOOST_ARTICLE = "boost.article";
    public static final String PREF_BOOST_NEWS = "boost.news";
    public static final String PREF_BOOST_QUESTION = "boost.question";
    public static final String PREF_BOOST_DISCUSSION = "boost.discussion";
    public static final String PREF_BOOST_DRIVER = "boost.driver";
    public static final String PREF_BOOST_BLOG = "boost.blog";
    public static final String PREF_BOOST_FAQ = "boost.faq";
    public static final String PREF_BOOST_DICTIONARY = "boost.dictionary";
    public static final String PREF_BOOST_PERSONALITIES = "boost.personalities";
    public static final String PREF_BOOST_SECTION = "boost.section";
    public static final String PREF_BOOST_POLL = "boost.poll";
    public static final String PREF_BOOST_DOCUMENT = "boost.document";
    public static final String PREF_BOOST_BAZAAR = "boost.bazaar";

    private final String LAST_RUN_FILE = "last_run.txt";

    static IndexWriter indexWriter;
    static String indexPath,lastRunFilename;
    static int mergeFactor, maxMergeDocs, maxFieldLength;
    static Persistence persistence;
    static SQLTool sqlTool;
    static HashMap indexed = new HashMap(150000, 0.99f);
    static float boostHardware, boostSoftware, boostArticle, boostNews, boostQuestion, boostDiscussion;
    static float boostDriver, boostBlog, boostFaq, boostDictionary, boostSection, boostPoll;
    static float boostDocument, boostBazaar, boostPersonalities;
    static int pageSize;

    static {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(new CreateIndex());
        persistence = PersistenceFactory.getPersistence();
        sqlTool = SQLTool.getInstance();
    }

    public static void main(String[] args) throws Exception {
        String PATH = indexPath;
        if ( args.length>0 ) PATH = args[0];

        try {
            long start = System.currentTimeMillis();

            indexWriter = new IndexWriter(PATH, new AbcCzechAnalyzer(), true);
            if (mergeFactor > 0)
                indexWriter.setMergeFactor(mergeFactor);
            if (maxMergeDocs > 0)
                indexWriter.setMaxMergeDocs(maxMergeDocs);
            if (maxFieldLength > 0)
                indexWriter.setMaxFieldLength(maxFieldLength);
            log.info("Starting to index data, directory: "+PATH +", mergeFactor: "+indexWriter.getMergeFactor()
                      + ", maxMergeDocs: "+indexWriter.getMaxMergeDocs()
                      + ", maxFieldLength: "+indexWriter.getMaxFieldLength());

            try {
                Relation articles = (Relation) persistence.findById(new Relation(Constants.REL_ARTICLES));
                makeIndexOnArticles(articles.getChild().getChildren());
            } catch (Exception e) {
                log.error("Failed to index articles", e);
            }
            try {
                makeIndexOnNews();
            } catch (Exception e) {
                log.error("Failed to index news", e);
            }
            try {
                makeIndexOnDictionary();
            } catch (Exception e) {
                log.error("Failed to index dictionary", e);
            }
            try {
                makeIndexOnPersonalities();
            } catch (Exception e) {
                log.error("Failed to index personalities", e);
            }
            try {
                makeIndexOnFaq();
            } catch (Exception e) {
                log.error("Failed to index FAQ", e);
            }
            try {
                makeIndexOnPolls();
            } catch (Exception e) {
                log.error("Failed to index Polls", e);
            }
            try {
                makeIndexOnBlogs();
            } catch (Exception e) {
                log.error("Failed to index blogs", e);
            }
            try {
                List forums = sqlTool.findCategoryRelationsWithType(Category.FORUM, null);
                makeIndexOnForums(forums, UrlUtils.PREFIX_FORUM);
            } catch (Exception e) {
                log.error("Failed to index forums", e);
            }
            try {
                makeIndexOnHardware();
            } catch (Exception e) {
                log.error("Failed to index hardware", e);
            }
            try {
                makeIndexOnSoftware();
            } catch (Exception e) {
                log.error("Failed to index software", e);
            }
            try {
                makeIndexOnBazaar();
            } catch (Exception e) {
                log.error("Failed to index bazaar", e);
            }
            try {
                makeIndexOnDrivers();
            } catch (Exception e) {
                log.error("Failed to index drivers", e);
            }

            log.info("Starting to optimize the index");
            indexWriter.optimize();
            indexWriter.close();
            long end = System.currentTimeMillis();

            Misc.touchFile(new File(PATH, lastRunFilename), System.currentTimeMillis());

            String message = "Indexing of "+indexWriter.docCount()+" documents took "+(end-start)/1000+" seconds.";
            log.info(message);
            System.out.println(message);
        } catch (Throwable e) {
            log.error("Indexing failed!",e);
            e.printStackTrace();
        }
    }

    /**
     * Indexes content of given forums.
     */
    static void makeIndexOnForums(List forums, String urlPrefix) throws Exception {
        int total, i;
        Relation relation;
        GenericObject child;
        MyDocument doc;

        for ( Iterator iter = forums.iterator(); iter.hasNext(); ) {
            relation = (Relation) iter.next();
            total = sqlTool.countDiscussionRelationsWithParent(relation.getId());

            for ( i = 0; i<total; ) {
                Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, pageSize)};
                List<Relation> discussions = sqlTool.findDiscussionRelationsWithParent(relation.getId(), qualifiers);
                Tools.syncList(discussions);
                i += discussions.size();

                for ( Relation relation2 : discussions ) {
                    try {
                        child = relation2.getChild();
                        if ( hasBeenIndexed(child) )
                            continue;
                        doc = indexDiscussion((Item)child, null);
                        doc.setURL(Tools.getUrlForDiscussion(relation2));
                        doc.setParent(relation2.getUpper());
                        indexWriter.addDocument(doc.getDocument());
                    } catch (Exception e) {
                        log.error("Chyba při indexování dotazu " + relation, e);
                    }
                }
            }
        }
    }

    /**
     * Indexes dictionary.
     */
    static void makeIndexOnDictionary() throws Exception {
        Item child;
        MyDocument doc;
        List<Relation> relations = sqlTool.findItemRelationsWithType(Item.DICTIONARY, new Qualifier[0]);
        Tools.syncList(relations);

        for (Relation relation : relations) {
            try {
                child = (Item) relation.getChild();
                if ( hasBeenIndexed(child) )
                    continue;
                doc = indexDictionary(relation);
                indexWriter.addDocument(doc.getDocument());
            } catch (Exception e) {
                log.error("Chyba při indexování pojmu " + relation, e);
            }
        }
    }

    /**
     * Indexes personalities.
     */
    static void makeIndexOnPersonalities() throws Exception {
        Item child;
        MyDocument doc;
        List<Relation> relations = sqlTool.findItemRelationsWithType(Item.PERSONALITY, new Qualifier[0]);
        Tools.syncList(relations);

        for (Relation relation : relations) {
            try {
                child = (Item) relation.getChild();
                if ( hasBeenIndexed(child) )
                    continue;
                doc = indexPersonality(relation);
                indexWriter.addDocument(doc.getDocument());
            } catch (Exception e) {
                log.error("Chyba při indexování osobnosti " + relation, e);
            }
        }
    }

    /**
     * Indexes drivers.
     */
    static void makeIndexOnDrivers() throws Exception {
        Item child;
        MyDocument doc;
        List<Relation> relations = sqlTool.findItemRelationsWithType(Item.DRIVER, new Qualifier[0]);
        Tools.syncList(relations);

        for (Relation relation : relations) {
            try {
                child = (Item) relation.getChild();
                if ( hasBeenIndexed(child) )
                    continue;
                doc = indexDriver(relation);
                indexWriter.addDocument(doc.getDocument());
            } catch (Exception e) {
                log.error("Chyba při indexování pojmu " + relation, e);
            }
        }
    }

    /**
     * Indexes dictionary.
     */
    static void makeIndexOnPolls() throws Exception {
        Poll child;
        MyDocument doc;
        List<Relation> relations = sqlTool.findStandalonePollRelations(new Qualifier[0]);
        Tools.syncList(relations);

        for (Relation relation : relations) {
            try {
                child = (Poll) relation.getChild();
                if ( hasBeenIndexed(child) )
                    continue;
                doc = indexPoll(relation);
                indexWriter.addDocument(doc.getDocument());
            } catch (Exception e) {
                log.error("Chyba při indexování ankety " + relation, e);
            }
        }
    }

    /**
     * Indexes frequently asked questions.
     */
    static void makeIndexOnFaq() throws Exception {
        Item child;
        MyDocument doc;
        List<Relation> relations = sqlTool.findItemRelationsWithType(Item.FAQ, new Qualifier[0]);
        Tools.syncList(relations);

        for (Relation relation : relations) {
            try {
                child = (Item) relation.getChild();
                if ( hasBeenIndexed(child) )
                    continue;
                doc = indexFaq(relation);
                indexWriter.addDocument(doc.getDocument());
            } catch (Exception e) {
                log.error("Chyba při indexování FAQ " + relation, e);
            }
        }
    }

    /**
     * Indexes (wiki) documents.
     */
    static void makeIndexOnDocuments() throws Exception {
        Item child;
        MyDocument doc;
        List<Relation> relations = sqlTool.findItemRelationsWithType(Item.CONTENT, null);
        Tools.syncList(relations);

        for (Relation relation : relations) {
            try {
                child = (Item) relation.getChild();
                if ( hasBeenIndexed(child) )
                    continue;
                doc = indexDocument(relation);
                indexWriter.addDocument(doc.getDocument());
            } catch (Exception e) {
                log.error("Chyba při indexování wiki dokumentu " + relation, e);
            }
        }
    }

    /**
     * Indexes news.
     */
    static void makeIndexOnNews() throws Exception {
        GenericObject child;
        MyDocument doc;

        int total = sqlTool.countNewsRelations(), i;
        for ( i = 0; i < total; ) {
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_ID, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, pageSize)};
            List<Relation> data = sqlTool.findNewsRelations(qualifiers);
            Tools.syncList(data);
            i += data.size();

            for ( Relation relation : data ) {
                try {
                    child = relation.getChild();
                    if ( hasBeenIndexed(child) )
                        continue;

                    doc = indexNews(relation);
                    indexWriter.addDocument(doc.getDocument());
                } catch (Exception e) {
                    log.error("Chyba při indexování zprávičky " + relation, e);
                }
            }
        }
    }

    /**
     * Indexes bazaar.
     */
    static void makeIndexOnBazaar() throws Exception {
        GenericObject child;
        MyDocument doc;

        int total = sqlTool.countItemRelationsWithType(Item.BAZAAR, null), i;
        for ( i = 0; i < total; ) {
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_ID, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, pageSize)};
            List<Relation> data = sqlTool.findItemRelationsWithType(Item.BAZAAR, qualifiers);
            Tools.syncList(data);
            i += data.size();

            for ( Relation relation : data ) {
                try {
                    child = relation.getChild();
                    if ( hasBeenIndexed(child) )
                        continue;

                    doc = indexBazaar(relation);
                    indexWriter.addDocument(doc.getDocument());
                } catch (Exception e) {
                    log.error("Chyba při indexování inzerátu " + relation, e);
                }
            }
        }
    }

    /**
     * Indexes hardware.
     */
    static void makeIndexOnHardware() throws Exception {
        GenericObject child;
        MyDocument doc;

        int total = sqlTool.countItemRelationsWithType(Item.HARDWARE, null), i;
        for (i = 0; i < total;) {
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_ID, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, pageSize)};
            List<Relation> data = sqlTool.findItemRelationsWithType(Item.HARDWARE, qualifiers);
            Tools.syncList(data);
            i += data.size();

            for (Relation relation : data) {
                try {
                    child = relation.getChild();
                    if (hasBeenIndexed(child))
                        continue;

                    doc = indexHardware(relation);
                    indexWriter.addDocument(doc.getDocument());
                } catch (Exception e) {
                    log.error("Chyba při indexování hardwaru " + relation, e);
                }
            }
        }

        total = sqlTool.countCategoryRelationsWithType(Category.HARDWARE_SECTION);
        for (i = 0; i < total;) {
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_ID, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, pageSize)};
            List<Relation> data = sqlTool.findCategoryRelationsWithType(Category.HARDWARE_SECTION, qualifiers);
            Tools.syncList(data);
            i += data.size();

            for (Relation relation : data) {
                try {
                    child = relation.getChild();
                    if (hasBeenIndexed(child))
                        continue;

                    doc = indexCategory(relation, UrlUtils.PREFIX_HARDWARE);
                    indexWriter.addDocument(doc.getDocument());
                } catch (Exception e) {
                    log.error("Chyba při indexování hardwaru " + relation, e);
                }
            }
        }
    }

    /**
     * Indexes software.
     */
    static void makeIndexOnSoftware() throws Exception {
        GenericObject child;
        MyDocument doc;

        int total = sqlTool.countItemRelationsWithType(Item.SOFTWARE, null), i;
        for (i = 0; i < total;) {
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_ID, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, pageSize)};
            List<Relation> data = sqlTool.findItemRelationsWithType(Item.SOFTWARE, qualifiers);
            Tools.syncList(data);
            i += data.size();

            for (Relation relation : data) {
                try {
                    child = relation.getChild();
                    if (hasBeenIndexed(child))
                        continue;

                    doc = indexSoftware(relation);
                    indexWriter.addDocument(doc.getDocument());
                } catch (Exception e) {
                    log.error("Chyba při indexování hardwaru " + relation, e);
                }
            }
        }

        total = sqlTool.countCategoryRelationsWithType(Category.SOFTWARE_SECTION);
        for (i = 0; i < total;) {
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_ID, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, pageSize)};
            List<Relation> data = sqlTool.findCategoryRelationsWithType(Category.SOFTWARE_SECTION, qualifiers);
            Tools.syncList(data);
            i += data.size();

            for (Relation relation : data) {
                try {
                    child = relation.getChild();
                    if (hasBeenIndexed(child))
                        continue;

                    doc = indexCategory(relation, UrlUtils.PREFIX_SOFTWARE);
                    indexWriter.addDocument(doc.getDocument());
                } catch (Exception e) {
                    log.error("Chyba při indexování hardwaru " + relation, e);
                }
            }
        }
    }

    /**
     * Indexes article.
     */
    static void makeIndexOnArticles(List sections) throws Exception {
        int total, i;
        Relation relation = null;
        GenericObject child;
        MyDocument doc;

        for (Iterator iter = sections.iterator(); iter.hasNext();) {
            Relation sectionRelation = (Relation) iter.next();
            int sectionId = sectionRelation.getChild().getId();
            total = sqlTool.countArticleRelations(sectionId);

            for (i = 0; i < total;) {
                Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, pageSize)};
                List<Relation> data = sqlTool.findArticleRelations(qualifiers, sectionId);
                Tools.syncList(data);
                Tools.initializeArticles(data, true, true);
                i += data.size();

                for (Iterator iter2 = data.iterator(); iter2.hasNext();) {
                    try {
                        relation = (Relation) iter2.next();
                        child = relation.getChild();
                        if (hasBeenIndexed(child))
                            continue;

                        doc = indexArticle(relation);
                        if (doc == null)
                            continue;

                        indexWriter.addDocument(doc.getDocument());
                    } catch (Exception e) {
                        log.error("Chyba při indexování článku " + relation, e);
                    }
                }
            }
        }
    }

    /**
     * Indexes blogs and stories.
     */
    static void makeIndexOnBlogs() throws Exception {
        GenericObject child;
        MyDocument doc;

        int total = sqlTool.countCategoryRelationsWithType(Category.BLOG);
        for (int i = 0; i < total;) {
            Qualifier[] qa = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, pageSize)};
            List<Relation> data = sqlTool.findCategoryRelationsWithType(Category.BLOG, qa);
            Tools.syncList(data);
            i += data.size();

            for (Relation relation : data) {
                try {
                    child = relation.getChild();
                    doc = indexBlog((Category) child);
                    indexWriter.addDocument(doc.getDocument());
                    makeIndexOnBlog((Category) child);
                } catch (Exception e) {
                    log.error("Chyba při indexování blogu " + relation, e);
                }
            }
        }
    }

    /**
     * Indexes blogs and stories.
     */
    static void makeIndexOnBlog(Category blog) throws Exception {
        GenericObject child;
        MyDocument doc;

        List qualifiers = new ArrayList();
        CompareCondition ownerCondition = new CompareCondition(Field.OWNER, Operation.EQUAL, blog.getOwner());
        qualifiers.add(ownerCondition);
        qualifiers.add(new CompareCondition(Field.CREATED, Operation.SMALLER_OR_EQUAL, SpecialValue.NOW));
        Qualifier[] qa = new Qualifier[qualifiers.size()];
        int total = sqlTool.countItemRelationsWithType(Item.BLOG, (Qualifier[]) qualifiers.toArray(qa));
        for ( int i = 0; i<total; ) {
            qa = new Qualifier[]{ownerCondition, Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, pageSize)};
            List<Relation> data = sqlTool.findItemRelationsWithType(Item.BLOG, qa);
            Tools.syncList(data);
            i += data.size();

            for ( Relation relation : data ) {
                try {
                    child = relation.getChild();
                    if ( hasBeenIndexed(child) )
                        continue;

                    doc = indexStory(relation);
                    doc.setParent(relation.getUpper());
                    indexWriter.addDocument(doc.getDocument());
                } catch (Exception e) {
                    log.error("Chyba při indexování zápisu " + relation, e);
                }
            }
        }
    }

    /**
     * Extracts data for indexing from blog.
     */
    static MyDocument indexBlog(Category category) {
        Element data = (Element) category.getData().selectSingleNode("//custom");
        Node node = data.element("page_title");
        String title = node.getText();
        ParsedDocument parsed = DocumentParser.parse(category);

        MyDocument doc = new MyDocument(title, parsed.getContent(), false);
        doc.setURL("/blog/" + category.getSubType());
        doc.setType(MyDocument.TYPE_BLOG);
        doc.setCreated(category.getCreated());
        doc.setCid(category);
        doc.setBoost(boostBlog);
        return doc;
    }

    /**
     * Extracts data for indexing from blog.
     * @param relation story relation
     */
    static MyDocument indexStory(Relation relation) throws IOException {
        Item story = (Item) relation.getChild();
        indexDiscussionFor(story);

        String title = story.getTitle();
        ParsedDocument parsed = DocumentParser.parse(story);

        MyDocument doc = new MyDocument(title, parsed.getContent(), false);
        doc.setURL(Tools.getUrlForBlogStory(relation));
        doc.setType(MyDocument.TYPE_BLOG);
        doc.setCreated(story.getCreated());
        doc.setUpdated(story.getUpdated());
        doc.setCid(story);
        doc.setBoost(boostBlog);
        return doc;
    }

    /**
     * Extracts data for indexing from category. Category must be synchronized.
     */
    static MyDocument indexCategory(Relation relation, String urlPrefix) {
        Category category = (Category) relation.getChild();
        String title = category.getTitle();
        ParsedDocument parsed = DocumentParser.parse(category);

        MyDocument doc = new MyDocument(title, parsed.getContent(), false);
        doc.setType(MyDocument.TYPE_CATEGORY);
        doc.setCreated(category.getCreated());
        doc.setUpdated(category.getUpdated());

        String url = relation.getUrl();
        if (url == null)
            url = urlPrefix+"/dir/"+relation.getId();
        doc.setURL(url);

        doc.setCid(category);
        doc.setBoost(boostSection);
        return doc;
    }

    /**
     * Extracts data for indexing from discussion. Item must be synchronized.
     */
    static MyDocument indexDiscussion(Item discussion, String title) {
        boolean question = Tools.isQuestion(discussion);
        title = discussion.getTitle();//todo overit

        ParsedDocument parsed = DocumentParser.parse(discussion);

        MyDocument doc = new MyDocument(title, parsed.getContent(), false);
        doc.setCreated(discussion.getCreated());
        doc.setUpdated(discussion.getUpdated());
        doc.setCid(discussion);
        if (question) {
            doc.setQuestionSolved(Tools.isQuestionSolved(discussion.getData()));
            doc.setType(MyDocument.TYPE_QUESTION);
            doc.setBoost(boostQuestion);
        } else {
            doc.setType(MyDocument.TYPE_DISCUSSION);
            doc.setBoost(boostDiscussion);
        }
        doc.setNumberOfReplies(Tools.xpath(discussion, "/data/comments"));
        return doc;
    }

    /**
     * Extracts data for indexing from hardware. Item must be synchronized.
     */
    static MyDocument indexHardware(Relation relation) {
        Item make = (Item) relation.getChild();
        String title = make.getTitle();
        ParsedDocument parsed = DocumentParser.parse(make);

        MyDocument doc = new MyDocument(title, parsed.getContent(), false);
        doc.setCreated(make.getCreated());
        doc.setUpdated(make.getUpdated());
        doc.setType(MyDocument.TYPE_HARDWARE);
        doc.setURL(relation.getUrl());
        doc.setCid(make);
        doc.setBoost(boostHardware);
        return doc;
    }


    /**
     * Extracts data for indexing from hardware. Item must be synchronized.
     */
    static MyDocument indexSoftware(Relation relation) {
        Item make = (Item) relation.getChild();
        String title = make.getTitle();
        ParsedDocument parsed = DocumentParser.parse(make);

        MyDocument doc = new MyDocument(title, parsed.getContent(), false);
        doc.setCreated(make.getCreated());
        doc.setUpdated(make.getUpdated());
        doc.setType(MyDocument.TYPE_SOFTWARE);
        doc.setURL(relation.getUrl());
        doc.setCid(make);
        doc.setBoost(boostSoftware);
        return doc;
    }

    /**
     * Extracts data for indexing from driver. Item must be synchronized.
     */
    static MyDocument indexDriver(Relation relation) {
        Item driver = (Item) relation.getChild();
        String title = driver.getTitle();
        ParsedDocument parsed = DocumentParser.parse(driver);

        MyDocument doc = new MyDocument(title, parsed.getContent(), false);
        doc.setType(MyDocument.TYPE_DRIVER);
        doc.setCreated(driver.getCreated());
        doc.setUpdated(driver.getUpdated());
        doc.setURL(relation.getUrl());
        doc.setCid(driver);
        doc.setBoost(boostDriver);
        return doc;
    }

    /**
     * Extracts data for indexing from article. Item must be synchronized.
     */
    static MyDocument indexArticle(Relation relation) throws IOException {
        Item article = (Item) relation.getChild();
        indexDiscussionFor(article);

        Element data = article.getData().getRootElement();
        if (data.attribute(EditArticle.INDEXING_FORBIDDEN) != null)
            return null;

        String title = article.getTitle();
        ParsedDocument parsed = DocumentParser.parse(article);

        MyDocument doc = new MyDocument(title, parsed.getContent(), false);
        doc.setType(MyDocument.TYPE_ARTICLE);
        doc.setCreated(article.getCreated());
        doc.setUpdated(article.getUpdated());
        doc.setURL(relation.getUrl());
        doc.setParent(relation.getUpper());
        doc.setCid(article);
        doc.setBoost(boostArticle);
        return doc;
    }

    /**
     * Extracts data for indexing from news. Item must be synchronized.
     */
    static MyDocument indexNews(Relation relation) throws IOException {
        Item news = (Item) relation.getChild();
        indexDiscussionFor(news);

        Element data = news.getData().getRootElement();
        String content = data.element("content").getText();
        String title = news.getTitle();

        String category = null;
        NewsCategory nc = NewsCategories.get(news.getSubType());
        if (nc != null)
            category = nc.getKey();

        ParsedDocument parsed = DocumentParser.parse(news);

        MyDocument doc = new MyDocument(title, parsed.getContent(), false);
        doc.setType(MyDocument.TYPE_NEWS);
        doc.setCreated(news.getCreated());
        doc.setUpdated(news.getUpdated());
        if (category != null)
            doc.setNewsCategory(category);
        String url = relation.getUrl();
        if (url == null) {
            log.error("Zpravicka nema url! " + relation); // todo zkontrolovat, zda se to deje
            url = UrlUtils.PREFIX_NEWS + "/show/" + relation.getId();
        }
        doc.setURL(url);
        doc.setParent(relation.getUpper());
        doc.setCid(news);
        doc.setBoost(boostNews);
        return doc;
    }

    /**
     * Extracts data for indexing from bazaar. Item must be synchronized.
     */
    static MyDocument indexBazaar(Relation relation) throws IOException {
        Item item = (Item) relation.getChild();
        indexDiscussionFor(item);

        String title = item.getTitle();
        ParsedDocument parsed = DocumentParser.parse(item);

        MyDocument doc = new MyDocument(title, parsed.getContent(), false);
        doc.setType(MyDocument.TYPE_BAZAAR);
        doc.setCreated(item.getCreated());
        doc.setUpdated(item.getUpdated());
        String url = relation.getUrl();
        if (url == null)
            url = UrlUtils.PREFIX_BAZAAR + "/show/" + relation.getId();
        doc.setURL(url);
        doc.setParent(relation.getUpper());
        doc.setCid(item);
        doc.setBoost(boostBazaar);
        return doc;
    }

    /**
     * Extracts data for indexing from poll. Poll must be synchronized.
     */
    static MyDocument indexPoll(Relation relation) throws IOException {
        Poll poll = (Poll) relation.getChild();
        indexDiscussionFor(poll);

        String title = Tools.removeTags(poll.getText());
        title = Tools.limit(title, 50, " ..");
        ParsedDocument parsed = DocumentParser.parse(poll);

        MyDocument doc = new MyDocument(title, parsed.getContent(), false);
        doc.setType(MyDocument.TYPE_POLL);
        doc.setCreated(poll.getCreated());
        doc.setUpdated(poll.getCreated());

        String url = relation.getUrl();
        if (url==null)
            url = UrlUtils.PREFIX_POLLS + "/show/" + relation.getId();
        doc.setURL(url);
        doc.setParent(relation.getUpper());
        doc.setCid(poll);
        doc.setBoost(boostPoll);
        return doc;
    }

    /**
     * Creates and stores document with discussion to given parent.
     * @param parent either article, news or blog
     */
    private static void indexDiscussionFor(GenericObject parent) throws IOException {
        for ( Iterator iter = parent.getChildren().iterator(); iter.hasNext(); ) {
            Relation child = (Relation) iter.next();

            if ( child.getChild() instanceof Item ) {
                Item item = (Item) persistence.findById(child.getChild());
                if (item.getType() != Item.DISCUSSION)
                    continue;

                String replies = Tools.xpath(item, "/data/comments");
                if (Misc.parseInt(replies, 0) == 0)
                    continue;

                String parentTitle = Tools.childName(parent), title = null;
                String urlPrefix = UrlUtils.PREFIX_FORUM;
                if (parent instanceof Item) {
                    Item parentItem = (Item) parent;
                    if (parentItem.getType() == Item.ARTICLE) {
                        title = "Diskuse k článku " + parentTitle;
                        urlPrefix = UrlUtils.PREFIX_CLANKY;
                    } else if (parentItem.getType() == Item.BLOG) {
                        title = "Diskuse k blogu " + parentTitle;
                        urlPrefix = UrlUtils.PREFIX_BLOG;
                    } else if (parentItem.getType() == Item.NEWS) {
                        title = "Diskuse k zprávičce " + parentTitle;
                        urlPrefix = UrlUtils.PREFIX_NEWS;
                    }
                } else if (parent instanceof Poll) {
                    title = "Diskuse k anketě " + parentTitle;
                    urlPrefix = UrlUtils.PREFIX_POLLS;
                }

                String url = Tools.getUrlForDiscussion(child);

                MyDocument doc = indexDiscussion(item, title);
                doc.setURL(url);
                indexWriter.addDocument(doc.getDocument());
                return;
            }
        }
    }

    /**
     * Extracts text from dictionary item (and its records) for indexing.
     * @param relation relation with initialized item
     */
    static MyDocument indexDictionary(Relation relation) {
        Item dictionary = (Item) relation.getChild();
        String title = dictionary.getTitle();
        ParsedDocument parsed = DocumentParser.parse(dictionary);

        MyDocument doc = new MyDocument(title, parsed.getContent(), false);
        doc.setType(MyDocument.TYPE_DICTIONARY);
        doc.setCreated(dictionary.getCreated());
        doc.setUpdated(dictionary.getUpdated());
        doc.setURL(relation.getUrl());
        doc.setCid(dictionary);
        doc.setBoost(boostDictionary);

        return doc;
    }

    /**
     * Extracts text from a personality item for indexing.
     * @param relation relation with initialized item
     */
    static MyDocument indexPersonality(Relation relation) {
        Item personality = (Item) relation.getChild();

        String title = Tools.childName(relation);
        ParsedDocument parsed = DocumentParser.parse(personality);

        MyDocument doc = new MyDocument(title, parsed.getContent(), false);
        doc.setType(MyDocument.TYPE_PERSONALITY);
        doc.setCreated(personality.getCreated());
        doc.setUpdated(personality.getUpdated());
        doc.setURL(relation.getUrl());
        doc.setCid(personality);
        doc.setBoost(boostPersonalities);

        return doc;
    }

    /**
     * Extracts text from faq item for indexing.
     * @param relation initialized relation
     */
    static MyDocument indexFaq(Relation relation) {
        Item faq = (Item) relation.getChild();
        String title = faq.getTitle();
        ParsedDocument parsed = DocumentParser.parse(faq);

        MyDocument doc = new MyDocument(title, parsed.getContent(), false);
        doc.setType(MyDocument.TYPE_FAQ);
        doc.setCreated(faq.getCreated());
        doc.setUpdated(faq.getUpdated());
        doc.setURL(relation.getUrl());
        doc.setCid(faq);
        doc.setBoost(boostFaq);

        return doc;
    }

    /**
     * Extracts text from content item for indexing.
     * @param relation initialized relation
     */
    static MyDocument indexDocument(Relation relation) {
        Item item = (Item) relation.getChild();
        String title = item.getTitle();
        ParsedDocument parsed = DocumentParser.parse(item);

        MyDocument doc = new MyDocument(title, parsed.getContent(), false);
        doc.setType(MyDocument.TYPE_DOCUMENT);
        doc.setCreated(item.getCreated());
        doc.setUpdated(item.getUpdated());
        doc.setURL(relation.getUrl());
        doc.setCid(item);
        doc.setBoost(boostDocument);

        return doc;
    }

    /**
     * Tests, whether child has been already indexed. If it has not been,
     * its empty clone is stored to mark child as indexed.
     */
    static boolean hasBeenIndexed(GenericObject child) throws Exception {
        if ( indexed.containsKey(child) )
            return true;
        else {
            GenericObject key = (GenericObject) ((Class)child.getClass()).newInstance();
            key.setId(child.getId());
            indexed.put(key, Boolean.TRUE);
            return false;
        }
    }

    /**
     * Empty constructor.
     */
    public CreateIndex() {
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        indexPath = prefs.get(PREF_PATH, null);
        lastRunFilename = prefs.get(PREF_LAST_RUN_NAME, LAST_RUN_FILE);
        mergeFactor = prefs.getInt(PREF_MERGE_FACTOR, 0);
        maxMergeDocs = prefs.getInt(PREF_MAX_MERGE_DOCS, 0);
        maxFieldLength = prefs.getInt(PREF_MAX_FIELD_LENGTH, 0);
        pageSize = prefs.getInt(PREF_INDEXING_PAGE_SIZE, 50);
        boostArticle = prefs.getFloat(PREF_BOOST_ARTICLE, 1.0f);
        boostBazaar = prefs.getFloat(PREF_BOOST_BAZAAR, 1.0f);
        boostBlog = prefs.getFloat(PREF_BOOST_BLOG, 1.0f);
        boostDictionary = prefs.getFloat(PREF_BOOST_DICTIONARY, 1.0f);
        boostPersonalities = prefs.getFloat(PREF_BOOST_PERSONALITIES, 1.0f);
        boostDiscussion = prefs.getFloat(PREF_BOOST_DISCUSSION, 1.0f);
        boostDocument = prefs.getFloat(PREF_BOOST_DOCUMENT, 1.0f);
        boostDriver = prefs.getFloat(PREF_BOOST_DRIVER, 1.0f);
        boostFaq = prefs.getFloat(PREF_BOOST_FAQ, 1.0f);
        boostHardware = prefs.getFloat(PREF_BOOST_HARDWARE, 1.0f);
        boostNews = prefs.getFloat(PREF_BOOST_NEWS, 1.0f);
        boostPoll = prefs.getFloat(PREF_BOOST_POLL, 1.0f);
        boostQuestion = prefs.getFloat(PREF_BOOST_QUESTION, 1.0f);
        boostSection = prefs.getFloat(PREF_BOOST_SECTION, 1.0f);
        boostSoftware = prefs.getFloat(PREF_BOOST_SOFTWARE, 1.0f);
    }

    /**
     * @return directory, where index has been generated.
     */
    public static String getIndexPath() {
        return indexPath;
    }

    /**
     * @return File, which serves as timestamp of last indexing. Used for monitoring.
     */
    public static File getLastRunFile() {
        return new File(indexPath,lastRunFilename);
    }

    static class EmptyIndexWriter extends IndexWriter {
        public EmptyIndexWriter(String s, Analyzer analyzer, boolean b) throws IOException {
            super(s, analyzer, b);
        }

        public synchronized void optimize() throws IOException {
        }

        public void addDocument(org.apache.lucene.document.Document document) throws IOException {
        }
    }
}
