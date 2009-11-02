package cz.abclinuxu.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.Node;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.XMLHandler;
import cz.abclinuxu.data.view.Author;
import cz.abclinuxu.data.view.Contract;
import cz.abclinuxu.data.view.Topic;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.Qualifier;

/**
 * Transforms persistence layer object into JavaBeans used in application
 * 
 * @author kapy
 */
public class BeanFetcher {
	protected static final Logger log = Logger.getLogger(BeanFetcher.class);

	/**
	 * Determines how data are fetched into bean
	 * 
	 * @author kapy
	 */
	public static enum FetchType {
				/**
		 * All data are fetched and processed, including external dependencies.
		 * This type of fetch can result in multiple SQL queries
		 */
		EAGER,
				/**
		 * Similar to lazy, but non-atomic types, such as XML chunks, are
		 * processed into atomic variables
		 */
		PROCESS_NONATOMIC,
				/**
		 * Only data directly accessible from columns of SQL query are fetched
		 */
		LAZY
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

		author.setActive(Util.booleanValue(item.getNumeric2()));
		author.setName(item.getString1());
		author.setSurname(item.getString2());

		// rights
		author.setOwner(item.getOwner());
		author.setPermissions(item.getPermissions());
		author.setGroup(item.getGroup());

		switch (ft) {
		case LAZY:
			break;
		case PROCESS_NONATOMIC:
		case EAGER:
			Element root = item.getData().getRootElement();
			author = fillXMLProperties(author, root);
		}

		return author;
	}

	public static List<Author> fetchAuthorsFromItems(List<Item> list, FetchType ft) {
		List<Author> authors = new ArrayList<Author>(list.size());
		for (Item i : list) {
			authors.add(fetchAuthorFromItem(i, ft));
		}
		return authors;
	}

	/**
	 * Creates author from array of objects. Expects object at specified
	 * positions
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
	 * @param ft Fetch type
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
			author.setActive(Util.booleanValue(objects[2]));
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
			case LAZY:
				break;
			case PROCESS_NONATOMIC:
			case EAGER:
				XMLHandler handler = new XMLHandler((String) objects[5]);
				Element root = handler.getData().getRootElement();
				author = fillXMLProperties(author, root);
			}
			return author;
		}
		catch (ClassCastException cce) {
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
		author.setAbout(Util.elementText(root, "/data/about"));
		author.setAddress(Util.elementText(root, "/data/address"));
		author.setAccountNumber(Util.elementText(root, "/data/accountNumber"));
		author.setBirthNumber(Util.elementText(root, "/data/birthNumber"));
		author.setEmail(Util.elementText(root, "/data/email"));
		author.setLogin(Util.elementText(root, "/data/login"));
		author.setNickname(Util.elementText(root, "/data/nickname"));
		author.setPhone(Util.elementText(root, "/data/phone"));
		author.setPhotoUrl(Util.elementText(root, "/data/photourl"));
		return author;
	}

	/**
	 * Creates contract JavaBean from persistence layer's object
	 * 
	 * @param relation Persistence layer object to fill contract
	 * @param ft Fetch type
	 * @return Contract object
	 */
	public static Contract fetchContractFromRelation(Relation relation, FetchType ft) {
		if (relation == null) return null;

		Contract contract = new Contract();
		contract.setId(relation.getId());

		contract.setEmployee(new Author(relation.getChild().getId()));
		switch (ft) {
		case LAZY:
			break;
		case PROCESS_NONATOMIC:
			Element root = relation.getData().getRootElement();
			contract = fillXMLProperties(contract, root);
			contract.setProposedDate(Util.elementDate(root, "/data/proposed-date"));
			break;
		case EAGER:
			root = relation.getData().getRootElement();
			contract = fillXMLProperties(contract, root);
			contract.setProposedDate(Util.elementDate(root, "/data/proposed-date"));

			// user objects are fetched from persistence layer
			SQLTool sqlTool = SQLTool.getInstance();
			if (contract.getEmployee() != null) {
				Qualifier[] qualifiers = new Qualifier[] {
				        new CompareCondition(Field.ID, Operation.EQUAL, contract.getEmployee().getId())
				        };
				contract.setEmployee(fetchAuthorFromItem(sqlTool.findItemsWithType(Item.AUTHOR, qualifiers).get(0), FetchType.EAGER));
			}
			break;
		}

		return contract;

	}

	public static List<Contract> fetchContractsFromRelations(List<Relation> relations, FetchType ft) {
		List<Contract> contracts = new ArrayList<Contract>(relations.size());
		for (Relation relation : relations) {
			contracts.add(fetchContractFromRelation(relation, ft));
		}
		return contracts;
	}

	/**
	 * Creates contract JavaBean from persistence layer's object
	 * 
	 * @param item Persistence layer object to fill contract
	 * @param ft Fetch type
	 * @return Contract object
	 */
	public static Contract fetchContractFromItem(Item item, FetchType ft) {

		if (item == null) return null;

		Contract contract = new Contract();
		contract.setId(item.getId());
		contract.setProposedDate(item.getDate1());

		switch (ft) {
		case LAZY:
			break;
		case PROCESS_NONATOMIC:
			Element root = item.getData().getRootElement();
			contract = fillXMLProperties(contract, root);
			break;
		case EAGER:
			root = item.getData().getRootElement();
			contract = fillXMLProperties(contract, root);
			break;
		}
		return contract;
	}

	public static List<Contract> fetchContractsFromItems(List<Item> items, FetchType ft) {
		List<Contract> contracts = new ArrayList<Contract>(items.size());
		for (Item item : items) {
			contracts.add(fetchContractFromItem(item, ft));
		}
		return contracts;
	}

	private static Contract fillXMLProperties(Contract contract, Element root) {
		contract.setTitle(Util.elementText(root, "/data/title"));
		contract.setVersion(Util.elementText(root, "/data/version"));
		contract.setDescription(Util.elementText(root, "/data/description"));
		contract.setContent(Util.elementText(root, "/data/content"));
		contract.setTemplateId(Util.elementInt(root, "/data/template-id"));
		contract.setEmployerSignature(Util.elementText(root, "/data/employer/signature"));
		contract.setEmployerName(Util.elementText(root, "/data/employer/name"));
		contract.setEmployerPosition(Util.elementText(root, "/data/employer/position"));
		contract.setSignedDate(Util.elementDate(root, "/data/signed-date"));
		contract.setObsolete(Util.elementBoolean(root, "/data/obsolete"));

		return contract;
	}

	/**
	 * Creates Topic JavaBeans from persistence object
	 * 
	 * @param item Persistence object to fill topic
	 * @param ft Fetch type
	 * @return Topic object
	 */
	public static Topic fetchTopicFromItem(Item item, FetchType ft) {

		if (item == null) return null;

		Topic topic = new Topic();
		topic.setId(item.getId());
		topic.setPublished(Util.booleanValue(item.getNumeric1()));
		topic.setAccepted(Util.booleanValue(item.getNumeric2()));
		topic.setDeadline(item.getDate1());
		topic.setTitle(item.getString1());

		switch (ft) {
		// non-atomic must be processed here at least partially
		case LAZY:
			break;
		case PROCESS_NONATOMIC:
		case EAGER:
			Element root = item.getData().getRootElement();
			topic = fillXMLProperties(topic, root);
		}

		// do additional query to fetch author
		if (ft == FetchType.EAGER && topic.getAuthor() != null) {
			SQLTool sqlTool = SQLTool.getInstance();
			Qualifier[] qualifiers = { new CompareCondition(Field.ID, Operation.EQUAL, topic.getAuthor().getId()) };
			List<Item> authors = (List<Item>) sqlTool.findItemsWithType(Item.AUTHOR, qualifiers);
			if (authors != null && authors.size() == 1)
			    topic.setAuthor(fetchAuthorFromItem(authors.get(0), FetchType.EAGER));
		}
		return topic;
	}

	public static List<Topic> fetchTopicsFromItems(List<Item> items, FetchType ft) {
		List<Topic> topics = new ArrayList<Topic>(items.size());
		for (Item item : items) {
			topics.add(fetchTopicFromItem(item, ft));
		}
		return topics;
	}

	private static Topic fillXMLProperties(Topic topic, Element root) {
		Author author = Util.template(Author.class, Util.elementInt(root, "/data/author"));
		topic.setAuthor(author);
		topic.setRoyalty(Util.elementDouble(root, "/data/royalty"));
		topic.setDescription(Util.elementText(root, "/data/description"));
		return topic;
	}

}

/**
 * BeanFetcher's helper utility
 * 
 * @author kapy
 * 
 */
class Util {

	/**
	 * Parses XML chunk into atomic type in safe manner
	 * 
	 * @param root XPath context root
	 * @param xpath XPath expression to be evaluated on root
	 * @return String value with result or {@code null}
	 */
	static String elementText(Element root, String xpath) {

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
	static Integer elementInt(Element root, String xpath) {
		try {
			String value = elementText(root, xpath);
			if (value == null) return null;
			return Integer.valueOf(value);
		}
		catch (NumberFormatException nfe) {
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
	static Double elementDouble(Element root, String xpath) {
		try {
			String value = elementText(root, xpath);
			if (value == null) return null;
			return Double.valueOf(value);
		}
		catch (NumberFormatException nfe) {
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
	static boolean elementBoolean(Element root, String xpath) {
		String value = elementText(root, xpath);
		if (value == null) return false;
		return Boolean.valueOf(value);
	}

	/**
	 * Parses XML chunk into atomic type in safe manner.
	 * Accepts values in number of milliseconds
	 * 
	 * @param root XPath context root
	 * @param xpath XPath expression to be evaluated on root
	 * @return Date or {@code null}
	 */
	static Date elementDate(Element root, String xpath) {
		try {
			String value = elementText(root, xpath);
			if (value == null) return null;
			return new Date(Long.valueOf(value));
		}
		catch (NumberFormatException nfe) {
			return null;
		}
	}

	/**
	 * Transforms object to boolean value
	 * 
	 * @param object Object to be checked
	 * @return {@code true} if object equals 1, {@code false} otherwise
	 */
	static boolean booleanValue(Object object) {
		return (object != null && object.equals(1)) ? true : false;
	}

	/**
	 * Creates template object for given class with filled id.
	 * Public empty constructor and setId(Integer) method are required. If no id
	 * is passed, {@code null} is returned from this method
	 * 
	 * @param <T> Type of object
	 * @param clazz Class of object to be created
	 * @param id Id to be set to this object
	 * @return Created object template
	 */
	static <T> T template(Class<T> clazz, Integer id) {
		if (id == null) return null;
		try {
			T object = clazz.newInstance();
			java.lang.reflect.Field field = clazz.getDeclaredField("id");
			field.setAccessible(true);
			field.set(object, id.intValue());
			return object;
		}
		catch (NoSuchFieldException nsme) {
			BeanFetcher.log.error("Class " + clazz.getCanonicalName() + " is missing id field", nsme);
		}
		catch (IllegalArgumentException iae) {
			BeanFetcher.log.error("Unable to access id on " + clazz.getCanonicalName(), iae);
		}
		catch (IllegalAccessException iae) {
			BeanFetcher.log.error("Unable to access id  on " + clazz.getCanonicalName(), iae);
		}
		catch (SecurityException se) {
			BeanFetcher.log.error("Unable to access id on " + clazz.getCanonicalName(), se);
		}
		catch (InstantiationException ie) {
			BeanFetcher.log.error("Unable to instantiate " + clazz.getCanonicalName(), ie);
		}
		return null;

	}
}
