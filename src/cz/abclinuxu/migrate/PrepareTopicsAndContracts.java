package cz.abclinuxu.migrate;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileWriter;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.view.Contract;
import cz.abclinuxu.data.view.Topic;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.servlets.Constants;

/**
 * Prepares database and system properties to contain objects used as further
 * reference for Contract and Topic.
 * 
 * This class searches for "conf/systemPrefs.xml" by default if system property
 * 
 * @{code system.prefs.file} is not set otherwise
 * 
 * @see Topic
 * @see Contract
 * @author kapy
 * 
 */
public class PrepareTopicsAndContracts {

	private String systemPrefsXml;

	private PrepareTopicsAndContracts() {
		this.systemPrefsXml = System.getProperty("system.prefs.file", "conf/systemPrefs.xml");
		File test = new File(systemPrefsXml);
		if (!test.exists()) {
			System.err.println("systemPrefs.xml not found at " + test.getAbsolutePath() + ", please set system.prefs.file property");
			System.err.println("Quitting script");
			System.exit(1);
		}
	}

	public static void main(String[] args) {

		PrepareTopicsAndContracts worker = new PrepareTopicsAndContracts();
		Persistence persistence = PersistenceFactory.getPersistence();

		try {
			Category system = (Category) persistence.findById(new Category(Constants.CAT_SYSTEM));
			Category topics = worker.prepareTopicsCategory();
			Category contracts = worker.prepareContractsCategory();

			// store categories entries in database
			persistence.create(topics);
			persistence.create(contracts);

			Relation topicsRel = new Relation(system, topics, 0);
			topicsRel.setUrl("/sprava/redakce/namety");
			Relation contractsRel = new Relation(system, contracts, 0);
			contractsRel.setUrl("/sprava/redakce/smlouvy");

			// store relations entries in database
			persistence.create(topicsRel);
			persistence.create(contractsRel);

			System.out.println("Created and persisted database objects");
			System.out.println("Categories: " + topics + ", " + contracts);
			System.out.println("Relations: " + topicsRel + ", " + contractsRel);

			// update system preferences
			SAXReader reader = new SAXReader();
			Document doc = reader.read(new File(worker.systemPrefsXml));

			Element constants = (Element) doc.selectSingleNode("//node[@name=\"Constants\"]/map");
			constants.add(worker.entry("CAT_TOPICS", topics.getId()));
			constants.add(worker.entry("CAT_CONTRACTS", contracts.getId()));
			constants.add(worker.entry("REL_TOPICS", topicsRel.getId()));
			constants.add(worker.entry("REL_CONTRACTS", contractsRel.getId()));

			XMLWriter writer = new XMLWriter(new FileWriter(worker.systemPrefsXml), new OutputFormat("    ", true));
			writer.write(doc);
			writer.flush();

			System.out.println("Successfully upgraded database and systemPrefs.xml");
		}
		catch (Exception e) {
			System.err.println("Failed prepace topic and contract");
			e.printStackTrace();
		}
	}

	private Category prepareTopicsCategory() throws DocumentException {
		Category topics = new Category();
		topics.setType(0);
		org.dom4j.Document doc = DocumentHelper.parseText("<data><name>Náměty</name><note>Sekce pro náměty</note><icon/></data>");
		topics.setData(doc);
		return topics;
	}

	private Category prepareContractsCategory() throws DocumentException {
		Category contracts = new Category();
		contracts.setType(0);
		org.dom4j.Document doc = DocumentHelper.parseText("<data><name>Smlouvy</name><note>Sekce pro smlouvy</note><icon/></data>");
		contracts.setData(doc);
		return contracts;
	}

	private Element entry(String key, int id) {
		return entry(key, String.valueOf(id));
	}

	private Element entry(String key, String value) {
		Element entry = DocumentHelper.createElement("entry");
		entry.addAttribute("key", key);
		entry.addAttribute("value", value);
		return entry;
	}

}
