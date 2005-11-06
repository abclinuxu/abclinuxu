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

/**
 * Interface, that configures objects. Configurator can work in two modes: automatically
 * and upon request.<p>
 * At initialization time, it scans KEY_AUTOCONFIG top level node for presence of KEY_NODES.
 * If it is there, its value is tokenized by whitespace or coma to absolute names og node,
 * that shall be autoconfigured. These nodes are loaded and if they contain KEY_AUTOCONFIG,
 * its value is instantiated. If instantiated object implements Configurable,
 * than configure() with current node is called. Of course, public no argument
 * constructor is required.<p>
 * Second approach requires caller to get Configurator from
 * ConfigurationManager and then call configureMe() with class to be configured as an argument.
 */
public interface Configurator {
    /** Specifies fully qualified class name, that shall be instantiated and configured */
    public static final String KEY_AUTOCONFIG = "autoConfig";
    /** Specifies nodes, that shall be autoconfigured */
    public static final String KEY_NODES = "nodes";

    /**
     * Loads preferences for argument and calls configurable.configure().
     */
    public void configureMe(Configurable configurable) throws ConfigurationException;

    /**
     * Loads preferences for argument and calls configurable.configure(). It also
     * remembers the instance, so reconfiguration is possible.
     */
    public void configureAndRememberMe(Configurable configurable) throws ConfigurationException;

    /**
     * Reloads preferences from external file and reconfigures instances, that wished it.
     */
    public void reconfigureAll() throws ConfigurationException;
}
