package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.User;
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
import cz.abclinuxu.persistence.extra.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;

/**
 */
public class ViewDesktop implements AbcAction {
    public static final String PARAM_RELATION = "rid";
    /** n-th oldest object, where display from */
    public static final String PARAM_FROM = "from";
    /** how many object to display */
    public static final String PARAM_COUNT = "count";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_ITEM = "ITEM";
    public static final String VAR_ITEMS = "ITEMS";
    public static final String VAR_MY_OLDER_DESKTOPS = "MY_OLDER_DESKTOPS";

    public static final String ACTION_USERS = "users";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Parametr rid je prázdný!");
        env.put(Constants.VAR_CANONICAL_URL, UrlUtils.getCanonicalUrl(relation, env));
        env.put(ShowObject.VAR_RELATION, relation);

        Persistence persistence = PersistenceFactory.getPersistence();
        List parents = persistence.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        if (relation.getId() == Constants.REL_DESKTOPS)
            return processSection(request, env);
        else
            return processItem(request, relation, env);
    }

    public static String processSection(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        SQLTool sqlTool = SQLTool.getInstance();

        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int count = Misc.getPageSize(20, 50, env, null);
        int total = sqlTool.countItemRelationsWithType(Item.DESKTOP, null);

        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(from, count)};
        List<Relation> list = sqlTool.findItemRelationsWithType(Item.DESKTOP, qualifiers);
        Tools.syncList(list);

        Paging paging = new Paging(list, from, count, total);
        env.put(VAR_ITEMS, paging);

        env.put(Constants.VAR_READ_COUNTERS, Tools.getRelationCountersValue(list, Constants.COUNTER_READ));

        env.put(Constants.VAR_RSS, FeedGenerator.getScreenshotsFeedUrl());
        return FMTemplateSelector.select("Desktop", "showList", env, request);
    }

    public static String processItem(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        if (ACTION_USERS.equals(params.get(PARAM_ACTION)))
            return processItemUsers(request, relation, env);
        else
            return processItemDisplay(request, relation, env);
    }

    public static String processItemDisplay(HttpServletRequest request, Relation relation, Map env) throws Exception {
        SQLTool sqlTool = SQLTool.getInstance();
        Item item = (Item) relation.getChild();
        env.put(VAR_ITEM, item);

        User user = (User) env.get(Constants.VAR_USER);
        if (user == null || user.getId() != item.getOwner())
            ReadRecorder.log(item, Constants.COUNTER_READ, env);

        Map children = Tools.groupByType(item.getChildren(), "Item");
        env.put(ShowObject.VAR_CHILDREN_MAP, children);

        Qualifier olderQualifier = new CompareCondition(Field.CREATED, Operation.SMALLER, item.getCreated());
        Qualifier myQualifier = new CompareCondition(Field.OWNER, Operation.EQUAL, item.getOwner());
        Qualifier[] qualifiers = new Qualifier[]{myQualifier, olderQualifier, Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, 3)};
        List<Relation> olderDesktops = sqlTool.findItemRelationsWithType(Item.DESKTOP, qualifiers);
        env.put(VAR_MY_OLDER_DESKTOPS, olderDesktops);

        env.put(Constants.VAR_RSS, FeedGenerator.getScreenshotsFeedUrl());
        return FMTemplateSelector.select("Desktop", "show", env, request);
    }

    /**
     * Generates list of software's users.
     */
    public static String processItemUsers(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Item item = (Item) relation.getChild();
        env.put(VAR_ITEM, item);

        List<User> users = Misc.loadUsersByProperty(item, Constants.PROPERTY_FAVOURITED_BY);
        env.put(Constants.VAR_USERS, users);

        env.put(Constants.VAR_RSS, FeedGenerator.getScreenshotsFeedUrl());
        return FMTemplateSelector.select("Desktop", "users", env, request);
    }
}