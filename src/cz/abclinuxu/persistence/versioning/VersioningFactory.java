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
package cz.abclinuxu.persistence.versioning;

import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;

import java.util.prefs.Preferences;

/**
 * Factory that provides Versioning service.
 * User: literakl
 * Date: 27.3.2005
 */
public final class VersioningFactory implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VersioningFactory.class);

    public static final String PREF_IMPLEMENTING_CLASS = "impl.class";

    static {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(new VersioningFactory());
    }

    private static Versioning instance;

    /**
     * @return implementation of versioning
     */
    public static Versioning getVersioning() {
        return instance;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        String className = prefs.get(PREF_IMPLEMENTING_CLASS, "cz.abclinuxu.persistence.versioning.MysqlVersioningProvider");
        log.info("VersioningFactory will use the class "+className+".");
        try {
            Class aClass = Class.forName(className);
            if (!Versioning.class.isAssignableFrom(aClass))
                throw new ConfigurationException("Class '" + className + "' does not implement Versioning!");
            instance = (Versioning) aClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Class '" +className+"' was not found. Please specify correct implementation of Versioning!");
        } catch (InstantiationException e) {
            throw new ConfigurationException("Class '" + className + "' cannot be instantiated. Please specify correct implementation of Versioning!");
        } catch (IllegalAccessException e) {
            throw new ConfigurationException("Class '" + className + "' is not public. Please specify correct implementation of Versioning!");
        }
    }
}
