/*
 * User: literakl
 * Date: Jan 10, 2002
 * Time: 5:07:04 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.AbcException;
import cz.abclinuxu.data.User;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceException;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.view.ViewUser;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.Tools;
import cz.abclinuxu.utils.email.EmailSender;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class for manipulation with User.
 */
public class EditUser extends AbcFMServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditUser.class);

    public static final String PARAM_USER = ViewUser.PARAM_USER;
    public static final String PARAM_LOGIN = "login";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_PASSWORD2 = "password2";
    public static final String PARAM_SEX = "sex";
    public static final String PARAM_CITY = "city";
    public static final String PARAM_AREA = "area";
    public static final String PARAM_COUNTRY = "country";
    public static final String PARAM_BIRTH_YEAR = "birth";
    public static final String PARAM_HOME_PAGE = "www";
    public static final String PARAM_LINUX_USER_FROM = "linuxFrom";
    public static final String PARAM_DISTRIBUTION = "distribution";
    public static final String PARAM_ABOUT_ME = "about";
    public static final String PARAM_EMOTICONS = "emoticons";
    public static final String PARAM_COOKIE_VALIDITY = "cookieValid";
    public static final String PARAM_SUBSCRIBE_MONTHLY = "monthly";
    public static final String PARAM_SUBSCRIBE_WEEKLY = "weekly";

    public static final String VAR_MANAGED = "MANAGED";

    public static final String ACTION_REGISTER = "register";
    public static final String ACTION_REGISTER_STEP2 = "register2";
    public static final String ACTION_EDIT_BASIC = "editBasic";
    public static final String ACTION_EDIT_BASIC_STEP2 = "editBasic2";
    public static final String ACTION_CHANGE_PASSWORD = "changePassword";
    public static final String ACTION_CHANGE_PASSWORD_STEP2 = "changePassword2";
    public static final String ACTION_EDIT_PERSONAL = "editPersonal";
    public static final String ACTION_EDIT_PERSONAL_STEP2 = "editPersonal2";
    public static final String ACTION_EDIT_PROFILE = "editProfile";
    public static final String ACTION_EDIT_PROFILE_STEP2 = "editProfile2";
    public static final String ACTION_UPLOAD_PHOTO = "uploadPhoto";
    public static final String ACTION_UPLOAD_PHOTO_STEP2 = "uploadPhoto2";
    public static final String ACTION_EDIT_SETTINGS = "editSettings";
    public static final String ACTION_EDIT_SETTINGS_STEP2 = "editSettings2";
    public static final String ACTION_EDIT_SUBSCRIPTION = "subscribe";
    public static final String ACTION_EDIT_SUBSCRIPTION_STEP2 = "subscribe2";


    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);

        User managed = (User) InstanceUtils.instantiateParam(PARAM_USER,User.class,params);
        User user = (User) env.get(Constants.VAR_USER);
        if ( managed==null ) managed = user;
        if ( managed!=null ) {
            PersistanceFactory.getPersistance().synchronize(managed);
            env.put(VAR_MANAGED, managed);
        }

        // registration doesn't require user to be logged in
        if (  action==null || action.equals(ACTION_REGISTER) ) {
            return FMTemplateSelector.select("EditUser","register",env,request);

        } else if ( action.equals(ACTION_REGISTER_STEP2) ) {
            return actionAddStep2(request,response,env);

        }

        // all other actions require user to be logged in and to have rights for this action
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if ( ! (user.getId()==managed.getId() || user.isAdmin()) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);


        if ( action.equals(ACTION_EDIT_BASIC) ) {
            return actionEditBasic(request, env);

        } else if ( action.equals(ACTION_EDIT_BASIC_STEP2) ) {
            return actionEditBasic2(request, response, env);

        } else if ( action.equals(ACTION_CHANGE_PASSWORD) ) {
            return FMTemplateSelector.select("EditUser", "changePassword", env, request);

        } else if ( action.equals(ACTION_CHANGE_PASSWORD_STEP2) ) {
            return actionPassword2(request, response, env);

        } else if ( action.equals(ACTION_EDIT_PERSONAL) ) {
            return actionEditPersonal(request, env);

        } else if ( action.equals(ACTION_EDIT_PERSONAL_STEP2) ) {
            return actionEditPersonal2(request, response, env);

        } else if ( action.equals(ACTION_EDIT_PROFILE) ) {
            return actionEditProfile(request, env);

        } else if ( action.equals(ACTION_EDIT_PROFILE_STEP2) ) {
            return actionEditProfile2(request, response, env);

        } else if ( action.equals(ACTION_EDIT_SETTINGS) ) {
            return actionEditSettings(request, env);

        } else if ( action.equals(ACTION_EDIT_SETTINGS_STEP2) ) {
            return actionEditSettings2(request, response, env);

        } else if ( action.equals(ACTION_EDIT_SUBSCRIPTION) ) {
            return actionEditSubscription(request, env);

        } else if ( action.equals(ACTION_EDIT_SUBSCRIPTION_STEP2) ) {
            return actionEditSubscription2(request, response, env);

        }
        return null;
    }

    /**
     * Creates new user.
     */
    protected String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = new User();
        Persistance persistance = PersistanceFactory.getPersistance();

        Document document = DocumentHelper.createDocument();
        DocumentHelper.makeElement(document, "/data/settings");
        DocumentHelper.makeElement(document, "/data/system");
        Element email = DocumentHelper.makeElement(document, "/data/communication/email");
        email.addAttribute("valid", "yes");
        managed.setData(document);

        boolean canContinue = true;
        canContinue &= setLogin(params, managed, env);
        canContinue &= setPassword(params, managed, env);
        canContinue &= setName(params, managed, env);
        canContinue &= setEmail(params, managed, env);
        canContinue &= setSex(params, managed, env);
        canContinue &= setWeeklySummary(params, managed);
        canContinue &= setMonthlySummary(params, managed);

        if ( !canContinue )
            return FMTemplateSelector.select("EditUser", "register", env, request);

        try {
            persistance.create(managed);
        } catch (PersistanceException e) {
            if ( e.getStatus()==AbcException.DB_DUPLICATE ) {
                ServletUtils.addError(PARAM_LOGIN, "Toto jm�no je ji� pou��v�no!", env, null);
            }
            return FMTemplateSelector.select("EditUser", "register", env, request);
        }

        HttpSession session = request.getSession();
        session.setAttribute(Constants.VAR_USER, managed);

        Map data = new HashMap();
        data.put(Constants.VAR_USER, managed);
        data.put(EmailSender.KEY_FROM, "admin@abclinuxu.cz");
        data.put(EmailSender.KEY_TO, managed.getEmail());
        data.put(EmailSender.KEY_SUBJECT, "Privitani na portalu www.abclinuxu.cz");
        data.put(EmailSender.KEY_TEMPLATE, "/mail/registrace.ftl");
        EmailSender.sendEmail(data);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Profile?registrace=true&action="+ViewUser.ACTION_SHOW_MY_PROFILE+"&userId="+managed.getId());
        return null;
    }

    /**
     * Shows form for editing of basic information about account.
     */
    protected String actionEditBasic(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);

        params.put(PARAM_LOGIN,managed.getLogin());
        params.put(PARAM_NAME,managed.getName());
        params.put(PARAM_EMAIL,managed.getEmail());

        return FMTemplateSelector.select("EditUser","editBasic",env,request);
    }

    /**
     * Updates basic information about account.
     */
    protected String actionEditBasic2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);
        User user = (User) env.get(Constants.VAR_USER);
        Persistance persistance = PersistanceFactory.getPersistance();

        boolean canContinue = true;
        if ( !user.isAdmin() )
            canContinue &= checkPassword(params, managed, env);
        canContinue &= setLogin(params, managed, env);
        canContinue &= setName(params, managed, env);
        canContinue &= setEmail(params, managed, env);

        if ( !canContinue )
            return FMTemplateSelector.select("EditUser", "editBasic", env, request);

        try {
            persistance.update(managed);
        } catch ( PersistanceException e ) {
            if ( e.getStatus()==AbcException.DB_DUPLICATE ) {
                ServletUtils.addError(PARAM_LOGIN,"Toto jm�no je ji� pou��v�no!",env, null);
            }
            return FMTemplateSelector.select("EditUser","editBasic",env,request);
        }

        User sessionUser = (User) env.get(Constants.VAR_USER);
        if ( managed.getId()==sessionUser.getId() ) {
            sessionUser.synchronizeWith(managed);
        }

        ServletUtils.addMessage("Zm�ny byly ulo�eny.",env, request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Profile?action="+ViewUser.ACTION_SHOW_MY_PROFILE+"&userId="+managed.getId());
        return null;
    }

    /**
     * Changes password of the user.
     */
    protected String actionPassword2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);
        User user = (User) env.get(Constants.VAR_USER);
        Persistance persistance = PersistanceFactory.getPersistance();

        boolean canContinue = true;
        if ( !user.isAdmin() )
            canContinue &= checkPassword(params, managed, env);
        canContinue &= setPassword(params,managed,env);

        if ( ! canContinue )
            return FMTemplateSelector.select("EditUser", "changePassword", env, request);

        persistance.update(managed);

        Cookie cookie = ServletUtils.getCookie(request, Constants.VAR_USER);
        if ( cookie!=null )
            ServletUtils.deleteCookie(cookie, response);

        ServletUtils.addMessage("Heslo bylo zm�n�no.", env, request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Profile?action="+ViewUser.ACTION_SHOW_MY_PROFILE+"&userId="+managed.getId());
        return null;
    }

    /**
     * Shows form for editing of basic information about account.
     */
    protected String actionEditPersonal(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);

        Document document = managed.getData();
        Node node = document.selectSingleNode("/data/personal/sex");
        if ( node!=null )
            params.put(PARAM_SEX, node.getText());
        node = document.selectSingleNode("/data/personal/birth_year");
        if ( node!=null )
            params.put(PARAM_BIRTH_YEAR, node.getText());
        node = document.selectSingleNode("/data/personal/city");
        if ( node!=null )
            params.put(PARAM_CITY, node.getText());
        node = document.selectSingleNode("/data/personal/area");
        if ( node!=null )
            params.put(PARAM_AREA, node.getText());
        node = document.selectSingleNode("/data/personal/country");
        if ( node!=null )
            params.put(PARAM_COUNTRY, node.getText());

        return FMTemplateSelector.select("EditUser", "editPersonal", env, request);
    }

    /**
     * Updates basic information about account.
     */
    protected String actionEditPersonal2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);
        User user = (User) env.get(Constants.VAR_USER);
        Persistance persistance = PersistanceFactory.getPersistance();

        boolean canContinue = true;
        if ( !user.isAdmin() )
            canContinue &= checkPassword(params, managed, env);
        canContinue &= setSex(params, managed, env);
        canContinue &= setBirthYear(params, managed, env);
        canContinue &= setCity(params, managed);
        canContinue &= setArea(params, managed);
        canContinue &= setCountry(params, managed);

        if ( !canContinue )
            return FMTemplateSelector.select("EditUser", "editPersonal", env, request);

        persistance.update(managed);

        ServletUtils.addMessage("Zm�ny byly ulo�eny.", env, request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Profile?userId="+managed.getId());
        return null;
    }

    /**
     * Shows form for editing of basic information about account.
     */
    protected String actionEditProfile(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);

        Document document = managed.getData();
        Node node = document.selectSingleNode("/data/profile/home_page");
        if ( node!=null )
            params.put(PARAM_HOME_PAGE, node.getText());
        node = document.selectSingleNode("/data/profile/linux_user_from_year");
        if ( node!=null )
            params.put(PARAM_LINUX_USER_FROM, node.getText());
        node = document.selectSingleNode("/data/profile/about_myself");
        if ( node!=null )
            params.put(PARAM_ABOUT_ME, node.getText());
        List nodes = Tools.xpaths(document,"/data/profile/distributions/distribution");
        if ( nodes.size()>0 )
            params.put(PARAM_DISTRIBUTION, nodes);

        return FMTemplateSelector.select("EditUser", "editProfile", env, request);
    }

    /**
     * Updates basic information about account.
     */
    protected String actionEditProfile2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);
        User user = (User) env.get(Constants.VAR_USER);
        Persistance persistance = PersistanceFactory.getPersistance();

        boolean canContinue = true;
        if ( !user.isAdmin() )
            canContinue &= checkPassword(params, managed, env);
        canContinue &= setMyPage(params, managed, env);
        canContinue &= setLinuxUserFrom(params, managed);
        canContinue &= setAbout(params, managed);
        canContinue &= setDistributions(params, managed);

        if ( !canContinue )
            return FMTemplateSelector.select("EditUser", "editProfile", env, request);

        persistance.update(managed);

        ServletUtils.addMessage("Zm�ny byly ulo�eny.", env, request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Profile?userId="+managed.getId());
        return null;
    }

    /**
     * Shows form for editing of account's settings.
     */
    protected String actionEditSettings(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);

        Document document = managed.getData();
        Node node = document.selectSingleNode("/data/settings/emoticons");
        if ( node!=null )
            params.put(PARAM_EMOTICONS, node.getText());
        node = document.selectSingleNode("/data/settings/cookie_valid");
        if ( node!=null )
            params.put(PARAM_COOKIE_VALIDITY, node.getText());

        return FMTemplateSelector.select("EditUser", "editSettings", env, request);
    }

    /**
     * Updates account's settings.
     */
    protected String actionEditSettings2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);
        User user = (User) env.get(Constants.VAR_USER);
        Persistance persistance = PersistanceFactory.getPersistance();

        boolean canContinue = true;
        if ( !user.isAdmin() )
            canContinue &= checkPassword(params, managed, env);
        canContinue &= setCookieValidity(params, managed);
        canContinue &= setEmoticons(params, managed);

        if ( !canContinue )
            return FMTemplateSelector.select("EditUser", "Settings", env, request);

        persistance.update(managed);

        ServletUtils.addMessage("Zm�ny byly ulo�eny.", env, request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Profile?action="+ViewUser.ACTION_SHOW_MY_PROFILE+"&userId="+managed.getId());
        return null;
    }

    /**
     * Shows form for editing of account's subscription.
     */
    protected String actionEditSubscription(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);

        Document document = managed.getData();
        Node node = document.selectSingleNode("/data/communication/email/weekly_summary");
        if ( node!=null )
            params.put(PARAM_SUBSCRIBE_WEEKLY, node.getText());
        node = document.selectSingleNode("/data/communication/email/newsletter");
        if ( node!=null )
            params.put(PARAM_SUBSCRIBE_MONTHLY, node.getText());

        return FMTemplateSelector.select("EditUser", "editSubscription", env, request);
    }

    /**
     * Updates account's subscription.
     */
    protected String actionEditSubscription2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User managed = (User) env.get(VAR_MANAGED);
        User user = (User) env.get(Constants.VAR_USER);
        Persistance persistance = PersistanceFactory.getPersistance();

        boolean canContinue = true;
        if ( !user.isAdmin() )
            canContinue &= checkPassword(params, managed, env);
        canContinue &= setWeeklySummary(params, managed);
        canContinue &= setMonthlySummary(params, managed);

        if ( !canContinue )
            return FMTemplateSelector.select("EditUser", "editSubscription", env, request);

        persistance.update(managed);

        ServletUtils.addMessage("Zm�ny byly ulo�eny.", env, request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Profile?action="+ViewUser.ACTION_SHOW_MY_PROFILE+"&userId="+managed.getId());
        return null;
    }

    /**
     * Checks password from parameters.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if password is missing or is not correct.
     */
    private boolean checkPassword(Map params, User user, Map env) {
        String password = (String) params.get(ServletUtils.PARAM_LOG_PASSWORD);

        if ( password==null || password.length()==0 ) {
            ServletUtils.addError(ServletUtils.PARAM_LOG_PASSWORD, "Zadejte heslo!", env, null);
            return false;
        }
        if ( ! user.validatePassword(password) ) {
            ServletUtils.addError(ServletUtils.PARAM_LOG_PASSWORD, "Nespr�vn� heslo!", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates password from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setPassword(Map params, User user, Map env) {
        String password = (String) params.get(PARAM_PASSWORD);
        String password2 = (String) params.get(PARAM_PASSWORD2);

        if ( password==null || password.length()<4 ) {
            ServletUtils.addError(PARAM_PASSWORD, "Heslo je p��li� kr�tk�!", env, null);
            return false;
        }
        if ( !(password.equals(password2)) ) {
            ServletUtils.addError(PARAM_PASSWORD, "Hesla se li��!", env, null);
            return false;
        }
        user.setPassword(password);
        return true;
    }

    /**
     * Updates login from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setLogin(Map params, User user, Map env) {
        String login = (String) params.get(PARAM_LOGIN);
        if ( login==null || login.length()<3 ) {
            ServletUtils.addError(PARAM_LOGIN, "P�ihla�ovac� jm�no mus� m�t nejm�n� t�i znaky!", env, null);
            return false;
        }
        user.setLogin(login);
        return true;
    }

    /**
     * Updates name from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setName(Map params, User user, Map env) {
        String name = (String) params.get(PARAM_NAME);
        if ( name==null || name.length()<5 ) {
            ServletUtils.addError(PARAM_NAME, "Jm�no je p��li� kr�tk�!", env, null);
            return false;
        }
        user.setName(name);
        return true;
    }

    /**
     * Updates email from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setEmail(Map params, User user, Map env) {
        String email = (String) params.get(PARAM_EMAIL);
        if ( email==null || email.length()<6 || email.indexOf('@')==-1 ) {
            ServletUtils.addError(PARAM_EMAIL, "Neplatn� email!", env, null);
            return false;
        }
        user.setEmail(email);
        return true;
    }

    /**
     * Updates sex from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setSex(Map params, User user, Map env) {
        String sex = (String) params.get(PARAM_SEX);
        if ( !("man".equals(sex) || "woman".equals(sex)) ) {
            ServletUtils.addError(PARAM_SEX, "Zadejte sv� pohlav�!", env, null);
            return false;
        }
        Node node = DocumentHelper.makeElement(user.getData(),"/data/personal/sex");
        node.setText(sex);
        return true;
    }

    /**
     * Updates birth year from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setBirthYear(Map params, User user, Map env) {
        String year = (String) params.get(PARAM_BIRTH_YEAR);
        Element personal = DocumentHelper.makeElement(user.getData(), "/data/personal");
        if ( year==null || year.length()==0 ) {
            Node node = personal.element("birth_year");
            if (node!=null)
                personal.remove(node);
            return true;
        }
        int valid = Misc.parseInt(year, -1);
        if ( valid==-1 ) {
            ServletUtils.addError(PARAM_BIRTH_YEAR, "Zadejte platn� rok!", env, null);
            return false;
        }
        DocumentHelper.makeElement(personal, "birth_year").setText(year);
        return true;
    }

    /**
     * Updates city from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setCity(Map params, User user) {
        String city = (String) params.get(PARAM_CITY);
        Element personal = DocumentHelper.makeElement(user.getData(), "/data/personal");
        if ( city==null || city.length()==0 ) {
            Node node = personal.element("city");
            if (node!=null)
                personal.remove(node);
            return true;
        }
        DocumentHelper.makeElement(personal, "city").setText(city);
        return true;
    }

    /**
     * Updates area from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setArea(Map params, User user) {
        String area = (String) params.get(PARAM_AREA);
        Element personal = DocumentHelper.makeElement(user.getData(), "/data/personal");
        if ( area==null || area.length()==0 ) {
            Node node = personal.element("area");
            if (node!=null)
                personal.remove(node);
            return true;
        }
        DocumentHelper.makeElement(personal, "area").setText(area);
        return true;
    }

    /**
     * Updates country from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setCountry(Map params, User user) {
        String country = (String) params.get(PARAM_COUNTRY);
        Element personal = DocumentHelper.makeElement(user.getData(), "/data/personal");
        if ( country==null || country.length()==0 ) {
            Node node = personal.element("country");
            if (node!=null)
                personal.remove(node);
            return true;
        }
        DocumentHelper.makeElement(personal, "country").setText(country);
        return true;
    }

    /**
     * Updates home page from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setMyPage(Map params, User user, Map env) {
        String page = (String) params.get(PARAM_HOME_PAGE);
        Element profile = DocumentHelper.makeElement(user.getData(), "/data/profile");
        if ( page==null || page.length()==0 ) {
            Node node = profile.element("my_page");
            if (node!=null)
                profile.remove(node);
            return true;
        }
        if ( ! page.startsWith("http://") ) {
            ServletUtils.addError(PARAM_HOME_PAGE, "Neplatn� URL!", env, null);
            return false;
        }
        DocumentHelper.makeElement(profile, "home_page").setText(page);
        return true;
    }

    /**
     * Updates linuxUserFromYear from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setLinuxUserFrom(Map params, User user) {
        String year = (String) params.get(PARAM_LINUX_USER_FROM);
        Element profile = DocumentHelper.makeElement(user.getData(), "/data/profile");
        if ( year==null || year.length()==0 ) {
            Node node = profile.element("linux_user_from_year");
            if (node!=null)
                profile.remove(node);
            return true;
        }
        DocumentHelper.makeElement(profile, "linux_user_from_year").setText(year);
        return true;
    }

    /**
     * Updates about from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setAbout(Map params, User user) {
        String about = (String) params.get(PARAM_ABOUT_ME);
        Element profile = DocumentHelper.makeElement(user.getData(), "/data/profile");
        if ( about==null || about.length()==0 ) {
            Node node = profile.element("about_myself");
            if (node!=null)
                profile.remove(node);
            return true;
        }
        DocumentHelper.makeElement(profile, "about_myself").setText(about);
        return true;
    }

    /**
     * Updates distributions from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setDistributions(Map params, User user) {
        List distros = (List) params.get(PARAM_DISTRIBUTION);
        Element profile = DocumentHelper.makeElement(user.getData(), "/data/profile");
        Node node = profile.element("distributions");
        if ( node!=null )
            profile.remove(node);
        if (distros==null || distros.size()==0)
            return true;
        Element distributions = DocumentHelper.makeElement(profile, "distributions");
        for ( Iterator iter = distros.iterator(); iter.hasNext(); ) {
            String distro = (String) iter.next();
            Element element = DocumentHelper.createElement("distribution");
            element.setText(distro);
            distributions.add(element);
        }
        return true;
    }

    /**
     * Updates emoticons from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setEmoticons(Map params, User user) {
        String emoticons = (String) params.get(PARAM_EMOTICONS);
        Element element = DocumentHelper.makeElement(user.getData(), "/data/settings/emoticons");
        String value = ("yes".equals(emoticons))? "yes":"no";
        element.setText(value);
        return true;
    }

    /**
     * Updates login cookie validity from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setCookieValidity(Map params, User user) {
        String validity = (String) params.get(PARAM_COOKIE_VALIDITY);
        Element element = DocumentHelper.makeElement(user.getData(), "/data/settings/cookie_valid");
        element.setText(validity);
        return true;
    }

    /**
     * Subscribes user to weekly summary from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setWeeklySummary(Map params, User user) {
        String subscription = (String) params.get(PARAM_SUBSCRIBE_WEEKLY);
        Element element = DocumentHelper.makeElement(user.getData(), "/data/communication/email/weekly_summary");
        String value = ("yes".equals(subscription))? "yes":"no";
        element.setText(value);
        return true;
    }

    /**
     * Subscribes user to monthly summary from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param user user to be updated
     * @return false, if there is a major error.
     */
    private boolean setMonthlySummary(Map params, User user) {
        String subscription = (String) params.get(PARAM_SUBSCRIBE_MONTHLY);
        Element element = DocumentHelper.makeElement(user.getData(), "/data/communication/email/newsletter");
        String value = ("yes".equals(subscription))? "yes":"no";
        element.setText(value);
        return true;
    }
}
