/*
 * User: literakl
 * Date: Jan 30, 2002
 * Time: 8:24:00 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.monitor.*;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.exceptions.PersistanceException;

import org.dom4j.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import java.util.Map;
import java.util.Date;

/**
 * This class is responsible for adding and
 * editing of hardware items and records.
 */
public class EditHardware extends AbcFMServlet {
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


    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        if ( action==null)
            throw new MissingArgumentException("Chybí parametr action!");

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT,PARAM_RELATION,Relation.class,params);
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
            return actionAddStep3(request, response, env);

        if ( action.equals(ACTION_ADD_RECORD) ) {
            params.put(PARAM_ACTION, ACTION_ADD_RECORD_STEP2);
            return FMTemplateSelector.select("EditHardware", "add_record", env, request);
        }

        if ( action.equals(ACTION_ADD_RECORD_STEP2) )
            return actionAddRecord(request, response, env);

        if ( action.equals(ACTION_EDIT_RECORD) || action.equals(ACTION_EDIT_RECORD_STEP2) ) {
            Record record = (Record) InstanceUtils.instantiateParam(PARAM_RECORD_ID, Record.class, params);
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

    protected String actionAddStep3(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation upper = (Relation) env.get(VAR_RELATION);
        User user = (User) env.get(Constants.VAR_USER);

        String name = (String) params.get(PARAM_NAME);
        if ( name==null || name.length()==0 ) {
            ServletUtils.addError(PARAM_NAME,"Zadejte název druhu!",env,null);
            return FMTemplateSelector.select("EditHardware","add_item",env,request);
        }
        String icon = (String) params.get(PARAM_ICON);

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        root.addElement("name").addText(name);
        if ( icon!=null && icon.length()>0 )
            root.addElement("icon").addText(icon);

        Item item = new Item(0,Item.MAKE);
        item.setData(document);
        item.setOwner(user.getId());

        String driver = (String) params.get(PARAM_DRIVER);
        String price = (String) params.get(PARAM_PRICE);
        String setup = (String) params.get(PARAM_SETUP);
        String tech = (String) params.get(PARAM_TECHPARAM);
        String note = (String) params.get(PARAM_NOTE);
        String identification = (String) params.get(PARAM_IDENTIFICATION);

        if ( (note==null || note.length()==0) && (setup==null || setup.length()==0) ) {
            ServletUtils.addError(PARAM_SETUP,"Vyplòte postup zprovoznìní nebo poznámku!",env,null);
            return FMTemplateSelector.select("EditHardware","add_record",env,request);
        }

        document = DocumentHelper.createDocument();
        root = document.addElement("data");
        if ( !Misc.empty(price) )
            root.addElement("price").addText(price);
        if ( !Misc.empty(driver) )
            root.addElement("driver").addText(driver);
        if ( !Misc.empty(setup) )
            root.addElement("setup").addText(setup);
        if ( !Misc.empty(tech) )
            root.addElement("params").addText(tech);
        if ( !Misc.empty(identification) )
            root.addElement("identification").addText(identification);
        if ( !Misc.empty(note) )
            root.addElement("note").addText(note);

        Record record = new Record(0,Record.HARDWARE);
        record.setData(document);
        record.setOwner(user.getId());

        try {
            persistance.create(item);
            Relation relation = new Relation(upper.getChild(),item,upper.getId());
            persistance.create(relation);
            persistance.create(record);
            persistance.create(new Relation(item,record,relation.getId()));

            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/ViewRelation?rid="+relation.getId());
            return null;
        } catch (PersistanceException e) {
            ServletUtils.addError(Constants.ERROR_GENERIC,e.getMessage(),env, null);
            return FMTemplateSelector.select("EditHardware","add_record",env,request);
        }
    }

    protected String actionAddRecord(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);

        String driver = (String) params.get(PARAM_DRIVER);
        String price = (String) params.get(PARAM_PRICE);
        String setup = (String) params.get(PARAM_SETUP);
        String tech = (String) params.get(PARAM_TECHPARAM);
        String note = (String) params.get(PARAM_NOTE);
        String identification = (String) params.get(PARAM_IDENTIFICATION);

        if ( (note==null || note.length()==0) && (setup==null || setup.length()==0) ) {
            ServletUtils.addError(PARAM_SETUP,"Vyplòte postup zprovoznìní nebo poznámku!",env,null);
            return FMTemplateSelector.select("EditHardware","add_record",env,request);
        }

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        if ( !Misc.empty(price) )
            root.addElement("price").addText(price);
        if ( !Misc.empty(driver) )
            root.addElement("driver").addText(driver);
        if ( !Misc.empty(setup) )
            root.addElement("setup").addText(setup);
        if ( !Misc.empty(tech) )
            root.addElement("params").addText(tech);
        if ( !Misc.empty(identification) )
            root.addElement("identification").addText(identification);
        if ( !Misc.empty(note) )
            root.addElement("note").addText(note);

        Record record = new Record(0,Record.HARDWARE);
        record.setData(document);
        record.setOwner(user.getId());
        Relation upper = (Relation) env.get(VAR_RELATION), relation = null;

        persistance.create(record);
        relation = new Relation(upper.getChild(), record, upper.getId());
        persistance.create(relation);

        // run monitor
        String url = "http://www.abclinuxu.cz"+urlUtils.getPrefix()+"/ViewRelation?rid="+upper.getId();
        Item item = (Item) persistance.findById(upper.getChild());
        MonitorAction action = new MonitorAction(user, UserAction.ADD, ObjectType.ITEM, item, url);
        MonitorPool.scheduleMonitorAction(action);

        urlUtils.redirect(response, "/ViewRelation?rid="+relation.getId());
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
        Document document = item.getData();

        Node node = DocumentHelper.makeElement(document,"data/name");
        tmp = (String) params.get(PARAM_NAME);
        if ( tmp==null || tmp.length()==0 ) {
            ServletUtils.addError(PARAM_NAME,"Zadejte název druhu!",env,null);
            return FMTemplateSelector.select("EditHardware","edit_item",env,request);
        }
        node.setText(tmp);

        node = DocumentHelper.makeElement(document,"data/icon");
        tmp = (String) params.get(PARAM_ICON);
        if ( tmp==null || tmp.length()==0 ) {
            ServletUtils.addError(PARAM_ICON,"Zadejte ikonu!",env,null);
            return FMTemplateSelector.select("EditHardware","edit_item",env,request);
        }
        node.setText(tmp);

        persistance.update(item);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/ViewRelation?rid="+relation.getUpper());
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

        String driver = (String) params.get(PARAM_DRIVER);
        String price = (String) params.get(PARAM_PRICE);
        String setup = (String) params.get(PARAM_SETUP);
        String tech = (String) params.get(PARAM_TECHPARAM);
        String note = (String) params.get(PARAM_NOTE);
        String identification = (String) params.get(PARAM_IDENTIFICATION);

        if ( Misc.empty(note) && Misc.empty(setup) ) {
            ServletUtils.addError(PARAM_SETUP,"Vyplòte postup zprovoznìní nebo poznámku!",env,null);
            return FMTemplateSelector.select("EditHardware","edit_record",env,request);
        }

        Relation relation = (Relation) env.get(VAR_RELATION);
        Record record = (Record) env.get(VAR_RECORD);
        Document document = record.getData();

        DocumentHelper.makeElement(document,"data/driver").setText(driver);
        DocumentHelper.makeElement(document,"data/price").setText(price);
        if ( !Misc.empty(setup) )
            DocumentHelper.makeElement(document,"data/setup").setText(setup);
        if ( !Misc.empty(tech) )
            DocumentHelper.makeElement(document,"data/params").setText(tech);
        if ( !Misc.empty(identification) )
            DocumentHelper.makeElement(document,"data/identification").setText(identification);
        if ( !Misc.empty(note) )
            DocumentHelper.makeElement(document,"data/note").setText(note);

        persistance.update(record);

        // run monitor
        String url = "http://www.abclinuxu.cz"+urlUtils.getPrefix()+"/ViewRelation?rid="+relation.getId();
        Item item = (Item) persistance.findById(relation.getChild());
        MonitorAction action = new MonitorAction(user, UserAction.EDIT, ObjectType.ITEM, item, url);
        MonitorPool.scheduleMonitorAction(action);

        urlUtils.redirect(response, "/ViewRelation?rid="+relation.getId());
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
        urlUtils.redirect(response, "/ViewRelation?rid="+relation.getId());
        return null;
    }
}
