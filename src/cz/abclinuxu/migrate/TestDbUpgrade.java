package cz.abclinuxu.migrate;

import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.data.Category;
import org.dom4j.Element;

/**
 * Utility class that updates selected category row in database with
 * text containing diacritics. This way we can find out whether system locale
 * is correctly set and data model upgrade will not harm data.
 */
public class TestDbUpgrade {
    private static final String TEST_CHARS = "ěščřžýáí";
    private static final int TEST_CATEGORY = 507;

    public static void main(String[] args) {
        Persistence persistence = PersistenceFactory.getPersistence();
        Category category = (Category) persistence.findById(new Category(TEST_CATEGORY));
        Element element = (Element) category.getData().selectSingleNode("/data/note");
        String content;
        if (element == null) {
            element = category.getData().getRootElement().addElement("note");
            content = "";
        } else
            content = element.getText();
        content = content.concat(TEST_CHARS);
        element.setText(content);
        category.setSubType(TEST_CHARS);
        persistence.update(category);
        System.out.println("Text set to: '" + content + "', subtype to " + TEST_CHARS + ".");
        System.out.println("Verify with 'select * from kategorie where cislo=" + TEST_CATEGORY+ "'");
    }
}
