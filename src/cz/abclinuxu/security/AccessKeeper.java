/*
 * User: literakl
 * Date: 5.3.2004
 * Time: 7:08:48
 */
package cz.abclinuxu.security;

import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Poll;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.exceptions.AccessDeniedException;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;
import java.util.prefs.Preferences;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * This class keeps access to given object.
 * For example for surveys it forces single
 * access within specified period for one user.
 * todo multiple policies, that can be set per object in configuration
 */
public class AccessKeeper implements Configurable {
    static Logger log = Logger.getLogger(AccessKeeper.class);

    /** how many seconds shall be blocked same IP address with same user agent */
    public static final String PREF_BLOCK_SAME_UA_IP_FOR = "block.same.ua.for";
    /** how many seconds shall be blocked same IP address with different user agent */
    public static final String PREF_BLOCK_SAME_IP_FOR = "block.same.ip.for";

    public static final String PREFIX_POLL = "P";
    public static final String PREFIX_ITEM = "I";

    static AccessKeeper singleton;

    static {
        singleton = new AccessKeeper();
        ConfigurationManager.getConfigurator().configureAndRememberMe(singleton);
    }

    /** contains information, when and from the users has accessed the tracked objects last time */
    protected Map sessionsMap;

    int sameUABlockPeriod, differentUABlockPeriod;


    /**
     * Private constructor.
     */
    private AccessKeeper() {
    }

    /**
     * @return singleton of this class
     */
    public static AccessKeeper getInstance() {
        return singleton;
    }

    /**
     * Checks, whether current request can be processed. For example
     * user may vote once per survey.
     * @param obj object to be guarded
     * @throws AccessDeniedException If access is denied.
     */
    public static void checkAccess(GenericObject obj, HttpServletRequest request, HttpServletResponse response) throws AccessDeniedException {
        singleton.checkAccessInternal(obj, request, response);
    }

    public void checkAccessInternal(GenericObject obj, HttpServletRequest request, HttpServletResponse response) throws AccessDeniedException {
        // check
        String id = getSystemId(obj);
        String remoteAddress = request.getRemoteAddr();
        String browser = request.getHeader("user-agent");
        long now = System.currentTimeMillis();
        HttpSession session = request.getSession();

        if ( session.getAttribute(id)!=null )
            throw new AccessDeniedException("Object has been already used!", false);

        Cookie[] cookies = request.getCookies();
        for ( int i = 0; cookies!=null && i<cookies.length; i++ ) {
            Cookie cookie = cookies[i];
            if ( cookie.getName().equals(id) )
                throw new AccessDeniedException("Object has been already used!", false);
        }

        Map storedSessions = (Map) sessionsMap.get(id);
        if (storedSessions!=null) {
            Session foundSession = (Session) storedSessions.get(remoteAddress);
            if (foundSession!=null) {
                if ( compareUserAgents(browser, foundSession) ) {
                    if ( now<(foundSession.accessed+sameUABlockPeriod) )
                        throw new AccessDeniedException("Object has been already used!", true);
                } else {
                    if ( now<(foundSession.accessed+differentUABlockPeriod) )
                        throw new AccessDeniedException("Object has been already used!", true);
                }
            }
        }

        //store
        session.setAttribute(id, Boolean.TRUE);

        Cookie cookie = new Cookie(id, Boolean.TRUE.toString());
        cookie.setPath("/");
        cookie.setMaxAge(1*30*24*3600); // one month
        response.addCookie(cookie);

        if (storedSessions==null) {
            storedSessions = new HashMap();
            sessionsMap.put(id, storedSessions);
        }

        Session savedSession = new Session(remoteAddress, now, browser);
        storedSessions.put(remoteAddress, savedSession);
    }

    /**
     * Compares, whether browser string is same as user agent in Session.
     * @return true, if they are same.
     */
    private boolean compareUserAgents(String browser, Session foundSession) {
        if (browser==null)
            return foundSession.userAgent==0;
        return browser.hashCode()==foundSession.userAgent;
    }

    /**
     * @return identifier of the object in sessionsMap.
     */
    protected String getSystemId(GenericObject obj) {
        if (obj instanceof Poll)
            return PREFIX_POLL+obj.getId();
        if (obj instanceof Item)
            return PREFIX_ITEM+obj.getId();
        return Integer.toString(obj.getId());
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        sameUABlockPeriod = 1000 * prefs.getInt(PREF_BLOCK_SAME_UA_IP_FOR, 7200);
        differentUABlockPeriod = 1000 * prefs.getInt(PREF_BLOCK_SAME_IP_FOR, 600);
        sessionsMap = Collections.synchronizedMap(new HashMap());
    }

    /** Data holder about one session */
    class Session {
        String ipAddress;
        int userAgent;
        long accessed;

        public Session(String ipAddress, long accessed, String userAgent) {
            this.ipAddress = ipAddress;
            this.accessed = accessed;
            this.userAgent = (userAgent!=null)? userAgent.hashCode() : 0;
        }
    }
}
