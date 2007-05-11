/*
 *  Copyright (C) 2007 Leos Literak
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

import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.url.URLMapper;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.exceptions.SecurityException;
import cz.abclinuxu.exceptions.InternalException;
import cz.abclinuxu.data.User;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Collections;
import java.net.URL;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

/**
 * This class is responsible for protection of actions against
 * various attacks. For example that action is really run from
 * the form (POST) on expected page (referer).
 * @author literakl
 * @since 22.4.2007
 */
public class ActionProtector {
    static Logger log = Logger.getLogger(ActionProtector.class);
    static Logger securityLog = Logger.getLogger("security");

    public static final String PARAM_TICKET = "ticket";

    private static URLMapper urlMapper = URLMapper.getInstance(URLMapper.Version.HTML);

    /**
     * Performs several checks to ensure that specified action is called correctly.
     * @param request current HTTP request
     * @param actionClass action that is called
     * @param userRequired whether user must be logged in
     * @param checkReferer referer must point to same controller (multiple step wizards)
     * @param checkPost HTTP POST method is mandatory
     * @param checkTicket when checkPost is false and checkTicker is true, there must be
     * correct ticket param equal to user secret ticker
     * @throws SecurityException constract is not fullfilled
     */
    public static void ensureContract(HttpServletRequest request, Class actionClass, boolean userRequired, boolean checkReferer,
                                      boolean checkPost, boolean checkTicket) {
        try {
            Map env = (Map) request.getAttribute(Constants.VAR_ENVIRONMENT);
            URL referer = ServletUtils.getReferer(request);
            User user = (User) env.get(Constants.VAR_USER);
            if (userRequired && user == null)
                throw new SecurityException("Uživatel není zalogován!");

            if (checkReferer && referer != null) {
                String refererHost = referer.getHost();
                if (! AbcConfig.getHostname().equalsIgnoreCase(refererHost)
                      &&
                    ! refererHost.endsWith("." + AbcConfig.getDomain())) {
                    logEvent(actionClass, "U referera (" + referer + ") nesouhlasí jméno stroje!", request, user);
                    throw new SecurityException("Chybné volání akce!");
                }

                AbcAction action = urlMapper.findAction(referer.getPath(), Collections.singletonMap(Constants.VAR_PARAMS, new HashMap(2)));
                if (action == null) {
                    logEvent(actionClass, "Pro referera (" + referer + ") nebyla nalezena žádná akce!", request, user);
                    throw new SecurityException("Chybné volání akce!");
                }

                if (! action.getClass().equals(actionClass)) {
                    logEvent(actionClass, "U referera (" + referer + ") nesouhlasí třída akce ("
                                          + action.getClass().getName() + ")!", request, user);
                    throw new SecurityException("Chybné volání akce!");
                }
            }

            if (checkPost && ! ServletUtils.isMethodPost(request)) {
                    logEvent(actionClass, "Akce nebyla volána metodou POST!", request, user);
                    throw new SecurityException("Chybné volání akce!");
            }

            if (checkTicket && userRequired) {
                Map params = (Map) env.get(Constants.VAR_PARAMS);
                String ticket = (String) params.get(PARAM_TICKET);
                if ( Misc.empty(ticket)) {
                    logEvent(actionClass, "Ticket je prázdný!", request, user);
                    throw new SecurityException("Chybné volání akce!");
                }

                String userTicket = user.getSingleProperty(Constants.PROPERTY_TICKET);
                if (Misc.empty(userTicket))
                    throw new InternalException("Pro váš účet " + user.getId() +
                                                " není definován ticket. Kontaktujte administrátory.");

                if (! userTicket.equals(ticket)) {
                    logEvent(actionClass, "Ticket pro účet " + user.getId() + " není správný! " +
                                           "Přišlo " + ticket + " místo " + userTicket, request, user);
                    throw new SecurityException("Chybné volání akce!");
                }
            }
        } catch (MalformedURLException e) {
            log.warn("Chyba při zjišťování referera", e);
        }
    }

    private static void logEvent(Class actionClass, String message, HttpServletRequest request, User user) {
        StringBuffer sb = new StringBuffer(message);
        sb.append("\nAction class = ").append(actionClass.getName());
        sb.append("\nRequest uri = ").append(ServletUtils.getURL(request));
        sb.append("\nRemote host = ").append(request.getRemoteHost());
        sb.append(", remote address = ").append(request.getRemoteAddr());
        sb.append(", user = ").append((user == null) ? "null" : Integer.toString(user.getId()));

        Map params = request.getParameterMap();
        if (params.size() > 0)
            sb.append("\nParameters: ");
        for (Iterator iter = params.keySet().iterator(); iter.hasNext();) {
            String param = (String) iter.next();
            String[] values = (String[]) request.getParameterValues(param);
            for (int i = 0; i < values.length; i++) {
                String value = values[i];
                sb.append(param).append(" = ").append(value).append("\n");
            }
        }
        securityLog.warn(sb.toString());
    }
}
