/*
 * User: literakl
 * Date: Jul 13, 2004
 * Time: 15:04:00 AM
 */
package cz.abclinuxu.servlets.html.edit;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.parser.safehtml.SafeHTMLGuard;
import cz.abclinuxu.utils.email.monitor.*;
import cz.abclinuxu.utils.format.Format;
import cz.abclinuxu.utils.format.FormatDetector;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.finesoft.socd.analyzer.DiacriticRemover;

import org.dom4j.*;
import org.htmlparser.util.ParserException;
import org.apache.regexp.RE;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Date;
import java.util.prefs.Preferences;

/**
 * This class is responsible for adding and
 * editing of dictionary items and records.
 */
public class EditDictionary implements AbcAction, Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditDictionary.class);

    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_DESCRIPTION = "desc";
    public static final String PARAM_RECORD_ID = "recordId";
    public static final String PARAM_PREVIEW = "preview";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_RECORD = "RECORD";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_ADD_RECORD = "addRecord";
    public static final String ACTION_ADD_RECORD_STEP2 = "addRecord2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";
    public static final String ACTION_ALTER_MONITOR = "monitor";

    public static final String PREF_INVALID_CHARACTERS = "regexp.invalid.characters";
    private static RE reInvalidCharacters;
    static {
        ConfigurationManager.getConfigurator().configureAndRememberMe(new EditDictionary());
    }


    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        if ( action==null)
            throw new MissingArgumentException("Chyb� parametr action!");

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if ( relation!=null ) {
            relation = (Relation) persistance.findById(relation);
            persistance.synchronize(relation.getChild());
            env.put(VAR_RELATION,relation);
        }

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if ( action.equals(ACTION_ADD) )
            return FMTemplateSelector.select("Dictionary", "add_item", env, request);

        if ( action.equals(ACTION_ADD_STEP2) )
            return actionAddStep2(request, response, env);

        if ( action.equals(ACTION_ADD_RECORD) ) {
            params.put(PARAM_ACTION, ACTION_ADD_RECORD_STEP2);
            return FMTemplateSelector.select("Dictionary", "add_record", env, request);
        }

        if ( action.equals(ACTION_ADD_RECORD_STEP2) )
            return actionAddRecord(request, response, env);

        if ( action.equals(ACTION_EDIT) || action.equals(ACTION_EDIT_STEP2) ) {
            Record record = (Record) InstanceUtils.instantiateParam(PARAM_RECORD_ID, Record.class, params, request);
            persistance.synchronize(record);
            env.put(VAR_RECORD, record);

            if ( user.getId()!=record.getOwner() && !user.hasRole(Roles.ROOT) )
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

            if ( action.equals(ACTION_EDIT) )
                return actionEdit(request, env);
            else
                return actionEdit2(request, response, env);
        }

        if ( ACTION_ALTER_MONITOR.equals(action) )
            return actionAlterMonitor(request, response, env);

        throw new MissingArgumentException("Chyb� parametr action!");
    }

    protected String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);

        Document documentItem = DocumentHelper.createDocument();
        Element rootItem = documentItem.addElement("data");
        Item item = new Item(0, Item.DICTIONARY);
        item.setData(documentItem);
        item.setOwner(user.getId());

        Document documentRecord = DocumentHelper.createDocument();
        Element rootRecord = documentRecord.addElement("data");
        Record record = new Record(0, Record.DICTIONARY);
        record.setData(documentRecord);
        record.setOwner(user.getId());

        boolean canContinue = true;
        canContinue &= setName(params, rootItem, env);
        canContinue &= setURLName(item, rootItem);
        canContinue &= setDescription(params, rootRecord, env);
        if (!canContinue || params.get(PARAM_PREVIEW)!=null)
            return FMTemplateSelector.select("Dictionary", "add_item", env, request);

        persistance.create(item);
        Relation relation = new Relation(new Category(Constants.CAT_DICTIONARY), item, Constants.REL_DICTIONARY);
        persistance.create(relation);
        relation.getParent().addChildRelation(relation);

        persistance.create(record);
        Relation recordRelation = new Relation(item, record, relation.getId());
        persistance.create(recordRelation);
        recordRelation.getParent().addChildRelation(recordRelation);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/slovnik/"+item.getSubType());
        return null;
    }

    protected String actionAddRecord(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);
        Relation upper = (Relation) env.get(VAR_RELATION), relation = null;
        if (upper==null)
            throw new MissingArgumentException("Chyb� parametr rid!");

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        Record record = new Record(0, Record.DICTIONARY);
        record.setData(document);
        record.setOwner(user.getId());

        boolean canContinue = true;
        canContinue &= setDescription(params, root, env);
        if ( !canContinue  || params.get(PARAM_PREVIEW)!=null )
            return FMTemplateSelector.select("Dictionary", "add_record", env, request);

        persistance.create(record);
        relation = new Relation(upper.getChild(), record, upper.getId());
        persistance.create(relation);
        relation.getParent().addChildRelation(relation);

        // run monitor
        Item item = (Item) persistance.findById(upper.getChild());
        String url = "http://www.abclinuxu.cz"+urlUtils.getPrefix()+"/slovnik/"+item.getSubType();
        MonitorAction action = new MonitorAction(user, UserAction.ADD, ObjectType.DICTIONARY, item, url);
        MonitorPool.scheduleMonitorAction(action);

        urlUtils.redirect(response, "/slovnik/"+item.getSubType());
        return null;
    }

    protected String actionEdit(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);

        Item item = (Item) relation.getChild();
        Document documentItem = item.getData();
        Node node = documentItem.selectSingleNode("data/name");
        params.put(PARAM_NAME, node.getText());

        Record record = (Record) env.get(VAR_RECORD);
        Document documentRecord = record.getData();
        Node desc = documentRecord.selectSingleNode("data/description");
        params.put(PARAM_DESCRIPTION, desc.getText());

        return FMTemplateSelector.select("Dictionary","edit",env,request);
    }

    protected String actionEdit2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        Element rootItem = item.getData().getRootElement();
        Record record = (Record) env.get(VAR_RECORD);
        Element rootRecord = record.getData().getRootElement();

        boolean canContinue = true;
        canContinue &= setName(params, rootItem, env);
        canContinue &= setURLName(item, rootItem);
        canContinue &= setDescription(params, rootRecord, env);
        if ( !canContinue )
            return FMTemplateSelector.select("Dictionary", "edit", env, request);

        Date updated = item.getUpdated();
        persistance.update(item);
        if (user.getId()!=item.getOwner() && user.getId()!=record.getOwner() )
            SQLTool.getInstance().setUpdatedTimestamp(item, updated);

        updated = record.getUpdated();
        persistance.update(record);
        if (user.getId()!=record.getOwner())
            SQLTool.getInstance().setUpdatedTimestamp(record, updated);

        // run monitor
        String url = "http://www.abclinuxu.cz"+urlUtils.getPrefix()+"/slovnik/"+item.getSubType();
        MonitorAction action = new MonitorAction(user, UserAction.EDIT, ObjectType.DICTIONARY, item, url);
        MonitorPool.scheduleMonitorAction(action);

        urlUtils.redirect(response, "/slovnik/"+item.getSubType());
        return null;
    }

    /**
     * Reverts current monitor state for the user on this driver.
     */
    protected String actionAlterMonitor(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) persistance.findById(relation.getChild());
        User user = (User) env.get(Constants.VAR_USER);

        Date originalUpdated = item.getUpdated();
        MonitorTools.alterMonitor(item.getData().getRootElement(), user);
        persistance.update(item);
        SQLTool.getInstance().setUpdatedTimestamp(item, originalUpdated);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/slovnik/"+item.getSubType());
        return null;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        String tmp = prefs.get(PREF_INVALID_CHARACTERS, null);
        reInvalidCharacters = new RE(tmp, RE.REPLACE_ALL);
    }

    /* ******** setters ********* */

    /**
     * Updates name from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element of item to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setName(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_NAME);
        if ( tmp!=null && tmp.length()>0 ) {
            DocumentHelper.makeElement(root, "name").setText(tmp);
            return true;
        } else {
            ServletUtils.addError(PARAM_NAME, "Zadejte jm�no pojmu!", env, null);
            return false;
        }
    }

    /**
     * Updates name, which is used to identify this object in URL requests.
     * Changes are not synchronized with persistance.
     * @param root root element of item to be updated
     * @return false, if there is a major error.
     * todo verify uniqueness in database
     */
    private boolean setURLName(Item item, Element root) {
        String name = root.elementText("name");
        if (name==null) return false;
        String tmp = DiacriticRemover.getInstance().removeDiacritics(name);
        tmp = reInvalidCharacters.subst(tmp, "_");
        item.setSubType(tmp.toLowerCase());
        return true;
    }

    /**
     * Updates explaination from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element of record to be updated
     * @return false, if there is a major error.
     */
    private boolean setDescription(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_DESCRIPTION);
        if ( tmp==null || tmp.length()==0 ) {
            ServletUtils.addError(PARAM_DESCRIPTION, "Zadejte popis pojmu!", env, null);
            return false;
        }

        try {
            SafeHTMLGuard.check(tmp);
        } catch (ParserException e) {
            log.error("ParseException on '"+tmp+"'", e);
            ServletUtils.addError(PARAM_DESCRIPTION, e.getMessage(), env, null);
            return false;
        } catch (Exception e) {
            ServletUtils.addError(PARAM_DESCRIPTION, e.getMessage(), env, null);
            return false;
        }

        Element element = DocumentHelper.makeElement(root, "description");
        element.setText(tmp);
        Format format = FormatDetector.detect(tmp);
        element.addAttribute("format", Integer.toString(format.getId()));

        return true;
    }
}
