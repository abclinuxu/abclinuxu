/*
 * User: literakl
 * Date: Feb 1, 2002
 * Time: 7:45:01 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.servlets.utils.VelocityHelper;
import cz.abclinuxu.servlets.utils.Email;
import cz.abclinuxu.data.User;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class ForgottenPassword extends AbcServlet {

    public static final String PARAM_LOGIN = "login";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_USER_ID = "userId";

    public static final String VAR_USERS = "USERS";

    public static final String ACTION_CHOOSE = "choose";
    public static final String ACTION_SEND = "send";

    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        String action = (String) params.get(AbcServlet.PARAM_ACTION);

        if ( ACTION_CHOOSE.equals(action) ) {
            return actionChoose(request,ctx);
        } else if ( ACTION_SEND.equals(action) ) {
            return actionSend(request,response,ctx);
        }
        return getTemplate("view/forgotten.vm");
    }

    protected Template actionChoose(HttpServletRequest request, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        User user = new User();
        List list = new ArrayList(1);
        list.add(user);

        String name = (String) params.get(PARAM_NAME);
        String login = (String) params.get(PARAM_LOGIN);

        if ( name!=null ) name = name.trim(); else name = "";
        if ( login!=null ) login = login.trim(); else login = "";

        if ( name.length()<3 && login.length()<3 ) {
            if ( name.length()<3 ) addError(PARAM_NAME,"Zadejte nejménì tøi písmena!",ctx,null);
            if ( login.length()<3 ) addError(PARAM_LOGIN,"Zadejte nejménì tøi písmena!",ctx,null);
            return getTemplate("view/forgotten.vm");
        }

        if ( name.length()>2 ) user.setName("%"+name+"%");
        if ( login.length()>2 ) user.setLogin("%"+login+"%");

        List found = persistance.findByExample(list,null);
        if ( found.size()==0 ) {
            addMessage("Nenalezen ¾ádný u¾ivatel!",ctx,null);
            return getTemplate("view/forgotten.vm");
        }

        ctx.put(VAR_USERS,found);
        return getTemplate("view/forgotten2.vm");
    }

    protected Template actionSend(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        User user = (User) instantiateParam(PARAM_USER_ID,User.class,params);
        PersistanceFactory.getPersistance().synchronize(user);

        VelocityContext tmpContext = new VelocityContext();
        tmpContext.put(AbcServlet.VAR_USER,user);
        String message = VelocityHelper.mergeTemplate("mail/password.vm",tmpContext);
        Email.sendEmail("admin@AbcLinuxu.cz",user.getEmail(),"Zapomenute heslo",message);

        addMessage("Heslo odeslano na adresu "+user.getEmail(),ctx,request.getSession());
        redirect("/index.html",response,ctx);
        return null;
    }
}
