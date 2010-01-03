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
import cz.abclinuxu.servlets.html.view.SendEmail;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.data.view.Comment;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.parser.clean.HtmlPurifier;
import cz.abclinuxu.utils.parser.clean.HtmlChecker;
import cz.abclinuxu.utils.parser.clean.Rules;
import cz.abclinuxu.utils.email.EmailSender;

import cz.abclinuxu.security.ActionCheck;
import cz.abclinuxu.servlets.AbcAutoAction;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.prefs.Preferences;

import freemarker.template.SimpleHash;

public class EditRequest extends AbcAutoAction implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditRequest.class);
    static org.apache.log4j.Logger logRequests = org.apache.log4j.Logger.getLogger("requests");

    public static final String PARAM_AUTHOR = "author";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_TEXT = "text";
    public static final String PARAM_URL = "url";
    public static final String PARAM_REQUEST = "requestId";
    public static final String PARAM_FORUM_ID = "forumId";
    public static final String PARAM_CATEGORY = "category";
    public static final String PARAM_PREVIEW = "preview";

    public static final String VAR_REQUEST_RELATION = "REQUEST";
    public static final String VAR_FORUM_LIST = "FORUMS";
    public static final String VAR_CATEGORIES = "CATEGORIES";
    public static final String VAR_COMMENT = "COMMENT";

    public static final String PREF_CATEGORIES = "categories";
    public static final String PREF_RESPONSE_SUBJECT= "response.subject";

    public static String[] categories;
    private static String subjectForResponse;

    static {
        EditRequest action = new EditRequest();
        ConfigurationManager.getConfigurator().configureAndRememberMe(action);
    }

    @Override
    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        init(request, response, env);

        if (relation == null) {
            relation = (Relation) InstanceUtils.instantiateParam(PARAM_REQUEST, Relation.class, params, request);
            if (relation != null)
                env.put(VAR_REQUEST_RELATION,relation);
        }

        return invokeAction();
    }

    @ActionCheck(checkPost = true)
    public String actionAdd() throws Exception {
        Relation saved = addRequest(null, true, env);
        if (saved == null) {
            env.put(EditRequest.VAR_CATEGORIES, EditRequest.categories);
            return FMTemplateSelector.select("EditRequest", "view", env, request);
        }

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(new Relation(Constants.REL_REQUESTS)) + "#" +saved.getId(), false);
        return null;
    }

    /**
     * Saves new request. The optional prefix will be prepended to the message.
     * @param response
     * @param prefix optional text to be put before the message
     * @param messageRequired when true, message must be specified. If false, prefix must be set.
     * @return relation, when request was successfully saved or null otherwise
     */
    protected Relation addRequest(String prefix, boolean messageRequired, Map env) throws Exception {
        String author = Misc.getString(params, PARAM_AUTHOR);
        String email = (String) params.get(PARAM_EMAIL);
        String text = (String) params.get(PARAM_TEXT);
        text = Misc.filterDangerousCharacters(text);
        String category = (String) params.get(PARAM_CATEGORY);
        String url = (String) params.get(PARAM_URL);
        boolean error = false;

        if ( ! EditDiscussion.checkSpambot(request, response, params, env, user))
            error = true;

        if ( author==null || author.length()==0 ) {
            ServletUtils.addError(PARAM_AUTHOR,"Zadejte prosím své jméno.",env,null);
            error = true;
        }

        if ( email==null || email.length()==0 ) {
            ServletUtils.addError(PARAM_EMAIL,"Zadejte, kam poslat vyrozumění.",env,null);
            error = true;
        } else if ( email.length()<6 || email.indexOf('@')==-1 || email.indexOf('.')==-1 ) {
            ServletUtils.addError(PARAM_EMAIL,"Neplatný email!.",env,null);
            error = true;
        }

        if (messageRequired) {
            if (text == null || text.length() == 0) {
                ServletUtils.addError(PARAM_TEXT, "Napište text vašeho vzkazu.", env, null);
                error = true;
            }
        } else if (prefix==null || prefix.length()==0) {
            log.error("messageRequired == false && prefix == null");
            ServletUtils.addError(PARAM_TEXT, "Interní chyba. Pošlete prosím email na admin@abclinuxu.cz.", env, null);
            error = true;
        }

        if (Misc.empty(category)) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Zvolte si kategorii vašeho požadavku.", env, null);
            error = true;
        }

        try {
            text = HtmlPurifier.clean(text);
            HtmlChecker.check(Rules.DEFAULT, text);
        } catch (Exception e) {
            ServletUtils.addError(PARAM_TEXT, e.getMessage(), env, null);
            error = true;
        }

        if ( error ) {
            params.remove(PARAM_PREVIEW);
            return null;
        }

        if (params.get(PARAM_PREVIEW) != null)
            return null;

        if (prefix!=null) {
            if (text==null)
                text = prefix;
            else
                text = prefix + "\n<br>\n" + text;
        }

		Relation parent = new Relation(Constants.REL_REQUESTS);
        Item req = new Item(0,Item.REQUEST);
        if (user != null)
            req.setOwner(user.getId());

        Tools.sync(parent);
        Category parentCat = (Category) parent.getChild();
		req.setGroup(parentCat.getGroup());
        req.setPermissions(parentCat.getPermissions());

        Document document = DocumentHelper.createDocument();
        DocumentHelper.makeElement(document,"/data/author").addText(author);
        DocumentHelper.makeElement(document,"/data/email").addText(email);
        DocumentHelper.makeElement(document,"/data/text").addText(text);
        DocumentHelper.makeElement(document,"/data/category").addText(category);
        if (url != null)
            DocumentHelper.makeElement(document, "/data/url").addText("http://" + AbcConfig.getHostname() + url);

        req.setData(document);

        Persistence persistence = PersistenceFactory.getPersistence();
        persistence.create(req);
        Relation newRelation = new Relation(parent.getChild(), req, parent.getId());
        persistence.create(newRelation);
        newRelation.getParent().addChildRelation(newRelation);

        sendNotification(newRelation);

        ServletUtils.addMessage("Váš požadavek byl přijat.",env,request.getSession());
        logRequests.info("Autor: "+author+"("+email+")\n"+text);
        return newRelation;
    }

    @ActionCheck(requireDeleteRight = true, itemType = Item.REQUEST, checkTicket = true)
    public String actionDelete() throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();

        persistence.synchronize(relation);
        persistence.remove(relation);
        relation.getParent().removeChildRelation(relation);
        ServletUtils.addMessage("Požadavek byl smazán.",env,request.getSession());

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/hardware/show/"+Constants.REL_REQUESTS);
        return null;
    }

    @ActionCheck(itemType = Item.REQUEST, requireModifyRight = true, checkTicket = true)
    public String actionDeliver() throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();

        persistence.synchronize(relation);
        Item req = (Item) relation.getChild();
        persistence.synchronize(req);
        persistence.remove(relation);
        relation.getParent().removeChildRelation(relation);

        Map emailParams = new HashMap();
        emailParams.put(EmailSender.KEY_TO,req.getData().selectSingleNode("data/email").getText());
        emailParams.put(EmailSender.KEY_FROM,user.getEmail());
        emailParams.put(EmailSender.KEY_BCC,user.getEmail());
        String text = "Hotovo.\n"+user.getName()+"\n\n\nVas pozadavek\n\n";
        text = text.concat(req.getData().selectSingleNode("data/text").getText());
        emailParams.put(EmailSender.KEY_BODY, text);
        emailParams.put(EmailSender.KEY_SUBJECT, "Pozadavek byl vyrizen");
        boolean sent = EmailSender.sendEmail(emailParams);
        if ( !sent )
            ServletUtils.addError(Constants.ERROR_GENERIC, "Nemohu odeslat email!", env, request.getSession());

        ServletUtils.addMessage("Požadavek byl vyřízen.",env,request.getSession());

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(new Relation(Constants.REL_REQUESTS)), false);
        return null;
    }

    @ActionCheck(itemType = Item.REQUEST, requireModifyRight = true)
    public String actionEmail() throws Exception {
        HttpSession session = request.getSession();
        Tools.sync(relation);
        Item req = (Item) relation.getChild();

        String email = req.getData().selectSingleNode("data/email").getText();
        session.setAttribute(SendEmail.PREFIX+EmailSender.KEY_TO, email);
        session.setAttribute(SendEmail.PREFIX+EmailSender.KEY_SUBJECT, subjectForResponse);
        session.setAttribute(SendEmail.PREFIX+SendEmail.PARAM_DISABLE_CODE, Boolean.TRUE);

        String url = response.encodeRedirectURL("/Mail?url=/pozadavky");
        response.sendRedirect(url);
        return null;
    }

    /**
     * Displays several options that user or admin can do with comment or question.
     * Relation id parameter must be set, comment id is optional.
     */
    public String actionComment() throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();

        relation = (Relation) persistence.findById(relation);
        Item discussion = (Item) persistence.findById(relation.getChild());
        Comment comment = EditDiscussion.getDiscussedComment(params, discussion, persistence);

        env.put(VAR_RELATION, relation);
        env.put(VAR_COMMENT, comment);
        return FMTemplateSelector.select("EditRequest", "comment", env, request);
    }

    /**
     * Adds user complaint into Requests to admins.
     */
    @ActionCheck(checkPost = true, checkReferer = true)
    public String actionSubmitComplaint() throws Exception {
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        Persistence persistence = PersistenceFactory.getPersistence();

        Item discussion = (Item) relation.getChild();
        Comment comment = EditDiscussion.getDiscussedComment(params, discussion, persistence);
        String title = comment.getTitle();
        String url = urlUtils.getRelationUrl(relation) + "#"+comment.getId();
        params.put(PARAM_URL, url);

        Relation saved = addRequest(title, false, env);
        if (saved == null)
            return actionComment();

        urlUtils.redirect(response, urlUtils.getRelationUrl(new Relation(Constants.REL_REQUESTS)) + "#" + saved.getId(), false);
        return null;
    }

    public String actionChooseRightForum() throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Category forum = (Category) persistence.findById(new Category(Constants.CAT_FORUM));
        List content = Tools.syncList(forum.getChildren());

        Map forums = new LinkedHashMap();
        content = Sorters2.byName(content);
        for ( Iterator iter = content.iterator(); iter.hasNext(); ) {
            Relation rel = (Relation) iter.next();
            String name = Tools.childName(rel);
            List children = Tools.syncList(rel.getChild().getChildren());
            children = Sorters2.byName(children);
            forums.put(name, children);
        }
        env.put(VAR_FORUM_LIST, new SimpleHash(forums));

        return FMTemplateSelector.select("EditRequest", "chooseRightForum", env, request);
    }

    @ActionCheck(checkPost = true, checkReferer = true)
    public String actionRightForum() throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        int forumId = Misc.parseInt((String) params.get(PARAM_FORUM_ID),0);
        int relationId = Misc.parseInt((String) params.get(PARAM_RELATION_SHORT),0);

        if (forumId==0) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Vyberte prosím diskusní fórum.", env, null);
            return actionChooseRightForum();
        }

        if (relationId == 0) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Parametr rid je prázdný! Napište prosím hlášení chyby.", env, null);
            urlUtils.redirect(response, "/hardware/show/"+Constants.REL_REQUESTS);
            return null;
        }

        Relation discussion = (Relation) persistence.findById(new Relation(relationId));
        String dizName = Tools.childName(discussion);
        String forumName = Tools.childName(new Integer(forumId));
        params.put(PARAM_CATEGORY, "Přesun diskuse");
        String url = urlUtils.getRelationUrl(discussion);
        params.put(PARAM_URL, url);
        String action = "Přesunout diskusi <a href=\"/forum/show/"+relationId+"\">"+dizName+
                        "</a> do fora <a href=\"/forum/dir/"+forumId+"\">"+forumName+"</a> "+forumId;

        Relation saved = addRequest(action, false, env);
        if (saved == null)
            return actionChooseRightForum();

        urlUtils.redirect(response, urlUtils.getRelationUrl(new Relation(Constants.REL_REQUESTS)) + "#" + saved.getId(), false);
        return null;
    }

    /**
     * Sends notification email to admin's mailing list.
     * @param relation relation with request
     */
    private void sendNotification(Relation relation) {
        Persistence persistence = PersistenceFactory.getPersistence();
        Relation requests = (Relation) persistence.findById(new Relation(Constants.REL_REQUESTS));
        Item item = (Item) relation.getChild();

        Map map = new HashMap();
        map.put(EmailSender.KEY_TO, AbcConfig.getAdminsEmail());
        map.put(EmailSender.KEY_SUBJECT, item.getData().selectSingleNode("/data/category").getText());
        map.put(EmailSender.KEY_FROM, "literakl@abclinuxu.cz");
        map.put(EmailSender.KEY_TEMPLATE, "/mail/requests.ftl");
        map.put("URL", requests.getUrl() + "#" + relation.getId());
        map.put("REQUEST", item);
        EmailSender.sendEmail(map);
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        subjectForResponse = prefs.get(PREF_RESPONSE_SUBJECT, "");
        String tmp = prefs.get(PREF_CATEGORIES,"");
        StringTokenizer stk = new StringTokenizer(tmp,",");
        categories = new String[stk.countTokens()];
        int i = 0;
        while(stk.hasMoreTokens()) {
            categories[i++] = stk.nextToken();
        }
    }
}
