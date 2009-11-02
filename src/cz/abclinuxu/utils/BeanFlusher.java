package cz.abclinuxu.utils;

//import java.sql.Date;

import java.util.Date;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
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

		// do not set employee for templates
		if (contract.getEmployee() != null)
		    item.setNumeric1(contract.getEmployee().getId());

		item.setDate1(contract.getProposedDate());

		// get/create XML document
		DocumentBuilder db = new DocumentBuilder(item.getData(), "data");

		db.store("/data/employer/name", contract.getEmployerName())
		        .store("/data/employer/position", contract.getEmployerPosition())
		        .store("/data/employer/signature", contract.getEmployerSignature())
		        .store("/data/title", contract.getTitle())
		        .store("/data/description", contract.getDescription())
		        .store("/data/template-id", contract.getTemplateId())
		        .store("/data/content", contract.getContent())
		        .store("/data/obsolete", contract.isObsolete())
		        .store("/data/signed-date", (Date) contract.getSignedDate())
		        .store("/data/version", contract.getVersion());

		item.setData(db.getDocument());
		debug("Flushed %s, value: %s %s", item, contract, db);
		return item;
	}

	public static Relation flushContractToRelation(Relation relation, Contract contract) {

		// do not set employee for templates
		if (contract.getEmployee() != null)
		    relation.setChild(new Item(contract.getEmployee().getId(), Item.CONTRACT));

		// get/create XML document
		DocumentBuilder db = new DocumentBuilder(relation.getData(), "data");

		db.store("/data/employer/name", contract.getEmployerName())
		        .store("/data/employer/position", contract.getEmployerPosition())
		        .store("/data/employer/signature", contract.getEmployerSignature())
		        .store("/data/title", contract.getTitle())
		        .store("/data/description", contract.getDescription())
		        .store("/data/template-id", contract.getTemplateId())
		        .store("/data/content", contract.getContent())
		        .store("/data/obsolete", contract.isObsolete())
		        .store("/data/signed-date", contract.getSignedDate())
		        .store("/data/proposed-date", contract.getProposedDate())
		        .store("/data/version", contract.getVersion());

		relation.setData(db.getDocument());
		debug("Flushed %s, value: %s %s", relation, contract, db);
		return relation;
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
		else
			db.store("/data/author", null);
		if (topic.hasRoyalty())
			db.store("/data/royalty", topic.getRoyalty().toString());
		else
			db.store("/data/royalty", null);

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
	public static class DocumentBuilder {

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
}
