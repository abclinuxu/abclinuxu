/*
 *  Copyright (C) 2005 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
package cz.abclinuxu.security;

import cz.abclinuxu.data.*;
import cz.abclinuxu.exceptions.AccessDeniedException;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.servlets.utils.ServletUtils;

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
 * todo create Task, that periodically cleans up expired Sessions
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
     * @param relation child object shall be guarded. It must be synchronized already.
     * @param user user that us performing action. It may be null.
     * @param type type of action
     * @throws AccessDeniedException If access is denied.
     */
    public static void checkAccess(Relation relation, User user, String type, HttpServletRequest request, HttpServletResponse response) throws AccessDeniedException {
        singleton.checkAccessInternal(relation, user, type, request, response);
    }

    public void checkAccessInternal(Relation relation, User user, String type, HttpServletRequest request, HttpServletResponse response) throws AccessDeniedException {
        String id = getSystemId(relation.getChild());

        if (user!=null) {
            SQLTool sqlTool = SQLTool.getInstance();
            if (sqlTool.getUserAction(user.getId(), relation.getId(), type)!=null)
                throw new AccessDeniedException("Object has been already used!", false);
            sqlTool.insertUserAction(user.getId(), relation.getId(), type);
        }

        HttpSession session = request.getSession();
        if ( session.getAttribute(id)!=null )
            throw new AccessDeniedException("Object has been already used!", false);

        Cookie[] cookies = request.getCookies();
        for ( int i = 0; cookies!=null && i<cookies.length; i++ ) {
            Cookie cookie = cookies[i];
            if ( cookie.getName().equals(id) )
                throw new AccessDeniedException("Object has been already used!", false);
        }

        String remoteAddress = ServletUtils.getClientIPAddress(request);
        String browser = request.getHeader("user-agent");
        long now = System.currentTimeMillis();

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
            storedSessions = Collections.synchronizedMap(new HashMap());
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
