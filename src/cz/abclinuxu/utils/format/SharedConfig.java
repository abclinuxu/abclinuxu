/*
 * User: literakl
 * Date: 29.1.2004
 * Time: 19:49:31
 */
package cz.abclinuxu.utils.format;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;

import java.util.prefs.Preferences;

/**
 * Helper, that contains data common to several renderers.
 */
public class SharedConfig implements Configurable {
    public static final String IMG_EMOTICON_SMICH = "smich";
    public static final String IMG_EMOTICON_USMEV = "usmev";
    public static final String IMG_EMOTICON_MRK = "mrk";
    public static final String IMG_EMOTICON_SMUTEK = "smutek";

    private static String smich, usmev, mrk, smutek;

    static {
        ConfigurationManager.getConfigurator().configureAndRememberMe(new SharedConfig());
    }

    public static String getImageOfSmich() {
        return smich;
    }

    public static String getImageOfUsmev() {
        return usmev;
    }

    public static String getImageOfMrk() {
        return mrk;
    }

    public static String getImageOfSmutek() {
        return smutek;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        usmev = prefs.get(IMG_EMOTICON_USMEV,":-)");
        smich = prefs.get(IMG_EMOTICON_SMICH,":-D");
        mrk = prefs.get(IMG_EMOTICON_MRK,";-)");
        smutek = prefs.get(IMG_EMOTICON_SMUTEK,":-(");
    }
}
