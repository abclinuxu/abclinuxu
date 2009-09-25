package cz.abclinuxu.servlets.html.admin.edit;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Author;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.data.view.Topic;
import cz.abclinuxu.exceptions.InternalException;
import cz.abclinuxu.exceptions.InvalidDataException;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.Qualifier;
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
import cz.abclinuxu.utils.forms.Validator;

public class EditTopic implements AbcAction {
	public static final String PARAM_ID = "topicId";
	public static final String PARAM_TITLE = "title";
	public static final String PARAM_DEADLINE = "deadline";
	public static final String PARAM_ROAYLTY_MOD = "royalty-mod";
	public static final String PARAM_ROYALTY = "royalty";
	public static final String PARAM_PUBLIC = "public";
	public static final String PARAM_DESCRIPTION = "description";
	public static final String PARAM_AUTHOR = "author";
	public static final String PARAM_DELETE = "delete";

	public static final String VAR_AUTHORS = "AUTHORS";
	public static final String VAR_TOPIC = "TOPIC";
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
		PwdNavigator navigator = new PwdNavigator(user, PageNavigation.ADMIN_TOPICS);

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

			ActionProtector.ensureContract(request, EditTopic.class, true, true, true, false);
			return actionAddStep2(request, response, env, navigator);
		}

		// determine given topic by id
		Persistence persistence = PersistenceFactory.getPersistence();
		Item item = (Item) InstanceUtils.instantiateParam(PARAM_ID, Item.class, params, request);
		if (item == null)
		    throw new MissingArgumentException("Chybí parametr topicId!");
		persistence.synchronize(item);
		Topic topic = BeanFetcher.fetchTopicFromItem(item, FetchType.EAGER);
		env.put(VAR_TOPIC, topic);

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

			ActionProtector.ensureContract(request, EditTopic.class, true, true, true, false);
			return actionEditStep2(request, response, env, navigator);
		}

		// remove step 1
		if (ACTION_REMOVE.equals(action)) {
			if (!navigator.permissionsFor(new Relation(Constants.REL_AUTHORS)).canDelete())
			    return FMTemplateSelector.select("AdministrationEditorsPortal", "forbidden", env, request);

			return actionRemoveStep(request, response, env, navigator);
		}

		// remove step 2
		if (ACTION_REMOVE_STEP2.equals(action)) {
			if (!navigator.permissionsFor(new Relation(Constants.REL_AUTHORS)).canDelete())
			    return FMTemplateSelector.select("AdministrationEditorsPortal", "forbidden", env, request);

			return actionRemoveStep2(request, response, env, navigator);
		}

		throw new MissingArgumentException("Chybí parametr action!");
	}

	// first step of author creation
	private String actionAddStep1(HttpServletRequest request, HttpServletResponse response, Map env, PwdNavigator navigator) throws Exception {

		Link tail = new Link("Nový námět", "edit?action=add", "Vytvořit nový námět");
		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));

		SQLTool sqlTool = SQLTool.getInstance();
		env.put(VAR_AUTHORS, getActiveAuthors(env));

		return FMTemplateSelector.select("AdministrationEditTopic", "add", env, request);
	}

	private String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, PwdNavigator navigator) throws Exception {

		Link tail = new Link("Nový námět", "edit?action=add", "Vytvořit nový námět");
		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));

		Persistence persistence = PersistenceFactory.getPersistence();

		// get topic from request
		Topic topic = new Topic();
		TopicValidator validator = new TopicValidator(topic, env);
		if (!validator.setAndValidate()) {
			// restore authors if not valid
			env.put(VAR_AUTHORS, getActiveAuthors(env));
			env.put(VAR_TOPIC, topic);
			return FMTemplateSelector.select("AdministrationEditTopic", "add", env, request);
		}

		// store in database
		Item item = new Item(0, Item.TOPIC);

		// refresh item content
		item = BeanFlusher.flushTopicToItem(item, topic);
		item.setTitle(topic.getTitle());
		persistence.create(item);

		// set unique url
		String url = proposeTopicsUrl(topic);

		// set relation
		Relation parent = new Relation(Constants.REL_TOPICS);
		persistence.synchronize(parent);
		persistence.synchronize(parent.getChild());
		Relation relation = new Relation(parent.getChild(), item, parent.getId());
		relation.setUrl(url);
		persistence.create(relation);
		relation.getParent().addChildRelation(relation);

		// retrieve fields changed by persistence
		topic = BeanFetcher.fetchTopicFromItem(item, FetchType.EAGER);
		redirect(response, env);
		return null;
	}

	private String actionEditStep1(HttpServletRequest request, HttpServletResponse response, Map env, PwdNavigator navigator) {
		Topic topic = (Topic) env.get(VAR_TOPIC);
		
		Link tail = new Link(topic.getTitle(), "edit/" + topic.getId() + "?action=edit", "Editace námětu: "  + topic.getTitle());

		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));
		env.put(VAR_AUTHORS, getActiveAuthors(env));
		env.put(VAR_EDIT_MODE, Boolean.TRUE);

		return FMTemplateSelector.select("AdministrationEditTopic", "edit", env, request);
	}

	private String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env, PwdNavigator navigator) throws Exception {

		Persistence persistence = PersistenceFactory.getPersistence();
        SQLTool sqlTool = SQLTool.getInstance();
        Topic topic = (Topic) env.get(VAR_TOPIC);
        TopicValidator validator = new TopicValidator(topic, env);
        
        if(!validator.setAndValidate()) {
        	Link tail = new Link(topic.getTitle(), "edit/" + topic.getId() + "?action=edit", "Editace námětu: " + topic.getTitle());
        	env.put(Constants.VAR_PARENTS, navigator.navigate(tail));
        	env.put(VAR_AUTHORS, getActiveAuthors(env));
        	env.put(VAR_EDIT_MODE, Boolean.TRUE);
        	return FMTemplateSelector.select("AdministrationEditTopic", "edit", env, request);
        }
        
        Item item = (Item) persistence.findById(new Item(topic.getId()));
        Relation relation = RelationUtil.findParent(item);

        // refresh item content
        item = BeanFlusher.flushTopicToItem(item, topic);
        persistence.update(item);

        String url = proposeTopicsUrl(topic);
        if (!url.equals(relation.getUrl())) {
            url = URLManager.protectFromDuplicates(url);
            sqlTool.insertOldAddress(relation.getUrl(), url, relation.getId());
            relation.setUrl(url);
            persistence.update(relation);
        }

        ServletUtils.addMessage("Námět " + topic.getTitle() + " byl upraven", env, request.getSession());
        redirect(response, env);
        return null;
	}

	private String actionRemoveStep(HttpServletRequest request, HttpServletResponse response, Map env, PwdNavigator navigator) {
        Topic topic = (Topic) env.get(VAR_TOPIC);
        Link tail = new Link(topic.getTitle(), "edit/" + topic.getId() + "?action=rm", "Smazání námětu, krok 1");
        env.put(Constants.VAR_PARENTS, navigator.navigate(tail));

        return FMTemplateSelector.select("AdministrationEditTopic", "remove", env, request);
	}

	private String actionRemoveStep2(HttpServletRequest request, HttpServletResponse response, Map env, PwdNavigator navigator) throws Exception {
		Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        // delete author
        String delete = (String) params.get(PARAM_DELETE);
        if (!Misc.empty(delete)) {
            Topic topic = (Topic) env.get(VAR_TOPIC);
            persistence.remove(RelationUtil.findParent(new Item(topic.getId())));
            persistence.remove(new Item(topic.getId()));
            ServletUtils.addMessage("Námět " + topic.getTitle() + " byl smazán!", env, request.getSession());
            persistence.clearCache();
        }
		redirect(response, env);
		return null;
	}

	// ////////////////////////////////////////////////////////////////////////
	// helpers

	@SuppressWarnings("unchecked")
	public static List<Author> getActiveAuthors(Map env) {
		SQLTool sqlTool = SQLTool.getInstance();
		List<Author> authors = (List<Author>) env.get(VAR_AUTHORS);
		if (Misc.empty(authors)) {
			// store available authors
			Qualifier[] qualifiers = new Qualifier[] {
			        new CompareCondition(Field.NUMERIC2, Operation.EQUAL, 1)
			        };
			List<Object[]> objects = sqlTool.getAuthorsWithArticlesCount(qualifiers);
			authors = BeanFetcher.fetchAuthorsFromObjects(objects, FetchType.LAZY);
		}
		return authors;
	}

	/**
	 * Creates absolute URL for this topic
	 * 
	 * @param topic Topic
	 * @return Created URL
	 */
	private String proposeTopicsUrl(Topic topic) {
		String url = UrlUtils.PREFIX_ADMINISTRATION + "redakce/namety/" + URLManager.enforceRelativeURL(topic.getTitle());
		return URLManager.protectFromDuplicates(url);
	}

	private void redirect(HttpServletResponse response, Map env) throws Exception {
		UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
		// redirect to topics in administration system
		urlUtils.redirect(response, urlUtils.make(UrlUtils.PREFIX_ADMINISTRATION + "/redakce/namety/show?action=list"));
	}

	/**
	 * Validates topics content
	 * 
	 * @author kapy
	 * 
	 */
	static class TopicValidator extends Validator<Topic> {

		@SuppressWarnings("serial")
		private final static Map<String, java.lang.reflect.Field> fields = new HashMap<String, java.lang.reflect.Field>() {
			{
				try {
					put(PARAM_TITLE, Topic.class.getDeclaredField("title"));
					put(PARAM_DEADLINE, Topic.class.getDeclaredField("deadline"));
					put(PARAM_DESCRIPTION, Topic.class.getDeclaredField("description"));
					put(PARAM_AUTHOR, Topic.class.getDeclaredField("author"));
					put(PARAM_ROYALTY, Topic.class.getDeclaredField("royalty"));
				}
				catch (NoSuchFieldException e) {
					throw new InternalException("Invalid configuration of Topics validator", e);
				}
			}
		};

		public TopicValidator(Topic topic, Map<?, ?> env) {
			super(topic, fields, env, null);
		}

		public boolean setAndValidate() {
			boolean result = true;
			result &= validateNotEmptyAndSet(String.class, PARAM_TITLE, "Zadejte název!");
			result &= validateNotEmptyAndSet(String.class, PARAM_DESCRIPTION, "Zadejte popis!");
			// date is not mandatory
			setBeanField(Date.class, PARAM_DEADLINE, (String) params.get(PARAM_DEADLINE), "Zadejte termnín!");
			// author
			if (!transform(Boolean.class, PARAM_PUBLIC, (String) params.get(PARAM_PUBLIC), "Námět musí být veřejný nebo přiřazen")) {
				result &= validateNotEmptyAndSet(Author.class, PARAM_AUTHOR, "Vyberte přiřazeného autora!");
			}
			// royalty
			if (!transform(Boolean.class, PARAM_ROAYLTY_MOD, (String) params.get(PARAM_ROAYLTY_MOD), "Honorář musí být běžný nebo přiřazen")) {
				result &= validateNotEmptyAndSet(Double.class, PARAM_ROYALTY, "Zadejte hodnotu honoráře!");
			}

			return result;
		}
	}
}
