/*
 * Created by IntelliJ IDEA.
 * User: literakl
 * Date: Jan 5, 2002
 * Time: 9:31:40 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.servlets.view.SelectIcon;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.PersistanceException;
import cz.abclinuxu.persistance.Persistance;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import java.util.Map;

/**
 * Class for manipulation with Category.<p>
 * <u>Parameters used by EditCategory</u>
 * <dl>
 * <dt><code>PARAM_RELATION</code></dt>
 * <dd>Relation, where current category is child.</dd>
 * <dt><code>PARAM_NAME</code></dt>
 * <dd>Specifies name of this category.</dd>
 * <dt><code>PARAM_OPEN</code></dt>
 * <dd>Specifies, whether normal user may add content to this category.</dd>
 * <dt><code>PARAM_ICON</code></dt>
 * <dd>Specifies name of the icon assigned to this category.</dd>
 * <dt><code>PARAM_NOTE</code></dt>
 * <dd>Specifies note related to this category.</dd>
 * <dt><code>PARAM_ICON_CHOOSER</code></dt>
 * <dd>If user selects Choose Icon button in edit context.</dd>
 * <p><u>Parameters used by EditCategory</u>
 * <dl>
 * <dt><code>VAR_RELATION</code></dt>
 * <dd>Relation, where current category is child.</dd>
 * </dl>
 */
public class EditCategory extends AbcServlet {
    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_OPEN = "open";
    public static final String PARAM_ICON = SelectIcon.PARAM_ICON;
    public static final String PARAM_NOTE = "note";
    public static final String PARAM_CHOOSE_ICON = "iconChooser";

    public static final String VAR_RELATION = "RELATION";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT2 = "edit2";


    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        String action = (String) params.get(AbcServlet.PARAM_ACTION);

        String tmp = (String) params.get(EditCategory.PARAM_RELATION);
        int relationId = Integer.parseInt(tmp);
        Relation relation = (Relation) PersistanceFactory.getPersistance().findById(new Relation(relationId));
        ctx.put(EditCategory.VAR_RELATION,relation);

        if ( action==null || action.equals(EditCategory.ACTION_ADD) ) {
            int rights = checkAccess(relation.getChild(),AbcServlet.METHOD_ADD,ctx);
            switch (rights) {
                case AbcServlet.LOGIN_REQUIRED: return getTemplate("login.vm");
                case AbcServlet.USER_INSUFFICIENT_RIGHTS: addErrorMessage(AbcServlet.GENERIC_ERROR,"Vase prava nejsou dostatecna pro tuto operaci!",ctx);
                default: return getTemplate("add/category.vm");
            }

        } else if ( action.equals(EditCategory.ACTION_ADD_STEP2) ) {
            int rights = checkAccess(relation.getChild(),AbcServlet.METHOD_ADD,ctx);
            switch (rights) {
                case AbcServlet.LOGIN_REQUIRED: return getTemplate("login.vm");
                case AbcServlet.USER_INSUFFICIENT_RIGHTS: {
                    addErrorMessage(AbcServlet.GENERIC_ERROR,"Vase prava nejsou dostatecna pro tuto operaci!",ctx);
                    return getTemplate("add/category.vm");
                }
                default: return actionAddStep2(request,response,ctx);
            }

        } else if ( action.equals(EditCategory.ACTION_EDIT) ) {
            int rights = checkAccess(relation.getChild(),AbcServlet.METHOD_EDIT,ctx);
            switch (rights) {
                case AbcServlet.LOGIN_REQUIRED: return getTemplate("login.vm");
                case AbcServlet.USER_INSUFFICIENT_RIGHTS: addErrorMessage(AbcServlet.GENERIC_ERROR,"Vase prava nejsou dostatecna pro tuto operaci!",ctx);
                default: return actionEditStep1(request,ctx);
            }

        } else if ( action.equals(EditCategory.ACTION_EDIT2) ) {
            int rights = checkAccess(relation.getChild(),AbcServlet.METHOD_EDIT,ctx);
            switch (rights) {
                case AbcServlet.LOGIN_REQUIRED: return getTemplate("login.vm");
                case AbcServlet.USER_INSUFFICIENT_RIGHTS: addErrorMessage(AbcServlet.GENERIC_ERROR,"Vase prava nejsou dostatecna pro tuto operaci!",ctx);
                default: return actionEditStep2(request,response,ctx);
            }

        }
        return getTemplate("add/category.vm");
    }

    /**
     * Creates new category
     */
    protected Template actionAddStep2(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);

        String name = (String) params.get(EditCategory.PARAM_NAME);
        String icon = (String) params.get(EditCategory.PARAM_ICON);
        String open = (String) params.get(EditCategory.PARAM_OPEN);
        String note = (String) params.get(EditCategory.PARAM_NOTE);
        if ( name!=null) name = name.trim();
        if ( note!=null) note = note.trim();

        if ( name==null || name.length()==0 ) {
            addErrorMessage(EditCategory.PARAM_NAME,"Nezadal jste jm�no kategorie!",ctx);
            return getTemplate("add/category.vm");
        }

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        root.addElement("name").addText(name);
        if ( icon!=null && icon.length()>0 ) root.addElement("icon").addText(icon);
        if ( note!=null && note.length()>0 ) root.addElement("note").addText(note);
        document.setRootElement(root);

        Relation upperRelation = (Relation) ctx.get(EditCategory.VAR_RELATION);
        User user = (User) ctx.get(AbcServlet.VAR_USER);
        Category category = new Category();

        category.setOpen("yes".equals(open));
        category.setData(document);
        category.setOwner(user.getId());
        Relation relation = null;

        try {
            PersistanceFactory.getPersistance().create(category);
            relation = new Relation(upperRelation.getChild(),category,upperRelation.getId());
            PersistanceFactory.getPersistance().create(relation);
        } catch (PersistanceException e) {
            addErrorMessage(AbcServlet.GENERIC_ERROR,e.getMessage(),ctx);
            return getTemplate("add/category.vm");
        }

        redirect("/ViewRelation?relationId="+relation.getId(),response,ctx);
        return null;
    }

    /**
     * First step for editing of category
     */
    protected Template actionEditStep1(HttpServletRequest request, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);

        Relation upperRelation = (Relation) ctx.get(EditCategory.VAR_RELATION);
        Category category = (Category) upperRelation.getChild();
        PersistanceFactory.getPersistance().synchronize(category);
        Document document = category.getData();

        String tmp = (String) params.get(AbcServlet.PARAM_ACTION);
        if ( EditCategory.ACTION_EDIT.equals(tmp) ) {
            Node node = document.selectSingleNode("data/name");
            if (node!=null) params.put(EditCategory.PARAM_NAME,node.getText());

            node = document.selectSingleNode("data/icon");
            if (node!=null) params.put(EditCategory.PARAM_ICON,node.getText());

            node = document.selectSingleNode("data/note");
            if (node!=null) params.put(EditCategory.PARAM_NOTE,node.getText());

            params.put(EditCategory.PARAM_OPEN, (category.isOpen())? "yes":"no");
        }

        return getTemplate("edit/category.vm");
    }

    /**
     * Final step for editing of category
     */
    protected Template actionEditStep2(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        String tmp = (String) params.get(EditCategory.PARAM_CHOOSE_ICON);
        if ( tmp!=null && tmp.length()>0 ) {
            // it is not possible to use AbcServlet.dispatch(), because it would prepend prefix!
            RequestDispatcher dispatcher = request.getRequestDispatcher("/SelectIcon");
            dispatcher.forward(request,response);
            return null;
        }

        Relation upperRelation = (Relation) ctx.get(EditCategory.VAR_RELATION);
        Category category = (Category) upperRelation.getChild();
        persistance.synchronize(category);
        Document document = category.getData();

        Node node = document.selectSingleNode("data/name");
        tmp = (String) params.get(EditCategory.PARAM_NAME);
        if (node!=null && tmp!=null) node.setText(tmp);

        node = document.selectSingleNode("data/icon");
        tmp = (String) params.get(EditCategory.PARAM_ICON);
        if (node!=null && tmp!=null) node.setText(tmp);

        node = document.selectSingleNode("data/note");
        tmp = (String) params.get(EditCategory.PARAM_NOTE);
        if (node!=null && tmp!=null) node.setText(tmp);

        tmp = (String) params.get(EditCategory.PARAM_OPEN);
        category.setOpen( "yes".equals(tmp) );

        persistance.update(category);

        redirect("/ViewRelation?relationId="+upperRelation.getId(),response,ctx);
        return null;
    }
}
