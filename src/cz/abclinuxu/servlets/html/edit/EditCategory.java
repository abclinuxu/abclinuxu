/*
 *  Copyright (C) 2005 Leos Literak
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
package cz.abclinuxu.servlets.html.edit;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.exceptions.PersistanceException;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.format.Format;
import cz.abclinuxu.utils.format.FormatDetector;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import java.util.Map;

/**
 * Class for manipulation with Category.
 */
public class EditCategory implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditCategory.class);

    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_CATEGORY = "categoryId";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_OPEN = "open";
    public static final String PARAM_ICON = cz.abclinuxu.servlets.html.select.SelectIcon.PARAM_ICON;
    public static final String PARAM_NOTE = "note";
    public static final String PARAM_CHOOSE_ICON = "iconChooser";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_CATEGORY = "CATEGORY";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT2 = "edit2";


    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        Category category = (Category) InstanceUtils.instantiateParam(PARAM_CATEGORY,Category.class,params, request);
        if ( category!=null ) {
            category = (Category) persistance.findById(category);
            env.put(VAR_CATEGORY,category);
        }

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if ( relation!=null ) {
            relation = (Relation) persistance.findById(relation);
            category = (Category) relation.getChild();
            env.put(VAR_RELATION,relation);
            env.put(VAR_CATEGORY,category);
        }

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        if ( !user.hasRole(Roles.CATEGORY_ADMIN) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if ( ACTION_ADD.equals(action) )
            return FMTemplateSelector.select("EditCategory", "add", env, request);

        if ( ACTION_ADD_STEP2.equals(action) )
            return actionAddStep2(request, response, env);

        if ( ACTION_EDIT.equals(action) )
            return actionEditStep1(request, env);

        if ( ACTION_EDIT2.equals(action) )
            return actionEditStep2(request, response, env);

        throw new MissingArgumentException("Chybí parametr action!");
    }

    /**
     * Creates new category
     */
    protected String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        String name = (String) params.get(PARAM_NAME);
        String icon = (String) params.get(PARAM_ICON);
        String open = (String) params.get(PARAM_OPEN);
        String note = (String) params.get(PARAM_NOTE);

        if ( name==null || name.length()==0 ) {
            ServletUtils.addError(PARAM_NAME,"Zadejte jméno kategorie!",env, null);
            return FMTemplateSelector.select("EditCategory","add",env,request);
        }

        Relation upperRelation = (Relation) env.get(VAR_RELATION);
        Category upperCategory = (Category) env.get(VAR_CATEGORY);
        User user = (User) env.get(Constants.VAR_USER);

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        root.addElement("name").addText(name);
        if ( icon!=null && icon.length()>0 ) root.addElement("icon").addText(icon);
        if ( note!=null && note.length()>0 ) {
            Element element = root.addElement("note");
            element.addText(note);
            Format format = FormatDetector.detect(note);
            element.addAttribute("format", Integer.toString(format.getId()));
        }
        document.setRootElement(root);

        Category category = new Category();
        category.setOpen("yes".equals(open));
        category.setData(document);
        category.setOwner(user.getId());
        Relation relation = null;

        try {
            persistance.create(category);
            int upper = (upperRelation!=null)? upperRelation.getId():0;
            relation = new Relation(upperCategory,category,upper);
            persistance.create(relation);
            relation.getParent().addChildRelation(relation);
        } catch (PersistanceException e) {
            ServletUtils.addError(Constants.ERROR_GENERIC,e.getMessage(),env, null);
            return FMTemplateSelector.select("EditCategory","add",env,request);
        }

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/dir/"+relation.getId());
        return null;
    }

    /**
     * First step for editing of category
     */
    protected String actionEditStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Category category = (Category) env.get(VAR_CATEGORY);
        persistance.synchronize(category);
        Document document = category.getData();
        Node node = document.selectSingleNode("data/name");
        if (node!=null) params.put(PARAM_NAME,node.getText());
        node = document.selectSingleNode("data/icon");
        if (node!=null) params.put(PARAM_ICON,node.getText());
        node = document.selectSingleNode("data/note");
        if (node!=null) params.put(PARAM_NOTE,node.getText());
        int type = category.getType();
        if (type==Category.OPEN_CATEGORY || type==Category.CLOSED_CATEGORY)
            params.put(PARAM_OPEN, (category.isOpen())? "yes":"no");

        return FMTemplateSelector.select("EditCategory","edit",env,request);
    }

    /**
     * Final step for editing of category
     */
    protected String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        String tmp = (String) params.get(PARAM_CHOOSE_ICON);
        if ( tmp!=null && tmp.length()>0 ) {
            // it is not possible to use UrlUtils.dispatch(), because it would prepend prefix!
            RequestDispatcher dispatcher = request.getRequestDispatcher("/SelectIcon");
            dispatcher.forward(request,response);
            return null;
        }

        Relation relation = (Relation) env.get(VAR_RELATION);
        Category category = (Category) env.get(VAR_CATEGORY);
        persistance.synchronize(category);
        Document document = category.getData();
        Element node = DocumentHelper.makeElement(document,"data/name");
        tmp = (String) params.get(PARAM_NAME);
        node.setText(tmp);

        node = DocumentHelper.makeElement(document,"data/icon");
        tmp = (String) params.get(PARAM_ICON);
        node.setText(tmp);

        node = DocumentHelper.makeElement(document,"data/note");
        tmp = (String) params.get(PARAM_NOTE);
        node.setText(tmp);
        Format format = FormatDetector.detect(tmp);
        node.addAttribute("format", Integer.toString(format.getId()));

        tmp = (String) params.get(PARAM_OPEN);
        if (!Misc.empty(tmp))
            category.setOpen( "yes".equals(tmp) );

        persistance.update(category);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        if ( relation!=null ) {
            if (relation.getUrl()!=null)
                urlUtils.redirect(response, relation.getUrl());
            else
                urlUtils.redirect(response, "/dir/"+relation.getId());
        } else {
            urlUtils.redirect(response, "/dir?categoryId="+category.getId());
        }
        return null;
    }
}
