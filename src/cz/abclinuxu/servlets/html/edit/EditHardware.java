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
import cz.abclinuxu.persistence.versioning.Versioning;
import cz.abclinuxu.persistence.versioning.VersioningFactory;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.parser.safehtml.SafeHTMLGuard;
import cz.abclinuxu.utils.email.monitor.*;
import cz.abclinuxu.utils.format.Format;
import cz.abclinuxu.utils.format.FormatDetector;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.scheduler.VariableFetcher;
import cz.abclinuxu.security.ActionProtector;

import org.dom4j.*;
import org.htmlparser.util.ParserException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * This class is responsible for adding and
 * editing of hardware items and records.
 */
public class EditHardware implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditHardware.class);

    public static final String PARAM_RELATION = "rid";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_DRIVER = "driver";
    public static final String PARAM_DRIVER_URL = "driverUrl";
    public static final String PARAM_PRICE = "price";
    public static final String PARAM_SUPPORT = "support";
    public static final String PARAM_SETUP = "setup";
    public static final String PARAM_TECHPARAM = "params";
    public static final String PARAM_IDENTIFICATION = "identification";
    public static final String PARAM_NOTE = "note";
    public static final String PARAM_PREVIEW = "preview";
    public static final String PARAM_OUTDATED = "outdated";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_PREVIEW = "PREVIEW";

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

    public static final String VALUE_SUPPORT_COMPLETE = "complete";
    public static final String VALUE_SUPPORT_PARTIAL = "partial";
    public static final String VALUE_SUPPORT_NONE = "none";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";


    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        if (ServletUtils.handleMaintainance(request, env))
            response.sendRedirect(response.encodeRedirectURL("/"));

        if ( action==null)
            throw new MissingArgumentException("Chybí parametr action!");

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
        if ( relation!=null ) {
            Tools.sync(relation);
            env.put(VAR_RELATION,relation);
        } else
            throw new MissingArgumentException("Chybí parametr relationId!");

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if ( action.equals(ACTION_ADD) )
            return FMTemplateSelector.select("EditHardware", "add", env, request);

        if ( action.equals(ACTION_ADD_STEP2) ) {
            ActionProtector.ensureContract(request, EditHardware.class, true, true, true, false);
            return actionAddStep2(request, response, env, true);
        }

        if ( action.equals(ACTION_EDIT) )
            return actionEditStep1(request, env);

        if ( action.equals(ACTION_EDIT_STEP2) ) {
            ActionProtector.ensureContract(request, EditHardware.class, true, true, true, false);
            return actionEditStep2(request, response, env);
        }

        throw new MissingArgumentException("Chybí parametr action!");
    }

    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        Relation upper = (Relation) env.get(VAR_RELATION);
        User user = (User) env.get(Constants.VAR_USER);

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        Item item = new Item(0, Item.HARDWARE);
        item.setData(document);
        item.setOwner(user.getId());

        boolean canContinue = true;
        canContinue &= setName(params, root, env);
        canContinue &= setSupport(params, root);
        canContinue &= setDriver(params, root);
        canContinue &= setDriverUrl(params, root, env);
        canContinue &= setPrice(params, root);
        canContinue &= setOutdated(params, root);
        canContinue &= setParameters(params, root, env);
        canContinue &= setIdentification(params, root, env);
        canContinue &= setSetup(params, root, env);
        canContinue &= setNote(params, root, env);

        if (!canContinue || params.get(PARAM_PREVIEW) != null) {
            if (!canContinue)
                params.remove(PARAM_PREVIEW);
            item.setInitialized(true);
            env.put(VAR_PREVIEW, item);
            return FMTemplateSelector.select("EditHardware", "add", env, request);
        }

        Versioning versioning = VersioningFactory.getVersioning();
        versioning.prepareObjectBeforeCommit(item, user.getId());
        persistence.create(item);
        versioning.commit(item, user.getId(), "Počáteční revize dokumentu");

        Relation relation = new Relation(upper.getChild(), item, upper.getId());
        String name = root.elementTextTrim("name");
        String url = upper.getUrl() + "/" + URLManager.enforceRelativeURL(name);
        url = URLManager.protectFromDuplicates(url);
        if (url != null)
            relation.setUrl(url);
        persistence.create(relation);
        relation.getParent().addChildRelation(relation);

        // refresh RSS
        FeedGenerator.updateHardware();
        VariableFetcher.getInstance().refreshHardware();

        if (redirect) {
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
        } else
            env.put(VAR_RELATION, relation);
        return null;
    }

    protected String actionEditStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        Element root = item.getData().getRootElement();

        Node node = root.element("name");
        if ( node!=null )
            params.put(PARAM_NAME, node.getText());
        node = root.element("support");
        if ( node!=null )
            params.put(PARAM_SUPPORT, node.getText());
        node = root.element("driver");
        if ( node!=null )
            params.put(PARAM_DRIVER, node.getText());
        node = root.element("driver_url");
        if ( node!=null )
            params.put(PARAM_DRIVER_URL, node.getText());
        node = root.element("price");
        if ( node!=null )
            params.put(PARAM_PRICE, node.getText());
        node = root.element("outdated");
        if ( node!=null )
            params.put(PARAM_OUTDATED, node.getText());
        node = root.element("setup");
        if ( node!=null )
            params.put(PARAM_SETUP, node.getText());
        node = root.element("params");
        if ( node!=null )
            params.put(PARAM_TECHPARAM, node.getText());
        node = root.element("identification");
        if ( node!=null )
            params.put(PARAM_IDENTIFICATION, node.getText());
        node = root.element("note");
        if ( node!=null )
            params.put(PARAM_NOTE, node.getText());

        return FMTemplateSelector.select("EditHardware","edit",env,request);
    }

    protected String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild().clone();
        Item origItem = (Item) item.clone();
        item.setOwner(user.getId());
        Element root = item.getData().getRootElement();

        boolean canContinue = true;
        canContinue &= setName(params, root, env);
        canContinue &= setSupport(params, root);
        canContinue &= setDriver(params, root);
        canContinue &= setDriverUrl(params, root, env);
        canContinue &= setPrice(params, root);
        canContinue &= setOutdated(params, root);
        canContinue &= setParameters(params, root, env);
        canContinue &= setIdentification(params, root, env);
        canContinue &= setSetup(params, root, env);
        canContinue &= setNote(params, root, env);
        canContinue &= ServletUtils.checkNoChange(item, origItem, env);
        String changesDescription = Misc.getRevisionString(params, env);
        canContinue &= ! Constants.ERROR.equals(changesDescription);

        if (!canContinue || params.get(PARAM_PREVIEW) != null) {
            if (!canContinue)
                params.remove(PARAM_PREVIEW);
            item.setInitialized(true);
            env.put(VAR_PREVIEW, item);
            return FMTemplateSelector.select("EditHardware", "edit", env, request);
        }

        Versioning versioning = VersioningFactory.getVersioning();
        versioning.prepareObjectBeforeCommit(item, user.getId());
        persistence.update(item);
        versioning.commit(item, user.getId(), changesDescription);

        FeedGenerator.updateHardware();

        // run monitor
        String url = relation.getUrl();
        if (url==null)
            url = "/hardware/show/"+relation.getId();
        url = "http://www.abclinuxu.cz" + url;
        MonitorAction action = new MonitorAction(user, UserAction.EDIT, ObjectType.ITEM, item, url);
        MonitorPool.scheduleMonitorAction(action);

        VariableFetcher.getInstance().refreshHardware();

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
        return null;
    }

    /* ******** setters ********* */

    /**
     * Updates name from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root root element of item to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setName(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_NAME);
        tmp = Misc.filterDangerousCharacters(tmp);
        if ( tmp!=null && tmp.length()>0 ) {
            DocumentHelper.makeElement(root, "name").setText(tmp);
            return true;
        } else {
            ServletUtils.addError(PARAM_NAME, "Zadejte název druhu!", env, null);
            return false;
        }
    }

    /**
     * Updates driver from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root root element of record to be updated
     * @return false, if there is a major error.
     */
    private boolean setDriver(Map params, Element root) {
        String tmp = (String) params.get(PARAM_DRIVER);
        if ( tmp!=null && tmp.length()>0 )
            DocumentHelper.makeElement(root, "driver").setText(tmp);
        else {
            Element element = root.element("driver");
            if (element!=null)
                element.detach();
        }
        return true;
    }

    /**
     * Updates price from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root root element of record to be updated
     * @return false, if there is a major error.
     */
    private boolean setPrice(Map params, Element root) {
        String tmp = (String) params.get(PARAM_PRICE);
        if ( tmp!=null && tmp.length()>0 )
            DocumentHelper.makeElement(root, "price").setText(tmp);
        else {
            Element element = root.element("price");
            if (element != null)
                element.detach();
        }
        return true;
    }

    /**
     * Updates outdated from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root root element of record to be updated
     * @return false, if there is a major error.
     */
    private boolean setOutdated(Map params, Element root) {
        String tmp = (String) params.get(PARAM_OUTDATED);
        if ( "yes".equals(tmp) )
            DocumentHelper.makeElement(root, "outdated").setText(tmp);
        else {
            Element element = root.element("outdated");
            if (element != null)
                element.detach();
        }
        return true;
    }

    /**
     * Updates support from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root root element of record to be updated
     * @return false, if there is a major error.
     */
    private boolean setSupport(Map params, Element root) {
        String tmp = (String) params.get(PARAM_SUPPORT);
        if ( tmp!=null && tmp.length()>0 )
            DocumentHelper.makeElement(root, "support").setText(tmp);
        else {
            Element element = root.element("support");
            if (element != null)
                element.detach();
        }
        return true;
    }

    /**
     * Updates driver url from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root root element of record to be updated
     * @return false, if there is a major error.
     */
    private boolean setDriverUrl(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_DRIVER_URL);
        tmp = Misc.filterDangerousCharacters(tmp);
        if (tmp != null && tmp.length() > 0) {
            if (tmp.indexOf('<')!=-1) {
                ServletUtils.addError(PARAM_DRIVER_URL, "HTML značky nejsou povoleny v URL!" , env, null);
                return false;
            }
            Element element = DocumentHelper.makeElement(root, "driver_url");
            element.setText(tmp);
        } else {
            Element element = root.element("driver_url");
            if (element != null)
                element.detach();
        }
        return true;
    }

    /**
     * Updates setup from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root root element of record to be updated
     * @return false, if there is a major error.
     */
    private boolean setSetup(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_SETUP);
        tmp = Misc.filterDangerousCharacters(tmp);
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
     * Updates parameters from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root root element of record to be updated
     * @return false, if there is a major error.
     */
    private boolean setParameters(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_TECHPARAM);
        tmp = Misc.filterDangerousCharacters(tmp);
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
     * Updates identification from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root root element of record to be updated
     * @return false, if there is a major error.
     */
    private boolean setIdentification(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_IDENTIFICATION);
        tmp = Misc.filterDangerousCharacters(tmp);
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
     * Updates note from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root root element of record to be updated
     * @return false, if there is a major error.
     */
    private boolean setNote(Map params, Element root, Map env) {
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
            Element element = DocumentHelper.makeElement(root, "note");
            element.setText(tmp);
            Format format = FormatDetector.detect(tmp);
            element.addAttribute("format", Integer.toString(format.getId()));
        }
        return true;
    }
}
