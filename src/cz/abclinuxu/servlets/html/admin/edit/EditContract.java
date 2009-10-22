package cz.abclinuxu.servlets.html.admin.edit;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Contract;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.data.view.Contract.ContractImage;
import cz.abclinuxu.exceptions.InternalException;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.PageNavigation;
import cz.abclinuxu.servlets.utils.url.PwdNavigator;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.BeanFetcher;
import cz.abclinuxu.utils.BeanFlusher;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.BeanFetcher.FetchType;
import cz.abclinuxu.utils.BeanFlusher.DocumentBuilder;
import cz.abclinuxu.utils.forms.Validator;

public class EditContract implements AbcAction {
	public static final String PARAM_ID = "contractId";
	public static final String PARAM_TITLE = "title";
	public static final String PARAM_PROPOSED_DATE = "proposedDate";
	public static final String PARAM_DESCRIPTION = "description";
	public static final String PARAM_VERSION = "version";
	public static final String PARAM_ROYALTY = "royalty";
	public static final String PARAM_EMPLOYER = "employer";
	public static final String PARAM_EMPLOYER_SIG = "employerSignature";
	public static final String PARAM_CONTENT = "content";

	public static final String VAR_CONTRACT = "CONTRACT";
	public static final String VAR_EDIT_MODE = "EDIT_MODE";

	public static final String ACTION_ADD = "add";
	public static final String ACTION_ADD_STEP2 = "add2";
	public static final String ACTION_EDIT = "edit";
	public static final String ACTION_EDIT_STEP2 = "edit2";
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

		if (action == null || action.length() == 0)
		    throw new MissingArgumentException("Chybí parametr action!");

		// check permissions
		if (user == null)
		    return FMTemplateSelector.select("AdministrationEditorsPortal", "login", env, request);

		// create navigator and store type of user
		PwdNavigator navigator = new PwdNavigator(env, PageNavigation.ADMIN_CONTRACTS);

		// add step 1
		if (ACTION_ADD.equals(action)) {
			if (!navigator.permissionsFor(new Relation(Constants.REL_AUTHORS)).canCreate())
			    return FMTemplateSelector.select("AdministrationEditorsPortal", "forbidden", env, request);

			return actionAddStep1(request, response, env, navigator);
		}

		// add step 2
		if (ACTION_ADD_STEP2.equals(action)) {
			if (!navigator.permissionsFor(new Relation(Constants.REL_AUTHORS)).canCreate())
			    return FMTemplateSelector.select("AdministrationEditorsPortal", "forbidden", env, request);

			ActionProtector.ensureContract(request, EditContract.class, true, true, true, false);
			return actionAddStep2(request, response, env, navigator);
		}

		// determine given topic by id
		Persistence persistence = PersistenceFactory.getPersistence();
		Item item = (Item) InstanceUtils.instantiateParam(PARAM_ID, Item.class, params, request);
		if (item == null)
		    throw new MissingArgumentException("Chybí parametr contractId!");
		persistence.synchronize(item);
		Contract contract = BeanFetcher.fetchContractFromItem(item, FetchType.EAGER);
		env.put(VAR_CONTRACT, contract);

		// edit step 1
		if (ACTION_EDIT.equals(action)) {
			if (!navigator.permissionsFor(new Relation(Constants.REL_AUTHORS)).canModify())
			    return FMTemplateSelector.select("AdministrationEditorsPortal", "forbidden", env, request);

			return actionEditStep1(request, response, env, navigator);
		}

		// edit step 2
		if (ACTION_EDIT_STEP2.equals(action)) {
			if (!navigator.permissionsFor(new Relation(Constants.REL_AUTHORS)).canModify())
			    return FMTemplateSelector.select("AdministrationEditorsPortal", "forbidden", env, request);

			ActionProtector.ensureContract(request, EditContract.class, true, true, true, false);
			return actionEditStep2(request, response, env, navigator);
		}

		throw new MissingArgumentException("Chybí parametr action!");
	}

	// first step of author creation
	private String actionAddStep1(HttpServletRequest request, HttpServletResponse response, Map env, PwdNavigator navigator) throws Exception {

		Link tail = new Link("Nová šablona smlouvy", "edit?action=add", "Vytvořit novou šablonu");
		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));

		return FMTemplateSelector.select("AdministrationEditContract", "add", env, request);
	}

	private String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, PwdNavigator navigator) throws Exception {

		Link tail = new Link("Nová šablona smlouvy", "edit?action=add", "Vytvořit novou šablonu");
		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));

		Persistence persistence = PersistenceFactory.getPersistence();

		// get contract from request
		Contract template = new Contract();
		ContractValidator validator = new ContractValidator(template, env);
		if (!validator.setAndValidate()) {
			// restore contract if not valid
			env.put(VAR_CONTRACT, template);
			return FMTemplateSelector.select("AdministrationEditContract", "add", env, request);
		}
		try {
			// store template in database
			Item item = new Item(0, Item.TEMPLATE);
			item.setSubType(Constants.TYPE_CONTRACT);

			// refresh item content
			item = BeanFlusher.flushContractToItem(item, template);
			item.setTitle(template.getTitle());
			DocumentBuilder db = new DocumentBuilder(item.getData(), "data");
			// store template id
			db.store("/data/template-id", template.getId());
			item.setData(db.getDocument());
			persistence.create(item);

			// retrieve fields changed by persistence
			template = BeanFetcher.fetchContractFromItem(item, FetchType.EAGER);
			env.put(VAR_CONTRACT, template);
			redirect(response, env);
			return null;
		}
		catch (Exception e) {
			Logger log = Logger.getLogger(EditContract.class);
			log.fatal("Unable to add contract template", e);
			return null;
		}
	}

	private String actionEditStep1(HttpServletRequest request, HttpServletResponse response, Map env, PwdNavigator navigator) {
		Contract template = (Contract) env.get(VAR_CONTRACT);

		Link tail = new Link(template.getTitle(), "edit/" + template.getId() + "?action=edit", "Editace smlouvy: " + template.getTitle());

		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));
		env.put(VAR_EDIT_MODE, Boolean.TRUE);

		return FMTemplateSelector.select("AdministrationEditContract", "edit", env, request);
	}

	private String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env, PwdNavigator navigator) throws Exception {

		Persistence persistence = PersistenceFactory.getPersistence();
		Contract template = (Contract) env.get(VAR_CONTRACT);
		ContractValidator validator = new ContractValidator(template, env);

		if (!validator.setAndValidate()) {
			Link tail = new Link(template.getTitle(), "edit/" + template.getId() + "?action=edit", "Editace námětu: " + template.getTitle());
			env.put(Constants.VAR_PARENTS, navigator.navigate(tail));
			env.put(VAR_EDIT_MODE, Boolean.TRUE);
			return FMTemplateSelector.select("AdministrationEditContract", "edit", env, request);
		}

		Item item = (Item) persistence.findById(new Item(template.getId()));

		// refresh item content
		item = BeanFlusher.flushContractToItem(item, template);
		persistence.update(item);

		ServletUtils.addMessage("Smlouva " + template.getTitle() + " byla upravena", env, request.getSession());
		redirect(response, env);
		return null;
	}

	///////////////////////////////////////////////////////////////////////////
	// helpers 
	public static void redirect(HttpServletResponse response, Map env) throws Exception {
		UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
		// redirect to topics in administration system
		urlUtils.redirect(response, urlUtils.make(UrlUtils.PREFIX_ADMINISTRATION + "/redakce/smlouvy/show?action=list"));
	}

	/**
	 * Validates contracts template content.
	 * FIXME Employers signature is hardcoded there as png version 
	 * 
	 * @author kapy
	 * 
	 */
	static class ContractValidator extends Validator<Contract> {

		@SuppressWarnings("serial")
		private final static Map<String, java.lang.reflect.Field> fields = new HashMap<String, java.lang.reflect.Field>() {
			{
				try {
					put(PARAM_TITLE, Contract.class.getDeclaredField("title"));
					put(PARAM_PROPOSED_DATE, Contract.class.getDeclaredField("proposedDate"));
					put(PARAM_DESCRIPTION, Contract.class.getDeclaredField("description"));
					put(PARAM_EMPLOYER, Contract.class.getDeclaredField("employer"));
					put(PARAM_VERSION, Contract.class.getDeclaredField("version"));
					//put(PARAM_EMPLOYER_SIG, Contract.class.getDeclaredField("employerSignature"));
					put(PARAM_CONTENT, Contract.class.getDeclaredField("content"));
				}
				catch (NoSuchFieldException e) {
					throw new InternalException("Invalid configuration of Contracts validator", e);
				}
			}
		};

		public ContractValidator(Contract contract, Map<?, ?> env) {
			super(contract, fields, env, null);
		}

		public boolean setAndValidate() {
			boolean result = true;
			result &= validateNotEmptyAndSet(String.class, PARAM_TITLE, "Zadejte název smlouvy!");
			result &= validateNotEmptyAndSet(String.class, PARAM_DESCRIPTION, "Zadejte popis smlouvy!");
			result &= validateNotEmptyAndSet(String.class, PARAM_VERSION, "Zadejte verzi smlouvy!");
			result &= validateNotEmptyAndSet(String.class, PARAM_CONTENT, "Zadejte obsah (šablonu) smlouvy!");
			result &= validateNotEmptyAndSet(Date.class, PARAM_PROPOSED_DATE, "Zadejte předpokládaný datum platnosti smlouvy!");
			// employer
			result &= validateNotEmptyAndSet(User.class, PARAM_EMPLOYER, "Vyberte přiřazeného jednatele!");

			// FIXME hardcoded
			// set employer signature
			if(validee.getEmployer()!=null)
				validee.setEmployeeSignature(validee.proposeImageUrl(ContractImage.SIGNATURE_EMPLOYER, "png"));
			
			return result;
		}
	}
}
