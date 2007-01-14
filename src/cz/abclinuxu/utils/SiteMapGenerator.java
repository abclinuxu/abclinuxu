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
package cz.abclinuxu.utils;

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.cache.OnlyUserCache;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.zip.GZIPOutputStream;


/**
 * This class generates file sitemap.xml according to http://www.sitemaps.org/
 * specification.
 * TODO the older last modification of the object is the bigger update interval shall be (e.g. daily, weekly, monthly)
 * TODO provide last modification date (optional, find out whether google bot will use it or not)
 * @author literakl
 * @since 30.12.2006
 */
public class SiteMapGenerator implements Configurable {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SiteMapGenerator.class);

    public static final String PREF_INDEX_FILE = "index.file";
    public static final String PREF_MAIN_FILE = "file.main";
    public static final String PREF_FORUM_FILE = "file.forum";

    public static final String ALWAYS = "always";
    public static final String HOURLY = "hourly";
    public static final String DAILY = "daily";
    public static final String WEEKLY = "weekly";
    public static final String MONTHLY = "monthly";
    public static final String YEARLY = "yearly";
    public static final String NEVER = "never";

    static String filenameIndex,filenameMain,filenameForum;
    static String server;
    static boolean debug;
    static Persistence persistence;
    static SQLTool sqlTool;
    static int count;

    static {
        ConfigurationManager.getConfigurator().configureMe(new SiteMapGenerator());
        persistence = PersistenceFactory.getPersistance(OnlyUserCache.class);
        sqlTool = SQLTool.getInstance();
    }

    public static void main(String[] args) throws IOException {
        debug = args.length > 0;
        long start = System.currentTimeMillis();

        Relation articles = (Relation) persistence.findById(new Relation(Constants.REL_ARTICLES));
        Relation hardware = (Relation) Tools.sync(new Relation(Constants.REL_HARDWARE));
        Relation software = (Relation) Tools.sync(new Relation(Constants.REL_SOFTWARE));
        Relation drivers = (Relation) Tools.sync(new Relation(Constants.REL_DRIVERS));
        Relation faqs = (Relation) Tools.sync(new Relation(Constants.REL_FAQ));
        List<Relation> forums = sqlTool.findCategoryRelationsWithType(Category.FORUM, null);

        // index of sitemaps
        File file = new File(AbcConfig.getDeployPath() + File.separator + filenameIndex);
        FileOutputStream fos = new FileOutputStream(file);
        GZIPOutputStream stream = new GZIPOutputStream(fos);
        fos.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes());
        fos.write("<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n".getBytes());
        writeSitemap(filenameMain, fos);
        writeSitemap(filenameForum, fos);
        fos.write("</sitemapindex>".getBytes());
        fos.close();

        // main sitemap file
        file = new File(AbcConfig.getDeployPath() + File.separator + filenameMain);
        fos = new FileOutputStream(file);
        stream = new GZIPOutputStream(fos);
        stream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes());
        stream.write("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n".getBytes());

        writeUrl(server, null, ALWAYS, 1.0f, stream);
        dumpUrlsFor(articles, UrlUtils.PREFIX_CLANKY, stream);
        dumpArticleUrls(articles.getChild().getChildren(), stream);
        dumpUrlsFor(hardware, UrlUtils.PREFIX_HARDWARE, stream);
        dumpUrlsFor(software, UrlUtils.PREFIX_SOFTWARE, stream);
        dumpUrlsFor(drivers, UrlUtils.PREFIX_DRIVERS, stream);
        dumpUrlsFor(faqs, UrlUtils.PREFIX_FAQ, stream);
        dumpFaqUrls(stream);
        writeUrl(server + "/doc/portal/rss-a-jine-pristupy", null, DAILY, 0.8f, stream);
        writeUrl(server + "/hosting", null, DAILY, 0.8f, stream);
        writeUrl(server + "/skoleni", null, DAILY, 0.8f, stream);
        writeUrl(server + "/hry", null, DAILY, 0.8f, stream);
        writeUrl(server + UrlUtils.PREFIX_DICTIONARY, null, DAILY, 0.8f, stream);
        dumpDictionaryUrls(stream);
        writeUrl(server + UrlUtils.PREFIX_POLLS, null, DAILY, 0.8f, stream);
        dumpPollUrls(stream);
        writeUrl(server + UrlUtils.PREFIX_NEWS, null, DAILY, 0.8f, stream);
        dumpNewsUrls(stream);
        writeUrl(server + "/ucebnice", null, DAILY, 0.8f, stream);
        writeUrl(server + UrlUtils.PREFIX_BLOG, null, HOURLY, 0.8f, stream);
        dumpBlogUrls(stream);
        writeUrl(server + UrlUtils.PREFIX_BAZAAR, null, DAILY, 0.8f, stream);

        stream.write("</urlset>".getBytes());
        stream.close();

        // second sitemap file for discussion forum
        file = new File(AbcConfig.getDeployPath() + File.separator + filenameForum);
        fos = new FileOutputStream(file);
        stream = new GZIPOutputStream(fos);
        stream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes());
        stream.write("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n".getBytes());

        writeUrl(server + UrlUtils.PREFIX_FORUM, null, HOURLY, 0.8f, stream);
        for (Relation relation : forums) {
            dumpUrlsFor(relation, UrlUtils.PREFIX_FORUM, stream);
            dumpDiscussionUrls(relation, stream);
        }

        stream.write("</urlset>".getBytes());
        stream.close();

        long end = System.currentTimeMillis();
        String message = "Generating of " + count + " sitemaps urls took " + (end - start) / 1000 + " seconds";
        log.info(message);
        System.out.println(message);
    }

    static void dumpUrlsFor(Relation root, String urlPrefix, OutputStream stream) throws IOException {
        List stack = new ArrayList(100);
        stack.add(root);

        Relation relation;
        GenericObject child;
        boolean indexChildren;
        String url;

        while (stack.size() > 0) {
            relation = (Relation) stack.remove(0);
            child = relation.getChild();
            url = server + UrlUtils.getRelationUrl(relation, urlPrefix);

            if (child instanceof Category) {
                writeUrl(url, null, DAILY, 0.8f, stream);
                indexChildren = true;
            } else {
                writeUrl(url, null, WEEKLY, null, stream);
                indexChildren = false;
            }

            if (indexChildren) {
                List children = child.getChildren();
                Tools.syncList(children);
                stack.addAll(children);
            }
        }
    }

    static void dumpArticleUrls(List<Relation> sections, OutputStream stream) throws IOException {
        int total, i;
        String url;

        for (Relation sectionRelation : sections) {
            int sectionId = sectionRelation.getChild().getId();
            total = sqlTool.countArticleRelations(sectionId);

            for (i = 0; i < total;) {
                Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, 100)};
                List<Relation> data = sqlTool.findArticleRelations(qualifiers, sectionId);
//                Tools.syncList(data);
                i += data.size();

                for (Relation relation : data) {
                    url = server + UrlUtils.getRelationUrl(relation, UrlUtils.PREFIX_CLANKY);
                    writeUrl(url, null, WEEKLY, null, stream);
                }
            }
        }
    }

    static void dumpDiscussionUrls(Relation forum, OutputStream stream) throws IOException {
        int total, i;
        String url;

        total = sqlTool.countDiscussionRelationsWithParent(forum.getId());
        for (i = 0; i < total;) {
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, 100)};
            List<Relation> discussions = sqlTool.findDiscussionRelationsWithParent(forum.getId(), qualifiers);
            Tools.syncList(discussions);
            i += discussions.size();

            for (Relation relation : discussions) {
                url = server + UrlUtils.getRelationUrl(relation, UrlUtils.PREFIX_FORUM);
                writeUrl(url, null, WEEKLY, null, stream);
            }
        }
    }

    static void dumpFaqUrls(OutputStream stream) throws IOException {
        String url;
        List<Relation> relations = sqlTool.findItemRelationsWithType(Item.FAQ, new Qualifier[0]);
//        Tools.syncList(relations);

        for (Relation relation : relations) {
            url = server + UrlUtils.getRelationUrl(relation, UrlUtils.PREFIX_FAQ);
            writeUrl(url, null, DAILY, null, stream);
        }
    }

    static void dumpDictionaryUrls(OutputStream stream) throws IOException {
        String url;
        List<Relation> relations = sqlTool.findItemRelationsWithType(Item.DICTIONARY, new Qualifier[0]);
//        Tools.syncList(relations);

        for (Relation relation : relations) {
            url = server + UrlUtils.getRelationUrl(relation, UrlUtils.PREFIX_DICTIONARY);
            writeUrl(url, null, DAILY, null, stream);
        }
    }

    static void dumpBlogUrls(OutputStream stream) throws IOException {
        String url;
        int total = sqlTool.countCategoryRelationsWithType(Category.BLOG);
        for (int i = 0; i < total;) {
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, 100)};
            List<Relation> data = sqlTool.findCategoryRelationsWithType(Category.BLOG, qualifiers);
            Tools.syncList(data);
            i += data.size();

            for (Relation relation : data) {
                url = server + UrlUtils.getRelationUrl(relation, UrlUtils.PREFIX_BLOG);
                writeUrl(url, null, DAILY, null, stream);
                dumpUrlsForBlog((Category) relation.getChild(), stream);
            }
        }
    }

    static void dumpUrlsForBlog(Category blog, OutputStream stream) throws IOException {
        String url;
        CompareCondition ownerCondition = new CompareCondition(Field.OWNER, Operation.EQUAL, blog.getOwner());
        Qualifier[] qa = new Qualifier[1];
        qa[0] = ownerCondition;
        int total = sqlTool.countItemRelationsWithType(Item.BLOG, qa);
        for (int i = 0; i < total;) {
            qa = new Qualifier[]{ownerCondition, Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, 100)};
            List<Relation> data = sqlTool.findItemRelationsWithType(Item.BLOG, qa);
            Tools.syncList(data);
            i += data.size();

            for (Relation relation : data) {
                url = server + UrlUtils.getRelationUrl(relation, UrlUtils.PREFIX_BLOG);
                writeUrl(url, null, WEEKLY, null, stream);
            }
        }
    }

    static void dumpNewsUrls(OutputStream stream) throws IOException {
        String url;
        int total = sqlTool.countNewsRelations(), i;
        for (i = 0; i < total;) {
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, 100)};
            List<Relation> data = sqlTool.findNewsRelations(qualifiers);
//            Tools.syncList(data);
            i += data.size();

            for (Relation relation : data) {
                url = server + UrlUtils.getRelationUrl(relation, UrlUtils.PREFIX_NEWS);
                writeUrl(url, null, DAILY, null, stream);
            }
        }
    }

    static void dumpPollUrls(OutputStream stream) throws IOException {
        String url;
        List<Relation> relations = sqlTool.findStandalonePollRelations(new Qualifier[0]);
//        Tools.syncList(relations);

        for (Relation relation : relations) {
            url = server + UrlUtils.getRelationUrl(relation, UrlUtils.PREFIX_POLLS);
            writeUrl(url, null, DAILY, null, stream);
        }
    }

    private static void writeUrl(String url, Date lastModified, String frequency, Float priority, OutputStream stream) throws IOException {
        stream.write("<url>".getBytes());
        if (debug) stream.write('\n');

        stream.write("<loc>".getBytes());
        stream.write(encode(url).getBytes());
        stream.write("</loc>".getBytes());
        if (debug) stream.write('\n');

        if (lastModified != null) {
            stream.write("<lastmod>".getBytes());
            stream.write(Constants.isoFormatShort.format(lastModified).getBytes());
            stream.write("</lastmod>".getBytes());
            if (debug) stream.write('\n');
        }

        if (frequency != null) {
            stream.write("<changefreq>".getBytes());
            stream.write(frequency.getBytes());
            stream.write("</changefreq>".getBytes());
            if (debug) stream.write('\n');
        }

        if (priority != null) {
            stream.write("<priority>".getBytes());
            stream.write(priority.toString().getBytes());
            stream.write("</priority>".getBytes());
            if (debug) stream.write('\n');
        }

        stream.write("</url>".getBytes());
        if (debug) stream.write('\n');
        count++;
    }

    private static void writeSitemap(String filename, OutputStream stream) throws IOException {
        stream.write("<sitemap>".getBytes());
        if (debug) stream.write('\n');

        stream.write("<loc>".getBytes());
        stream.write((server + "/" + filename).getBytes());
        stream.write("</loc>".getBytes());
        if (debug) stream.write('\n');

        stream.write("</sitemap>".getBytes());
        if (debug) stream.write('\n');
    }

    /**
     * TODO XML encode &," a ', URL encode all other characters except a-Z, 0-9, colon, slash and dot.
     */
    private static String encode(String url) {
        return url;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        filenameIndex = prefs.get(PREF_INDEX_FILE, null);
        filenameMain = prefs.get(PREF_MAIN_FILE, null);
        filenameForum = prefs.get(PREF_FORUM_FILE, null);
        server = "http://" + AbcConfig.getHostname();
    }
}
