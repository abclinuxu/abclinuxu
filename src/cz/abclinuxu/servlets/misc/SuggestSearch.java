/*
 *  Copyright (C) 2006 Leos Literak
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
package cz.abclinuxu.servlets.misc;

import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.FMUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.prefs.Preferences;
import java.util.List;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.Template;
import org.apache.log4j.Logger;


/**
 * Servlet that returns javascript containing most often searched similar queries.
 * @author literakl
 * @since 15.10.2006
 */
public class SuggestSearch extends HttpServlet implements Configurable {
    static Logger log = Logger.getLogger(SuggestSearch.class);

    public static final String PREF_RESULTS_LIMIT = "results.limit";

    public static final String PARAM_FIELD_NAME = "type";
    public static final String PARAM_QUERY = "dotaz";

    public static final String VAR_QUERIES = "QUERIES";

    private static int limit;

    static {
        ConfigurationManager.getConfigurator().configureAndRememberMe(new SuggestSearch());
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        String query = request.getParameter(PARAM_QUERY);
        try {
            List found;
            if (Misc.empty(query))
                found = Collections.EMPTY_LIST;
            else {
//                query = new String(query.getBytes("ISO-8859-1"));

                SQLTool sqlTool = SQLTool.getInstance();
                Qualifier[] qualifiers = new Qualifier[]{new LimitQualifier(0, limit)};
                found = sqlTool.getSearchQueries(query, qualifiers);
            }

            Template template = FMUtils.getConfiguration().getTemplate("/print/ajax/suggest_search.ftl");
            response.setContentType("text/html; charset=UTF-8");
            Writer writer = response.getWriter();

            response.setDateHeader("Last-Modified", new Date().getTime());
            response.setHeader("Expires", "Fri, 22 Dec 2000 05:00:00 GMT");
            response.setHeader("Cache-Control", "no-cache, must-revalidate");
            response.setHeader("Pragma", "no-cache");

            Map env = new HashMap();
            env.put(VAR_QUERIES, found);
            template.process(env, writer);

            if (log.isDebugEnabled()) {
                long end = System.currentTimeMillis();
                log.debug("Processing of '" + query + "' query took " + (end - start) + " ms.");
            }
        } catch (Exception e) {
            log.warn("Processing of '" + query + "' query failed", e);
        }
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        limit = prefs.getInt(PREF_RESULTS_LIMIT, 10);
    }
}
