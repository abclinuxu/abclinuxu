package cz.abclinuxu.servlets.html.admin.view;

import static cz.abclinuxu.utils.forms.FormFilter.Filter.TOPICS_BY_ACCEPTED;
import static cz.abclinuxu.utils.forms.FormFilter.Filter.TOPICS_BY_AUTHOR;
import static cz.abclinuxu.utils.forms.FormFilter.Filter.TOPICS_BY_OPENED;
import static cz.abclinuxu.utils.forms.FormFilter.Filter.TOPICS_BY_ROYALTY;
import static cz.abclinuxu.utils.forms.FormFilter.Filter.TOPICS_BY_TERM;
import static cz.abclinuxu.utils.forms.FormFilter.Filter.TOPICS_BY_TITLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Author;
import cz.abclinuxu.data.view.Topic;
import cz.abclinuxu.exceptions.InvalidDataException;
import cz.abclinuxu.exceptions.InvalidInputException;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.LogicalOperation;
import cz.abclinuxu.persistence.extra.NestedCondition;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.OperationIn;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.QualifierTool;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.admin.edit.EditTopic;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.PageNavigation;
import cz.abclinuxu.servlets.utils.url.PwdNavigator;
import cz.abclinuxu.utils.BeanFetcher;
import cz.abclinuxu.utils.BeanFlusher;
import cz.abclinuxu.utils.DateTool;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.BeanFetcher.FetchType;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.utils.forms.FormFilter;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.paging.Paging;

public class ShowTopic implements AbcAction, Configurable {
	private static final Logger log = Logger.getLogger(ShowTopic.class);

	// force configuration of topics mailing list
	static {
		Configurator configurator = ConfigurationManager.getConfigurator();
		configurator.configureAndRememberMe(new ShowTopic());
	}

	public static final String DEFAULT_AUTHORS_CONFERENCE = "namety-list@abclinuxu.cz";
	public static final String DEFAULT_SENDER = "robot@abclinuxu.cz";
	public static final String PREF_CONFERENCE = "authors.conference";
	public static final String PREF_SENDER = "sender";
	public static String authorsConference;
	public static String sender;

	/**
	 * list of found topics that matched the conditions
	 */
	public static final String VAR_FOUND = "FOUND";

	/**
	 * list of active authors
	 */
	public static final String VAR_AUTHORS = "AUTHORS";

	/**
	 * Active author
	 */
	public static final String VAR_AUTHOR = "AUTHOR";

	/**
	 * distinct topic to be shown
	 */
	public static final String VAR_TOPIC = "TOPIC";

	/**
	 * form filtering
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

	public static final String PARAM_FROM = "from";

	public static final String PARAM_SINGLE_AUTHOR = "authorId";
	public static final String PARAM_DESTINATION = "dest";
	public static final String PARAM_TOPIC_ID = "topicId";
	public static final String PARAM_DESCRIPTION = "description";
	public static final String PARAM_EMAIL = "email";

	public static final String DEST_AUTHOR = "author";
	public static final String DEST_CONFERENCE = "conference";
	public static final String DEST_DIRECT = "direct";

	public static final String ACTION_LIST = "list";
	public static final String ACTION_SHOW = "show";
	public static final String ACTION_MAIL = "mail";
	public static final String ACTION_PREPARE = "notify";
	public static final String ACTION_BACK = "back";
	public static final String ACTION_ACCEPT = "accept";

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

		PwdNavigator navigator = new PwdNavigator(env, PageNavigation.ADMIN_TOPICS);

		// check invocation by author
		if (ServletUtils.pathBeginsWith(request, "/redakce")) {
			// TODO is there any way how to check invocation with finer granularity within current 
			// permissions system?
			SQLTool sqlTool = SQLTool.getInstance();
			Item item = sqlTool.findAuthorByUserId(user.getId());
			if (item == null)
			    return FMTemplateSelector.select("AdministrationAEPortal", "forbidden", env, request);

			// author was found, limit results to current author
			Author author = BeanFetcher.fetchAuthorFromItem(item, FetchType.LAZY);
			env.put(VAR_AUTHOR, author);

			if (ServletUtils.determineAction(params, ACTION_ACCEPT)) {
				return actionAccept(request, response, env);
			}

			List<Qualifier> preQualifiers = new ArrayList<Qualifier>();
			preQualifiers.add(new NestedCondition(new Qualifier[] {
			        new CompareCondition(Field.DATA, Operation.LIKE, "%<author>" + author.getId() + "</author>%"),
			        new CompareCondition(Field.DATA, Operation.NOT_LIKE, "%<author>%</author>%")
			        }, LogicalOperation.OR));
			preQualifiers.add(new CompareCondition(Field.NUMERIC2, Operation.EQUAL, 0));
			actionList(request, env, navigator, preQualifiers);
			return FMTemplateSelector.select("AdministrationShowTopic", "author-list", env, request);
		}

		// check permissions
		if (!navigator.permissionsFor(new Relation(Constants.REL_TOPICS)).canCreate()) {
			return FMTemplateSelector.select("AdministrationAEPortal", "forbidden", env, request);
		}

		// prepare notify page
		if (ServletUtils.determineAction(params, ACTION_PREPARE)) {
			return actionNotify(request, env, navigator);
		}
		// mail topics
		else if (ServletUtils.determineAction(params, ACTION_MAIL)) {
			return actionMail(request, response, env);
		}
		// list topics
		else if (ServletUtils.determineAction(params, ACTION_LIST) || ServletUtils.determineAction(params, ACTION_BACK) || Misc.empty(action)) {
			actionList(request, env, navigator, null);
			return FMTemplateSelector.select("AdministrationShowTopic", "list", env, request);
		}

		throw new MissingArgumentException("Chybí parametr action!");

	}

	private void actionList(HttpServletRequest request, Map env, PwdNavigator navigator, List<Qualifier> preQualifiers) {

		Map params = (Map) env.get(Constants.VAR_PARAMS);
		int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
		int count = Misc.getPageSize(50, 50, env, null);

		// store navigation structure
		env.put(Constants.VAR_PARENTS, navigator.navigate());

		// store authors
		env.put(VAR_AUTHORS, EditTopic.getActiveAuthors(env));

		// create filters
		FormFilter filter = createFilter(params);

		Paging found = null;
		int total = 0;

		SQLTool sqlTool = SQLTool.getInstance();
		Qualifier[] qualifiers = getQualifiers(preQualifiers, filter, from, count);
		List<Item> items = sqlTool.getTopics(qualifiers);
		total = sqlTool.countTopics(QualifierTool.removeOrderQualifiers(qualifiers));
		found = new Paging(BeanFetcher.fetchTopicsFromItems(items, FetchType.EAGER), from, count, total, qualifiers);

		env.put(VAR_FILTER, filter);
		env.put(VAR_FOUND, found);

		// store url links
		env.put(VAR_URL_BEFORE_FROM, "/sprava/redakce/namety/?from=");
		env.put(VAR_URL_AFTER_FROM, filter.encodeAsURL());
	}

	private String actionNotify(HttpServletRequest request, Map env, PwdNavigator navigator) {
		Map params = (Map) env.get(Constants.VAR_PARAMS);

		// there is no tail link, authors are necessary
		env.put(Constants.VAR_PARENTS, navigator.navigate());
		env.put(VAR_AUTHORS, EditTopic.getActiveAuthors(env));

		// get filter because it must be stored
		FormFilter filter = createFilter(params);

		// determine if topics share same author
		Integer authorId = null;
		boolean authorAssigned = false;

		List<Topic> topics = getTopics(params);
		StringBuilder sb = new StringBuilder("Náměty\n\n");

		for (Topic topic : topics) {
			sb.append(topic.getTitle()).append("\n");
			// append topics deadline if any
			if (topic.getDeadline() != null) {
				DateTool dateTool = new DateTool();
				sb.append("Termín: ").append(dateTool.show(topic.getDeadline(), DateTool.CZ_DAY_MONTH_YEAR)).append("\n");
			}
			// append topics royalty
			sb.append("Honorář: ");
			if (topic.hasRoyalty())
				sb.append(topic.getRoyalty().intValue()).append("\n");
			else
				sb.append("běžný").append("\n");
			// append topics description
			if (topic.getDescription() != null)
			    sb.append("Popis: ").append(topic.getDescription()).append("\n\n");

			// assign first author available 
			if (authorAssigned == false && !topic.isPublic()) {
				authorId = topic.getAuthor().getId();
				authorAssigned = true;
			}
			// check if assigned to author or public
			else if (authorId != null && authorAssigned == true && !topic.isPublic()) {
				if (!authorId.equals(topic.getAuthor().getId())) {
					authorId = null;
				}
			}
		}

		// store computed results
		if (authorId != null) {
			filter.appendFilterQualifier(PARAM_DESTINATION, null, DEST_AUTHOR);
			filter.appendFilterQualifier(PARAM_SINGLE_AUTHOR, null, authorId.toString());
		}
		// by default, store destination to conference
		else {
			filter.appendFilterQualifier(PARAM_DESTINATION, null, DEST_CONFERENCE);
		}

		// add description
		filter.appendFilterQualifier(PARAM_DESCRIPTION, null, sb.toString());
		// filter add selected topic ids
		@SuppressWarnings("unchecked")
		List<String> strings = (List<String>) Tools.asList(params.get(PARAM_TOPIC_ID));
		filter.appendFilterQualifier(PARAM_TOPIC_ID, null, strings);

		env.put(VAR_FILTER, filter);

		return FMTemplateSelector.select("AdministrationShowTopic", "notify", env, request);
	}

	private String actionMail(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
		Map params = (Map) env.get(Constants.VAR_PARAMS);

		// determine and set mail destination
		String dest = (String) params.get(PARAM_DESTINATION);
		String destEmail = authorsConference;
		if (DEST_AUTHOR.equals(dest)) {
			SQLTool sqlTool = SQLTool.getInstance();
			// find author
			Integer aId = null;
			try {
				aId = Misc.parsePossiblyWrongInt((String) params.get(PARAM_SINGLE_AUTHOR));
			}
			catch (InvalidInputException iie) {
				throw new MissingArgumentException("Chybí parametr authorId!");
			}

			Qualifier[] qualifiers = { new CompareCondition(Field.ID, Operation.EQUAL, aId) };
			List<Object[]> authorObjects = sqlTool.getAuthorsWithArticlesCount(qualifiers);
			if (authorObjects.isEmpty()) {
				throw new InvalidDataException("Nepodařilo se najít rodičovskou relaci pro autora" + aId + "!");
			}

			Author author = BeanFetcher.fetchAuthorFromObjects(authorObjects.get(0), FetchType.PROCESS_NONATOMIC);
			destEmail = author.getEmail();
		}
		else if (DEST_DIRECT.equals(dest)) {
			destEmail = (String) params.get(PARAM_EMAIL);
		}

		String body = (String) params.get(PARAM_DESCRIPTION);

		// send message
		boolean sent = EmailSender.sendEmail(sender, destEmail, "Náměty pro články na abclinuxu.cz", body);

		if (sent)
			ServletUtils.addMessage("Zpráva byla zaslaná na " + destEmail, env, request.getSession());
		else
			ServletUtils.addError("generic", "Nepodařilo se zaslat email na " + destEmail, env, request.getSession());

		EditTopic.redirect(response, env);
		return null;

	}

	private String actionAccept(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
		Map params = (Map) env.get(Constants.VAR_PARAMS);

		// determine given topic by id
		Persistence persistence = PersistenceFactory.getPersistence();
		Item item = (Item) InstanceUtils.instantiateParam(PARAM_TOPIC_ID, Item.class, params, request);
		if (item == null)
		    throw new MissingArgumentException("Chybí parametr topicId!");
		persistence.synchronize(item);

		Author author = (Author) env.get(VAR_AUTHOR);
		Topic topic = BeanFetcher.fetchTopicFromItem(item, FetchType.EAGER);
		topic.setAccepted(true);
		topic.setAuthor(author);

		item = BeanFlusher.flushTopicToItem(item, topic);
		persistence.update(item);

		ServletUtils.addMessage("Námět " + topic.getTitle() + " byl přijat", env, request.getSession());
		EditTopic.redirect(response, env);
		return null;

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
		qualifiers.add(Qualifier.SORT_BY_ISNULL);
		qualifiers.add(Qualifier.ORDER_ASCENDING);
		qualifiers.add(Qualifier.SORT_BY_DATE1);
		qualifiers.add(Qualifier.SORT_BY_STRING1);
		qualifiers.add(Qualifier.ORDER_ASCENDING);
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
		FormFilter filter = new FormFilter(params, TOPICS_BY_TITLE, TOPICS_BY_AUTHOR, TOPICS_BY_ACCEPTED, TOPICS_BY_OPENED, TOPICS_BY_ROYALTY, TOPICS_BY_TERM);
		return filter.appendFilterQualifier(PARAM_TOPIC_ID, null, Tools.asList(params.get(PARAM_TOPIC_ID)));
	}

	private List<Topic> getTopics(Map params) {
		@SuppressWarnings("unchecked")
		List<String> strings = (List<String>) Tools.asList(params.get(PARAM_TOPIC_ID));

		if (Misc.empty(strings)) {
			return Collections.emptyList();
		}

		List<Integer> ids = new ArrayList<Integer>(strings.size());
		for (String topicId : strings) {
			try {
				ids.add(Integer.parseInt(topicId));
			}
			catch (NumberFormatException e) {
				log.warn("Passed invalid topic identification: " + topicId);
			}
		}

		if (Misc.empty(ids)) {
			return Collections.emptyList();
		}

		SQLTool sqlTool = SQLTool.getInstance();

		Qualifier[] qualifiers = new Qualifier[] {
		        new CompareCondition(Field.ID, new OperationIn(ids.size()), ids)
		        };
		// we must process non-atomically for author ids are stored in XML
		return BeanFetcher.fetchTopicsFromItems(sqlTool.getTopics(qualifiers), FetchType.PROCESS_NONATOMIC);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(Preferences prefs) throws ConfigurationException {
		authorsConference = prefs.get(PREF_CONFERENCE, DEFAULT_AUTHORS_CONFERENCE);
		sender = prefs.get(PREF_SENDER, DEFAULT_SENDER);
	}

}
