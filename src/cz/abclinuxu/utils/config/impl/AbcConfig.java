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
    public static final String PREF_SECTION_ARTICLES_COUNT = "section.article.count";
    public static final String PREF_ARTICLE_SECTION_ARTICLES_COUNT = "article.section.articles.count";
    public static final String PREF_SEARCH_RESULTS_COUNT = "search.results.count";
    public static final String PREF_FAQ_COUNT = "section.faq.count";

    static String deployPath;
    static int viewUserPageSize, sectionArticleCount;
    static int articleSectionArticlesCount, searchResultsCount, faqSectionCount;

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        deployPath = prefs.get(PREF_DEPLOY_PATH, null);
        viewUserPageSize = prefs.getInt(PREF_VIEWUSER_PAGINGSIZE,20);
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
     * @return default maximum for questions count on FAQ section page
     */
    public static int getFaqSectionCount() {
        return faqSectionCount;
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
