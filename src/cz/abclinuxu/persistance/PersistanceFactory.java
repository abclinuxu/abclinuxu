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
    static String defaultUrl = "jdbc:mysql://localhost/abc?user=literakl&password=lkaretil&useUnicode=true&characterEncoding=ISO-8859-2";

    static {
        instances = new HashMap();
    }

    /**
     * Get default persistance.
     * @return instance of object, which implements <code>Persistance</code>.
     */
    public static Persistance getPersistance() {
        return getPersistance(defaultUrl);
    }

    /**
     * @return instance of object, which implements <code>Persistance</code>
     * and is described by <code>url</code>. If <code>url</code> is null,
     * <code>defaultUrl</code> is used.
     */
    public static Persistance getPersistance(String url) {
        if ( url==null ) url = defaultUrl;
        Persistance persistance = (Persistance) instances.get(url);
        if ( persistance==null ) {
            persistance = new MySqlPersistance(url);
            persistance.setCache(new Cache());
            instances.put(url,persistance);
        }
        return persistance;
    }

    /**
     * Sets default persistance URL.
     */
    public static void setDefaultUrl(String defaultUrl) {
        PersistanceFactory.defaultUrl = defaultUrl;
    }
}
