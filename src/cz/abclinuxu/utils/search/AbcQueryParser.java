/*
 * User: literakl
 * Date: 6.1.2004
 * Time: 21:12:49
 */
package cz.abclinuxu.utils.search;

import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.CharStream;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.index.Term;
import cz.abclinuxu.servlets.view.Search;

import java.util.Iterator;

/**
 * Customized query parser.
 */
public class AbcQueryParser extends QueryParser {
    /** fields to be searched */
    static final String[] fields = new String[]{MyDocument.CONTENT, MyDocument.TITLE};

    public AbcQueryParser(CharStream stream) {
        super(stream);
    }

    /**
     * Parses query.
     * @param queryString string holding the query.
     * @param analyzer Analyzer to use
     * @throws ParseException if query parsing fails
     */
    public static Query parse(String queryString, Analyzer analyzer, Search.Types types) throws ParseException {
        QueryParser queryParser = new QueryParser(MyDocument.CONTENT, analyzer);
        queryParser.setOperator(QueryParser.DEFAULT_OPERATOR_AND);
        Query query = queryParser.parse(queryString);

        if (types.isNothingSelected() || types.isEverythingSelected())
            return query;

        BooleanQuery typeQuery = new BooleanQuery();
        TermQuery termQuery;
        String type;

        for ( Iterator iter = types.getMap().keySet().iterator(); iter.hasNext(); ) {
            type = (String) iter.next();
            termQuery = new TermQuery(new Term(MyDocument.TYPE, type));
            typeQuery.add(termQuery, false, false);
        }

        BooleanQuery combined = new BooleanQuery();
        combined.add(query, true, false);
        combined.add(typeQuery, true, false);
        query = combined;

        return query;
    }

    /**
     * Add parent to query, if it is defined.
     * @return modified query
     */
    public static Query addParentToQuery(String parent, Query query) {
        if (parent==null || parent.length()==0)
            return query;

        BooleanQuery combined = new BooleanQuery();
        combined.add(query, true, false);
        TermQuery parentQuery = new TermQuery(new Term(MyDocument.PARENT, parent));
        combined.add(parentQuery, true, false);
        return combined;
    }
}
