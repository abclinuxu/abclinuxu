/*
 * User: literakl
 * Date: Dec 19, 2001
 * Time: 5:55:35 PM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.webwork.init;

import java.io.IOException;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import cz.abclinuxu.persistance.PersistanceFactory;
import org.apache.log4j.Category;

/**
 * This servlet initializes persistance layer
 */
public class PersistanceInit extends HttpServlet {
    static Category log = Category.getInstance(PersistanceInit.class);

    public void init() throws ServletException {
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
