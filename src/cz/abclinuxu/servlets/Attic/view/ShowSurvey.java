/*
 * User: literakl
 * Date: Oct 26, 2002
 * Time: 9:17:19 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcVelocityServlet;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.VelocityTemplateSelector;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.AbcException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.velocity.context.Context;
import org.dom4j.*;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;

import java.util.*;
import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

/**
 * Survey implementation
 */
public class ShowSurvey extends AbcVelocityServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ShowSurvey.class);

    public static final String PARAM_SCREEN_CURRENT = "SCREEN_CURRENT";
    public static final String PARAM_SCREEN_NEXT = "SCREEN_NEXT";
    public static final String PARAM_SAVE_PARAMS = "SAVE_PARAMS";
    public static final String PARAM_SURVEY_ID = "surveyId";

    public static final String ATTRIB_DATA = "survey";

    /** screen with this id starts survey */
    public static final String START_ID = "START";

    protected String process(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Persistance persistance = PersistanceFactory.getPersistance();
        Map params = (Map) request.getAttribute(AbcVelocityServlet.ATTRIB_PARAMS);
        Item survey = (Item) InstanceUtils.instantiateParam(PARAM_SURVEY_ID,Item.class,params);
        if ( survey==null ) {
            ServletUtils.addError(null,"Anketa nebyla nalezena!",ctx,request.getSession());
            return VelocityTemplateSelector.selectTemplate(request,ctx,"ViewIndex","show");
        }
        persistance.synchronize(survey);
        if ( survey.getType()!=Item.SURVEY ) {
            ServletUtils.addError(null,"Tento objekt není anketa!",ctx,request.getSession());
            UrlUtils.redirect("/Index",response,ctx);
            return null;
        }

        String currentScreen = (String) params.get(PARAM_SCREEN_CURRENT);
        if ( ! Misc.empty(currentScreen) ) {
            saveParameters(request.getSession(),params,currentScreen);
        }

        String nextScreen = (String) params.get(PARAM_SCREEN_NEXT);
        if ( Misc.empty(nextScreen) ) nextScreen = START_ID;

        String xpath = "//screen[@id='"+nextScreen+"']";
        Element screen = (Element) survey.getData().selectSingleNode(xpath);

        if ( screen==null ) {
            log.error("Survey "+survey.getId()+" does not define screen "+nextScreen+
                      " (called from "+currentScreen+")!");
            ServletUtils.addError(null,"Omlouváme se, ale v anketì nastala chyba.",ctx,request.getSession());
            UrlUtils.redirect("/Index",response,ctx);
            return null;
        }

        Element dump = screen.element("dump");
        if ( dump!=null ) {
            Document data = (Document) request.getSession().getAttribute(ATTRIB_DATA);
            try {
                dump(data,dump);
            } catch (Exception e) {
                log.error("Error in survey "+survey.getId(),e);
                ServletUtils.addError(null,"Omlouváme se, ale v anketì nastala chyba.",ctx,request.getSession());
                UrlUtils.redirect("/Index",response,ctx);
                return null;
            }
            request.getSession().removeAttribute(ATTRIB_DATA);
        }

        Element template = screen.element("template");
        if ( template==null || template.getTextTrim().length()==0 ) {
            log.error("Survey "+survey.getId()+" does not define template in screen "+nextScreen+"!");
            ServletUtils.addError(null,"Omlouváme se, ale v anketì nastala chyba.",ctx,request.getSession());
            UrlUtils.redirect("/Index",response,ctx);
            return null;
        }

        ctx.put("TITLE","Anketa");
        return VelocityTemplateSelector.selectTemplate(request,ctx,template.getTextTrim());
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
        StringTokenizer stk = new StringTokenizer(saveParams,",");

        while ( stk.hasMoreTokens() ) {
            String var = stk.nextToken();
            Object value = params.get(var);
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
            throw new AbcException("No dir in survey!",0);
        String dir = element.getTextTrim();

        element = dump.element("prefix");
        if ( element==null || element.getTextTrim().length()==0 )
            throw new AbcException("No prefix in survey!",0);
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
