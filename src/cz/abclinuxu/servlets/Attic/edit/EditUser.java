/*
 * User: literakl
 * Date: Jan 10, 2002
 * Time: 5:07:04 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.data.User;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.PersistanceException;
import cz.abclinuxu.AbcException;
import cz.abclinuxu.servlets.utils.Email;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
 * </dl>
 */
public class EditUser extends AbcServlet {
    public static final String PARAM_LOGIN = "login";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_PASSWORD2 = "password2";
    public static final String PARAM_ILIKEQ = "ilikeq";
    public static final String PARAM_NEWS = "news";
    public static final String PARAM_ADS = "ads";
    public static final String PARAM_SEX = "sex";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";

    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        String action = (String) params.get(AbcServlet.PARAM_ACTION);
        User user = (User) ctx.get(AbcServlet.VAR_USER);

        if ( action!=null ) {
            if ( action.equals(EditUser.ACTION_ADD) ) {
                return getTemplate("add/user.vm");
            } else if ( action.equals(EditUser.ACTION_ADD_STEP2) ) {
                return actionAddStep2(request,response,ctx);
            } else if ( action.equals(EditUser.ACTION_EDIT) ) {
                if ( user==null ) {
                    return getTemplate("login.vm");
                } else {
                    return actionEditStep1(request,ctx);
                }
            } else if ( action.equals(EditUser.ACTION_EDIT_STEP2) ) {
                if ( user==null ) {
                    return getTemplate("login.vm");
                } else {
                    return actionEditStep2(request,response,ctx);
                }
            }
        }

        if ( user==null ) {
            return getTemplate("add/user.vm");
        } else {
            return getTemplate("edit/user.vm");
        }
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
                addErrorMessage(PARAM_LOGIN,"Toto jmeno je jiz pouzivano!",ctx);
            }
            return getTemplate("add/user.vm");
        }

        VelocityContext tmpContext = new VelocityContext();
        tmpContext.put(AbcServlet.VAR_USER,user);
        String message = mergeTemplate("mail/welcome_user.vm",tmpContext);
        Email.sendEmail("admin@AbcLinuxu.cz",user.getEmail(),"Privitani",message);

        // log in user
        return getTemplate("messages/welcome_user.vm");
    }

    /**
     * Prepares for an update of user
     */
    protected Template actionEditStep1(HttpServletRequest request, Context ctx) throws Exception {
        User user = (User) ctx.get(AbcServlet.VAR_USER);
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);

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

        return getTemplate("edit/user.vm");
    }

    /**
     * Updates existing user
     */
    protected Template actionEditStep2(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        User user = (User) ctx.get(AbcServlet.VAR_USER);
        String pass = request.getParameter(EditUser.PARAM_PASSWORD);
        if ( !user.validatePassword(pass) ) {
            addErrorMessage(PARAM_PASSWORD,"Neplatne heslo!",ctx);
            return getTemplate("edit/user.vm");
        }

        if ( !fillUser(request,user,ctx,false) ) return getTemplate("edit/user.vm");
        PersistanceFactory.getPersistance().update(user);

        addMessage("Zmeny byly ulozeny.",ctx);
        redirect("/",response,ctx);
        return null;
    }

    /**
     * Fills selected user with values from request. Common to add and edit.
     * @param updatePassword if true, it attempts to read and update password
     */
    protected boolean fillUser(HttpServletRequest request, User user, Context ctx, boolean updatePassword) throws Exception {
        boolean error = false;

        String login = (String) request.getParameter(EditUser.PARAM_LOGIN);
        String name = (String) request.getParameter(EditUser.PARAM_NAME);
        String password = (String) request.getParameter(EditUser.PARAM_PASSWORD);
        String email = (String) request.getParameter(EditUser.PARAM_EMAIL);

        if ( login==null || login.length()<4 ) {
            addErrorMessage(PARAM_LOGIN,"Zadane prihlasovaci jmeno je prilis kratke!",ctx);
            error = true;
        }
        if ( name==null || name.length()<5 ) {
            addErrorMessage(PARAM_NAME,"Zadane jmeno je prilis kratke!",ctx);
            error = true;
        }
        if ( updatePassword ) {
            if ( password==null || password.length()<4 ) {
                addErrorMessage(PARAM_PASSWORD,"Heslo je prilis kratke!",ctx);
                error = true;
            }
            if ( password!=null && !(password.equals((String) request.getParameter(EditUser.PARAM_PASSWORD2))) ) {
                addErrorMessage(PARAM_PASSWORD,"Hesla se lisi!",ctx);
                error = true;
            }
        }
        if ( email==null || email.length()<6 || email.indexOf('@')==-1 ) {
            addErrorMessage(PARAM_EMAIL,"Neplatny email!",ctx);
            error = true;
        }

        if ( error ) return false;

        String ilikeq = (String) request.getParameter(EditUser.PARAM_ILIKEQ);
        String news = (String) request.getParameter(EditUser.PARAM_NEWS);
        String sex = (String) request.getParameter(EditUser.PARAM_SEX);
        String ads = (String) request.getParameter(EditUser.PARAM_ADS);

        // make it less aggresive. if document exists, just replace these values
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        if ( ilikeq!=null && ilikeq.length()>0 ) root.addElement("ilikeq").addText(ilikeq);
        if ( news!=null && news.length()>0 ) root.addElement("news").addText(news);
        if ( sex!=null && sex.length()>0 ) root.addElement("sex").addText(sex);
        if ( ads!=null && ads.length()>0 ) root.addElement("ads").addText(ads);

        user.setName(name);
        user.setEmail(email);
        user.setLogin(login);
        if ( updatePassword ) user.setPassword(password);
        user.setData(document);

        return true;
    }
}
