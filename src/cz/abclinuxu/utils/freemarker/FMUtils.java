/*
 * User: Leos Literak
 * Date: Jan 21, 2003
 * Time: 9:03:14 AM
 */
package cz.abclinuxu.utils.freemarker;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Configuration;
import freemarker.ext.beans.BeansWrapper;

import java.io.*;
import java.util.Map;
import java.util.prefs.Preferences;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.impl.AbcConfig;

/**
 * Various FreeMarker's utilities.
 */
public class FMUtils implements Configurable {

    /** Directory, where templates are located. Use relative path to deploy_path */
    public static final String PREF_TEMPLATES_DIRECTORY = "directory.templates";
    public static final String DEFAULT_TEMPLATES_DIRECTORY = "WEB-INF/freemarker";

    static Configuration config;

    static {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(new FMUtils());
    }

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
     * @param env data model used within execution of code
     * @param file file, where executed code will be saved
     */
    public static void executeTemplate(String template, Map env, File file) throws Exception {
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
        config = freemarker.template.Configuration.getDefaultConfiguration();
        BeansWrapper wrapper = BeansWrapper.getDefaultInstance();
        config.setDefaultEncoding("ISO-8859-2");
        config.setObjectWrapper(wrapper);
        config.setStrictSyntaxMode(true);
        config.setTemplateUpdateDelay(1);
        String path = prefs.get(PREF_TEMPLATES_DIRECTORY,DEFAULT_TEMPLATES_DIRECTORY);
        String templatesDir = AbcConfig.calculateDeployedPath(path);
        try {
            config.setDirectoryForTemplateLoading(new File(templatesDir));
        } catch (IOException e) {
            throw new ConfigurationException("Cannot set Freemarker templates dir to "+templatesDir);
        }
    }
}
