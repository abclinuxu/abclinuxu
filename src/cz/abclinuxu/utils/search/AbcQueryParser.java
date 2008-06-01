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

import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.index.Term;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;

import cz.abclinuxu.utils.forms.DocumentTypesSet;
import cz.abclinuxu.utils.forms.NewsCategoriesSet;
import java.util.Map;
import java.util.HashMap;
import java.util.prefs.Preferences;

/**
 * Customized query parser.
 */
public class AbcQueryParser implements Configurable {
    public static final String PREF_USE_MULTIFIED_QUERY_PARSER = "use.multifieldqueryparser";
    public static final String PREF_BOOST_TITLE = "boost.title";
    public static final String PREF_BOOST_CONTENT = "boost.content";

    static final String[] fields = new String[]{MyDocument.CONTENT, MyDocument.TITLE};
    static float boostTitle, boostContent;
    static boolean useMutliFieldQueryParser;
    static Map<String, Float> boostMap;

    static {
        AbcQueryParser instance = new AbcQueryParser();
        ConfigurationManager.getConfigurator().configureAndRememberMe(instance);
    }

    /**
     * Parses query.
     * @param queryString string holding the query.
     * @param analyzer Analyzer to use
     * @param types DocumentTypes searched
     * @param categories NewsCategories searched
     * @throws ParseException if query parsing fails
     */
    public static Query parse(String queryString, Analyzer analyzer, 
            DocumentTypesSet types, NewsCategoriesSet categories) throws ParseException {
        QueryParser queryParser = null;
        if (useMutliFieldQueryParser)
            queryParser = new MultiFieldQueryParser(fields, analyzer, boostMap);
        else
            queryParser = new QueryParser(MyDocument.CONTENT, analyzer);
        queryParser.setDefaultOperator(QueryParser.AND_OPERATOR);
        Query query = queryParser.parse(queryString);

        BooleanQuery combined = new BooleanQuery();
        combined.add(query, BooleanClause.Occur.MUST);
        boolean combination = false;

        BooleanQuery typeQuery = new BooleanQuery();
        BooleanQuery categoryQuery = new BooleanQuery();
        TermQuery termQuery;

        if ( !(types.isNothingSelected() || types.isEverythingSelected()) ) {
            for (String type: types.selectedSet()) {
                termQuery = new TermQuery(new Term(MyDocument.TYPE, type));
                typeQuery.add(termQuery, BooleanClause.Occur.SHOULD);
            }
            combined.add(typeQuery, BooleanClause.Occur.MUST);
            combination = true;
        }

        if ( !(categories.isNothingSelected() || categories.isEverythingSelected()) ) {
            for (String category: categories.selectedSet()) {
                termQuery = new TermQuery(new Term(MyDocument.NEWS_CATEGORY, category));
                categoryQuery.add(termQuery, BooleanClause.Occur.SHOULD);
            }
            combined.add(categoryQuery, BooleanClause.Occur.MUST);
            combination = true;
        }
        return (combination)? combined : query;
    }

    /**
     * Add parent to query, if it is defined.
     * @return modified query
     */
    public static Query addParentToQuery(String parent, Query query) {
        if (parent==null || parent.length()==0)
            return query;

        BooleanQuery combined = new BooleanQuery();
        combined.add(query, BooleanClause.Occur.MUST);
        TermQuery parentQuery = new TermQuery(new Term(MyDocument.PARENT, parent));
        combined.add(parentQuery, BooleanClause.Occur.MUST);
        return combined;
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        boostTitle = prefs.getFloat(PREF_BOOST_TITLE, 1.0f);
        boostContent = prefs.getFloat(PREF_BOOST_CONTENT, 1.0f);
        useMutliFieldQueryParser = prefs.getBoolean(PREF_USE_MULTIFIED_QUERY_PARSER, true);

        boostMap = new HashMap<String, Float>(3, 1.0f);
        boostMap.put(MyDocument.CONTENT, boostContent);
        boostMap.put(MyDocument.TITLE, boostTitle);
    }
}
