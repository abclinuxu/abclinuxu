/*
 * User: literakl
 * Date: Feb 1, 2002
 * Time: 4:48:29 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.format.Format;
import cz.abclinuxu.utils.format.FormatDetector;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.exceptions.PersistanceException;

import org.dom4j.*;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.apache.regexp.StringCharacterIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.text.ParseException;

/**
 * Class for manipulation of articles.
 */
public class EditArticle extends AbcFMServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditArticle.class);

    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_PEREX = "perex";
    public static final String PARAM_CONTENT = "content";
    public static final String PARAM_PUBLISHED = "published";
    public static final String PARAM_AUTHOR = "authorId";
    public static final String PARAM_FORBID_DISCUSSIONS = "forbid_discussions";
    public static final String PARAM_RELATED_ARTICLES = "related";
    public static final String PARAM_RESOURCES = "resources";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_AUTHORS = "AUTHORS";

    public static final String ACTION_ADD_ITEM = "add";
    public static final String ACTION_ADD_ITEM_STEP2 = "add2";
    public static final String ACTION_EDIT_ITEM = "edit";
    public static final String ACTION_EDIT_ITEM_STEP2 = "edit2";

    private static RE reBreak;
    static {
        try {
            reBreak = new RE("<page title=\"([^\"]+)\">", RE.MATCH_SINGLELINE);
        } catch (RESyntaxException e) {
            log.fatal("Cannot compile regular expression!",e);
        }
    }

    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT,PARAM_RELATION,Relation.class,params);
        if ( relation==null )
            throw new MissingArgumentException("Chybí parametr relationId!");

        persistance.synchronize(relation);
        persistance.synchronize(relation.getChild());
        env.put(VAR_RELATION,relation);

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        if ( !user.hasRole(Roles.ARTICLE_ADMIN) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if ( ACTION_ADD_ITEM.equals(action) )
            return actionAddStep1(request, env);

        if ( action.equals(ACTION_ADD_ITEM_STEP2) )
            return actionAddStep2(request, response, env);

        if ( action.equals(ACTION_EDIT_ITEM) )
            return actionEditItem(request, env);

        if ( action.equals(ACTION_EDIT_ITEM_STEP2) )
            return actionEditItem2(request, response, env);

        throw new MissingArgumentException("Chybí parametr action!");
    }

    private String actionAddStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        params.put(PARAM_PUBLISHED,Constants.isoFormat.format(new Date()));
        addAuthors(env);
        return FMTemplateSelector.select("EditArticle","add",env,request);
    }

    protected String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation upper = (Relation) env.get(VAR_RELATION);
        User user = (User) env.get(Constants.VAR_USER);

        Item item = new Item(0,Item.ARTICLE);
        item.setData(DocumentHelper.createDocument());
        item.setOwner(user.getId());

        Record record = new Record(0,Record.ARTICLE);
        record.setData(DocumentHelper.createDocument());
        record.setOwner(user.getId());

        boolean canContinue = true;
        canContinue &= setTitle(params, item, env);
        canContinue &= setAuthor(params, item, env);
        canContinue &= setEditor(item, env);
        canContinue &= setPerex(params, item, env);
        canContinue &= setPublishDate(params, item, env);
        canContinue &= setForbidDiscussions(params, item);
        canContinue &= setArticleContent(params, record, env);
        canContinue &= setRelatedArticles(params, record, env);
        canContinue &= setResources(params, record, env);

        if ( !canContinue ) {
            addAuthors(env);
            return FMTemplateSelector.select("EditArticle", "edit", env, request);
        }

        try {
            persistance.create(item);
            Relation relation = new Relation(upper.getChild(),item,upper.getId());
            persistance.create(relation);
            persistance.create(record);
            persistance.create(new Relation(item,record,relation.getId()));

            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/ViewRelation?rid="+relation.getId());
            return null;
        } catch (PersistanceException e) {
            ServletUtils.addError(Constants.ERROR_GENERIC,e.getMessage(),env, null);
            addAuthors(env);
            return FMTemplateSelector.select("EditArticle", "edit", env, request);
        }
    }

    protected String actionEditItem(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        Document document = item.getData();

        Node node = document.selectSingleNode("/data/name");
        if ( node!=null )
            params.put(PARAM_TITLE,node.getText());
        node = document.selectSingleNode("/data/perex");
        if ( node!=null )
            params.put(PARAM_PEREX,node.getText());
        params.put(PARAM_PUBLISHED, Constants.isoFormat.format(item.getCreated()));
        node = document.selectSingleNode("/data/author");
        params.put(PARAM_AUTHOR,node.getText());
        node = document.selectSingleNode("/data/forbid_discussions");
        if ( node!=null && "yes".equals(node.getText()) )
            params.put(PARAM_FORBID_DISCUSSIONS, node.getText());

        Relation child = InstanceUtils.findFirstChildRecordOfType(item,Record.ARTICLE);
        Record record = (Record) child.getChild();
        document = record.getData();

        addAuthors(env);
        addArticleContent(document, params);
        addLinks(document, "/data/related/link", params, PARAM_RELATED_ARTICLES);
        addLinks(document, "/data/resources/link", params, PARAM_RESOURCES);

        return FMTemplateSelector.select("EditArticle","edit",env,request);
    }

    protected String actionEditItem2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation upper = (Relation) env.get(VAR_RELATION);

        Item item = (Item) upper.getChild();
        Relation child = InstanceUtils.findFirstChildRecordOfType(item, Record.ARTICLE);
        Record record = (Record) child.getChild();

        boolean canContinue = true;
        canContinue &= setTitle(params, item, env);
        canContinue &= setAuthor(params, item, env);
        canContinue &= setEditor(item, env);
        canContinue &= setPerex(params, item, env);
        canContinue &= setPublishDate(params, item, env);
        canContinue &= setForbidDiscussions(params, item);
        canContinue &= setArticleContent(params, record, env);
        canContinue &= setRelatedArticles(params, record, env);
        canContinue &= setResources(params, record, env);

        if ( !canContinue ) {
            addAuthors(env);
            return FMTemplateSelector.select("EditArticle","edit",env,request);
        }

        persistance.update(item);
        persistance.update(record);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/ViewRelation?rid="+upper.getId());
        return null;
    }

    /**
     * Updates title from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item article to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setTitle(Map params, Item item, Map env) {
        String name = (String) params.get(PARAM_TITLE);
        if ( name==null || name.length()==0 ) {
            ServletUtils.addError(PARAM_TITLE, "Vyplòte titulek èlánku!", env, null);
            return false;
        }
        Element element = DocumentHelper.makeElement(item.getData(), "/data/name");
        element.setText(name);
        return true;
    }

    /**
     * Updates perex from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item article to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setPerex(Map params, Item item, Map env) {
        String perex = (String) params.get(PARAM_PEREX);
        if ( perex==null || perex.length()==0 ) {
            ServletUtils.addError(PARAM_PEREX, "Vyplòte popis èlánku!", env, null);
            return false;
        }
        Element element = DocumentHelper.makeElement(item.getData(), "/data/perex");
        element.setText(perex);
        return true;
    }

    /**
     * Updates author from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item article to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setAuthor(Map params, Item item, Map env) {
        /** todo: support for author not listed in section Authors */
        User author = (User) InstanceUtils.instantiateParam(PARAM_AUTHOR, User.class, params);
        if ( author==null ) {
            ServletUtils.addError(PARAM_AUTHOR, "Vyberte autora!", env, null);
            return false;
        }
        Element element = DocumentHelper.makeElement(item.getData(), "/data/author");
        element.setText(String.valueOf(author.getId()));
        return true;
    }

    /**
     * Updates editor . Changes are not synchronized with persistance.
     * @param item article to be updated.
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setEditor(Item item, Map env) {
        User user = (User) env.get(Constants.VAR_USER);
        Element element = DocumentHelper.makeElement(item.getData(), "/data/editor");
        element.setText(String.valueOf(user.getId()));
        return true;
    }

    /**
     * Updates date of publishing from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item article to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setPublishDate(Map params, Item item, Map env) {
        try {
            Date publish = Constants.isoFormat.parse((String) params.get(PARAM_PUBLISHED));
            item.setCreated(publish);
        } catch (ParseException e) {
            ServletUtils.addError(PARAM_PUBLISHED, "Správný formát je 2002-02-10 06:22", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates content of the article from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param record article record to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    public static boolean setArticleContent(Map params, Record record, Map env) {
        String content = (String) params.get(PARAM_CONTENT);
        if ( content==null || content.length()==0 ) {
            ServletUtils.addError(PARAM_CONTENT, "Vyplòte obsah èlánku!", env, null);
            return false;
        }

        // cleanup - remove all pages, if there were some
        List nodes = record.getData().selectNodes("/data/content");
        for ( Iterator iter = nodes.iterator(); iter.hasNext(); )
            ((Node) iter.next()).detach();

        Format format = FormatDetector.detect(content);
        if ( reBreak.match(content) ) {
            StringCharacterIterator stringIter = new StringCharacterIterator(content);
            String title, page; int start, end; boolean canContinue;
            DocumentHelper.makeElement(record.getData(), "data");
            Element data = record.getData().getRootElement();
            do {
                title = reBreak.getParen(1);
                start = reBreak.getParenEnd(0);
                canContinue = reBreak.match(stringIter, start);
                end = (canContinue) ? reBreak.getParenStart(0) : content.length();
                page = stringIter.substring(start, end);
                start = end;

                Element element = data.addElement("content");
                element.addAttribute("title", title);
                element.addAttribute("format", Integer.toString(format.getId()));
                element.setText(page);
            } while (canContinue);
        } else {
            Element element = DocumentHelper.makeElement(record.getData(), "data/content");
            element.addAttribute("format", Integer.toString(format.getId()));
            element.setText(content);
        }

        return true;
    }

    /**
     * Updates forbid_discussions from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item article  to be updated
     * @return false, if there is a major error.
     */
    private boolean setForbidDiscussions(Map params, Item item) {
        String content = (String) params.get(PARAM_FORBID_DISCUSSIONS);
        Element element = (Element) item.getData().selectSingleNode("/data/forbid_discussions");
        if ( element!=null )
            element.detach();

        if ( content==null || content.length()==0 )
            return true;

        element = DocumentHelper.makeElement(item.getData(), "/data/forbid_discussions");
        element.setText(content);
        return true;
    }

    /**
     * Updates related articles from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param record article's record to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setRelatedArticles(Map params, Record record, Map env) {
        String links = (String) params.get(PARAM_RELATED_ARTICLES);
        Element related = (Element) record.getData().selectSingleNode("/data/related");
        if ( related!=null )
            related.detach();

        if (links==null || links.length()==0)
            return true;

        related = DocumentHelper.makeElement(record.getData(), "data/related");
        StringTokenizer stk = new StringTokenizer(links,"\n");
        String url, title;
        while ( stk.hasMoreTokens() ) {
            url = stk.nextToken();
            if ( url.trim().length()==0 )
                break; // whitespaces on empty line
            if ( ! stk.hasMoreTokens() ) {
                ServletUtils.addError(PARAM_RELATED_ARTICLES, "Chybí titulek pro URL "+url+"!", env, null);
                return false;
            }
            title = stk.nextToken();

            Element link = related.addElement("link");
            link.addAttribute("url",url);
            link.setText(title);
        }
        return true;
    }

    /**
     * Updates resources from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param record article's record to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setResources(Map params, Record record, Map env) {
        String links = (String) params.get(PARAM_RESOURCES);
        Element resources = (Element) record.getData().selectSingleNode("/data/resources");
        if ( resources!=null )
            resources.detach();

        if (links==null || links.length()==0)
            return true;

        resources = DocumentHelper.makeElement(record.getData(), "data/resources");
        StringTokenizer stk = new StringTokenizer(links,"\n");
        String url, title;
        while ( stk.hasMoreTokens() ) {
            url = stk.nextToken();
            if ( url.trim().length()==0 )
                break; // whitespaces on empty line
            if ( ! stk.hasMoreTokens() ) {
                ServletUtils.addError(PARAM_RESOURCES, "Chybí titulek pro URL "+url+"!", env, null);
                return false;
            }
            title = stk.nextToken();

            Element link = resources.addElement("link");
            link.addAttribute("url",url);
            link.setText(title);
        }
        return true;
    }

    /**
     * Adds list of authors (User) to env in VAR_AUTHORS.
     * todo sort authors by name
     */
    private void addAuthors(Map env) {
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = null;

        Category category = (Category) persistance.findById(new Category(Constants.CAT_AUTHORS));
        List authors = new ArrayList(category.getContent().size());
        for (Iterator it = category.getContent().iterator(); it.hasNext();) {
            GenericObject child = ((Relation) it.next()).getChild();
            if ( child instanceof User ) {
                user = (User) persistance.findById(child);
                authors.add(user);
            }
        }
        env.put(VAR_AUTHORS,authors);

        Map params = (Map) env.get(Constants.VAR_PARAMS);
        if ( params.get(PARAM_AUTHOR)==null ) {
            params.put(PARAM_AUTHOR,Integer.toString(user.getId()));
        }
    }

    /**
     * Adds related articles or resources to map params under given name.
     */
    private void addLinks(Document document, String xpath, Map params, String var) {
        List nodes = document.selectNodes(xpath);
        if ( nodes!=null && nodes.size()>0 ) {
            StringBuffer sb = new StringBuffer();
            for ( Iterator iter = nodes.iterator(); iter.hasNext(); ) {
                Element element = (Element) iter.next();
                sb.append(element.attributeValue("url"));
                sb.append("\n");
                sb.append(element.getText());
            }
            params.put(var, sb.toString());
        }
    }

    /**
     * Adds content of the article to params.
     */
    private void addArticleContent(Document document, Map params) {
        List nodes = document.selectNodes("/data/content");
        if ( nodes.size()==0 ) {
            return;
        } else if ( nodes.size()==1 ) {
            params.put(PARAM_CONTENT, ((Node) nodes.get(0)).getText());
        } else {
            StringBuffer sb = new StringBuffer();
            for ( Iterator iter = nodes.iterator(); iter.hasNext(); ) {
                Element element = (Element) iter.next();
                sb.append("<page title=\"");
                sb.append(element.attributeValue("title"));
                sb.append("\">");
                sb.append(element.getText());
            }
            params.put(PARAM_CONTENT, sb.toString());
        }
    }
}
