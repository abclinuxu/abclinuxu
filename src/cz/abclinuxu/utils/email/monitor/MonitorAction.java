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

import cz.abclinuxu.data.User;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;

import java.util.*;

/**
 * Holder of data for monitor, when the event
 * was triggered.
 */
public class MonitorAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MonitorAction.class);
    /** user, whose action triggered this action */
    protected String actor;
    protected Integer actorId;
    protected UserAction action;
    protected ObjectType type;
    protected String url;
    protected GenericDataObject object;
    protected Relation relation;
    protected Date performed;
    protected Map<String, Object> map;
    protected Set<Integer> recipients = new HashSet<Integer>();

    /**
     * Creates new instance of MonitorAction.
     * @param user User, who performed the action. It must be initialized.
     * @param action What kind of action was performed.
     * @param type Type of GenericObject.
     * @param url URL, which can be opened by visitor.
     * @param relation Parent of the affected GenericObject.
     */
    public MonitorAction(User user, UserAction action, ObjectType type, Relation relation, String url) {
        this.actor = user.getNick() != null ? user.getNick() : user.getName();
        this.actorId = user.getId();
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
        this.actorId = 0;
        this.action = action;
        this.type = type;
        this.url = url;
        this.relation = relation;
    }

    /**
     * Sets value identified by key as property.
     * @param key name of property
     * @param value property
     */
    public void setProperty(String key, Object value) {
        if (map == null)
            map = new HashMap<String, Object>(3, 1.0f);
        map.put(key,value);
    }

    /**
     * Gets value of property identified by key.
     * @param key name of property
     * @return property
     */
    public Object getProperty(String key) {
        if (map == null)
            return null;
        return map.get(key);
    }

    /**
     * Generates a list of notification's recipients by walking through the Relation tree.
     * User, who performed this action, will be skipped.
     */
    public void gatherRecipients() {
        Persistence persistence = PersistenceFactory.getPersistence();
        GenericDataObject obj = (GenericDataObject) persistence.findById(this.object);

        Relation rel = this.relation;
        do {
            recipients.addAll(MonitorTool.get(obj));

            int upper = rel.getUpper();
            if (upper == 0)
                break;

            rel = (Relation) persistence.findById(new Relation(upper));
            GenericObject genobj = rel.getChild();
            if (!(genobj instanceof GenericDataObject))
                break;

            obj = (GenericDataObject) persistence.findById(genobj);

            // send discussion changes only to people watching the discussion
            // or whole forum
            if (this.type == ObjectType.COMMENT || this.type == ObjectType.DISCUSSION)
                if (!(obj instanceof Category) || obj.getType() != Category.FORUM)
                    break;
        } while (true);

        recipients.remove(this.actorId);
    }

    public String toString() {
        return actor + ": " + action + " on " + type + " " + relation;
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

    public Set<Integer> getRecipients() {
        return recipients;
    }
}
