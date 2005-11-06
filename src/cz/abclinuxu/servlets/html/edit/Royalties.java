/*
 *  Copyright (C) 2005 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
package cz.abclinuxu.servlets.html.edit;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.extra.CompareCondition;
import cz.abclinuxu.persistance.extra.Field;
import cz.abclinuxu.persistance.extra.Operation;
import cz.abclinuxu.persistance.extra.Qualifier;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.text.ParseException;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Document;

/**
 * Adds, edits royalties and performs queries over them.
 */
public class Royalties implements AbcAction {
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_PAID = "paid";
    public static final String PARAM_AMOUNT = "amount";
    public static final String PARAM_NOTE = "note";
    public static final String PARAM_PUBLISHED = "published";
    public static final String PARAM_AUTHOR = "authorId";
    public static final String PARAM_SINCE = "since";
    public static final String PARAM_UNTIL = "until";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_RESULT = "RESULT";

    public static final String ACTION_ADD_ROYALTIES = "add";
    public static final String ACTION_ADD_ROYALTIES_STEP2 = "add2";
    public static final String ACTION_EDIT_ROYALTIES = "edit";
    public static final String ACTION_EDIT_ROYALTIES_STEP2 = "edit2";
    public static final String ACTION_REPORT = "report";

    /**
     * Processes request.
     * @return name of template to be executed or null
     */
    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        if ( !user.hasRole(Roles.ARTICLE_ADMIN) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if ( action==null || action.length()==0 )
            return actionReportStep1(request, env);

        if ( ACTION_REPORT.equals(action) )
            return actionReportStep2(request, env);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if ( relation==null )
            throw new MissingArgumentException("Chybí parametr relationId!");

        Persistance persistance = PersistanceFactory.getPersistance();
        persistance.synchronize(relation);
        persistance.synchronize(relation.getChild());
        env.put(VAR_RELATION, relation);

        if ( ACTION_ADD_ROYALTIES.equals(action) )
            return actionAddRoyaltiesStep1(request, env);

        if ( ACTION_ADD_ROYALTIES_STEP2.equals(action) )
            return actionAddStep2(request, response, env);

        if ( ACTION_EDIT_ROYALTIES.equals(action) )
            return actionEditStep1(request, env);

        if ( ACTION_EDIT_ROYALTIES_STEP2.equals(action) )
            return actionEditStep2(request, response, env);

        throw new MissingArgumentException("Chybí parametr action!");
    }

    private String actionReportStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        if ( params.get(PARAM_SINCE)==null ) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MONTH, -1);
            params.put(PARAM_SINCE, Constants.isoFormatShort.format(calendar.getTime()));
        }
        if ( params.get(PARAM_UNTIL)==null ) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            params.put(PARAM_UNTIL, Constants.isoFormatShort.format(calendar.getTime()));
        }
        return FMTemplateSelector.select("Royalties", "form", env, request);
    }

    private String actionReportStep2(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String tmp = (String) params.get(PARAM_SINCE);
        if (tmp==null)
            return actionReportStep1(request, env);
        Date since = Constants.isoFormatShort.parse(tmp);
        tmp = (String) params.get(PARAM_UNTIL);
        if (tmp==null)
            return actionReportStep1(request, env);
        Date until = Constants.isoFormatShort.parse(tmp);

        CompareCondition conditionFrom = new CompareCondition(Field.CREATED, Operation.GREATER_OR_EQUAL, since);
        CompareCondition conditionTo = new CompareCondition(Field.CREATED, Operation.SMALLER_OR_EQUAL, until);
        Qualifier[] qualifiers = new Qualifier[]{conditionFrom, conditionTo, Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING};
        List list = SQLTool.getInstance().findItemRelationsWithType(Item.ROYALTIES, qualifiers);

        env.put(VAR_RESULT, Tools.syncList(list));
        return FMTemplateSelector.select("Royalties", "report", env, request);
    }

    private String actionAddRoyaltiesStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation upper = (Relation) env.get(VAR_RELATION);
        Item article = (Item) upper.getChild();
        if ( params.get(PARAM_AUTHOR)==null )
            params.put(PARAM_AUTHOR, article.getData().getRootElement().elementText("author"));
        if ( params.get(PARAM_PUBLISHED)==null )
            params.put(PARAM_PUBLISHED, Constants.isoFormatShort.format(article.getCreated()));
        if ( params.get(PARAM_AMOUNT)==null )
            params.put(PARAM_AMOUNT, "0");
        return FMTemplateSelector.select("Royalties", "add", env, request);
    }

    protected String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation upper = (Relation) env.get(VAR_RELATION);

        Item item = new Item(0, Item.ROYALTIES);
        Document document = DocumentHelper.createDocument();
        document.addElement("data");
        item.setData(document);

        boolean canContinue = true;
        canContinue &= setAuthor(params, item, env, request);
        canContinue &= setPublishDate(params, item, env);
        canContinue &= setPaidDate(params, item, env);
        canContinue &= setAmount(params, item, env);
        canContinue &= setNote(params, item);

        if ( !canContinue )
            return actionAddRoyaltiesStep1(request, env);

        persistance.create(item);
        Relation relation = new Relation(upper.getChild(), item, upper.getId());
        persistance.create(relation);
        relation.getParent().addChildRelation(relation);

        User user = (User) env.get(Constants.VAR_USER);
        AdminLogger.logEvent(user, "pøidal honoráø "+relation.getId());

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/show/"+upper.getId());
        return null;
    }

    private String actionEditStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        Element root = item.getData().getRootElement();
        if ( params.get(PARAM_AUTHOR)==null )
            params.put(PARAM_AUTHOR, Integer.toString(item.getOwner()));
        if ( params.get(PARAM_PUBLISHED)==null )
            params.put(PARAM_PUBLISHED, Constants.isoFormatShort.format(item.getCreated()));
        if ( params.get(PARAM_AMOUNT)==null )
            params.put(PARAM_AMOUNT, root.elementText("amount"));
        if ( params.get(PARAM_PAID)==null )
            params.put(PARAM_PAID, root.elementText("paid"));
        if ( params.get(PARAM_NOTE)==null )
            params.put(PARAM_NOTE, root.elementText("note"));
        return FMTemplateSelector.select("Royalties", "add", env, request);
    }

    protected String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();

        boolean canContinue = true;
        canContinue &= setAuthor(params, item, env, request);
        canContinue &= setPublishDate(params, item, env);
        canContinue &= setPaidDate(params, item, env);
        canContinue &= setAmount(params, item, env);
        canContinue &= setNote(params, item);

        if ( !canContinue )
            return actionEditStep1(request, env);
        persistance.update(item);

        User user = (User) env.get(Constants.VAR_USER);
        AdminLogger.logEvent(user, "upravil honoráø "+relation.getId());

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/show/"+relation.getUpper());
        return null;
    }

    // setters


    /**
     * Updates author from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item article to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setAuthor(Map params, Item item, Map env, HttpServletRequest request) {
        User author = (User) InstanceUtils.instantiateParam(PARAM_AUTHOR, User.class, params, request);
        if ( author==null ) {
            ServletUtils.addError(PARAM_AUTHOR, "Vyberte autora!", env, null);
            return false;
        }
        item.setOwner(author.getId());
        return true;
    }

    /**
     * Updates date of publishing from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item article to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setPublishDate(Map params, Item item, Map env) {
        try {
            Date publish = Constants.isoFormatShort.parse((String) params.get(PARAM_PUBLISHED));
            item.setCreated(publish);
        } catch (ParseException e) {
            ServletUtils.addError(PARAM_PUBLISHED, "Správný formát je 2002-02-10", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates date of paying royalties from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item article to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setPaidDate(Map params, Item item, Map env) {
        try {
            Element element = (Element) item.getData().selectSingleNode("/data/paid");
            if ( element!=null )
                element.detach();

            String tmp = (String) params.get(PARAM_PAID);
            if ( tmp==null || tmp.length()==0 )
                return true;

            Date date = Constants.isoFormatShort.parse(tmp);
            element = DocumentHelper.makeElement(item.getData(), "/data/paid");
            element.setText(Constants.isoFormatShort.format(date));
        } catch (ParseException e) {
            ServletUtils.addError(PARAM_PAID, "Správný formát je 2004-02-07", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates royalties amount from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item article to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setAmount(Map params, Item item, Map env) {
        int amount = Misc.parseInt((String) params.get(PARAM_AMOUNT), -1);
        if ( amount<0 ) {
            ServletUtils.addError(PARAM_AMOUNT, "Honoráø musí být celé nezáporné èíslo!", env, null);
            return false;
        }
        DocumentHelper.makeElement(item.getData(), "/data/amount").setText(Integer.toString(amount));
        return true;
    }

    /**
     * Updates royalties amount from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item article to be updated
     * @return false, if there is a major error.
     */
    private boolean setNote(Map params, Item item) {
        String tmp = (String) params.get(PARAM_NOTE);
        Element element = (Element) item.getData().selectSingleNode("/data/note");
        if ( element!=null )
            element.detach();
        element = DocumentHelper.makeElement(item.getData(), "/data/note");
        element.setText(tmp);
        return true;
    }
}
