/*
 *  Copyright (C) 2005 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
package cz.abclinuxu.persistance;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.data.*;
import cz.abclinuxu.exceptions.PersistanceException;
import cz.abclinuxu.persistance.extra.*;
import cz.abclinuxu.persistance.impl.MySqlPersistance;

import java.sql.*;
import java.util.prefs.Preferences;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Set;

/**
 * Thread-safe singleton, that encapsulates SQL commands
 * used outside of Persistance implementations.
 */
public final class SQLTool implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SQLTool.class);

    public static final String PREF_RECORD_RELATIONS_BY_TYPE = "relations.record.by.type";
    public static final String PREF_RECORD_PARENT_RELATIONS_BY_TYPE = "relations.parent.record.by.type";
    public static final String PREF_ITEM_RELATIONS_BY_TYPE = "relations.item.by.type";
    public static final String PREF_ITEM_RELATIONS_BY_TYPE_WITH_FILTERS = "relations.item.by.type.with.filters";
    public static final String PREF_CATEGORY_RELATIONS_BY_TYPE = "relations.categories.by.type";
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
    public static final String PREF_COUNT_DISCUSSIONS_BY_USER = "count.relations.comment.by.user";
    public static final String PREF_STANDALONE_POLL_RELATIONS = "relations.standalone.polls";
    public static final String PREF_USERS_WITH_WEEKLY_EMAIL = "users.with.weekly.email";
    public static final String PREF_USERS_WITH_FORUM_BY_EMAIL = "users.with.forum.by.email";
    public static final String PREF_USERS_WITH_ROLES = "users.with.roles";
    public static final String PREF_USERS_IN_GROUP = "users.in.group";
    public static final String PREF_ITEMS_WITH_TYPE = "items.with.type";
    public static final String PREF_RECORDS_WITH_TYPE = "records.with.type";
    public static final String PREF_FAQ_SECTION_SIZE = "faq.section.size";
    public static final String PREF_MAX_USER = "max.user";
    public static final String PREF_USER_BY_LOGIN = "user.by.login";
    public static final String PREF_COUNT_ARTICLES_BY_USER = "count.articles.by.user";
    public static final String PREF_DICTIONARY_RELATION_BY_URL_NAME = "relation.dictionary.by.urlname";
    public static final String PREF_RELATION_BY_URL = "relation.by.url";
    public static final String PREF_INSERT_LAST_COMMENT = "insert.last.comment";
    public static final String PREF_GET_LAST_COMMENT = "get.last.comment";
    public static final String PREF_GET_LAST_COMMENTS = "get.last.comments";
    public static final String PREF_DELETE_OLD_COMMENTS = "delete.old.comments";
    public static final String PREF_INSERT_USER_ACTION = "insert.user.action";
    public static final String PREF_GET_USER_ACTION = "get.user.action";
    public static final String PREF_REMOVE_USER_ACTION = "remove.user.action";
    public static final String PREF_INSERT_STATISTICS = "insert.statistika";
    public static final String PREF_INCREMENT_STATISTICS = "update.statistika";
    public static final String PREF_GET_STATISTICS = "get.statistika";
    public static final String PREF_GET_STATISTICS_BY_MONTH = "get.statistika.by.month";
    public static final String PREF_INSERT_OLD_ADDRESS = "insert.stara_adresa";
    public static final String PREF_OLD_ADDRESS = "select.stara.adresa";

    private static SQLTool singleton;

    static {
        singleton = new SQLTool();
        ConfigurationManager.getConfigurator().configureAndRememberMe(singleton);
    }

    // todo presun vsechny stringy do Mapy
    private String relationsRecordByType, relationsParentRecordByType, relationsSectionByType;
    private String relationsItemsByType, relationsItemsByTypeWithFilters, relationsCategoriesByType;
    private String relationsDiscussion, relationsDiscussionInSection;
    private String relationsArticle, relationsArticleWithinPeriod, relationsArticlesOnIndex;
    private String relationsNews, relationsNewsWithinPeriod;
    private String relationsNewsByUser, relationsRecordByUserAndType, relationsArticleByUser;
    private String relationsQuestionsByUser, relationsCommentsByUser;
    private String relationsStandalonePolls;
    private String relationDictionaryByUrlName, relationByURL;
    private String usersWithWeeklyEmail, usersWithForumByEmail, usersWithRoles, usersInGroup;
    private String maxUser, userByLogin, faqSectionSize;
    private String itemsByType, recordsByType;
    private String countArticlesByUser, countDiscussionsByUser;
    private String insertLastComment, getLastComment, getLastComments, deleteOldComments;
    private String insertUserAction, getUserAction, removeUserAction;
    private String incrementStatistics, addStatistics, getStatisticsByMonth, getStatistics;
    private String insertOldAddress, getOldAddress;

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
                result.add(new Relation(id));
            }

            persistance.synchronizeList(result);
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
     * Generic method to get unspecified data from database using given SQL command. If list params is not
     * empty, PreparedStatement is created and fed up from params. Returned list contains arrays of objects,
     * each row is mapped to single array.
     * @param sql Command to execute.
     * @param params List of parameters. It must not be null.
     * @return list of Object[].
     * @throws PersistanceException if something goes wrong.
     */
    private List loadObjects(String sql, List params) {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        List list = new LinkedList();
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql);
            int i = 1;
            for (Iterator iter = params.iterator(); iter.hasNext();)
                statement.setObject(i++, iter.next());

            resultSet = statement.executeQuery();
            int columns = resultSet.getMetaData().getColumnCount();
            Object[] row;
            while (resultSet.next()) {
                row = new Object[columns];
                for (i = 0; i < columns; i++)
                    row[i] = resultSet.getObject(i+1);
                list.add(row);
            }

            return list;
        } catch (SQLException e) {
            throw new PersistanceException("Nemohu vykonat SQL pøíkaz " + sql, e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Appends qualifiers to StringBufefr holding SQL command.
     * @param tableNick nick of table to distinguish columns. Default is null.
     */
    private void appendQualifiers(StringBuffer sb, Qualifier[] qualifiers, List params, String tableNick) {
        if (qualifiers==null || qualifiers.length==0)
            return;
        Qualifier qualifier;
        for ( int i = 0; i<qualifiers.length; i++ ) {
            qualifier = qualifiers[i];
            if ( qualifier.equals(Qualifier.SORT_BY_CREATED) ) {
                sb.append(" order by ");
                if (tableNick != null) {
                    sb.append(tableNick);
                    sb.append(".");
                }
                sb.append("vytvoreno");
            } else if ( qualifier.equals(Qualifier.SORT_BY_UPDATED) ) {
                sb.append(" order by ");
                if (tableNick != null) {
                    sb.append(tableNick);
                    sb.append(".");
                }
                sb.append("zmeneno");
            } else if ( qualifier.equals(Qualifier.SORT_BY_ID) ) {
                sb.append(" order by ");
                if (tableNick != null) {
                    sb.append(tableNick);
                    sb.append(".");
                }
                sb.append("cislo");
            } else if ( qualifier.equals(Qualifier.ORDER_ASCENDING) ) {
                sb.append(" asc");
            } else if ( qualifier.equals(Qualifier.ORDER_DESCENDING) ) {
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
     * @deprecated after removal of software and upgrade of hardware section nobody uses this method
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
     *
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
     * Finds relations, where child is item of specified type and pass the given filters.
     * There is an AND relation between properties and OR between values of each property.
     * Use Qualifiers to set additional parameters.
     * @param filters map where key is name of property (see properties.txt) and value is Set of property values
     * @return List of initialized relations
     * @throws PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findItemRelationsWithTypeWithFilters(int type, Qualifier[] qualifiers, Map filters) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsItemsByTypeWithFilters);
        List params = new ArrayList();
        params.add(new Integer(type));
        appendFilterConditions(filters, sb, params);
        appendQualifiers(sb, qualifiers, params, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Finds relations, where child is item of specified type and pass the given filters.
     * There is an AND relation between properties and OR between values of each property.
     * Use Qualifiers to set additional parameters.
     * @param filters map where key is name of property (see properties.txt) and value is Set of property values
     * @return number of matching relations
     * @throws PersistanceException if there is an error with the underlying persistent storage.
     */
    public int countItemRelationsWithTypeWithFilters(int type, Qualifier[] qualifiers, Map filters) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsItemsByTypeWithFilters);
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(new Integer(type));
        appendFilterConditions(filters, sb, params);
        appendQualifiers(sb, qualifiers, params, null);
        return loadNumber(sb.toString(), params).intValue();
    }

    private void appendFilterConditions(Map filters, StringBuffer sb, List params) {
        Iterator i = filters.entrySet().iterator();
        if (i.hasNext()) {
            sb.append(" and V.predek=P.cislo and V.typ_predka='P' and (");
            while (i.hasNext()) {
                Map.Entry entry = (Map.Entry) i.next();
                String typ = (String) entry.getKey();
                Set values = (Set) entry.getValue();
                sb.append("V.typ=? and (");
                params.add(typ);

                Iterator i2 = values.iterator();
                while (i2.hasNext()) {
                    sb.append("V.hodnota=?");
                    params.add(i2.next());
                    if (i2.hasNext())
                        sb.append(" or ");
                }
                sb.append(" )");
                if(i.hasNext())
                    sb.append(" or ");
            }
            sb.append(")");
        }
    }

    /**
     * Finds relations, where child is category of specified type.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findCategoriesRelationsWithType(int type, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsCategoriesByType);
        List params = new ArrayList();
        params.add(new Integer(type));
        appendQualifiers(sb, qualifiers, params, null);
        return loadRelations(sb.toString(), params);
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
     * Use Qualifiers to set additional parameters. Qualifier SORT_BY is prohibited
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
        StringBuffer sb = new StringBuffer(countDiscussionsByUser);
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
     * Finds relations, where child is poll and upper relation is 250.
     * Use Qualifiers to set additional parameters, do not use SORT_BY findQualifier.
     * @return List of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistanceException
     *          if there is an error with the underlying persistent storage.
     */
    public List findStandalonePollRelations(Qualifier[] qualifiers) {
        if (qualifiers == null) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(relationsStandalonePolls);
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is poll and upper relation is 250.
     * @throws cz.abclinuxu.exceptions.PersistanceException
     *          if there is an error with the underlying persistent storage.
     */
    public int countStandalonePollRelations() {
        StringBuffer sb = new StringBuffer(relationsStandalonePolls);
        changeToCountStatement(sb);
        List params = new ArrayList();
        return loadNumber(sb.toString(), params).intValue();
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
     * @throws PersistanceException if there is an error with the underlying persistant storage.
     */
    public Relation findActivePoll() {
        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, 1)};
        List relations = findStandalonePollRelations(qualifiers);
        Relation relation = (Relation) relations.get(0);

        Persistance persistance = PersistanceFactory.getPersistance();
        Poll poll = (Poll) persistance.findById(relation.getChild());
        if (poll.isClosed())
            return null;
        else
            return relation;
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
            throw new PersistanceException("Chyba pøi hledání!", e);
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
            throw new PersistanceException("Chyba pøi hledání!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, null);
        }
    }

    /**
     * Finds items of given type ordered by id property in ascending order.
     * Use offset to skip some record .
     * @return List of initialized Items
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
                result.add(new Item(id));
            }
            persistance.synchronizeList(result);
            return result;
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pøi hledání!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds items of given type ordered by id property in ascending order.
     * Use offset to skip some record.
     * @return List of initialized Records
     * @throws PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findRecordsWithType(int type, int offset, int count) {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(recordsByType.concat(" order by cislo asc limit ?,?"));
            statement.setInt(1, type);
            statement.setInt(2, offset);
            statement.setInt(3, count);

            resultSet = statement.executeQuery();
            List result = new ArrayList(count);
            while ( resultSet.next() ) {
                int id = resultSet.getInt(1);
                result.add(new Record(id));
            }
            persistance.synchronizeList(result);
            return result;
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pøi hledání!", e);
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
                result.add(new Item(id));
            }
            persistance.synchronizeList(result);
            return result;
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pøi hledání!", e);
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
                result.add(new Item(id));
            }
            persistance.synchronizeList(result);
            return result;
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pøi hledání!", e);
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
     * Finds records of given type.
     * @return Number of matched records
     * @throws PersistanceException if there is an error with the underlying persistent storage.
     */
    public int countRecordsWithType(int type) {
        StringBuffer sb = new StringBuffer(recordsByType);
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
     * Finds last seen comments for selected user.
     * @param userId id of the user
     * @param size number of returned rows
     * @return id of the comment or null
     */
    public List getLastSeenComments(int userId, int size) {
        List params = new ArrayList();
        params.add(new Integer(userId));
        params.add(new Integer(0));
        params.add(new Integer(size));
        return loadObjects(getLastComments, params);
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
            statement = con.prepareStatement(getLastComments);
            statement.setInt(1, userId);
            statement.setInt(2, preserveCount);
            statement.setInt(3, 1);
            resultSet = statement.executeQuery();
            if ( !resultSet.next() )
                return 0;

            Timestamp timestamp = resultSet.getTimestamp(3);
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
     * Finds all nonempty FAQ sections and returns number of their children.
     *
     * @return map where section relation id is key (string) and number of
     *         children as value (integer).
     */
    public Map getFaqSectionsSize() {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        Statement statement = null;
        try {
            int rid, size;
            Map result = new HashMap(30);

            con = persistance.getSQLConnection();
            statement = con.createStatement();
            ResultSet rs = statement.executeQuery(faqSectionSize);
            while (rs.next()) {
                rid = rs.getInt(1);
                size = rs.getInt(2);
                result.put(Integer.toString(rid), new Integer(size));
            }

            return result;
        } catch (SQLException e) {
            throw new PersistanceException("Chyba v SQL!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, null);
        }
    }

    /**
     * Records new page view in statistics for selected type of page.
     */
    public void recordPageView(String pageType, int count) {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statementUpdate = null, statementInsert = null;
        try {
            con = persistance.getSQLConnection();
            statementUpdate = con.prepareStatement(incrementStatistics);
            statementUpdate.setInt(1, count);
            statementUpdate.setString(2, pageType);
            int updated = statementUpdate.executeUpdate();
            if (updated != 0)
                return;

            statementInsert = con.prepareStatement(addStatistics);
            statementInsert.setString(1, pageType);
            statementInsert.setInt(2, count);
            statementInsert.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // duplicate key error
                try {
                    statementUpdate.executeUpdate();
                    return;
                } catch (SQLException e1) {
                    throw new PersistanceException("Chyba v SQL!", e);
                }
            }
            throw new PersistanceException("Chyba v SQL!", e);
        } finally {
            persistance.releaseSQLResources(con, new Statement[]{statementInsert, statementUpdate}, null);
        }
    }

    /**
     * Loads statistics according to qualifiers. To specify date range, use
     * CompareCondition with Field.DAY.
     * @param qualifiers
     * @return list of Object arrays
     */
    public List getStatistics(Qualifier[] qualifiers) {
        if (qualifiers == null) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer(getStatistics);
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null);
        return loadObjects(sb.toString(), params);
    }

    /**
     * Inserts deprecated URL into table of replacement. Either newUrl or
     * rid must be supplied.
     * @param oldUrl old URL that shall be sustained, it must start with /
     * @param newUrl new address, it may even lead to diferent server (though unlikely)
     * @param rid relation id
     */
    public void insertOldAddress(String oldUrl, String newUrl, Integer rid) {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(insertOldAddress);
            statement.setString(1, oldUrl);
            if (rid == null)
                statement.setNull(2, Types.INTEGER);
            else
                statement.setInt(2, rid.intValue());
            statement.setString(3, newUrl);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pøi ukládání staré adresy!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, null);
        }
    }

    /**
     * Finds new address for an old URL. If found, then it returns either initialized Relation
     * or String holding new URL. Null is returned for no match.
     * @param oldUrl urtl starting with /
     * @return null when not found, initialized Relation or String with new URL (absolute with or without host)
     */
    public Object findNewAddress(String oldUrl) {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(getOldAddress);
            statement.setString(1, oldUrl);
            resultSet = statement.executeQuery();
            if (!resultSet.next())
                return null;

            int rid = resultSet.getInt(1);
            if (! resultSet.wasNull())
                return persistance.findById(new Relation(rid));

            return resultSet.getString(2);
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pøi hledání!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Loads statistics grouped (and summarized) by month.
     * @return list of Object arrays
     */
    public List getStatisticsByMonth() {
        StringBuffer sb = new StringBuffer(getStatisticsByMonth);
        return loadObjects(sb.toString(), Collections.EMPTY_LIST);
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
        relationsItemsByTypeWithFilters = getValue(PREF_ITEM_RELATIONS_BY_TYPE_WITH_FILTERS, prefs);
        relationsCategoriesByType = getValue(PREF_CATEGORY_RELATIONS_BY_TYPE, prefs);
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
        relationsStandalonePolls = getValue(PREF_STANDALONE_POLL_RELATIONS, prefs);
        relationByURL = getValue(PREF_RELATION_BY_URL, prefs);
        usersWithWeeklyEmail = getValue(PREF_USERS_WITH_WEEKLY_EMAIL, prefs);
        usersWithForumByEmail = getValue(PREF_USERS_WITH_FORUM_BY_EMAIL, prefs);
        usersWithRoles = getValue(PREF_USERS_WITH_ROLES, prefs);
        usersInGroup = getValue(PREF_USERS_IN_GROUP, prefs);
        faqSectionSize = getValue(PREF_FAQ_SECTION_SIZE, prefs);
        maxUser = getValue(PREF_MAX_USER, prefs);
        userByLogin = getValue(PREF_USER_BY_LOGIN, prefs);
        itemsByType = getValue(PREF_ITEMS_WITH_TYPE, prefs);
        recordsByType = getValue(PREF_RECORDS_WITH_TYPE, prefs);
        countArticlesByUser = getValue(PREF_COUNT_ARTICLES_BY_USER, prefs);
        countDiscussionsByUser = getValue(PREF_COUNT_DISCUSSIONS_BY_USER, prefs);
        insertLastComment = getValue(PREF_INSERT_LAST_COMMENT, prefs);
        getLastComment = getValue(PREF_GET_LAST_COMMENT, prefs);
        getLastComments = getValue(PREF_GET_LAST_COMMENTS, prefs);
        deleteOldComments = getValue(PREF_DELETE_OLD_COMMENTS, prefs);
        insertUserAction = getValue(PREF_INSERT_USER_ACTION, prefs);
        getUserAction = getValue(PREF_GET_USER_ACTION, prefs);
        removeUserAction = getValue(PREF_REMOVE_USER_ACTION, prefs);
        incrementStatistics = getValue(PREF_INCREMENT_STATISTICS, prefs);
        addStatistics = getValue(PREF_INSERT_STATISTICS, prefs);
        getStatistics = getValue(PREF_GET_STATISTICS, prefs);
        getStatisticsByMonth = getValue(PREF_GET_STATISTICS_BY_MONTH, prefs);
        insertOldAddress = getValue(PREF_INSERT_OLD_ADDRESS, prefs);
        getOldAddress = getValue(PREF_OLD_ADDRESS, prefs);
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
        int where = sb.indexOf("where ") + "where ".length();
        if (where < sb.length())
            sb.append(" and "); // probably there was at least one condition after where

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
        else if (field==Field.UPPER)
            sb.append("predchozi");
        else if (field==Field.DAY)
            sb.append("den");
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
