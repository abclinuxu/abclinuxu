/*
 * User: literakl
 * Date: 15.3.2004
 * Time: 21:04:00
 */
package cz.abclinuxu.servlets.wap;

import cz.abclinuxu.servlets.utils.URLMapper;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.utils.Misc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.io.IOException;
import java.io.Writer;

import freemarker.template.Template;
import freemarker.template.Configuration;
import org.apache.log4j.Logger;

/**
 * This class renders WAP version of portal.
 */
public class WapVersion {
    static Logger log = Logger.getLogger(WapVersion.class);

    public static void process(HttpServletRequest request, HttpServletResponse response, Map env) throws IOException {
        try {
            URLMapper urlMapper = URLMapper.getInstance(URLMapper.Version.WAP);
            AbcAction action = urlMapper.findAction(request);
            String templateName = action.process(request, response, env);
            if ( Misc.empty(templateName) )
                return;

            Template template = Configuration.getDefaultConfiguration().getTemplate(templateName);
            response.setContentType("text/vnd.wap.wml; charset=ISO-8859-2");
            Writer writer = response.getWriter();
            template.process(env, writer);
            writer.flush();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            log.error("Chyba", e);
        }
    }
}
