/*
 * User: literakl
 * Date: Dec 5, 2001
 * Time: 3:45:13 PM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.persistance;

import java.util.*;
import java.sql.*;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.PersistanceException;
import cz.abclinuxu.persistance.LogicalExpressionTokenizer;
import cz.abclinuxu.AbcException;
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
 * <tr><td>User</td><td>U</td></tr>
 * <tr><td>error</td><td>E</td></tr>
 * </table>
 */
public class MySqlPersistance implements Persistance {

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MySqlPersistance.class);

    /** contains URL to database connection */
    String dbUrl = "jdbc:mysql://localhost/abc?user=literakl";

    static {
        try {
            Class.forName("org.gjt.mm.mysql.Driver");
        } catch (Exception e) {
            log.fatal("Nemuzu vytvorit instanci JDBC driveru, zkontroluj CLASSPATH!",e);
        }
    }

    public MySqlPersistance() {
    }

    public MySqlPersistance(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public void create(GenericObject obj, GenericObject parent) throws PersistanceException {
        Connection con = null;

        try {
            con = getSQLConnection();
            if ( obj.getId()==0 ) {
                if ( obj instanceof Poll ) {
                    storePoll((Poll)obj);
                } else {
                    List conditions = new ArrayList();
                    StringBuffer sb = new StringBuffer();
                    appendCreateParams(obj,sb,conditions);
                    PreparedStatement statement = con.prepareStatement(sb.toString());
                    for ( int i=0; i<conditions.size(); i++ ) {
                        Object o = conditions.get(i);
                        statement.setObject(i+1,o);
                    }

                    int result = statement.executeUpdate();
                    if ( result==0 ) {
                        log.error("Nepodarilo se vlozit "+obj+" do databaze!");
                        throw new PersistanceException("Nepodarilo se vlozit "+obj+" do databaze!", AbcException.DB_INSERT, obj, null);
                    }
                    org.gjt.mm.mysql.PreparedStatement mm = (org.gjt.mm.mysql.PreparedStatement)statement;
                    obj.setId((int)mm.getLastInsertID());
                }
                obj.setInitialized(true);
            }

            if ( parent!=null ) {
                if ( parent.getId()==0 ) {
                    log.error("Neni mozne vlozit relaci vlastnictvi pro neinicializovaneho predka!");
                    throw new PersistanceException("Neni mozne vlozit relaci vlastnictvi pro neinicializovaneho predka!",AbcException.DB_INCOMPLETE,parent,null);
                }

                PreparedStatement statement = con.prepareStatement("insert into strom values(?,?,?,?)");
                statement.setString(1,getTableId(parent));
                statement.setInt(2,parent.getId());
                statement.setString(3,getTableId(obj));
                statement.setInt(4,obj.getId());

                int result = statement.executeUpdate();
                if ( result==0 ) {
                    log.error("Nepodarilo se vlozit relaci mezi "+parent+" a "+obj+" do databaze!");
                    throw new PersistanceException("Nepodarilo se vlozit relaci do databaze!", AbcException.DB_INSERT, obj, null);
                }
            }
            return;
        } catch ( SQLException e ) {
            log.error("Nemohu ulozit "+obj+", predek je "+parent,e);
            if ( e.getErrorCode()==1062 ) {
                throw new PersistanceException("Prihlasovaci jmeno "+((User)obj).getLogin()+" je uz registrovano!", AbcException.DB_DUPLICATE, obj, e);
            } else {
                throw new PersistanceException("Nemohu ulozit "+obj,AbcException.DB_INSERT,obj,e);
            }
        } finally {
            releaseSQLConnection(con);
        }
    }

    public void update(GenericObject obj) throws PersistanceException {
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

    public void synchronize(GenericObject obj) throws PersistanceException {
        GenericObject temp = findById(obj);
        obj.setContent(temp.getContent());
        if (obj instanceof Record) {
            Record a = (Record) obj, b= (Record) temp;
            a.setData(b.getData());
            a.setOwner(b.getOwner());
            a.setUpdated(b.getUpdated());
        } else if (obj instanceof Item) {
            Item a = (Item) obj, b= (Item) temp;
            a.setData(b.getData());
            a.setOwner(b.getOwner());
            a.setUpdated(b.getUpdated());
        } else if (obj instanceof Category) {
            Category a = (Category) obj, b= (Category) temp;
            a.setData(b.getData());
            a.setOwner(b.getOwner());
            a.setUpdated(b.getUpdated());
            a.setOpen(b.isOpen());
        } else if (obj instanceof Data) {
            Data a = (Data) obj, b= (Data) temp;
            a.setData(b.getData());
            a.setOwner(b.getOwner());
            a.setFormat(b.getFormat());
        } else if (obj instanceof Link) {
            Link a = (Link) obj, b= (Link) temp;
            a.setServer(b.getServer());
            a.setText(b.getText());
            a.setFixed(b.isFixed());
            a.setOwner(b.getOwner());
            a.setUpdated(b.getUpdated());
        } else if (obj instanceof Poll) {
            Poll a = (Poll) obj, b= (Poll) temp;
            a.setChoices(b.getChoices());
            a.setText(b.getText());
            a.setUpdated(b.getUpdated());
            a.setMultiChoice(b.isMultiChoice());
        } else if (obj instanceof User) {
            User a = (User) obj, b= (User) temp;
            a.setLogin(b.getLogin());
            a.setName(b.getName());
            a.setEmail(b.getEmail());
            a.setPassword(b.getPassword());
        }
        obj.setInitialized(true);
    }

    public GenericObject findById(GenericObject obj) throws PersistanceException {
        GenericObject result = null;
        try {
            if (obj instanceof Record) {
                result = loadRecord((Record)obj);
            } else if (obj instanceof Item) {
                result = loadItem((Item)obj);
            } else if (obj instanceof Category) {
                result = loadCategory((Category)obj);
            } else if (obj instanceof Data) {
                result = loadData((Data)obj);
            } else if (obj instanceof Link) {
                result = loadLink((Link)obj);
            } else if (obj instanceof Poll) {
                result = loadPoll((Poll)obj);
            } else if (obj instanceof User) {
                result = loadUser((User)obj);
            }
            if ( result!=null ) result.setInitialized(true);
            return result;
        } catch (SQLException e) {
            throw new PersistanceException("Nemohu nahrat "+obj.toString()+" z databaze!",AbcException.DB_FIND,obj,e);
        }
    }

    public List findByExample(List objects, String relations) throws PersistanceException {
        Connection con = null;
        if ( objects.size()==0 ) return new ArrayList();
        if ( relations==null ) relations = LogicalExpressionTokenizer.makeOrRelation(objects);

        try {
            con = getSQLConnection();

            List result = new ArrayList(), conditions = new ArrayList();
            StringBuffer sb = new StringBuffer("SELECT cislo FROM ");
            GenericObject obj = (GenericObject) objects.get(0);
            Class kind = getClass(obj);

            sb.append(getTable(obj));
            sb.append(" where ");
            LogicalExpressionTokenizer stk = new LogicalExpressionTokenizer(relations);
            String token = null;
            while ( (token = stk.nextToken())!=null ) {
                try {
                    int index = Integer.parseInt(token);
                    sb.append('(');
                    obj = (GenericObject) objects.get(index);
                    if ( getClass(obj)!=kind ) {
                        throw new PersistanceException("Ruzne objekty v listu objects!",AbcException.DB_WRONG_DATA,obj,null);
                    }
                    appendFindParams(obj,sb, conditions);
                    sb.append(')');
                } catch ( NumberFormatException e ) {
                    sb.append(token);
                }
            }

            PreparedStatement statement = con.prepareStatement(sb.toString());
            for ( int i=0; i<conditions.size(); i++ ) {
                Object o = conditions.get(i);
                statement.setObject(i+1,o);
            }

            ResultSet resultSet = statement.executeQuery();
            while ( resultSet.next() ) {
                try {
                    GenericObject o = (GenericObject) kind.newInstance();
                    o.setId(resultSet.getInt(1));
                    synchronize(o);
                    result.add(o);
                } catch (Exception e) {
                    log.error("Cannot instantiate "+kind);
                }
            }
            return result;
        } catch ( SQLException e ) {
            throw new PersistanceException("Nemohu ",AbcException.DB_INSERT,objects,e);
        } finally {
            releaseSQLConnection(con);
        }
    }

    public List findByCommand(String command) throws PersistanceException {
        return null;
    }

    public void remove(GenericObject obj, GenericObject parent) throws PersistanceException {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = getSQLConnection();

            if ( parent!=null ) {
                statement = con.prepareStatement("delete from strom where predek_typ like ? and predek_id=? and obsah_typ like ? and obsah_id=?");
                statement.setString(1,getTableId(parent));
                statement.setInt(2,parent.getId());
                statement.setString(3,getTableId(obj));
                statement.setInt(4,obj.getId());
                statement.executeUpdate();
            }

            statement = con.prepareStatement("select predek_id from strom where obsah_typ like ? and obsah_id=?");
            statement.setString(1,getTableId(obj));
            statement.setInt(2,obj.getId());
            ResultSet resultSet = statement.executeQuery();
            if ( resultSet.next() ) return;

            findChildren(obj,con);
            for (Iterator iter = obj.getContent().iterator(); iter.hasNext();) {
                GenericObject child = (GenericObject) iter.next();
                remove(child,obj);
            }

            statement = con.prepareStatement("delete from "+getTable(obj)+" where cislo=?");
            statement.setInt(1,obj.getId());
            statement.executeUpdate();

            if ( obj instanceof Poll ) {
                statement = con.prepareStatement("delete from data_ankety where anketa=?");
                statement.setInt(1,obj.getId());
                statement.executeUpdate();
            }
        } catch ( SQLException e ) {
            throw new PersistanceException("Nemohu smazat ze stromu dvojici ("+obj+","+parent+")",AbcException.DB_INSERT,obj,e);
        } finally {
            releaseSQLConnection(con);
        }
    }

    public void incrementCounter(GenericObject obj) throws PersistanceException {
        Connection con = null;
        try {
            con = getSQLConnection();
            List conditions = new ArrayList();
            StringBuffer sb = new StringBuffer();

            appendIncrementUpdateParams(obj,sb,conditions);
            PreparedStatement statement = con.prepareStatement(sb.toString());
            for ( int i=0; i<conditions.size(); i++ ) {
                Object o = conditions.get(i);
                statement.setObject(i+1,o);
            }
            int result = statement.executeUpdate();

            if ( result==0 ) {
                sb.setLength(0); conditions.clear();
                appendIncrementInsertParams(obj,sb,conditions);
                statement = con.prepareStatement(sb.toString());
                for ( int i=0; i<conditions.size(); i++ ) {
                    Object o = conditions.get(i);
                    statement.setObject(i+1,o);
                }
                result = statement.executeUpdate();
            }
        } catch ( SQLException e ) {
            log.error("Nepodarilo se zvysit citac pro "+obj,e);
        } finally {
            releaseSQLConnection(con);
        }
    }

    public int getCounterValue(GenericObject obj) throws PersistanceException {
        Connection con = null;
        try {
            con = getSQLConnection();
            List conditions = new ArrayList();
            StringBuffer sb = new StringBuffer();

            appendIncrementSelectParams(obj,sb,conditions);
            PreparedStatement statement = con.prepareStatement(sb.toString());
            for ( int i=0; i<conditions.size(); i++ ) {
                Object o = conditions.get(i);
                statement.setObject(i+1,o);
            }

            ResultSet resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                log.error("Nepodarilo se zjistit hodnotu citace pro "+obj);
                return 0;
            }
            return resultSet.getInt(1);
        } catch ( SQLException e ) {
            log.error("Nepodarilo se zjistit hodnotu citace pro "+obj,e);
            return 0;
        } finally {
            releaseSQLConnection(con);
        }
    }

    public void removeCounter(GenericObject obj) throws PersistanceException {
        Connection con = null;
        try {
            con = getSQLConnection();
            List conditions = new ArrayList();
            StringBuffer sb = new StringBuffer();

            appendIncrementDeleteParams(obj,sb,conditions);
            PreparedStatement statement = con.prepareStatement(sb.toString());
            for ( int i=0; i<conditions.size(); i++ ) {
                Object o = conditions.get(i);
                statement.setObject(i+1,o);
            }
            int result = statement.executeUpdate();
        } catch ( SQLException e ) {
            log.error("Nepodarilo se smazat citac pro "+obj,e);
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * @return connection to database from connection pool.
     */
    private Connection getSQLConnection() throws PersistanceException {
        try {
            return DriverManager.getConnection(dbUrl);
        } catch (SQLException e) {
            throw new PersistanceException("Spojeni s databazi selhalo!",AbcException.DB_REFUSED,null,e);
        }
    }

    /**
     * Closes database connection and logs any errors
     */
    private void releaseSQLConnection(Connection con) {
        try {
            con.close();
        } catch (Exception e) {
            log.error("Problems while closing connection to database!",e);
        }
    }

    /**
     * @return Identification of table in the tree
     */
    private String getTableId(GenericObject obj) {
        if (obj instanceof Record) {
            return "Z";
        } else if (obj instanceof Item) {
            return "P";
        } else if (obj instanceof Category) {
            return "K";
        } else if (obj instanceof Data) {
            return "O";
        } else if (obj instanceof Link) {
            return "L";
        } else if (obj instanceof Poll) {
            return "A";
        } else if (obj instanceof User) {
            return "U";
        }
        log.error("getTableId called with object, which can't be stored in tree! "+obj);
        return "E";
    }

    /**
     * @return SQL table name for this GenericObject
     */
    private String getTable(GenericObject obj) throws PersistanceException {
        if (obj instanceof Record) {
            return "zaznam";
        } else if (obj instanceof Item) {
            return "polozka";
        } else if (obj instanceof Category) {
            return "kategorie";
        } else if (obj instanceof Data) {
            return "objekt";
        } else if (obj instanceof Link) {
            return "odkaz";
        } else if (obj instanceof Poll) {
            return "anketa";
        } else if (obj instanceof User) {
            return "uzivatel";
        }
        throw new PersistanceException("Nepodporovany typ tridy!",AbcException.DB_UNKNOWN_CLASS,obj,null);
    }

    /**
     * @return first descendant of GenericObject. E.g. for Category,
     * Data, User, Link, Item, Record and Poll it returns associated class.
     * But for HardwareRecord, SoftwareRecord, Make, Question, Rating etc.
     * it returns its superclass.
     */
    private Class getClass(GenericObject obj) throws PersistanceException {
        if (obj instanceof Record) {
            return Record.class;
        } else if (obj instanceof Item) {
            return Item.class;
        } else if (obj instanceof Category) {
            return Category.class;
        } else if (obj instanceof Data) {
            return Data.class;
        } else if (obj instanceof Link) {
            return Link.class;
        } else if (obj instanceof Poll) {
            return Poll.class;
        } else if (obj instanceof User) {
            return User.class;
        }
        throw new PersistanceException("Nepodporovany typ tridy!",AbcException.DB_UNKNOWN_CLASS,obj,null);
    }

    /**
     * lookup tree for children of <code>obj</code> with <code>treeId</code> and sets them
     * with <code>obj.setContent()</code> call.
     */
    private void findChildren(GenericObject obj, Connection con) throws PersistanceException, SQLException {
        PreparedStatement statement = con.prepareStatement("select obsah_typ,obsah_id from strom where predek_typ like ? and predek_id=?");
        statement.setString(1,getTableId(obj));
        statement.setInt(2,obj.getId());

        ResultSet resultSet = statement.executeQuery();
        obj.clearContent();
        while ( resultSet.next() ) {
            char type = resultSet.getString(1).charAt(0);
            int id = resultSet.getInt(2);

            if ( type=='K' ) {
                obj.addContent(new Category(id));
            } else if ( type=='P' ) {
                obj.addContent(new Item(id));
            } else if ( type=='Z' ) {
                obj.addContent(new Record(id));
            } else if ( type=='A' ) {
                obj.addContent(new Poll(id));
            } else if ( type=='O' ) {
                obj.addContent(new Data(id));
            } else if ( type=='L' ) {
                obj.addContent(new Link(id));
            } else if ( type=='U' ) {
                obj.addContent(new User(id));
            } else if ( type=='E' ) {
                continue;
            }
        }
    }

    /**
     * Appends INSERT prepared statement for this object to <code>sb</code> and parameter
     * to <code>conditions</code> for each asterisk in perpared statement.
     */
    private void appendCreateParams(GenericObject obj, StringBuffer sb, List conditions ) throws PersistanceException {
        if (obj instanceof Item) {
            sb.append("insert into polozka values(0,?,?,?,NULL)");
            if ( obj instanceof Make ) {
                conditions.add(new Integer(1));
            } else if ( obj instanceof Article ) {
                conditions.add(new Integer(2));
            } else if ( obj instanceof Question ) {
                conditions.add(new Integer(3));
            } else if ( obj instanceof Request ) {
                conditions.add(new Integer(4));
            } else {
                throw new PersistanceException("Neznamy typ polozky "+ obj.toString()+"!",AbcException.DB_UNKNOWN_CLASS,obj,null);
            }
            conditions.add(((Item)obj).getData().getBytes());
            conditions.add(new Integer(((Item)obj).getOwner()));

        } else if (obj instanceof Record) {
            sb.append("insert into zaznam values(0,?,?,?,NULL)");
            if ( obj instanceof HardwareRecord ) {
                conditions.add(new Integer(1));
            } else if ( obj instanceof SoftwareRecord ) {
                conditions.add(new Integer(2));
            } else if ( obj instanceof ArticleRecord ) {
                conditions.add(new Integer(3));
            } else {
                throw new PersistanceException("Neznamy typ zaznamu "+ obj.toString()+"!",AbcException.DB_UNKNOWN_CLASS,obj,null);
            }
            conditions.add(((Record)obj).getData().getBytes());
            conditions.add(new Integer(((Record)obj).getOwner()));

        } else if (obj instanceof Category) {
            sb.append("insert into kategorie values(0,?,?,?,NULL)");
            conditions.add(((Category)obj).getData().getBytes());
            conditions.add(new Boolean(((Category)obj).isOpen()));
            conditions.add(new Integer(((Category)obj).getOwner()));

        } else if (obj instanceof Data) {
            sb.append("insert into objekt values(0,?,?,?)");
            conditions.add(((Data)obj).getFormat());
            conditions.add(((Data)obj).getData());
            conditions.add(new Integer(((Data)obj).getOwner()));

        } else if (obj instanceof User) {
            sb.append("insert into uzivatel values(0,?,?,?,?)");
            conditions.add(((User)obj).getLogin());
            conditions.add(((User)obj).getName());
            conditions.add(((User)obj).getEmail());
            conditions.add(((User)obj).getPassword());

        } else if (obj instanceof Link) {
            sb.append("insert into odkaz values(0,?,?,?,?,?,NULL)");
            conditions.add(new Integer(((Link)obj).getServer()));
            conditions.add(((Link)obj).getText());
            conditions.add(((Link)obj).getUrl());
            conditions.add(new Boolean(((Link)obj).isFixed()));
            conditions.add(new Integer(((Link)obj).getOwner()));
        }
    }

    /**
     * append SQL statements to <code>sb</code> and objects to <code>conditions</code>
     * as PreparedStatement requires.
     */
    private void appendFindParams(GenericObject obj, StringBuffer sb, List conditions ) {
        boolean addAnd = false;
        int type = 0;

        if (obj instanceof Record) {
            if ( ((Record)obj).getOwner()!=0 ) {
                addAnd = true;
                sb.append("pridal=?");
                conditions.add(new Integer(((Record)obj).getOwner()));
            }

            if ( obj instanceof HardwareRecord ) type = 1;
            else if ( obj instanceof SoftwareRecord ) type = 2;
            else if ( obj instanceof ArticleRecord ) type = 3;
            if ( type!=0 ) {
                if ( addAnd ) sb.append(" and ");
                addAnd = true;
                sb.append("typ=?");
                conditions.add(new Integer(type));
            }

            if ( ((Record)obj).getData()!=null ) {
                if ( addAnd ) sb.append(" and ");
                sb.append("data like ?");
                conditions.add(((Record)obj).getData());
            }
            return;

        } else if (obj instanceof Item) {
            if ( ((Item)obj).getOwner()!=0 ) {
                addAnd = true;
                sb.append("pridal=?");
                conditions.add(new Integer(((Item)obj).getOwner()));
            }

            if ( obj instanceof Make ) type = 1;
            else if ( obj instanceof Article ) type = 2;
            else if ( obj instanceof Question ) type = 3;
            else if ( obj instanceof Request ) type = 4;
            if ( type!=0 ) {
                if ( addAnd ) sb.append(" and ");
                addAnd = true;
                sb.append("typ=?");
                conditions.add(new Integer(type));
            }

            if ( ((Item)obj).getData()!=null ) {
                if ( addAnd ) sb.append(" and ");
                sb.append("data like ?");
                conditions.add(((Item)obj).getData());
            }
            return;

        } else if (obj instanceof Category) {
            sb.append("verejny=?");
            conditions.add(new Boolean(((Category)obj).isOpen()));

            if ( ((Category)obj).getOwner()!=0 ) {
                sb.append("and pridal=?");
                conditions.add(new Integer(((Category)obj).getOwner()));
            }

            if ( ((Category)obj).getData()!=null ) {
                sb.append("and data like ?");
                conditions.add(((Category)obj).getData());
            }
            return;

        } else if (obj instanceof Data) {
            if ( ((Data)obj).getOwner()!=0 ) {
                addAnd = true;
                sb.append("pridal=?");
                conditions.add(new Integer(((Data)obj).getOwner()));
            }

            if ( ((Data)obj).getData()!=null ) {
                if ( addAnd ) sb.append(" and ");
                addAnd = true;
                sb.append("data like ?");
                conditions.add(((Data)obj).getData());
            }

            if ( ((Data)obj).getFormat()!=null ) {
                if ( addAnd ) sb.append(" and ");
                sb.append("format like ?");
                conditions.add(((Data)obj).getFormat());
            }
            return;

        } else if (obj instanceof Link) {
            sb.append("trvaly=?");
            conditions.add(new Boolean(((Link)obj).isFixed()));

            if ( ((Link)obj).getServer()!=0 ) {
                sb.append("and server=?");
                conditions.add(new Integer(((Link)obj).getServer()));
            }

            if ( ((Link)obj).getOwner()!=0 ) {
                sb.append("and pridal=?");
                conditions.add(new Integer(((Link)obj).getOwner()));
            }

            if ( ((Link)obj).getText()!=null ) {
                sb.append("and nazev like ?");
                conditions.add(((Link)obj).getText());
            }

            if ( ((Link)obj).getUrl()!=null ) {
                sb.append("and url like ?");
                conditions.add(((Link)obj).getUrl());
            }
            return;

        } else if (obj instanceof Poll) {
            if ( obj instanceof Survey ) type = 1;
            else if ( obj instanceof Rating ) type = 2;
            if ( type!=0 ) {
                addAnd = true;
                sb.append("typ=?");
                conditions.add(new Integer(type));
            }

            if ( ((Poll)obj).getText()!=null ) {
                if ( addAnd ) sb.append(" and ");
                sb.append("otazka like ?");
                conditions.add(((Poll)obj).getText());
            }
            return;
        }
    }

    /**
     * Appends UPDATE prepared statement to increment counter of this object to <code>sb</code> and parameter
     * to <code>conditions</code> for each asterisk in perpared statement.
     */
    private void appendIncrementUpdateParams(GenericObject obj, StringBuffer sb, List conditions ) throws PersistanceException {
        if (obj instanceof Link) {
            sb.append("update presmerovani set soucet=soucet+1 where server=? and den=curdate()");
            conditions.add(new Integer(((Link)obj).getServer()));
        } else {
            sb.append("update citac set soucet=soucet+1 where typ like ? and cislo=?");
            conditions.add(getTableId(obj));
            conditions.add(new Integer(obj.getId()));
        }
    }

    /**
     * Appends INSERT prepared statement to increment counter of this object to <code>sb</code> and parameter
     * to <code>conditions</code> for each asterisk in perpared statement.
     */
    private void appendIncrementInsertParams(GenericObject obj, StringBuffer sb, List conditions ) throws PersistanceException {
        if (obj instanceof Link) {
            sb.append("insert into presmerovani values(curdate(),?,1)");
            conditions.add(new Integer(((Link)obj).getServer()));
        } else {
            sb.append("insert into citac values(?,?,1)");
            conditions.add(getTableId(obj));
            conditions.add(new Integer(obj.getId()));
        }
    }

    /**
     * Appends SELECT prepared statement to get counter of this object to <code>sb</code> and parameter
     * to <code>conditions</code> for each asterisk in perpared statement.
     */
    private void appendIncrementSelectParams(GenericObject obj, StringBuffer sb, List conditions ) throws PersistanceException {
        if (obj instanceof Link) {
            sb.append("select soucet from presmerovani where server=? and den=curdate()");
            conditions.add(new Integer(((Link)obj).getServer()));
        } else {
            sb.append("select soucet from citac where typ like ? and cislo=?");
            conditions.add(getTableId(obj));
            conditions.add(new Integer(obj.getId()));
        }
    }

    /**
     * Appends DELETE prepared statement to remove counter for this object to <code>sb</code> and parameter
     * to <code>conditions</code> for each asterisk in perpared statement.
     */
    private void appendIncrementDeleteParams(GenericObject obj, StringBuffer sb, List conditions ) throws PersistanceException {
        if (obj instanceof Link) {
            sb.append("delete from presmerovani where server=? and den=curdate()");
            conditions.add(new Integer(((Link)obj).getServer()));
        } else {
            sb.append("delete from citac where typ like ? and cislo=?");
            conditions.add(getTableId(obj));
            conditions.add(new Integer(obj.getId()));
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

            org.gjt.mm.mysql.PreparedStatement mm = (org.gjt.mm.mysql.PreparedStatement)statement;
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
     * @return user from mysql db
     */
    protected GenericObject loadUser(User obj) throws PersistanceException, SQLException {
        Connection con = null;
        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("select * from uzivatel where cislo=?");
            statement.setInt(1,obj.getId());

            ResultSet resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new PersistanceException("Uzivatel "+obj.getId()+" nebyl nalezen!",AbcException.DB_NOT_FOUND,obj,null);
            }

            User user = new User(obj.getId());
            user.setLogin(resultSet.getString(2));
            user.setName(resultSet.getString(3));
            user.setEmail(resultSet.getString(4));
            user.setPassword(resultSet.getString(5));

            findChildren(obj,con);
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

            ResultSet resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new PersistanceException("Polozka "+obj.getId()+" nebyla nalezena!",AbcException.DB_NOT_FOUND,obj,null);
            }

            Item item = null;
            switch ( resultSet.getInt(2) ) {
                case 1: item = new Make(obj.getId());break;
                case 2: item = new Article(obj.getId());break;
                case 3: item = new Question(obj.getId());break;
                case 4: item = new Request(obj.getId());break;
                default: throw new PersistanceException("Nalezena polozka "+obj.getId()+" neznameho typu "+resultSet.getInt(2)+"!",AbcException.DB_UNKNOWN_CLASS,obj,null);
            }
            item.setData(new String(resultSet.getBytes(3)));
            item.setOwner(resultSet.getInt(4));
            item.setUpdated(resultSet.getTimestamp(5));

            findChildren(item,con);
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

            ResultSet resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new PersistanceException("Zaznam "+obj.getId()+" nebyl nalezen!",AbcException.DB_NOT_FOUND,obj,null);
            }

            Record record = null;
            switch ( resultSet.getInt(2) ) {
                case 1: record = new HardwareRecord(obj.getId());break;
                case 2: record = new SoftwareRecord(obj.getId());break;
                case 3: record = new ArticleRecord(obj.getId());break;
                default: throw new PersistanceException("Nalezen zaznam "+obj.getId()+" neznameho typu "+resultSet.getInt(2)+"!",AbcException.DB_UNKNOWN_CLASS,obj,null);
            }
            record.setData(new String(resultSet.getBytes(3)));
            record.setOwner(resultSet.getInt(4));
            record.setUpdated(resultSet.getTimestamp(5));

            findChildren(record,con);
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

            ResultSet resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new PersistanceException("Kategorie "+obj.getId()+" nebyla nalezena!",AbcException.DB_NOT_FOUND,obj,null);
            }

            Category ctg = new Category(obj.getId());
            ctg.setData(new String(resultSet.getBytes(2)));
            ctg.setOpen(resultSet.getBoolean(3));
            ctg.setOwner(resultSet.getInt(4));
            ctg.setUpdated(resultSet.getTimestamp(5));

            findChildren(ctg,con);
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
            PreparedStatement statement = con.prepareStatement("select * from objekt where cislo=?");
            statement.setInt(1,obj.getId());

            ResultSet resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new PersistanceException("Datovy objekt "+obj.getId()+" nebyl nalezen!",AbcException.DB_NOT_FOUND,obj,null);
            }

            Data data = new Data(obj.getId());
            data.setFormat(resultSet.getString(2));
            data.setData(resultSet.getBytes(3));
            data.setOwner(resultSet.getInt(4));

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
            PreparedStatement statement = con.prepareStatement("select * from odkaz where cislo=?");
            statement.setInt(1,obj.getId());

            ResultSet resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new PersistanceException("Odkaz "+obj.getId()+" nebyl nalezen!",AbcException.DB_NOT_FOUND,obj,null);
            }

            Link link = new Link(obj.getId());
            link.setServer(resultSet.getInt(2));
            link.setText(resultSet.getString(3));
            link.setUrl(resultSet.getString(4));
            link.setFixed(resultSet.getBoolean(5));
            link.setOwner(resultSet.getInt(6));
            link.setUpdated(resultSet.getTimestamp(7));

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

            ResultSet resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new PersistanceException("Anketa "+obj.getId()+" nebyla nalezena!",AbcException.DB_NOT_FOUND,obj,null);
            }

            Poll poll = null;
            switch ( resultSet.getInt(2) ) {
                case 1: poll = new Survey(obj.getId());break;
                case 2: poll = new Rating(obj.getId());break;
                default: throw new PersistanceException("Nalezena anketa "+obj.getId()+" neznameho typu "+resultSet.getInt(2)+"!",AbcException.DB_UNKNOWN_CLASS,obj,null);
            }
            poll.setText(new String(resultSet.getString(3)));
            poll.setMultiChoice(resultSet.getBoolean(4));
            poll.setUpdated(resultSet.getTimestamp(5));

            statement = con.prepareStatement("select volba,pocet from data_ankety where anketa=? order by cislo asc");
            statement.setInt(1,obj.getId());

            resultSet = statement.executeQuery();
            List choices = new ArrayList();
            while ( resultSet.next() ) {
                PollChoice choice = new PollChoice(resultSet.getString(1));
                choice.setCount(resultSet.getInt(2));
                choice.setPoll(obj.getId());
                choices.add(choice);
            }
            if ( choices.size()==0 ) {
                throw new PersistanceException("Anketa "+obj.getId()+" nema zadne volby!",AbcException.DB_INCOMPLETE,obj,null);
            }
            poll.setChoices(choices);

            findChildren(poll,con);
            return poll;
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * updates record in database
     */
    private void updateRecord(Record record) throws PersistanceException, SQLException {
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
    private void updateItem(Item item) throws PersistanceException, SQLException {
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
    private void updateCategory(Category category) throws PersistanceException, SQLException {
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
    private void updateData(Data data) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("update objekt set data=?,format=? where cislo=?");
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
    private void updateLink(Link link) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("update odkaz set server=?,nazev=?,url=?,trvaly=?,pridal=? where cislo=?");
            statement.setInt(1,link.getServer());
            statement.setString(2,link.getText());
            statement.setString(3,link.getUrl());
            statement.setBoolean(4,link.isFixed());
            statement.setInt(5,link.getOwner());
            statement.setInt(6,link.getId());

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
    private void updatePoll(Poll poll) throws PersistanceException, SQLException {
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

            statement = con.prepareStatement("update _data_ankety set volba=? where cislo=? and anketa=?");
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
    private void updateUser(User user) throws PersistanceException, SQLException {
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
}
