/*
 * User: literakl
 * Date: 13.6.2004
 * Time: 16:08:35
 */
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Record;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.exceptions.InvalidDataException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;

import org.apache.regexp.RE;

/**
 * Displays dictionary
 */
public class ShowDictionary implements AbcAction {
    public static final String PARAM_RELATION_ID_SHORT = "rid";
    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_ITEM = "ITEM";
    public static final String VAR_CHILDREN_MAP = "CHILDREN";

    RE reName;

    public ShowDictionary() {
        reName = new RE("(^/slovnik/)([^?;]+)");
    }

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = findRelation(params, request);
        Tools.sync(relation);
        env.put(VAR_RELATION, relation);
        Item item = (Item) relation.getChild();
        return show(env, item, request);
    }

    /**
     * Shows the article.
     */
    static String show(Map env, Item item, HttpServletRequest request) throws Exception {
        Map children = Tools.groupByType(item.getChildren());
        env.put(VAR_CHILDREN_MAP, children);
        List list = (List) children.get(Constants.TYPE_RECORD);
        if ( list==null || list.size()==0 )
            throw new NotFoundException("Pojem "+item.getId()+" nemá obsah!");

        Record record = (Record) ((Relation) list.get(0)).getChild();
        if ( record.getType()!=Record.DICTIONARY )
            throw new InvalidDataException("Záznam "+record.getId()+" není typu slovnik!");

        env.put(VAR_ITEM, item);
        return FMTemplateSelector.select("Dictionary", "show", env, request);
    }

    /**
     * Parses request URI, which contains either relation id or URLName of dictionary item.
     * @return elation id of the item
     */
    Relation findRelation(Map params, HttpServletRequest request) throws NotFoundException {
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_ID_SHORT, Relation.class, params, request);
        if (relation!=null)
            return relation;

        String url = ServletUtils.combinePaths(request.getServletPath(), request.getPathInfo());
        if (reName.match(url)) {
            String urlName = reName.getParen(2);
            relation = SQLTool.getInstance().findDictionaryByURLName(urlName);
            if (relation==null)
                throw new MissingArgumentException("Pojem '"+urlName+"' nebyl nalezen ve slovníku!");
            return relation;
        }
        throw new MissingArgumentException("Parametr rid je prázdný!");
    }
}