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
            throw new PersistanceException("Nemuzu nahrat "+obj.toString()+" z databaze!",e);
        }
        return null;
    }

    /**
     * Makes object peristant. It may modify <code>id</code> of argument.
     */
    public void storeObject(GenericObject obj) throws PersistanceException {
    }

    /**
     * Synchronizes changes in the object with the persistant storage.
     */
    public void updateObject(GenericObject obj) throws PersistanceException {
    }

    /**
     * Remove object and references in tree from persistant storage.
     */
    public void removeObject(GenericObject obj) throws PersistanceException {
    }

    /**
     * Adds <code>obj</code> under <code>parent</code> in the object tree.
     */
    public void addObjectToTree(GenericObject obj, GenericObject parent) throws PersistanceException {
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
            throw new PersistanceException("Spojeni s databazi selhalo!",e);
        }
    }

    /**
     * @return <code>obj.getId()</code> with table's prefix
     */
    protected static String getTreeId(GenericObject obj) {
        if (obj instanceof Record) {
            return "Z"+obj.getId();  // 700 000 calls per second of getTreeId(ArticleRecord)
        } else if (obj instanceof Item) {
            return "P"+obj.getId();
        } else if (obj instanceof Category) {
            return "K"+obj.getId();
        } else if (obj instanceof Data) {
            return "O"+obj.getId();
        } else if (obj instanceof Link) {
            return "L"+obj.getId();
        } else if (obj instanceof Poll) {
            return "A"+obj.getId();  // 180 000 calls per second of getTreeId(Poll)
        }
        log.error("getTreeId called with object, which can't be stored in tree!");
        return "E"+obj.getId();
    }

    /**
     * @return user from mysql db
     */
    protected GenericObject loadUser(User obj) throws PersistanceException, SQLException {
        Connection con = getSQLConnection();
        PreparedStatement statement = con.prepareStatement("select * from uzivatel where cislo=?");
        statement.setInt(1,obj.getId());

        ResultSet result = statement.executeQuery();
        if ( !result.next() ) {
            con.close();
            throw new PersistanceException("Uzivatel "+obj.getId()+" nebyl nalezen!");
        }

        User user = new User(obj.getId());
        user.setLogin(result.getString(2));
        user.setName(result.getString(3));
        user.setEmail(result.getString(4));
        user.setPassword(result.getString(5));

        con.close();
        return user;
    }

    /**
     * @return item descendant from mysql db
     */
    protected GenericObject loadItem(Item obj) throws PersistanceException, SQLException {
        Connection con = getSQLConnection();
        PreparedStatement statement = con.prepareStatement("select * from polozka where cislo=?");
        statement.setInt(1,obj.getId());

        ResultSet result = statement.executeQuery();
        if ( !result.next() ) {
            con.close();
            throw new PersistanceException("Polozka "+obj.getId()+" nebyla nalezena!");
        }

        Item item = null;
        switch ( result.getInt(2) ) {
            case 1: item = new Make(obj.getId());break;
            case 2: item = new Article(obj.getId());break;
            case 3: item = new Question(obj.getId());break;
            case 4: item = new Request(obj.getId());break;
            default: con.close();
                     throw new PersistanceException("Nalezena polozka "+obj.getId()+" neznameho typu "+result.getInt(2)+"!");
        }
        item.setData(new String(result.getBytes(3)));
        item.setOwner(result.getInt(4));
        item.setUpdated(result.getTimestamp(5));

        con.close();
        findChildren(item,"P"+item.getId());
        return item;
    }

    /**
     * @return item descendant from mysql db
     */
    protected GenericObject loadRecord(Record obj) throws PersistanceException, SQLException {
        Connection con = getSQLConnection();
        PreparedStatement statement = con.prepareStatement("select * from zaznam where cislo=?");
        statement.setInt(1,obj.getId());

        ResultSet result = statement.executeQuery();
        if ( !result.next() ) {
            con.close();
            throw new PersistanceException("Zaznam "+obj.getId()+" nebyl nalezen!");
        }

        Record record = null;
        switch ( result.getInt(2) ) {
            case 1: record = new HardwareRecord(obj.getId());break;
            case 2: record = new SoftwareRecord(obj.getId());break;
            case 3: record = new ArticleRecord(obj.getId());break;
            default: con.close();
                     throw new PersistanceException("Nalezen zaznam "+obj.getId()+" neznameho typu "+result.getInt(2)+"!");
        }
        record.setData(new String(result.getBytes(3)));
        record.setOwner(result.getInt(4));
        record.setUpdated(result.getTimestamp(5));

        con.close();
        findChildren(record,"Z"+record.getId());
        return record;
    }

    /**
     * @return category from mysql db
     */
    protected GenericObject loadCategory(Category obj) throws PersistanceException, SQLException {
        Connection con = getSQLConnection();
        PreparedStatement statement = con.prepareStatement("select * from kategorie where cislo=?");
        statement.setInt(1,obj.getId());

        ResultSet result = statement.executeQuery();
        if ( !result.next() ) {
            con.close();
            throw new PersistanceException("Kategorie "+obj.getId()+" nebyla nalezena!");
        }

        Category ctg = new Category(obj.getId());
        ctg.setData(new String(result.getBytes(2)));
        ctg.setOpen(result.getBoolean(3));
        ctg.setOwner(result.getInt(4));
        ctg.setUpdated(result.getTimestamp(5));

        con.close();
        findChildren(ctg,"K"+ctg.getId());
        return ctg;
    }

    /**
     * @return data from mysql db
     */
    protected GenericObject loadData(Data obj) throws PersistanceException, SQLException {
        Connection con = getSQLConnection();
        PreparedStatement statement = con.prepareStatement("select * from objekty where cislo=?");
        statement.setInt(1,obj.getId());

        ResultSet result = statement.executeQuery();
        if ( !result.next() ) {
            con.close();
            throw new PersistanceException("Datovy objekt "+obj.getId()+" nebyl nalezen!");
        }

        Data data = new Data(obj.getId());
        data.setOwner(result.getInt(2));
        data.setFormat(result.getString(3));
        data.setData(result.getBytes(4));

        con.close();
        return data;
    }

    /**
     * @return link from mysql db
     */
    protected GenericObject loadLink(Link obj) throws PersistanceException, SQLException {
        Connection con = getSQLConnection();
        PreparedStatement statement = con.prepareStatement("select * from odkazy where cislo=?");
        statement.setInt(1,obj.getId());

        ResultSet result = statement.executeQuery();
        if ( !result.next() ) {
            con.close();
            throw new PersistanceException("Odkaz "+obj.getId()+" nebyl nalezen!");
        }

        Link link = new Link(obj.getId());
        link.setServer(new Server(result.getInt(2)));
        link.setText(result.getString(3));
        link.setUrl(result.getString(4));

        con.close();
        return link;
    }

    /**
     * @return poll from mysql db
     */
    protected GenericObject loadPoll(Poll obj) throws PersistanceException, SQLException {
        Connection con = getSQLConnection();
        PreparedStatement statement = con.prepareStatement("select * from anketa where cislo=?");
        statement.setInt(1,obj.getId());

        ResultSet result = statement.executeQuery();
        if ( !result.next() ) {
            con.close();
            throw new PersistanceException("Kategorie "+obj.getId()+" nebyla nalezena!");
        }

        Poll poll = null;
        boolean isSurvey = false;
        switch ( result.getInt(2) ) {
            case 1: poll = new Survey(obj.getId()); isSurvey = true; break;
            case 2: poll = new Rating(obj.getId());break;
            default: con.close();
                     throw new PersistanceException("Nalezena anketa "+obj.getId()+" neznameho typu "+result.getInt(2)+"!");
        }
        poll.setText(new String(result.getString(3)));
        if ( isSurvey ) ((Survey)poll).setMultiChoice(result.getBoolean(4));
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
            con.close();
            throw new PersistanceException("Anketa "+obj.getId()+" nema zadne volby!");
        }
        poll.setChoices(choices);

        con.close();
        findChildren(poll,"A"+poll.getId());
        return poll;
    }

    /**
     * lookup tree for children of <code>obj</code> with <code>treeId</code> and sets them
     * with <code>obj.setContent()</code> call.
     */
    protected void findChildren(GenericObject obj, String treeId) throws PersistanceException, SQLException {
        Connection con = getSQLConnection();
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
    }

    public static void main(String[] args) throws Exception {
        GenericObject obj = new Make(1);
        Persistance persistance = PersistanceFactory.getPersistance();
        int  i;
        long start = System.currentTimeMillis();

        for (i=0; i<1; i++ ) {
            GenericObject obj2 = persistance.loadObject(obj);
            System.out.println("obj2 = " + obj2.toString());
            List children = obj2.getContent();
            for (Iterator it = children.iterator(); it.hasNext();) {
                GenericObject obj3 = (GenericObject) it.next();
                obj3 = persistance.loadObject(obj3);
                System.out.println("obj3 = " + obj3.toString());
            }
        }
        long end = System.currentTimeMillis();
        float avg = (end-start)/(float)i;
        System.out.println("celkem = "+(end-start)+" ,prumer = "+avg);
    }
}
