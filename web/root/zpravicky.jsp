<%@ page import="java.io.IOException,
                 java.util.Map,
                 cz.abclinuxu.servlets.utils.template.FMTemplateSelector,
                 cz.abclinuxu.persistance.extra.Qualifier,
                 cz.abclinuxu.persistance.extra.LimitQualifier,
                 java.util.List,
                 cz.abclinuxu.persistance.SQLTool,
                 cz.abclinuxu.servlets.html.view.Search,
                 cz.abclinuxu.servlets.Constants"%>
<%@ page
  extends="cz.abclinuxu.servlets.AbcFMServlet"
%>
<%!
    public static final String VAR_CATEGORIES = "CATEGORIES";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, 10)};
        List news = SQLTool.getInstance().findNewsRelations(qualifiers);
        env.put("NEWS",news);

        Map params = (Map) env.get(Constants.VAR_PARAMS);
        env.put(VAR_CATEGORIES, Search.getNewsCategories(params));

        return FMTemplateSelector.select("ViewCategory", "news", env, request);
    }
%>