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
import cz.abclinuxu.utils.Tools;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.Qualifier;
import cz.abclinuxu.persistance.LimitQualifier;
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
    /** specifies attribute, by which data shall be sorted */
    public static final String PARAM_ORDER_BY = "orderBy";
    /** specifies direction of sort order */
    public static final String PARAM_ORDER_DIR = "orderDir";

    public static final String VALUE_ORDER_BY_CREATED = "create";
    public static final String VALUE_ORDER_BY_UPDATED = "update";
    public static final String VALUE_ORDER_BY_ID = "id";

    public static final String VALUE_ORDER_DIR_ASC = "asc";
    public static final String VALUE_ORDER_DIR_DESC = "desc";

    public static final String VALUE_TYPE_ARTICLES = "articles";
    public static final String VALUE_TYPE_NEWS = "news";
    public static final String VALUE_TYPE_HARDWARE = "hardware";
    public static final String VALUE_TYPE_SOFTWARE = "software";
    public static final String VALUE_TYPE_DISCUSSION = "discussions";

    static final Qualifier[] QUALIFIERS_ARRAY = new Qualifier[]{};

    /** list of found relations, that match the conditions */
    public static final String VAR_FOUND = "FOUND";
    /** normalized type */
    public static final String VAR_TYPE = "TYPE";

    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        SQLTool sqlTool = SQLTool.getInstance();

        String type = (String) params.get(PARAM_TYPE);
        int from = Misc.parseInt((String)params.get(PARAM_FROM),0);
        int count = Misc.parseInt((String)params.get(PARAM_COUNT),10);
        count = Misc.limit(count,1,50);

        Paging found = null;
        Qualifier[] qualifiers;

        if ( VALUE_TYPE_ARTICLES.equalsIgnoreCase(type) ) {
            qualifiers = getQualifiers(params, Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, from, count);
            List data = sqlTool.findArticleRelations(qualifiers);
            int total = sqlTool.countArticleRelations();
            found = new Paging(data,from,count,total,qualifiers);
            type = VALUE_TYPE_ARTICLES;

        } else if ( VALUE_TYPE_NEWS.equalsIgnoreCase(type) ) {
            qualifiers = getQualifiers(params, Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, from, count);
            List data = sqlTool.findNewsRelations(qualifiers);
            int total = sqlTool.countNewsRelations();
            found = new Paging(data, from, count, total, qualifiers);
            type = VALUE_TYPE_NEWS;

        } else if ( VALUE_TYPE_HARDWARE.equalsIgnoreCase(type) ) {
            qualifiers = getQualifiers(params, Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, from, count);
            List data = sqlTool.findRecordRelationsWithType(Record.HARDWARE, qualifiers);
            int total = sqlTool.countRecordRelationsWithType(Record.HARDWARE);
            found = new Paging(data, from, count, total, qualifiers);
            type = VALUE_TYPE_HARDWARE;

        } else if ( VALUE_TYPE_SOFTWARE.equalsIgnoreCase(type) ) {
            qualifiers = getQualifiers(params, Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, from, count);
            List data = sqlTool.findRecordRelationsWithType(Record.SOFTWARE, qualifiers);
            int total = sqlTool.countRecordRelationsWithType(Record.SOFTWARE);
            found = new Paging(data, from, count, total, qualifiers);
            type = VALUE_TYPE_SOFTWARE;

        } else if ( VALUE_TYPE_DISCUSSION.equalsIgnoreCase(type) ) {
            qualifiers = getQualifiers(params, Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, from, count);
            List data = sqlTool.findDiscussionRelations(qualifiers);
            int total = sqlTool.countDiscussionRelations();
            found = new Paging(data, from, count, total, qualifiers);
            type = VALUE_TYPE_DISCUSSION;

        } else
            return ServletUtils.showErrorPage("Chybí parametr type!",env,request);

        Tools.sync(found.getData());
        env.put(VAR_FOUND,found);
        env.put(VAR_TYPE, type);
        return FMTemplateSelector.select("ShowOlder",type,env,request);
    }

    /**
     * Gets qualifiers, which user might overwrote in params.
     * @param params Map of parameters.
     * @param sortBy Optional sortBy Qualifier.
     * @param sortDir Optional sort direction Qualifier.
     * @param fromRow Optional first row of data to be fetched.
     * @param rowCount 0 means do not set LimiQualifier. Otherwise it sets size of page to be fetched.
     * @return Qualifiers.
     */
    public static Qualifier[] getQualifiers(Map params, Qualifier sortBy, Qualifier sortDir, int fromRow, int rowCount) {
        String sBy = (String) params.get(PARAM_ORDER_BY);
        if (sBy!=null) {
            if (VALUE_ORDER_BY_CREATED.equals(sBy))
                sortBy = Qualifier.SORT_BY_CREATED;
            else if (VALUE_ORDER_BY_UPDATED.equals(sBy))
                sortBy = Qualifier.SORT_BY_UPDATED;
            else if (VALUE_ORDER_BY_ID.equals(sBy))
                sortBy = Qualifier.SORT_BY_ID;
        }

        String sDir = (String) params.get(PARAM_ORDER_DIR);
        if (sDir!=null) {
            if ( VALUE_ORDER_DIR_ASC.equals(sDir) )
                sortDir = Qualifier.ORDER_ASCENDING;
            else if ( VALUE_ORDER_DIR_DESC.equals(sDir) )
                sortDir = Qualifier.ORDER_DESCENDING;
        }

        LimitQualifier limit = null;
        if (rowCount>0)
            limit = new LimitQualifier(fromRow,rowCount);

        List qualifiers = new ArrayList(3);
        if (sortBy!=null)
            qualifiers.add(sortBy);
        if (sortDir!=null)
            qualifiers.add(sortDir);
        if (limit!=null)
            qualifiers.add(limit);

        return (Qualifier[]) qualifiers.toArray(QUALIFIERS_ARRAY);
    }
}
