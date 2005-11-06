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

import cz.abclinuxu.data.Link;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.Persistance;

import javax.servlet.http.*;
import javax.servlet.ServletException;
import java.io.IOException;

public class Redirect extends HttpServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Redirect.class);
    static org.apache.log4j.Logger logRedirect = org.apache.log4j.Logger.getLogger("redirect");

    /** contains id of link */
    public static final String PARAM_LINK = "linkId";

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String linkId = request.getParameter(PARAM_LINK);
        Link link = findLink(linkId);
        String url = link.getUrl();
        logRedirect.info(linkId);
        response.sendRedirect(response.encodeRedirectURL(url));
    }

    /**
     * finds link in database, or returns link to /
     */
    Link findLink(String linkId) {
        Persistance persistance = PersistanceFactory.getPersistance();
        Link link = null;

        if ( linkId!=null && linkId.length()>0 ) {
            int id = Integer.parseInt(linkId);
            try {
                link = (Link) persistance.findById(new Link(id));
                return link;
            } catch (Exception e) {
                log.debug("Cannot redirect to link "+linkId,e);
            }
        }

        link = new Link();
        link.setUrl("/");
        return link;
    }
}
