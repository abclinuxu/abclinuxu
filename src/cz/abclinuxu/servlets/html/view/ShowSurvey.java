/*
 * User: literakl
 * Date: Oct 26, 2002
 * Time: 9:17:19 PM
 */
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.exceptions.InvalidDataException;
import cz.abclinuxu.exceptions.AccessDeniedException;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.security.AccessKeeper;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Survey implementation
 */
public class ShowSurvey implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ShowSurvey.class);

    public static final String PARAM_SCREEN_CURRENT = "SCREEN_CURRENT";
    public static final String PARAM_SCREEN_NEXT = "SCREEN_NEXT";
    public static final String PARAM_SAVE_PARAMS = "SAVE_PARAMS";
    public static final String PARAM_SURVEY_ID = "surveyId";

    public static final String ATTRIB_DATA = "survey";

    /** screen with this id starts survey */
    public static final String START_ID = "START";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        Item survey = (Item) InstanceUtils.instantiateParam(PARAM_SURVEY_ID, Item.class, params, request);
        if ( survey==null ) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Anketa nebyla nalezena!", env, request.getSession());
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/");
            return null;
        }

        persistance.synchronize(survey);
        if ( survey.getType()!=Item.SURVEY ) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Tento objekt není anketa!", env, request.getSession());
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/");
            return null;
        }

        String currentScreen = (String) params.get(PARAM_SCREEN_CURRENT);
        if ( !Misc.empty(currentScreen) ) {
            saveParameters(request.getSession(), params, currentScreen);
        }
        String nextScreen = (String) params.get(PARAM_SCREEN_NEXT);
        if ( Misc.empty(nextScreen) ) nextScreen = START_ID;

        String xpath = "//screen[@id='"+nextScreen+"']";
        Element screen = (Element) survey.getData().selectSingleNode(xpath);

        if ( screen==null ) {
            log.error("Survey "+survey.getId()+" does not define screen "+nextScreen+" (called from "+currentScreen+")!");
            ServletUtils.addError(Constants.ERROR_GENERIC, "Omlouváme se, ale v anketì nastala chyba.", env, request.getSession());
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/");
            return null;
        }

        if (screen.attributeValue("check")!=null)
            try {
                AccessKeeper.checkAccess(survey, request, response);
            } catch (AccessDeniedException e) {
                if ( e.isIpAddressBlocked() )
                    return ServletUtils.showErrorPage("Z této IP adresy se u¾ volilo. Zkuste to pozdìji.", env, request);
                else
                    return ServletUtils.showErrorPage("U¾ jste jednou volil!", env, request);
            }

        Element dump = screen.element("dump");
        if ( dump!=null ) {
            Document data = (Document) request.getSession().getAttribute(ATTRIB_DATA);
            try {
                dump(data, dump);
            } catch (Exception e) {
                log.error("Error in survey "+survey.getId(), e);
                ServletUtils.addError(Constants.ERROR_GENERIC, "Omlouváme se, ale v anketì nastala chyba.", env, request.getSession());
                UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
                urlUtils.redirect(response, "/");
                return null;
            }
            request.getSession().removeAttribute(ATTRIB_DATA);
        }

        Element template = screen.element("template");
        if ( template==null || template.getTextTrim().length()==0 ) {
            log.error("Survey "+survey.getId()+" does not define template in screen "+nextScreen+"!");
            ServletUtils.addError(Constants.ERROR_GENERIC, "Omlouváme se, ale v anketì nastala chyba.", env, request.getSession());
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/");
        }

        env.put("TITLE", "Anketa");
        return FMTemplateSelector.select(template.getTextTrim(), env, request);
    }

    /**
     * this method ads all parameters, that shall be saved, to Document data bound to
     * atrribute in session.
     */
    void saveParameters(HttpSession session, Map params, String currentScreen) {
        Document data = (Document) session.getAttribute(ATTRIB_DATA);
        if ( data==null ) {
            data = DocumentHelper.createDocument();
            data.addElement("anketa");
            session.setAttribute(ATTRIB_DATA,data);
        }
        Element screen = data.getRootElement().addElement("screen");
        screen.addAttribute("id",currentScreen);

        String saveParams = (String) params.get(PARAM_SAVE_PARAMS);
        if ( saveParams==null ) saveParams = "";
        StringTokenizer stk = new StringTokenizer(saveParams,", \r\n");

        while ( stk.hasMoreTokens() ) {
            String var = stk.nextToken();
            var = var.trim();
            Object value = params.get(var);
            if (value==null)
                continue;
            if ( value instanceof String ) {
                String s = (String) value;
                if ( !Misc.empty(s) )
                    screen.addElement(var).addText(s);
            } else {
                List list = (List) value;
                if ( Misc.empty(list) )
                    continue;
                for (Iterator iter = list.iterator(); iter.hasNext();) {
                    String s = (String) iter.next();
                    if ( !Misc.empty(s) )
                        screen.addElement(var).addText(s);
                }
            }
        }
    }

    /**
     * Dumps data to file specified in element dump.
     */
    void dump(Document data, Element dump) throws Exception {
        Element element = dump.element("dir");
        if ( element==null || element.getTextTrim().length()==0 )
            throw new InvalidDataException("No dir in survey!");
        String dir = element.getTextTrim();

        element = dump.element("prefix");
        if ( element==null || element.getTextTrim().length()==0 )
            throw new InvalidDataException("No prefix in survey!");
        String prefix = element.getTextTrim();

        String suffix = null;
        element = dump.element("suffix");
        if ( element!=null ) suffix = element.getTextTrim();

        File directory = new File(dir);
        if ( !directory.exists() )
            directory.mkdirs();
        File file = File.createTempFile(prefix,suffix,directory);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("ISO-8859-2");
        XMLWriter writer = new XMLWriter(bos, format);
        writer.write(data);
        writer.flush();
        bos.close();
    }
}
