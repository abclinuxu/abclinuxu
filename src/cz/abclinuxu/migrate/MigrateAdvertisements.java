/*
 *  Copyright (C) 2008 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */

package cz.abclinuxu.migrate;

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.Advertisement;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 *
 * @author lubos
 */
public class MigrateAdvertisements {
    public void run() {
        Persistence persistence = PersistenceFactory.getPersistence();
        Item dyndata = (Item) persistence.findById(new Item(Constants.ITEM_DYNAMIC_CONFIGURATION));
        Node advertRoot = dyndata.getData().selectSingleNode("/data/advertisement");
        List<Element> positions = advertRoot.selectNodes("position");
        
        Category parent = new Category(Constants.CAT_ADVERTISEMENTS);
        
        for (Element position : positions) {
            String name = position.elementText("name");
            String desc = position.elementText("description");
            String id = position.attributeValue("id");
            String active = position.attributeValue("active");
            
            Item newPosition = new Item();
            Document document = DocumentHelper.createDocument();
            Element root = document.addElement("data");
            
            newPosition.setData(document);
            
            newPosition.setType(Item.ADVERTISEMENT);
            newPosition.setString1(id);
            newPosition.setTitle(name);
            
            if (desc != null)
                DocumentHelper.makeElement(root, "description").setText(desc);
            DocumentHelper.makeElement(root, "active").setText(active);
            
            migrateCodes(position, root);
            
            persistence.create(newPosition);
            
            Relation rel = new Relation(parent, newPosition, Constants.REL_ADVERTISEMENTS);
            persistence.create(rel);
        }
        
        advertRoot.detach();
        persistence.update(dyndata);
    }
    
    private static void migrateCodes(Element position, Element targetRoot) {
        List<Element> codes = position.selectNodes("code");
        Element targetCodes = DocumentHelper.makeElement(targetRoot, "codes");
        
        for (Element code : codes) {
            Element targetCode = targetCodes.addElement("code");
            
            String regexp = code.attributeValue("regexp");
            String description = code.attributeValue("description");
            String dynamic = code.attributeValue("dynamic");
            String htmlCode = code.getText();
            
            if (regexp != null && regexp.length() > 0) {
                targetCode.addAttribute("regexp", regexp);
                String name = regexp;
                
                for (Map.Entry<String,String> entry : Advertisement.standardRegexps.entrySet()) {
                    if (entry.getKey().equals(regexp)) {
                        name = entry.getValue();
                        break;
                    }
                }
                targetCode.addAttribute("name", "Kód pro "+name);
            } else {
                targetCode.addAttribute("name", "Kód pro všechny stránky");
            }
            if (description != null)
                targetCode.addAttribute("description", description);
            
            Element variant = DocumentHelper.makeElement(targetCode, "variants").addElement("variant");
            variant.addAttribute("dynamic", dynamic);
            variant.addAttribute("active", "yes");
            variant.addAttribute("description", "defaultní");
            variant.setText(htmlCode);
        }
    }
    
    public static void main(String[] args) throws Exception {
        MigrateAdvertisements task = new MigrateAdvertisements();
        task.run();
    }
}
