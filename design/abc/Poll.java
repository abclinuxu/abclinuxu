/*
 * Copyright Leos Literak 2001
 */
package abc;

import java.util.*;

/**
 * generic class for various polls
 */
public class Poll extends GenericObject {

    public static final int SURVEY = 1;
    public static final int RATING = 2;

    /** Specifies type of Item. You must set it, before you stores it with Persistance! */
    protected int type = 0;
    /** question of the poll */
    protected String text;
    /** Indicates, that poll is closed and no further voting is possible */
    protected boolean closed = false;
    /** list of the choices 
     * @clientCardinality 1
     * @supplierCardinality 1..**/
    protected PollChoice[] choices;
    /** creation date or last update of this object */
    protected Date updated;
    /** whether the user may select multiple choices */
    protected boolean multiChoice;


    /**
     * @return sum of votes of all poll choices
     */
    public int getTotalVotes() {
        return sum;
    }
}
