/*
 * Copyright Leos Literak 2001
 */
package abc;

import java.util.Date;

public class Item extends GenericDataObject {

    /** Leaf of the category. It contains at least one hardware or software record. */
    public static final int MAKE = 1;
    /** Article header. The article is consisted from this header and at least one Article record. */
    public static final int ARTICLE = 2;
    /** Discussion may contain one DiscussionQuestion and many DiscussionItems. */
    public static final int DISCUSSION = 3;
    /** User's request to administrators. */
    public static final int REQUEST = 4;

    /** Specifies type of Item. You must set it, before you stores it with Persistance! */
    protected int type = 0;

}
