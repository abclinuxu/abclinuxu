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
