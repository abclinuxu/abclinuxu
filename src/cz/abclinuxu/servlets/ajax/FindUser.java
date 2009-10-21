package cz.abclinuxu.servlets.ajax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;

public class FindUser implements AbcAction {

	public static final String PARAM_USER = "uid";
	public static final String PARAM_LOGIN = "login";
	public static final String PARAM_NAME = "name";
	public static final String PARAM_EMAIL = "email";
	public static final String PARAM_CITY = "city";
	public static final String PARAM_LIST_MODE = "list";
	public static final String PARAM_FIELD = "field";

	public static final String VAR_USERS = "USERS";
	public static final String VAR_TOO_MANY_USERS = "MANY";
	public static final String VAR_ZERO_USERS = "ZERO";
	public static final String VAR_SELECT_MODE = "SELECT_MODE";
	public static final String VAR_FIELD = "FIELD";

	public static final String ACTION_SELECT = "select";

	public static final String AJAX_TEMPLATE = "/print/ajax/finduser.ftl";

	public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
		Persistence persistence = PersistenceFactory.getPersistence();

		// security check as user field are retrieved raw
        User admin = (User) env.get(Constants.VAR_USER);
        if (admin == null || !Tools.permissionsFor(admin, new Relation(Constants.REL_AUTHORS)).canModify()) {
            return FMTemplateSelector.select("AdministrationEditorsPortal", "login", env, request);
        }
		
		// set passed parameters
		Map params = (Map) env.get(Constants.VAR_PARAMS);
		User searched = new User();
		setId(params, searched, env);
		setName(params, searched, env);
		setLogin(params, searched, env);
		setEmail(params, searched, env);
		setCity(params, searched, env);

		// set searched field
		String field = (String) params.get(PARAM_FIELD);
		env.put(VAR_FIELD, field != null ? field : "");

		String action = (String) params.get(PARAM_ACTION);
		if (ACTION_SELECT.equals(action))
		    env.put(VAR_SELECT_MODE, Boolean.TRUE);

		// add empty list to be sure there is some
		env.put(VAR_USERS, Collections.emptyList());

		// skip execution of query if not set
		if (searched.preciseEquals(new User())) {
			env.put(VAR_TOO_MANY_USERS, Boolean.TRUE);
			return AJAX_TEMPLATE;
		}

		// search for similar users
		// use results only if there is less than maximum results
		List<User> users = persistence.findUsersLike(searched);
		int maxUsers = AbcConfig.getViewUserPageSize();
		if (users.size() == 0)
			env.put(VAR_ZERO_USERS, Boolean.TRUE);
		else if (users.size() > maxUsers)
			env.put(VAR_TOO_MANY_USERS, Boolean.TRUE);
		else {
			List<User> results = new ArrayList<User>(users.size());
			for (User user : users) {
				results.add((User) persistence.findById(user));
			}
			// store results
			env.put(VAR_USERS, results);

		}

		return AJAX_TEMPLATE;
	}

	///////////////////////////////////////////////////////////////////////////
	//                          Setters                                      //
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Sets city search field from parameters.
	 * 
	 * @param params map holding request's parameters
	 * @param user user to be updated
	 * @param env environment
	 */
	private void setCity(Map params, User user, Map env) {
		String city = (String) params.get(PARAM_CITY);
		if (city != null && city.length() > 3)
		    user.setData("%<city>%" + city + "%</city>%");
	}

	/**
	 * Sets name search field from parameters.
	 * 
	 * @param params map holding request's parameters
	 * @param user user to be updated
	 * @param env environment
	 */
	private void setName(Map params, User user, Map env) {
		String name = (String) params.get(PARAM_NAME);
		if (name != null) {
			name = name.trim();
			if (name.length() > 2)
			    user.setName("%" + name + "%");
		}
	}

	/**
	 * Sets login search field from parameters.
	 * 
	 * @param params map holding request's parameters
	 * @param user user to be updated
	 * @param env environment
	 */
	private void setLogin(Map params, User user, Map env) {
		String login = (String) params.get(PARAM_LOGIN);
		if (login != null && login.length() > 2)
		    user.setLogin("%" + login + "%");
	}

	/**
	 * Sets email search field from parameters.
	 * 
	 * @param params map holding request's parameters
	 * @param user user to be updated
	 * @param env environment
	 */
	private void setEmail(Map params, User user, Map env) {
		String email = (String) params.get(PARAM_EMAIL);
		if (email != null && email.length() > 2)
		    user.setEmail("%" + email + "%");
	}

	/**
	 * Sets id search field from parameters.
	 * 
	 * @param params map holding request's parameters
	 * @param user user to be updated
	 * @param env environment
	 */
	private void setId(Map params, User user, Map env) {
		String tmp = (String) params.get(PARAM_USER);
		if (tmp == null)
		    return;
		tmp = tmp.trim();
		if (tmp.length() == 0)
		    return;
		// safely add uid of user
		try {
			int id = Integer.parseInt(tmp);
			user.setId(id);
		}
		catch (NumberFormatException e) {
		}
	}

}
