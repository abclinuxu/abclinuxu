/*
 * User: literakl
 * Date: Dec 17, 2001
 * Time: 7:45:13 AM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.webwork.category;

import java.util.Iterator;
import webwork.action.Action;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;

/**
 * Actions for viewing content of Category.
 */
public class ViewCategory implements Action {
    Category category;
    int categoryId;

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setCategoryId(int id) {
        category = new Category(id);
    }

    public String execute() throws Exception {
        String url = System.getProperty("PERSISTANCE_URL");
        Persistance persistance = PersistanceFactory.getPersistance(url);

        category = (Category) persistance.findById(category);
        for (Iterator iter = category.getContent().iterator(); iter.hasNext();) {
            GenericObject object = (GenericObject) iter.next();
            persistance.synchronize(object);
        }
        return Action.SUCCESS;
    }
}
