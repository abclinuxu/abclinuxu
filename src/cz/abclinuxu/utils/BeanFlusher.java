package cz.abclinuxu.utils;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.view.Author;
import cz.abclinuxu.data.view.Contract;
import cz.abclinuxu.data.view.Topic;

/**
 * Transforms JavaBeans into persistence layer objects
 * 
 * @author kapy
 */
public class BeanFlusher {
	private static final Logger log = Logger.getLogger(BeanFlusher.class);

	// disable instantiation
	private BeanFlusher() {
		throw new AssertionError();
	}

	public static Item flushContractToItem(Item item, Contract contract) {

		item.setNumeric1(contract.getEmployee());
		item.setNumeric2(contract.getEmployer());
		item.setString1(Misc.filterDangerousCharacters(contract.getVersion()));

		// get/create XML document
		DocumentBuilder db = new DocumentBuilder(item.getData(), "data");

		db.store("/data/signatures/employee", contract.getEmployeeSignature())
		        .store("/data/signatures/employer", contract.getEmployerSignature())
		        .store("/data/title", contract.getTitle())
		        .store("/data/content", contract.getContent());

		item.setData(db.getDocument());
		debug("Flushed %s, value: %s %s", item, contract, db);
		return item;
	}

	public static Item flushAuthorToItem(Item item, Author author) {

		item.setNumeric1(author.getUid());
		item.setNumeric2(author.isActive() ? 1 : 0);
		item.setString1(Misc.filterDangerousCharacters(author.getName()));
		item.setString2(Misc.filterDangerousCharacters(author.getSurname()));
		item.setTitle(author.getTitle());

		// rights
		item.setOwner(author.getOwner());
		item.setGroup(author.getGroup());
		item.setPermissions(author.getPermissions());

		// get/create XML document
		DocumentBuilder db = new DocumentBuilder(item.getData(), "data");
		db.store("/data/about", author.getAbout())
		        .store("/data/accountNumber", author.getAccountNumber())
		        .store("/data/address", author.getAddress())
		        .store("/data/birthNumber", author.getBirthNumber())
		        .store("/data/email", author.getEmail())
		        .store("/data/login", author.getLogin())
		        .store("/data/nickname", author.getNickname())
		        .store("/data/phone", author.getPhone())
		        .store("/data/photourl", author.getPhotoUrl());

		item.setData(db.getDocument());
		debug("Flushed %s, value: %s %s", item, author, db);
		return item;

	}

	public static Item flushTopicToItem(Item item, Topic topic) {

		item.setNumeric1(topic.isPublished() ? 1 : 0);
		item.setNumeric2(topic.isAccepted() ? 1 : 0);
		item.setDate1(topic.getDeadline());
		item.setString1(Misc.filterDangerousCharacters(topic.getTitle()));

		DocumentBuilder db = new DocumentBuilder(item.getData(), "data");

		if (!topic.isPublic())
		    db.store("/data/author", String.valueOf(topic.getAuthor().getId()));
		if (topic.hasRoyalty())
		    db.store("/data/royalty", topic.getRoyalty().toString());

		db.store("/data/description", topic.getDescription());

		item.setData(db.getDocument());
		debug("Flushed %s, value: %s %s", item, topic, db);
		return item;

	}

	/**
	 * Debug all passed objects
	 * 
	 * @param objects
	 */
	private static final void debug(String format, Object... args) {
		if (log.isDebugEnabled()) {
			log.debug(String.format(format, args));
		}
	}

	/**
	 * Helper for construction of DOM tree
	 * 
	 * @author kapy
	 * 
	 */
	private static class DocumentBuilder {

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
		public DocumentBuilder store(String xpath, String value) {
			Node node = doc.selectSingleNode(xpath);
			// node will be detached, no value provided
			if (Misc.empty(value) && node != null)
				node.detach();
			// ommit empty value
			else if (Misc.empty(value))
				return this;
			// change text value
			else {
				if (node == null) node = DocumentHelper.makeElement(doc, xpath);
				node.setText(Misc.filterDangerousCharacters(value));
			}
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
}
