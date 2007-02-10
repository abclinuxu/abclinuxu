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
package cz.abclinuxu.servlets.utils.template;

import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.exceptions.NotFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.List;
import java.util.Iterator;

/**
 * This template selector is for FreeMarker template engine.
 */
public class FMTemplateSelector extends TemplateSelector {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FMTemplateSelector.class);

    /**
     * Selects page to be processed and template to decorate it. The page is defined as combination
     * of servlet and action in the configuration file. The template, which decorates page, is
     * chosen by browser or taken from users profile.
     * @param servlet name of servlet
     * @param action name of action
     * @param data map, where method can store variables like browser
     * @param request used for browser identification
     * @return page to be processed
     * @throws NotFoundException when such combination of servlet and action doesn't exist
     */
    public static String select(String servlet, String action, Map data, HttpServletRequest request) {
        ServletAction servletAction = (ServletAction) mappings.get(servlet + action);
        if ( servletAction==null ) {
            log.warn("Neexistuje šablona pro kombinaci "+servlet +","+ action);
            throw new NotFoundException("Neexistuje šablona pro kombinaci "+servlet +","+ action);
        }

        String browser = findBrowser(request);
        String template = selectTemplate(servletAction,browser,request);
        String page = servletAction.getContent();
        storeVariables(data,servletAction.getVariables());

        StringBuffer sb = new StringBuffer("/");
        sb.append(template);
        sb.append(page);
        return sb.toString();
    }

    /**
     * Selects page to be processed. The page is defined as combination
     * of servlet and action in the configuration file. The decorator - template, is
     * given as parameter.
     * @param servlet name of servlet
     * @param action name of action
     * @param data map, where method can store variables like browser
     * @return page to be processed
     * @throws NotFoundException when such combination of servlet and action doesn't exist
     */
    public static String select(String servlet, String action, Map data, String template) {
        ServletAction servletAction = (ServletAction) mappings.get(servlet + action);
        if ( servletAction==null ) {
            log.warn("Neexistuje šablona pro kombinaci "+servlet+","+action);
            throw new NotFoundException("Neexistuje šablona pro kombinaci "+servlet +","+ action);
        }

        String page = servletAction.getContent();
        storeVariables(data,servletAction.getVariables());

        StringBuffer sb = new StringBuffer("/");
        sb.append(template);
        sb.append(page);
        return sb.toString();
    }
    /**
     * Selects template to decorate given page. The template, which decorates page, is
     * chosen by browser or taken from users profile.
     * @param data map, where method can store variables like browser
     * @param request used for browser identification
     * @return page to be processed
     */
    public static String select(String page, Map data, HttpServletRequest request) {
        String browser = findBrowser(request);
        String template = selectTemplate(null, browser, request);

        StringBuffer sb = new StringBuffer("/");
        sb.append(template);
        sb.append(page);
        return sb.toString();
    }

    /**
     * Stores list of Variable into map. It also evaluates LazyVar before storing it as String.
     */
    static void storeVariables(Map data, List variables) {
        if ( variables==null ) return;
        for (Iterator iter = variables.iterator(); iter.hasNext();) {
            Variable variable = (Variable) iter.next();
            Object value = variable.getValue();
            if ( value instanceof LazyVar ) {
                String code = ((LazyVar)value).getValue();
                try {
                    data.put(variable.getName(),FMUtils.executeCode(code,data));
                } catch (Exception e) {
                    log.error("Cannot evaluate lazy variable "+variable.getName()+"["+code+"]!", e);
                }
            } else {
                data.put(variable.getName(),value);
            }
        }
    }

    /**
     * Verifies that layout identified by argument exists.
     * @param layout name of layout
     * @return whether it exists
     */
    public static boolean layoutExists(String layout) {
        if ("lynx".equalsIgnoreCase(layout))
            return true;
        if ("pda".equalsIgnoreCase(layout))
            return true;
        if ("print".equalsIgnoreCase(layout))
            return true;
        if ("wap".equalsIgnoreCase(layout))
            return true;
        if ("web".equalsIgnoreCase(layout))
            return true;
        return false;
    }
}
