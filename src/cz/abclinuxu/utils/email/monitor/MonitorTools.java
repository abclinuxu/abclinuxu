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

import org.dom4j.Element;
import cz.abclinuxu.data.User;

/**
 * Utility class for manipulation with monitors.
 */
public class MonitorTools {

    /**
     * Reverses current monitor settings for given user. E.g. if he didn't monitor
     * the object yet, he will. And if he did monitor, he will not.
     * @param root Usually root element of DOM4J tree, parent of monitor tag.
     * @param user User, which wants to aletr his monitor settings.
     */
    public static void alterMonitor(Element root, User user) {
        Element monitor = root.element("monitor");
        if ( monitor==null )
            monitor = root.addElement("monitor");
        Element id = (Element) monitor.selectSingleNode("//id[text()='"+user.getId()+"']");
        if ( id!=null )
            id.detach();
        else
            monitor.addElement("id").setText(new Integer(user.getId()).toString());
    }
    
    /**
     * If the user doesn't monitor the object, he will. If he already does,
     * no operation will be performed.
     * @param root Usually the root element of the DOM4J tree, the parent of the monitor tag.
     * @param user The user that wants to monitor the object.
     * @return Returns true if a monitor was added.
     */
    public static boolean addMonitor(Element root, User user) {
        Element monitor = root.element("monitor");
        if ( monitor==null )
            monitor = root.addElement("monitor");
        Element id = (Element) monitor.selectSingleNode("//id[text()='"+user.getId()+"']");
        if ( id == null ) {
            monitor.addElement("id").setText(new Integer(user.getId()).toString());
            return true;
        } else
            return false;
    }
}
