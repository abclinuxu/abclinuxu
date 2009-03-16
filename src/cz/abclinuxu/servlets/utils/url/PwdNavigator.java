package cz.abclinuxu.servlets.utils.url;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.security.Permissions;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.freemarker.Tools;

/**
 * Creates links for navigation panel (pwd-box). Checks user rights during
 * creation of links. Verification status for given user is stored inside of
 * this object.
 * 
 * @author kapy
 * @since 16.3.2009
 */
public class PwdNavigator {

	/**
	 * Internally holds possible rights to create navigation structure
	 * 
	 * @author kapy
	 * 
	 */
	private static enum Right {
		NONE, AUTHOR, EDITOR
	}

	/**
	 * Responsible for rights and navigation logic. Fills rights appropriately
	 * and creates navigation stub.
	 * 
	 * @author kapy
	 * 
	 */
	public enum NavigationType {
		/**
		 * Navigation concerning administration part
		 */
		ADMINISTRATION {
			@Override
			EnumSet<Right> fillRights(User user, EnumSet<Right> rights) {
	
				// set editor rights
				Permissions editor = Tools.permissionsFor(user,
						Constants.REL_AUTHORS);
				if (editor.canModify()) {
					rights.add(Right.EDITOR);
				}
				return rights;
			}
			@Override
			List<Link> navigate(List<Link> links, Link tail) {

				StringBuilder url = new StringBuilder(UrlUtils.PREFIX_ADMINISTRATION);				
				links.add(new Link("Správa", url.toString(), "Portál správy abclinuxu.cz"));
				url.append("/redakce");
				links.add(new Link("Redakce", url.toString(), "Redakční systém"));				
				return links;
			}
		};

		abstract EnumSet<Right> fillRights(User user, EnumSet<Right> rights);
		abstract List<Link> navigate(List<Link> links, Link tail);
	}

	private EnumSet<Right> rights;
	private NavigationType nt;

	/**
	 * Creates navigation creator for given user.
	 * 
	 * @param user
	 *            User to create navigation for
	 * @param nt Navigation type of this navigator
	 */
	public PwdNavigator(User user, NavigationType nt) {

		// set navigation type & rights
		this.nt = nt;
		this.rights = setRights(user);
	}

	/**
	 * Checks whether user has any rights for given page
	 * 
	 * @return {@code true} if user has rights, {@code false} otherwise
	 */
	public boolean hasAppropriateRights() {
		return !rights.isEmpty();
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
	 * @param tail
	 *            Link to be added at the end
	 * @return List with parents of current page
	 */
	public List<Link> navigate(Link tail) {

		List<Link> links = new ArrayList<Link>();
		
		links = nt.navigate(links, tail);				
		if (tail != null) {
			// append first link
			if(links.isEmpty())
				links.add(tail);
			else {
				String url = links.get(links.size()-1).getUrl() + "/" + tail.getUrl();
				links.add(new Link(tail.getTitle(), url, tail.getDescription()));
			}
		}
		
		return links;

	}

	/**
	 * Sets rights for given user and navigation type
	 * 
	 * @param user
	 *            User object
	 * @return Set of rights
	 */
	private EnumSet<Right> setRights(User user) {

		EnumSet<Right> set = EnumSet.noneOf(Right.class);

		// check root rights
		if (user.isRoot() || user.hasRole(Roles.ROOT)) {
			return EnumSet.allOf(Right.class);
		}

		// delegate functionality
		return nt.fillRights(user, set);
	}
}
