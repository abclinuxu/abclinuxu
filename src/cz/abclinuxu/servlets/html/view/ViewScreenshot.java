package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.ReadRecorder;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.LimitQualifier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;

/**
 */
public class ViewScreenshot implements AbcAction {
    public static final String PARAM_RELATION = "rid";
    /** n-th oldest object, where display from */
    public static final String PARAM_FROM = "from";
    /** how many object to display */
    public static final String PARAM_COUNT = "count";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_ITEM = "ITEM";
    public static final String VAR_ITEMS = "ITEMS";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Parametr rid je prázdný!");

        Tools.sync(relation);
        env.put(ShowObject.VAR_RELATION, relation);
        Persistence persistence = PersistenceFactory.getPersistence();
        List parents = persistence.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        if (relation.getId() == Constants.REL_SCREENSHOTS)
            return processSection(request, env);
        else
            return processItem(request, relation, env);
    }

    public static String processSection(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        SQLTool sqlTool = SQLTool.getInstance();

        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int count = Misc.getPageSize(10, 50, env, "/data/settings/forum_size"); // todo generic listing size setting
        int total = sqlTool.countItemRelationsWithType(Item.SCREENSHOT, null);

        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(from, count)};
        List list = sqlTool.findItemRelationsWithType(Item.SCREENSHOT, qualifiers);
        Tools.syncList(list);

        Paging paging = new Paging(list, from, count, total);
        env.put(VAR_ITEMS, paging);

        env.put(Constants.VAR_RSS, FeedGenerator.getScreenshotsFeedUrl());
        return FMTemplateSelector.select("Screenshot", "showList", env, request);
    }

    public static String processItem(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Item item = (Item) relation.getChild();
        env.put(VAR_ITEM, item);
        ReadRecorder.log(item, Constants.COUNTER_READ, env);

        Map children = Tools.groupByType(item.getChildren());
        env.put(ShowObject.VAR_CHILDREN_MAP, children);

        env.put(Constants.VAR_RSS, FeedGenerator.getScreenshotsFeedUrl());
        return FMTemplateSelector.select("Screenshot", "show", env, request);
    }
}