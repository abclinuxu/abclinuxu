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
        if ( rekurze ) {
            System.out.println("REKURZE!");
            return;
        }
        rekurze = true;
        test(object);
    }

    public static void main(String[] args) {
        Polymorphism test = new Polymorphism();
        User user = new User();
        test.test(user);
    }
}
