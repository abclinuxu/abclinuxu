/*
 * User: literakl
 * Date: 22.8.2002
 * Time: 11:09:45
 */
package cz.abclinuxu.servlets.utils;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import cz.abclinuxu.AbcException;

/**
 * This class is responsible for selecting presentation variant
 * and template mapping.<p>
 *
 */
public class VariantTool {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(VariantTool.class);

    /** if not overriden, this variant will be used */
    static String DEFAULT_VARIANT = "web";

    /** custom selection of variant */
    public static final String PARAM_VARIANTA = "varianta";

    /** this context variable holds name of template to be included */
    public static final String VAR_CONTENT_TEMPLATE = "CONTENT";
    /** this context variable holds type of browser, which is used by visitor */
    public static final String VAR_BROWSER = "BROWSER";
    /** lynx broswer */
    public static final String BROWSER_LYNX = "LYNX";
    /** mozilla browser */
    public static final String BROWSER_MOZILLA = "MOZILLA";
    /** Internet Explorer browser */
    public static final String BROWSER_IE = "IE";
    /** plucker PDA browser */
    public static final String BROWSER_PLUCKER = "PLUCKER";
    /** other browser */
    public static final String BROWSER_OTHER = "OTHER";

    /** singleton. Dont forget to initialize it! */
    static VariantTool singleton;

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
     * Here we store mappings. key is concatenation of servlet name and action, value is map
     * where key is variant and value is templet name.
     */
    HashMap mappings;

    /**
     * nonpublic constructor
     * @param size initial size of hash map.
     */
    protected VariantTool(int size) {
        mappings = new HashMap(size,0.9f);
    }

    /**
     * Loads configuration and instantiates singleton of VariantTool.
     * @param filename name of configuration file
     * @return initialized instance of VariantTool
     */
    public static void initialize(String filename) throws Exception {
        Document document = new SAXReader().read(filename);
        List servlets = document.getRootElement().elements("servlet");
        VariantTool variants = new VariantTool(servlets.size()*3);

        for (Iterator servletIter = servlets.iterator(); servletIter.hasNext();) { // for each servlet
            Element servlet = (Element) servletIter.next();
            String servletName = servlet.attributeValue("name");
            List actions = servlet.elements("action");

            for (Iterator actionIter = actions.iterator(); actionIter.hasNext();) { // for each action
                Element action = (Element) actionIter.next();
                String actionName = action.attributeValue("name");
                List mappings = action.elements("mapping");
                HashMap mappingsMap = new HashMap(mappings.size()+1,0.95f);

                for (Iterator mappingIter = mappings.iterator(); mappingIter.hasNext();) { // for each mapping
                    Element mapping = (Element) mappingIter.next();
                    String variant = mapping.attributeValue("variant");
                    String template = mapping.attributeValue("template");
                    mappingsMap.put(variant,template);
                }

                String name = servletName + actionName;
                variants.mappings.put(name,mappingsMap);
            }
        }

        singleton = variants;
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
        return singleton.doSelectTemplate(request,ctx,servlet,action);
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
        ctx.put(VAR_CONTENT_TEMPLATE,template);
        return variant+"/template.vm";
    }

    /**
     * Finds variant based on browser characteristics and user's preferences.
     * It also puts browser information into context.
     */
    static String selectVariant(HttpServletRequest request, Context ctx) {
        String variant = DEFAULT_VARIANT;
        String browser = request.getHeader("user-agent");

        if ( browser==null ) {
            log.debug("No user-agent header!");
        } else if ( reWget.match(browser) ) {
            return "lynx/other/nomirror.vm"; // mirroring forbidden
        } else if ( reLynx.match(browser) ) {
            ctx.put(VAR_BROWSER,BROWSER_LYNX);
            variant = "lynx";
        } else if ( rePlucker.match(browser) ) {
            ctx.put(VAR_BROWSER,BROWSER_PLUCKER);
            variant = "lynx";
        } else {
            ctx.put(VAR_BROWSER,BROWSER_OTHER);
        }

        String tmp = request.getParameter(PARAM_VARIANTA);
        if ( tmp!=null ) {
            variant = tmp;
        }

        return variant;
    }

    /**
     * real processing is done here
     */
    String doSelectTemplate(HttpServletRequest request, Context ctx, String servlet, String action) {
        String variant = selectVariant(request,ctx);

        String name = servlet + action, content = null;
        Map map = (Map) mappings.get(name);
        if ( map==null ) {
            throw new AbcException("Cannot find mapping for ["+servlet+","+action+"]!",AbcException.RUNTIME);
        }

        content = (String) map.get(variant);
        if ( content==null && ! DEFAULT_VARIANT.equals(variant) ) {
             // if current variant is not specified, use default
            content = (String) map.get(DEFAULT_VARIANT);
        }

        if ( content==null ) {
            throw new AbcException("Cannot find template for ["+servlet+","+action+","+variant+"]!",AbcException.RUNTIME);
        }

        ctx.put(VAR_CONTENT_TEMPLATE,content);
        return variant+"/template.vm";
    }
}
