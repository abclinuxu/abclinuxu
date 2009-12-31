package cz.abclinuxu.servlets.html.admin.edit;

import static cz.abclinuxu.servlets.Constants.PARAM_NAME;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.EditionRole;
import cz.abclinuxu.data.view.Author;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.util.RelationUtil;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.PageNavigation;
import cz.abclinuxu.servlets.utils.url.PwdNavigator;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.BeanFetcher;
import cz.abclinuxu.utils.BeanFlusher;
import cz.abclinuxu.utils.ImageTool;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.BeanFetcher.FetchType;
import cz.abclinuxu.utils.freemarker.Tools;

public class EditAuthor implements AbcAction {
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_SURNAME = "surname";
    public static final String PARAM_NICKNAME = "nickname";
    public static final String PARAM_BIRTH_NUMBER = "birthNumber";
    public static final String PARAM_ACCOUNT_NUMBER = "accountNumber";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_PHONE = "phone";
    public static final String PARAM_ADDRESS = "address";
    public static final String PARAM_REMOVE_PHOTO = "remove_photo";
    public static final String PARAM_PHOTO = "photo";
    public static final String PARAM_UID = "uid";
    public static final String PARAM_LOGIN = "login";
    public static final String PARAM_ACTIVE = "active";
    public static final String PARAM_PREVIEW = "preview";
    public static final String PARAM_ABOUT = "about";

    public static final String PARAM_DELETE = "delete";

    public static final String VAR_AUTHOR = "AUTHOR";
    public static final String VAR_EDIT_MODE = "EDIT_MODE";
    public static final String VAR_UNDELETABLE = "UNDELETABLE";
    public static final String VAR_EDITOR_MODE = "EDITOR_MODE";
    public static final String VAR_RELATION = "RELATION";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";
    public static final String ACTION_EDIT_SELF = "editSelf";
    public static final String ACTION_EDIT_SELF_STEP2 = "editSelf2";
    public static final String ACTION_REMOVE = "rm";
    public static final String ACTION_REMOVE_STEP2 = "rm2";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        if (ServletUtils.handleMaintainance(request, env)) {
            response.sendRedirect(response.encodeRedirectURL("/"));
            return null;
        }

        // check permissions
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);

        EditionRole role = ServletUtils.getEditionRole(user, request);
        if (role == EditionRole.NONE)
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (action == null || action.length() == 0)
            throw new MissingArgumentException("Chybí parametr action!");


        boolean editor = (role == EditionRole.EDITOR || role == EditionRole.EDITOR_IN_CHIEF);
        if (editor)
            env.put(VAR_EDITOR_MODE, Boolean.TRUE);

        PwdNavigator navigator = new PwdNavigator(env, PageNavigation.ADMIN_AUTHORS);

        if (ACTION_ADD.equals(action)) {
            if (editor)
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

            return actionAddStep1(request, env, navigator);
        }

        if (ACTION_ADD_STEP2.equals(action)) {
            if (! editor)
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

            ActionProtector.ensureContract(request, EditAuthor.class, true, true, true, false);
            return actionAddStep2(request, response, env, navigator);
        }

        // find author
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Chybí parametr relationId!");
        Tools.sync(relation);
        env.put(VAR_RELATION, relation);
        Author author = BeanFetcher.fetchAuthor(relation, FetchType.PROCESS_NONATOMIC);
        env.put(VAR_AUTHOR, author);

        if (ACTION_EDIT_SELF.equals(action)) {
            if (author.getUid() != user.getId())
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

            return actionEditSelfStep1(request, env, navigator);
        }

        if (ACTION_EDIT_SELF_STEP2.equals(action)) {
            if (author.getUid() != user.getId())
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

            ActionProtector.ensureContract(request, EditAuthor.class, true, true, true, false);
            return actionEditSelfStep2(request, response, env, navigator);
        }

        if (!editor)
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (ACTION_EDIT.equals(action))
            return actionEditStep1(request, env, navigator);

        if (ACTION_EDIT_STEP2.equals(action)) {
            ActionProtector.ensureContract(request, EditAuthor.class, true, true, true, false);
            return actionEditStep2(request, response, env, navigator);
        }

        if (ACTION_REMOVE.equals(action)) {
            return actionRemoveStep1(request, env, navigator);
        }

        if (ACTION_REMOVE_STEP2.equals(action)) {
            return actionRemoveStep2(request, response, env);
        }

        throw new MissingArgumentException("Chybí parametr action!");
    }

    // first step of author creation
    public String actionAddStep1(HttpServletRequest request, Map env, PwdNavigator navigator) throws Exception {
        Link tail = new Link("Nový autor", "edit?action=add", "Vytvořit nového autora, krok 1");
        env.put(Constants.VAR_PARENTS, navigator.navigate(tail));
        return FMTemplateSelector.select("AdministrationEditAuthor", "add", env, request);
    }

    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, PwdNavigator navigator) throws Exception {

        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);

        Link tail = new Link("Nový autor", "edit?action=add2", "Vytvořit nového autora, krok 2");
        env.put(Constants.VAR_PARENTS, navigator.navigate(tail));

        // create author in persistence storage
        // if author is set, change owner of object
        Author author = new Author();
        boolean canContinue = fillAuthor(env, author);
        if (!canContinue) {
            return FMTemplateSelector.select("AdministrationEditAuthor", "add", env, request);
        }

        // set rights
        Relation parent = new Relation(Constants.REL_AUTHORS);
        persistence.synchronize(parent);
        persistence.synchronize(parent.getChild());

        Item item = new Item(0, Item.AUTHOR);

        // refresh item content
        item = BeanFlusher.flushAuthor(item, author);
        item.setTitle(Tools.getPersonName(item));
        item.setOwner(user.getId());
        persistence.create(item);

        // set url
        String url = proposeAuthorsUrl(author);
        url = URLManager.protectFromDuplicates(url);

        Relation relation = new Relation(parent.getChild(), item, parent.getId());
        relation.setUrl(url);
        persistence.create(relation);
        relation.getParent().addChildRelation(relation);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.make(UrlUtils.PREFIX_ADMINISTRATION + "/redakce/autori/show/" + relation.getId()));
        return null;
    }

    //todo
    protected String actionEditSelfStep1(HttpServletRequest request, Map env, PwdNavigator navigator) throws Exception {
        Author author = (Author) env.get(VAR_AUTHOR);
        Boolean isEditor = (Boolean) env.get(VAR_EDITOR_MODE);

        Link tail;
        if (isEditor == Boolean.TRUE)
            tail = new Link(author.getTitle(), "edit/" + author.getId() + "?action=edit", "Editace autora, krok 1");
        else
            tail = new Link("Osobní údaje", "/sprava/redakce/autori/edit/" + author.getId() + "?action=edit", "Osobní údaje");
        env.put(Constants.VAR_PARENTS, navigator.navigate(tail));

        env.put(VAR_EDIT_MODE, Boolean.TRUE);

        return FMTemplateSelector.select("AdministrationEditAuthor", "editSelf", env, request);
    }

    //todo
    protected String actionEditSelfStep2(HttpServletRequest request, HttpServletResponse response, Map env, PwdNavigator navigator) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        SQLTool sqlTool = SQLTool.getInstance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Author author = (Author) env.get(VAR_AUTHOR);
        Boolean isEditor = (Boolean) env.get(VAR_EDITOR_MODE);

        boolean canContinue = fillAuthor(env, author);
        if (!canContinue) {
            Link tail;
            if (isEditor == Boolean.TRUE)
                tail = new Link(author.getTitle(), "edit/" + author.getId() + "?action=edit", "Editace autora, krok 1");
            else
                tail = new Link("Osobní údaje", "/sprava/redakce/autori/edit/" + author.getId() + "?action=edit", "Osobní údaje");
            env.put(Constants.VAR_PARENTS, navigator.navigate(tail));

            return FMTemplateSelector.select("AdministrationEditAuthor", "editSelf", env, request);
        }

        // refresh item content
        Item item = (Item) persistence.findById(new Item(author.getId()));
        item = BeanFlusher.flushAuthor(item, author);
        persistence.update(item);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
//        urlUtils.redirect(response, urlUtils.make(UrlUtils.PREFIX_ADMINISTRATION + "/redakce/autori/show/" + relation.getId()));
        return null;
    }

    protected String actionEditStep1(HttpServletRequest request, Map env, PwdNavigator navigator) throws Exception {
        Author author = (Author) env.get(VAR_AUTHOR);
        Boolean isEditor = (Boolean) env.get(VAR_EDITOR_MODE);

        Link tail;
        if (isEditor == Boolean.TRUE)
            tail = new Link(author.getTitle(), "edit/" + author.getId() + "?action=edit", "Editace autora, krok 1");
        else
            tail = new Link("Osobní údaje", "/sprava/redakce/autori/edit/" + author.getId() + "?action=edit", "Osobní údaje");
        env.put(Constants.VAR_PARENTS, navigator.navigate(tail));

        env.put(VAR_EDIT_MODE, Boolean.TRUE);

        return FMTemplateSelector.select("AdministrationEditAuthor", "edit", env, request);
    }

    protected String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env, PwdNavigator navigator) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        SQLTool sqlTool = SQLTool.getInstance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Author author = (Author) env.get(VAR_AUTHOR);
        Boolean isEditor = (Boolean) env.get(VAR_EDITOR_MODE);

        boolean canContinue = fillAuthor(env, author);
        if (!canContinue) {
            Link tail;
            if (isEditor == Boolean.TRUE)
                tail = new Link(author.getTitle(), "edit/" + author.getId() + "?action=edit", "Editace autora, krok 1");
            else
                tail = new Link("Osobní údaje", "/sprava/redakce/autori/edit/" + author.getId() + "?action=edit", "Osobní údaje");
            env.put(Constants.VAR_PARENTS, navigator.navigate(tail));

            env.put(VAR_EDIT_MODE, Boolean.TRUE);
            return FMTemplateSelector.select("AdministrationEditAuthor", "edit", env, request);
        }


        Item item = (Item) persistence.findById(new Item(author.getId()));
        Relation relation = RelationUtil.findParent(item);

        // change owner if user with passed login exists
        if (author.getUid() != null)
            item.setOwner(author.getUid());

        // refresh item content
        item = BeanFlusher.flushAuthor(item, author);
        persistence.update(item);

        String url = proposeAuthorsUrl(author);
        if (!url.equals(relation.getUrl())) {
            url = URLManager.protectFromDuplicates(url);
            sqlTool.insertOldAddress(relation.getUrl(), url, relation.getId());
            relation.setUrl(url);
            persistence.update(relation);
        }

        env.put(VAR_AUTHOR, author);

        // just photo was deleted
        if (!Misc.empty((String) params.get(PARAM_REMOVE_PHOTO))) {
            env.put(VAR_EDIT_MODE, Boolean.TRUE);
            return FMTemplateSelector.select("AdministrationEditAuthor", "edit", env, request);
        }

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.make(UrlUtils.PREFIX_ADMINISTRATION + "/redakce/autori/show/" + relation.getId()));
        return null;
    }

    protected String actionRemoveStep1(HttpServletRequest request, Map env, PwdNavigator navigator) throws Exception {
        SQLTool sqlTool = SQLTool.getInstance();

        Author author = (Author) env.get(VAR_AUTHOR);

        Link tail = new Link(author.getTitle(), "edit/" + author.getId() + "?action=rm", "Smazání autora, krok 1");
        env.put(Constants.VAR_PARENTS, navigator.navigate(tail));

        // count articles written by author
        Relation authorRel = RelationUtil.findParent(new Item(author.getId()));
        int articles = sqlTool.countArticleRelationsByAuthor(authorRel.getId());
        if (articles > 0)
            env.put(VAR_UNDELETABLE, Boolean.TRUE);

        return FMTemplateSelector.select("AdministrationEditAuthor", "remove", env, request);
    }

    protected String actionRemoveStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        // delete author
        String delete = (String) params.get(PARAM_DELETE);
        if (!Misc.empty(delete)) {
            Author author = (Author) env.get(VAR_AUTHOR);
            ImageTool.deleteImage(Author.AuthorImage.PHOTO, author);
            persistence.remove(RelationUtil.findParent(new Item(author.getId())));
            persistence.remove(new Item(author.getId()));
            ServletUtils.addMessage("Autor " + author.getTitle() + " byl smazán!", env, request.getSession());
            persistence.clearCache();
        }

        // redirect to author list
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.make(UrlUtils.PREFIX_ADMINISTRATION + "/redakce/autori"));
        return null;
    }

    /**
     * @return absolute url for this author
     */
    private String proposeAuthorsUrl(Author author) {
        StringBuilder sb = new StringBuilder();
        if (author.getName() != null)
            sb.append(author.getName()).append(" ");
        if (author.getSurname() != null)
            sb.append(author.getSurname());

        return UrlUtils.PREFIX_AUTHORS + "/" + URLManager.enforceRelativeURL(sb.toString());
    }

    /**
     * Creates author from parameters passed
     *
     * @param env    Variables holder
     * @param author Author
     * @return {@code true} if checks passed, {@code false} otherwise. Returns
     *         {@code false} when photo is deleted
     */
    private boolean fillAuthor(Map env, Author author) {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Boolean isEditor = (Boolean) env.get(VAR_EDITOR_MODE);

        boolean result = true;
        String tmp;

        if (isEditor == Boolean.TRUE) {
            // set surname
            String surname = (String) params.get(PARAM_SURNAME);
            author.setSurname(surname);
            if (Misc.empty(surname)) {
                ServletUtils.addError(PARAM_SURNAME, "Zadejte příjmení!", env, null);
                result = false;
            }

            // will set both uid and login
            String login = (String) params.get(PARAM_LOGIN);
            if (Misc.empty(login)) {
                author.setUid(null);
                author.setLogin(login);
            } else {
                Integer uid = SQLTool.getInstance().getUserByLogin(login);
                if (uid == null) {
                    ServletUtils.addError(PARAM_LOGIN, "Zadejte platný login!", env, null);
                    result = false;
                }
                author.setUid(uid);
                author.setLogin(login);
            }

            tmp = (String) params.get(PARAM_NAME);
            author.setName(tmp);
            tmp = (String) params.get(PARAM_NICKNAME);
            author.setNickname(tmp);
            tmp = (String) params.get(PARAM_BIRTH_NUMBER);
            author.setBirthNumber(tmp);
            tmp = (String) params.get(PARAM_ACTIVE);
            author.setActive("1".equalsIgnoreCase(tmp));
            tmp = (String) params.get(PARAM_ABOUT);
            author.setAbout(tmp);

            String oldPhoto = (String) params.get(PARAM_REMOVE_PHOTO);
            if (oldPhoto != null && oldPhoto.length() > 0) {
                ImageTool.deleteImage(Author.AuthorImage.PHOTO, author);
                ServletUtils.addMessage("Fotografie byla odstraněna", env, null);
                return true;
            }

            // set photo
            FileItem photo = (FileItem) params.get(PARAM_PHOTO);
            if (photo != null && photo.getSize() > 0) {
                ImageTool imageTool = new ImageTool(photo, author, ImageTool.AUTHOR_PHOTO_RESTRICTIONS);
                result = imageTool.checkImage(env, PARAM_PHOTO);
                if (result)
                    result = imageTool.storeImage(Author.AuthorImage.PHOTO, env, PARAM_PHOTO);
            }
        }

        // author field available both for editor and author
        tmp = (String) params.get(PARAM_ACCOUNT_NUMBER);
        author.setAccountNumber(tmp);
        tmp = (String) params.get(PARAM_EMAIL);
        author.setEmail(tmp);
        tmp = (String) params.get(PARAM_PHONE);
        author.setPhone(tmp);
        tmp = (String) params.get(PARAM_ADDRESS);
        author.setAddress(tmp);

        return result;
    }
}