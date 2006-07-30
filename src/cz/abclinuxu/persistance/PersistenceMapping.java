package cz.abclinuxu.persistance;

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
        } else if (type == 'O') {
            return new Data(id);
        }
        throw new InvalidDataException("Nepodporovany typ tridy "+type+"!");
    }

    /**
     * @return Identification of table in the tree
     */
    public static String getGenericObjectType(GenericObject obj) {
        if (obj instanceof Record) {
            return "Z";
        } else if (obj instanceof Item) {
            return "P";
        } else if (obj instanceof Category) {
            return "K";
        } else if (obj instanceof User) {
            return "U";
        } else if (obj instanceof Link) {
            return "L";
        } else if (obj instanceof Server) {
            return "S";
        } else if (obj instanceof Poll) {
            return "A";
        } else if (obj instanceof Data) {
            return "O";
        }
        throw new InvalidDataException("Nepodporovany typ tridy!");
    }
}
