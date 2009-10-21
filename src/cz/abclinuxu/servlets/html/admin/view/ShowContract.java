package cz.abclinuxu.servlets.html.admin.view;

import static cz.abclinuxu.utils.forms.FormFilter.Filter.CONTRACT_BY_DESCRIPTION;
import static cz.abclinuxu.utils.forms.FormFilter.Filter.CONTRACT_BY_TITLE;
import static cz.abclinuxu.utils.forms.FormFilter.Filter.CONTRACT_BY_VERSION;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Author;
import cz.abclinuxu.data.view.Contract;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.exceptions.InvalidDataException;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.OperationIn;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.QualifierTool;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.admin.edit.EditTopic;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.PageNavigation;
import cz.abclinuxu.servlets.utils.url.PwdNavigator;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.BeanFetcher;
import cz.abclinuxu.utils.BeanFlusher;
import cz.abclinuxu.utils.DateTool;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.BeanFetcher.FetchType;
import cz.abclinuxu.utils.forms.FormFilter;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.paging.Paging;
import freemarker.template.TemplateException;

public class ShowContract implements AbcAction {
	private static final Logger log = Logger.getLogger(ShowContract.class);

	/**
	 * list of found contracts that matched the conditions
	 */
	public static final String VAR_CONTRACTS = "CONTRACTS";

	/**
	 * a contract which have to be accepted
	 */
	public static final String VAR_NEW_CONTRACT = "NEW_CONTRACT";

	/**
	 * Active authors, that is authors which can have assigned contract
	 */
	public static final String VAR_AUTHORS = "AUTHORS";

	/**
	 * Active author, that is author assigned to current user
	 */
	public static final String VAR_AUTHOR = "AUTHOR";

	/**
	 * content of contract as generated from template
	 */
	public static final String VAR_DRAFT = "DRAFT";

	public static final String VAR_TODAY = "TODAY";

	/**
	 * contracts found for narrowing conditions
	 */
	public static final String VAR_FOUND = "FOUND";

	/**
	 * filtering object
	 */
	public static final String VAR_FILTER = "FILTER";

	/**
	 * Starting part of URL, until value of from parameter
	 */
	public static final String VAR_URL_BEFORE_FROM = "URL_BEFORE_FROM";
	/**
	 * Final part of URL, after value of from parameter
	 */
	public static final String VAR_URL_AFTER_FROM = "URL_AFTER_FROM";

	/**
	 * Location of author when signing contract
	 */
	public static final String PARAM_LOCATION = "location";

	/**
	 * Contracts to be assigned to author
	 */
	public static final String PARAM_CONTRACT_ID = "contractId";

	public static final String PARAM_AUTHOR_ID = "authorId";

	public static final String PARAM_FROM = "from";

	public static final String ACTION_ACCEPT = "accept";
	public static final String ACTION_ASSIGN = "assign";
	public static final String ACTION_ASSIGN2 = "assign2";

	public static final String ACTION_LIST = "list";
	public static final String ACTION_AUTHOR_LIST = "author-list";

	@Override
	public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {

		Map params = (Map) env.get(Constants.VAR_PARAMS);
		User user = (User) env.get(Constants.VAR_USER);
		String action = (String) params.get(PARAM_ACTION);

		if (ServletUtils.handleMaintainance(request, env)) {
			response.sendRedirect(response.encodeRedirectURL("/"));
			return null;
		}

		if (user == null)
		    return FMTemplateSelector.select("AdministrationAEPortal", "login", env, request);

		PwdNavigator navigator = new PwdNavigator(env, PageNavigation.AUTHORS_EDITORS_PORTAL);

		// TODO if contracts are extended to handle different contract relation than 
		// that between author and editor (for example developer), this 
		// is an extension point, for current moment handle only author related contract logic
		Author author = getAuthor(user);
		env.put(VAR_AUTHOR, author);
		if (author == null) {
			throw new InvalidDataException("Nelze podepsat smlouvu jinou než autorskou!");
		}

		if (!navigator.permissionsFor(author).canModify()) {
			return FMTemplateSelector.select("AdministrationAEPortal", "forbidden", env, request);
		}

		// administration actions
		if (ServletUtils.pathBeginsWith(request, UrlUtils.PREFIX_ADMINISTRATION)) {

			// prepare contract(s) to be assigned
			if (ServletUtils.determineAction(params, ACTION_ASSIGN)) {
				if (!navigator.permissionsFor(new Relation(Constants.REL_CONTRACTS)).canCreate()) {
					return FMTemplateSelector.select("AdministrationAEPortal", "forbidden", env, request);
				}
				return actionAssignStep1(request, env, params, navigator);
			}
			// assign contracts
			else if (ServletUtils.determineAction(params, ACTION_ASSIGN2)) {
				if (!navigator.permissionsFor(new Relation(Constants.REL_CONTRACTS)).canCreate()) {
					return FMTemplateSelector.select("AdministrationAEPortal", "forbidden", env, request);
				}
				ActionProtector.ensureContract(request, ShowContract.class, true, true, true, false);
				return actionAssignStep2(request, response, env, params, navigator);
			}

			// list contracts
			else if (ServletUtils.determineAction(params, ACTION_LIST) || Misc.empty(action)) {
				if (!navigator.permissionsFor(new Relation(Constants.REL_CONTRACTS)).canCreate()) {
					return FMTemplateSelector.select("AdministrationAEPortal", "forbidden", env, request);
				}
				return actionContractList(request, env, params, navigator);
			}
		}
		// author actions
		else if (ServletUtils.pathBeginsWith(request, "/redakce")) {
			// list accepted contracts, prepare contract to be signed if any
			if (ServletUtils.determineAction(params, ACTION_AUTHOR_LIST) || Misc.empty(action)) {
				return actionAuthorContractList(request, env, params, navigator);
			}
			else if (ServletUtils.determineAction(params, ACTION_ACCEPT) || Misc.empty(action)) {
				return actionAccept(request, response, env, params, navigator);
			}
		}

		throw new MissingArgumentException("Chybí parametr action!");

	}

	private String actionContractList(HttpServletRequest request, Map env, Map params, PwdNavigator navigator) {

		int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
		int count = Misc.getPageSize(50, 50, env, null);

		// store navigation structure
		Link tail = new Link("Správa smluv", "smlouvy", "Správa smluv");
		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));

		Paging found = null;
		int total = 0;
		SQLTool sqlTool = SQLTool.getInstance();

		FormFilter filter = createFilter(params);
		env.put(VAR_FILTER, filter);

		List<Qualifier> preQualifiers = new ArrayList<Qualifier>() {
			{
				add(new CompareCondition(Field.SUBTYPE, Operation.EQUAL, Constants.TYPE_CONTRACT));
			}
		};

		Qualifier[] qualifiers = getQualifiers(preQualifiers, filter, from, count);
		List<Item> items = sqlTool.findItemsWithType(Item.TEMPLATE, qualifiers);
		total = sqlTool.countItemsWithType(Item.TEMPLATE, QualifierTool.removeOrderQualifiers(qualifiers));
		found = new Paging(BeanFetcher.fetchContractsFromItems(items, FetchType.PROCESS_NONATOMIC), from, count, total, qualifiers);

		env.put(VAR_FOUND, found);

		// store url links
		env.put(VAR_URL_BEFORE_FROM, "/sprava/redakce/smlouvy/?action=list&from=");
		env.put(VAR_URL_AFTER_FROM, filter.encodeAsURL());
		return FMTemplateSelector.select("AdministrationShowContract", "list", env, request);
	}

	private String actionAuthorContractList(HttpServletRequest request, Map env, Map params, PwdNavigator navigator) {

		SQLTool sqlTool = SQLTool.getInstance();
		User user = (User) env.get(Constants.VAR_USER);

		// store navigation structure
		Link tail = new Link("Mé smlouvy", "smlouvy", "Mnou schválené smlouvy");
		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));

		// get all contracts for current user
		Qualifier[] qualifiers = new Qualifier[] {
		        new CompareCondition(Field.NUMERIC1, Operation.EQUAL, user.getId()),
		        Qualifier.SORT_BY_DATE1,
		        Qualifier.ORDER_DESCENDING
		        };

		List<Item> items = sqlTool.findItemsWithType(Item.CONTRACT, qualifiers);
		List<Item> accepted = new ArrayList<Item>(items);
		List<Item> toBeSigned = new ArrayList<Item>();
		Iterator<Item> i = accepted.iterator();
		while (i.hasNext()) {
			Item item = i.next();
			// item was not signed by author
			if (item.getDate1() == null) {
				i.remove();
				// contract has to be signed 
				toBeSigned.add(item);
			}
		}
		// get contract which was added as last
		if (!toBeSigned.isEmpty()) {
			Collections.sort(toBeSigned, new Date2ItemComparator(false));
			Contract newContract = BeanFetcher.fetchContractFromItem(toBeSigned.get(0), FetchType.EAGER);
			env.put(VAR_NEW_CONTRACT, newContract);
			env.put(VAR_TODAY, new Date());
			env.put(VAR_DRAFT, processContractDraft(newContract, env));

		}

		if (log.isDebugEnabled()) {
			log.debug(String.format("Contracts for user %d were found total: %d, accepted: %d, to be signed: %d",
			        user.getId(), items.size(), accepted.size(), toBeSigned.size()));
		}

		// put accepted contracts
		if (!accepted.isEmpty())
		    env.put(VAR_CONTRACTS, BeanFetcher.fetchContractsFromItems(accepted, FetchType.PROCESS_NONATOMIC));

		return FMTemplateSelector.select("AdministrationShowContract", "author-list", env, request);
	}

	// Prepare assigned contracts to be assigned to active authors
	private String actionAssignStep1(HttpServletRequest request, Map env, Map params, PwdNavigator navigator) {

		// get filter because it must be stored
		FormFilter filter = createFilter(params);

		Link tail = new Link("Přiřazení smluv autorům", "smlouvy/?action=assign" + filter.encodeAsURL(), "Přiřazení smluv autorům");
		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));
		env.put(VAR_AUTHORS, EditTopic.getActiveAuthors(env));

		env.put(VAR_FILTER, filter);
		env.put(VAR_CONTRACTS, getContract(params));

		return FMTemplateSelector.select("AdministrationShowContract", "assign", env, request);
	}

	private String actionAssignStep2(HttpServletRequest request, HttpServletResponse response, Map env, Map params, PwdNavigator navigator) throws Exception {

		Persistence persistence = PersistenceFactory.getPersistence();
		List<Contract> contracts = getContract(params);
		List<Integer> users = getUsers(params);

		for (Integer userId : users) {
			for (Contract template : contracts) {
				Contract contract = new Contract(template);
				contract.setEmployee(new User(userId));
				contract.setTemplateId(template.getId());
				Item item = new Item(0, Item.CONTRACT);

				persistence.create(BeanFlusher.flushContractToItem(item, contract));
			}
		}

		ServletUtils.addMessage("Smlouvy byly přiřazeny", env, request.getSession());
		redirect(response, env);
		return null;
	}

	private String actionAccept(HttpServletRequest request, HttpServletResponse response, Map env, Map params, PwdNavigator navigator) throws Exception {

		SQLTool sqlTool = SQLTool.getInstance();
		Persistence persistence = PersistenceFactory.getPersistence();
		User user = (User) env.get(Constants.VAR_USER);

		Integer contractId = Misc.parseInt((String) params.get(PARAM_CONTRACT_ID), 0);
		String location = (String) params.get(PARAM_LOCATION);

		Qualifier[] qualifiers = new Qualifier[] {
		        new CompareCondition(Field.ID, Operation.EQUAL, contractId)
		        };

		Item item = sqlTool.findItemsWithType(Item.CONTRACT, qualifiers).get(0);
		Contract contract = BeanFetcher.fetchContractFromItem(item, FetchType.EAGER);
		contract.setContent(processContractDraft(contract, env));
		contract.setEmployee(user);
		contract.setEffectiveDate(new Date());

		String today = (new DateTool()).show(new Date(), "CZ_DMY", false);

		// add contract location
		String suffix =
		        "<div class=\"two-columns\"> " +
		        "<div class=\"left-column\"> " +
		        "V " + location + " dne " + today + " <br /> " +
		        user.getName() + " <br/> " +
		        "</div> " +
		        "<div class=\"right-column\"> " +
		        " V Praze dne " + today + " <br /> " +
		        "<img src=\"" + contract.getEmployerSignature() + "\" /> <br /> " +
		        contract.getEmployer().getName() + " <br /> " +
		        "Objednatel</div></div>";

		contract.setContent(contract.getContent() + suffix);

		persistence.update(BeanFlusher.flushContractToItem(item, contract));

		ServletUtils.addMessage("Smlouvy byla přijata", env, request.getSession());
		redirect(response, env);
		return null;

	}

	/**
	 * Finds template for contract specified by passed contract and evaluates
	 * it within passed environment
	 * 
	 * @param contract Contract whose template is searched and evaluated
	 * @param env Environment used to evaluate template
	 * @return Evaluated template as string
	 */
	private String processContractDraft(Contract contract, Map env) {
		try {
			String text = contract.getContent();
			return FMUtils.executeCode(text, env);
		}
		catch (NullPointerException e) {
			throw new InvalidDataException("Nelze nalézt šablonu pro smlouvu: " + contract.getTitle(), e);
		}
		catch (TemplateException e) {
			throw new InvalidDataException("Nemohu zpracovat šablonu pro smlouvu: " + contract.getTitle(), e);
		}
		catch (IOException e) {
			throw new InvalidDataException("Nemohu zpracovat šablonu pro smlouvu: " + contract.getTitle(), e);
		}

	}

	/**
	 * Constructs qualifiers from input passed in form. Allows initial argument
	 * to be passed by the first argument
	 * 
	 * @param preQualifiers Qualifiers applied before filtering
	 * @param filter Filter object constructed from HTTP parameters
	 * @param from From parameter for paging results
	 * @param count Count limit parameter for paging results
	 * @return Array of qualifiers
	 */
	private Qualifier[] getQualifiers(List<Qualifier> preQualifiers, FormFilter filter, int from, int count) {

		List<Qualifier> qualifiers = preQualifiers == null ? new ArrayList<Qualifier>() : new ArrayList<Qualifier>(preQualifiers);
		qualifiers.addAll(filter.getQualifiers());
		// sort by surname in ascending order
		qualifiers.add(Qualifier.SORT_BY_DATE2);
		qualifiers.add(Qualifier.ORDER_DESCENDING);
		qualifiers.add(new LimitQualifier(from, count));

		return qualifiers.toArray(Qualifier.ARRAY_TYPE);
	}

	/**
	 * Constructs filter from parameters. Adds additional data for filtering.
	 * 
	 * @param params Map with passed parameters
	 * @return Filter containing both SQL filters and additional filtering
	 *         items.
	 */
	private FormFilter createFilter(Map params) {
		FormFilter filter = new FormFilter(params, CONTRACT_BY_DESCRIPTION, CONTRACT_BY_TITLE, CONTRACT_BY_VERSION);
		return filter.appendFilterQualifier(PARAM_CONTRACT_ID, null, Tools.asList(params.get(PARAM_CONTRACT_ID)));
	}

	/**
	 * Returns author from user object
	 * 
	 * @param user User object
	 * @return Author object if found or {@code null} if there is no author for
	 *         given object
	 */
	private Author getAuthor(User user) {
		SQLTool sqlTool = SQLTool.getInstance();
		Item item = sqlTool.findAuthorByUserId(user.getId());
		if (item == null)
		    return null;

		return BeanFetcher.fetchAuthorFromItem(item, FetchType.EAGER);
	}

	private List<Contract> getContract(Map params) {
		@SuppressWarnings("unchecked")
		List<String> strings = (List<String>) Tools.asList(params.get(PARAM_CONTRACT_ID));

		if (Misc.empty(strings)) {
			return Collections.emptyList();
		}

		List<Integer> ids = new ArrayList<Integer>(strings.size());
		for (String topicId : strings) {
			try {
				ids.add(Integer.parseInt(topicId));
			}
			catch (NumberFormatException e) {
				log.warn("Passed invalid contract template identification: " + topicId);
			}
		}

		if (Misc.empty(ids)) {
			return Collections.emptyList();
		}

		SQLTool sqlTool = SQLTool.getInstance();

		Qualifier[] qualifiers = new Qualifier[] {
		        new CompareCondition(Field.ID, new OperationIn(ids.size()), ids),
		        new CompareCondition(Field.SUBTYPE, Operation.EQUAL, Constants.TYPE_CONTRACT)
		        };
		// we must process non-atomically for author ids are stored in XML
		return BeanFetcher.fetchContractsFromItems(sqlTool.findItemsWithType(Item.TEMPLATE, qualifiers), FetchType.PROCESS_NONATOMIC);

	}

	private List<Integer> getUsers(Map params) {
		@SuppressWarnings("unchecked")
		List<String> strings = (List<String>) Tools.asList(params.get(PARAM_AUTHOR_ID));

		if (Misc.empty(strings)) {
			return Collections.emptyList();
		}

		List<Integer> ids = new ArrayList<Integer>(strings.size());
		for (String topicId : strings) {
			try {
				ids.add(Integer.parseInt(topicId));
			}
			catch (NumberFormatException e) {
				log.warn("Passed invalid author identification: " + topicId);
			}
		}

		if (Misc.empty(ids)) {
			return Collections.emptyList();
		}

		log.warn("Total users" + ids.size());

		SQLTool sqlTool = SQLTool.getInstance();

		Qualifier[] qualifiers = new Qualifier[] {
		        new CompareCondition(Field.ID, new OperationIn(ids.size()), ids),
		        };

		List<Author> authors = BeanFetcher.fetchAuthorsFromItems(sqlTool.findItemsWithType(Item.AUTHOR, qualifiers), FetchType.LAZY);
		log.warn("Authors: " + authors.size());

		// retrieve user ids
		List<Integer> userIds = new ArrayList<Integer>(authors.size());
		for (Author author : authors) {
			userIds.add(author.getUid());
		}

		log.warn("Total users authors: " + userIds.size());

		return userIds;
	}

	public static void redirect(HttpServletResponse response, Map env) throws Exception {
		UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
		// redirect to topics in administration system
		urlUtils.redirect(response, urlUtils.make(UrlUtils.PREFIX_ADMINISTRATION + "/redakce/smlouvy/show?action=list"));
	}

	/**
	 * Compares items according to date2 column.
	 * 
	 * @author kapy
	 * 
	 */
	public static class Date2ItemComparator implements Comparator<Item> {

		// direction of comparision
		private boolean ascending;

		public Date2ItemComparator() {
			this(true);
		}

		public Date2ItemComparator(boolean ascending) {
			this.ascending = ascending;
		}

		@Override
		public int compare(Item o1, Item o2) {
			if (ascending)
				return o1.getDate2().compareTo(o2.getDate2());
			else
				return o2.getDate2().compareTo(o1.getDate2());
		}
	}

}
