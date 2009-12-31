package cz.abclinuxu.servlets.utils.url;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.Misc;

/**
 * Creates links for navigation panel (pwd-box). Checks appropriate rights
 * during creation of links.
 *
 * @author kapy
 * @since 16.3.2009
 */
public class PwdNavigator {
    private PageNavigation pn;
    private User user;
    private UrlUtils urlUtils;

    /**
     * Creates navigation creator for given user.
     *
     * @param user     User to create navigation for
     * @param urlUtils URL utilities to be used to create location relative
     *                 links
     * @param pn       Page to be navigated
     */
    public PwdNavigator(User user, UrlUtils urlUtils, PageNavigation pn) {
        this.pn = pn;
        this.user = user;
        this.urlUtils = urlUtils;
    }

    /**
     * Creates navigation for given environment. User and URL context is
     * fetched from environment
     *
     * @param env Context environment
     * @param pn  Page to be navigated
     */
    public PwdNavigator(Map env, PageNavigation pn) {
        this.pn = pn;
        this.user = (User) env.get(Constants.VAR_USER);
        this.urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
    }

    /**
     * Creates navigation stub for this user and type of navigation
     *
     * @return List with parents of current page
     */
    public List<Link> navigate() {
        return navigate(null);
    }

    /**
     * Creates navigation stub for this user and type of navigation. Allows one
     * link to be added at the end of navigation stub.
     *
     * @param tail Link to be added at the end
     * @return List with parents of current page
     */
    public List<Link> navigate(Link tail) {
        // retrieve link stub
        List<Link> links = new ArrayList<Link>();
        links = pn.getLinks(user, urlUtils, links);
        // append last link if any
        if (tail != null) {
            if (links.isEmpty() || tail.getUrl().startsWith("/"))
                links.add(tail);
            else {
                Link absoluteLink = new Link(tail.getTitle(), Misc.getLastLink(links) + tail.getUrl(), tail.getDescription());
                links.add(absoluteLink);
            }
        }
        return links;
    }

    public UrlUtils getUrlUtils() {
        return urlUtils;
    }
}