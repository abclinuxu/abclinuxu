/*
 * User: Leos Literak
 * Date: Nov 26, 2002
 * Time: 9:16:28 AM
 */
package cz.abclinuxu.servlets.utils.template;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

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

    /** if not overriden, this template will be used */
    static String DEFAULT_TEMPLATE = "web";

    /** custom selection of variant */
    public static final String PARAM_VARIANTA = "varianta";

    /** this variable holds name of template to be included */
    public static final String VAR_CONTENT = "CONTENT";
    /** this variable holds type of browser, which is used by visitor */
    public static final String VAR_BROWSER = "BROWSER";
    /** this variable holds browser's operating system */
    public static final String VAR_OS = "OS";

    /** lynx broswer */
    public static final String BROWSER_LYNX = "LYNX";
    /** mozilla browser */
    public static final String BROWSER_MOZILLA = "MOZILLA";
    /** Internet Explorer browser */
    public static final String BROWSER_EXPLORER = "EXPLORER";
    /** forbidden mirroring tool like wget or Custo */
    public static final String BROWSER_MIRROR = "MIRROR";
    /** plucker PDA browser */
    public static final String BROWSER_PLUCKER = "PLUCKER";
    /** other browser */
    public static final String BROWSER_UNKNOWN = "OTHER";

    /** OS Linux */
    public static final String OS_LINUX = "LINUX";
    /** OS Windows */
    public static final String OS_WINDOWS = "WINDOWS";
    /** OS Unknown */
    public static final String OS_UNKNOWN = "UNKNOWN";

    /**
     * Here we store mappings. key is concatenation of servlet name and action, value is map
     * where key is variant and value is templet name.
     */
    static HashMap mappings = new HashMap(75,0.8f);

    /**
     * Loads configuration and initializes TemplateSelector.
     * @param filename name of configuration file
     */
    public static void initialize(String filename) throws Exception {
        Document document = new SAXReader().read(filename);
        List tagServlets = document.getRootElement().elements("servlet");

        for (Iterator servletIter = tagServlets.iterator(); servletIter.hasNext();) { // for each servlet
            Element tagServlet = (Element) servletIter.next();
            String servlet = tagServlet.attributeValue("name");
            List tagActions = tagServlet.elements("action");

            for (Iterator actionIter = tagActions.iterator(); actionIter.hasNext();) { // for each action
                Element tagAction = (Element) actionIter.next();
                String action = tagAction.attributeValue("name");
                List tagMappings = tagAction.elements("mapping");
                ServletAction servletAction = new ServletAction(tagMappings.size());
                servletAction.setForcedTemplate(tagAction.attributeValue("template"));

                for (Iterator mappingIter = tagMappings.iterator(); mappingIter.hasNext();) { // for each mapping
                    Element tagMapping = (Element) mappingIter.next();
                    String template = tagMapping.attributeValue("template");
                    String content = tagMapping.attributeValue("content");
                    List variables = null;

                    List tagVariables = tagMapping.elements("var");
                    if ( !Misc.empty(tagVariables) ) {
                        variables = new ArrayList(tagVariables.size());
                        for (Iterator varIter = tagVariables.iterator(); varIter.hasNext();) { // for each var
                            variables.add(createVar((Element) varIter.next()));
                        }
                    }
                    servletAction.addMapping(template,content,variables);
                }

                String name = servlet + action;
                mappings.put(name,servletAction);
            }
        }
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
        if ( ! Misc.empty(servletAction.getForcedTemplate()) )
            return servletAction.getForcedTemplate();

        if ( ! Misc.empty(request.getParameter(PARAM_VARIANTA)) )
            return request.getParameter(PARAM_VARIANTA);

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
        if ( FMTemplateSelector.reExplorer.match(browser) )
            return BROWSER_EXPLORER;
        if ( FMTemplateSelector.reLynx.match(browser) )
            return BROWSER_LYNX;
        if ( FMTemplateSelector.rePlucker.match(browser) )
            return BROWSER_PLUCKER;
        if ( FMTemplateSelector.reWget.match(browser) )
            return BROWSER_MIRROR;
        return BROWSER_UNKNOWN;
    }

    /**
     * @return browser, that requests this page.
     */
    static String findOS(HttpServletRequest request) {
        if ( request==null ) return OS_UNKNOWN;
        String os = request.getHeader("user-agent");
        if ( os==null )
            return OS_UNKNOWN;
        if ( FMTemplateSelector.reWindows.match(os) )
            return OS_WINDOWS;
        if ( FMTemplateSelector.reLinux.match(os) )
            return OS_LINUX;
        return OS_UNKNOWN;
    }
}
