/*
 * Created by IntelliJ IDEA.
 * User: literakl
 * Date: Jan 8, 2002
 * Time: 9:42:15 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package cz.abclinuxu.persistance;

import cz.abclinuxu.data.*;
import cz.abclinuxu.AbcException;

public class Polymorphism {
    boolean rekurze;

    public Polymorphism() {
        rekurze = false;
    }

    public void test(User user) {
        System.out.println("User");
    };
    public void test(GenericDataObject object) {
        System.out.println("data object");
    };
    public void test(Link link){
        System.out.println("link");
    };
    public void test(Poll poll){
        System.out.println("poll");
    };
    public void test(Relation relation){
        System.out.println("relation");
    };
    public void test(Server server){
        System.out.println("server");
    };

    public void test(GenericObject object) {
        System.out.println("generic object");
    }

    public static void main(String[] args) throws Exception {
        Polymorphism test = new Polymorphism();
        User user = new User();
        test.test(user);
        System.out.println(test.getTable(user));
        System.out.println(test.trest(user));
    }

    public String trest(GenericObject obj) throws Exception{
        return getTable(obj);
    }


    public String getTable(GenericObject obj) throws PersistanceException { return "generic";}
    public String getTable(Record obj) throws PersistanceException   { return "zaznam";}
    public String getTable(Item obj) throws PersistanceException     { return "polozka";}
    public String getTable(Category obj) throws PersistanceException { return "kategorie";}
    public String getTable(Relation obj) throws PersistanceException { return "strom";}
    public String getTable(Data obj) throws PersistanceException     { return "objekt";}
    public String getTable(Link obj) throws PersistanceException     { return "odkaz";}
    public String getTable(Poll obj) throws PersistanceException     { return "anketa";}
    public String getTable(User obj) throws PersistanceException     { return "uzivatel";}
    public String getTable(Server obj) throws PersistanceException   { return "server";}
    public String getTable(AccessRights obj) throws PersistanceException { return "pravo";}
}
