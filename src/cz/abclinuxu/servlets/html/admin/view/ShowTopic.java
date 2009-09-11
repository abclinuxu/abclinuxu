package cz.abclinuxu.servlets.html.admin.view;

import static cz.abclinuxu.utils.forms.FormFilter.Filter.*;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.data.view.Topic;
import cz.abclinuxu.exceptions.InvalidDataException;
import cz.abclinuxu.exceptions.InvalidInputException;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.Operation;
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
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.BeanFetcher.FetchType;
import cz.abclinuxu.utils.forms.FormFilter;
import cz.abclinuxu.utils.paging.Paging;

public class ShowTopic implements AbcAction {

	/**
	 * list of found topics that matched the conditions
	 */
	public static final String VAR_FOUND = "FOUND";
	
	/**
	 * list of active authors
	 */
	public static final String VAR_AUTHORS = "AUTHORS";
	
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
	public static final String PARAM_TOPIC_ID = "topicId";

	public static final String ACTION_LIST = "list";
	public static final String ACTION_SHOW = "show";

	@Override
	public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {

		Map<?,?> params = (Map<?,?>) env.get(Constants.VAR_PARAMS);
		User user = (User) env.get(Constants.VAR_USER);
		String action = (String) params.get(PARAM_ACTION);

		if (ServletUtils.handleMaintainance(request, env)) {
			response.sendRedirect(response.encodeRedirectURL("/"));
			return null;
		}

		if (user == null)
		    return FMTemplateSelector.select("AdministrationAEPortal", "login", env, request);

		PwdNavigator navigator = new PwdNavigator(user, PageNavigation.ADMIN_TOPICS);
		
		// check permissions
		if(!navigator.permissionsFor(new Relation(Constants.REL_TOPICS)).canCreate()) {
			return FMTemplateSelector.select("AdministrationAEPortal", "forbidden", env, request);
		}
		
		// show author
		if (ACTION_SHOW.equals(action)) {
			Topic topic = getTopic(env);
			return show(request, env, navigator, topic);
		}

		// list authors
		if (ACTION_LIST.equals(action) || action == null || action.length() == 0) {
					return list(request, env, navigator);
		}
		
		throw new MissingArgumentException("Chybí parametr action!");

	}

	private String list(HttpServletRequest request, Map env, PwdNavigator navigator) {

		Map<?,?> params = (Map<?,?>) env.get(Constants.VAR_PARAMS);
		int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
		int count = Misc.getPageSize(50, 50, env, null);

		// store navigation structure
		List<Link> parents = navigator.navigate();
		env.put(Constants.VAR_PARENTS, parents);

		// store authors
		env.put(VAR_AUTHORS, EditTopic.getActiveAuthors(env));
		
		// create filters
		FormFilter filter = new FormFilter(params, TOPICS_BY_TITLE, TOPICS_BY_AUTHOR, TOPICS_BY_ACCEPTED, TOPICS_BY_OPENED, TOPICS_BY_ROYALTY, TOPICS_BY_TERM);

		Paging found = null;
		int total = 0;

		SQLTool sqlTool = SQLTool.getInstance();
		Qualifier[] qualifiers = getQualifiers(filter, from, count);
		List<Item> items = sqlTool.getTopics(qualifiers);
		total = sqlTool.countTopics(QualifierTool.removeOrderQualifiers(qualifiers));
		found = new Paging(BeanFetcher.fetchTopicsFromItems(items, FetchType.EAGER), from, count, total, qualifiers);

		env.put(VAR_FILTER, filter);
		env.put(VAR_FOUND, found);

		// store url links
		env.put(VAR_URL_BEFORE_FROM, "/sprava/redakce/namety/?from=");
		env.put(VAR_URL_AFTER_FROM, filter.encodeAsURL());

		return FMTemplateSelector.select("AdministrationShowTopic", "list", env, request);
	}

	private String show(HttpServletRequest request, Map env, PwdNavigator navigator, Topic topic) {
		Link tail = new Link(topic.getTitle(), "/sprava/redakce/namety/show?topicId=" + topic.getId() + "&amp;action=show", "Zobrazení námětu");
		env.put(VAR_TOPIC, topic);
		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));
		return FMTemplateSelector.select("AdministrationShowTopic", "show", env, request);
	}

	private Topic getTopic(Map<?,?> env) {
		SQLTool sqlTool = SQLTool.getInstance();
		Map<?,?> params = (Map<?,?>) env.get(Constants.VAR_PARAMS);

		// find topic
		Integer topicId = null;
		try {
			topicId = Misc.parsePossiblyWrongInt((String) params.get(PARAM_TOPIC_ID));
		}
		catch (InvalidInputException iie) {
			throw new MissingArgumentException("Chybí parametr topicId!");
		}

		Qualifier[] qualifiers = { new CompareCondition(Field.ID, Operation.EQUAL, topicId) };
		List<Object[]> authorObjects = sqlTool.getAuthorsWithArticlesCount(qualifiers);
		if (authorObjects.isEmpty()) {
			throw new InvalidDataException("Nepodařilo se najít rodičovskou relaci pro námět" + topicId + "!");
		}

		return null; //BeanFetcher.fetchAuthorFromObjects(authorObjects.get(0), FetchType.PROCESS_NONATOMIC);
	}

	private Qualifier[] getQualifiers(FormFilter filter, int from, int count) {
		List<Qualifier> qualifiers = filter.getQualifiers();
		// sort by surname in ascending order
		qualifiers.add(Qualifier.SORT_BY_ISNULL);
		qualifiers.add(Qualifier.ORDER_ASCENDING);
		qualifiers.add(Qualifier.SORT_BY_DATE1);
		qualifiers.add(Qualifier.SORT_BY_STRING1);
		qualifiers.add(Qualifier.ORDER_ASCENDING);
		qualifiers.add(new LimitQualifier(from, count));

		return qualifiers.toArray(Qualifier.ARRAY_TYPE);
	}

}
