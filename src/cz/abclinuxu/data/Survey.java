/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

/**
 * special case of Poll used for user's surveys
 */
public class Survey extends Poll {

    /** whether the user may select multiple choices */
    protected boolean multiChoice;


    public Survey(int id) {
        super(id);
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
}
