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
import cz.abclinuxu.persistence.cache.OnlyUserCache;
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
import cz.abclinuxu.exceptions.InvalidDataException;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.scheduler.WhatHappened;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Document;
import org.apache.lucene.index.IndexWriter;

import java.util.*;
import java.util.prefs.Preferences;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class is responsible for creating and maintaining Lucene's index.
 * TODO vymyslet jak zoptimalizovat nacitani diskusi a jejich zaznamu (pouzivat hromadne ready)
 * TODO zrusit static veci, at se to da volat z ruznymi parametry, napriklad persistence
 */
public class CreateIndex implements Configurable {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CreateIndex.class);

    public static final String PREF_PATH = "path";
    public static final String PREF_LAST_RUN_NAME = "last.run.file";
    public static final String PREF_MERGE_FACTOR = "merge.factor";
    public static final String PREF_MIN_MERGE_DOCS = "min.merge.docs";
    public static final String PREF_MAX_MERGE_DOCS = "max.merge.docs";
    public static final String PREF_MAX_FIELD_LENGTH = "max.field.length";
    public static final String PREF_BOOST_HARDWARE = "boost.hardware";
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

    private final String LAST_RUN_FILE = "last_run.txt";

    static IndexWriter indexWriter;
    static String indexPath,lastRunFilename;
    static int mergeFactor, minMergeDocs, maxMergeDocs, maxFieldLength;
    static Persistence persistence;
    static SQLTool sqlTool;
    static HashMap indexed = new HashMap(150000, 0.99f);
    static float boostHardware, boostArticle, boostNews, boostQuestion, boostDiscussion;
    static float boostDriver, boostBlog, boostFaq, boostDictionary, boostSection, boostPoll;

    static {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(new CreateIndex());
        persistence = PersistenceFactory.getPersistance(OnlyUserCache.class);
        sqlTool = SQLTool.getInstance();
    }

    public static void main(String[] args) throws Exception {
        String PATH = indexPath;
        if ( args.length>0 ) PATH = args[0];

        try {
            Relation articles = (Relation) persistence.findById(new Relation(Constants.REL_ARTICLES));
            Relation hardware = (Relation) Tools.sync(new Relation(Constants.REL_HARDWARE));
            Relation drivers = (Relation) Tools.sync(new Relation(Constants.REL_DRIVERS));
            Relation abc = (Relation) Tools.sync(new Relation(Constants.REL_ABC)); // neni cas to smazat?
            Relation blogs = (Relation) persistence.findById(new Relation(Constants.REL_BLOGS));
            List forums = sqlTool.findSectionRelationsWithType(Category.FORUM,null);

            long start = System.currentTimeMillis();

            indexWriter = new IndexWriter(PATH, new AbcCzechAnalyzer(), true);
            if (mergeFactor > 0)
                indexWriter.mergeFactor = mergeFactor;
            if (minMergeDocs > 0)
                indexWriter.minMergeDocs = minMergeDocs;
            if (maxMergeDocs > 0)
                indexWriter.maxMergeDocs = maxMergeDocs;
            if (maxFieldLength > 0)
                indexWriter.maxFieldLength = maxFieldLength;
            log.info("Starting to index data, directory: "+PATH +", mergeFactor: "+indexWriter.mergeFactor
                      + ", minMergeDocs: "+indexWriter.minMergeDocs + ", maxMergeDocs: "+indexWriter.maxMergeDocs
                      + ", maxFieldLength: "+indexWriter.maxFieldLength);

            try {
                makeIndexOnArticles(articles.getChild().getChildren());
                makeIndexOnNews();
                makeIndexOnDictionary();
                makeIndexOnFaq();
                makeIndexOnPolls();
                makeIndexOnBlogs(blogs.getChild().getChildren());
                makeIndexOnForums(forums, UrlUtils.PREFIX_FORUM);
                makeIndexOn(hardware, UrlUtils.PREFIX_HARDWARE);
                makeIndexOn(drivers, UrlUtils.PREFIX_DRIVERS);
                makeIndexOn(abc, UrlUtils.PREFIX_CLANKY);
            } finally {
                log.info("Starting to optimize the index");
                indexWriter.optimize();
                indexWriter.close();
            }
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
     * Creates index on subtree starting at root.
     * @param root relation, where to start. It must be already synchronized.
     * @param urlPrefix prefix for URL for this subtree
     */
    static void makeIndexOn(Relation root, String urlPrefix) throws Exception {
        List stack = new ArrayList(100);
        stack.add(root);

        Relation relation;
        GenericObject child;
        Item item;
        MyDocument doc;
        boolean indexChildren;
        String url;

        while(stack.size()>0) {
            relation = (Relation) stack.remove(0);
            child = relation.getChild();
            if (hasBeenIndexed(child))
                continue;

            doc = null; indexChildren = true;
            url = relation.getUrl();
            if (url == null)
                url = urlPrefix + "/show/" + relation.getId();
            if (child instanceof Category) {
                doc = indexCategory(relation);
            } else if (child instanceof Item) {
                item = (Item) child;
                switch ( item.getType() ) {
                    case Item.DISCUSSION:
                        doc = indexDiscussion(item); break;
                    case Item.HARDWARE:
                        doc = indexHardware(item); break;
                    case Item.DRIVER:
                        doc = indexDriver(item); break;
                }
                indexChildren = false;
            }

            if ( doc!=null ) {
                doc.setURL(url);
                doc.setParent(relation.getUpper());
                indexWriter.addDocument(doc.getDocument());
            }

            if (indexChildren) {
                List children = child.getChildren();
                Tools.syncList(children);
                stack.addAll(children);
            }
        }
    }

    /**
     * Indexes content of given forums.
     */
    static void makeIndexOnForums(List forums, String urlPrefix) throws Exception {
        int total, i;
        Relation relation, relation2;
        GenericObject child;
        MyDocument doc;

        for ( Iterator iter = forums.iterator(); iter.hasNext(); ) {
            relation = (Relation) iter.next();
            total = sqlTool.countDiscussionRelationsWithParent(relation.getId());

            for ( i = 0; i<total; ) {
                Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, 100)};
                List discussions = sqlTool.findDiscussionRelationsWithParent(relation.getId(), qualifiers);
                Tools.syncList(discussions);
                i += discussions.size();

                for ( Iterator iter2 = discussions.iterator(); iter2.hasNext(); ) {
                    relation2 = (Relation) iter2.next();
                    child = relation2.getChild();
                    if ( hasBeenIndexed(child) ) continue;
                    try {
                        doc = indexDiscussion((Item)child);
                        doc.setURL(urlPrefix+"/show/"+relation2.getId());
                        doc.setParent(relation2.getUpper());
                        indexWriter.addDocument(doc.getDocument());
                    } catch (InvalidDataException e) {
                        log.error("Cannot index relation "+child.getId(), e);
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
        List relations = sqlTool.findItemRelationsWithType(Item.DICTIONARY, new Qualifier[0]);
        Tools.syncList(relations);
        Relation relation;

        for (Iterator iter = relations.iterator(); iter.hasNext();) {
            relation = (Relation) iter.next();
            child = (Item) relation.getChild();
            if ( hasBeenIndexed(child) )
                continue;
            doc = indexDictionary(relation);
            indexWriter.addDocument(doc.getDocument());
        }
    }

    /**
     * Indexes dictionary.
     */
    static void makeIndexOnPolls() throws Exception {
        Poll child;
        MyDocument doc;
        List relations = sqlTool.findStandalonePollRelations(new Qualifier[0]);
        Tools.syncList(relations);
        Relation relation;

        for (Iterator iter = relations.iterator(); iter.hasNext();) {
            relation = (Relation) iter.next();
            child = (Poll) relation.getChild();
            if ( hasBeenIndexed(child) )
                continue;
            doc = indexPoll(relation);
            indexWriter.addDocument(doc.getDocument());
        }
    }

    /**
     * Indexes frequently asked questions.
     */
    static void makeIndexOnFaq() throws Exception {
        Item child;
        MyDocument doc;
        List relations = sqlTool.findItemRelationsWithType(Item.FAQ, new Qualifier[0]);
        Tools.syncList(relations);
        Relation relation;

        for (Iterator iter = relations.iterator(); iter.hasNext();) {
            relation = (Relation) iter.next();
            child = (Item) relation.getChild();
            if ( hasBeenIndexed(child) )
                continue;
            doc = indexFaq(relation);
            indexWriter.addDocument(doc.getDocument());
        }
    }

    /**
     * Indexes news.
     */
    static void makeIndexOnNews() throws Exception {
        int total = sqlTool.countNewsRelations(), i;
        Relation relation;
        GenericObject child;
        MyDocument doc;

        for ( i = 0; i<total; ) {
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, 100)};
            List data = sqlTool.findNewsRelations(qualifiers);
            Tools.syncList(data);
            i += data.size();

            for ( Iterator iter2 = data.iterator(); iter2.hasNext(); ) {
                relation = (Relation) iter2.next();
                child = relation.getChild();
                if ( hasBeenIndexed(child) )
                    continue;

                doc = indexNews(relation);
                indexWriter.addDocument(doc.getDocument());
            }
        }
    }

    /**
     * Indexes article.
     */
    static void makeIndexOnArticles(List sections) throws Exception {
        int total, i;
        Relation relation;
        GenericObject child;
        MyDocument doc;

        for (Iterator iter = sections.iterator(); iter.hasNext();) {
            Relation sectionRelation = (Relation) iter.next();
            int sectionId = sectionRelation.getChild().getId();
            total = sqlTool.countArticleRelations(sectionId);

            for (i = 0; i < total;) {
                Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, 100)};
                List data = sqlTool.findArticleRelations(qualifiers, sectionId);
                Tools.syncList(data);
                i += data.size();

                for (Iterator iter2 = data.iterator(); iter2.hasNext();) {
                    relation = (Relation) iter2.next();
                    child = relation.getChild();
                    if (hasBeenIndexed(child))
                        continue;

                    doc = indexArticle(relation);
                    if (doc==null)
                        continue;

                    indexWriter.addDocument(doc.getDocument());
                }
            }
        }
    }

    /**
     * Indexes blogs and stories.
     */
    static void makeIndexOnBlogs(List blogs) throws Exception {
        Relation relation;
        GenericObject child;
        MyDocument doc;

        Tools.syncList(blogs);
        for (Iterator iter = blogs.iterator(); iter.hasNext();) {
            relation = (Relation) iter.next();
            child = relation.getChild();

            doc = indexBlog((Category) child);
            indexWriter.addDocument(doc.getDocument());
            makeIndexOnBlog((Category) child);
        }
    }

    /**
     * Indexes blogs and stories.
     */
    static void makeIndexOnBlog(Category blog) throws Exception {
        Relation relation;
        GenericObject child;
        MyDocument doc;

        List qualifiers = new ArrayList();
        CompareCondition ownerCondition = new CompareCondition(Field.OWNER, Operation.EQUAL,new Integer(blog.getOwner()));
        qualifiers.add(ownerCondition);
        Qualifier[] qa = new Qualifier[qualifiers.size()];
        int total = sqlTool.countItemRelationsWithType(Item.BLOG, (Qualifier[]) qualifiers.toArray(qa));
        for ( int i = 0; i<total; ) {
            qa = new Qualifier[]{ownerCondition, Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, 100)};
            List data = sqlTool.findItemRelationsWithType(Item.BLOG, qa);
            Tools.syncList(data);
            i += data.size();

            for ( Iterator iter2 = data.iterator(); iter2.hasNext(); ) {
                relation = (Relation) iter2.next();
                child = relation.getChild();
                if ( hasBeenIndexed(child) )
                    continue;

                doc = indexStory(relation, blog);
                doc.setParent(relation.getUpper());
                indexWriter.addDocument(doc.getDocument());
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
        sb.append(title);

        node = data.element("title");
        if ( node!=null ) {
            sb.append(" ");
            sb.append(node.getText());
        }

        node = data.element("intro");
        if ( node!=null ) {
            sb.append(" ");
            sb.append(node.getText());
        }

        MyDocument doc = new MyDocument(Tools.removeTags(sb.toString()));
        doc.setTitle(title);
        doc.setURL("/blog/"+category.getSubType());
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
        StringBuffer sb = new StringBuffer();
        String title, s;
        Item story = (Item) relation.getChild();

        storeUser(story.getOwner(), sb);
        Element data = story.getData().getRootElement();
        Node node = data.element("name");
        title = node.getText();
        sb.append(title);

        node = data.element("perex");
        if (node!=null) {
            sb.append(" ");
            s = node.getText();
            sb.append(s);
        }

        node = data.element("content");
        sb.append(" ");
        s = node.getText();
        sb.append(s);

        indexDiscussionFor(story);

        MyDocument doc = new MyDocument(Tools.removeTags(sb.toString()));
        doc.setTitle(title);
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
    static MyDocument indexCategory(Relation relation) {
        Category category = (Category) relation.getChild();
        StringBuffer sb = new StringBuffer();
        String title;

        Element data = category.getData().getRootElement();
        Node node = data.element("name");
        title = node.getText();
        sb.append(title);

        node = data.element("note");
        if ( node!=null ) {
            sb.append(" ");
            sb.append(node.getText());
        }

        MyDocument doc = new MyDocument(Tools.removeTags(sb.toString()));
        doc.setTitle(title);
        doc.setType(MyDocument.TYPE_CATEGORY);
        doc.setCreated(category.getCreated());
        doc.setUpdated(category.getUpdated());
        String url = relation.getUrl();
        if (url == null)
            url = UrlUtils.PREFIX_HARDWARE+"/dir/"+relation.getId(); // TODO teoreticky by to nekdy mohlo vadit
        doc.setURL(url);
        doc.setCid(category);
        doc.setBoost(boostSection);
        return doc;
    }

    /**
     * Extracts data for indexing from discussion. Item must be synchronized.
     */
    static MyDocument indexDiscussion(Item discussion) {
        StringBuffer sb = new StringBuffer();
        String title;
        boolean question = false;

        Document document = discussion.getData();
        Element data = document.getRootElement();
        Node node = data.element("title");
        if ( node != null ) {
            title = node.getText();
            sb.append(title);
            question = true;
        } else
            title = "Diskuse";

        node = data.element("text");
        if ( node != null ) {
            sb.append(" ");
            sb.append(node.getText());
        }

        if ( discussion.getChildren().size()>0 ) {
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
                        storeUser(id.intValue(), sb);
                }
            }
        }

        MyDocument doc = new MyDocument(Tools.removeTags(sb.toString()));
        doc.setTitle(title);
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
    static MyDocument indexHardware(Item make) {
        Element data = make.getData().getRootElement();
        String title;

        Node node = data.element("name");
        title = node.getText();
        StringBuffer sb = new StringBuffer(title);
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

        String tmp = Tools.removeTags(sb.toString());
        MyDocument doc = new MyDocument(tmp);
        doc.setTitle(title);
        doc.setCreated(make.getCreated());
        doc.setUpdated(make.getUpdated());
        doc.setType(MyDocument.TYPE_HARDWARE);
        doc.setCid(make);
        doc.setBoost(boostHardware);
        return doc;
    }

    /**
     * Extracts data for indexing from driver. Item must be synchronized.
     */
    static MyDocument indexDriver(Item driver) {
        StringBuffer sb = new StringBuffer();
        String title;

        Element data = driver.getData().getRootElement();
        Node node = data.element("name");
        title = node.getText();
        sb.append(title);

        node = data.element("note");
        if ( node!=null ) {
            sb.append(" ");
            sb.append(node.getText());
        }

        MyDocument doc = new MyDocument(Tools.removeTags(sb.toString()));
        doc.setTitle(title);
        doc.setType(MyDocument.TYPE_DRIVER);
        doc.setCreated(driver.getCreated());
        doc.setUpdated(driver.getUpdated());
        doc.setCid(driver);
        doc.setBoost(boostDriver);
        return doc;
    }

    /**
     * Extracts data for indexing from article. Item must be synchronized.
     */
    static MyDocument indexArticle(Relation relation) throws IOException {
        Item article = (Item) relation.getChild();
        StringBuffer sb = new StringBuffer();
        String title;
        storeUser(article.getOwner(), sb);

        Element data = article.getData().getRootElement();
        if (data.attribute(WhatHappened.INDEXING_FORBIDDEN)!=null)
            return null;

        Node node = data.element("name");
        title = node.getText();
        sb.append(title);
        sb.append(" ");

        node = data.element("perex");
        if ( node!=null ) {
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

        indexDiscussionFor(article);

        MyDocument doc = new MyDocument(Tools.removeTags(sb.toString()));
        doc.setTitle(title);
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
        StringBuffer sb = new StringBuffer();
        String title;

        storeUser(news.getOwner(), sb);

        Element data = news.getData().getRootElement();
        String content = data.element("content").getText();
        sb.append(content);
        sb.append(" ");

        String tmp = Tools.removeTags(content);
        title = Tools.limit(tmp,50," ..");

        Node node = data.element("category");
        String category = null;
        if (node!=null)
            category = node.getText();

        indexDiscussionFor(news);

        MyDocument doc = new MyDocument(Tools.removeTags(sb.toString()));
        doc.setTitle(title);
        doc.setType(MyDocument.TYPE_NEWS);
        doc.setCreated(news.getCreated());
        doc.setUpdated(news.getUpdated());
        if (category!=null)
            doc.setNewsCategory(category);
        String url = relation.getUrl();
        if (url==null)
            url = UrlUtils.PREFIX_NEWS + "/show/" + relation.getId();
        doc.setURL(url);
        doc.setParent(relation.getUpper());
        doc.setCid(news);
        doc.setBoost(boostNews);
        return doc;
    }

    /**
     * Extracts data for indexing from poll. Poll must be synchronized.
     */
    static MyDocument indexPoll(Relation relation) throws IOException {
        Poll poll = (Poll) relation.getChild();
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

        indexDiscussionFor(poll);

        MyDocument doc = new MyDocument(Tools.removeTags(sb.toString()));
        doc.setTitle(title);
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

                MyDocument doc = indexDiscussion(item);

                String parentTitle = Tools.childName(parent);
                String urlPrefix = UrlUtils.PREFIX_FORUM;
                if (parent instanceof Item) {
                    Item parentItem = (Item) parent;
                    if (parentItem.getType() == Item.ARTICLE) {
                        doc.setTitle("Diskuse k èlánku " + parentTitle);
                        urlPrefix = UrlUtils.PREFIX_CLANKY;
                    } else if (parentItem.getType() == Item.BLOG) {
                        doc.setTitle("Diskuse k blogu " + parentTitle);
                        urlPrefix = UrlUtils.PREFIX_BLOG;
                    } else if (parentItem.getType() == Item.NEWS) {
                        doc.setTitle("Diskuse k zprávièce " + parentTitle);
                        urlPrefix = UrlUtils.PREFIX_NEWS;
                    }
                } else if (parent instanceof Poll) {
                    doc.setTitle("Diskuse k anketì " + parentTitle);
                    urlPrefix = UrlUtils.PREFIX_POLLS;
                }

                String url = child.getUrl();
                if (url == null)
                    url = urlPrefix + "/show/" + child.getId();
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
        String title = Tools.xpath(dictionary, "/data/name"), s;
        StringBuffer sb = new StringBuffer(title);
        Relation childRelation;
        Record record;

        for (Iterator iter = dictionary.getChildren().iterator(); iter.hasNext();) {
            childRelation = (Relation) iter.next();
            record = (Record) persistence.findById(childRelation.getChild());
            s = Tools.xpath(record, "//description");
            sb.append(' ');
            sb.append(Tools.removeTags(s));
        }

        MyDocument doc = new MyDocument(sb.toString());
        doc.setTitle(title);
        doc.setType(MyDocument.TYPE_DICTIONARY);
        doc.setCreated(dictionary.getCreated());
        doc.setUpdated(dictionary.getUpdated());
        doc.setURL("/slovnik/"+dictionary.getSubType());
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
        StringBuffer sb = new StringBuffer(title);
        String content = Tools.xpath(faq, "/data/text");
        sb.append(content);

        MyDocument doc = new MyDocument(Tools.removeTags(sb.toString()));
        doc.setTitle(title);
        doc.setType(MyDocument.TYPE_FAQ);
        doc.setCreated(faq.getCreated());
        doc.setUpdated(faq.getUpdated());
        doc.setURL(relation.getUrl());
        doc.setCid(faq);
        doc.setBoost(boostFaq);

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
            GenericObject key = (GenericObject) child.getClass().newInstance();
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
        minMergeDocs = prefs.getInt(PREF_MIN_MERGE_DOCS, 0);
        maxMergeDocs = prefs.getInt(PREF_MAX_MERGE_DOCS, 0);
        maxFieldLength = prefs.getInt(PREF_MAX_FIELD_LENGTH, 0);
        boostArticle = prefs.getFloat(PREF_BOOST_ARTICLE, 1.0f);
        boostBlog = prefs.getFloat(PREF_BOOST_BLOG, 1.0f);
        boostDictionary = prefs.getFloat(PREF_BOOST_DICTIONARY, 1.0f);
        boostDiscussion = prefs.getFloat(PREF_BOOST_DISCUSSION, 1.0f);
        boostDriver = prefs.getFloat(PREF_BOOST_DRIVER, 1.0f);
        boostFaq = prefs.getFloat(PREF_BOOST_FAQ, 1.0f);
        boostHardware = prefs.getFloat(PREF_BOOST_HARDWARE, 1.0f);
        boostNews = prefs.getFloat(PREF_BOOST_NEWS, 1.0f);
        boostQuestion = prefs.getFloat(PREF_BOOST_QUESTION, 1.0f);
        boostSection = prefs.getFloat(PREF_BOOST_SECTION, 1.0f);
        boostPoll = prefs.getFloat(PREF_BOOST_POLL, 1.0f);
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
}
