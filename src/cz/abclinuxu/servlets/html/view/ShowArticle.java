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
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.ReadRecorder;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.exceptions.InvalidDataException;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.extra.Qualifier;
import cz.abclinuxu.persistance.extra.LimitQualifier;
import cz.abclinuxu.security.Roles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Node;
import org.dom4j.Element;
import org.dom4j.Document;

/**
 * Serves to display an article to the user.
 */
public class ShowArticle implements AbcAction {
    public static final String PARAM_RELATION_ID = "relationId";
    public static final String PARAM_RELATION_ID_SHORT = "rid";
    public static final String PARAM_PAGE = "page";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_PARENTS = "PARENTS";
    public static final String VAR_CHILDREN_MAP = "CHILDREN";
    public static final String VAR_PAGES = "PAGES";
    public static final String VAR_PAGE = "PAGE";
    public static final String VAR_ARTICLE_TEXT = "TEXT";
    public static final String VAR_RELATED_ARTICLES = "RELATED";
    public static final String VAR_RELATED_RESOURCES = "RESOURCES";
    public static final String VAR_ARTICLES_IN_SAME_SECTION = "SAME_SECTION_ARTICLES";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_ID_SHORT, Relation.class, params, request);
        if ( relation==null ) {
            throw new MissingArgumentException("Parametr relationId je prázdný!");
        }

        Tools.sync(relation);
        env.put(VAR_RELATION, relation);

        List parents = persistance.findParents(relation);
        env.put(VAR_PARENTS, parents);
        Item item = (Item) relation.getChild();
        Tools.sync(item);

        return show(env, item, request);
    }

    /**
     * Shows the article.
     */
    static String show(Map env, Item item, HttpServletRequest request) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Record record = null;

        Map children = (Map) env.get(VAR_CHILDREN_MAP);
        if (children==null) {
            children = Tools.groupByType(item.getChildren());
            env.put(VAR_CHILDREN_MAP, children);
        }

        List list = (List) children.get(Constants.TYPE_RECORD);
        if ( list==null || list.size()==0 )
            throw new NotFoundException("Èlánek "+item.getId()+" nemá obsah!");
        record = (Record) ((Relation) list.get(0)).getChild();
        if ( record.getType()!=Record.ARTICLE )
            throw new InvalidDataException("Záznam "+record.getId()+" není typu èlánek!");

        Document document = record.getData();
        List nodes = document.selectNodes("/data/content");
        if ( nodes.size()==0 ) {
            throw new InvalidDataException("Záznam "+record.getId()+" má ¹patný obsah!");
        } else if ( nodes.size()==1 ) {
            env.put(VAR_ARTICLE_TEXT,((Node)nodes.get(0)).getText());
        } else {
            Map params = (Map) env.get(Constants.VAR_PARAMS);
            int page = Misc.parseInt((String) params.get(PARAM_PAGE),0);
            env.put(VAR_PAGE, new Integer(page));
            env.put(VAR_ARTICLE_TEXT, ((Node) nodes.get(page)).getText());

            List pages = new ArrayList(nodes.size());
            for ( Iterator iter = nodes.iterator(); iter.hasNext(); )
                pages.add(((Element)iter.next()).attributeValue("title"));
            env.put(VAR_PAGES,pages);
        }

        nodes = document.selectNodes("/data/related/link");
        if ( nodes!=null && nodes.size()>0 ) {
            List articles = new ArrayList(nodes.size());
            for ( Iterator iter = nodes.iterator(); iter.hasNext(); ) {
                Element element = (Element) iter.next();
                Link link = new Link(element.getText(), element.attributeValue("url"), element.attributeValue("description"));
                articles.add(link);
            }
            env.put(VAR_RELATED_ARTICLES,articles);
        }

        nodes = document.selectNodes("/data/resources/link");
        if ( nodes!=null && nodes.size()>0 ) {
            List resources = new ArrayList(nodes.size());
            for ( Iterator iter = nodes.iterator(); iter.hasNext(); ) {
                Element element = (Element) iter.next();
                Link link = new Link(element.getText(), element.attributeValue("url"), element.attributeValue("description"));
                resources.add(link);
            }
            env.put(VAR_RELATED_RESOURCES,resources);
        }

        List parents = (List) env.get(VAR_PARENTS);
        if (parents.size()>1) {
            Relation relation = (Relation) parents.get(parents.size() - 2);
            if (relation.getChild() instanceof Category) {
                Category section = (Category) relation.getChild();
                int max = AbcConfig.getArticleSectionArticlesCount();
                SQLTool sqlTool = SQLTool.getInstance();
                Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, max)};
                List articles = sqlTool.findArticleRelations(qualifiers, section.getId());
                Tools.syncList(articles);
                env.put(VAR_ARTICLES_IN_SAME_SECTION, articles);
            }
        }

        User user = (User) env.get(Constants.VAR_USER);
        if ( user==null || !user.hasRole(Roles.ARTICLE_ADMIN) )
            ReadRecorder.log(item, Constants.COUNTER_READ, env);

        return FMTemplateSelector.select("ShowObject", "article", env, request);
    }

    /**
     * Holder of one link from related articles or resources.
     */
    public static class Link {
        private String url, title, description;

        public Link(String title, String url, String description) {
            this.url = url;
            this.title = title;
            this.description = description;
        }

        public String getUrl() {
            return url;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String toString() {
            return url+" -> "+title;
        }
    }
}
