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
package cz.abclinuxu.utils.email.monitor;

import java.util.Map;

/**
 * Serializes object. It works as decorator - based on
 * input data and characteristics of the object it
 * initializes some data and finds correct template,
 * that will display the object.
 */
public interface Decorator {
    public static final String PREF_SUBJECT = "subject";

    public static final String PROPERTY_NAME = "NAME";

    public static final String VAR_URL = "URL";
    public static final String VAR_NAME = "NAME";
    public static final String VAR_ACTOR = "ACTOR";
    public static final String VAR_ACTION = "ACTION";
    public static final String VAR_PERFORMED = "PERFORMED";

    /**
     * Creates environment for given MonitorAction. This
     * environment will be used by template engine to
     * render notification for the user.
     * @param action MonitorAction to be decorated into Map.
     * @return environment
     */
    public Map getEnvironment(MonitorAction action);
}
