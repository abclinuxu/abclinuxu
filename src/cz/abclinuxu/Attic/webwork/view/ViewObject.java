/*
 * User: literakl
 * Date: Dec 20, 2001
 * Time: 3:34:09 PM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.webwork.view;

import java.util.Iterator;
import java.util.List;
import webwork.action.Action;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.data.*;

/**
 * This Action provides uniform read access to all objects in model.
 * It can be used two ways:<ul>
 * <li>by specifying <code>parent</code>
 * <li>by specifying content
 * </ul>
 *  You shall not mix these modes!<p>
 * Ad 1\ call setParentById() to initialize parent. Then execute() will lookup
 * this GenericObject in database. It will also fill <code>content</code>.<p>
 * Ad 2\ call setContentById(). The execute() method will then skip look up,
 *  but it will initialize values of objects in <code>content</code>. You can't
 * use getParent methods!
 */
public class ViewObject implements Action {
    GenericObject parent;
    List content;

    public void setParentById(Class parentClass,int id) {
        try {
            parent = (GenericObject) parentClass.newInstance();
            parent.setId(id);
        } catch (Exception e) {
        }
    }

    public String execute() throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        parent = persistance.findById(parent);

        for (Iterator iter = parent.getContent().iterator(); iter.hasNext();) {
            GenericObject object = (GenericObject) iter.next();
            persistance.synchronize(object);
        }
        return Action.SUCCESS;
    }

    public GenericObject getParent() {
        return parent;
    }
}
