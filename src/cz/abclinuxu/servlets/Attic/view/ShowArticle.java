/*
 * User: literakl
 * Date: 15.8.2003
 * Time: 18:50:55
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Record;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.exceptions.InvalidDataException;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;

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
    public static final String VAR_ALLOW_DISCUSSIONS = "ALLOW_DISCUSSIONS";
    public static final String VAR_RELATED_ARTICLES = "RELATED";
    public static final String VAR_RELATED_RESOURCES = "RESOURCES";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_ID_SHORT,PARAM_RELATION_ID, Relation.class, params);
        if ( relation==null ) {
            throw new MissingArgumentException("Parametr relationId je prázdný!");
        }

        Tools.sync(relation);
        env.put(VAR_RELATION, relation);
        List parents = persistance.findParents(relation);
        env.put(VAR_PARENTS, parents);
        Item item = (Item) relation.getChild();
        Tools.sync(item);

        return show(env, item, request, response);
    }

    /**
     * Shows the article.
     */
    static String show(Map env, Item item, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Record record = null;

        Map children = Tools.groupByType(item.getContent());
        env.put(VAR_CHILDREN_MAP, children);

        List list = (List) children.get(Constants.TYPE_RECORD);
        if ( list==null || list.size()==0 )
            throw new NotFoundException("Èlánek "+item.getId()+" nemá obsah!");
        record = (Record) ((Relation) list.get(0)).getChild();
        if ( record.getType()!=Record.ARTICLE )
            throw new InvalidDataException("Záznam "+record.getId()+" není typu èlánek!");

        list = (List) children.get(Constants.TYPE_DISCUSSION);
        if ( list!=null && list.size()==1 ) {
            Item discussion = (Item)((Relation) list.get(0)).getChild();
            Tools.sync(discussion.getContent());
            Tools.handleNewComments(discussion,env,request,response);
        }

        Document document = item.getData();

        boolean allow = true;
        Node node = document.selectSingleNode("/data/forbid_discussions");
        if ( node!=null && "yes".equals(node.getText()))
            allow = false;
        env.put(VAR_ALLOW_DISCUSSIONS, new Boolean(allow));

        document = record.getData();

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
                Link link = new Link(element.getText(), element.attributeValue("url"));
                articles.add(link);
            }
            env.put(VAR_RELATED_ARTICLES,articles);
        }

        nodes = document.selectNodes("/data/resources/link");
        if ( nodes!=null && nodes.size()>0 ) {
            List resources = new ArrayList(nodes.size());
            for ( Iterator iter = nodes.iterator(); iter.hasNext(); ) {
                Element element = (Element) iter.next();
                Link link = new Link(element.getText(), element.attributeValue("url"));
                resources.add(link);
            }
            env.put(VAR_RELATED_RESOURCES,resources);
        }

        persistance.incrementCounter(item);
        return FMTemplateSelector.select("ViewRelation", "article", env, request);
    }
    /**
     * Holder of one link from related articles or resources.
     */
    public static class Link {
        private String url, title;

        public Link(String title, String url) {
            this.url = url;
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public String getTitle() {
            return title;
        }

        public String toString() {
            return url+" -> "+title;
        }
    }
}
