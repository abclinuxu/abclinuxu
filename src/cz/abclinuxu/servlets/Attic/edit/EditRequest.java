/*
 * User: literakl
 * Date: Feb 4, 2002
 * Time: 2:06:36 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.AbcVelocityServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.utils.template.VelocityTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.security.Guard;
import cz.abclinuxu.utils.InstanceUtils;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class EditRequest extends AbcVelocityServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditRequest.class);

    public static final String PARAM_AUTHOR = "author";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_TEXT = "text";
    public static final String PARAM_REQUEST = "requestId";

    public static final String VAR_REQUEST_RELATION = "REQUEST";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_DELIVER = "deliver";

    protected String process(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Map params = (Map) request.getAttribute(AbcVelocityServlet.ATTRIB_PARAMS);
        String action = (String) params.get(AbcVelocityServlet.PARAM_ACTION);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_REQUEST,Relation.class,params);
        if ( relation!=null ) ctx.put(VAR_REQUEST_RELATION,relation);

        if ( action==null || action.equals(ACTION_ADD) ) {
            return actionAdd(request,response,ctx);

        } else if ( action.equals(ACTION_DELETE) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation,Guard.OPERATION_REMOVE,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return VelocityTemplateSelector.selectTemplate(request,ctx,"EditUser","login");
                case Guard.ACCESS_DENIED: return VelocityTemplateSelector.selectTemplate(request,ctx,"EditUser","forbidden");
                default: return actionDelete(request,response,ctx);
            }

        } else if ( action.equals(ACTION_DELIVER) ) {
            int rights = Guard.check((User)ctx.get(VAR_USER),relation,Guard.OPERATION_REMOVE,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return VelocityTemplateSelector.selectTemplate(request,ctx,"EditUser","login");
                case Guard.ACCESS_DENIED: return VelocityTemplateSelector.selectTemplate(request,ctx,"EditUser","forbidden");
                default: return actionDeliver(request,response,ctx);
            }
        }
        return actionAdd(request,response,ctx);
    }

    protected String actionAdd(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        User user = (User) ctx.get(VAR_USER);
        Map params = (Map) request.getAttribute(AbcVelocityServlet.ATTRIB_PARAMS);

        String author = (String) params.get(PARAM_AUTHOR);
        String email = (String) params.get(PARAM_EMAIL);
        String text = (String) params.get(PARAM_TEXT);
        boolean error = false;

        if ( author==null || author.length()==0 ) {
            ServletUtils.addError(PARAM_AUTHOR,"Slu¹ností je pøedstavit se.",ctx,null);
            error = true;
        }

        if ( email==null || email.length()==0 ) {
            ServletUtils.addError(PARAM_EMAIL,"Nevím, kam poslat vyrozumìní.",ctx,null);
            error = true;
        } else if ( email.length()<6 || email.indexOf('@')==-1 ) {
            ServletUtils.addError(PARAM_EMAIL,"Nelatný email!.",ctx,null);
            error = true;
        }

        if ( text==null || text.length()==0 ) {
            ServletUtils.addError(PARAM_TEXT,"Co potøebujete?",ctx,null);
            error = true;
        }

        if ( error ) return VelocityTemplateSelector.selectTemplate(request,ctx,"EditRequest","view");

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

        ServletUtils.addMessage("Vá¹ po¾adavek byl pøijat.",ctx,request.getSession());

        UrlUtils.redirect(response, "/clanky/ViewRelation?relationId="+Constants.REL_REQUESTS, ctx);
        return null;
    }

    protected String actionDelete(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();

        Relation relation = (Relation) ctx.get(VAR_REQUEST_RELATION);
        persistance.synchronize(relation);
        persistance.remove(relation);
        ServletUtils.addMessage("Po¾adavek byl smazán.",ctx,request.getSession());

        UrlUtils.redirect(response, "/clanky/ViewRelation?relationId="+Constants.REL_REQUESTS, ctx);
        return null;
    }

    protected String actionDeliver(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        User user = (User) ctx.get(VAR_USER);
        Persistance persistance = PersistanceFactory.getPersistance();

        Relation relation = (Relation) ctx.get(VAR_REQUEST_RELATION);
        persistance.synchronize(relation);
        Item req = (Item) relation.getChild();
        persistance.synchronize(req);

        String requestor = req.getData().selectSingleNode("data/email").getText();
        String text = "Hotovo.\n"+user.getName()+"\n\n\nVas pozadavek\n\n";
        text = text.concat(req.getData().selectSingleNode("data/text").getText());
        Email.sendEmail(user.getEmail(),requestor,"Vas pozadavek na AbcLinuxu byl vyrizen",text);

        persistance.remove(relation);
        ServletUtils.addMessage("Po¾adavek byl vyøízen.",ctx,request.getSession());

        UrlUtils.redirect(response, "/clanky/ViewRelation?relationId="+Constants.REL_REQUESTS, ctx);
        return null;
    }
}
