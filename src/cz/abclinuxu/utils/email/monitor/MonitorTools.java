/*
 * User: literakl
 * Date: 6.11.2003
 * Time: 16:40:06
 */
package cz.abclinuxu.utils.email.monitor;

import org.dom4j.Element;
import cz.abclinuxu.data.User;

/**
 * Utility class for manipulation with monitors.
 */
public class MonitorTools {

    /**
     * Reverts current monitor settings for given user. E.g. if he didn't monitor
     * the object yet, he will. And if he did monitor, he will not.
     * @param root Usually root element of DOM4J tree, parent of monitor tag.
     * @param user User, which wants to aletr his monitor settings.
     */
    public static void alterMonitor(Element root, User user) {
        Element monitor = root.element("monitor");
        if ( monitor==null )
            monitor = root.addElement("monitor");
        Element id = (Element) monitor.selectSingleNode("//id[text()='"+user.getId()+"'");
        if ( id!=null )
            id.detach();
        else
            monitor.addElement("id").setText(new Integer(user.getId()).toString());
    }
}
