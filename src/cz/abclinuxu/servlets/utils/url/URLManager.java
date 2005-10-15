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
     * @return normalized URL that obeys all rules
     * @throws AbcException if URL is after conversions empty
     */
    public static String enforceLastURLPart(String url) {
        if (url==null || url.length() == 0)
            throw new AbcException("URL nesmí být prázdné!");
        if (url.charAt(0)=='/')
            url = url.substring(1);
        int length = url.length();
        if (url.charAt(length-1)=='/')
            url = url.substring(0, length-1);
        if (url.length()==0)
            throw new AbcException("Zvolte jiné URL, po odstranìní lomítek nic nezbylo!");

        if (Character.isDigit(url.charAt(0)))
            url = "-" + url;

        String fixedURL = DiacriticRemover.getInstance().removeDiacritics(url);
        fixedURL = new RE(reInvalidCharacters, RE.REPLACE_ALL).subst(fixedURL, "-");
        fixedURL = fixedURL.toLowerCase();
        while(fixedURL.endsWith("-"))
            fixedURL = fixedURL.substring(0, fixedURL.length()-1);

        if (fixedURL.length() == 0)
            throw new AbcException("Zvolte jiné URL bez speciálních znakù!");
        return fixedURL;
    }

    /**
     * Validates absolute URL, whether it conforms abclinuxu rules. It tries to fix
     * some errors.
     * @param url
     * @return URL according all rules.
     * @throws AbcException if URL is wrong and cannot be fixed
     */
    public static String enforceAbsoluteURL(String url) {
        if (url == null || url.length() == 0)
            throw new AbcException("URL nesmí být prázdné!");
        if (!url.startsWith("/"))
            throw new AbcException("Adresa stránky musí být absolutní!");
        int length = url.length();
        if (url.charAt(length - 1) == '/')
            url = url.substring(0, length - 1);
        if (url.length() == 0)
            throw new AbcException("Zvolte jiné URL, po odstranìní lomítek nic nezbylo!");

        if (Character.isDigit(url.charAt(0)))
            url = "-" + url;

        String fixedURL = DiacriticRemover.getInstance().removeDiacritics(url);
        fixedURL = new RE(reInvalidCharacters, RE.REPLACE_ALL).subst(fixedURL, "-");
        fixedURL = fixedURL.toLowerCase();
        while (fixedURL.endsWith("-"))
            fixedURL = fixedURL.substring(0, fixedURL.length() - 1);

        if (fixedURL.length() == 0)
            throw new AbcException("Zvolte jiné URL bez speciálních znakù!");
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
            testedURL = url + "-" + counter;
            counter++;
            relation = sqlTool.findRelationByURL(testedURL);
        }
        return testedURL;
    }

    /**
     * Tests, whether URL exists in database. URL format is not checked or modified.
     * @param url URL to be tested
     * @return true, if database contains this URL already
     */
    public static boolean exists(String url) {
        SQLTool sqlTool = SQLTool.getInstance();
        Relation relation = sqlTool.findRelationByURL(url);
        return relation!=null;
    }

    /**
     * Tests, whether URL exists in database. URL format is not checked or modified.
     * If the URL already exists, its relation id is compared to relationId param.
     * @param url URL to be tested
     * @param relationId id of existing relation or 0
     * @return true, URL is not present in database or its relation id is equal to relationId
     */
    public static boolean isURLUnique(String url, int relationId) {
        SQLTool sqlTool = SQLTool.getInstance();
        Relation existingRelation = sqlTool.findRelationByURL(url);
        if (existingRelation == null)
            return true;
        if (relationId == 0)
            return true;
        if (relationId != existingRelation.getId())
            return false;
        return true;
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        String tmp = prefs.get(PREF_INVALID_CHARACTERS, null);
        reInvalidCharacters = new RECompiler().compile(tmp);
    }
}
