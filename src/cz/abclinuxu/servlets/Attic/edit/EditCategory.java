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
import cz.abclinuxu.servlets.view.ViewIcons;
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
import java.util.Map;

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
    public static final String PARAM_ICON = ViewIcons.PARAM_ICON;
    public static final String PARAM_NOTE = "note";


    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_LINK = "link";
    public static final String ACTION_REMOVE = "remove";


    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        String tmp = (String) ((Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS)).get(EditCategory.PARAM_CATEGORY_ID);
        int categoryId = Integer.parseInt(tmp);

        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        String action = (String) params.get(AbcServlet.PARAM_ACTION);

        if ( action==null || action.equals(EditCategory.ACTION_ADD) ) {
            int rights = checkAccess(new Category(categoryId),AbcServlet.METHOD_ADD,ctx);
            switch (rights) {
                case AbcServlet.LOGIN_REQUIRED: return getTemplate("login.vm");
                case AbcServlet.USER_INSUFFICIENT_RIGHTS: addErrorMessage(null,"Vase prava nejsou dostatecna pro tuto operaci!",ctx);
                default: return getTemplate("add/category.vm");
            }
        } else if ( action.equals(EditCategory.ACTION_ADD_STEP2) ) {
            int rights = checkAccess(new Category(categoryId),AbcServlet.METHOD_ADD,ctx);
            switch (rights) {
                case AbcServlet.LOGIN_REQUIRED: return getTemplate("login.vm");
                case AbcServlet.USER_INSUFFICIENT_RIGHTS: {
                    addErrorMessage(AbcServlet.GENERIC_ERROR,"Vase prava nejsou dostatecna pro tuto operaci!",ctx);
                    return getTemplate("add/category.vm");
                }
                default: return actionAddStep2(request,response,ctx);
            }
        } else if ( action.equals(EditCategory.ACTION_EDIT) ) {
        } else if ( action.equals(EditCategory.ACTION_LINK) ) {
        } else if ( action.equals(EditCategory.ACTION_REMOVE) ) {
        }
        return getTemplate("add/category.vm");
    }

    /**
     * Creates new category
     */
    protected Template actionAddStep2(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        int upperCategory = 0, upperRelation = 0;
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);

        String tmp = (String) params.get(EditCategory.PARAM_CATEGORY_ID);
        if ( tmp!=null ) upperCategory = Integer.parseInt(tmp);
        if ( upperCategory==0 ) {
            throw new Exception("Chybí parametry!");
        }
        tmp = (String) params.get(EditCategory.PARAM_UPPER_RELATION);
        if ( tmp!=null ) { try { upperRelation = Integer.parseInt(tmp); } catch (NumberFormatException e) {} }

        User user = (User) ctx.get(AbcServlet.VAR_USER);
        String name = (String) params.get(EditCategory.PARAM_NAME);
        String icon = (String) params.get(EditCategory.PARAM_ICON);
        String open = (String) params.get(EditCategory.PARAM_OPEN);
        String note = (String) params.get(EditCategory.PARAM_NOTE);
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
        category.setOwner(user.getId());
        Relation relation = null;

        try {
            PersistanceFactory.getPersistance().create(category);
            relation = new Relation(new Category(upperCategory),category,upperRelation);
            PersistanceFactory.getPersistance().create(relation);
        } catch (PersistanceException e) {
            addErrorMessage(AbcServlet.GENERIC_ERROR,e.getMessage(),ctx);
            return getTemplate("add/category.vm");
        }

        redirect("/ViewRelation?relationId="+relation.getId(),response,ctx);
        return null;
    }
}
