/*
 * User: literakl
 * Date: Feb 1, 2002
 * Time: 7:45:01 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.utils.InstanceUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class ForgottenPassword extends AbcFMServlet {

    public static final String PARAM_LOGIN = "login";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_USER_ID = "userId";

    public static final String VAR_USERS = "USERS";

    public static final String ACTION_CHOOSE = "choose";
    public static final String ACTION_SEND = "send";

    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);

        if ( ACTION_CHOOSE.equals(action) ) {
            return actionChoose(request,env);
        } else if ( ACTION_SEND.equals(action) ) {
            return actionSend(request,response,env);
        }
        return FMTemplateSelector.select("ForgottenPassword","step1",env,request);
    }

    protected String actionChoose(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        User user = new User();
        List list = new ArrayList(1);
        list.add(user);

        String name = (String) params.get(PARAM_NAME);
        String login = (String) params.get(PARAM_LOGIN);

        if ( name!=null ) name = name.trim(); else name = "";
        if ( login!=null ) login = login.trim(); else login = "";

        if ( name.length()<3 && login.length()<3 ) {
            if ( name.length()<3 ) ServletUtils.addError(PARAM_NAME,"Zadejte nejm�n� t�i p�smena!",env,null);
            if ( login.length()<3 ) ServletUtils.addError(PARAM_LOGIN,"Zadejte nejm�n� t�i p�smena!",env,null);
            return FMTemplateSelector.select("ForgottenPassword","step1",env,request);
        }

        if ( name.length()>2 ) user.setName("%"+name+"%");
        if ( login.length()>2 ) user.setLogin("%"+login+"%");

        List found = persistance.findByExample(list,null);
        if ( found.size()==0 ) {
            ServletUtils.addMessage("Nenalezen ��dn� u�ivatel!",env,null);
            return FMTemplateSelector.select("ForgottenPassword","step1",env,request);
        } else {
            List result = new ArrayList(found.size());
            for (Iterator iter = found.iterator(); iter.hasNext();) {
                result.add(persistance.findById((GenericObject) iter.next()));
            }
            env.put(VAR_USERS,result);
        }

        return FMTemplateSelector.select("ForgottenPassword","step2",env,request);
    }

    protected String actionSend(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) InstanceUtils.instantiateParam(PARAM_USER_ID,User.class,params);
        PersistanceFactory.getPersistance().synchronize(user);

        VelocityContext tmpContext = new VelocityContext();
        tmpContext.put(Constants.VAR_USER,user);
        String message = VelocityHelper.mergeTemplate("mail/password.vm",tmpContext);
        Email.sendEmail("admin@abclinuxu.cz",user.getEmail(),"Zapomenute heslo",message);

        ServletUtils.addMessage("Heslo odeslano na adresu "+user.getEmail(),env,request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Index");
        return null;
    }
}
