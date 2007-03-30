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
package cz.abclinuxu.persistence.impl;

import java.util.*;
import java.sql.*;

import cz.abclinuxu.data.*;
import cz.abclinuxu.data.view.RowComment;
import cz.abclinuxu.data.view.DiscussionRecord;
import cz.abclinuxu.exceptions.*;
import cz.abclinuxu.AbcException;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.cache.TransparentCache;
import cz.abclinuxu.persistence.Nursery;
import cz.abclinuxu.persistence.PersistenceMapping;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.dom4j.DocumentHelper;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

/**
 * This class provides persistence backed up by MySQL database. You should consult
 * file create_mysql_scheme.sql for data scheme.
 */
public class MySqlPersistence implements Persistence {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MySqlPersistence.class);

    /** contains URL to database connection */
    String dbUrl = null;
    TransparentCache cache = null;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e) {
            log.fatal("Nemohu vytvořit instanci JDBC driveru, zkontroluj CLASSPATH!",e);
        }
    }

    public MySqlPersistence(String dbUrl) {
        if ( dbUrl==null )
            throw new MissingArgumentException("Není možné inicializovat MySqlPersistenci prázdným URL!");
        this.dbUrl = dbUrl;
    }

    public void setCache(TransparentCache cache) {
        this.cache = cache;
    }

    /**
     * Removes content of associated cache.
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Stores given object in cache. For use in SQLTool only!
     */
    public void storeInCache(GenericObject obj) {
        cache.store(obj);
    }

    public void create(GenericObject obj) {
        Connection con = null; PreparedStatement statement = null;
        if ( obj==null )
            throw new NullPointerException("Nemohu  uložit prázdný objekt!");

        try {
            con = getSQLConnection();
            if (log.isDebugEnabled()) log.debug("Chystám se uložit "+obj);
            if ( obj instanceof Poll ) {
                storePoll((Poll)obj);
            } else {
                List conditions = new ArrayList();
                StringBuffer sb = new StringBuffer();
                appendCreateParams(obj,sb,conditions);
                statement = con.prepareStatement(sb.toString());
                for ( int i=0; i<conditions.size(); i++ ) {
                    Object o = conditions.get(i);
                    statement.setObject(i+1,o);
                }

                int result = statement.executeUpdate();
                if ( result==0 )
                    throw new PersistenceException("Nepodařilo se vložit "+obj+" do databáze!");
                obj.setId(getAutoId(statement));

                if (obj instanceof CommonObject) {
                    CommonObject commonObj = (CommonObject) obj;
                    saveCommonObjectProperties(commonObj, commonObj.getProperties(), false);
                }
            }
            obj.setInitialized(true);
            cache.store(obj);

            if (obj instanceof Record && ((Record) obj).getType() == Record.DISCUSSION) {
                statement.close();
                statement = con.prepareStatement("insert into komentar values (NULL,?,?,?,?,?,?)");

                DiscussionRecord diz = (DiscussionRecord) ((Record)obj).getCustom();
                for (Iterator iter = diz.getThreads().iterator(); iter.hasNext();) {
                    RowComment comment = (RowComment) iter.next();
                    comment.setRecord(obj.getId());
                    storeComment(comment, statement);
                }
            }

            if ( log.isDebugEnabled() ) log.debug("Objekt ["+obj+"] uložen");
        } catch ( SQLException e ) {
            if ( e.getErrorCode()==1062 ) {
                throw new DuplicateKeyException("Duplikátní údaj!");
            } else {
                throw new PersistenceException("Nemohu uložit "+obj,e);
            }
        } finally {
            releaseSQLResources(con,statement,null);
        }
    }

    /**
     * Recursively walks through the thread and persists any comment
     * that has row id equal to zero (otherwise skips this comment).
     */
    private void storeComment(RowComment comment, PreparedStatement statement) throws SQLException {
        if (comment.getRowId()==0) {
            statement.setInt(1, comment.getRecord());
            statement.setInt(2, comment.getId());
            if (comment.getParent() != null)
                statement.setInt(3, comment.getParent().intValue());
            else
                statement.setNull(3, Types.INTEGER);

            java.util.Date d = (comment.getCreated() != null) ? comment.getCreated() : new java.util.Date();
            statement.setTimestamp(4, new Timestamp(d.getTime()));
            comment.setCreated(d);

            if (comment.getAuthor() != null)
                statement.setInt(5, comment.getAuthor().intValue());
            else
                statement.setNull(5, Types.INTEGER);
            statement.setObject(6, comment.getDataAsString().getBytes());

            int result = statement.executeUpdate();
            if (result == 0)
                throw new PersistenceException("Nepodařilo se vložit " + comment + " do databáze!");
            int autoId = getAutoId(statement);
            comment.setRowId(autoId);
        }

        for (Iterator iter = comment.getChildren().iterator(); iter.hasNext();) {
            RowComment child = (RowComment) iter.next();
            child.setRecord(comment.getRecord());
            storeComment(child, statement);
        }
    }

    public void update(GenericObject obj) {
        if (obj instanceof GenericDataObject) {
            update((GenericDataObject)obj);
        } else if ( obj instanceof User ) {
            update((User) obj);
        } else if (obj instanceof Relation) {
            update((Relation)obj);
        } else if ( obj instanceof Poll ) {
            update((Poll) obj);
        } else if (obj instanceof Data) {
            update((Data)obj);
        } else if (obj instanceof Link) {
            update((Link)obj);
        }
    }

    public void synchronize(GenericObject obj) {
        if ( obj==null ) return;
        GenericObject found = cache.load(obj);
        if ( found==null ) found = findById(obj);

        obj.synchronizeWith(found);
        obj.setInitialized(true);
    }

    public GenericObject findById(GenericObject obj) {
        if ( obj==null )
            throw new NullPointerException("Nemohu hledat prázdný objekt!");

        GenericObject result = cache.load(obj);
        if ( result!=null && result.isInitialized() ) return result;

        if ( log.isDebugEnabled() ) log.debug("Hledám podle PK "+obj);
        try {
            result = loadObject(obj);
            cache.store(result);
            return result;
        } catch (SQLException e) {
            throw new PersistenceException("Objekt "+obj+" nenalezen!",e);
        }
    }

    /**
     * Finds all relations where obj is children.
     * @param child
     * @return list of initialized relations
     * @throws PersistenceException
     */
    public List findRelations(GenericObject child) throws PersistenceException {
        Connection con; Statement statement = null;ResultSet resultSet = null;
        con = getSQLConnection();
        List found = new ArrayList(5);

        StringBuffer sb = new StringBuffer("select * from relace where ");
        sb.append("typ_potomka='");
        sb.append(PersistenceMapping.getGenericObjectType(child));
        sb.append("' and potomek=");
        sb.append(child.getId());

        try {
            statement = con.createStatement();
            resultSet = statement.executeQuery(sb.toString());
            while ( resultSet.next() ) {
                Relation relation = new Relation(resultSet.getInt(1));
                syncRelationFromRS(relation, resultSet);
                found.add(relation);
            }
            return found;
        } catch (SQLException e) {
            throw new PersistenceException("Databázová chyba!", e);
        } finally {
            releaseSQLResources(con, statement, resultSet);
        }
    }
    /**
     * todo It always tries to parseInt, even on string. It catches NumberFormatException
     * to recover! That's terribly slow and ugly!
     */
    public List findByExample(List objects, String relations) {
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;
        if ( objects.size()==0 ) return new ArrayList();
        if ( relations==null ) relations = makeOrRelation(objects);

        try {
            List result = new ArrayList(), conditions = new ArrayList();
            StringBuffer sb = new StringBuffer("SELECT cislo FROM ");
            GenericObject obj = (GenericObject) objects.get(0);
            Class kind = obj.getClass();

            sb.append(getTable(obj));
            sb.append(" where ");
            StringTokenizer stk = new StringTokenizer(relations," ()",true);
            while ( stk.hasMoreTokens() ) {
                String token = stk.nextToken();
                try {
                    int index = Integer.parseInt(token);
                    if ( obj.getClass()!=kind ) {
                        throw new InvalidDataException("Různé typy objektů!");
                    }
                    sb.append('(');
                    obj = (GenericObject) objects.get(index);
                    appendFindParams(obj,sb, conditions);
                    sb.append(')');
                } catch ( NumberFormatException e ) {
                    sb.append(token);
                }
            }

            con = getSQLConnection();
            statement = con.prepareStatement(sb.toString());
            for ( int i=0; i<conditions.size(); i++ ) {
                Object o = conditions.get(i);
                statement.setObject(i+1,o);
            }

            resultSet = statement.executeQuery();
            while ( resultSet.next() ) {
                try {
                    GenericObject o = (GenericObject) kind.newInstance();
                    o.setId(resultSet.getInt(1));
                    result.add(o);
                } catch (Exception e) {
                    log.error("Nemohu vytvořit instanci "+kind,e);
                }
            }
            return result;
        } catch ( SQLException e ) {
            StringBuffer sb = new StringBuffer(" Examples: ");
            for (Iterator iter = objects.iterator(); iter.hasNext();) {
                sb.append(iter.next().toString());
            }
            throw new PersistenceException("Nemohu provést zadané vyhledávání!"+sb,e);
        } finally {
            releaseSQLResources(con,statement,resultSet);
        }
    }

    public List findByCommand(String command) {
        if ( command==null || command.length()==0 )
            throw new InvalidDataException("Nemohu hledat prázdný objekt!");

        Connection con = null; Statement statement = null; ResultSet resultSet = null;
        List result = new ArrayList(5);
        try {
            con = getSQLConnection();
            if (log.isDebugEnabled()) log.debug("Chystám se hledat podle "+command);

            statement = con.createStatement();
            resultSet = statement.executeQuery(command);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columns = metaData.getColumnCount();

            while ( resultSet.next() ) {
                Object[] objects = new Object[columns];
                for ( int i=0; i<columns; i++ ) {
                    objects[i] = resultSet.getObject(i+1);
                }
                result.add(objects);
            }
        } catch ( SQLException e ) {
            throw new PersistenceException("Chyba při hledání podle "+command+"!",e);
        } finally {
            releaseSQLResources(con,statement,resultSet);
        }
        return result;
    }

    /**
     * Finds all parents of this Relation. First element is top level relation, the second is its child and the
     * last element is this relation.
     */
    public List findParents(Relation relation) {
        List result = new ArrayList(6);
        result.add(relation);

        int upper = relation.getUpper();
        while ( upper!=0 ) {
            relation = (Relation) findById(new Relation(upper));
            result.add(0, relation);
            upper = relation.getUpper();
        }

        return result;
    }

    public Relation[] findByExample(Relation example) {
        Statement statement = null; ResultSet resultSet = null;
        Connection con = getSQLConnection();
        List found = new ArrayList(5);

        StringBuffer sb = new StringBuffer("select * from relace where ");
        boolean addAnd = false;

        if ( example.getUpper()!=0 ) {
            sb.append("predchozi=");
            sb.append(example.getUpper());
            addAnd = true;
        }

        GenericObject child = example.getChild();
        if ( child!=null ) {
            if ( addAnd ) sb.append(" and "); else addAnd = true;
            sb.append("typ_potomka='");
            sb.append(PersistenceMapping.getGenericObjectType(child));
            sb.append("' and potomek=");
            sb.append(child.getId());
        }

        GenericObject parent = example.getParent();
        if ( parent!=null ) {
            if ( addAnd ) sb.append(" and ");
            sb.append("typ_predka='");
            sb.append(PersistenceMapping.getGenericObjectType(parent));
            sb.append("' and predek=");
            sb.append(parent.getId());
        }

        try {
            statement = con.createStatement();
            resultSet = statement.executeQuery(sb.toString());
            while ( resultSet.next() ) {
                Relation relation = new Relation(resultSet.getInt(1));
                syncRelationFromRS(relation, resultSet);
                found.add(relation);
            }

            Relation[] relations = new Relation[found.size()];
            int i = 0;
            for (Iterator iter = found.iterator(); iter.hasNext();) {
                relations[i++] = (Relation) iter.next();
            }

            return relations;
        } catch (SQLException e) {
            throw new PersistenceException("Cannot find relation by example of "+example,e);
        } finally {
            releaseSQLResources(con,statement,resultSet);
        }
    }

    public void remove(GenericObject obj) {
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;
        if ( log.isDebugEnabled() ) log.debug("Chystám se smazat "+obj);

        try {
            con = getSQLConnection();
            ArrayList queue = new ArrayList();
            queue.add(obj);

            do {
                obj = (GenericObject) queue.remove(0);
                statement = con.prepareStatement("delete from "+getTable(obj)+" where cislo=?");
                statement.setInt(1,obj.getId());
                statement.executeUpdate();

                // remove all properties from table vlastnost
                if(obj instanceof CommonObject)
                    deleteCommonObjectProperties((CommonObject) obj, PersistenceMapping.getGenericObjectType(obj));

                // remove comments, they are not referenced via relation table
                if (obj instanceof Record) {
                    Record record = (Record) findById(obj);
                    if (record.getType() == Record.DISCUSSION) {
                        statement = con.prepareStatement("delete from komentar where zaznam=?");
                        statement.setInt(1, obj.getId());
                        statement.executeUpdate();
                    }
                }

                // if relation.getChild() became unreferenced, delete that child
                if ( obj instanceof Relation ) {
                    statement = con.prepareStatement("select predek from relace where typ_potomka=? and potomek=?");
                    GenericObject child = ((Relation)obj).getChild();
                    statement.setString(1,PersistenceMapping.getGenericObjectType(child));
                    statement.setInt(2,child.getId());
                    resultSet = statement.executeQuery();
                    if ( !resultSet.next() )
                        queue.add(child);
                    if ( log.isDebugEnabled() ) log.debug("Smazan objekt "+obj);
                    continue; // relation doesn't have content
                }

                List children = Nursery.getInstance().getChildren(obj);
                for (Iterator iter = children.iterator(); iter.hasNext();) {
                    Relation child = (Relation) iter.next();
                    queue.add(child);
                }

                cache.remove(obj);
                if ( log.isDebugEnabled() ) log.debug("Smazan objekt "+obj);
            } while ( queue.size()!=0 );
        } catch ( SQLException e ) {
            throw new PersistenceException("Nemohu smazat objekt!",e);
        } finally {
            releaseSQLResources(con,statement,resultSet);
        }
    }

    /**
     * Synchronizes list of GenericObjects. The list may hold different objects.
     * It tries to work in batches for optimal access.
     * @param list
     */
    public void synchronizeList(List list) {
        if (list.size()==0)
            return;

        long start = System.currentTimeMillis();
        String type = null;

        Set relations = null;
        Set users = null;
        Set items = null;
        Set records = null;
        Set categories = null;
        Set links = null;
        Set servers = null;
        Set polls = null;

        for (Iterator iter = list.iterator(); iter.hasNext();) {
            GenericObject obj = (GenericObject) iter.next();
            if (obj == null)
                continue;
            if (obj.isInitialized())
                continue;

            GenericObject cached = cache.load(obj);
            if (cached != null && cached.isInitialized()) {
                obj.synchronizeWith(cached);
                continue;
            }

            if (obj instanceof Relation) {
                if (relations==null) relations = new TreeSet(new Sorters2.IdComparator());
                relations.add(obj);
                type = "relation";
            } else if (obj instanceof Item) {
                if (items == null) items = new TreeSet(new Sorters2.IdComparator());
                items.add(obj);
                type = "item";
            } else if (obj instanceof Category) {
                if (categories == null) categories = new TreeSet(new Sorters2.IdComparator());
                categories.add(obj);
                type = "category";
            } else if (obj instanceof Record) {
                if (records == null) records = new TreeSet(new Sorters2.IdComparator());
                records.add(obj);
                type = "record";
            } else if (obj instanceof Link) {
                if (links == null) links = new TreeSet(new Sorters2.IdComparator());
                links.add(obj);
                type = "link";
            } else if (obj instanceof Server) {
                if (servers == null) servers = new TreeSet(new Sorters2.IdComparator());
                servers.add(obj);
                type = "server";
            } else if (obj instanceof User) {
                if (users == null) users = new TreeSet(new Sorters2.IdComparator());
                users.add(obj);
                type = "user";
            } else if (obj instanceof Poll) {
                if (polls == null) polls = new TreeSet(new Sorters2.IdComparator());
                polls.add(obj);
                type = "poll";
            } else if (obj instanceof Data) {
                synchronize(obj);
                type = "data";
            }
        }

        try {
            // loads them and merge fresh objects with objects from list, store them in cache
            if (relations != null)
                syncRelations(relations);
            if (items != null)
                syncDataObjects(items);
            if (categories != null)
                syncDataObjects(categories);
            if (records != null)
                syncDataObjects(records);
            if (links != null)
                syncLinks(links);
            if (servers != null)
                syncServers(servers);
            if (users != null)
                syncUsers(users);
            if (polls != null)
                syncPolls(polls);

            if (log.isDebugEnabled()) {
                long end = System.currentTimeMillis();
                log.debug("syncList for " + list.size() + " " + type + "s took " + (end - start) + " ms");
            }
        } catch (SQLException e) {
            throw new PersistenceException("Nemohu synchronizovat objekty!", e);
        } finally {
            if (relations!=null) relations.clear();
            if (items!=null) items.clear();
            if (categories!=null) categories.clear();
            if (records!=null) records.clear();
            if (links!=null) links.clear();
            if (servers!=null) servers.clear();
            if (users!=null) users.clear();
        }
    }

    public void incrementCounter(GenericObject obj, String  type) {
        Connection con = null; PreparedStatement statement = null;
        try {
            con = getSQLConnection();
            List conditions = new ArrayList();
            StringBuffer sb = new StringBuffer();

            appendIncrementUpdateParams(obj, sb, conditions, type);
            statement = con.prepareStatement(sb.toString());
            for ( int i=0; i<conditions.size(); i++ ) {
                Object o = conditions.get(i);
                statement.setObject(i+1,o);
            }
            int result = statement.executeUpdate();

            if ( result==0 ) {
                sb.setLength(0); conditions.clear();
                appendCounterInsertParams(obj, sb, conditions, type);
                statement = con.prepareStatement(sb.toString());
                for ( int i=0; i<conditions.size(); i++ ) {
                    Object o = conditions.get(i);
                    statement.setObject(i+1,o);
                }
                statement.executeUpdate();
            }
        } catch ( SQLException e ) {
            log.error("Nepodařilo se zvýšit čítač pro "+obj,e);
        } finally {
            releaseSQLResources(con,statement,null);
        }
    }

    public int getCounterValue(GenericObject obj, String type) {
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("select soucet from citac where typ=? and cislo=? and druh=?");
            statement.setString(1, PersistenceMapping.getGenericObjectType(obj));
            statement.setInt(2, obj.getId());
            statement.setString(3, type);

            resultSet = statement.executeQuery();
            if ( !resultSet.next() )
                return 0;
            return resultSet.getInt(1);
        } catch ( SQLException e ) {
            log.error("Nepodařilo se zjistit hodnotu čítače pro "+obj,e);
            return 0;
        } finally {
            releaseSQLResources(con,statement,resultSet);
        }
    }

    public Map getCountersValue(List objects, String  type) {
        Connection con = null; Statement statement = null; ResultSet resultSet = null;
        if (objects == null || objects.size() == 0)
            return Collections.EMPTY_MAP;
        if (type.indexOf(';') != -1 || type.indexOf('\'') != -1)
            throw new InvalidInputException("Type contains illegal characters: '"+type+"'!");

        Map map = new HashMap(objects.size() + 1, 1.0f);
        StringBuffer sql = new StringBuffer("select soucet, typ, cislo from citac where druh='"+type+"' and ");
        appendMatchAllObjectsCondition(sql, objects, "typ", "cislo");
        try {
            con = getSQLConnection();
            statement = con.createStatement();
            resultSet = statement.executeQuery(sql.toString());
            GenericObject obj;
            int value;
            char objType;
            int id;

            while (resultSet.next()) {
                value = resultSet.getInt(1);
                objType = resultSet.getString(2).charAt(0);
                id = resultSet.getInt(3);
                obj = PersistenceMapping.createGenericObject(objType, id);
                map.put(obj, value);
            }

            for (Object object1 : objects) {
                GenericObject object = (GenericObject) object1;
                if (map.get(object) != null)
                    continue;
                map.put(object, 0);
            }

            return map;
        } catch (SQLException e) {
            log.error("Selhalo hledání čítačů pro " + objects, e);
            throw new PersistenceException("Selhalo hledání čítačů!");
        } finally {
            releaseSQLResources(con, statement, resultSet);
        }
    }

    public void removeCounter(GenericObject obj, String type) {
        Connection con = null; PreparedStatement statement = null;
        try {
            con = getSQLConnection();
            List conditions = new ArrayList();
            StringBuffer sb = new StringBuffer();

            appendCounterDeleteParams(obj, sb, conditions, type);
            statement = con.prepareStatement(sb.toString());
            for ( int i=0; i<conditions.size(); i++ ) {
                Object o = conditions.get(i);
                statement.setObject(i+1,o);
            }
            statement.executeUpdate();
        } catch ( SQLException e ) {
            log.error("Nepodařilo se smazat čítač pro "+obj,e);
        } finally {
            releaseSQLResources(con,statement,null);
        }
    }

    /**
     * Loads object by PK from database.
     */
    private GenericObject loadObject(GenericObject obj) throws SQLException {
        if (obj instanceof Relation)
            return loadRelation((Relation)obj);
        if (obj instanceof GenericDataObject)
            return loadDataObject((GenericDataObject)obj);
        if (obj instanceof User)
            return loadUser((User)obj);
        if (obj instanceof Server)
            return loadServer((Server)obj);
        if (obj instanceof Link)
            return loadLink((Link)obj);
        if (obj instanceof Poll)
            return loadPoll((Poll)obj);
        if (obj instanceof Data)
            return loadData((Data)obj);
        return null;
    }

    /**
     * Finds children of given GenericObject. Children are not initialized.
     * If there is no child for the obj, empty list is returned.
     * @throws cz.abclinuxu.exceptions.PersistenceException When something goes wrong.
     * @return list of initialized Relations
     */
    public List<Relation> findChildren(GenericObject obj) {
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;
        List<Relation> children = new ArrayList<Relation>();
        Relation relation;
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("select * from relace where typ_predka=? and predek=?");
            statement.setString(1,PersistenceMapping.getGenericObjectType(obj));
            statement.setInt(2,obj.getId());
            resultSet = statement.executeQuery();

            while ( resultSet.next() ) {
                relation = new Relation(resultSet.getInt(1));
                syncRelationFromRS(relation, resultSet);
                relation.setParent(obj);
                children.add(relation);
            }

            if (children.size() == 0)
                return Collections.emptyList();
            else
                return children;
        } catch (SQLException e) {
            log.error("Selhalo hledání potomků pro "+obj, e);
            throw new PersistenceException("Selhalo hledání potomků pro " + obj);
        } finally {
            releaseSQLResources(con,statement,resultSet);
        }
    }

    /**
     * Finds children for list of GenericObjects. Children are not initialized.
     * If there is no child for the obj, empty list is used.
     * @param objects list of GenericObject
     * @return Map where GenericObject is key and List with initialized Relations is value.
     * @throws cz.abclinuxu.exceptions.PersistenceException When something goes wrong.
     */
    public Map<GenericObject, List<Relation>> findChildren(List objects) {
        Connection con = null; Statement statement = null; ResultSet resultSet = null;
        if (objects==null || objects.size()==0)
            return Collections.emptyMap();

        Map<GenericObject, List<Relation>> map = new HashMap<GenericObject, List<Relation>>(objects.size()+1, 1.0f);
        StringBuffer sql = new StringBuffer("select * from relace where ");
        appendMatchAllObjectsCondition(sql, objects, "typ_predka", "predek");
        Relation relation;
        List<Relation> list;
        try {
            con = getSQLConnection();
            statement = con.createStatement();
            resultSet = statement.executeQuery(sql.toString());

            while ( resultSet.next() ) {
                relation = new Relation(resultSet.getInt(1));
                syncRelationFromRS(relation, resultSet);
                cache.store(relation);

                list = (List<Relation>) map.get(relation.getParent());
                if (list == null) {
                    list = new ArrayList<Relation>();
                    map.put(relation.getParent(), list);
                }
                list.add(relation);
            }

            return map;
        } catch (SQLException e) {
            log.error("Selhalo hledání potomků pro "+objects, e);
            throw new PersistenceException("Selhalo hledání potomků!");
        } finally {
            releaseSQLResources(con,statement,resultSet);
        }
    }

    /**
     * Appends conditions to match all children.
     */
    private void appendMatchAllObjectsCondition(StringBuffer sb, List objects, String typeColumn, String idColumn) {
        Map byTable = new HashMap(objects.size()+1, 1.0f);
        String tableId;
        List list;
        for (Iterator iter = objects.iterator(); iter.hasNext();) {
            GenericObject object = (GenericObject) iter.next();
            tableId = PersistenceMapping.getGenericObjectType(object);
            list = (List) byTable.get(tableId);
            if (list==null) {
                list = new ArrayList();
                byTable.put(tableId, list);
            }
            list.add(new Integer(object.getId()));
        }

        int i = 0;
        for (Iterator iter = byTable.keySet().iterator(); iter.hasNext();) {
            if (i++ > 0)
                sb.append("or ");
            String table = (String) iter.next();
            List ids = (List) byTable.get(table);
            sb.append("(");
            sb.append(typeColumn);
            sb.append("='");
            sb.append(table);
            sb.append("' and ");
            sb.append(idColumn);
            if (ids.size()==1) {
                sb.append('=');
                sb.append(ids.get(0));
            } else {
                sb.append(" in (");
                for (Iterator iterIds = ids.iterator(); iterIds.hasNext();) {
                    sb.append(iterIds.next());
                    if (iterIds.hasNext())
                        sb.append(',');
                }
                sb.append(')');
            }
            sb.append(')');
        }
    }

    /**
     * Appends INSERT prepared statement for this object to <code>sb</code> and parameter
     * to <code>conditions</code> for each asterisk in prepared statement.
     */
    private void appendCreateParams(GenericObject obj, StringBuffer sb, List conditions ) {
        if (obj instanceof GenericDataObject) {
            GenericDataObject gdo = (GenericDataObject) obj;
            sb.append("insert into ").append(getTable(obj)).append(" values(0,?,?,?,?,?,now())");
            if ( !(obj instanceof Category) && gdo.getType()==0 ) {
                log.warn("Type not set! "+obj.toString());
            }
            conditions.add(new Integer(gdo.getType()));
            conditions.add(gdo.getSubType());
            conditions.add(gdo.getDataAsString().getBytes());
            conditions.add(new Integer(gdo.getOwner()));

            java.util.Date d = ( gdo.getCreated()!=null ) ? gdo.getCreated() : new java.util.Date();
            conditions.add(new Timestamp(d.getTime()));

            gdo.setCreated(d);
            gdo.setUpdated(new java.util.Date());

        } else if (obj instanceof Relation) {
            sb.append("insert into relace values(0,?,?,?,?,?,?,?)");
            Relation relation = (Relation)obj;
            conditions.add(new Integer(relation.getUpper()));
            conditions.add(PersistenceMapping.getGenericObjectType(relation.getParent()));
            conditions.add(new Integer(relation.getParent().getId()));
            conditions.add(PersistenceMapping.getGenericObjectType(relation.getChild()));
            conditions.add(new Integer(relation.getChild().getId()));
            conditions.add(relation.getUrl());
            String tmp = relation.getDataAsString();
            conditions.add((tmp!=null)? tmp.getBytes():null);

        } else if (obj instanceof Data) {
            sb.append("insert into objekt values(0,?,?,?)");
            conditions.add(((Data)obj).getFormat());
            conditions.add(((Data)obj).getData());
            conditions.add(new Integer(((Data)obj).getOwner()));

        } else if (obj instanceof User) {
            sb.append("insert into uzivatel values(0,?,?,?,?,?,?)");
            User user = (User)obj;
            conditions.add(user.getLogin());
            conditions.add(user.getName());
            conditions.add(user.getEmail());
            conditions.add(user.getPassword());
            conditions.add(user.getNick());
            conditions.add(user.getDataAsString().getBytes());

        } else if (obj instanceof Link) {
            sb.append("insert into odkaz values(0,?,?,?,?,?,?)");
            Link link = (Link)obj;
            conditions.add(new Integer(link.getServer()));
            conditions.add(link.getText());
            conditions.add(link.getUrl());
            conditions.add(Boolean.valueOf(link.isFixed()));
            conditions.add(new Integer(link.getOwner()));
            if (link.getUpdated() == null)
                link.setUpdated(new java.util.Date());
            conditions.add(new Timestamp(link.getUpdated().getTime()));
        }
    }

    /**
     * append SQL statements to <code>sb</code> and objects to <code>conditions</code>
     * as PreparedStatement requires.
     */
    private void appendFindParams(GenericObject obj, StringBuffer sb, List conditions ) {
        boolean addAnd = false;

        if (obj instanceof GenericDataObject) {
            GenericDataObject gdo = (GenericDataObject) obj;

            if ( gdo.getId()>0 ) {
                addAnd = true;
                sb.append("cislo=?");
                conditions.add(new Integer(gdo.getId()));
            }

            if ( gdo.getOwner()!=0 ) {
                if ( addAnd ) sb.append(" and "); else addAnd = true;
                sb.append("pridal=?");
                conditions.add(new Integer(gdo.getOwner()));
            }

            String search = gdo.getSearchString();
            if ( (search!=null && search.length()>0 )) {
                if ( addAnd ) sb.append(" and "); else addAnd = true;
                sb.append("data like ?");
                conditions.add(search);
            }

            if ( gdo.getType()!=0 ) {
                if ( addAnd ) sb.append(" and ");
                sb.append("typ=?");
                conditions.add(new Integer(gdo.getType()));
            }

        } else if ( obj instanceof User ) {
            User user = (User) obj;

            if ( user.getId()>0 ) {
                addAnd = true;
                sb.append("cislo=?");
                conditions.add(new Integer(user.getId()));
            }

            String tmp = user.getLogin();
            if ( tmp!=null && tmp.length()>0 ) {
                if ( addAnd ) sb.append(" and "); else addAnd = true;
                sb.append("login like ?");
                conditions.add(tmp);
            }

            tmp = user.getName();
            if ( tmp!=null && tmp.length()>0 ) {
                if ( addAnd ) sb.append(" and "); else addAnd = true;
                sb.append("jmeno like ?");
                conditions.add(tmp);
            }

            tmp = user.getEmail();
            if ( tmp!=null && tmp.length()>0 ) {
                if ( addAnd ) sb.append(" and ");
                sb.append("email like ?");
                conditions.add(tmp);
            }

            tmp = user.getNick();
            if ( tmp!=null && tmp.length()>0 ) {
                if ( addAnd ) sb.append(" and ");
                sb.append("prezdivka like ?");
                conditions.add(tmp);
            }

            tmp = user.getDataAsString();
            if ((tmp != null && tmp.length() > 0)) {
                if (addAnd) sb.append(" and "); else addAnd = true;
                sb.append("data like ?");
                conditions.add(tmp);
            }
        }
    }

    /**
     * Appends UPDATE prepared statement to increment counter of this object to <code>sb</code> and parameter
     * to <code>conditions</code> for each asterisk in perpared statement.
     */
    private void appendIncrementUpdateParams(GenericObject obj, StringBuffer sb, List conditions, String type) {
        sb.append("update citac set soucet=soucet+1 where typ=? and cislo=? and druh=?");
        conditions.add(PersistenceMapping.getGenericObjectType(obj));
        conditions.add(new Integer(obj.getId()));
        conditions.add(type);
    }

    /**
     * Appends INSERT prepared statement to increment counter of this object to <code>sb</code> and parameter
     * to <code>conditions</code> for each asterisk in perpared statement.
     */
    private void appendCounterInsertParams(GenericObject obj, StringBuffer sb, List conditions, String type) {
        sb.append("insert into citac values(?,?,1,?)");
        conditions.add(PersistenceMapping.getGenericObjectType(obj));
        conditions.add(new Integer(obj.getId()));
        conditions.add(type);
    }

    /**
     * Appends DELETE prepared statement to remove counter for this object to <code>sb</code> and parameter
     * to <code>conditions</code> for each asterisk in perpared statement.
     */
    private void appendCounterDeleteParams(GenericObject obj, StringBuffer sb, List conditions, String type ) {
        sb.append("delete from citac where typ=? and cislo=? and type=?");
        conditions.add(PersistenceMapping.getGenericObjectType(obj));
        conditions.add(new Integer(obj.getId()));
        conditions.add(type);
    }

    /**
     * stores poll to database and updates <code>id</code>
     */
    protected void storePoll(Poll poll) throws SQLException {
        Connection con = null; PreparedStatement statement = null;

        try {
            con = getSQLConnection();
            statement = con.prepareStatement("insert into anketa2 values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

            statement.setInt(1, poll.getId());
            statement.setBoolean(2, poll.isMultiChoice());
            statement.setBoolean(3, poll.isClosed());
            statement.setInt(4, poll.getOwner());
            long when;
            if (poll.getCreated()!=null) {
                when = poll.getCreated().getTime();
            } else {
                when = System.currentTimeMillis();
                poll.setCreated(new java.util.Date(when));
            }
            statement.setTimestamp(5, new Timestamp(when));
            statement.setInt(6, poll.getTotalVoters());

            PollChoice[] choices = poll.getChoices();
            if ( choices==null || choices.length<1 )
                throw new InvalidDataException("Anketa musí mít nejméně jednu volbu!");

            Document document = DocumentHelper.createDocument();
            Element root = document.addElement("data");
            root.addElement("name").setText(poll.getText());

            int i = 0;
            for (; i < choices.length; i++) {
                PollChoice choice = choices[i];
                statement.setInt(7+i, choice.getCount());
                root.addElement("choice").setText(choice.getText());
            }
            for (; i<10; i++)
                statement.setInt(7+i, 0);

            String formatted = XMLHandler.getDocumentAsString(document);
            statement.setString(17, formatted);

            int result = statement.executeUpdate();
            if ( result==0 ) {
                throw new PersistenceException("Nepodařilo se vložit anketu do databáze!");
            }

            if (poll.getId()==0)
                poll.setId(getAutoId(statement));

            for (i = 0; i < choices.length; i++) {
                PollChoice choice = choices[i];
                choice.setPoll(poll.getId());
                choice.setId(i);
            }
        } finally {
            releaseSQLResources(con,statement,null);
        }
    }

    /**
     * @return user from mysql db
     */
    protected GenericObject loadUser(User obj) throws SQLException {
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("select * from uzivatel where cislo=?");
            statement.setInt(1,obj.getId());

            resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new NotFoundException("Uživatel "+obj.getId()+" nebyl nalezen!");
            }

            User user = new User(obj.getId());
            syncUserFromRS(user, resultSet);
            loadCommonObjectProperties(user);
            return user;
        } finally {
            releaseSQLResources(con,statement,resultSet);
        }
    }

    /**
     * Synchronizes specified users from database.
     * @param users
     */
    protected void syncUsers(Collection users) throws SQLException {
        Connection con = null; PreparedStatement statement = null; ResultSet rs = null;
        User user;
        try {
            Map<Integer, User>  objects = new HashMap<Integer, User>();
            con = getSQLConnection();
            statement = con.prepareStatement("select * from uzivatel where cislo in "+Misc.getInCondition(users.size()) + " order by cislo");
            int i = 1;
            for (Iterator iter = users.iterator(); iter.hasNext();) {
                user = (User) iter.next();
                statement.setInt(i++, user.getId());
                objects.put(user.getId(), user);
            }
            rs = statement.executeQuery();

            for (Iterator iter = users.iterator(); iter.hasNext();) {
                user = (User) iter.next();
                if (! rs.next() || rs.getInt(1) != user.getId())
                    throw new NotFoundException("Uživatel " + user.getId() + " nebyl nalezen!");

                syncUserFromRS(user, rs);
                if (! PropertiesConfig.isSupported(user))
                    objects.remove(user);
            }

            loadCommonObjectsProperties(objects, PersistenceMapping.TREE_USER);
            for (Iterator iter = objects.values().iterator(); iter.hasNext();) {
                user = (User) iter.next();
                cache.store(user);
            }
        } finally {
            releaseSQLResources(con,statement,rs);
        }
    }

    /**
     * Synchronizes user from result set.
     * @throws SQLException
     */
    private void syncUserFromRS(User user, ResultSet resultSet) throws SQLException {
        user.setLogin(resultSet.getString(2));
        user.setName(resultSet.getString(3));
        user.setEmail(resultSet.getString(4));
        user.setPassword(resultSet.getString(5));
        user.setNick(resultSet.getString(6));
        user.setData(insertEncoding(resultSet.getString(7)));
        user.setInitialized(true);
    }

    /**
     * @return item descendant from mysql db
     */
    protected GenericDataObject loadDataObject(GenericDataObject obj) throws SQLException {
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;

        try {
            con = getSQLConnection();
            statement = con.prepareStatement("select * from "+getTable(obj)+" where cislo=?");
            statement.setInt(1,obj.getId());

            resultSet = statement.executeQuery();
            if ( !resultSet.next() )
                throw new NotFoundException("Položka "+obj.getId()+" nebyla nalezena!");

            GenericDataObject data;
            if (obj instanceof Category)
                data = new Category(obj.getId());
            else if (obj instanceof Item)
                data = new Item(obj.getId());
            else
                data = new Record(obj.getId());
            syncGenericDataObjectFromRS(data, resultSet);

            if (data instanceof Record && ((Record) data).getType() == Record.DISCUSSION)
                loadComments((Record)data);

            loadCommonObjectProperties(data);

            return data;
        } finally {
            releaseSQLResources(con,statement,resultSet);
        }
    }

    /**
     * Synchronizes specified GenericDataObjects from database.
     * @param objs at least one generic object, all must be same class
     */
    protected void syncDataObjects(Collection objs) throws SQLException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        Map objects = new HashMap();
        GenericDataObject obj;
        GenericDataObject representant = (GenericDataObject) objs.iterator().next();
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("select * from " + getTable(representant) + " where cislo in " + Misc.getInCondition(objs.size()));
            int i = 1;
            for (Iterator iter = objs.iterator(); iter.hasNext();) {
                obj = (GenericDataObject) iter.next();
                statement.setInt(i++, obj.getId());
                objects.put(obj.getId(), obj);
            }

            rs = statement.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                obj = (GenericDataObject) objects.get(id);
                if (obj == null) {
                    // this cannot happen
                    log.warn("Datova položka nebyla nalezena: " + obj);
                    continue;
                }

                syncGenericDataObjectFromRS(obj, rs);
                if (obj instanceof Record && ((Record) obj).getType() == Record.DISCUSSION)
                    loadComments((Record) obj);

                if ( ! PropertiesConfig.isSupported(obj))
                    objects.remove(id);
            }
        } finally {
            releaseSQLResources(con, statement, rs);
        }

        loadCommonObjectsProperties(objects, PersistenceMapping.getGenericObjectType(representant));
        for (Iterator iter = objects.values().iterator(); iter.hasNext();) {
            obj = (GenericDataObject) iter.next();
            cache.store(obj);
        }
    }

    /**
     * Synchronizes GenericDataObject from result set.
     * @throws SQLException
     */
    private void syncGenericDataObjectFromRS(GenericDataObject item, ResultSet resultSet) throws SQLException {
        item.setType(resultSet.getInt(2));
        item.setSubType(resultSet.getString(3));

        String tmp = resultSet.getString(4);
        tmp = insertEncoding(tmp);
        item.setData(tmp);

        item.setOwner(resultSet.getInt(5));
        item.setCreated(new java.util.Date(resultSet.getTimestamp(6).getTime()));
        item.setUpdated(new java.util.Date(resultSet.getTimestamp(7).getTime()));
        item.setInitialized(true);
    }

    /**
     * Loads comments for given Record and sets them as custom object.
     * @param record record that has type Discussion
     * @throws SQLException
     */
    private void loadComments(Record record) throws SQLException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            con = getSQLConnection();
            statement = con.prepareStatement("select * from komentar where zaznam=? order by cislo asc");
            statement.setInt(1, record.getId());
            resultSet = statement.executeQuery();

            DiscussionRecord diz = new DiscussionRecord();
            Map map = new HashMap();
            RowComment current, upper;
            List alone = new ArrayList();
            int max = 0, count = 0;
            while (resultSet.next()) {
                current = new RowComment();
                syncCommentFromRS(current, resultSet);
                map.put(new Integer(current.getId()), current);
                count++;
                if (current.getId() > max)
                    max = current.getId();

                if (current.getParent() != null) {
                    upper = (RowComment) map.get(current.getParent());
                    if (upper != null)
                        upper.addChild(current);
                    else
                        alone.add(current);
                } else
                    diz.addThread(current);
            }

            if (alone.size() > 0) {
                for (Iterator iter = alone.iterator(); iter.hasNext();) {
                    current = (RowComment) iter.next();
                    upper = (RowComment) map.get(current.getParent());
                    if (upper != null)
                        upper.addChild(current);
                    else {
                        diz.addThread(current);
                        log.warn("Nenalezen předek pro komentář "+current.getRowId()+"!");
                    }
                }
            }

            map.clear();
            alone.clear();
            diz.setTotalComments(count);
            diz.setMaxCommentId(max);
            record.setCustom(diz);
        } finally {
            releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Initializes RowComment with values from result set's current row.
     */
    private void syncCommentFromRS(RowComment current, ResultSet resultSet) throws SQLException {
        current.setRowId(resultSet.getInt(1));
        current.setRecord(resultSet.getInt(2));
        current.setId(resultSet.getInt(3));
        current.setParent(resultSet.getInt(4));
        if (resultSet.wasNull())
            current.setParent(null);
        current.setAuthor(resultSet.getInt(6));
        if (resultSet.wasNull())
            current.setAuthor(null);
        current.setCreated(new java.util.Date(resultSet.getTimestamp(5).getTime()));
        String tmp = resultSet.getString(7);
        tmp = insertEncoding(tmp);
        current.setData(tmp);
    }

    /**
     * @return user from mysql db
     */
    protected GenericObject loadRelation(Relation obj) throws SQLException {
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("select * from relace where cislo=?");
            statement.setInt(1,obj.getId());

            resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new NotFoundException("Relace "+obj.getId()+" nebyla nalezena!");
            }

            Relation relation = new Relation(obj.getId());
            syncRelationFromRS(relation, resultSet);
            return relation;
        } finally {
            releaseSQLResources(con,statement,resultSet);
        }
    }

    /**
     * Synchronizes specified relations from database.
     * @param relations
     */
    protected void syncRelations(Collection relations) throws SQLException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("select * from relace where cislo in " + Misc.getInCondition(relations.size())+" order by cislo");
            int i = 1;
            for (Iterator iter = relations.iterator(); iter.hasNext();) {
                Relation relation = (Relation) iter.next();
                statement.setInt(i++, relation.getId());
            }
            rs = statement.executeQuery();

            for (Iterator iter = relations.iterator(); iter.hasNext();) {
                Relation relation = (Relation) iter.next();
                boolean hasNext = rs.next();
                if (!hasNext || rs.getInt(1) != relation.getId())
                    throw new NotFoundException("Relace " + relation.getId() + " nebyla nalezen!");
                syncRelationFromRS(relation, rs);
                cache.store(relation);
            }
        } finally {
            releaseSQLResources(con, statement, rs);
        }
    }

    /**
     * Synchronizes relation from result set.
     * @throws SQLException
     */
    private void syncRelationFromRS(Relation relation, ResultSet resultSet) throws SQLException {
        relation.setUpper(resultSet.getInt(2));

        char type = resultSet.getString(3).charAt(0);
        int id = resultSet.getInt(4);
        GenericObject parent = PersistenceMapping.createGenericObject(type,id);
        relation.setParent(parent);

        type = resultSet.getString(5).charAt(0);
        id = resultSet.getInt(6);
        GenericObject child = PersistenceMapping.createGenericObject(type,id);
        relation.setChild(child);

        relation.setUrl(resultSet.getString(7));

        String tmp = resultSet.getString(8);
        if ( tmp!=null ) {
            tmp = insertEncoding(tmp);
            relation.setData(tmp);
        }
        relation.setInitialized(true);
    }

    /**
     * todo - probably remove this Object
     * @return data from mysql db
     */
    protected GenericObject loadData(Data obj) throws SQLException {
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;

        try {
            con = getSQLConnection();
            statement = con.prepareStatement("select * from objekt where cislo=?");
            statement.setInt(1,obj.getId());

            resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new NotFoundException("Datovy objekt "+obj.getId()+" nebyl nalezen!");
            }

            Data data = new Data(obj.getId());
            data.setFormat(resultSet.getString(2));
            data.setData(resultSet.getBytes(3));
            data.setOwner(resultSet.getInt(4));
            data.setInitialized(true);

            return data;
        } finally {
            releaseSQLResources(con,statement,resultSet);
        }
    }

    /**
     * @return link from mysql db
     */
    protected GenericObject loadLink(Link obj) throws SQLException {
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;

        try {
            con = getSQLConnection();
            statement = con.prepareStatement("select * from odkaz where cislo=?");
            statement.setInt(1,obj.getId());

            resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new NotFoundException("Odkaz "+obj.getId()+" nebyl nalezen!");
            }

            Link link = new Link(obj.getId());
            syncLinkFromRS(link, resultSet);
            return link;
        } finally {
            releaseSQLResources(con,statement,resultSet);
        }
    }

    /**
     * Synchronizes specified Links from database.
     * @param links
     */
    protected void syncLinks(Collection links) throws SQLException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("select * from odkaz where cislo in " + Misc.getInCondition(links.size()) + " order by cislo");
            int i = 1;
            for (Iterator iter = links.iterator(); iter.hasNext();) {
                Link link = (Link) iter.next();
                statement.setInt(i++, link.getId());
            }
            rs = statement.executeQuery();

            for (Iterator iter = links.iterator(); iter.hasNext();) {
                Link link = (Link) iter.next();
                if (!rs.next() || rs.getInt(1) != link.getId())
                    throw new NotFoundException("Odkaz " + link.getId() + " nebyl nalezen!");
                syncLinkFromRS(link, rs);
                cache.store(link);
            }
        } finally {
            releaseSQLResources(con, statement, rs);
        }
    }

    /**
     * Synchronizes Link from result set.
     * @throws SQLException
     */
    private void syncLinkFromRS(Link link, ResultSet resultSet) throws SQLException {
        link.setServer(resultSet.getInt(2));
        link.setText(resultSet.getString(3));
        link.setUrl(resultSet.getString(4));
        link.setFixed(resultSet.getBoolean(5));
        link.setOwner(resultSet.getInt(6));
        link.setUpdated(new java.util.Date(resultSet.getTimestamp(7).getTime()));
        link.setInitialized(true);
    }

    /**
     * @return poll from mysql db
     */
    protected GenericObject loadPoll(Poll obj) throws SQLException {
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;

        try {
            con = getSQLConnection();
            statement = con.prepareStatement("select * from anketa2 where cislo=?");
            statement.setInt(1,obj.getId());

            resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new NotFoundException("Anketa "+obj.getId()+" nebyla nalezena!");
            }

            Poll poll = new Poll(obj.getId());
            syncPollFromRS(poll, resultSet);
            poll.setInitialized(true);
            return poll;
        } finally {
            releaseSQLResources(con,statement,resultSet);
        }
    }

    /**
     * Synchronizes specified Polls from database.
     * @param polls
     */
    protected void syncPolls(Collection polls) throws SQLException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("select * from anketa2 where cislo in " + Misc.getInCondition(polls.size()) + " order by cislo");
            int i = 1;
            for (Iterator iter = polls.iterator(); iter.hasNext();) {
                Poll poll = (Poll) iter.next();
                statement.setInt(i++, poll.getId());
            }
            rs = statement.executeQuery();

            for (Iterator iter = polls.iterator(); iter.hasNext();) {
                Poll poll = (Poll) iter.next();
                if (!rs.next() || rs.getInt(1) != poll.getId())
                    throw new NotFoundException("Anketa " + poll.getId() + " nebyla nalezena!");
                syncPollFromRS(poll, rs);
                cache.store(poll);
            }
        } finally {
            releaseSQLResources(con, statement, rs);
        }
    }

    protected void syncPollFromRS(Poll poll, ResultSet resultSet) throws SQLException {
        String tmp = resultSet.getString(17);
        try {
            tmp = insertEncoding(tmp);
            Document data = DocumentHelper.parseText(tmp);
            Element root = data.getRootElement();
            Element element = root.element("name");
            poll.setText(element.getText());

            int i = 0;
            List choices = new ArrayList();
            List elements = root.elements("choice");
            for (Iterator iter = elements.iterator(); iter.hasNext();) {
                element = (Element) iter.next();
                PollChoice choice = new PollChoice(element.getText());
                choice.setCount(resultSet.getInt(7 + i));
                choice.setPoll(poll.getId());
                choice.setId(i);
                choices.add(choice);
                i++;
            }
            poll.setChoices(choices);
        } catch (DocumentException e) {
            log.error(e.getMessage(), e);
            throw new SQLException("Chyba při čtení ankety!");
        }

        poll.setMultiChoice(resultSet.getBoolean(2));
        poll.setClosed(resultSet.getBoolean(3));
        poll.setOwner(resultSet.getInt(4));
        poll.setCreated(new java.util.Date(resultSet.getTimestamp(5).getTime()));
        poll.setTotalVoters(resultSet.getInt(6));
    }

    /**
     * Increment counter for one or more PollChoices of the same Poll.
     * @param choices list of PollChoices. They must have valid poll and id properties.
     */
    public void incrementPollChoicesCounter(List choices) {
        if (choices == null || choices.size() == 0)
            return;
        PollChoice firstChoice = (PollChoice) choices.get(0);

        Connection con = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            con = getSQLConnection();
            statement = con.createStatement();

            StringBuffer sql = new StringBuffer("update anketa2 set hlasu=hlasu+1, ");
            for (Iterator iter = choices.iterator(); iter.hasNext();) {
                PollChoice choice = (PollChoice) iter.next();
                String column = "volba" + (choice.getId() + 1);
                sql.append(column).append("=").append(column).append("+1");
                if (iter.hasNext())
                    sql.append(',');
            }
            sql.append(" where cislo=");
            sql.append(firstChoice.getPoll());
            statement.executeUpdate(sql.toString());

            resultSet = statement.executeQuery("select * from anketa2 where cislo=" + firstChoice.getPoll());
            resultSet.next();
            Poll poll = (Poll) findById(new Poll(firstChoice.getPoll()));
            poll.setTotalVoters(resultSet.getInt(6));
            for (int i = 0; i < poll.getChoices().length; i++) {
                PollChoice choice = poll.getChoices()[i];
                choice.setCount(resultSet.getInt(7 + choice.getId()));
            }

            cache.store(poll);
        } catch (SQLException e) {
            log.error("Nepodařilo se zvýšit čítač pro " + firstChoice, e);
        } finally {
            releaseSQLResources(con, statement, null);
        }
    }

    /**
     * @return Server from mysql db
     */
    protected GenericObject loadServer(Server obj) throws SQLException {
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;

        try {
            con = getSQLConnection();
            statement = con.prepareStatement("select * from server where cislo=?");
            statement.setInt(1,obj.getId());

            resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new NotFoundException("Server "+obj.getId()+" nebyl nalezen!");
            }

            Server server = new Server(obj.getId());
            syncServerFromRS(server, resultSet);
            return server;
        } finally {
            releaseSQLResources(con,statement,resultSet);
        }
    }

    /**
     * Synchronizes specified Servers from database.
     * @param servers
     */
    protected void syncServers(Collection servers) throws SQLException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("select * from server where cislo in " + Misc.getInCondition(servers.size()) + " order by cislo");
            int i = 1;
            for (Iterator iter = servers.iterator(); iter.hasNext();) {
                Server server = (Server) iter.next();
                statement.setInt(i++, server.getId());
            }
            rs = statement.executeQuery();

            for (Iterator iter = servers.iterator(); iter.hasNext();) {
                Server server = (Server) iter.next();
                if (!rs.next() || rs.getInt(1) != server.getId())
                    throw new NotFoundException("Server " + server.getId() + " nebyl nalezen!");
                syncServerFromRS(server, rs);
                cache.store(server);
            }
        } finally {
            releaseSQLResources(con, statement, rs);
        }
    }

    /**
     * Synchronizes server from result set.
     * @throws SQLException
     */
    private void syncServerFromRS(Server server, ResultSet resultSet) throws SQLException {
        server.setName(resultSet.getString(2));
        server.setUrl(resultSet.getString(3));
        server.setContact(resultSet.getString(4));
        server.setInitialized(true);
    }

    /**
     * updates record in database
     */
    public void update(GenericDataObject obj) {
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;
        if ( obj==null )
            throw new NullPointerException("Nemohu  uložit prázdný objekt!");

        try {
            con = getSQLConnection();
            statement = null;

            statement = con.prepareStatement("update "+getTable(obj)+" set typ=?,podtyp=?,data=?,pridal=?,vytvoreno=? where cislo=?");
            statement.setInt(1,obj.getType());
            statement.setString(2,obj.getSubType());
            statement.setBytes(3,obj.getDataAsString().getBytes());
            statement.setInt(4,obj.getOwner());
            statement.setTimestamp(5,new Timestamp(obj.getCreated().getTime()));
            statement.setInt(6,obj.getId());

            int result = statement.executeUpdate();
            if ( result!=1 )
                throw new PersistenceException("Nepodařilo se uložit změny v "+obj.toString()+" do databáze!");

            if (obj instanceof Record && obj.getType() == Record.DISCUSSION)
                updateComments((DiscussionRecord) obj.getCustom(), obj.getId(), con);

            saveCommonObjectProperties(obj, obj.getProperties(), true);

            obj.setUpdated(new java.util.Date());
            cache.store(obj);
        } catch (SQLException e) {
            throw new PersistenceException("Nemohu uložit změny do databáze!",e);
        } finally {
            releaseSQLResources(con,statement,resultSet);
        }
    }

    /**
     * Deletes removed comments, adds new comments, updates modified comments.
     * @param discussion
     */
    private void updateComments(DiscussionRecord discussion, int recordId, Connection con) throws SQLException {
        PreparedStatement statement1 = null, statement2 = null, statement3 = null;
        try {
            List deleted = discussion.getDeletedComments();
            if (deleted.size() > 0) {
                String sql = "delete from komentar where cislo in " + Misc.getInCondition(deleted.size());
                statement1 = con.prepareStatement(sql);
                int i = 1;
                for (Iterator iter = deleted.iterator(); iter.hasNext();) {
                    Integer id = (Integer) iter.next();
                    statement1.setInt(i++, id.intValue());
                }
                statement1.executeUpdate();
                deleted.clear();
            }

            List comments = new ArrayList();
            comments.addAll(discussion.getThreads());
            RowComment comment;
            while (comments.size() > 0) {
                comment = (RowComment) comments.remove(0);
                comment.setRecord(recordId);
                comments.addAll(comment.getChildren());

                if (comment.getRowId() == 0) {
                    if (statement2 == null)
                        statement2 = con.prepareStatement("insert into komentar values (NULL,?,?,?,?,?,?)");
                    storeComment(comment, statement2);
                } else if (comment.is_dirty()) {
                    if (statement3 == null)
                        statement3 = con.prepareStatement("update komentar set zaznam=?,nadrazeny=?,autor=?,vytvoreno=?,data=? where cislo=?");

                    statement3.setInt(1, comment.getRecord());
                    if (comment.getParent() != null)
                        statement3.setInt(2, comment.getParent().intValue());
                    else
                        statement3.setNull(2, Types.INTEGER);
                    if (comment.getAuthor() != null)
                        statement3.setInt(3, comment.getAuthor().intValue());
                    else
                        statement3.setNull(3, Types.INTEGER);
                    statement3.setTimestamp(4, new Timestamp(comment.getCreated().getTime()));
                    statement3.setObject(5, comment.getDataAsString().getBytes());
                    statement3.setInt(6, comment.getRowId());

                    statement3.executeUpdate();
                }
            }
        } finally {
            releaseSQLResources(null, new Statement[] {statement1, statement2, statement3}, null);
        }
    }

    /**
     * updates data in database
     */
    public void update(Relation relation) {
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;
        if ( relation==null )
            throw new NullPointerException("Nemohu  uložit prázdný objekt!");

        try {
            con = getSQLConnection();
            statement = con.prepareStatement("update relace set typ_predka=?,predek=?,typ_potomka=?,potomek=?,data=?,predchozi=?,url=? where cislo=?");

            statement.setString(1,PersistenceMapping.getGenericObjectType(relation.getParent()));
            statement.setInt(2,relation.getParent().getId());
            statement.setString(3,PersistenceMapping.getGenericObjectType(relation.getChild()));
            statement.setInt(4,relation.getChild().getId());

            String tmp = relation.getDataAsString();
            if ( tmp==null || tmp.length()==0 ) {
                statement.setBytes(5,null);
            } else {
                statement.setBytes(5,tmp.getBytes());
            }
            statement.setInt(6,relation.getUpper());
            statement.setString(7,relation.getUrl());
            statement.setInt(8,relation.getId());

            int result = statement.executeUpdate();
            if ( result!=1 ) {
                throw new PersistenceException("Nepodařilo se uložit změny v "+relation.toString()+" do databáze!");
            }

            cache.store(relation);
        } catch (SQLException e) {
            throw new PersistenceException("Nemohu uložit změny do databáze!",e);
        } finally {
            releaseSQLResources(con,statement,resultSet);
        }
    }

    /**
     * updates data in database
     */
    public void update(Data data) {
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;
        if ( data==null )
            throw new NullPointerException("Nemohu  uložit prázdný objekt!");

        try {
            con = getSQLConnection();
            statement = con.prepareStatement("update objekt set data=?,format=? where cislo=?");
            statement.setBytes(1,data.getData());
            statement.setString(2,data.getFormat());
            statement.setInt(3,data.getId());

            int result = statement.executeUpdate();
            if ( result!=1 ) {
                throw new PersistenceException("Nepodarilo se ulozit zmeny v "+data.toString()+" do databáze!");
            }

            cache.store(data);
        } catch (SQLException e) {
            throw new PersistenceException("Nemohu uložit změny do databáze!", e);
        } finally {
            releaseSQLResources(con,statement,resultSet);
        }
    }

    /**
     * updates link in database
     */
    public void update(Link link) {
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;
        if ( link==null )
            throw new NullPointerException("Nemohu  uložit prázdný objekt!");

        try {
            con = getSQLConnection();
            statement = con.prepareStatement("update odkaz set server=?,nazev=?,url=?,trvaly=?,pridal=?,kdy=? where cislo=?");
            statement.setInt(1,link.getServer());
            statement.setString(2,link.getText());
            statement.setString(3,link.getUrl());
            statement.setBoolean(4,link.isFixed());
            statement.setInt(5,link.getOwner());
            if (link.getUpdated() != null)
                statement.setTimestamp(6, new Timestamp(link.getUpdated().getTime()));
            else
                statement.setNull(6, Types.TIMESTAMP);
            statement.setInt(7,link.getId());

            int result = statement.executeUpdate();
            if ( result!=1 ) {
                throw new PersistenceException("Nepodarilo se uložit změny v "+link.toString()+" do databáze!");
            }

            link.setUpdated(new java.util.Date());
            cache.store(link);
        } catch (SQLException e) {
            throw new PersistenceException("Nemohu uložit změny v "+link.toString()+" do databáze!",e);
        } finally {
            releaseSQLResources(con,statement,resultSet);
        }
    }

    /**
     * updates poll in database
     */
    public void update(Poll poll) {
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;
        if ( poll==null )
            throw new NullPointerException("Nemohu  uložit prázdný objekt!");

        try {
            con = getSQLConnection();
            statement = con.prepareStatement("update anketa2 set data=?,vice=?,uzavrena=? where cislo=?");

            Document document = DocumentHelper.createDocument();
            Element root = document.addElement("data");
            root.addElement("name").setText(poll.getText());
            PollChoice[] choices = poll.getChoices();
            for (int i=0; i < choices.length; i++) {
                PollChoice choice = choices[i];
                root.addElement("choice").setText(choice.getText());
            }
            String formatted = XMLHandler.getDocumentAsString(document);

            statement.setString(1, formatted);
            statement.setBoolean(2,poll.isMultiChoice());
            statement.setBoolean(3,poll.isClosed());
            statement.setInt(4,poll.getId());

            int result = statement.executeUpdate();
            if ( result!=1 )
                throw new PersistenceException("Nepodarilo se uložit změny v "+poll.toString()+" do databáze!");

            cache.store(poll);
        } catch (SQLException e) {
            throw new PersistenceException("Nemohu uložit změny v "+poll.toString()+" do databáze!",e);
        } finally {
            releaseSQLResources(con,statement,resultSet);
        }
    }

    /**
     * updates user in database
     */
    public void update(User user) {
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;
        if ( user==null )
            throw new NullPointerException("Nemohu  uložit prázdný objekt!");

        try {
            con = getSQLConnection();
            statement = con.prepareStatement("update uzivatel set login=?,jmeno=?,email=?,heslo=?,prezdivka=?,data=? where cislo=?");
            statement.setString(1,user.getLogin());
            statement.setString(2,user.getName());
            statement.setString(3,user.getEmail());
            statement.setString(4,user.getPassword());
            statement.setString(5,user.getNick());
            statement.setBytes(6,user.getDataAsString().getBytes());
            statement.setInt(7,user.getId());

            int result = statement.executeUpdate();
            if ( result!=1 ) {
                throw new PersistenceException("Nepodarilo se uložit změny v "+user.toString()+" do databáze!");
            }

            saveCommonObjectProperties(user, user.getProperties(), true);

            cache.store(user);
        } catch (SQLException e) {
            if ( e.getErrorCode()==1062 ) {
                throw new DuplicateKeyException("Přihlašovací jméno (login) nebo přezdívka jsou již používány!");
            } else {
                throw new PersistenceException("Nemohu uložit změny do databáze!",e);
            }
        } finally {
            releaseSQLResources(con,statement,resultSet);
        }
    }

    /**
     * Loads properties of <code>obj</code> from database.
     */
    protected void loadCommonObjectProperties(CommonObject obj) throws SQLException {
        if ( ! PropertiesConfig.isSupported(obj))
            return;

        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("select * from vlastnost where typ_predka=? and predek=?");
            statement.setString(1, PersistenceMapping.getGenericObjectType(obj));
            statement.setInt(2, obj.getId());

            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String property = resultSet.getString(3);
                String value = resultSet.getString(4);
                obj.addProperty(property, value);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Loads properties for specified objects from database.
     * @param objects map, where key is id (integer) and value is CommonObject with this id
     * @param objectType
     */
    protected void loadCommonObjectsProperties(Map objects, String objectType) throws SQLException {
        if (objects.size() == 0)
            return;

        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("select * from vlastnost where typ_predka=? and predek in " + Misc.getInCondition(objects.size()));
            statement.setString(1, objectType);
            int i = 2, id;
            for (Iterator iter = objects.keySet().iterator(); iter.hasNext();) {
                id = (Integer) iter.next();
                statement.setInt(i++, id);
            }

            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String property = resultSet.getString(3);
                String value = resultSet.getString(4);
                id = resultSet.getInt(2);
                CommonObject obj = (CommonObject) objects.get(id);
                obj.addProperty(property, value);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Deletes all properties of <code>obj</code> from database.
     */
    protected void deleteCommonObjectProperties(CommonObject obj, String objectType) throws SQLException {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("delete from vlastnost where typ_predka=? and predek=?");
            statement.setString(1, objectType);
            statement.setInt(2, obj.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            releaseSQLResources(con, statement, null);
        }
    }

    /**
     * Updates all properties of <code>obj</code> in database
     * @param resetProperties if true, all object properties will be deleted from database first (to avoid duplicates)
     */
    public void saveCommonObjectProperties(CommonObject obj, Map properties, boolean resetProperties) throws SQLException {
        Connection con = null;
        PreparedStatement statement = null;
        log.debug("Saving properties for: " + obj.getId());
        try {
            String objectType = PersistenceMapping.getGenericObjectType(obj);
            if (resetProperties)
                deleteCommonObjectProperties(obj, objectType);

            con = getSQLConnection();
            statement = con.prepareStatement("insert into vlastnost values (?, ?, ?, ?)");
            for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                Set values = (Set) entry.getValue();
                if (values == null)
                    continue;

                for (Iterator iterSet = values.iterator(); iterSet.hasNext();) {
                    String value = (String) iterSet.next();
                    statement.setString(1, objectType);
                    statement.setInt(2, obj.getId());
                    statement.setString(3, key);
                    statement.setString(4, value);
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            log.error(e);
            throw e;
        } finally {
            releaseSQLResources(con, statement, null);
        }
    }

    /**
     * @return String, where each object in list <code>objects</code>
     * is represented by its index (starting at 0, maximum size is 10)
     * and there is OR relation between all objects.
     */
    private String makeOrRelation(List objects) {
        StringBuffer sb = new StringBuffer();
        if (objects.size()==1) return "0";

        int size = objects.size() - 1;
        if (size>9) size = 9;

        for (int i = 0; i<size; i++) {
            sb.append(i);
            sb.append(" OR ");
        }

        sb.append(size);
        return sb.toString();
    }

    /**
     * @return id of last inserted row
     */
    private int getAutoId(Statement statement) throws AbcException {
        if ( ! (statement instanceof com.mysql.jdbc.Statement) )
            try {
                statement = ProxoolFacade.getDelegateStatement(statement);
            } catch (ProxoolException e) {
                throw new AbcException("Cannot obtain delegated statement from "+statement, e);
            }
        if ( statement instanceof com.mysql.jdbc.PreparedStatement ) {
            com.mysql.jdbc.PreparedStatement mm = (com.mysql.jdbc.PreparedStatement) statement;
            return (int)mm.getLastInsertID();
        }
        return -1;
    }

    /**
     * @return connection to database from connection pool.
     */
    public Connection getSQLConnection() {
        try {
            return DriverManager.getConnection(dbUrl);
        } catch (SQLException e) {
            throw new PersistenceException("Nemohu se spojit s databazi!",e);
        }
    }

    /**
     * Closes database connection and logs any errors
     */
    public void releaseSQLResources(Connection con, Statement statement, ResultSet rs) {
        try {
            if ( rs!=null )
                rs.close();
        } catch (Exception e) {
            log.warn("Problems while closing ResultSet!",e);
        }
        try {
            if ( statement!=null )
                statement.close();
        } catch (Exception e) {
            log.warn("Problems while closing statement!",e);
        }
        try {
            if ( con!=null )
                con.close();
        } catch (Exception e) {
            log.warn("Problems while closing connection to database!",e);
        }
    }

    /**
     * Closes database connection and logs any errors
     */
    public void releaseSQLResources(Connection con, Statement[] statements, ResultSet[] rs) {
        if (rs != null)
            for (int i = 0; i < rs.length; i++) {
                ResultSet resultSet = rs[i];
                try {
                    if (resultSet != null)
                        resultSet.close();
                } catch (Exception e) {
                    log.warn("Problems while closing ResultSet!", e);
                }
            }
        if (statements != null)
            for (int i = 0; i < statements.length; i++) {
                Statement statement = statements[i];
                try {
                    if (statement != null)
                        statement.close();
                } catch (Exception e) {
                    log.warn("Problems while closing statement!", e);
                }
            }
        try {
            if (con != null)
                con.close();
        } catch (Exception e) {
            log.warn("Problems while closing connection to database!", e);
        }
    }

    /**
     * @return SQL table name for this GenericObject
     */
    public String getTable(GenericObject obj) {
        if (obj instanceof Relation) {
            return "relace";
        } else if (obj instanceof Record) {
            return "zaznam";
        } else if (obj instanceof Item) {
            return "polozka";
        } else if (obj instanceof Category) {
            return "kategorie";
        } else if (obj instanceof Link) {
            return "odkaz";
        } else if (obj instanceof User) {
            return "uzivatel";
        } else if (obj instanceof Poll) {
            return "anketa2";
        } else if (obj instanceof Server) {
            return "server";
        } else if (obj instanceof Data) {
            return "objekt";
        }
        throw new InvalidDataException("Nepodporovany typ tridy!");
    }

    /**
     * Safely adds encoding to string with xml
     */
    public static String insertEncoding(String xml) {
        if ( xml==null || xml.startsWith("<?xml") ) return xml;
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"+xml;
    }
}
