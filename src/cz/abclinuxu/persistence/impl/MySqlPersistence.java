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
import java.io.File;

import cz.abclinuxu.data.*;
import cz.abclinuxu.data.view.RowComment;
import cz.abclinuxu.data.view.DiscussionRecord;
import cz.abclinuxu.exceptions.*;
import cz.abclinuxu.AbcException;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.servlets.utils.url.CustomURLCache;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.cache.TransparentCache;
import cz.abclinuxu.persistence.cache.TagCache;
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
    TagCache tagCache = TagCache.getInstance();

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
                if (result == 0)
                    throw new PersistenceException("Nepodařilo se vložit "+obj+" do databáze!");

                if (obj.getId() == 0)
                    obj.setId(getAutoId(statement));

                if (obj instanceof CommonObject) {
                    CommonObject commonObj = (CommonObject) obj;
                    saveCommonObjectProperties(commonObj, commonObj.getProperties(), false);
                }

                if (obj instanceof GenericDataObject) {
                    GenericDataObject gdo = (GenericDataObject) obj;
                    PreparedStatement commonStatement = con.prepareStatement("INSERT INTO spolecne (typ,cislo,jmeno,vytvoreno,zmeneno,pridal) VALUES (?,?,?,?,?,?)");
                    commonStatement.setString(1, PersistenceMapping.getGenericObjectType(obj));
                    commonStatement.setInt(2, obj.getId());
                    commonStatement.setString(3, gdo.getTitle());
                    java.util.Date d = (gdo.getCreated() != null) ? gdo.getCreated() : new java.util.Date();
                    gdo.setCreated(d);
                    gdo.setUpdated(new java.util.Date());
                    commonStatement.setTimestamp(4, new Timestamp(gdo.getCreated().getTime()));
                    commonStatement.setTimestamp(5, new Timestamp(gdo.getUpdated().getTime()));
                    commonStatement.setInt(6, gdo.getOwner());
                    commonStatement.executeUpdate();
                    commonStatement.close();
                }
            }
            obj.setInitialized(true);
            cache.store(obj);

            if (obj instanceof Record && ((Record) obj).getType() == Record.DISCUSSION) {
                statement.close();
                statement = con.prepareStatement("INSERT INTO komentar (cislo,zaznam,id,nadrazeny,vytvoreno,autor,data) VALUES (NULL,?,?,?,?,?,?)");

                DiscussionRecord diz = (DiscussionRecord) ((Record)obj).getCustom();
                if (diz != null) {
                    for (Iterator iter = diz.getThreads().iterator(); iter.hasNext();) {
                        RowComment comment = (RowComment) iter.next();
                        comment.setRecord(obj.getId());
                        storeComment(comment, statement);
                    }
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
                statement.setInt(3, comment.getParent());
            else
                statement.setNull(3, Types.INTEGER);

            java.util.Date d = (comment.getCreated() != null) ? comment.getCreated() : new java.util.Date();
            statement.setTimestamp(4, new Timestamp(d.getTime()));
            comment.setCreated(d);

            if (comment.getAuthor() != null)
                statement.setInt(5, comment.getAuthor());
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

    public List findRelationsFor(GenericObject child) throws PersistenceException {
        Connection con; Statement statement = null;ResultSet resultSet = null;
        con = getSQLConnection();
        List found = new ArrayList(5);

        StringBuffer sb = new StringBuffer("SELECT cislo,predchozi,typ_predka,predek,typ_potomka,potomek,url,data FROM relace WHERE ");
        sb.append("typ_potomka='");
        sb.append(PersistenceMapping.getGenericObjectType(child));
        sb.append("' AND potomek=");
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

    public List<User> findUsersLike(User sample) {
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;
        StringBuffer sb = null;
        try {
            List<User> result = new ArrayList<User>();
            List conditions = new ArrayList();
            sb = new StringBuffer("SELECT cislo FROM uzivatel WHERE ");
            appendFindParams(sample, sb, conditions);

            con = getSQLConnection();
            statement = con.prepareStatement(sb.toString());
            for (int i = 0; i < conditions.size(); i++) {
                Object o = conditions.get(i);
                statement.setObject(i + 1, o);
            }

            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                User o = new User(resultSet.getInt(1));
                result.add(o);
            }
            return result;
        } catch ( SQLException e ) {
            throw new PersistenceException("Nemohu provést zadané vyhledávání!" + statement, e);
        } finally {
            releaseSQLResources(con,statement,resultSet);
        }
    }

    /**
     * Append SQL statements to <code>sb</code> and parameter values to <code>conditions</code>
     * as PreparedStatement requires.
     */
    private void appendFindParams(User user, StringBuffer sb, List conditions) {
        boolean addAnd = false;
        String tmp = user.getLogin();
        if (tmp != null && tmp.length() > 0) {
            if (addAnd) sb.append(" AND "); else addAnd = true;
            sb.append("login LIKE ?");
            conditions.add(tmp);
        }

        tmp = user.getName();
        if (tmp != null && tmp.length() > 0) {
            if (addAnd) sb.append(" AND "); else addAnd = true;
            sb.append("jmeno LIKE ?");
            conditions.add(tmp);
        }

        tmp = user.getEmail();
        if (tmp != null && tmp.length() > 0) {
            if (addAnd) sb.append(" AND ");
            sb.append("email LIKE ?");
            conditions.add(tmp);
        }

        tmp = user.getNick();
        if (tmp != null && tmp.length() > 0) {
            if (addAnd) sb.append(" AND ");
            sb.append("prezdivka LIKE ?");
            conditions.add(tmp);
        }

        tmp = user.getDataAsString();
        if ((tmp != null && tmp.length() > 0)) {
            if (addAnd) sb.append(" AND ");
            sb.append("data LIKE ?");
            conditions.add(tmp);
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

    public List<Relation> findParents(Relation relation) {
        List<Relation> result = new ArrayList<Relation>(6);
        result.add(relation);

        int upper = relation.getUpper();
        while ( upper!=0 ) {
            relation = (Relation) findById(new Relation(upper));
            result.add(0, relation);
            upper = relation.getUpper();
        }

        return result;
    }

    public Relation[] findRelationsLike(Relation example) {
        Statement statement = null; ResultSet resultSet = null;
        Connection con = getSQLConnection();
        List found = new ArrayList(5);

        StringBuffer sb = new StringBuffer("SELECT cislo,predchozi,typ_predka,predek,typ_potomka,potomek,url,data FROM relace WHERE ");
        boolean addAnd = false;

        if ( example.getUpper()!=0 ) {
            sb.append("predchozi=");
            sb.append(example.getUpper());
            addAnd = true;
        }

        GenericObject child = example.getChild();
        if ( child!=null ) {
            if ( addAnd ) sb.append(" AND "); else addAnd = true;
            sb.append("typ_potomka='");
            sb.append(PersistenceMapping.getGenericObjectType(child));
            sb.append("' AND potomek=");
            sb.append(child.getId());
        }

        GenericObject parent = example.getParent();
        if ( parent!=null ) {
            if ( addAnd ) sb.append(" AND ");
            sb.append("typ_predka='");
            sb.append(PersistenceMapping.getGenericObjectType(parent));
            sb.append("' AND predek=");
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
                statement = con.prepareStatement("DELETE FROM "+getTable(obj)+" WHERE cislo=?");
                statement.setInt(1, obj.getId());
                statement.executeUpdate();

                if (obj instanceof GenericDataObject) {
                    String objectType = PersistenceMapping.getGenericObjectType(obj);

                    // remove referenced record from table spolecne
                    statement.close();
                    statement = con.prepareStatement("DELETE FROM spolecne WHERE typ=? AND cislo=?");
                    statement.setString(1, objectType);
                    statement.setInt(2, obj.getId());
                    statement.executeUpdate();

                    // unassign any tags for deleted object
                    GenericDataObject gdo = (GenericDataObject) obj;
                    List<String> tags = getAssignedTags(gdo);
                    for (String tag : tags) {
                        tagCache.unassignTag(gdo, tag);
                    }

                    statement.close();
                    statement = con.prepareStatement("DELETE FROM stitkovani WHERE typ=? AND cislo=?");
                    statement.setString(1, objectType);
                    statement.setInt(2, obj.getId());
                    statement.executeUpdate();
                    statement.close();
                    tagCache.removeAssignment(gdo);

                    // remove comments, they are not referenced via relation table
                    if (obj instanceof Record) {
                        Record record = (Record) findById(obj);
                        if (record.getType() == Record.DISCUSSION) {
                            statement.close();
                            statement = con.prepareStatement("DELETE FROM komentar WHERE zaznam=?");
                            statement.setInt(1, obj.getId());
                            statement.executeUpdate();
                        }
                    }
                }

                // remove all properties from table vlastnost
                if (obj instanceof CommonObject)
                    deleteCommonObjectProperties((CommonObject) obj, PersistenceMapping.getGenericObjectType(obj));

                // remove referenced files from disk
                if (obj instanceof Data) {
                    Data data = (Data) findById(obj);
                    deleteAttachment(data);
                }

                // if relation.getChild() became unreferenced, delete that child
                if ( obj instanceof Relation ) {
                    Relation rel = (Relation) obj;

                    statement = con.prepareStatement("SELECT predek FROM relace WHERE typ_potomka=? AND potomek=?");
                    GenericObject child = rel.getChild();
                    statement.setString(1,PersistenceMapping.getGenericObjectType(child));
                    statement.setInt(2,child.getId());
                    resultSet = statement.executeQuery();
                    if ( !resultSet.next() )
                        queue.add(child);

                    if (rel.getUrl() != null)
                        CustomURLCache.getInstance().remove(rel.getUrl());
                } else { // relation doesn't have content
                    List children = Nursery.getInstance().getChildren(obj);
                    for (Iterator iter = children.iterator(); iter.hasNext();) {
                        Relation child = (Relation) iter.next();
                        queue.add(child);
                    }
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

    public void synchronizeList(List list) {
        synchronizeList(list, false);
    }

    public void synchronizeList(List list, boolean ignoreMissing) {
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
        Set data = null;

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
                if (data == null) data = new TreeSet(new Sorters2.IdComparator());
                data.add(obj);
                type = "data";
            }
        }

        try {
            // loads them and merge fresh objects with objects FROM list, store them in cache
            if (relations != null)
                syncRelations(relations, ignoreMissing);
            if (items != null)
                syncDataObjects(items, ignoreMissing);
            if (categories != null)
                syncDataObjects(categories, ignoreMissing);
            if (records != null)
                syncDataObjects(records, ignoreMissing);
            if (data != null)
                syncDataObjects(data, ignoreMissing);
            if (links != null)
                syncLinks(links, ignoreMissing);
            if (servers != null)
                syncServers(servers, ignoreMissing);
            if (users != null)
                syncUsers(users, ignoreMissing);
            if (polls != null)
                syncPolls(polls, ignoreMissing);

            if (log.isDebugEnabled()) {
                long end = System.currentTimeMillis();
                log.debug("syncList for " + list.size() + " " + type + "s took " + (end - start) + " ms");
            }
        } catch (SQLException e) {
            throw new PersistenceException("Nemohu synchronizovat objekty!", e);
        } finally {
            if (relations != null) relations.clear();
            if (items != null) items.clear();
            if (categories != null) categories.clear();
            if (records != null) records.clear();
            if (data != null) data.clear();
            if (links != null) links.clear();
            if (servers != null) servers.clear();
            if (users != null) users.clear();
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
            statement = con.prepareStatement("SELECT soucet FROM citac WHERE typ=? AND cislo=? AND druh=?");
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
        StringBuffer sql = new StringBuffer("SELECT soucet, typ, cislo FROM citac WHERE druh='"+type+"' AND ");
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
        return null;
    }

    public List<Relation> findChildren(GenericObject obj) {
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;
        List<Relation> children = new ArrayList<Relation>();
        Relation relation;
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("SELECT cislo,predchozi,typ_predka,predek,typ_potomka,potomek,url,data FROM relace WHERE typ_predka=? AND predek=?");
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

    public Map<GenericObject, List<Relation>> findChildren(List objects) {
        Connection con = null; Statement statement = null; ResultSet resultSet = null;
        if (objects==null || objects.size()==0)
            return Collections.emptyMap();

        Map<GenericObject, List<Relation>> map = new HashMap<GenericObject, List<Relation>>(objects.size()+1, 1.0f);
        StringBuffer sql = new StringBuffer("SELECT cislo,predchozi,typ_predka,predek,typ_potomka,potomek,url,data FROM relace WHERE ");
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
            list.add(object.getId());
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
            sb.append("' AND ");
            sb.append(idColumn);
            if (ids.size()==1) {
                sb.append('=');
                sb.append(ids.get(0));
            } else {
                sb.append(" IN (");
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
            sb.append("INSERT INTO ").append(getTable(obj)).append(" (cislo,typ,podtyp,data) VALUES (?,?,?,?)");
            if ( !(obj instanceof Category || obj instanceof Data) && gdo.getType()==0 ) {
                log.warn("Type not set! "+obj.toString());
            }
            conditions.add(gdo.getId());
            conditions.add(gdo.getType());
            conditions.add(gdo.getSubType());
            conditions.add(gdo.getDataAsString().getBytes());

        } else if (obj instanceof Relation) {
            sb.append("INSERT INTO relace (cislo,predchozi,typ_predka,predek,typ_potomka,potomek,url,data) VALUES (?,?,?,?,?,?,?,?)");
            Relation relation = (Relation)obj;
            conditions.add(relation.getId());
            conditions.add(relation.getUpper());
            conditions.add(PersistenceMapping.getGenericObjectType(relation.getParent()));
            conditions.add(relation.getParent().getId());
            conditions.add(PersistenceMapping.getGenericObjectType(relation.getChild()));
            conditions.add(relation.getChild().getId());
            conditions.add(relation.getUrl());
            String tmp = relation.getDataAsString();
            conditions.add((tmp!=null)? tmp.getBytes():null);

        } else if (obj instanceof User) {
            sb.append("INSERT INTO uzivatel (cislo,login,jmeno,email,openid,prezdivka,data) VALUES (?,?,?,?,?,?,?)");
            User user = (User)obj;
            conditions.add(user.getId());
            conditions.add(user.getLogin());
            conditions.add(user.getName());
            conditions.add(user.getEmail());
            conditions.add(user.getOpenId());
            conditions.add(user.getNick());
            conditions.add(user.getDataAsString().getBytes());

        } else if (obj instanceof Link) {
            sb.append("INSERT INTO odkaz (cislo,server,nazev,url,trvaly,pridal,kdy) VALUES (?,?,?,?,?,?,?)");
            Link link = (Link)obj;
            conditions.add(link.getId());
            conditions.add(link.getServer());
            conditions.add(link.getText());
            conditions.add(link.getUrl());
            conditions.add(Boolean.valueOf(link.isFixed()));
            conditions.add(link.getOwner());
            if (link.getUpdated() == null)
                link.setUpdated(new java.util.Date());
            conditions.add(new Timestamp(link.getUpdated().getTime()));

        } else if (obj instanceof Server) {
            sb.append("INSERT INTO server (cislo,jmeno,url,kontakt) VALUES (?,?,?,?)");
            Server server = (Server) obj;
            conditions.add(server.getId());
            conditions.add(server.getName());
            conditions.add(server.getUrl());
            conditions.add(server.getContact());
        }
    }

    /**
     * Appends UPDATE prepared statement to increment counter of this object to <code>sb</code> and parameter
     * to <code>conditions</code> for each asterisk in perpared statement.
     */
    private void appendIncrementUpdateParams(GenericObject obj, StringBuffer sb, List conditions, String type) {
        sb.append("UPDATE citac SET soucet=soucet+1 WHERE typ=? AND cislo=? AND druh=?");
        conditions.add(PersistenceMapping.getGenericObjectType(obj));
        conditions.add(obj.getId());
        conditions.add(type);
    }

    /**
     * Appends INSERT prepared statement to increment counter of this object to <code>sb</code> and parameter
     * to <code>conditions</code> for each asterisk in perpared statement.
     */
    private void appendCounterInsertParams(GenericObject obj, StringBuffer sb, List conditions, String type) {
        sb.append("INSERT INTO citac (typ,cislo,soucet,druh) VALUES (?,?,1,?)");
        conditions.add(PersistenceMapping.getGenericObjectType(obj));
        conditions.add(obj.getId());
        conditions.add(type);
    }

    /**
     * Appends DELETE prepared statement to remove counter for this object to <code>sb</code> and parameter
     * to <code>conditions</code> for each asterisk in perpared statement.
     */
    private void appendCounterDeleteParams(GenericObject obj, StringBuffer sb, List conditions, String type ) {
        sb.append("DELETE FROM citac WHERE typ=? AND cislo=? AND druh=?");
        conditions.add(PersistenceMapping.getGenericObjectType(obj));
        conditions.add(obj.getId());
        conditions.add(type);
    }

    /**
     * stores poll to database and updates <code>id</code>
     */
    protected void storePoll(Poll poll) throws SQLException {
        Connection con = null; PreparedStatement statement = null;

        try {
            con = getSQLConnection();
            statement = con.prepareStatement("INSERT INTO anketa2 (cislo, vice, uzavrena, pridal, vytvoreno, hlasu, " +
                    "volba1, volba2, volba3, volba4,volba5,volba6,volba7,volba8,volba9,volba10,volba11,volba12,volba13,volba14,volba15,data) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
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
            for (; i<15; i++)
                statement.setInt(7+i, 0);

            String formatted = XMLHandler.getDocumentAsString(document);
            statement.setString(22, formatted);

            int result = statement.executeUpdate();
            if ( result==0 ) {
                throw new PersistenceException("Nepodařilo se vložit anketu do databáze!");
            }

            if (poll.getId() == 0)
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
            statement = con.prepareStatement("SELECT cislo,login,jmeno,email,openid,prezdivka,data FROM uzivatel WHERE cislo=?");
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
    protected void syncUsers(Collection users, boolean ignoreMissing) throws SQLException {
        Connection con = null; PreparedStatement statement = null; ResultSet rs = null;
        User user;
        try {
            Map<Integer, User>  objects = new HashMap<Integer, User>();
            con = getSQLConnection();
            statement = con.prepareStatement("SELECT cislo,login,jmeno,email,openid,prezdivka,data FROM uzivatel WHERE cislo IN "
                    + Misc.getInCondition(users.size()) + " ORDER BY cislo");
            int i = 1;
            for (Iterator iter = users.iterator(); iter.hasNext();) {
                user = (User) iter.next();
                statement.setInt(i++, user.getId());
                objects.put(user.getId(), user);
            }
            rs = statement.executeQuery();

            for (Iterator iter = users.iterator(); iter.hasNext();) {
                user = (User) iter.next();
                if (! rs.next() || rs.getInt(1) != user.getId()) {
                    if (!ignoreMissing)
                        throw new NotFoundException("Uživatel " + user.getId() + " nebyl nalezen!");
                    else
                        continue;
                }

                syncUserFromRS(user, rs);
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
        user.setOpenId(resultSet.getString(5));
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
            statement = con.prepareStatement("SELECT P.cislo,P.typ,P.podtyp,P.data,S.pridal,S.vytvoreno,S.zmeneno,S.jmeno FROM "
                    + getTable(obj) + " P, spolecne S WHERE S.cislo=P.cislo AND S.typ=? AND P.cislo=?");
            statement.setString(1, PersistenceMapping.getGenericObjectType(obj));
            statement.setInt(2, obj.getId());

            resultSet = statement.executeQuery();
            if ( !resultSet.next() )
                throw new NotFoundException("Položka "+obj.getId()+" nebyla nalezena!");

            GenericDataObject data;
            if (obj instanceof Category)
                data = new Category(obj.getId());
            else if (obj instanceof Item)
                data = new Item(obj.getId());
            else if (obj instanceof Record)
                data = new Record(obj.getId());
            else
                data = new Data(obj.getId());
            syncGenericDataObjectFromRS(data, resultSet);

            if (data instanceof Record && data.getType() == Record.DISCUSSION)
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
    protected void syncDataObjects(Collection objs, boolean ignoreMissing) throws SQLException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        Map objects = new HashMap();
        GenericDataObject obj;
        GenericDataObject representant = (GenericDataObject) objs.iterator().next();
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("SELECT P.cislo,P.typ,P.podtyp,P.data,S.pridal,S.vytvoreno,S.zmeneno,S.jmeno FROM "
                    + getTable(representant) + "  P, spolecne S WHERE S.cislo=P.cislo AND S.typ=? AND P.cislo IN " + Misc.getInCondition(objs.size()));
            int i = 1;
            for (Iterator iter = objs.iterator(); iter.hasNext();) {
                obj = (GenericDataObject) iter.next();
                if (i == 1)
                    statement.setString(i++, PersistenceMapping.getGenericObjectType(obj));
                statement.setInt(i++, obj.getId());
                objects.put(obj.getId(), obj);
            }

            rs = statement.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                obj = (GenericDataObject) objects.get(id);
                if (obj == null) {
                    if (!ignoreMissing) {
                        // this cannot happen
                        log.warn("Datova položka nebyla nalezena: " + id);
                    }
                    continue;
                }

                syncGenericDataObjectFromRS(obj, rs);
                if (obj instanceof Record && obj.getType() == Record.DISCUSSION)
                    loadComments((Record) obj); // todo read it in single query

                if ( ! PropertiesConfig.isSupported(obj))
                    objects.remove(id);
            }
        } finally {
            releaseSQLResources(con, statement, rs);
        }

        loadCommonObjectsProperties(objects, PersistenceMapping.getGenericObjectType(representant));
        for (Iterator iter = objs.iterator(); iter.hasNext();) {
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
        item.setTitle(resultSet.getString(8));
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
            statement = con.prepareStatement("SELECT cislo,zaznam,id,nadrazeny,vytvoreno,autor,data FROM komentar WHERE zaznam=? ORDER BY cislo asc");
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
                map.put(current.getId(), current);
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
            statement = con.prepareStatement("SELECT cislo,predchozi,typ_predka,predek,typ_potomka,potomek,url,data FROM relace WHERE cislo=?");
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
    protected void syncRelations(Collection relations, boolean ignoreMissing) throws SQLException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("SELECT cislo,predchozi,typ_predka,predek,typ_potomka,potomek,url,data FROM relace WHERE cislo IN "
                    + Misc.getInCondition(relations.size())+" ORDER BY cislo");
            int i = 1;
            for (Iterator iter = relations.iterator(); iter.hasNext();) {
                Relation relation = (Relation) iter.next();
                statement.setInt(i++, relation.getId());
            }
            rs = statement.executeQuery();

            for (Iterator iter = relations.iterator(); iter.hasNext();) {
                Relation relation = (Relation) iter.next();
                boolean hasNext = rs.next();
                if (!hasNext || rs.getInt(1) != relation.getId()) {
                    if (!ignoreMissing)
                        throw new NotFoundException("Relace " + relation.getId() + " nebyla nalezen!");
                    else
                        continue;
                }
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
     * @return link from mysql db
     */
    protected GenericObject loadLink(Link obj) throws SQLException {
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;

        try {
            con = getSQLConnection();
            statement = con.prepareStatement("SELECT cislo,server,nazev,url,trvaly,pridal,kdy FROM odkaz WHERE cislo=?");
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
    protected void syncLinks(Collection links, boolean ignoreMissing) throws SQLException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("SELECT cislo,server,nazev,url,trvaly,pridal,kdy FROM odkaz WHERE cislo IN "
                    + Misc.getInCondition(links.size()) + " ORDER BY cislo");
            int i = 1;
            for (Iterator iter = links.iterator(); iter.hasNext();) {
                Link link = (Link) iter.next();
                statement.setInt(i++, link.getId());
            }
            rs = statement.executeQuery();

            for (Iterator iter = links.iterator(); iter.hasNext();) {
                Link link = (Link) iter.next();
                if (!rs.next() || rs.getInt(1) != link.getId()) {
                    if (!ignoreMissing)
                        throw new NotFoundException("Odkaz " + link.getId() + " nebyl nalezen!");
                    else
                        continue;
                }
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
            statement = con.prepareStatement("SELECT cislo,vice,uzavrena,pridal,vytvoreno,hlasu,volba1,volba2,volba3," +
                    "volba4,volba5,volba6,volba7,volba8,volba9,volba10,volba11,volba12,volba13,volba14,volba15,data FROM anketa2 WHERE cislo=?");
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
    protected void syncPolls(Collection polls, boolean ignoreMissing) throws SQLException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("SELECT cislo,vice,uzavrena,pridal,vytvoreno,hlasu,volba1,volba2,volba3," +
                    "volba4,volba5,volba6,volba7,volba8,volba9,volba10,volba11,volba12,volba13,volba14,volba15,data FROM anketa2 WHERE cislo IN "
                    + Misc.getInCondition(polls.size()) + " ORDER BY cislo");
            int i = 1;
            for (Iterator iter = polls.iterator(); iter.hasNext();) {
                Poll poll = (Poll) iter.next();
                statement.setInt(i++, poll.getId());
            }
            rs = statement.executeQuery();

            for (Iterator iter = polls.iterator(); iter.hasNext();) {
                Poll poll = (Poll) iter.next();
                if (!rs.next() || rs.getInt(1) != poll.getId()) {
                    if (!ignoreMissing)
                        throw new NotFoundException("Anketa " + poll.getId() + " nebyla nalezena!");
                    else
                        continue;
                }
                syncPollFromRS(poll, rs);
                cache.store(poll);
            }
        } finally {
            releaseSQLResources(con, statement, rs);
        }
    }

    protected void syncPollFromRS(Poll poll, ResultSet resultSet) throws SQLException {
        String tmp = resultSet.getString(22);
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

            StringBuffer sql = new StringBuffer("UPDATE anketa2 SET hlasu=hlasu+1, ");
            for (Iterator iter = choices.iterator(); iter.hasNext();) {
                PollChoice choice = (PollChoice) iter.next();
                String column = "volba" + (choice.getId() + 1);
                sql.append(column).append("=").append(column).append("+1");
                if (iter.hasNext())
                    sql.append(',');
            }
            sql.append(" WHERE cislo=");
            sql.append(firstChoice.getPoll());
            statement.executeUpdate(sql.toString());

            resultSet = statement.executeQuery("SELECT hlasu,volba1,volba2,volba3,volba4,volba5,volba6,volba7,volba8,volba9," +
                    "volba10,volba11,volba12,volba13,volba14,volba15 FROM anketa2 WHERE cislo=" + firstChoice.getPoll());
            resultSet.next();
            Poll poll = (Poll) findById(new Poll(firstChoice.getPoll()));
            poll.setTotalVoters(resultSet.getInt(1));
            for (int i = 0; i < poll.getChoices().length; i++) {
                PollChoice choice = poll.getChoices()[i];
                choice.setCount(resultSet.getInt(2 + choice.getId()));
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
            statement = con.prepareStatement("SELECT cislo,jmeno,url,kontakt FROM server WHERE cislo=?");
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
    protected void syncServers(Collection servers, boolean ignoreMissing) throws SQLException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("SELECT cislo,jmeno,url,kontakt FROM server WHERE cislo IN " +
                    Misc.getInCondition(servers.size()) + " ORDER BY cislo");
            int i = 1;
            for (Iterator iter = servers.iterator(); iter.hasNext();) {
                Server server = (Server) iter.next();
                statement.setInt(i++, server.getId());
            }
            rs = statement.executeQuery();

            for (Iterator iter = servers.iterator(); iter.hasNext();) {
                Server server = (Server) iter.next();
                if (!rs.next() || rs.getInt(1) != server.getId()) {
                    if (!ignoreMissing)
                        throw new NotFoundException("Server " + server.getId() + " nebyl nalezen!");
                    else
                        continue;
                }
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

            statement = con.prepareStatement("UPDATE "+getTable(obj)+" SET typ=?,podtyp=?,data=? WHERE cislo=?");
            statement.setInt(1,obj.getType());
            statement.setString(2,obj.getSubType());
            statement.setBytes(3,obj.getDataAsString().getBytes());
            statement.setInt(4,obj.getId());

            int result = statement.executeUpdate();
            if ( result!=1 )
                throw new PersistenceException("Nepodařilo se uložit změny v "+obj.toString()+" do databáze!");

            statement.close();
            statement = con.prepareStatement("UPDATE spolecne SET jmeno=?,pridal=?,vytvoreno=? WHERE typ=? and cislo=?");
            statement.setString(1, obj.getTitle());
            statement.setInt(2, obj.getOwner());
            statement.setTimestamp(3, new Timestamp(obj.getCreated().getTime()));
            statement.setString(4, PersistenceMapping.getGenericObjectType(obj));
            statement.setInt(5, obj.getId());
            result = statement.executeUpdate();

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
                String sql = "DELETE FROM komentar WHERE cislo IN " + Misc.getInCondition(deleted.size());
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
                        statement2 = con.prepareStatement("INSERT INTO komentar (cislo,zaznam,id,nadrazeny,vytvoreno,autor,data) VALUES (NULL,?,?,?,?,?,?)");
                    storeComment(comment, statement2);
                } else if (comment.is_dirty()) {
                    if (statement3 == null)
                        statement3 = con.prepareStatement("UPDATE komentar SET zaznam=?,nadrazeny=?,autor=?,vytvoreno=?,data=? WHERE cislo=?");

                    statement3.setInt(1, comment.getRecord());
                    if (comment.getParent() != null)
                        statement3.setInt(2, comment.getParent());
                    else
                        statement3.setNull(2, Types.INTEGER);
                    if (comment.getAuthor() != null)
                        statement3.setInt(3, comment.getAuthor());
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
            statement = con.prepareStatement("UPDATE relace SET typ_predka=?,predek=?,typ_potomka=?,potomek=?,data=?,predchozi=?,url=? WHERE cislo=?");

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
     * updates link in database
     */
    public void update(Link link) {
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;
        if ( link==null )
            throw new NullPointerException("Nemohu  uložit prázdný objekt!");

        try {
            con = getSQLConnection();
            statement = con.prepareStatement("UPDATE odkaz SET server=?,nazev=?,url=?,trvaly=?,pridal=?,kdy=? WHERE cislo=?");
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
            statement = con.prepareStatement("UPDATE anketa2 SET data=?,vice=?,uzavrena=? WHERE cislo=?");

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
            statement = con.prepareStatement("UPDATE uzivatel SET login=?,jmeno=?,email=?,openid=?,prezdivka=?,data=? WHERE cislo=?");
            statement.setString(1,user.getLogin());
            statement.setString(2,user.getName());
            statement.setString(3,user.getEmail());
            statement.setString(4,user.getOpenId());
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
            statement = con.prepareStatement("SELECT typ_predka,predek,typ,hodnota FROM vlastnost WHERE typ_predka=? AND predek=?");
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
     * @param objects map, WHERE key is id (integer) and value is CommonObject with this id
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
            statement = con.prepareStatement("SELECT typ_predka,predek,typ,hodnota FROM vlastnost WHERE typ_predka=? AND predek IN "
                                            + Misc.getInCondition(objects.size()));
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
            statement = con.prepareStatement("DELETE FROM vlastnost WHERE typ_predka=? AND predek=?");
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
            statement = con.prepareStatement("INSERT INTO vlastnost (typ_predka,predek,typ,hodnota) VALUES (?,?,?,?)");
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

    public void create(Tag tag) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("INSERT INTO stitek (id,titulek,vytvoreno,nadrazeny) VALUES (?,?,?,?)");
            statement.setString(1, tag.getId());
            statement.setString(2, tag.getTitle());
            tag.setCreated(new java.util.Date());
            statement.setTimestamp(3, new Timestamp(tag.getCreated().getTime()));
            statement.setString(4, tag.getParent());

            int result = statement.executeUpdate();
            if (result == 0)
                throw new PersistenceException("Nepodařilo se vložit " + tag + " do databáze!");

            tagCache.put(tag);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DuplicateKeyException("Duplikátní údaj: " + tag.getId() + "!");
            } else {
                throw new PersistenceException("Nemohu uložit " + tag, e);
            }
        } finally {
            releaseSQLResources(con, statement, null);
        }
    }

    public void update(Tag tag) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("UPDATE stitek SET titulek=?, nadrazeny=? WHERE id=?");
            statement.setString(1, tag.getTitle());
            statement.setString(2, tag.getParent());
            statement.setString(3, tag.getId());

            int result = statement.executeUpdate();
            if (result == 0)
                throw new PersistenceException("Nepodařilo se aktualizovat " + tag + " v databázi!");

            tagCache.put(tag);
        } catch (SQLException e) {
            throw new PersistenceException("Nemohu uložit " + tag, e);
        } finally {
            releaseSQLResources(con, statement, null);
        }
    }

    public void remove(Tag tag) {
        Connection con = null;
        PreparedStatement statement = null, statement2 = null, statement3 = null;
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("DELETE FROM stitkovani WHERE stitek=?");
            statement.setString(1, tag.getId());
            statement.executeUpdate();

            statement2 = con.prepareStatement("DELETE FROM stitek WHERE id=?");
            statement2.setString(1, tag.getId());
            statement2.executeUpdate();

            statement2 = con.prepareStatement("UPDATE stitek SET nadrazeny=NULL WHERE nadrazeny=?");
            statement2.setString(1, tag.getId());
            statement2.executeUpdate();

            tagCache.remove(tag.getId());
        } catch (SQLException e) {
            throw new PersistenceException("Nemohu smazat " + tag, e);
        } finally {
            releaseSQLResources(con, new Statement[] {statement, statement2, statement3}, null);
        }
    }

    public Map<String, Tag> getTags() {
        Connection con = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            HashMap<String, Tag> tags = new HashMap<String, Tag>(100);
            con = getSQLConnection();
            statement = con.createStatement();
            resultSet = statement.executeQuery("SELECT id,titulek,vytvoreno,nadrazeny, (SELECT count(*) FROM stitkovani WHERE stitek=id) FROM stitek");
            tags.clear();
            while (resultSet.next()) {
                String id = resultSet.getString(1);
                Tag tag = new Tag(id, resultSet.getString(2));
                tag.setCreated(new java.util.Date(resultSet.getTimestamp(3).getTime()));
                tag.setParent(resultSet.getString(4));
                tag.setUsage(resultSet.getInt(5));
                tags.put(id, tag);
                tagCache.put(tag);
            }

            return tags;
        } catch (SQLException e) {
            throw new PersistenceException("Nemohu získat seznam štítků!", e);
        } finally {
            releaseSQLResources(con, statement, resultSet);
        }
    }

    public List<String> getAssignedTags(GenericDataObject obj) {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<String> tags;

        try {
            tags = tagCache.getAssignedTags(obj);
            if (tags != null)
                return tags;

            con = getSQLConnection();
            statement = con.prepareStatement("SELECT stitek FROM stitkovani WHERE typ=? AND cislo=?");
            statement.setString(1, PersistenceMapping.getGenericObjectType(obj));
            statement.setInt(2, obj.getId());

            resultSet = statement.executeQuery();
            tags = new ArrayList<String>();
            while (resultSet.next()) {
                tags.add(resultSet.getString(1));
            }

            tagCache.storeAssignedTags(obj, tags);
            return tags;
        } catch (SQLException e) {
            throw new PersistenceException("Nemohu načíst štítky pro " + obj, e);
        } finally {
            releaseSQLResources(con, statement, resultSet);
        }
    }

    public void assignTags(GenericDataObject obj, List<String> tags) {
        if (tags.isEmpty())
            return;

        int updated;
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("REPLACE INTO stitkovani (typ,cislo,stitek) VALUES (?,?,?)");
            statement.setString(1, PersistenceMapping.getGenericObjectType(obj));
            statement.setInt(2, obj.getId());

            for (String tag : tags) {
                statement.setString(3, tag);
                updated = statement.executeUpdate();
                if (updated == 1)
                    tagCache.assignTag(obj, tag);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Nemohu uložit štítky pro " + obj, e);
        } finally {
            releaseSQLResources(con, statement, null);
        }
    }

    public void unassignTags(GenericDataObject obj, List<String> tags) {
        if (tags.isEmpty())
            return;

        int updated;
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = getSQLConnection();
            statement = con.prepareStatement("DELETE FROM stitkovani WHERE typ=? AND cislo=? AND stitek=?");
            statement.setString(1, PersistenceMapping.getGenericObjectType(obj));
            statement.setInt(2, obj.getId());

            for (String tag : tags) {
                statement.setString(3, tag);
                updated = statement.executeUpdate();
                if (updated == 1)
                    tagCache.unassignTag(obj, tag);
            }
        } catch (SQLException e) {
            throw new PersistenceException("Nemohu smazat štítky pro " + obj, e);
        } finally {
            releaseSQLResources(con, statement, null);
        }
    }

    /**
     * @return id of last inserted row
     */
    private int getAutoId(Statement statement) throws AbcException {
        if ( ! (statement instanceof com.mysql.jdbc.Statement) )
            try {
                statement = ProxoolFacade.getDelegateStatement(statement);
            } catch (ProxoolException e) {
                throw new AbcException("Cannot obtain delegated statement FROM "+statement, e);
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

    // copied from EditAttachment
    public static void deleteAttachment(Data data) {
        Document document = data.getData();
        List elements = document.selectNodes("//*[@path]");
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            Element element = (Element) iter.next();
            String path = element.attributeValue("path");

            File file = new File(AbcConfig.getDeployPath() + path);
            if (!file.exists()) {
                return;
            }

            if (!file.delete()) {
                log.warn("Nepodařilo se smazat soubor " + file.getAbsolutePath());
                return;
            }
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
            return "data";
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
