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
import cz.abclinuxu.utils.search.CreateIndex;
import cz.abclinuxu.utils.search.AbcCzechAnalyzer;
import cz.abclinuxu.utils.search.AbcQueryParser;
import cz.abclinuxu.utils.search.MyDocument;
import org.apache.lucene.search.*;
import org.apache.lucene.queryParser.ParseException;
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
    /** holds map of chosen types */
    public static final String VAR_TYPES = "TYPES";

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

        String queryString = (String) params.get(PARAM_QUERY);
        if ( queryString == null || queryString.length()==0 ) {
            return FMTemplateSelector.select("Search","show",env,request);
        }
        env.put(VAR_QUERY,queryString);
        Types types = new Types(params.get(PARAM_TYPE));
        env.put(VAR_TYPES, types);

        Query query = null;
        try {
            query = AbcQueryParser.parse(queryString, new AbcCzechAnalyzer(), types);
        } catch (ParseException e) {
            ServletUtils.addError(PARAM_QUERY, "Hledaný øetìzec obsahuje chybu!", env, null);
            return FMTemplateSelector.select("Search", "show", env, request);
        }

        try {
            int from = getFrom(params);
            int count = Misc.parseInt((String) params.get(PARAM_COUNT), 50);
            count = Misc.limit(count, 1, 100);
            List list = new ArrayList(count);
            NumberFormat percentFormat = NumberFormat.getPercentInstance();

            Searcher searcher = new IndexSearcher(CreateIndex.getIndexPath());
            Hits hits = searcher.search(query);
            int total = hits.length();
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

    public static class Types {
        Map map = new HashMap();

        public Types(Object param) {
            if (param==null)
                return;

            List params;
            if (param instanceof String) {
                params = new ArrayList(1);
                params.add(param);
            } else
                params = (List) param;

            for ( Iterator iter = params.iterator(); iter.hasNext(); ) {
                String s = (String) iter.next();
                map.put(s, Boolean.TRUE);
            }
        }

        public boolean isNoneSelected() {
            return map.size()==0;
        }

        public boolean isSection() {
            if ( map.size()==0 )
                return true;
            return map.containsKey(MyDocument.TYPE_CATEGORY);
        }

        public boolean isHardware() {
            if ( map.size()==0 )
                return true;
            return map.containsKey(MyDocument.TYPE_HARDWARE);
        }

        public boolean isSoftware() {
            if ( map.size()==0 )
                return true;
            return map.containsKey(MyDocument.TYPE_SOFTWARE);
        }

        public boolean isDriver() {
            if ( map.size()==0 )
                return true;
            return map.containsKey(MyDocument.TYPE_DRIVER);
        }

        public boolean isDiscussion() {
            if ( map.size()==0 )
                return true;
            return map.containsKey(MyDocument.TYPE_DISCUSSION);
        }

        public boolean isArticle() {
            if ( map.size()==0 )
                return true;
            return map.containsKey(MyDocument.TYPE_ARTICLE);
        }

        public boolean isNews() {
            if ( map.size()==0 )
                return true;
            return map.containsKey(MyDocument.TYPE_NEWS);
        }

        public Map getMap() {
            return Collections.unmodifiableMap(map);
        }
    }
}
