/*
 * User: literakl
 * Date: 2.10.2002
 * Time: 13:40:09
 */
package cz.abclinuxu.transfer;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.servlets.Constants;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;

import org.dom4j.Node;

/**
 * This class removes XML tag published and puts its
 * content to created field.
 */
public class TransferArticles {

    public static void main(String[] args) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        List exampleList = new ArrayList();exampleList.add(new Item(0,Item.ARTICLE));
        List found = persistance.findByExample(exampleList,null);

        for (Iterator iter = found.iterator(); iter.hasNext();) {
            Item item = (Item) iter.next();
            persistance.synchronize(item);
            Node node = item.getData().selectSingleNode("data/published");
            if ( node!=null ) {
                String tmp = node.getText();
                node.getParent().remove(node);
                Date published = Constants.isoFormat.parse(tmp);
                item.setCreated(published);
                persistance.update(item);
            }
        }
    }
}
