/*
 * User: literakl
 * Date: 6.11.2003
 * Time: 12:09:26
 */
package cz.abclinuxu.utils.email.monitor;

import org.dom4j.Element;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.GenericDataObject;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;

/**
 * Holder of data for monitor, when the event
 * was triggered.
 */
public class MonitorAction {
    /** user, whose action triggered this action */
    String actor;
    Integer actorId;
    UserAction action;
    ObjectType type;
    String url;
    Element monitor;
    GenericDataObject object;
    Date performed;
    Map map;

    /**
     * Creates new instance of MonitorAction.
     * @param user User, who performed the action. It must be initialized.
     * @param action What kind of action was performed.
     * @param type Type of GenericObject.
     * @param url URL, which can be opened by visitor.
     * @param object Affected GenericObject.
     */
    public MonitorAction(User user, UserAction action, ObjectType type, GenericDataObject object, String url) {
        this.actor = user.getNick()!=null ? user.getNick():user.getName();
        this.actorId = new Integer(user.getId());
        this.action = action;
        this.type = type;
        this.url = url;
        this.object = object;
    }

    /**
     * Creates new instance of MonitorAction.
     * @param actor Name of user, who performed the action.
     * @param action What kind of action was performed.
     * @param type Type of GenericObject.
     * @param url URL, which can be opened by visitor.
     * @param object Affected GenericObject.
     */
    public MonitorAction(String actor, UserAction action, ObjectType type, GenericDataObject object, String url) {
        this.actor = actor;
        this.actorId = new Integer(0);
        this.action = action;
        this.type = type;
        this.url = url;
        this.object = object;
    }

    public String toString() {
        return actor+": "+action+" on "+type+" "+object;
    }

    public String getActor() {
        return actor;
    }

    public Integer getActorId() {
        return actorId;
    }

    public UserAction getAction() {
        return action;
    }

    public ObjectType getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public Element getMonitor() {
        return monitor;
    }

    public GenericDataObject getObject() {
        return object;
    }

    public Date getPerformed() {
        return performed;
    }

    /**
     * Sets value identified by key as property.
     * @param key name of property
     * @param value property
     */
    public void setProperty(String key, Object value) {
        if (map==null)
            map = new HashMap(3,1.0f);
        map.put(key,value);
    }

    /**
     * Gets value of property identified by key.
     * @param key name of property
     * @return property
     */
    public Object getProperty(String key) {
        if (map==null)
            return null;
        return map.get(key);
    }
}
