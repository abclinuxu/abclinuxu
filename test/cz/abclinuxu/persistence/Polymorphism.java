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
package cz.abclinuxu.persistence;

import cz.abclinuxu.data.*;
import cz.abclinuxu.exceptions.PersistenceException;

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


    public String getTable(GenericObject obj) throws PersistenceException { return "generic";}
    public String getTable(Record obj) throws PersistenceException   { return "zaznam";}
    public String getTable(Item obj) throws PersistenceException     { return "polozka";}
    public String getTable(Category obj) throws PersistenceException { return "kategorie";}
    public String getTable(Relation obj) throws PersistenceException { return "strom";}
    public String getTable(Data obj) throws PersistenceException     { return "objekt";}
    public String getTable(Link obj) throws PersistenceException     { return "odkaz";}
    public String getTable(Poll obj) throws PersistenceException     { return "anketa";}
    public String getTable(User obj) throws PersistenceException     { return "uzivatel";}
    public String getTable(Server obj) throws PersistenceException   { return "server";}
}
