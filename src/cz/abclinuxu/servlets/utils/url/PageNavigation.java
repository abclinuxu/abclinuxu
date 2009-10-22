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
	 * 
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
                links.add(new Link("Správa", urlUtils.noPrefix(UrlUtils.PREFIX_ADMINISTRATION), "Portál správy abclinuxu.cz"));
            return links;
        }
    },

    /**
     * Author or editor main portal
     */
    AUTHORS_EDITORS_PORTAL {
        @Override
        public List<Link> getLinks(User user, UrlUtils urlUtils, List<Link> links) {
            links = ADMINISTRATION.getLinks(user, urlUtils, links);
            links.add(new Link("Redakce", urlUtils.make("/redakce"), "Redakční systém"));
            return links;
        }
    },

    /**
     * Authors administration link. It is shown only when user can modify Authors
     */
    ADMIN_AUTHORS {
        @Override
        public List<Link> getLinks(User user, UrlUtils urlUtils, List<Link> links) {
            links = AUTHORS_EDITORS_PORTAL.getLinks(user, urlUtils, links);
            links.add(new Link("Správa autorů", Util.prefix(links) + "autori", "Správa autorů"));
            return links;
        }
    },
    
    /**
     * Topics administration link.
     */
    ADMIN_TOPICS {
    	@Override
    	public List<Link> getLinks(User user, UrlUtils urlUtils, List<Link> links) {
    		links = AUTHORS_EDITORS_PORTAL.getLinks(user, urlUtils, links);
    		links.add(new Link("Náměty", Util.prefix(links) + "namety", "Správa námětů"));
    	    return links;
    	}
    },
    
    /**
     * Contracts administration link.
     */
    ADMIN_CONTRACTS {
    	@Override
    	public List<Link> getLinks(User user, UrlUtils urlUtils, List<Link> links) {
    		links = AUTHORS_EDITORS_PORTAL.getLinks(user, urlUtils, links);
    		links.add(new Link("Smlouvy", Util.prefix(links) + "smlouvy", "Smlouvy"));
    		return links;
    	}
    }
    ;

    public abstract List<Link> getLinks(User user, UrlUtils urlUtils, List<Link> links);
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
