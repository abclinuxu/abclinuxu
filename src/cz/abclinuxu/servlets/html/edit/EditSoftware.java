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
import cz.abclinuxu.utils.email.monitor.*;
import cz.abclinuxu.utils.format.Format;
import cz.abclinuxu.utils.format.FormatDetector;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.exceptions.PersistanceException;

import org.dom4j.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import java.util.Map;
import java.util.Date;

public class EditSoftware implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditSoftware.class);

    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_ICON = cz.abclinuxu.servlets.html.select.SelectIcon.PARAM_ICON;
    public static final String PARAM_URL = "url";
    public static final String PARAM_TEXT = "text";
    public static final String PARAM_VERSION = "version";
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


    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        if ( action==null )
            throw new MissingArgumentException("Chybí parametr action!");

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if ( relation!=null ) {
            persistance.synchronize(relation);
            persistance.synchronize(relation.getChild());
            env.put(VAR_RELATION,relation);
        } else
            throw new MissingArgumentException("Chybí parametr relationId!");

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if ( action.equals(ACTION_ADD_ITEM) )
            return FMTemplateSelector.select("EditSoftware", "add_item", env, request);

        if ( action.equals(ACTION_ADD_ITEM_STEP2) )
            return actionAddStep2(request, env);

        if ( action.equals(ACTION_ADD_ITEM_STEP3) )
            return actionAddStep3(request, response, env);

        if ( action.equals(ACTION_ADD_RECORD) ) {
            params.put(PARAM_ACTION, ACTION_ADD_RECORD_STEP2);
            return FMTemplateSelector.select("EditSoftware", "add_record", env, request);
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
            ServletUtils.addError(PARAM_NAME,"Nevyplnil jste název druhu!",env,null);
            return FMTemplateSelector.select("EditSoftware","add_item",env,request);
        }
        return FMTemplateSelector.select("EditSoftware","add_record",env,request);
    }

    protected String actionAddStep3(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation upper = (Relation) env.get(VAR_RELATION);
        User user = (User) env.get(Constants.VAR_USER);

        String name = (String) params.get(PARAM_NAME);
        if ( Misc.empty(name) ) {
            ServletUtils.addError(PARAM_NAME,"Nevyplnil jste název druhu!",env,null);
            return FMTemplateSelector.select("EditSoftware","add_item",env,request);
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

        String url = (String) params.get(PARAM_URL);
        String text = (String) params.get(PARAM_TEXT);
        String version = (String) params.get(PARAM_VERSION);

        if ( Misc.empty(text) ) {
            ServletUtils.addError(PARAM_TEXT,"Vyplòte návod!",env,null);
            return FMTemplateSelector.select("EditSoftware","add_record",env,request);
        }

        document = DocumentHelper.createDocument();
        root = document.addElement("data");

        Element element = root.addElement("text");
        element.addText(text);
        Format format = FormatDetector.detect(text);
        element.addAttribute("format", Integer.toString(format.getId()));

        if ( url!=null && url.length()>0 )
            root.addElement("url").addText(url);
        if ( version!=null && version.length()>0 )
            root.addElement("version").addText(version);

        Record record = new Record(0,Record.SOFTWARE);
        record.setData(document);
        record.setOwner(user.getId());

        try {
            persistance.create(item);
            Relation relation = new Relation(upper.getChild(),item,upper.getId());
            persistance.create(relation);
            relation.getParent().addChildRelation(relation);

            persistance.create(record);
            Relation recordRelation = new Relation(item,record,relation.getId());
            persistance.create(recordRelation);
            recordRelation.getParent().addChildRelation(recordRelation);

            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/show/"+relation.getId());
            return null;
        } catch (PersistanceException e) {
            ServletUtils.addError(Constants.ERROR_GENERIC,e.getMessage(),env, null);
            return FMTemplateSelector.select("EditSoftware","add_record",env,request);
        }
    }

    protected String actionAddRecord(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation upper = (Relation) env.get(VAR_RELATION);
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);

        String url = (String) params.get(PARAM_URL);
        String text = (String) params.get(PARAM_TEXT);
        String version = (String) params.get(PARAM_VERSION);

        if ( Misc.empty(text) ) {
            ServletUtils.addError(PARAM_TEXT,"Vyplòte návod!",env,null);
            return FMTemplateSelector.select("EditSoftware","add_record",env,request);
        }

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");

        Element element = root.addElement("text");
        element.addText(text);
        Format format = FormatDetector.detect(text);
        element.addAttribute("format", Integer.toString(format.getId()));

        if ( url!=null && url.length()>0 )
            root.addElement("url").addText(url);
        if ( version!=null && version.length()>0 )
            root.addElement("version").addText(version);

        Record record = new Record(0,Record.SOFTWARE);
        record.setData(document);
        record.setOwner(user.getId());

        persistance.create(record);
        Relation relation = new Relation(upper.getChild(), record, upper.getId());
        persistance.create(relation);
        relation.getParent().addChildRelation(relation);

        // run monitor
        url = "http://www.abclinuxu.cz"+urlUtils.getPrefix()+"/show/"+upper.getId();
        Item item = (Item) persistance.findById(upper.getChild());
        MonitorAction action = new MonitorAction(user, UserAction.ADD, ObjectType.ITEM, item, url);
        MonitorPool.scheduleMonitorAction(action);

        urlUtils.redirect(response, "/show/"+relation.getId());
        return null;
    }

    protected String actionEditRecord(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Record record = (Record) env.get(VAR_RECORD);

        Document document = record.getData();
        Node node = document.selectSingleNode("data/url");
        if ( node!=null )
            params.put(PARAM_URL,node.getText());
        node = document.selectSingleNode("data/version");
        if ( node!=null )
            params.put(PARAM_VERSION,node.getText());
        node = document.selectSingleNode("data/text");
        if ( node!=null )
            params.put(PARAM_TEXT,node.getText());

        params.put(PARAM_ACTION,ACTION_EDIT_RECORD_STEP2);
        return FMTemplateSelector.select("EditSoftware","edit_record",env,request);
    }

    protected String actionEditRecord2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(VAR_RELATION);
        Record record = (Record) env.get(VAR_RECORD);
        Document document = record.getData();

        String url = (String) params.get(PARAM_URL);
        String text = (String) params.get(PARAM_TEXT);
        String version = (String) params.get(PARAM_VERSION);

        if ( Misc.empty(text) ) {
            ServletUtils.addError(PARAM_TEXT,"Vyplòte návod!",env,null);
            return FMTemplateSelector.select("EditSoftware","edit_record",env,request);
        }

        Element element = DocumentHelper.makeElement(document,"data/text");
        element.setText(text);
        Format format = FormatDetector.detect(text);
        element.addAttribute("format", Integer.toString(format.getId()));

        if ( url!=null )
            DocumentHelper.makeElement(document,"data/url").setText(url);
        if ( version!=null )
            DocumentHelper.makeElement(document,"data/version").setText(version);

        Date updated = record.getUpdated();
        persistance.update(record);
        if ( user.getId()!=record.getOwner() )
            SQLTool.getInstance().setUpdatedTimestamp(record, updated);

        // run monitor
        url = "http://www.abclinuxu.cz"+urlUtils.getPrefix()+"/show/"+relation.getId();
        GenericObject obj = (relation.getParent() instanceof Item) ? relation.getParent() : relation.getChild();
        Item item = (Item) persistance.findById(obj);
        MonitorAction action = new MonitorAction(user, UserAction.EDIT, ObjectType.ITEM, item, url);
        MonitorPool.scheduleMonitorAction(action);

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

        return FMTemplateSelector.select("EditSoftware","edit_item",env,request);
    }

    protected String actionEditItem2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        String tmp = (String) params.get(PARAM_CHOOSE_ICON);
        if ( !Misc.empty(tmp) ) {
            // it is not possible to use UrlUtils.dispatch(), because it would prepend prefix!
            RequestDispatcher dispatcher = request.getRequestDispatcher("/SelectIcon");
            dispatcher.forward(request, response);
            return null;
        }

        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        Document document = item.getData();

        Node node = document.selectSingleNode("data/name");
        tmp = (String) params.get(PARAM_NAME);
        if ( Misc.empty(tmp) ) {
            ServletUtils.addError(PARAM_NAME,"Nevyplnil jste název druhu!",env,null);
            return FMTemplateSelector.select("EditSoftware","edit_item",env,request);
        }
        node.setText(tmp);

        node = DocumentHelper.makeElement(document, "data/icon");
        tmp = (String) params.get(PARAM_ICON);
        if (tmp == null || tmp.length() == 0) {
            ServletUtils.addError(PARAM_ICON, "Zadejte ikonu!", env, null);
            return FMTemplateSelector.select("EditHardware", "edit_item", env, request);
        }
        node.setText(tmp);

        persistance.update(item);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/show/"+relation.getUpper());
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
}
