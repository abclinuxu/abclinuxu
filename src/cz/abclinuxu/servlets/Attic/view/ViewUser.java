/*
 * User: literakl
 * Date: Feb 3, 2002
 * Time: 8:22:28 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.data.*;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Tools;
import cz.abclinuxu.utils.email.EmailSender;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Profile of the user
 */
public class ViewUser extends AbcFMServlet {
    public static final String PARAM_USER = "userId";
    public static final String PARAM_URL = "url";

    public static final String VAR_PROFILE = "PROFILE";
    public static final String VAR_SW_RECORDS = "SW";
    public static final String VAR_HW_RECORDS = "HW";
    public static final String VAR_ARTICLES = "ARTICLE";
    public static final String VAR_KOD = "KOD";

    public static final String PARAM_FROM = "from";
    public static final String PARAM_SUBJECT = "subject";
    public static final String PARAM_MESSAGE = "message";

    public static final String ACTION_LOGIN = "login";
    public static final String ACTION_LOGIN2 = "login2";
    public static final String ACTION_SEND_EMAIL = "sendEmail";
    public static final String ACTION_FINISH_SEND_EMAIL = "sendEmail2";

    /**
     * Put your processing here. Return null, if you have redirected browser to another URL.
     * @param env holds all variables, that shall be available in template, when it is being processed.
     * It may also contain VAR_USER and VAR_PARAMS objects.
     * @return name of template to be executed or null
     */
    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);

        if ( action==null ) {
            return handleProfile(request,env);
        }

        if ( action.equals(ACTION_LOGIN) ) {
            return handleLogin(request,env);
        } else if ( action.equals(ACTION_LOGIN2) ) {
            return handleLogin2(request,response,env);
        } else if ( action.equals(ACTION_SEND_EMAIL) ) {
            return handleSendEmail(request,env);
        } else if ( action.equals(ACTION_FINISH_SEND_EMAIL) ) {
            return handleSendEmail2(request,response,env);
        }

        return handleProfile(request,env);
    }

    /**
     * shows profile for selected user
     */
    protected String handleProfile(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        User user = (User) InstanceUtils.instantiateParam(PARAM_USER,User.class,params);
        persistance.synchronize(user);
        env.put(VAR_PROFILE,user);

        User loggedIn = (User) env.get(Constants.VAR_USER);
        if ( ! user.equals(loggedIn) ) // other user is lookig at profile
            return FMTemplateSelector.select("ViewUser","profile",env,request);

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
        Tools.sync(hw);
        env.put(VAR_HW_RECORDS,hw);

        record.setType(Record.SOFTWARE);
        found = persistance.findByExample(list,null);

        List sw = new ArrayList(found.size());
        for (Iterator iter = found.iterator(); iter.hasNext();) {
            rel.setChild((GenericObject) iter.next());
            sw.add(persistance.findByExample(rel)[0]);
        }
        Tools.sync(sw);
        env.put(VAR_SW_RECORDS,sw);

        return FMTemplateSelector.select("ViewUser","myProfile",env,request);
    }

    /**
     * shows login screen
     */
    protected String handleLogin(HttpServletRequest request, Map env) throws Exception {
        return FMTemplateSelector.select("ViewUser","login",env,request);
    }

    /**
     * handle login submit
     * @todo investigate, why after log in there is a login dialog
     */
    protected String handleLogin2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        if ( env.get(Constants.VAR_USER)!=null ) {
            Map params = (Map) env.get(Constants.VAR_PARAMS);
            String id = new Integer(((User)env.get(Constants.VAR_USER)).getId()).toString();
            params.put(PARAM_USER,id);
            return handleProfile(request,env);
        }
        else
            return FMTemplateSelector.select("ViewUser","login",env,request);
    }

    /**
     * shows login screen
     */
    protected String handleSendEmail(HttpServletRequest request, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        if ( user!=null ) {
            Map params = (Map) env.get(Constants.VAR_PARAMS);
            params.put(PARAM_FROM,user.getEmail());
        }

        Integer kod = new Integer(new Random().nextInt(10000));
        request.getSession().setAttribute(VAR_KOD,kod);
        env.put(VAR_KOD,kod);

        return FMTemplateSelector.select("ViewUser","sendEmail",env,request);
    }

    /**
     * shows login screen
     */
    protected String handleSendEmail2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        boolean chyba = false;

        String kod = (String) params.get(VAR_KOD);
        try {
            Integer ulozenyKod = (Integer) request.getSession().getAttribute(VAR_KOD);
            env.put(VAR_KOD,ulozenyKod);
            Integer nalezenyKod = Integer.valueOf(kod);
            if ( ! nalezenyKod.equals(ulozenyKod) ) {
                ServletUtils.addError(VAR_KOD,"Vypl�te spr�vn� k�d!",env,null);
                chyba = true;
            }
        } catch (Exception e) {
            ServletUtils.addError(VAR_KOD,"Vypl�te spr�vn� k�d!",env,null);
            chyba = true;
        }
        String from = (String) params.get(PARAM_FROM);
        if ( from==null || from.length()<6 || from.indexOf('@')==-1 ) {
            ServletUtils.addError(PARAM_FROM,"Zadejte platnou adresu!",env,null);
            chyba = true;
        }
        String subject = (String) params.get(PARAM_SUBJECT);
        if ( subject==null || subject.length()==0 ) {
            ServletUtils.addError(PARAM_SUBJECT,"Zadejte p�edm�t!",env,null);
            chyba = true;
        }
        String message = (String) params.get(PARAM_MESSAGE);
        if ( message==null || message.length()==0 ) {
            ServletUtils.addError(PARAM_MESSAGE,"Zadejte zpr�vu!",env,null);
            chyba = true;
        }
        if ( chyba )
            return FMTemplateSelector.select("ViewUser","sendEmail",env,request);

        User user = (User) InstanceUtils.instantiateParam(PARAM_USER,User.class,params);
        user = (User) persistance.findById(user);

        Map data = new HashMap();
        data.put(EmailSender.KEY_FROM,from);
        data.put(EmailSender.KEY_TO,user.getEmail());
        data.put(EmailSender.KEY_SUBJECT,subject);
        data.put(EmailSender.KEY_BODY,message);
        if ( EmailSender.sendEmail(data) )
            ServletUtils.addMessage("Va�e zpr�va byla odesl�na.",env,null);
        else
            ServletUtils.addMessage("Litujeme, ale do�lo k chyb� p�i odes�l�n� va�i zpr�vy.",env,null);

        return handleProfile(request,env);
    }
}
