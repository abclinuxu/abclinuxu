/*
 * User: literakl
 * Date: Apr 21, 2002
 * Time: 8:51:06 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcVelocityServlet;
import cz.abclinuxu.servlets.utils.VelocityHelper;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.VelocityTemplateSelector;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.search.MyDocument;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.lucene.search.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;
import java.io.*;

/**
 * Performs search across the data.
 */
public class Search extends AbcVelocityServlet {
    static String INDEX_PATH = "/home/literakl/ROOT/WEB-INF/index";

    /** contains relation, that match the expression */
    public static final String VAR_RESULT = "RESULT";
    /** query to be searched */
    public static final String VAR_QUERY = "QUERY";
    /** total number of found documents */
    public static final String VAR_TOTAL = "TOTAL";

    /** expression to be searched */
    public static final String PARAM_QUERY = "query";


    protected String process(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Map params = (Map) request.getAttribute(AbcVelocityServlet.ATTRIB_PARAMS);
        String query = (String) params.get(PARAM_QUERY);
        if ( query == null || query.length()==0 ) {
            ServletUtils.addError(PARAM_QUERY,"Prosím zadejte hledaný øetìzec!",ctx,null);
            return VelocityTemplateSelector.selectTemplate(request,ctx,"Search","show");
        }

        try {
            Searcher searcher = new IndexSearcher(INDEX_PATH);
            Query q = QueryParser.parse(query, "contents", new StandardAnalyzer());
            ctx.put(VAR_QUERY,q.toString("contents"));

            Hits hits = searcher.search(q);
            int length = hits.length();
            Map map = new HashMap(5/4 * length + 1); //no rehash
            for ( int i=0; i<length; i++ ) {
                Document doc = (Document) hits.doc(i);
                String score = new Float(hits.score(i)).toString();
                doc.add(Field.UnIndexed("score",score));
                Misc.storeToMap(map,doc.get(MyDocument.TYPE),doc);
            }
            ctx.put(VAR_RESULT,map);
            ctx.put(VAR_TOTAL,new Integer(length));
        } catch (Exception e) {
            ServletUtils.addError(PARAM_QUERY,"Nemohu provést dané hledání. Zadejte jiný øetìzec!",ctx,null);
            return VelocityTemplateSelector.selectTemplate(request,ctx,"Search","show");
        }

        return VelocityTemplateSelector.selectTemplate(request,ctx,"Search","show");
    }

    /**
     * Sets path to directory with index files.
     */
    public static void setIndexPath(String path) {
        Search.INDEX_PATH = path;
    }

    /**
     * @return path, where index files are stored.
     */
    public static String getIndexPath() {
        return INDEX_PATH;
    }

    public static void main(String[] args) throws Exception {
        Searcher searcher = new IndexSearcher(INDEX_PATH);
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
