/*
 * User: Leos Literak
 * Date: Jan 21, 2003
 * Time: 9:03:14 AM
 */
package cz.abclinuxu.utils.freemarker;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Configuration;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Various FreeMarker's utilities.
 */
public class FMUtils {
    static Configuration config = Configuration.getDefaultConfiguration();

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
}
