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
    public static final String PREF_ARTICLE_RELATIONS_WITHIN_PERIOD = "relations.article.within.period";
    public static final String PREF_NEWS_RELATIONS = "relations.news";
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
    public static final String PREF_COUNT_ARTICLES_BY_USER = "count.articles.by.user";
    public static final String PREF_DICTIONARY_RELATION_BY_URL_NAME = "relation.dictionary.by.urlname";

    private static SQLTool singleton;

    static {
        singleton = new SQLTool();
        ConfigurationManager.getConfigurator().configureAndRememberMe(singleton);
    }

    private String relationsRecordByType, relationsParentRecordByType, relationsItemsByType, relationsSectionByType;
    private String relationsDiscussion, relationsDiscussionInSection;
    private String relationsArticle, relationsArticleWithinPeriod;
    private String relationsNews, relationsNewsWithinPeriod;
    private String relationsNewsByUser, relationsRecordByUserAndType, relationsArticleByUser;
    private String relationsQuestionsByUser, relationsCommentsByUser;
    private String relationDictionaryByUrlName;
    private String usersWithWeeklyEmail, usersWithForumByEmail, usersWithRoles, usersInGroup;
    private String maxPoll, maxUser;
    private String itemsByType;
    private String countArticlesByUser;


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
     * @return integer.
     * @throws PersistanceException if something goes wrong.
     */
    private int loadNumber(String sql, List params) throws PersistanceException {
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
                throw new PersistanceException("SQL pøíkaz nevrátil ¾ádná data!");

            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new PersistanceException("Nemohu vykonat SQL pøíkaz "+sql, e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Appends qualifiers to StringBufefr holding SQL command.
     */
    private void appendQualifiers(StringBuffer sb, Qualifier[] qualifiers, List params) {
        Qualifier qualifier;
        boolean sort = false;
        for ( int i = 0; i<qualifiers.length; i++ ) {
            qualifier = qualifiers[i];
            if ( qualifier.equals(Qualifier.SORT_BY_CREATED) ) {
                sb.append(" order by vytvoreno");
                sort = true;
            } else if ( qualifier.equals(Qualifier.SORT_BY_UPDATED) ) {
                sb.append(" order by zmeneno");
                sort = true;
            } else if ( qualifier.equals(Qualifier.SORT_BY_ID) ) {
                sb.append(" order by cislo");
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
        int space = sb.indexOf(" ");
        sb.insert(space+1, "count(");
        space = sb.indexOf(" ", space+6);
        sb.insert(space, ')');
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
        appendQualifiers(sb,qualifiers,params);
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
        appendQualifiers(sb,qualifiers,params);
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
        return loadNumber(sb.toString(),params);
    }

    /**
     * Finds relations, where child is item of specified type.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findItemRelationsWithType(int type, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsItemsByType);
        List params = new ArrayList();
        params.add(new Integer(type));
        appendQualifiers(sb, qualifiers, params);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is item of specified type.
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public int countItemRelationsWithType(int type) {
        StringBuffer sb = new StringBuffer(relationsItemsByType);
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(new Integer(type));
        return loadNumber(sb.toString(), params);
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
        appendQualifiers(sb, qualifiers, params);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is a category of specified type.
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public int countSectionRelationsWithType(int type) {
        StringBuffer sb = new StringBuffer(relationsSectionByType);
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(new Integer(type));
        return loadNumber(sb.toString(), params);
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
        appendQualifiers(sb, qualifiers, params);
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
        return loadNumber(sb.toString(), params);
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
        appendQualifiers(sb, qualifiers, params);
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
        return loadNumber(sb.toString(), params);
    }

    /**
     * Finds relations, where child is article item.
     * Items with property created set to future and outside of typical columns are skipped.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findArticleRelations(Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsArticle);
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is article item.
     * Items with property created set to future and outside of typical columns are skipped.
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public int countArticleRelations() {
        StringBuffer sb = new StringBuffer(relationsArticle);
        changeToCountStatement(sb);
        List params = new ArrayList();
        return loadNumber(sb.toString(), params);
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
        appendQualifiers(sb, qualifiers, params);
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
        return loadNumber(sb.toString(), params);
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
        appendQualifiers(sb, qualifiers, params);
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
        return loadNumber(sb.toString(), params);
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
        appendQualifiers(sb, qualifiers, params);
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
        return loadNumber(sb.toString(), params);
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
        appendQualifiers(sb, qualifiers, params);
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
        return loadNumber(sb.toString(), params);
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
        appendQualifiers(sb, qualifiers, params);
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
        return loadNumber(sb.toString(), params);
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
        appendQualifiers(sb, qualifiers, params);
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
        return loadNumber(sb.toString(), params);
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
        appendQualifiers(sb, qualifiers, params);
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
        return loadNumber(sb.toString(), params);
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
        appendQualifiers(sb, qualifiers, params);
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
        return loadNumber(sb.toString(), params);
    }

    /**
     * Finds dictionary item identifies by urlName.
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
     * Finds users, that have active email and have subscribed weekly email.
     * Use Qualifiers to set additional parameters.
     * @return list of Integers of user ids.
     */
    public List findUsersWithWeeklyEmail(Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(usersWithWeeklyEmail);
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params);
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
        appendQualifiers(sb, qualifiers, params);
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
        appendQualifiers(sb, qualifiers, params);
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
        appendQualifiers(sb, qualifiers, params);
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
        return loadNumber(sb.toString(), params);
    }

    /**
     * Finds the last poll. If it is not active, null is returned.
     * @return last initialized poll, if it is active, null otherwise.
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistant storage.
     */
    public Poll findActivePoll() {
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
            return (poll.isClosed()) ? null : poll;
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
     * Finds items of given type.
     * @return Number of matched items
     * @throws PersistanceException if there is an error with the underlying persistent storage.
     */
    public int countItemsWithType(int type) {
        StringBuffer sb = new StringBuffer(itemsByType);
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(new Integer(type));
        return loadNumber(sb.toString(), params);
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
        relationsNews = getValue(PREF_NEWS_RELATIONS, prefs);
        relationsNewsWithinPeriod = getValue(PREF_NEWS_RELATIONS_WITHIN_PERIOD, prefs);
        relationsNewsByUser = getValue(PREF_NEWS_RELATIONS_BY_USER, prefs);
        relationsRecordByUserAndType = getValue(PREF_RECORD_RELATIONS_BY_USER_AND_TYPE, prefs);
        relationsArticleByUser = getValue(PREF_ARTICLE_RELATIONS_BY_USER, prefs);
        relationsQuestionsByUser = getValue(PREF_QUESTION_RELATIONS_BY_USER, prefs);
        relationsCommentsByUser = getValue(PREF_COMMENT_RELATIONS_BY_USER, prefs);
        relationDictionaryByUrlName = getValue(PREF_DICTIONARY_RELATION_BY_URL_NAME, prefs);
        usersWithWeeklyEmail = getValue(PREF_USERS_WITH_WEEKLY_EMAIL, prefs);
        usersWithForumByEmail = getValue(PREF_USERS_WITH_FORUM_BY_EMAIL, prefs);
        usersWithRoles = getValue(PREF_USERS_WITH_ROLES, prefs);
        usersInGroup = getValue(PREF_USERS_IN_GROUP, prefs);
        maxPoll = getValue(PREF_MAX_POLL, prefs);
        maxUser = getValue(PREF_MAX_USER, prefs);
        itemsByType = getValue(PREF_ITEMS_WITH_TYPE, prefs);
        countArticlesByUser = getValue(PREF_COUNT_ARTICLES_BY_USER, prefs);
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
