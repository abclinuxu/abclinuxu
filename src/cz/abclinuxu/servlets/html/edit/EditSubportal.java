/*
 *  Copyright (C) 2005 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */

package cz.abclinuxu.servlets.html.edit;

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.versioning.Versioning;
import cz.abclinuxu.persistence.versioning.VersioningFactory;
import cz.abclinuxu.scheduler.VariableFetcher;
import cz.abclinuxu.security.ActionCheck;
import cz.abclinuxu.security.Permissions;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.servlets.AbcAutoAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.parser.clean.HtmlPurifier;
import cz.abclinuxu.utils.parser.clean.HtmlChecker;
import cz.abclinuxu.utils.parser.clean.Rules;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.fileupload.FileItem;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 *
 * @author lubos
 */
public class EditSubportal extends AbcAutoAction {
	public static final String PARAM_ADMIN = "admin";
	public static final String PARAM_TITLE = "title";
	public static final String PARAM_DESCRIPTION = "desc";
    public static final String PARAM_DESCRIPTION_SHORT = "descShort";
	public static final String PARAM_URL = "url";
	public static final String PARAM_ICON = "icon";
	public static final String PARAM_REMOVE_ICON = "removeIcon";
    public static final String PARAM_HIDE_FORUM = "hideForum";

	private Persistence persistence = PersistenceFactory.getPersistence();
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditSubportal.class);

    @ActionCheck(requireCreateRight = true)
    public String actionAdd() throws Exception {
        return FMTemplateSelector.select("EditSubportal", "add", env, request);
    }

    @ActionCheck(requireCreateRight = true, checkReferer = true, checkPost = true)
	public String actionAddStep2() throws Exception {
		Category category = new Category();
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        category.setData(document);
		category.setType(Category.SUBPORTAL);

		Relation newRelation = new Relation(new Category(Constants.CAT_SUBPORTALS), category, Constants.REL_SUBPORTALS);

        boolean canContinue = setTitle(params, category, env);
        canContinue &= setShortDescription(params, root, env);
		canContinue &= setDescription(params, root, env);
		canContinue &= setUrl(params, newRelation, env);
        canContinue &= checkImage(params, env);

        if (user.hasRole(Roles.ROOT))
            canContinue &= setForumHidden(params, root);

		if (!canContinue)
            return FMTemplateSelector.select("EditSubportal", "add", env, request);

		// create a forum, wiki, article section(s)
		int gid = createGroup(params, root, category);
		addUserToGroup(params, gid, category);

		persistence.create(category);
		persistence.create(newRelation);
		newRelation.getParent().addChildRelation(newRelation);

		setIcon(params, newRelation, root, env);

        Relation forum;

        // a wiki page
		createContent(root, newRelation);
        // a section for articles
		createSection(root, newRelation, "Články", "articles", "/clanky", Category.SECTION);
        // waiting articles
		createSection(root, newRelation, "Čekající články", "article_pool", null, 0);
        // a discussion forum
		forum = createSection(root, newRelation, "Poradna", "forum", "/poradna", Category.FORUM);

        FeedGenerator.findSubportalForums();
        FeedGenerator.updateForum(forum.getId());

        Relation events;
        Category catEvents;

        // a section for events
        events = createSection(root, newRelation, "Akce", "events", "/akce", Category.EVENT); // a section for events

        catEvents = (Category) events.getChild();
        // give everybody the right to create events in the pool
        int perms = catEvents.getPermissions();
        perms |= Permissions.PERMISSION_CREATE << Permissions.PERMISSIONS_OTHERS_SHIFT;
        catEvents.setPermissions(perms);

        persistence.update(catEvents);
		persistence.update(category);

        VariableFetcher.getInstance().refreshSubportalSizes(newRelation);
        VariableFetcher.getInstance().refreshLatestSubportalChanges();

		UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, newRelation.getUrl());

		return null;
	}

    public String actionListRSS() throws Exception {
        return null;
    }

    @ActionCheck(relationRequired = true, requireModifyRight = true)
	public String actionEdit() throws Exception {
		Category cat = (Category) relation.getChild();

		params.put(PARAM_TITLE, cat.getTitle());

		Element root = cat.getData().getRootElement();

        Node node = root.element("description");
        if (node != null)
            params.put(PARAM_DESCRIPTION, node.getText());
        node = root.element("descriptionShort");
        if (node != null)
            params.put(PARAM_DESCRIPTION_SHORT, node.getText());
        node = root.element("forumHidden");
        if (node != null)
            params.put(PARAM_HIDE_FORUM, node.getText());

		return FMTemplateSelector.select("EditSubportal", "edit", env, request);
	}

    @ActionCheck(relationRequired = true, requireModifyRight = true, checkPost = true, checkReferer = true)
	public String actionEdit2() throws Exception {
		Category category = (Category) relation.getChild().clone();

        Element root = category.getData().getRootElement();

		boolean canContinue = setTitle(params, category, env);
        canContinue &= setDescription(params, root, env);
		canContinue &= setShortDescription(params, root, env);
        canContinue &= checkImage(params, env);
		canContinue &= setIcon(params, relation, root, env);

        if (user.hasRole(Roles.ROOT))
            canContinue &= setForumHidden(params, root);

		if (!canContinue)
            return FMTemplateSelector.select("EditSubportal", "edit", env, request);

		persistence.update(category);

		UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, relation.getUrl());

		return null;
	}

    @ActionCheck(relationRequired = true, checkTicket = true)
	public String actionToggleMember() throws Exception {
        Category cat = (Category) relation.getChild();
        Set<String> users = cat.getProperty(Constants.PROPERTY_MEMBER);

        // see whether user wants to remove or add himself
        String userid = Integer.toString(user.getId());
        if (! users.contains(userid))
            cat.addProperty(Constants.PROPERTY_MEMBER, userid);
        else
            cat.removePropertyValue(Constants.PROPERTY_MEMBER, userid);

        persistence.update(cat);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation));

        return null;
	}

	private int createGroup(Map params, Element root, Category cat) {
		Item group = new Item(0, Item.GROUP);
		String name = (String) params.get(PARAM_TITLE);

        group.setData(DocumentHelper.createDocument());

		group.setTitle(name);
		Node node = DocumentHelper.makeElement(group.getData(), "/data/desc");

		node.setText("Subportal "+name);

        group.setCreated(new Date());

        persistence.create(group);
        group.setGroup(group.getId());
        persistence.update(group);

		DocumentHelper.makeElement(root, "gid").setText(String.valueOf(group.getId()));
		cat.setGroup(group.getId());
		cat.setPermissions((Permissions.PERMISSION_CREATE|Permissions.PERMISSION_DELETE|Permissions.PERMISSION_MODIFY)
				<< Permissions.PERMISSIONS_GROUP_SHIFT);

		return group.getId();
	}

	private void createContent(Element root, Relation rel) {
		Category cat = (Category) rel.getChild();

		Item content = new Item(0, Item.CONTENT);
        Item toc = new Item(0, Item.TOC);

		content.setGroup(cat.getGroup());
		content.setOwner(user.getId());
		content.setPermissions(cat.getPermissions() | (Permissions.PERMISSION_MODIFY)
				<< Permissions.PERMISSIONS_OTHERS_SHIFT);
		content.setTitle("Úvodní wiki stránka");

        content.setData(DocumentHelper.createDocument());

		Element element = DocumentHelper.makeElement(content.getData(), "/data/content");
        element.setText("Vítejte na úvodní stránce vaší nové wiki.");
		element.addAttribute("execute", "no");

		Versioning versioning = VersioningFactory.getVersioning();
        versioning.prepareObjectBeforeCommit(content, user.getId());
		persistence.create(content);
        versioning.commit(content, user.getId(), "Počáteční revize dokumentu");

		Relation subrel = new Relation(rel.getChild(), content, rel.getId());
		subrel.setUrl(rel.getUrl() + "/wiki");
		persistence.create(subrel);

        toc.setTitle("Obsah");
        toc.setData("<data><node rid=\"" + subrel.getId() + "\"></node></data>");
        persistence.create(toc);

        Relation tocRelation = new Relation(subrel.getChild(), toc, subrel.getId());
        tocRelation.setUrl(subrel.getUrl() + "/obsah");
        persistence.create(tocRelation);

        DocumentHelper.makeElement(content.getData(), "//data/toc").setText(Integer.toString(tocRelation.getId()));
        persistence.update(content);

		DocumentHelper.makeElement(root, "wiki").setText(String.valueOf(subrel.getId()));
	}

	private Relation createSection(Element root, Relation rel, String title, String id, String url, int type) {
		Category category = new Category();
		Category parent = (Category) rel.getChild();

		category.setTitle(title);
		category.setGroup(parent.getGroup());
		category.setPermissions(parent.getPermissions());
		category.setOwner(user.getId());
        category.setType(type);
		//category.setSubType("subportal");

		Document document = DocumentHelper.createDocument();
        document.addElement("data");
        category.setData(document);

		persistence.create(category);

		Relation subrel = new Relation(parent, category, rel.getId());

		if (url != null)
			subrel.setUrl(rel.getUrl()+url);

		persistence.create(subrel);

		DocumentHelper.makeElement(root, id).setText(String.valueOf(subrel.getId()));

        return subrel;
	}

	private boolean addUserToGroup(int uid, int gid) {
		try {
			User user = Tools.createUser(uid);

			Element element = (Element) user.getData().selectSingleNode("/data/system/group[text()='"+gid+"']");
            if (element != null)
                return true; // the user already is in that group

			Element system = DocumentHelper.makeElement(user.getData(), "/data/system");

			system.addElement("group").setText(Integer.toString(gid));
			persistence.update(user);
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	private boolean addUserToGroup(Map params, int gid, Category category) {
		int user = Misc.parseInt((String) params.get(PARAM_ADMIN), 0);

        if (user != 0) {
            category.addProperty(Constants.PROPERTY_MEMBER, String.valueOf(user));
            return addUserToGroup(user, gid);
        }
        return true;
	}

	private boolean setTitle(Map params, Category cat, Map env) {
		String title = (String) params.get(PARAM_TITLE);

		title = Misc.filterDangerousCharacters(title);

		if (Misc.empty(title)) {
			ServletUtils.addError(PARAM_TITLE, "Zadejte název!", env, null);
			return false;
		}

		cat.setTitle(title);

		return true;
	}

	private boolean setDescription(Map params, Element data, Map env) {
		String desc = (String) params.get(PARAM_DESCRIPTION);
        desc = Misc.filterDangerousCharacters(desc);

        if (Misc.empty(desc)) {
			ServletUtils.addError(PARAM_DESCRIPTION, "Zadejte popis!", env, null);
			return false;
		}

        try {
            desc = HtmlPurifier.clean(desc);
            HtmlChecker.check(Rules.DEFAULT, desc);
        } catch (Exception e) {
            ServletUtils.addError(PARAM_DESCRIPTION, e.getMessage(), env, null);
            return false;
        }

		DocumentHelper.makeElement(data, "description").setText(desc);

		return true;
	}

    private boolean setShortDescription(Map params, Element data, Map env) {
		String desc = (String) params.get(PARAM_DESCRIPTION_SHORT);
        desc = Misc.filterDangerousCharacters(desc);

        if (Misc.empty(desc)) {
			ServletUtils.addError(PARAM_DESCRIPTION_SHORT, "Zadejte popis!", env, null);
			return false;
		}

        try {
            desc = HtmlPurifier.clean(desc);
            HtmlChecker.check(Rules.DEFAULT, desc);
        } catch (Exception e) {
            ServletUtils.addError(PARAM_DESCRIPTION_SHORT, e.getMessage(), env, null);
            return false;
        }

		DocumentHelper.makeElement(data, "descriptionShort").setText(desc);

		return true;
	}

	private boolean setUrl(Map params, Relation rel, Map env) {
		String url = (String) params.get(PARAM_URL);

		if (url == null || url.length() == 0) {
            ServletUtils.addError(PARAM_URL, "Zadejte URL!", env, null);
            return false;
        }

		if (url.charAt(url.length() - 1) == '/')
            url = url.substring(0, url.length() - 1);

        try {
            url = URLManager.enforceAbsoluteURL(url);
        } catch (Exception e) {
            ServletUtils.addError(PARAM_URL, e.getMessage(), env, null);
            return false;
        }

        if (URLManager.exists(url)) {
            ServletUtils.addError(PARAM_URL, "Toto URL již existuje!", env, null);
            return false;
        }

        rel.setUrl(url);

		return true;
	}

    private boolean setForumHidden(Map params, Element data) {
        String hidden = (String) params.get(PARAM_HIDE_FORUM);

        if ("yes".equals(hidden)) {
            Element elem = DocumentHelper.makeElement(data, "forumHidden");
            elem.setText("yes");
        } else {
            Node node = data.selectSingleNode("forumHidden");
            if (node != null)
                node.detach();
        }

        return true;
    }

	public static String getFileSuffix(String name) {
        if ( name==null )
            return "";
        int i = name.lastIndexOf('.');
        if ( i==-1 )
            return "";
        else
            return name.substring(i+1);
    }

    private boolean checkImage(Map params, Map env) {
        FileItem fileItem = (FileItem) params.get(PARAM_ICON);
        if ( fileItem == null || fileItem.getSize() == 0)
			return true;

		String suffix = getFileSuffix(fileItem.getName()).toLowerCase();
        if ( ! (suffix.equals("jpg") || suffix.equals("jpeg") || suffix.equals("png") || suffix.equals("gif")) ) {
            ServletUtils.addError(PARAM_ICON, "Soubor musí být typu JPG, GIF nebo JPEG!", env, null);
            return false;
        }

		try {
            Iterator readers = ImageIO.getImageReadersBySuffix(suffix);
            ImageReader reader = (ImageReader) readers.next();
            ImageInputStream iis = ImageIO.createImageInputStream(fileItem.getInputStream());
            reader.setInput(iis, false);
            if (reader.getNumImages(true) > 1) {
                ServletUtils.addError(PARAM_ICON, "Animované obrázky nejsou povoleny!", env, null);
                return false;
            }
            if (reader.getHeight(0) > 100 || reader.getWidth(0) > 100) {
                ServletUtils.addError(PARAM_ICON, "Ikonka přesahuje povolené maximální rozměry!", env, null);
                return false;
            }
        } catch(Exception e) {
            ServletUtils.addError(PARAM_ICON, "Nelze načíst obrázek!", env, null);
            return false;
        }
        return true;
    }

	private boolean setIcon(Map params, Relation rel, Element root, Map env) {
		if (params.containsKey(PARAM_REMOVE_ICON)) {
            Node node = root.selectSingleNode("icon");
            if (node != null) {
                String localPath = AbcConfig.calculateDeployedPath(node.getText().substring(1));
                new File(localPath).delete();
                node.detach();
            }
            return true;
        }

		FileItem fileItem = (FileItem) params.get(PARAM_ICON);
        if ( fileItem == null || fileItem.getSize() == 0)
			return true;

		String suffix = getFileSuffix(fileItem.getName()).toLowerCase();
        String fileName = "images/subportals/" + rel.getId() + "." + suffix;
        File file = new File(AbcConfig.calculateDeployedPath(fileName));
        try {
            fileItem.write(file);
        } catch (Exception e) {
            ServletUtils.addError(PARAM_ICON, "Chyba při zápisu na disk!", env, null);
            log.error("Není možné uložit ikonku " + file.getAbsolutePath() + " na disk!",e);
            return false;
        }

        Element photo = DocumentHelper.makeElement(root, "icon");
        photo.setText("/"+fileName);

		return true;
	}
}
