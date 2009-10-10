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
package cz.abclinuxu.utils.freemarker;

import freemarker.template.*;
import freemarker.ext.beans.BeansWrapper;
import freemarker.cache.FileTemplateLoader;

import java.io.*;
import java.util.Map;
import java.util.prefs.Preferences;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.ConfigurationManager;
import org.apache.log4j.Logger;

/**
 * Various FreeMarker's utilities.
 */
public class FMUtils implements Configurable {
    static Logger log = Logger.getLogger(FMUtils.class);

    /** Directory, where templates are located. Use relative path to deploy_path */
    public static final String PREF_TEMPLATES_DIRECTORY = "directory.templates";
    /** interval in seconds between check, whether templaet has been changed */
    public static final String PREF_TEMPLATE_UPDATE_INTERVAL = "template.update";
    /** constant for choosing error handler for template errors */
    public static final String PREF_ERROR_HANDLER = "error.handler";

    public static final String ERROR_HANDLER_IGNORE = "IGNORE";
    public static final String ERROR_HANDLER_RETHROW = "RETHROW";
    public static final String ERROR_HANDLER_HTML_DEBUG = "HTML_DEBUG";
    public static final String ERROR_HANDLER_DEBUG = "DEBUG";
    public static final String ERROR_HANDLER_LOGURL = "LOG_URL";

    static Configuration config;

    static {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureAndRememberMe(new FMUtils());
    }

    static String templatesDir;

    /**
     * Executes given code using variables from data.
     * @param code FTL to execute
     * @param data data model used within execution of code
     * @return result of execution
     */
    public static String executeCode(String code, Map data) throws IOException, TemplateException {
        StringReader reader = new StringReader(code);
        Template template = new Template("tmp", reader, config);
        StringWriter writer = new StringWriter();
        template.process(data,writer);
        return writer.toString();
    }

    /**
     * Tests whether file relative to templates dir exists.
     * @param path relative path to file.
     * @return true if there is such file
     */
    public static boolean fileExists(String path) {
        File file = new File(templatesDir, path);
        return file.exists();
    }

    /**
     * Executes template specified by name using variables from data.
     * @param name name of template
     * @param data data model used within execution of code
     * @return executed template
     */
    public static String executeTemplate(String name, Map data) throws IOException, TemplateException {
        Template template = config.getTemplate(name);
        StringWriter writer = new StringWriter();
        template.process(data,writer);
        return writer.toString();
    }

    /**
     * Executes template specified by name using variables from data.
     * @param template name of template
     * @param env data model used within execution of code. It shall be Map or TemplateModel
     * @param file file, where executed code will be saved
     */
    public static void executeTemplate(String template, Object env, File file) throws Exception {
        Template tpl = config.getTemplate(template);
        FileWriter writer = new FileWriter(file);
        tpl.process(env, writer);
        writer.close();
    }

    /**
     * @return directory where the templates are stored
     */
    public static String getTemplatesDir() {
        return templatesDir;
    }

    /**
     * @return Configuration used by this object.
     */
    public static Configuration getConfiguration() {
        return config;
    }

    /**
     * Callback to configure this instance.
     * @param prefs
     * @throws ConfigurationException
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        TemplateExceptionHandler exceptionHandler = TemplateExceptionHandler.IGNORE_HANDLER;
        String tmp = prefs.get(PREF_ERROR_HANDLER, null);
        if (ERROR_HANDLER_IGNORE.equalsIgnoreCase(tmp))
            exceptionHandler = TemplateExceptionHandler.IGNORE_HANDLER;
        else if (ERROR_HANDLER_HTML_DEBUG.equalsIgnoreCase(tmp))
            exceptionHandler = TemplateExceptionHandler.HTML_DEBUG_HANDLER;
        else if (ERROR_HANDLER_DEBUG.equalsIgnoreCase(tmp))
            exceptionHandler = TemplateExceptionHandler.DEBUG_HANDLER;
        else if (ERROR_HANDLER_RETHROW.equalsIgnoreCase(tmp))
            exceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER;
        else if (ERROR_HANDLER_LOGURL.equalsIgnoreCase(tmp))
            exceptionHandler = new LogUrlExceptionHandler();

        BeansWrapper beansWrapper = new BeansWrapper();
//        beansWrapper.setSimpleMapWrapper(true); // rids off hashCode etc from ?keys

        config = new Configuration();
        config.setTemplateExceptionHandler(exceptionHandler);
        config.setTemplateUpdateDelay(prefs.getInt(PREF_TEMPLATE_UPDATE_INTERVAL, 5));
        config.setDefaultEncoding("UTF-8");
        config.setOutputEncoding("UTF-8");
        config.setObjectWrapper(beansWrapper);
        config.setStrictSyntaxMode(true);
        config.setWhitespaceStripping(true);
        config.addAutoImport("lib", "web/macros.ftl");

        try {
            config.setSetting("number_format", "0");
        } catch (TemplateException e) {
            log.error("Settings failed", e);
        }

        templatesDir = prefs.get(PREF_TEMPLATES_DIRECTORY, null);
        try {
            FileTemplateLoader fileTemplateLoader = new FileTemplateLoader(new File(templatesDir));
            FallBackTemplateLoader loader = new FallBackTemplateLoader(fileTemplateLoader, "web");
            config.setTemplateLoader(loader);
        } catch (IOException e) {
            throw new ConfigurationException("Cannot set Freemarker templates dir to "+templatesDir);
        }
    }
}
