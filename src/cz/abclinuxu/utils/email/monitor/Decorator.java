package cz.abclinuxu.utils.email.monitor;

import java.util.Map;

/*
 * User: literakl
 * Date: 6.11.2003
 * Time: 12:13:05
 */

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
