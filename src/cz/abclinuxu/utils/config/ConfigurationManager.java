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
package cz.abclinuxu.utils.config;

import cz.abclinuxu.utils.config.impl.SimpleConfigurator;

/**
 * Manager for configuration of system.
 */
public class ConfigurationManager {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConfigurationManager.class);

    /**
     * ConfigurationManager will try to automatically loads preferences
     * from file specified by this system property.
     */
    public static final String PROPERTY_CONFIG_FILE = "abc.config";

    /** singleton */
    static Configurator configurator;

    /**
     * Gets instance of available configurator.
     */
    public static Configurator getConfigurator() {
        if ( configurator==null )
            init();
        return configurator;
    }

    /**
     * Sets default configurator.
     */
    public static void setConfigurator(Configurator configurator) {
        if ( log.isDebugEnabled() )
            log.debug("Setting "+configurator+" as default configurator.");
        ConfigurationManager.configurator = configurator;
    }

    /**
     * Reloads preferences from external file and reconfigures instances, that wished it.
     */
    public static void reconfigureAll() throws ConfigurationException {
        configurator.reconfigureAll();
    }

    /**
     * Sets default configurator from system properties.
     */
    protected static synchronized void init() {
        if ( configurator!=null ) return;
        String file = System.getProperty(PROPERTY_CONFIG_FILE, null);
        if ( file==null || file.length()==0 ) {
            log.fatal("You must set property "+ConfigurationManager.PROPERTY_CONFIG_FILE+"!");
            System.exit(1);
        }

        Configurator aConfigurator = new SimpleConfigurator(file);
        setConfigurator(aConfigurator);
    }

    /**
     * Sets default configurator from system properties.
     */
    public static synchronized void init(String file) {
        if ( configurator!=null ) return;
        if ( file==null || file.length()==0 ) {
            log.fatal("You must set provide valid file!");
            System.exit(1);
        }

        Configurator aConfigurator = new SimpleConfigurator(file);
        setConfigurator(aConfigurator);
    }
}
