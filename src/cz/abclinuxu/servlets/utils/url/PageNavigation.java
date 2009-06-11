package cz.abclinuxu.servlets.utils.url;

import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.freemarker.Tools;

import java.util.List;

/**
 * Encapsulates links created for PwdNavigator. Call are able to be chained
 * inside.
 *
 * @author kapy
 * @since 20.05.2009
 */
public enum PageNavigation {

    /**
     * Administration link. It is shown only when appropriate rights are present (root)
     */
    ADMINISTRATION {
        @Override
        public List<Link> getLinks(User user, List<Link> links) {
            if (user.isMemberOf(Constants.GROUP_ADMINI))
                links.add(new Link("Správa", UrlUtils.PREFIX_ADMINISTRATION, "Portál správy abclinuxu.cz"));
            return links;
        }
    },

    /**
     * Author or editor main portal
     */
    AUTHORS_EDITORS_PORTAL {
        @Override
        public List<Link> getLinks(User user, List<Link> links) {
            links = ADMINISTRATION.getLinks(user, links);
            links.add(new Link("Redakce", UrlUtils.PREFIX_ADMINISTRATION + "/redakce", "Redakční systém"));
            return links;
        }
    },

    /**
     * Authors administration link. It is shown only when user can modify Authors
     */
    ADMIN_AUTHORS {
        @Override
        public List<Link> getLinks(User user, List<Link> links) {
            links = AUTHORS_EDITORS_PORTAL.getLinks(user, links);
            if (user.isMemberOf(Constants.GROUP_ADMINI) || Tools.permissionsFor(user, Constants.REL_AUTHORS).canModify())
                links.add(new Link("Správa autorů", Util.prefix(links) + "autori", "Správa autorů"));
            return links;
        }
    };

    public abstract List<Link> getLinks(User user, List<Link> links);
}

class Util {

    /**
     * Extract URL prefix from last link in list
     *
     * @param links List of links
     * @return Prefix from last link
     */
    static String prefix(List<Link> links) {
        if (links.isEmpty())
            return "/";
        return links.get(links.size() - 1).getUrl() + "/";
    }
}
