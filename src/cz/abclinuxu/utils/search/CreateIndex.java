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

import cz.abclinuxu.persistence.*;
import cz.abclinuxu.persistence.extra.*;
import cz.abclinuxu.data.*;
import cz.abclinuxu.data.view.DiscussionRecord;
import cz.abclinuxu.data.view.Comment;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.scheduler.WhatHappened;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.analysis.Analyzer;

import java.util.*;
import java.util.prefs.Preferences;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
    public static final String PREF_ADD_TITLE_TO_CONTENT = "add.title.to.content";
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
    static float boostDocument, boostBazaar;
    static boolean addTitleToContent;
    static int pageSize;

    static {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(new CreateIndex());
        persistence = PersistenceFactory.getPersistance();
        sqlTool = SQLTool.getInstance();
    }

    public static void main(String[] args) throws Exception {
        String PATH = indexPath;
        if ( args.length>0 ) PATH = args[0];

        try {
            long start = System.currentTimeMillis();

            indexWriter = new EmptyIndexWriter(PATH, new AbcCzechAnalyzer(), true);
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

            FileWriter fos = new FileWriter(new File(PATH, lastRunFilename));
            fos.write(Constants.czFormat.format(new Date()));
            fos.close();

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
                        doc.setURL(urlPrefix+"/show/"+relation2.getId());
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

                    doc = indexStory(relation, blog);
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
        StringBuffer sb = new StringBuffer();
        String title;

        Element data = (Element) category.getData().selectSingleNode("//custom");
        Node node = data.element("page_title");
        title = node.getText();

        node = data.element("title");
        if ( node != null )
            sb.append(node.getText());

        node = data.element("intro");
        if ( node != null ) {
            sb.append(" ");
            sb.append(node.getText());
        }

        MyDocument doc = new MyDocument(title, sb.toString(), addTitleToContent);
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
    static MyDocument indexStory(Relation relation, Category category) throws IOException {
        Item story = (Item) relation.getChild();
        indexDiscussionFor(story);
        StringBuffer sb = new StringBuffer();
        String title, s;

        storeUser(story.getOwner(), sb);
        Element data = story.getData().getRootElement();
        Node node = data.element("name");
        title = node.getText();

        node = data.element("perex");
        if (node != null) {
            sb.append(" ");
            s = node.getText();
            sb.append(s);
        }

        node = data.element("content");
        sb.append(" ");
        s = node.getText();
        sb.append(s);


        MyDocument doc = new MyDocument(title, sb.toString(), addTitleToContent);
        doc.setURL(Tools.getUrlForBlogStory(category.getSubType(), story.getCreated(), relation.getId()));
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
        StringBuffer sb = new StringBuffer();
        String title;

        Element data = category.getData().getRootElement();
        Node node = data.element("name");
        title = node.getText();

        node = data.element("note");
        if ( node != null )
            sb.append(node.getText());

        MyDocument doc = new MyDocument(title, sb.toString(), addTitleToContent);
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
        StringBuffer sb = new StringBuffer();
        boolean question = false;

        Document document = discussion.getData();
        Element data = document.getRootElement();
        if (title == null ) {
            Node node = data.element("title");
            if (node != null) {
                title = node.getText();
                question = true;
            } else
                title = "Diskuse";
        }

        Node node = data.element("text");
        if ( node != null )
            sb.append(node.getText());

        if ( discussion.getChildren().size() > 0 ) {
            Record record = (Record) ((Relation) discussion.getChildren().get(0)).getChild();
            record = (Record) persistence.findById(record);
            DiscussionRecord dizRecord = (DiscussionRecord) record.getCustom();
            LinkedList stack = new LinkedList(dizRecord.getThreads());

            while (stack.size() > 0) {
                Comment comment = (Comment) stack.removeFirst();
                stack.addAll(comment.getChildren());
                String s = comment.getTitle();
                if ( s != null ) {
                    sb.append(" ");
                    sb.append(s);
                }

                node = comment.getData().getRootElement().element("text");
                if ( node!=null ) {
                    sb.append(" ");
                    sb.append(node.getText());
                }

                s = comment.getAnonymName();
                if ( s != null ) {
                    sb.append(" ");
                    sb.append(s);
                } else {
                    Integer id = comment.getAuthor();
                    if (id != null)
                        storeUser(id, sb);
                }
            }
        }

        MyDocument doc = new MyDocument(title, sb.toString(), addTitleToContent);
        doc.setCreated(discussion.getCreated());
        doc.setUpdated(discussion.getUpdated());
        doc.setCid(discussion);
        if (question) {
            doc.setQuestionSolved(Tools.isQuestionSolved(document));
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
        Element data = make.getData().getRootElement();

        Node node = data.element("name");
        String title = node.getText();
        StringBuffer sb = new StringBuffer();
        // todo wiki zaznamy by spise nemely ukladat autora posledni revize
        storeUser(make.getOwner(), sb);

        node = data.element("setup");
        if (node != null) {
            sb.append(" ");
            sb.append(node.getText());
        }

        node = data.element("params");
        if (node != null) {
            sb.append(" ");
            sb.append(node.getText());
        }

        node = data.element("identification");
        if (node != null) {
            sb.append(" ");
            sb.append(node.getText());
        }

        node = data.element("note");
        if (node != null) {
            sb.append(" ");
            sb.append(node.getText());
        }
        sb.append(" ");

        MyDocument doc = new MyDocument(title, sb.toString(), addTitleToContent);
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
        Element data = make.getData().getRootElement();

        Node node = data.element("name");
        String title = node.getText();
        StringBuffer sb = new StringBuffer();
        storeUser(make.getOwner(), sb);

        node = data.element("description");
        if (node != null) {
            sb.append(" ");
            sb.append(node.getText());
        }
        sb.append(" ");

        MyDocument doc = new MyDocument(title, sb.toString(), addTitleToContent);
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
        StringBuffer sb = new StringBuffer();
        String title;

        Element data = driver.getData().getRootElement();
        Node node = data.element("name");
        title = node.getText();

        node = data.element("note");
        if ( node!=null )
            sb.append(node.getText());

        MyDocument doc = new MyDocument(title, sb.toString(), addTitleToContent);
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
        StringBuffer sb = new StringBuffer();

        Set authors = article.getProperty(Constants.PROPERTY_AUTHOR);
        for (Iterator iter = authors.iterator(); iter.hasNext();) {
            int rid = Misc.parseInt((String)iter.next(), 0);
            storeAuthor(rid, sb);
        }

        Element data = article.getData().getRootElement();
        if (data.attribute(WhatHappened.INDEXING_FORBIDDEN)!=null)
            return null;

        Node node = data.element("name");
        String title = node.getText();

        node = data.element("perex");
        if ( node != null ) {
            sb.append(node.getText());
            sb.append(" ");
        }

        for ( Iterator iter = article.getChildren().iterator(); iter.hasNext(); ) {
            Relation child = (Relation) iter.next();

            if ( child.getChild() instanceof Record ) {
                Record record = (Record) persistence.findById(child.getChild());
                if ( record.getType()==Record.ARTICLE ) {
                    List nodes = record.getData().selectNodes("/data/content");
                    if (nodes.size()==1) {
                        sb.append(((Node)nodes.get(0)).getText());
                        sb.append(" ");
                    } else
                        for ( Iterator iter2 = nodes.iterator(); iter2.hasNext(); ) {
                            node = (Element) iter2.next();
                            sb.append(node.getText());
                            sb.append(" ");
                            sb.append(((Element)node).attributeValue("title"));
                            sb.append(" ");
                        }
                }
            }
        }

        MyDocument doc = new MyDocument(title, sb.toString(), addTitleToContent);
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
        StringBuffer sb = new StringBuffer();

        storeUser(news.getOwner(), sb);
        Element data = news.getData().getRootElement();
        String content = data.element("content").getText();
        sb.append(content);
        sb.append(" ");

        String title = null;
        Node node = data.element("title");
        if ( node != null)
            title = node.getText();
        else {
            String tmp = Tools.removeTags(content);
            title = Tools.limit(tmp, 50, " ..");
        }

        String category = null;
        node = data.element("category");
        if (node != null)
            category = node.getText();

        MyDocument doc = new MyDocument(title, sb.toString(), addTitleToContent);
        doc.setType(MyDocument.TYPE_NEWS);
        doc.setCreated(news.getCreated());
        doc.setUpdated(news.getUpdated());
        if (category != null)
            doc.setNewsCategory(category);
        String url = relation.getUrl();
        if (url == null)
            url = UrlUtils.PREFIX_NEWS + "/show/" + relation.getId();
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
        StringBuffer sb = new StringBuffer();

        storeUser(item.getOwner(), sb);
        Element data = item.getData().getRootElement();
        String title = data.elementText("title");

        String content = data.element("text").getText();
        sb.append(content);

        Node node = data.element("price");
        if (node != null) {
            sb.append(" ");
            sb.append(node.getText());
        }

        node = data.element("contact");
        if (node != null) {
            sb.append(" ");
            sb.append(node.getText());
        }

        MyDocument doc = new MyDocument(title, sb.toString(), addTitleToContent);
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
        StringBuffer sb = new StringBuffer();

        String tmp = poll.getText();
        sb.append(tmp);
        sb.append(" ");
        for (int i = 0; i < poll.getChoices().length; i++) {
            PollChoice choice = poll.getChoices()[i];
            sb.append(choice.getText());
            sb.append(" ");
        }

        tmp = Tools.removeTags(tmp);
        String title = Tools.limit(tmp, 50, " ..");

        MyDocument doc = new MyDocument(title, sb.toString(), addTitleToContent);
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

                String url = child.getUrl();
                if (url == null)
                    url = urlPrefix + "/show/" + child.getId();

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
        String title = Tools.xpath(dictionary, "/data/name");
        String s = Tools.xpath(dictionary, "/data/description");
        StringBuffer sb = new StringBuffer(s);

        MyDocument doc = new MyDocument(title, sb.toString(), addTitleToContent);
        doc.setType(MyDocument.TYPE_DICTIONARY);
        doc.setCreated(dictionary.getCreated());
        doc.setUpdated(dictionary.getUpdated());
        doc.setURL(relation.getUrl());
        doc.setCid(dictionary);
        doc.setBoost(boostDictionary);

        return doc;
    }

    /**
     * Extracts text from faq item for indexing.
     * @param relation initialized relation
     */
    static MyDocument indexFaq(Relation relation) {
        Item faq = (Item) relation.getChild();
        String title = Tools.xpath(faq, "/data/title");
        String content = Tools.xpath(faq, "/data/text");
        StringBuffer sb = new StringBuffer(content);

        MyDocument doc = new MyDocument(title, sb.toString(), addTitleToContent);
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
        String title = Tools.xpath(item, "/data/name");
        String content = Tools.xpath(item, "/data/content");
        StringBuffer sb = new StringBuffer(content);

        MyDocument doc = new MyDocument(title, sb.toString(), addTitleToContent);
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
     * Appends user information into stringbuffer. If there is no such user,
     * error is ignored and this method does nothing.
     * @param id user id
     * @param sb
     */
    private static void storeUser(int id, StringBuffer sb) {
        try {
            sb.append(" ");
            User user = (User) persistence.findById(new User(id));
            String nick = user.getNick();
            if (nick!=null) {
                sb.append(nick);
                sb.append(" ");
            }
            sb.append(user.getName());
            sb.append(" ");
        } catch (NotFoundException e) {
            // user could be deleted
        }
    }

    /**
     * Appends user information into stringbuffer. If there is no such user,
     * error is ignored and this method does nothing.
     * @param rid author relation id
     * @param sb
     */
    private static void storeAuthor(int rid, StringBuffer sb) {
        try {
            sb.append(" ");
            Relation relation = (Relation) persistence.findById(new Relation(rid));
            Item author = (Item) persistence.findById(relation.getChild());
            sb.append(Tools.childName(author));
            sb.append(" ");
        } catch (NotFoundException e) {
            // user could be deleted
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
        addTitleToContent = prefs.getBoolean(PREF_ADD_TITLE_TO_CONTENT, true);
        boostArticle = prefs.getFloat(PREF_BOOST_ARTICLE, 1.0f);
        boostBazaar = prefs.getFloat(PREF_BOOST_BAZAAR, 1.0f);
        boostBlog = prefs.getFloat(PREF_BOOST_BLOG, 1.0f);
        boostDictionary = prefs.getFloat(PREF_BOOST_DICTIONARY, 1.0f);
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
