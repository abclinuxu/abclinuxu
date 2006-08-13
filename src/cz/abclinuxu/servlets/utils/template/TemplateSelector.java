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

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.apache.regexp.RESyntaxException;
import org.apache.regexp.RE;
import org.apache.regexp.REProgram;
import org.apache.regexp.RECompiler;

import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import cz.abclinuxu.utils.Misc;

import javax.servlet.http.HttpServletRequest;

/**
 * Super class for all template selectors.
 * Its responsibility is to read configuration.
 * Inherited classes will define contract for specific
 * template engine.
 */
public class TemplateSelector {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TemplateSelector.class);

    /** if not overriden, this template will be used */
    static String DEFAULT_TEMPLATE = "web";

    /** custom selection of variant */
    public static final String PARAM_VARIANTA = "varianta";

    /** this variable holds name of template to be included */
    public static final String VAR_CONTENT = "CONTENT";

    /** lynx broswer */
    public static final String BROWSER_LYNX = "LYNX";
    /** plucker PDA browser */
    public static final String BROWSER_PLUCKER = "PLUCKER";
    /** other browser */
    public static final String BROWSER_UNKNOWN = "OTHER";

    /** regular expressions to match UA */
    static REProgram reLynx, rePlucker;

    static {
        try {
            reLynx = new RECompiler().compile("(Lynx|Links)");
            rePlucker = new RECompiler().compile("(Plucker)|(Windows.?CE)");
        } catch (RESyntaxException e) {
            log.error("Wrong regexp!", e);
        }
    }

    /**
     * Here we store mappings. key is concatenation of servlet name and action, value is map
     * where key is variant and value is templet name.
     */
    static HashMap mappings;

    static String configFile;

    /**
     * Loads configuration and initializes TemplateSelector.
     * @param filename name of configuration file
     */
    public static void initialize(String filename) throws Exception {
        if (filename==null)
            filename = configFile;
        else
            configFile = filename;

        HashMap newMappings = new HashMap(100, 0.95f);

        Document document = new SAXReader().read(filename);
        List tagServlets = document.getRootElement().elements("servlet");

        for (Iterator servletIter = tagServlets.iterator(); servletIter.hasNext();) { // for each servlet
            Element tagServlet = (Element) servletIter.next();
            String servlet = tagServlet.attributeValue("name");
            List tagActions = tagServlet.elements("action");

            for (Iterator actionIter = tagActions.iterator(); actionIter.hasNext();) { // for each action
                Element tagAction = (Element) actionIter.next();
                String action = tagAction.attributeValue("name");
                String content = tagAction.attributeValue("content");
                String forcedTemplate = tagAction.attributeValue("template");

                ServletAction servletAction = new ServletAction(action);
                servletAction.setForcedTemplate(forcedTemplate);
                servletAction.setContent(content);

                List tagVariables = tagAction.elements("var");
                if ( !Misc.empty(tagVariables) ) {
                    List variables = new ArrayList(tagVariables.size());
                    for (Iterator varIter = tagVariables.iterator(); varIter.hasNext();) { // for each var
                        variables.add(createVar((Element) varIter.next()));
                    }
                    servletAction.setVariables(variables);
                }

                String name = servlet + action;
                newMappings.put(name,servletAction);
            }
        }

        mappings = newMappings;
    }

    /**
     * Creates Variable of tag Var.
     */
    static Variable createVar(Element tag) {
        String attribName = tag.attributeValue("name");
        String attribValue = tag.attributeValue("value");
        String attribType = tag.attributeValue("type");

        Object value = null;
        if ( Misc.same(attribType,"Boolean") ) {
            value = Boolean.valueOf(attribValue);
        } else if ( Misc.same(attribType,"Lazy") ) {
            value = new LazyVar(attribValue);
        } else {
            value = attribValue;
        }

        return new Variable(attribName,value);
    }

    /**
     * If servletAction defines forced template, it is returned. Otherwise
     * request is searched for parameter PARAM_VARIANTA. If neither this is set,
     * template is chosen by browser signature.
     * @return template
     */
    static String selectTemplate(ServletAction servletAction, String browser, HttpServletRequest request) {
        if ( servletAction!=null && ! Misc.empty(servletAction.getForcedTemplate()) )
            return servletAction.getForcedTemplate();

        String parameter = request.getParameter(PARAM_VARIANTA);
        if ( ! Misc.empty(parameter) )
            return parameter.toLowerCase();

        parameter = (String) request.getAttribute(PARAM_VARIANTA);
        if ( ! Misc.empty(parameter) )
            return parameter.toLowerCase();

        if ( Misc.same(browser,BROWSER_LYNX) )
            return "lynx";
        if ( Misc.same(browser,BROWSER_PLUCKER) )
            return "plucker";
        return DEFAULT_TEMPLATE;
    }

    /**
     * @return browser, that requests this page.
     */
    static String findBrowser(HttpServletRequest request) {
        if ( request==null ) return BROWSER_UNKNOWN;
        String browser = request.getHeader("user-agent");
        if ( browser==null )
            return BROWSER_UNKNOWN;
        try {
            if ( new RE(reLynx, RE.MATCH_CASEINDEPENDENT).match(browser) )
                return BROWSER_LYNX;
//            if ( new RE(rePlucker, RE.MATCH_CASEINDEPENDENT).match(browser) )
//                return BROWSER_PLUCKER;
        } catch (Exception e) {
            log.warn("Error on parsing User Agent '"+browser+"' ! "+e.getMessage());
        }
        return BROWSER_UNKNOWN;
    }
}
