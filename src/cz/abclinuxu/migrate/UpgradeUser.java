/*
 * User: literakl
 * Date: 12.7.2003
 * Time: 10:06:56
 */
package cz.abclinuxu.migrate;

import cz.abclinuxu.data.User;
import cz.abclinuxu.persistance.EmptyCache;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.Element;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This utility will migrate User's XML data
 * to new DTD.
 * @deprecated this class is not maintained
 */
public class UpgradeUser {

    /**
     * It does all work. First it finds relevant users, load them into memory
     * and migrates their data property to new XML.
     */
    public static void main(String[] args) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance(EmptyCache.class);

        System.out.print("Starting to search for users ..");
        List users = findExistingUsers(persistance);
        System.out.println(" done");

        System.out.print("Conversion of found users starts ..");
        List inform = new ArrayList(users.size());
        for (Iterator iterator = users.iterator(); iterator.hasNext();) {
            int key = ((Integer) iterator.next()).intValue();
            User user = (User) persistance.findById(new User(key));
            upgradeData(persistance,user,inform);
        }
        System.out.println(" done");

        FileOutputStream os = new FileOutputStream("nalezeni_uzivatele.txt");
        PrintStream out = new PrintStream(os);
        System.out.print("Writing list of users, that can be informed ..");
        for (Iterator iterator = inform.iterator(); iterator.hasNext();) {
            Integer key = (Integer) iterator.next();
            out.println(key);
        }
        System.out.println(" done");
    }

    /**
     * Finds all existing users in database.
     * @param persistance persistance to be used
     * @return List of Integers, which are ids of found users.
     */
    static List findExistingUsers(Persistance persistance) {
        List list = new ArrayList(3100);
        List result = persistance.findByCommand("select cislo from uzivatel");
        for (Iterator iterator = result.iterator(); iterator.hasNext();) {
            Object[] objects = (Object[]) iterator.next();
            list.add(objects[0]);
        }
        return list;
    }

    /**
     * Constructs new XML data from existing data and updates database.
     * @param persistance persistance to be used
     * @param user user to be upgraded, must be already synchronized
     * @param inform list of Integers, which are ids of users, that shall receive information user
     */
    static void upgradeData(Persistance persistance, User user, List inform) {
        Document old = user.getData();
        Document document = DocumentHelper.createDocument();

        // <data><personal>
        Node node = old.selectSingleNode("data/sex");
        DocumentHelper.makeElement(document, "/data/personal/sex").addText(node.getText());

        // <data><communication><email>
        Element email = DocumentHelper.makeElement(document, "/data/communication/email");
        email.addAttribute("valid","yes");
//        DocumentHelper.makeElement(document, "/data/communication/email/newsletter").addText("no");
//        DocumentHelper.makeElement(document, "/data/communication/email/weekly_summary").addText("no");

        // <data><profile>
        DocumentHelper.makeElement(document, "/data/profile");
        node = old.selectSingleNode("data/www");
        if ( node!=null )
            DocumentHelper.makeElement(document, "/data/profile/home_page").addText(node.getText());
        node = old.selectSingleNode("data/personal");
        if (node != null)
            DocumentHelper.makeElement(document, "/data/profile/about_myself").addText(node.getText());

        // <data><settings>
        DocumentHelper.makeElement(document, "/data/settings");
//        DocumentHelper.makeElement(document, "/data/settings/emoticons").addText("yes");
//        DocumentHelper.makeElement(document, "/data/settings/cookie_valid").addText("8035200");

        // <data><system>
        DocumentHelper.makeElement(document, "/data/system");

        user.setData(document);
        persistance.update(user);

        if ( shallReceiveMail(old) )
            inform.add(new Integer(user.getId()));
    }

    /**
     * Finds out, whether we can send email to user.
     * @param document
     * @return
     */
    static boolean shallReceiveMail(Document document) {
        boolean weCan = false;

        Node node = document.selectSingleNode("data/news");
        if (node != null) {
            if ("yes".equals(node.getText()))
                weCan = true;
        }

        node = document.selectSingleNode("data/ads");
        if (node != null) {
            if ("yes".equals(node.getText()))
                weCan = true;
        }

        node = document.selectSingleNode("data/active");
        if (node != null) {
            weCan = "yes".equals(node.getText());
        }

        return weCan;
    }
}
