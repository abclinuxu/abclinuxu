/*
 * User: Leos Literak
 * Date: May 27, 2003
 * Time: 7:55:28 AM
 */
package cz.abclinuxu.servlets.admin;

import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.servlets.init.AbcInit;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * When called, clears content of cache of default persistance.
 */
public class ClearCacheServlet extends HttpServlet {

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String result = null;

        try {
            Persistance persistance = PersistanceFactory.getPersistance();
            persistance.clearCache();
            AbcInit.setSharedVariables();
            result = "OK";
        } catch (Exception e) {
            result = "Failed";
        }

        PrintWriter writer = response.getWriter();
        writer.print("<h1>"+result+"</h1>");
        writer.flush();
        writer.close();
    }
}
