/*
 * User: literakl
 * Date: Feb 3, 2002
 * Time: 8:22:28 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcVelocityServlet;
import cz.abclinuxu.servlets.utils.VelocityHelper;
import cz.abclinuxu.servlets.utils.VelocityTemplateSelector;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.data.*;
import cz.abclinuxu.utils.InstanceUtils;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Profile of the user
 */
public class ViewUser extends AbcVelocityServlet {
    public static final String PARAM_USER = "userId";
    public static final String PARAM_URL = "url";

    public static final String VAR_PROFILE = "PROFILE";
    public static final String VAR_SW_RECORDS = "SW";
    public static final String VAR_HW_RECORDS = "HW";
    public static final String VAR_ARTICLES = "ARTICLE";

    public static final String ACTION_LOGIN = "login";
    public static final String ACTION_LOGIN2 = "login2";
    public static final String ACTION_SEND_EMAIL = "sendEmail";

    protected String process(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Map params = (Map) request.getAttribute(AbcVelocityServlet.ATTRIB_PARAMS);
        String action = (String) params.get(AbcVelocityServlet.PARAM_ACTION);

        if ( action==null ) {
            return handleProfile(request,ctx);
        }

        if ( action.equals(ACTION_LOGIN) ) {
            return handleLogin(request,ctx);
        } else if ( action.equals(ACTION_LOGIN2) ) {
            return handleLogin2(request,response,ctx);
        } else if ( action.equals(ACTION_SEND_EMAIL) ) {
            return handleEmail(request,response,ctx);
        }

        return handleProfile(request,ctx);
    }

    /**
     * shows profile for selected user
     */
    protected String handleProfile(HttpServletRequest request, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcVelocityServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        VelocityHelper helper = new VelocityHelper();

        User user = (User) InstanceUtils.instantiateParam(PARAM_USER,User.class,params);
        persistance.synchronize(user);
        ctx.put(VAR_PROFILE,user);

        Record record = new Record();
        record.setType(Record.HARDWARE);
        record.setOwner(user.getId());

        List list = new ArrayList(1);
        list.add(record);
        List found = persistance.findByExample(list,null);

        List hw = new ArrayList(found.size());
        Relation rel = new Relation();
        for (Iterator iter = found.iterator(); iter.hasNext();) {
            rel.setChild((GenericObject) iter.next());
            hw.add(persistance.findByExample(rel)[0]);
        }
        helper.sync(hw);
        ctx.put(VAR_HW_RECORDS,hw);

        record.setType(Record.SOFTWARE);
        found = persistance.findByExample(list,null);

        List sw = new ArrayList(found.size());
        for (Iterator iter = found.iterator(); iter.hasNext();) {
            rel.setChild((GenericObject) iter.next());
            sw.add(persistance.findByExample(rel)[0]);
        }
        helper.sync(sw);
        ctx.put(VAR_SW_RECORDS,sw);

        return VelocityTemplateSelector.selectTemplate(request,ctx,"ViewUser","profile");
    }

    /**
     * shows login screen
     */
    protected String handleLogin(HttpServletRequest request, Context ctx) throws Exception {
        return VelocityTemplateSelector.selectTemplate(request,ctx,"EditUser","login");
    }

    /**
     * handle login submit
     * @todo investigate, why after log in there is a login dialog
     */
    protected String handleLogin2(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        if ( ctx.get(VAR_USER)!=null ) {
            Map params = (Map) request.getAttribute(AbcVelocityServlet.ATTRIB_PARAMS);
            String id = new Integer(((User)ctx.get(VAR_USER)).getId()).toString();
            params.put(PARAM_USER,id);
            return handleProfile(request,ctx);
        }
        else
            return VelocityTemplateSelector.selectTemplate(request,ctx,"EditUser","login");
    }

    /**
     * sending email address
     */
    protected String handleEmail(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcVelocityServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) InstanceUtils.instantiateParam(PARAM_USER,User.class,params);
        persistance.synchronize(user);
        String url = "mailto:"+user.getEmail();
        response.sendRedirect(url);
        return null;
    }
}
