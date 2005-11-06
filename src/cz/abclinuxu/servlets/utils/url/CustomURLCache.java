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
package cz.abclinuxu.servlets.utils.url;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;

import java.util.prefs.Preferences;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;

/**
 * Cache of custom URLs, that are known.
 * It maps URL to relation id.
 * User: literakl
 * Date: 17.4.2005
 */
public class CustomURLCache implements Configurable {
    private static final String PREF_CACHE_SIZE = "size";

    private static CustomURLCache instance;
    static {
        instance = new CustomURLCache();
    }
    int size;
    Map cache;


    private CustomURLCache() {
        ConfigurationManager.getConfigurator().configureMe(this);
        cache = Collections.synchronizedMap(new HashMap());
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        size = prefs.getInt(PREF_CACHE_SIZE, 100);
    }
}
