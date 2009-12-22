/*
 * Copyright (C) 2005 Leos Literak
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; see the file COPYING. If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */
package cz.abclinuxu.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import cz.abclinuxu.data.CommonObject;
import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Poll;
import cz.abclinuxu.data.Record;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Server;
import cz.abclinuxu.data.Tag;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Solution;
import cz.abclinuxu.exceptions.PersistenceException;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.OperationIn;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.QualifierTool;
import cz.abclinuxu.persistence.extra.tags.TagExpression;
import cz.abclinuxu.persistence.impl.MySqlPersistence;
import cz.abclinuxu.persistence.versioning.VersionedDocument;
import cz.abclinuxu.scheduler.VariableFetcher;
import cz.abclinuxu.utils.BeanFetcher;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.TagTool;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;

/**
 * Thread-safe singleton, that encapsulates SQL commands
 * used outside of Persistance implementations.
 */
public final class SQLTool implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SQLTool.class);

    public static final String ITEM_RELATIONS_BY_TYPE = "relations.item.by.type";
    public static final String ITEM_RELATIONS_BY_TYPE_WITH_FILTERS = "relations.item.by.type.with.filters";
    public static final String CATEGORY_RELATIONS = "relations.categories";
    public static final String SECTION_RELATIONS_BY_TYPE = "relations.section.by.type";
    public static final String DISCUSSION_RELATIONS = "relations.discussion";
    public static final String DISCUSSION_RELATIONS_IN_SECTION = "relations.discussion.in.section";
    public static final String LAST_SEEN_DISCUSSION_RELATIONS_BY_USER = "relations.discussion.last.seen.by.user";
    public static final String ARTICLE_RELATIONS = "relations.article";
    public static final String ARTICLES_ON_INDEX_RELATIONS = "relations.article.on.index";
    // todo calculate it dynamically with findqualifier
    public static final String ARTICLE_RELATIONS_WITHIN_PERIOD = "relations.article.within.period";
    public static final String UNSIGNED_CONTRACT_RELATIONS = "relations.unsigned.contract";
    public static final String NEWS_RELATIONS = "relations.news";
    // todo calculate it dynamically with findqualifier
    public static final String NEWS_RELATIONS_WITHIN_PERIOD = "relations.news.within.period";
    public static final String NEWS_RELATIONS_BY_USER = "relations.news.by.user";
    public static final String QUESTION_RELATIONS_BY_USER = "relations.question.by.user";
    public static final String COMMENT_RELATIONS_BY_USER = "relations.comment.by.user";
    public static final String COUNT_DISCUSSIONS_BY_USER = "count.relations.comment.by.user";
    public static final String STANDALONE_POLL_RELATIONS = "relations.standalone.polls";
    public static final String WIKI_RELATIONS_BY_USER = "relations.wiki.by.user";
    public static final String COUNT_WIKI_RELATIONS_BY_USER = "count.relations.wiki.by.user";
    public static final String RELATIONS_WITH_TAGS = "relations.with.tag";
    public static final String USERS_WITH_WEEKLY_EMAIL = "users.with.weekly.email";
    public static final String USERS_WITH_FORUM_BY_EMAIL = "users.with.forum.by.email";
    public static final String USERS_WITH_ROLES = "users.with.roles";
    public static final String USERS_IN_GROUP = "users.in.group";
    public static final String USERS_WITH_LOGIN = "users.with.login";
    public static final String USERS_WITH_NICK = "users.with.nick";
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
    public static final String DELETE_PROPERTY = "delete.property";
    public static final String ROYALTY_RELATIONS = "relations.royalty";
    public static final String USERS_COUNT_FORUM_COMMENTS = "users.count.forum.comments";
    public static final String USERS_COUNT_SOLUTIONS = "users.count.solutions";
    public static final String USERS_COUNT_ARTICLES = "users.count.articles";
    public static final String USERS_COUNT_DIGEST_STORIES = "users.count.digest.stories";
    public static final String USERS_COUNT_WIKI_RECORDS = "users.count.wiki.records";
    public static final String USERS_COUNT_NEWS = "users.count.news";
    public static final String LAST_REVISIONS = "last.versions";
    public static final String TAG_LOG_ACTION = "tag.log.action";
    public static final String TAG_GET_CREATOR = "tag.get.creator";

    public static final String DELETE_USER = "delete.user";
    public static final String DELETE_USER_TICKET = "delete.user.ticket";

    public static final String CHANGE_REVISION_OWNER = "change.revision.owner";
    public static final String CHANGE_COMMENT_OWNER = "change.comment.owner";
    public static final String CHANGE_ITEM_OWNER = "change.item.owner";
    public static final String CHANGE_RECORD_OWNER = "change.record.owner";
    public static final String CHANGE_CATEGORY_OWNER = "change.category.owner";
    public static final String CHANGE_PROPERTY_OWNER = "change.property.owner";
    public static final String COUNT_PROPERTIES_BY_USER = "count.properties.by.user";

    public static final String MOST_COMMENTED_RELATIONS = "most.commented.relations";
    public static final String MOST_COMMENTED_RECENT_RELATIONS = "most.commented.recent.relations";
    public static final String MOST_HAVING_PROPERTY_RELATIONS = "most.having.property.relations";
    public static final String MOST_HAVING_PROPERTY_RECENT_RELATIONS = "most.having.property.recent.relations";
    public static final String MOST_COUNTED_RELATIONS = "most.counted.relations";
    public static final String MOST_COUNTED_RECENT_RELATIONS = "most.counted.recent.relations";
    public static final String MOST_COMMENTED_POLLS = "most.commented.polls";
    public static final String MOST_VOTED_POLLS = "most.voted.polls";
	public static final String HIGHEST_SCORE_USERS = "highest.score.users";

    public static final String SUBPORTALS_COUNT_ARTICLES = "subportal.count.articles";
    public static final String SUBPORTALS_COUNT_EVENTS = "subportal.count.events";
    public static final String SUBPORTALS_COUNT_FORUM_QUESTIONS = "subportal.count.forum.questions";
    public static final String SUBPORTALS_ORDERED_BY_SCORE = "subportals.ordered.by.score";
    public static final String SUBPORTALS_ORDERED_BY_MEMBER_COUNT = "subportals.ordered.by.member.count";

    public static final String VALID_SERVERS = "valid.servers";
    public static final String SERVER_RELATIONS_IN_CATEGORY = "server.relations.in.category";
    public static final String FIND_SUBPORTAL_MEMBERSHIP  = "find.subportal.membership";
    public static final String FIND_HP_SUBPORTAL_ARTICLES = "find.hp.subportal.articles";

    public static final String MAX_SUBPORTAL_READS = "max.subportal.reads";
    public static final String FIND_ADVERTISEMENT_BY_STRING = "find.advertisement.by.string";

	public static final String GET_AUTHORS_WITH_ARTICLES_COUNT = "get.authors.with.articles.count";
	public static final String COUNT_AUTHORS_WITH_ARTICLES_COUNT ="count.authors.with.articles.count";

    public static final String GET_TOPICS = "get.topics";
    public static final String COUNT_TOPICS = "count.topics";

    public static final String MONITOR_INSERT_USER = "insert.monitor";
    public static final String MONITOR_REMOVE_USER = "remove.monitor";
    public static final String MONITOR_REMOVE_ALL = "remove.users.monitors";
    public static final String MONITOR_GET = "get.monitors";
    public static final String MONITOR_FIND_BY_USER = "find.users.monitors";

    public static final String SOLUTIONS_GET = "get.solutions";
    public static final String SOLUTIONS_INSERT = "insert.solution";
    public static final String SOLUTIONS_DELETE = "delete.solutions";
    public static final String SOLUTIONS_DELETE_SINGLE = "delete.single.solution";

    private static SQLTool singleton;
    static {
        singleton = new SQLTool();
        ConfigurationManager.getConfigurator().configureAndRememberMe(singleton);
    }

    private Map<String, String> sql = new HashMap<String, String>(100, 0.9f);

    /**
     * Returns singleton of SQLTool.
     */
    public static SQLTool getInstance() {
        return singleton;
    }

    /**
     * Loads relations from database using given SQL command.
     * @param sql Command to execute.
     * @param params List of parameters. It must not be null.
     * @return List of initialized relations.
     * @throws PersistenceException if something goes wrong.
     */
    private List<Relation> loadRelations(String sql, List params) throws PersistenceException {
        if (log.isDebugEnabled())
            log.debug(sql);
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
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
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Loads relations and associated number from database using given SQL command. The query is expected to return
     * two values per row, the first must be id of relation, the second some number.
     * @param sql Command to execute.
     * @param params List of parameters. It must not be null.
     * @return LinkedMap, where keys are initialized relations and values are their numbers.
     * @throws PersistenceException if something goes wrong.
     */
    private Map<Relation, Integer> loadRelationWithNumber(String sql, List params) throws PersistenceException {
        if (log.isDebugEnabled())
            log.debug(sql);
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Relation> relations = new ArrayList<Relation>();
        Map<Relation, Integer> result = new LinkedHashMap<Relation, Integer>();
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql);
            int i = 1;
            for (Iterator iter = params.iterator(); iter.hasNext();)
                statement.setObject(i++, iter.next());

            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Relation relation = new Relation(resultSet.getInt(1));
                int score = resultSet.getInt(2);
                result.put(relation, score);
                relations.add(relation);
            }

            PersistenceFactory.getPersistence().synchronizeList(relations);
            return result;
        } catch (SQLException e) {
            throw new PersistenceException("Nemohu vykonat SQL příkaz " + sql, e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
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
    private List<Integer> loadUsers(String sql, List params) throws PersistenceException {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
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
            List<Integer> result = new ArrayList<Integer>();
            while ( resultSet.next() ) {
                result.add(resultSet.getInt(1));
            }
            return result;
        } catch (SQLException e) {
			throw new PersistenceException("Nemohu vykonat SQL příkaz " + sql, e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
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
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
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
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
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
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
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
			throw new PersistenceException("Nemohu vykonat SQL příkaz " + sql, e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
        }
    }

	/**
	 * Loads items from database using given SQL command.
	 * @param sql Command to execute.
	 * @param params List of parameters. It must not be null.
	 * @return List of initialized items.
	 * @throws PersistenceException If something goes wrong.
	 */
	private List<Item> loadItems(String sql, List params)
	        throws PersistenceException {
		if (log.isDebugEnabled())
		    log.debug(sql);

		MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			con = persistance.getSQLConnection();
			statement = con.prepareStatement(sql);
			int i = 1;
			for (Iterator iter = params.iterator(); iter.hasNext();)
				statement.setObject(i++, iter.next());

			resultSet = statement.executeQuery();
			List<Item> result = new ArrayList<Item>();
			while (resultSet.next()) {
				int id = resultSet.getInt(1);
				result.add(new Item(id));
			}
			persistance.synchronizeList(result);
			return result;
		} catch (SQLException e) {
			throw new PersistenceException("Nemohu vykonat SQL příkaz " + sql, e);
		} finally {
			PersistenceFactory.releaseSQLResources(con, statement, resultSet);
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
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
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
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
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
     * Removes a user from the database.
     * @param uid User's UID
     */
    public void deleteUser(int uid) throws Exception {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;

        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql.get(DELETE_USER));
            statement.setInt(1, uid);
            statement.executeUpdate();
            PersistenceFactory.releaseSQLResources(null, statement, null);

            statement = con.prepareStatement(sql.get(DELETE_USER_TICKET));
            statement.setInt(1, uid);
            statement.executeUpdate();

            persistance.clearCache();
            VariableFetcher.getInstance().run();
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, null);
        }
    }

    /**
     * Merges two users - changes ownership for all items from user #1 to user #2.
     * @param from UID of the original user.
     * @param to UID of the destination user.
     */
    public void mergeUsers(int from, int to) throws Exception {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;

        try {
            con = persistance.getSQLConnection();

            mergeUsersStep(con, CHANGE_REVISION_OWNER, from, to);
            mergeUsersStep(con, CHANGE_COMMENT_OWNER, from, to);
            // todo mergovat nasledujici tri SQLka
            mergeUsersStep(con, CHANGE_ITEM_OWNER, from, to);
            mergeUsersStep(con, CHANGE_RECORD_OWNER, from, to);
            mergeUsersStep(con, CHANGE_CATEGORY_OWNER, from, to);
            mergeUsersStep(con, CHANGE_PROPERTY_OWNER, from, to);
        } finally {
            PersistenceFactory.releaseSQLResources(con, (Statement) null, null);
        }
    }

    /**
     * Executes one step of merging two users
     * @param con Connection to the database
     * @param sqlKey key to map with SQL commands
     * @param from UID of the original user.
     * @param to UID of the destination user.
     */
    private void mergeUsersStep(Connection con, String sqlKey, int from, int to) throws Exception {
        PreparedStatement statement = null;
        try {
            statement = con.prepareStatement(sql.get(sqlKey));
            statement.setInt(1, to);
            statement.setInt(2, from);
            statement.executeUpdate();
        } finally {
            PersistenceFactory.releaseSQLResources(null, statement, null);
        }
    }

    /**
     * Finds properties containing the given uid. The only likely use is to verify that a user account
     * is unused.
     * @param uid User's id
     * @return property count
     */
    public int countPropertiesByUser(int uid) {
        List params = new ArrayList();
        params.add(uid);
        return loadNumber(sql.get(COUNT_PROPERTIES_BY_USER), params);
    }

    /**
     * Finds relations, where child is item of specified type.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> findItemRelationsWithType(int type, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuilder sb = new StringBuilder(sql.get(ITEM_RELATIONS_BY_TYPE));
        List params = new ArrayList();
        params.add(type);
        Map<Field, String> fieldMapping = new HashMap<Field, String>();
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
        StringBuilder sb = new StringBuilder(sql.get(ITEM_RELATIONS_BY_TYPE));
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(type);
        Map<Field, String> fieldMapping = new HashMap<Field, String>();
        fieldMapping.put(Field.UPPER, "R");
        appendQualifiers(sb, qualifiers, params, "P", fieldMapping);
        return loadNumber(sb.toString(), params);
    }

    /**
     * Finds relations, where child is associated with given tag.
     * Use Qualifiers to set additional parameters.
     * @param tag id of tag to be searched
     * @param qualifiers Additional qualifiers to narrow search
     * @return List of initialized relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> findRelationsWithTag(String tag, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuilder sb = new StringBuilder(sql.get(RELATIONS_WITH_TAGS));
        List params = new ArrayList();
        params.add(tag);
        Map<Field,String> fieldMappings = new HashMap<Field,String>();
        fieldMappings.put(Field.TYPE, "P");
        appendQualifiers(sb, qualifiers, params, null, fieldMappings);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is associated with given tag.
     * Use Qualifiers to set additional parameters.
     * @param tag id of tag to be searched
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public int countRelationsWithTag(String tag, Qualifier[] qualifiers) {
        StringBuilder sb = new StringBuilder(sql.get(RELATIONS_WITH_TAGS));
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(tag);
        Map<Field, String> fieldMappings = new HashMap<Field, String>();
        fieldMappings.put(Field.TYPE, "P");
        appendQualifiers(sb, qualifiers, params, null, fieldMappings);
        return loadNumber(sb.toString(), params);
    }

    /**
     * @param uid
     * @return Subportal relations where the user is a member.
     */
    public List<Relation> findSubportalMembership(int uid) {
        StringBuilder sb = new StringBuilder(sql.get(FIND_SUBPORTAL_MEMBERSHIP));
        return loadRelations(sb.toString(), Collections.singletonList(uid));
    }

    /**
     * Finds relations, where child is item of specified type and pass the given filters.
     * There is an AND relation between properties and OR between values of each property.
     * Use Qualifiers to set additional parameters.
     * @param filters map where key is name of property (see properties.txt) and value is Set of property values
     * @return List of initialized relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> findItemRelationsWithTypeWithFilters(int type, Qualifier[] qualifiers,
                                                               Map<String, Set<String>> filters) {
        if (qualifiers == null)
            qualifiers = new Qualifier[]{};
        if (filters.isEmpty())
            return findItemRelationsWithType(type, qualifiers);

        StringBuilder sb = new StringBuilder(sql.get(ITEM_RELATIONS_BY_TYPE_WITH_FILTERS));
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
    public int countItemRelationsWithTypeWithFilters(int type, Qualifier[] qualifiers, Map<String, Set<String>> filters) {
        if (qualifiers == null)
            qualifiers = new Qualifier[]{};
        if (filters.isEmpty())
            return countItemRelationsWithType(type, qualifiers);

        StringBuilder sb = new StringBuilder(sql.get(ITEM_RELATIONS_BY_TYPE_WITH_FILTERS));
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(type);
        appendFilterConditions(filters, sb, params);
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadNumber(sb.toString(), params);
    }

    private void appendFilterConditions(Map<String, Set<String>> filters, StringBuilder sb, List params) {
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
        StringBuilder sb = new StringBuilder(sql.get(CATEGORY_RELATIONS));
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Finds subportals and orders them by the score value
     * @return List of initialized relations
     */
    public List<Relation> findSubportalsOrderedByScore(Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuilder sb = new StringBuilder(sql.get(SUBPORTALS_ORDERED_BY_SCORE));
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Finds subportals and orders them by the member count
     * @return List of initialized relations
     */
    public List<Relation> findSubportalsOrderedByMemberCount(Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuilder sb = new StringBuilder(sql.get(SUBPORTALS_ORDERED_BY_MEMBER_COUNT));
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
        StringBuilder sb = new StringBuilder(sql.get(SECTION_RELATIONS_BY_TYPE));
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
        StringBuilder sb = new StringBuilder(sql.get(SECTION_RELATIONS_BY_TYPE));
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
        StringBuilder sb = new StringBuilder(sql.get(DISCUSSION_RELATIONS));
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is discussion item.
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public int countDiscussionRelations() {
        StringBuilder sb = new StringBuilder(sql.get(DISCUSSION_RELATIONS));
        changeToCountStatement(sb);
        List params = new ArrayList();
        return loadNumber(sb.toString(), params);
    }

    // todo fakt by to neslo nejak dat do systemPrefs.xml? Snazim se externalizovat vsechna SQLka
    private StringBuilder constructDiscussionRelationsWithTagsQuery(TagExpression expr, List<String> tags, List params) {
        StringBuilder sb = new StringBuilder();
        sb.append("select R.cislo from (select T.cislo from stitkovani T where T.typ = 'P' and T.stitek in ");
        sb.append(Misc.getInCondition(tags.size()));
        params.addAll(tags);
        sb.append(" group by T.cislo having ");
        sb.append(expr.toString());
        sb.append(") T join relace R on (T.cislo = R.potomek) join spolecne S on (S.cislo = R.potomek and S.typ = 'P')"
                +" where R.typ_potomka = 'P' and R.predchozi = ?");
        return sb;
    }

    public List<Relation> findDiscussionRelationsWithTags(TagExpression expr, List<String> tags, int parent, Qualifier[] qualifiers) {
        if (tags.size() == 0 || expr == null)
            return findDiscussionRelationsWithParent(parent, qualifiers);

        List params = new ArrayList(tags.size()+2);
        StringBuilder sb = constructDiscussionRelationsWithTagsQuery(expr, tags, params);
        params.add(parent);

        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelations(sb.toString(), params);
    }

    public int countDiscussionRelationsWithTags(TagExpression expr, List<String> tags, int parent) {
        if (tags.size() == 0 || expr == null)
            return countDiscussionRelationsWithParent(parent);

        List params = new ArrayList(tags.size()+2);
        StringBuilder sb = constructDiscussionRelationsWithTagsQuery(expr, tags, params);
        params.add(parent);

        changeToCountStatement(sb);
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
        StringBuilder sb = new StringBuilder(sql.get(DISCUSSION_RELATIONS_IN_SECTION));
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
        StringBuilder sb = new StringBuilder(sql.get(DISCUSSION_RELATIONS_IN_SECTION));
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(parent);
        return loadNumber(sb.toString(), params);
    }

    /**
     * Finds relations, where child is discussion item previously seen by specified user.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> findLastSeenDiscussionRelationsBy(int who, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuilder sb = new StringBuilder(sql.get(LAST_SEEN_DISCUSSION_RELATIONS_BY_USER));
        List params = new ArrayList();
        params.add(who);
        Map<Field, String> fieldMapping = new HashMap<Field, String>();
        fieldMapping.put(Field.WHEN, "K");
        appendQualifiers(sb, qualifiers, params, "P", fieldMapping);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is discussion item previously seen by specified user.
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public int countLastSeenDiscussionRelationsBy(int who) {
        StringBuilder sb = new StringBuilder(sql.get(LAST_SEEN_DISCUSSION_RELATIONS_BY_USER));
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(who);
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
        StringBuilder sb = new StringBuilder(sql.get(ARTICLES_ON_INDEX_RELATIONS));
        List params = new ArrayList();
        Map<Field, String> fieldMapping = new HashMap<Field, String>();
        fieldMapping.put(Field.UPPER, "R");
        appendQualifiers(sb, qualifiers, params, "P", fieldMapping);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Finds relations, where child is a contract template item in active mode (not draft, not obsolete)
     * and there is no signed contract owned by given user.
     *
     * @param uid id of user searched for contracts
     * @return initialized relation or null
     * @throws PersistenceException if there is an error with the underlying
     *                              persistent storage.
     */
    public Relation findUnsignedContractRelation(int uid) {
        StringBuilder sb = new StringBuilder(sql.get(UNSIGNED_CONTRACT_RELATIONS));
        List params = new ArrayList();
        params.add(uid);
        List<Relation> relations = loadRelations(sb.toString(), params);
        if (!relations.isEmpty()) {
            Relation relation = relations.get(0);
            Tools.sync(relation);
            return relation;
        } else {
            return null;
        }
    }

    /**
     * Finds articles from subportals that are supposed to be shown on the HP.
     * @param qualifiers
     * @return
     */
    public List<Relation> findHPSubportalArticles(Qualifier[] qualifiers) {
        StringBuilder sb = new StringBuilder(sql.get(FIND_HP_SUBPORTAL_ARTICLES));
        List params = new ArrayList();
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
        StringBuilder sb = new StringBuilder(sql.get(ARTICLE_RELATIONS));
        List params = new ArrayList();
        if (section>0) {
            params.add(section);
            sb.append(" and K.cislo=?");
        }
        Map<Field, String> fieldMapping = new HashMap<Field, String>();
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
        StringBuilder sb = new StringBuilder(sql.get(ARTICLE_RELATIONS));
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
        StringBuilder sb = new StringBuilder(sql.get(ARTICLE_RELATIONS_WITHIN_PERIOD));
        List params = new ArrayList();
        params.add(new java.sql.Date(from.getTime()));
        params.add(new java.sql.Date(until.getTime()));
        Map<Field, String> fieldMapping = new HashMap<Field, String>();
        fieldMapping.put(Field.UPPER, "R");
        appendQualifiers(sb, qualifiers, params, "P", fieldMapping);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Finds relations, where child is an article item with created property,
     * that is inside given time period.
     */
    public int countArticleRelationsWithinPeriod(Date from, Date until) throws PersistenceException {
        StringBuilder sb = new StringBuilder(sql.get(ARTICLE_RELATIONS_WITHIN_PERIOD));
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
        StringBuilder sb = new StringBuilder(sql.get(NEWS_RELATIONS));
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
        StringBuilder sb = new StringBuilder(sql.get(NEWS_RELATIONS));
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
        StringBuilder sb = new StringBuilder(sql.get(NEWS_RELATIONS_WITHIN_PERIOD));
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
        StringBuilder sb = new StringBuilder(sql.get(NEWS_RELATIONS_WITHIN_PERIOD));
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(new java.sql.Date(from.getTime()));
        params.add(new java.sql.Date(until.getTime()));
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
        StringBuilder sb = new StringBuilder(sql.get(ARTICLE_RELATIONS_BY_AUTHOR));
        List params = new ArrayList();
        params.add(authorId);
        Map<Field, String> fieldMapping = new HashMap<Field, String>();
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
	    StringBuilder sb = new StringBuilder(sql.get(COUNT_ARTICLES_BY_AUTHOR));
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
	    StringBuilder sb = new StringBuilder(sql.get(COUNT_ARTICLES_BY_AUTHORS));
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
        StringBuilder sb = new StringBuilder(sql.get(NEWS_RELATIONS_BY_USER));
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
        StringBuilder sb = new StringBuilder(sql.get(NEWS_RELATIONS_BY_USER));
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
        StringBuilder sb = new StringBuilder(sql.get(QUESTION_RELATIONS_BY_USER));
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
        StringBuilder sb = new StringBuilder(sql.get(QUESTION_RELATIONS_BY_USER));
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(userId);
        return loadNumber(sb.toString(), params);
    }

    /**
     * Finds relations, where child is wiki item edited by user.
     * Use Qualifiers to set additional parameters.
     * @return List of initialized relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> findWikiRelationsByUser(int userId, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuilder sb = new StringBuilder(sql.get(WIKI_RELATIONS_BY_USER));
        List params = new ArrayList();
        params.add(userId);
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is wiki item edited by user.
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public int countWikiRelationsByUser(int userId) {
        StringBuilder sb = new StringBuilder(sql.get(COUNT_WIKI_RELATIONS_BY_USER));
        return loadNumber(sb.toString(), Collections.singletonList(userId));
    }

    /**
     * Finds relations, where child is discussion item with comment from user.
     * Use Qualifiers to set additional parameters. Qualifier SORT_BY is prohibited
     * @return List of initialized relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> findCommentRelationsByUser(int userId, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuilder sb = new StringBuilder(sql.get(COMMENT_RELATIONS_BY_USER));
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
        StringBuilder sb = new StringBuilder(sql.get(COUNT_DISCUSSIONS_BY_USER));
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
        List result = loadRelations(sql.get(RELATION_BY_URL), params);
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
        StringBuilder sb = new StringBuilder(sql.get(STANDALONE_POLL_RELATIONS));
        List params = new ArrayList();
        Map<Field, String> fieldMapping = new HashMap<Field, String>();
        fieldMapping.put(Field.CREATED, "A");
        appendQualifiers(sb, qualifiers, params, null, fieldMapping);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Counts relations, where child is poll and upper relation is 250.
     * @throws PersistenceException
     *          if there is an error with the underlying persistent storage.
     */
    public int countStandalonePollRelations() {
        StringBuilder sb = new StringBuilder(sql.get(STANDALONE_POLL_RELATIONS));
        changeToCountStatement(sb);
        List params = new ArrayList();
        return loadNumber(sb.toString(), params);
    }

    /**
     * Finds users, the condition or sort order must be specified in qualifiers.
     * @return list of Integers of user ids.
     */
    public List<Integer> findUsers(Qualifier[] qualifiers) {
        if ( qualifiers == null || qualifiers.length == 0)
            throw new IllegalArgumentException("qualifiers are mandatory");
        StringBuilder sb = new StringBuilder("select cislo from uzivatel ");
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadUsers(sb.toString(), params);
    }

    /**
     * Finds users, that have active email and have subscribed weekly email.
     * Use Qualifiers to set additional parameters.
     * @return list of Integers of user ids.
     */
    public List<Integer> findUsersWithWeeklyEmail(Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuilder sb = new StringBuilder(sql.get(USERS_WITH_WEEKLY_EMAIL));
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadUsers(sb.toString(), params);
    }

    /**
     * Finds users, that have active email and have subscribed forum by email.
     * Use Qualifiers to set additional parameters.
     * @return list of Integers of user ids.
     */
    public List<Integer> findUsersWithForumByEmail(Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuilder sb = new StringBuilder(sql.get(USERS_WITH_FORUM_BY_EMAIL));
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadUsers(sb.toString(), params);
    }

    /**
     * Finds users with given login (case and locale insensitive search).
     * Use Qualifiers to set additional parameters.
     * @return list of Integers of user ids.
     */
    public List<Integer> findUsersWithLogin(String login, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        // todo odstranit SQL ze systemPrefs, pouzit findUsers s qualifiery
        StringBuilder sb = new StringBuilder(sql.get(USERS_WITH_LOGIN));
        List params = new ArrayList();
        params.add(login);
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadUsers(sb.toString(), params);
    }

    /**
     * Finds users with given login (case and locale insensitive search).
     * Use Qualifiers to set additional parameters.
     * @return list of Integers of user ids.
     */
    public List<Integer> findUsersByLogins(List<String> logins) {
        if (logins == null || logins.isEmpty())
            return Collections.emptyList();

        Qualifier[] qualifiers = new Qualifier[] {
            new CompareCondition(Field.LOGIN, new OperationIn(logins.size()), logins)
        };
        return findUsers(qualifiers);
    }

    /**
     * Finds users with given nick name (case and locale insensitive search).
     * Use Qualifiers to set additional parameters.
     * @return list of Integers of user ids.
     */
    public List<Integer> findUsersWithNick(String nick, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        // todo odstranit SQL ze systemPrefs, pouzit findUsers s qualifiery
        StringBuilder sb = new StringBuilder(sql.get(USERS_WITH_NICK));
        List params = new ArrayList();
        params.add(nick);
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadUsers(sb.toString(), params);
    }

    /**
     * Finds users, that have at least one role.
     * Use Qualifiers to set additional parameters.
     * @return list of Integers of user ids.
     */
    public List<Integer> findUsersWithRoles(Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuilder sb = new StringBuilder(sql.get(USERS_WITH_ROLES));
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
    public List<Integer> findUsersInGroup(int group, Qualifier[] qualifiers) {
        if ( qualifiers==null ) qualifiers = new Qualifier[]{};
        StringBuilder sb = new StringBuilder(sql.get(USERS_IN_GROUP));
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
        StringBuilder sb = new StringBuilder(sql.get(USERS_IN_GROUP));
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
        // todo odstranit SQL ze systemPrefs, pouzit findUsers s qualifiery
        StringBuilder sb = new StringBuilder(sql.get(USER_BY_LOGIN));
        List params = new ArrayList();
        params.add(login);
        return loadNumber(sb.toString(), params);
    }

    /**
     * Finds all users that commented some question in the forum and number of their comments there.
     * @return list of integer arrays, first item is user id, second item is count of his comments
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Object[]> countUsersCommentsInForum() {
        StringBuilder sb = new StringBuilder(sql.get(USERS_COUNT_FORUM_COMMENTS));
        return loadObjects(sb.toString(), Collections.EMPTY_LIST);
    }

    /**
     * Finds all users that wrote an answear to some question accepted as solution of the question.
     * @return list of integer arrays, first item is user id, second item is count of his comments
     * accepted as a solution by question author and the third item is count of his comments marked 
     * as a solution by other users.
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Object[]> countUsersSolutions() {
        StringBuilder sb = new StringBuilder(sql.get(USERS_COUNT_SOLUTIONS));
        return loadObjects(sb.toString(), Collections.EMPTY_LIST);
    }

    /**
     * Finds all users that wrote some articles and number of their articles.
     * @return list of arrays, first item is user id (String), second item is count of his articles (integer)
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Object[]> countUsersArticles() {
        StringBuilder sb = new StringBuilder(sql.get(USERS_COUNT_ARTICLES));
        return loadObjects(sb.toString(), Collections.EMPTY_LIST);
    }

    /**
     * Finds all users that wrote blog stories marked as digest and their count.
     * @return list of arrays, first item is user id (String), second item is count of his stories (integer)
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Object[]> countUsersDigestStories() {
        StringBuilder sb = new StringBuilder(sql.get(USERS_COUNT_DIGEST_STORIES));
        return loadObjects(sb.toString(), Collections.EMPTY_LIST);
    }

    /**
     * Finds all users that updated some wiki record and number of their modified (distinct) wiki records.
     * @return list of integer arrays, first item is user id, second item is count of his wiki records
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Object[]> countUsersModifiedWikiRecords() {
        StringBuilder sb = new StringBuilder(sql.get(USERS_COUNT_WIKI_RECORDS));
        return loadObjects(sb.toString(), Collections.EMPTY_LIST);
    }

    /**
     * Finds all users that wrote some news and number of their news.
     * @return list of integer arrays, first item is user id, second item is count of news
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Object[]> countUsersNews() {
        StringBuilder sb = new StringBuilder(sql.get(USERS_COUNT_NEWS));
        return loadObjects(sb.toString(), Collections.EMPTY_LIST);
    }

    /**
     * Finds all subportals with articles.
     * @return a list of integer arrays with the first item being the user id and the second the number of articles
     */
    public List<Object[]> countSubportalArticles() {
        StringBuilder sb = new StringBuilder(sql.get(SUBPORTALS_COUNT_ARTICLES));
        return loadObjects(sb.toString(), Collections.EMPTY_LIST);
    }

    /**
     * Finds all subportals with events.
     * @return a list of integer arrays with the first item being the user id and the second the number of events
     */
    public List<Object[]> countSubportalEvents() {
        StringBuilder sb = new StringBuilder(sql.get(SUBPORTALS_COUNT_EVENTS));
        return loadObjects(sb.toString(), Collections.EMPTY_LIST);
    }

    /**
     * Finds all subportals with forum questions.
     * @return a list of integer arrays with the first item being the user id and the second the number of questions
     */
    public List<Object[]> countSubportalForumQuestions() {
        StringBuilder sb = new StringBuilder(sql.get(SUBPORTALS_COUNT_FORUM_QUESTIONS));
        return loadObjects(sb.toString(), Collections.EMPTY_LIST);
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

        Persistence persistence = PersistenceFactory.getPersistence();
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
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.createStatement();
            resultSet = statement.executeQuery(sql.get(MAX_USER));
            if ( !resultSet.next() )
                return 0;
            return resultSet.getInt(1);
        } catch (SQLException e) {
            throw new PersistenceException("Chyba při hledání!", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Sets updated timestamp in database for given GenericObject.
     * @param obj object to be modified.
     * @param date timestamp to be set.
     * @throws PersistenceException if object doesn't contain such property.
     */
    public void setUpdatedTimestamp(GenericDataObject obj, Date date) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        obj.setUpdated(date);
        persistance.storeInCache(obj);

        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = persistance.getSQLConnection();
            String sql = "update spolecne set zmeneno=? where typ=? and cislo=?";
            statement = con.prepareStatement(sql);
            statement.setTimestamp(1, new Timestamp(date.getTime()));
            statement.setString(2, PersistenceMapping.getGenericObjectType(obj));
            statement.setInt(3, obj.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Chyba při hledání!", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, null);
        }
    }

    /**
	 * Finds items of given type with applied qualifiers
	 * @param type Type of items to be retrieved
	 * @param qualifiers Narrowing qualifiers
	 * @return List of items which fitted conditions
	 */
	public List<Item> findItemsWithType(int type, Qualifier[] qualifiers) {
		StringBuilder sb = new StringBuilder(sql.get(ITEMS_WITH_TYPE));
		List params = new ArrayList();
		params.add(type);
		appendQualifiers(sb, qualifiers, params, null, null);
		return loadItems(sb.toString(), params);
	}

    /**
     * Counts items of given type with applied qualifiers
     *
     * @param type       Type of items to be retrieved
     * @param qualifiers Narrowing qualifiers
     * @return Number of items which fitted conditions
     */
    public Integer countItemsWithType(int type, Qualifier[] qualifiers) {
        StringBuilder sb = new StringBuilder(sql.get(ITEMS_WITH_TYPE));
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(type);
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadNumber(sb.toString(), params);
    }

    /**
     * Finds items of given type ordered by id property in ascending order.
     * Use offset to skip some record .
     * @return List of initialized Items
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Item> findItemsWithType(int type, int offset, int count) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            StringBuilder sb = new StringBuilder(sql.get(ITEMS_WITH_TYPE));
            sb.append(" order by cislo asc limit ?,?");
            statement = con.prepareStatement(sb.toString());
            statement.setInt(1, type);
            statement.setInt(2, offset);
            statement.setInt(3, count);

            resultSet = statement.executeQuery();
            List<Item> result = new ArrayList<Item>(count);
            while ( resultSet.next() ) {
                int id = resultSet.getInt(1);
                result.add(new Item(id));
            }
            persistance.synchronizeList(result);
            return result;
        } catch (SQLException e) {
            throw new PersistenceException("Chyba při hledání!", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds items of given type ordered by id property in ascending order.
     * Use offset to skip some record.
     * @return List of initialized Records
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List findRecordsWithType(int type, int offset, int count) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            StringBuilder sb = new StringBuilder(sql.get(RECORDS_WITH_TYPE));
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
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds items of given type and subtype ordered by id property in ascending order.
     * @return List of itialized Items
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List findItemsWithTypeAndSubtype(int type, String subType) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            StringBuilder sb = new StringBuilder(sql.get(ITEMS_WITH_TYPE));
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
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds item relations that alphabetically neighbours selected one.
     * @param type Item type, e.g. Item.DICTIONARY
     * @param smaller whether the returned items shall be smaller or greater
     * @return List of itialized Relations, first item is closest to selected one.
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> getNeighbourItemRelations(String urlName, int type, boolean smaller, int count) {
        StringBuilder sb = new StringBuilder(sql.get(ITEM_RELATIONS_BY_TYPE));
        if (smaller)
            sb.append("  and podtyp<? order by podtyp desc limit ?");
        else
            sb.append("  and podtyp>? order by podtyp asc limit ?");

        List params = new ArrayList();
        params.add(type);
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
        StringBuilder sb = new StringBuilder(sql.get(ITEMS_WITH_TYPE));
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
        StringBuilder sb = new StringBuilder(sql.get(RECORDS_WITH_TYPE));
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
        return loadNumber(sql.get(GET_LAST_COMMENT), params);
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
        return loadObjects(sql.get(GET_LAST_COMMENTS), params);
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
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql.get(INSERT_LAST_COMMENT));
            statement.setInt(1, userId);
            statement.setInt(2, discussion);
            statement.setInt(3, lastComment);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Chyba pri ukladani!", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, null);
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
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql.get(GET_LAST_COMMENTS));
            statement.setInt(1, userId);
            statement.setInt(2, preserveCount);
            statement.setInt(3, 1);
            resultSet = statement.executeQuery();
            if ( !resultSet.next() )
                return 0;

            Timestamp timestamp = resultSet.getTimestamp(3);
            statement = con.prepareStatement(sql.get(DELETE_OLD_COMMENTS));
            statement.setInt(1, userId);
            statement.setTimestamp(2, timestamp);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Chyba pri ukladani!", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
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
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql.get(INSERT_USER_ACTION));
            statement.setInt(1, userId);
            statement.setInt(2, rid);
            statement.setString(3, type);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Chyba pri ukladani!", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, null);
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
        return loadDate(sql.get(GET_USER_ACTION), params);
    }

    /**
     * Removes information when user performed specified action on selected relation.
     * @param userId id of the user
     * @param rid    id of the relation where the user performed the action
     * @param type   optional type of the user action on given relation
     */
    public void removeUserAction(int userId, int rid, String type) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql.get(REMOVE_USER_ACTION));
            statement.setInt(1, userId);
            statement.setInt(2, rid);
            statement.setString(3, type);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Chyba pri mazani!", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, null);
        }
    }

    /**
     * Finds all sections containing at least one Item and returns number of their Items.
     * @param categories List of Integers containing id of categories to be examined
     * @return map where section id is a key and number of children is a value
     */
    public Map<Integer, Integer> getItemsCountInSections(List categories) {
        if (categories.isEmpty())
            return Collections.emptyMap();

        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            int id, size;
            Map<Integer, Integer> result = new HashMap<Integer, Integer> (categories.size());

            String query = sql.get(ITEMS_COUNT_IN_SECTION);
            int position = query.indexOf('?');
            query = query.substring(0, position) + Misc.getInCondition(categories.size())
                    + query.substring(position + 1);

            con = persistance.getSQLConnection();
            statement = con.prepareStatement(query);
            int i = 1;
            for (Iterator iter = categories.iterator(); iter.hasNext();) {
                id = (Integer) iter.next();
                statement.setInt(i++, id);
            }

            rs = statement.executeQuery();
            while (rs.next()) {
                id = rs.getInt(1);
                size = rs.getInt(2);
                result.put(id, size);
            }

            return result;
        } catch (SQLException e) {
            throw new PersistenceException("Chyba v SQL!", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, rs);
        }
    }

    /**
     * Finds all sections containing at least one Item and returns number of their Items.
     * @param categories List of Integers containing id of categories to be examined
     * @return map where section id is a key and value is array of integers. The first element
     * is count of items in specified section and the second is relation id of last item.
     */
    public Map<Integer, Integer[]> getLastItemAndItemsCountInSections(List categories) {
        if (categories.isEmpty())
            return Collections.emptyMap();

        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try {
            int id, size, last;
            Map<Integer, Integer[]> result = new HashMap<Integer, Integer[]> (categories.size());

            String query = sql.get(LAST_ITEM_AND_COUNT_IN_SECTION);
            int position = query.indexOf('?');
            query = query.substring(0, position) + Misc.getInCondition(categories.size())
                    + query.substring(position + 1);

            con = persistance.getSQLConnection();
            statement = con.prepareStatement(query);
            int i = 1;
            for (Iterator iter = categories.iterator(); iter.hasNext();) {
                id = (Integer) iter.next();
                statement.setInt(i++, id);
            }

            rs = statement.executeQuery();
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
            PersistenceFactory.releaseSQLResources(con, statement, rs);
        }
    }

	public Map<User, Integer> getHighestScoreUsers(Qualifier[] qualifiers) {
		MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

		try {
			List params = new ArrayList();
            con = persistance.getSQLConnection();

            StringBuilder sb = new StringBuilder(sql.get(HIGHEST_SCORE_USERS));

			appendQualifiers(sb, qualifiers, params, null, null);
			statement = con.prepareStatement(sb.toString());

			int i = 1;
            for ( Iterator iter = params.iterator(); iter.hasNext(); )
                statement.setObject(i++, iter.next());

            List<User> users = new ArrayList<User>();
            Map<User, Integer> result = new LinkedHashMap<User, Integer>();
            rs = statement.executeQuery();
			while (rs.next()) {
				User user = new User(rs.getInt(1));
				int score = rs.getInt(2);
				result.put(user, score);
                users.add(user);
			}

            persistance.synchronizeList(users);
            return result;
		} catch (SQLException e) {
            throw new PersistenceException("Chyba v SQL!", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, rs);
        }
	}

    public Map<Relation, Integer> getMostCommentedPolls(Qualifier[] qualifiers) {
        if (qualifiers == null)
            qualifiers = new Qualifier[]{};
        StringBuilder sb = new StringBuilder(sql.get(MOST_COMMENTED_POLLS));
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelationWithNumber(sb.toString(), params);
    }

    public Map<Relation, Integer> getMostVotedPolls(Qualifier[] qualifiers) {
        if (qualifiers == null)
            qualifiers = new Qualifier[]{};
        StringBuilder sb = new StringBuilder(sql.get(MOST_VOTED_POLLS));
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelationWithNumber(sb.toString(), params);
    }

    /**
     * Finds relations for specified item that have biggest value of given counter. If dateFrom is specified, only
     * items created since this time are considered.
     * @param itemType type of Item
     * @param counter counter type, see Constants.
     * @param dateFrom optional date
     * @param qualifiers optional qualifiers
     * @return map, where key is initialized relation and value is value of its counter
     */
    public Map<Relation, Integer> getMostCountedRelations(int itemType, String counter, Date dateFrom, Qualifier[] qualifiers) {
        if (qualifiers == null)
            qualifiers = new Qualifier[]{};
        StringBuilder sb;
        List params = new ArrayList();
        params.add(counter);
        params.add(itemType);

        if (dateFrom != null) {
            sb = new StringBuilder(sql.get(MOST_COUNTED_RECENT_RELATIONS));
            params.add(dateFrom);
        } else
            sb = new StringBuilder(sql.get(MOST_COUNTED_RELATIONS));

        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelationWithNumber(sb.toString(), params);
    }

    /**
     * Finds relations for specified item that have biggest value of given counter. If dateFrom is specified, only
     * items created since this time are considered.
     * @param itemType type of Item
     * @param property property, see Constants or properties.txt
     * @param dateFrom optional date
     * @param qualifiers optional qualifiers
     * @return map, where key is initialized relation and value is count of this property
     */
    public Map<Relation, Integer> getMostHavingPropertyRelations(int itemType, String property, Date dateFrom, Qualifier[] qualifiers) {
        if (qualifiers == null)
            qualifiers = new Qualifier[]{};
        StringBuilder sb;
        List params = new ArrayList();
        params.add(property);
        params.add(itemType);

        if (dateFrom != null) {
            sb = new StringBuilder(sql.get(MOST_HAVING_PROPERTY_RECENT_RELATIONS));
            params.add(dateFrom);
        } else
            sb = new StringBuilder(sql.get(MOST_HAVING_PROPERTY_RELATIONS));

        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelationWithNumber(sb.toString(), params);
    }

    /**
     * Finds relations for specified item that have biggest number of comments. If dateFrom is specified, only
     * items created since this time are considered.
     * @param itemType type of Item
     * @param dateFrom optional date
     * @param qualifiers optional qualifiers
     * @return map, where key is initialized relation and value is number of comments
     */
    public Map<Relation, Integer> getMostCommentedRelations(int itemType, Date dateFrom, Qualifier[] qualifiers) {
        if (qualifiers == null)
            qualifiers = new Qualifier[]{};
        StringBuilder sb;
        List params = new ArrayList();
        params.add(itemType);

        if (dateFrom != null) {
            sb = new StringBuilder(sql.get(MOST_COMMENTED_RECENT_RELATIONS));
            params.add(dateFrom);
        } else
            sb = new StringBuilder(sql.get(MOST_COMMENTED_RELATIONS));

        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelationWithNumber(sb.toString(), params);
    }

    /**
     * Records new page view in statistics for selected type of page.
     */
    public void recordPageView(String pageType, int count) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statementUpdate = null, statementInsert = null;
        try {
            con = persistance.getSQLConnection();
            statementUpdate = con.prepareStatement(sql.get(INCREMENT_STATISTICS));
            statementUpdate.setInt(1, count);
            statementUpdate.setString(2, pageType);
            int updated = statementUpdate.executeUpdate();
            if (updated != 0)
                return;

            statementInsert = con.prepareStatement(sql.get(INSERT_STATISTICS));
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
            PersistenceFactory.releaseSQLResources(con, new Statement[]{statementInsert, statementUpdate}, null);
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
        StringBuilder sb = new StringBuilder(sql.get(GET_STATISTICS));
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
        StringBuilder sb = new StringBuilder(sql.get(GET_STATISTICS_BY_MONTH));
        return loadObjects(sb.toString(), Collections.EMPTY_LIST);
    }

    /**
     * Records the query. If it has not been recorded yet, this query is
     * inserted, otherwise its counter is incremented.
     * @param query normalized query string
     */
    public void recordSearchedQuery(String query) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statementUpdate = null, statementInsert = null;
        try {
            con = persistance.getSQLConnection();
            statementUpdate = con.prepareStatement(sql.get(INCREMENT_SEARCH_QUERY));
            statementUpdate.setString(1, query);
            int updated = statementUpdate.executeUpdate();
            if (updated != 0)
                return;

            statementInsert = con.prepareStatement(sql.get(INSERT_SEARCH_QUERY));
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
            PersistenceFactory.releaseSQLResources(con, new Statement[]{statementInsert, statementUpdate}, null);
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
        StringBuilder sb = new StringBuilder(sql.get(GET_SEARCH_QUERY));
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
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql.get(PROPERTY_VALUES));
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
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Sets (or updates) selected object properties.
     * @param obj object which properties shall be updated
     * @param type property to be updated in database (constant as defined in properties.txt)
     * @param values property values to be updated
     */
    public void setProperty(CommonObject obj, String type, Set values) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql.get(DELETE_PROPERTY));
            statement.setString(1, PersistenceMapping.getGenericObjectType(obj));
            statement.setInt(2, obj.getId());
            statement.setString(3, type);
            statement.executeUpdate();

            persistance.saveCommonObjectProperties(obj, Collections.singletonMap(type, values), false);
        } catch (SQLException e) {
            throw new PersistenceException("Chyba při hledání!", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Logs action with the tag.
     * @param tag mandatory parameter containing affected tag
     * @param action action performed with the tag
     * @param user optional parameter holding instance of User object that performed the action
     * @param ip mandatory parameter holding IP address of the user that performed the action
     * @param document document to which this tag was (un)assigned, null for other actions
     */
    public void logTagAction(Tag tag, TagTool.Action action, User user, String ip, GenericObject document) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql.get(TAG_LOG_ACTION));
            statement.setString(1, action.toString());
            statement.setString(2, tag.getId());

            if (user == null) {
                statement.setNull(3, Types.INTEGER);
            } else {
                statement.setInt(3, user.getId());
            }
            statement.setString(4, ip);

            if (action.equals(TagTool.Action.ASSIGN) || action.equals(TagTool.Action.UNASSIGN)) {
                statement.setString(5, PersistenceMapping.getGenericObjectType(document));
                statement.setInt(6, document.getId());
                statement.setNull(7, Types.VARCHAR);
            } else {
                statement.setNull(5, Types.CHAR);
                statement.setNull(6, Types.INTEGER);
                statement.setString(7, tag.getTitle());
            }

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Chyba při logování akce se štítkem!", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, null);
        }
    }

	/**
	 * Gets the creator of the tag
	 * @param tag
	 * @return Map containg one or two values: ip (String) and possibly user (User)
	 */
	public Map getTagCreator(String tag) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql.get(TAG_GET_CREATOR));
            statement.setString(1, tag);

            resultSet = statement.executeQuery();

            if (!resultSet.next())
                return null;

            HashMap<String,Object> info = new HashMap<String,Object>(2);
            String ip = resultSet.getString(1);
            Integer uid = resultSet.getInt(2);

            info.put("ip", ip);

            if (uid != null && uid != 0) {
                User user = new User(uid);
                persistance.synchronize(user);

                info.put("user", user);
            }

            return info;
        } catch (SQLException e) {
            throw new PersistenceException("Chyba při hledání autora štítku!", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
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
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql.get(INSERT_OLD_ADDRESS));
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
            PersistenceFactory.releaseSQLResources(con, statement, null);
        }
    }

    /**
     * Inserts monitor on given object for specified user.
     * @param obj item or category that user wants to monitor
     * @param user user wishing to start monitor
     */
    public void insertMonitor(GenericDataObject obj, User user) {
        alterMonitor(obj, user, true);
    }

    /**
     * Removes monitor of given object for specified user.
     * @param obj item or category that user wants to stop monitoring
     * @param user user wishing to stop monitor
     */
    public void removeMonitor(GenericDataObject obj, User user) {
        alterMonitor(obj, user, false);
    }

    private int alterMonitor(GenericDataObject obj, User user, boolean insert) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            if (insert)
                statement = con.prepareStatement(sql.get(MONITOR_INSERT_USER));
            else
                statement = con.prepareStatement(sql.get(MONITOR_REMOVE_USER));
            statement.setString(1, PersistenceMapping.getGenericObjectType(obj));
            statement.setInt(2, obj.getId());
            statement.setInt(3, user.getId());

            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Chyba při změně monitoru!", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Removes all monitors for given user.
     * @param user user
     * @return number of removed monitors
     */
    public int removeAllMonitors(User user) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql.get(MONITOR_REMOVE_ALL));
            statement.setInt(1, user.getId());

            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Chyba při vypínání monitoru!", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Loads users monitoring given document.
     * @param obj document
     * @return Set of user ids
     */
    public Set<Integer> getMonitors(GenericDataObject obj) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql.get(MONITOR_GET));
            statement.setString(1, PersistenceMapping.getGenericObjectType(obj));
            statement.setInt(2, obj.getId());
            resultSet = statement.executeQuery();

            Set<Integer> result = new HashSet<Integer>();
            while (resultSet.next()) {
                result.add(resultSet.getInt(1));
            }
            return result;
        } catch (SQLException e) {
            throw new PersistenceException("Chyba při hledání monitoru!", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds relations where child is monitored by specified user. You shall
     * add additional criteria into qualifiers.
     * @return List of initialized relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public List<Relation> findMonitoredRelations(int user, Qualifier[] qualifiers) {
        if (qualifiers == null)
            qualifiers = new Qualifier[]{};
        StringBuilder sb = new StringBuilder(sql.get(MONITOR_FIND_BY_USER));
        List params = new ArrayList();
        params.add(user);
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadRelations(sb.toString(), params);
    }

    /**
     * Count relations where child is monitored by specified user.
     * @return number of found relations
     * @throws PersistenceException if there is an error with the underlying persistent storage.
     */
    public int countMonitoredRelations(int user) {
        StringBuilder sb = new StringBuilder(sql.get(MONITOR_FIND_BY_USER));
        changeToCountStatement(sb);
        List params = new ArrayList();
        params.add(user);
        return loadNumber(sb.toString(), params);
    }

    /**
     * Loads solutions for given question.
     * @param diz question
     * @return List of solutions
     */
    public List<Solution> getSolutions(Item diz) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql.get(SOLUTIONS_GET));
            statement.setInt(1, diz.getId());
            resultSet = statement.executeQuery();

            List<Solution> result = new ArrayList<Solution>();
            Solution solution = null;
            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                if (solution == null || solution.getId() != id) { // SQL query is ordered by id
                    solution = new Solution(id);
                    result.add(solution);
                }
                solution.addVoter(resultSet.getInt(2));
            }
            return result;
        } catch (SQLException e) {
            throw new PersistenceException("Chyba při hledání řešení!", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
        }
    }

    public boolean insertSolutionVote(Item diz, int thread, int uid) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql.get(SOLUTIONS_INSERT));
            statement.setInt(1, uid);
            statement.setInt(2, diz.getId() );
            statement.setInt(3, thread);

            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new PersistenceException("Chyba při hlasování pro řešení!", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
        }
    }

    public boolean removeSolutionVote(Item diz, int thread, int uid) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql.get(SOLUTIONS_DELETE_SINGLE));
            statement.setInt(1, diz.getId() );
            statement.setInt(2, thread);
            statement.setInt(3, uid);

            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new PersistenceException("Chyba při mazání hlasování pro řešení!", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds new address for an old URL. If found, then it returns either initialized Relation
     * or String holding new URL. Null is returned for no match.
     * @param oldUrl urtl starting with /
     * @return null when not found, initialized Relation or String with new URL (absolute with or without host)
     */
    public Object findNewAddress(String oldUrl) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql.get(OLD_ADDRESS));
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
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds last revisions for specified object.
     * @param obj generic data object to be searched
     * @param count number of revisions to be loaded
     * @return list of VersionedDocuments
     */
    public List<VersionedDocument> getLastRevisions(GenericDataObject obj, int count) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql.get(LAST_REVISIONS));
            statement.setString(1, PersistenceMapping.getGenericObjectType(obj));
            statement.setInt(2, obj.getId());
            statement.setInt(3, count);
            resultSet = statement.executeQuery();

            List<VersionedDocument> result = new ArrayList<VersionedDocument>();
            while (resultSet.next()) {
                VersionedDocument doc = new VersionedDocument();
                doc.setVersion(resultSet.getInt(1));
                doc.setUser(resultSet.getInt(2));
                doc.setCommited(new Date(resultSet.getTimestamp(3).getTime()));
                doc.setDocument(resultSet.getString(4));
                doc.setDiff(resultSet.getString(5));
                doc.setDescription(resultSet.getString(6));
                result.add(doc);
            }

            return result;
        } catch (SQLException e) {
            throw new PersistenceException("Chyba při hledání!", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
        }
    }

    public List<Server> getValidServers() {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql.get(VALID_SERVERS));
            resultSet = statement.executeQuery();

            List<Server> result = new ArrayList();
            while (resultSet.next()) {
                Server server = new Server(resultSet.getInt(1));
                result.add(server);
            }

            persistance.synchronizeList(result);
            return result;
        } catch (SQLException e) {
            throw new PersistenceException("Chyba při hledání serverů!", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds author for given userId or returns null.
     * @param userId Id of user
     * @return initialized Relation holding Author Item
     */
    public Relation findAuthorByUserId(int userId) {
        Qualifier[] qualifiers = new Qualifier[]{new CompareCondition(Field.NUMERIC1, Operation.EQUAL, userId)};
        StringBuilder sb = new StringBuilder(sql.get(ITEM_RELATIONS_BY_TYPE));
        List params = new ArrayList();
        params.add(Item.AUTHOR);
        Map<Field, String> fieldMapping = new HashMap<Field, String>();
        fieldMapping.put(Field.TYPE, "P");
        appendQualifiers(sb, qualifiers, params, "P", fieldMapping);

        List<Relation> relations = loadRelations(sb.toString(), params);
        if (!relations.isEmpty())
            return (Relation) Tools.sync(relations.get(0));

        return null;
    }

	/**
     * Gets authors with additional information fetched, such as article count or last article date
     * @param qualifiers Narrowing qualifiers
     * @return List of arrays of object containing data
     * @see BeanFetcher To transform it to according Author JavaBean
     */
	public List<Object[]> getAuthorsWithArticlesCount(Qualifier[] qualifiers) {
		final Map<Field, String> mapping = new HashMap<Field, String>(1) {{
			put(Field.ID, "P");
		}};
		
		if (qualifiers == null)
			qualifiers = Qualifier.ARRAY_TYPE;
		StringBuilder sb = new StringBuilder(sql.get(GET_AUTHORS_WITH_ARTICLES_COUNT));
		List params = new ArrayList();
		appendQualifiers(sb, qualifiers, params, null, mapping);
		return loadObjects(sb.toString(), params);
	}
	
	/**
     * Counts authors with additional information fetched, such as article count or last article date
     * @param qualifiers Narrowing qualifiers
     * @return Size of author collection satisfying conditions
     */
	public Integer countAuthorWithArticlesCount(Qualifier[] qualifiers) {
		final Map<Field, String> mapping = new HashMap<Field, String>(1) {{
			put(Field.ID, "P");
		}};
		
		if (qualifiers == null)
			qualifiers = Qualifier.ARRAY_TYPE;
		StringBuilder sb = new StringBuilder(sql.get(COUNT_AUTHORS_WITH_ARTICLES_COUNT));
		List params = new ArrayList();
		appendQualifiers(sb, qualifiers, params, null, mapping);
		return loadNumber(sb.toString(), params);
	}

    /**
     * Retrieves topics available for authors
     *
     * @param qualifiers Narrowing qualifiers
     * @return List of items containing data
     */
    public List<Item> getTopics(Qualifier[] qualifiers) {
        if (qualifiers == null)
            qualifiers = Qualifier.ARRAY_TYPE;
        StringBuilder sb = new StringBuilder(sql.get(GET_TOPICS));
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadItems(sb.toString(), params);
    }

    /**
     * Counts topics available for authors
     *
     * @param qualifiers Narrowing qualifiers
     * @return Size of topics collection satisfying conditions
     */
    public Integer countTopics(Qualifier[] qualifiers) {
        if (qualifiers == null)
            qualifiers = Qualifier.ARRAY_TYPE;
        StringBuilder sb = new StringBuilder(sql.get(COUNT_TOPICS));
        List params = new ArrayList();
        appendQualifiers(sb, qualifiers, params, null, null);
        return loadNumber(sb.toString(), params);
    }

    public List<Relation> findServerRelationsInCategory(int cat) {
        String query = sql.get(SERVER_RELATIONS_IN_CATEGORY);
        return loadRelations(query, Collections.singletonList(cat));
    }

    public Item findAdvertisementByString(String str) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(sql.get(FIND_ADVERTISEMENT_BY_STRING));
            statement.setString(1, str);
            resultSet = statement.executeQuery();

            if (!resultSet.next())
                return null;

            return (Item) persistance.findById(new Item(resultSet.getInt(1)));
        } catch (SQLException e) {
            throw new PersistenceException("Chyba při hledání reklamy!", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Sums all read counters across subportals.
     * @return The number of reads.
     */
    public int maxSubportalReads() {
        return loadNumber(sql.get(MAX_SUBPORTAL_READS), Collections.EMPTY_LIST);
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
        store(ITEM_RELATIONS_BY_TYPE, prefs);
        store(ITEM_RELATIONS_BY_TYPE_WITH_FILTERS, prefs);
        store(CATEGORY_RELATIONS, prefs);
        store(SECTION_RELATIONS_BY_TYPE, prefs);
        store(DISCUSSION_RELATIONS, prefs);
        store(DISCUSSION_RELATIONS_IN_SECTION, prefs);
        store(LAST_SEEN_DISCUSSION_RELATIONS_BY_USER, prefs);
        store(ARTICLE_RELATIONS, prefs);
        store(ARTICLE_RELATIONS_WITHIN_PERIOD, prefs);
        store(ARTICLES_ON_INDEX_RELATIONS, prefs);
        store(UNSIGNED_CONTRACT_RELATIONS, prefs);
        store(NEWS_RELATIONS, prefs);
        store(NEWS_RELATIONS_WITHIN_PERIOD, prefs);
        store(NEWS_RELATIONS_BY_USER, prefs);
        store(WIKI_RELATIONS_BY_USER, prefs);
        store(COUNT_WIKI_RELATIONS_BY_USER, prefs);
        store(RELATIONS_WITH_TAGS, prefs);
        store(QUESTION_RELATIONS_BY_USER, prefs);
        store(COMMENT_RELATIONS_BY_USER, prefs);
        store(STANDALONE_POLL_RELATIONS, prefs);
        store(RELATION_BY_URL, prefs);
        store(USERS_WITH_WEEKLY_EMAIL, prefs);
        store(USERS_WITH_FORUM_BY_EMAIL, prefs);
        store(USERS_WITH_ROLES, prefs);
        store(USERS_WITH_LOGIN, prefs);
        store(USERS_WITH_NICK, prefs);
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
        store(DELETE_PROPERTY, prefs);
        store(ROYALTY_RELATIONS, prefs);
        store(USERS_COUNT_FORUM_COMMENTS, prefs);
        store(USERS_COUNT_SOLUTIONS, prefs);
        store(USERS_COUNT_DIGEST_STORIES, prefs);
        store(USERS_COUNT_ARTICLES, prefs);
        store(USERS_COUNT_WIKI_RECORDS, prefs);
        store(USERS_COUNT_NEWS, prefs);
        store(LAST_REVISIONS, prefs);
        store(DELETE_USER, prefs);
        store(DELETE_USER_TICKET, prefs);
        store(CHANGE_REVISION_OWNER, prefs);
        store(CHANGE_COMMENT_OWNER, prefs);
        store(CHANGE_ITEM_OWNER, prefs);
        store(CHANGE_RECORD_OWNER, prefs);
        store(CHANGE_CATEGORY_OWNER, prefs);
        store(CHANGE_PROPERTY_OWNER, prefs);
        store(COUNT_PROPERTIES_BY_USER, prefs);
        store(TAG_LOG_ACTION, prefs);
        store(TAG_GET_CREATOR, prefs);
        store(MOST_COUNTED_RELATIONS, prefs);
        store(MOST_COUNTED_RECENT_RELATIONS, prefs);
        store(MOST_COMMENTED_RELATIONS, prefs);
        store(MOST_COMMENTED_RECENT_RELATIONS, prefs);
        store(MOST_HAVING_PROPERTY_RELATIONS, prefs);
        store(MOST_HAVING_PROPERTY_RECENT_RELATIONS, prefs);
        store(SUBPORTALS_COUNT_ARTICLES, prefs);
        store(SUBPORTALS_COUNT_EVENTS, prefs);
        store(SUBPORTALS_COUNT_FORUM_QUESTIONS, prefs);
        store(SUBPORTALS_ORDERED_BY_SCORE, prefs);
        store(SUBPORTALS_ORDERED_BY_MEMBER_COUNT, prefs);
        store(VALID_SERVERS, prefs);
        store(SERVER_RELATIONS_IN_CATEGORY, prefs);
        store(FIND_SUBPORTAL_MEMBERSHIP, prefs);
        store(FIND_HP_SUBPORTAL_ARTICLES, prefs);
        store(MAX_SUBPORTAL_READS, prefs);
        store(MOST_COMMENTED_POLLS, prefs);
        store(MOST_VOTED_POLLS, prefs);
		store(HIGHEST_SCORE_USERS, prefs);
        store(FIND_ADVERTISEMENT_BY_STRING, prefs);
        store(GET_AUTHORS_WITH_ARTICLES_COUNT, prefs);
		store(COUNT_AUTHORS_WITH_ARTICLES_COUNT, prefs);
        store(MONITOR_GET, prefs);
        store(MONITOR_INSERT_USER, prefs);
        store(MONITOR_REMOVE_USER, prefs);
        store(MONITOR_REMOVE_ALL, prefs);
        store(MONITOR_FIND_BY_USER, prefs);
        store(SOLUTIONS_GET, prefs);
        store(SOLUTIONS_INSERT, prefs);
        store(SOLUTIONS_DELETE, prefs);
        store(SOLUTIONS_DELETE_SINGLE, prefs);
        store(GET_TOPICS, prefs);
        store(COUNT_TOPICS, prefs);
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

    public static void appendQualifiers(StringBuilder sb, Qualifier[] qualifiers, List params, String defaultTableNick,
                                        Map<Field, String> fieldMapping) {
        QualifierTool.appendQualifiers(sb, qualifiers, params, defaultTableNick, fieldMapping);
    }

    /**
     * Changes first select clause to count. E.g. from id to count(id).
     */
    private void changeToCountStatement(StringBuilder sb) {
        int position = sb.indexOf(" ");
        sb.insert(position + 1, "count(");
        position = sb.indexOf("from", position + 6);
        if (position == -1)
            position = sb.indexOf("FROM", position + 6);
        sb.insert(position - 1, ')');
    }
}
