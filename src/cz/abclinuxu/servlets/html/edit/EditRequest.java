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
import cz.abclinuxu.servlets.html.view.SendEmail;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.data.view.Comment;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.parser.safehtml.SafeHTMLGuard;
import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.AbcException;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.htmlparser.util.ParserException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.prefs.Preferences;

import freemarker.template.SimpleHash;

public class EditRequest implements AbcAction, Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditRequest.class);
    static org.apache.log4j.Logger logRequests = org.apache.log4j.Logger.getLogger("requests");

    public static final String PARAM_AUTHOR = "author";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_TEXT = "text";
    public static final String PARAM_REQUEST = "requestId";
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_FORUM_ID = "forumId";
    public static final String PARAM_CATEGORY = "category";
    public static final String PARAM_PREVIEW = "preview";

    public static final String VAR_REQUEST_RELATION = "REQUEST";
    public static final String VAR_FORUM_LIST = "FORUMS";
    public static final String VAR_CATEGORIES = "CATEGORIES";
    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_COMMENT = "COMMENT";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_DELIVER = "deliver";
    public static final String ACTION_MAIL = "email";
    public static final String ACTION_COMMENT = "comment";
    public static final String ACTION_COMPLAINT = "complaint";
    public static final String ACTION_CHOOSE_RIGHT_FORUM = "chooseRightForum";
    public static final String ACTION_RIGHT_FORUM = "rightForum";

    public static final String PREF_CATEGORIES = "categories";

    public static String[] categories;
    static {
        EditRequest action = new EditRequest();
        ConfigurationManager.getConfigurator().configureAndRememberMe(action);
    }

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_REQUEST, Relation.class, params, request);
        if ( relation!=null )
            env.put(VAR_REQUEST_RELATION,relation);

        if ( action==null || action.equals(ACTION_ADD) )
            return actionAdd(request,response,env);

        if ( action.equals(ACTION_COMMENT) )
            return actionCommentTools(request, env);

        if ( action.equals(ACTION_COMPLAINT) )
            return actionSubmitComplaint(request,response,env);

        if ( action.equals(ACTION_CHOOSE_RIGHT_FORUM) )
            return actionChooseForum(request,env);

        if ( action.equals(ACTION_RIGHT_FORUM) )
            return actionAskForumChange(request,response,env);

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        if ( !user.hasRole(Roles.REQUESTS_ADMIN) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if ( action.equals(ACTION_MAIL) )
            return actionSendEmail(request, response, env);

        if ( action.equals(ACTION_DELETE) )
            return actionDelete(request, response, env);

        if ( action.equals(ACTION_DELIVER) )
            return actionDeliver(request, response, env);

        throw new MissingArgumentException("Chyb� parametr action!");
    }

    protected String actionAdd(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        boolean saved = addRequest(request, null, true, env);
        if (!saved) {
            env.put(EditRequest.VAR_CATEGORIES, EditRequest.categories);
            return FMTemplateSelector.select("EditRequest", "view", env, request);
        }

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/hardware/show/" + Constants.REL_REQUESTS);
        return null;
    }

    /**
     * Saves new request. The optional prefix will be prepended to the message.
     * @param prefix optional text to be put before the message
     * @param messageRequired when true, message must be specified. If false, prefix must be set.
     * @return true, when request was successfully saved
     */
    protected boolean addRequest(HttpServletRequest request, String prefix, boolean messageRequired, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        String author = (String) params.get(PARAM_AUTHOR);
        String email = (String) params.get(PARAM_EMAIL);
        String text = (String) params.get(PARAM_TEXT);
        text = Misc.filterDangerousCharacters(text);
        String category = (String) params.get(PARAM_CATEGORY);
        boolean error = false;

        if ( author==null || author.length()==0 ) {
            ServletUtils.addError(PARAM_AUTHOR,"Zadejte pros�m sv� jm�no.",env,null);
            error = true;
        }

        if ( email==null || email.length()==0 ) {
            ServletUtils.addError(PARAM_EMAIL,"Zadejte, kam poslat vyrozum�n�.",env,null);
            error = true;
        } else if ( email.length()<6 || email.indexOf('@')==-1 || email.indexOf('.')==-1 ) {
            ServletUtils.addError(PARAM_EMAIL,"Neplatn� email!.",env,null);
            error = true;
        }

        if ( "Duplicitn� diskuse".equals(category) )
            messageRequired = false;

        if (messageRequired) {
            if (text == null || text.length() == 0) {
                ServletUtils.addError(PARAM_TEXT, "Napi�te text va�eho vzkazu.", env, null);
                error = true;
            }
        } else if (prefix==null || prefix.length()==0) {
            log.error("messageRequired == false && prefix == null");
            ServletUtils.addError(PARAM_TEXT, "Intern� chyba. Po�lete pros�m email na admin@abclinuxu.cz.", env, null);
            error = true;
        }

        if (Misc.empty(category)) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Zvolte si kategorii va�eho po�adavku.", env, null);
            error = true;
        }

        try {
            SafeHTMLGuard.check(text);
        } catch (ParserException e) {
            log.error("ParseException on '"+text+"'", e);
            ServletUtils.addError(PARAM_TEXT, e.getMessage(), env, null);
            error = true;
        } catch (Exception e) {
            ServletUtils.addError(PARAM_TEXT, e.getMessage(), env, null);
            error = true;
        }

        if ( error ) {
            params.remove(PARAM_PREVIEW);
            return false;
        }

        if (params.get(PARAM_PREVIEW) != null)
            return false;

        if (prefix!=null) {
            if (text==null)
                text = prefix;
            else
                text = prefix + "\n<br>\n" + text;
        }

        Item req = new Item(0,Item.REQUEST);
        if ( user!=null ) req.setOwner(user.getId());

        Document document = DocumentHelper.createDocument();
        DocumentHelper.makeElement(document,"/data/author").addText(author);
        DocumentHelper.makeElement(document,"/data/email").addText(email);
        DocumentHelper.makeElement(document,"/data/text").addText(text);
        DocumentHelper.makeElement(document,"/data/category").addText(category);

        req.setData(document);

        Persistance persistance = PersistanceFactory.getPersistance();
        persistance.create(req);
        Relation relation = new Relation(new Category(Constants.CAT_REQUESTS),req,Constants.REL_REQUESTS);
        persistance.create(relation);
        relation.getParent().addChildRelation(relation);

        ServletUtils.addMessage("V� po�adavek byl p�ijat.",env,request.getSession());
        logRequests.info("Autor: "+author+"("+email+")\n"+text);
        return true;
    }

    protected String actionDelete(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();

        Relation relation = (Relation) env.get(VAR_REQUEST_RELATION);
        persistance.synchronize(relation);
        persistance.remove(relation);
        relation.getParent().removeChildRelation(relation);
        ServletUtils.addMessage("Po�adavek byl smaz�n.",env,request.getSession());

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/hardware/show/"+Constants.REL_REQUESTS);
        return null;
    }

    protected String actionDeliver(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        Persistance persistance = PersistanceFactory.getPersistance();

        Relation relation = (Relation) env.get(VAR_REQUEST_RELATION);
        persistance.synchronize(relation);
        Item req = (Item) relation.getChild();
        persistance.synchronize(req);
        persistance.remove(relation);
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

        ServletUtils.addMessage("Po�adavek byl vy��zen.",env,request.getSession());

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/hardware/show/"+Constants.REL_REQUESTS);
        return null;
    }

    protected String actionSendEmail(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        HttpSession session = request.getSession();
        User user = (User) env.get(Constants.VAR_USER);
        Relation relation = (Relation) env.get(VAR_REQUEST_RELATION);
        Tools.sync(relation);
        Item req = (Item) relation.getChild();

        String email = req.getData().selectSingleNode("data/email").getText();
        session.setAttribute(SendEmail.PREFIX+EmailSender.KEY_TO, email);
        session.setAttribute(SendEmail.PREFIX+EmailSender.KEY_BCC, user.getEmail());

        String url = response.encodeRedirectURL("/Mail?url=/hardware/dir/"+Constants.REL_REQUESTS);
        response.sendRedirect(url);
        return null;
    }

    /**
     * Displays several options that user or admin can do with comment or question.
     * Relation id parameter must be set, comment id is optional.
     */
    private String actionCommentTools(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if (relation==null)
            throw new AbcException("Chyb� ��slo relace! Pros�m kontaktujte n�s, a� m��eme probl�m vy�e�it.");

        relation = (Relation) persistance.findById(relation);
        Item discussion = (Item) persistance.findById(relation.getChild());
        Comment comment = EditDiscussion.getDiscussedComment(params, discussion, persistance);

        env.put(VAR_RELATION, relation);
        env.put(VAR_COMMENT, comment);
        return FMTemplateSelector.select("EditRequest", "comment", env, request);
    }

    /**
     * Adds user complaint into Requests to admins.
     */
    private String actionSubmitComplaint(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);

        relation = (Relation) persistance.findById(relation);
        Item discussion = (Item) persistance.findById(relation.getChild());
        Comment comment = EditDiscussion.getDiscussedComment(params, discussion, persistance);
        String title = comment.getTitle();
        String action = "<a href=\"/forum/show/" + relation.getId() + "#"+comment.getId()+"\">" + title + "</a>";

        boolean saved = addRequest(request, action, true, env);
        if (!saved)
            return actionCommentTools(request, env);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/hardware/show/" + Constants.REL_REQUESTS);
        return null;
    }

    private String actionChooseForum(HttpServletRequest request, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Category forum = (Category) persistance.findById(new Category(Constants.CAT_FORUM));
        List content = Tools.syncList(forum.getChildren());

        Map forums = new LinkedHashMap();
        content = Sorters2.byName(content);
        for ( Iterator iter = content.iterator(); iter.hasNext(); ) {
            Relation relation = (Relation) iter.next();
            String name = Tools.childName(relation);
            List children = Tools.syncList(relation.getChild().getChildren());
            children = Sorters2.byName(children);
            forums.put(name, children);
        }
        env.put(VAR_FORUM_LIST, new SimpleHash(forums));

        return FMTemplateSelector.select("EditRequest", "chooseRightForum", env, request);
    }

    private String actionAskForumChange(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        int forumId = Misc.parseInt((String) params.get(PARAM_FORUM_ID),0);
        int relationId = Misc.parseInt((String) params.get(PARAM_RELATION_SHORT),0);

        if (forumId==0) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Vyberte pros�m diskusn� f�rum.", env, null);
            return actionChooseForum(request, env);
        }

        if (relationId==0) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Parametr rid je pr�zdn�! Napi�te pros�m hl�en� chyby.", env, null);
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/hardware/show/"+Constants.REL_REQUESTS);
            return null;
        }

        String dizName = Tools.childName(new Integer(relationId));
        String forumName = Tools.childName(new Integer(forumId));
        params.put(PARAM_CATEGORY, "P�esun diskuse");
        String action = "P�esunout diskusi <a href=\"/forum/show/"+relationId+"\">"+dizName+
                        "</a> do fora <a href=\"/forum/dir/"+forumId+"\">"+forumName+"</a> "+forumId;

        boolean saved = addRequest(request, action, false, env);
        if (!saved)
            return actionChooseForum(request, env);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/hardware/show/" + Constants.REL_REQUESTS);
        return null;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        String tmp = prefs.get(PREF_CATEGORIES,"");
        StringTokenizer stk = new StringTokenizer(tmp,",");
        categories = new String[stk.countTokens()];
        int i = 0;
        while(stk.hasMoreTokens()) {
            categories[i++] = stk.nextToken();
        }
    }
}
