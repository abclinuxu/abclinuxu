/*
 * User: literakl
 * Date: 26.8.2003
 * Time: 22:19:06
 */
package cz.abclinuxu.utils.search;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.de.WordlistLoader;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.finesoft.socd.analyzer.RemoveDiacriticsReader;

import java.util.prefs.Preferences;
import java.util.Hashtable;
import java.io.Reader;

/**
 * Lucene Analyzer, that strips diacritics and uses LowerCaseFilter and custom StopFilter.
 */
public class AbcCzechAnalyzer extends Analyzer implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbcCzechAnalyzer.class);

    public static final String PREF_STOP_WORDS_FILE = "stop.words.file";

    static Hashtable stopTable;

    static {
        try {
            ConfigurationManager.getConfigurator().configureAndRememberMe(new AbcCzechAnalyzer());
        } catch (ConfigurationException e) {
            log.error("Cannot configure!", e);
        }
    }

    /**
     * Tokenizes stream.
     */
    public TokenStream tokenStream(String s, Reader reader) {
        TokenStream result = new LowerCaseTokenizer(new RemoveDiacriticsReader(reader));
        result = new StopFilter(result, stopTable);
        return result;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        String file = prefs.get(PREF_STOP_WORDS_FILE,null);
        log.info("Loading stop words from file '"+file+"'.");
        stopTable = WordlistLoader.getWordtable(file);
        log.info(stopTable.size()+" stop words loaded.");
    }
}
