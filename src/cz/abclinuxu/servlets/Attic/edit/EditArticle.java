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
import cz.abclinuxu.security.Guard;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.exceptions.MissingArgumentException;

import org.dom4j.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Class for manipulation of articles.
 * @todo implement flow of checks: Author enters article, revisor corrects grammar, editor approves article and selects publish date.
 */
public class EditArticle extends AbcFMServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditArticle.class);

    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_PEREX = "perex";
    public static final String PARAM_CONTENT = "content";
    public static final String PARAM_PUBLISHED = "published";
    public static final String PARAM_AUTHOR = "authorId";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_AUTHORS = "AUTHORS";

    public static final String ACTION_ADD_ITEM = "add";
    public static final String ACTION_ADD_ITEM_STEP2 = "add2";
    public static final String ACTION_EDIT_ITEM = "edit";
    public static final String ACTION_EDIT_ITEM_STEP2 = "edit2";


    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        String action = (String) params.get(PARAM_ACTION);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION,Relation.class,params);
        if ( relation==null )
            throw new MissingArgumentException("Chybí parametr relationId!");

        persistance.synchronize(relation);
        persistance.synchronize(relation.getChild());
        env.put(VAR_RELATION,relation);

        if ( ACTION_ADD_ITEM.equals(action) ) {
            int rights = Guard.check((User)env.get(Constants.VAR_USER),relation.getChild(),Guard.OPERATION_ADD,Item.class);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return FMTemplateSelector.select("ViewUser","login",env,request);
                case Guard.ACCESS_DENIED: return FMTemplateSelector.select("ViewUser","forbidden",env,request);
                default: return actionAddStep1(request,env);
            }

        } else if ( action.equals(ACTION_ADD_ITEM_STEP2) ) {
            int rights = Guard.check((User)env.get(Constants.VAR_USER),relation.getChild(),Guard.OPERATION_ADD,Item.class);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return FMTemplateSelector.select("ViewUser","login",env,request);
                case Guard.ACCESS_DENIED: return FMTemplateSelector.select("ViewUser","forbidden",env,request);
                default: return actionAddStep2(request,response,env);
            }

        } else if ( action.equals(ACTION_EDIT_ITEM) ) {
            int rights = Guard.check((User)env.get(Constants.VAR_USER),relation.getChild(),Guard.OPERATION_EDIT,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return FMTemplateSelector.select("ViewUser","login",env,request);
                case Guard.ACCESS_DENIED: return FMTemplateSelector.select("ViewUser","forbidden",env,request);
                default: return actionEditItem(request,env);
            }

        } else if ( action.equals(ACTION_EDIT_ITEM_STEP2) ) {
            int rights = Guard.check((User)env.get(Constants.VAR_USER),relation.getChild(),Guard.OPERATION_EDIT,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return FMTemplateSelector.select("ViewUser","login",env,request);
                case Guard.ACCESS_DENIED: return FMTemplateSelector.select("ViewUser","forbidden",env,request);
                default: return actionEditItem2(request,response,env);
            }

        }
        return actionAddStep1(request,env);
    }

    private String actionAddStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        params.put(PARAM_PUBLISHED,Constants.isoFormat.format(new Date()));
        addAuthors(env);
        return FMTemplateSelector.select("EditArticle","add",env,request);
    }

    protected String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation upper = (Relation) env.get(VAR_RELATION);
        User user = (User) env.get(Constants.VAR_USER);

        boolean error = false;
        Date publish = null;

        String name = (String) params.get(PARAM_TITLE);
        if ( name==null || name.length()==0 ) {
            ServletUtils.addError(PARAM_TITLE,"Vyplòte titulek èlánku!",env,null); error = true;
        }

        /** todo: support for author not listed in section Authors */
        User author = (User) InstanceUtils.instantiateParam(PARAM_AUTHOR,User.class,params);
        if ( author==null ) {
            ServletUtils.addError(PARAM_AUTHOR,"Vyberte autora!",env,null); error = true;
        } else {
            persistance.synchronize(author);
        }

        String perex = (String) params.get(PARAM_PEREX);
        if ( perex==null || perex.length()==0 ) {
            ServletUtils.addError(PARAM_PEREX,"Vyplòte popis èlánku!",env,null); error = true;
        }

        String content = (String) params.get(PARAM_CONTENT);
        if ( content==null || content.length()==0 ) {
            ServletUtils.addError(PARAM_CONTENT,"Vyplòte obsah èlánku!",env,null); error = true;
        }

        try {
            publish = Constants.isoFormat.parse((String) params.get(PARAM_PUBLISHED));
        } catch (ParseException e) {
            ServletUtils.addError(PARAM_PUBLISHED,"Správný formát je 2002-02-10 06:22",env,null); error = true;
        }

        if ( error ) {
            addAuthors(env);
            return actionAddStep1(request,env);
        }

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        root.addElement("name").addText(name);
        root.addElement("author").addText(""+author.getId());
        root.addElement("editor").addText(""+user.getId());
        root.addElement("revisor").addText(""+user.getId());
        root.addElement("perex").addText(perex);


        Item item = new Item(0,Item.ARTICLE);
        item.setData(document);
        item.setOwner(user.getId());
        item.setCreated(publish);

        document = DocumentHelper.createDocument();
        root = document.addElement("data");
        root.addElement("content").addText(content);
        root.addElement("part").addText("1");

        Record record = new Record(0,Record.ARTICLE);
        record.setData(document);
        record.setOwner(user.getId());

        try {
            persistance.create(item);
            Relation relation = new Relation(upper.getChild(),item,upper.getId());
            persistance.create(relation);
            persistance.create(record);
            persistance.create(new Relation(item,record,relation.getId()));

            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
            urlUtils.redirect(response, "/ViewRelation?relationId="+relation.getId());
            return null;
        } catch (PersistanceException e) {
            ServletUtils.addError(Constants.ERROR_GENERIC,e.getMessage(),env, null);
            return actionAddStep1(request,env);
        }
    }

    protected String actionEditItem(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Relation relation = (Relation) env.get(VAR_RELATION);
        Item item = (Item) relation.getChild();
        Document document = item.getData();

        Node node = document.selectSingleNode("data/name");
        params.put(PARAM_TITLE,node.getText());
        node = document.selectSingleNode("data/perex");
        if ( node!=null ) params.put(PARAM_PEREX,node.getText());
        params.put(PARAM_PUBLISHED,item.getCreated());
        node = document.selectSingleNode("data/author");
        params.put(PARAM_AUTHOR,node.getText());

        Relation child = InstanceUtils.findFirstChildRecordOfType(item,Record.ARTICLE);
        Record record = (Record) child.getChild();
        node = record.getData().selectSingleNode("data/content");
        params.put(PARAM_CONTENT,node.getText());

        addAuthors(env);
        return FMTemplateSelector.select("EditArticle","edit",env,request);
    }

    protected String actionEditItem2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation upper = (Relation) env.get(VAR_RELATION);

        boolean error = false;
        Date publish = null;

        String name = (String) params.get(PARAM_TITLE);
        if ( name==null || name.length()==0 ) {
            ServletUtils.addError(PARAM_TITLE,"Vyplòte titulek èlánku!",env,null); error = true;
        }

        /** todo: support for author not listed in section Authors */
        User author = (User) InstanceUtils.instantiateParam(PARAM_AUTHOR,User.class,params);
        if ( author==null ) {
            ServletUtils.addError(PARAM_AUTHOR,"Vyberte autora!",env,null); error = true;
        } else {
            persistance.synchronize(author);
        }

        String perex = (String) params.get(PARAM_PEREX);
        if ( perex==null || perex.length()==0 ) {
            ServletUtils.addError(PARAM_PEREX,"Vyplòte popis èlánku!",env,null); error = true;
        }

        String content = (String) params.get(PARAM_CONTENT);
        if ( content==null || content.length()==0 ) {
            ServletUtils.addError(PARAM_CONTENT,"Vyplòte obsah èlánku!",env,null); error = true;
        }

        try {
            publish = Constants.isoFormat.parse((String) params.get(PARAM_PUBLISHED));
        } catch (ParseException e) {
            ServletUtils.addError(PARAM_PUBLISHED,"Správný formát je 2002-02-10 06:22",env,null); error = true;
        }

        if ( error ) {
            addAuthors(env);
            return FMTemplateSelector.select("EditArticle","edit",env,request);
        }

        Item item = (Item) upper.getChild();
        item.setCreated(publish);

        Document document = item.getData();
        DocumentHelper.makeElement(document,"data/name").setText(name);
        DocumentHelper.makeElement(document,"data/author").setText(Integer.toString(author.getId()));
        DocumentHelper.makeElement(document,"data/perex").setText(perex);
        persistance.update(item);

        Relation child = InstanceUtils.findFirstChildRecordOfType(item,Record.ARTICLE);
        Record record = (Record) child.getChild();
        DocumentHelper.makeElement(record.getData(),"data/content").setText(content);
        persistance.update(record);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/ViewRelation?relationId="+upper.getId());
        return null;
    }

    /**
     * Adds list of authors (User) to env in VAR_AUTHORS.
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
}
