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
package cz.abclinuxu.servlets.misc;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.impl.AbcConfig;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import java.util.prefs.Preferences;
import java.io.IOException;

/**
 * This class sends robots.txt based on current URL.
 * If the URL starts with www, then normal robots.txt
 * is served, otherwise content that forbids everything
 * is returned.
 * @author literakl
 * @since 15.10.2005
 */
public class Robots extends HttpServlet implements Configurable {
    public static final String PREF_WWW_CONTENT = "content.for.www";
    public static final String PREF_OTHER_CONTENT = "content.for.other";

    String wwwContent, otherContent;

    public Robots() {
        ConfigurationManager.getConfigurator().configureMe(this);
    }

    /**
     * Based on URL of the request it chooses AbcAction implementation in HTML format,
     * that fullfills the request.
     */
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = null;
        String server = request.getServerName();
        if (server.equals(AbcConfig.getHostname()))
            dispatcher = request.getRequestDispatcher(wwwContent);
        else
            dispatcher = request.getRequestDispatcher(otherContent);
        dispatcher.forward(request, response);
    }


    public void configure(Preferences prefs) throws ConfigurationException {
        wwwContent = prefs.get(PREF_WWW_CONTENT, "");
        otherContent = prefs.get(PREF_OTHER_CONTENT, "");
    }
}
