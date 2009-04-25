package cz.abclinuxu.utils;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.view.Author;

/**
 * Transforms JavaBeans into persistence layer objects
 * 
 * @author kapy
 * 
 */
public class BeanFlusher {
	private static final Logger log = Logger.getLogger(BeanFlusher.class);

	// disable instantiation
	private BeanFlusher() {
		throw new AssertionError();
	}

	public static Item flushAuthorToItem(Item item, Author author) {

		item.setNumeric1(author.getUid());
		item.setNumeric2(author.isActive() ? 1 : 0);
		item.setString1(Misc.filterDangerousCharacters(author.getName()));
		item.setString2(Misc.filterDangerousCharacters(author.getSurname()));
		item.setTitle(author.getTitle());

		// get/create XML document
		Document doc = item.getData();
		if (doc == null)
			doc = DocumentHelper.createDocument(DocumentHelper
					.createElement("data"));

		safeStoreNode(doc, "/data/about", author.getAbout());
		safeStoreNode(doc, "/data/accountNumber", author.getAccountNumber());
		safeStoreNode(doc, "/data/address", author.getAddress());
		safeStoreNode(doc, "/data/birthNumber", author.getBirthNumber());
		safeStoreNode(doc, "/data/email", author.getEmail());
		safeStoreNode(doc, "/data/login", author.getLogin());
		safeStoreNode(doc, "/data/nickname", author.getNickname());
		safeStoreNode(doc, "/data/phone", author.getPhone());
		safeStoreNode(doc, "/data/photourl", author.getPhotoUrl());

		item.setData(doc);

		if (log.isDebugEnabled()) {
			log.debug("Flushed:" + authorToString(item));
		}

		return item;

	}

	private static void safeStoreNode(Document doc, String xpath, String value) {

		// check for existence of node
		Node tmp = doc.selectSingleNode(xpath);
		// check for value passed
		if (Misc.empty(value)) {
			// detach node if it will be empty
			if (tmp != null) {
				tmp.detach();
			}
			return;
		}
		// create node if not found
		if (tmp == null) {
			tmp = DocumentHelper.makeElement(doc, xpath);
		}

		tmp.setText(Misc.filterDangerousCharacters(value));
	}

	/**
	 * Helper function to print author
	 * 
	 * @param author
	 * @return
	 */
	private static String authorToString(Item author) {
		StringBuilder sb = new StringBuilder();

		sb.append("Author:").append(" id:").append(author.getId()).append(
				" first name:").append(author.getString1()).append(
				" last name:").append(author.getString2()).append(" uid:")
				.append(author.getNumeric1()).append(" activity:").append(
						author.getNumeric2()).append("\n");
		sb.append("XML:").append(author.getDataAsString());

		return sb.toString();

	}
}
