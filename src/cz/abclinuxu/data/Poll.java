/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

import cz.abclinuxu.utils.InstanceUtils;

import java.util.*;

/**
 * generic class for various polls
 */
public class Poll extends GenericObject {

    public static final int SURVEY = 1;
    public static final int RATING = 2;

    /** Specifies type of Item. You must set it, before you stores it with Persistance! */
    int type = 0;
    /** question of the poll */
    protected String text;
    /** Indicates, that poll is closed and no further voting is possible */
    boolean closed = false;
    /** list of the choices */
    protected PollChoice[] choices;
    /** creation date or last update of this object */
    protected Date updated;
    /** whether the user may select multiple choices */
    protected boolean multiChoice;


    public Poll() {
        super();
    }

    public Poll(int id) {
        super(id);
    }

    public Poll(int id, int type) {
        super(id);
        this.type = type;
    }

    /**
     * @return last updated (or creation) date
     */
    public Date getUpdated() {
        return updated;
    }

    /**
     * sets last updated (or creation) date
     */
    public void setUpdated(Date updated) {
        this.updated = updated;
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
            this.choices[i++] = choice;
        }
    }

    /**
     * @return whether the user may select multiple choices
     */
    public boolean isMultiChoice() {
        return false;
    }

    /**
     * sets whether the user may select multiple choices
     */
    public void setMultiChoice(boolean multiChoice) {
    }

    /**
     * @return sum of votes of all poll choices
     */
    public int getTotalVotes() {
        int sum = 0;

        for (int i = 0; i<choices.length; i++) {
            sum += choices[i].count;
        }
        return sum;
    }

    /**
     * @return Type of Record
     */
    public int getType() {
        return type;
    }

    /**
     * Sets type of Record
     */
    public void setType(int type) {
        this.type = type;
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
     * Initialize this object with values from <code>obj</code>, if
     * this.getClass.equals(obj.getClass()).
     */
    public void synchronizeWith(GenericObject obj) {
        if ( ! (obj instanceof Poll) ) return;
        super.synchronizeWith(obj);
        Poll b = (Poll) obj;
        text = b.getText();
        closed = b.isClosed();
        multiChoice = b.isMultiChoice();
        updated = b.getUpdated();
        type = b.getType();
        choices = b.getChoices();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        switch ( type ) {
            case 1: sb.append("Survey");break;
            case 2: sb.append("Rating");break;
            default: sb.append("Unknown Poll");
        }
        sb.append(": id="+id);
        if ( text!=null ) sb.append(",text="+text);
        return sb.toString();
    }

    public boolean preciseEquals(Object o) {
        if ( !( o instanceof Poll) ) return false;
        if ( id!=((GenericObject)o).getId() ) return false;
        if ( type!=((Poll)o).getType() ) return false;
        if ( ! InstanceUtils.same(this.text,((Poll)o).getText()) ) return false;
        return true;
    }

    public int hashCode() {
        String tmp = "Poll"+id;
        return tmp.hashCode();
    }
}
