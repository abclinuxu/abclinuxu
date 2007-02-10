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
package cz.abclinuxu.servlets.wap;

import cz.abclinuxu.servlets.utils.url.URLMapper;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.FMUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Date;
import java.io.IOException;
import java.io.Writer;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import freemarker.template.Template;
import org.apache.log4j.Logger;

/**
 * This class renders WAP version of portal.
 */
public class WapVersion {
    static Logger log = Logger.getLogger(WapVersion.class);
    static Logger transferLog = Logger.getLogger("wap");
    static DateFormat format = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss ZZZ");

    public static void process(HttpServletRequest request, HttpServletResponse response, Map env) throws IOException {
        try {
            URLMapper urlMapper = URLMapper.getInstance(URLMapper.Version.WAP);
            AbcAction action = urlMapper.findAction(request, response, env);
            String templateName = action.process(request, response, env);
            if ( Misc.empty(templateName) )
                return;

            Template template = FMUtils.getConfiguration().getTemplate(templateName);
            StringWriter tmpWriter = new StringWriter();
            template.process(env, tmpWriter);

            response.setContentType("text/vnd.wap.wml; charset=UTF-8");
            Writer writer = response.getWriter();
            StringBuffer buffer = tmpWriter.getBuffer();
            int responseLength = buffer.length();
            for (int i=0; i<responseLength; i++) {
                writer.write(buffer.charAt(i));
            }
            writer.flush();
            logAccess(request, responseLength);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            log.error("Chyba", e);
        }
    }

//213.151.208.225 - - [23/Apr/2004:10:10:26 +0000] "POST /zpravicky HTTP/1.1" 200 1683 "http://wap.abclinuxu.cz/zpravicky" "Mozilla/1.22 (compatible; MMEF20; Cellphone; Sony CMD-J7/J70)"
    private static void logAccess(HttpServletRequest request, int responseLength) {
        StringBuffer buf = new StringBuffer(160);
        buf.append(request.getRemoteAddr());
        buf.append(" - - [");
        buf.append(format.format(new Date()));
        buf.append("] \"");
        buf.append(request.getMethod());
        buf.append(' ');
        buf.append(request.getRequestURI());
        buf.append(' ');
        buf.append("HTTP/1.1");
//            buf.append(request.getVersion());
        buf.append("\" ");
        buf.append('2');
        buf.append('0');
        buf.append('0'); // status 200
        buf.append(' ');
        buf.append(Integer.toString(responseLength));
        buf.append(' ');
        String referer = request.getHeader("Referer");
        if ( referer==null )
            buf.append("\"-\" ");
        else {
            buf.append('"');
            buf.append(referer);
            buf.append("\" ");
        }

        String agent = request.getHeader("user-agent");
        if ( agent==null )
            buf.append("\"-\"");
        else {
            buf.append('"');
            buf.append(agent);
            buf.append('"');
        }

        transferLog.info(buf.toString());
    }
}
