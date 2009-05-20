package cz.abclinuxu.servlets.utils.url;

import java.util.ArrayList;
import java.util.List;

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.security.Permissions;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.utils.freemarker.Tools;

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

    /**
     * Creates navigation creator for given user.
     * 
     * @param user User to create navigation for
     * @param pn Page to be navigated
     */
    public PwdNavigator(User user, PageNavigation pn) {

	// set page navigation
	this.pn = pn;
	this.user = user;
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
	links = pn.getLinks(user, links);
	// append last link if any
	if (tail != null) {
	    if (links.isEmpty())
		links.add(tail);
	    else
		links.add(new Link(tail.getTitle(), Util.prefix(links) + tail.getUrl(), tail.getDescription()));
	}
	return links;
    }

    /**
     * Checks permission for generic data object
     * 
     * @param user User to be checked against
     * @param gdo Generic data object
     * @return Right
     */
    public Permissions directPerm(GenericDataObject gdo) {
	if (user.hasRole(Roles.ROOT))
	    return Permissions.PERMISSIONS_ROOT;

	Tools.sync(gdo);

	int permissions, shift;

	if (user.isMemberOf(gdo.getGroup()))
	    shift = Permissions.PERMISSIONS_GROUP_SHIFT;
	else
	    shift = Permissions.PERMISSIONS_OTHERS_SHIFT;

	permissions = gdo.getPermissions();

	if (gdo instanceof Category)
	    permissions &= ~Permissions.PERMISSIONS_CATEGORY_MASK;

	return new Permissions((permissions >> shift) & 0xff);

    }

    /**
     * Checks right for child give by parental relation
     * 
     * @param user User to be checked against
     * @param rel Parental relation
     * @return Rights for given object
     */
    public Permissions indirectPerm(Relation rel) {
	return Tools.permissionsFor(user, rel);
    }
}