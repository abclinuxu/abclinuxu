/*
 * User: literakl
 * Date: Jan 15, 2002
 * Time: 9:24:05 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.view.SelectRelation;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.security.Guard;
import cz.abclinuxu.utils.InstanceUtils;

import org.dom4j.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Class for removing relations or creating links.
 */
public class EditRelation extends AbcFMServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditRelation.class);

    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_PREFIX = "prefix";
    public static final String PARAM_SELECTED = SelectRelation.PARAM_SELECTED;

    public static final String VAR_CURRENT = "CURRENT";
    public static final String VAR_SELECTED = "SELECTED";
    public static final String VAR_PARENTS = "PARENTS";

    public static final String ACTION_LINK = "add";
    public static final String ACTION_LINK_STEP2 = "add2";
    public static final String ACTION_REMOVE = "remove";
    public static final String ACTION_REMOVE_STEP2 = "remove2";
    public static final String ACTION_MOVE = "move";


    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        String action = (String) params.get(PARAM_ACTION);
        User user = (User) env.get(Constants.VAR_USER);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION,Relation.class,params);
        if ( relation!=null ) {
            relation = (Relation) persistance.findById(relation);
            env.put(VAR_CURRENT,relation);
        }

        if ( action==null || action.equals(ACTION_LINK) ) {
            int rights = Guard.check(user,relation.getChild(),Guard.OPERATION_ADD,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return FMTemplateSelector.select("ViewUser","login",env,request);
                case Guard.ACCESS_DENIED: return FMTemplateSelector.select("ViewUser","forbidden",env,request);
                default: return actionLinkStep1(request,env);
            }

        } else if ( action.equals(ACTION_LINK_STEP2) ) {
            int rights = Guard.check(user,relation.getChild(),Guard.OPERATION_ADD,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return FMTemplateSelector.select("ViewUser","login",env,request);
                case Guard.ACCESS_DENIED: return FMTemplateSelector.select("ViewUser","forbidden",env,request);
                default: return actionLinkStep2(request,response,env);
            }

        } else if ( action.equals(ACTION_REMOVE) ) {
            int rights = Guard.check(user,relation,Guard.OPERATION_REMOVE,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return FMTemplateSelector.select("ViewUser","login",env,request);
                case Guard.ACCESS_DENIED: return FMTemplateSelector.select("ViewUser","forbidden",env,request);
                default: return actionRemove1(request,env);
            }

        } else if ( action.equals(ACTION_REMOVE_STEP2) ) {
            int rights = Guard.check(user,relation,Guard.OPERATION_REMOVE,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return FMTemplateSelector.select("ViewUser","login",env,request);
                case Guard.ACCESS_DENIED: return FMTemplateSelector.select("ViewUser","forbidden",env,request);
                default: return actionRemove2(request,response,env);
            }

        } else if ( action.equals(ACTION_MOVE) ) {
            int rights = Guard.check(user,relation,Guard.OPERATION_EDIT,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return FMTemplateSelector.select("ViewUser","login",env,request);
                case Guard.ACCESS_DENIED: return FMTemplateSelector.select("ViewUser","forbidden",env,request);
                default: return actionMove(request,response,env);
            }

        }

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/Index");
        return null;
    }

    protected String actionLinkStep1(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_SELECTED,Relation.class,params);
        if ( relation!=null ) {
            relation = (Relation) persistance.findById(relation);
            env.put(VAR_SELECTED,relation);
        }
        return FMTemplateSelector.select("EditRelation","add",env,request);
    }

    protected String actionLinkStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Relation parent = (Relation) env.get(VAR_CURRENT);
        Relation child = (Relation) InstanceUtils.instantiateParam(PARAM_SELECTED,Relation.class,params);
        persistance.synchronize(child);

        Relation relation = new Relation();
        relation.setParent(parent.getChild());
        relation.setChild(child.getChild());
        relation.setUpper(parent.getId());

        String tmp = (String) params.get(PARAM_NAME);
        if ( tmp!=null && tmp.length()>0 ) {
            Document document = DocumentHelper.createDocument();
            Element root = document.addElement("data");
            root.addElement("name").addText(tmp);
            relation.setData(document);
        }

        persistance.create(relation);

        String prefix = (String)params.get(PARAM_PREFIX);
        UrlUtils urlUtils = new UrlUtils(prefix, response);
        urlUtils.redirect(response, "/ViewRelation?relationId="+parent.getId());
        return null;
    }

    protected String actionRemove1(HttpServletRequest request, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_CURRENT);

        Relation[] parents = persistance.findByExample(new Relation(null,relation.getChild(),0));
        env.put(VAR_PARENTS,parents);
        return FMTemplateSelector.select("EditRelation","remove",env,request);
    }

    protected String actionRemove2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_CURRENT);

        persistance.remove(relation);
        String url = null;
        String prefix = (String) params.get(PARAM_PREFIX);
        if ( prefix!=null ) {
            url = prefix.concat("/ViewRelation?relationId="+relation.getUpper());
        } else url = "/Index";

        UrlUtils urlUtils = new UrlUtils(prefix, response);
        urlUtils.redirect(response, url);
        return null;
    }

    /**
     * Called, when user selects destination in SelectRelation. It replaces parent in relation with child
     * in destination.
     */
    protected String actionMove(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) env.get(VAR_CURRENT);
        int originalUpper = relation.getUpper();

        Relation destination = (Relation) InstanceUtils.instantiateParam(PARAM_SELECTED,Relation.class,params);
        persistance.synchronize(destination);

        relation.setParent(destination.getChild());
        relation.setUpper(destination.getId());
        persistance.update(relation);

        String url = null;
        String prefix = (String) params.get(PARAM_PREFIX);
        if ( prefix!=null ) {
            if ( originalUpper==Constants.REL_FORUM )
                url = "/diskuse.jsp";
            else
                url = prefix.concat("/ViewRelation?relationId="+relation.getUpper());
        } else url = "/Index";

        UrlUtils urlUtils = new UrlUtils("", response);
        urlUtils.redirect(response, url);
        return null;
    }
}
