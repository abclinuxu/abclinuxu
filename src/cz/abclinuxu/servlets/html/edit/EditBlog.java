/*
 * User: literakl
 * Date: 23.11.2004
 * Time: 8:44:27
 */
package cz.abclinuxu.servlets.html.edit;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.view.ViewUser;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.format.Format;
import cz.abclinuxu.persistance.extra.CompareCondition;
import cz.abclinuxu.persistance.extra.Field;
import cz.abclinuxu.persistance.extra.Operation;
import cz.abclinuxu.persistance.extra.Qualifier;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.security.Roles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.prefs.Preferences;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.dom4j.Element;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import freemarker.template.SimpleHash;

/**
 * Servlet used to manipulate with user blogs.
 */
public class EditBlog implements AbcAction, Configurable {
    public static final String PARAM_BLOG_NAME = "blogName";
    public static final String PARAM_BLOG_ID = "blogId";
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_PAGE_TITLE = "htitle";
    public static final String PARAM_INTRO = "intro";
    public static final String PARAM_CATEGORY_ID = "cid";
    public static final String PARAM_CATEGORY_NAME = "category";
    public static final String PARAM_CONTENT = "content";
    public static final String PARAM_RELATION = "rid";
    public static final String PARAM_PREVIEW = "preview";
    public static final String PARAM_PAGE_SIZE = "pageSize";

    public static final String ACTION_ADD_BLOG = "addBlog";
    public static final String ACTION_ADD_BLOG_STEP2 = "addBlog2";
    public static final String ACTION_CUSTOMIZATION = "custom";
    public static final String ACTION_CUSTOMIZATION_STEP2 = "custom2";
    public static final String ACTION_EDIT_CATEGORIES = "categories";
    public static final String ACTION_ADD_CATEGORY = "addCategory";
    public static final String ACTION_ADD_CATEGORY_STEP2 = "addCategory2";
    public static final String ACTION_EDIT_CATEGORY = "editCategory";
    public static final String ACTION_EDIT_CATEGORY_STEP2 = "editCategory2";
    public static final String ACTION_REMOVE_CATEGORY = "removeCategory";
    public static final String ACTION_REMOVE_CATEGORY_STEP2 = "removeCategory2";
    public static final String ACTION_RENAME_BLOG = "rename";
    public static final String ACTION_RENAME_BLOG_STEP2 = "rename2";
    public static final String ACTION_ADD_STORY = "add";
    public static final String ACTION_ADD_STORY_STEP2 = "add2";
    public static final String ACTION_EDIT_STORY = "edit";
    public static final String ACTION_EDIT_STORY_STEP2 = "edit2";
    public static final String ACTION_REMOVE_STORY = "remove";
    public static final String ACTION_REMOVE_STORY_STEP2 = "remove2";

    public static final String VAR_BLOG = "BLOG";
    public static final String VAR_BLOG_RELATION = "REL_BLOG";
    public static final String VAR_STORY = "STORY";
    /** map, key is id (string), value is human readable name of category */
    public static final String VAR_CATEGORIES = "CATEGORIES";
    public static final String VAR_PREVIEW = "PREVIEW";

    public static final String BREAK_TAG = "<break>";
    public static final String PREF_RE_INVALID_BLOG_NAME = "regexp.invalid.blogname";
    public static final String PREF_MAX_STORY_TITLE_LENGTH = "max.story.title.length";
    public static final String PREF_MAX_STORY_WORD_COUNT = "max.story.word.count";

    static RE reBlogName;
    static int maxStoryTitleLength, maxStoryWordCount;

    static {
        EditBlog instance = new EditBlog();
        ConfigurationManager.getConfigurator().configureAndRememberMe(instance);
    }

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        String action = (String) params.get(PARAM_ACTION);

        if ( ACTION_ADD_BLOG.equals(action) )
            return FMTemplateSelector.select("EditBlog", "addBlog", env, request);

        if ( ACTION_ADD_BLOG_STEP2.equals(action) )
            return actionAddBlog(request, response, env);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
        if ( relation==null )
            throw new MissingArgumentException("Chybí parametr relId!");

        relation = (Relation) persistance.findById(relation);
        Tools.sync(relation);

        Category blog = null;
        Relation blogRelation = null;
        if (relation.getChild() instanceof Category) {
            blog = (Category) relation.getChild();
        } else if (relation.getChild() instanceof Item) {
            blog = (Category) relation.getParent();
            Tools.sync(blog);
            blogRelation = relation;
            env.put(VAR_STORY, blogRelation);
            relation = (Relation) persistance.findById(new Relation(blogRelation.getUpper()));
        }
        env.put(VAR_BLOG, blog);
        env.put(VAR_BLOG_RELATION, relation);

        // check permissions
        User user = (User) env.get(Constants.VAR_USER);
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        if ( user.getId()!=blog.getOwner() && !user.hasRole(Roles.ROOT) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if ( ACTION_ADD_STORY.equals(action) )
            return actionAddStoryStep1(request, blog, env);

        if ( ACTION_ADD_STORY_STEP2.equals(action) )
            return actionAddStoryStep2(request, response, relation, env);

        if ( ACTION_EDIT_STORY.equals(action) )
            return actionEditStoryStep1(request, blog, env);

        if ( ACTION_EDIT_STORY_STEP2.equals(action) )
            return actionEditStoryStep2(request, response, blog, env);

        if ( ACTION_REMOVE_STORY.equals(action) )
            return actionRemoveStoryStep1(request, blogRelation, env);

        if ( ACTION_REMOVE_STORY_STEP2.equals(action) )
            return actionRemoveStoryStep2(request, response, blogRelation, blog, env);

        if ( ACTION_CUSTOMIZATION.equals(action) )
            return actionEditCustomStep1(request, blog, env);

        if ( ACTION_CUSTOMIZATION_STEP2.equals(action) )
            return actionEditCustomStep2(request, response, blog, env);

        if ( ACTION_RENAME_BLOG.equals(action) )
            return actionRenameBlogStep1(request, blog, env);

        if ( ACTION_RENAME_BLOG_STEP2.equals(action) )
            return actionRenameBlogStep2(request, response, blog, env);

        throw new MissingArgumentException("Chybí parametr action!");
    }

    /**
     * Adds category with blog for current user.
     */
    protected String actionAddBlog(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        Persistance persistance = PersistanceFactory.getPersistance();

        Element settings = (Element) user.getData().selectSingleNode("/data/settings");
        if ( settings.element("blog")!=null ) {
            ServletUtils.addError(Constants.ERROR_GENERIC,"Chyba: blog ji¾ existuje!",env,request.getSession());
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/Profile/"+user.getId()+"?action="+ViewUser.ACTION_SHOW_MY_PROFILE);
            return null;
        }

        Category blog = new Category();
        blog.setOwner(user.getId());
        blog.setType(Category.SECTION_BLOG);
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");

        boolean canContinue = setBlogName(params, blog, env);
        canContinue &= setCategories(params, root);

        if ( !canContinue )
            return FMTemplateSelector.select("EditBlog", "addBlog", env, request);

        DocumentHelper.makeElement(root, "/custom/page_title").setText(blog.getSubType());
        DocumentHelper.makeElement(root, "/custom/title").setText(blog.getSubType());
        blog.setData(document);
        persistance.create(blog);

        Relation relation = new Relation(new Category(Constants.CAT_BLOGS), blog, Constants.REL_BLOGS);
        persistance.create(relation);

        DocumentHelper.makeElement(settings,"blog").setText(Integer.toString(blog.getId()));
        persistance.update(user);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/blog/"+blog.getSubType()+"/");
        return null;
    }

    /**
     * First step of submitting single story to the user's blog.
     */
    protected String actionAddStoryStep1(HttpServletRequest request, Category blog, Map env) throws Exception {
        storeCategories(blog, env);
        return FMTemplateSelector.select("EditBlog", "add", env, request);
    }

    /**
     * Final step of submitting single story to the user's blog.
     */
    protected String actionAddStoryStep2(HttpServletRequest request, HttpServletResponse response, Relation blogRelation, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        Persistance persistance = PersistanceFactory.getPersistance();

        Category blog = (Category) blogRelation.getChild();
        Item story = new Item();
        story.setType(Item.BLOG);
        story.setOwner(user.getId());
        Document document = DocumentHelper.createDocument();
        story.setData(document);
        Element root = document.addElement("data");

        boolean canContinue = setStoryTitle(params, root, env);
        canContinue &= setStoryContent(params, root, env);
        canContinue &= setStoryCategory(params, story);

        if ( !canContinue || params.get(PARAM_PREVIEW)!=null ) {
            if ( canContinue ) {
                story.setCreated(new Date());
                story.setInitialized(true);
                env.put(VAR_PREVIEW, story);
            }
            return actionAddStoryStep1(request, blog, env);
        }

        persistance.create(story);
        Relation relation = new Relation(blog, story, blogRelation.getId());
        persistance.create(relation);
        incrementArchiveRecord(blog.getData().getRootElement(), new Date());
        persistance.update(blog);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, Tools.getUrlForBlogStory(blog.getSubType(),story.getCreated(),relation.getId()));
        return null;
    }

    /**
     * First step of editing selected story to the user's blog.
     */
    protected String actionEditStoryStep1(HttpServletRequest request, Category blog, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_STORY);
        Item story = (Item) relation.getChild();
        Document document = story.getData();
        Node node = document.selectSingleNode("/data/name");
        params.put(PARAM_TITLE, node.getText());
        String text = null;
        node = document.selectSingleNode("/data/perex");
        if (node!=null)
            text = node.getText();
        node = document.selectSingleNode("/data/content");
        if (text!=null) {
            text = text + "<break>" + node.getText();
        } else
            text = node.getText();
        params.put(PARAM_CONTENT, text);
        params.put(PARAM_CATEGORY_ID, story.getSubType());

        storeCategories(blog, env);
        return FMTemplateSelector.select("EditBlog", "edit", env, request);
    }

    /**
     * Final step of editing selected story to the user's blog.
     */
    protected String actionEditStoryStep2(HttpServletRequest request, HttpServletResponse response, Category blog, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        storeCategories(blog, env);

        Relation relation = (Relation) env.get(VAR_STORY);
        Item story = (Item) relation.getChild();
        Document document = story.getData();
        Element root = document.getRootElement();

        boolean canContinue = setStoryTitle(params, root, env);
        canContinue &= setStoryContent(params, root, env);
        canContinue &= setStoryCategory(params, story);

        if ( !canContinue || params.get(PARAM_PREVIEW)!=null ) {
            if ( canContinue )
                env.put(VAR_PREVIEW, story);
            return actionEditStoryStep1(request, blog, env);
        }
        persistance.update(story);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, Tools.getUrlForBlogStory(blog.getSubType(),story.getCreated(),relation.getId()));
        return null;
    }

    /**
     * First step of renaming blog.
     */
    protected String actionRenameBlogStep1(HttpServletRequest request, Category blog, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        params.put(PARAM_BLOG_NAME, blog.getSubType());
        return FMTemplateSelector.select("EditBlog", "rename", env, request);
    }

    /**
     * Final step of renaming blog.
     */
    protected String actionRenameBlogStep2(HttpServletRequest request, HttpServletResponse response, Category blog, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        boolean canContinue = setBlogName(params, blog, env);
        if ( !canContinue )
            return actionRenameBlogStep1(request, blog, env);

        Persistance persistance = PersistanceFactory.getPersistance();
        persistance.update(blog);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/blog/"+blog.getSubType()+"/");
        return null;
    }

    /**
     * First step of renaming blog.
     */
    protected String actionRemoveStoryStep1(HttpServletRequest request, Relation story, Map env) throws Exception {
        return FMTemplateSelector.select("EditBlog", "remove", env, request);
    }

    /**
     * Final step of renaming blog.
     */
    protected String actionRemoveStoryStep2(HttpServletRequest request, HttpServletResponse response, Relation story, Category blog, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        persistance.remove(story);
        decrementArchiveRecord(blog.getData().getRootElement(), ((Item)story.getChild()).getCreated());
        persistance.update(blog);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/blog/"+blog.getSubType()+"/");
        return null;
    }

    /**
     * Edits blog customization.
     */
    protected String actionEditCustomStep1(HttpServletRequest request, Category blog, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Document document = blog.getData();
        Node node = document.selectSingleNode("//custom/page_title");
        params.put(PARAM_PAGE_TITLE, node.getText());
        node = document.selectSingleNode("//custom/title");
        if ( node!=null )
            params.put(PARAM_TITLE, node.getText());
        node = document.selectSingleNode("//custom/intro");
        if ( node!=null )
            params.put(PARAM_INTRO, node.getText());
        node = document.selectSingleNode("//settings/page_size");
        if ( node!=null )
            params.put(PARAM_PAGE_SIZE, node.getText());
        return FMTemplateSelector.select("EditBlog", "custom", env, request);
    }

    /**
     * Edits blog customization.
     */
    protected String actionEditCustomStep2(HttpServletRequest request, HttpServletResponse response, Category blog, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Element root = blog.getData().getRootElement();

        boolean canContinue = setPageTitle(params, root, env);
        canContinue &= setBlogTitle(params, root, env);
        canContinue &= setBlogIntro(params, root);
        canContinue &= setPageSize(params, root, env);

        if ( !canContinue )
            return actionEditCustomStep1(request, blog, env);

        persistance.update(blog);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/blog/"+blog.getSubType()+"/");
        return null;
    }

    /**
     * Puts map of categories into environment.
     * @param blog the blog
     * @param env the environment
     */
    public static void storeCategories(Category blog, Map env) {
        Document document = blog.getData();
        List nodes = document.selectNodes("//categories/category");
        Map categories = new LinkedHashMap(nodes.size());
        for (Iterator iter = nodes.iterator(); iter.hasNext();) {
            Element category = (Element) iter.next();
            String id = category.attributeValue("id");
            String name = category.attributeValue("name");
            categories.put(id, name);
        }
        env.put(VAR_CATEGORIES, new SimpleHash(categories));
    }

    // setters

    /**
     * Sets blog name. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param category category to be updated
     * @return false, if there is a major error.
     */
    boolean setBlogName(Map params, Category category, Map env) {
        String name = (String) params.get(PARAM_BLOG_NAME);
        if ( name==null || name.trim().length()<1) {
            ServletUtils.addError(PARAM_BLOG_NAME, "Zadejte jméno blogu!", env, null);
            return false;
        }
        name = name.trim();
        if (reBlogName.match(name)) {
            ServletUtils.addError(PARAM_BLOG_NAME, "Jméno blogu smí obsahovat jen znaky a-z, 0-9 a _!", env, null);
            return false;
        }

        CompareCondition condition = new CompareCondition(Field.SUBTYPE, Operation.EQUAL, name);
        SQLTool sqlTool = SQLTool.getInstance();
        List list = sqlTool.findSectionRelationsWithType(Category.SECTION_BLOG, new Qualifier[]{condition});
        if (list.size()>0) {
            Relation relation = (Relation) list.get(0);
            Category existing = (Category) relation.getChild();
            if (existing.getId()!=category.getId()) {
                ServletUtils.addError(PARAM_BLOG_NAME, "Zadejte jiné jméno blogu, toto se ji¾ pou¾ívá!", env, null);
                return false;
            }
        }

        category.setSubType(name);
        return true;
    }

    /**
     * Sets first categories. Overwrites existing categories, if they exist.
     * Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root XML document
     * @return false, if there is a major error.
     */
    boolean setCategories(Map params, Element root) {
        Element categories = root.element("categories");
        if (categories!=null)
            categories.detach();
        categories = root.addElement("categories");

        int id = 0;
        String name = (String) params.get(PARAM_CATEGORY_NAME+"1");
        if (name!=null) {
            Element category = categories.addElement("category");
            category.addAttribute("id",Integer.toString(id++));
            category.addAttribute("name",name);
        }

        name = (String) params.get(PARAM_CATEGORY_NAME+"2");
        if (name!=null) {
            Element category = categories.addElement("category");
            category.addAttribute("id",Integer.toString(id++));
            category.addAttribute("name",name);
        }

        name = (String) params.get(PARAM_CATEGORY_NAME+"3");
        if (name!=null) {
            Element category = categories.addElement("category");
            category.addAttribute("id",Integer.toString(id++));
            category.addAttribute("name",name);
        }

        return true;
    }

    /**
     * Sets page title for blog. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root XML document
     * @return false, if there is a major error.
     */
    boolean setPageTitle(Map params, Element root, Map env) {
        Element custom = root.element("custom");
        Element title = custom.element("page_title");
        if (title==null)
            title = custom.addElement("page_title");

        String s = (String) params.get(PARAM_PAGE_TITLE);
        if (Misc.empty(s)) {
            ServletUtils.addError(PARAM_PAGE_TITLE, "Prosím zadejte hodnotu.", env, null);
            return false;
        }
        if ( s.indexOf("<")!=-1 ) {
            params.put(PARAM_PAGE_TITLE, "");
            ServletUtils.addError(PARAM_PAGE_TITLE, "Pou¾ití HTML znaèek je v titulku zakázáno!", env, null);
            return false;
        }
        title.setText(s);
        return true;
    }

    /**
     * Sets title for blog. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root XML document
     * @return false, if there is a major error.
     */
    boolean setBlogTitle(Map params, Element root, Map env) {
        Element custom = root.element("custom");
        Element title = custom.element("title");
        String s = (String) params.get(PARAM_TITLE);
        if (Misc.empty(s)) {
            if (title!=null)
                title.detach();
        } else {
            if ( s.indexOf("<")!=-1 ) {
                params.put(PARAM_TITLE, "");
                ServletUtils.addError(PARAM_TITLE, "Pou¾ití HTML znaèek je v titulku zakázáno!", env, null);
                return false;
            }
            if (title==null)
                title = custom.addElement("title");
            title.setText(s);
        }
        return true;
    }

    /**
     * Sets intro for blog. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root XML document
     * @return false, if there is a major error.
     */
    boolean setBlogIntro(Map params, Element root) {
        Element custom = root.element("custom");
        Element intro = custom.element("intro");
        String s = (String) params.get(PARAM_INTRO);
        if (Misc.empty(s)) {
            if (intro!=null)
                intro.detach();
        } else {
            if (intro==null)
                intro = custom.addElement("intro");
            intro.setText(s);
        }
        return true;
    }

    /**
     * Sets page size for blog archive. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root XML document
     * @return false, if there is a major error.
     */
    boolean setPageSize(Map params, Element root, Map env) {
        Element pageSize = DocumentHelper.makeElement(root, "/settings/page_size");
        String s = (String) params.get(PARAM_PAGE_SIZE);
        if (Misc.empty(s)) {
            if (pageSize!=null)
                pageSize.detach();
        } else {
            try {
                Integer.parseInt(s);
            } catch (NumberFormatException e) {
                ServletUtils.addError(PARAM_PAGE_SIZE, "Prosím zadejte celé èíslo.", env, null);
                return false;
            }
            pageSize.setText(s);
        }
        return true;
    }

    /**
     * Sets title for story. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root XML document
     * @return false, if there is a major error.
     */
    boolean setStoryTitle(Map params, Element root, Map env) {
        String s = (String) params.get(PARAM_TITLE);
        if (Misc.empty(s)) {
            ServletUtils.addError(PARAM_TITLE, "Prosím zadejte hodnotu.", env, null);
            return false;
        }
        s = s.trim();
        if (s.length()>maxStoryTitleLength) {
            ServletUtils.addError(PARAM_TITLE, "Prosím zadejte krat¹í titulek. Maximální povolená délka je "+maxStoryTitleLength+".", env, null);
            return false;
        }
        if ( s.indexOf("<")!=-1 ) {
            params.put(PARAM_TITLE, "");
            ServletUtils.addError(PARAM_TITLE, "Pou¾ití HTML znaèek je v titulku zakázáno!", env, null);
            return false;
        }

        Element title = root.element("name");
        if (title==null)
            title = root.addElement("name");
        title.setText(s);
        return true;
    }

    /**
     * Sets content for story. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param root XML document
     * @return false, if there is a major error.
     */
    boolean setStoryContent(Map params, Element root, Map env) {
        String content = (String) params.get(PARAM_CONTENT);
        if (Misc.empty(content)) {
            ServletUtils.addError(PARAM_CONTENT, "Prosím zadejte hodnotu.", env, null);
            return false;
        }

        String stripped = null, perex = null;
        int position = content.indexOf(BREAK_TAG);
        if (position==-1)
            stripped = Tools.removeTags(content);
        else {
            perex = content.substring(0, position);
            content = content.substring(position+BREAK_TAG.length());
            stripped = Tools.removeTags(perex);
        }

        StringTokenizer stk = new StringTokenizer(stripped, " \t\n\r\f,.");
        if (stk.countTokens()>maxStoryWordCount) {
            ServletUtils.addError(PARAM_CONTENT, "Vá¹ zápis je pøíli¹ dlouhý. Rozdìlte jej pomocí znaèky <break> tak, aby perex mìl ménì ne¾ "+maxStoryWordCount+" slov.", env, null);
            return false;
        }

        // todo zkontroluj validitu HTML, ochrana pred XSS

        Element tagPerex = root.element("perex");
        if (position!=-1) {
            if (tagPerex==null)
                tagPerex = root.addElement("perex");
            tagPerex.setText(perex);
            tagPerex.addAttribute("format", Integer.toString(Format.HTML.getId()));
        } else {
            if (tagPerex!=null)
                tagPerex.detach();
        }

        Element tagContent = root.element("content");
        if (tagContent==null)
            tagContent = root.addElement("content");
        tagContent.setText(content);
        tagContent.addAttribute("format", Integer.toString(Format.HTML.getId()));
        return true;
    }

    /**
     * Sets category for story. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param story story to be updated
     * @return false, if there is a major error.
     */
    boolean setStoryCategory(Map params, Item story) {
        String category = (String) params.get(PARAM_CATEGORY_ID);
        story.setSubType(category);
        return true;
    }

    /**
     * Inserts (or increments) archive story counter for this month.
     * @param root
     * @param date
     */
    void incrementArchiveRecord(Element root, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Element archive = root.element("archive");
        if (archive==null)
            archive = root.addElement("archive");
        Element year = (Element) archive.selectSingleNode("year[@value="+calendar.get(Calendar.YEAR)+"]");
        if (year==null) {
            year = archive.addElement("year");
            year.addAttribute("value", Integer.toString(calendar.get(Calendar.YEAR)));
        }
        Element month = (Element) year.selectSingleNode("month[@value="+(calendar.get(Calendar.MONTH)+1)+"]");
        if (month==null) {
            month = year.addElement("month");
            month.addAttribute("value", Integer.toString(calendar.get(Calendar.MONTH)+1));
            month.setText("1");
        } else {
            int count = Misc.parseInt(month.getText(), 1);
            month.setText(Integer.toString(count+1));
        }
    }

    /**
     * Decrements archive story counter for specified date.
     * @param root
     * @param date
     */
    void decrementArchiveRecord(Element root, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Element year = (Element) root.selectSingleNode("//archive/year[@value="+calendar.get(Calendar.YEAR)+"]");
        if (year==null)
            return;
        Element month = (Element) year.selectSingleNode("month[@value="+(calendar.get(Calendar.MONTH)+1)+"]");
        if (month==null)
            return;
        int count = Misc.parseInt(month.getText(), 1);
        if (count>1)
            month.setText(Integer.toString(count-1));
        else {
            month.detach();
            if (year.elements().size()==0)
                year.detach();
        }
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        maxStoryTitleLength = prefs.getInt(PREF_MAX_STORY_TITLE_LENGTH, maxStoryTitleLength);
        maxStoryWordCount = prefs.getInt(PREF_MAX_STORY_WORD_COUNT, maxStoryWordCount);
        String re = prefs.get(PREF_RE_INVALID_BLOG_NAME, null);
        try {
            reBlogName = new RE(re);
        } catch (RESyntaxException e) {
            throw new ConfigurationException("Cannot compile regular expression '"+re+"' given by "+PREF_RE_INVALID_BLOG_NAME);
        }
    }
}
