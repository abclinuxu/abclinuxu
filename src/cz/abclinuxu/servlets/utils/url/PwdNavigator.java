package cz.abclinuxu.servlets.utils.url;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Stack;

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
 * 
 */
public class PwdNavigator {

	/**
	 * Internally holds possible rights to create navigation structure
	 * 
	 * @author kapy
	 * 
	 */
	private static enum Right {
		AUTHOR, EDITOR
	}

	/**
	 * Responsible for rights and navigation logic. Fills rights appropriately.
	 * Navigation stub is created in PwdCreator according to navigation type.
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
				if (user.isMemberOf(Constants.GROUP_AUTORI))
					rights.add(Right.AUTHOR);

				// set editor rights
				Permissions editor = Tools.permissionsFor(user,
						Constants.REL_AUTHORS);
				if (editor.canModify()) {
					rights.add(Right.EDITOR);
				}
				return rights;
			}
		};

		abstract EnumSet<Right> fillRights(User user, EnumSet<Right> rights);
	}

	private EnumSet<Right> rights;
	private NavigationType nt;
	private Stack<String> prefixes;
	private List<Link> links;

	/**
	 * Creates navigation creator for given user.
	 * 
	 * @param user
	 *            User to create navigation for
	 * @param nt Navigation type of this navigator
	 */
	public PwdNavigator(User user, NavigationType nt) {

		this.prefixes = new Stack<String>();
		this.prefixes.push("");
		this.links = new ArrayList<Link>();

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

		switch (nt) {
		case ADMINISTRATION:

			nestLink(Right.EDITOR, "Správa", UrlUtils.PREFIX_ADMINISTRATION
					+ "/", "Portál správy abclinuxu.cz");
			if (tail == null)
				flatLink(Right.AUTHOR, "Redakce", "redakce", "Redakční systém");
			else {
				nestLink(Right.AUTHOR, "Redakce", "redakce", "Redakční systém");
				flatLink(Right.AUTHOR, tail.getTitle(), tail.getUrl(), tail
						.getDescription());
				unnest(Right.AUTHOR);
			}
			unnest(Right.EDITOR);

			break;
		default:
			throw new AssertionError("Undefined behaviour in navigation part");
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

	/**
	 * Creates link in navigation structure. This link cannot have ancestors.
	 * 
	 * @param right
	 *            Right obligatory for this link
	 * @param text
	 *            Text of link
	 * @param url
	 *            URL of link, current prefix is added
	 * @param description
	 *            Textual description (alt text)
	 */
	private void flatLink(Right right, String text, String url,
			String description) {
		if (rights.contains(right)) {
			links.add(new Link(text, prefixes.peek() + url, description));
		}
	}

	/**
	 * Creates link in navigation structure. This link will be new prefix for
	 * ancestor link
	 * 
	 * @param right
	 *            Right obligatory for this link
	 * @param text
	 *            Text of link
	 * @param prefx
	 *            URL of link, current prefix is added, this url is stored as
	 *            new prefix
	 * @param description
	 *            Textual description (alt text)
	 */
	private void nestLink(Right right, String text, String prefix,
			String description) {
		if (rights.contains(right)) {
			links.add(new Link(text, prefixes.peek() + prefix, description));
			prefixes.push(prefix);
		}
	}

	/**
	 * Removes current prefix
	 * 
	 * @param right
	 *            Right obligatory for this operation
	 */
	private void unnest(Right right) {
		if (rights.contains(right)) {
			prefixes.pop();
		}
	}
}
