/*
 * User: literakl
 * Date: 29.9.2003
 * Time: 20:35:28
 */
package cz.abclinuxu.servlets.html.edit;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.html.view.SendEmail;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.utils.news.NewsCategories;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.parser.safehtml.NewsGuard;
import cz.abclinuxu.utils.format.Format;
import cz.abclinuxu.utils.email.EmailSender;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Date;
import java.text.ParseException;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.htmlparser.util.ParserException;

/**
 * This servlet manipulates with News.
 */
public class EditNews implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditNews.class);

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";
    public static final String ACTION_APPROVE = "approve";
    public static final String ACTION_REMOVE = "remove";
    public static final String ACTION_REMOVE_STEP2 = "remove2";
    public static final String ACTION_SEND_EMAIL = "mail";
    public static final String ACTION_LOCK = "lock";
    public static final String ACTION_UNLOCK = "unlock";

    public static final String VAR_CATEGORIES = "CATEGORIES";
    public static final String VAR_CATEGORY = "CATEGORY";
    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_MESSAGE = "MESSAGE";
    public static final String VAR_AUTHOR = "AUTHOR";
    public static final String VAR_ADMIN = "ADMIN";

    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_CATEGORY = "category";
    public static final String PARAM_CONTENT = "content";
    public static final String PARAM_APPROVE = "approve";
    public static final String PARAM_MESSAGE = "message";
    public static final String PARAM_PREVIEW = "preview";
    public static final String PARAM_PUBLISH_DATE = "publish";


    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if ( ACTION_ADD.equals(action) )
            return actionAddStep1(request, env);

        if ( ACTION_ADD_STEP2.equals(action) )
            return actionAddStep2(request, response, env);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if ( relation==null )
            throw new MissingArgumentException("Chyb� parametr relationId!");
        Tools.sync(relation);
        env.put(VAR_RELATION, relation);

        // check permissions
        if ( !(user.hasRole(Roles.NEWS_ADMIN) || relation.getUpper()==Constants.REL_NEWS_POOL))
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if ( ACTION_EDIT.equals(action) )
            return actionEditStep1(request, env);

        if ( ACTION_EDIT_STEP2.equals(action) )
            return actionEditStep2(request, response, env);

        // check permissions
        if ( !user.hasRole(Roles.NEWS_ADMIN) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if ( ACTION_APPROVE.equals(action) )
            return actionApprove(request, response, env);

        if ( ACTION_REMOVE.equals(action) )
            return FMTemplateSelector.select("EditNews", "remove", env, request);

        if ( ACTION_REMOVE_STEP2.equals(action) )
            return actionRemoveStep2(request, response, env);

        if ( ACTION_SEND_EMAIL.equals(action) )
            return actionSendEmail(request, response, env);

        if ( ACTION_LOCK.equals(action) )
            return actionLock(request, response, env);

        if ( ACTION_UNLOCK.equals(action) )
            return actionUnlock(request, response, env);

        throw new MissingArgumentException("Chyb� parametr action!");
    }

    private String actionAddStep1(HttpServletRequest request, Map env) throws Exception {
        env.put(VAR_CATEGORIES, NewsCategories.getAllCategories());
        return FMTemplateSelector.select("EditNews", "add", env, request);
    }

    protected String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);

        Item item = new Item(0, Item.NEWS);
        item.setData(DocumentHelper.createDocument());
        item.setOwner(user.getId());

        boolean canContinue = true;
        canContinue &= setContent(params, item, env);
        canContinue &= setCategory(params, item);
        canContinue &= setPublishDate(params, item, env);

        if ( !canContinue || params.get(PARAM_PREVIEW)!=null) {
            Relation relation = new Relation(null,item,0);
            item.setInitialized(true);
            item.setCreated(new Date());
            env.put(VAR_RELATION, relation);
            return actionAddStep1(request,env);
        }

        persistance.create(item);
        Relation relation = new Relation(new Category(Constants.CAT_NEWS_POOL),item,Constants.REL_NEWS_POOL);
        persistance.create(relation);
        relation.getParent().addChildRelation(relation);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/news/show/"+relation.getId());
        return null;
    }

    /**
     * Adds admini mailing list to session and redirects to send email screen.
     */
    private String actionSendEmail(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        HttpSession session = request.getSession();
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        User user = (User) persistance.findById(new User(item.getOwner()));

        session.setAttribute(SendEmail.PREFIX+EmailSender.KEY_TO, user.getEmail());
        session.setAttribute(SendEmail.PREFIX+EmailSender.KEY_BCC, "admini@abclinuxu.cz"); // inform group of admins too

        String url = response.encodeRedirectURL("/Mail?url=/news/dir/37672");
        response.sendRedirect(url);
        return null;
    }

    private String actionEditStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();

        Element element = (Element) item.getData().selectSingleNode("/data/content");
        params.put(PARAM_CONTENT,element.getText());
        params.put(PARAM_CATEGORY,item.getSubType());

        env.put(VAR_CATEGORIES, NewsCategories.getAllCategories());

        return FMTemplateSelector.select("EditNews", "edit", env, request);
    }

    protected String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();

        boolean canContinue = true;
        canContinue &= setContent(params, item, env);
        canContinue &= setCategory(params, item);
        canContinue &= setPublishDate(params, item, env);

        if ( !canContinue || params.get(PARAM_PREVIEW)!=null) {
            env.put(VAR_RELATION, relation);
            env.put(VAR_CATEGORIES, NewsCategories.getAllCategories());
            return FMTemplateSelector.select("EditNews", "edit", env, request);
        }

        persistance.update(item);
        User user = (User) env.get(Constants.VAR_USER);
        AdminLogger.logEvent(user, "  edit | news "+relation.getId());
        FeedGenerator.updateNews();

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/news/show/"+relation.getId());
        return null;
    }

    protected String actionApprove(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        if (relation.getParent().getId() != Constants.CAT_NEWS_POOL) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Zpr�vi�ka ji� byla schv�lena!", env, request.getSession());
            urlUtils.redirect(response, "/news/show/" + relation.getId());
            return null;
        }

        Item item = (Item) relation.getChild();
        Element element = DocumentHelper.makeElement(item.getData(), "/data/approved_by");
        User user = (User) env.get(Constants.VAR_USER);
        element.setText(new Integer(user.getId()).toString());
        persistance.update(item);

        relation.getParent().removeChildRelation(relation);
        relation.getParent().setId(Constants.CAT_NEWS);
        relation.setUpper(Constants.REL_NEWS);
        persistance.update(relation);
        relation.getParent().addChildRelation(relation);
        AdminLogger.logEvent(user, "  approve | news " + relation.getId());
        FeedGenerator.updateNews();

        urlUtils.redirect(response, "/news/show/" + relation.getId());
        return null;
    }

    protected String actionRemoveStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        User user = (User) env.get(Constants.VAR_USER);
        Item item = (Item) relation.getChild();
        User author = (User) persistance.findById(new User(item.getOwner()));

        Map map = new HashMap();
        map.put(VAR_RELATION, relation);
        map.put(VAR_AUTHOR, author);
        map.put(VAR_ADMIN, user);
        map.put(EmailSender.KEY_FROM, null); // use default admin email address
        map.put(EmailSender.KEY_BCC, "admini@abclinuxu.cz"); // inform group of admins too
        map.put(EmailSender.KEY_TO, author.getEmail());
        map.put(EmailSender.KEY_SUBJECT, "admin: zpravicka byla smazana");
        map.put(EmailSender.KEY_TEMPLATE, "/mail/rm_zpravicka.ftl");

        String text = (String) params.get(PARAM_MESSAGE);
        if ( text!=null && text.trim().length()>0 )
            map.put(VAR_MESSAGE, text);

        EmailSender.sendEmail(map);

        persistance.remove(relation);
        relation.getParent().removeChildRelation(relation);
        AdminLogger.logEvent(user, "  remove | news " + relation.getId());
        FeedGenerator.updateNews();

        response.sendRedirect(response.encodeRedirectURL("/"));
        return null;
    }

    protected String actionLock(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();

        Element element = DocumentHelper.makeElement(item.getData(), "/data/locked_by");
        User user = (User) env.get(Constants.VAR_USER);
        element.setText(new Integer(user.getId()).toString());

        persistance.update(item);
        AdminLogger.logEvent(user, "  lock | news "+relation.getId());

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/news/show/"+relation.getId());
        return null;
    }

    protected String actionUnlock(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();

        Element element = (Element) item.getData().selectSingleNode("/data/locked_by");
        element.detach();
        persistance.update(item);

        User user = (User) env.get(Constants.VAR_USER);
        AdminLogger.logEvent(user, "  unlock | news "+relation.getId());

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/news/show/"+relation.getId());
        return null;
    }

    // setters

    /**
     * Updates news' content from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item news to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setContent(Map params, Item item, Map env) {
        String text = (String) params.get(PARAM_CONTENT);
        if ( text==null || text.trim().length()==0 ) {
            ServletUtils.addError(PARAM_CONTENT, "Vypl�te obsah zpr�vi�ky", env, null);
            return false;
        }
        try {
            NewsGuard.check(text);
        } catch (ParserException e) {
            log.error("ParseException on '"+text+"'", e);
            ServletUtils.addError(PARAM_CONTENT, e.getMessage(), env, null);
            return false;
        } catch (Exception e) {
            ServletUtils.addError(PARAM_CONTENT, e.getMessage(), env, null);
            return false;
        }
        Element element = DocumentHelper.makeElement(item.getData(), "/data/content");
        element.setText(text);
        element.addAttribute("format", Integer.toString(Format.HTML.getId()));
        return true;
    }

    /**
     * Updates news' content from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item news to be updated
     * @return false, if there is a major error.
     */
    private boolean setCategory(Map params, Item item) {
        String text = (String) params.get(PARAM_CATEGORY);
        if (text==null || text.length()==0)
            return true;

        List categories = NewsCategories.listKeys();
        if ( categories.contains(text) ) {
            item.setSubType(text);
        } else {
            log.warn("Nalezena neznama kategorie zpravicek '"+text+"'!");
        }

        return true;
    }

    /**
     * Updates news from parameters. Changes are not synchronized with persistance.
     *
     * @param params map holding request's parameters
     * @param item   news to be updated
     * @return false, if there is a major error.
     */
    private boolean setPublishDate(Map params, Item item, Map env) {
        String tmp = (String) params.get(PARAM_PUBLISH_DATE);
        if (tmp==null || tmp.length()==0)
            return true;
        try {
            Date date = Constants.isoFormat.parse(tmp);
            item.setCreated(date);
            return true;
        } catch (ParseException e) {
            ServletUtils.addError(PARAM_PUBLISH_DATE, "Chybn� form�t datumu!", env, null);
            return false;
        }
    }
}