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
import cz.abclinuxu.servlets.utils.TextUtils;
import cz.abclinuxu.servlets.view.SelectIcon;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.PersistanceException;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.security.Guard;
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
 * <dt><code>PARAM_CATEGORY</code></dt>
 * <dd>Current category.</dd>
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
    public static final String PARAM_CATEGORY = "categoryId";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_OPEN = "open";
    public static final String PARAM_ICON = SelectIcon.PARAM_ICON;
    public static final String PARAM_NOTE = "note";
    public static final String PARAM_CHOOSE_ICON = "iconChooser";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_CATEGORY = "CATEGORY";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT2 = "edit2";


    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Relation relation = null;
        Category category = null;

        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        String action = (String) params.get(AbcServlet.PARAM_ACTION);

        category = (Category) instantiateParam(EditCategory.PARAM_CATEGORY,Category.class,params);
        if ( category!=null ) {
            category = (Category) PersistanceFactory.getPersistance().findById(category);
            ctx.put(EditCategory.VAR_CATEGORY,category);
        }

        relation = (Relation) instantiateParam(EditCategory.PARAM_RELATION,Relation.class,params);
        if ( relation!=null ) {
            relation = (Relation) PersistanceFactory.getPersistance().findById(relation);
            ctx.put(EditCategory.VAR_RELATION,relation);
            category = (Category) relation.getChild();
            ctx.put(EditCategory.VAR_CATEGORY,category);
        }

        if ( action==null || action.equals(EditCategory.ACTION_ADD) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),category,Guard.OPERATION_ADD,Category.class);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return getTemplate("login.vm");
                case Guard.ACCESS_DENIED: addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx, null);
                default: return getTemplate("add/category.vm");
            }

        } else if ( action.equals(EditCategory.ACTION_ADD_STEP2) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),category,Guard.OPERATION_ADD,Category.class);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return getTemplate("login.vm");
                case Guard.ACCESS_DENIED: {
                    addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx, null);
                    return getTemplate("add/category.vm");
                }
                default: return actionAddStep2(request,response,ctx);
            }

        } else if ( action.equals(EditCategory.ACTION_EDIT) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),category,Guard.OPERATION_EDIT,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return getTemplate("login.vm");
                case Guard.ACCESS_DENIED: addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx, null);
                default: return actionEditStep1(request,ctx);
            }

        } else if ( action.equals(EditCategory.ACTION_EDIT2) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),category,Guard.OPERATION_EDIT,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return getTemplate("login.vm");
                case Guard.ACCESS_DENIED: addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx, null);
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

        if ( name==null || name.length()==0 ) {
            addError(EditCategory.PARAM_NAME,"Nezadal jste jméno kategorie!",ctx, null);
            return getTemplate("add/category.vm");
        }

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        root.addElement("name").addText(name);
        if ( icon!=null && icon.length()>0 ) root.addElement("icon").addText(icon);
        if ( note!=null && note.length()>0 ) root.addElement("note").addText(TextUtils.fixLines(note));
        document.setRootElement(root);

        Relation upperRelation = (Relation) ctx.get(EditCategory.VAR_RELATION);
        Category upperCategory = (Category) ctx.get(EditCategory.VAR_CATEGORY);
        User user = (User) ctx.get(AbcServlet.VAR_USER);
        Category category = new Category();

        category.setOpen("yes".equals(open));
        category.setData(document);
        category.setOwner(user.getId());
        Relation relation = null;

        try {
            PersistanceFactory.getPersistance().create(category);
            int upper = (upperRelation!=null)? upperRelation.getId():0;
            relation = new Relation(upperCategory,category,upper);
            PersistanceFactory.getPersistance().create(relation);
        } catch (PersistanceException e) {
            addError(AbcServlet.GENERIC_ERROR,e.getMessage(),ctx, null);
            return getTemplate("add/category.vm");
        }

        redirect("/ViewRelation?relationId="+relation.getId(),response,ctx);
        return null;
    }

    /**
     * First step for editing of category
     * @todo verify logic of ACTION check
     */
    protected Template actionEditStep1(HttpServletRequest request, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);

        Category category = (Category) ctx.get(EditCategory.VAR_CATEGORY);
        PersistanceFactory.getPersistance().synchronize(category);
        Document document = category.getData();

        String tmp = (String) params.get(AbcServlet.PARAM_ACTION);
        if ( EditCategory.ACTION_EDIT.equals(tmp) ) { // IS NOT THIS CHECK DUPLICATE ?!!?!
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
        Category category = (Category) ctx.get(EditCategory.VAR_CATEGORY);
        persistance.synchronize(category);
        Document document = category.getData();

        Node node = DocumentHelper.makeElement(document,"data/name");
        tmp = (String) params.get(EditCategory.PARAM_NAME);
        node.setText(tmp);

        node = DocumentHelper.makeElement(document,"data/icon");
        tmp = (String) params.get(EditCategory.PARAM_ICON);
        node.setText(tmp);

        node = DocumentHelper.makeElement(document,"data/note");
        tmp = (String) params.get(EditCategory.PARAM_NOTE);
        tmp = TextUtils.fixLines(tmp);
        node.setText(tmp);

        tmp = (String) params.get(EditCategory.PARAM_OPEN);
        category.setOpen( "yes".equals(tmp) );

        persistance.update(category);

        if ( upperRelation!=null ) {
            redirect("/ViewRelation?relationId="+upperRelation.getId(),response,ctx);
        } else {
            redirect("/ViewCategory?categoryId="+category.getId(),response,ctx);
        }
        return null;
    }
}
