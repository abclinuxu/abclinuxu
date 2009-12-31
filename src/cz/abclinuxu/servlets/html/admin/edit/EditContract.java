package cz.abclinuxu.servlets.html.admin.edit;

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.EditionRole;
import cz.abclinuxu.data.view.Author;
import cz.abclinuxu.data.view.ContractTemplate;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.data.view.SignedContract;
import cz.abclinuxu.exceptions.InternalException;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.versioning.Versioning;
import cz.abclinuxu.persistence.versioning.VersioningFactory;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.admin.view.ShowContract;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.PageNavigation;
import cz.abclinuxu.servlets.utils.url.PwdNavigator;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.BeanFetcher;
import cz.abclinuxu.utils.BeanFetcher.FetchType;
import cz.abclinuxu.utils.BeanFlusher;
import cz.abclinuxu.utils.ImageTool;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.forms.Validator;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.utils.freemarker.Tools;
import freemarker.template.TemplateException;
import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditContract implements AbcAction {
    private static final Logger log = Logger.getLogger(EditContract.class);

    public static final String PARAM_RELATION_SHORT = "rid";
	public static final String PARAM_TITLE = "title";
	public static final String PARAM_DESCRIPTION = "description";
	public static final String PARAM_CONTENT = "content";
    public static final String PARAM_PICTURE = "picture";
    public static final String PARAM_PREVIEW = "preview";
    public static final String PARAM_DELETE = "delete";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_CONTRACT_TEMPLATE = "CONTRACT_TEMPLATE";
	public static final String VAR_EDIT_MODE = "EDIT_MODE";
    public static final String VAR_PREVIEW = "PREVIEW";
    public static final String VAR_AUTHOR = "AUTHOR";
    public static final String VAR_TODAY = "TODAY";
    public static final String VAR_UNDELETABLE = "UNDELETABLE";

    public static final String ACTION_ADD = "add";
	public static final String ACTION_ADD_STEP2 = "add2";
	public static final String ACTION_EDIT = "edit";
	public static final String ACTION_EDIT_STEP2 = "edit2";
	public static final String ACTION_REMOVE = "rm";
	public static final String ACTION_REMOVE_STEP2 = "rm2";
	public static final String ACTION_PUBLISH = "publish";
    public static final String ACTION_CLONE = "clone";
    public static final String ACTION_SIGN = "sign";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
		Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);
        User user = (User) env.get(Constants.VAR_USER);

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

        boolean editor = (role == EditionRole.EDITOR || role == EditionRole.EDITOR_IN_CHIEF);

        PwdNavigator navigator = new PwdNavigator(env, PageNavigation.ADMIN_CONTRACTS);

        if (action == null || action.length() == 0)
		    throw new MissingArgumentException("Chybí parametr action!");

        if (ACTION_ADD.equals(action)) {
            if (! editor)
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

            return actionAddStep1(request, env, navigator);
		}

		if (ACTION_ADD_STEP2.equals(action)) {
            if (! editor)
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

            ActionProtector.ensureContract(request, EditContract.class, true, true, true, false);
			return actionAddStep2(request, response, env, navigator);
		}

		// determine given topic by id
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Chybí parametr relationId!");
        Tools.sync(relation);
        env.put(VAR_RELATION, relation);

		ContractTemplate template = BeanFetcher.fetchContractTemplate(relation, FetchType.EAGER);
		env.put(VAR_CONTRACT_TEMPLATE, template);

        if (ACTION_SIGN.equals(action)) {
            ActionProtector.ensureContract(request, ShowContract.class, true, true, true, false);
            return actionSignContract(request, response, env);
        }

        if (! editor)
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (ACTION_EDIT.equals(action)) {
			return actionEditStep1(request, env, navigator);
		}

		if (ACTION_EDIT_STEP2.equals(action)) {
			ActionProtector.ensureContract(request, EditContract.class, true, true, true, false);
			return actionEditStep2(request, response, env, navigator);
		}

		if (ACTION_REMOVE.equals(action)) {
			return actionRemoveStep1(request, env);
		}

		if (ACTION_REMOVE_STEP2.equals(action)) {
			ActionProtector.ensureContract(request, EditContract.class, true, true, true, false);
			return actionRemoveStep2(request, response, env);
		}

		if (ACTION_CLONE.equals(action)) {
			return actionClone(request, env, navigator);
		}

		if (ACTION_PUBLISH.equals(action)) {
			ActionProtector.ensureContract(request, ShowContract.class, true, true, false, true);
			return actionPublish(request, response, env);
		}

		throw new MissingArgumentException("Chybí parametr action!");
	}

    private String actionSignContract(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        ContractTemplate template = (ContractTemplate) env.get(VAR_CONTRACT_TEMPLATE);
        Relation templateRelation = (Relation) env.get(VAR_RELATION);

        SignedContract contract = new SignedContract();
        contract.setSigned(new Date());
        contract.setUid(user.getId());
        contract.setTemplate(template.getId());
        contract.setIpAddress(ServletUtils.getClientIPAddress(request));

        Author author = Tools.getAuthor(user.getId());
        contract.setAuthor(author);
        try {
            String text = fillContractTemplate(template, author);
            contract.setContent(text);
        } catch (Exception e) {
            log.error("Chyba v šabloně smlouvy " + template.getTitle() + " (relace = " + template.getRelationId() + ", author = " + author.getId() + ")", e);
            ServletUtils.addError(Constants.ERROR_GENERIC, "Omlouváme se, ale ve smlouvě je chyba, informujte prosím šefredaktora.", env, request.getSession());
            urlUtils.redirect(response, urlUtils.make(UrlUtils.PREFIX_ADMINISTRATION + "/redakce/smlouvy/"));
            return null;
        }

        Item item = new Item(0, Item.SIGNED_CONTRACT);
        item = BeanFlusher.flushSignedContract(item, contract);
        persistence.create(item);
        Relation relation = new Relation(templateRelation.getChild(), item, templateRelation.getId());
        persistence.create(relation);
        relation.getParent().addChildRelation(relation);

        Item authorItem = (Item) persistence.findById(new Item(author.getId()));
        author.setContractId(contract.getTemplate());
        authorItem = BeanFlusher.flushAuthor(authorItem, author);
        persistence.update(authorItem);

        ServletUtils.addMessage("Váš souhlas s touto smlouvou byl zaznamenán. Děkujeme.", env, request.getSession());
        urlUtils.redirect(response, urlUtils.make(UrlUtils.PREFIX_ADMINISTRATION + "/redakce/smlouvy/show/" + relation.getId()));
        return null;
    }

    private String actionAddStep1(HttpServletRequest request, Map env, PwdNavigator navigator) throws Exception {
		Link tail = new Link("Nová šablona smlouvy", "edit?action=add", "Vytvořit novou šablonu");
		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));
		return FMTemplateSelector.select("EditContract", "add", env, request);
	}

	private String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, PwdNavigator navigator) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);

        Link tail = new Link("Nová šablona smlouvy", "edit?action=add", "Vytvořit novou šablonu");
		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));

		// get contract from request
		ContractTemplate template = new ContractTemplate();
        template.setDraft(true);

        ImageTool imageTool = null;
        FileItem fileItem = (FileItem) params.get(PARAM_PICTURE);
        if (fileItem != null && fileItem.getSize() > 0)
            imageTool = new ImageTool(fileItem, template, ImageTool.CONTRACT_IMAGE_RESTRICTIONS);

        ContractValidator validator = new ContractValidator(template, env, imageTool);
        boolean canContinue = validator.setAndValidate();
        if (!canContinue || params.get(PARAM_PREVIEW) != null) {
            if (!canContinue)
                params.remove(PARAM_PREVIEW);
			env.put(VAR_PREVIEW, template);
			env.put(VAR_CONTRACT_TEMPLATE, template);
			return actionAddStep1(request, env, navigator);
		}

        Item item = new Item(0, Item.CONTRACT_TEMPLATE);
        item = BeanFlusher.flushContractTemplate(item, template);
        Versioning versioning = VersioningFactory.getVersioning();
        Relation relation;

        if (imageTool != null) {
            persistence.create(item);
            template.setId(item.getId());

            relation = new Relation(new Category(Constants.CAT_CONTRACTS), item, Constants.REL_CONTRACTS);
            persistence.create(relation);
            template.setRelationId(relation.getId());

            boolean result = imageTool.storeImage(ContractTemplate.Image.PICTURE, env, PARAM_PICTURE);

            versioning.prepareObjectBeforeCommit(item, user.getId());
            persistence.update(item);
            versioning.commit(item, user.getId(), "Počáteční revize dokumentu");

            AdminLogger.logEvent(user, "  add contract template " + item.getId());

            env.put(VAR_CONTRACT_TEMPLATE, template);
            if (result)
                ServletUtils.addMessage("Obrázek byl přidán na konec textu, přesuňte jej podle potřeby.", env, null);
            return actionAddStep1(request, env, navigator);
        } else {
            versioning.prepareObjectBeforeCommit(item, user.getId());
            persistence.create(item);

            versioning.commit(item, user.getId(), "Počáteční revize dokumentu");
            relation = new Relation(new Category(Constants.CAT_CONTRACTS), item, Constants.REL_CONTRACTS);
            persistence.create(relation);

            AdminLogger.logEvent(user, "  add contract template " + item.getId());

            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, urlUtils.make(UrlUtils.PREFIX_ADMINISTRATION + "/redakce/smlouvy/show/" + relation.getId()));
            return null;
        }
    }

    private String actionClone(HttpServletRequest request, Map env, PwdNavigator navigator) throws Exception {
        ContractTemplate template = (ContractTemplate) env.get(VAR_CONTRACT_TEMPLATE);
        env.put(VAR_CONTRACT_TEMPLATE, template);
        return actionAddStep1(request, env, navigator);
    }

    private String actionEditStep1(HttpServletRequest request, Map env, PwdNavigator navigator) {
		ContractTemplate template = (ContractTemplate) env.get(VAR_CONTRACT_TEMPLATE);
		Link tail = new Link(template.getTitle(), "edit/" + template.getId() + "?action=edit", "Editace smlouvy: " + template.getTitle());

		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));
		env.put(VAR_EDIT_MODE, Boolean.TRUE);

		return FMTemplateSelector.select("EditContract", "edit", env, request);
	}

	private String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env, PwdNavigator navigator) throws Exception {
		Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(VAR_RELATION);
        ContractTemplate template = (ContractTemplate) env.get(VAR_CONTRACT_TEMPLATE);
        ImageTool imageTool = null;
        FileItem fileItem = (FileItem) params.get(PARAM_PICTURE);
        if (fileItem != null && fileItem.getSize() > 0)
            imageTool = new ImageTool(fileItem, template, ImageTool.CONTRACT_IMAGE_RESTRICTIONS);

		ContractValidator validator = new ContractValidator(template, env, imageTool);

        boolean canContinue = validator.setAndValidate();
        if (canContinue && imageTool != null) {
            boolean result = imageTool.storeImage(ContractTemplate.Image.PICTURE, env, PARAM_PICTURE);
            if (result)
                ServletUtils.addMessage("Obrázek byl přidán na konec textu, přesuňte jej podle potřeby.", env, null);
            canContinue = false;
        }

        if (!canContinue || params.get(PARAM_PREVIEW) != null) {
            if (!canContinue)
                params.remove(PARAM_PREVIEW);
            env.put(VAR_PREVIEW, template);
            return actionEditStep1(request, env, navigator);
		}

        Item item = (Item) persistence.findById(new Item(template.getId()));
		item = BeanFlusher.flushContractTemplate(item, template);

        Versioning versioning = VersioningFactory.getVersioning();
        versioning.prepareObjectBeforeCommit(item, user.getId());
        persistence.update(item);
        versioning.commit(item, user.getId(), "");

        AdminLogger.logEvent(user, "  edit contract template " + item.getId());

        ServletUtils.addMessage("Smlouva " + template.getTitle() + " byla upravena", env, request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.make(UrlUtils.PREFIX_ADMINISTRATION + "/redakce/smlouvy/show/" + relation.getId()));
        return null;
	}

    private String actionPublish(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) env.get(VAR_RELATION);
        ContractTemplate template = (ContractTemplate) env.get(VAR_CONTRACT_TEMPLATE);
        template.setPublished(new Date());
        template.setDraft(false);

        Item item = (Item) persistence.findById(new Item(template.getId()));
        item = BeanFlusher.flushContractTemplate(item, template);
        persistence.update(item);
        AdminLogger.logEvent(user, "  publish contract template " + item.getId());

        // deprecate older
        List<Relation> previous = getPreviousContracts(template);
        for (Relation older : previous) {
            ContractTemplate old = BeanFetcher.fetchContractTemplate(older, FetchType.PROCESS_NONATOMIC);
            if ( ! (old.isObsolete() || old.isDraft())) {
                old.setObsolete(true);
                item = (Item) older.getChild();
                item = BeanFlusher.flushContractTemplate(item, old);
                persistence.update(item);
            }
        }

        ServletUtils.addMessage("Smlouva " + template.getTitle() + " byla publikována", env, request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.make(UrlUtils.PREFIX_ADMINISTRATION + "/redakce/smlouvy/show/" + relation.getId()));
        return null;
    }

    private String actionRemoveStep1(HttpServletRequest request, Map env) {
        ContractTemplate template = (ContractTemplate) env.get(VAR_CONTRACT_TEMPLATE);
        if ( template.getSignedContracts() > 0)
            env.put(VAR_UNDELETABLE, Boolean.TRUE);

        return FMTemplateSelector.select("EditContract", "remove", env, request);
    }

    private String actionRemoveStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);
        ContractTemplate template = (ContractTemplate) env.get(VAR_CONTRACT_TEMPLATE);
        if (template.getSignedContracts() > 0) {
            env.put(VAR_UNDELETABLE, Boolean.TRUE);
            return FMTemplateSelector.select("EditContract", "remove", env, request);
        }

        Map params = (Map) env.get(Constants.VAR_PARAMS);
        if (params.containsKey(PARAM_DELETE)) {
            Relation relation = (Relation) env.get(VAR_RELATION);
            persistence.remove(relation);

            AdminLogger.logEvent(user, "  deleted contract template " + template.getId());
            ServletUtils.addMessage("Smlouva " + template.getTitle() + " byla smazána", env, request.getSession());
        }

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.make(UrlUtils.PREFIX_ADMINISTRATION + "/redakce/smlouvy"));
        return null;
    }

    /**
     * Executes given contract template with values taken from passed author instance.
     * @param template the template, it must contain valid content
     * @param author initialized author
     * @return executed contract template
     * @throws IOException I/O error, impossible
     * @throws TemplateException freemarker language error in the template
     */
    public static String fillContractTemplate(ContractTemplate template, Author author) throws IOException, TemplateException {
        Author clonedAuthor = (Author) author.clone();
        String address = clonedAuthor.getAddress();
        if (address != null) {
            clonedAuthor.setAddress(Misc.normalizeWhiteSpaces(address));
        }

        Map env = new HashMap();
        env.put(VAR_AUTHOR, clonedAuthor);
        env.put(VAR_TODAY, new Date());
        return FMUtils.executeCode(template.getContent(), env);
    }

    /**
     * Get contract templates older than given template
     * @param current contract template
     * @return list of templates older than parameter current
     */
    private List<Relation> getPreviousContracts(ContractTemplate current) {
        SQLTool sqlTool = SQLTool.getInstance();
        CompareCondition condition = new CompareCondition(new Field(Field.ID, "R"), Operation.NOT_EQUAL, current.getRelationId());
        Qualifier[] qualifiers = new Qualifier[]{condition, Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, 2)};
        List<Relation> relations = sqlTool.findItemRelationsWithType(Item.CONTRACT_TEMPLATE, qualifiers);
        Tools.syncList(relations);
        return relations;
    }

    /**
	 * Validates contracts template content.
	 * @author kapy
	 */
	static class ContractValidator extends Validator<ContractTemplate> {
        ImageTool imageTool;

		@SuppressWarnings("serial")
		private final static Map<String, java.lang.reflect.Field> fields = new HashMap<String, java.lang.reflect.Field>() {
			{
				try {
					put(PARAM_TITLE, ContractTemplate.class.getDeclaredField("title"));
					put(PARAM_DESCRIPTION, ContractTemplate.class.getDeclaredField("description"));
					put(PARAM_CONTENT, ContractTemplate.class.getDeclaredField("content"));
				}
				catch (NoSuchFieldException e) {
					throw new InternalException("Invalid configuration of Contracts validator", e);
				}
			}
		};

		public ContractValidator(ContractTemplate contractTemplate, Map env, ImageTool imageTool) {
			super(contractTemplate, fields, env, null);
            this.imageTool = imageTool;
		}

		public boolean setAndValidate() {
			boolean result = validateNotEmptyAndSet(String.class, PARAM_TITLE, "Zadejte název smlouvy!");
			result &= validateNotEmptyAndSet(String.class, PARAM_DESCRIPTION, "Zadejte popis smlouvy!");
			result &= validateNotEmptyAndSet(String.class, PARAM_CONTENT, "Zadejte obsah (šablonu) smlouvy!");
            if (imageTool != null)
    			result &= imageTool.checkImage(env, PARAM_PICTURE);
			return result;
		}
    }
}
