/*
 * User: literakl
 * Date: 14.12.2003
 * Time: 20:28:27
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Tools;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.exceptions.MissingArgumentException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;

/**
 * Used to display content of the selected discussion forum.
 * Attention: each forum must be set in MySQLPersistance as forbidden
 * to load children and its type must be set to SECTION_FORUM!
 * select potomek from relace where predchozi in (49655,49490,49488,49489);
 */
public class ShowForum extends AbcFMServlet {
    public static final String PARAM_RELATION_ID_SHORT = "rid";
    /** n-th oldest object, where display from */
    public static final String PARAM_FROM = "from";
    /** how many object to display */
    public static final String PARAM_COUNT = "count";

    /** holds forum to be displayed */
    public static final String VAR_CATEGORY = "ITEM";
    /** holds list of discussions */
    public static final String VAR_DISCUSSIONS = "DIZS";

    static Persistance persistance = PersistanceFactory.getPersistance();

    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_ID_SHORT, Relation.class, params);
        if ( relation==null ) {
            throw new MissingArgumentException("Parametr rid je pr�zdn�!");
        }

        Tools.sync(relation);
        env.put(ViewRelation.VAR_RELATION, relation);
        env.put(VAR_CATEGORY, relation.getChild());
        List parents = persistance.findParents(relation);
        env.put(ViewRelation.VAR_PARENTS, parents);

        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int count = Misc.parseInt((String) params.get(PARAM_COUNT), 40);
        count = Misc.limit(count, 1, 50);

        SQLTool sqlTool = SQLTool.getInstance();
        List discussions = sqlTool.findDiscussionRelationsByCreatedIn(relation.getId(),from,count);
        int total = sqlTool.getDiscussionCountIn(relation.getId());
        Tools.sync(discussions);

        Paging paging = new Paging(discussions, from, count, total);
        env.put(VAR_DISCUSSIONS,paging);

        return FMTemplateSelector.select("ShowForum","show",env,request);
    }
}
