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

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.view.ViewUser;
import cz.abclinuxu.servlets.html.view.ShowObject;
import cz.abclinuxu.servlets.html.view.ViewBlog;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.TagTool;
import cz.abclinuxu.utils.parser.safehtml.BlogHTMLGuard;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.format.Format;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.exceptions.InvalidInputException;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.scheduler.VariableFetcher;
import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.utils.email.monitor.*;
import cz.abclinuxu.data.view.BlogCategory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.prefs.Preferences;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.apache.regexp.REProgram;
import org.apache.regexp.RECompiler;
import org.apache.commons.fileupload.FileItem;
import org.dom4j.Element;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.io.DOMWriter;
import org.htmlparser.util.ParserException;
import freemarker.ext.dom.NodeModel;

/**
 * Servlet used to manipulate with user blogs.
 */
public class EditBlog implements AbcAction, Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditBlog.class);

    public static final String PARAM_BLOG_NAME = "blogName";
    public static final String PARAM_BLOG_ID = "blogId";
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_PAGE_TITLE = "htitle";
    public static final String PARAM_INTRO = "intro";
    public static final String PARAM_CATEGORY_ID = "cid";
    public static final String PARAM_CATEGORY_NAME = "category";
    public static final String PARAM_CONTENT = "content";
    public static final String PARAM_CONTENT_FILE = "contentFile";
    public static final String PARAM_RELATION = "rid";
    public static final String PARAM_PREVIEW = "preview";
    public static final String PARAM_DELAY = "delay";
    public static final String PARAM_UPLOAD = "upload";
    public static final String PARAM_WATCH_DISCUSSION = "watchDiz";
    public static final String PARAM_URL = "url";
    public static final String PARAM_POSITION = "position";
    public static final String PARAM_PUBLISH = "publish";

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
    public static final String ACTION_EDIT_LINKS = "links";
    public static final String ACTION_ADD_LINK = "addLink";
    public static final String ACTION_EDIT_LINK = "editLink";
    public static final String ACTION_EDIT_LINK_STEP2 = "editLink2";
    public static final String ACTION_REMOVE_LINK = "rmLink";
    public static final String ACTION_REMOVE_LINK_STEP2 = "rmLink2";
    public static final String ACTION_MOVE_LINK_UP = "mvLinkUp";
    public static final String ACTION_MOVE_LINK_DOWN = "mvLinkDown";
    public static final String ACTION_TOGGLE_DIGEST = "toggleDigest";
    public static final String ACTION_TOGGLE_BAN = "toggleBlogBan";
    public static final String ACTION_PUBLISH_DELAYED = "publish";

    public static final String VAR_BLOG = "BLOG";
    public static final String VAR_BLOG_RELATION = "REL_BLOG";
    public static final String VAR_STORY = "STORY";
    /** map, key is id (string), value is human readable name of category */
    public static final String VAR_CATEGORIES = "CATEGORIES";
    public static final String VAR_PREVIEW = "PREVIEW";
    public static final String VAR_IS_DELAYED = "DELAYED";
    public static final String VAR_RELATION = "RELATION";

    public static final String PREF_RE_INVALID_BLOG_NAME = "regexp.invalid.blogname";
    public static final String PREF_MAX_STORY_TITLE_LENGTH = "max.story.title.length";
    public static final String PREF_MAX_STORY_WORD_COUNT = "max.story.word.count";
    public static final String PREF_FORBIDDEN_CATEGORY_NAMES = "forbidden.category.names";

    static REProgram reBlogName;
    static int maxStoryTitleLength, maxStoryWordCount;
    static List forbiddenCategoryNames;
    private final Pattern breakTagPattern = Pattern.compile("\\<break[ ]*/?\\>", Pattern.CASE_INSENSITIVE);

    static {
        EditBlog instance = new EditBlog();
        ConfigurationManager.getConfigurator().configureAndRememberMe(instance);
    }

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        String action = (String) params.get(PARAM_ACTION);

        if (ServletUtils.handleMaintainance(request, env)) {
            response.sendRedirect(response.encodeRedirectURL("/"));
            return null;
        }

        if ( ACTION_ADD_BLOG.equals(action) )
            return FMTemplateSelector.select("EditBlog", "addBlog", env, request);

        if ( ACTION_ADD_BLOG_STEP2.equals(action) ) {
            ActionProtector.ensureContract(request, EditBlog.class, true, true, true, false);
            return actionAddBlog(request, response, env);
        }

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
        if ( relation==null )
            throw new MissingArgumentException("Chybí parametr relId!");

        relation = (Relation) persistence.findById(relation);
        Tools.sync(relation);

        Category blog = null;
        Relation blogRelation = null;
        if (relation.getChild() instanceof Category) {
            blog = (Category) relation.getChild();
        } else if (relation.getChild() instanceof Item) {
            if ( ! (relation.getParent() instanceof Category))
                throw new InvalidInputException("Tato relace nepatří blogu!");
            blog = (Category) relation.getParent();
            Tools.sync(blog);
            blogRelation = relation;
            env.put(VAR_STORY, blogRelation);
            relation = (Relation) persistence.findById(new Relation(blogRelation.getUpper()));
        }
        env.put(VAR_BLOG, blog);
        env.put(VAR_BLOG_RELATION, relation);

        List parents = persistence.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);

        // check permissions
        User user = (User) env.get(Constants.VAR_USER);
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        if ( ACTION_TOGGLE_DIGEST.equals(action) ) {
            if ( ! user.hasRole(Roles.BLOG_DIGEST_ADMIN) )
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            ActionProtector.ensureContract(request, EditBlog.class, true, false, false, true);
            return actionToggleStoryDigest(response, env);
        }

        if ( ACTION_TOGGLE_BAN.equals(action) ) {
            if ( ! user.hasRole(Roles.BLOG_DIGEST_ADMIN) )
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
            ActionProtector.ensureContract(request, EditBlog.class, true, false, false, true);
            return actionToggleStoryBan(response, env);
        }

        if ( user.getId()!=blog.getOwner() && !user.hasRole(Roles.ROOT) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if ( ACTION_ADD_STORY.equals(action) )
            return actionAddStoryStep1(request, blog, env);

        if ( ACTION_ADD_STORY_STEP2.equals(action) ) {
            ActionProtector.ensureContract(request, EditBlog.class, true, true, true, false);
            return actionAddStoryStep2(request, response, relation, env, true);
        }

        if ( ACTION_EDIT_STORY.equals(action) )
            return actionEditStoryStep1(request, blog, env);

        if ( ACTION_EDIT_STORY_STEP2.equals(action) ) {
            ActionProtector.ensureContract(request, EditBlog.class, true, true, true, false);
            return actionEditStoryStep2(request, response, blog, env);
        }

        if ( ACTION_REMOVE_STORY.equals(action) )
            return actionRemoveStoryStep1(request, response, blogRelation, blog, env);

        if ( ACTION_REMOVE_STORY_STEP2.equals(action) ) {
            ActionProtector.ensureContract(request, EditBlog.class, true, true, true, false);
            return actionRemoveStoryStep2(request, response, blogRelation, blog, env);
        }

        if (ACTION_PUBLISH_DELAYED.equals(action)) {
            ActionProtector.ensureContract(request, EditBlog.class, true, false, false, true);
            return actionPublishDelayedStory(request, response, blog, env);
        }

        if ( ACTION_CUSTOMIZATION.equals(action) )
            return actionEditCustomStep1(request, blog, env);

        if ( ACTION_CUSTOMIZATION_STEP2.equals(action) ) {
            ActionProtector.ensureContract(request, EditBlog.class, true, true, true, false);
            return actionEditCustomStep2(request, response, blog, env);
        }

        if ( ACTION_RENAME_BLOG.equals(action) )
            return actionRenameBlogStep1(request, response, blog, env);

        if ( ACTION_RENAME_BLOG_STEP2.equals(action) ) {
            ActionProtector.ensureContract(request, EditBlog.class, true, true, true, false);
            return actionRenameBlogStep2(request, response, blog, env);
        }

        if (ACTION_EDIT_CATEGORIES.equals(action))
            return actionShowCategories(request, blog, env);

        if (ACTION_ADD_CATEGORY.equals(action))
            return actionAddCategory(request, blog, env);

        if (ACTION_ADD_CATEGORY_STEP2.equals(action)) {
            ActionProtector.ensureContract(request, EditBlog.class, true, true, true, false);
            return actionAddCategoryStep2(request, response, blog, env);
        }

        if (ACTION_EDIT_CATEGORY.equals(action))
            return actionEditCategory(request, response, blog, env);

        if (ACTION_EDIT_CATEGORY_STEP2.equals(action)) {
            ActionProtector.ensureContract(request, EditBlog.class, true, true, true, false);
            return actionEditCategoryStep2(request, response, blog, env);
        }

        if (ACTION_EDIT_LINKS.equals(action))
            return actionShowLinks(request, blog, env);

        if (ACTION_ADD_LINK.equals(action)) {
            ActionProtector.ensureContract(request, EditBlog.class, true, true, true, false);
            return actionAddLink(request, response, blog, env);
        }

        if (ACTION_EDIT_LINK.equals(action))
            return actionEditLink(request, blog, env);

        if (ACTION_EDIT_LINK_STEP2.equals(action)) {
            ActionProtector.ensureContract(request, EditBlog.class, true, true, true, false);
            return actionEditLinkStep2(request, response, blog, env);
        }

        if (ACTION_REMOVE_LINK.equals(action))
            return actionRemoveLink(request, blog, env);

        if (ACTION_REMOVE_LINK_STEP2.equals(action)) {
            ActionProtector.ensureContract(request, EditBlog.class, true, true, true, false);
            return actionRemoveLinkStep2(request, response, blog, env);
        }

        if (ACTION_MOVE_LINK_UP.equals(action)) {
            ActionProtector.ensureContract(request, EditBlog.class, true, true, false, true);
            return actionMoveLink(request, response, blog, true, env);
        }

        if (ACTION_MOVE_LINK_DOWN.equals(action)) {
            ActionProtector.ensureContract(request, EditBlog.class, true, true, false, true);
            return actionMoveLink(request, response, blog, false, env);
        }

        throw new MissingArgumentException("Chybí parametr action!");
    }

    /**
     * Adds category with blog for current user.
     */
    protected String actionAddBlog(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        Persistence persistence = PersistenceFactory.getPersistence();

        Element settings = (Element) user.getData().selectSingleNode("/data/settings");
        if ( settings.element("blog")!=null ) {
            ServletUtils.addError(Constants.ERROR_GENERIC,"Chyba: blog již existuje!",env,request.getSession());
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/Profile/"+user.getId()+"?action="+ViewUser.ACTION_SHOW_MY_PROFILE);
            return null;
        }

        Category blog = new Category();
        blog.setOwner(user.getId());
        blog.setType(Category.BLOG);
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");

        boolean canContinue = setBlogName(params, blog, env);
        canContinue &= setCategories(params, root);

        if ( !canContinue )
            return FMTemplateSelector.select("EditBlog", "addBlog", env, request);

        DocumentHelper.makeElement(root, "/custom/page_title").setText(blog.getSubType());
        blog.setTitle(blog.getSubType());
        blog.setData(document);
        persistence.create(blog);

        Relation relation = new Relation(new Category(Constants.CAT_BLOGS), blog, Constants.REL_BLOGS);
        persistence.create(relation);

        Element element = DocumentHelper.makeElement(settings,"blog");
        element.setText(Integer.toString(blog.getId()));
        element.addAttribute("name", blog.getSubType());
        persistence.update(user);

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
    public String actionAddStoryStep2(HttpServletRequest request, HttpServletResponse response, Relation blogRelation, Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        Persistence persistence = PersistenceFactory.getPersistence();

        Category blog = (Category) blogRelation.getChild();
        Item story = new Item();
        story.setType(Item.BLOG);
        story.setOwner(user.getId());
        Document document = DocumentHelper.createDocument();
        story.setData(document);
        Element root = document.addElement("data");

        boolean uploading = params.containsKey(PARAM_UPLOAD);
        if (uploading) {
            setStoryContentFromFile(params, env);
            return actionAddStoryStep1(request, blog, env);
        }

        boolean canContinue = setStoryTitle(params, story, env);
        canContinue &= setStoryContent(params, root, env);
        canContinue &= setStoryCategory(params, story);
        canContinue &= setStoryPublishTime(params, story, env);

        if ( !canContinue || params.get(PARAM_PREVIEW)!=null ) {
            if ( canContinue ) {
                story.setCreated(new Date());
                story.setInitialized(true);
                env.put(VAR_PREVIEW, story);
            }
            return actionAddStoryStep1(request, blog, env);
        }

        boolean delayed = params.get(PARAM_DELAY)!=null;
        if (delayed)
            story.setType(Item.UNPUBLISHED_BLOG);

        persistence.create(story);
        Relation relation = new Relation(blog, story, blogRelation.getId());
        persistence.create(relation);

        Relation dizRelation = EditDiscussion.createEmptyDiscussion(relation, user, persistence);

        String watchDiscussion = (String) params.get(PARAM_WATCH_DISCUSSION);
        if ("yes".equals(watchDiscussion))
            EditDiscussion.alterDiscussionMonitor((Item) dizRelation.getChild(), user, persistence);

        String storyUrl = null;
        if (!delayed) {
            storyUrl = generateStoryURL(blog, story);

            if (storyUrl != null) {
                relation.setUrl(storyUrl);
                persistence.update(relation);
            }

            TagTool.assignDetectedTags(story, user);

            incrementArchiveRecord(blog.getData().getRootElement(), new Date());
            persistence.update(blog);
            FeedGenerator.updateBlog(blog);
            VariableFetcher.getInstance().refreshStories();
            sendDigestMessage(relation);

            // run monitor
            MonitorAction action = new MonitorAction(user, UserAction.ADD, ObjectType.BLOG, relation, "http://www.abclinuxu.cz"+storyUrl);
            MonitorPool.scheduleMonitorAction(action);
        } else {
            Element unpublished = DocumentHelper.makeElement(blog.getData(), "/data/unpublished");
            unpublished.addElement("rid").setText(Integer.toString(relation.getId()));
            persistence.update(blog);
        }

        if (storyUrl == null)
            storyUrl = Tools.getUrlForBlogStory(blog.getSubType(), story.getCreated(), relation.getId());

        if (redirect) {
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, storyUrl);
        } else
            env.put(VAR_RELATION, relation);
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
        params.put(PARAM_TITLE, story.getTitle());
        String text = null;
        Node node = document.selectSingleNode("/data/perex");
        if (node!=null)
            text = node.getText();
        node = document.selectSingleNode("/data/content");
        if (text!=null) {
            text = text + "<break>" + node.getText();
        } else
            text = node.getText();
        params.put(PARAM_CONTENT, text);
        params.put(PARAM_CATEGORY_ID, story.getSubType());
        
        Date created = story.getCreated();
        Date now = Calendar.getInstance().getTime();
        
        if (created.compareTo(now) > 0) {
            String date = Constants.isoFormat.format(created);
            params.put(PARAM_PUBLISH, date);
        }
        
        env.put(VAR_IS_DELAYED, Boolean.valueOf(story.getType() == Item.UNPUBLISHED_BLOG));

        storeCategories(blog, env);
        return FMTemplateSelector.select("EditBlog", "edit", env, request);
    }

    /**
     * Final step of editing selected story to the user's blog.
     */
    protected String actionEditStoryStep2(HttpServletRequest request, HttpServletResponse response, Category blog, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);
        storeCategories(blog, env);

        Relation relation = (Relation) env.get(VAR_STORY);
        Item story = (Item) relation.getChild();
        Document document = story.getData();
        Element root = document.getRootElement();
        
        Date previousDate = story.getCreated();

        boolean canContinue = setStoryTitle(params, story, env);
        canContinue &= setStoryContent(params, root, env);
        canContinue &= setStoryCategory(params, story);
        canContinue &= setStoryPublishTime(params, story, env);

        if ( !canContinue || params.get(PARAM_PREVIEW)!=null ) {
            if ( canContinue )
                env.put(VAR_PREVIEW, story);

            env.put(VAR_IS_DELAYED, Boolean.valueOf(story.getType() == Item.UNPUBLISHED_BLOG));
            storeCategories(blog, env);
            return FMTemplateSelector.select("EditBlog", "edit", env, request);
        }
        
        if (!previousDate.equals(story.getCreated())) {
            String storyUrl = generateStoryURL(blog, story);

            if (storyUrl != null && !storyUrl.equals(relation.getUrl())) {
                relation.setUrl(storyUrl);
                persistence.update(relation);
            }
            
            decrementArchiveRecord(blog.getData().getRootElement(), previousDate);
            incrementArchiveRecord(blog.getData().getRootElement(), story.getCreated());
        }

        boolean delayed = params.get(PARAM_DELAY) != null;
        if (story.getType()==Item.UNPUBLISHED_BLOG && ! delayed)
            publishDelayedStory(story, blog, relation, user);
        else
            persistence.update(story);

        FeedGenerator.updateBlog(blog);
        VariableFetcher.getInstance().refreshStories();

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, Tools.getUrlForBlogStory(relation));
        return null;
    }

    protected void publishDelayedStory(Item story, Category blog, Relation relation, User user) {
        Persistence persistence = PersistenceFactory.getPersistence();
        Date timeNow = new Date();

        String storyUrl = generateStoryURL(blog, story);

        if (storyUrl != null) {
            relation.setUrl(storyUrl);
            persistence.update(relation);
        }

        story.setType(Item.BLOG);
        
        if (timeNow.after(story.getCreated()))
            story.setCreated(timeNow);
        
        story.setUpdated(timeNow);
        persistence.update(story);

        incrementArchiveRecord(blog.getData().getRootElement(), timeNow);
        Element unpublishedStory = (Element) blog.getData().selectSingleNode("/data/unpublished/rid[text()=\"" + relation.getId() + "\"]");
        if (unpublishedStory != null)
            unpublishedStory.detach();
        persistence.update(blog);

        TagTool.assignDetectedTags(story, user);

        sendDigestMessage(relation);
    }

    /**
     * Automatically switch delayed story to published state.
     */
    protected String actionPublishDelayedStory(HttpServletRequest request, HttpServletResponse response, Category blog, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        Relation relation = (Relation) env.get(VAR_STORY);
        Item story = (Item) relation.getChild();

        publishDelayedStory(story, blog, relation, user);

        FeedGenerator.updateBlog(blog);
        VariableFetcher.getInstance().refreshStories();

        urlUtils.redirect(response, Tools.getUrlForBlogStory(blog.getSubType(), story.getCreated(), relation.getId()));
        return null;
    }

    /**
     * Toggles flag whether the story is blog digest or not.
     * @return null, redirect
     */
    private String actionToggleStoryDigest(HttpServletResponse response, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        Persistence persistence = PersistenceFactory.getPersistence();
        Relation relation = (Relation) env.get(VAR_STORY);
        Item story = (Item) relation.getChild();

        Set set = story.getProperty(Constants.PROPERTY_BLOG_DIGEST);
        if (set.size() == 0) {
            story.addProperty(Constants.PROPERTY_BLOG_DIGEST, "yes");
            AdminLogger.logEvent(user, "pridal do digestu zapis "+Tools.childName(story));
        } else {
            story.removeProperty(Constants.PROPERTY_BLOG_DIGEST);
            AdminLogger.logEvent(user, "odebral z digestu zapis " + Tools.childName(story));
        }

        Date originalUpdated = story.getUpdated();
        persistence.update(story);
        SQLTool.getInstance().setUpdatedTimestamp(story, originalUpdated);

        FeedGenerator.updateBlogDigest();

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, Tools.getUrlForBlogStory(relation));
        return null;
    }

    /**
     * Toggles flag whether the story is inappropriate for public or not.
     * @return null, redirect
     */
    private String actionToggleStoryBan(HttpServletResponse response, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        Persistence persistence = PersistenceFactory.getPersistence();
        Relation relation = (Relation) env.get(VAR_STORY);
        Item story = (Item) relation.getChild();

        Set set = story.getProperty(Constants.PROPERTY_BANNED_BLOG);
        if (set.isEmpty()) {
            story.addProperty(Constants.PROPERTY_BANNED_BLOG, "yes");
            story.removeProperty(Constants.PROPERTY_BLOG_DIGEST); // digest & ban at once doesn't make sense
            AdminLogger.logEvent(user, "oznacil jako nevhodny zapis "+Tools.childName(story));
        } else {
            story.removeProperty(Constants.PROPERTY_BANNED_BLOG);
            AdminLogger.logEvent(user, "zrusil oznaceni jako nevhodny zapisu " + Tools.childName(story));
        }

        Date originalUpdated = story.getUpdated();
        persistence.update(story);
        SQLTool.getInstance().setUpdatedTimestamp(story, originalUpdated);

        FeedGenerator.updateBlog(null);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, Tools.getUrlForBlogStory(relation));
        return null;
    }

    /**
     * First step of renaming blog.
     */
    protected String actionRenameBlogStep1(HttpServletRequest request, HttpServletResponse response, Category blog, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        SQLTool sqlTool = SQLTool.getInstance();

        Qualifier[] qa = new Qualifier[]{ new CompareCondition(Field.OWNER, Operation.EQUAL, blog.getOwner()) };
        int stories = sqlTool.countItemRelationsWithType(Item.BLOG, qa);

        if (stories == 0) {
            params.put(PARAM_BLOG_NAME, blog.getSubType());
            return FMTemplateSelector.select("EditBlog", "rename", env, request);
        } else {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Tento blog nelze přejmenovat, protože obsahuje zápisky.", env, request.getSession());

            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/blog/"+blog.getSubType()+"/");
            return null;
        }
    }

    /**
     * Final step of renaming blog.
     * todo je to podporovano? nemelo by se to smazat>
     */
    protected String actionRenameBlogStep2(HttpServletRequest request, HttpServletResponse response, Category blog, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        SQLTool sqlTool = SQLTool.getInstance();

        Qualifier[] qa = new Qualifier[]{ new CompareCondition(Field.OWNER, Operation.EQUAL, blog.getOwner()) };
        int stories = sqlTool.countItemRelationsWithType(Item.BLOG, qa);

        if (stories > 0) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Tento blog nelze přejmenovat, protože obsahuje zápisky.", env, request.getSession());

            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/blog/"+blog.getSubType()+"/");
            return null;
        }

        boolean canContinue = setBlogName(params, blog, env);
        if ( !canContinue )
            return actionRenameBlogStep1(request, response, blog, env);

        User user = (User) env.get(Constants.VAR_USER);
        Element element = (Element) user.getData().selectSingleNode("//settings/blog");
        element.addAttribute("name", blog.getSubType());

        Persistence persistence = PersistenceFactory.getPersistence();
        persistence.update(blog);
        persistence.update(user);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/blog/"+blog.getSubType()+"/");
        return null;
    }

    /**
     * First step of renaming blog.
     */
    protected String actionRemoveStoryStep1(HttpServletRequest request, HttpServletResponse response, Relation story, Category blog, Map env) throws Exception {
        if (Misc.containsForeignComments((Item) story.getChild())) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Tento zápis není možné smazat, neboť obsahuje cizí komentáře.", env, request.getSession());
            Item item = (Item) story.getChild();
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, Tools.getUrlForBlogStory(blog.getSubType(), item.getCreated(), story.getId()));
            return null;
        }

        return FMTemplateSelector.select("EditBlog", "remove", env, request);
    }

    /**
     * Final step of removing a blog story.
     */
    protected String actionRemoveStoryStep2(HttpServletRequest request, HttpServletResponse response, Relation story, Category blog, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        Item item = (Item) story.getChild();

        if (Misc.containsForeignComments((Item) story.getChild())) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Tento zápis není možné smazat, neboť obsahuje cizí komentáře.", env, request.getSession());
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, Tools.getUrlForBlogStory(blog.getSubType(), item.getCreated(), story.getId()));
            return null;
        }

        Persistence persistence = PersistenceFactory.getPersistence();

        Document document = blog.getData();
        decrementArchiveRecord(document.getRootElement(), ((Item)story.getChild()).getCreated());
        Element unpublishedStory = (Element) document.selectSingleNode("/data/unpublished/rid[text()=\"" + story.getId() + "\"]");
        if (unpublishedStory != null)
            unpublishedStory.detach();

        // run monitor
        if (unpublishedStory == null) {
            MonitorAction action = new MonitorAction(user, UserAction.REMOVE, ObjectType.BLOG, story, null);
            String name = item.getTitle();
            action.setProperty(Decorator.PROPERTY_NAME, name);
            MonitorPool.scheduleMonitorAction(action);
        }

        persistence.update(blog);
        persistence.remove(story);

        FeedGenerator.updateBlog(blog);
        VariableFetcher.getInstance().refreshStories();

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
        params.put(PARAM_TITLE, blog.getTitle());
        node = document.selectSingleNode("//custom/intro");
        if ( node!=null )
            params.put(PARAM_INTRO, node.getText());
        return FMTemplateSelector.select("EditBlog", "custom", env, request);
    }

    /**
     * Edits blog customization.
     */
    protected String actionEditCustomStep2(HttpServletRequest request, HttpServletResponse response, Category blog, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();

        Element root = blog.getData().getRootElement();

        boolean canContinue = setPageTitle(params, root, env);
        canContinue &= setBlogTitle(params, blog, env);
        canContinue &= setBlogIntro(params, root, env);

        if ( !canContinue )
            return actionEditCustomStep1(request, blog, env);

        persistence.update(blog);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/blog/"+blog.getSubType()+"/");
        return null;
    }

    /**
     * Puts map of categories into environment.
     * @param blog the blog
     * @param env the environment
     * @return categories
     */
    public static List<BlogCategory> storeCategories(Category blog, Map env) {
        Document document = blog.getData();
        List nodes = document.selectNodes("//categories/category");
        List<BlogCategory> categories = new ArrayList<BlogCategory>(nodes.size());

        for (Iterator iter = nodes.iterator(); iter.hasNext();) {
            Element category = (Element) iter.next();
            String id = category.attributeValue("id");
            String name = category.attributeValue("name");
            String url = category.attributeValue("url");
            BlogCategory cat;

            cat = new BlogCategory(id, name, url);
            categories.add(cat);
        }

        Collections.sort(categories);
        env.put(VAR_CATEGORIES, categories);
        return categories;
    }

    /**
     * Shows all blog's categories so user can edit them.
     */
    protected String actionShowCategories(HttpServletRequest request, Category blog, Map env) throws Exception {
        storeCategories(blog, env);
        return FMTemplateSelector.select("EditBlog", "categories", env, request);
    }

    /**
     * Confirmation dialog for submitting new category.
     */
    protected String actionAddCategory(HttpServletRequest request, Category blog, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String name = (String) params.get(PARAM_CATEGORY_NAME);
        if (Misc.empty(name))
            ServletUtils.addError(PARAM_CATEGORY_NAME, "Prosím zadejte hodnotu.", env, null);
        else {
            try {
                String normalized = URLManager.enforceRelativeURL(name);

                for (Iterator iter = forbiddenCategoryNames.iterator(); iter.hasNext();) {
                    String cname = (String) iter.next();
                    if (cname.equals(normalized)) {
                        ServletUtils.addError(PARAM_CATEGORY_NAME, "Zadané jméno kategorie je vyhrazené!", env, null);
                        break;
                    }
                }
            } catch(Exception e) {
                ServletUtils.addError(PARAM_CATEGORY_NAME, "Zadané jméno kategorie je neplatné!", env, null);
            }
        }

        return FMTemplateSelector.select("EditBlog", "add_category", env, request);
    }

    /**
     * Saving new category.
     */
    protected String actionAddCategoryStep2(HttpServletRequest request, HttpServletResponse response, Category blog, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_BLOG_RELATION);
        Persistence persistence = PersistenceFactory.getPersistence();

        Element root = blog.getData().getRootElement();
        boolean canContinue = addCategory(params, root, env);
        if (!canContinue)
            return actionAddCategory(request, blog, env);

        persistence.update(blog);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/blog/edit/"+relation.getId()+"?action=categories");
        return null;
    }

    /**
     * Dialog to rename category.
     */
    protected String actionEditCategory(HttpServletRequest request, HttpServletResponse response, Category blog, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_BLOG_RELATION);

        int id = Misc.parseInt((String) params.get(PARAM_CATEGORY_ID), -1);
        if (id==-1) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Chybí parametr s číslem kategorie!", env, request.getSession());
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/blog/edit/" + relation.getId() + "?action=categories");
            return null;
        }

        Document document = blog.getData();
        Element category = (Element) document.selectSingleNode("//categories/category[@id="+id+"]");
        if (category==null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Kategorie s číslem "+id+" nebyla nalezena!", env, request.getSession());
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/blog/edit/" + relation.getId() + "?action=categories");
            return null;
        }

        params.put(PARAM_CATEGORY_NAME, category.attributeValue("name"));

        return FMTemplateSelector.select("EditBlog", "edit_category", env, request);
    }

    /**
     * Saving new name of the category.
     */
    protected String actionEditCategoryStep2(HttpServletRequest request, HttpServletResponse response, Category blog, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_BLOG_RELATION);
        Persistence persistence = PersistenceFactory.getPersistence();

        Element root = blog.getData().getRootElement();
        boolean canContinue = setCategory(params, root, env);
        if (!canContinue)
            return actionEditCategory(request, response, blog, env);

        persistence.update(blog);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/blog/edit/" + relation.getId() + "?action=categories");
        return null;
    }

    /**
     * Shows all blog's links so user can edit them.
     */
    protected String actionShowLinks(HttpServletRequest request, Category blog, Map env) throws Exception {
        env.put(ViewBlog.VAR_BLOG_XML, NodeModel.wrap((new DOMWriter().write(blog.getData()))));
        return FMTemplateSelector.select("EditBlog", "links", env, request);
    }

    /**
     * Adds new link.
     */
    protected String actionAddLink(HttpServletRequest request, HttpServletResponse response, Category blog, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_BLOG_RELATION);
        Persistence persistence = PersistenceFactory.getPersistence();

        Element root = blog.getData().getRootElement();
        boolean canContinue = addRecommendedLink(params, root, env);
        if (!canContinue)
            return actionShowLinks(request, blog, env);

        persistence.update(blog);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/blog/edit/" + relation.getId() + "?action=links");
        return null;
    }

    /**
     * Dialog to edit existing link
     */
    protected String actionEditLink(HttpServletRequest request, Category blog, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Element root = blog.getData().getRootElement();

        Element link = getLinkAtPosition(root, params, env);
        if (link!=null) {
            params.put(PARAM_URL, link.getText());
            params.put(PARAM_TITLE, link.attributeValue("caption"));
        }

        return FMTemplateSelector.select("EditBlog", "edit_link", env, request);
    }

    /**
     * Edits existing link.
     */
    protected String actionEditLinkStep2(HttpServletRequest request, HttpServletResponse response, Category blog, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_BLOG_RELATION);
        Persistence persistence = PersistenceFactory.getPersistence();

        Element root = blog.getData().getRootElement();
        boolean canContinue = setRecommendedLink(params, root, env);
        if (!canContinue)
            return actionEditLink(request, blog, env);

        persistence.update(blog);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/blog/edit/" + relation.getId() + "?action=links");
        return null;
    }

    /**
     * Confirmation dialog to remove existing link
     */
    protected String actionRemoveLink(HttpServletRequest request, Category blog, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Element root = blog.getData().getRootElement();

        Element link = getLinkAtPosition(root, params, env);
        if (link!=null) {
            params.put(PARAM_URL, link.getText());
            params.put(PARAM_TITLE, link.attributeValue("caption"));
        }

        return FMTemplateSelector.select("EditBlog", "remove_link", env, request);
    }

    /**
     * Removes existing link.
     */
    protected String actionRemoveLinkStep2(HttpServletRequest request, HttpServletResponse response, Category blog, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_BLOG_RELATION);
        Persistence persistence = PersistenceFactory.getPersistence();

        Element root = blog.getData().getRootElement();
        boolean canContinue = removeRecommendedLink(params, root, env);
        if (!canContinue)
            return actionRemoveLink(request, blog, env);

        persistence.update(blog);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/blog/edit/" + relation.getId() + "?action=links");
        return null;
    }

    /**
     * Moves existing link.
     */
    protected String actionMoveLink(HttpServletRequest request, HttpServletResponse response, Category blog, boolean up, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_BLOG_RELATION);
        Persistence persistence = PersistenceFactory.getPersistence();

        Element root = blog.getData().getRootElement();
        int position = Misc.parseInt((String) params.get(PARAM_POSITION), -1);
        if (position == -1) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Chybí parametr position!", env, null);
            return null;
        }

        Element linksElement = (Element) root.selectSingleNode("/data/custom/links");
        List links = linksElement.elements("link");
        if (links == null || links.size() == 0) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Oops, nemáte definovány žádné linky!", env, null);
            return null;
        }

        if (up) {
            if (position > 0) {
                Element link = (Element) links.remove(position);
                links.add(position-1, link);
            }
        } else {
            if ((position+1) < links.size()) {
                Element link = (Element) links.remove(position);
                links.add(position + 1, link);
            }
        }

        persistence.update(blog);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/blog/edit/" + relation.getId() + "?action=links");
        return null;
    }

    // setters

    /**
     * Sets blog name. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param category category to be updated
     * @return false, if there is a major error.
     */
    boolean setBlogName(Map params, Category category, Map env) {
        String name = (String) params.get(PARAM_BLOG_NAME);
        name = Misc.filterDangerousCharacters(name);
        if ( name==null || name.trim().length()<1) {
            ServletUtils.addError(PARAM_BLOG_NAME, "Zadejte jméno blogu!", env, null);
            return false;
        }
        name = name.trim();
        if (new RE(reBlogName).match(name)) {
            ServletUtils.addError(PARAM_BLOG_NAME, "Jméno blogu smí obsahovat jen znaky a-z, 0-9 a _!", env, null);
            return false;
        }

        CompareCondition condition = new CompareCondition(Field.SUBTYPE, Operation.EQUAL, name);
        SQLTool sqlTool = SQLTool.getInstance();
        List list = sqlTool.findCategoryRelationsWithType(Category.BLOG, new Qualifier[]{condition});
        if (list.size()>0) {
            Relation relation = (Relation) list.get(0);
            Category existing = (Category) relation.getChild();
            if (existing.getId()!=category.getId()) {
                ServletUtils.addError(PARAM_BLOG_NAME, "Zadejte jiné jméno blogu, toto se již používá!", env, null);
                return false;
            }
        }

        category.setSubType(name);
        return true;
    }

    /**
     * Sets first categories. Overwrites existing categories, if they exist.
     * Changes are not synchronized with persistence.
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
        if (name!=null && name.trim().length()>0) {
            Element category = categories.addElement("category");
            category.addAttribute("id",Integer.toString(id++));
            category.addAttribute("name",name);
        }

        name = (String) params.get(PARAM_CATEGORY_NAME+"2");
        if (name!=null && name.trim().length() > 0) {
            Element category = categories.addElement("category");
            category.addAttribute("id",Integer.toString(id++));
            category.addAttribute("name",name);
        }

        name = (String) params.get(PARAM_CATEGORY_NAME+"3");
        if (name!=null && name.trim().length() > 0) {
            Element category = categories.addElement("category");
            category.addAttribute("id",Integer.toString(id++));
            category.addAttribute("name",name);
        }

        return true;
    }

    /**
     * Inserts new category to the list of categories. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root XML document
     * @return false, if there is a major error.
     */
    boolean addCategory(Map params, Element root, Map env) {
        String normalizedName;
        String name = (String) params.get(PARAM_CATEGORY_NAME);
        name = Misc.filterDangerousCharacters(name);
        if (Misc.empty(name)) {
            ServletUtils.addError(PARAM_CATEGORY_NAME, "Prosím zadejte hodnotu.", env, null);
            return false;
        }

        if (name.indexOf('<') != -1) {
            ServletUtils.addError(PARAM_CATEGORY_NAME, "Zadané jméno kategorie je neplatné!", env, null);
            return false;
        }

        try {
            normalizedName = URLManager.enforceRelativeURL(name);

            for (Iterator iter = forbiddenCategoryNames.iterator(); iter.hasNext();) {
                String cname = (String) iter.next();
                if (cname.equals(normalizedName)) {
                    ServletUtils.addError(PARAM_CATEGORY_NAME, "Zadané jméno kategorie je vyhrazené!", env, null);
                    return false;
                }
            }
        } catch(Exception e) {
            ServletUtils.addError(PARAM_CATEGORY_NAME, "Zadané jméno kategorie je neplatné!", env, null);
            return false;
        }

        Element categories = root.element("categories");
        if (categories == null)
            categories = root.addElement("categories");

        int id = 0;
        List list = categories.elements();
        if (list!=null && list.size()>0) {
            Element category = (Element) list.get(list.size()-1);
            id = Misc.parseInt(category.attributeValue("id"), 0);
        }

        Element category = categories.addElement("category");
        category.addAttribute("id", Integer.toString(id+1));
        category.addAttribute("name", name);
        category.addAttribute("url", normalizedName);

        return true;
    }

    /**
     * Sets new name for existing category. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root XML document
     * @return false, if there is a major error.
     */
    boolean setCategory(Map params, Element root, Map env) {
        String name = (String) params.get(PARAM_CATEGORY_NAME);
        name = Misc.filterDangerousCharacters(name);
        if (Misc.empty(name)) {
            ServletUtils.addError(PARAM_CATEGORY_NAME, "Prosím zadejte hodnotu.", env, null);
            return false;
        }

        if (name.indexOf('<') != -1) {
            ServletUtils.addError(PARAM_CATEGORY_NAME, "Zadané jméno kategorie je neplatné!", env, null);
            return false;
        }

        int id = Misc.parseInt((String) params.get(PARAM_CATEGORY_ID), -1);
        if (id == -1) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Chybí parametr s číslem kategorie!", env, null);
            return false;
        }

        Element category = (Element) root.selectSingleNode("//categories/category[@id=" + id + "]");
        if (category == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Kategorie s číslem " + id + " nebyla nalezena!", env, null);
            return false;
        }

        try {
            String normalized = URLManager.enforceRelativeURL(name);

            for (Iterator iter = forbiddenCategoryNames.iterator(); iter.hasNext();) {
                String cname = (String) iter.next();
                if (cname.equals(normalized)) {
                    ServletUtils.addError(PARAM_CATEGORY_NAME, "Zadané jméno kategorie je vyhrazené!", env, null);
                    return false;
                }
            }

            Attribute attr = category.attribute("url");
            if (attr != null)
                attr.setText(normalized);
            else
                category.addAttribute("url", normalized);
        } catch(Exception e) {
            ServletUtils.addError(PARAM_CATEGORY_NAME, "Zadané jméno kategorie je neplatné!", env, null);
            return false;
        }

        category.attribute("name").setText(name);
        return true;
    }

    /**
     * Sets page title for blog. Changes are not synchronized with persistence.
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
        s = Misc.filterDangerousCharacters(s);
        if (Misc.empty(s)) {
            ServletUtils.addError(PARAM_PAGE_TITLE, "Prosím zadejte hodnotu.", env, null);
            return false;
        }
        if ( s.indexOf("<")!=-1 ) {
            params.put(PARAM_PAGE_TITLE, "");
            ServletUtils.addError(PARAM_PAGE_TITLE, "Použití HTML značek je v titulku zakázáno!", env, null);
            return false;
        }
        title.setText(s);
        return true;
    }

    /**
     * Sets title for blog. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param category category
     * @return false, if there is a major error.
     */
    boolean setBlogTitle(Map params, Category category, Map env) {
        String s = (String) params.get(PARAM_TITLE);
        s = Misc.filterDangerousCharacters(s);
        if (! Misc.empty(s) && s.indexOf("<")!=-1 ) {
            params.put(PARAM_TITLE, "");
            ServletUtils.addError(PARAM_TITLE, "Použití HTML značek je v titulku zakázáno!", env, null);
            return false;
        }
        category.setTitle(s);
        return true;
    }

    /**
     * Sets intro for blog. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root XML document
     * @return false, if there is a major error.
     */
    boolean setBlogIntro(Map params, Element root, Map env) {
        Element custom = root.element("custom");
        Element intro = custom.element("intro");
        String s = (String) params.get(PARAM_INTRO);
        s = Misc.filterDangerousCharacters(s);
        if (Misc.empty(s)) {
            if (intro!=null)
                intro.detach();
        } else {
            try {
                BlogHTMLGuard.check(s);
            } catch (ParserException e) {
                log.error("ParseException on '" + s + "'", e);
                ServletUtils.addError(PARAM_INTRO, e.getMessage(), env, null);
                return false;
            } catch (Exception e) {
                ServletUtils.addError(PARAM_INTRO, e.getMessage(), env, null);
                return false;
            }

            if (intro==null)
                intro = custom.addElement("intro");
            intro.setText(s);
        }
        return true;
    }

    /**
     * Adds another recommended link to blog. Changes are not synchronized with persistence.
     *
     * @param params map holding request's parameters
     * @param root   XML document
     * @return false, if there is a major error.
     */
    boolean addRecommendedLink(Map params, Element root, Map env) {
        String title = (String) params.get(PARAM_TITLE);
        title = Misc.filterDangerousCharacters(title);
        if (title==null || title.length()==0) {
            ServletUtils.addError(PARAM_TITLE, "Zadejte titulek odkazu.", env, null);
            return false;
        }

        String url = (String) params.get(PARAM_URL);
        if (url==null || url.length()==0 || url.indexOf("://")==-1) {
            ServletUtils.addError(PARAM_URL, "Zadejte platnou adresu odkazu.", env, null);
            return false;
        }

        Element custom = root.element("custom");
        Element links = custom.element("links");
        if (links==null)
            links = custom.addElement("links");

        Element link = links.addElement("link");
        link.setText(url);
        link.addAttribute("caption", title);
        return true;
    }

    /**
     * Changes properties of existing recommended link. Changes are not synchronized with persistence.
     *
     * @param params map holding request's parameters
     * @param root   XML document
     * @return false, if there is a major error.
     */
    boolean setRecommendedLink(Map params, Element root, Map env) {
        String title = (String) params.get(PARAM_TITLE);
        title = Misc.filterDangerousCharacters(title);
        if (title == null || title.length() == 0) {
            ServletUtils.addError(PARAM_TITLE, "Zadejte titulek odkazu.", env, null);
            return false;
        }

        String url = (String) params.get(PARAM_URL);
        if (url == null || url.length() == 0 || url.indexOf("://") == -1) {
            ServletUtils.addError(PARAM_URL, "Zadejte platnou adresu odkazu.", env, null);
            return false;
        }

        Element link = getLinkAtPosition(root, params, env);
        if (link==null)
            return false;

        link.setText(url);
        link.attribute("caption").setText(title);
        return true;
    }

    /**
     * Removes existing recommended link from blog. Changes are not synchronized with persistence.
     *
     * @param params map holding request's parameters
     * @param root   XML document
     * @return false, if there is a major error.
     */
    boolean removeRecommendedLink(Map params, Element root, Map env) {
        Element link = getLinkAtPosition(root, params, env);
        if (link==null)
            return false;
        link.detach();
        return true;
    }

    /**
     * Finds value of position parameter and looks up link in list of links.
     * @return either link or null.
     */
    Element getLinkAtPosition(Element root, Map params, Map env) {
        int position = Misc.parseInt((String) params.get(PARAM_POSITION), -1);
        if (position == -1) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Chybí parametr position!", env, null);
            return null;
        }

        List links = root.selectNodes("/data/custom/links/link");
        if (links == null || links.size() == 0) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Oops, nemáte definovány žádné linky!", env, null);
            return null;
        }

        if (position >= links.size()) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Oops, takový link neexistuje!", env, null);
            return null;
        }

        return (Element) links.get(position);
    }

    /**
     * Sets title for story. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param item item
     * @return false, if there is a major error.
     */
    boolean setStoryTitle(Map params, Item item, Map env) {
        String s = (String) params.get(PARAM_TITLE);
        if (Misc.empty(s)) {
            ServletUtils.addError(PARAM_TITLE, "Prosím zadejte hodnotu.", env, null);
            return false;
        }
        s = s.trim();
        s = Misc.filterDangerousCharacters(s);
        if (s.length()>maxStoryTitleLength) {
            ServletUtils.addError(PARAM_TITLE, "Prosím zadejte kratší titulek. Maximální povolená délka je "+maxStoryTitleLength+".", env, null);
            return false;
        }
        if ( s.indexOf("<")!=-1 ) {
            params.put(PARAM_TITLE, "");
            ServletUtils.addError(PARAM_TITLE, "Použití HTML značek je v titulku zakázáno!", env, null);
            return false;
        }

        item.setTitle(s);
        return true;
    }

    /**
     * Loads text from an uploaded file. Its contents are stored into params
     * @param params map holding request's parameters
     * @return false, if there is a major error
     */
    boolean setStoryContentFromFile(Map params, Map env) {
        FileItem fileItem = (FileItem) params.get(PARAM_CONTENT_FILE);
        // 0-length data is what we get if user doesn't choose any file
        if (fileItem == null || fileItem.getSize() == 0) {
            ServletUtils.addError(PARAM_CONTENT_FILE, "Vyberte soubor.", env, null);
            return false;
        }

        params.put(PARAM_CONTENT, new String(fileItem.get()));
        return true;
    }

    /**
     * Sets content for story. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root XML document
     * @return false, if there is a major error.
     */
    boolean setStoryContent(Map params, Element root, Map env) {
        String content = (String) params.get(PARAM_CONTENT);
        content = Misc.filterDangerousCharacters(content);
        if (Misc.empty(content)) {
            ServletUtils.addError(PARAM_CONTENT, "Prosím zadejte obsah zápisku.", env, null);
            return false;
        }

        String stripped = null, perex = null;
        Matcher matcher = breakTagPattern.matcher(content);
        if ( matcher.find() ) {
            perex = content.substring(0, matcher.start());
            content = content.substring(matcher.end());
            stripped = Tools.removeTags(perex);
        } else
            stripped = Tools.removeTags(content);

        StringTokenizer stk = new StringTokenizer(stripped, " \t\n\r\f,.");
        if (stk.countTokens()>maxStoryWordCount) {
            ServletUtils.addError(PARAM_CONTENT, "Váš zápis je příliš dlouhý. Rozdělte jej pomocí značky &lt;break&gt; tak, aby perex měl méně než "+maxStoryWordCount+" slov.", env, null);
            return false;
        }

        try {
            if (perex != null)
                BlogHTMLGuard.checkPerex(perex);
            BlogHTMLGuard.check(content);
        } catch (ParserException e) {
            log.error("ParseException on '" + content + "'", e);
            ServletUtils.addError(PARAM_CONTENT, e.getMessage(), env, null);
            return false;
        } catch (Exception e) {
            ServletUtils.addError(PARAM_CONTENT, e.getMessage(), env, null);
            return false;
        }

        Element tagPerex = root.element("perex");
        if (perex != null) {
            perex = EditDiscussion.processLocalLinks(perex, null);
            
            if (tagPerex == null)
                tagPerex = root.addElement("perex");
            tagPerex.setText(perex);
            tagPerex.addAttribute("format", Integer.toString(Format.HTML.getId()));
        } else {
            if (tagPerex != null)
                tagPerex.detach();
        }

        Element tagContent = root.element("content");
        if (tagContent == null)
            tagContent = root.addElement("content");
        content = EditDiscussion.processLocalLinks(content, null);
        tagContent.setText(content);
        tagContent.addAttribute("format", Integer.toString(Format.HTML.getId()));
        return true;
    }

    /**
     * Sets category for story. Changes are not synchronized with persistence.
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
     * Sets the time the story is supposed to be published
     * @param params a map holding request's parameters
     * @param story the story to be updated
     * @param env
     * @return false, if there is a major error
     */
    boolean setStoryPublishTime(Map params, Item story, Map env) {
        String publishTime = (String) params.get(PARAM_PUBLISH);
        
        if (Misc.empty(publishTime))
            return true;
        
        Date now = new Date();
        Date created = story.getCreated();
        
        if (story.getType() == Item.BLOG && created != null && created.before(now)) {
            ServletUtils.addError(PARAM_PUBLISH, "Nemůžete změnit čas vydaného zápisku!", env, null);
            return false;
        }
        
        Date date;
        try {
            synchronized (Constants.isoFormat) {
                date = Constants.isoFormat.parse(publishTime);
            }
        } catch (Exception e) {
            ServletUtils.addError(PARAM_PUBLISH, "Neplatné datum/čas!", env, null);
            return false;
        }
        
        if (date.before(now)) {
            ServletUtils.addError(PARAM_PUBLISH, "Nemůžete publikovat v minulosti!", env, null);
            return false;
        }
        
        story.setCreated(date);
        
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

    /**
     * Generates a text URL for a blog story.
     * @param blog Blog that contains this story
     * @param story Story to generate the title for
     * @return Generated url or null, if story's title has no valid characters
     */
    public static String generateStoryURL(Category blog, Item story) {
        try {
            String title = story.getTitle();
            String normalized = URLManager.enforceRelativeURL(title);
            String storyUrl;

            storyUrl = Tools.getUrlForBlogStory(blog.getSubType(), story.getCreated(), 0) + normalized;
            storyUrl = URLManager.protectFromDuplicates(storyUrl);

            return storyUrl;
        } catch(Exception e) {
            return null;
        }
    }

    void sendDigestMessage(Relation relation) {
        Persistence persistence = PersistenceFactory.getPersistence();
        Item story = (Item) relation.getChild();
        Map data = new HashMap();

        String title = story.getTitle();
        data.put(VAR_RELATION, relation);
        data.put(EmailSender.KEY_TO, AbcConfig.getBlogWatchEmail());
//        data.put(EmailSender.KEY_RECEPIENT_UID, Integer.toString(user.getId())); TODO
        data.put(EmailSender.KEY_SUBJECT, title);
        data.put(EmailSender.KEY_TEMPLATE, "/mail/blogdigest.ftl");
        data.put("URL", Tools.getUrlForBlogStory(relation));
        data.put("AUTHOR", persistence.findById(new User(story.getOwner())));
        EmailSender.sendEmail(data);
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        maxStoryTitleLength = prefs.getInt(PREF_MAX_STORY_TITLE_LENGTH, maxStoryTitleLength);
        maxStoryWordCount = prefs.getInt(PREF_MAX_STORY_WORD_COUNT, maxStoryWordCount);
        String re = prefs.get(PREF_RE_INVALID_BLOG_NAME, null);
        try {
            reBlogName = new RECompiler().compile(re);
        } catch (RESyntaxException e) {
            throw new ConfigurationException("Cannot compile regular expression '"+re+"' given by "+PREF_RE_INVALID_BLOG_NAME);
        }

        List tmpList = new ArrayList();
        String tmp = prefs.get(PREF_FORBIDDEN_CATEGORY_NAMES, null);
        if (tmp!=null && tmp.length()!=0) {
            StringTokenizer stk = new StringTokenizer(tmp,",");
            while (stk.hasMoreTokens())
                tmpList.add(stk.nextToken());
        }
        forbiddenCategoryNames = tmpList;
    }
}
