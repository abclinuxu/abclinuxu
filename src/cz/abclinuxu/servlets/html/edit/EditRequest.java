/*
 * User: literakl
 * Date: Feb 4, 2002
 * Time: 2:06:36 PM
 */
package cz.abclinuxu.servlets.html.edit;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.html.view.SendEmail;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
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

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
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

    public static final String VAR_REQUEST_RELATION = "REQUEST";
    public static final String VAR_FORUM_LIST = "FORUMS";
    public static final String VAR_CATEGORIES = "CATEGORIES";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_DELIVER = "deliver";
    public static final String ACTION_MOVE_TO_TODO = "todo";
    public static final String ACTION_MAIL = "email";
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

        if ( action.equals(ACTION_MOVE_TO_TODO) )
            return actionMoveToTODO(request, response, env);

        throw new MissingArgumentException("Chyb� parametr action!");
    }

    protected String actionAdd(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        String author = (String) params.get(PARAM_AUTHOR);
        String email = (String) params.get(PARAM_EMAIL);
        String text = (String) params.get(PARAM_TEXT);
        String category = (String) params.get(PARAM_CATEGORY);
        boolean error = false;

        if ( author==null || author.length()==0 ) {
            ServletUtils.addError(PARAM_AUTHOR,"Slu�nost� je p�edstavit se.",env,null);
            error = true;
        }

        if ( email==null || email.length()==0 ) {
            ServletUtils.addError(PARAM_EMAIL,"Nev�m, kam poslat vyrozum�n�.",env,null);
            error = true;
        } else if ( email.length()<6 || email.indexOf('@')==-1 || email.indexOf('.')==-1 ) {
            ServletUtils.addError(PARAM_EMAIL,"Neplatn� email!.",env,null);
            error = true;
        }

        if ( text==null || text.length()==0 ) {
            ServletUtils.addError(PARAM_TEXT,"Napi�te, co pot�ebujete?",env,null);
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
            env.put(EditRequest.VAR_CATEGORIES, EditRequest.categories);
            return FMTemplateSelector.select("EditRequest","view",env,request);
        }

        Item req = new Item(0,Item.REQUEST);
        if ( user!=null ) req.setOwner(user.getId());

        Document document = DocumentHelper.createDocument();
        DocumentHelper.makeElement(document,"/data/author").addText(author);
        DocumentHelper.makeElement(document,"/data/email").addText(email);
        DocumentHelper.makeElement(document,"/data/text").addText(text);
        if(!Misc.empty(category))
            DocumentHelper.makeElement(document,"/data/category").addText(category);

        req.setData(document);

        Persistance persistance = PersistanceFactory.getPersistance();
        persistance.create(req);
        Relation relation = new Relation(new Category(Constants.CAT_REQUESTS),req,Constants.REL_REQUESTS);
        persistance.create(relation);
        relation.getParent().addChildRelation(relation);

        ServletUtils.addMessage("V� po�adavek byl p�ijat.",env,request.getSession());
        logRequests.info("Autor: "+author+"("+email+")\n"+text);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/hardware/show/"+Constants.REL_REQUESTS);
        return null;
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

    private String actionMoveToTODO(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        Persistance persistance = PersistanceFactory.getPersistance();

        Relation relation = (Relation) env.get(VAR_REQUEST_RELATION);
        persistance.synchronize(relation);
        Item req = (Item) relation.getChild();
        persistance.synchronize(req);
        persistance.remove(relation);
        relation.getParent().removeChildRelation(relation);

        Item diz = (Item) persistance.findById(new Item(Constants.ITEM_DIZ_TODO));
        Record record = (Record) persistance.findById(new Record(Constants.REC_DIZ_TODO));
        Map dizParams = new HashMap();

        Element comment = DocumentHelper.createElement("comment");
        Element root = record.getData().getRootElement();
        EditDiscussion.setCreated(comment);
        EditDiscussion.setParent(dizParams, comment);
        if(req.getOwner()!=0)
            EditDiscussion.setCommentAuthor(dizParams, new User(req.getOwner()), comment, env);
        else {
            dizParams.put(EditDiscussion.PARAM_AUTHOR, req.getData().selectSingleNode("/data/author").getText());
            EditDiscussion.setCommentAuthor(dizParams, null, comment, env);
        }
        dizParams.put(EditDiscussion.PARAM_TITLE, "pozadavek");
        EditDiscussion.setTitle(dizParams, comment, env);
        String s = req.getData().selectSingleNode("/data/text").getText();
        String email = req.getData().selectSingleNode("/data/email").getText();
        dizParams.put(EditDiscussion.PARAM_TEXT, email+"\n\n"+s);
        EditDiscussion.setText(dizParams, comment, env);

        // save comment
        synchronized (root) {
            EditDiscussion.setId(root, comment);
            root.add(comment);
            persistance.update(record);
        }
        diz = (Item) persistance.findById(diz);
        Element itemRoot = diz.getData().getRootElement();
        synchronized (itemRoot) {
            EditDiscussion.setCommentsCount(itemRoot, root);
            persistance.update(diz);
        }

        Map emailParams = new HashMap();
        emailParams.put(EmailSender.KEY_TO, req.getData().selectSingleNode("data/email").getText());
        emailParams.put(EmailSender.KEY_FROM, user.getEmail());
        emailParams.put(EmailSender.KEY_BCC, user.getEmail());
        String text = "Pozadavek byl presunut do seznamu ukolu.\n"+user.getName()+"\n\n\nVas pozadavek\n\n";
        text = text.concat(req.getData().selectSingleNode("data/text").getText());
        emailParams.put(EmailSender.KEY_BODY, text);
        emailParams.put(EmailSender.KEY_SUBJECT, "Pozadavek byl prijat");
        boolean sent = EmailSender.sendEmail(emailParams);
        if ( !sent )
            ServletUtils.addError(Constants.ERROR_GENERIC, "Nemohu odeslat email!", env, request.getSession());

        ServletUtils.addMessage("Po�adavek byl p�esunut.", env, request.getSession());

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/hardware/show/"+Constants.REL_DIZ_TODO);
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
        String text = (String) params.get(PARAM_TEXT);

        if (forumId==0) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Zvolte pros�m lep�� diskusn� f�rum pro tuto diskusi.", env, null);
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
        String action = "P�esunout diskusi <a href=\"/forum/show/"+relationId+"\">"+dizName+
                        "</a> do fora <a href=\""+forumId+"\">"+forumName+"</a> "+forumId;
        if (!Misc.empty(text)) action = action+"<br>\n"+text;
        params.put(PARAM_TEXT, action);

        return actionAdd(request, response, env);
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
