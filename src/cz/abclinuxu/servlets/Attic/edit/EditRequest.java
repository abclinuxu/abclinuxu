/*
 * User: literakl
 * Date: Feb 4, 2002
 * Time: 2:06:36 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.AbcVelocityServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.utils.template.VelocityTemplateSelector;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.security.Guard;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.email.EmailSender;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class EditRequest extends AbcFMServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditRequest.class);

    public static final String PARAM_AUTHOR = "author";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_TEXT = "text";
    public static final String PARAM_REQUEST = "requestId";

    public static final String VAR_REQUEST_RELATION = "REQUEST";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_DELIVER = "deliver";


    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) request.getAttribute(AbcVelocityServlet.ATTRIB_PARAMS);
        String action = (String) params.get(AbcVelocityServlet.PARAM_ACTION);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_REQUEST,Relation.class,params);
        if ( relation!=null ) env.put(VAR_REQUEST_RELATION,relation);

        if ( action==null || action.equals(ACTION_ADD) ) {
            return actionAdd(request,response,env);

        } else if ( action.equals(ACTION_DELETE) ) {
            int rights = Guard.check((User)env.get(Constants.VAR_USER),relation,Guard.OPERATION_REMOVE,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return FMTemplateSelector.select("ViewUser","login",env,request);
                case Guard.ACCESS_DENIED: return FMTemplateSelector.select("ViewUser","forbidden",env,request);
                default: return actionDelete(request,response,env);
            }

        } else if ( action.equals(ACTION_DELIVER) ) {
            int rights = Guard.check((User)env.get(Constants.VAR_USER),relation,Guard.OPERATION_REMOVE,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return FMTemplateSelector.select("ViewUser","login",env,request);
                case Guard.ACCESS_DENIED: return FMTemplateSelector.select("ViewUser","forbidden",env,request);
                default: return actionDeliver(request,response,env);
            }
        }
        return actionAdd(request,response,env);
    }

    protected String actionAdd(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        Map params = (Map) request.getAttribute(AbcVelocityServlet.ATTRIB_PARAMS);

        String author = (String) params.get(PARAM_AUTHOR);
        String email = (String) params.get(PARAM_EMAIL);
        String text = (String) params.get(PARAM_TEXT);
        boolean error = false;

        if ( author==null || author.length()==0 ) {
            ServletUtils.addError(PARAM_AUTHOR,"Slu¹ností je pøedstavit se.",env,null);
            error = true;
        }

        if ( email==null || email.length()==0 ) {
            ServletUtils.addError(PARAM_EMAIL,"Nevím, kam poslat vyrozumìní.",env,null);
            error = true;
        } else if ( email.length()<6 || email.indexOf('@')==-1 ) {
            ServletUtils.addError(PARAM_EMAIL,"Nelatný email!.",env,null);
            error = true;
        }

        if ( text==null || text.length()==0 ) {
            ServletUtils.addError(PARAM_TEXT,"Co potøebujete?",env,null);
            error = true;
        }

        if ( error ) return FMTemplateSelector.select("EditRequest","view",env,request);

        Item req = new Item(0,Item.REQUEST);
        if ( user!=null ) req.setOwner(user.getId());

        Document document = DocumentHelper.createDocument();
        DocumentHelper.makeElement(document,"/data/author").addText(author);
        DocumentHelper.makeElement(document,"/data/email").addText(email);
        DocumentHelper.makeElement(document,"/data/text").addText(text);

        req.setData(document);

        Persistance persistance = PersistanceFactory.getPersistance();
        persistance.create(req);
        Relation relation = new Relation(new Category(Constants.CAT_REQUESTS),req,Constants.REL_REQUESTS);
        persistance.create(relation);

        ServletUtils.addMessage("Vá¹ po¾adavek byl pøijat.",env,request.getSession());

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/clanky/ViewRelation?relationId="+Constants.REL_REQUESTS);
        return null;
    }

    protected String actionDelete(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();

        Relation relation = (Relation) env.get(VAR_REQUEST_RELATION);
        persistance.synchronize(relation);
        persistance.remove(relation);
        ServletUtils.addMessage("Po¾adavek byl smazán.",env,request.getSession());

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/clanky/ViewRelation?relationId="+Constants.REL_REQUESTS);
        return null;
    }

    protected String actionDeliver(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        Persistance persistance = PersistanceFactory.getPersistance();

        Relation relation = (Relation) env.get(VAR_REQUEST_RELATION);
        persistance.synchronize(relation);
        Item req = (Item) relation.getChild();
        persistance.synchronize(req);

        String requestor = req.getData().selectSingleNode("data/email").getText();
        String text = "Hotovo.\n"+user.getName()+"\n\n\nVas pozadavek\n\n";
        text = text.concat(req.getData().selectSingleNode("data/text").getText());
        EmailSender.sendEmail(user.getEmail(),requestor,"Vas pozadavek na AbcLinuxu byl vyrizen",text);

        persistance.remove(relation);
        ServletUtils.addMessage("Po¾adavek byl vyøízen.",env,request.getSession());

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/clanky/ViewRelation?relationId="+Constants.REL_REQUESTS);
        return null;
    }
}
