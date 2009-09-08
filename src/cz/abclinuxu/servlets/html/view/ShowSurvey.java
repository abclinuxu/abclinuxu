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
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.exceptions.InvalidDataException;
import cz.abclinuxu.exceptions.AccessDeniedException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.security.AccessKeeper;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.apache.commons.fileupload.FileItem;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;

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
    public static final String ATTRIB_STARTED = "started";

    /**
     * screen with this id starts survey
     */
    public static final String START_ID = "START";

    /* because we dont have always relation for survey, we have to simulate it with high enough number,
    * so collision with existing relation is not possible
    */
    public static final int SURVEY_PREFIX = 30000000;

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        HttpSession session = request.getSession();

        Item survey = (Item) InstanceUtils.instantiateParam(PARAM_SURVEY_ID, Item.class, params, request);
        if (survey == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Anketa nebyla nalezena!", env, session);
            urlUtils.redirect(response, "/");
            return null;
        }

        persistence.synchronize(survey);
        if (survey.getType() != Item.SURVEY) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Tento objekt není anketa!", env, session);
            urlUtils.redirect(response, "/");
            return null;
        }

        String currentScreen = (String) params.get(PARAM_SCREEN_CURRENT);
        String nextScreen = (String) params.get(PARAM_SCREEN_NEXT);
        if (Misc.empty(nextScreen))
            nextScreen = START_ID;

        String xpath = "//screen[@id='" + nextScreen + "']";
        Element screen = (Element) survey.getData().selectSingleNode(xpath);

        if (screen == null) {
            log.error("Survey " + survey.getId() + " does not define screen " + nextScreen + " (called from " + currentScreen + ")!");
            ServletUtils.addError(Constants.ERROR_GENERIC, "Omlouváme se, ale v anketě nastala chyba.", env, session);
            urlUtils.redirect(response, "/");
            return null;
        }

        boolean mustBeLogged = Boolean.valueOf(screen.attributeValue("onlyUsers"));
        boolean forbidDuplicates = Boolean.valueOf(screen.attributeValue("check"));
        boolean recordTime = screen.attributeValue("recordTime") != null;
        List<FileItem> files = new ArrayList<FileItem>();

        if (mustBeLogged && user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if (!Misc.empty(currentScreen))
            saveParameters(session, params, currentScreen, files);
        if (recordTime)
            session.setAttribute(ATTRIB_STARTED, new Date());

        if (forbidDuplicates)
            try {
                Relation relation = new Relation(survey, survey, 0);
                relation.setId(survey.getId() + SURVEY_PREFIX);
                AccessKeeper.checkAccess(relation, user, "survey", request, response);
            } catch (AccessDeniedException e) {
                if (e.isIpAddressBlocked() && !mustBeLogged)
                    return ServletUtils.showErrorPage("Z této IP adresy se už volilo. Zkuste to později.", env, request);
                else
                    return ServletUtils.showErrorPage("Už jste jednou volil!", env, request);
            }

        Element dump = screen.element("dump");
        if (dump != null) {
            try {
                dump(session, dump, files);
            } catch (Exception e) {
                log.error("Error in survey " + survey.getId(), e);
                ServletUtils.addError(Constants.ERROR_GENERIC, "Omlouváme se, ale v anketě nastala chyba.", env, session);
                urlUtils.redirect(response, "/");
                return null;
            }
        }

        Element url = screen.element("url");
        if (url != null) {
            String surl = url.getText();
            if (surl.startsWith("/"))
                urlUtils.redirect(response, url.getText());
            else
                response.sendRedirect(surl);
            return null;
        }

        Element template = screen.element("template");
        if (template == null || template.getTextTrim().length() == 0) {
            log.error("Survey " + survey.getId() + " does not define template in screen " + nextScreen + "!");
            ServletUtils.addError(Constants.ERROR_GENERIC, "Omlouváme se, ale v anketě nastala chyba.", env, session);
            urlUtils.redirect(response, "/");
        }

        env.put("TITLE", "Anketa");
        return FMTemplateSelector.select(template.getTextTrim(), env, request);
    }

    /**
     * This method adds all parameters, that shall be saved, to Document data bound to
     * atrribute in session.
     */
    void saveParameters(HttpSession session, Map params, String currentScreen, List<FileItem> files) {
        Document data = (Document) session.getAttribute(ATTRIB_DATA);
        if (data == null) {
            data = DocumentHelper.createDocument();
            data.addElement("anketa");
            session.setAttribute(ATTRIB_DATA, data);
        }
        Element screen = data.getRootElement().addElement("screen");
        screen.addAttribute("id", currentScreen);

        String saveParams = (String) params.get(PARAM_SAVE_PARAMS);
        if (saveParams == null)
            saveParams = "";
        StringTokenizer stk = new StringTokenizer(saveParams, ", \r\n");

        while (stk.hasMoreTokens()) {
            String var = stk.nextToken();
            var = var.trim();
            Object value = params.get(var);
            if (value == null)
                continue;
            if (value instanceof String) {
                String s = (String) value;
                if (!Misc.empty(s))
                    screen.addElement(var).addText(s);
            } else if (value instanceof FileItem) {
                files.add((FileItem) value);
            } else {
                List list = (List) value;
                if (Misc.empty(list))
                    continue;
                for (Iterator iter = list.iterator(); iter.hasNext();) {
                    String s = (String) iter.next();
                    if (!Misc.empty(s))
                        screen.addElement(var).addText(s);
                }
            }
        }
    }

    /**
     * Dumps data to file specified in element dump.
     */
    void dump(HttpSession session, Element dump, List<FileItem> files) throws Exception {
        Document data = (Document) session.getAttribute(ATTRIB_DATA);
        session.removeAttribute(ATTRIB_DATA);
        Date started = (Date) session.getAttribute(ATTRIB_STARTED);
        if (started != null) {
            long time = (System.currentTimeMillis() - started.getTime()) / 1000;
            List screens = data.getRootElement().elements();
            Element lastScreen = (Element) screens.get(screens.size() -1);
            lastScreen.addElement("recordedTime").addText(Long.toString(time));
        }

        StringWriter sWriter = new StringWriter();
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        XMLWriter xmlWriter = new XMLWriter(sWriter, format);
        xmlWriter.write(data);
        xmlWriter.flush();
        String xmlString = sWriter.toString();

        Element emailElement = dump.element("email");
        if (emailElement != null) {
            Map params = new HashMap();
            // put addresses
            for (Iterator iter = emailElement.elementIterator(); iter.hasNext();) {
                Element element = (Element) iter.next();
                params.put(element.getName().toUpperCase(), element.getTextTrim());
            }
            params.put(EmailSender.KEY_SUBJECT, "anketa");
            params.put(EmailSender.KEY_BODY, xmlString);
            if (!EmailSender.sendEmail(params))
                log.warn("Failed to send form data:\n" + xmlString);
        }

        Element element = dump.element("dir");
        if (element != null) {
            if (element.getTextTrim().length() == 0)
                throw new InvalidDataException("No dir in survey!");
            String dir = element.getTextTrim();

            element = dump.element("prefix");
            if (element == null || element.getTextTrim().length() == 0)
                throw new InvalidDataException("No prefix in survey!");
            String prefix = element.getTextTrim();

            String suffix;
            element = dump.element("suffix");
            if (element != null)
                suffix = element.getTextTrim();
            else
                suffix = ".xml";

            File directory = new File(FMUtils.getTemplatesDir(), "web/ankety/" + dir);
            if (!directory.exists())
                directory.mkdirs();

            File file = File.createTempFile(prefix, suffix, directory);
            Writer writer = new BufferedWriter(new FileWriter(file));
            writer.write(xmlString);
            writer.close();

            for (FileItem fileItem : files) {
                int position = file.getName().indexOf(suffix);
                String name = file.getName().substring(0, position) + fileItem.getName();
                File attachedFile = new File(directory, name);
                fileItem.write(attachedFile);
            }
        }
    }
}
