package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.persistance.extra.Qualifier;
import cz.abclinuxu.persistance.extra.LimitQualifier;
import cz.abclinuxu.persistance.SQLTool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;

/**
 * Default page for news.
 * User: literakl
 * Date: 17.4.2005
 */
public class ShowNewsPage implements AbcAction {
    public static final String VAR_CATEGORIES = "CATEGORIES";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, 10)};
        List news = SQLTool.getInstance().findNewsRelations(qualifiers);
        env.put("NEWS", news);

        Map params = (Map) env.get(Constants.VAR_PARAMS);
        env.put(VAR_CATEGORIES, Search.getNewsCategories(params));

        return FMTemplateSelector.select("ViewCategory", "news", env, request);
    }
}
