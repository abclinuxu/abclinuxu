/*
 * User: literakl
 * Date: 2.3.2004
 * Time: 7:16:04
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Date;
import java.text.ParseException;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

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

    public static final String VAR_RELATION = "RELATION";

    public static final String ACTION_ADD_ROYALTIES = "add";
    public static final String ACTION_ADD_ROYALTIES_STEP2 = "add2";
    public static final String ACTION_EDIT_ROYALTIES = "edit";
    public static final String ACTION_EDIT_ROYALTIES_STEP2 = "edit2";

    /**
     * Processes request.
     * @return name of template to be executed or null
     */
    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if ( relation==null )
            throw new MissingArgumentException("Chybí parametr relationId!");

        Persistance persistance = PersistanceFactory.getPersistance();
        persistance.synchronize(relation);
        persistance.synchronize(relation.getChild());
        env.put(VAR_RELATION, relation);

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        if ( !user.hasRole(Roles.ARTICLE_ADMIN) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if ( ACTION_ADD_ROYALTIES.equals(action) )
            return actionAddRoyaltiesStep1(request, env);

        if ( action.equals(ACTION_ADD_ROYALTIES_STEP2) )
            return actionAddRoyaltiesStep2(request, response, env);

        if ( ACTION_EDIT_ROYALTIES.equals(action) )
            return actionEditRoyaltiesStep1(request, env);

        if ( action.equals(ACTION_EDIT_ROYALTIES_STEP2) )
            return actionEditRoyaltiesStep2(request, response, env);

        throw new MissingArgumentException("Chybí parametr action!");
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

    protected String actionAddRoyaltiesStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation upper = (Relation) env.get(VAR_RELATION);

        Item item = new Item(0, Item.ROYALTIES);
        item.setData(DocumentHelper.createDocument());

        boolean canContinue = true;
        canContinue &= setAuthorForRoyalties(params, item, env, request);
        canContinue &= setPublishDateForRoyalties(params, item, env);
        canContinue &= setPaidDateForRoyalties(params, item, env);
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

    private String actionEditRoyaltiesStep1(HttpServletRequest request, Map env) throws Exception {
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

    protected String actionEditRoyaltiesStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();

        boolean canContinue = true;
        canContinue &= setAuthorForRoyalties(params, item, env, request);
        canContinue &= setPublishDateForRoyalties(params, item, env);
        canContinue &= setPaidDateForRoyalties(params, item, env);
        canContinue &= setAmount(params, item, env);
        canContinue &= setNote(params, item);

        if ( !canContinue )
            return actionEditRoyaltiesStep1(request, env);
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
    private boolean setAuthorForRoyalties(Map params, Item item, Map env, HttpServletRequest request) {
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
    private boolean setPublishDateForRoyalties(Map params, Item item, Map env) {
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
    private boolean setPaidDateForRoyalties(Map params, Item item, Map env) {
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
