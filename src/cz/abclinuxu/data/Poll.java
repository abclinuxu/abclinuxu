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
package cz.abclinuxu.data;

import cz.abclinuxu.utils.Misc;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * generic class for various polls
 */
public class Poll extends GenericObject implements Cloneable {

    public static final int SURVEY = 1;
    public static final int RATING = 2;

    /**
     * total number of voting user
     */
    int totalVoters = 0;
    /**
     * Id of owner of this object
     */
    int owner = 0;
    /** question of the poll */
    protected String text;
    /** Indicates, that poll is closed and no further voting is possible */
    boolean closed = false;
    /** list of the choices */
    protected PollChoice[] choices;
    /** creation date of this poll */
    protected Date created;
    /** whether the user may select multiple choices */
    protected boolean multiChoice;


    public Poll() {
        super();
    }

    public Poll(int id) {
        super(id);
    }

    /**
     * @return creation date
     */
    public Date getCreated() {
        return created;
    }

    /**
     * sets creation date
     */
    public void setCreated(Date updated) {
        this.created = updated;
    }

    /**
     * @return question of the poll
     */
    public String getText() {
        return text;
    }

    /**
     * sets question of the poll
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return list of the poll choices
     */
    public PollChoice[] getChoices() {
        return choices;
    }

    /**
     * sets list of the poll choices
     */
    public void setChoices(PollChoice[] choices) {
        this.choices = choices;
    }

    /**
     * sets list of the poll choices
     */
    public void setChoices(List choices) {
        this.choices = new PollChoice[choices.size()];
        int i = 0;

        for (Iterator iterator = choices.iterator(); iterator.hasNext();) {
            PollChoice choice = (PollChoice) iterator.next();
            choice.setId(i);
            this.choices[i++] = choice;
        }
    }

    /**
     * @return whether the user may select multiple choices
     */
    public boolean isMultiChoice() {
        return multiChoice;
    }

    /**
     * sets whether the user may select multiple choices
     */
    public void setMultiChoice(boolean multiChoice) {
        this.multiChoice = multiChoice;
    }

    /**
     * @return number of users who voted in this poll
     */
    public int getTotalVoters() {
        return totalVoters;
    }

    /**
     * Set number of users who voted in this poll
     */
    public void setTotalVoters(int totalVoters) {
        this.totalVoters = totalVoters;
    }

    /**
     * @return Whether it is possible to vote in this poll or not.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Sets, whether it is possible to vote in this poll or not.
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    /**
     * Gets owner of this poll.
     * @return id of owner of this poll
     */
    public int getOwner() {
        return owner;
    }

    /**
     * Sets owner of this poll.
     * @param owner if of owner of this poll
     */
    public void setOwner(int owner) {
        this.owner = owner;
    }

    /**
     * Initialize this object with values from <code>obj</code>, if
     * this.getClass.equals(obj.getClass()).
     */
    public void synchronizeWith(GenericObject obj) {
        if ( ! (obj instanceof Poll) ) return;
        if ( obj==this ) return;
        super.synchronizeWith(obj);
        Poll b = (Poll) obj;
        text = b.getText();
        closed = b.isClosed();
        multiChoice = b.isMultiChoice();
        created = b.getCreated();
        totalVoters = b.getTotalVoters();
        choices = b.getChoices();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Poll");
        sb.append(": id=").append(id);
        if ( text!=null ) sb.append(",text=").append(text);
        if (closed)
            sb.append(",closed");
        if (multiChoice)
            sb.append(",multiple choices allowed");
        if ( choices!=null ) {
            sb.append(" [");
            for (int i = 0; i < choices.length; i++) {
                PollChoice choice = choices[i];
                sb.append(choice.text).append(":").append(choice.count);
                if ( i<choices.length-1 ) sb.append("|");
            }
            sb.append("]");
        }
        return sb.toString();
    }

    public boolean preciseEquals(Object o) {
        if ( !( o instanceof Poll) ) return false;
        if ( id!=((GenericObject)o).getId() ) return false;
        if ( totalVoters!=((Poll)o).getTotalVoters() ) return false;
        return Misc.same(this.text, ((Poll) o).getText());
    }

    /**
     * Compares content fields of this and that GenericObject. The argument
     * must be instance of same class and have same content properties.
     * @param obj compared class
     * @return true if both instances have same content
     */
    public boolean contentEquals(GenericObject obj) {
        if (obj == this)
            return true;
        if (! super.contentEquals(obj))
            return false;
        Poll p = (Poll) obj;
        if (closed != p.closed)
            return false;
        if (multiChoice != p.multiChoice)
            return false;
        if (choices.length != p.choices.length )
            return false;
        for (int i = 0; i < choices.length; i++) {
            PollChoice choice = choices[i];
            if ( ! Misc.same(choice, p.choices[i]))
                return false;
        }
        return Misc.same(text, p.text);
    }

    public int hashCode() {
        String tmp = "Poll"+id;
        return tmp.hashCode();
    }
}
