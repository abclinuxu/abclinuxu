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
import org.logicalcobwebs.proxool.ProxoolException;

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
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MySqlPersistance.class);

    /** contains URL to database connection */
    String dbUrl = null;
    Cache cache = null;

    static {
        try {
//            Class.forName("org.gjt.mm.mysql.Driver");
            Class.forName("com.mysql.jdbc.Driver");
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

    public void create(GenericObject obj) {
        Connection con = null;
        if ( obj==null ) throw new PersistanceException("Pokus ulozit prazdny objekt!",AbcException.DB_INCOMPLETE);

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
                    throw new PersistanceException("Nepodarilo se vlozit "+obj+" do databaze!", AbcException.DB_INSERT);
                }
                setAutoId(obj,statement);
            }
            obj.setInitialized(true);
            cache.store(obj);
            log.info("Objekt ["+obj+"] ulozen");
        } catch ( SQLException e ) {
            log.error("Nemohu ulozit "+obj+"!",e);
            if ( e.getErrorCode()==1062 ) {
                throw new PersistanceException("Duplikátní údaj!", AbcException.DB_DUPLICATE);
            } else {
                throw new PersistanceException("Nemohu ulozit "+obj,AbcException.DB_INSERT);
            }
        } finally {
            releaseSQLConnection(con);
        }
    }

    public void update(GenericObject obj) {
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

    public void synchronize(GenericObject obj) {
        if ( obj==null ) return;
        GenericObject found = cache.load(obj);
        if ( found==null ) found = findById(obj);

        obj.synchronizeWith(found);
        obj.setInitialized(true);
    }

    public GenericObject findById(GenericObject obj) {
        if ( obj==null ) throw new PersistanceException("Nemuzu hledat prazdny objekt!",AbcException.DB_NOT_FOUND);
        GenericObject result = cache.load(obj);
        if ( result!=null && result.isInitialized() ) return result;

        if ( log.isDebugEnabled() ) log.debug("Hledam podle PK "+obj);
        try {
            result = loadObject(obj);
            if ( result!=null ) result.setInitialized(true);
            cache.store(result);
            return result;
        } catch (SQLException e) {
            log.error("Nemohu nahrat "+obj.toString()+" z databaze!",e);
            throw new PersistanceException("Chyba pri hledani",AbcException.DB_FIND);
        }
    }

    /**
     * @todo It always tries to parseInt, even on string. It catches NumberFormatException
     * to recover! That's terribly slow and ugly!
     */
    public List findByExample(List objects, String relations) {
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
                        log.error("Ruzne objekty v listu objects! Predvolena trida je "+kind+", aktualni je "+obj.getClass());
                        throw new PersistanceException("Chyba pri hledani!",AbcException.WRONG_FORMAT);
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
                    log.error("Nemuzu vytvorit instanci "+kind,e);
                }
            }
            return result;
        } catch ( SQLException e ) {
            StringBuffer sb = new StringBuffer(" Examples: ");
            for (Iterator iter = objects.iterator(); iter.hasNext();) {
                sb.append(((GenericObject) iter.next()).toString());
            }
            log.error("Chyba pri hledani podle prikladu. Relations: "+relations+sb.toString(),e);
            throw new PersistanceException("Nemohu provest zadane vyhledavani!",AbcException.DB_WRONG_COMMAND);
        } finally {
            releaseSQLConnection(con);
        }
    }

    public List findByCommand(String command) {
        Connection con = null;
        if ( command==null || command.length()==0 ) throw new PersistanceException("Nemohu hledat prazdny objekt!",AbcException.DB_INCOMPLETE);
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
            log.error("Chyba pri hledani podle "+command,e);
            throw new PersistanceException("Chyba pri hledani!",AbcException.DB_FIND);
        } finally {
            releaseSQLConnection(con);
        }
        return result;
    }

    /**
     * @todo unit test
     */
    public List findParents(Relation relation) {
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

                String tmp = resultSet.getString(4);
                if ( tmp!=null ) {
                    tmp = insertEncoding(tmp);
                    relation.setData(new String(tmp));
                }

                result.add(0,relation);
            }
            return result;
        } catch ( SQLException e ) {
            log.error("Nepodarilo se zjistit predky pro "+relation,e);
            throw new PersistanceException("Nepodarilo se zjistit predky!",AbcException.DB_FIND);
        } finally {
            releaseSQLConnection(con);
        }
    }

    public Relation[] findByExample(Relation example) {
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

                String tmp = rs.getString(7);
                if ( tmp!=null ) {
                    tmp = insertEncoding(tmp);
                    relation.setData(new String(tmp));
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
            //@todo wouldn't it be better to throw exception?
            return null;
        }
    }

    public void remove(GenericObject obj) {
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
            throw new PersistanceException("Nemohu smazat objekt!",AbcException.DB_REMOVE);
        } finally {
            releaseSQLConnection(con);
        }
    }

    public void incrementCounter(GenericObject obj) {
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
                appendCounterInsertParams(obj,sb,conditions);
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

    public void incrementCounter(PollChoice choice) {
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

    public int getCounterValue(GenericObject obj) {
        Connection con = null;
        try {
            con = getSQLConnection();
            List conditions = new ArrayList();
            StringBuffer sb = new StringBuffer();

            appendCounterSelectParams(obj,sb,conditions);
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

    public void removeCounter(GenericObject obj) {
        Connection con = null;
        try {
            con = getSQLConnection();
            List conditions = new ArrayList();
            StringBuffer sb = new StringBuffer();

            appendCounterDeleteParams(obj,sb,conditions);
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
    private Connection getSQLConnection() {
        try {
            return DriverManager.getConnection(dbUrl);
        } catch (SQLException e) {
            log.fatal("Nemohu se spojit s databazi!",e);
            throw new PersistanceException("Nemohu se spojit s databazi!",AbcException.DB_REFUSED);
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
    private String getTable(GenericObject obj) {
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
            return "anketa";
        } else if (obj instanceof Server) {
            return "server";
        } else if (obj instanceof Data) {
            return "objekt";
        } else if (obj instanceof AccessRights) {
            return "pravo";
        }
        throw new PersistanceException("Nepodporovany typ tridy!",AbcException.DB_UNKNOWN_CLASS);
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
    private GenericObject loadObject(GenericObject obj) throws SQLException {
        if (obj instanceof Relation) {
            return loadRelation((Relation)obj);
        } else if (obj instanceof GenericDataObject) {
            return loadDataObject((GenericDataObject)obj);
        } else if (obj instanceof User) {
            return loadUser((User)obj);
        } else if (obj instanceof Server) {
            return loadServer((Server)obj);
        } else if (obj instanceof Link) {
            return loadLink((Link)obj);
        } else if (obj instanceof Poll) {
            return loadPoll((Poll)obj);
        } else if (obj instanceof Data) {
            return loadData((Data)obj);
        } else if (obj instanceof AccessRights) {
            return loadRights((AccessRights)obj);
        }
        return null;
    }

    /**
     * lookup tree for children of <code>obj</code> with <code>treeId</code> and sets them
     * with <code>obj.setContent()</code> call. Children are not initialized.
     */
    private void findChildren(GenericObject obj, Connection con) throws SQLException {
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

            String tmp = resultSet.getString(5);
            if ( tmp!=null) {
                tmp = insertEncoding(tmp);
                relation.setData(new String(tmp));
            }

            relation.setInitialized(true);
            obj.addContent(relation);
        }
    }

    /**
     * Appends INSERT prepared statement for this object to <code>sb</code> and parameter
     * to <code>conditions</code> for each asterisk in prepared statement.
     */
    private void appendCreateParams(GenericObject obj, StringBuffer sb, List conditions ) {
        if (obj instanceof GenericDataObject) {
            GenericDataObject gdo = (GenericDataObject) obj;
            sb.append("insert into "+getTable(obj)+" values(0,?,?,?,?,now())");
            if ( !(obj instanceof Category) && gdo.getType()==0 ) {
                log.warn("Type not set! "+obj.toString());
            }
            conditions.add(new Integer(gdo.getType()));
            conditions.add(gdo.getDataAsString().getBytes());
            conditions.add(new Integer(gdo.getOwner()));

            java.util.Date d = ( gdo.getCreated()!=null ) ? gdo.getCreated() : new java.util.Date();
            conditions.add(new Timestamp(d.getTime()));

            gdo.setCreated(d);
            gdo.setUpdated(new java.util.Date());

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
    private void appendFindParams(GenericObject obj, StringBuffer sb, List conditions ) {
        boolean addAnd = false;

        if (obj instanceof GenericDataObject) {
            GenericDataObject gdo = (GenericDataObject) obj;

            if ( gdo.getOwner()!=0 ) {
                addAnd = true;
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
            if ( ((Poll)obj).getType()!=0 ) {
                addAnd = true;
                sb.append("typ=?");
                conditions.add(new Integer(((Poll)obj).getType()));
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
    private void appendIncrementUpdateParams(GenericObject obj, StringBuffer sb, List conditions ) {
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
    private void appendCounterInsertParams(GenericObject obj, StringBuffer sb, List conditions ) {
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
    private void appendCounterSelectParams(GenericObject obj, StringBuffer sb, List conditions ) {
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
    private void appendCounterDeleteParams(GenericObject obj, StringBuffer sb, List conditions ) {
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
    protected void storePoll(Poll poll) throws SQLException {
        Connection con = null;

        try {
            PollChoice[] choices = poll.getChoices();
            if ( choices==null || choices.length<2 ) {
                log.error("Anketa musi mit nejmene dve volby!"+poll);
                throw new PersistanceException("Anketa musi mit nejmene dve volby!", AbcException.DB_INCOMPLETE);
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
                log.error("Nepodarilo se vlozit anketu do databaze!"+poll);
                throw new PersistanceException("Nepodarilo se vlozit anketu do databaze!", AbcException.DB_INSERT);
            }

            com.mysql.jdbc.PreparedStatement mm = (com.mysql.jdbc.PreparedStatement)statement;
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
    protected GenericObject loadUser(User obj) throws SQLException {
        Connection con = null;
        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("select * from uzivatel where cislo=?");
            statement.setInt(1,obj.getId());

            ResultSet resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new PersistanceException("Uzivatel "+obj.getId()+" nebyl nalezen!",AbcException.DB_NOT_FOUND);
            }

            User user = new User(obj.getId());
            user.setLogin(resultSet.getString(2));
            user.setName(resultSet.getString(3));
            user.setEmail(resultSet.getString(4));
            user.setPassword(resultSet.getString(5));
            user.setData(insertEncoding(resultSet.getString(6)));

            findChildren(user,con);
            return user;
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * @return item descendant from mysql db
     */
    protected GenericDataObject loadDataObject(GenericDataObject obj) throws SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("select * from "+getTable(obj)+" where cislo=?");
            statement.setInt(1,obj.getId());

            ResultSet resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new PersistanceException("Polozka "+obj.getId()+" nebyla nalezena!",AbcException.DB_NOT_FOUND);
            }

            GenericDataObject item = null;
            if ( obj instanceof Category )
                item = new Category(obj.getId());
            else if ( obj instanceof Item )
                item = new Item(obj.getId());
            else
                item = new Record(obj.getId());

            item.setType(resultSet.getInt(2));

            String tmp = resultSet.getString(3);
            tmp = insertEncoding(tmp);
            item.setData(new String(tmp));

            item.setOwner(resultSet.getInt(4));
            item.setCreated(resultSet.getTimestamp(5));
            item.setUpdated(resultSet.getTimestamp(6));

            findChildren(item,con);
            return item;
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * @return user from mysql db
     */
    protected GenericObject loadRelation(Relation obj) throws SQLException {
        Connection con = null;
        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("select * from relace where cislo=?");
            statement.setInt(1,obj.getId());

            ResultSet resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new PersistanceException("Relace "+obj.getId()+" nebyla nalezena!",AbcException.DB_NOT_FOUND);
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

            String tmp = resultSet.getString(7);
            if ( tmp!=null ) {
                tmp = insertEncoding(tmp);
                relation.setData(new String(tmp));
            }

            return relation;
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * @return data from mysql db
     */
    protected GenericObject loadData(Data obj) throws SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("select * from objekt where cislo=?");
            statement.setInt(1,obj.getId());

            ResultSet resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new PersistanceException("Datovy objekt "+obj.getId()+" nebyl nalezen!",AbcException.DB_NOT_FOUND);
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
    protected GenericObject loadLink(Link obj) throws SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("select * from odkaz where cislo=?");
            statement.setInt(1,obj.getId());

            ResultSet resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new PersistanceException("Odkaz "+obj.getId()+" nebyl nalezen!",AbcException.DB_NOT_FOUND);
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
    protected GenericObject loadPoll(Poll obj) throws SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("select * from anketa where cislo=?");
            statement.setInt(1,obj.getId());

            ResultSet resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new PersistanceException("Anketa "+obj.getId()+" nebyla nalezena!",AbcException.DB_NOT_FOUND);
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
                throw new PersistanceException("Anketa "+obj.getId()+" nema zadne volby!",AbcException.DB_INCOMPLETE);
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
    protected GenericObject loadServer(Server obj) throws SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("select * from server where cislo=?");
            statement.setInt(1,obj.getId());

            ResultSet resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new PersistanceException("Server "+obj.getId()+" nebyl nalezen!",AbcException.DB_NOT_FOUND);
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
    protected GenericObject loadRights(AccessRights obj) throws SQLException {
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("select * from pravo where cislo=?");
            statement.setInt(1,obj.getId());

            ResultSet resultSet = statement.executeQuery();
            if ( !resultSet.next() ) {
                throw new PersistanceException("Pravo "+obj.getId()+" nebylo nalezen!",AbcException.DB_NOT_FOUND);
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
    public void update(GenericDataObject obj) {
        if ( obj==null ) throw new PersistanceException("Neni mozne ulozit prazdny objekt!",AbcException.DB_INCOMPLETE);
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = null;

            statement = con.prepareStatement("update "+getTable(obj)+" set typ=?,data=?,pridal=?,vytvoreno=? where cislo=?");
            statement.setInt(1,obj.getType());
            statement.setBytes(2,obj.getDataAsString().getBytes());
            statement.setInt(3,obj.getOwner());
            statement.setTimestamp(4,new Timestamp(obj.getCreated().getTime()));
            statement.setInt(5,obj.getId());

            int result = statement.executeUpdate();
            if ( result!=1 ) {
                throw new PersistanceException("Nepodarilo se ulozit zmeny v "+obj.toString()+" do databaze!", AbcException.DB_UPDATE);
            }

            obj.setUpdated(new java.util.Date());
            cache.store(obj);
        } catch (SQLException e) {
            log.error("Nemohu ulozit zmeny v "+obj,e);
            throw new PersistanceException("Nemohu ulozit zmeny do databaze!",AbcException.DB_UPDATE);
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * updates data in database
     */
    public void update(Relation relation) {
        if ( relation==null ) throw new PersistanceException("Neni mozne ulozit prazdny objekt!",AbcException.DB_INCOMPLETE);
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("update relace set typ_predka=?,predek=?,typ_potomka=?,potomek=?,data=?,predchozi=? where cislo=?");

            statement.setString(1,getTableId(relation.getParent()));
            statement.setInt(2,relation.getParent().getId());
            statement.setString(3,getTableId(relation.getChild()));
            statement.setInt(4,relation.getChild().getId());

            String tmp = relation.getDataAsString();
            if ( tmp==null || tmp.length()==0 ) {
                statement.setBytes(5,null);
            } else {
                statement.setBytes(5,tmp.getBytes());
            }
            statement.setInt(6,relation.getUpper());
            statement.setInt(7,relation.getId());

            int result = statement.executeUpdate();
            if ( result!=1 ) {
                throw new PersistanceException("Nepodarilo se ulozit zmeny v "+relation.toString()+" do databaze!", AbcException.DB_UPDATE);
            }

            cache.store(relation);
        } catch (SQLException e) {
            log.error("Nemohu ulozit zmeny v "+relation,e);
            throw new PersistanceException("Nemohu ulozit zmeny do databaze!",AbcException.DB_UPDATE);
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * updates data in database
     */
    public void update(Data data) {
        if ( data==null ) throw new PersistanceException("Neni mozne ulozit prazdny objekt!",AbcException.DB_INCOMPLETE);
        Connection con = null;

        try {
            con = getSQLConnection();
            PreparedStatement statement = con.prepareStatement("update objekt set data=?,format=? where cislo=?");
            statement.setBytes(1,data.getData());
            statement.setString(2,data.getFormat());
            statement.setInt(3,data.getId());

            int result = statement.executeUpdate();
            if ( result!=1 ) {
                throw new PersistanceException("Nepodarilo se ulozit zmeny v "+data.toString()+" do databaze!", AbcException.DB_UPDATE);
            }

            cache.store(data);
        } catch (SQLException e) {
            log.error("Nemohu ulozit zmeny v "+data,e);
            throw new PersistanceException("Nemohu ulozit zmeny do databaze!",AbcException.DB_UPDATE);
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * updates link in database
     */
    public void update(Link link) {
        if ( link==null ) throw new PersistanceException("Neni mozne ulozit prazdny objekt!",AbcException.DB_INCOMPLETE);
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
                throw new PersistanceException("Nepodarilo se ulozit zmeny v "+link.toString()+" do databaze!", AbcException.DB_UPDATE);
            }

            ((Link)link).setUpdated(new java.util.Date());
            cache.store(link);
        } catch (SQLException e) {
            log.error("Nemohu ulozit zmeny v "+link,e);
            throw new PersistanceException("Nemohu ulozit zmeny v "+link.toString()+" do databaze!",AbcException.DB_UPDATE);
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * updates poll in database
     */
    public void update(Poll poll) {
        if ( poll==null ) throw new PersistanceException("Neni mozne ulozit prazdny objekt!",AbcException.DB_INCOMPLETE);
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
                throw new PersistanceException("Nepodarilo se ulozit zmeny v "+poll.toString()+" do databaze!", AbcException.DB_UPDATE);
            }

            PollChoice[] choices = poll.getChoices();
            if ( choices==null || choices.length<2 ) {
                log.error("Anketa musi mit nejmene dve volby!"+poll);
                throw new PersistanceException("Anketa musi mit nejmene dve volby!", AbcException.DB_INCOMPLETE);
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
            log.error("Nemohu ulozit zmeny v "+poll,e);
            throw new PersistanceException("Nemohu ulozit zmeny v "+poll.toString()+" do databaze!",AbcException.DB_UPDATE);
        } finally {
            releaseSQLConnection(con);
        }
    }

    /**
     * updates user in database
     */
    public void update(User user) {
        if ( user==null ) throw new PersistanceException("Neni mozne ulozit prazdny objekt!",AbcException.DB_INCOMPLETE);
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
                throw new PersistanceException("Nepodarilo se ulozit zmeny v "+user.toString()+" do databaze!", AbcException.DB_UPDATE);
            }

            cache.store(user);
        } catch (SQLException e) {
            log.error("Nemohu ulozit zmeny v "+user,e);
            if ( e.getErrorCode()==1062 ) {
                throw new PersistanceException("Prihlasovaci jmeno "+user.getLogin()+" je uz registrovano!", AbcException.DB_DUPLICATE);
            } else {
                throw new PersistanceException("Nemohu ulozit zmeny do databaze!",AbcException.DB_UPDATE);
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

    /**
     * Sets id to object, which has been autoincremented.
     */
    private void setAutoId(GenericObject obj, Statement statement) {
        try {
            statement = org.logicalcobwebs.proxool.ProxoolFacade.getDelegateStatement(statement);
            if ( statement instanceof com.mysql.jdbc.PreparedStatement ) {
                com.mysql.jdbc.PreparedStatement mm = (com.mysql.jdbc.PreparedStatement) statement;
                obj.setId((int)mm.getLastInsertID());
            }
        } catch (ProxoolException e) {
            log.error("Proxool cannot get delegated statement from "+statement, e);
        }
    }
}
