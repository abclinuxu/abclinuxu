/*
 * User: Leos Literak
 * Date: Jan 19, 2003
 * Time: 8:12:38 PM
 */
package cz.abclinuxu.servlets.utils.template;

import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.AbcException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.io.StringWriter;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 * This template selector is for FreeMarker template engine.
 */
public class FMTemplateSelector extends TemplateSelector {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FMTemplateSelector.class);

    /** regular expressions to match UA */
    static RE reLynx, reWget, rePlucker, reMozilla, reExplorer, reLinux, reWindows;

    static {
        try {
            reLynx = new RE("(Lynx)",RE.MATCH_CASEINDEPENDENT);
            rePlucker = new RE("(Plucker)",RE.MATCH_CASEINDEPENDENT);
            reWget = new RE("(wget)|(custo([^m]|($)))",RE.MATCH_CASEINDEPENDENT); // dont catch "custom"
            reExplorer = new RE("(MSIE)");
            reLinux = new RE("Linux");
            reWindows = new RE("Windows");
        } catch (RESyntaxException e) {
            log.error("Wrong regexp!", e);
        }
    }

    /**
     * This method selects template to be processed. If content is not set, it is searched
     * in global configuration file.
     * @param servlet name of servlet
     * @param action name of action
     * @param data map, where some variables like browser may be stored
     * @param request used for browser identification
     * @param content page to be processed or null
     * @return page to be processed
     */
    public static String select(String servlet, String action, Map data, HttpServletRequest request, String content) {
        String browser = findBrowser(request);
        String os = findOS(request);
        if ( Misc.same(browser,BROWSER_MIRROR) )
            return "lynx/other/nomirror.vm";

        ServletAction servletAction = (ServletAction) mappings.get(servlet + action);
        if ( servletAction==null )
            throw new AbcException("Neexistuje sablona pro kombinaci "+servlet +","+ action);

        String template = selectTemplate(servletAction,browser,request);
        if ( Misc.empty(content) ) {
            Mapping mapping = servletAction.getMapping(template);
            content = mapping.getContent();
            storeVariables(data,mapping.getVariables());
        }

        data.put(VAR_CONTENT,content);
        data.put(VAR_BROWSER,browser);
        data.put(VAR_OS,os);

        return template+"/template.vm";
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
                    data.put(variable.getName(),FMUtils.execute(code,data));
                } catch (Exception e) {
                    log.error("Cannot evaluate lazy variable "+variable.getName()+"["+code+"]!", e);
                }
            } else {
                data.put(variable.getName(),value);
            }
        }
    }
}
