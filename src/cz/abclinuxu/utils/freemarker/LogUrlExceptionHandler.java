package cz.abclinuxu.utils.freemarker;

import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateException;
import freemarker.core.Environment;

import java.io.Writer;

import org.apache.log4j.Logger;
import cz.abclinuxu.servlets.utils.ServletUtils;

/**
 * User: literakl
 * Date: 21.4.2005
 */
public class LogUrlExceptionHandler implements TemplateExceptionHandler {
    static Logger log = Logger.getLogger(LogUrlExceptionHandler.class);


    /**
     * Logs template exception and current URL.
     * @param e
     * @param environment
     * @param writer
     * @throws TemplateException
     */
    public void handleTemplateException(TemplateException e, Environment environment, Writer writer) throws TemplateException {
        String url = ServletUtils.getCurrentURL();
        log.error("Chyba v sablone na adrese "+url+"\n"+e.getMessage());
    }
}
