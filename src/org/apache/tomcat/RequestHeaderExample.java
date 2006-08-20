package org.apache.tomcat;

/*
* Copyright 2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
/* $Id$
*
*/

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import cz.abclinuxu.utils.freemarker.Tools;

/**
 * Example servlet showing request headers
 *
 * @author James Duncan Davidson <duncan@eng.sun.com>
 */

public class RequestHeaderExample extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<body bgcolor=\"white\">");
        out.println("<head>");

        String title = "Request Header Example";
        out.println("<title>" + title + "</title>");
        out.println("</head>");
        out.println("<body>");

        // all links relative

        // XXX
        // making these absolute till we work out the
        // addition of a PathInfo issue

        out.println("<h3>" + title + "</h3>");
        out.println("<table border=0>");
        Enumeration e = request.getHeaderNames();
        while (e.hasMoreElements()) {
            String headerName = (String) e.nextElement();
            String headerValue = request.getHeader(headerName);
            out.println("<tr><td bgcolor=\"#CCCCCC\">");
            out.println(Tools.encodeSpecial(headerName));
            out.println("</td><td>");
            out.println(Tools.encodeSpecial(headerValue));
            out.println("</td></tr>");
        }
        out.println("</table>");
    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {
        doGet(request, response);
    }
}

