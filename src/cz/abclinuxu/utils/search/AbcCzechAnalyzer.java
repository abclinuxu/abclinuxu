/*
 *  Copyright (C) 2005 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
package cz.abclinuxu.utils.search;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.de.WordlistLoader;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.finesoft.socd.analyzer.RemoveDiacriticsReader;

import java.util.prefs.Preferences;
import java.util.Hashtable;
import java.io.Reader;
import java.io.IOException;

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
        TokenStream stream = new StandardTokenizer(new RemoveDiacriticsReader(reader));
        stream = new LowerCaseFilter(stream);
        stream = new StopFilter(stream, stopTable);
        return stream;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        String file = prefs.get(PREF_STOP_WORDS_FILE,null);
        log.info("Loading stop words from file '"+file+"'.");
        try {
	        stopTable = WordlistLoader.getWordtable(file);
	} catch (IOException e) {
	        throw new ConfigurationException(e);
	}

	log.info(stopTable.size()+" stop words loaded.");

    }
}
