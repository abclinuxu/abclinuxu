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

import cz.abclinuxu.persistance.*;
import cz.abclinuxu.persistance.cache.OnlyUserCache;
import cz.abclinuxu.persistance.extra.*;
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
import cz.abclinuxu.exceptions.PersistanceException;
import cz.abclinuxu.exceptions.InvalidDataException;
import cz.abclinuxu.scheduler.WhatHappened;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Document;
import org.apache.lucene.index.IndexWriter;

import java.util.*;
import java.util.prefs.Preferences;
import java.io.File;
import java.io.FileWriter;

/**
 * This class is responsible for creating and
 * maintaining Lucene's index.
 * todo indexovat diskuse u clanku, zpravicek a blogu samostatne
 */
public class CreateIndex implements Configurable {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CreateIndex.class);

    public static final String PREF_PATH = "path";
    public static final String PREF_LAST_RUN_NAME = "last.run.file";

    private final String LAST_RUN_FILE = "last_run.txt";

    static String indexPath,lastRunFilename;
    static Persistance persistance;
    static SQLTool sqlTool;
    static HashMap indexed = new HashMap(150000, 0.99f);

    static {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(new CreateIndex());
        persistance = PersistanceFactory.getPersistance(OnlyUserCache.class);
        sqlTool = SQLTool.getInstance();
    }

    public static void main(String[] args) throws Exception {
        String PATH = indexPath;
        if ( args.length>0 ) PATH = args[0];
        log.info("Starting to index data, using directory "+PATH);

        try {
            Relation articles = (Relation) persistance.findById(new Relation(Constants.REL_ARTICLES));
            Relation hardware = (Relation) Tools.sync(new Relation(Constants.REL_HARDWARE));
            Relation drivers = (Relation) Tools.sync(new Relation(Constants.REL_DRIVERS));
            Relation abc = (Relation) Tools.sync(new Relation(Constants.REL_ABC)); // neni cas to smazat?
            Relation blogs = (Relation) persistance.findById(new Relation(Constants.REL_BLOGS));
            List forums = sqlTool.findSectionRelationsWithType(Category.FORUM,null);

            long start = System.currentTimeMillis();

            IndexWriter indexWriter = new IndexWriter(PATH, new AbcCzechAnalyzer(), true);

            try {
                makeIndexOnArticles(indexWriter, articles.getChild().getChildren());
                makeIndexOnNews(indexWriter, UrlUtils.PREFIX_NEWS);
                makeIndexOnDictionary(indexWriter);
                makeIndexOnFaq(indexWriter);
                makeIndexOnBlogs(indexWriter, blogs.getChild().getChildren());
                makeIndexOnForums(indexWriter, forums, UrlUtils.PREFIX_FORUM);
                makeIndexOn(indexWriter, hardware, UrlUtils.PREFIX_HARDWARE);
                makeIndexOn(indexWriter, drivers, UrlUtils.PREFIX_DRIVERS);
                makeIndexOn(indexWriter, abc, UrlUtils.PREFIX_CLANKY);
            } finally {
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
    static void makeIndexOn(IndexWriter indexWriter, Relation root, String urlPrefix) throws Exception {
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
            if (hasBeenIndexed(child)) continue;
//            child = persistance.findById(child);

            doc = null; indexChildren = true; url = relation.getUrl();
            if (child instanceof Category) {
                doc = indexCategory((Category) child);
                if (url==null)
                    url = urlPrefix+"/dir/"+relation.getId();
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
                if (url==null)
                    url = urlPrefix+"/show/"+relation.getId();
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
//                for ( Iterator iter = children.iterator(); iter.hasNext(); )
//                    stack.add(iter.next());
            }
        }
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
     * Indexes content of given forums.
     */
    static void makeIndexOnForums(IndexWriter indexWriter, List forums, String urlPrefix) throws Exception {
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
    static void makeIndexOnDictionary(IndexWriter indexWriter) throws Exception {
        Item child;
        MyDocument doc;
        List relations = sqlTool.findItemRelationsWithType(Item.DICTIONARY, new Qualifier[0]);
        Tools.syncList(relations);
        Relation relation;

        for (Iterator iter = relations.iterator(); iter.hasNext();) {
            relation = (Relation) iter.next();
            child = (Item) relation.getChild();
            if ( hasBeenIndexed(child) ) continue;
            doc = indexDictionary(child);
            indexWriter.addDocument(doc.getDocument());
        }
    }

    /**
     * Indexes frequently asked questions.
     */
    static void makeIndexOnFaq(IndexWriter indexWriter) throws Exception {
        Item child;
        MyDocument doc;
        List relations = sqlTool.findItemRelationsWithType(Item.FAQ, new Qualifier[0]);
        Tools.syncList(relations);
        Relation relation;

        for (Iterator iter = relations.iterator(); iter.hasNext();) {
            relation = (Relation) iter.next();
            child = (Item) relation.getChild();
            if ( hasBeenIndexed(child) ) continue;
            doc = indexFaq(relation);
            indexWriter.addDocument(doc.getDocument());
        }
    }

    /**
     * Indexes news.
     */
    static void makeIndexOnNews(IndexWriter indexWriter, String urlPrefix) throws Exception {
        int total = sqlTool.countNewsRelations(), i;
        Relation relation;
        GenericObject child;
        MyDocument doc;
        String url;

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

                doc = indexNews((Item) child);
                url = relation.getUrl();
                if (url==null)
                    url = urlPrefix + "/show/" + relation.getId();
                doc.setURL(url);
                doc.setParent(relation.getUpper());
                indexWriter.addDocument(doc.getDocument());
            }
        }
    }

    /**
     * Indexes article.
     */
    static void makeIndexOnArticles(IndexWriter indexWriter, List sections) throws Exception {
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

                    doc = indexArticle((Item) child);
                    if (doc==null)
                        continue;

                    doc.setURL(relation.getUrl());
                    doc.setParent(relation.getUpper());
                    indexWriter.addDocument(doc.getDocument());
                }
            }
        }
    }

    /**
     * Indexes blogs and stories.
     */
    static void makeIndexOnBlogs(IndexWriter indexWriter, List blogs) throws Exception {
        Relation relation;
        GenericObject child;
        MyDocument doc;

        Tools.syncList(blogs);
        for (Iterator iter = blogs.iterator(); iter.hasNext();) {
            relation = (Relation) iter.next();
            child = relation.getChild();

            doc = indexBlog((Category) child);
            indexWriter.addDocument(doc.getDocument());
            makeIndexOnBlog(indexWriter, (Category) child);
        }
    }

    /**
     * Indexes blogs and stories.
     */
    static void makeIndexOnBlog(IndexWriter indexWriter, Category blog) throws Exception {
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
        String title = null;

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
        return doc;
    }

    /**
     * Extracts data for indexing from blog.
     * @param relation story relation
     */
    static MyDocument indexStory(Relation relation, Category category) {
        StringBuffer sb = new StringBuffer();
        String title = null, s;
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

        for (Iterator iter = story.getChildren().iterator(); iter.hasNext();) {
            Relation child = (Relation) iter.next();
            if ( child.getChild() instanceof Item ) {
                Item item = (Item) persistance.findById(child.getChild());
                if (item.getType()!=Item.DISCUSSION)
                    continue;

                MyDocument doc = indexDiscussion(item);
                s = doc.getDocument().get(MyDocument.CONTENT);
                if (s!=null) {
                    sb.append(" ");
                    sb.append(s);
                }
            }
        }

        MyDocument doc = new MyDocument(Tools.removeTags(sb.toString()));
        doc.setTitle(title);
        doc.setURL(Tools.getUrlForBlogStory(category.getSubType(), story.getCreated(), relation.getId()));
        doc.setType(MyDocument.TYPE_BLOG);
        doc.setCreated(story.getCreated());
        doc.setUpdated(story.getUpdated());
        return doc;
    }

    /**
     * Extracts data for indexing from category. Category must be synchronized.
     */
    static MyDocument indexCategory(Category category) {
        StringBuffer sb = new StringBuffer();
        String title = null;

        Element data = (Element) category.getData().getRootElement();
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
        return doc;
    }

    /**
     * Extracts data for indexing from discussion. Item must be synchronized.
     */
    static MyDocument indexDiscussion(Item discussion) {
        StringBuffer sb = new StringBuffer();
        String title = null;

        Document document = discussion.getData();
        Element data = (Element) document.getRootElement();
        Node node = data.element("title");
        if ( node!=null ) {
            title = node.getText();
            sb.append(title);
        } else
            title = "Diskuse";

        node = data.element("text");
        if ( node!=null ) {
            sb.append(" ");
            sb.append(node.getText());
        }

        if ( discussion.getChildren().size()>0 ) {
            Record record = (Record) ((Relation) discussion.getChildren().get(0)).getChild();
            record = (Record) persistance.findById(record);
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
        doc.setType(MyDocument.TYPE_DISCUSSION);
        doc.setCreated(discussion.getCreated());
        doc.setUpdated(discussion.getUpdated());
        doc.setQuestionSolved(Tools.isQuestionSolved(document));
        doc.setNumberOfReplies(Tools.xpath(discussion, "/data/comments"));
        return doc;
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
            User user = (User) persistance.findById(new User(id));
            String nick = user.getNick();
            if (nick!=null) {
                sb.append(nick);
                sb.append(" ");
            }
            sb.append(user.getName());
            sb.append(" ");
        } catch (PersistanceException e) {
            // user could be deleted
        }
    }

    /**
     * Extracts data for indexing from hardware. Item must be synchronized.
     */
    static MyDocument indexHardware(Item make) {
        Element data = (Element) make.getData().getRootElement();
        String title = "";

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
        return doc;
    }

    /**
     * Extracts data for indexing from driver. Item must be synchronized.
     */
    static MyDocument indexDriver(Item driver) {
        StringBuffer sb = new StringBuffer();
        String title = null;

        Element data = (Element) driver.getData().getRootElement();
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
        return doc;
    }

    /**
     * Extracts data for indexing from article. Item must be synchronized.
     */
    static MyDocument indexArticle(Item article) {
        StringBuffer sb = new StringBuffer();
        String title = null;
        storeUser(article.getOwner(), sb);

        Element data = (Element) article.getData().getRootElement();
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
                Record record = (Record) persistance.findById(child.getChild());
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
            } else if ( child.getChild() instanceof Item ) {
                // todo indexuj diskuse vzdy samostatne a do titulku pridej jmeno clanku
                Item item = (Item) persistance.findById(child.getChild());
                if (item.getType()!=Item.DISCUSSION) continue;
                MyDocument doc = indexDiscussion(item);
                String diz = doc.getDocument().get(MyDocument.CONTENT);
                if (diz!=null) {
                    sb.append(diz);
                    sb.append(" ");
                }
            }
        }

        MyDocument doc = new MyDocument(Tools.removeTags(sb.toString()));
        doc.setTitle(title);
        doc.setType(MyDocument.TYPE_ARTICLE);
        doc.setCreated(article.getCreated());
        doc.setUpdated(article.getUpdated());
        doc.setBoost(1.5f);
        return doc;
    }

    /**
     * Extracts data for indexing from news. Item must be synchronized.
     */
    static MyDocument indexNews(Item news) {
        StringBuffer sb = new StringBuffer();
        String title;

        storeUser(news.getOwner(), sb);

        Element data = (Element) news.getData().getRootElement();
        String content = data.element("content").getText();
        sb.append(content);
        sb.append(" ");

        String tmp = Tools.removeTags(content);
        title = Tools.limit(tmp,50," ..");

        Node node = data.element("category");
        String category = null;
        if (node!=null)
            category = node.getText();

        for ( Iterator iter = news.getChildren().iterator(); iter.hasNext(); ) {
            Relation child = (Relation) iter.next();

            if ( child.getChild() instanceof Item ) {
                Item item = (Item) persistance.findById(child.getChild());
                if (item.getType()!=Item.DISCUSSION) continue;
                MyDocument doc = indexDiscussion(item);
                String diz = doc.getDocument().get(MyDocument.CONTENT);
                if (diz!=null) {
                    sb.append(diz);
                    sb.append(" ");
                }
            }
        }

        MyDocument doc = new MyDocument(Tools.removeTags(sb.toString()));
        doc.setTitle(title);
        doc.setType(MyDocument.TYPE_NEWS);
        doc.setCreated(news.getCreated());
        doc.setUpdated(news.getUpdated());
        if (category!=null)
            doc.setNewsCategory(category);
        doc.setBoost(0.5f);
        return doc;
    }

    /**
     * Extracts text from dictionary item (and its records) for indexing.
     * @param dictionary initialized item
     */
    static MyDocument indexDictionary(Item dictionary) {
        String title = Tools.xpath(dictionary, "/data/name"), s;
        StringBuffer sb = new StringBuffer(title);
        Relation relation;
        Record record;

        for (Iterator iter = dictionary.getChildren().iterator(); iter.hasNext();) {
            relation = (Relation) iter.next();
            record = (Record) persistance.findById(relation.getChild());
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
        doc.setBoost(2.0f);

        return doc;
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
