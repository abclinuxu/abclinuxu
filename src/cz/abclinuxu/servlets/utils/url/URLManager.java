package cz.abclinuxu.servlets.utils.url;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.AbcException;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.data.Relation;
import cz.finesoft.socd.analyzer.DiacriticRemover;

import java.util.prefs.Preferences;

import org.apache.regexp.RE;
import org.apache.regexp.REProgram;
import org.apache.regexp.RECompiler;

/**
 * This class is responsible for enforcing rules
 * defined in design/hierarchicka_url.txt.
 * User: literakl
 * Date: 17.4.2005
 */
public class URLManager implements Configurable {
    public static final String PREF_INVALID_CHARACTERS = "regexp.invalid.characters";
    private static REProgram reInvalidCharacters;

    static {
        ConfigurationManager.getConfigurator().configureAndRememberMe(new URLManager());
    }

    private URLManager() {}


    /**
     * This method shall be used on last part of URL (e.g. after last slash).
     * It converts it to obey common rules.
     * @param url must not be null
     * @return
     */
    public static String enforceLastURLPart(String url) {
        if (url.charAt(0)=='/')
            url = url.substring(1);
        int length = url.length();
        if (url.charAt(length-1)=='/')
            url = url.substring(0, length-1);
        if (url.length()==0)
            throw new AbcException("prazdne url!");

        String fixedURL = DiacriticRemover.getInstance().removeDiacritics(url);
        fixedURL = new RE(reInvalidCharacters, RE.REPLACE_ALL).subst(fixedURL, "-");
        return fixedURL;
    }

    /**
     * This method checks whether the url already exists in database.
     * If it does, it returns URL with suffix, that has not been already
     * stored. No URL format check is performed.
     * @param url
     * @return unique URL
     */
    public static String protectFromDuplicates(String url) {
        SQLTool sqlTool = SQLTool.getInstance();
        int counter = 2;
        String testedURL = url;
        Relation relation = sqlTool.findRelationByURL(testedURL);

        while (relation!=null) {
            testedURL = url + "_" + counter;
            counter++;
            relation = sqlTool.findRelationByURL(testedURL);
        }
        return testedURL;
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        String tmp = prefs.get(PREF_INVALID_CHARACTERS, null);
        reInvalidCharacters = new RECompiler().compile(tmp);
    }
}
