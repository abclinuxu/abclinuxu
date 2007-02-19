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
package cz.abclinuxu.persistence;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.data.*;
import cz.abclinuxu.exceptions.PersistenceException;
import cz.abclinuxu.persistence.extra.*;
import cz.abclinuxu.persistence.impl.MySqlPersistence;

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

    public static final String RECORD_RELATIONS_BY_TYPE = "relations.record.by.type";
    public static final String ITEM_RELATIONS_BY_TYPE = "relations.item.by.type";
    public static final String ITEM_RELATIONS_BY_TYPE_WITH_FILTERS = "relations.item.by.type.with.filters";
    public static final String CATEGORY_RELATIONS = "relations.categories";
    public static final String SECTION_RELATIONS_BY_TYPE = "relations.section.by.type";
    public static final String DISCUSSION_RELATIONS = "relations.discussion";
    public static final String DISCUSSION_RELATIONS_IN_SECTION = "relations.discussion.in.section";
    public static final String ARTICLE_RELATIONS = "relations.article";
    public static final String ARTICLES_ON_INDEX_RELATIONS = "relations.article.on.index";
    // todo calculate it dynamically with findqualifier
    public static final String ARTICLE_RELATIONS_WITHIN_PERIOD = "relations.article.within.period";
    public static final String NEWS_RELATIONS = "relations.news";
    // todo calculate it dynamically with findqualifier
    public static final String NEWS_RELATIONS_WITHIN_PERIOD = "relations.news.within.period";
    public static final String RECORD_RELATIONS_BY_USER_AND_TYPE = "relations.record.by.user.and.type";
    public static final String NEWS_RELATIONS_BY_USER = "relations.news.by.user";
    public static final String QUESTION_RELATIONS_BY_USER = "relations.question.by.user";
    public static final String COMMENT_RELATIONS_BY_USER = "relations.comment.by.user";
    public static final String COUNT_DISCUSSIONS_BY_USER = "count.relations.comment.by.user";
    public static final String STANDALONE_POLL_RELATIONS = "relations.standalone.polls";
    public static final String USERS_WITH_WEEKLY_EMAIL = "users.with.weekly.email";
    public static final String USERS_WITH_FORUM_BY_EMAIL = "users.with.forum.by.email";
    public static final String USERS_WITH_ROLES = "users.with.roles";
    public static final String USERS_IN_GROUP = "users.in.group";
    public static final String ITEMS_WITH_TYPE = "items.with.type";
    public static final String RECORDS_WITH_TYPE = "records.with.type";
    public static final String ITEMS_COUNT_IN_SECTION = "items.count.in.section";
    public static final String LAST_ITEM_AND_COUNT_IN_SECTION = "item.last.and.count.in.section";
    public static final String MAX_USER = "max.user";
    public static final String USER_BY_LOGIN = "user.by.login";
    public static final String ARTICLE_RELATIONS_BY_AUTHOR = "relations.article.by.author";
    public static final String COUNT_ARTICLES_BY_AUTHOR = "count.articles.by.author";
    public static final String COUNT_ARTICLES_BY_AUTHORS = "count.articles.by.authors";
    public static final String RELATION_BY_URL = "relation.by.url";
    public static final String INSERT_LAST_COMMENT = "insert.last.comment";
    public static final String GET_LAST_COMMENT = "get.last.comment";
    public static final String GET_LAST_COMMENTS = "get.last.comments";
    public static final String DELETE_OLD_COMMENTS = "delete.old.comments";
    public static final String INSERT_USER_ACTION = "insert.user.action";
    public static final String GET_USER_ACTION = "get.user.action";
    public static final String REMOVE_USER_ACTION = "remove.user.action";
    public static final String INSERT_STATISTICS = "insert.statistika";
    public static final String INCREMENT_STATISTICS = "update.statistika";
    public static final String INSERT_SEARCH_QUERY = "insert.search.query";
    public static final String INCREMENT_SEARCH_QUERY = "update.search.query";
    public static final String GET_SEARCH_QUERY = "get.search.query.for";
    public static final String GET_STATISTICS = "get.statistika";
    public static final String GET_STATISTICS_BY_MONTH = "get.statistika.by.month";
    public static final String INSERT_OLD_ADDRESS = "insert.stara_adresa";
    public static final String OLD_ADDRESS = "select.stara.adresa";
    public static final String PROPERTY_VALUES = "get.property.values";
    public static final String ROYALTY_RELATIONS = "relations.royalty";

    private static SQLTool singleton;
    static {
        singleton = new SQLTool();
        ConfigurationManager.getConfigurator().configureAndRememberMe(singleton);
    }

    private Map sql = new HashMap(60, 0.9f);

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
     * @throws PersistenceException if something goes wrong.
     */
    private List<Relation>  loadRelations(String sql, List params) throws PersistenceException {
        if (log.isDebugEnabled())
            log.debug(sql);
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
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
            List<Relation>  result = new ArrayList<Relation>();
            while ( resultSet.next() ) {
                int id = resultSet.getInt(1);
                result.add(new Relation(id));
            }

            persistance.synchronizeList(result);
            return result;
        } catch (SQLException e) {
            throw new PersistenceException("Nemohu vykonat SQL příkaz "+sql, e);
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
     * @throws PersistenceException if something goes wrong.
     */
    private List loadUsers(String sql, List params) throws PersistenceException {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
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
                result.add(resultSet.getInt(1));
            }
            return result;
        } catch (SQLException e) {
            throw new PersistenceException("Nemohu vykonat SQL příkaz "+sql, e);
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
     * @throws PersistenceException if something goes wrong.
     */
    private Integer loadNumber(String sql, List params) throws PersistenceException {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
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
            return i;
        } catch (SQLException e) {
            throw new PersistenceException("Nemohu vykonat SQL příkaz "+sql, e);
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
     * @throws PersistenceException if something goes wrong.
     */
    private Date loadDate(String sql, List params) throws PersistenceException {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
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
            throw new PersistenceException("Nemohu vykonat SQL příkaz "+sql, e);
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
     * @throws PersistenceException if something goes wrong.
     */
    private List<Object[]> loadObjects(String sql, List params) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        List<Object[]> list = new LinkedList<Object[]>();
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
            throw new PersistenceException("Nemohu vykonat SQL příkaz " + sql, e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Removes dangerous characters that may be used to escape current SQL command.
     * @param param SQL command, null is accepted
     * @return SQL-injection safe parameter
     */
    public String protectSQLParameter(String param) {
        if (param == null || param.length() == 0)
            return param;
        return param.replace('\\', ' ');
    }

    /**
     * Finds relations, where child is record of specified type. Use Qualifiers
     * to set additional parameters.
     * @deprecated after removal of software and upgrade of hardware section nobody uses this method
     * @return List of initialized relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> findRecordRelationsWithType(int type, Qualifier[] qualifiers) {
        if (qualifiers==null) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String)sql.get(RECORD_RELATIONS_BY_TYPE));
        List params = new ArrayList();
        params.add(type);
        appendQualifiers(sb,qualifiers,params, null, null);
        return loadRelations(sb.toString(),params);
    }

    /**
     * Counts relations, where child is record of specified type.
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public int countRecordRelationsWithType(int type) {
        StringBuffer sb = new StringBuffer((String) sql.get(RECORD_RELATIONS_BY_TYPE));
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(type);
        return loadNumber(sb.toString(),params);
    }

    /**
     * Finds relations, where child is item of specified type.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> findItemRelationsWithType(int type, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(ITEM_RELATIONS_BY_TYPE));
        List params = new ArrayList();
        params.add(type);
        Map fieldMapping = new HashMap();
        fieldMapping.put(Field.UPPER, "R");
        appendQualifiers(sb, qualifiers, params, "P", fieldMapping);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is item of specified type.
     * Use Qualifiers to set additional parameters.
     *
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public int countItemRelationsWithType(int type, Qualifier[] qualifiers) {
        StringBuffer sb = new StringBuffer((String) sql.get(ITEM_RELATIONS_BY_TYPE));
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(type);
        Map fieldMapping = new HashMap();
        fieldMapping.put(Field.UPPER, "R");
        appendQualifiers(sb, qualifiers, params, "P", fieldMapping);
        return loadNumber(sb.toString(), params);
    }

    /**
     * Finds relations, where child is item of specified type and pass the given filters.
     * There is an AND relation between properties and OR between values of each property.
     * Use Qualifiers to set additional parameters.
     * @param filters map where key is name of property (see properties.txt) and value is Set of property values
     * @return List of initialized relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> findItemRelationsWithTypeWithFilters(int type, Qualifier[] qualifiers, Map filters) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(ITEM_RELATIONS_BY_TYPE_WITH_FILTERS));
        List params = new ArrayList();
        params.add(type);
        appendFilterConditions(filters, sb, params);
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Finds relations, where child is item of specified type and pass the given filters.
     * There is an AND relation between properties and OR between values of each property.
     * Use Qualifiers to set additional parameters.
     * @param filters map where key is name of property (see properties.txt) and value is Set of property values
     * @return number of matching relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public int countItemRelationsWithTypeWithFilters(int type, Qualifier[] qualifiers, Map filters) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(ITEM_RELATIONS_BY_TYPE_WITH_FILTERS));
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(type);
        appendFilterConditions(filters, sb, params);
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadNumber(sb.toString(), params);
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
     * Finds relations, where child is category. Unless you wish to receive all
     * categories, you shall add additional criteria into qualifiers.
     * @return List of initialized relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> findCategoriesRelations(Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(CATEGORY_RELATIONS));
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Finds relations, where child is a category of specified type.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> findCategoryRelationsWithType(int type, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(SECTION_RELATIONS_BY_TYPE));
        List params = new ArrayList();
        params.add(type);
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is a category of specified type.
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public int countCategoryRelationsWithType(int type) {
        StringBuffer sb = new StringBuffer((String) sql.get(SECTION_RELATIONS_BY_TYPE));
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(type);
        return loadNumber(sb.toString(), params);
    }

    /**
     * Finds relations, where child is discussion item.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> findDiscussionRelations(Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(DISCUSSION_RELATIONS));
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is discussion item.
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public int countDiscussionRelations() {
        StringBuffer sb = new StringBuffer((String) sql.get(DISCUSSION_RELATIONS));
        changeToCountStatement(sb);
        List params = new ArrayList();
        return loadNumber(sb.toString(), params);
    }

    /**
     * Finds relations, where child is discussion item with specified parent.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> findDiscussionRelationsWithParent(int parent, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(DISCUSSION_RELATIONS_IN_SECTION));
        List params = new ArrayList();
        params.add(parent);
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is discussion item with specified parent.
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public int countDiscussionRelationsWithParent(int parent) {
        StringBuffer sb = new StringBuffer((String) sql.get(DISCUSSION_RELATIONS_IN_SECTION));
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(parent);
        return loadNumber(sb.toString(), params);
    }

    /**
     * Finds relations, where child is article item in the section folder.
     * Items with property created set to future and subtype equal to NOINDEX are skipped.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> findIndexArticlesRelations(Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(ARTICLES_ON_INDEX_RELATIONS));
        List params = new ArrayList();
        Map fieldMapping = new HashMap();
        fieldMapping.put(Field.UPPER, "R");
        appendQualifiers(sb, qualifiers, params, "P", fieldMapping);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Finds relations, where child is article item.
     * Items with property created set to future and outside of typical columns are skipped.
     * Use Qualifiers to set additional parameters.
     * @param section id of section to be searched. If equal to 0, than all sections will be searched
     * @return List of initialized relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> findArticleRelations(Qualifier[] qualifiers, int section) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(ARTICLE_RELATIONS));
        List params = new ArrayList();
        if (section>0) {
            params.add(section);
            sb.append(" and K.cislo=?");
        }
        Map fieldMapping = new HashMap();
        fieldMapping.put(Field.UPPER, "R");
        appendQualifiers(sb, qualifiers, params, "P", fieldMapping);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is article item.
     * Items with property created set to future and outside of typical columns are skipped.
     * @param section id of section to be searched. If equal to 0, than all sections will be searched
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public int countArticleRelations(int section) {
        StringBuffer sb = new StringBuffer((String) sql.get(ARTICLE_RELATIONS));
        changeToCountStatement(sb);
        List params = new ArrayList();
        if (section > 0) {
            params.add(section);
            sb.append(" and K.cislo=?");
        }
        return loadNumber(sb.toString(), params);
    }

    /**
     * Finds relations, where child is an article item with created property,
     * that is inside given time period.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     */
    public List<Relation> findArticleRelationsWithinPeriod(Date from, Date until, Qualifier[] qualifiers) throws PersistenceException {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(ARTICLE_RELATIONS_WITHIN_PERIOD));
        List params = new ArrayList();
        params.add(new java.sql.Date(from.getTime()));
        params.add(new java.sql.Date(until.getTime()));
        Map fieldMapping = new HashMap();
        fieldMapping.put(Field.UPPER, "R");
        appendQualifiers(sb, qualifiers, params, "P", fieldMapping);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Finds relations, where child is an article item with created property,
     * that is inside given time period.
     */
    public int countArticleRelationsWithinPeriod(Date from, Date until) throws PersistenceException {
        StringBuffer sb = new StringBuffer((String) sql.get(ARTICLE_RELATIONS_WITHIN_PERIOD));
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
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> findNewsRelations(Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(NEWS_RELATIONS));
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is news item .
     * Items with created property in future and in newspool are skipped.
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public int countNewsRelations() {
        StringBuffer sb = new StringBuffer((String) sql.get(NEWS_RELATIONS));
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
    public List<Relation> findNewsRelationsWithinPeriod(Date from, Date until, Qualifier[] qualifiers) throws PersistenceException {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(NEWS_RELATIONS_WITHIN_PERIOD));
        List params = new ArrayList();
        params.add(new java.sql.Date(from.getTime()));
        params.add(new java.sql.Date(until.getTime()));
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is an article item with created property, that is inside
     * given time period.
     */
    public int countNewsRelationsWithinPeriod(Date from, Date until) throws PersistenceException {
        StringBuffer sb = new StringBuffer((String) sql.get(NEWS_RELATIONS_WITHIN_PERIOD));
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
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> findRecordRelationsWithUserAndType(int userId, int type, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(RECORD_RELATIONS_BY_USER_AND_TYPE));
        List params = new ArrayList();
        params.add(userId);
        params.add(type);
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts user's relations, where child is with given type.
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public int countRecordRelationsWithUserAndType(int userId, int type) {
        StringBuffer sb = new StringBuffer((String) sql.get(RECORD_RELATIONS_BY_USER_AND_TYPE));
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(userId);
        params.add(type);
        return loadNumber(sb.toString(), params);
    }

    /**
     * Finds relations, where child is article submitted by given author.
     * Items with property created set to future and outside of typical columns are skipped.
     * Use Qualifiers to set additional parameters.
     * @param authorId id of Item containing author
     * @return List of initialized relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> findArticleRelationsByAuthor(int authorId, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(ARTICLE_RELATIONS_BY_AUTHOR));
        List params = new ArrayList();
        params.add(authorId);
        Map fieldMapping = new HashMap();
        fieldMapping.put(Field.UPPER, "R");
        appendQualifiers(sb, qualifiers, params, "P", fieldMapping);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is article written by given author.
     * @param authorId id of Item containing author
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public int countArticleRelationsByAuthor(int authorId) {
	    StringBuffer sb = new StringBuffer((String) sql.get(COUNT_ARTICLES_BY_AUTHOR));
        List params = new ArrayList();
        params.add(authorId);
        return loadNumber(sb.toString(), params);
    }

    /**
     * Counts article relations by authors.
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     * @return list of integer arrays, first item is author's relation id, second item is count of his articles
     */
    public List<Object[]> countArticleRelationsByAuthors() {
	    StringBuffer sb = new StringBuffer((String) sql.get(COUNT_ARTICLES_BY_AUTHORS));
        return loadObjects(sb.toString(), Collections.EMPTY_LIST);
    }

    /**
     * Finds relations, where child is news item submitted by user.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> findNewsRelationsByUser(int userId, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(NEWS_RELATIONS_BY_USER));
        List params = new ArrayList();
        params.add(userId);
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is news item submitted by user.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public int countNewsRelationsByUser(int userId) {
        StringBuffer sb = new StringBuffer((String) sql.get(NEWS_RELATIONS_BY_USER));
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(userId);
        return loadNumber(sb.toString(), params);
    }

    /**
     * Finds relations, where child is question item submited by user.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> findQuestionRelationsByUser(int userId, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(QUESTION_RELATIONS_BY_USER));
        List params = new ArrayList();
        params.add(userId);
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is question item submited by user.
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public int countQuestionRelationsByUser(int userId) {
        StringBuffer sb = new StringBuffer((String) sql.get(QUESTION_RELATIONS_BY_USER));
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(userId);
        return loadNumber(sb.toString(), params);
    }


    /**
     * Finds relations, where child is discussion item with comment from user.
     * Use Qualifiers to set additional parameters. Qualifier SORT_BY is prohibited
     * @return List of initialized relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> findCommentRelationsByUser(int userId, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(COMMENT_RELATIONS_BY_USER));
        List params = new ArrayList();
        params.add(userId);
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is discussion item with comment from user.
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public int countCommentRelationsByUser(int userId) {
        StringBuffer sb = new StringBuffer((String) sql.get(COUNT_DISCUSSIONS_BY_USER));
        List params = new ArrayList();
        params.add(userId);
        return loadNumber(sb.toString(), params);
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
        List result = loadRelations((String) sql.get(RELATION_BY_URL), params);
        if (result.size()==0)
            return null;
        return (Relation) result.get(0);
    }

    /**
     * Finds relations, where child is poll and upper relation is 250.
     * Use Qualifiers to set additional parameters, do not use SORT_BY findQualifier.
     * @return List of initialized relations
     * @throws PersistenceException
     *          if there is an error with the underlying persistent storage.
     */
    public List<Relation> findStandalonePollRelations(Qualifier[] qualifiers) {
        if (qualifiers == null) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(STANDALONE_POLL_RELATIONS));
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is poll and upper relation is 250.
     * @throws PersistenceException
     *          if there is an error with the underlying persistent storage.
     */
    public int countStandalonePollRelations() {
        StringBuffer sb = new StringBuffer((String) sql.get(STANDALONE_POLL_RELATIONS));
        changeToCountStatement(sb);
        List params = new ArrayList();
        return loadNumber(sb.toString(), params);
    }

    /**
     * Finds users, that have active email and have subscribed weekly email.
     * Use Qualifiers to set additional parameters.
     * @return list of Integers of user ids.
     */
    public List findUsersWithWeeklyEmail(Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(USERS_WITH_WEEKLY_EMAIL));
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadUsers(sb.toString(), params);
    }

    /**
     * Finds users, that have active email and have subscribed forum by email.
     * Use Qualifiers to set additional parameters.
     * @return list of Integers of user ids.
     */
    public List findUsersWithForumByEmail(Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(USERS_WITH_FORUM_BY_EMAIL));
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadUsers(sb.toString(), params);
    }

    /**
     * Finds users, that have at least one role.
     * Use Qualifiers to set additional parameters.
     * @return list of Integers of user ids.
     */
    public List findUsersWithRoles(Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(USERS_WITH_ROLES));
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadUsers(sb.toString(), params);
    }

    /**
     * Finds users, that are members of given group.
     * Use Qualifiers to set additional parameters.
     * @return list of Integers of user ids.
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List findUsersInGroup(int group, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(USERS_IN_GROUP));
        List params = new ArrayList();
        params.add(group);
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadUsers(sb.toString(), params);
    }

    /**
     * Finds users, that are members of given group.
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public int countUsersInGroup(int group) {
        StringBuffer sb = new StringBuffer((String) sql.get(USERS_IN_GROUP));
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(group);
        return loadNumber(sb.toString(), params);
    }

    /**
     * Finds user with specified login.
     * @return id of found user or null, if there is no such user
     * @throws PersistenceException
     *          if there is an error with the underlying persistent storage.
     */
    public Integer getUserByLogin(String login) {
        StringBuffer sb = new StringBuffer((String) sql.get(USER_BY_LOGIN));
        List params = new ArrayList();
        params.add(login);
        return loadNumber(sb.toString(), params);
    }

    /**
     * Finds the last poll. If it is not active, null is returned.
     * @return last initialized poll, if it is active, null otherwise.
     * @throws PersistenceException if there is an error with the underlying persistant storage.
     */
    public Relation findActivePoll() {
        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, 1)};
        List relations = findStandalonePollRelations(qualifiers);
        Relation relation = (Relation) relations.get(0);

        Persistence persistence = PersistenceFactory.getPersistance();
        Poll poll = (Poll) persistence.findById(relation.getChild());
        if (poll.isClosed())
            return null;
        else
            return relation;
    }

    /**
     * Finds maximum id between users.
     */
    public int getMaximumUserId() {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
        Connection con = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.createStatement();
            resultSet = statement.executeQuery((String) sql.get(MAX_USER));
            if ( !resultSet.next() )
                return 0;
            Integer id = resultSet.getInt(1);
            return id;
        } catch (SQLException e) {
            throw new PersistenceException("Chyba při hledání!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Sets updated timestamp in database for given GenericObject.
     * @param obj object to be modified.
     * @param date timestamp to be set.
     * @throws PersistenceException if object doesn't contain such property.
     */
    public void setUpdatedTimestamp(GenericDataObject obj, Date date) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
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
            throw new PersistenceException("Chyba při hledání!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, null);
        }
    }

    /**
     * Finds items of given type ordered by id property in ascending order.
     * Use offset to skip some record .
     * @return List of initialized Items
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List findItemsWithType(int type, int offset, int count) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            StringBuffer sb = new StringBuffer((String) sql.get(ITEMS_WITH_TYPE));
            sb.append(" order by cislo asc limit ?,?");
            statement = con.prepareStatement(sb.toString());
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
            throw new PersistenceException("Chyba při hledání!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds items of given type ordered by id property in ascending order.
     * Use offset to skip some record.
     * @return List of initialized Records
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List findRecordsWithType(int type, int offset, int count) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            StringBuffer sb = new StringBuffer((String) sql.get(RECORDS_WITH_TYPE));
            sb.append(" order by cislo asc limit ?,?");
            statement = con.prepareStatement(sb.toString());
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
            throw new PersistenceException("Chyba při hledání!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds items of given type and subtype ordered by id property in ascending order.
     * @return List of itialized Items
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List findItemsWithTypeAndSubtype(int type, String subType) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            StringBuffer sb = new StringBuffer((String) sql.get(ITEMS_WITH_TYPE));
            sb.append(" order by cislo asc limit ?,?");
            statement = con.prepareStatement(sb.toString());
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
            throw new PersistenceException("Chyba při hledání!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds dictionary item relations that alphabetically neighbours selected one.
     * @param smaller whether the returned items shall be smaller or greater
     * @return List of itialized Relations, first item is closest to selected one.
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> getNeighbourDictionaryItemRelations(String urlName, boolean smaller, int count) {
        StringBuffer sb = new StringBuffer((String) sql.get(ITEM_RELATIONS_BY_TYPE));
        if (smaller)
            sb.append("  and podtyp<? order by podtyp desc limit ?");
        else
            sb.append("  and podtyp>? order by podtyp asc limit ?");

        List params = new ArrayList();
        params.add(Item.DICTIONARY);
        params.add(urlName);
        params.add(count);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Finds items of given type.
     * @return Number of matched items
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public int countItemsWithType(int type) {
        StringBuffer sb = new StringBuffer((String) sql.get(ITEMS_WITH_TYPE));
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(type);
        return loadNumber(sb.toString(), params);
    }

    /**
     * Finds records of given type.
     * @return Number of matched records
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public int countRecordsWithType(int type) {
        StringBuffer sb = new StringBuffer((String) sql.get(RECORDS_WITH_TYPE));
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(type);
        return loadNumber(sb.toString(), params);
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
        params.add(userId);
        params.add(discussion);
        return loadNumber((String) sql.get(GET_LAST_COMMENT), params);
    }

    /**
     * Finds last seen comments for selected user.
     * @param userId id of the user
     * @param size number of returned rows
     * @return id of the comment or null
     */
    public List getLastSeenComments(int userId, int size) {
        List params = new ArrayList();
        params.add(userId);
        params.add(0);
        params.add(size);
        return loadObjects((String) sql.get(GET_LAST_COMMENTS), params);
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
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement((String) sql.get(INSERT_LAST_COMMENT));
            statement.setInt(1, userId);
            statement.setInt(2, discussion);
            statement.setInt(3, lastComment);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Chyba pri ukladani!", e);
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
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement((String) sql.get(GET_LAST_COMMENTS));
            statement.setInt(1, userId);
            statement.setInt(2, preserveCount);
            statement.setInt(3, 1);
            resultSet = statement.executeQuery();
            if ( !resultSet.next() )
                return 0;

            Timestamp timestamp = resultSet.getTimestamp(3);
            statement = con.prepareStatement((String) sql.get(DELETE_OLD_COMMENTS));
            statement.setInt(1, userId);
            statement.setTimestamp(2, timestamp);
            int affected = statement.executeUpdate();
            return affected;
        } catch (SQLException e) {
            throw new PersistenceException("Chyba pri ukladani!", e);
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
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement((String) sql.get(INSERT_USER_ACTION));
            statement.setInt(1, userId);
            statement.setInt(2, rid);
            statement.setString(3, type);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Chyba pri ukladani!", e);
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
        params.add(userId);
        params.add(rid);
        params.add(type);
        return loadDate((String) sql.get(GET_USER_ACTION), params);
    }

    /**
     * Removes information when user performed specified action on selected relation.
     * @param userId id of the user
     * @param rid    id of the relation where the user performed the action
     * @param type   optional type of the user action on given relation
     */
    public void removeUserAction(int userId, int rid, String type) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement((String) sql.get(REMOVE_USER_ACTION));
            statement.setInt(1, userId);
            statement.setInt(2, rid);
            statement.setString(3, type);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Chyba pri mazani!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, null);
        }
    }

    /**
     * Finds all sections containing at least one Item and returns number of their Items.
     * @param categories List of Integers containing id of categories to be examined
     * @return map where section id is a key and number of children is a value
     */
    public Map<Integer, Integer> getItemsCountInSections(List categories) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        try {
            int id, size;
            Map<Integer, Integer> result = new HashMap<Integer, Integer> (categories.size());

            String query = (String) sql.get(ITEMS_COUNT_IN_SECTION);
            int position = query.indexOf('?');
            query = query.substring(0, position) + MySqlPersistence.getInCondition(categories.size())
                    + query.substring(position + 1);

            con = persistance.getSQLConnection();
            statement = con.prepareStatement(query);
            int i = 1;
            for (Iterator iter = categories.iterator(); iter.hasNext();) {
                id = (Integer) iter.next();
                statement.setInt(i++, id);
            }

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                id = rs.getInt(1);
                size = rs.getInt(2);
                result.put(id, size);
            }

            return result;
        } catch (SQLException e) {
            throw new PersistenceException("Chyba v SQL!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, null);
        }
    }

    /**
     * Finds all sections containing at least one Item and returns number of their Items.
     * @param categories List of Integers containing id of categories to be examined
     * @return map where section id is a key and value is array of integers. The first element
     * is count of items in specified section and the second is relation id of last item.
     */
    public Map<Integer, Integer[]> getLastItemAndItemsCountInSections(List categories) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        try {
            int id, size, last;
            Map<Integer, Integer[]> result = new HashMap<Integer, Integer[]> (categories.size());

            String query = (String) sql.get(LAST_ITEM_AND_COUNT_IN_SECTION);
            int position = query.indexOf('?');
            query = query.substring(0, position) + MySqlPersistence.getInCondition(categories.size())
                    + query.substring(position + 1);

            con = persistance.getSQLConnection();
            statement = con.prepareStatement(query);
            int i = 1;
            for (Iterator iter = categories.iterator(); iter.hasNext();) {
                id = (Integer) iter.next();
                statement.setInt(i++, id);
            }

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                id = rs.getInt(1);
                size = rs.getInt(2);
                last = rs.getInt(3);
                result.put(id, new Integer[] {size, last});
            }

            return result;
        } catch (SQLException e) {
            throw new PersistenceException("Chyba v SQL!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, null);
        }
    }

    /**
     * Records new page view in statistics for selected type of page.
     */
    public void recordPageView(String pageType, int count) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statementUpdate = null, statementInsert = null;
        try {
            con = persistance.getSQLConnection();
            statementUpdate = con.prepareStatement((String) sql.get(INCREMENT_STATISTICS));
            statementUpdate.setInt(1, count);
            statementUpdate.setString(2, pageType);
            int updated = statementUpdate.executeUpdate();
            if (updated != 0)
                return;

            statementInsert = con.prepareStatement((String) sql.get(INSERT_STATISTICS));
            statementInsert.setString(1, pageType);
            statementInsert.setInt(2, count);
            statementInsert.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // duplicate key error
                try {
                    statementUpdate.executeUpdate();
                    return;
                } catch (SQLException e1) {
                    throw new PersistenceException("Chyba v SQL!", e);
                }
            }
            throw new PersistenceException("Chyba v SQL!", e);
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
        StringBuffer sb = new StringBuffer((String) sql.get(GET_STATISTICS));
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadObjects(sb.toString(), params);
    }

    /**
     * Loads statistics grouped (and summarized) by month.
     *
     * @return list of Object arrays
     */
    public List getStatisticsByMonth() {
        StringBuffer sb = new StringBuffer((String) sql.get(GET_STATISTICS_BY_MONTH));
        return loadObjects(sb.toString(), Collections.EMPTY_LIST);
    }

    /**
     * Records the query. If it has not been recorded yet, this query is
     * inserted, otherwise its counter is incremented.
     * @param query normalized query string
     */
    public void recordSearchedQuery(String query) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statementUpdate = null, statementInsert = null;
        try {
            con = persistance.getSQLConnection();
            statementUpdate = con.prepareStatement((String) sql.get(INCREMENT_SEARCH_QUERY));
            statementUpdate.setString(1, query);
            int updated = statementUpdate.executeUpdate();
            if (updated != 0)
                return;

            statementInsert = con.prepareStatement((String) sql.get(INSERT_SEARCH_QUERY));
            statementInsert.setString(1, query);
            statementInsert.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // duplicate key error
                try {
                    statementUpdate.executeUpdate();
                    return;
                } catch (SQLException e1) {
                    throw new PersistenceException("Chyba v SQL!", e);
                }
            }
            throw new PersistenceException("Chyba v SQL!", e);
        } finally {
            persistance.releaseSQLResources(con, new Statement[]{statementInsert, statementUpdate}, null);
        }
    }

    /**
     * Loads search queries starting with value of query parameter. You should
     * use qualifiers to limit results and define sort order.
     * @param query normalized query
     * @param qualifiers
     * @return list of Object arrays (first is query, second is counter of its usage)
     */
    public List getSearchQueries(String query, Qualifier[] qualifiers) {
        if (qualifiers == null) qualifiers = new Qualifier[]{};
        StringBuffer sb = new StringBuffer((String) sql.get(GET_SEARCH_QUERY));
        List params = new ArrayList();
        params.add(query);
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadObjects(sb.toString(), params);
    }

    /**
     * Loads of values of certain property.
     * @param type constant as defined in properties.txt
     * @return List of Strings (no duplicates)
     */
    public List getPropertyValues(String type) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement((String) sql.get(PROPERTY_VALUES));
            statement.setString(1, type);
            resultSet = statement.executeQuery();

            List result = new ArrayList();
            while (resultSet.next()) {
                result.add(resultSet.getString(1));
            }
            return result;
        } catch (SQLException e) {
            throw new PersistenceException("Chyba při hledání!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Inserts deprecated URL into table of replacement. Either newUrl or
     * rid must be supplied.
     * @param oldUrl old URL that shall be sustained, it must start with /
     * @param newUrl new address, it may even lead to diferent server (though unlikely)
     * @param rid relation id
     */
    public void insertOldAddress(String oldUrl, String newUrl, Integer rid) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement((String) sql.get(INSERT_OLD_ADDRESS));
            statement.setString(1, oldUrl);
            if (rid == null)
                statement.setNull(2, Types.INTEGER);
            else
                statement.setInt(2, rid);
            statement.setString(3, newUrl);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Chyba při ukládání staré adresy!", e);
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
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement((String) sql.get(OLD_ADDRESS));
            statement.setString(1, oldUrl);
            resultSet = statement.executeQuery();
            if (!resultSet.next())
                return null;

            int rid = resultSet.getInt(1);
            if (! resultSet.wasNull())
                return persistance.findById(new Relation(rid));

            return resultSet.getString(2);
        } catch (SQLException e) {
            throw new PersistenceException("Chyba při hledání!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
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
        store(RECORD_RELATIONS_BY_TYPE, prefs);
        store(ITEM_RELATIONS_BY_TYPE, prefs);
        store(ITEM_RELATIONS_BY_TYPE_WITH_FILTERS, prefs);
        store(CATEGORY_RELATIONS, prefs);
        store(SECTION_RELATIONS_BY_TYPE, prefs);
        store(DISCUSSION_RELATIONS, prefs);
        store(DISCUSSION_RELATIONS_IN_SECTION, prefs);
        store(ARTICLE_RELATIONS, prefs);
        store(ARTICLE_RELATIONS_WITHIN_PERIOD, prefs);
        store(ARTICLES_ON_INDEX_RELATIONS, prefs);
        store(NEWS_RELATIONS, prefs);
        store(NEWS_RELATIONS_WITHIN_PERIOD, prefs);
        store(NEWS_RELATIONS_BY_USER, prefs);
        store(RECORD_RELATIONS_BY_USER_AND_TYPE, prefs);
        store(QUESTION_RELATIONS_BY_USER, prefs);
        store(COMMENT_RELATIONS_BY_USER, prefs);
        store(STANDALONE_POLL_RELATIONS, prefs);
        store(RELATION_BY_URL, prefs);
        store(USERS_WITH_WEEKLY_EMAIL, prefs);
        store(USERS_WITH_FORUM_BY_EMAIL, prefs);
        store(USERS_WITH_ROLES, prefs);
        store(USERS_IN_GROUP, prefs);
        store(ITEMS_COUNT_IN_SECTION, prefs);
        store(LAST_ITEM_AND_COUNT_IN_SECTION, prefs);
        store(MAX_USER, prefs);
        store(USER_BY_LOGIN, prefs);
        store(ITEMS_WITH_TYPE, prefs);
        store(RECORDS_WITH_TYPE, prefs);
        store(ARTICLE_RELATIONS_BY_AUTHOR, prefs);
        store(COUNT_ARTICLES_BY_AUTHOR, prefs);
        store(COUNT_ARTICLES_BY_AUTHORS, prefs);
        store(COUNT_DISCUSSIONS_BY_USER, prefs);
        store(INSERT_LAST_COMMENT, prefs);
        store(GET_LAST_COMMENT, prefs);
        store(GET_LAST_COMMENTS, prefs);
        store(DELETE_OLD_COMMENTS, prefs);
        store(INSERT_USER_ACTION, prefs);
        store(GET_USER_ACTION, prefs);
        store(REMOVE_USER_ACTION, prefs);
        store(INCREMENT_STATISTICS, prefs);
        store(INSERT_STATISTICS, prefs);
        store(GET_STATISTICS, prefs);
        store(GET_STATISTICS_BY_MONTH, prefs);
        store(INCREMENT_SEARCH_QUERY, prefs);
        store(INSERT_SEARCH_QUERY, prefs);
        store(GET_SEARCH_QUERY, prefs);
        store(INSERT_OLD_ADDRESS, prefs);
        store(OLD_ADDRESS, prefs);
        store(PROPERTY_VALUES, prefs);
        store(ROYALTY_RELATIONS, prefs);
    }

    /**
     * Gets value from preferences. If value is not defined, it dumps info into logs.
     */
    private void store(String name, Preferences prefs) {
        String command = prefs.get(name,null);
        if (command == null) {
            log.fatal("Hodnota SQL příkazu " + name + " nebyla nastavena!");
            return;
        }
        sql.put(name, command);
    }

    /**
     * Appends qualifiers to StringBufefr holding SQL command. DefaultTableNick is added
     * before every column in custom conditions (in WHERE or ORDER BY clauses).
     * if defaultTableNick cannot distinguish between two tables, fieldMapping can
     * be used to assign exact tableNick to specific Field from qualifiers.
     * @param defaultTableNick nick of table to distinguish columns. Default is null.
     * @param fieldMapping key is PersistenceMapping.Table, value is tableNick to be used.
     */
    private void appendQualifiers(StringBuffer sb, Qualifier[] qualifiers, List params, String defaultTableNick, Map fieldMapping) {
        if (qualifiers == null || qualifiers.length == 0)
            return;
        if (fieldMapping == null)
            fieldMapping = Collections.EMPTY_MAP;
        Qualifier qualifier;
        for (int i = 0; i < qualifiers.length; i++) {
            qualifier = qualifiers[i];
            if (qualifier.equals(Qualifier.SORT_BY_CREATED)) {
                sb.append(" order by ");
                if (defaultTableNick != null) {
                    sb.append(defaultTableNick);
                    sb.append(".");
                }
                sb.append("vytvoreno");
            } else if (qualifier.equals(Qualifier.SORT_BY_UPDATED)) {
                sb.append(" order by ");
                if (defaultTableNick != null) {
                    sb.append(defaultTableNick);
                    sb.append(".");
                }
                sb.append("zmeneno");
            } else if (qualifier.equals(Qualifier.SORT_BY_ID)) {
                sb.append(" order by ");
                if (defaultTableNick != null) {
                    sb.append(defaultTableNick);
                    sb.append(".");
                }
                sb.append("cislo");
            } else if (qualifier.equals(Qualifier.ORDER_ASCENDING)) {
                sb.append(" asc");
            } else if (qualifier.equals(Qualifier.ORDER_DESCENDING)) {
                sb.append(" desc");
            } else if (qualifier instanceof LimitQualifier) {
                sb.append(" limit ?,?");
                LimitQualifier limitQualifier = (LimitQualifier) qualifier;
                params.add(limitQualifier.getOffset());
                params.add(limitQualifier.getCount());
            } else if (qualifier instanceof CompareCondition)
                appendCompareCondition(sb, (CompareCondition) qualifier, params, defaultTableNick, fieldMapping);
        }
    }

    /**
     * Changes first select clause to count. E.g. from id to count(id).
     */
    private void changeToCountStatement(StringBuffer sb) {
        int position = sb.indexOf(" ");
        sb.insert(position + 1, "count(");
        position = sb.indexOf("from", position + 6);
        sb.insert(position - 1, ')');
    }

    /**
     * Append comparation condition to stringbuffer.
     */
    private void appendCompareCondition(StringBuffer sb, CompareCondition condition, List params,
                                        String defaultTableNick, Map fieldMapping) {
        int where = sb.indexOf("where ") + "where ".length();
        if (where < sb.length())
            sb.append(" and "); // probably there was at least one condition after where

        Field field = condition.getField();
        String tableNick = defaultTableNick;
        if (fieldMapping.containsKey(field))
            tableNick = (String) fieldMapping.get(field);
        if (tableNick != null) {
            sb.append(tableNick);
            sb.append(".");
        }

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
        else if (field==Field.PARENT_TYPE)
            sb.append("typ_predka");
        else if (field==Field.PARENT)
            sb.append("predek");
        else if (field==Field.CHILD_TYPE)
            sb.append("typ_potomka");
        else if (field==Field.CHILD)
            sb.append("potomek");
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
