package cz.abclinuxu.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.XMLHandler;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.view.Author;
import cz.abclinuxu.data.view.ContractTemplate;
import cz.abclinuxu.data.view.Topic;
import cz.abclinuxu.data.view.SignedContract;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.exceptions.InvalidDataException;

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
	 * @param relation completely initialized relation
	 * @param ft
	 * @return Author object created from provided data
	 */
	public static Author fetchAuthor(Relation relation, FetchType ft) {
		if (relation == null)
		    return null;

        Item item = (Item) relation.getChild();
		Author author = new Author();
		author.setId(item.getId());
        author.setRelationId(relation.getId());
		author.setUid(item.getNumeric1());
		author.setContractId(item.getNumeric2());
		author.setActive(item.isBoolean1());
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
     * <li>relation.cislo</li>
     * <li>contractTemplate.boolean2</li>
     * </ol>
	 *
	 * @param objects Array of objects
	 * @param ft Fetch type
	 * @return Created author object
	 */
	public static Author fetchAuthorFromObjects(Object[] objects, FetchType ft) {
		// ! exact object length must be passed !
		if (objects == null || objects.length != 13)
		    throw new InvalidDataException("Illegal argument passed");

		Author author = new Author();
		try {
			author.setId((Integer) objects[0]);
			author.setUid((Integer) objects[1]);
			author.setActive("1".equals(objects[2]));
			author.setName((String) objects[3]);
			author.setSurname((String) objects[4]);
			author.setArticleCount(((Long) objects[6]).intValue());

			java.sql.Timestamp date = (java.sql.Timestamp) objects[7];
			if (date != null)
			    author.setLastArticleDate(new Date(date.getTime()));

			author.setOwner((Integer) objects[8]);
			author.setGroup((Integer) objects[9]);
			author.setPermissions((Integer) objects[10]);
			author.setRelationId((Integer) objects[11]);

            if (objects[12] == null)
                author.setContractStatus(Author.ContractStatus.UNSIGNED);
            else if (objects[12].equals("1"))
                author.setContractStatus(Author.ContractStatus.OBSOLETE);
            else
                author.setContractStatus(Author.ContractStatus.CURRENT);

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
		author.setAbout(XmlUtils.getNodeText(root, "/data/about"));
		author.setAddress(XmlUtils.getNodeText(root, "/data/address"));
		author.setAccountNumber(XmlUtils.getNodeText(root, "/data/accountNumber"));
		author.setBirthNumber(XmlUtils.getNodeText(root, "/data/birthNumber"));
		author.setEmail(XmlUtils.getNodeText(root, "/data/email"));
		author.setLogin(XmlUtils.getNodeText(root, "/data/login"));
		author.setNickname(XmlUtils.getNodeText(root, "/data/nickname"));
		author.setPhone(XmlUtils.getNodeText(root, "/data/phone"));
		author.setPhotoUrl(XmlUtils.getNodeText(root, "/data/photourl"));
		return author;
	}

	/**
	 * Creates contract template JavaBean from persistence layer's object
	 * @param relation initialized relation to Item to be converted to the bean
	 * @param ft Fetch type
	 * @return ContractTemplate object
	 */
	public static ContractTemplate fetchContractTemplate(Relation relation, FetchType ft) {
		if (relation == null)
            return null;

        Item item = (Item) relation.getChild();
        ContractTemplate template = new ContractTemplate();
        template.setId(item.getId());
        template.setRelationId(relation.getId());
        template.setPublished(item.getDate1());
        template.setTitle(item.getTitle());
        template.setDraft(item.isBoolean1());
        template.setObsolete(item.isBoolean2());
        template.setSignedContracts(item.getChildren().size());

        switch (ft) {
            case LAZY:
                break;
            case PROCESS_NONATOMIC:
            case EAGER:
                Element root = item.getData().getRootElement();
                template = fillXMLProperties(template, root);
        }
        return template;
    }

	public static List<ContractTemplate> fetchContractTemplates(List<Relation> relations, FetchType ft) {
		List<ContractTemplate> contractTemplates = new ArrayList<ContractTemplate>(relations.size());
		for (Relation relation : relations) {
            ContractTemplate template = fetchContractTemplate(relation, ft);
            contractTemplates.add(template);
		}
		return contractTemplates;
	}

	private static ContractTemplate fillXMLProperties(ContractTemplate contractTemplate, Element root) {
		contractTemplate.setDescription(XmlUtils.getNodeText(root, "/data/description"));
		contractTemplate.setContent(XmlUtils.getNodeText(root, "/data/content"));
		return contractTemplate;
	}

	/**
	 * Creates signed contract JavaBean from persistence layer's object
	 * @param item initialized Item to be converted to the bean
	 * @param ft Fetch type
	 * @return SignedContract object
	 */
	public static SignedContract fetchSignedContract(Relation relation, FetchType ft) {
		if (relation == null)
            return null;

        Item item = (Item) relation.getChild();
        SignedContract contract = new SignedContract();
		contract.setId(item.getId());
		contract.setRelationId(relation.getId());
		contract.setUid(item.getOwner());
        contract.setSigned(item.getCreated());
        contract.setIpAddress(item.getString1());
        contract.setTemplate(item.getNumeric1());

		switch (ft) {
            case LAZY:
                break;
            case PROCESS_NONATOMIC:
            case EAGER:
                Element root = item.getData().getRootElement();
                contract = fillXMLProperties(contract, root);
		}

        Persistence persistence = PersistenceFactory.getPersistence();
        Item template = (Item) persistence.findById(new Item(contract.getTemplate()));
        contract.setTitle(template.getTitle());
        contract.setObsolete(template.isBoolean2());

        Relation authorRelation = (Relation) persistence.findById(new Relation(item.getNumeric2()));
        Tools.sync(authorRelation);
        contract.setAuthor(fetchAuthor(authorRelation, BeanFetcher.FetchType.LAZY));

        return contract;
	}

	public static List<SignedContract> fetchSignedContracts(List<Relation> relations, FetchType ft) {
		List<SignedContract> contracts = new ArrayList<SignedContract>(relations.size());
		for (Relation relation : relations) {
            SignedContract template = fetchSignedContract(relation, ft);
            contracts.add(template);
		}
		return contracts;
	}

	private static SignedContract fillXMLProperties(SignedContract contract, Element root) {
		contract.setContent(XmlUtils.getNodeText(root, "/data/content"));
		return contract;
	}

	/**
	 * Creates Topic JavaBeans from persistence object
	 *
	 * @param item Persistence object to fill topic
	 * @param ft Fetch type
	 * @return Topic object
	 */
	public static Topic fetchTopic(Item item, FetchType ft) {
		if (item == null)
            return null;

		Topic topic = new Topic();
		topic.setId(item.getId());
		topic.setPublished(XmlUtils.booleanValue(item.getNumeric1()));
		topic.setAccepted(XmlUtils.booleanValue(item.getNumeric2()));
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
            Author author = Tools.getAuthor(topic.getAuthor().getId());
            topic.setAuthor(author);
		}

		return topic;
	}

	public static List<Topic> fetchTopics(List<Item> items, FetchType ft) {
		List<Topic> topics = new ArrayList<Topic>(items.size());
		for (Item item : items) {
			topics.add(fetchTopic(item, ft));
		}
		return topics;
	}

	private static Topic fillXMLProperties(Topic topic, Element root) {
		Author author = new Author();
        author.setId(XmlUtils.getNodeInt(root, "/data/author"));
		topic.setAuthor(author);
		topic.setRoyalty(XmlUtils.getNodeDouble(root, "/data/royalty"));
		topic.setDescription(XmlUtils.getNodeText(root, "/data/description"));
		return topic;
	}
}