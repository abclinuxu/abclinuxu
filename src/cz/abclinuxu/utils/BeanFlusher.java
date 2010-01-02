package cz.abclinuxu.utils;

import org.apache.log4j.Logger;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.view.Author;
import cz.abclinuxu.data.view.ContractTemplate;
import cz.abclinuxu.data.view.Topic;
import cz.abclinuxu.data.view.SignedContract;

/**
 * Transforms JavaBeans into persistence layer objects
 *
 * @author kapy Leos
 */
public class BeanFlusher {
    private static final Logger log = Logger.getLogger(BeanFlusher.class);

    // disable instantiation
    private BeanFlusher() {
        throw new AssertionError();
    }

    /**
     * Converts bean to its representation in persistence. No changes to persistence are made, it is caller's responsability.
     *
     * @param item     item to be serialized to the persistence
     * @param template bean to be converted
     * @return modified item
     */
    public static Item flushContractTemplate(Item item, ContractTemplate template) {
        DocumentBuilder db = new DocumentBuilder(item.getData(), "data");
        db.store("/data/description", template.getDescription()).store("/data/content", template.getContent());
        item.setData(db.getDocument());

        item.setId(template.getId());
        item.setDate1(template.getPublished());
        item.setTitle(template.getTitle());
        item.setBoolean1(template.isDraft());
        item.setBoolean2(template.isObsolete());

        debug("Flushed %s, value: %s %s", item, template, db);
        return item;
    }

    /**
     * Converts bean to its representation in persistence. No changes to persistence are made, it is caller's responsability.
     * Properties dynamically loaded from contract template are ignored.
     *
     * @param item     item to be serialized to the persistence
     * @param contract bean to be converted
     * @return modified item
     */
    public static Item flushSignedContract(Item item, SignedContract contract) {
        DocumentBuilder db = new DocumentBuilder(item.getData(), "data");
        db.store("/data/content", contract.getContent());
        item.setData(db.getDocument());

        item.setId(contract.getId());
        item.setOwner(contract.getUid());
        item.setCreated(contract.getSigned());
        item.setNumeric1(contract.getTemplate());
        item.setNumeric2(contract.getAuthor().getRelationId());
        item.setString1(contract.getIpAddress());

        debug("Flushed %s, value: %s %s", item, contract, db);
        return item;
    }

    public static Item flushAuthor(Item item, Author author) {
        item.setNumeric1(author.getUid());
        item.setNumeric2(author.getContractId());
        item.setBoolean1(author.isActive());
        item.setString1(Misc.filterDangerousCharacters(author.getName()));
        item.setString2(Misc.filterDangerousCharacters(author.getSurname()));
        item.setTitle(author.getTitle());

        // get/create XML document
        DocumentBuilder db = new DocumentBuilder(item.getData(), "data");
        db.store("/data/about", author.getAbout()).store("/data/accountNumber", author.getAccountNumber()).store("/data/address", author.getAddress()).store("/data/birthNumber", author.getBirthNumber()).store("/data/email", author.getEmail()).store("/data/login", author.getLogin()).store("/data/nickname", author.getNickname()).store("/data/phone", author.getPhone()).store("/data/photourl", author.getPhotoUrl());

        item.setData(db.getDocument());
        debug("Flushed %s, value: %s %s", item, author, db);
        return item;
    }

    /**
     * Converts bean to its representation in persistence. No changes to persistence are made, it is caller's responsability.
     * @param item     item to be serialized to the persistence
     * @param topic bean to be converted
     * @return modified item
     */
    public static Item flushTopic(Item item, Topic topic) {
        item.setTitle(Misc.filterDangerousCharacters(topic.getTitle()));
        item.setDate1(topic.getDeadline());

        if (topic.getAuthor() != null)
            item.setNumeric1(topic.getAuthor().getRelationId());
        else
            item.setNumeric1(null);

        item.setNumeric2(topic.getRoyalty());

        if (topic.getArticle() != null)
            item.setNumeric3(topic.getArticle().getId());
        else
            item.setNumeric3(null);

        DocumentBuilder db = new DocumentBuilder(item.getData(), "data");
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
    private static void debug(String format, Object... args) {
        if (log.isDebugEnabled()) {
            log.debug(String.format(format, args));
        }
    }
}
