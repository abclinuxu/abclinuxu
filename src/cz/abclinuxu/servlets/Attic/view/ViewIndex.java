/*
 * User: literakl
 * Date: Jan 14, 2002
 * Time: 9:20:03 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.utils.Tools;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.paging.Paging;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import org.dom4j.Node;

/**
 * This servlet renders index page of AbcLinuxu.
 */
public class ViewIndex extends AbcFMServlet {
    public static final String VAR_HARDWARE = "HARDWARE";
    public static final String VAR_SOFTWARE = "SOFTWARE";
    public static final String VAR_FORUM = "FORUM";
    public static final String VAR_ARTICLES = "ARTICLES";

    /**
     * Evaluate the request.
     */
    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);
        Tools tools = new Tools();

        Category currentArticles = (Category) persistance.findById(new Category(Constants.CAT_ACTUAL_ARTICLES));
        tools.sync(currentArticles.getContent());
        List articles = Sorters2.byDate(currentArticles.getContent(), Sorters2.DESCENDING);
        env.put(ViewIndex.VAR_ARTICLES,articles);

        Category hw = (Category) persistance.findById(new Category(Constants.CAT_386));
        env.put(ViewIndex.VAR_HARDWARE,hw.getContent());

        Category sw = (Category) persistance.findById(new Category(Constants.CAT_SOFTWARE));
        env.put(ViewIndex.VAR_SOFTWARE,sw.getContent());

        int userLimit = getNumberOfDiscussions(user);
        if ( userLimit>0 ) {
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, userLimit)};
            List found = SQLTool.getInstance().findDiscussionRelations(qualifiers);
            Tools.sync(found);
            List discussions = tools.analyzeDiscussions(found);
            Paging paging = new Paging(discussions, 0, userLimit);
            env.put(ViewIndex.VAR_FORUM, paging);
        }

        return FMTemplateSelector.select("ViewIndex","show",env, request);
    }

    /**
     * Gets limit on discussions displayed on main page. If user is not authenticated or he didn't
     * configured this value, default value will be used.
     * @param user
     * @return number of discussions to be displayed
     */
    private int getNumberOfDiscussions(User user) {
        int defaultValue = AbcConfig.getViewIndexDiscussionsCount();
        if ( user==null )
            return defaultValue;
        Node node = user.getData().selectSingleNode("/data/settings/index_discussions");
        if ( node==null )
            return defaultValue;
        int count = Misc.parseInt(node.getText(),defaultValue);
        if (count==-1) count = defaultValue;
        return count;
    }
}
