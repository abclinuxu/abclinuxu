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
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

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
 * </dl>
 */
public class EditUser extends AbcServlet {
    public static final String PARAM_LOGIN = "login";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_PASSWORD2 = "password2";
    public static final String PARAM_ILIKEQ = "ilikeq";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";

    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        String action = (String) params.get(AbcServlet.PARAM_ACTION);

        if ( action!=null ) {
            if ( action.equals(EditUser.ACTION_ADD) ) {
                return getTemplate("add/user.vm");
            } else if ( action.equals(EditUser.ACTION_ADD_STEP2) ) {
                actionAddStep2(request,response,ctx);
            } else if ( action.equals(EditUser.ACTION_EDIT) ) {
                return getTemplate("edit/user.vm");
            }
        }

        User user = (User) ctx.get(AbcServlet.VAR_USER);
        if ( user==null ) {
            return getTemplate("add/user.vm");
        } else {
            return getTemplate("edit/user.vm");
        }
    }

    /**
     * Creates new category
     */
    protected Template actionAddStep2(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        boolean error = false;

        String login = (String) request.getParameter(EditUser.PARAM_LOGIN);
        if ( login==null || login.length()<4 ) {
            addErrorMessage(PARAM_LOGIN,"Zadane prihlasovaci jmeno je prilis kratke!",ctx);
            error = true;
        }

        String name = (String) request.getParameter(EditUser.PARAM_NAME);
        if ( name==null || name.length()<5 ) {
            addErrorMessage(PARAM_NAME,"Zadane jmeno je prilis kratke!",ctx);
            error = true;
        }

        String password = (String) request.getParameter(EditUser.PARAM_PASSWORD);
        if ( password==null || password.length()<4 ) {
            addErrorMessage(PARAM_PASSWORD,"Heslo je prilis kratke!",ctx);
            error = true;
        }
        if ( password!=null && !(password.equals((String) request.getParameter(EditUser.PARAM_PASSWORD2))) ) {
            addErrorMessage(PARAM_PASSWORD,"Hesla se lisi!",ctx);
            error = true;
        }

        String email = (String) request.getParameter(EditUser.PARAM_EMAIL);
        if ( email==null || email.length()<6 || email.indexOf('@')==-1 ) {
            addErrorMessage(PARAM_EMAIL,"Neplatny email!",ctx);
            error = true;
        }

        String ilikeq = (String) request.getParameter(EditUser.PARAM_ILIKEQ);

        if ( error ) return getTemplate("add/user.vm");

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        if ( ilikeq!=null && ilikeq.length()>0 ) root.addElement("ilikeq").addText(ilikeq);

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setLogin(login);
        user.setPassword(password);
        user.setData(document);

        try {
            PersistanceFactory.getPersistance().create(user);
        } catch ( PersistanceException e ) {
            if ( e.getStatus()==AbcException.DB_DUPLICATE ) {
                addErrorMessage(PARAM_NAME,"Zadane prihlasovaci jmeno je prilis kratke!",ctx);
                return getTemplate("add/user.vm");
            }
        }

        addMessage("Dekujeme vam za registraci.",ctx);
        return getTemplate("add/user.vm"); // zmen to na thanks page
    }
}
