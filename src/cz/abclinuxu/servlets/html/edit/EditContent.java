/*
 * User: literakl
 * Date: 21.9.2004
 * Time: 0:09:21
 */
package cz.abclinuxu.servlets.html.edit;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.utils.InstanceUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import org.dom4j.Document;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Used to add/edit static content
 */
public class EditContent implements AbcAction {
    static Logger log = Logger.getLogger(EditContent.class);

    public static final String PARAM_TITLE = "title";
    public static final String PARAM_CONTENT = "content";
    public static final String PARAM_URL = "url";
    public static final String PARAM_CLASS = "java_class";
    public static final String PARAM_EXECUTE_AS_TEMPLATE = "execute";
    public static final String PARAM_RELATION_SHORT = "rid";

    public static final String VAR_RELATION = "RELATION";

    public static final String ACTION_ADD_ITEM = "add";
    public static final String ACTION_ADD_ITEM_STEP2 = "add2";
    public static final String ACTION_EDIT_ITEM = "edit";
    public static final String ACTION_EDIT_ITEM_STEP2 = "edit2";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        if ( !user.hasRole(Roles.ARTICLE_ADMIN) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if ( ACTION_ADD_ITEM.equals(action) )
            return FMTemplateSelector.select("EditContent", "add", env, request);

        if ( action.equals(ACTION_ADD_ITEM_STEP2) )
            return actionAddStep2(request, response, env);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if ( relation!=null ) {
            Persistance persistance = PersistanceFactory.getPersistance();
            relation = (Relation) persistance.findById(relation);
            persistance.synchronize(relation.getChild());
            env.put(VAR_RELATION, relation);
        }

        if ( action.equals(ACTION_EDIT_ITEM) )
            return actionEditItem(request, env);

        if ( action.equals(ACTION_EDIT_ITEM_STEP2) )
            return actionEditItem2(request, response, env);

        throw new MissingArgumentException("Chybí parametr action!");
    }


    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);

        Item item = new Item(0, Item.CONTENT);
        item.setData(DocumentHelper.createDocument());
        item.setOwner(user.getId());
        Relation relation = new Relation();

        boolean canContinue = true;
        canContinue &= setTitle(params, item, env);
        canContinue &= setContent(params, item, env);
        canContinue &= setURL(params, relation, env);
        canContinue &= setClass(params, item);

        //over, ze takove URL jeste neexistuje

        if ( !canContinue )
            return FMTemplateSelector.select("EditContent", "add", env, request);

        persistance.create(item);
        relation.setParent(new Category(Constants.CAT_DOCUMENTS));
        relation.setChild(item);
        relation.setUpper(Constants.REL_DOCUMENTS);
        persistance.create(relation);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, relation.getUrl());
        return null;
    }

    protected String actionEditItem(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();

        Document document = item.getData();
        Element element = (Element) document.selectSingleNode("/data/name");
        params.put(PARAM_TITLE, element.getText());
        element = (Element) document.selectSingleNode("/data/content");
        params.put(PARAM_CONTENT, element.getText());
        params.put(PARAM_EXECUTE_AS_TEMPLATE, element.attributeValue("execute"));
        element = (Element) document.selectSingleNode("/data/java_class");
        if (element!=null)
            params.put(PARAM_CLASS, element.getText());
        params.put(PARAM_URL, relation.getUrl());

        return FMTemplateSelector.select("EditContent", "edit", env, request);
    }

    protected String actionEditItem2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();

        boolean canContinue = true;
        canContinue &= setTitle(params, item, env);
        canContinue &= setContent(params, item, env);
        canContinue &= setURL(params, relation, env);
        canContinue &= setClass(params, item);

        //over, ze takove URL jeste neexistuje

        if ( !canContinue )
            return FMTemplateSelector.select("EditContent", "edit", env, request);

        persistance.update(item);
        persistance.update(relation);
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, relation.getUrl());
        return null;
    }

    // setters


    /**
     * Updates title from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item article to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setTitle(Map params, Item item, Map env) {
        String name = (String) params.get(PARAM_TITLE);
        if ( name==null || name.length()==0 ) {
            ServletUtils.addError(PARAM_TITLE, "Vyplòte titulek stránky!", env, null);
            return false;
        }
        Element element = DocumentHelper.makeElement(item.getData(), "/data/name");
        element.setText(name);
        return true;
    }

    /**
     * Updates content from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item article  to be updated
     * @return false, if there is a major error.
     */
    private boolean setContent(Map params, Item item, Map env) {
        String content = (String) params.get(PARAM_CONTENT);
        String exec = (String) params.get(PARAM_EXECUTE_AS_TEMPLATE);
        Element element = (Element) item.getData().selectSingleNode("/data/content");
        if ( element!=null )
            element.detach();

        if ( content==null || content.length()==0 ) {
            ServletUtils.addError(PARAM_CONTENT, "Vyplòte obsah stránky!", env, null);
            return false;
        }

        element = DocumentHelper.makeElement(item.getData(), "/data/content");
        element.setText(content);
        if (!"yes".equals(exec)) exec = "no";
        element.addAttribute("execute", exec);
        return true;
    }

    /**
     * Updates url from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param relation relation to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setURL(Map params, Relation relation, Map env) {
        String url = (String) params.get(PARAM_URL);
        if ( url==null || url.length()==0 ) {
            ServletUtils.addError(PARAM_URL, "Vyplòte adresu stránky!", env, null);
            return false;
        }
        if (!url.startsWith("/")) {
            ServletUtils.addError(PARAM_URL, "Adresa stránky nesmí být absolutní!", env, null);
            return false;
        }
        relation.setUrl(url);

        Relation existingRelation = SQLTool.getInstance().findRelationByURL(relation.getUrl());
        if (existingRelation!=null && (relation==null || relation.getId()!=existingRelation.getId())) {
            ServletUtils.addError(PARAM_URL, "Tato adresa je ji¾ pou¾ita!", env, null);
            return false;
        }

        return true;
    }

    /**
     * Updates title from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item article to be updated
     * @return false, if there is a major error.
     */
    private boolean setClass(Map params, Item item) {
        Element element = (Element) item.getData().selectSingleNode("/data/java_class");
        if ( element!=null )
            element.detach();

        String clazz = (String) params.get(PARAM_CLASS);
        if ( clazz==null || clazz.length()==0 )
            return true;

        element = DocumentHelper.makeElement(item.getData(), "/data/java_class");
        element.setText(clazz);
        return true;
    }
}
