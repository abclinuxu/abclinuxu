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
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.utils.search.CreateIndex;
import cz.abclinuxu.utils.search.AbcCzechAnalyzer;
import cz.abclinuxu.utils.search.AbcQueryParser;
import cz.abclinuxu.utils.search.MyDocument;
import cz.abclinuxu.data.view.SearchResult;
import cz.abclinuxu.data.view.DocumentTypes;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.utils.forms.DocumentTypesSet;
import cz.abclinuxu.utils.forms.NewsCategoriesSet;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.analysis.TokenStream;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.io.StringReader;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.prefs.Preferences;

/**
 * Performs search across the data.
 */
public class Search implements AbcAction, Configurable {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Search.class);

    /** contains relation, that match the expression */
    public static final String VAR_RESULT = "RESULT";
    /** query to be searched */
    public static final String VAR_QUERY = "QUERY";
    /** extra google query string */
    public static final String VAR_EXTRA_QUERY = "EXTRA_QUERY";
    /** total number of found documents */
    public static final String VAR_TOTAL = "TOTAL";
    /** holds map of chosen types */
    public static final String VAR_TYPES = "TYPES";
    /** date when index was updated last time */
    public static final String VAR_UPDATED = "UPDATED";
    /** number of milliseconds that shows how long search took */
    public static final String VAR_SEARCH_TIME = "SEARCH_TIME";
    public static final String VAR_NEWS_CATEGORIES = "CATEGORIES";
    /** current url with all parameters except temporary or paging */
    public static final String VAR_CURRENT_URL = "CURRENT_URL";
    /** base url without any parameters */
    public static final String VAR_BASE_URL = "BASE_URL";
    public static final String VAR_GOOGLE_PARAMS = "GOOGLE_PARAMS";

    /** expression to be searched */
    public static final String PARAM_QUERY = "dotaz";
    /** type of object to search */
    public static final String PARAM_TYPE = "typ";
    /** id of parental relation */
    public static final String PARAM_PARENT = "parent";
    /** news category */
    public static final String PARAM_CATEGORY = "category";
    public static final String PARAM_ACTION = "action";
    public static final String PARAM_ADVANCED_MODE = "advancedMode";
    public static final String PARAM_GOOGLE="google";

    public static final String ACTION_TO_ADVANCED_MODE = "toAdvanced";

    public static final String PREF_GOOGLE_PARAMS_CX = "google_params.cx";
    public static final String PREF_GOOGLE_PARAMS_COF = "google_params.cof";

    static IndexReader indexReader;
    static Date lastUpdated;
    static Map<String,String> googleParams;

    public Search() {
        ConfigurationManager.getConfigurator().configureAndRememberMe(this);
    }

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        if (params.containsKey(PARAM_GOOGLE))
            return performGoogleSearch(request, env);
        else
            return performSearch(request, env);
    }

    public static String performSearch(HttpServletRequest request, Map env) throws Exception {
        boolean initIndexReader = indexReader == null, lastRunFileMissing = false;
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        boolean toAdvanced = ACTION_TO_ADVANCED_MODE.equals(params.get(PARAM_ACTION));
        String uri = (String) env.get(Constants.VAR_REQUEST_URI);
        boolean onlyNews = uri.startsWith(UrlUtils.PREFIX_NEWS);

        File file = CreateIndex.getLastRunFile();
        if (! file.exists()) {
            lastRunFileMissing = true;
            ServletUtils.addError(Constants.ERROR_GENERIC, "Index je porušen, vyhledávání nemusí fungovat.", env, null);
        } else {
            if (lastUpdated == null || lastUpdated.getTime() < file.lastModified()) {
                lastUpdated = new Date(file.lastModified());
                initIndexReader = true;
            }
            env.put(VAR_UPDATED, lastUpdated);
        }

        DocumentTypesSet types = new DocumentTypesSet(params.get(PARAM_TYPE), ! toAdvanced, DocumentTypes.Types.SEARCH);
        env.put(VAR_TYPES, types);
        NewsCategoriesSet newsCategoriesSet = new NewsCategoriesSet(params.get(PARAM_CATEGORY));
        env.put(VAR_NEWS_CATEGORIES, newsCategoriesSet);
        if (toAdvanced)
            params.put(PARAM_ADVANCED_MODE, "true");

        setCurrentUrl(onlyNews, params, env);

        String queryString = (String) params.get(PARAM_QUERY);
        env.put(VAR_QUERY, queryString);
        if (queryString == null || queryString.length() == 0 || toAdvanced)
            return choosePage(onlyNews, request, env);

        int from = Misc.parseInt((String) params.get(Constants.PARAM_FROM), 0);
        int count = Misc.getPageSize(AbcConfig.getSearchResultsCount(), 100, env, "/data/settings/found_size");
        Sort sort = detectSort(params);

        long start = System.currentTimeMillis(), end;
        AbcCzechAnalyzer analyzer = new AbcCzechAnalyzer();
        Query query;
        try {
            query = AbcQueryParser.parse(queryString, analyzer, types, newsCategoriesSet);
            query = AbcQueryParser.addParentToQuery((String)params.get(PARAM_PARENT), query);
            if (params.get(Constants.PARAM_FROM) == null) // user is on the first page of the result
                logSearch(queryString);
        } catch (ParseException e) {
            ServletUtils.addError(PARAM_QUERY, "Hledaný řetězec obsahuje chybu!", env, null);
            return choosePage(onlyNews, request, env);
        }

        try {
            if (initIndexReader)
                indexReader = IndexReader.open(CreateIndex.getIndexPath());

            query = query.rewrite(indexReader);
            Searcher searcher = new IndexSearcher(indexReader);
            Hits hits = searcher.search(query, sort);
            end = System.currentTimeMillis();

            // vytvoreni query a hledani trva do 10 ms, highlight trva 300 ms
            SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<span class=\"highlight\">", "</span>");
            Highlighter highlighter = new Highlighter(formatter, new QueryScorer(query, MyDocument.CONTENT));

            List<SearchResult> list = new ArrayList<SearchResult>(count);
            int total = hits.length();
            SearchResult foundItem;
            for ( int i = from, j = 0; i < total && j < count; i++, j++ ) {
                Document doc = hits.doc(i);
                String text = doc.get(MyDocument.TITLE) + " " + doc.get(MyDocument.CONTENT);
                TokenStream tokenStream = analyzer.tokenStream(MyDocument.CONTENT, new StringReader(text));
                String fragment = highlighter.getBestFragments(tokenStream, text, 3, "...");

                foundItem = new SearchResult(doc);
                foundItem.setHighlightedText(fragment);
                list.add(foundItem);
            }

//            end = System.currentTimeMillis();
            env.put(VAR_SEARCH_TIME, end - start);

            Paging paging = new Paging(list,from,count,total);
            env.put(VAR_RESULT,paging);
            env.put(VAR_TOTAL, total);
        } catch (Exception e) {
            log.error("Cannot search '"+query+"'",e);
            if (lastRunFileMissing)
                ServletUtils.addError(PARAM_QUERY,"Došlo k chybě při hledání. Kontaktujte prosím správce.",env,null);
            else
                ServletUtils.addError(PARAM_QUERY,"Nemohu provést dané hledání. Zkuste zadat jiný řetězec.",env,null);
            return choosePage(onlyNews, request, env);
        }

        return choosePage(onlyNews, request, env);
    }

    public static String performGoogleSearch(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        boolean toAdvanced = ACTION_TO_ADVANCED_MODE.equals(params.get(PARAM_ACTION));

        DocumentTypesSet types = new DocumentTypesSet(params.get(PARAM_TYPE), ! toAdvanced, DocumentTypes.Types.SEARCH);
        env.put(VAR_TYPES, types);

        String queryString = (String) params.get(PARAM_QUERY);
        env.put(VAR_QUERY, queryString);

        String extraQueryString = "";

        if (!types.isEverythingSelected()) {
            for(DocumentTypesSet.SelectedDocumentType doc : types.values()) {
                if (!doc.isSet() || doc.getGoogleQuery() == null)
                    continue;

                if (extraQueryString.length() != 0)
                    extraQueryString += " OR ";
                extraQueryString += doc.getGoogleQuery();
            }
        }
        
        setCurrentUrl(false, params, env);

        env.put(VAR_EXTRA_QUERY, extraQueryString);
        env.put(VAR_GOOGLE_PARAMS, googleParams);

        return FMTemplateSelector.select("Search", "show", env, request);
    }

    /**
     * Finds out sorting parameters for current query
     * @param params query parameters
     * @return sort instance or null, if default relevance sort shall be used
     */
    private static Sort detectSort(Map params) {
        String sDir = (String) params.get(Constants.PARAM_ORDER_DIR);
        boolean descending = Constants.ORDER_DIR_DESC.equals(sDir);

        String sBy = (String) params.get(Constants.PARAM_ORDER_BY);
        if (Constants.ORDER_BY_RELEVANCE.equals(sBy))
            return null;
        if (Constants.ORDER_BY_CREATED.equals(sBy))
            return new Sort(MyDocument.CREATED, descending);
        if (Constants.ORDER_BY_UPDATED.equals(sBy))
            return new Sort(MyDocument.UPDATED, descending);
        return null;
    }

    /**
     * Logs the search query, so we can know the statistics.
     * @param query non-normalized search query, it must not be null
     */
    public static void logSearch(String query) {
        SQLTool sqlTool = SQLTool.getInstance();
        sqlTool.recordSearchedQuery(query.toLowerCase());
    }

    /**
     * Creates current URL without information about current page.
     * @param news whether the current url is serach news
     * @param params
     */
    private static void setCurrentUrl(boolean news, Map params, Map env) {
        StringBuffer sb = new StringBuffer();
        if (news)
            sb.append(UrlUtils.PREFIX_NEWS);
        sb.append("/hledani");
        env.put(VAR_BASE_URL, sb.toString());

        boolean asterisk = true;
        for (Iterator iter = params.keySet().iterator(); iter.hasNext();) {
            String param = (String) iter.next();
            if (Constants.PARAM_FROM.equals(param))
                continue;

            List values = Tools.asList(params.get(param));
            for (Iterator iterValues = values.iterator(); iterValues.hasNext();) {
                String value = (String) iterValues.next();
                if (asterisk) {
                    asterisk = false;
                    sb.append('?');
                } else
                    sb.append('&');

                try {
                    value = URLEncoder.encode(value, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    log.warn(e, e);
                }
                sb.append(param).append('=').append(value);
            }
        }
        env.put(VAR_CURRENT_URL, sb.toString());
    }

    private static String choosePage(boolean displayNews, HttpServletRequest request, Map env) throws Exception {
        if (displayNews) {
            return FMTemplateSelector.select("Search", "news", env, request);
        } else
            return FMTemplateSelector.select("Search", "show", env, request);
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        googleParams = new HashMap(2);
        googleParams.put("cx", prefs.get(PREF_GOOGLE_PARAMS_CX, null));
        googleParams.put("cof", prefs.get(PREF_GOOGLE_PARAMS_COF, null));
    }
}
