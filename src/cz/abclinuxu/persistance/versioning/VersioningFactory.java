package cz.abclinuxu.persistance.versioning;

import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;

import java.util.prefs.Preferences;

/**
 * Factory that provides Versioning service.
 * User: literakl
 * Date: 27.3.2005
 */
public final class VersioningFactory implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VersioningFactory.class);

    public static final String PREF_IMPLEMENTING_CLASS = "impl.class";

    static {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(new VersioningFactory());
    }

    private static Versioning instance;

    /**
     * @return implementation of versioning
     */
    public static Versioning getVersioning() {
        return instance;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        String className = prefs.get(PREF_IMPLEMENTING_CLASS, "cz.abclinuxu.persistance.versioning.MysqlVersioningProvider");
        log.info("VersioningFactory will use the class "+className+".");
        try {
            Class aClass = Class.forName(className);
            if (!aClass.isAssignableFrom(Versioning.class))
                throw new ConfigurationException("Class '" + className + "' does not implement Versioning!");
            instance = (Versioning) aClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Class '" +className+"' was not found. Please specify correct implementation of Versioning!");
        } catch (InstantiationException e) {
            throw new ConfigurationException("Class '" + className + "' cannot be instantiated. Please specify correct implementation of Versioning!");
        } catch (IllegalAccessException e) {
            throw new ConfigurationException("Class '" + className + "' is not public. Please specify correct implementation of Versioning!");
        }
    }
}
