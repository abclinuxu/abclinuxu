/*
 * User: literakl
 * Date: 7.11.2003
 * Time: 8:17:39
 */
package cz.abclinuxu.utils.monitor;

/**
 * Enumaration of available action, that user can perform on monitored objects.
 */
public class UserAction {
    private final String name;

    public static final UserAction ADD = new UserAction("add");
    public static final UserAction EDIT = new UserAction("edit");
    public static final UserAction REMOVE = new UserAction("remove");
    public static final UserAction CENSORE = new UserAction("censore");

    private UserAction(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
