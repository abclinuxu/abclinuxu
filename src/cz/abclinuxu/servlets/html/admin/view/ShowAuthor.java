package cz.abclinuxu.servlets.html.admin.view;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Author;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.exceptions.InvalidDataException;
import cz.abclinuxu.exceptions.InvalidInputException;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.QualifierTool;
import cz.abclinuxu.security.Permissions;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.PwdNavigator;
import cz.abclinuxu.utils.BeanFetcher;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.BeanFetcher.FetchType;
import cz.abclinuxu.utils.forms.FormFilter;
import cz.abclinuxu.utils.paging.Paging;

public class ShowAuthor implements AbcAction {

	/** list of found relations, that match the conditions*/
	public static final String VAR_FOUND = "FOUND";
	/** distinct author to be shown*/	
	public static final String VAR_AUTHOR = "AUTHOR";
	
	/** form filtering */
	public static final String VAR_FILTER = "FILTER";

	public static final String PARAM_FROM = "from";
	public static final String PARAM_AUTHOR_ID = "aId";
	
	public static final String ACTION_LIST = "list";
	public static final String ACTION_SHOW = "show";
	
	public String process(HttpServletRequest request,
			HttpServletResponse response, Map env) throws Exception {

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

		// check permissions and create navigation
		// tree according to permissions given
		PwdNavigator navigator = new PwdNavigator(user,
				PwdNavigator.NavigationType.ADMIN_AUTHORS);

		if (!navigator.hasPermissions(Permissions.PERMISSION_MODIFY)) {
			return FMTemplateSelector.select("AdministrationEditorsPortal",
					"forbidden", env, request);
		}

		if (ACTION_SHOW.equals(action))
			return show(request, env, navigator);
		
		if (ACTION_LIST.equals(action) || action == null || action.length() == 0) 
			return list(request, env, navigator);

		throw new MissingArgumentException("Chybí parametr action!");
				
		
	}
	
	private String list(HttpServletRequest request, Map env, PwdNavigator navigator) {

		Map params = (Map) env.get(Constants.VAR_PARAMS);
		int from = Misc.parseInt((String) params.get(PARAM_FROM),0);
		int count = Misc.getPageSize(50, 50, env, null);

		// store navigation structure
		List<Link> parents = navigator.navigate();
		env.put(Constants.VAR_PARENTS, parents);
		
		// create filters
		FormFilter filter = new FormFilter(params, 
				FormFilter.AUTHORS_BY_NAME, FormFilter.AUTHORS_BY_SURNAME,
				FormFilter.AUTHORS_BY_CONTRACT, FormFilter.AUTHORS_BY_ACTIVE,
				FormFilter.AUTHORS_BY_ARTICLES, FormFilter.AUTHORS_BY_RECENT);
		
		Paging found = null;
		int total = 0;
		// select according query
		// FIXME add contract logic
		//if(filter.containsParam(FormFilter.AUTHORS_BY_CONTRACT)) {
			
		//}
		// no additional queries required
		//else {
			SQLTool sqlTool = SQLTool.getInstance();
			Qualifier[] qualifiers = getQualifiers(filter, from, count);
			List<Object[]> items = sqlTool.getAuthorsWithArticlesCount(qualifiers);
			total = sqlTool.countAuthorWithArticlesCount(QualifierTool.removeOrderQualifiers(qualifiers));
			found = new Paging(BeanFetcher.fetchAuthorsFromObjects(items, FetchType.EAGER), from, count, total, qualifiers);
		//}			
		env.put(VAR_FILTER, filter);
		env.put(VAR_FOUND, found);
		
		return FMTemplateSelector.select("AdministrationShowAuthor", "list", env, request);
	}
	
	private String show(HttpServletRequest request, Map env, PwdNavigator navigator) {
		Persistence persistence = PersistenceFactory.getPersistence();
		SQLTool sqlTool = SQLTool.getInstance();
		Map params = (Map) env.get(Constants.VAR_PARAMS);
		
		// find author
		Integer aId = null;
		try {
			aId = Misc.parsePossiblyWrongInt((String)params.get(PARAM_AUTHOR_ID));
		}
		catch (InvalidInputException iie) {
			throw new MissingArgumentException("Chybí parametr aId!");
		}		

		Qualifier[] qualifiers = {new CompareCondition(Field.ID,Operation.EQUAL, aId)};
		List<Object[]> authorObjects = sqlTool.getAuthorsWithArticlesCount(qualifiers);
		
		if(authorObjects.isEmpty()) {
			throw new InvalidDataException("Nepodařilo se najít rodičovskou relaci pro autora" + aId +"!");
		}
		
		Author author = BeanFetcher.fetchAuthorFromObjects(authorObjects.get(0), FetchType.EAGER);
		 
		Link tail = new Link(author.getTitle(), "show?aId="+ author.getId()+"&amp;action=show", "Zobrazení autora");
		env.put(Constants.VAR_PARENTS, navigator.navigate(tail));		
		env.put(VAR_AUTHOR, author);
		return FMTemplateSelector.select("AdministrationShowAuthor", "show", env, request);
	}
	
	private Qualifier[] getQualifiers(FormFilter filter, int from, int count) {
		List<Qualifier> qualifiers = filter.getQualifiers();
		// sort by surname in ascending order
		qualifiers.add(Qualifier.SORT_BY_STRING2);
		qualifiers.add(Qualifier.ORDER_ASCENDING);
		qualifiers.add(new LimitQualifier(from, count));
		
		return qualifiers.toArray(Qualifier.ARRAY_TYPE);
	}
}
