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
package cz.abclinuxu.utils.config.impl;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.Misc;

import java.util.prefs.Preferences;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Configures logging. It shall be one of the first
 * things to be called.
 * todo: use -Dlog4j.configuration=log4j.xml everywhere
 * or look at jakarta logging for better solution.
 */
public class LoggingConfig implements Configurable {
    static boolean hasBeenConfigured = false;

    /** location of Log4J configuration file */
    public static final String PREF_FILE = "file";

    /**
     * If logging has not been configured yet, uses
     * fail-safe default to console.
     */
    public LoggingConfig() {
        if ( !hasBeenConfigured )
            BasicConfigurator.configure();
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        if ( hasBeenConfigured )
            return;
        String file = prefs.get(PREF_FILE,null);
        if ( !Misc.empty(file) ) {
            DOMConfigurator.configure(file);
            hasBeenConfigured = true;
        }
    }

    /**
     * Initializes logging subsystem.
     */
    public static void initialize() {
    }
}
