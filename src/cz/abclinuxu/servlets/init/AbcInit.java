/*
 * User: literakl
 * Date: Dec 19, 2001
 * Time: 5:55:35 PM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.servlets.init;

import java.io.IOException;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Category;
import cz.abclinuxu.persistance.PersistanceFactory;

/**
 * This servlet initializes Log4J
 */
public class AbcInit extends HttpServlet {
    static Category log = Category.getInstance(AbcInit.class);

    public void init() throws ServletException {
        String path = getServletContext().getRealPath("/");

        String file = getInitParameter("CONFIG");
        if ( file!=null ) {
            DOMConfigurator.configure(path+file);
        } else {
            BasicConfigurator.configure();
        }

        String url = getInitParameter("URL");
        if ( url!=null ) {
            log.info("Inicializuji vrstvu persistence pomoci URL "+url);
            PersistanceFactory.setDefaultUrl(url);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // this servlet shall be never called
    }
}
