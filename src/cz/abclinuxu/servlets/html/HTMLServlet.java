/*
 * User: literakl
 * Date: 15.3.2004
 * Time: 21:17:03
 */
package cz.abclinuxu.servlets.html;

import cz.abclinuxu.servlets.Controller;
import cz.abclinuxu.servlets.utils.template.TemplateSelector;
import cz.abclinuxu.servlets.utils.URLMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is to be used as parent of jsp classes. It
 * combines tasks from Controller and HTMLVersion classes.
 */
public class HTMLServlet extends Controller {
    static Logger log = Logger.getLogger(HTMLServlet.class);

    /**
     * Override to add your behaviour.
     * @return name of template to be rendered.
     */
    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        return "";
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Writer writer = null;
        try {
            Map env = new HashMap();
            performInit(request, response, env);

            URLMapper urlMapper = URLMapper.getInstance(URLMapper.Version.HTML);
            HTMLVersion.setLayout(request, urlMapper);

            String templateName = process(request, response, env);
            Template template = Configuration.getDefaultConfiguration().getTemplate(templateName);
            response.setContentType("text/html; charset=ISO-8859-2");
            writer = response.getWriter();

            response.setDateHeader("Last-Modified", new Date().getTime());
            response.setHeader("Expires", "Fri, 22 Dec 2000 05:00:00 GMT");
            response.setHeader("Cache-Control", "no-cache, must-revalidate");
            response.setHeader("Pragma", "no-cache");

            template.process(env, writer);
            writer.flush();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            HTMLVersion.error(request, response, e);
        }
    }
}
