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
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.PersistanceException;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;

/**
 * Class for manipulation with Category.<p>
 * <u>Parameters used by EditCategory</u>
 * <dl>
 * <dt><code>PARAM_CATEGORY_ID</code></dt>
 * <dd>Id of Category, that will be affected by changes.</dd>
 * <dt><code>PARAM_UPPER_RELATION</code></dt>
 * <dd>For add context, id of upper level relation.</dd>
 * <dt><code>PARAM_NAME</code></dt>
 * <dd>Specifies name of this category.</dd>
 * <dt><code>PARAM_OPEN</code></dt>
 * <dd>Specifies, whether normal user may add content to this category.</dd>
 * <dt><code>PARAM_ICON</code></dt>
 * <dd>Specifies name of the icon assigned to this category.</dd>
 * <dt><code>PARAM_NOTE</code></dt>
 * <dd>Specifies note related to this category.</dd>
 * </dl>
 */
public class EditCategory extends AbcServlet {
    public static final String PARAM_CATEGORY_ID = "categoryId";
    public static final String PARAM_UPPER_RELATION = "upperRelation";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_OPEN = "open";
    public static final String PARAM_ICON = "icon";
    public static final String PARAM_NOTE = "note";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_LINK = "link";
    public static final String ACTION_REMOVE = "remove";


    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        String action = request.getParameter(AbcServlet.PARAM_ACTION);
        if ( action==null || action.equals(EditCategory.ACTION_ADD) ) {
            // check rights
            return getTemplate("add/category.vm");
        } else if ( action.equals(EditCategory.ACTION_ADD_STEP2) ) {
            actionAddStep2(request,response,ctx);
        } else if ( action.equals(EditCategory.ACTION_EDIT) ) {
        } else if ( action.equals(EditCategory.ACTION_LINK) ) {
        } else if ( action.equals(EditCategory.ACTION_REMOVE) ) {
        }
        // check rights
        return getTemplate("add/category.vm");
    }

    /**
     * Creates new category
     */
    protected Template actionAddStep2(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        int upperCategory = 0, upperRelation = 0;
        String tmp = request.getParameter(EditCategory.PARAM_CATEGORY_ID);
        if ( tmp!=null ) { try { upperCategory = Integer.parseInt(tmp); } catch (NumberFormatException e) {} }
        if ( upperCategory==0 ) {
            throw new Exception("Chybí parametry!");
        }
        tmp = request.getParameter(EditCategory.PARAM_UPPER_RELATION);
        if ( tmp!=null ) { try { upperRelation = Integer.parseInt(tmp); } catch (NumberFormatException e) {} }

        User user = (User) ctx.get(AbcServlet.VAR_USER);
        String name = request.getParameter(EditCategory.PARAM_NAME);
        String icon = request.getParameter(EditCategory.PARAM_ICON);
        String open = request.getParameter(EditCategory.PARAM_OPEN);
        String note = request.getParameter(EditCategory.PARAM_NOTE);
        if ( name!=null) name = name.trim();
        if ( note!=null) note = note.trim();

        if ( name==null || name.length()==0 ) {
            addErrorMessage(EditCategory.PARAM_NAME,"Nezadal jste jméno kategorie!",ctx);
            return getTemplate("add/category.vm");
        }

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        root.addElement("name").addText(name);
        if ( icon!=null && icon.length()>0 ) root.addElement("icon").addText(icon);
        if ( note!=null && note.length()>0 ) root.addElement("note").addText(note);
        document.setRootElement(root);

        Category category = new Category();
        category.setOpen("yes".equals(open));
        category.setData(document);
        if ( user!=null) category.setOwner(user.getId());
        Relation relation = null;

        try {
            PersistanceFactory.getPersistance().create(category);
            relation = new Relation(new Category(upperCategory),category,upperRelation);
            PersistanceFactory.getPersistance().create(relation);
        } catch (PersistanceException e) {
            addErrorMessage(null,e.getMessage(),ctx);
            return getTemplate("add/category.vm");
        }

        redirect("/ViewRelation?relationId="+relation.getId(),response,ctx);
        return null;
    }
}
