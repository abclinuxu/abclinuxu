/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

/**
 * one choice of the poll. Its order in the poll is identified
 * by order in <code>choices</code> array of Poll.
 */
public class PollChoice {

    /**
     * text of the poll choice
     */
    protected String text;
    /**
     * number of votes for this choice
     */
    protected int count;


    public PollChoice(String text) {
        this.text = text;
    }

    /**
     * @return text of the poll choice
     */
    public String getText() {
        return text;
    }

    /**
     * sets text of the poll choice
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return number of votes for this choice
     */
    public int getCount() {
        return count;
    }

    /**
     * sets number of votes for this choice
     */
    public void setCount(int count) {
        this.count = count;
    }
}
