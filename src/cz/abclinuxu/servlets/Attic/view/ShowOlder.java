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
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.data.Relation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This servlet is responsible for displaying
 * the range of objects in specified time interval.
 * @todo odstranit duplicitu u linkovanych objektu u SQL_ARTICLES
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

    public static final String SQL_HARDWARE = "select R.cislo from zaznam Z, relace R where typ=1 and Z.cislo=R.potomek and typ_potomka='Z' order by zmeneno desc";
    public static final String SQL_SOFTWARE = "select R.cislo from zaznam Z, relace R where typ=2 and Z.cislo=R.potomek and typ_potomka='Z' order by zmeneno desc";
    public static final String SQL_DRIVERS = "select R.cislo from polozka P, relace R where typ=5 and P.cislo=R.potomek and typ_potomka='P' order by zmeneno desc";
    public static final String SQL_ARTICLES = "select R.cislo from polozka P, relace R where R.predchozi in (2,3,4,5,6,251,5324,8546,12448) and R.typ_potomka='P' and P.typ=2 and P.cislo=R.potomek and P.vytvoreno<now() order by vytvoreno desc";

    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        String type = (String) params.get(PARAM_TYPE);
        int from = Misc.parseInt((String)params.get(PARAM_FROM),0);
        int count = Misc.parseInt((String)params.get(PARAM_COUNT),10);
        count = Misc.limit(count,1,50);

        List found = null, result = new ArrayList(count);
        String sql = null;

        if ( "hardware".equalsIgnoreCase(type) ) {
            sql = SQL_HARDWARE;
            env.put(VAR_TYPE,"hardware");
        } else if ( "software".equalsIgnoreCase(type) ) {
            sql = SQL_SOFTWARE;
            env.put(VAR_TYPE,"software");
        } else if ( "articles".equalsIgnoreCase(type) ) {
            sql = SQL_ARTICLES;
            env.put(VAR_TYPE,"articles");
        } else {
            ServletUtils.addError(PARAM_TYPE,"Chybí parametr typ!",env,null);
            return FMTemplateSelector.select("ViewIndex","show",env,request);
        }

        found = persistance.findByCommand(sql+" limit "+from+","+count);
        for (Iterator iter = found.iterator(); iter.hasNext();) {
            Object[] objects = (Object[]) iter.next();
            int id = ((Integer)objects[0]).intValue();
            Relation relation = (Relation) persistance.findById(new Relation(id));
            result.add(relation);
        }

        env.put(VAR_FOUND,result);
        env.put(VAR_FROM,new Integer(from));
        env.put(VAR_COUNT,new Integer(count));

        return FMTemplateSelector.select("ShowOlder","show",env,request);
    }
}
