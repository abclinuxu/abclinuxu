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
package cz.abclinuxu.utils.config.impl;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.PathGeneratorImpl;
import cz.abclinuxu.utils.PathGenerator;

import java.util.prefs.Preferences;
import java.io.File;

/**
 * Replacement for configuration part of AbcInit servlet.
 */
public class AbcConfig implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbcConfig.class);

    static AbcConfig instance;
    static {
        instance = new AbcConfig();
        ConfigurationManager.getConfigurator().configureAndRememberMe(instance);
    }

    public static final String PREF_DEPLOY_PATH = "deploy.path";
    public static final String PREF_DOMAIN = "domain";
    public static final String PREF_HOSTNAME = "hostname";
    public static final String PREF_DEFAULT_PAGE_SIZE = "default.page.size";
    public static final String PREF_VIEW_USER_PAGE_SIZE = "viewuser.page.size";
    public static final String PREF_SECTION_ARTICLES_COUNT = "section.article.count";
    public static final String PREF_SERIES_ARTICLES_COUNT = "series.article.count";
    public static final String PREF_ARTICLE_SECTION_ARTICLES_COUNT = "article.section.articles.count";
    public static final String PREF_AUTHOR_ARTICLES_PAGE_SIZE = "author.articles.page.size";
    public static final String PREF_INDEX_COMPLETE_ARTICLES = "index.complete.articles";
    public static final String PREF_BAZAAR_PAGE_SIZE = "bazaar.page.size";
    public static final String PREF_SEARCH_RESULTS_COUNT = "search.results.count";
    public static final String PREF_FAQ_COUNT = "section.faq.count";
    public static final String PREF_WATCHED_DISCUSSION_LIMIT = "watched.discussions.limit";
    public static final String PREF_MAINTAINANCE_MODE = "maintainance.mode";
    public static final String PREF_TICKET_LENGTH = "user.ticket.length";
    public static final String PREF_MAILING_LIST_BLOG_WATCH = "mail.blog.watch";
    public static final String PREF_MAILING_LIST_ADMINS = "mailing.list.admini";
    public static final String PREF_WARN_OLD_DISCUSSION_CREATED = "warn.old.diz.created.days";
    public static final String PREF_WARN_OLD_DISCUSSION_COMMENTED = "warn.old.diz.commented.days";
    public static final String PREF_LOGIN_USE_HTTPS = "login.use.https";

    static String deployPath, domain, hostname, blogWatchEmail, adminsEmail;
    static int defaultPageSize, viewUserPageSize, sectionArticleCount, seriesArticleCount, bazaarPageSize;
    static int articleSectionArticlesCount, authorArticlesPageSize, searchResultsCount, faqSectionCount;
    static int maxWatchedDiscussions, ticketLength, oldDiscussionAge, oldDiscussionSleep;
    static int indexCompleteArticles;
    static boolean maintainanceMode, loginUseHttps;

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        domain = prefs.get(PREF_DOMAIN, "abclinuxu.cz");
        hostname = prefs.get(PREF_HOSTNAME, "www.abclinuxu.cz");
        deployPath = prefs.get(PREF_DEPLOY_PATH, null);
        if (! deployPath.endsWith(File.separator))
            deployPath = deployPath.concat(File.separator);
        defaultPageSize = prefs.getInt(PREF_DEFAULT_PAGE_SIZE, 20);
        viewUserPageSize = prefs.getInt(PREF_VIEW_USER_PAGE_SIZE,20);
        sectionArticleCount = prefs.getInt(PREF_SECTION_ARTICLES_COUNT, 25);
        seriesArticleCount = prefs.getInt(PREF_SECTION_ARTICLES_COUNT, 25);
        articleSectionArticlesCount = prefs.getInt(PREF_ARTICLE_SECTION_ARTICLES_COUNT, 5);
        authorArticlesPageSize = prefs.getInt(PREF_AUTHOR_ARTICLES_PAGE_SIZE, 20);
        searchResultsCount = prefs.getInt(PREF_SEARCH_RESULTS_COUNT, 10);
        faqSectionCount = prefs.getInt(PREF_FAQ_COUNT, 20);
        bazaarPageSize = prefs.getInt(PREF_BAZAAR_PAGE_SIZE, 20);
        maxWatchedDiscussions = prefs.getInt(PREF_WATCHED_DISCUSSION_LIMIT, 50);
        ticketLength = prefs.getInt(PREF_TICKET_LENGTH, 10);
        oldDiscussionAge = prefs.getInt(PREF_WARN_OLD_DISCUSSION_CREATED, 30);
        oldDiscussionSleep = prefs.getInt(PREF_WARN_OLD_DISCUSSION_COMMENTED, 10);
        indexCompleteArticles = prefs.getInt(PREF_INDEX_COMPLETE_ARTICLES, 6);
        maintainanceMode = prefs.getBoolean(PREF_MAINTAINANCE_MODE, false);
        blogWatchEmail = prefs.get(PREF_MAILING_LIST_BLOG_WATCH, null);
        adminsEmail = prefs.get(PREF_MAILING_LIST_ADMINS, null);
        loginUseHttps = prefs.getBoolean(PREF_LOGIN_USE_HTTPS, false);
    }

    /**
     * @return singleton of this class
     */
    public static AbcConfig getInstance() {
        return instance;
    }

    /**
     * @return directory, where application was deployed. Last character is directory separator.
     */
    public static String getDeployPath() {
        return deployPath;
    }

    /**
     * @return domain name served by this installation
     */
    public static String getDomain() {
        return domain;
    }

    /**
     * @return hostname of the computer that runs this installation
     */
    public static String getHostname() {
        return hostname;
    }

    /**
     * @return http url to this computer (e.g. http://www.abclinuxu.cz)
     */
    public static String getAbsoluteUrl() {
        return "http://" + hostname;
    }

    /**
     * If given path is not absolute, we expect, that it is relative
     * to deploy path and concatenates them.
     * @return absolute path
     */
    public static String calculateDeployedPath(String path) {
        if ( path==null ) throw new NullPointerException("path cannot be null!");
        if ( path.startsWith(File.separator) )
            return path;
        return deployPath.concat(path);
    }

    /**
     * @return email address of mailing list of administrators that bans stories inappropriate for home page
     */
    public static String getBlogWatchEmail() {
        return blogWatchEmail;
    }

    /**
     * @return email address of administrators mailing list
     */
    public static String getAdminsEmail() {
        return adminsEmail;
    }

    /**
     * @return default page size.
     */
    public static int getDefaultPageSize() {
        return defaultPageSize;
    }

    /**
     * @return size of page with user listing.
     * todo use map to configure page size by type. add String parameter to method signature to choose type
     */
    public static int getViewUserPageSize() {
        return viewUserPageSize;
    }

    /**
     * @return number of articles displayed with perex on home page
     */
    public static int getIndexCompleteArticles() {
        return indexCompleteArticles;
    }

    /**
     * @return default maximum for questions count on FAQ section page
     */
    public static int getFaqSectionCount() {
        return faqSectionCount;
    }

    /**
     * @return default page size for advertisements count in bazaar
     */
    public static int getBazaarPageSize() {
        return bazaarPageSize;
    }

    /**
     * @return number of articles to be displayed in the section.
     */
    public static int getSectionArticleCount() {
        return sectionArticleCount;
    }

    /**
     * @return number of articles to be displayed in the series page.
     */
    public static int getSeriesArticleCount() {
        return seriesArticleCount;
    }

    /**
     * @return number of articles from same section to be displayed in the article.
     */
    public static int getArticleSectionArticlesCount() {
        return articleSectionArticlesCount;
    }

    /**
     * @return number of articles displayed on single page of author
     */
    public static int getAuthorArticlesPageSize() {
        return authorArticlesPageSize;
    }

    /**
     * @return default number of found objects in search page.
     */
    public static int getSearchResultsCount() {
        return searchResultsCount;
    }

    /**
     * @return default number of discussions for which the user can see new comments
     */
    public static int getMaxWatchedDiscussionLimit() {
        return maxWatchedDiscussions;
    }

    /**
     * @return length of the ticket to be generated for the user
     */
    public static int getTicketLength() {
        return ticketLength;
    }

    /**
     * @return number of days since creating a discussion to be considered as old
     */
    public static int getOldDiscussionAge() {
        return oldDiscussionAge;
    }

    /**
     * @return number of days since last comment in old discussion
     */
    public static int getOldDiscussionSleep() {
        return oldDiscussionSleep;
    }

    /**
     * @return instance of PathGenerator
     */
    public static PathGenerator getPathGenerator() {
        return new PathGeneratorImpl();
    }

    /**
     * @return Whether HTTPS should be used for login
     */
    public static boolean getLoginUseHttps() {
        return loginUseHttps;
    }

    /**
     * @return true when portal is in maintainance mode and it shall be in read only mode
     */
    public static boolean isMaintainanceMode() {
        return maintainanceMode;
    }

    /**
     * Sets maintainance mode.
     * @param maintainanceMode true if portal is in maintainance mode and only read-only operations are allowed
     */
    public static void setMaintainanceMode(boolean maintainanceMode) {
        AbcConfig.maintainanceMode = maintainanceMode;
    }
}
