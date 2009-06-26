package cz.abclinuxu.servlets.utils.url;

import cz.abclinuxu.data.AccessControllable;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.security.Permissions;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.freemarker.Tools;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates links for navigation panel (pwd-box). Checks appropriate rights
 * during creation of links.
 *
 * @author kapy
 * @since 16.3.2009
 */
public class PwdNavigator {
    private static final Logger log = Logger.getLogger(PwdNavigator.class);

    /**
     * Determines type of user
     *
     * @author kapy
     */
    public static enum Discriminator {
        AUTHOR, EDITOR, }


    private PageNavigation pn;
    private User user;


    /**
     * Creates navigation creator for given user.
     *
     * @param user User to create navigation for
     * @param pn   Page to be navigated
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
            if (links.isEmpty() || tail.getUrl().startsWith("/"))
                links.add(tail);
            else
                links.add(new Link(tail.getTitle(), Util.prefix(links) + tail.getUrl(), tail.getDescription()));
        }
        return links;
    }

    /**
     * Checks permission for object
     *
     * @param user User to be checked against
     * @param gdo  Generic data object
     * @return Permissions to access given object
     */
    public Permissions permissionsFor(AccessControllable object) {
        if (user.hasRole(Roles.ROOT))
            return Permissions.PERMISSIONS_ROOT;

        int permissions = object.getPermissions();

        List<Permissions> perms = new ArrayList<Permissions>(3);

        // check ownership
        if (object.determineOwnership(user.getId()))
            perms.add(Permissions.extractOwner(permissions));

        if (user.isMemberOf(object.getGroup()))
            perms.add(Permissions.extractGroup(permissions));

        perms.add(Permissions.extractOthers(permissions));

        if (log.isDebugEnabled()) {
            for (Permissions p : perms)
                log.debug(p);
            log.debug("combined: " + Permissions.combine(perms));
        }

        return Permissions.combine(perms);
    }

    /**
     * Checks right for child given by parental relation
     *
     * @param user User to be checked against
     * @param rel  Parental relation
     * @return Permissions to access given object
     */
    public Permissions permissionsFor(Relation rel) {
        return Tools.permissionsFor(user, rel);
    }

    /**
     * Determines whether user is author or editor
     *
     * @return Type of user
     */
    public Discriminator determine() {
        if (user.isMemberOf(Constants.GROUP_ADMINI) || Tools.permissionsFor(user, Constants.REL_AUTHORS).canModify())
            return Discriminator.EDITOR;
        return Discriminator.AUTHOR;
    }

}