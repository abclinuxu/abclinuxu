/*
 * User: literakl
 * Date: 3.12.2002
 * Time: 20:09:26
 */
package cz.abclinuxu.servlets.utils;

import org.apache.velocity.servlet.VelocityServlet;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * It just finds file, which is being processed
 * and merges it with default context.
 */
public class DumpVelocityServlet extends VelocityServlet {

    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx)
            throws Exception {
        Template t = null;
        String path = request.getRequestURI();
        return getTemplate(path);
    }
}
