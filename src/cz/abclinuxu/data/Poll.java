/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

import java.util.*;

/**
 * generic class for various polls
 */
public class Poll extends GenericObject {
    /** question of the poll */
    protected String text;
    /** list of the choices */
    protected PollChoice[] choices;
    /** creation date or last update of this object */
    protected Date updated;
    /** whether the user may select multiple choices */
    protected boolean multiChoice;


    public Poll(int id) {
        super(id);
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
}
