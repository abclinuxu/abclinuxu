/*
 * User: literakl
 * Date: Apr 21, 2002
 * Time: 8:51:06 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.search.MyDocument;
import cz.abclinuxu.utils.search.CreateIndex;
import org.apache.lucene.search.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.text.NumberFormat;

/**
 * Performs search across the data.
 */
public class Search extends AbcFMServlet {
    org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Search.class);

    /** contains relation, that match the expression */
    public static final String VAR_RESULT = "RESULT";
    /** query to be searched */
    public static final String VAR_QUERY = "QUERY";
    /** total number of found documents */
    public static final String VAR_TOTAL = "TOTAL";

    /** expression to be searched */
    public static final String PARAM_QUERY = "query";

    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String query = (String) params.get(PARAM_QUERY);
        if ( query == null || query.length()==0 ) {
            ServletUtils.addError(PARAM_QUERY,"Prosím zadejte hledaný øetìzec!",env,null);
            return FMTemplateSelector.select("Search","show",env,request);
        }
        env.put(VAR_QUERY,query);

        try {
            Searcher searcher = new IndexSearcher(CreateIndex.getIndexPath());
            Query q = QueryParser.parse(query, "contents", new StandardAnalyzer());

            Hits hits = searcher.search(q);

            int length = hits.length();
            Map map = new HashMap(length + 1,1.0f); //no rehash
            NumberFormat percentFormat = NumberFormat.getPercentInstance();
            for ( int i=0; i<length; i++ ) {
                Document doc = hits.doc(i);
                if ( hits.score(i)<0.01 )
                    continue;
                String score = percentFormat.format(hits.score(i));
                doc.add(Field.UnIndexed("score",score));
                Misc.storeToMap(map,doc.get(MyDocument.TYPE),doc);
            }
            env.put(VAR_RESULT,map);
            env.put(VAR_TOTAL,new Integer(length));
        } catch (Exception e) {
            log.error("Cannot search "+query,e);
            ServletUtils.addError(PARAM_QUERY,"Nemohu provést dané hledání. Zadejte jiný øetìzec!",env,null);
            return FMTemplateSelector.select("Search","show",env,request);
        }

        return FMTemplateSelector.select("Search","show",env,request);
    }

    public static void main(String[] args) throws Exception {
        Searcher searcher = new IndexSearcher(CreateIndex.getIndexPath());
        Analyzer analyzer = new StandardAnalyzer();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("Query: ");
            String line = in.readLine();
            if ( line.length()==0 ) break;

            Query query = QueryParser.parse(line, "contents", analyzer);
            System.out.println("Searching for: " + query.toString("contents"));

            Hits hits = searcher.search(query);
            System.out.println(hits.length() + " total matching documents");

            final int HITS_PER_PAGE = 10;
            for (int start = 0; start < hits.length(); start += HITS_PER_PAGE) {
                int end = Math.min(hits.length(), start + HITS_PER_PAGE);
                for (int i = start; i < end; i++) {
                    Document doc = hits.doc(i);
                    String url = doc.get("url");
                    System.out.println(i + ". ("+hits.score(i)+") " + url);
                }

                if ( hits.length()>end ) {
                    System.out.print("more (y/n) ? ");
                    line = in.readLine();
                    if ( line.length()==0 || line.charAt(0)=='n' )
                        break;
                }
            }
        }
        searcher.close();
    }
}
