/*
 * User: literakl
 * Date: 22.8.2002
 * Time: 11:09:45
 */
package cz.abclinuxu.servlets.utils.template;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.apache.velocity.context.Context;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ResourceNotFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.io.StringWriter;
import java.io.IOException;

import cz.abclinuxu.AbcException;
import cz.abclinuxu.utils.Misc;

/**
 * This class is responsible for selecting presentation variant
 * and template mapping.<p>
 *
 */
public class VelocityTemplateSelector extends TemplateSelector {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VelocityTemplateSelector.class);

    /** regular expressions to match UA */
    static RE reLynx, reWget, rePlucker;

    static {
        try {
            reLynx = new RE("(Lynx)",RE.MATCH_CASEINDEPENDENT);
            rePlucker = new RE("(Plucker)",RE.MATCH_CASEINDEPENDENT);
            reWget = new RE("(Wget)|(custo([^m]|($)))",RE.MATCH_CASEINDEPENDENT); // dont catch "custom"
        } catch (RESyntaxException e) {
            log.error("Wrong regexp!", e);
        }
    }

    /**
     * Based on browser's characteristics and user's preference this method selects presentation
     * variant and returns Template for given servlet and action. The variables VAR_BROWSER and
     * VAR_CONTENT_TEMPLATE are put into context.
     * @param request request information
     * @param servlet servlet name, same as in configuration file
     * @param action action name, same as in configuration file
     * @return Name of template to be rendered or null, if no mapping found
     */
    public static String selectTemplate(HttpServletRequest request, Context ctx, String servlet, String action) {
        String variant = selectVariant(request,ctx);
        if ( Misc.same((String) ctx.get(VAR_BROWSER),BROWSER_MIRROR) ) {
            return "lynx/other/nomirror.vm";
        }

        String name = servlet + action;
        Map map = (Map) mappings.get(name);
        if ( map==null ) {
            throw new AbcException("Cannot find mapping for ["+servlet+","+action+"]!",AbcException.RUNTIME);
        }

        Mapping mapping = (Mapping) map.get(variant);
        if ( mapping==null && ! DEFAULT_TEMPLATE.equals(variant) ) {
            mapping = (Mapping) map.get(DEFAULT_TEMPLATE); // use default variant
        }

        if ( mapping==null ) {
            throw new AbcException("Cannot find template for ["+servlet+","+action+","+variant+"]!",AbcException.RUNTIME);
        }

        storeVariablesIntoContext(ctx,mapping.getVariables());
        ctx.put(VAR_CONTENT,mapping.getContent());
        return variant+"/template.vm";
    }

    /**
     * Based on browser's characteristics and user's preference this method selects presentation
     * variant and returns template. The variables VAR_BROWSER and
     * VAR_CONTENT_TEMPLATE are put into context.
     * @param request request information
     * @return Name of template to be rendered
     */
    public static String selectTemplate(HttpServletRequest request, Context ctx, String template) {
        String variant = selectVariant(request,ctx);
        if ( Misc.same((String) ctx.get(VAR_BROWSER),BROWSER_MIRROR) ) {
            return "lynx/other/nomirror.vm";
        }
        ctx.put(VAR_CONTENT,template);
        return variant+"/template.vm";
    }

    /**
     * Stores list of Variable into context. It also evaluates LazyVar before storing it as String.
     */
    static void storeVariablesIntoContext(Context ctx, List variables) {
        if ( variables==null ) return;
        for (Iterator iter = variables.iterator(); iter.hasNext();) {
            Variable variable = (Variable) iter.next();
            Object value = variable.getValue();
            if ( value instanceof LazyVar ) {
                try {
                    StringWriter sw = new StringWriter();
                    Velocity.evaluate(ctx,sw,"log tag",((LazyVar)value).getValue());
                    ctx.put(variable.getName(),sw.toString());
                } catch (Exception e) {
                    log.error("Cannot evaluate lazy variable "+variable.getName()+"["+((LazyVar)value).getValue()+"]!", e);
                }
            } else {
                ctx.put(variable.getName(),value);
            }
        }
    }

    /**
     * Finds variant based on browser characteristics and user's preferences.
     * It also puts browser information into context.
     */
    static String selectVariant(HttpServletRequest request, Context ctx) {
        String variant = DEFAULT_TEMPLATE;
        String browser = request.getHeader("user-agent");

        if ( browser==null ) {
        } else if ( reWget.match(browser) ) { // mirroring forbidden
            ctx.put(VAR_BROWSER,BROWSER_MIRROR);
            variant = "lynx";
        } else if ( reLynx.match(browser) ) {
            ctx.put(VAR_BROWSER,BROWSER_LYNX);
            variant = "lynx";
        } else if ( rePlucker.match(browser) ) {
            ctx.put(VAR_BROWSER,BROWSER_PLUCKER);
            variant = "lynx";
        } else {
            ctx.put(VAR_BROWSER,BROWSER_UNKNOWN);
        }

        String tmp = request.getParameter(PARAM_VARIANTA);
        if ( !Misc.empty(tmp) ) {
            variant = tmp;
        }

        return variant;
    }
}
