/*
 * User: literakl
 * Date: Apr 21, 2002
 * Time: 8:51:06 AM
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.utils.search.MyDocument;
import cz.abclinuxu.utils.search.CreateIndex;
import cz.abclinuxu.utils.search.AbcCzechAnalyzer;
import org.apache.lucene.search.*;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.text.NumberFormat;

/**
 * Performs search across the data.
 */
public class Search extends AbcFMServlet {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Search.class);

    /** contains relation, that match the expression */
    public static final String VAR_RESULT = "RESULT";
    /** query to be searched */
    public static final String VAR_QUERY = "QUERY";
    /** total number of found documents */
    public static final String VAR_TOTAL = "TOTAL";

    /** expression to be searched */
    public static final String PARAM_QUERY = "query";
    /** type of object to search */
    public static final String PARAM_TYPE = "type";
    /** n-th oldest object, from where to display */
    public static final String PARAM_FROM = "from";
    /** how many objects to display */
    public static final String PARAM_COUNT = "count";

    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        return performSearch(request, env);
    }

    public static String performSearch(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String query = (String) params.get(PARAM_QUERY);
        if ( query == null || query.length()==0 ) {
            ServletUtils.addError(PARAM_QUERY,"Prosím zadejte hledaný øetìzec!",env,null);
            return FMTemplateSelector.select("Search","show",env,request);
        }
        env.put(VAR_QUERY,query);

        try {
            Searcher searcher = new IndexSearcher(CreateIndex.getIndexPath());
            Query q = QueryParser.parse(query, MyDocument.CONTENT, new AbcCzechAnalyzer());

            Hits hits = searcher.search(q);

            int from = getFrom(params);
            int count = Misc.parseInt((String) params.get(PARAM_COUNT), 50);
            count = Misc.limit(count, 1, 100);

            List list = new ArrayList(count);
            int total = hits.length();
            NumberFormat percentFormat = NumberFormat.getPercentInstance();

            for ( int i=from,j=0; i<total && j<count; i++, j++ ) {
                Document doc = hits.doc(i);
                float score = (hits.score(i)>0.01) ? hits.score(i) : 0.01f;
                doc.add(Field.UnIndexed("score", percentFormat.format(score)));
                list.add(doc);
            }

            Paging paging = new Paging(list,from,count,total);
            env.put(VAR_RESULT,paging);
            env.put(VAR_TOTAL,new Integer(total));
        } catch (Exception e) {
            log.error("Cannot search "+query,e);
            ServletUtils.addError(PARAM_QUERY,"Nemohu provést dané hledání. Zadejte jiný øetìzec!",env,null);
            return FMTemplateSelector.select("Search","show",env,request);
        }

        return FMTemplateSelector.select("Search","show",env,request);
    }

    /**
     * Extracts value of FROM encoded in parameter name.
     */
    private static int getFrom(Map params) {
        for ( Iterator iter = params.keySet().iterator(); iter.hasNext(); ) {
            String param = (String) iter.next();
            if (!param.startsWith(PARAM_FROM)) continue;
            if (param.length()<6) continue;
            int from = Misc.parseInt(param.substring(5), 0);
            return from;
        }
        return 0;
    }
}
