package cz.abclinuxu.servlets.html.admin.edit;

import static cz.abclinuxu.servlets.Constants.PARAM_NAME;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Element;

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Author;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.security.Permissions;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.PwdNavigator;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.BeanFetcher;
import cz.abclinuxu.utils.BeanFlusher;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.BeanFetcher.FetchType;
import cz.abclinuxu.utils.freemarker.Tools;

public class EditAuthor implements AbcAction {

	public static final String PARAM_AUTHOR_ID = "aId";
	public static final String PARAM_SURNAME = "surname";
	public static final String PARAM_NICKNAME = "nickname";
	public static final String PARAM_BIRTH_NUMBER = "birthNumber";
	public static final String PARAM_ACCOUNT_NUMBER = "accountNumber";
	public static final String PARAM_EMAIL = "email";
	public static final String PARAM_PHONE = "phone";
	public static final String PARAM_ADDRESS = "address";
	public static final String PARAM_UID = "uid";
	public static final String PARAM_LOGIN = "login";
	public static final String PARAM_PREVIEW = "preview";

	public static final String VAR_AUTHOR = "AUTHOR";
	public static final String VAR_PREVIEW = "PREVIEW";
	public static final String VAR_EDIT_MODE = "EDIT_MODE";

	public static final String ACTION_ADD = "add";
	public static final String ACTION_ADD_STEP2 = "add2";
	public static final String ACTION_EDIT = "edit";
	public static final String ACTION_EDIT_STEP2 = "edit2";

	public String process(HttpServletRequest request,
			HttpServletResponse response, Map env) throws Exception {

		Persistence persistence = PersistenceFactory.getPersistence();
		Map params = (Map) env.get(Constants.VAR_PARAMS);
		User user = (User) env.get(Constants.VAR_USER);
		String action = (String) params.get(PARAM_ACTION);

		if (ServletUtils.handleMaintainance(request, env)) {
			response.sendRedirect(response.encodeRedirectURL("/"));
			return null;
		}

		if (action == null || action.length() == 0)
			throw new MissingArgumentException("Chybí parametr action!");

		// check permissions
		if (user == null)
			return FMTemplateSelector.select("AdministrationEditorsPortal",
					"login", env, request);

		PwdNavigator navigator = new PwdNavigator(user,
				PwdNavigator.NavigationType.ADMIN_AUTHORS);

		// add step 1
		if (action.equals(ACTION_ADD)) {
			if (!navigator.hasPermissions(Permissions.PERMISSION_CREATE))
				return FMTemplateSelector.select("AdministrationEditorsPortal",
						"forbidden", env, request);

			return actionAddStep1(request, response, env, navigator);
		}

		// add step 2
		if (action.equals(ACTION_ADD_STEP2)) {
			if (!navigator.hasPermissions(Permissions.PERMISSION_CREATE))
				return FMTemplateSelector.select("AdministrationEditorsPortal",
						"forbidden", env, request);

			ActionProtector.ensureContract(request, EditAuthor.class, true,
					true, true, false);
			return actionAddStep2(request, response, env, navigator);
		}

		// check edit permissions
		if (!navigator.hasPermissions(Permissions.PERMISSION_MODIFY))
			return FMTemplateSelector.select("AdministrationEditorsPortal",
					"forbidden", env, request);

		// find author
		Item item = (Item) InstanceUtils.instantiateParam(PARAM_AUTHOR_ID,
				Item.class, params, request);
		if(item==null)
			throw new MissingArgumentException("Chybí parametr aId!");
		
		persistence.synchronize(item);
		env.put(VAR_AUTHOR, BeanFetcher.fetchAuthorFromItem(item, FetchType.EAGER));
		
		if (action.equals(ACTION_EDIT))
			return actionEditStep1(request, env, navigator);

		if (action.equals(ACTION_EDIT_STEP2)) {
			ActionProtector.ensureContract(request, EditAuthor.class, true,
					true, true, false);
			return actionEditStep2(request, response, env, navigator);
		}

		throw new MissingArgumentException("Chybí parametr action!");
	}

	// first step of author creation
	public String actionAddStep1(HttpServletRequest request,
			HttpServletResponse response, Map env, PwdNavigator navigator)
			throws Exception {
		Link tail = new Link("Nový autor", "edit?action=add",
				"Vytvořit nového autora, krok 1");
		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));
		return FMTemplateSelector.select("AdministrationEditAuthor", "add",
				env, request);
	}

	public String actionAddStep2(HttpServletRequest request,
			HttpServletResponse response, Map env, PwdNavigator navigator) throws Exception {

		Persistence persistence = PersistenceFactory.getPersistence();
		Map params = (Map) env.get(Constants.VAR_PARAMS);
		User user = (User) env.get(Constants.VAR_USER);

		Link tail = new Link("Nový autor", "edit?action=add2",
				"Vytvořit nového autora, krok 2");
		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));

		Relation parent = (Relation) persistence.findById(new Relation(
				Constants.REL_AUTHORS));

		Item item = new Item(0, Item.AUTHOR);
		item.setOwner(user.getId());

		Category cat = (Category) parent.getChild();
		item.setGroup(cat.getGroup());
		item.setPermissions(cat.getPermissions());

		Author author = new Author();
		boolean canContinue = fillAuthor(env, author);

		if (!canContinue || params.get(PARAM_PREVIEW) != null) {
			if (!canContinue)
				params.remove(PARAM_PREVIEW);
			item.setInitialized(true);
			env.put(VAR_PREVIEW, item);
			return FMTemplateSelector.select("AdministrationEditAuthor", "add",
					env, request);
		}

		// refresh item content
		item = BeanFlusher.flushAuthorToItem(item, author);
		item.setTitle(Tools.getPersonName(item));
		persistence.create(item);

		// set url
		String url = proposeAuthorsUrl(author);
		url = URLManager.protectFromDuplicates(url);

		Relation relation = new Relation(parent.getChild(), item, parent
				.getId());
		relation.setUrl(url);
		persistence.create(relation);
		relation.getParent().addChildRelation(relation);

		env.put(VAR_AUTHOR, author);
		
		UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
		urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
		return null;
	}

	protected String actionEditStep1(HttpServletRequest request, Map env,
			PwdNavigator navigator) throws Exception {

		Persistence persistence = PersistenceFactory.getPersistence();
		Map params = (Map) env.get(Constants.VAR_PARAMS);
		Author author = (Author) env.get(VAR_AUTHOR);

		Link tail = new Link(author.getTitle(), "edit/"+ author.getId()+"?action=edit",
				"Editace autora, krok 1");
		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));

		params.put(PARAM_SURNAME, author.getSurname());
		params.put(PARAM_NAME, author.getName());
		params.put(PARAM_NICKNAME, author.getNickname());
		params.put(PARAM_BIRTH_NUMBER, author.getBirthNumber());
		params.put(PARAM_LOGIN, author.getLogin());
		params.put(PARAM_ACCOUNT_NUMBER, author.getAccountNumber());
		params.put(PARAM_EMAIL, author.getEmail());
		params.put(PARAM_PHONE, author.getPhone());
		params.put(PARAM_ADDRESS, author.getAddress());

		env.put(VAR_EDIT_MODE, Boolean.TRUE);

		return FMTemplateSelector.select("AdministrationEditAuthor", "edit",
				env, request);
	}

	protected String actionEditStep2(HttpServletRequest request,
			HttpServletResponse response, Map env, PwdNavigator navigator)
			throws Exception {

		Persistence persistence = PersistenceFactory.getPersistence();
		SQLTool sqlTool = SQLTool.getInstance();
		Map params = (Map) env.get(Constants.VAR_PARAMS);

		Author author = (Author) env.get(VAR_AUTHOR);
		
		Link tail = new Link(author.getTitle(), "edit/" + author.getId() + "?action=edit2",
				"Editace autora, krok 2");
		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));

		boolean canContinue = fillAuthor(env, author);

		Item item = (Item) persistence.findById(new Item(author.getId()));
		Relation relation = findParent(item);
		
		if (!canContinue || params.get(PARAM_PREVIEW) != null) {
			if (!canContinue)
				params.remove(PARAM_PREVIEW);

			item.setInitialized(true);
			env.put(VAR_PREVIEW, item);
			env.put(VAR_EDIT_MODE, Boolean.TRUE);
			return FMTemplateSelector.select("AdministrationEditAuthor",
					"edit", env, request);
		}

		// refresh item content
		item.setTitle(author.getTitle());
		item = BeanFlusher.flushAuthorToItem(item, author);
		persistence.update(item);

		String url = proposeAuthorsUrl(author);
		if (!url.equals(relation.getUrl())) {
			url = URLManager.protectFromDuplicates(url);
			sqlTool.insertOldAddress(relation.getUrl(), url, relation.getId());
			relation.setUrl(url);
			persistence.update(relation);
		}

		env.put(VAR_AUTHOR, author);
		UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
		urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
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

		String url = UrlUtils.PREFIX_AUTHORS + "/"
				+ URLManager.enforceRelativeURL(sb.toString());
		return url;
	}

	/**
	 * Creates author from parameters passed
	 * 
	 * @param env
	 *            Variables holder
	 * @param author
	 *            Author
	 * @return {@code true} if checks passed, {@code false} otherwise
	 */
	private boolean fillAuthor(Map env, Author author) {

		Map params = (Map) env.get(Constants.VAR_PARAMS);
		boolean result = true;
		String surname = (String) params.get(PARAM_SURNAME);
		if (!Misc.empty(surname))
			author.setSurname(surname);
		else {
			ServletUtils
					.addError(PARAM_SURNAME, "Zadejte příjmení!", env, null);
			result = false;
		}
		String login = (String) params.get(PARAM_LOGIN);
		if (Misc.empty(login)) {
			author.setUid(null);
		} else {
			Integer uid = SQLTool.getInstance().getUserByLogin(login);
			if (uid == null) {
				ServletUtils.addError(PARAM_LOGIN, "Zadejte login!", env, null);
				result = false;
			}
			author.setUid(uid);
		}

		String tmp = (String) params.get(PARAM_NAME);
		author.setName(tmp);
		tmp = (String) params.get(PARAM_NICKNAME);
		author.setNickname(tmp);
		tmp = (String) params.get(PARAM_BIRTH_NUMBER);
		author.setBirthNumber(tmp);
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

	public Relation findParent(Item item) {
		Persistence persistence = PersistenceFactory.getPersistence();
		List<Relation> parents = persistence.findRelationsFor(item);

		if (parents.size() == 1)
			return parents.get(0);

		throw new MissingArgumentException(
			"Nepodařilo se najít rodičovskou relaci pro autora!");
	}

}
