/*
 * User: literakl
 * Date: 6.11.2003
 * Time: 12:13:25
 */
package cz.abclinuxu.utils.monitor;

import java.util.Map;

/**
 * Decorator for Discussions.
 */
public class DiscussionDecorator implements Decorator {

    /**
     * Creates environment for given MonitorAction. This
     * environment will be used by template engine to
     * render notification for the user.
     * @param action MonitorAction to be decorated into Map.
     * @return environment
     */
    public Map getEnvironment(MonitorAction action) {
        return null;
    }
}
