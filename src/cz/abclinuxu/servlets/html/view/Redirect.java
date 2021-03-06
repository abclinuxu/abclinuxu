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
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.PersistenceMapping;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.Misc;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Redirect extends HttpServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Redirect.class);

    /** contains id of link */
    public static final String PARAM_URL = "url";
    public static final String PARAM_CLASS = "class";
    public static final String PARAM_ID = "id";

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String url = request.getParameter(PARAM_URL);
        response.sendRedirect(response.encodeRedirectURL(url));

        String paramClass = request.getParameter(PARAM_CLASS);
        if (Misc.empty(paramClass))
            return;
        int id = Misc.parseInt(request.getParameter(PARAM_ID), 0);
        if (id == 0)
            return;

        GenericObject obj = PersistenceMapping.createGenericObject(paramClass.charAt(0), id);
        PersistenceFactory.getPersistence().incrementCounter(obj, Constants.COUNTER_VISIT);
    }
}
