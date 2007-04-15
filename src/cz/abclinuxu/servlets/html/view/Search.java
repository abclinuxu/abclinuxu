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
import cz.abclinuxu.utils.news.NewsCategories;
import cz.abclinuxu.utils.news.NewsCategory;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.utils.search.CreateIndex;
import cz.abclinuxu.utils.search.AbcCzechAnalyzer;
import cz.abclinuxu.utils.search.AbcQueryParser;
import cz.abclinuxu.utils.search.MyDocument;
import cz.abclinuxu.data.view.SearchResult;
import cz.abclinuxu.persistence.SQLTool;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.analysis.TokenStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.io.StringReader;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Performs search across the data.
 */
public class Search implements AbcAction {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Search.class);

    /** contains relation, that match the expression */
    public static final String VAR_RESULT = "RESULT";
    /** query to be searched */
    public static final String VAR_QUERY = "QUERY";
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

    /** expression to be searched */
    public static final String PARAM_QUERY = "dotaz";
    /** type of object to search */
    public static final String PARAM_TYPE = "typ";
    /** n-th oldest object, from where to display */
    public static final String PARAM_FROM = "from";
    /** how many objects to display */
    public static final String PARAM_COUNT = "count";
    /** id of parental relation */
    public static final String PARAM_PARENT = "parent";
    /** news category */
    public static final String PARAM_CATEGORY = "category";

    static IndexReader indexReader;
    static Date lastUpdated;


    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        return performSearch(request, env);
    }

    public static String performSearch(HttpServletRequest request, Map env) throws Exception {
        boolean initIndexReader = indexReader == null, lastRunFileMissing = false;
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int count = Misc.getPageSize(AbcConfig.getSearchResultsCount(), 100, env, "/data/settings/found_size");

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

        Types types = new Types(params.get(PARAM_TYPE));
        env.put(VAR_TYPES, types);
        String uri = (String) env.get(Constants.VAR_REQUEST_URI);
        boolean onlyNews = uri.startsWith(UrlUtils.PREFIX_NEWS);
        NewsCategoriesSet newsCategoriesSet = getNewsCategories(params);
        String baseUrl = getCurrentUrl(onlyNews, params);
        env.put(VAR_CURRENT_URL, baseUrl);

        String queryString = (String) params.get(PARAM_QUERY);
        if ( queryString == null || queryString.length()==0 )
            return choosePage(onlyNews, newsCategoriesSet, request, env);
        env.put(VAR_QUERY,queryString);

        long start = System.currentTimeMillis(), end;
        AbcCzechAnalyzer analyzer = new AbcCzechAnalyzer();
        Query query;
        try {
            query = AbcQueryParser.parse(queryString, analyzer, types, newsCategoriesSet);
            query = AbcQueryParser.addParentToQuery((String)params.get(PARAM_PARENT), query);
            if (params.get(PARAM_FROM) == null) // user is on the first page of the result
                logSearch(queryString);
        } catch (ParseException e) {
            ServletUtils.addError(PARAM_QUERY, "Hledaný řetězec obsahuje chybu!", env, null);
            return choosePage(onlyNews, newsCategoriesSet, request, env);
        }

        try {
            if (initIndexReader)
                indexReader = IndexReader.open(CreateIndex.getIndexPath());

            query = query.rewrite(indexReader);
            Searcher searcher = new IndexSearcher(indexReader);
            Hits hits = searcher.search(query);
            end = System.currentTimeMillis();

            // vytvoreni query a hledani trva do 10 ms, highlight trva 300 ms
            SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<span class=\"highlight\">", "</span>");
            Highlighter highlighter = new Highlighter(formatter, new QueryScorer(query));

            List<SearchResult> list = new ArrayList<SearchResult>(count);
            int total = hits.length();
            SearchResult foundItem;
            for ( int i = from, j = 0; i < total && j < count; i++, j++ ) {
                Document doc = hits.doc(i);
                // todo bug #196
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
            return choosePage(onlyNews, newsCategoriesSet, request, env);
        }

        return choosePage(onlyNews, newsCategoriesSet, request, env);
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
     * @return absolute url (without host)
     */
    private static String getCurrentUrl(boolean news, Map params) {
        StringBuffer sb = new StringBuffer();
        if (news)
            sb.append(UrlUtils.PREFIX_NEWS);
        sb.append("/hledani");
        boolean asterisk = true;
        for (Iterator iter = params.keySet().iterator(); iter.hasNext();) {
            String param = (String) iter.next();
            if (PARAM_FROM.equals(param))
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
        return sb.toString();
    }

    private static String choosePage(boolean displayNews, NewsCategoriesSet newsCategories, HttpServletRequest request, Map env) throws Exception {
        if (displayNews) {
            env.put(VAR_NEWS_CATEGORIES, newsCategories);
            return FMTemplateSelector.select("Search", "news", env, request);
        } else
            return FMTemplateSelector.select("Search", "show", env, request);
    }

    /**
     * Converts selected categories to list.
     */
    private static List getSelectedCategories(Map params) {
        return Tools.asList(params.get(PARAM_CATEGORY));
    }

    public static NewsCategoriesSet getNewsCategories(Map params) {
        List selected = getSelectedCategories(params);
        return new NewsCategoriesSet(selected);
    }

    public static class NewsCategoriesSet extends AbstractCollection {
        List list;
        List selected;

        public NewsCategoriesSet(List selected) {
            this.selected = selected;
            Collection categories = NewsCategories.getAllCategories();
            list = new ArrayList(categories.size());
            for ( Iterator iter = categories.iterator(); iter.hasNext(); ) {
                NewsCategory newsCategory = (NewsCategory) iter.next();
                add(new SelectedNewsCategory(newsCategory, selected));
            }
        }

        public boolean isNothingSelected() {
            return selected.size()==0;
        }

        public boolean isEverythingSelected() {
            return selected.size()==list.size();
        }

        public List getSelected() {
            return selected;
        }

        public Iterator iterator() {
            return list.iterator();
        }

        public int size() {
            return list.size();
        }

        public boolean add(Object o) {
            return list.add(o);
        }
    }

    public static class SelectedNewsCategory extends NewsCategory {
        boolean set;

        public SelectedNewsCategory(NewsCategory category, List selected) {
            super(category.getKey(), category.getName(), category.getDesc());
            setSet(selected);
        }

        public boolean isSet() {
            return set;
        }

        public void setSet(boolean set) {
            this.set = set;
        }

        public void setSet(List selected) {
            if (selected.size()==0)
                set = true;
            else
                set = selected.contains(getKey());
        }
    }

    public static class Types {
        Map map = new HashMap();

        public Types(Object param) {
            List params = Tools.asList(param);
            for ( Iterator iter = params.iterator(); iter.hasNext(); ) {
                String s = (String) iter.next();
                map.put(s, Boolean.TRUE);
            }
        }

        public boolean isNothingSelected() {
            return map.size()==0;
        }

        public boolean isEverythingSelected() {
            return map.size()==MyDocument.ALL_TYPES_COUNT;
        }

        public boolean isArticle() {
            if (map.size() == 0)
                return true;
            return map.containsKey(MyDocument.TYPE_ARTICLE);
        }

        public boolean isBlog() {
            if (map.size() == 0)
                return true;
            return map.containsKey(MyDocument.TYPE_BLOG);
        }

        public boolean isBazaar() {
            if (map.size() == 0)
                return true;
            return map.containsKey(MyDocument.TYPE_BAZAAR);
        }

        public boolean isSection() {
            if ( map.size()==0 )
                return true;
            return map.containsKey(MyDocument.TYPE_CATEGORY);
        }

        public boolean isDictionary() {
            if (map.size() == 0)
                return true;
            return map.containsKey(MyDocument.TYPE_DICTIONARY);
        }

        public boolean isDiscussion() {
            if (map.size() == 0)
                return true;
            return map.containsKey(MyDocument.TYPE_DISCUSSION);
        }

        public boolean isDocument() {
            if (map.size() == 0)
                return true;
            return map.containsKey(MyDocument.TYPE_DOCUMENT);
        }

        public boolean isDriver() {
            if (map.size() == 0)
                return true;
            return map.containsKey(MyDocument.TYPE_DRIVER);
        }

        public boolean isFaq() {
            if (map.size() == 0)
                return true;
            return map.containsKey(MyDocument.TYPE_FAQ);
        }

        public boolean isHardware() {
            if ( map.size()==0 )
                return true;
            return map.containsKey(MyDocument.TYPE_HARDWARE);
        }

        public boolean isNews() {
            if (map.size() == 0)
                return true;
            return map.containsKey(MyDocument.TYPE_NEWS);
        }

        public boolean isPoll() {
            if (map.size() == 0)
                return true;
            return map.containsKey(MyDocument.TYPE_POLL);
        }

        public boolean isQuestion() {
            if ( map.size()==0 )
                return true;
            return map.containsKey(MyDocument.TYPE_QUESTION);
        }

        public boolean isSoftware() {
            if (map.size() == 0)
                return true;
            return map.containsKey(MyDocument.TYPE_SOFTWARE);
        }

        public Map getMap() {
            return Collections.unmodifiableMap(map);
        }

        public int size() {
            return map.size();
        }
    }
}
