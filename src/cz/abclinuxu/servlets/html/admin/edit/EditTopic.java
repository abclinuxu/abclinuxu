package cz.abclinuxu.servlets.html.admin.edit;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.EditionRole;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.view.Author;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.data.view.Topic;
import cz.abclinuxu.exceptions.InternalException;
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
import cz.abclinuxu.servlets.html.admin.view.ShowTopic;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.PageNavigation;
import cz.abclinuxu.servlets.utils.url.PwdNavigator;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.BeanFetcher;
import cz.abclinuxu.utils.BeanFlusher;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.BeanFetcher.FetchType;
import cz.abclinuxu.utils.forms.Validator;

public class EditTopic implements AbcAction {
    public static final String PARAM_RELATION = "rid";
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
    public static final String VAR_RELATION = "RELATION";

    public static final String ACTION_ADD = "add";
	public static final String ACTION_ADD_STEP2 = "add2";
	public static final String ACTION_EDIT = "edit";
	public static final String ACTION_EDIT_STEP2 = "edit2";
	public static final String ACTION_REMOVE = "rm";
	public static final String ACTION_REMOVE_STEP2 = "rm2";
    public static final String ACTION_ACCEPT = "accept";


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

        if (ServletUtils.determineAction(params, ACTION_ACCEPT)) {
            ActionProtector.ensureContract(request, ShowTopic.class, true, true, false, true);
            return actionAccept(request, response, env);
        }

        boolean editor = (role == EditionRole.EDITOR || role == EditionRole.EDITOR_IN_CHIEF);
        if (!editor)
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (action == null || action.length() == 0)
		    throw new MissingArgumentException("Chybí parametr action!");

        // create navigator and store type of user
		PwdNavigator navigator = new PwdNavigator(env, PageNavigation.ADMIN_TOPICS);

        // add step 1
		if (ACTION_ADD.equals(action)) {
			return actionAddStep1(request, env, navigator);
		}

		// add step 2
		if (ACTION_ADD_STEP2.equals(action)) {
			ActionProtector.ensureContract(request, EditTopic.class, true, true, true, false);
			return actionAddStep2(request, response, env, navigator);
		}

		Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
		if (relation == null)
            throw new MissingArgumentException("Chybí parametr relationId!");
        Tools.sync(relation);
        env.put(VAR_RELATION, relation);

        Topic topic = BeanFetcher.fetchTopic(relation, FetchType.EAGER);
		env.put(VAR_TOPIC, topic);

		if (ACTION_EDIT.equals(action)) {
			return actionEditStep1(request, env, navigator);
		}

		if (ACTION_EDIT_STEP2.equals(action)) {
			ActionProtector.ensureContract(request, EditTopic.class, true, true, true, false);
			return actionEditStep2(request, response, env, navigator);
		}

		if (ACTION_REMOVE.equals(action)) {
			return actionRemoveStep(request, env, navigator);
		}

		if (ACTION_REMOVE_STEP2.equals(action)) {
            ActionProtector.ensureContract(request, EditTopic.class, true, true, true, false);
            return actionRemoveStep2(request, response, env);
		}

		throw new MissingArgumentException("Chybí parametr action!");
	}

	// first step of topic creation
	private String actionAddStep1(HttpServletRequest request, Map env, PwdNavigator navigator) throws Exception {
		Link tail = new Link("Nový námět", "edit?action=add", "Vytvořit nový námět");
		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));
		env.put(VAR_AUTHORS, getActiveAuthors(env));
		return FMTemplateSelector.select("EditTopic", "add", env, request);
	}

	private String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, PwdNavigator navigator) throws Exception {
		Persistence persistence = PersistenceFactory.getPersistence();

		// get topic from request
		Topic topic = new Topic();
		TopicValidator validator = new TopicValidator(topic, env);
		if (!validator.setAndValidate()) {
            env.put(VAR_TOPIC, topic);
            return actionAddStep1(request, env, navigator);
		}

        Item item = new Item(0, Item.TOPIC);
        item = BeanFlusher.flushTopic(item, topic);
        persistence.create(item);
        Relation relation = new Relation(new Category(Constants.CAT_TOPICS), item, Constants.REL_TOPICS);
        persistence.create(relation);
        relation.getParent().addChildRelation(relation);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.make(UrlUtils.PREFIX_ADMINISTRATION + "/redakce/namety/"));
        return null;
    }

	private String actionEditStep1(HttpServletRequest request, Map env, PwdNavigator navigator) {
		Topic topic = (Topic) env.get(VAR_TOPIC);

		Link tail = new Link(topic.getTitle(), "edit/" + topic.getId() + "?action=edit", "Editace námětu: " + topic.getTitle());

		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));
		env.put(VAR_AUTHORS, getActiveAuthors(env));
		env.put(VAR_EDIT_MODE, Boolean.TRUE);

		return FMTemplateSelector.select("EditTopic", "edit", env, request);
	}

	private String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env, PwdNavigator navigator) throws Exception {
		Persistence persistence = PersistenceFactory.getPersistence();
		Topic topic = (Topic) env.get(VAR_TOPIC);
		TopicValidator validator = new TopicValidator(topic, env);

		if (!validator.setAndValidate()) {
			Link tail = new Link(topic.getTitle(), "edit/" + topic.getId() + "?action=edit", "Editace námětu: " + topic.getTitle());
			env.put(Constants.VAR_PARENTS, navigator.navigate(tail));
			env.put(VAR_AUTHORS, getActiveAuthors(env));
			env.put(VAR_EDIT_MODE, Boolean.TRUE);
			return FMTemplateSelector.select("EditTopic", "edit", env, request);
		}

		Item item = (Item) persistence.findById(new Item(topic.getId()));

		// refresh item content
		item = BeanFlusher.flushTopic(item, topic);
		persistence.update(item);

		ServletUtils.addMessage("Námět '" + topic.getTitle() + "' byl upraven", env, request.getSession());
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.make(UrlUtils.PREFIX_ADMINISTRATION + "/redakce/namety/"));
        return null;
	}

	private String actionRemoveStep(HttpServletRequest request, Map env, PwdNavigator navigator) {
		Topic topic = (Topic) env.get(VAR_TOPIC);
		Link tail = new Link(topic.getTitle(), "edit/" + topic.getId() + "?action=rm", "Smazání námětu, krok 1");
		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));

		return FMTemplateSelector.select("EditTopic", "remove", env, request);
	}

	private String actionRemoveStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
		Persistence persistence = PersistenceFactory.getPersistence();
		Map params = (Map) env.get(Constants.VAR_PARAMS);

		// delete topic
		String delete = (String) params.get(PARAM_DELETE);
		if (!Misc.empty(delete)) {
			Topic topic = (Topic) env.get(VAR_TOPIC);
			persistence.remove(new Item(topic.getId()));
			ServletUtils.addMessage("Námět '" + topic.getTitle() + "' byl smazán!", env, request.getSession());
			persistence.clearCache();
		}

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.make(UrlUtils.PREFIX_ADMINISTRATION + "/redakce/namety/"));
        return null;
	}

    private String actionAccept(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
        Tools.sync(relation);

        Author author = Tools.getAuthor(user.getId());
        Topic topic = BeanFetcher.fetchTopic(relation, FetchType.EAGER);
        if (topic.getAuthor() == null) {
            topic.setAuthor(author);

            Item item = (Item) relation.getChild();
            item = BeanFlusher.flushTopic(item, topic);
            persistence.update(item);

            StringBuilder sb = new StringBuilder("Autor ").append(Tools.getPersonName(author));
            sb.append(" si rezervoval námět '").append(topic.getTitle()).append("'.");
            EmailSender.sendEmail(author.getEmail(), AbcConfig.getEditorsEmail(), "Přijatý námět", sb.toString());

            ServletUtils.addMessage("Námět '" + topic.getTitle() + "' byl přijat", env, request.getSession());
        } else {
            ServletUtils.addMessage("Námět '" + topic.getTitle() + "' je již rezervován.", env, request.getSession());
        }

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.make(UrlUtils.PREFIX_ADMINISTRATION + "/redakce/namety/"));
        return null;

    }

    // ////////////////////////////////////////////////////////////////////////
	// helpers

	/**
	 * Gets list of active authors ordered by their surnames.
	 * Checks whether authors are already stored in conversation,
	 * if not, performs query to persistence layer
	 * 
	 * @param env Conversation variables
	 * @return List of active authors
	 */
	@SuppressWarnings("unchecked")
	public static List<Author> getActiveAuthors(Map env) {
		List<Author> authors = (List<Author>) env.get(VAR_AUTHORS);
		if (Misc.empty(authors)) {
			// store available authors
			SQLTool sqlTool = SQLTool.getInstance();
			Qualifier[] qualifiers = new Qualifier[] {
			        new CompareCondition(new Field(Field.BOOLEAN1, "P"), Operation.EQUAL, 1),
			        Qualifier.SORT_BY_STRING2
			        };
			List<Object[]> objects = sqlTool.getAuthorsWithArticlesCount(qualifiers);
			authors = BeanFetcher.fetchAuthorsFromObjects(objects, FetchType.LAZY);
		}
		return authors;
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

		public TopicValidator(Topic topic, Map env) {
			super(topic, fields, env, null);
		}

		public boolean setAndValidate() {
			boolean result = true;
			result &= validateNotEmptyAndSet(String.class, PARAM_TITLE, "Zadejte název!");
			result &= validateNotEmptyAndSet(String.class, PARAM_DESCRIPTION, "Zadejte popis!");
			// date is not mandatory
			setBeanField(Date.class, PARAM_DEADLINE, (String) params.get(PARAM_DEADLINE), "Zadejte termín!");
			// author
			if (!transform(Boolean.class, PARAM_PUBLIC, (String) params.get(PARAM_PUBLIC))) {
				result &= validateNotEmptyAndSet(Author.class, PARAM_AUTHOR, "Vyberte přiřazeného autora!");
			} else {
				validee.setAuthor(null);
			}
			// royalty
			if (!transform(Boolean.class, PARAM_ROAYLTY_MOD, (String) params.get(PARAM_ROAYLTY_MOD))) {
				result &= validateNotEmptyAndSet(Integer.class, PARAM_ROYALTY, "Zadejte hodnotu honoráře!");
			} else {
				validee.setRoyalty(null);
			}

			return result;
		}
	}
}
