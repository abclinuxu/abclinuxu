package cz.abclinuxu.utils;

import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Attribute;
import org.dom4j.DocumentHelper;
import org.apache.log4j.Logger;

import java.util.Date;

/**
 * Methods to simplify work with XML.
 * @author kapy & leos
 *
 */
public class XmlUtils {
    protected static final Logger log = Logger.getLogger(XmlUtils.class);

    /**
     * Extracts value from element or attribute selected by given xpath starting at element.
     * Purpose of this method is to simplify readability of source code, because it handles
     * situation that xpath matched nothing.
     *
     * @param element starting element
     * @param xpath   xpath expression
     * @return value of matched node or null
     */
    public static String getNodeText(Node root, String xpath) {
		Node node = root.selectSingleNode(xpath);
		if (node != null)
		    return node.getText();

		return null;
	}

    /**
	 * Parses XML chunk into atomic type in safe manner
	 *
	 * @param root XPath context root
	 * @param xpath XPath expression to be evaluated on root
	 * @return Integer value with result or {@code null}
	 */
	public static Integer getNodeInt(Node root, String xpath) {
        String value = getNodeText(root, xpath);
        if (value == null)
            return null;

        try {
			return Integer.valueOf(value);
		}
		catch (NumberFormatException e) {
            log.warn("Error parsing " + value + " from xpath " + xpath, e);
			return null;
		}
	}

	/**
	 * Parses XML chunk into atomic type in safe manner
	 *
	 * @param root XPath context root
	 * @param xpath XPath expression to be evaluated on root
	 * @return Double value with result or {@code null}
	 */
	public static Double getNodeDouble(Node root, String xpath) {
        String value = getNodeText(root, xpath);
        if (value == null)
            return null;

        try {
			return Double.valueOf(value);
		}
		catch (NumberFormatException e) {
            log.warn("Error parsing " + value + " from xpath " + xpath, e);
            return null;
		}
	}

	/**
	 * Parses XML chunk into atomic type in safe manner.
	 * Accepts values {@code true} or {@code false}
	 *
	 * @param root XPath context root
	 * @param xpath XPath expression to be evaluated on root
	 * @return Boolean value with result or {@code null}
	 */
	public static boolean getNodeBoolean(Node root, String xpath) {
		String value = getNodeText(root, xpath);
		if (value == null)
            return false;
		return Boolean.valueOf(value);
	}

    /**
     * Extracts boolean value from element or attribute selected by given xpath starting at element.
     * When xpath does not match anything, defaultValue is returned.
     *
     * @param element      starting element
     * @param xpath        xpath expression
     * @param defaultValue value to be returned when nothing is matched
     * @return value of matched node or defaultValue
     */
    public static Boolean getNodeSetting(Node element, String xpath, Boolean defaultValue) {
        Node node = element.selectSingleNode(xpath);
        if (node != null)
            return "yes".equalsIgnoreCase(node.getText());
        return defaultValue;
    }

    /**
	 * Parses XML chunk into atomic type in safe manner.
	 * Accepts values in number of milliseconds
	 *
	 * @param root XPath context root
	 * @param xpath XPath expression to be evaluated on root
	 * @return Date or {@code null}
	 */
	public static Date getNodeDate(Node root, String xpath) {
        String value = getNodeText(root, xpath);
        if (value == null)
            return null;

        try {
			return new Date(Long.valueOf(value));
		} catch (NumberFormatException e) {
            log.warn("Error parsing " + value + " from xpath " + xpath, e);
            return null;
		}
	}

	/**
	 * Transforms object to boolean value
	 * @param object Object to be checked
	 * @return {@code true} if object equals 1, {@code false} otherwise
	 */
	static boolean booleanValue(Object object) {
		return (object != null && object.equals(1));
	}

    /**
     * Sets value to existing or creates new attribute.
     * @param element parent element
     * @param name attribute name
     * @param value desired value
     */
    public static void setAttribute(Element element, String name, String value) {
        Attribute attribute = element.attribute(name);
        if (attribute != null)
            attribute.setText(value);
        else {
            attribute = DocumentHelper.createAttribute(element, name, value);
            element.add(attribute);
        }
    }
}
