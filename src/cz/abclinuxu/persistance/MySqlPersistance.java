/*
 * User: literakl
 * Date: Nov 17, 2001
 * Time: 8:21:06 AM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.persistance;

import java.sql.*;
import java.io.*;
import java.util.*;
import cz.abclinuxu.data.*;
import cz.abclinuxu.AbcException;
import com.codestudio.sql.PoolManPreparedStatement;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * This class provides persistance backed up by MySQl database. You should consult
 * file SQL_def.sql for data scheme. <p>
 * Basicly, all classes are represented by separate table, if they don't have common
 * table for their super class (Item, Record and Poll). In this case, one table represents
 * many descendants. They can be distinguished by <code>typ</code> field.<p>
 * Descendants of Record:<br>
 * <table border="1">
 * <tr><th>class</th><th>typ</th></tr>
 * <tr><td>HardwareRecord</td><td>1</td></tr>
 * <tr><td>SoftwareRecord</td><td>2</td></tr>
 * <tr><td>ArticleRecord</td><td>3</td></tr>
 * </table>
 * <p>Descendants of Item:<br>
 * <table border="1">
 * <tr><th>class</th><th>typ</th></tr>
 * <tr><td>Make</td><td>1</td></tr>
 * <tr><td>Article</td><td>2</td></tr>
 * <tr><td>Question</td><td>3</td></tr>
 * <tr><td>Request</td><td>4</td></tr>
 * </table>
 * <p>Descendants of Poll:<br>
 * <table border="1">
 * <tr><th>class</th><th>typ</th></tr>
 * <tr><td>Survey</td><td>1</td></tr>
 * <tr><td>Rating</td><td>2</td></tr>
 * </table>
 * <p>Tree contains references of GenericObjects. Each supported class may contain any number
 * of children - other GenericObjects. Each object may be referenced as child of more objects
 * and they have equal rights for it. Tree uses its own id schema - which is made as concatenation
 * of database table identifier and objects <code>id</code>.
 * <table border="1">
 * <tr><th>class</th><th>identifier</th></tr>
 * <tr><td>Poll</td><td>A</td></tr>
 * <tr><td>Data</td><td>O</td></tr>
 * <tr><td>Item</td><td>P</td></tr>
 * <tr><td>Record</td><td>Z</td></tr>
 * <tr><td>Category</td><td>K</td></tr>
 * <tr><td>Link</td><td>L</td></tr>
 * <tr><td>error</td><td>E</td></tr>
 * </table>
 */
public class MySqlPersistance extends Persistance {

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MySqlPersistance.class);

    static {
        try {
            Class.forName("com.codestudio.sql.PoolMan").newInstance();
        } catch (Exception e) {
            log.fatal("Nemuzu vytvorit instanci PoolMana, zkontroluj CLASSPATH!",e);
        }
    }

    /**
     * @return instance (or singleton) of this object
     */
    public static Persistance getInstance() {
        MySqlPersistance persistance = new MySqlPersistance();
        return persistance;
    }

    /**
     * Downloads object described by <code>obj</code> (id and class name) from persistant storage.
     * It also fills its <code>content</code> field with uninitialized objects (only <code>id</code>).
     * They may be downloaded on request with this method again.
     */
    public GenericObject loadObject(GenericObject obj) throws PersistanceException {
        try {
            if (obj instanceof Record) {
                return loadRecord((Record)obj);
            } else if (obj instanceof Item) {
                return loadItem((Item)obj);
            } else if (obj instanceof Category) {
                return loadCategory((Category)obj);
            } else if (obj instanceof Data) {
                return loadData((Data)obj);
            } else if (obj instanceof Link) {
                return loadLink((Link)obj);
            } else if (obj instanceof Poll) {
                return loadPoll((Poll)obj);
            } else if (obj instanceof User) {
                return loadUser((User)obj);
            }
        } catch (SQLException e) {
            throw new PersistanceException("Nemohu nahrat "+obj.toString()+" z databaze!",AbcException.DB_FIND,obj,e);
        }
        return null;
    }

    /**
     * Makes object peristant. It may modify <code>id</code> of argument.
     */
    public void storeObject(GenericObject obj) throws PersistanceException {
        try {
            if (obj instanceof Record) {
                storeRecord((Record)obj);
            } else if (obj instanceof Item) {
                storeItem((Item)obj);
            } else if (obj instanceof Category) {
                storeCategory((Category)obj);
            } else if (obj instanceof Data) {
                storeData((Data)obj);
            } else if (obj instanceof Link) {
                storeLink((Link)obj);
            } else if (obj instanceof Poll) {
                storePoll((Poll)obj);
            } else if (obj instanceof User) {
                storeUser((User)obj);
            }
        } catch (SQLException e) {
            throw new PersistanceException("Nemohu ulozit "+obj.toString()+" do databaze!",AbcException.DB_INSERT,obj,e);
        }
    }

    /**
     * Synchronizes changes in the object with the persistant storage.
     */
    public void updateObject(GenericObject obj) throws PersistanceException {
        try {
            if (obj instanceof Record) {
                updateRecord((Record)obj);
            } else if (obj instanceof Item) {
                updateItem((Item)obj);
            } else if (obj instanceof Category) {
                updateCategory((Category)obj);
            } else if (obj instanceof Data) {
                updateData((Data)obj);
            } else if (obj instanceof Link) {
                updateLink((Link)obj);
            } else if (obj instanceof Poll) {
                updatePoll((Poll)obj);
            } else if (obj instanceof User) {
                updateUser((User)obj);
            }
        } catch (SQLException e) {
            throw new PersistanceException("Nemohu ulozit zmeny v "+obj.toString()+" do databaze!",AbcException.DB_UPDATE,obj,e);
        }
    }

    /**
     * Remove object and its references in tree from persistant storage.
     */
    public void removeObject(GenericObject obj) throws PersistanceException {
        try {
            if (obj instanceof Record) {
                removeRecord((Record)obj);
            } else if (obj instanceof Item) {
                removeItem((Item)obj);
            } else if (obj instanceof Category) {
                removeCategory((Category)obj);
            } else if (obj instanceof Data) {
                removeData((Data)obj);
            } else if (obj instanceof Link) {
                removeLink((Link)obj);
            } else if (obj instanceof Poll) {
                removePoll((Poll)obj);
            } else if (obj instanceof User) {
                removeUser((User)obj);
            }
        } catch (SQLException e) {
            throw new PersistanceException("Nemohu smazat "+obj.toString()+" z databaze!",AbcException.DB_REMOVE,obj,e);
        }
    }

    /**
     * Searches persistant storage for objects, which are similar to arguments. For each
     * GenericObject: find objects, which have same values. <code>Id</code> field is
     * ignored, same as all null fields. There is a <code>and</code> relationship between
     * non-null fields (non-zero for integer fields). There is a <code>or</code>
     * relationship between objects in <code>objects</code> list.<p>
     * For text pattern search, you can use wildchars '%' and '?', in SQL meaning.<p>
     * Example: find all open categories, which have 'HP' in their names<br>
     * <pre>Category cat = new Category(0);
     *cat.setOpen(true);
     *cat.setData("%&lt;name>%HP%&lt;/name>%");
     *List objects = new ArrayList().add(cat);
     *List result = findByExample(objects);
     *</pre>
     * @return List of GenericObjects
     */
    public List findByExample(List objects) throws PersistanceException {
        List result = new LinkedList();
        for (Iterator iter = result.iterator(); iter.hasNext();) {
            GenericObject obj = null;
            try {
                obj = (GenericObject) iter.next();
                if (obj instanceof Record) {
                    findRecordByExample((Record)obj,result);
                } else if (obj instanceof Item) {
                    findItemByExample((Item)obj,result);
                } else if (obj instanceof Category) {
                    findCategoryByExample((Category)obj,result);
                } else if (obj instanceof User) {
                    findUserByExample((User)obj,result);
                } else if (obj instanceof Link) {
                    findLinkByExample((Link)obj,result);
                } else if (obj instanceof Poll) {
                    findPollByExample((Poll)obj,result);
                } else if (obj instanceof Data) {
                    findDataByExample((Data)obj,result);
                }
            } catch (SQLException e) {
                throw new PersistanceException("Nemohu najit "+obj.toString()+" v databazi!",AbcException.DB_FIND,obj,e);
            }
        }
        return result;
    }

    /**
     * Adds <code>obj</code> under <code>parent</code> in the object tree.
     */
    public void addObjectToTree(GenericObject obj, GenericObject parent) throws PersistanceException {
        Connection con = null;
        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("insert into strom values(?,?)");
            statement.setString(1,getTreeId(parent));
            statement.setString(2,getTreeId(obj));

            int result = statement.executeUpdate();
        } catch ( SQLException e ) {
            throw new PersistanceException("Nemohu vlozit do stromu dvojici ("+parent+","+obj+")",AbcException.DB_INSERT,obj,e);
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * Removes <code>obj</code> from <code>parent</code> in the object tree.
     */
    public void removeObjectFromTree(GenericObject obj, GenericObject parent) throws PersistanceException {
    }

    /**
     * Increments counter for <code>obj</code>. This method can be used for ArticleRecord or Link.
     */
    public void incrementCounter(GenericObject obj) throws PersistanceException {
    }

    /**
     * @return connection to database from connection pool.
     */
    protected Connection getSQLConnection() throws PersistanceException {
        try {
            return DriverManager.getConnection("jdbc:poolman");
        } catch (SQLException e) {
            throw new PersistanceException("Spojeni s databazi selhalo!",AbcException.DB_REFUSED,null,e);
        }
    }

    /**
     * closes database connection and logs any errors
     */
    protected void releaseSQLConnection(Connection con) {
        try {
            con.close();
        } catch (Exception e) {
            log.error("Problems while closing connection to database!",e);
        }
    }

    /**
     * @return <code>obj.getId()</code> with table's prefix
     */
    protected static String getTreeId(GenericObject obj) {
        if (obj instanceof Record) {
            return "Z" + obj.getId();
        } else if (obj instanceof Item) {
            return "P" + obj.getId();
        } else if (obj instanceof Category) {
            return "K" + obj.getId();
        } else if (obj instanceof Data) {
            return "O" + obj.getId();
        } else if (obj instanceof Link) {
            return "L" + obj.getId();
        } else if (obj instanceof Poll) {
            return "A" + obj.getId();
        }
        log.error("getTreeId called with object, which can't be stored in tree!");
        return "E" + obj.getId();
    }

    /**
     * @return generic object, which is described by <code>id</code> in tree
     * convention (not filled with values, just primary key)
     */
    protected GenericObject getObjectFromTreeId(String id) throws PersistanceException {
        char classKey = id.charAt(0);
        int i = 0;
        try {
            i = Integer.parseInt(id.substring(1));
        } catch (NumberFormatException e) {
            throw new PersistanceException(id+" is not valid tree identifier!",AbcException.DB_WRONG_DATA,id,e);
        }

        if ( classKey=='Z' ) {
            return new Record(i);
        } else if ( classKey=='P' ) {
            return new Item(i);
        } else if ( classKey=='K' ) {
            return new Category(i);
        } else if ( classKey=='O' ) {
            return new Data(i);
        } else if ( classKey=='L' ) {
            return new Link(i);
        } else if ( classKey=='A' ) {
            return new Poll(i);
        } else {
            throw new PersistanceException(id+" is not valid tree identifier!",AbcException.DB_WRONG_DATA,id,null);
        }
    }

    /**
     * @return user from mysql db
     */
    protected GenericObject loadUser(User obj) throws PersistanceException, SQLException {
        Connection con = null;
        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("select * from uzivatel where cislo=?");
            statement.setInt(1,obj.getId());

            ResultSet result = statement.executeQuery();
            if ( !result.next() ) {
                throw new PersistanceException("Uzivatel "+obj.getId()+" nebyl nalezen!",AbcException.DB_NOT_FOUND,obj,null);
            }

            User user = new User(obj.getId());
            user.setLogin(result.getString(2));
            user.setName(result.getString(3));
            user.setEmail(result.getString(4));
            user.setPassword(result.getString(5));

            return user;
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * @return item descendant from mysql db
     */
    protected GenericObject loadItem(Item obj) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("select * from polozka where cislo=?");
            statement.setInt(1,obj.getId());

            ResultSet result = statement.executeQuery();
            if ( !result.next() ) {
                throw new PersistanceException("Polozka "+obj.getId()+" nebyla nalezena!",AbcException.DB_NOT_FOUND,obj,null);
            }

            Item item = null;
            switch ( result.getInt(2) ) {
                case 1: item = new Make(obj.getId());break;
                case 2: item = new Article(obj.getId());break;
                case 3: item = new Question(obj.getId());break;
                case 4: item = new Request(obj.getId());break;
                default: throw new PersistanceException("Nalezena polozka "+obj.getId()+" neznameho typu "+result.getInt(2)+"!",AbcException.DB_UNKNOWN_CLASS,obj,null);
            }
            item.setData(new String(result.getBytes(3)));
            item.setOwner(result.getInt(4));
            item.setUpdated(result.getTimestamp(5));

            findChildren(item,"P"+item.getId());
            return item;
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * @return item descendant from mysql db
     */
    protected GenericObject loadRecord(Record obj) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("select * from zaznam where cislo=?");
            statement.setInt(1,obj.getId());

            ResultSet result = statement.executeQuery();
            if ( !result.next() ) {
                throw new PersistanceException("Zaznam "+obj.getId()+" nebyl nalezen!",AbcException.DB_NOT_FOUND,obj,null);
            }

            Record record = null;
            switch ( result.getInt(2) ) {
                case 1: record = new HardwareRecord(obj.getId());break;
                case 2: record = new SoftwareRecord(obj.getId());break;
                case 3: record = new ArticleRecord(obj.getId());break;
                default: throw new PersistanceException("Nalezen zaznam "+obj.getId()+" neznameho typu "+result.getInt(2)+"!",AbcException.DB_UNKNOWN_CLASS,obj,null);
            }
            record.setData(new String(result.getBytes(3)));
            record.setOwner(result.getInt(4));
            record.setUpdated(result.getTimestamp(5));

            findChildren(record,"Z"+record.getId());
            return record;
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * @return category from mysql db
     */
    protected GenericObject loadCategory(Category obj) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("select * from kategorie where cislo=?");
            statement.setInt(1,obj.getId());

            ResultSet result = statement.executeQuery();
            if ( !result.next() ) {
                throw new PersistanceException("Kategorie "+obj.getId()+" nebyla nalezena!",AbcException.DB_NOT_FOUND,obj,null);
            }

            Category ctg = new Category(obj.getId());
            ctg.setData(new String(result.getBytes(2)));
            ctg.setOpen(result.getBoolean(3));
            ctg.setOwner(result.getInt(4));
            ctg.setUpdated(result.getTimestamp(5));

            findChildren(ctg,"K"+ctg.getId());
            return ctg;
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * @return data from mysql db
     */
    protected GenericObject loadData(Data obj) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("select * from objekty where cislo=?");
            statement.setInt(1,obj.getId());

            ResultSet result = statement.executeQuery();
            if ( !result.next() ) {
                throw new PersistanceException("Datovy objekt "+obj.getId()+" nebyl nalezen!",AbcException.DB_NOT_FOUND,obj,null);
            }

            Data data = new Data(obj.getId());
            data.setFormat(result.getString(2));
            data.setData(result.getBytes(3));
            data.setOwner(result.getInt(4));

            return data;
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * @return link from mysql db
     */
    protected GenericObject loadLink(Link obj) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("select * from odkazy where cislo=?");
            statement.setInt(1,obj.getId());

            ResultSet result = statement.executeQuery();
            if ( !result.next() ) {
                throw new PersistanceException("Odkaz "+obj.getId()+" nebyl nalezen!",AbcException.DB_NOT_FOUND,obj,null);
            }

            Link link = new Link(obj.getId());
            link.setServer(result.getInt(2));
            link.setText(result.getString(3));
            link.setUrl(result.getString(4));

            return link;
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * @return poll from mysql db
     */
    protected GenericObject loadPoll(Poll obj) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("select * from anketa where cislo=?");
            statement.setInt(1,obj.getId());

            ResultSet result = statement.executeQuery();
            if ( !result.next() ) {
                throw new PersistanceException("Anketa "+obj.getId()+" nebyla nalezena!",AbcException.DB_NOT_FOUND,obj,null);
            }

            Poll poll = null;
            switch ( result.getInt(2) ) {
                case 1: poll = new Survey(obj.getId());break;
                case 2: poll = new Rating(obj.getId());break;
                default: throw new PersistanceException("Nalezena anketa "+obj.getId()+" neznameho typu "+result.getInt(2)+"!",AbcException.DB_UNKNOWN_CLASS,obj,null);
            }
            poll.setText(new String(result.getString(3)));
            poll.setMultiChoice(result.getBoolean(4));
            poll.setUpdated(result.getTimestamp(5));

            statement = con.prepareStatement("select volba,pocet from anketa_data where anketa=? order by cislo asc");
            statement.setInt(1,obj.getId());

            result = statement.executeQuery();
            List choices = new ArrayList();
            while ( result.next() ) {
                PollChoice choice = new PollChoice(result.getString(1));
                choice.setCount(result.getInt(2));
                choices.add(choice);
            }
            if ( choices.size()==0 ) {
                throw new PersistanceException("Anketa "+obj.getId()+" nema zadne volby!",AbcException.DB_INCOMPLETE,obj,null);
            }
            poll.setChoices(choices);

            findChildren(poll,"A"+poll.getId());
            return poll;
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * lookup tree for children of <code>obj</code> with <code>treeId</code> and sets them
     * with <code>obj.setContent()</code> call.
     */
    protected void findChildren(GenericObject obj, String treeId) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("select obsah from strom where id=?");
            statement.setString(1,treeId);

            ResultSet result = statement.executeQuery();
            while ( result.next() ) {
                String tmp = result.getString(1);
                try {
                    char type = tmp.charAt(0);
                    int id = Integer.parseInt(tmp.substring(1));

                    GenericObject child = null;
                    if ( type=='K' ) {
                        child = new Category(id);
                    } else if ( type=='P' ) {
                        child = new Item(id);
                    } else if ( type=='Z' ) {
                        child = new Record(id);
                    } else if ( type=='A' ) {
                        child = new Poll(id);
                    } else if ( type=='O' ) {
                        child = new Data(id);
                    } else if ( type=='L' ) {
                        child = new Link(id);
                    } else if ( type=='E' ) {
                        continue;
                    }

                    obj.addContent(child);
                } catch (Exception e) {
                    log.error("Cannot convert "+tmp+" from tree to GenericObject!",e);
                }
            }
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * stores record to database and updates <code>id</code>
     */
    protected void storeRecord(Record record) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("insert into zaznam values(0,?,?,?,NULL)");

            if ( record instanceof HardwareRecord ) {
                statement.setInt(1,1);
            } else if ( record instanceof SoftwareRecord ) {
                statement.setInt(1,2);
            } else if ( record instanceof ArticleRecord ) {
                statement.setInt(1,3);
            } else {
                throw new PersistanceException("Neznamy typ zaznamu "+ record.toString()+"!",AbcException.DB_UNKNOWN_CLASS,record,null);
            }

            statement.setBytes(2,record.getData().getBytes());
            statement.setInt(3,record.getOwner());

            int result = statement.executeUpdate();
            if ( result==0 ) {
                throw new PersistanceException("Nepodarilo se vlozit zaznam "+record.toString()+" do databaze!", AbcException.DB_INSERT, record, null);
            }

            org.gjt.mm.mysql.PreparedStatement mm = (org.gjt.mm.mysql.PreparedStatement)((PoolManPreparedStatement)statement).getNativePreparedStatement();
            record.setId((int)mm.getLastInsertID());

        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * stores item to database and updates <code>id</code>
     */
    protected void storeItem(Item item) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("insert into polozka values(0,?,?,?,NULL)");

            if ( item instanceof Make ) {
                statement.setInt(1,1);
            } else if ( item instanceof Article ) {
                statement.setInt(1,2);
            } else if ( item instanceof Question ) {
                statement.setInt(1,3);
            } else if ( item instanceof Request ) {
                statement.setInt(1,4);
            } else {
                throw new PersistanceException("Neznamy typ polozky "+ item.toString()+"!",AbcException.DB_UNKNOWN_CLASS,item,null);
            }

            statement.setBytes(2,item.getData().getBytes());
            statement.setInt(3,item.getOwner());

            int result = statement.executeUpdate();
            if ( result==0 ) {
                throw new PersistanceException("Nepodarilo se vlozit polozku "+item.toString()+" do databaze!", AbcException.DB_INSERT, item, null);
            }

            org.gjt.mm.mysql.PreparedStatement mm = (org.gjt.mm.mysql.PreparedStatement)((PoolManPreparedStatement)statement).getNativePreparedStatement();
            item.setId((int)mm.getLastInsertID());

        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * stores category to database and updates <code>id</code>
     */
    protected void storeCategory(Category category) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("insert into kategorie values(0,?,?,?,NULL)");

            statement.setBytes(1,category.getData().getBytes());
            statement.setBoolean(2,category.isOpen());
            statement.setInt(3,category.getOwner());

            int result = statement.executeUpdate();
            if ( result==0 ) {
                throw new PersistanceException("Nepodarilo se vlozit kategorii "+category.toString()+" do databaze!", AbcException.DB_INSERT, category, null);
            }

            org.gjt.mm.mysql.PreparedStatement mm = (org.gjt.mm.mysql.PreparedStatement)((PoolManPreparedStatement)statement).getNativePreparedStatement();
            category.setId((int)mm.getLastInsertID());

        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * stores data to database and updates <code>id</code>
     */
    protected void storeData(Data data) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("insert into objekty values(0,?,?,?)");

            statement.setString(1,data.getFormat());
            statement.setBytes(2,data.getData());
            statement.setInt(3,data.getOwner());

            int result = statement.executeUpdate();
            if ( result==0 ) {
                throw new PersistanceException("Nepodarilo se vlozit objekt "+data.toString()+" do databaze!", AbcException.DB_INSERT, data, null);
            }

            org.gjt.mm.mysql.PreparedStatement mm = (org.gjt.mm.mysql.PreparedStatement)((PoolManPreparedStatement)statement).getNativePreparedStatement();
            data.setId((int)mm.getLastInsertID());

        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * stores link to database and updates <code>id</code>
     */
    protected void storeLink(Link link) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("insert into odkazy values(0,?,?,?,?)");

            statement.setInt(1,link.getServer());
            statement.setString(2,link.getText());
            statement.setString(3,link.getUrl());
            statement.setBoolean(4,link.isFixed());

            int result = statement.executeUpdate();
            if ( result==0 ) {
                throw new PersistanceException("Nepodarilo se vlozit objekt "+link.toString()+" do databaze!", AbcException.DB_INSERT, link, null);
            }

            org.gjt.mm.mysql.PreparedStatement mm = (org.gjt.mm.mysql.PreparedStatement)((PoolManPreparedStatement)statement).getNativePreparedStatement();
            link.setId((int)mm.getLastInsertID());

        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * stores poll to database and updates <code>id</code>
     */
    protected void storePoll(Poll poll) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            PollChoice[] choices = poll.getChoices();
            if ( choices==null || choices.length<2 ) {
                throw new PersistanceException("Anketa musi mit nejmene dve volby!", AbcException.DB_INCOMPLETE, poll, null);
            }

            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("insert into anketa values(0,?,?,?,NULL)");

            if ( poll instanceof Survey ) {
                statement.setInt(1,1);
            } else if ( poll instanceof Rating ) {
                statement.setInt(1,2);
            } else {
                throw new PersistanceException("Neznamy typ ankety "+ poll.toString()+"!",AbcException.DB_UNKNOWN_CLASS,poll,null);
            }

            statement.setString(2,poll.getText());
            statement.setBoolean(3,poll.isMultiChoice());

            int result = statement.executeUpdate();
            if ( result==0 ) {
                throw new PersistanceException("Nepodarilo se vlozit anketu "+poll.toString()+" do databaze!", AbcException.DB_INSERT, poll, null);
            }

            org.gjt.mm.mysql.PreparedStatement mm = (org.gjt.mm.mysql.PreparedStatement)((PoolManPreparedStatement)statement).getNativePreparedStatement();
            poll.setId((int)mm.getLastInsertID());

            statement = con.prepareStatement("insert into anketa_data values(?,?,?,0)");
            for ( int i=0; i<choices.length; i++ ) {
                statement.clearParameters();
                statement.setInt(1,i);
                statement.setInt(2,poll.getId());
                statement.setString(3,choices[i].getText());

                result = statement.executeUpdate();
            }
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * stores user to database and updates <code>id</code>
     */
    protected void storeUser(User user) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("insert into uzivatel values(0,?,?,?,?)");

            statement.setString(1,user.getLogin());
            statement.setString(2,user.getName());
            statement.setString(3,user.getEmail());
            statement.setString(4,user.getPassword());

            int result = statement.executeUpdate();
            if ( result==0 ) {
                throw new PersistanceException("Nepodarilo se vlozit objekt "+user.toString()+" do databaze!", AbcException.DB_INSERT, user, null);
            }

            org.gjt.mm.mysql.PreparedStatement mm = (org.gjt.mm.mysql.PreparedStatement)((PoolManPreparedStatement)statement).getNativePreparedStatement();
            user.setId((int)mm.getLastInsertID());
        } catch ( SQLException e ) {
            if ( e.getErrorCode()==1062 ) {
                throw new PersistanceException("Prihlasovaci jmeno "+user.getLogin()+" je uz registrovano!", AbcException.DB_DUPLICATE, user, e);
            } else throw e;
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * updates record in database
     */
    protected void updateRecord(Record record) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("update zaznam set data=? where cislo=?");
            statement.setBytes(1,record.getData().getBytes());
            statement.setInt(2,record.getId());

            int result = statement.executeUpdate();
            if ( result!=1 ) {
                throw new PersistanceException("Nepodarilo se ulozit zmeny v "+record.toString()+" do databaze!", AbcException.DB_UPDATE, record, null);
            }
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * updates item in database
     */
    protected void updateItem(Item item) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("update polozka set data=? where cislo=?");
            statement.setBytes(1,item.getData().getBytes());
            statement.setInt(2,item.getId());

            int result = statement.executeUpdate();
            if ( result!=1 ) {
                throw new PersistanceException("Nepodarilo se ulozit zmeny v "+item.toString()+" do databaze!", AbcException.DB_UPDATE, item, null);
            }
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * updates category in database
     */
    protected void updateCategory(Category category) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("update kategorie set data=?,verejny=? where cislo=?");
            statement.setBytes(1,category.getData().getBytes());
            statement.setBoolean(2,category.isOpen());
            statement.setInt(3,category.getId());

            int result = statement.executeUpdate();
            if ( result!=1 ) {
                throw new PersistanceException("Nepodarilo se ulozit zmeny v "+category.toString()+" do databaze!", AbcException.DB_UPDATE, category, null);
            }
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * updates data in database
     */
    protected void updateData(Data data) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("update objekty set data=?,format=? where cislo=?");
            statement.setBytes(1,data.getData());
            statement.setString(2,data.getFormat());
            statement.setInt(3,data.getId());

            int result = statement.executeUpdate();
            if ( result!=1 ) {
                throw new PersistanceException("Nepodarilo se ulozit zmeny v "+data.toString()+" do databaze!", AbcException.DB_UPDATE, data, null);
            }
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * updates link in database
     */
    protected void updateLink(Link link) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("update odkazy set server=?,nazev=?,url=?,trvaly=? where cislo=?");
            statement.setInt(1,link.getServer());
            statement.setString(2,link.getText());
            statement.setString(3,link.getUrl());
            statement.setBoolean(4,link.isFixed());
            statement.setInt(5,link.getId());

            int result = statement.executeUpdate();
            if ( result!=1 ) {
                throw new PersistanceException("Nepodarilo se ulozit zmeny v "+link.toString()+" do databaze!", AbcException.DB_UPDATE, link, null);
            }
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * updates poll in database
     */
    protected void updatePoll(Poll poll) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("update anketa set otazka=?,vice=? where cislo=?");
            statement.setString(1,poll.getText());
            statement.setBoolean(2,poll.isMultiChoice());
            statement.setInt(3,poll.getId());

            int result = statement.executeUpdate();
            if ( result!=1 ) {
                throw new PersistanceException("Nepodarilo se ulozit zmeny v "+poll.toString()+" do databaze!", AbcException.DB_UPDATE, poll, null);
            }

            PollChoice[] choices = poll.getChoices();
            if ( choices==null || choices.length<2 ) {
                throw new PersistanceException("Anketa musi mit nejmene dve volby!", AbcException.DB_INCOMPLETE, poll, null);
            }

            statement = con.prepareStatement("update anketa_data set volba=? where cislo=? and anketa=?");
            for (int i = 0; i<choices.length; i++) {
                PollChoice choice = choices[i];
                statement.clearParameters();
                statement.setString(1,choice.getText());
                statement.setInt(2,i);
                statement.setInt(3,poll.getId());
                result = statement.executeUpdate();
            }
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * updates user in database
     */
    protected void updateUser(User user) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("update uzivatel set login=?,jmeno=?,email=?,heslo=? where cislo=?");
            statement.setString(1,user.getLogin());
            statement.setString(2,user.getName());
            statement.setString(3,user.getEmail());
            statement.setString(4,user.getPassword());
            statement.setInt(5,user.getId());

            int result = statement.executeUpdate();
            if ( result!=1 ) {
                throw new PersistanceException("Nepodarilo se ulozit zmeny v "+user.toString()+" do databaze!", AbcException.DB_UPDATE, user, null);
            }
        } catch ( SQLException e ) {
            if ( e.getErrorCode()==1062 ) {
                throw new PersistanceException("Prihlasovaci jmeno "+user.getLogin()+" je uz registrovano!", AbcException.DB_DUPLICATE, user, e);
            } else throw e;
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * removes record from database
     */
    protected void removeRecord(Record record) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            // kill all siblings
            PreparedStatement statement = con.prepareStatement("select obsah from strom where id=?");
            statement.setString(1,"Z"+record.getId());

            ResultSet result = statement.executeQuery();
            while ( result.next() ) {
                String child = result.getString(1);
                statement = con.prepareStatement("select id from strom where id!=? and obsah=?");
                statement.setString(1,"Z"+record.getId());
                statement.setString(2,child);

                ResultSet sibling = statement.executeQuery();
                if ( !sibling.next() ) {
                    removeObject(getObjectFromTreeId(child));
                }
                sibling.close();
            }
            result.close();

            // remove all references from tree
            statement = con.prepareStatement("delete from strom where id=? or obsah=?");
            statement.setString(1,"Z"+record.getId());
            statement.setString(2,"Z"+record.getId());
            statement.executeUpdate();

            // kill record itself
            statement = con.prepareStatement("delete from zaznam where cislo=?");
            statement.setInt(1,record.getId());
            statement.executeUpdate();
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * removes item from database
     */
    protected void removeItem(Item item) throws PersistanceException, SQLException {
    }

    /**
     * removes category from database
     */
    protected void removeCategory(Category category) throws PersistanceException, SQLException {
    }

    /**
     * removes data from database
     */
    protected void removeData(Data data) throws PersistanceException, SQLException {
    }

    /**
     * removes link from database
     */
    protected void removeLink(Link link) throws PersistanceException, SQLException {
    }

    /**
     * removes poll from database
     */
    protected void removePoll(Poll poll) throws PersistanceException, SQLException {
    }

    /**
     * removes user from database
     */
    protected void removeUser(User user) throws PersistanceException, SQLException {
    }

    public void findRecordByExample(Record record,List result) throws PersistanceException,SQLException {}
    public void findItemByExample(Item item,List result) throws PersistanceException,SQLException {}
    public void findCategoryByExample(Category category,List result) throws PersistanceException,SQLException {}
    public void findDataByExample(Data data,List result) throws PersistanceException,SQLException {}
    public void findLinkByExample(Link link,List result) throws PersistanceException,SQLException {}
    public void findPollByExample(Poll poll,List result) throws PersistanceException,SQLException {}
    public void findUserByExample(User user,List result) throws PersistanceException,SQLException {}

    public static void main(String[] args) throws Exception {
        DOMConfigurator.configure("WEB-INF/log4j.xml");
        Persistance persistance = PersistanceFactory.getPersistance();
        int  i=0,j=0;
        long start = System.currentTimeMillis();

        for (j=0; j<1 ;j++) {
            Record a = new HardwareRecord(0);
            a.setOwner(1);
            a.setData("hw a");
            persistance.storeObject(a);

            Record b = new SoftwareRecord(0);
            b.setOwner(2);
            b.setData("sw b");
            persistance.storeObject(b);

            Record c = new ArticleRecord(0);
            c.setOwner(1);
            c.setData("article c");
            persistance.storeObject(c);

            Record d = new SoftwareRecord(0);
            d.setOwner(2);
            d.setData("sw d");
            persistance.storeObject(d);

            System.out.println("a = " + a);
            System.out.println("b = " + b);
            System.out.println("c = " + c);
            System.out.println("d = " + d);

            persistance.addObjectToTree(b,a);
            persistance.addObjectToTree(c,a);
            persistance.addObjectToTree(c,d);

            persistance.removeObject(a);
        }
        long end = System.currentTimeMillis();
        float avg = (end-start)/(float)(j*i);
        System.out.println("celkem = "+(end-start)+" ,prumer = "+avg);
    }
}
