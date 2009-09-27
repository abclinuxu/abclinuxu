package cz.abclinuxu.servlets.html.admin.view;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Author;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.data.view.Topic;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.PageNavigation;
import cz.abclinuxu.servlets.utils.url.PwdNavigator;
import cz.abclinuxu.utils.BeanFetcher;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.BeanFetcher.FetchType;

/**
 * Handles both editor's and authors portal
 *
 * @author kapy
 */
public class AEPortal implements AbcAction {

    public static final String VAR_AUTHOR = "AUTHOR";
    public static final String VAR_TOPICS = "TOPICS";
    public static final String VAR_EDITOR_MODE = "EDITOR_MODE";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);

        if (ServletUtils.handleMaintainance(request, env)) {
            response.sendRedirect(response.encodeRedirectURL("/"));
            return null;
        }

        // check permissions
        if (user == null)
            return FMTemplateSelector.select("AdministrationAEPortal", "login", env, request);

        // check advanced permissions and create navigation
        // tree according to permissions given
        PwdNavigator navigator = new PwdNavigator(user, PageNavigation.AUTHORS_EDITORS_PORTAL);

        // store navigation structure
        List<Link> parents = navigator.navigate();
        env.put(Constants.VAR_PARENTS, parents);

        // store author
        Author author = findAssignedAuthor(user.getId());
        env.put(VAR_AUTHOR, author);

        switch (navigator.determine()) {
            case AUTHOR:
                if (author == null)
                    throw new MissingArgumentException("K uživateli není přiřazen žádný autor");
                if (!navigator.permissionsFor(author).canModify())
                    return FMTemplateSelector.select("AdministrationAEPortal", "forbidden", env, request);
                break;
            case EDITOR:
            	env.put(VAR_EDITOR_MODE, Boolean.TRUE);
            	break;
        }
        
        listTopics(request, env, author);
        return FMTemplateSelector.select("AdministrationAEPortal", "ae-portal", env, request);
    }
    
    private void listTopics(HttpServletRequest request, Map env, Author author) {
    	if(author==null)
    		return;
    	
    	SQLTool sqlTool = SQLTool.getInstance();
		Qualifier[] qualifiers = new Qualifier[] {
				new CompareCondition(Field.DATA, Operation.LIKE, "%<author>" + author.getId() + "</author>%"),
				new CompareCondition(Field.DATE1, Operation.IS_NOT_NULL, null),
				new CompareCondition(Field.NUMERIC2, Operation.EQUAL, 1),
				Qualifier.SORT_BY_DATE1,
				Qualifier.ORDER_ASCENDING
		};

		List<Topic> topics = BeanFetcher.fetchTopicsFromItems(sqlTool.getTopics(qualifiers), FetchType.EAGER);
		if(!Misc.empty(topics))
			env.put(VAR_TOPICS, topics);
    }

    /**
     * Find author object for given user id, if any
     *
     * @param userId Id of user
     * @return Author object, if given user is author at the same time
     */
    private Author findAssignedAuthor(int userId) {
        SQLTool sqlTool = SQLTool.getInstance();
        return BeanFetcher.fetchAuthorFromItem(sqlTool.findAuthorByUserId(userId), FetchType.LAZY);
    }

}