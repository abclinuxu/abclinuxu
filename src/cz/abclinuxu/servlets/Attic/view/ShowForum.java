/*
 * User: literakl
 * Date: 14.12.2003
 * Time: 20:28:27
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.exceptions.MissingArgumentException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;

import org.dom4j.Node;

/**
 * Used to display content of the selected discussion forum.
 * Attention: each forum must be set in MySQLPersistance as forbidden
 * to load children and its type must be set to SECTION_FORUM!
 * select potomek from relace where predchozi in (49655,49490,49488,49489);
 */
public class ShowForum implements AbcAction {
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

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_ID_SHORT, Relation.class, params);
        if ( relation==null ) {
            throw new MissingArgumentException("Parametr rid je prázdný!");
        }

        Tools.sync(relation);
        env.put(ViewRelation.VAR_RELATION, relation);
        env.put(VAR_CATEGORY, relation.getChild());
        List parents = persistance.findParents(relation);
        env.put(ViewRelation.VAR_PARENTS, parents);

        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int count = getPageSize(params, env);
        count = Misc.limit(count, 1, 50);

        SQLTool sqlTool = SQLTool.getInstance();
        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(from, count)};
        List discussions = sqlTool.findDiscussionRelationsWithParent(relation.getId(),qualifiers);
        int total = sqlTool.countDiscussionRelationsWithParent(relation.getId());
        Tools.sync(discussions);

        Paging paging = new Paging(discussions, from, count, total);
        env.put(VAR_DISCUSSIONS,paging);

        return FMTemplateSelector.select("ShowForum","show",env,request);
    }

    /**
     * Gets page size for found discussions. Paramaters take precendence over user settings.
     * @return page size for found documents.
     */
    private int getPageSize(Map params, Map env) {
        int count = -1;
        String str = (String) params.get(PARAM_COUNT);
        if ( str!=null && str.length()>0 )
            count = Misc.parseInt(str, -1);

        User user = (User) env.get(Constants.VAR_USER);
        if ( user!=null && count<0 ) {
            Node node = user.getData().selectSingleNode("/data/settings/forum_size");
            if ( node!=null )
                count = Misc.parseInt(node.getText(), -1);
        }

        if ( count==-1 )
            return 40;
        else
            return Misc.limit(count, 10, 100);
    }
}
