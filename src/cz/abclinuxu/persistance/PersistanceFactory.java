/*
 * User: literakl
 * Date: Nov 17, 2001
 * Time: 8:21:33 AM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.persistance;

/**
 * Factory, which select Persistance class
 */
public class PersistanceFactory {

    /**
     * @return instance of object, which implements <code>Persistance</code>
     */
    public static Persistance getPersistance() {
        return new MySqlPersistance();
    }

    /**
     * @return instance of object, which implements <code>Persistance</code>
     * and is described by <code>url</code>
     */
    public static Persistance getPersistance(String url) {
        return new MySqlPersistance(url);
    }
}
