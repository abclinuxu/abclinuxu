/*
 * User: Leos Literak
 * Date: Jan 21, 2003
 * Time: 9:03:14 AM
 */
package cz.abclinuxu.utils.freemarker;

import freemarker.template.*;

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

    static Configuration config;

    static {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(new FMUtils());
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
        Template template = new Template("tmp",reader);
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

        config = freemarker.template.Configuration.getDefaultConfiguration();
        config.setTemplateExceptionHandler(exceptionHandler);
        config.setTemplateUpdateDelay(prefs.getInt(PREF_TEMPLATE_UPDATE_INTERVAL, 5));
        config.setDefaultEncoding("ISO-8859-2");
        config.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
        config.setStrictSyntaxMode(true);
        config.setWhitespaceStripping(true);

        try {
            config.setSetting("number_format", "0");
        } catch (TemplateException e) {
            log.error("Settings failed", e);
        }

        templatesDir = prefs.get(PREF_TEMPLATES_DIRECTORY, null);
        try {
            config.setDirectoryForTemplateLoading(new File(templatesDir));
        } catch (IOException e) {
            throw new ConfigurationException("Cannot set Freemarker templates dir to "+templatesDir);
        }
    }
}
