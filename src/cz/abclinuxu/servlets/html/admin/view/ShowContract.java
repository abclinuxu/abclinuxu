package cz.abclinuxu.servlets.html.admin.view;

import static cz.abclinuxu.utils.forms.FormFilter.Filter.CONTRACT_BY_DESCRIPTION;
import static cz.abclinuxu.utils.forms.FormFilter.Filter.CONTRACT_BY_TITLE;
import static cz.abclinuxu.utils.forms.FormFilter.Filter.CONTRACT_BY_VERSION;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Author;
import cz.abclinuxu.data.view.Contract;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.data.view.Contract.ContractComparator;
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
import cz.abclinuxu.servlets.utils.url.PwdNavigator.Discriminator;
import cz.abclinuxu.utils.BeanFetcher;
import cz.abclinuxu.utils.BeanFlusher;
import cz.abclinuxu.utils.InstanceUtils;
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
	 * Tail of contract
	 */
	public static final String VAR_CONTRACT_TAIL = "CONTRACT_TAIL";

	/**
	 * Contract to be shown
	 */
	public static final String VAR_CONTRACT = "CONTRACT";

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

	/**
	 * This day
	 */
	public static final String VAR_TODAY = "TODAY";

	/**
	 * Location of author
	 */
	public static final String VAR_LOCATION = "LOCATION";

	/**
	 * contracts found for narrowing conditions
	 */
	public static final String VAR_FOUND = "FOUND";

	/**
	 * filtering object
	 */
	public static final String VAR_FILTER = "FILTER";

	/**
	 * Editor flag
	 */
	public static final String VAR_EDITOR = "EDITOR";

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

	public static final String ACTION_SHOW = "show";
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

		PwdNavigator navigator = new PwdNavigator(env, PageNavigation.ADMIN_CONTRACTS);

		// contracts are directly bound to author
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
			// show contract
			else if (ServletUtils.determineAction(params, ACTION_SHOW)) {
				return actionShowTemplate(request, env, params, navigator);
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

			if (ServletUtils.determineAction(params, ACTION_ACCEPT)) {
				return actionAccept(request, response, env, params, navigator);
			}
			// show contract
			if (ServletUtils.determineAction(params, ACTION_SHOW)) {
				return actionShow(request, env, params, navigator);
			}

			// list accepted contracts, prepare contract to be signed if any
			else if (ServletUtils.determineAction(params, ACTION_AUTHOR_LIST) || Misc.empty(action)) {
				return actionAuthorContractList(request, env, params, navigator);
			}

		}

		throw new MissingArgumentException("Chybí parametr action!");

	}

	private String actionShowTemplate(HttpServletRequest request, Map env, Map params, PwdNavigator navigator) {

		if (navigator.determine() == Discriminator.EDITOR) {
			env.put(VAR_EDITOR, Boolean.TRUE);
		}

		// determine given contract by id
		Persistence persistence = PersistenceFactory.getPersistence();
		Item item = (Item) InstanceUtils.instantiateParam(PARAM_CONTRACT_ID, Item.class, params, request);
		if (item == null)
		    throw new MissingArgumentException("Chybí parametr contractId!");
		persistence.synchronize(item);
		Contract contract = BeanFetcher.fetchContractFromItem(item, FetchType.EAGER);
		env.put(VAR_CONTRACT, contract);

		Link tail = new Link(contract.getTitle(), "/redakce/smlouvy/?contractId=" + contract.getId() + "&amp;action=show", "Zobrazení smlouvy");
		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));
		return FMTemplateSelector.select("AdministrationShowContract", "show", env, request);
	}

	private String actionShow(HttpServletRequest request, Map env, Map params, PwdNavigator navigator) {

		// determine given contract by id
		Persistence persistence = PersistenceFactory.getPersistence();

		int id = 0;
		try {
			id = Integer.parseInt((String) params.get(PARAM_CONTRACT_ID));
		}
		catch (NumberFormatException e) {
			throw new MissingArgumentException("Chybí parametr contractId!");
		}

		Relation relation = (Relation) persistence.findById(new Relation(id));
		if (relation == null)
		    throw new MissingArgumentException("Chybí parametr contractId!");
		// set contract as signed
		persistence.synchronize(relation);
		Contract contract = BeanFetcher.fetchContractFromRelation(relation, FetchType.EAGER);
		env.put(VAR_CONTRACT, contract);

		Link tail = new Link(contract.getTitle(), "/redakce/smlouvy/?contractId=" + contract.getId() + "&amp;action=show", "Zobrazení smlouvy");
		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));
		return FMTemplateSelector.select("AdministrationShowContract", "show", env, request);
	}

	private String actionContractList(HttpServletRequest request, Map env, Map params, PwdNavigator navigator) {

		int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
		int count = Misc.getPageSize(50, 50, env, null);

		// store navigation structure		
		env.put(Constants.VAR_PARENTS, navigator.navigate());

		Paging found = null;
		int total = 0;
		SQLTool sqlTool = SQLTool.getInstance();

		FormFilter filter = createFilter(params);
		env.put(VAR_FILTER, filter);

		Qualifier[] qualifiers = getQualifiers(null, filter, from, count);
		List<Item> items = sqlTool.findItemsWithType(Item.CONTRACT, qualifiers);
		total = sqlTool.countItemsWithType(Item.CONTRACT, QualifierTool.removeOrderQualifiers(qualifiers));
		found = new Paging(BeanFetcher.fetchContractsFromItems(items, FetchType.PROCESS_NONATOMIC), from, count, total, qualifiers);

		env.put(VAR_FOUND, found);

		// store url links
		env.put(VAR_URL_BEFORE_FROM, "/sprava/redakce/smlouvy/?action=list&from=");
		env.put(VAR_URL_AFTER_FROM, filter.encodeAsURL());
		return FMTemplateSelector.select("AdministrationShowContract", "list", env, request);
	}

	private String actionAuthorContractList(HttpServletRequest request, Map env, Map params, PwdNavigator navigator) {

		Persistence persistence = PersistenceFactory.getPersistence();
		Author author = (Author) env.get(VAR_AUTHOR);

		// store navigation structure		
		env.put(Constants.VAR_PARENTS, navigator.navigate());

		// go trough all relations for this author and find to be signed and not signed
		List<Relation> relations = getContracts(author);
		int total = relations.size();
		List<Contract> toBeSigned = new ArrayList<Contract>();
		Iterator<Relation> i = relations.iterator();
		while (i.hasNext()) {
			Relation relation = i.next();
			Contract contract = BeanFetcher.fetchContractFromRelation(relation, FetchType.PROCESS_NONATOMIC);
			// contract was not accepted and it is not obsolete, propose it 
			if (!contract.isAccepted() && !contract.isObsolete()) {
				i.remove();
				// get template's content
				toBeSigned.add(contract);
				log.debug("To be signed with id:" + contract.getId());
			}
			// remove obsolete contracts from accepted
			else if (!contract.isAccepted() && contract.isObsolete()) {
				i.remove();
			}
		}
		// get contract which was added as last
		// we will use template for content and then we must reset id to be the id of relation
		if (!toBeSigned.isEmpty()) {
			Collections.sort(toBeSigned, new ContractComparator(false));
			Contract first = toBeSigned.get(0);
			Item item = (Item) persistence.findById(new Item(first.getTemplateId(), Item.CONTRACT));
			persistence.synchronize(item);
			Contract newContract = BeanFetcher.fetchContractFromItem(item, FetchType.EAGER);
			newContract.setId(first.getId());
			newContract.setTemplateId(first.getTemplateId());
			newContract.setEmployee(author);
			env.put(VAR_NEW_CONTRACT, newContract);
			env.put(VAR_TODAY, new Date());
			env.put(VAR_DRAFT, processContractDraft(newContract, env));

		}

		if (log.isDebugEnabled()) {
			log.debug(String.format("Contracts for author %d were found total: %d, accepted: %d, to be signed: %d",
			        author.getId(), total, relations.size(), toBeSigned.size()));
		}

		// put accepted contracts
		if (!relations.isEmpty()) {
			List<Contract> contracts = BeanFetcher.fetchContractsFromRelations(relations, FetchType.PROCESS_NONATOMIC);
			Collections.sort(contracts, new ContractComparator(false));
			env.put(VAR_CONTRACTS, contracts);
		}

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
		env.put(VAR_CONTRACTS, getContracts(params));

		return FMTemplateSelector.select("AdministrationShowContract", "assign", env, request);
	}

	private String actionAssignStep2(HttpServletRequest request, HttpServletResponse response, Map env, Map params, PwdNavigator navigator) throws Exception {

		Persistence persistence = PersistenceFactory.getPersistence();
		List<Contract> contracts = getContracts(params);
		List<Author> authors = getAuthors(params);

		log.debug(String.format("To be assigned: %d contracts, %d authors", contracts.size(), authors.size()));

		// for all authors assign them all templates selected
		for (Author author : authors) {

			// already signed 
			List<Contract> signed = BeanFetcher.fetchContractsFromRelations(getContracts(author), FetchType.PROCESS_NONATOMIC);
			Set<Integer> usedTemplates = new HashSet<Integer>(signed.size());
			for (Contract tmp : signed) {
				usedTemplates.add(tmp.getTemplateId());
			}

			for (Contract template : contracts) {

				// skip already used templates
				if (usedTemplates.contains(template.getId()))
				    continue;

				Contract contract = new Contract(template);
				contract.setEmployee(author);
				contract.setTemplateId(template.getId());

				// force creation of new relation
				Relation relation = new Relation(new Category(Constants.CAT_CONTRACTS), new Item(author.getId(), Item.AUTHOR), Constants.REL_CONTRACTS);
				persistence.create(BeanFlusher.flushContractToRelation(relation, contract));
			}
		}

		ServletUtils.addMessage("Smlouvy byly přiřazeny", env, request.getSession());
		UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
		// redirect to topics in administration system
		urlUtils.redirect(response, urlUtils.noPrefix("/sprava/redakce/smlouvy"));
		return null;
	}

	private String actionAccept(HttpServletRequest request, HttpServletResponse response, Map env, Map params, PwdNavigator navigator) throws Exception {

		Persistence persistence = PersistenceFactory.getPersistence();
		Author author = (Author) env.get(VAR_AUTHOR);

		env.put(VAR_LOCATION, params.get(PARAM_LOCATION));

		int id = 0;
		try {
			id = Integer.parseInt((String) params.get(PARAM_CONTRACT_ID));
		}
		catch (NumberFormatException e) {
			throw new MissingArgumentException("Chybí parametr contractId!");
		}

		Relation relation = (Relation) persistence.findById(new Relation(id));
		if (relation == null)
		    throw new MissingArgumentException("Chybí parametr contractId!");
		// set contract as signed
		persistence.synchronize(relation);

		Contract contract = BeanFetcher.fetchContractFromRelation(relation, FetchType.EAGER);
		contract.setSignedDate(new Date());
		env.put(VAR_NEW_CONTRACT, contract);

		StringBuilder content = new StringBuilder();
		content.append(processContractDraft(contract, env));
		content.append(processContractTail(contract, env));
		contract.setContent(content.toString());

		persistence.update(BeanFlusher.flushContractToRelation(relation, contract));

		// set old contracts as obsolete
		List<Contract> oldCandidates = BeanFetcher.fetchContractsFromRelations(getContracts(author), FetchType.PROCESS_NONATOMIC);
		for (Contract candidate : oldCandidates) {
			// check if not accepted above
			if (!candidate.isAccepted() && contract.getId() != candidate.getId()) {
				candidate.setObsolete(true);
				Relation underlying = (Relation) persistence.findById(new Relation(candidate.getId()));
				persistence.synchronize(underlying);
				persistence.update(BeanFlusher.flushContractToRelation(underlying, candidate));
			}
		}

		ServletUtils.addMessage("Smlouvy byla přijata", env, request.getSession());
		UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
		// redirect to topics in administration system
		urlUtils.redirect(response, urlUtils.make("/redakce/smlouvy/?contractId=" + contract.getId() + "&action=show"));
		return null;

	}

	// append contract tail
	private String processContractTail(Contract contract, Map env) {
		try {
			return FMUtils.executeTemplate("/include/misc/contract_tail.ftl", env);
		}
		catch (IOException e) {
			log.error("Unable to access contract tail template", e);
			throw new InvalidDataException("Nelze nalézt šablonu smlouvy", e);
		}
		catch (TemplateException e) {
			log.error("Unable to process contract tail template", e);
			throw new InvalidDataException("Nelze zpracovat šablonu smlouvy", e);
		}
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
			log.error("Cannot find template for contract", e);
			throw new InvalidDataException("Nelze nalézt šablonu pro smlouvu: " + contract.getTitle(), e);
		}
		catch (TemplateException e) {
			log.error("Cannot process template for contract", e);
			throw new InvalidDataException("Nemohu zpracovat šablonu pro smlouvu: " + contract.getTitle(), e);
		}
		catch (IOException e) {
			log.error("Cannot process template for contract", e);
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
		qualifiers.add(Qualifier.SORT_BY_DATE1);
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

	// get contracts for author
	private List<Relation> getContracts(Author author) {

		SQLTool sqlTool = SQLTool.getInstance();
		Persistence persistence = PersistenceFactory.getPersistence();
		// get all contracts for current user
		Qualifier[] qualifiers = new Qualifier[] {
		        new CompareCondition(Field.CHILD, Operation.EQUAL, author.getId()),
		        new CompareCondition(Field.UPPER, Operation.EQUAL, Constants.REL_CONTRACTS)
		        };

		// go trough all relations for this author and find to be signed and signed
		List<Relation> list = sqlTool.findItemRelationsWithType(Item.AUTHOR, qualifiers);
		persistence.synchronizeList(list);
		return list;

	}

	// get contracts templates
	private List<Contract> getContracts(Map params) {
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
		        };
		// we must process non-atomically for author ids are stored in XML
		return BeanFetcher.fetchContractsFromItems(sqlTool.findItemsWithType(Item.CONTRACT, qualifiers), FetchType.PROCESS_NONATOMIC);

	}

	private List<Author> getAuthors(Map params) {
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

		SQLTool sqlTool = SQLTool.getInstance();
		Qualifier[] qualifiers = new Qualifier[] {
		        new CompareCondition(Field.ID, new OperationIn(ids.size()), ids),
		        };

		List<Author> authors = BeanFetcher.fetchAuthorsFromItems(sqlTool.findItemsWithType(Item.AUTHOR, qualifiers), FetchType.LAZY);
		log.info("Authors: " + authors.size());

		return authors;
	}
}
