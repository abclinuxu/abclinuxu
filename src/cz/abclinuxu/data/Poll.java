/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

import cz.abclinuxu.utils.Misc;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

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
    /** creation date of this poll */
    protected Date created;
    /** whether the user may select multiple choices */
    protected boolean multiChoice;


    public Poll() {
        super();
        content = Collections.EMPTY_LIST;
    }

    public Poll(int id) {
        super(id);
        content = Collections.EMPTY_LIST;
    }

    public Poll(int id, int type) {
        super(id);
        this.type = type;
        content = Collections.EMPTY_LIST;
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
        if ( obj==this ) return;
        super.synchronizeWith(obj);
        Poll b = (Poll) obj;
        text = b.getText();
        closed = b.isClosed();
        multiChoice = b.isMultiChoice();
        created = b.getCreated();
        type = b.getType();
        choices = b.getChoices();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        switch ( type ) {
            case 1: sb.append("Survey");break;
            case 2: sb.append("Rating");break;
            default: sb.append("Poll");
        }
        sb.append(": id="+id);
        if ( text!=null ) sb.append(",text="+text);
        sb.append(",closed="+closed);
        sb.append(",multichoice="+multiChoice);
        if ( choices!=null ) {
            sb.append(" [");
            for (int i = 0; i < choices.length; i++) {
                PollChoice choice = choices[i];
                sb.append(choice.text+":"+choice.count);
                if ( i<choices.length-1 ) sb.append("|");
            }
            sb.append("]");
        }
        return sb.toString();
    }

    public boolean preciseEquals(Object o) {
        if ( !( o instanceof Poll) ) return false;
        if ( id!=((GenericObject)o).getId() ) return false;
        if ( type!=((Poll)o).getType() ) return false;
        if ( ! Misc.same(this.text,((Poll)o).getText()) ) return false;
        return true;
    }

    public int hashCode() {
        String tmp = "Poll"+id;
        return tmp.hashCode();
    }
}
