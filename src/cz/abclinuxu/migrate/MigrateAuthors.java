package cz.abclinuxu.migrate;

import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import org.dom4j.Element;
import org.dom4j.Node;

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.exceptions.InvalidDataException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.security.Permissions;
import cz.abclinuxu.servlets.Constants;

/**
 * Migrates author items to new storage format. No relation is modified, because
 * only storage format differs, so items are just updated. Author permissions
 * are modified. Permissions of Category author is also modified
 * 
 * Old storage format: numeric1 - user id string1 - user login data - rest in
 * XML format
 * 
 * New storage format:
 * 
 * numeric1 - user id numeric2 - activity string1 - first name string2 - surname
 * data - rest in XML format
 * 
 * 
 * 
 * @author kapy
 * 
 */
public class MigrateAuthors {

    private static final int MONTHS_TO_SET_INACTIVE = 9;
    private static final int AUTHORS_PERMISSIONS = Permissions.computePermissions(
	new Permissions(Permissions.PERMISSION_MODIFY),
	new Permissions(Permissions.PERMISSION_CREATE | Permissions.PERMISSION_DELETE
	    | Permissions.PERMISSION_MODIFY), Permissions.NO_PERMISSIONS);

    private SQLTool sqlTool;
    private Persistence persistence;

    /**
     * Creates instance of this migration script
     */
    public MigrateAuthors() {
	this.sqlTool = SQLTool.getInstance();
	this.persistence = PersistenceFactory.getPersistence();
    }

    /**
     * Performs migration task
     * 
     * @throws Exception If anything goes wrong
     */
    public void run() throws Exception {

	int total = sqlTool.countItemsWithType(Item.AUTHOR);
	int i = 0;
	while (i < total) {
	    List<Item> authors = sqlTool.findItemsWithType(Item.AUTHOR, i, 100);
	    i += authors.size();
	    for (Item item : authors) {
		processAuthor(item);
	    }
	}

	System.out.println("Migrated " + i + " authors");

	System.out.println("Changing author group default permissions");
	Relation parent = (Relation) persistence.findById(new Relation(Constants.REL_AUTHORS));
	persistence.synchronize(parent);
	persistence.synchronize(parent.getChild());

	Category cat = (Category) parent.getChild();
	cat.setPermissions(AUTHORS_PERMISSIONS);

	persistence.update(cat);

    }

    private void processAuthor(Item item) throws Exception {
	Item author = (Item) persistence.findById(item);
	Element xmlRoot = author.getData().getRootElement();
	// activity
	List<Relation> children = persistence.findRelationsFor(item);
	int authorId = children.get(0).getId();
	author.setNumeric2(determineActivity(authorId));

	// permission of author
	author.setPermissions(AUTHORS_PERMISSIONS);

	// fetch old data
	String login = author.getString1();
	String firstName = safeRetrieveElementText(xmlRoot, "/data/firstname");
	String lastName = safeRetrieveElementText(xmlRoot, "/data/surname");
	String nickname = safeRetrieveElementText(xmlRoot, "/data/nickname");

	// create/add/remove new XMLData
	safeAppendElement(xmlRoot, "/data/login", login);
	safeAppendElement(xmlRoot, "/data/nickname", nickname);
	safeRemoveElement(xmlRoot, "/data/firstname");
	safeRemoveElement(xmlRoot, "/data/surname");
	author.setData(xmlRoot.getDocument());

	// set other fields
	author.setString1(firstName);
	author.setString2(lastName);

	// System.out.println(author.getPermissions());
	// System.out.println(authorToString(author));

	persistence.update(author);
    }

    private String safeRetrieveElementText(Element root, String xpath) {

	Node node = root.selectSingleNode(xpath);
	if (node != null)
	    return node.getText();

	return null;

    }

    /**
     * Adds new child element under given parent. Constructs tree if necessary.
     * 
     * @param parent Parent to get new child
     * @param name Child's name
     * @param value Text value of child
     * @throws InvalidDataException When there is data conflict between datum
     *             included and data added.
     */
    private void safeAppendElement(Element root, String xpath, String value) throws InvalidDataException {

	Node tmp = root.selectSingleNode(xpath);

	// add element value into tree
	if (tmp == null) {
	    StringTokenizer tokenizer = new StringTokenizer(xpath, "/");
	    try {
		String name = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
		Element parent = (root.getName().equals(name)) ? root : null;
		Element child = null;
		while (tokenizer.hasMoreTokens()) {
		    name = tokenizer.nextToken();
		    child = parent.element(name);
		    if (child == null) {
			child = parent.addElement(name);
		    }
		    parent = child;
		}
		if (value != null)
		    child.addText(value);
	    } catch (NullPointerException npe) {
		npe.printStackTrace();
	    }
	}
	// there is node, what to do if its value differs from one to be set
	else if (value != null && !value.equals(tmp.getText())) {

	    throw new InvalidDataException("Author already contains node of " + root.getPath()
		+ "/"
		+ xpath
		+ ", with value: "
		+ tmp.getText()
		+ ", which differs from: "
		+ value);
	}
    }

    /**
     * Removes element of its complete branch.
     * 
     * @param root Root element
     * @param xpath XPath path to determine removed element
     */
    private void safeRemoveElement(Element root, String xpath) {

	Node tmp = root.selectSingleNode(xpath);

	if (tmp != null) {
	    StringTokenizer tokenizer = new StringTokenizer(xpath, "/");
	    try {
		String name = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
		Element parent = (root.getName().equals(name)) ? root : null;
		Element child = null;
		while (tokenizer.hasMoreTokens()) {
		    name = tokenizer.nextToken();
		    child = parent.element(name);
		    if (child != null && child.elements().isEmpty()) {
			parent.remove(child);
			break;
		    }
		    parent = child;
		}
	    } catch (NullPointerException npe) {
		npe.printStackTrace();
	    }
	}
    }

    /**
     * Determines whether author is active by checking whether his last article
     * date is within given period. This period is by default set to 9 months.
     * 
     * @return {@code 0}, if author was not active during given period, {@code
     *         1} otherwise
     */
    private int determineActivity(int authorId) {
	Calendar cal = Calendar.getInstance();
	cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - MONTHS_TO_SET_INACTIVE);
	java.sql.Date after = new java.sql.Date(cal.getTimeInMillis());

	final Qualifier[] qualifiers = { new CompareCondition(Field.CREATED, Operation.GREATER, after) };

	// no qualifiers for count functions are present
	// just fetch relations list and chect size
	List<Relation> relations = sqlTool.findArticleRelationsByAuthor(authorId, qualifiers);
	return (relations.isEmpty()) ? 0 : 1;
    }

    /**
     * Helper function to print author
     * 
     * @param author
     * @return
     */
    private String authorToString(Item author) {
	StringBuilder sb = new StringBuilder();

	sb.append("Author:").append(" id:").append(author.getId()).append(" first name:").append(
	    author.getString1()).append(" last name:").append(author.getString2()).append(" uid:").append(
	    author.getNumeric1()).append(" activity:").append(author.getNumeric2()).append("\n");
	sb.append("XML:").append(author.getDataAsString());

	return sb.toString();

    }

    /**
     * Processes author migration
     * 
     * @param args
     */
    public static void main(String[] args) {
	try {
	    MigrateAuthors migration = new MigrateAuthors();
	    migration.run();
	} catch (Exception e) {
	    System.err.println("Migration of author was not sucessful.");
	    e.printStackTrace();
	}
    }

}
