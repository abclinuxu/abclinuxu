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
import java.sql.Date;

import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.PersistanceException;
import cz.abclinuxu.AbcException;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * This class provides persistance backed up by MySQl database. You should consult
 * file SQL_def.sql for data scheme.
 * <p>Tree contains references of GenericObjects. Each supported class may contain any number
 * of children - other GenericObjects. Each object may be referenced as child of more objects
 * and they have equal rights for it. Tree uses its own id schema - which is made as union
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
 * <tr><td>AccessRights</td><td>X</td></tr>
 * <tr><td>error</td><td>E</td></tr>
 * </table>
 */
public class MySqlPersistance implements Persistance {

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MySqlPersistance.class);

    /** contains URL to database connection */
    String dbUrl = null;
    Cache cache = null;

    static {
        try {
            Class.forName("org.gjt.mm.mysql.Driver");
        } catch (Exception e) {
            log.fatal("Nemohu vytvorit instanci JDBC driveru, zkontroluj CLASSPATH!",e);
        }
    }

    public MySqlPersistance(String dbUrl) {
        if ( dbUrl==null ) log.fatal("Neni mozne inicializovat MySqlPersistenci prazdnym URL!");
        this.dbUrl = dbUrl;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public void create(GenericObject obj) throws PersistanceException {
        Connection con = null;
        if ( obj==null ) throw new PersistanceException("Neni mozne ulozit prazdny objekt!",AbcException.DB_INCOMPLETE,obj,null);

        try {
            con = getSQLConnection();
            if (log.isDebugEnabled()) log.debug("Chystam se ulozit "+obj);
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
            // todo get updated field
            cache.store(obj);
            log.info("Objekt ["+obj+"] ulozen");
        } catch ( SQLException e ) {
            log.error("Nemohu ulozit "+obj+"!",e);
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
        log.debug("Updated "+obj);

        if (obj instanceof GenericDataObject) {
            update((GenericDataObject)obj);
        } else if (obj instanceof Relation) {
            update((Relation)obj);
        } else if (obj instanceof Data) {
            update((Data)obj);
        } else if (obj instanceof Link) {
            update((Link)obj);
        } else if (obj instanceof Poll) {
            update((Poll)obj);
        } else if (obj instanceof User) {
            update((User)obj);
        }
    }

    public void synchronize(GenericObject obj) throws PersistanceException {
        if ( obj==null ) return;
        GenericObject found = cache.load(obj);
        if ( found==null ) found = findById(obj);

        obj.synchronizeWith(found);
        obj.setInitialized(true);
    }

    public GenericObject findById(GenericObject obj) throws PersistanceException {
        if ( obj==null ) throw new PersistanceException("Objekt nebyl nalezen!",AbcException.DB_NOT_FOUND,obj,null);
        GenericObject result = cache.load(obj);
        if ( result!=null && result.isInitialized() ) return result;

        if ( log.isDebugEnabled() ) log.debug("Hledam podle PK "+obj);
        try {
            result = loadObject(obj);
            if ( result!=null ) result.setInitialized(true);
            cache.store(result);
            return result;
        } catch (SQLException e) {
            log.error("Chyba pri hledani "+obj,e);
            throw new PersistanceException("Nemohu nahrat "+obj.toString()+" z databaze!",AbcException.DB_FIND,obj,e);
        }
    }

    public List findByExample(List objects, String relations) throws PersistanceException {
        Connection con = null;
        if ( objects.size()==0 ) return new ArrayList();
        if ( relations==null ) relations = makeOrRelation(objects);

        try {
            con = getSQLConnection();

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
                        throw new PersistanceException("Ruzne objekty v listu objects!",AbcException.WRONG_FORMAT,obj,null);
                    }
                    sb.append('(');
                    obj = (GenericObject) objects.get(index);
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
                    result.add(o);
                } catch (Exception e) {
                    log.error("Cannot instantiate "+kind);
                }
            }
            return result;
        } catch ( SQLException e ) {
            StringBuffer sb = new StringBuffer(" Examples: ");
            for (Iterator iter = objects.iterator(); iter.hasNext();) {
                sb.append(((GenericObject) iter.next()).toString());
            }
            log.error("Chyba pri hledani podle prikladu. Relations: "+relations+sb.toString(),e);
            throw new PersistanceException("Nemohu provest zadane vyhledavani.",AbcException.DB_WRONG_COMMAND,objects,e);
        } finally {
            releaseSQLConnection(con);
        }
    }

    public List findByCommand(String command) throws PersistanceException {
        Connection con = null;
        if ( command==null || command.length()==0 ) throw new PersistanceException("Neni mozne ulozit prazdny objekt!",AbcException.DB_INCOMPLETE,command,null);
        List result = new ArrayList(5);

        try {
            con = getSQLConnection();
            if (log.isDebugEnabled()) log.debug("Chystam se hledat podle "+command);

            Statement statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery(command);
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
            log.error("Chyba pri hledani "+command,e);
            throw new PersistanceException("Chyba pri hledani!",AbcException.DB_FIND,command,e);
        } finally {
            releaseSQLConnection(con);
        }
        return result;
    }

    /**
     * @todo unit test
     */
    public List findParents(Relation relation) throws PersistanceException {
        Connection con = null;
        try {
            con = getSQLConnection();
            List result = new ArrayList(3);
            int upper = relation.getUpper();
            GenericObject parent = relation.getParent();

            while ( upper!=0 ) {
                PreparedStatement statement = con.prepareStatement("select predchozi,typ_predka,predek,data from relace where cislo=?");
                statement.setInt(1,upper);
                ResultSet resultSet = statement.executeQuery();
                if ( !resultSet.next() ) break;

                relation = new Relation(upper);
                upper = resultSet.getInt(1);
                relation.setUpper(upper);
                relation.setChild(parent);

                char type = resultSet.getString(2).charAt(0);
                int id = resultSet.getInt(3);
                parent = instantiateFromTree(type,id);
                relation.setParent(parent);

                try {
                    String tmp = resultSet.getString(4);
                    if ( tmp!=null ) {
                        tmp = insertEncoding(tmp);
                        relation.setData(new String(tmp));
                    }
                } catch (AbcException e) {
                    throw new PersistanceException(e.getMessage(),e.getStatus(),e.getSinner(),e.getNestedException());
                }

                result.add(0,relation);
            }
            return result;
        } catch ( SQLException e ) {
            log.error("Nepodarilo se zjistit predky pro "+relation,e);
            throw new PersistanceException("Nepodarilo se zjistit predky pro ",AbcException.DB_FIND,relation,e);
        } finally {
            releaseSQLConnection(con);
        }
    }

    public Relation[] findByExample(Relation example) throws PersistanceException {
        Connection con = null;
        con = getSQLConnection();
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
            sb.append(getTableId(child));
            sb.append("' and potomek=");
            sb.append(child.getId());
        }

        GenericObject parent = example.getParent();
        if ( parent!=null ) {
            if ( addAnd ) sb.append(" and ");
            sb.append("typ_predka='");
            sb.append(getTableId(parent));
            sb.append("' and predek=");
            sb.append(parent.getId());
        }

        try {
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sb.toString());
            while ( rs.next() ) {
                Relation relation = new Relation(rs.getInt(1));
                relation.setUpper(rs.getInt(2));

                char type = rs.getString(3).charAt(0);
                int id = rs.getInt(4);
                parent = instantiateFromTree(type,id);
                relation.setParent(parent);

                type = rs.getString(5).charAt(0);
                id = rs.getInt(6);
                child = instantiateFromTree(type,id);
                relation.setChild(child);

                try {
                    String tmp = rs.getString(7);
                    if ( tmp!=null ) {
                        tmp = insertEncoding(tmp);
                        relation.setData(new String(tmp));
                    }
                } catch (AbcException e) {
                    throw new PersistanceException(e.getMessage(),e.getStatus(),e.getSinner(),e.getNestedException());
                }

                found.add(relation);
            }

            if ( found==null ) return null;

            Relation[] relations = new Relation[found.size()];
            int i = 0;
            for (Iterator iter = found.iterator(); iter.hasNext();) {
                Relation relation = (Relation) iter.next();
                relations[i++] = relation;
            }

            return relations;
        } catch (SQLException e) {
            log.error("Cannot find relation "+example,e);
            return null;
        }
    }

    public void remove(GenericObject obj) throws PersistanceException {
        Connection con = null;
        PreparedStatement statement = null;
        if ( log.isDebugEnabled() ) log.debug("Chystam se smazat "+obj);

        try {
            con = getSQLConnection();
            ArrayList queue = new ArrayList();
            queue.add(obj);

            do {
                obj = (GenericObject) queue.remove(0);
                statement = con.prepareStatement("delete from "+getTable(obj)+" where cislo=?");
                statement.setInt(1,obj.getId());
                statement.executeUpdate();
                cache.remove(obj);

                if ( obj instanceof Poll ) {
                    statement = con.prepareStatement("delete from data_ankety where anketa=?");
                    statement.setInt(1,obj.getId());
                    statement.executeUpdate();
                }

                // if relation.getChild() became unreferenced, delete that child
                if ( obj instanceof Relation ) {
                    statement = con.prepareStatement("select predek from relace where typ_potomka=? and potomek=?");
                    GenericObject child = ((Relation)obj).getChild();
                    statement.setString(1,getTableId(child));
                    statement.setInt(2,child.getId());
                    ResultSet resultSet = statement.executeQuery();
                    if ( !resultSet.next() ) queue.add(child);
                    log.info("Smazan objekt "+obj);
                    continue; // relation doesn't have content
                }

                findChildren(obj,con);
                for (Iterator iter = obj.getContent().iterator(); iter.hasNext();) {
                    Relation child = (Relation) iter.next();
                    queue.add(child);
                }

                log.info("Smazan objekt "+obj);
            } while ( queue.size()!=0 );
        } catch ( SQLException e ) {
            log.error("Nemohu smazat objekt "+obj,e);
            throw new PersistanceException("Nemohu smazat "+obj+"!",AbcException.DB_REMOVE,obj,e);
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

    public void incrementCounter(PollChoice choice) throws PersistanceException {
        Connection con = null;
        try {
            con = getSQLConnection();

            PreparedStatement statement = con.prepareStatement("update data_ankety set pocet=pocet+1 where cislo=? and anketa=?");
            statement.setInt(1,choice.getId());
            statement.setInt(2,choice.getPoll());
            statement.executeUpdate();

            statement = con.prepareStatement("select pocet from data_ankety where cislo=? and anketa=?");
            statement.setInt(1,choice.getId());
            statement.setInt(2,choice.getPoll());

            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            choice.setCount(resultSet.getInt(1));
        } catch ( SQLException e ) {
            log.error("Nepodarilo se zvysit citac pro "+choice,e);
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
            log.fatal("Nemohu se spojit s databazi!",e);
            throw new PersistanceException("Nemohu se spojit s databazi!",AbcException.DB_REFUSED,null,e);
        }
    }

    /**
     * Closes database connection and logs any errors
     */
    private void releaseSQLConnection(Connection con) {
        try {
            if ( con!=null ) con.close();
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
        } else if (obj instanceof AccessRights) {
            return "X";
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
        } else if (obj instanceof Relation) {
            return "relace";
        } else if (obj instanceof Data) {
            return "objekt";
        } else if (obj instanceof Link) {
            return "odkaz";
        } else if (obj instanceof Poll) {
            return "anketa";
        } else if (obj instanceof User) {
            return "uzivatel";
        } else if (obj instanceof Server) {
            return "server";
        } else if (obj instanceof AccessRights) {
            return "pravo";
        }
        throw new PersistanceException("Nepodporovany typ tridy!",AbcException.DB_UNKNOWN_CLASS,obj,null);
    }

    /**
     * instantiates new GenericObject, which class is specified by <code>type</code> and
     * with desired <code>id</code>.
     */
    private GenericObject instantiateFromTree(char type, int id) {
        if ( type=='K' ) {
            return new Category(id);
        } else if ( type=='P' ) {
            return new Item(id);
        } else if ( type=='Z' ) {
            return new Record(id);
        } else if ( type=='A' ) {
            return new Poll(id);
        } else if ( type=='O' ) {
            return new Data(id);
        } else if ( type=='L' ) {
            return new Link(id);
        } else if ( type=='U' ) {
            return new User(id);
        } else if ( type=='X' ) {
            return new AccessRights(id);
        }
        return null;
    }

    /**
     * Safely adds encoding to string with xml
     */
    private String insertEncoding(String xml) {
        if ( xml==null || xml.startsWith("<?xml") ) return xml;
        return "<?xml version=\"1.0\" encoding=\"ISO-8859-2\" ?>\n"+xml;
    }

    /**
     * Loads object by PK from database.
     */
    private GenericObject loadObject(GenericObject obj) throws PersistanceException, SQLException {
        GenericObject result = null;
        if (obj instanceof GenericDataObject) {
            result = loadDataObject((GenericDataObject)obj);
        } else if (obj instanceof Relation) {
            result = loadRelation((Relation)obj);
        } else if (obj instanceof Data) {
            result = loadData((Data)obj);
        } else if (obj instanceof Link) {
            result = loadLink((Link)obj);
        } else if (obj instanceof Poll) {
            result = loadPoll((Poll)obj);
        } else if (obj instanceof User) {
            result = loadUser((User)obj);
        } else if (obj instanceof Server) {
            result = loadServer((Server)obj);
        } else if (obj instanceof AccessRights) {
            result = loadRights((AccessRights)obj);
        }
        return result;
    }

    /**
     * lookup tree for children of <code>obj</code> with <code>treeId</code> and sets them
     * with <code>obj.setContent()</code> call. Children are not initialized.
     */
    private void findChildren(GenericObject obj, Connection con) throws PersistanceException, SQLException {
        PreparedStatement statement = con.prepareStatement("select cislo,predchozi,typ_potomka,potomek,data from relace where typ_predka=? and predek=?");
        statement.setString(1,getTableId(obj));
        statement.setInt(2,obj.getId());
        ResultSet resultSet = statement.executeQuery();

        obj.clearContent();
        while ( resultSet.next() ) {
            Relation relation = new Relation(resultSet.getInt(1));
            relation.setUpper(resultSet.getInt(2));
            relation.setParent(obj);

            char type = resultSet.getString(3).charAt(0);
            int id = resultSet.getInt(4);
            GenericObject child = instantiateFromTree(type,id);
            relation.setChild(child);

            try {
                String tmp = resultSet.getString(5);
                if ( tmp!=null) {
                    tmp = insertEncoding(tmp);
                    relation.setData(new String(tmp));
                }
            } catch (AbcException e) {
                throw new PersistanceException(e.getMessage(),e.getStatus(),e.getSinner(),e.getNestedException());
            }

            relation.setInitialized(true);
            obj.addContent(relation);
        }
    }

    /**
     * Appends INSERT prepared statement for this object to <code>sb</code> and parameter
     * to <code>conditions</code> for each asterisk in prepared statement.
     */
    private void appendCreateParams(GenericObject obj, StringBuffer sb, List conditions ) throws PersistanceException {
        int type = 0;
        if (obj instanceof GenericDataObject) {
            sb.append("insert into "+getTable(obj)+" values(0,?,?,?,?)");
            if ( obj instanceof Category ) {
                conditions.add(new Boolean(((Category)obj).isOpen()));
            } else {
                type = ( obj instanceof Item )? ((Item)obj).getType() : ((Record)obj).getType();
                conditions.add(new Integer(type));
                if ( type==0 ) {
                    log.warn("Type not set! "+obj.toString());
                }
            }
            conditions.add(((GenericDataObject)obj).getDataAsString().getBytes());
            conditions.add(new Integer(((GenericDataObject)obj).getOwner()));
            long now = System.currentTimeMillis();
            conditions.add(new Timestamp(now));
            ((GenericDataObject)obj).setUpdated(new java.util.Date(now));

        } else if (obj instanceof Relation) {
            sb.append("insert into relace values(0,?,?,?,?,?,?)");
            conditions.add(new Integer(((Relation)obj).getUpper()));
            conditions.add(getTableId(((Relation)obj).getParent()));
            conditions.add(new Integer(((Relation)obj).getParent().getId()));
            conditions.add(getTableId(((Relation)obj).getChild()));
            conditions.add(new Integer(((Relation)obj).getChild().getId()));
            String tmp = ((Relation)obj).getDataAsString();
            conditions.add((tmp!=null)? tmp.getBytes():null);

        } else if (obj instanceof Data) {
            sb.append("insert into objekt values(0,?,?,?)");
            conditions.add(((Data)obj).getFormat());
            conditions.add(((Data)obj).getData());
            conditions.add(new Integer(((Data)obj).getOwner()));

        } else if (obj instanceof User) {
            sb.append("insert into uzivatel values(0,?,?,?,?,?)");
            conditions.add(((User)obj).getLogin());
            conditions.add(((User)obj).getName());
            conditions.add(((User)obj).getEmail());
            conditions.add(((User)obj).getPassword());
            conditions.add(((User)obj).getDataAsString().getBytes());

        } else if (obj instanceof Link) {
            sb.append("insert into odkaz values(0,?,?,?,?,?,?)");
            conditions.add(new Integer(((Link)obj).getServer()));
            conditions.add(((Link)obj).getText());
            conditions.add(((Link)obj).getUrl());
            conditions.add(new Boolean(((Link)obj).isFixed()));
            conditions.add(new Integer(((Link)obj).getOwner()));
            long now = System.currentTimeMillis();
            conditions.add(new Timestamp(now));
            ((Link)obj).setUpdated(new java.util.Date(now));
        }
    }

    /**
     * append SQL statements to <code>sb</code> and objects to <code>conditions</code>
     * as PreparedStatement requires.
     */
    private void appendFindParams(GenericObject obj, StringBuffer sb, List conditions ) throws PersistanceException {
        boolean addAnd = false;
        int type = 0;

        if (obj instanceof GenericDataObject) {
            GenericDataObject dataObject = (GenericDataObject) obj;
            if ( dataObject.getOwner()!=0 ) {
                addAnd = true;
                sb.append("pridal=?");
                conditions.add(new Integer(dataObject.getOwner()));
            }

            String search = dataObject.getSearchString();
            if ( (search!=null && search.length()>0 )) {
                if ( addAnd ) sb.append(" and ");
                sb.append("data like ?");
                conditions.add(search);
            }

            if ( obj instanceof Category ) {
                if ( addAnd ) sb.append(" and ");
                addAnd = true;
                sb.append("verejny=?");
                conditions.add(new Boolean(((Category)obj).isOpen()));
            } else {
                type = ( obj instanceof Item )? ((Item)obj).getType() : ((Record)obj).getType();
                if ( type!=0 ) {
                    if ( addAnd ) sb.append(" and ");
                    sb.append("typ=?");
                    conditions.add(new Integer(type));
                }
            }
            return;

        } else if (obj instanceof Data) {
            Data data = (Data) obj;
            if ( data.getOwner()!=0 ) {
                addAnd = true;
                sb.append("pridal=?");
                conditions.add(new Integer(data.getOwner()));
            }

            if (data.getData()!=null ) {
                if ( addAnd ) sb.append(" and ");
                addAnd = true;
                sb.append("data like ?");
                conditions.add(data.getData());
            }

            if ( data.getFormat()!=null && data.getFormat().length()>0 ) {
                if ( addAnd ) sb.append(" and ");
                sb.append("format like ?");
                conditions.add(data.getFormat());
            }
            return;

        } else if (obj instanceof Link) {
            Link link = (Link) obj;
            sb.append("trvaly=?");
            conditions.add(new Boolean(link.isFixed()));

            if ( link.getServer()!=0 ) {
                sb.append("and server=?");
                conditions.add(new Integer(link.getServer()));
            }

            if ( link.getOwner()!=0 ) {
                sb.append("and pridal=?");
                conditions.add(new Integer(link.getOwner()));
            }

            if ( link.getText()!=null ) {
                sb.append("and nazev like ?");
                conditions.add(link.getText());
            }

            if ( ((Link)obj).getUrl()!=null ) {
                sb.append("and url like ?");
                conditions.add(link.getUrl());
            }
            return;

        } else if (obj instanceof Poll) {
            type = ((Poll)obj).getType();
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

        } else if ( obj instanceof User ) {
            User user = (User) obj;

            String tmp = user.getLogin();
            if ( tmp!=null && tmp.length()>0 ) {
                addAnd = true;
                sb.append("login like ?");
                conditions.add(tmp);
            }

            tmp = user.getName();
            if ( tmp!=null && tmp.length()>0 ) {
                if ( addAnd ) sb.append(" and ");
                addAnd = true;
                sb.append("jmeno like ?");
                conditions.add(tmp);
            }

            tmp = user.getEmail();
            if ( tmp!=null && tmp.length()>0 ) {
                if ( addAnd ) sb.append(" and ");
                sb.append("email like ?");
                conditions.add(tmp);
            }
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
            PreparedStatement statement = con.prepareStatement("insert into anketa values(0,?,?,?,?,?)");

            statement.setInt(1,poll.getType() );
            if ( poll.getType()==0 ) {
                log.warn("Type is not set! "+poll.toString());
            }

            statement.setString(2,poll.getText());
            statement.setBoolean(3,poll.isMultiChoice());
            long when = 0;
            if (poll.getCreated()!=null) {
                when = poll.getCreated().getTime();
            } else {
                when = new java.util.Date().getTime();
                poll.setCreated(new java.util.Date(when));
            }
            statement.setTimestamp(4,new Timestamp(when));
            statement.setBoolean(5,poll.isClosed());

            int result = statement.executeUpdate();
            if ( result==0 ) {
                throw new PersistanceException("Nepodarilo se vlozit anketu "+poll.toString()+" do databaze!", AbcException.DB_INSERT, poll, null);
            }

            org.gjt.mm.mysql.PreparedStatement mm = (org.gjt.mm.mysql.PreparedStatement)statement;
            poll.setId((int)mm.getLastInsertID());

            statement = con.prepareStatement("insert into data_ankety values(?,?,?,?)");
            for ( int i=0; i<choices.length; i++ ) {
                statement.clearParameters();
                statement.setInt(1,i);
                statement.setInt(2,poll.getId());
                statement.setString(3,choices[i].getText());
                statement.setInt(4,choices[i].getCount());

                result = statement.executeUpdate();
                choices[i].setPoll(poll.getId());
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
            try {
                String tmp = resultSet.getString(6);
                tmp = insertEncoding(tmp);
                user.setData(tmp);
            } catch (AbcException e) {
                throw new PersistanceException(e.getMessage(),e.getStatus(),e.getSinner(),e.getNestedException());
            }

            findChildren(user,con);
            return user;
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * @return item descendant from mysql db
     */
    protected GenericDataObject loadDataObject(GenericDataObject obj) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("select * from "+getTable(obj)+" where cislo=?");
            statement.setInt(1,obj.getId());

            ResultSet resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new PersistanceException("Polozka "+obj.getId()+" nebyla nalezena!",AbcException.DB_NOT_FOUND,obj,null);
            }

            GenericDataObject item = null;
            if ( obj instanceof Category ) {
                item = new Category(obj.getId());
                ((Category)item).setOpen(resultSet.getBoolean(2));
            } else {
                if ( obj instanceof Item ) {
                    item = new Item(obj.getId(),resultSet.getInt(2));
                } else {
                    item = new Record(obj.getId(),resultSet.getInt(2));
                }
            }
            try {
                String tmp = resultSet.getString(3);
                tmp = insertEncoding(tmp);
                item.setData(new String(tmp));
            } catch (AbcException e) {
                throw new PersistanceException(e.getMessage(),e.getStatus(),e.getSinner(),e.getNestedException());
            }
            item.setOwner(resultSet.getInt(4));
            item.setUpdated(resultSet.getTimestamp(5));

            findChildren(item,con);
            return item;
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * @return user from mysql db
     */
    protected GenericObject loadRelation(Relation obj) throws PersistanceException, SQLException {
        Connection con = null;
        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("select * from relace where cislo=?");
            statement.setInt(1,obj.getId());

            ResultSet resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new PersistanceException("Relace "+obj.getId()+" nebyla nalezena!",AbcException.DB_NOT_FOUND,obj,null);
            }

            Relation relation = new Relation(resultSet.getInt(1));
            relation.setUpper(resultSet.getInt(2));

            char type = resultSet.getString(3).charAt(0);
            int id = resultSet.getInt(4);
            GenericObject parent = instantiateFromTree(type,id);
            relation.setParent(parent);

            type = resultSet.getString(5).charAt(0);
            id = resultSet.getInt(6);
            GenericObject child = instantiateFromTree(type,id);
            relation.setChild(child);

            try {
                String tmp = resultSet.getString(7);
                if ( tmp!=null ) {
                    tmp = insertEncoding(tmp);
                    relation.setData(new String(tmp));
                }
            } catch (AbcException e) {
                throw new PersistanceException(e.getMessage(),e.getStatus(),e.getSinner(),e.getNestedException());
            }

            return relation;
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

            Poll poll = new Poll(obj.getId(),resultSet.getInt(2));
            poll.setText(new String(resultSet.getString(3)));
            poll.setMultiChoice(resultSet.getBoolean(4));
            poll.setCreated(resultSet.getTimestamp(5));
            poll.setClosed(resultSet.getBoolean(6));

            statement = con.prepareStatement("select volba,pocet from data_ankety where anketa=? order by cislo asc");
            statement.setInt(1,obj.getId());

            resultSet = statement.executeQuery();
            List choices = new ArrayList();
            int i = 0;
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
     * @return Server from mysql db
     */
    protected GenericObject loadServer(Server obj) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("select * from server where cislo=?");
            statement.setInt(1,obj.getId());

            ResultSet resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new PersistanceException("Server "+obj.getId()+" nebyl nalezen!",AbcException.DB_NOT_FOUND,obj,null);
            }

            Server server = new Server(resultSet.getInt(1));
            server.setName(resultSet.getString(2));
            server.setUrl(resultSet.getString(3));
            server.setContact(resultSet.getString(4));
            return server;
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * @return link from mysql db
     */
    protected GenericObject loadRights(AccessRights obj) throws PersistanceException, SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("select * from pravo where cislo=?");
            statement.setInt(1,obj.getId());

            ResultSet resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new PersistanceException("Pravo "+obj.getId()+" nebylo nalezen!",AbcException.DB_NOT_FOUND,obj,null);
            }

            AccessRights rights = new AccessRights(obj.getId());
            rights.setAdmin(resultSet.getBoolean(2));

            return rights;
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * updates record in database
     */
    public void update(GenericDataObject obj) throws PersistanceException {
        if ( obj==null ) throw new PersistanceException("Neni mozne ulozit prazdny objekt!",AbcException.DB_INCOMPLETE,null,null);
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = null;
            if ( obj instanceof Category ) {
                statement = con.prepareStatement("update kategorie set data=?,verejny=? where cislo=?");
                statement.setBoolean(2,((Category)obj).isOpen());
                statement.setInt(3,obj.getId());
            } else if ( obj instanceof Record) {
                statement = con.prepareStatement("update zaznam set data=? where cislo=?");
                statement.setInt(2,obj.getId());
            } else {
                statement = con.prepareStatement("update polozka set data=? where cislo=?");
                statement.setInt(2,obj.getId());
            }
            statement.setBytes(1,obj.getDataAsString().getBytes());

            int result = statement.executeUpdate();
            if ( result!=1 ) {
                throw new PersistanceException("Nepodarilo se ulozit zmeny v "+obj.toString()+" do databaze!", AbcException.DB_UPDATE, obj, null);
            }
            ((GenericDataObject)obj).setUpdated(new java.util.Date());
            cache.store(obj);
        } catch (SQLException e) {
            log.error("Nemohu ulozit zmeny v "+obj,e);
            throw new PersistanceException("Nemohu ulozit zmeny v "+obj.toString()+" do databaze!",AbcException.DB_UPDATE,null,e);
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * updates data in database
     */
    public void update(Relation relation) throws PersistanceException {
        if ( relation==null ) throw new PersistanceException("Neni mozne ulozit prazdny objekt!",AbcException.DB_INCOMPLETE,null,null);
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("update relace set typ_predka=?,predek=?,typ_potomka=?,potomek=?,data=? where cislo=?");

            statement.setString(1,getTableId(relation.getParent()));
            statement.setInt(2,relation.getParent().getId());
            statement.setString(3,getTableId(relation.getChild()));
            statement.setInt(4,relation.getChild().getId());

            String tmp = relation.getDataAsString();
            if ( tmp==null || tmp.length()==0 ) {
                statement.setBytes(5,null);
//                statement.setNull(5,);
            } else {
                statement.setBytes(5,tmp.getBytes());
            }
            statement.setInt(6,relation.getId());

            int result = statement.executeUpdate();
            if ( result!=1 ) {
                throw new PersistanceException("Nepodarilo se ulozit zmeny v "+relation.toString()+" do databaze!", AbcException.DB_UPDATE, relation, null);
            }
            // todo get updated field
            cache.store(relation);
        } catch (SQLException e) {
            log.error("Nemohu ulozit zmeny v "+relation);
            throw new PersistanceException("Nemohu ulozit zmeny v "+relation.toString()+" do databaze!",AbcException.DB_UPDATE,relation,e);
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * updates data in database
     */
    public void update(Data data) throws PersistanceException {
        if ( data==null ) throw new PersistanceException("Neni mozne ulozit prazdny objekt!",AbcException.DB_INCOMPLETE,null,null);
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
            cache.store(data);
        } catch (SQLException e) {
            log.error("Nemohu ulozit zmeny v "+data);
            throw new PersistanceException("Nemohu ulozit zmeny v "+data.toString()+" do databaze!",AbcException.DB_UPDATE,data,e);
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * updates link in database
     */
    public void update(Link link) throws PersistanceException {
        if ( link==null ) throw new PersistanceException("Neni mozne ulozit prazdny objekt!",AbcException.DB_INCOMPLETE,null,null);
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
            ((Link)link).setUpdated(new java.util.Date());
            cache.store(link);
        } catch (SQLException e) {
            log.error("Nemohu ulozit zmeny v "+link);
            throw new PersistanceException("Nemohu ulozit zmeny v "+link.toString()+" do databaze!",AbcException.DB_UPDATE,link,e);
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * updates poll in database
     */
    public void update(Poll poll) throws PersistanceException {
        if ( poll==null ) throw new PersistanceException("Neni mozne ulozit prazdny objekt!",AbcException.DB_INCOMPLETE,null,null);
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("update anketa set otazka=?,vice=?,uzavrena=? where cislo=?");
            statement.setString(1,poll.getText());
            statement.setBoolean(2,poll.isMultiChoice());
            statement.setBoolean(3,poll.isClosed());
            statement.setInt(4,poll.getId());

            int result = statement.executeUpdate();
            if ( result!=1 ) {
                throw new PersistanceException("Nepodarilo se ulozit zmeny v "+poll.toString()+" do databaze!", AbcException.DB_UPDATE, poll, null);
            }

            PollChoice[] choices = poll.getChoices();
            if ( choices==null || choices.length<2 ) {
                throw new PersistanceException("Anketa musi mit nejmene dve volby!", AbcException.DB_INCOMPLETE, poll, null);
            }

            statement = con.prepareStatement("update data_ankety set volba=?,pocet=? where cislo=? and anketa=?");
            for (int i = 0; i<choices.length; i++) {
                PollChoice choice = choices[i];
                statement.clearParameters();
                statement.setString(1,choice.getText());
                statement.setInt(2,choice.getCount());
                statement.setInt(3,i);
                statement.setInt(4,poll.getId());
                result = statement.executeUpdate();
            }
            cache.store(poll);
        } catch (SQLException e) {
            log.error("Nemohu ulozit zmeny v "+poll);
            throw new PersistanceException("Nemohu ulozit zmeny v "+poll.toString()+" do databaze!",AbcException.DB_UPDATE,poll,e);
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * updates user in database
     */
    public void update(User user) throws PersistanceException {
        if ( user==null ) throw new PersistanceException("Neni mozne ulozit prazdny objekt!",AbcException.DB_INCOMPLETE,null,null);
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("update uzivatel set login=?,jmeno=?,email=?,heslo=?,data=? where cislo=?");
            statement.setString(1,user.getLogin());
            statement.setString(2,user.getName());
            statement.setString(3,user.getEmail());
            statement.setString(4,user.getPassword());
            statement.setBytes(5,user.getDataAsString().getBytes());
            statement.setInt(6,user.getId());

            int result = statement.executeUpdate();
            if ( result!=1 ) {
                throw new PersistanceException("Nepodarilo se ulozit zmeny v "+user.toString()+" do databaze!", AbcException.DB_UPDATE, user, null);
            }
            cache.store(user);
        } catch (SQLException e) {
            log.error("Nemohu ulozit zmeny v "+user);
            if ( e.getErrorCode()==1062 ) {
                throw new PersistanceException("Prihlasovaci jmeno "+user.getLogin()+" je uz registrovano!", AbcException.DB_DUPLICATE, user, e);
            } else {
                throw new PersistanceException("Nemohu ulozit zmeny v "+user.toString()+" do databaze!",AbcException.DB_UPDATE,user,e);
            }
        } finally {
            releaseSQLConnection(con);
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
}
