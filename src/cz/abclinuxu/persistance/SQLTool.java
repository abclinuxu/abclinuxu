/*
 * User: Leos Literak
 * Date: Jun 21, 2003
 * Time: 11:42:59 AM
 */
package cz.abclinuxu.persistance;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.data.Item;

import java.util.prefs.Preferences;
import java.util.Date;
import java.util.List;

/**
 * Thread-safe singleton, that encapsulates SQL commands
 * used outside of Persistance implementations.
 */
public final class SQLTool implements Configurable {

    public static final String PREF_MAX_RECORD_CREATED_OF_ITEM = "max.record.created.of.item";
    private static final String DEFAULT_MAX_RECORD_CREATED_OF_ITEM = "select max(vytvoreno) from relace R left join zaznam Z on Z.cislo=R.potomek where R.typ_predka='P' and R.predek=";

    private static SQLTool singleton;

    static {
        singleton = new SQLTool();
        ConfigurationManager.getConfigurator().configureMe(singleton);
    }

    private String sqlMaxRecordCreatedOfItem;


    /**
     * Returns singleton of SQLTool.
     */
    public static SQLTool getInstance() {
        return singleton;
    }

    /**
     * Finds maximum value of created property of records belonging to given item.
     * If the item doesn't have any associated records, its created property is
     * returned. Argument shall be initialized.
     */
    public Date getMaxCreatedDateOfRecordForItem(Item item) {
        if ( ! item.isInitialized() )
            throw new IllegalStateException("Item is not initialized!");
        Persistance persistance = PersistanceFactory.getPersistance();
        List objects = persistance.findByCommand(sqlMaxRecordCreatedOfItem+item.getId());
        java.sql.Timestamp max = (java.sql.Timestamp) ((Object[])objects.get(0))[0];
        if ( max==null )
            return item.getCreated();
        else
            return new Date(max.getTime());
    }

    /**
     * Private constructor
     */
    private SQLTool() {
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        sqlMaxRecordCreatedOfItem = prefs.get(PREF_MAX_RECORD_CREATED_OF_ITEM,DEFAULT_MAX_RECORD_CREATED_OF_ITEM);
    }
}
