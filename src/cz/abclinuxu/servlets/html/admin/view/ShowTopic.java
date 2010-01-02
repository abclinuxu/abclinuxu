package cz.abclinuxu.servlets.html.admin.view;

import static cz.abclinuxu.utils.forms.FormFilter.Filter.TOPICS_BY_STATE;
import static cz.abclinuxu.utils.forms.FormFilter.Filter.TOPICS_BY_AUTHOR;
import static cz.abclinuxu.utils.forms.FormFilter.Filter.TOPICS_BY_PUBLIC;
import static cz.abclinuxu.utils.forms.FormFilter.Filter.TOPICS_BY_ROYALTY;
import static cz.abclinuxu.utils.forms.FormFilter.Filter.TOPICS_BY_DEADLINE;
import static cz.abclinuxu.utils.forms.FormFilter.Filter.TOPICS_BY_TITLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.abclinuxu.data.User;
import cz.abclinuxu.data.EditionRole;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.view.Author;
import cz.abclinuxu.data.view.Topic;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.QualifierTool;
import cz.abclinuxu.persistence.extra.OrderByQualifier;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.admin.edit.EditTopic;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.PageNavigation;
import cz.abclinuxu.servlets.utils.url.PwdNavigator;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.BeanFetcher;
import cz.abclinuxu.utils.DateTool;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.BeanFetcher.FetchType;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.utils.forms.FormFilter;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.paging.Paging;

public class ShowTopic implements AbcAction {

	public static final String VAR_FOUND = "FOUND";
	public static final String VAR_AUTHORS = "AUTHORS";
	public static final String VAR_AUTHOR = "AUTHOR";
	public static final String VAR_TOPIC = "TOPIC";
	public static final String VAR_FILTER = "FILTER";
	public static final String VAR_URL_BEFORE_FROM = "URL_BEFORE_FROM";
	public static final String VAR_URL_AFTER_FROM = "URL_AFTER_FROM";
    public static final String VAR_EDITOR = "EDITOR";

    public static final String PARAM_RELATION = "rid";
    public static final String PARAM_RELATION2 = "trid";
    public static final String PARAM_FROM = "from";
	public static final String PARAM_SINGLE_AUTHOR = "authorId";
	public static final String PARAM_DESTINATION = "dest";
	public static final String PARAM_DESCRIPTION = "description";
	public static final String PARAM_EMAIL = "email";

	public static final String VALUE_DESTINATION_AUTHOR = "author";
	public static final String VALUE_DESTINATION_MAILING_LIST = "conference";
	public static final String VALUE_DESTINATION_DIRECT = "direct";

	public static final String ACTION_LIST = "list";
	public static final String ACTION_SHOW = "show";
	public static final String ACTION_NOTIFY = "notify";
    public static final String ACTION_NOTIFY_STEP2 = "notify2";
    public static final String ACTION_BACK = "back";

    @Override
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

        boolean editor = (role == EditionRole.EDITOR || role == EditionRole.EDITOR_IN_CHIEF);
        if (editor)
            env.put(VAR_EDITOR, Boolean.TRUE);

        PwdNavigator navigator = new PwdNavigator(env, PageNavigation.ADMIN_TOPICS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
        if (relation != null) {
            Tools.sync(relation);
            if (relation.getChild() instanceof Item && ((Item)relation.getChild()).getType() == Item.TOPIC) {
                Topic topic = BeanFetcher.fetchTopic(relation, FetchType.EAGER);
                env.put(VAR_TOPIC, topic);
                return actionShowTopic(request, env, navigator);
            }
        }

        if (role == EditionRole.AUTHOR)
			return actionListTopicForAuthor(request, env, navigator);

		if (! editor)
			return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        boolean back = ServletUtils.determineAction(params, ACTION_BACK);
        if (! back && ServletUtils.determineAction(params, ACTION_NOTIFY))
			return actionNotify(request, env, navigator);
		if (ServletUtils.determineAction(params, ACTION_NOTIFY_STEP2))
			return actionNotifyStep2(request, response, env);
        if (ServletUtils.determineAction(params, ACTION_LIST) || back || Misc.empty(action))
			return actionListTopicsForEditor(request, env, navigator);


		throw new MissingArgumentException("Chybí parametr action!");
	}

	private String actionShowTopic(HttpServletRequest request, Map env, PwdNavigator navigator) {
        env.put(Constants.VAR_PARENTS, navigator.navigate());
        return FMTemplateSelector.select("ShowTopic", "detail", env, request);
    }

	private String actionListTopicForAuthor(HttpServletRequest request, Map env, PwdNavigator navigator) {
        SQLTool sqlTool = SQLTool.getInstance();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
		int count = Misc.getPageSize(50, 50, env, null);

		env.put(Constants.VAR_PARENTS, navigator.navigate());

        Author author = Tools.getAuthor(user.getId());
        env.put(VAR_AUTHOR, author);


        FormFilter filter = createFilter(params);
        // hide topics with published articles by default
        filter.appendFilterQualifier(TOPICS_BY_STATE.getName(), null, "-1");

        List<Qualifier> preQualifiers = new ArrayList<Qualifier>();
        preQualifiers.add(new CompareCondition(new Field(Field.NUMERIC1, "P"), Operation.IS_NULL, null));
        Qualifier[] qualifiers = getQualifiers(preQualifiers, filter, from, count);
		List<Relation> relations = sqlTool.getTopics(qualifiers);
        Tools.syncList(relations);

		int total = sqlTool.countTopics(QualifierTool.removeOrderQualifiers(qualifiers));
		Paging found = new Paging(BeanFetcher.fetchTopics(relations, FetchType.EAGER), from, count, total, qualifiers);

		env.put(VAR_FILTER, filter);
		env.put(VAR_FOUND, found);

		env.put(VAR_URL_BEFORE_FROM, "/sprava/redakce/namety/?from=");
		env.put(VAR_URL_AFTER_FROM, filter.encodeAsURL());
        return FMTemplateSelector.select("ShowTopic", "listForAuthor", env, request);
    }

	private String actionListTopicsForEditor(HttpServletRequest request, Map env, PwdNavigator navigator) {
		Map params = (Map) env.get(Constants.VAR_PARAMS);
		int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
		int count = Misc.getPageSize(50, 50, env, null);

		env.put(Constants.VAR_PARENTS, navigator.navigate());
        env.put(VAR_AUTHORS, EditTopic.getActiveAuthors(env));

        if (!params.containsKey(FormFilter.PARAM_FILTER_TOPICS_BY_STATE)) {
            // hide topics with published articles by default
            params.put(FormFilter.PARAM_FILTER_TOPICS_BY_STATE, "-1");
        }
        FormFilter filter = createFilter(params);

		SQLTool sqlTool = SQLTool.getInstance();
		Qualifier[] qualifiers = getQualifiers(null, filter, from, count);
		List<Relation> relations = sqlTool.getTopics(qualifiers);
        Tools.syncList(relations);

		int total = sqlTool.countTopics(QualifierTool.removeOrderQualifiers(qualifiers));
		Paging found = new Paging(BeanFetcher.fetchTopics(relations, FetchType.EAGER), from, count, total, qualifiers);

		env.put(VAR_FILTER, filter);
		env.put(VAR_FOUND, found);

		env.put(VAR_URL_BEFORE_FROM, "/sprava/redakce/namety/?from=");
		env.put(VAR_URL_AFTER_FROM, filter.encodeAsURL());

        return FMTemplateSelector.select("ShowTopic", "list", env, request);
    }

	private String actionNotify(HttpServletRequest request, Map env, PwdNavigator navigator) {
		Map params = (Map) env.get(Constants.VAR_PARAMS);

		env.put(Constants.VAR_PARENTS, navigator.navigate());
		env.put(VAR_AUTHORS, EditTopic.getActiveAuthors(env));

		Integer authorId = null;
        boolean publicTopic = false;

        DateTool dateTool = new DateTool();
        StringBuilder sb = new StringBuilder("Náměty\n------\n\n");
        List<Topic> topics = getTopics(params);
        for (Topic topic : topics) {
			sb.append("Téma: ").append(topic.getTitle()).append("\n");
			if (topic.getDeadline() != null) {
                String deadline = dateTool.show(topic.getDeadline(), DateTool.CZ_DAY_MONTH_YEAR);
                sb.append("Termín: ").append(deadline).append("\n");
			}

            sb.append("Honorář: ");
			if (topic.hasRoyalty())
				sb.append(topic.getRoyalty().intValue()).append("\n");
			else
				sb.append("běžný").append("\n");

            sb.append(AbcConfig.getAbsoluteUrl()).append(UrlUtils.PREFIX_ADMINISTRATION).append("/redakce/namety/")
                    .append(topic.getRelationId()).append("\n\n");

            if (! publicTopic) {
                if (topic.isPublic())
                    publicTopic = true;
                else {
                    if (authorId == null)
                        authorId = topic.getAuthor().getRelationId();
                    else {
                        if (!authorId.equals(topic.getAuthor().getRelationId()))
                            publicTopic = true;
                    }
                }
            }
		}

		// store computed results
        FormFilter filter = createFilter(params);
        if (! publicTopic && authorId != null) {
			filter.appendFilterQualifier(PARAM_DESTINATION, null, VALUE_DESTINATION_AUTHOR);
			filter.appendFilterQualifier(PARAM_SINGLE_AUTHOR, null, authorId.toString());
		} else {
            // by default, store destination to conference
            filter.appendFilterQualifier(PARAM_DESTINATION, null, VALUE_DESTINATION_MAILING_LIST);
		}

		filter.appendFilterQualifier(PARAM_DESCRIPTION, null, sb.toString());
		env.put(VAR_FILTER, filter);

		return FMTemplateSelector.select("ShowTopic", "notify", env, request);
	}

	private String actionNotifyStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
		Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);

        String destEmail = AbcConfig.getAuthorsEmail();
        String dest = (String) params.get(PARAM_DESTINATION);
        if (VALUE_DESTINATION_AUTHOR.equals(dest)) {
			Integer authorRelationId = Misc.parseInt((String) params.get(PARAM_SINGLE_AUTHOR), -1);
            Relation relation = new Relation(authorRelationId);
            Tools.sync(relation);
			Author author = BeanFetcher.fetchAuthor(relation, FetchType.PROCESS_NONATOMIC);
			destEmail = author.getEmail();
		} else if (VALUE_DESTINATION_DIRECT.equals(dest))
            destEmail = (String) params.get(PARAM_EMAIL);


		String body = (String) params.get(PARAM_DESCRIPTION);
		boolean sent = EmailSender.sendEmail(user.getEmail(), destEmail, "Nové náměty", body);

		if (sent)
			ServletUtils.addMessage("Zpráva byla zaslána na adresu " + destEmail, env, request.getSession());
		else
			ServletUtils.addError("generic", "Nepodařilo se zaslat email na adresu " + destEmail, env, request.getSession());

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.make(UrlUtils.PREFIX_ADMINISTRATION + "/redakce/namety/"));
        return null;

	}

	/**
	 * Constructs qualifiers from input passed in form. Allows initial argument
	 * to be passed by the first argument
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
		qualifiers.add(new OrderByQualifier(Qualifier.SORT_BY_DATE1, "P"));
		qualifiers.add(Qualifier.SORT_BY_TITLE);
		qualifiers.add(Qualifier.ORDER_ASCENDING);
		qualifiers.add(new LimitQualifier(from, count));

		return qualifiers.toArray(new Qualifier[qualifiers.size()]);
	}

	/**
	 * Constructs filter from parameters. Adds additional data for filtering.
	 * 
	 * @param params Map with passed parameters
	 * @return Filter containing both SQL filters and additional filtering
	 *         items.
	 */
	private FormFilter createFilter(Map params) {
		return new FormFilter(params, TOPICS_BY_TITLE, TOPICS_BY_AUTHOR, TOPICS_BY_STATE, TOPICS_BY_PUBLIC,
                TOPICS_BY_ROYALTY, TOPICS_BY_DEADLINE);
	}

	private List<Topic> getTopics(Map params) {
        // param rid cannot be used in view pages, as it is overriden during processing
		List<String> strings = (List<String>) Tools.asList(params.get(PARAM_RELATION2));
		if (Misc.empty(strings))
			return Collections.emptyList();

		List<Relation> relations = new ArrayList<Relation>(strings.size());
		for (String s : strings) {
            int rid = Misc.parseInt(s, -1);
            relations.add(new Relation(rid));
		}
        Tools.syncList(relations);

		return BeanFetcher.fetchTopics(relations, FetchType.EAGER);
	}
}
