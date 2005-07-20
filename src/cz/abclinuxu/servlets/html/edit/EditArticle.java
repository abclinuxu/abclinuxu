/*
 * User: literakl
 * Date: Feb 1, 2002
 * Time: 4:48:29 PM
 */
package cz.abclinuxu.servlets.html.edit;

import cz.abclinuxu.data.*;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.exceptions.PersistanceException;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.utils.format.Format;
import cz.abclinuxu.utils.format.FormatDetector;
import org.apache.regexp.*;
import org.dom4j.*;
import org.dom4j.io.DOMWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.*;

import freemarker.ext.dom.NodeModel;

/**
 * Class for manipulation of articles.
 */
public class EditArticle implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditArticle.class);

    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_PEREX = "perex";
    public static final String PARAM_CONTENT = "content";
    public static final String PARAM_PUBLISHED = "published";
    public static final String PARAM_AUTHOR = "uid";
    public static final String PARAM_FORBID_DISCUSSIONS = "forbid_discussions";
    public static final String PARAM_FORBID_RATING = "forbid_rating";
    public static final String PARAM_RELATED_ARTICLES = "related";
    public static final String PARAM_RESOURCES = "resources";
    public static final String PARAM_THUMBNAIL = "thumbnail";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_MODERATOR = "moderator";
    public static final String PARAM_ADDRESSES = "addresses";
    public static final String PARAM_QUESTION_ID = "id";
    public static final String PARAM_NOT_ON_INDEX = "notOnIndex";
    public static final String PARAM_DESIGNATED_SECTION = "section";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_TALK_XML = "XML";
    public static final String VAR_SECTIONS = "SECTIONS";

    public static final String ACTION_ADD_ITEM = "add";
    public static final String ACTION_ADD_ITEM_STEP2 = "add2";
    public static final String ACTION_EDIT_ITEM = "edit";
    public static final String ACTION_EDIT_ITEM_STEP2 = "edit2";
    public static final String ACTION_DOCBOOK = "docbook";
    public static final String ACTION_SHOW_TALK = "showTalk";
    public static final String ACTION_TALK_EMAILS = "talkEmails";
    public static final String ACTION_TALK_EMAILS_STEP2 = "talkEmails2";
    public static final String ACTION_ADD_QUESTION = "addQuestion";
    public static final String ACTION_REMOVE_QUESTION = "removeQuestion";
    public static final String ACTION_SEND_QUESTION = "sendQuestion";
    public static final String ACTION_ADD_REPLY = "addReply";
    public static final String ACTION_SUBMIT_REPLY = "submitReply";

    private static REProgram reBreak;
    static {
        try {
            reBreak = new RECompiler().compile("<page title=\"([^\"]+)\">");
        } catch (RESyntaxException e) {
            log.fatal("Cannot compile regular expression!",e);
        }
    }

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
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

        if ( ACTION_ADD_ITEM_STEP2.equals(action) )
            return actionAddStep2(request, response, env, true);

        if ( ACTION_EDIT_ITEM.equals(action) )
            return actionEditItem(request, env);

        if ( ACTION_EDIT_ITEM_STEP2.equals(action) )
            return actionEditItem2(request, response, env);

        if (ACTION_SHOW_TALK.equals(action))
            return actionShowTalk(request, env);

        if (ACTION_ADD_QUESTION.equals(action))
            return actionAddQuestion(request, response, env);

        if (ACTION_SEND_QUESTION.equals(action))
            return actionSendQuestion(request, response, env);

        if (ACTION_ADD_REPLY.equals(action))
            return actionAddReply(request, response, env);

        if (ACTION_SUBMIT_REPLY.equals(action))
            return actionSubmitReply(request, response, env);

        if (ACTION_REMOVE_QUESTION.equals(action))
            return actionRemoveQuestion(request, response, env);

        if (ACTION_TALK_EMAILS.equals(action))
            return actionSetTalkAddresses(request, env);

        if (ACTION_TALK_EMAILS_STEP2.equals(action))
            return actionSetTalkAddressesStep2(request, response, env);

        throw new MissingArgumentException("Chybí parametr action!");
    }

    private String actionAddStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        synchronized (Constants.isoFormat) {
            params.put(PARAM_PUBLISHED,Constants.isoFormat.format(new Date()));
        }
        Persistance persistance = PersistanceFactory.getPersistance();
        Category sections = (Category) persistance.findById(new Category(Constants.CAT_ARTICLES));
        env.put(VAR_SECTIONS, sections.getChildren());
        return FMTemplateSelector.select("EditArticle","add",env,request);
    }

    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation upper = (Relation) env.get(VAR_RELATION);
        User user = (User) env.get(Constants.VAR_USER);

        Item item = new Item(0,Item.ARTICLE);
        Document document = DocumentHelper.createDocument();
        document.addElement("data");
        item.setData(document);
        item.setOwner(user.getId());

        Record record = new Record(0,Record.ARTICLE);
        document = DocumentHelper.createDocument();
        document.addElement("data");
        record.setData(document);
        record.setOwner(user.getId());

        boolean canContinue = true;
        canContinue &= setTitle(params, item, env);
        canContinue &= setAuthor(params, item, env, request);
        canContinue &= setEditor(item, env);
        canContinue &= setPerex(params, item, env);
        canContinue &= setPublishDate(params, item, env);
        canContinue &= setForbidDiscussions(params, item);
        canContinue &= setForbidRating(params, item);
        canContinue &= setThumbnail(params, item);
        canContinue &= setArticleContent(params, record, env);
        canContinue &= setRelatedArticles(params, record, env);
        canContinue &= setResources(params, record, env);
        canContinue &= setNotOnIndex(params, item);
        canContinue &= setDesignatedSection(params, item);

        if ( !canContinue ) {
            return FMTemplateSelector.select("EditArticle", "edit", env, request);
        }

        try {
            persistance.create(item);
            Relation relation = new Relation(upper.getChild(),item,upper.getId());
            persistance.create(relation);
            relation.getParent().addChildRelation(relation);

            persistance.create(record);
            Relation recordRelation = new Relation(item,record,relation.getId());
            persistance.create(recordRelation);
            recordRelation.getParent().addChildRelation(recordRelation);

            if (redirect) {
                UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
                urlUtils.redirect(response, "/show/"+relation.getId());
            } else {
                env.put(VAR_RELATION, relation);
            }
            return null;
        } catch (PersistanceException e) {
            ServletUtils.addError(Constants.ERROR_GENERIC,e.getMessage(),env, null);
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
        synchronized (Constants.isoFormat) {
            params.put(PARAM_PUBLISHED, Constants.isoFormat.format(item.getCreated()));
        }
        node = document.selectSingleNode("/data/author");
        params.put(PARAM_AUTHOR,node.getText());
        node = document.selectSingleNode("/data/forbid_discussions");
        if ( node!=null && "yes".equals(node.getText()) )
            params.put(PARAM_FORBID_DISCUSSIONS, node.getText());
        node = document.selectSingleNode("/data/forbid_rating");
        if ( node!=null && "yes".equals(node.getText()) )
            params.put(PARAM_FORBID_RATING, node.getText());
        node = document.selectSingleNode("/data/thumbnail");
        if ( node!=null )
            params.put(PARAM_THUMBNAIL, node.getText());
        params.put(PARAM_NOT_ON_INDEX, item.getSubType());
        if (relation.getUpper()==Constants.REL_ARTICLEPOOL) {
            Persistance persistance = PersistanceFactory.getPersistance();
            Category sections = (Category) persistance.findById(new Category(Constants.CAT_ARTICLES));
            env.put(VAR_SECTIONS, sections.getChildren());
            node = document.selectSingleNode("/data/section_rid");
            if (node != null)
                params.put(PARAM_DESIGNATED_SECTION, Integer.valueOf(node.getText()));
        }

        Relation child = InstanceUtils.findFirstChildRecordOfType(item,Record.ARTICLE);
        Record record = (Record) child.getChild();
        document = record.getData();

        addArticleContent(document, params);
        addLinks(document, "/data/related/link", params, PARAM_RELATED_ARTICLES);
        addLinks(document, "/data/resources/link", params, PARAM_RESOURCES);

        return FMTemplateSelector.select("EditArticle","edit",env,request);
    }

    protected String actionEditItem2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_RELATION);

        Item item = (Item) relation.getChild();
        Relation child = InstanceUtils.findFirstChildRecordOfType(item, Record.ARTICLE);
        Record record = (Record) child.getChild();

        boolean canContinue = true;
        canContinue &= setTitle(params, item, env);
        canContinue &= setAuthor(params, item, env, request);
        canContinue &= setEditor(item, env);
        canContinue &= setPerex(params, item, env);
        canContinue &= setPublishDate(params, item, env);
        canContinue &= setForbidDiscussions(params, item);
        canContinue &= setForbidRating(params, item);
        canContinue &= setThumbnail(params, item);
        canContinue &= setArticleContent(params, record, env);
        canContinue &= setRelatedArticles(params, record, env);
        canContinue &= setResources(params, record, env);
        canContinue &= setNotOnIndex(params, item);
        if (relation.getUpper()==Constants.REL_ARTICLEPOOL)
            canContinue &= setDesignatedSection(params, item);

        if ( !canContinue ) {
            return FMTemplateSelector.select("EditArticle","edit",env,request);
        }

        persistance.update(item);
        persistance.update(record);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/show/"+relation.getId());
        return null;
    }

    /**
     * If the article is not talk, converts it to talk. It also puts talk XML into environment.
     * Main page for talks.
     */
    private String actionShowTalk(HttpServletRequest request, Map env) throws Exception {
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        Document document = item.getData();

        Element talk = (Element) document.selectSingleNode("/data/talk");
        if (talk == null) {
            talk = DocumentHelper.makeElement(document, "/data/talk");
            PersistanceFactory.getPersistance().update(item);
        }
        env.put(VAR_TALK_XML, NodeModel.wrap((new DOMWriter().write(document))));

        return FMTemplateSelector.select("EditArticle", "talk", env, request);
    }

    /**
     * Displayes email addresses of this talk.
     */
    protected String actionSetTalkAddresses(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        Document document = item.getData();

        Element addresses = (Element) document.selectSingleNode("/data/talk/addresses");
        if (addresses != null) {
            StringBuffer sb = new StringBuffer();
            for (Iterator iter = addresses.elements().iterator(); iter.hasNext();) {
                Element email = (Element) iter.next();
                sb.append(email.getTextTrim());
                sb.append('\n');
            }
            params.put(PARAM_ADDRESSES, sb.toString());
            params.put(PARAM_MODERATOR, addresses.attributeValue("moderator"));
        }

        return FMTemplateSelector.select("EditArticle", "talkAddresses", env, request);
    }

    /**
     * Sets email addresses for this talk.
     */
    protected String actionSetTalkAddressesStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();

        boolean canContinue = setAddresses(params, item.getData(), env);
        if (canContinue)
            PersistanceFactory.getPersistance().update(item);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/edit/" + relation.getId()+"?action=showTalk");
        return null;
    }

    /**
     * Adds new question.
     */
    protected String actionAddQuestion(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();

        boolean canContinue = addTalkQuestion(params, item.getData(), env);
        if (canContinue)
            PersistanceFactory.getPersistance().update(item);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/edit/" + relation.getId()+"?action=showTalk");
        return null;
    }

    /**
     * Removes existing question.
     */
    protected String actionRemoveQuestion(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();

        int id = Misc.parseInt((String) params.get(PARAM_QUESTION_ID), -1);
        if (id!=-1) {
            Element question = (Element) item.getData().selectSingleNode("/data/talk/question[@id="+id+"]");
            if (question!=null) {
                question.detach();
                PersistanceFactory.getPersistance().update(item);
            }
        }

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/edit/" + relation.getId()+"?action=showTalk");
        return null;
    }

    /**
     * Sends this question by email to all addresses.
     */
    protected String actionSendQuestion(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);

        Document document = item.getData();
        Element addresses = (Element) document.selectSingleNode("/data/talk/addresses");
        if (addresses==null || addresses.elements().size()==0) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Nejsou definovány ¾ádné emailové adresy!", env, request.getSession());
            urlUtils.redirect(response, "/edit/" + relation.getId() + "?action=showTalk");
            return null;
        }

        Element question = null;
        int id = Misc.parseInt((String) params.get(PARAM_QUESTION_ID), -1);
        if (id != -1) {
            question = (Element) document.selectSingleNode("/data/talk/question[@id=" + id + "]");
            if (question == null) {
                ServletUtils.addError(Constants.ERROR_GENERIC, "Otázka "+id+" nebyla nalezena!", env, request.getSession());
                urlUtils.redirect(response, "/edit/" + relation.getId() + "?action=showTalk");
                return null;
            }
        }

        Map email = new HashMap();
        email.put(EmailSender.KEY_SUBJECT, "otazka "+id);
        email.put(EmailSender.KEY_FROM, addresses.attributeValue("moderator"));
        email.put(EmailSender.KEY_BODY, question.getText());

        for (Iterator iter = addresses.elements().iterator(); iter.hasNext();) {
            Element element = (Element) iter.next();
            email.put(EmailSender.KEY_TO, element.getTextTrim());
            EmailSender.sendEmail(email);
        }

        urlUtils.redirect(response, "/edit/" + relation.getId()+"?action=showTalk");
        return null;
    }

    /**
     * Displays form, where article admin can change the question and adds replies.
     */
    protected String actionAddReply(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();

        int id = Misc.parseInt((String) params.get(PARAM_QUESTION_ID), -1);
        if (id==-1) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Èíslo otázky "+id+" je neplatné!", env, request.getSession());
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/edit/" + relation.getId() + "?action=showTalk");
            return null;
        }

        Element question = (Element) item.getData().selectSingleNode("/data/talk/question[@id=" + id + "]");
        if (question==null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Otázka èíslo " + id + " neexistuje!", env, request.getSession());
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/edit/" + relation.getId() + "?action=showTalk");
            return null;
        }

        params.put(PARAM_NAME, question.attributeValue("name"));
        params.put(PARAM_CONTENT, question.getText());

        return FMTemplateSelector.select("EditArticle", "talkQuestion", env, request);
    }

    /**
     * Edits the question, sets replies and renders the question and its replies to originated article.
     */
    protected String actionSubmitReply(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();

        int id = Misc.parseInt((String) params.get(PARAM_QUESTION_ID), -1);
        if (id == -1) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Èíslo otázky " + id + " je neplatné!", env, request.getSession());
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/edit/" + relation.getId() + "?action=showTalk");
            return null;
        }

        Element question = (Element) item.getData().selectSingleNode("/data/talk/question[@id=" + id + "]");
        if (question == null) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Otázka èíslo " + id + " neexistuje!", env, request.getSession());
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/edit/" + relation.getId() + "?action=showTalk");
            return null;
        }

        List responders = new ArrayList();
        List responses = new ArrayList();

        for (int i=1; i<=5; i++) {
            String who = (String) params.get("responder"+i);
            String reply = (String) params.get("reply"+i);
            if (!Misc.empty(who) && !Misc.empty(reply)) {
                responders.add(who.trim());
                responses.add(reply.trim());
            }
        }

        Map renderEnv = new HashMap();
        renderEnv.put("QUESTIONER", params.get(PARAM_NAME));
        renderEnv.put("QUESTION", params.get(PARAM_CONTENT));
        renderEnv.put("RESPONDERS", responders);
        renderEnv.put("RESPONSES", responses);

        String renderedQuestion = FMUtils.executeTemplate("/include/misc/talk_question.ftl", renderEnv);

        Persistance persistance = PersistanceFactory.getPersistance();
        question.detach();
        persistance.update(item);

        Relation child = InstanceUtils.findFirstChildRecordOfType(item, Record.ARTICLE);
        Record record = (Record) child.getChild();
        Element article = (Element) record.getData().selectSingleNode("data/content");
        String content = article.getText().concat(renderedQuestion);
        article.setText(content);
        persistance.update(record);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/edit/" + relation.getId() + "?action=showTalk");
        return null;
    }

    // setters


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
    private boolean setAuthor(Map params, Item item, Map env, HttpServletRequest request) {
        User author = (User) InstanceUtils.instantiateParam(PARAM_AUTHOR, User.class, params, request);
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
            Date publish = null;
            synchronized (Constants.isoFormat) {
                publish = Constants.isoFormat.parse((String) params.get(PARAM_PUBLISHED));
            }
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
        RE regexp = new RE(reBreak, RE.MATCH_SINGLELINE);
        if ( regexp.match(content) ) {
            StringCharacterIterator stringIter = new StringCharacterIterator(content);
            String title, page; int start, end; boolean canContinue;
            DocumentHelper.makeElement(record.getData(), "data");
            Element data = record.getData().getRootElement();
            do {
                title = regexp.getParen(1);
                start = regexp.getParenEnd(0);
                canContinue = regexp.match(stringIter, start);
                end = (canContinue) ? regexp.getParenStart(0) : content.length();
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
     * Updates forbid_rating from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item article  to be updated
     * @return false, if there is a major error.
     */
    private boolean setForbidRating(Map params, Item item) {
        String content = (String) params.get(PARAM_FORBID_RATING);
        Element element = (Element) item.getData().selectSingleNode("/data/forbid_rating");
        if ( element!=null )
            element.detach();

        if ( content==null || content.length()==0 )
            return true;

        element = DocumentHelper.makeElement(item.getData(), "/data/forbid_rating");
        element.setText(content);
        return true;
    }

    /**
     * Updates thumbnail from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item article  to be updated
     * @return false, if there is a major error.
     */
    private boolean setThumbnail(Map params, Item item) {
        String content = (String) params.get(PARAM_THUMBNAIL);
        Element element = (Element) item.getData().selectSingleNode("/data/thumbnail");
        if ( element!=null )
            element.detach();

        if ( content==null || content.trim().length()==0 )
            return true;

        element = DocumentHelper.makeElement(item.getData(), "/data/thumbnail");
        element.setText(content);
        return true;
    }

    /**
     * Updates not on index attribute from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item article  to be updated
     * @return false, if there is a major error.
     */
    private boolean setNotOnIndex(Map params, Item item) {
        String content = (String) params.get(PARAM_NOT_ON_INDEX);
        if (content!=null)
            item.setSubType("NOTONINDEX");
        else
            item.setSubType(null);
        return true;
    }

    /**
     * Updates designated section from parameters.
     * Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param item   article  to be updated
     * @return false, if there is a major error.
     */
    private boolean setDesignatedSection(Map params, Item item) {
        String content = (String) params.get(PARAM_DESIGNATED_SECTION);
        Element element = DocumentHelper.makeElement(item.getData(), "/data/section_rid");
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
        return setLinks(params, record, PARAM_RELATED_ARTICLES, "/data/related", env);
    }

    /**
     * Updates resources from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param record article's record to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setResources(Map params, Record record, Map env) {
        return setLinks(params, record, PARAM_RESOURCES, "/data/resources", env);
    }

    /**
     * Updates resources from parameters. Changes are not synchronized with persistance.
     * @param params map holding request's parameters
     * @param record article's record to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setLinks(Map params, Record record, String param, String xpath, Map env) {
        String links = (String) params.get(param);
        Element resources = (Element) record.getData().selectSingleNode(xpath);
        if ( resources!=null )
            resources.detach();

        if (links==null || links.length()==0)
            return true;

        resources = DocumentHelper.makeElement(record.getData(), xpath);
        StringTokenizer stk = new StringTokenizer(links,"\n");
        String url, title, desc;
        int position;
        while ( stk.hasMoreTokens() ) {
            url = stk.nextToken();
            url = url.trim();
            if ( url.length()==0 )
                continue; // whitespaces on empty line
            if ( ! stk.hasMoreTokens() ) {
                ServletUtils.addError(param, "Chybí titulek pro URL "+url+"!", env, null);
                return false;
            }

            title = stk.nextToken();
            title = title.trim();

            position = title.indexOf('|');
            if ( position!=-1 ) {
                desc = title.substring(position+1);
                title = title.substring(0, position);
            } else
                desc = null;

            Element link = resources.addElement("link");
            link.setText(title);
            link.addAttribute("url",url);
            if (desc!=null)
                link.addAttribute("description", desc);
        }
        return true;
    }

    /**
     * Adds related articles or resources to map params under given name.
     */
    private void addLinks(Document document, String xpath, Map params, String var) {
        List nodes = document.selectNodes(xpath);
        if ( nodes!=null && nodes.size()>0 ) {
            StringBuffer sb = new StringBuffer();
            String  desc;
            for ( Iterator iter = nodes.iterator(); iter.hasNext(); ) {
                Element element = (Element) iter.next();
                sb.append(element.attributeValue("url"));
                sb.append("\n");
                sb.append(element.getText());
                desc = element.attributeValue("description");
                if (desc!=null) {
                    sb.append('|');
                    sb.append(desc);
                }
                sb.append('\n');
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

    /**
     * Updates talk addresses from parameters. Changes are not synchronized with persistance.
     *
     * @param params map holding request's parameters
     * @param document article's record to be updated
     * @return false, if there is a major error.
     */
    private boolean setAddresses(Map params, Document document, Map env) {
        String s = (String) params.get(PARAM_ADDRESSES);
        if (s==null || s.length()==0) {
            ServletUtils.addError(PARAM_ADDRESSES, "Zadejte adresy úèastníkù diskuse!", env, null);
            return false;
        }

        String moderator = (String) params.get(PARAM_MODERATOR);
        if (moderator==null || moderator.length()==0) {
            ServletUtils.addError(PARAM_MODERATOR, "Zadejte adresu moderátora diskuse!", env, null);
            return false;
        }

        Element talk = (Element) document.selectSingleNode("/data/talk");
        Element addresses = talk.element("addresses");
        if (addresses!=null)
            addresses.elements().clear();
        else
            addresses = talk.addElement("addresses");

        addresses.addAttribute("moderator", moderator);

        StringTokenizer stk = new StringTokenizer(s);
        while (stk.hasMoreTokens()) {
            String email = stk.nextToken();
            addresses.addElement("email").setText(email);
        }

        return true;
    }

    /**
     * Updates one question to talk from parameters. Changes are not synchronized with persistance.
     *
     * @param params map holding request's parameters
     * @param document article's record to be updated
     * @return false, if there is a major error.
     */
    private boolean addTalkQuestion(Map params, Document document, Map env) {
        String name = (String) params.get(PARAM_NAME);
        String content = (String) params.get(PARAM_CONTENT);

        if (name==null || name.length()==0) {
            ServletUtils.addError(PARAM_NAME, "Vyplòte jméno tazatele!", env, null);
            return false;
        }
        if (content==null || content.length()==0) {
            ServletUtils.addError(PARAM_CONTENT, "Zadejte otázku!", env, null);
            return false;
        }

        int lastId = 1;
        Element talk = (Element) document.selectSingleNode("/data/talk");
        Attribute attrib = talk.attribute("lastid");
        if (attrib!=null) {
            lastId = Misc.parseInt(attrib.getValue(), 1);
            attrib.setText(Integer.toString(++lastId));
        } else
            talk.addAttribute("lastid", "1");

        Element question = talk.addElement("question");
        question.setText(content);
        question.addAttribute("name", name);
        question.addAttribute("id", Integer.toString(lastId));

        return true;
    }
}
