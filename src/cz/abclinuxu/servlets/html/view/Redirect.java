/*
 * User: literakl
 * Date: May 26, 2002
 * Time: 7:09:08 PM
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
                log.warn("Cannot redirect to link "+linkId,e);
            }
        }

        link = new Link();
        link.setUrl("/");
        return link;
    }
}
