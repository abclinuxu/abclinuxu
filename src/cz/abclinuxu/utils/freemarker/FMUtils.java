/*
 * User: Leos Literak
 * Date: Jan 21, 2003
 * Time: 9:03:14 AM
 */
package cz.abclinuxu.utils.freemarker;

import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Various FreeMarker's utilities.
 */
public class FMUtils {

    /**
     * Executes given code using variables from data.
     * @param code FTL to execute
     * @param data data model used within execution of code
     * @return result of execution
     */
    public static String execute(String code, Map data) throws IOException, TemplateException {
        StringReader reader = new StringReader(code);
        Template template = new Template("tmp",reader);
        StringWriter writer = new StringWriter();
        template.process(data,writer);
        return writer.toString();
    }
}
