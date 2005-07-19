/*
 * User: Leos Literak
 * Date: Jun 2, 2003
 * Time: 10:10:55 PM
 */
package cz.abclinuxu.utils.config.impl;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.ConfigurationException;

import java.util.prefs.Preferences;
import java.io.File;

/**
 * Replacement for configuration part of AbcInit servlet.
 */
public class AbcConfig implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbcConfig.class);

    static {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureAndRememberMe(new AbcConfig());
    }

    public static final String PREF_DEPLOY_PATH = "deploy.path";
    public static final String PREF_VIEWUSER_PAGINGSIZE = "viewuser.page.size";
    public static final String PREF_INDEX_DISCUSSIONS_COUNT = "index.discussions.count";
    public static final String PREF_TEMPLATE_NEWS_COUNT = "template.news.count";
    public static final String PREF_INDEX_ARTICLES_COUNT = "index.article.count";
    public static final String PREF_SECTION_ARTICLES_COUNT = "section.article.count";
    public static final String PREF_ARTICLE_SECTION_ARTICLES_COUNT = "article.section.articles.count";
    public static final String PREF_SEARCH_RESULTS_COUNT = "search.results.count";
    public static final String PREF_FAQ_COUNT = "section.faq.count";

    static String deployPath;
    static int viewUserPageSize, indexDiscussionCount, newsCount, indexArticleCount, sectionArticleCount;
    static int articleSectionArticlesCount, searchResultsCount, faqSectionCount;

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        deployPath = prefs.get(PREF_DEPLOY_PATH, null);
        viewUserPageSize = prefs.getInt(PREF_VIEWUSER_PAGINGSIZE,20);
        indexDiscussionCount = prefs.getInt(PREF_INDEX_DISCUSSIONS_COUNT,20);
        newsCount = prefs.getInt(PREF_TEMPLATE_NEWS_COUNT,5);
        indexArticleCount = prefs.getInt(PREF_INDEX_ARTICLES_COUNT, 9);
        sectionArticleCount = prefs.getInt(PREF_SECTION_ARTICLES_COUNT, 25);
        articleSectionArticlesCount = prefs.getInt(PREF_ARTICLE_SECTION_ARTICLES_COUNT, 5);
        searchResultsCount = prefs.getInt(PREF_SEARCH_RESULTS_COUNT, 10);
        faqSectionCount = prefs.getInt(PREF_FAQ_COUNT, 20);
    }

    /**
     * @return directory, where application was deployed
     */
    public static String getDeployPath() {
        return deployPath;
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
        if ( deployPath.endsWith(File.separator) )
            return deployPath.concat(path);
        else
            return deployPath.concat(File.separator).concat(path);
    }

    /**
     * @return size of page with user listing.
     * todo use map to configure page size by type. add String parameter to method signature to choose type
     */
    public static int getViewUserPageSize() {
        return viewUserPageSize;
    }

    /**
     * @return default maximum for discussions count on Index page
     */
    public static int getIndexDiscussionCount() {
        return indexDiscussionCount;
    }

    /**
     * @return default maximum for questions count on FAQ section page
     */
    public static int getFaqSectionCount() {
        return faqSectionCount;
    }

    /**
     * @return default limit for displayed news
     */
    public static int getNewsCount() {
        return newsCount;
    }

    /**
     * @return number of articles to be displayed on the start page.
     */
    public static int getIndexArticleCount() {
        return indexArticleCount;
    }

    /**
     * @return number of articles to be displayed in the section.
     */
    public static int getSectionArticleCount() {
        return sectionArticleCount;
    }

    /**
     * @return number of articles from same section to be displayed in the article.
     */
    public static int getArticleSectionArticlesCount() {
        return articleSectionArticlesCount;
    }

    /**
     * @return default number of found objects in search page.
     */
    public static int getSearchResultsCount() {
        return searchResultsCount;
    }
}
