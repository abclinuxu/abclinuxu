/*
 * User: Leos Literak
 * Date: Jun 2, 2003
 * Time: 4:25:09 PM
 */
package cz.abclinuxu.utils.config;

/**
 * Interface, that configures objects. Configurator can work in two modes: automatically
 * and upon request.<p>
 * At initialization time, it scans KEY_AUTOCONFIG top level node for presence of KEY_NODES.
 * If it is there, its value is tokenized by whitespace or coma to absolute names og node,
 * that shall be autoconfigured. These nodes are loaded and if they contain KEY_AUTOCONFIG,
 * its value is instantiated. If instantiated object implements Configurable,
 * than configure() with current node is called. Of course, public no argument
 * constructor is required.<p>
 * Second approach requires caller to get Configurator from
 * ConfigurationManager and then call configureMe() with class to be configured as an argument.
 */
public interface Configurator {
    /** Specifies fully qualified class name, that shall be instantiated and configured */
    public static final String KEY_AUTOCONFIG = "autoConfig";
    /** Specifies nodes, that shall be autoconfigured */
    public static final String KEY_NODES = "nodes";

    /**
     * Loads preferences for argument and calls configurable.configure().
     */
    public void configureMe(Configurable configurable) throws ConfigurationException;
}
