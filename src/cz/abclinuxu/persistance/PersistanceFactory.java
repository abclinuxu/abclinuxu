/*
 * User: literakl
 * Date: Nov 17, 2001
 * Time: 8:21:33 AM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.persistance;

import java.util.Map;
import java.util.HashMap;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Factory, which select Persistance class
 */
public class PersistanceFactory {
    static Map instances;

    static {
        instances = new HashMap();
    }

    /**
     * @return instance of object, which implements <code>Persistance</code>
     */
    public static Persistance getPersistance() {
        Persistance persistance = (Persistance) instances.get(null);
        if ( persistance==null ) {
            persistance = new MySqlPersistance();
            instances.put(null,persistance);
        }
        return persistance;
    }

    /**
     * @return instance of object, which implements <code>Persistance</code>
     * and is described by <code>url</code>
     */
    public static Persistance getPersistance(String url) {
        Persistance persistance = (Persistance) instances.get(url);
        if ( persistance==null ) {
            persistance = new MySqlPersistance(url);
            instances.put(url,persistance);
        }
        return persistance;
    }
}
