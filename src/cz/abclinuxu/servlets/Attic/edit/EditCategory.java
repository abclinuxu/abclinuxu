/*
 * Created by IntelliJ IDEA.
 * User: literakl
 * Date: Jan 5, 2002
 * Time: 9:31:40 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.view.SelectIcon;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.PersistanceException;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.security.Guard;
import cz.abclinuxu.utils.InstanceUtils;

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
public class EditCategory extends AbcFMServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditCategory.class);

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


    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        String action = (String) params.get(PARAM_ACTION);

        Category category = (Category) InstanceUtils.instantiateParam(PARAM_CATEGORY,Category.class,params);
        if ( category!=null ) {
            category = (Category) persistance.findById(category);
            env.put(VAR_CATEGORY,category);
        }

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION,Relation.class,params);
        if ( relation!=null ) {
            relation = (Relation) persistance.findById(relation);
            category = (Category) relation.getChild();
            env.put(VAR_RELATION,relation);
            env.put(VAR_CATEGORY,category);
        }

        if ( ACTION_ADD.equals(action) ) {
            int rights = Guard.check((User)env.get(Constants.VAR_USER),category,Guard.OPERATION_ADD,Category.class);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return FMTemplateSelector.select("ViewUser","login",env,request);
                case Guard.ACCESS_DENIED: return FMTemplateSelector.select("ViewUser","forbidden",env,request);
                default: return FMTemplateSelector.select("EditCategory","add",env,request);
            }

        } else if ( ACTION_ADD_STEP2.equals(action) ) {
            int rights = Guard.check((User)env.get(Constants.VAR_USER),category,Guard.OPERATION_ADD,Category.class);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return FMTemplateSelector.select("ViewUser","login",env,request);
                case Guard.ACCESS_DENIED: return FMTemplateSelector.select("ViewUser","forbidden",env,request);
                default: return actionAddStep2(request,response,env);
            }

        } else if ( ACTION_EDIT.equals(action) ) {
            int rights = Guard.check((User)env.get(Constants.VAR_USER),category,Guard.OPERATION_EDIT,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return FMTemplateSelector.select("ViewUser","login",env,request);
                case Guard.ACCESS_DENIED: return FMTemplateSelector.select("ViewUser","forbidden",env,request);
                default: return actionEditStep1(request,env);
            }

        } else if ( ACTION_EDIT2.equals(action) ) {
            int rights = Guard.check((User)env.get(Constants.VAR_USER),category,Guard.OPERATION_EDIT,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: FMTemplateSelector.select("ViewUser","login",env,request);
                case Guard.ACCESS_DENIED: return FMTemplateSelector.select("ViewUser","forbidden",env,request);
                default: return actionEditStep2(request,response,env);
            }

        }
        return FMTemplateSelector.select("EditCategory","add",env,request);
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
        if ( note!=null && note.length()>0 ) root.addElement("note").addText(note);
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
        } catch (PersistanceException e) {
            ServletUtils.addError(Constants.ERROR_GENERIC,e.getMessage(),env, null);
            return FMTemplateSelector.select("EditCategory","add",env,request);
        }

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/ViewRelation?relationId="+relation.getId());
        return null;
    }

    /**
     * First step for editing of category
     * @todo verify logic of ACTION check
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

        Relation upperRelation = (Relation) env.get(VAR_RELATION);
        Category category = (Category) env.get(VAR_CATEGORY);
        persistance.synchronize(category);
        Document document = category.getData();
        Node node = DocumentHelper.makeElement(document,"data/name");
        tmp = (String) params.get(PARAM_NAME);
        node.setText(tmp);

        node = DocumentHelper.makeElement(document,"data/icon");
        tmp = (String) params.get(PARAM_ICON);
        node.setText(tmp);

        node = DocumentHelper.makeElement(document,"data/note");
        tmp = (String) params.get(PARAM_NOTE);
        node.setText(tmp);

        tmp = (String) params.get(PARAM_OPEN);
        category.setOpen( "yes".equals(tmp) );

        persistance.update(category);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        if ( upperRelation!=null ) {
            urlUtils.redirect(response, "/ViewRelation?relationId="+upperRelation.getId());
        } else {
            urlUtils.redirect(response, "/ViewCategory?categoryId="+category.getId());
        }
        return null;
    }
}
