/*
 * User: literakl
 * Date: Jan 10, 2002
 * Time: 5:07:04 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.servlets.view.ViewUser;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.PersistanceException;
import cz.abclinuxu.AbcException;
import cz.abclinuxu.security.Guard;
import cz.abclinuxu.servlets.utils.Email;
import cz.abclinuxu.servlets.utils.VelocityHelper;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import javax.servlet.http.*;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Class for manipulation with User.
 * <dl>
 * <dt><code>PARAM_LOGIN</code></dt>
 * <dd>Login name of the user.</dd>
 * <dt><code>PARAM_NAME</code></dt>
 * <dd>Real name of the user.</dd>
 * <dt><code>PARAM_EMAIL</code></dt>
 * <dd>His email address.</dd>
 * <dt><code>PARAM_PASSWORD</code></dt>
 * <dd>His password.</dd>
 * <dt><code>PARAM_PASSWORD2</code></dt>
 * <dd>Check of password.</dd>
 * <dt><code>PARAM_ILIKEQ</code></dt>
 * <dd>I Like Q account id.</dd>
 * <dt><code>PARAM_NEWS</code></dt>
 * <dd>If the value is "yes", we may send news about this server to the user.</dd>
 * <dt><code>PARAM_ADS</code></dt>
 * <dd>If this value is "yes", we may send two advertisements a month to the user.</dd>
 * <dt><code>PARAM_SEX</code></dt>
 * <dd>Sex of the user - "man" or "woman".</dd>
 * <dt><code>PARAM_WWW</code></dt>
 * <dd>URL of user's homepage.</dd>
 * </dl>
 */
public class EditUser extends AbcServlet {
    public static final String PARAM_LOGIN = "login";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_PASSWORD2 = "password2";
    public static final String PARAM_PASSCHECK = "passcheck";
    public static final String PARAM_ILIKEQ = "ilikeq";
    public static final String PARAM_NEWS = "news";
    public static final String PARAM_ADS = "ads";
    public static final String PARAM_SEX = "sex";
    public static final String PARAM_WWW = "www";
    public static final String PARAM_PERSONAL = "personal";
    public static final String PARAM_USER = ViewUser.PARAM_USER;

    public static final String VAR_MANAGED = "MANAGED";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";
    public static final String ACTION_PASSWORD = "password";
    public static final String ACTION_PASSWORD2 = "password2";

    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        String action = (String) params.get(AbcServlet.PARAM_ACTION);

        User managed = (User) instantiateParam(PARAM_USER,User.class,params);
        User user = (User) ctx.get(AbcServlet.VAR_USER);
        if ( managed==null ) managed = user;
        ctx.put(VAR_MANAGED,managed);

        if (  action==null || action.equals(EditUser.ACTION_ADD) ) {
            return getTemplate("add/user.vm");

        } else if ( action.equals(EditUser.ACTION_ADD_STEP2) ) {
            return actionAddStep2(request,response,ctx);

        } else if ( action.equals(EditUser.ACTION_EDIT) ) {
            if ( managed!=null ) PersistanceFactory.getPersistance().synchronize(managed);
            int rights = Guard.check(user,managed,Guard.OPERATION_EDIT,params.get(PARAM_PASSWORD));

            switch (rights) {
                case Guard.ACCESS_LOGIN: return getTemplate("login.vm");
                //case Guard.ACCESS_DENIED: addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná nebo jste zadal ¹patné heslo!",ctx, null);
                default: return actionEditStep1(request,ctx);
            }

        } else if ( action.equals(EditUser.ACTION_EDIT_STEP2) ) {
            if ( managed!=null ) PersistanceFactory.getPersistance().synchronize(managed);
            int rights = Guard.check(user,managed,Guard.OPERATION_EDIT,params.get(PARAM_PASSCHECK));

            switch (rights) {
                case Guard.ACCESS_LOGIN: return getTemplate("login.vm");
                case Guard.ACCESS_DENIED: {
                    addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná nebo jste zadal ¹patné heslo!",ctx, null);
                    return actionEditStep1(request,ctx);
                }
                default: return actionEditStep2(request,response,ctx);
            }

        } else if ( action.equals(EditUser.ACTION_PASSWORD) ) {
            if ( managed!=null ) PersistanceFactory.getPersistance().synchronize(managed);
            int rights = Guard.check(user,managed,Guard.OPERATION_EDIT,params.get(PARAM_PASSCHECK));
            if ( user!=null && managed.equals(user) ) rights = Guard.ACCESS_OK;

            switch (rights) {
                case Guard.ACCESS_LOGIN: return getTemplate("login.vm");
                case Guard.ACCESS_DENIED: addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná nebo jste zadal ¹patné heslo!",ctx, null);
                default: return getTemplate("edit/password.vm");
            }

        } else if ( action.equals(EditUser.ACTION_PASSWORD2) ) {
            if ( managed!=null ) PersistanceFactory.getPersistance().synchronize(managed);
            int rights = Guard.check(user,managed,Guard.OPERATION_EDIT,params.get(PARAM_PASSCHECK));

            switch (rights) {
                case Guard.ACCESS_LOGIN: return getTemplate("login.vm");
                case Guard.ACCESS_DENIED: {
                    addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná nebo jste zadal ¹patné heslo!",ctx, null);
                    return getTemplate("edit/password.vm");
                }
                default: return actionPassword(request,response,ctx);
            }
        }
        return getTemplate("add/user.vm");
    }

    /**
     * Creates new user
     */
    protected Template actionAddStep2(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        User user = new User();
        if ( !fillUser(request,user,ctx,true) ) return getTemplate("add/user.vm");

        try {
            PersistanceFactory.getPersistance().create(user);
        } catch ( PersistanceException e ) {
            if ( e.getStatus()==AbcException.DB_DUPLICATE ) {
                addError(PARAM_LOGIN,"Toto jméno je ji¾ pou¾íváno!",ctx, null);
            }
            return getTemplate("add/user.vm");
        }

        VelocityContext tmpContext = new VelocityContext();
        tmpContext.put(AbcServlet.VAR_USER,user);
        String message = VelocityHelper.mergeTemplate("mail/welcome_user.vm",tmpContext);
        Email.sendEmail("admin@AbcLinuxu.cz",user.getEmail(),"Privitani",message);

        HttpSession session = request.getSession();
        session.setAttribute(VAR_USER,user);
        return getTemplate("messages/welcome_user.vm");
    }

    /**
     * Prepares for an update of user
     */
    protected Template actionEditStep1(HttpServletRequest request, Context ctx) throws Exception {
        User user = (User) ctx.get(VAR_MANAGED);
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        PersistanceFactory.getPersistance().synchronize(user);

        params.put(EditUser.PARAM_LOGIN,user.getLogin());
        params.put(EditUser.PARAM_NAME,user.getName());
        params.put(EditUser.PARAM_EMAIL,user.getEmail());

        Document document = user.getData();
        Node node = document.selectSingleNode("data/ilikeq");
        if (node!=null) params.put(EditUser.PARAM_ILIKEQ,node.getText());

        node = document.selectSingleNode("data/news");
        if (node!=null) params.put(EditUser.PARAM_NEWS,node.getText());

        node = document.selectSingleNode("data/ads");
        if (node!=null) params.put(EditUser.PARAM_ADS,node.getText());

        node = document.selectSingleNode("data/sex");
        if (node!=null) params.put(EditUser.PARAM_SEX,node.getText());

        node = document.selectSingleNode("data/www");
        if (node!=null) params.put(EditUser.PARAM_WWW,node.getText());

        node = document.selectSingleNode("data/personal");
        if (node!=null) params.put(EditUser.PARAM_PERSONAL,VelocityHelper.escapeAmpersand(node.getText()));

        return getTemplate("edit/user.vm");
    }

    /**
     * Updates existing user
     */
    protected Template actionEditStep2(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        User user = (User) ctx.get(VAR_MANAGED);
        PersistanceFactory.getPersistance().synchronize(user);

        if ( !fillUser(request,user,ctx,false) ) return getTemplate("edit/user.vm");
        try {
            PersistanceFactory.getPersistance().update(user);
        } catch ( PersistanceException e ) {
            if ( e.getStatus()==AbcException.DB_DUPLICATE ) {
                addError(PARAM_LOGIN,"Toto jméno je ji¾ pou¾íváno!",ctx, null);
            }
            return getTemplate("edit/user.vm");
        }

        User sessionUser = (User) ctx.get(VAR_USER);
        if ( user.getId()==sessionUser.getId() ) {
            sessionUser.synchronizeWith(user);
        }

        addMessage("Zmìny byly ulo¾eny.",ctx, request.getSession());
        redirect("/Index",response,ctx);
        return null;
    }

    /**
     * Fills selected user with values from request. Common to add and edit.
     * @param updatePassword if true, it attempts to read and update password
     */
    protected boolean fillUser(HttpServletRequest request, User user, Context ctx, boolean updatePassword) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        boolean error = false;

        String login = (String) params.get(EditUser.PARAM_LOGIN);
        String name = (String) params.get(EditUser.PARAM_NAME);
        String password = (String) params.get(EditUser.PARAM_PASSWORD);
        String email = (String) params.get(EditUser.PARAM_EMAIL);

        if ( login==null || login.length()<4 ) {
            addError(PARAM_LOGIN,"Zadané pøihla¹ovací jméno je pøíli¹ krátké!",ctx, null);
            error = true;
        }
        if ( name==null || name.length()<5 ) {
            addError(PARAM_NAME,"Zadané jméno je pøíli¹ krátké!",ctx, null);
            error = true;
        }
        if ( updatePassword ) {
            if ( password==null || password.length()<4 ) {
                addError(PARAM_PASSWORD,"Heslo je pøíli¹ krátké!",ctx, null);
                error = true;
            }
            if ( password!=null && !(password.equals((String) request.getParameter(EditUser.PARAM_PASSWORD2))) ) {
                addError(PARAM_PASSWORD,"Hesla se li¹í!",ctx, null);
                error = true;
            }
        }
        if ( email==null || email.length()<6 || email.indexOf('@')==-1 ) {
            addError(PARAM_EMAIL,"Neplatný email!",ctx, null);
            error = true;
        }

        if ( error ) return false;

        String ilikeq = (String) params.get(EditUser.PARAM_ILIKEQ);
        String personal = (String) params.get(EditUser.PARAM_PERSONAL);
        String news = (String) params.get(EditUser.PARAM_NEWS);
        String sex = (String) params.get(EditUser.PARAM_SEX);
        String ads = (String) params.get(EditUser.PARAM_ADS);
        String www = (String) params.get(EditUser.PARAM_WWW);

        Document document = DocumentHelper.createDocument();
        if ( ilikeq!=null && ilikeq.length()>0 ) DocumentHelper.makeElement(document,"/data/ilikeq").addText(ilikeq);
        if ( personal!=null && personal.length()>0 ) DocumentHelper.makeElement(document,"data/personal").addText(personal);
        if ( news!=null && news.length()>0 ) DocumentHelper.makeElement(document,"data/news").addText(news);
        if ( sex!=null && sex.length()>0 ) DocumentHelper.makeElement(document,"data/sex").addText(sex);
        if ( ads!=null && ads.length()>0 ) DocumentHelper.makeElement(document,"data/ads").addText(ads);
        if ( www!=null && www.length()>13 && www.startsWith("http://") ) DocumentHelper.makeElement(document,"data/www").addText(www);

        user.setName(name);
        user.setEmail(email);
        user.setLogin(login);
        if ( updatePassword ) user.setPassword(password);
        user.setData(document);

        return true;
    }

    /**
     * Prepares for an update of user
     */
    protected Template actionPassword(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        User user = (User) ctx.get(VAR_MANAGED);
        String pass = (String) params.get(PARAM_PASSWORD);
        String pass2 = (String) params.get(PARAM_PASSWORD2);
        boolean error = false;

        if ( pass==null || pass.length()<4 ) {
            addError(PARAM_PASSWORD,"Heslo je pøíli¹ krátké!",ctx, null);
            error = true;
        }
        if ( pass!=null && !(pass.equals(pass2)) ) {
            addError(PARAM_PASSWORD,"Hesla se li¹í!",ctx, null);
            error = true;
        }

        if ( error ) return getTemplate("edit/password.vm");
        user.setPassword(pass);
        PersistanceFactory.getPersistance().update(user);

        addMessage("Heslo bylo zmìnìno.",ctx, request.getSession());

        Cookie[] cookies = request.getCookies();
        for (int i = 0; user==null && cookies!=null && i<cookies.length; i++) {
            Cookie cookie = cookies[i];
            if ( cookie.getName().equals(AbcServlet.VAR_USER) ) {
                deleteCookie(cookie,response);
                break;
            }
        }

        redirect("/Index",response,ctx);
        return null;
    }
}
