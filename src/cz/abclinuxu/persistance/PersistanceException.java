/*
 * User: literakl
 * Date: Nov 17, 2001
 * Time: 8:27:09 AM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.persistance;

import org.apache.log4j.Category;

/**
 * Exception related to persitance
 */
public class PersistanceException extends Exception {

    static Category log = Category.getInstance(PersistanceException.class);

    public PersistanceException(String s, Exception e) {
        super(s);
        log.error("Caught an exception: "+s,e);
    }

    public PersistanceException(String s) {
        super(s);
        log.error("Caught an exception: "+s,this);
    }
}
