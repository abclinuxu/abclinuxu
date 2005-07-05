package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.extra.Qualifier;
import cz.abclinuxu.persistance.extra.LimitQualifier;
import cz.abclinuxu.data.Item;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;

/**
 * User: literakl
 * Date: 5.7.2005
 */
public class ViewGuestBook implements AbcAction {
    /** n-th oldest object, where display from */
    public static final String PARAM_FROM = "from";
    /** how many object to display */
    public static final String PARAM_COUNT = "count";

    public static final String VAR_ENTRIES = "ENTRIES";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int count = Misc.parseInt((String) params.get(PARAM_FROM), 20);
        count = Misc.limit(count, 1, 50);

        SQLTool sqlTool = SQLTool.getInstance();
        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(from, count)};
        List discussions = sqlTool.findItemRelationsWithType(Item.GUESTBOOK, qualifiers);
        int total = sqlTool.countItemRelationsWithType(Item.GUESTBOOK, null);
        Tools.syncList(discussions);

        Paging paging = new Paging(discussions, from, count, total);
        env.put(VAR_ENTRIES, paging);

        return FMTemplateSelector.select("ViewGuestBook", "show", env, request);
    }
}
