/*
 * User: literakl
 * Date: 13.9.2003
 * Time: 17:42:09
 */
package cz.abclinuxu.security;

/**
 * Defines available roles, that users may play.
 */
public class Roles {

    /** this role grants the user all rights */
    public static final String ROOT = "root";
    /** user can move relations */
    public static final String CAN_MOVE_RELATION = "move relation";
    /** user can remove relations and objects */
    public static final String CAN_REMOVE_RELATION = "remove relation";
    /** user can create, edit and delete categories */
    public static final String CATEGORY_ADMIN = "category admin";
    /** user can edit, move and remove discussions */
    public static final String DISCUSSION_ADMIN = "discussion admin";
    /** user can edit, approve and delete news */
    public static final String NEWS_ADMIN = "news admin";
    /** user can create, edit, move and delete articles */
    public static final String ARTICLE_ADMIN = "article admin";
    /** user can create, edit and delete surveys */
    public static final String SURVEY_ADMIN = "survey admin";
    /** user can create, edit and delete polls */
    public static final String POLL_ADMIN = "poll admin";
    /** user can edit other users */
    public static final String USER_ADMIN = "user admin";
    /** user can invalidate emails of other users */
    public static final String CAN_INVALIDATE_EMAILS = "email invalidator";
}
