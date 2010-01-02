package cz.abclinuxu.servlets.utils.url;

import java.util.ArrayList;
import java.util.List;

import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.servlets.Constants;

/**
 * Encapsulates links created for PwdNavigator. Call are able to be chained
 * inside.
 *
 * @author kapy
 * @since 20.05.2009
 */
public enum PageNavigation {

    /**
     * Page navigation which just passes current content in chain.
     * This can be used when PageNavigation should be disabled but
     * it can't be directly removed from invocation.
     * <p/>
     * This type of PageNavigation guarantees that all passed arguments
     * can be {@code null}. Collection returned is mutable.
     */
    VOID {
        @Override
        public List<Link> getLinks(User user, UrlUtils urlUtils, List<Link> links) {
            return new ArrayList<Link>(1);
        }
    },

    /**
     * Administration link. It is shown only when appropriate rights are present (root)
     */
    ADMINISTRATION {
        @Override
        public List<Link> getLinks(User user, UrlUtils urlUtils, List<Link> links) {
            if (user.isMemberOf(Constants.GROUP_ADMINI))
                links.add(new Link("Správa", urlUtils.noPrefix(UrlUtils.PREFIX_ADMINISTRATION), "Administrační rozhraní"));
            return links;
        }
    },

    /**
     * Author or editor main portal
     */
    EDITION_PORTAL {
        @Override
        public List<Link> getLinks(User user, UrlUtils urlUtils, List<Link> links) {
            links = ADMINISTRATION.getLinks(user, urlUtils, links);
            links.add(new Link("Redakce", urlUtils.make("/redakce", UrlUtils.PREFIX_ADMINISTRATION), "Redakční systém"));
            return links;
        }
    },

    /**
     * Authors administration link. It is shown only when user can modify Authors
     */
    EDITION_AUTHORS {
        @Override
        public List<Link> getLinks(User user, UrlUtils urlUtils, List<Link> links) {
            links = EDITION_PORTAL.getLinks(user, urlUtils, links);
            links.add(new Link("Autoři", urlUtils.make("/redakce/autori", UrlUtils.PREFIX_ADMINISTRATION), "Správa autorů"));
            return links;
        }
    },

    /**
     * Topics administration link.
     */
    EDITION_TOPICS {
        @Override
        public List<Link> getLinks(User user, UrlUtils urlUtils, List<Link> links) {
            links = EDITION_PORTAL.getLinks(user, urlUtils, links);
            links.add(new Link("Náměty", urlUtils.make("/redakce/namety", UrlUtils.PREFIX_ADMINISTRATION), "Správa námětů"));
            return links;
        }
    },

    /**
     * Contracts administration link.
     */
    EDITION_CONTRACTS {
        @Override
        public List<Link> getLinks(User user, UrlUtils urlUtils, List<Link> links) {
            links = EDITION_PORTAL.getLinks(user, urlUtils, links);
            links.add(new Link("Smlouvy", urlUtils.make("/redakce/smlouvy", UrlUtils.PREFIX_ADMINISTRATION), "Smlouvy"));
            return links;
        }
    };

    public abstract List<Link> getLinks(User user, UrlUtils urlUtils, List<Link> links);
}
