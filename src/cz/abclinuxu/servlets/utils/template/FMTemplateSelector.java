/*
 * User: Leos Literak
 * Date: Jan 19, 2003
 * Time: 8:12:38 PM
 */
package cz.abclinuxu.servlets.utils.template;

import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.AbcException;
import cz.abclinuxu.exceptions.NotFoundException;

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
     * Selects page to be processed and template to decorate it. The page is defined as combination
     * of servlet and action in the configuration file. The template, which decorates page, is
     * chosen by browser or taken from users profile.
     * @param servlet name of servlet
     * @param action name of action
     * @param data map, where method can store variables like browser
     * @param content page to be processed or null
     * @param request used for browser identification
     * @return page to be processed
     * @throws NotFoundException when such combination of servlet and action doesn't exist
     */
    public static String select(String servlet, String action, Map data, HttpServletRequest request) {
        String browser = findBrowser(request);
        if ( Misc.same(browser,BROWSER_MIRROR) )
            return "/lynx/other/nomirror.vm";

        ServletAction servletAction = (ServletAction) mappings.get(servlet + action);
        if ( servletAction==null )
            throw new NotFoundException("Neexistuje ¹ablona pro kombinaci "+servlet +","+ action);

        String template = selectTemplate(servletAction,browser,request);
        Mapping mapping = servletAction.getMapping(template);
        if ( mapping==null ) {
            throw new NotFoundException("Neexistuje ¹ablona pro kombinaci ["+servlet+","+action+","+template+"]!");
        }

        String page = mapping.getContent();
        storeVariables(data,mapping.getVariables());
        data.put(VAR_BROWSER,browser);

        return page;
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
        if ( servletAction==null )
            throw new NotFoundException("Neexistuje ¹ablona pro kombinaci "+servlet +","+ action);

        Mapping mapping = servletAction.getMapping(template);
        String page = mapping.getContent();
        storeVariables(data,mapping.getVariables());

        return page;
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
}
