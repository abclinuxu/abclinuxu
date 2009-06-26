package cz.abclinuxu.utils;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.XMLHandler;
import cz.abclinuxu.data.view.Author;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Transforms persistence layer object into JavaBeans used in application
 *
 * @author kapy
 */
public class BeanFetcher {
    private static final Logger log = Logger.getLogger(BeanFetcher.class);

    /**
     * Determines how data are fetched into bean
     *
     * @author kapy
     */
    public static enum FetchType {
        /**
         * All data are fetched, including XML parsing
         */
        EAGER,
        /**
         * Only data directly accessible from columns of SQL table are fetched,
         * XML parsing is ommited
         */
        OMIT_XML
    }

    // Disable instantiation
    private BeanFetcher() {
    }

    /**
     * Creates author JavaBean object from data fetched out of persistence layer
     *
     * @param item Record from persistence layer
     * @param ft
     * @return Author object created from provided data
     */
    public static Author fetchAuthorFromItem(Item item, FetchType ft) {

        if (item == null)
            return null;

        Author author = new Author();

        author.setId(item.getId());
        author.setUid(item.getNumeric1());

        boolean active = (item.getNumeric2() != null && item.getNumeric2().equals(1)) ? true : false;
        author.setActive(active);
        author.setName(item.getString1());
        author.setSurname(item.getString2());

        // rights
        author.setOwner(item.getOwner());
        author.setPermissions(item.getPermissions());
        author.setGroup(item.getGroup());

        switch (ft) {
            case OMIT_XML:
                return author;
            case EAGER:
                Element root = item.getData().getRootElement();
                return fillXMLProperties(author, root);
            default:
                throw new UnsupportedOperationException("Unable to fetch data with FetchType " + ft.toString());
        }
    }

    public static List<Author> fetchAuthorsFromItems(List<Item> list, FetchType ft) {
        List<Author> authors = new ArrayList<Author>(list.size());
        for (Item i : list) {
            authors.add(fetchAuthorFromItem(i, ft));
        }
        return authors;
    }

    /**
     * Creates author from array of objects. Expects object at specified positions
     * <ol>
     * <li>cislo</li>
     * <li>numeric1</li>
     * <li>numeric2</li>
     * <li>string1</li>
     * <li>string2</li>
     * <li>data</li>
     * <li>articleCount</li>
     * <li>lastArticleDate</li>
     * <li>owner</li>
     * <li>group</li>
     * <li>permissions</li>
     * </ol>
     *
     * @param objects Array of objects
     * @param ft      Fetch type
     * @return Created author object
     */
    public static Author fetchAuthorFromObjects(Object[] objects, FetchType ft) {
        // ! exact object length must be passed !
        if (objects == null || objects.length != 11)
            return null;

        Author author = new Author();
        try {
            author.setId((Integer) objects[0]);
            author.setUid((Integer) objects[1]);
            boolean active = (objects[2] != null && objects[2].equals(1)) ? true : false;
            author.setActive(active);
            author.setName((String) objects[3]);
            author.setSurname((String) objects[4]);
            author.setArticleCount(((Long) objects[6]).intValue());

            java.sql.Timestamp date = (java.sql.Timestamp) objects[7];
            if (date != null)
                author.setLastArticleDate(new Date(date.getTime()));

            author.setOwner((Integer) objects[8]);
            author.setGroup((Integer) objects[9]);
            author.setPermissions((Integer) objects[10]);

            switch (ft) {
                case OMIT_XML:
                    return author;
                case EAGER:
                    XMLHandler handler = new XMLHandler((String) objects[5]);
                    Element root = handler.getData().getRootElement();
                    return fillXMLProperties(author, root);
                default:
                    throw new UnsupportedOperationException("Unable to fetch data with FetchType " + ft.toString());
            }

        } catch (ClassCastException cce) {
            log.warn("Unable to create author from array of objects", cce);
        }

        return null;
    }

    public static List<Author> fetchAuthorsFromObjects(List<Object[]> list, FetchType ft) {
        List<Author> authors = new ArrayList<Author>(list.size());
        for (Object[] objects : list) {
            authors.add(fetchAuthorFromObjects(objects, ft));
        }
        return authors;
    }

    private static Author fillXMLProperties(Author author, Element root) {
        author.setAbout(safeRetrieveElementText(root, "/data/about"));
        author.setAddress(safeRetrieveElementText(root, "/data/address"));
        author.setAccountNumber(safeRetrieveElementText(root, "/data/accountNumber"));
        author.setBirthNumber(safeRetrieveElementText(root, "/data/birthNumber"));
        author.setEmail(safeRetrieveElementText(root, "/data/email"));
        author.setLogin(safeRetrieveElementText(root, "/data/login"));
        author.setNickname(safeRetrieveElementText(root, "/data/nickname"));
        author.setPhone(safeRetrieveElementText(root, "/data/phone"));
        author.setPhotoUrl(safeRetrieveElementText(root, "/data/photourl"));
        return author;
    }

    private static String safeRetrieveElementText(Element root, String xpath) {

        Node node = root.selectSingleNode(xpath);
        if (node != null)
            return node.getText();

        return null;

    }

}
