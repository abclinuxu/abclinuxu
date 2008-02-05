package cz.abclinuxu.persistence;

import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Record;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Poll;
import cz.abclinuxu.data.Link;
import cz.abclinuxu.data.Server;
import cz.abclinuxu.data.Data;
import cz.abclinuxu.exceptions.InvalidDataException;

/**
 * Utility class that provides few methods about mapping various
 * objects to database. The main purpose is to separate it from
 * persistence implementation, so it canbe used independantly.
 */
public class PersistenceMapping {
    public static final String TREE_ITEM = "P";
    public static final String TREE_RECORD = "Z";
    public static final String TREE_CATEGORY = "K";
    public static final String TREE_DATA = "D";
    public static final String TREE_POLL = "A";
    public static final String TREE_SERVER = "S";
    public static final String TREE_LINK = "L";
    public static final String TREE_USER = "U";

    /** enumaration of available tables */
    public static class Table {
        public static final Table RELATION = new Table("RELATION");
        public static final Table ITEM = new Table("ITEM");
        public static final Table RECORD = new Table("RECORD");
        public static final Table CATEGORY = new Table("CATEGORY");
        public static final Table DATA = new Table("DATA");
        public static final Table POLL = new Table("POLL");
        public static final Table SERVER = new Table("SERVER");
        public static final Table LINK = new Table("LINK");
        public static final Table USER = new Table("USER");
        public static final Table FEATURE = new Table("FEATURE");
        public static final Table COUNTER = new Table("COUNTER");
        public static final Table STATISTICS = new Table("STATISTICS");
        public static final Table ACTION = new Table("ACTION");
        public static final Table VERSION = new Table("VERSION");
        public static final Table COMMENT = new Table("COMMENT");
        public static final Table LAST_SEEN_COMMENT = new Table("LAST_SEEN_COMMENT");

        private final String myName; // for debug only

        private Table(String name) {
            myName = name;
        }

        public String toString() {
            return myName;
        }
    }

    /**
     * instantiates new GenericObject, which class is specified by <code>type</code> and
     * with desired <code>id</code>.
     * @param type constant for GenericObject subclass as defined in getGenericObjectType()
     * @param id id to be passed to constructor
     * @return new instance of GenericObject subclass with preset id
     */
    public static GenericObject createGenericObject(char type, int id) {
        if (type == 'K') {
            return new Category(id);
        } else if (type == 'P') {
            return new Item(id);
        } else if (type == 'Z') {
            return new Record(id);
        } else if (type == 'U') {
            return new User(id);
        } else if (type == 'A') {
            return new Poll(id);
        } else if (type == 'L') {
            return new Link(id);
        } else if (type == 'S') {
            return new Server(id);
        } else if (type == 'D') {
            return new Data(id);
        }
        throw new InvalidDataException("Nepodporovany typ tridy "+type+"!");
    }

    /**
     * @return Identification of table in the tree
     */
    public static String getGenericObjectType(GenericObject obj) {
        if (obj instanceof Record) {
            return TREE_RECORD;
        } else if (obj instanceof Item) {
            return TREE_ITEM;
        } else if (obj instanceof Category) {
            return TREE_CATEGORY;
        } else if (obj instanceof User) {
            return TREE_USER;
        } else if (obj instanceof Link) {
            return TREE_LINK;
        } else if (obj instanceof Server) {
            return TREE_SERVER;
        } else if (obj instanceof Poll) {
            return TREE_POLL;
        } else if (obj instanceof Data) {
            return TREE_DATA;
        }
        throw new InvalidDataException("Nepodporovany typ tridy!");
    }
}
