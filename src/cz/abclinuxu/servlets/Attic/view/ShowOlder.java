/*
 * User: literakl
 * Date: 4.9.2002
 * Time: 9:43:38
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcVelocityServlet;
import cz.abclinuxu.servlets.utils.VelocityTemplateSelector;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.data.Relation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.context.Context;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This servlet is responsible for displaying
 * the range of objects in specified time interval.
 */
public class ShowOlder extends AbcVelocityServlet {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ShowOlder.class);

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
    public static final String SQL_ARTICLES = "select R.cislo from polozka P, relace R where typ=2 and P.cislo=R.potomek and typ_potomka='P' and vytvoreno<now() order by zmeneno desc";

    /**
     * Put your processing here. Return null, if you redirected browser to another URL.
     */
    protected String process(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);
        Map params = (Map) request.getAttribute(AbcVelocityServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        String type = (String) params.get(PARAM_TYPE);
        int from = Misc.parseInt((String)params.get(PARAM_FROM),0);
        int count = Misc.parseInt((String)params.get(PARAM_COUNT),10);
        count = Misc.limit(count,1,50);

        List found = null, result = new ArrayList(count);
        String sql = null;

        if ( "hardware".equalsIgnoreCase(type) ) {
            sql = SQL_HARDWARE;
            ctx.put(VAR_TYPE,"hardware");
        } else if ( "software".equalsIgnoreCase(type) ) {
            sql = SQL_SOFTWARE;
            ctx.put(VAR_TYPE,"software");
        } else if ( "articles".equalsIgnoreCase(type) ) {
            sql = SQL_ARTICLES;
            ctx.put(VAR_TYPE,"articles");
        } else {
            ServletUtils.addError(PARAM_TYPE,"Chybí parametr typ!",ctx,null);
            return VelocityTemplateSelector.selectTemplate(request,ctx,"ViewIndex","show");
        }

        found = persistance.findByCommand(sql+" limit "+from+","+count);
        for (Iterator iter = found.iterator(); iter.hasNext();) {
            Object[] objects = (Object[]) iter.next();
            int id = ((Integer)objects[0]).intValue();
            Relation relation = (Relation) persistance.findById(new Relation(id));
            result.add(relation);
        }

        ctx.put(VAR_FOUND,result);
        ctx.put(VAR_FROM,new Integer(from));
        ctx.put(VAR_COUNT,new Integer(count));

        return VelocityTemplateSelector.selectTemplate(request,ctx,"ShowOlder","show");
    }
}
