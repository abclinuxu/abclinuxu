/*
 * User: Leos Literak
 * Date: Jan 4, 2003
 * Time: 9:47:03 AM
 */
package cz.abclinuxu.servlets.utils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import cz.abclinuxu.utils.Misc;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

/**
 * Simple servlet, which renders file given in URI.
 * If the URI doesn't have suffix html, the file
 * is rendered with default template.
 */
public class TemplateEngineServlet extends HttpServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TemplateEngineServlet.class);

    /** freemarker's main class */
    private Configuration config;

    /**
     * request is processed here
     */
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=ISO-8859-2");
        Writer writer = response.getWriter();
        HashMap root = new HashMap();
        String path = request.getRequestURI();
        Template template = config.getTemplate(path);
        try {
            template.process(root,writer);
        } catch (TemplateException e) {
            log.error("Cannot process file "+path,e);
        }
        writer.flush();
    }

    /**
     * Servlet initialization
     */
    public void init() throws ServletException {
        config = Configuration.getDefaultConfiguration();
        config.setDefaultEncoding("iso-8859-2");

        String path = getServletContext().getRealPath("/")+"/";
        String tmp = getInitParameter("FREEMARKER");
        try {
            if ( ! Misc.empty(tmp) )
                config.setDirectoryForTemplateLoading(new File(path,tmp));
            else
                config.setDirectoryForTemplateLoading(new File(path));
        } catch (IOException e) {
            String cesta = path + ((tmp!=null)? tmp:"");
            log.error("Nemohu nastavit cestu k sablonam na "+cesta+"!",e);
        }
    }

}
