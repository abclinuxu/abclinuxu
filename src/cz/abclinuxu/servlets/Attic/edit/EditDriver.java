/*
 * User: literakl
 * Date: Feb 6, 2002
 * Time: 6:32:36 PM
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.format.Format;
import cz.abclinuxu.utils.format.FormatDetector;
import cz.abclinuxu.utils.monitor.*;
import cz.abclinuxu.exceptions.MissingArgumentException;

import org.dom4j.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Date;

/**
 * todo archive last three versions of driver
 */
public class EditDriver extends AbcFMServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditDriver.class);

    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_VERSION = "version";
    public static final String PARAM_URL = "url";
    public static final String PARAM_NOTE = "note";
    public static final String PARAM_PREVIEW = "preview";

    public static final String VAR_RELATION = "RELATION";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";
    public static final String ACTION_ALTER_MONITOR = "monitor";


    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT,PARAM_RELATION,Relation.class,params);
        if ( relation!=null ) {
            persistance.synchronize(relation);
            env.put(VAR_RELATION,relation);
        }

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if ( ACTION_ALTER_MONITOR.equals(action) )
            return actionAlterMonitor(request, response, env);

        if ( ACTION_ADD.equals(action) )
            return FMTemplateSelector.select("EditDriver", "add", env, request);

        if ( ACTION_ADD_STEP2.equals(action) )
            return actionAddStep2(request, response, env);

        if ( ACTION_EDIT.equals(action) )
            return actionEdit(request, env);

        if ( ACTION_EDIT_STEP2.equals(action) )
            return actionEditStep2(request, response, env);

        throw new MissingArgumentException("Chybí parametr action!");
    }

    /**
     * Adds new driver to the database.
     * @return page to be rendered
     */
    protected String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Item driver = new Item(0, Item.DRIVER);
        Document document = DocumentHelper.createDocument();
        driver.setData(document);

        boolean canContinue = true;
        canContinue &= setName(params, document, env);
        canContinue &= setVersion(params, document, env);
        canContinue &= setURL(params, document, env);
        canContinue &= setNote(params, document, env);

        if ( !canContinue || params.get(PARAM_PREVIEW)!=null )
            return FMTemplateSelector.select("EditDriver", "add", env, request);

        User user = (User) env.get(Constants.VAR_USER);
        driver.setOwner(user.getId());
        driver.setCreated(new Date());

        persistance.create(driver);
        Relation relation = new Relation(new Category(Constants.CAT_DRIVERS), driver, Constants.REL_DRIVERS);
        persistance.create(relation);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/ViewRelation?rid="+relation.getId());
        return null;
    }

    /**
     * Fills environment with existing data of the driver.
     * @return template to be rendered.
     */
    protected String actionEdit(HttpServletRequest request, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item driver = (Item) persistance.findById(relation.getChild());

        Document document = driver.getData();
        Node node = document.selectSingleNode("data/name");
        if ( node!=null ) params.put(PARAM_NAME, node.getText());
        node = document.selectSingleNode("data/version");
        if ( node!=null ) params.put(PARAM_VERSION, node.getText());
        node = document.selectSingleNode("data/url");
        if ( node!=null ) params.put(PARAM_URL, node.getText());
        node = document.selectSingleNode("data/note");
        if ( node!=null ) params.put(PARAM_NOTE, node.getText());

        return FMTemplateSelector.select("EditDriver","edit",env,request);
    }

    /**
     * Validates input values and if it is OK, that it updates the driver and displays it.
     * @return page to be rendered
     */
    protected String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item driver = (Item) persistance.findById(relation.getChild());
        Document document = driver.getData();

        boolean canContinue = true;
        canContinue &= setName(params, document, env);
        canContinue &= setVersion(params, document, env);
        canContinue &= setURL(params, document, env);
        canContinue &= setNote(params, document, env);

        if ( !canContinue || params.get(PARAM_PREVIEW)!=null )
            return FMTemplateSelector.select("EditDriver", "edit", env, request);

        User user = (User) env.get(Constants.VAR_USER);
        driver.setOwner(user.getId());
        driver.setCreated(new Date());
        persistance.update(driver);

        // run monitor
        String url = "http://www.abclinuxu.cz/drivers/ViewRelation?rid="+relation.getId();
        MonitorAction action = new MonitorAction(user,UserAction.EDIT,ObjectType.DRIVER,driver,url);
        MonitorPool.scheduleMonitorAction(action);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/ViewRelation?rid="+relation.getId());
        return null;
    }

    /**
     * Reverts current monitor state for the user on this driver.
     */
    protected String actionAlterMonitor(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item driver = (Item) persistance.findById(relation.getChild());
        User user = (User) env.get(Constants.VAR_USER);

        MonitorTools.alterMonitor(driver.getData().getRootElement(),user);
        persistance.update(driver);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/ViewRelation?rid="+relation.getId());
        return null;
    }

    /* ******* setters ********* */

    /**
     * Updates name of driver from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param document Document of discussion to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setName(Map params, Document document, Map env) {
        String tmp = (String) params.get(PARAM_NAME);
        if ( tmp!=null && tmp.length()>0 ) {
            DocumentHelper.makeElement(document, "data/name").setText(tmp);
        } else {
            ServletUtils.addError(PARAM_NAME, "Zadejte název ovladaèe!", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates driver's version from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param document Document of discussion to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setVersion(Map params, Document document, Map env) {
        String tmp = (String) params.get(PARAM_VERSION);
        if ( tmp!=null && tmp.length()>0 ) {
            DocumentHelper.makeElement(document, "data/version").setText(tmp);
        } else {
            ServletUtils.addError(PARAM_VERSION, "Zadejte verzi ovladaèe!", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates driver's URL from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param document Document of discussion to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setURL(Map params, Document document, Map env) {
        String url = (String) params.get(PARAM_URL);
        if ( url==null || url.length()==0 ) {
            ServletUtils.addError(PARAM_URL, "Zadejte adresu ovladaèe!", env, null);
            return false;
        } else if ( url.indexOf("tp://")==-1 || url.length()<12 ) {
            ServletUtils.addError(PARAM_URL, "Neplatná adresa ovladaèe!", env, null);
            return false;
        }
        DocumentHelper.makeElement(document, "data/url").setText(url);
        return true;
    }

    /**
     * Updates driver's note from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param document Document of discussion to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setNote(Map params, Document document, Map env) {
        String tmp = (String) params.get(PARAM_NOTE);
        if ( tmp!=null && tmp.length()>0 ) {
            Element element = DocumentHelper.makeElement(document, "data/note");
            element.setText(tmp);
            Format format = FormatDetector.detect(tmp);
            element.addAttribute("format", Integer.toString(format.getId()));
        } else {
            ServletUtils.addError(PARAM_NOTE, "Zadejte poznámku!", env, null);
            return false;
        }
        return true;
    }
}
