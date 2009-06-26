package cz.abclinuxu.servlets.html.admin.view;

import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Author;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.PageNavigation;
import cz.abclinuxu.servlets.utils.url.PwdNavigator;
import cz.abclinuxu.utils.BeanFetcher;
import cz.abclinuxu.utils.BeanFetcher.FetchType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Handles both editor's and authors portal
 *
 * @author kapy
 */
public class AEPortal implements AbcAction {

    public static final String VAR_AUTHOR = "AUTHOR";

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

                return FMTemplateSelector.select("AdministrationAEPortal", "author", env, request);
            case EDITOR:
                return FMTemplateSelector.select("AdministrationAEPortal", "editor", env, request);
        }

        return null;
    }

    /**
     * Find author object for given user id, if any
     *
     * @param userId Id of user
     * @return Author object, if given user is author at the same time
     */
    private Author findAssignedAuthor(int userId) {
        SQLTool sqlTool = SQLTool.getInstance();
        return BeanFetcher.fetchAuthorFromItem(sqlTool.findAuthorByUserId(userId), FetchType.OMIT_XML);
    }

}