/*
 * User: literakl
 * Date: 14.8.2003
 * Time: 20:32:04
 */
package cz.abclinuxu.persistance;

import org.dom4j.Document;
import org.dom4j.Node;

import java.util.List;
import java.util.ArrayList;

import cz.abclinuxu.data.User;

/**
 * Tests speed of DOM4J's selectSingleNode() method.
 * 14.8.2003: DOM4J 1.4 - testing first 1000 users took 213 ms,
 *            one invocation of selectSingleNode took 0.213 ms.
 * Conclusion: it makes no sense to cache XPath execution result.
 */
public class TestXPathSpeed {
    public static void main(String[] args) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = null; Document data = null; Node node = null; int total = 1000;
        List users = new ArrayList(total);
        for (int i=1; users.size()<total; i++) {
            try {
                user = (User) persistance.findById(new User(i));
                users.add(user);
                data = user.getData(); // forces lazy init to happen now
            } catch (Exception e) { /* user doesn't exist, skip it */}
        }
        node = data.selectSingleNode("/data/settings/emoticons");

        int i = 0;
        long start = System.currentTimeMillis();
        for ( i = 0; i<total; i++ ) {
            //place your code to measure here
            data = ((User) users.get(i)).getData();
            node = data.selectSingleNode("/data/settings/emoticons");
        }
        long end = System.currentTimeMillis();

        if (node!=null)
            System.out.println("Value="+node.getText());

        float avg = (end-start)/(float) i;
        System.out.println("celkem = "+(end-start)+" ms ,prumer = "+avg+" ms.");
    }
}
