/*
 * User: Leos Literak
 * Date: Jun 21, 2003
 * Time: 11:42:59 AM
 */
package cz.abclinuxu.persistance;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.data.*;
import cz.abclinuxu.exceptions.PersistanceException;
import cz.abclinuxu.persistance.extra.*;
import cz.abclinuxu.persistance.impl.MySqlPersistance;

import java.util.prefs.Preferences;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.sql.*;

/**
 * Thread-safe singleton, that encapsulates SQL commands
 * used outside of Persistance implementations.
 */
public final class SQLTool implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SQLTool.class);

    public static final String PREF_RECORD_RELATIONS_BY_TYPE = "relations.record.by.type";
    public static final String PREF_RECORD_PARENT_RELATIONS_BY_TYPE = "relations.parent.record.by.type";
    public static final String PREF_ITEM_RELATIONS_BY_TYPE = "relations.item.by.type";
    public static final String PREF_SECTION_RELATIONS_BY_TYPE = "relations.section.by.type";
    public static final String PREF_DISCUSSION_RELATIONS = "relations.discussion";
    public static final String PREF_DISCUSSION_RELATIONS_IN_SECTION = "relations.discussion.in.section";
    public static final String PREF_ARTICLE_RELATIONS = "relations.article";
    public static final String PREF_ARTICLES_ON_INDEX_RELATIONS = "relations.article.on.index";
    // todo calculate it dynamically with findqualifier
    public static final String PREF_ARTICLE_RELATIONS_WITHIN_PERIOD = "relations.article.within.period";
    public static final String PREF_NEWS_RELATIONS = "relations.news";
    // todo calculate it dynamically with findqualifier
    public static final String PREF_NEWS_RELATIONS_WITHIN_PERIOD = "relations.news.within.period";
    public static final String PREF_RECORD_RELATIONS_BY_USER_AND_TYPE = "relations.record.by.user.and.type";
    public static final String PREF_ARTICLE_RELATIONS_BY_USER = "relations.article.by.user";
    public static final String PREF_NEWS_RELATIONS_BY_USER = "relations.news.by.user";
    public static final String PREF_QUESTION_RELATIONS_BY_USER = "relations.question.by.user";
    public static final String PREF_COMMENT_RELATIONS_BY_USER = "relations.comment.by.user";
    public static final String PREF_USERS_WITH_WEEKLY_EMAIL = "users.with.weekly.email";
    public static final String PREF_USERS_WITH_FORUM_BY_EMAIL = "users.with.forum.by.email";
    public static final String PREF_USERS_WITH_ROLES = "users.with.roles";
    public static final String PREF_USERS_IN_GROUP = "users.in.group";
    public static final String PREF_ITEMS_WITH_TYPE = "items.with.type";
    public static final String PREF_MAX_POLL = "max.poll";
    public static final String PREF_MAX_USER = "max.user";
    public static final String PREF_USER_BY_LOGIN = "user.by.login";
    public static final String PREF_COUNT_ARTICLES_BY_USER = "count.articles.by.user";
    public static final String PREF_DICTIONARY_RELATION_BY_URL_NAME = "relation.dictionary.by.urlname";
    public static final String PREF_RELATION_BY_URL = "relation.by.url";
    public static final String PREF_INSERT_LAST_COMMENT = "insert.last.comment";
    public static final String PREF_GET_LAST_COMMENT = "get.last.comment";
    public static final String PREF_GET_OLD_COMMENT = "get.xth.comment.date";
    public static final String PREF_DELETE_OLD_COMMENTS = "delete.old.comments";
    public static final String PREF_INSERT_USER_ACTION = "insert.user.action";
    public static final String PREF_GET_USER_ACTION = "get.user.action";
    public static final String PREF_REMOVE_USER_ACTION = "remove.user.action";

    private static SQLTool singleton;

    static {
        singleton = new SQLTool();
        ConfigurationManager.getConfigurator().configureAndRememberMe(singleton);
    }

    // todo presun vsechny stringy do Mapy
    private String relationsRecordByType, relationsParentRecordByType, relationsItemsByType, relationsSectionByType;
    private String relationsDiscussion, relationsDiscussionInSection;
    private String relationsArticle, relationsArticleWithinPeriod, relationsArticlesOnIndex;
    private String relationsNews, relationsNewsWithinPeriod;
    private String relationsNewsByUser, relationsRecordByUserAndType, relationsArticleByUser;
    private String relationsQuestionsByUser, relationsCommentsByUser;
    private String relationDictionaryByUrlName, relationByURL;
    private String usersWithWeeklyEmail, usersWithForumByEmail, usersWithRoles, usersInGroup;
    private String maxPoll, maxUser, userByLogin;
    private String itemsByType;
    private String countArticlesByUser;
    private String insertLastComment, getLastComment, getXthComment, deleteOldComments;
    private String insertUserAction, getUserAction, removeUserAction;


    /**
     * Returns singleton of SQLTool.
     */
    public static SQLTool getInstance() {
        return singleton;
    }

    /**
     * Loads relations from database using given SQL command. If list params is not
     * empty, PreparedStatement is created and fed up from params.
     * @param sql Command to execute.
     * @param params List of parameters. It must not be null.
     * @return List of initialized relations.
     * @throws PersistanceException if something goes wrong.
     */
    private List loadRelations(String sql, List params) throws PersistanceException {
        if (log.isDebugEnabled())
            log.debug(sql);
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql);
            int i = 1;
            for ( Iterator iter = params.iterator(); iter.hasNext(); )
                statement.setObject(i++, iter.next());

            resultSet = statement.executeQuery();
            List result = new ArrayList();
            while ( resultSet.next() ) {
                int id = resultSet.getInt(1);
                Relation relation = (Relation) persistance.findById(new Relation(id));
                result.add(relation);
            }
            return result;
        } catch (SQLException e) {
            throw new PersistanceException("Nemohu vykonat SQL pøíkaz "+sql, e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Loads users from database using given SQL command. If list params is not
     * empty, PreparedStatement is created and fed up from params.
     * @param sql Command to execute.
     * @param params List of parameters. It must not be null.
     * @return List of Integers.
     * @throws PersistanceException if something goes wrong.
     */
    private List loadUsers(String sql, List params) throws PersistanceException {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql);
            int i = 1;
            for ( Iterator iter = params.iterator(); iter.hasNext(); )
                statement.setObject(i++, iter.next());

            resultSet = statement.executeQuery();
            List result = new ArrayList();
            while ( resultSet.next() ) {
                result.add(new Integer(resultSet.getInt(1)));
            }
            return result;
        } catch (SQLException e) {
            throw new PersistanceException("Nemohu vykonat SQL pøíkaz "+sql, e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Gets number from database using given SQL command. If list params is not
     * empty, PreparedStatement is created and fed up from params.
     * @param sql Command to execute.
     * @param params List of parameters. It must not be null.
     * @return integer or null.
     * @throws PersistanceException if something goes wrong.
     */
    private Integer loadNumber(String sql, List params) throws PersistanceException {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql);
            int i = 1;
            for ( Iterator iter = params.iterator(); iter.hasNext(); )
                statement.setObject(i++, iter.next());

            resultSet = statement.executeQuery();
            if (!resultSet.next())
                return null;

            i = resultSet.getInt(1);
            return new Integer(i);
        } catch (SQLException e) {
            throw new PersistanceException("Nemohu vykonat SQL pøíkaz "+sql, e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Gets date from database using given SQL command. If list params is not
     * empty, PreparedStatement is created and fed up from params.
     * @param sql Command to execute.
     * @param params List of parameters. It must not be null.
     * @return date or null.
     * @throws PersistanceException if something goes wrong.
     */
    private Date loadDate(String sql, List params) throws PersistanceException {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql);
            int i = 1;
            for ( Iterator iter = params.iterator(); iter.hasNext(); )
                statement.setObject(i++, iter.next());

            resultSet = statement.executeQuery();
            if (!resultSet.next())
                return null;

            Date date = resultSet.getTimestamp(1);
            return new Date(date.getTime());
        } catch (SQLException e) {
            throw new PersistanceException("Nemohu vykonat SQL pøíkaz "+sql, e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Appends qualifiers to StringBufefr holding SQL command.
     * @param tableNick nick of table to distinguish columns. Default is null.
     */
    private void appendQualifiers(StringBuffer sb, Qualifier[] qualifiers, List params, String tableNick) {
        Qualifier qualifier;
        boolean sort = false;
        for ( int i = 0; i<qualifiers.length; i++ ) {
            qualifier = qualifiers[i];
            if ( qualifier.equals(Qualifier.SORT_BY_CREATED) ) {
                sb.append(" order by ");
                if (tableNick != null) {
                    sb.append(tableNick);
                    sb.append(".");
                }
                sb.append("vytvoreno");
                sort = true;
            } else if ( qualifier.equals(Qualifier.SORT_BY_UPDATED) ) {
                sb.append(" order by ");
                if (tableNick != null) {
                    sb.append(tableNick);
                    sb.append(".");
                }
                sb.append("zmeneno");
                sort = true;
            } else if ( qualifier.equals(Qualifier.SORT_BY_ID) ) {
                sb.append(" order by ");
                if (tableNick != null) {
                    sb.append(tableNick);
                    sb.append(".");
                }
                sb.append("cislo");
                sort = true;
            } else if ( sort && qualifier.equals(Qualifier.ORDER_ASCENDING) ) {
                sb.append(" asc");
            } else if ( sort && qualifier.equals(Qualifier.ORDER_DESCENDING) ) {
                sb.append(" desc");
            } else if ( qualifier instanceof LimitQualifier ) {
                sb.append(" limit ?,?");
                LimitQualifier limitQualifier = (LimitQualifier) qualifier;
                params.add(new Integer(limitQualifier.getOffset()));
                params.add(new Integer(limitQualifier.getCount()));
            } else if ( qualifier instanceof CompareCondition )
                appendCompareCondition(sb, (CompareCondition) qualifier,  params);
        }
    }

    /**
     * Changes first select clause to count. E.g. from id to count(id).
     */
    private void changeToCountStatement(StringBuffer sb) {
        int position = sb.indexOf(" ");
        sb.insert(position+1, "count(");
        position = sb.indexOf("from", position+6);
        sb.insert(position-1, ')');
    }

    /**
     * Finds relations, where child is record of specified type. Use Qualifiers
     * to set additional parameters.
     * @return List of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findRecordRelationsWithType(int type, Qualifier[] qualifiers) {
        if (qualifiers==null) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsRecordByType);
        List params = new ArrayList();
        params.add(new Integer(type));
        appendQualifiers(sb,qualifiers,params, null);
        return loadRelations(sb.toString(),params);
    }

    /**
     * Finds relations, where child is record of specified type. Parent relations (distinct) are returned.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findRecordParentRelationsWithType(int type, Qualifier[] qualifiers) {
        if (qualifiers==null) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsParentRecordByType);
        List params = new ArrayList();
        params.add(new Integer(type));
        appendQualifiers(sb,qualifiers,params, null);
        return loadRelations(sb.toString(),params);
    }

    /**
     * Finds relations, where child is record of specified type owned by given user.
     * Parent relations (distinct) are returned.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findRecordParentRelationsByUserAndType(int user, int type, Qualifier[] qualifiers) {
        if (qualifiers==null) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsParentRecordByType);
        sb.append(" and pridal=?");
        List params = new ArrayList();
        params.add(new Integer(type));
        params.add(new Integer(user));
        appendQualifiers(sb,qualifiers,params, null);
        return loadRelations(sb.toString(),params);
    }

    /**
     * Counts relations, where child is record of specified type.
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public int countRecordRelationsWithType(int type) {
        StringBuffer sb = new StringBuffer(relationsRecordByType);
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(new Integer(type));
        return loadNumber(sb.toString(),params).intValue();
    }

    /**
     * Counts relations, where child is record of specified type. Operates on distinct set of parent.
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public int countRecordParentRelationsWithType(int type) {
        StringBuffer sb = new StringBuffer(relationsParentRecordByType);
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(new Integer(type));
        return loadNumber(sb.toString(),params).intValue();
    }

    /**
     * Counts relations, where child is record of specified type owned by given user. Operates on distinct set of parent.
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public int countRecordParentRelationsByUserAndType(int user, int type) {
        StringBuffer sb = new StringBuffer(relationsParentRecordByType);
        sb.append(" and pridal=?");
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(new Integer(type));
        params.add(new Integer(user));
        return loadNumber(sb.toString(),params).intValue();
    }

    /**
     * Finds relations, where child is item of specified type.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findItemRelationsWithType(int type, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsItemsByType);
        List params = new ArrayList();
        params.add(new Integer(type));
        appendQualifiers(sb, qualifiers, params, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is item of specified type.
     * Use Qualifiers to set additional parameters.
     * @throws PersistanceException if there is an error with the underlying persistent storage.
     */
    public int countItemRelationsWithType(int type, Qualifier[] qualifiers) {
        StringBuffer sb = new StringBuffer(relationsItemsByType);
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(new Integer(type));
        appendQualifiers(sb, qualifiers, params, null);
        return loadNumber(sb.toString(), params).intValue();
    }

    /**
     * Finds relations, where child is a category of specified type.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findSectionRelationsWithType(int type, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsSectionByType);
        List params = new ArrayList();
        params.add(new Integer(type));
        appendQualifiers(sb, qualifiers, params, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is a category of specified type.
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public int countFolderRelationsWithType(int type) {
        StringBuffer sb = new StringBuffer(relationsSectionByType);
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(new Integer(type));
        return loadNumber(sb.toString(), params).intValue();
    }

    /**
     * Finds relations, where child is discussion item.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findDiscussionRelations(Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsDiscussion);
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is discussion item.
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public int countDiscussionRelations() {
        StringBuffer sb = new StringBuffer(relationsDiscussion);
        changeToCountStatement(sb);
        List params = new ArrayList();
        return loadNumber(sb.toString(), params).intValue();
    }

    /**
     * Finds relations, where child is discussion item with specified parent.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findDiscussionRelationsWithParent(int parent, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsDiscussionInSection);
        List params = new ArrayList();
        params.add(new Integer(parent));
        appendQualifiers(sb, qualifiers, params, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is discussion item with specified parent.
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public int countDiscussionRelationsWithParent(int parent) {
        StringBuffer sb = new StringBuffer(relationsDiscussionInSection);
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(new Integer(parent));
        return loadNumber(sb.toString(), params).intValue();
    }

    /**
     * Finds relations, where child is article item in the section folder.
     * Items with property created set to future and subtype equal to NOINDEX are skipped.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findIndexArticlesRelations(Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsArticlesOnIndex);
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, "P");
        return loadRelations(sb.toString(), params);
    }

    /**
     * Finds relations, where child is article item.
     * Items with property created set to future and outside of typical columns are skipped.
     * Use Qualifiers to set additional parameters.
     * @param section id of section to be searched. If equal to 0, than all sections will be searched
     * @return List of initialized relations
     * @throws PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findArticleRelations(Qualifier[] qualifiers, int section) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsArticle);
        List params = new ArrayList();
        if (section>0) {
            params.add(new Integer(section));
            sb.append(" and K.cislo=?");
        }
        appendQualifiers(sb, qualifiers, params, "P");
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is article item.
     * Items with property created set to future and outside of typical columns are skipped.
     * @param section id of section to be searched. If equal to 0, than all sections will be searched
     * @throws PersistanceException if there is an error with the underlying persistent storage.
     */
    public int countArticleRelations(int section) {
        StringBuffer sb = new StringBuffer(relationsArticle);
        changeToCountStatement(sb);
        List params = new ArrayList();
        if (section > 0) {
            params.add(new Integer(section));
            sb.append(" and K.cislo=?");
        }
        return loadNumber(sb.toString(), params).intValue();
    }

    /**
     * Finds relations, where child is an article item with created property,
     * that is inside given time period.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     */
    public List findArticleRelationsWithinPeriod(Date from, Date until, Qualifier[] qualifiers) throws PersistanceException {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsArticleWithinPeriod);
        List params = new ArrayList();
        params.add(new java.sql.Date(from.getTime()));
        params.add(new java.sql.Date(until.getTime()));
        appendQualifiers(sb, qualifiers, params, "P");
        return loadRelations(sb.toString(), params);
    }

    /**
     * Finds relations, where child is an article item with created property,
     * that is inside given time period.
     */
    public int countArticleRelationsWithinPeriod(Date from, Date until) throws PersistanceException {
        StringBuffer sb = new StringBuffer(relationsArticleWithinPeriod);
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(new java.sql.Date(from.getTime()));
        params.add(new java.sql.Date(until.getTime()));
        return loadNumber(sb.toString(), params).intValue();
    }

    /**
     * Finds relations, where child is news item .
     * Items with created property in future and in newspool are skipped.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findNewsRelations(Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsNews);
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is news item .
     * Items with created property in future and in newspool are skipped.
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public int countNewsRelations() {
        StringBuffer sb = new StringBuffer(relationsNews);
        changeToCountStatement(sb);
        List params = new ArrayList();
        return loadNumber(sb.toString(), params).intValue();
    }

    /**
     * Finds relations, where child is an article item with created property, that is inside
     * given time period.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     */
    public List findNewsRelationsWithinPeriod(Date from, Date until, Qualifier[] qualifiers) throws PersistanceException {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsNewsWithinPeriod);
        List params = new ArrayList();
        params.add(new java.sql.Date(from.getTime()));
        params.add(new java.sql.Date(until.getTime()));
        appendQualifiers(sb, qualifiers, params, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is an article item with created property, that is inside
     * given time period.
     */
    public int countNewsRelationsWithinPeriod(Date from, Date until) throws PersistanceException {
        StringBuffer sb = new StringBuffer(relationsNewsWithinPeriod);
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(new java.sql.Date(from.getTime()));
        params.add(new java.sql.Date(until.getTime()));
        return loadNumber(sb.toString(), params).intValue();
    }

    /**
     * Finds user's relations, where child is with given type.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findRecordRelationsWithUserAndType(int userId, int type, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsRecordByUserAndType);
        List params = new ArrayList();
        params.add(new Integer(userId));
        params.add(new Integer(type));
        appendQualifiers(sb, qualifiers, params, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts user's relations, where child is with given type.
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public int countRecordRelationsWithUserAndType(int userId, int type) {
        StringBuffer sb = new StringBuffer(relationsRecordByUserAndType);
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(new Integer(userId));
        params.add(new Integer(type));
        return loadNumber(sb.toString(), params).intValue();
    }

    /**
     * Finds relations, where child is article submitted by user.
     * Items with property created set to future and outside of typical columns are skipped.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findArticleRelationsByUser(int userId, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsArticleByUser);
        List params = new ArrayList();
        params.add(new Integer(userId));
        appendQualifiers(sb, qualifiers, params, "P");
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is article submitted by user.
     * Items with property created set to future and outside of typical columns are skipped.
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public int countArticleRelationsByUser(int userId) {
        StringBuffer sb = new StringBuffer(countArticlesByUser);
        List params = new ArrayList();
        params.add(new Integer(userId));
        return loadNumber(sb.toString(), params).intValue();
    }

    /**
     * Finds relations, where child is news item submitted by user.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findNewsRelationsByUser(int userId, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsNewsByUser);
        List params = new ArrayList();
        params.add(new Integer(userId));
        appendQualifiers(sb, qualifiers, params, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is news item submitted by user.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public int countNewsRelationsByUser(int userId) {
        StringBuffer sb = new StringBuffer(relationsNewsByUser);
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(new Integer(userId));
        return loadNumber(sb.toString(), params).intValue();
    }

    /**
     * Finds relations, where child is question item submited by user.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findQuestionRelationsByUser(int userId, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsQuestionsByUser);
        List params = new ArrayList();
        params.add(new Integer(userId));
        appendQualifiers(sb, qualifiers, params, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is question item submited by user.
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public int countQuestionRelationsByUser(int userId) {
        StringBuffer sb = new StringBuffer(relationsQuestionsByUser);
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(new Integer(userId));
        return loadNumber(sb.toString(), params).intValue();
    }


    /**
     * Finds relations, where child is discussion item with comment from user.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findCommentRelationsByUser(int userId, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsCommentsByUser);
        List params = new ArrayList();
        params.add(new Integer(userId));
        appendQualifiers(sb, qualifiers, params, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is discussion item with comment from user.
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public int countCommentRelationsByUser(int userId) {
        StringBuffer sb = new StringBuffer(relationsCommentsByUser);
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(new Integer(userId));
        return loadNumber(sb.toString(), params).intValue();
    }

    /**
     * Finds dictionary item identified by urlName.
     * @param urlName name to be used in URI
     * @return relation of dictionary item or null
     */
    public Relation findDictionaryByURLName(String urlName) {
        List params = new ArrayList();
        params.add(urlName);
        List result = loadRelations(relationDictionaryByUrlName, params);
        if (result.size()==0)
            return null;
        return (Relation) result.get(0);
    }

    /**
     * Finds relation identified by given url.
     * @param url URL identification of resource
     * @return found relation or null
     */
    public Relation findRelationByURL(String url) {
        if ( url!=null && url.endsWith("/") )
            url = url.substring(0, url.length()-1);
        List params = new ArrayList();
        params.add(url);
        List result = loadRelations(relationByURL, params);
        if (result.size()==0)
            return null;
        return (Relation) result.get(0);
    }

    /**
     * Finds users, that have active email and have subscribed weekly email.
     * Use Qualifiers to set additional parameters.
     * @return list of Integers of user ids.
     */
    public List findUsersWithWeeklyEmail(Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(usersWithWeeklyEmail);
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null);
        return loadUsers(sb.toString(), params);
    }

    /**
     * Finds users, that have active email and have subscribed forum by email.
     * Use Qualifiers to set additional parameters.
     * @return list of Integers of user ids.
     */
    public List findUsersWithForumByEmail(Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(usersWithForumByEmail);
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null);
        return loadUsers(sb.toString(), params);
    }

    /**
     * Finds users, that have at least one role.
     * Use Qualifiers to set additional parameters.
     * @return list of Integers of user ids.
     */
    public List findUsersWithRoles(Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(usersWithRoles);
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null);
        return loadUsers(sb.toString(), params);
    }

    /**
     * Finds users, that are members of given group.
     * Use Qualifiers to set additional parameters.
     * @return list of Integers of user ids.
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findUsersInGroup(int group, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(usersInGroup);
        List params = new ArrayList();
        params.add(new Integer(group));
        appendQualifiers(sb, qualifiers, params, null);
        return loadUsers(sb.toString(), params);
    }

    /**
     * Finds users, that are members of given group.
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public int countUsersInGroup(int group) {
        StringBuffer sb = new StringBuffer(usersInGroup);
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(new Integer(group));
        return loadNumber(sb.toString(), params).intValue();
    }

    /**
     * Finds user with specified login.
     * @return id of found user or null, if there is no such user
     * @throws cz.abclinuxu.exceptions.PersistanceException
     *          if there is an error with the underlying persistent storage.
     */
    public Integer getUserByLogin(String login) {
        StringBuffer sb = new StringBuffer(userByLogin);
        List params = new ArrayList();
        params.add(login);
        return loadNumber(sb.toString(), params);
    }

    /**
     * Finds the last poll. If it is not active, null is returned.
     * @return last initialized poll, if it is active, null otherwise.
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistant storage.
     */
    public Relation findActivePoll() {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.createStatement();
            resultSet = statement.executeQuery(maxPoll);
            resultSet.next();

            int id = resultSet.getInt(1);
            Poll poll = (Poll) persistance.findById(new Poll(id));
            if (poll.isClosed())
                return null;
            List relations = persistance.findRelations(poll);
            return (Relation) relations.get(0);
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pri hledani!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds maximum id between users.
     */
    public int getMaximumUserId() {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.createStatement();
            resultSet = statement.executeQuery(maxUser);
            if ( !resultSet.next() )
                return 0;
            Integer id = new Integer(resultSet.getInt(1));
            return id.intValue();
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pri hledani!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Sets updated timestamp in database for given GenericObject.
     * @param obj object to be modified.
     * @param date timestamp to be set.
     * @throws PersistanceException if object doesn't contain such property.
     */
    public void setUpdatedTimestamp(GenericDataObject obj, Date date) {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        obj.setUpdated(date);
        persistance.storeInCache(obj);

        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = persistance.getSQLConnection();
            String sql = "update "+persistance.getTable(obj)+" set zmeneno=? where cislo=?";
            statement = con.prepareStatement(sql);
            statement.setTimestamp(1, new Timestamp(date.getTime()));
            statement.setInt(2, obj.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pri hledani!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, null);
        }
    }

    /**
     * Finds items of given type ordered by id property in ascending order.
     * Use offset to skip some record .
     * @return List of itialized Items
     * @throws PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findItemsWithType(int type, int offset, int count) {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(itemsByType.concat(" order by cislo asc limit ?,?"));
            statement.setInt(1, type);
            statement.setInt(2, offset);
            statement.setInt(3, count);

            resultSet = statement.executeQuery();
            List result = new ArrayList(count);
            while ( resultSet.next() ) {
                int id = resultSet.getInt(1);
                Item item = (Item) persistance.findById(new Item(id));
                result.add(item);
            }
            return result;
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pri hledani!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds items of given type and subtype ordered by id property in ascending order.
     * @return List of itialized Items
     * @throws PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findItemsWithTypeAndSubtype(int type, String subType) {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(itemsByType.concat(" and podtyp=? order by cislo asc"));
            statement.setInt(1, type);
            statement.setString(2, subType);

            resultSet = statement.executeQuery();
            List result = new ArrayList();
            while ( resultSet.next() ) {
                int id = resultSet.getInt(1);
                Item item = (Item) persistance.findById(new Item(id));
                result.add(item);
            }
            return result;
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pri hledani!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds dictionary items that alphabetically neighbours selected one.
     * @param smaller whether the returned items shall be smaller or greater
     * @return List of itialized Items, first item is closest to selected one.
     * @throws PersistanceException if there is an error with the underlying persistent storage.
     */
    public List getNeighbourDictionaryItems(String urlName, boolean smaller, int count) {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            if (smaller)
                statement = con.prepareStatement(itemsByType.concat("  and podtyp<? order by podtyp desc limit ?"));
            else
                statement = con.prepareStatement(itemsByType.concat("  and podtyp>? order by podtyp asc limit ?"));
            statement.setInt(1, Item.DICTIONARY);
            statement.setString(2, urlName);
            statement.setInt(3, count);

            resultSet = statement.executeQuery();
            List result = new ArrayList(count);
            while ( resultSet.next() ) {
                int id = resultSet.getInt(1);
                Item item = (Item) persistance.findById(new Item(id));
                result.add(item);
            }
            return result;
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pri hledani!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds items of given type.
     * @return Number of matched items
     * @throws PersistanceException if there is an error with the underlying persistent storage.
     */
    public int countItemsWithType(int type) {
        StringBuffer sb = new StringBuffer(itemsByType);
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(new Integer(type));
        return loadNumber(sb.toString(), params).intValue();
    }

    /**
     * Finds id of last seen comment or returns null, if the discussion
     * has not been opened by the user yet.
     * @param userId id of the user
     * @param discussion id of the discussion item
     * @return id of the comment or null
     */
    public Integer getLastSeenComment(int userId, int discussion) {
        List params = new ArrayList();
        params.add(new Integer(userId));
        params.add(new Integer(discussion));
        return loadNumber(getLastComment, params);
    }

    /**
     * Inserts into comments information about last comment of given discussion seen
     * by selected user. If the discussion has been already displayed to this user,
     * its previous lastComment value is replaced by new value.
     * @param userId id of the user
     * @param discussion id of the discussion item
     * @param lastComment id of the comment that is currently displayed to the user
     */
    public void insertLastSeenComment(int userId, int discussion, int lastComment) {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(insertLastComment);
            statement.setInt(1, userId);
            statement.setInt(2, discussion);
            statement.setInt(3, lastComment);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pri ukladani!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, null);
        }
    }

    /**
     * Deletes comment seen information for given user except
     * preserveCount most fresh rows.
     * @param userId id of the user
     * @param preserveCount how many freshest rows to be preserved
     * @return number of deleted discussions
     */
    public int deleteOldComments(int userId, int preserveCount) {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(getXthComment);
            statement.setInt(1, userId);
            statement.setInt(2, preserveCount);
            resultSet = statement.executeQuery();
            if ( !resultSet.next() )
                return 0;

            Timestamp timestamp = resultSet.getTimestamp(1);
            statement = con.prepareStatement(deleteOldComments);
            statement.setInt(1, userId);
            statement.setTimestamp(2, timestamp);
            int affected = statement.executeUpdate();
            return affected;
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pri ukladani!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Inserts information about what user has performed on selected relation.
     * The purpose is to be able to limit multiple actions.
     * @param userId id of the user
     * @param rid  id of the relation where the user performed the action
     * @param type optional type of the user action on given relation
     */
    public void insertUserAction(int userId, int rid, String type) {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(insertUserAction);
            statement.setInt(1, userId);
            statement.setInt(2, rid);
            statement.setString(3, type);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pri ukladani!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, null);
        }
    }

    /**
     * Retrieves information when user performed specified action on selected relation.
     * The purpose is to be able to limit multiple actions.
     * @param userId id of the user
     * @param rid  id of the relation where the user performed the action
     * @param type optional type of the user action on given relation
     */
    public Date getUserAction(int userId, int rid, String type) {
        List params = new ArrayList();
        params.add(new Integer(userId));
        params.add(new Integer(rid));
        params.add(type);
        return loadDate(getUserAction, params);
    }

    /**
     * Removes information when user performed specified action on selected relation.
     * @param userId id of the user
     * @param rid    id of the relation where the user performed the action
     * @param type   optional type of the user action on given relation
     */
    public void removeUserAction(int userId, int rid, String type) {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(removeUserAction);
            statement.setInt(1, userId);
            statement.setInt(2, rid);
            statement.setString(3, type);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pri mazani!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, null);
        }
    }

    /**
     * Private constructor
     */
    private SQLTool() {
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        relationsRecordByType = getValue(PREF_RECORD_RELATIONS_BY_TYPE, prefs);
        relationsParentRecordByType = getValue(PREF_RECORD_PARENT_RELATIONS_BY_TYPE, prefs);
        relationsItemsByType = getValue(PREF_ITEM_RELATIONS_BY_TYPE, prefs);
        relationsSectionByType = getValue(PREF_SECTION_RELATIONS_BY_TYPE, prefs);
        relationsDiscussion = getValue(PREF_DISCUSSION_RELATIONS, prefs);
        relationsDiscussionInSection = getValue(PREF_DISCUSSION_RELATIONS_IN_SECTION, prefs);
        relationsArticle = getValue(PREF_ARTICLE_RELATIONS, prefs);
        relationsArticleWithinPeriod = getValue(PREF_ARTICLE_RELATIONS_WITHIN_PERIOD, prefs);
        relationsArticlesOnIndex = getValue(PREF_ARTICLES_ON_INDEX_RELATIONS, prefs);
        relationsNews = getValue(PREF_NEWS_RELATIONS, prefs);
        relationsNewsWithinPeriod = getValue(PREF_NEWS_RELATIONS_WITHIN_PERIOD, prefs);
        relationsNewsByUser = getValue(PREF_NEWS_RELATIONS_BY_USER, prefs);
        relationsRecordByUserAndType = getValue(PREF_RECORD_RELATIONS_BY_USER_AND_TYPE, prefs);
        relationsArticleByUser = getValue(PREF_ARTICLE_RELATIONS_BY_USER, prefs);
        relationsQuestionsByUser = getValue(PREF_QUESTION_RELATIONS_BY_USER, prefs);
        relationsCommentsByUser = getValue(PREF_COMMENT_RELATIONS_BY_USER, prefs);
        relationDictionaryByUrlName = getValue(PREF_DICTIONARY_RELATION_BY_URL_NAME, prefs);
        relationByURL = getValue(PREF_RELATION_BY_URL, prefs);
        usersWithWeeklyEmail = getValue(PREF_USERS_WITH_WEEKLY_EMAIL, prefs);
        usersWithForumByEmail = getValue(PREF_USERS_WITH_FORUM_BY_EMAIL, prefs);
        usersWithRoles = getValue(PREF_USERS_WITH_ROLES, prefs);
        usersInGroup = getValue(PREF_USERS_IN_GROUP, prefs);
        maxPoll = getValue(PREF_MAX_POLL, prefs);
        maxUser = getValue(PREF_MAX_USER, prefs);
        userByLogin = getValue(PREF_USER_BY_LOGIN, prefs);
        itemsByType = getValue(PREF_ITEMS_WITH_TYPE, prefs);
        countArticlesByUser = getValue(PREF_COUNT_ARTICLES_BY_USER, prefs);
        insertLastComment = getValue(PREF_INSERT_LAST_COMMENT, prefs);
        getLastComment = getValue(PREF_GET_LAST_COMMENT, prefs);
        getXthComment = getValue(PREF_GET_OLD_COMMENT, prefs);
        deleteOldComments = getValue(PREF_DELETE_OLD_COMMENTS, prefs);
        insertUserAction = getValue(PREF_INSERT_USER_ACTION, prefs);
        getUserAction = getValue(PREF_GET_USER_ACTION, prefs);
        removeUserAction = getValue(PREF_REMOVE_USER_ACTION, prefs);
    }

    /**
     * Gets value from preferences. If value is not defined, it dumps info
     * into logs.
     */
    private String getValue(String name, Preferences prefs) {
        String sql = prefs.get(name,null);
        if (sql!=null)
            return sql;

        log.fatal("Hodnota SQL prikazu "+name+" nebyla nastavena!");
        return null;
    }

    /**
     * Append comparation condition to stringbuffer.
     */
    private void appendCompareCondition(StringBuffer sb, CompareCondition condition, List params) {
        sb.append(" and "); // expect, that each sql command starts with at least one condition

        Field field = condition.getField();
        if (field==Field.CREATED)
            sb.append("vytvoreno");
        else if (field==Field.UPDATED)
            sb.append("zmeneno");
        else if (field==Field.ID)
            sb.append("cislo");
        else if (field==Field.TYPE)
            sb.append("typ");
        else if (field==Field.SUBTYPE)
            sb.append("podtyp");
        else if (field==Field.OWNER)
            sb.append("pridal");
        else if (field==Field.DATA)
            sb.append("data");

        Operation operation = condition.getOperation();
        if (operation==Operation.GREATER)
            sb.append(">");
        else if (operation==Operation.GREATER_OR_EQUAL)
            sb.append(">=");
        else if (operation==Operation.SMALLER)
            sb.append("<");
        else if (operation==Operation.SMALLER_OR_EQUAL)
            sb.append("<=");
        else if (operation==Operation.EQUAL)
            sb.append("=");
        else if (operation==Operation.NOT_EQUAL)
            sb.append("!=");
        else if (operation==Operation.LIKE)
            sb.append(" like ");

        sb.append("? ");

        Object value = condition.getValue();
        if (value instanceof Date)
            value = new java.sql.Date(((Date)value).getTime());
        params.add(value);
    }
}
