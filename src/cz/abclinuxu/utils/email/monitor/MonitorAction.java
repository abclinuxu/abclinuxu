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
import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.utils.freemarker.Tools;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Holder of data for monitor, when the event
 * was triggered.
 */
public class MonitorAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MonitorAction.class);
    /** user, whose action triggered this action */
    String actor;
    Integer actorId;
    UserAction action;
    ObjectType type;
    String url;
    GenericDataObject object;
    Relation relation;
    Date performed;
    Map map;
    List<Integer> recipients;

    /**
     * Creates new instance of MonitorAction.
     * @param user User, who performed the action. It must be initialized.
     * @param action What kind of action was performed.
     * @param type Type of GenericObject.
     * @param url URL, which can be opened by visitor.
     * @param relation Parent of the affected GenericObject.
     */
    public MonitorAction(User user, UserAction action, ObjectType type, Relation relation, String url) {
        this.actor = user.getNick()!=null ? user.getNick():user.getName();
        this.actorId = new Integer(user.getId());
        this.action = action;
        this.type = type;
        this.url = url;
        this.relation = relation;
    }

    /**
     * Creates new instance of MonitorAction.
     * @param actor Name of user, who performed the action.
     * @param action What kind of action was performed.
     * @param type Type of GenericObject.
     * @param url URL, which can be opened by visitor.
     * @param relation Parent of the affected GenericObject.
     */
    public MonitorAction(String actor, UserAction action, ObjectType type, Relation relation, String url) {
        this.actor = actor;
        this.actorId = new Integer(0);
        this.action = action;
        this.type = type;
        this.url = url;
        this.relation = relation;
    }

    public String toString() {
        return actor+": "+action+" on "+type+" "+relation;
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

    public GenericDataObject getObject() {
        return object;
    }

    public Relation getRelation() {
        return relation;
    }

    public Date getPerformed() {
        return performed;
    }

    public List<Integer> getRecipients() {
        return recipients;
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

    /**
     * Generates a list of notification's recipients by walking through the Relation tree.
     * User, who performed this action, will be skipped.
     */
    public void gatherRecipients() {
        Relation rel = this.relation;
        Integer actor = Integer.valueOf(this.actorId);
        GenericDataObject obj = this.object;

        recipients = new ArrayList();

        Tools.sync(obj);

        do {
            Element monitor = (Element) obj.getData().selectSingleNode("//monitor");

            if (monitor != null) {
                List keys = monitor.elements("id");
                for ( Iterator iter = keys.iterator(); iter.hasNext(); ) {
                    Element id = (Element) iter.next();
                    try {
                        Integer key = Integer.valueOf(id.getTextTrim());
                        if ( !actor.equals(key) && !recipients.contains(key) )
                            recipients.add(key);
                    } catch (NumberFormatException e) {
                        log.error("Error in XML in object "+this.getObject(), e);
                    }
                }
            }

            int upper = rel.getUpper();
            if ( upper == 0)
                rel = null;
            else {
                rel = new Relation(upper);
                Tools.sync(rel);

                obj = (GenericDataObject) rel.getChild();

                // send discussion changes only to people watching the discussion
                // or whole forum
                if ( ObjectType.DISCUSSION.equals(this.type) ) {
                    if ( ! (obj instanceof Category) || ((Category) obj).getType() != Category.FORUM )
                        rel = null;
                }
            }
        } while (rel != null);
    }
}
