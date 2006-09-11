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
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistence.*;
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
import java.util.Map;
import java.util.Date;

/**
 * Cotroller for working drivers manipulation.
 */
public class EditDriver implements AbcAction {
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


    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if ( relation!=null ) {
            persistence.synchronize(relation);
            env.put(VAR_RELATION,relation);
        }

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if ( ACTION_ADD.equals(action) )
            return FMTemplateSelector.select("EditDriver", "add", env, request);

        if ( ACTION_ADD_STEP2.equals(action) )
            return actionAddStep2(request, response, env, true);

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
    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();

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

        Element element = (Element) driver.getData().selectSingleNode("/data/name");
        String url = UrlUtils.PREFIX_DRIVERS + "/" + URLManager.enforceLastURLPart(element.getTextTrim());
        url = URLManager.protectFromDuplicates(url);

        persistence.create(driver);
        Relation relation = new Relation(new Category(Constants.CAT_DRIVERS), driver, Constants.REL_DRIVERS);
        relation.setUrl(url);
        persistence.create(relation);
        relation.getParent().addChildRelation(relation);

        // commit new version
        Misc.commitRelation(document.getRootElement(), relation, user);

        FeedGenerator.updateDrivers();
        VariableFetcher.getInstance().refreshDrivers();

        if (redirect) {
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, url);
        } else
            env.put(VAR_RELATION, relation);

        return null;
    }

    /**
     * Fills environment with existing data of the driver.
     * @return template to be rendered.
     */
    protected String actionEdit(HttpServletRequest request, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item driver = (Item) persistence.findById(relation.getChild());

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
        Persistence persistence = PersistenceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item driver = (Item) persistence.findById(relation.getChild());
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
        persistence.update(driver);

        // commit new version
        Misc.commitRelation(document.getRootElement(), relation, user);

        String url = relation.getUrl();
        if (url==null)
            url = UrlUtils.PREFIX_DRIVERS + "/show/" + relation.getId();

        // run monitor
        String absoluteUrl = "http://www.abclinuxu.cz"+url;
        MonitorAction action = new MonitorAction(user,UserAction.EDIT,ObjectType.DRIVER,driver,absoluteUrl);
        MonitorPool.scheduleMonitorAction(action);

        FeedGenerator.updateDrivers();
        VariableFetcher.getInstance().refreshDrivers();

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, url);
        return null;
    }

    /* ******* setters ********* */

    /**
     * Updates name of driver from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param document Document of discussion to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setName(Map params, Document document, Map env) {
        String tmp = (String) params.get(PARAM_NAME);
        tmp = Misc.filterDangerousCharacters(tmp);
        if ( tmp!=null && tmp.length()>0 ) {
            DocumentHelper.makeElement(document, "data/name").setText(tmp);
        } else {
            ServletUtils.addError(PARAM_NAME, "Zadejte název ovladaèe!", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates driver's version from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param document Document of discussion to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setVersion(Map params, Document document, Map env) {
        String tmp = (String) params.get(PARAM_VERSION);
        tmp = Misc.filterDangerousCharacters(tmp);
        if ( tmp!=null && tmp.length()>0 ) {
            DocumentHelper.makeElement(document, "data/version").setText(tmp);
        } else {
            ServletUtils.addError(PARAM_VERSION, "Zadejte verzi ovladaèe!", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates driver's URL from parameters. Changes are not synchronized with persistence.
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
        } else if ( url.indexOf("://")==-1 || url.length()<12 ) {
            ServletUtils.addError(PARAM_URL, "Neplatná adresa ovladaèe!", env, null);
            return false;
        }
        DocumentHelper.makeElement(document, "data/url").setText(url);
        return true;
    }

    /**
     * Updates driver's note from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param document Document of discussion to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setNote(Map params, Document document, Map env) {
        String tmp = (String) params.get(PARAM_NOTE);
        tmp = Misc.filterDangerousCharacters(tmp);
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
