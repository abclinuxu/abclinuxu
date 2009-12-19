package cz.abclinuxu.utils;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import java.util.Date;

/**
 * Helper for construction of DOM tree
 *
 * @author kapy
 *
 */
public class DocumentBuilder {

    private Document doc;

    /**
     * Constructs new DocumentBuilder using either document root or creating
     * its own with provided qname
     *
     * @param doc Document element
     * @param rootQName Root element qualified name
     */
    public DocumentBuilder(Document doc, String rootQName) {
        if (doc == null) doc = DocumentHelper.createDocument(DocumentHelper.createElement(rootQName));
        this.doc = doc;
    }

    /**
     * Appends/updates/detaches element at given path depending on value
     * passed
     *
     * @param xpath XPath locator in document
     * @param value Value passed for DOM modification
     * @return Modified instance
     */
    public DocumentBuilder store(String xpath, Object value) {
        Node node = doc.selectSingleNode(xpath);
        // node will be detached, no value provided
        if (node != null && (value == null || Misc.empty(value.toString())))
            node.detach();
        // omit empty value
        else if (value == null || Misc.empty(value.toString()))
            return this;
        // change text value
        else {
            if (node == null) node = DocumentHelper.makeElement(doc, xpath);
            node.setText(Misc.filterDangerousCharacters(value.toString()));
        }
        return this;
    }

    /**
     * Appends/updates/detaches element at given path depending on value
     * passed
     *
     * @param xpath XPath locator in document
     * @param value Value passed for DOM modification
     * @return Modified instance
     */
    public DocumentBuilder store(String xpath, boolean value) {
        if (value)
            store(xpath, "true");
        else
            store(xpath, "false");
        return this;
    }

    /**
     * Appends/updates/detaches element at given path depending on value
     * passed
     *
     * @param xpath XPath locator in document
     * @param value Value passed for DOM modification
     * @return Modified instance
     */
    public DocumentBuilder store(String xpath, Date value) {
        if (value != null)
            store(xpath, Long.toString(value.getTime()));
        else
            store(xpath, (Object) null);
        return this;
    }

    /**
     * Returns document
     *
     * @return the document
     */
    public Document getDocument() {
        return doc;
    }

    /**
     * Returns document flattered to text
     *
     * @return XML representation of content
     */
    @Override
    public String toString() {
        return doc.toString();
    }

}
