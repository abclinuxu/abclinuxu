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
package cz.abclinuxu.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

/**
 * This class is dedicated to testing request encoding.
 */
public class CharsetServlet extends HttpServlet {
/*
web.xml
    <servlet>
        <servlet-name>charset</servlet-name>
        <servlet-class>cz.abclinuxu.servlets.CharsetServlet</servlet-class>
        <load-on-startup>3</load-on-startup>
    </servlet>
    <servlet-mapping>
        <servlet-name>charset</servlet-name>
        <url-pattern>/charset</url-pattern>
    </servlet-mapping>
*/
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("ISO-8859-1");
        String tmp = request.getParameter("note");
//        String tmp = "křížáček";
        String tmp2 = new String(tmp.getBytes(),"ISO-8859-2");
        String tmp3 = new String(tmp.getBytes(),"ISO-8859-1");
        String tmp4 = new String(tmp.getBytes(),"UTF-8");
        String tmp5 = new String(tmp.getBytes());
        String tmp6 = new String(tmp.getBytes("ISO-8859-2"));
        String tmp7 = new String(tmp.getBytes("ISO-8859-1")); // correct for request locale cs
        String tmp8 = new String(tmp.getBytes("UTF-8"));

        Locale locale = Locale.getDefault();
        System.out.println("default locale = " + locale); // cs_Cz
        locale = request.getLocale();
        System.out.println("request locale = " + locale); // cs !!

        response.setContentType("text/html; charset=ISO-8859-2");
        Writer w = response.getWriter();
        w.write("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-2\"></head><body>");
        w.write("parameter has value: "+tmp+"<br>");
        w.write("<form action=\"\" method=post>");
        w.write("<input type=\"text\" name=\"LOGIN\" size=\"8\">");
        w.write("<input type=\"submit\">");
        w.write("</form>");
        w.write("</body></html>");
    }

    public static void main(String[] args) throws Exception {
        CharsetServlet servlet = new CharsetServlet();
        servlet.service(null,null);
    }
}
