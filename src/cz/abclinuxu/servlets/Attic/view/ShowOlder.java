/*
 * User: literakl
 * Date: 4.9.2002
 * Time: 9:43:38
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.data.Record;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * This servlet is responsible for displaying
 * the range of objects in specified time interval.
 * todo odstranit duplicitu u linkovanych objektu u SQL_ARTICLES
 */
public class ShowOlder extends AbcFMServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ShowOlder.class);

    /** type of object to display */
    public static final String PARAM_TYPE = "type";
    /** n-th oldest object, where display from */
    public static final String PARAM_FROM = "from";
    /** how many object to display */
    public static final String PARAM_COUNT = "count";

    /** list of found relations, that match the conditions */
    public static final String VAR_FOUND = "FOUND";
    /** start point */
    public static final String VAR_FROM = "FROM";
    /** count of relations to display */
    public static final String VAR_COUNT = "COUNT";
    /** normalized type */
    public static final String VAR_TYPE = "TYPE";

    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        String type = (String) params.get(PARAM_TYPE);
        int from = Misc.parseInt((String)params.get(PARAM_FROM),0);
        int count = Misc.parseInt((String)params.get(PARAM_COUNT),10);
        count = Misc.limit(count,1,50);

        List found = new ArrayList(count);

        if ( "articles".equalsIgnoreCase(type) ) {
            found = SQLTool.getInstance().findArticleRelationsByCreated(from,count);
            env.put(VAR_TYPE,"articles");
        } else if ( "news".equalsIgnoreCase(type) ) {
            found = SQLTool.getInstance().findNewsRelationsByCreated(from,count);
            env.put(VAR_TYPE,"news");
        } else if ( "hardware".equalsIgnoreCase(type) ) {
            found = SQLTool.getInstance().findRecordRelationsByUpdated(Record.HARDWARE, from,count);
            env.put(VAR_TYPE,"hardware");
        } else if ( "software".equalsIgnoreCase(type) ) {
            found = SQLTool.getInstance().findRecordRelationsByUpdated(Record.SOFTWARE, from,count);
            env.put(VAR_TYPE,"software");
        } else if ( "discussions".equalsIgnoreCase(type) ) {
            found = SQLTool.getInstance().findDiscussionRelationsByCreated(from,count);
            env.put(VAR_TYPE,"discussions");
        } else {
            ServletUtils.addError(PARAM_TYPE,"Chybí parametr type!",env,null);
            return FMTemplateSelector.select("ViewIndex","show",env,request);
        }

        env.put(VAR_FOUND,found);
        env.put(VAR_FROM,new Integer(from));
        env.put(VAR_COUNT,new Integer(count));

        return FMTemplateSelector.select("ShowOlder","show",env,request);
    }
}
