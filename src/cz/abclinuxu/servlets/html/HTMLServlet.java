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
package cz.abclinuxu.servlets.html;

import cz.abclinuxu.servlets.Controller;
import cz.abclinuxu.utils.freemarker.FMUtils;
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
        Map env = new HashMap();
        try {
            performInit(request, response, env);
            HTMLVersion.setLayout(request, env);

            String templateName = process(request, response, env);
            Template template = FMUtils.getConfiguration().getTemplate(templateName);
            response.setContentType("text/html; charset=UTF-8");
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
            HTMLVersion.error(request, response, e, env);
        }
    }
}
