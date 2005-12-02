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

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.parser.safehtml.SafeHTMLGuard;
import cz.abclinuxu.utils.email.monitor.*;
import cz.abclinuxu.utils.format.Format;
import cz.abclinuxu.utils.format.FormatDetector;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.scheduler.VariableFetcher;

import org.dom4j.*;
import org.htmlparser.util.ParserException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import java.util.Map;
import java.util.Date;

/**
 * This class is responsible for adding and
 * editing of hardware items and records.
 */
public class EditHardware implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditHardware.class);

    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_ICON = "icon";
    public static final String PARAM_DRIVER = "driver";
    public static final String PARAM_PRICE = "price";
    public static final String PARAM_SETUP = "setup";
    public static final String PARAM_TECHPARAM = "params";
    public static final String PARAM_IDENTIFICATION = "identification";
    public static final String PARAM_NOTE = "note";
    public static final String PARAM_RECORD_ID = "recordId";
    public static final String PARAM_CHOOSE_ICON = "iconChooser";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_RECORD = "RECORD";

    public static final String VAL_HW_DRIVER_KERNEL = "kernel";
    public static final String VAL_HW_DRIVER_XFREE = "xfree";
    public static final String VAL_HW_DRIVER_MAKER = "maker";
    public static final String VAL_HW_DRIVER_OTHER = "other";
    public static final String VAL_HW_DRIVER_NONE = "none";

    public static final String VAL_HW_PRICE_VERYLOW = "verylow";
    public static final String VAL_HW_PRICE_LOW = "low";
    public static final String VAL_HW_PRICE_GOOD = "good";
    public static final String VAL_HW_PRICE_HIGH = "high";
    public static final String VAL_HW_PRICE_TOOHIGH = "toohigh";

    public static final String ACTION_ADD_ITEM = "addItem";
    public static final String ACTION_ADD_ITEM_STEP2 = "addItem2";
    public static final String ACTION_ADD_ITEM_STEP3 = "addItem3";
    public static final String ACTION_ADD_RECORD = "addRecord";
    public static final String ACTION_ADD_RECORD_STEP2 = "addRecord2";
    public static final String ACTION_EDIT_ITEM = "editItem";
    public static final String ACTION_EDIT_ITEM_STEP2 = "editItem2";
    public static final String ACTION_EDIT_RECORD = "editRecord";
    public static final String ACTION_EDIT_RECORD_STEP2 = "editRecord2";
    public static final String ACTION_ALTER_MONITOR = "monitor";


    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        if ( action==null)
            throw new MissingArgumentException("Chybí parametr action!");

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if ( relation!=null ) {
            relation = (Relation) persistance.findById(relation);
            persistance.synchronize(relation.getChild());
            env.put(VAR_RELATION,relation);
        } else
            throw new MissingArgumentException("Chybí parametr relationId!");

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if ( action.equals(ACTION_ADD_ITEM) )
            return FMTemplateSelector.select("EditHardware", "add_item", env, request);

        if ( action.equals(ACTION_ADD_ITEM_STEP2) )
            return actionAddStep2(request, env);

        if ( action.equals(ACTION_ADD_ITEM_STEP3) )
            return actionAddStep3(request, response, env, true);

        if ( action.equals(ACTION_ADD_RECORD) ) {
            params.put(PARAM_ACTION, ACTION_ADD_RECORD_STEP2);
            return FMTemplateSelector.select("EditHardware", "add_record", env, request);
        }

        if ( action.equals(ACTION_ADD_RECORD_STEP2) )
            return actionAddRecord(request, response, env);

        if ( action.equals(ACTION_EDIT_RECORD) || action.equals(ACTION_EDIT_RECORD_STEP2) ) {
            Record record = (Record) InstanceUtils.instantiateParam(PARAM_RECORD_ID, Record.class, params, request);
            persistance.synchronize(record);
            env.put(VAR_RECORD, record);

            if ( user.getId()!=record.getOwner() && !user.hasRole(Roles.ROOT) )
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

            if ( action.equals(ACTION_EDIT_RECORD) )
                return actionEditRecord(request, env);
            else
                return actionEditRecord2(request, response, env);
        }

        if ( action.equals(ACTION_EDIT_ITEM) || action.equals(ACTION_EDIT_ITEM_STEP2) ) {
            Item item = (Item) relation.getChild();
            if ( user.getId()!=item.getOwner() && !user.hasRole(Roles.ROOT) )
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

            if ( action.equals(ACTION_EDIT_ITEM) )
                return actionEditItem(request, env);
            else
                return actionEditItem2(request, response, env);
        }

        if ( ACTION_ALTER_MONITOR.equals(action) )
            return actionAlterMonitor(request, response, env);

        throw new MissingArgumentException("Chybí parametr action!");
    }

    protected String actionAddStep2(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        String name = (String) params.get(PARAM_NAME);
        if ( name==null || name.length()==0 ) {
            ServletUtils.addError(PARAM_NAME,"Zadejte název druhu!",env,null);
            return FMTemplateSelector.select("EditHardware","add_item",env,request);
        }
        return FMTemplateSelector.select("EditHardware","add_record",env,request);
    }

    public String actionAddStep3(HttpServletRequest request, HttpServletResponse response, Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation upper = (Relation) env.get(VAR_RELATION);
        User user = (User) env.get(Constants.VAR_USER);

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        Item item = new Item(0, Item.HARDWARE);
        item.setData(document);
        item.setOwner(user.getId());

        boolean canContinue = true;
        canContinue &= setName(params, root, env);
        canContinue &= setIcon(params, root);
        if (!canContinue)
            return FMTemplateSelector.select("EditHardware", "add_item", env, request);

        String setup = (String) params.get(PARAM_SETUP);
        String note = (String) params.get(PARAM_NOTE);
        if ( Misc.empty(note) && Misc.empty(setup) ) {
            ServletUtils.addError(PARAM_SETUP,"Vyplòte postup zprovoznìní nebo poznámku!",env,null);
            return FMTemplateSelector.select("EditHardware","add_record",env,request);
        }

        document = DocumentHelper.createDocument();
        root = document.addElement("data");
        Record record = new Record(0,Record.HARDWARE);
        record.setData(document);
        record.setOwner(user.getId());

        canContinue = true;
        canContinue &= setDriver(params, root, env);
        canContinue &= setPrice(params, root, env);
        canContinue &= setParameters(params, root, env);
        canContinue &= setIdentification(params, root, env);
        canContinue &= setSetup(params, root, env);
        canContinue &= setNote(params, root, env);
        if ( !canContinue )
            return FMTemplateSelector.select("EditHardware", "add_record", env, request);

        persistance.create(item);
        Relation relation = new Relation(upper.getChild(), item, upper.getId());
        persistance.create(relation);
        relation.getParent().addChildRelation(relation);

        persistance.create(record);
        Relation recordRelation = new Relation(item, record, relation.getId());
        persistance.create(recordRelation);
        recordRelation.getParent().addChildRelation(recordRelation);

        FeedGenerator.updateHardware();
        VariableFetcher.getInstance().refreshHardware();

        if (redirect) {
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/show/"+relation.getId());
        } else
            env.put(VAR_RELATION, relation);
        return null;
    }

    protected String actionAddRecord(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);

        String setup = (String) params.get(PARAM_SETUP);
        String note = (String) params.get(PARAM_NOTE);
        if ( Misc.empty(note) && Misc.empty(setup) ) {
            ServletUtils.addError(PARAM_SETUP,"Vyplòte postup zprovoznìní nebo poznámku!",env,null);
            return FMTemplateSelector.select("EditHardware","add_record",env,request);
        }

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        Record record = new Record(0, Record.HARDWARE);
        record.setData(document);
        record.setOwner(user.getId());

        boolean canContinue = true;
        canContinue &= setDriver(params, root, env);
        canContinue &= setPrice(params, root, env);
        canContinue &= setParameters(params, root, env);
        canContinue &= setIdentification(params, root, env);
        canContinue &= setSetup(params, root, env);
        canContinue &= setNote(params, root, env);
        if ( !canContinue )
            return FMTemplateSelector.select("EditHardware", "add_record", env, request);


        persistance.create(record);
        Relation upper = (Relation) env.get(VAR_RELATION), relation = null;
        relation = new Relation(upper.getChild(), record, upper.getId());
        persistance.create(relation);
        relation.getParent().addChildRelation(relation);

        // run monitor
        String url = "http://www.abclinuxu.cz"+urlUtils.getPrefix()+"/show/"+upper.getId();
        Item item = (Item) persistance.findById(upper.getChild());
        MonitorAction action = new MonitorAction(user, UserAction.ADD, ObjectType.ITEM, item, url);
        MonitorPool.scheduleMonitorAction(action);

        VariableFetcher.getInstance().refreshHardware();

        urlUtils.redirect(response, "/show/"+relation.getId());
        return null;
    }

    protected String actionEditItem(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        Document document = item.getData();

        Node node = document.selectSingleNode("data/name");
        params.put(PARAM_NAME,node.getText());
        node = document.selectSingleNode("data/icon");
        if ( node!=null )
            params.put(PARAM_ICON,node.getText());

        return FMTemplateSelector.select("EditHardware","edit_item",env,request);
    }

    protected String actionEditItem2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        String tmp = (String) params.get(PARAM_CHOOSE_ICON);
        if ( !Misc.empty(tmp) ) {
            // it is not possible to use UrlUtils.dispatch(), because it would prepend prefix!
            RequestDispatcher dispatcher = request.getRequestDispatcher("/SelectIcon");
            dispatcher.forward(request,response);
            return null;
        }

        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        Element root = item.getData().getRootElement();

        boolean canContinue = true;
        canContinue &= setName(params, root, env);
        canContinue &= setIcon(params, root);
        if ( !canContinue )
            return FMTemplateSelector.select("EditHardware", "add_item", env, request);

        persistance.update(item);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/show/"+relation.getUpper());
        return null;
    }

    protected String actionEditRecord(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Record record = (Record) env.get(VAR_RECORD);
        Document document = record.getData();

        Node node = document.selectSingleNode("data/driver");
        if ( node!=null )
            params.put(PARAM_DRIVER,node.getText());
        node = document.selectSingleNode("data/price");
        if ( node!=null )
            params.put(PARAM_PRICE,node.getText());
        node = document.selectSingleNode("data/setup");
        if ( node!=null )
            params.put(PARAM_SETUP,node.getText());
        node = document.selectSingleNode("data/params");
        if ( node!=null )
            params.put(PARAM_TECHPARAM,node.getText());
        node = document.selectSingleNode("data/identification");
        if ( node!=null )
            params.put(PARAM_IDENTIFICATION,node.getText());
        node = document.selectSingleNode("data/note");
        if ( node!=null )
            params.put(PARAM_NOTE,node.getText());

        params.put(PARAM_ACTION,ACTION_EDIT_RECORD_STEP2);
        return FMTemplateSelector.select("EditHardware","edit_record",env,request);
    }

    protected String actionEditRecord2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);

        String setup = (String) params.get(PARAM_SETUP);
        String note = (String) params.get(PARAM_NOTE);
        if ( Misc.empty(note) && Misc.empty(setup) ) {
            ServletUtils.addError(PARAM_SETUP,"Vyplòte postup zprovoznìní nebo poznámku!",env,null);
            return FMTemplateSelector.select("EditHardware","edit_record",env,request);
        }

        Relation relation = (Relation) env.get(VAR_RELATION);
        Record record = (Record) env.get(VAR_RECORD);
        Element root = record.getData().getRootElement();

        boolean canContinue = true;
        canContinue &= setDriver(params, root, env);
        canContinue &= setPrice(params, root, env);
        canContinue &= setParameters(params, root, env);
        canContinue &= setIdentification(params, root, env);
        canContinue &= setSetup(params, root, env);
        canContinue &= setNote(params, root, env);
        if ( !canContinue )
            return FMTemplateSelector.select("EditHardware", "edit_record", env, request);

        Date updated = record.getUpdated();
        persistance.update(record);
        if (user.getId()!=record.getOwner())
            SQLTool.getInstance().setUpdatedTimestamp(record, updated);

        // run monitor
        String url = "http://www.abclinuxu.cz"+urlUtils.getPrefix()+"/show/"+relation.getId();
        GenericObject obj = (relation.getParent() instanceof Item)? relation.getParent() : relation.getChild();
        Item item = (Item) persistance.findById(obj);
        MonitorAction action = new MonitorAction(user, UserAction.EDIT, ObjectType.ITEM, item, url);
        MonitorPool.scheduleMonitorAction(action);

        VariableFetcher.getInstance().refreshHardware();

        urlUtils.redirect(response, "/show/"+relation.getId());
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
        urlUtils.redirect(response, "/show/"+relation.getId());
        return null;
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
            ServletUtils.addError(PARAM_NAME, "Zadejte název druhu!", env, null);
            return false;
        }
    }

    /**
     * Updates icon from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element of item to be updated
     * @return false, if there is a major error.
     */
    private boolean setIcon(Map params, Element root) {
        String tmp = (String) params.get(PARAM_ICON);
        if ( tmp!=null && tmp.length()>0 ) {
            DocumentHelper.makeElement(root, "icon").setText(tmp);
        }
        return true;
    }

    /**
     * Updates driver from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element of record to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setDriver(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_DRIVER);
        if ( tmp!=null && tmp.length()>0 ) {
            DocumentHelper.makeElement(root, "driver").setText(tmp);
            return true;
        } else {
            ServletUtils.addError(PARAM_NAME, "Zadejte ovladaè!", env, null);
            return false;
        }
    }

    /**
     * Updates price from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element of record to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setPrice(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_PRICE);
        if ( tmp!=null && tmp.length()>0 ) {
            DocumentHelper.makeElement(root, "price").setText(tmp);
            return true;
        } else {
            ServletUtils.addError(PARAM_NAME, "Zadejte cenu!", env, null);
            return false;
        }
    }

    /**
     * Updates setup from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element of record to be updated
     * @return false, if there is a major error.
     */
    private boolean setSetup(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_SETUP);
        if ( tmp!=null && tmp.length()>0 ) {
            try {
                SafeHTMLGuard.check(tmp);
            } catch (ParserException e) {
                log.error("ParseException on '"+tmp+"'", e);
                ServletUtils.addError(PARAM_SETUP, e.getMessage(), env, null);
                return false;
            } catch (Exception e) {
                ServletUtils.addError(PARAM_SETUP, e.getMessage(), env, null);
                return false;
            }
            Element element = DocumentHelper.makeElement(root, "setup");
            element.setText(tmp);
            Format format = FormatDetector.detect(tmp);
            element.addAttribute("format", Integer.toString(format.getId()));
        }
        return true;
    }

    /**
     * Updates parameters from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element of record to be updated
     * @return false, if there is a major error.
     */
    private boolean setParameters(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_TECHPARAM);
        if ( tmp!=null && tmp.length()>0 ) {
            try {
                SafeHTMLGuard.check(tmp);
            } catch (ParserException e) {
                log.error("ParseException on '"+tmp+"'", e);
                ServletUtils.addError(PARAM_TECHPARAM, e.getMessage(), env, null);
                return false;
            } catch (Exception e) {
                ServletUtils.addError(PARAM_TECHPARAM, e.getMessage(), env, null);
                return false;
            }
            Element element = DocumentHelper.makeElement(root, "params");
            element.setText(tmp);
            Format format = FormatDetector.detect(tmp);
            element.addAttribute("format", Integer.toString(format.getId()));
        }
        return true;
    }

    /**
     * Updates identification from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element of record to be updated
     * @return false, if there is a major error.
     */
    private boolean setIdentification(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_IDENTIFICATION);
        if ( tmp!=null && tmp.length()>0 ) {
            try {
                SafeHTMLGuard.check(tmp);
            } catch (ParserException e) {
                log.error("ParseException on '"+tmp+"'", e);
                ServletUtils.addError(PARAM_IDENTIFICATION, e.getMessage(), env, null);
                return false;
            } catch (Exception e) {
                ServletUtils.addError(PARAM_IDENTIFICATION, e.getMessage(), env, null);
                return false;
            }
            Element element = DocumentHelper.makeElement(root, "identification");
            element.setText(tmp);
            Format format = FormatDetector.detect(tmp);
            element.addAttribute("format", Integer.toString(format.getId()));
        }
        return true;
    }

    /**
     * Updates note from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root root element of record to be updated
     * @return false, if there is a major error.
     */
    private boolean setNote(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_NOTE);
        if ( tmp!=null && tmp.length()>0 ) {
            try {
                SafeHTMLGuard.check(tmp);
            } catch (ParserException e) {
                log.error("ParseException on '"+tmp+"'", e);
                ServletUtils.addError(PARAM_NOTE, e.getMessage(), env, null);
                return false;
            } catch (Exception e) {
                ServletUtils.addError(PARAM_NOTE, e.getMessage(), env, null);
                return false;
            }
            Element element = DocumentHelper.makeElement(root, "note");
            element.setText(tmp);
            Format format = FormatDetector.detect(tmp);
            element.addAttribute("format", Integer.toString(format.getId()));
        }
        return true;
    }
}
