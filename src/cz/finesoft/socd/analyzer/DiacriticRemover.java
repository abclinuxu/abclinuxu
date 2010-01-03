package cz.finesoft.socd.analyzer;

import cz.abclinuxu.exceptions.InvalidDataException;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.prefs.Preferences;

/**
 * Helper class providing some diacritics transformations.
 * Works only for STANDARD LATIN and CYRILIC characters!
 * Other characters are untouched...
 *
 * Author: Lukas Zapletal [lzap at root.cz]
 * Date: 2.3.2003
 */
@Deprecated
public class DiacriticRemover implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DiacriticRemover.class);

    public static final String PREF_UNICODE_DATA_FILE = "unicode.data.file";
    private static DiacriticRemover singleton;

    private Hashtable table = new Hashtable(60,0.99f);

    /**
     * Default non public constructor.
     */
    private DiacriticRemover() {
    }

    /**
     * Returns singleton of DiacriticRemover.
     * @return
     */
    public static DiacriticRemover getInstance() {
        if ( singleton==null ) {
            singleton = new DiacriticRemover();
            ConfigurationManager.getConfigurator().configureMe(singleton);
        }
        return singleton;
    }

    /**
     * Fills unicode translation table.
     * @param fileName
     */
    void fillTranslationTable(String fileName) {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            throw new NotFoundException("Cannot find Unicode table in file "+fileName, e);
        }
        UnicodeTableReader utr = new UnicodeTableReader(inputStream);

        // load LATIN and CYLIRIC characters
        try {
            log.info("Loading Unicode translation table from file "+fileName);
            int i = 0;
            while ( utr.next() ) {
                if ( utr.getCodePoint()>='\u0531' ) {
                    // this is ARMENIAN character already
                    break;
                }
                table.put(new Character(utr.getCodePoint()), new Character(utr.getRelative()));
                i++;
            }
            log.info("Unicode translation table populated with "+i+" characters.");
        } catch (Exception e) {
            throw new InvalidDataException("Exception while loading Unicode table at character "+utr.getCodePoint()+"!", e);
        }
    }

    /**
     * Replaces accented cyrillic characters with latin characters equivalent.
     * @param source original string
     * @return string without diacritics
     */
    public String removeDiacritics(String source) {
        Character temp;
        StringBuffer result = new StringBuffer(source.length());
        for ( int i = 0; i<source.length(); i++ ) {
            temp = (Character) table.get(new Character(source.charAt(i)));
            if ( (temp==null) || (temp.charValue()=='\uFFFF') ) {
                result.append(source.charAt(i));
            } else {
                result.append(temp.charValue());
            }
        }
        return result.toString();
    }

    /**
     * Replaces accented eastern european characters with latin characters equivalent.
     * @param source original character
     * @return character with stripped diacritics
     */
    public char removeDiacritics(char source) {
        Character temp = (Character) table.get(new Character(source));
        if ( (temp==null) || (temp.charValue()=='\uFFFF') )
            return source;
        else
            return temp.charValue();
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        String file = prefs.get(PREF_UNICODE_DATA_FILE,null);
        try {
            fillTranslationTable(file);
        } catch (Exception e) {
            log.error(e);
        }
    }
}
