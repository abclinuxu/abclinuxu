/*
 * Copyright Leos Literak 2001
 */
package abc;

import java.util.Date;

public class Record extends GenericDataObject {

    public static final int HARDWARE = 1;
    public static final int SOFTWARE = 2;
    /** part of the article, each article is consisted from article header and at least one record */
    public static final int ARTICLE = 3;
    /** initial question in the discussion */
    public static final int DISCUSSION_QUESTION = 4;
    /** one reaction in Discussion */
    public static final int DISCUSSION_ITEM = 5;

    /** Specifies type of record. You must set it, before you stores it with Persistance! */
    protected int type = 0;
}
