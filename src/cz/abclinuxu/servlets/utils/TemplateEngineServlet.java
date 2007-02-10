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
package cz.abclinuxu.servlets.utils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.FMUtils;

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
        response.setContentType("text/html; charset=UTF-8");
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
        config = FMUtils.getConfiguration();
        config.setDefaultEncoding("UTF-8");

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
