package cz.abclinuxu.servlets.html.admin.view;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Author;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.PwdNavigator;
import cz.abclinuxu.utils.BeanFetcher;
import cz.abclinuxu.utils.BeanFetcher.FetchType;

public class EditorsPortal implements AbcAction {

	public static final String VAR_PARENTS = Constants.VAR_PARENTS;
	public static final String VAR_AUTHOR = "AUTHOR";

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

		// check permissions
		if (user == null)
			return FMTemplateSelector.select("AdministrationEditorsPortal",
					"login", env, request);

		// check advanced permissions and create navigation
		// tree according to permissions given
		PwdNavigator navigator = new PwdNavigator(user, PwdNavigator.NavigationType.ADMINISTRATION);

		if (!navigator.hasAppropriateRights()) {
			return FMTemplateSelector.select("AdministrationEditorsPortal",
					"forbidden", env, request);
		}

		// store navigation structure
		List<Link> parents = navigator.navigate();
		env.put(VAR_PARENTS, parents);

		// store author if any
		Author author = findAssignedAuthor(user.getId());
		env.put(VAR_AUTHOR, author);

		if (action == null || action.length() == 0)
			return FMTemplateSelector.select("AdministrationEditorsPortal",
					"content", env, request);

		throw new MissingArgumentException("Chybí parametr action!");
	}

	/**
	 * Find author object for given user id, if any
	 * 
	 * @param userId
	 *            Id of user
	 * @return Author object, if given user is author at the same time
	 */
	private Author findAssignedAuthor(int userId) {
		SQLTool sqlTool = SQLTool.getInstance();
		return BeanFetcher.fetchAuthorFromItem(sqlTool
				.findAuthorByUserId(userId), FetchType.OMIT_XML);
	}
}
