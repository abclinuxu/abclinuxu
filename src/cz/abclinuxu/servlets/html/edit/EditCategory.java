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
import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.security.Permissions;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.TagTool;
import cz.abclinuxu.utils.format.Format;
import cz.abclinuxu.utils.format.FormatDetector;
import cz.abclinuxu.utils.forms.PermissionsSet;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.parser.safehtml.WikiContentGuard;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.htmlparser.util.ParserException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Class for manipulation with Category.
 */
public class EditCategory implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditCategory.class);

    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_OPEN = "open";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_SUBTYPE = "subtype";
    public static final String PARAM_NOTE = "note";
    public static final String PARAM_UPPER = "upper";
	public static final String PARAM_GROUP = "group";
	public static final String PARAM_GROUP_PERMISSIONS = "groupPermissions";
	public static final String PARAM_OTHERS_PERMISSIONS = "othersPermissions";
	public static final String PARAM_RECURSE = "recurse";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_CATEGORY = "CATEGORY";
	public static final String VAR_GROUP_PERMISSIONS = "GROUP_PERMISSIONS";
	public static final String VAR_OTHERS_PERMISSIONS = "OTHERS_PERMISSIONS";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT2 = "edit2";


    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        if (ServletUtils.handleMaintainance(request, env)) {
            response.sendRedirect(response.encodeRedirectURL("/"));
            return null;
        }

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Chybí parametr relationId!");

        relation = (Relation) persistence.findById(relation);
        Category category = (Category) persistence.findById(relation.getChild()).clone();
        env.put(VAR_RELATION, relation);
        env.put(VAR_CATEGORY, category);

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        if ( !Tools.permissionsFor(user, relation).canModify() )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if ( ACTION_ADD.equals(action) )
			return actionAddStep1(request, response, env);

        if ( ACTION_ADD_STEP2.equals(action) ) {
            ActionProtector.ensureContract(request, EditCategory.class, true, true, true, false);
            return actionAddStep2(request, response, env);
        }
        
        int upper = relation.getUpper();
        if (upper != 0) {
            if ( !Tools.permissionsFor(user, new Relation(upper)).canModify() )
                return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
        }

        if ( ACTION_EDIT.equals(action) )
            return actionEditStep1(request, env);

        if ( ACTION_EDIT2.equals(action) ) {
            ActionProtector.ensureContract(request, EditCategory.class, true, true, true, false);
            return actionEditStep2(request, response, env);
        }

        throw new MissingArgumentException("Chybí parametr action!");
    }
	
	/**
     * First step of creating a new category
     */
	protected String actionAddStep1(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
		Map params = (Map) env.get(Constants.VAR_PARAMS);
		Category category = (Category) env.get(VAR_CATEGORY);
		
		setTypeParam(params, category);
		
		int perms = category.getPermissions();
		PermissionsSet group, others;
		
		group = new PermissionsSet(new Permissions((perms >> Permissions.PERMISSIONS_GROUP_SHIFT) & 0xff));
		env.put(VAR_GROUP_PERMISSIONS, group);
		others = new PermissionsSet(new Permissions((perms >> Permissions.PERMISSIONS_OTHERS_SHIFT) & 0xff));
		env.put(VAR_OTHERS_PERMISSIONS, others);
		
		params.put(PARAM_GROUP, category.getGroup());
		
        return FMTemplateSelector.select("EditCategory", "add", env, request);
	}

    /**
     * Creates new category
     */
    protected String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();

        Relation upper = (Relation) env.get(VAR_RELATION);
        User user = (User) env.get(Constants.VAR_USER);

        Category category = new Category();
        Document document = DocumentHelper.createDocument();
        document.addElement("data");
        category.setData(document);
        category.setOwner(user.getId());
		
		PermissionsSet group, others;
		group = new PermissionsSet(params.get(PARAM_GROUP_PERMISSIONS));
		env.put(VAR_GROUP_PERMISSIONS, group);
		others = new PermissionsSet(params.get(PARAM_OTHERS_PERMISSIONS));
		env.put(VAR_OTHERS_PERMISSIONS, others);

        boolean canContinue = setName(params, category, env);
        canContinue &= setDescription(params, document, env);
        canContinue &= setType(params, category, env);
        canContinue &= setSubType(params, category);
        //canContinue &= setOpen(params, document);
		canContinue &= setGroup(params, category, env);
		canContinue &= setPermissions(params, category, env);
        if (! canContinue)
            return FMTemplateSelector.select("EditCategory", "add", env, request);

        persistence.create(category);
        Relation relation = null;
        relation = new Relation(upper.getChild(), category, upper.getId());

        String upperUrl = upper.getUrl();
        if (upperUrl != null) {
            String name = category.getTitle();
            String url = upperUrl + "/" + URLManager.enforceRelativeURL(name);
            url = URLManager.protectFromDuplicates(url);
            relation.setUrl(url);
        }

        persistence.create(relation);
        relation.getParent().addChildRelation(relation);

        TagTool.assignDetectedTags(category, user);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
        return null;
    }

    /**
     * First step for editing of category
     */
    protected String actionEditStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Category category = (Category) env.get(VAR_CATEGORY);

        Document document = category.getData();
        params.put(PARAM_NAME, category.getTitle());
        Node node = document.selectSingleNode("data/note");
        if (node != null)
            params.put(PARAM_NOTE, node.getText());
        node = document.selectSingleNode("data/writeable");
        //if (node != null)
        //    params.put(PARAM_OPEN, node.getText());
        setTypeParam(params, category);
        params.put(PARAM_SUBTYPE, category.getSubType());
        params.put(PARAM_UPPER, relation.getUpper());
		params.put(PARAM_GROUP, category.getGroup());
		
		int perms = category.getPermissions();
		PermissionsSet group, others;
		
		group = new PermissionsSet(new Permissions((perms >> Permissions.PERMISSIONS_GROUP_SHIFT) & 0xff));
		env.put(VAR_GROUP_PERMISSIONS, group);
		others = new PermissionsSet(new Permissions((perms >> Permissions.PERMISSIONS_OTHERS_SHIFT) & 0xff));
		env.put(VAR_OTHERS_PERMISSIONS, others);
		
        return FMTemplateSelector.select("EditCategory","edit",env,request);
    }

    /**
     * Final step for editing of category
     */
    protected String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();

        Relation relation = (Relation) env.get(VAR_RELATION);
        Category category = (Category) env.get(VAR_CATEGORY);
        Document document = category.getData();
		
		PermissionsSet group, others;
		group = new PermissionsSet(params.get(PARAM_GROUP_PERMISSIONS));
		env.put(VAR_GROUP_PERMISSIONS, group);
		others = new PermissionsSet(params.get(PARAM_OTHERS_PERMISSIONS));
		env.put(VAR_OTHERS_PERMISSIONS, others);

        boolean canContinue = setName(params, category, env);
        canContinue &= setDescription(params, document, env);
        canContinue &= setType(params, category, env);
        canContinue &= setSubType(params, category);
        //canContinue &= setOpen(params, document);
		canContinue &= setGroup(params, category, env);
		canContinue &= setPermissions(params, category, env);
        if (canContinue)
            canContinue = setUpper(params, relation, env);
        if (! canContinue)
            return FMTemplateSelector.select("EditCategory", "edit", env, request);

        persistence.update(category);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        if ( relation!=null )
            urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
        else
            urlUtils.redirect(response, "/dir?categoryId="+category.getId());
        return null;
    }

    // setters


    /**
     * Updates name of category from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param category category to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setName(Map params, Category category, Map env) {
        String tmp = (String) params.get(PARAM_NAME);
        tmp = Misc.filterDangerousCharacters(tmp);
        if (Misc.empty(tmp)) {
            ServletUtils.addError(PARAM_NAME, "Zadejte jméno sekce!", env, null);
            return false;
        }
        category.setTitle(tmp);
        return true;
    }

    /**
     * Updates driver's note from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param document Document of discussion to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setDescription(Map params, Document document, Map env) {
        String tmp = (String) params.get(PARAM_NOTE);
        tmp = Misc.filterDangerousCharacters(tmp);
        if (tmp != null && tmp.length() > 0) {
            try {
                WikiContentGuard.check(tmp);
            } catch (ParserException e) {
                log.error("ParseException on '" + tmp + "'", e);
                ServletUtils.addError(PARAM_NOTE, e.getMessage(), env, null);
                return false;
            } catch (Exception e) {
                ServletUtils.addError(PARAM_NOTE, e.getMessage(), env, null);
                return false;
            }
            Element element = DocumentHelper.makeElement(document, "data/note");
            element.setText(tmp);
            Format format = FormatDetector.detect(tmp);
            element.addAttribute("format", Integer.toString(format.getId()));
        } else {
            Element element = document.getRootElement().element("note");
            if (element != null)
                element.detach();
        }
        return true;
    }

    /**
     * Updates type from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param category category to be updated
     * @return false, if there is a major error.
     */
    private boolean setType(Map params, Category category, Map env) {
        String type = (String) params.get(PARAM_TYPE);
        if (type == null || type.length() == 0) {
            ServletUtils.addError(PARAM_TYPE, "Vyberte typ sekce!", env, null);
            return false;
        }
        if ("generic".equals(type))
            category.setType(0);
        else if ("software".equals(type))
            category.setType(Category.SOFTWARE_SECTION);
        else if ("hardware".equals(type))
            category.setType(Category.HARDWARE_SECTION);
        else if ("forum".equals(type))
            category.setType(Category.FORUM);
        else if ("blog".equals(type))
            category.setType(Category.BLOG);
        else if ("section".equals(type))
            category.setType(Category.SECTION);
        else if ("faq".equals(type))
            category.setType(Category.FAQ);

        return true;
    }

    private void setTypeParam(Map params, Category category) {
        switch (category.getType()) {
            case Category.SOFTWARE_SECTION:
                params.put(PARAM_TYPE, "software");
                break;
            case Category.HARDWARE_SECTION:
                params.put(PARAM_TYPE, "hardware");
                break;
            case Category.FORUM:
                params.put(PARAM_TYPE, "forum");
                break;
            case Category.BLOG:
                params.put(PARAM_TYPE, "blog");
                break;
            case Category.SECTION:
                params.put(PARAM_TYPE, "section");
                break;
            case Category.FAQ:
                params.put(PARAM_TYPE, "faq");
                break;
			case Category.SUBPORTAL:
				params.put(PARAM_TYPE, "subportal");
				break;
            default:
                params.put(PARAM_TYPE, "generic");
        }
    }

    /**
     * Updates subtype from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param category category to be updated
     * @return false, if there is a major error.
     */
    private boolean setSubType(Map params, Category category) {
        String tmp = (String) params.get(PARAM_SUBTYPE);
        tmp = Misc.filterDangerousCharacters(tmp);
        if (tmp != null && tmp.trim().length() == 0)
            tmp = null;
        category.setSubType(tmp);
        return true;
    }

    /**
     * Updates relation from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param relation relation to be updated
     * @return false, if there is a major error.
     */
    private boolean setUpper(Map params, Relation relation, Map env) {
        String tmp = (String) params.get(PARAM_UPPER);
        if (tmp == null || tmp.trim().length() == 0)
            return true;

        Persistence persistence = PersistenceFactory.getPersistence();
        int upper = Misc.parseInt(tmp, 0);
        if (upper == relation.getUpper())
            return true;

        if (upper == 0) {
            relation.setUpper(0);
            persistence.update(relation);
            return true;
        }

        try {
            persistence.findById(new Relation(upper));
            relation.setUpper(upper);
            persistence.update(relation);
            return true;
        } catch (NotFoundException e) {
            ServletUtils.addError(PARAM_UPPER, "Relace s číslem " + upper + " nebyla nalezena!", env, null);
            return false;
        }
    }

    /**
     * Updates open flag from parameters. Changes are not synchronized with persistence.
     * @param params   map holding request's parameters
     * @param document document to be update
     * @return false, if there is a major error.
     */
    private boolean setOpen(Map params, Document document) {
        Boolean open = Boolean.valueOf((String) params.get(PARAM_OPEN));
        if (open.booleanValue())
            DocumentHelper.makeElement(document, "/data/writeable").setText("true");
        else {
            Element element = document.getRootElement().element("writeable");
            if (element != null)
                element.detach();
        }
        return true;
    }
	
	/**
     * Updates the group from parameters. Changes may be synchronized with persistence.
     * @param params   map holding request's parameters
     * @param catgeory category to be updated
     * @return false, if there is a major error.
     */
	private boolean setGroup(Map params, Category category, Map env) {
		String tmp = (String) params.get(PARAM_GROUP);
		int gid = Misc.parseInt(tmp, 0);
		
        if (gid != 0) {
            try {
                Item group = new Item(gid);
                GenericObject gdo;

                Persistence persistence = PersistenceFactory.getPersistence();
                gdo = persistence.findById(group);

                if (! (gdo instanceof Item)) {
                    ServletUtils.addError(PARAM_GROUP, "Objekt s číslem " + gid + " není skupina!", env, null);
                    return false;
                }

                group = (Item) gdo;
                if (group.getType() != Item.GROUP) {
                    ServletUtils.addError(PARAM_GROUP, "Objekt s číslem " + gid + " není skupina!", env, null);
                    return false;
                }
            } catch (NotFoundException e) {
                ServletUtils.addError(PARAM_GROUP, "Skupina s číslem " + gid + " nebyla nalezena!", env, null);
                return false;
            }
        }
		
		Boolean recurse = Boolean.valueOf((String) params.get(PARAM_RECURSE));
		if (!recurse.booleanValue())
			category.setGroup(gid);
		else
			setGroupRecurse(category, gid);
		
		return true;
	}
	
	private void setGroupRecurse(Category category, int gid) {
		List<Relation> rels = Tools.syncList(category.getChildren());
		Persistence persistence = PersistenceFactory.getPersistence();
		
		category.setGroup(gid);
		
		for (Relation rel : rels) {
			GenericObject child = rel.getChild();
			if (child instanceof Category)
				setGroupRecurse((Category) child, gid);
			else if (child instanceof GenericDataObject)
				((GenericDataObject) child).setGroup(gid);
			persistence.update(child);
		}
	}
	
	private boolean setPermissions(Map params, Category category, Map env) {
		
		PermissionsSet group = (PermissionsSet) env.get(VAR_GROUP_PERMISSIONS);
		PermissionsSet others = (PermissionsSet) env.get(VAR_OTHERS_PERMISSIONS);
		
		int permissions;
		
		permissions = group.getPermissions() << Permissions.PERMISSIONS_GROUP_SHIFT;
		permissions |= others.getPermissions() << Permissions.PERMISSIONS_OTHERS_SHIFT;
		
		Boolean recurse = Boolean.valueOf((String) params.get(PARAM_RECURSE));
		if (!recurse.booleanValue())
			category.setPermissions(permissions);
		else
			setPermissionsRecurse(category, permissions);
		
		return true;
	}
	
	private void setPermissionsRecurse(Category category, int permissions) {
		List<Relation> rels = Tools.syncList(category.getChildren());
		Persistence persistence = PersistenceFactory.getPersistence();
		
		category.setPermissions(permissions);
		
		for (Relation rel : rels) {
			GenericObject child = rel.getChild();
			if (child instanceof Category)
				setPermissionsRecurse((Category) child, permissions);
			else if (child instanceof GenericDataObject)
				((GenericDataObject) child).setPermissions(permissions);
			persistence.update(child);
		}
	}
}

