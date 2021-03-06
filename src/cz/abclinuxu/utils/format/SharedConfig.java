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
