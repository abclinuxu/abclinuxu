/*
 * User: literakl
 * Date: 13.6.2004
 * Time: 16:08:35
 */
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.edit.EditDictionary;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.extra.Qualifier;
import cz.abclinuxu.persistance.extra.LimitQualifier;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Record;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.exceptions.InvalidDataException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.prefs.Preferences;

import org.apache.regexp.RE;

/**
 * Displays dictionary
 */
public class ShowDictionary implements AbcAction, Configurable {
    public static final String PARAM_RELATION_ID_SHORT = "rid";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_ITEM = "ITEM";
    public static final String VAR_CHILDREN_MAP = "CHILDREN";
    public static final String VAR_FOUND = "FOUND";
    public static final String VAR_NEXT = "NEXT";
    public static final String VAR_PREVIOUS = "PREV";

    public static final String PREF_REGEXP_NAME = "regexp.name";
    public static final String PREF_MAX_ITEMS = "max.items";

    static RE reName;
    static int maxItems = 20;
    static {
        ConfigurationManager.getConfigurator().configureAndRememberMe(new ShowDictionary());
    }

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_ID_SHORT, Relation.class, params, request);
        if ( relation==null ) {
            String url = ServletUtils.combinePaths(request.getServletPath(), request.getPathInfo());
            if ( reName.match(url) ) {
                String urlName = reName.getParen(2);
                relation = SQLTool.getInstance().findDictionaryByURLName(urlName);
                if ( relation==null ) {
                    params.put(EditDictionary.PARAM_NAME, urlName);
                    ServletUtils.addMessage("Tento pojem nebyl je¹tì popsán. V tomto formuláøi jej mù¾ete vysvìtlit jako první.", env, null);
                    return FMTemplateSelector.select("Dictionary", "add_item", env, request);
                }
            }
        }

        if (relation!=null) {
            Tools.sync(relation);
            env.put(VAR_RELATION, relation);
            Item item = (Item) relation.getChild();
            return showOne(env, item, request);
        } else
            return showMany(env, request);
    }

    /**
     * Shows single dictionary item identified by short name or relation id.
     */
    static String showOne(Map env, Item item, HttpServletRequest request) throws Exception {
        Map children = Tools.groupByType(item.getChildren());
        env.put(VAR_CHILDREN_MAP, children);
        List list = (List) children.get(Constants.TYPE_RECORD);
        if ( list==null || list.size()==0 )
            throw new NotFoundException("Pojem "+item.getId()+" nemá obsah!");

        Record record = (Record) ((Relation) list.get(0)).getChild();
        if ( record.getType()!=Record.DICTIONARY )
            throw new InvalidDataException("Záznam "+record.getId()+" není typu slovnik!");

        env.put(VAR_ITEM, item);
        SQLTool sqlTool = SQLTool.getInstance();
        List siblings = sqlTool.getNeighbourDictionaryItems(item.getSubType(), true, 3);
        env.put(VAR_PREVIOUS, siblings);
        siblings = sqlTool.getNeighbourDictionaryItems(item.getSubType(), false, 3);
        env.put(VAR_NEXT, siblings);
        return FMTemplateSelector.select("Dictionary", "show", env, request);
    }

    /**
     * Shows page with list of latest dictionary items when no argument is given.
     */
    static String showMany(Map env, HttpServletRequest request) throws Exception {
        SQLTool sqlTool = SQLTool.getInstance();
        Qualifier[] qualifiers = new Qualifier[]{};
        List data = sqlTool.findItemRelationsWithType(Item.DICTIONARY, qualifiers);
        for ( Iterator iter = data.iterator(); iter.hasNext(); ) {
            Relation relation = (Relation) iter.next();
            Tools.sync(relation);
        }
        Sorters2.byName(data);

        Paging found = new Paging(data, 0, data.size(), data.size(), qualifiers);
        env.put(VAR_FOUND, found);
        return FMTemplateSelector.select("Dictionary", "showList", env, request);
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        String regexp = prefs.get(PREF_REGEXP_NAME, null);
        reName = new RE(regexp);
        maxItems = prefs.getInt(PREF_MAX_ITEMS, 30);
    }
}