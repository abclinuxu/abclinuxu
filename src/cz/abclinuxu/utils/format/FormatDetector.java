/*
 * User: literakl
 * Date: 29.1.2004
 * Time: 19:27:17
 */
package cz.abclinuxu.utils.format;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;

import java.util.prefs.Preferences;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.apache.regexp.REProgram;
import org.apache.regexp.RECompiler;
import org.apache.log4j.Logger;

/**
 * Guesses the format of the text.
 */
public class FormatDetector implements Configurable {
    static Logger log = Logger.getLogger(FormatDetector.class);

    public static final String PREF_REGEXP_LINE_BREAK = "RE_BREAK";
    protected static REProgram lineBreaks;

    static {
        FormatDetector detector = new FormatDetector();
        ConfigurationManager.getConfigurator().configureAndRememberMe(detector);
    }

    /**
     * Guesses, in which format the input string is.
     */
    public static Format detect(String input) {
        if ( new RE(lineBreaks, RE.MATCH_CASEINDEPENDENT).match(input) )
            return Format.HTML;
        else
            return Format.SIMPLE;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        try {
            String pref = prefs.get(PREF_REGEXP_LINE_BREAK, null);
            lineBreaks = new RECompiler().compile(pref);
        } catch (RESyntaxException e) {
            log.error("", e);
        }
    }
}
