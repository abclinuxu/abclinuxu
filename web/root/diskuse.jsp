<%@ page import="java.util.Map,
                 cz.abclinuxu.servlets.utils.template.FMTemplateSelector"%>
<%@ page
  extends="cz.abclinuxu.servlets.html.HTMLServlet"
%>
<%!
    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        return FMTemplateSelector.select("ShowForum", "main", env, request);
    }
%>