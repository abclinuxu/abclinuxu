/*
 * User: literakl
 * Date: Jan 24, 2002
 * Time: 12:33:39 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.edit;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.PersistanceFactory;

import java.util.*;

/**
 * Servlet for manipulation with Polls.
 * <p><u>Parameters used by EditCategory</u>
 * <dl>
 * <dt><code>PARAM_RELATION</code></dt>
 * <dd>Relation, where child is/will be parent for this Poll.</dd>
 * </dl>
 */
public class EditPoll extends AbcServlet {
    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_TEXT = "text";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_MULTICHOICE = "multichoice";
    public static final String PARAM_CHOICES = "choices";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT2 = "edit2";

    public static final String VAR_RELATION = "relation";

    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);
        Relation relation = null;

        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        String tmp = (String) params.get(EditCategory.PARAM_RELATION);
        if ( tmp!=null && tmp.length()>0 ) {
            int relationId = Integer.parseInt(tmp);
            relation = (Relation) PersistanceFactory.getPersistance().findById(new Relation(relationId));
            ctx.put(EditPoll.VAR_RELATION,relation);
        } else throw new Exception("Chybí parametr relationId!");

        String action = (String) params.get(AbcServlet.PARAM_ACTION);

        if ( action==null || action.equals(EditPoll.ACTION_ADD) ) {
           int rights = checkAccess(relation.getChild(),AbcServlet.METHOD_ADD,ctx);
            switch (rights) {
                case AbcServlet.LOGIN_REQUIRED: return getTemplate("login.vm");
                case AbcServlet.USER_INSUFFICIENT_RIGHTS: addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx, null);
                default: return getTemplate("add/poll.vm");
            }

        } else if ( action.equals(EditPoll.ACTION_ADD_STEP2) ) {
            int rights = checkAccess(relation.getChild(),AbcServlet.METHOD_ADD,ctx);
            switch (rights) {
                case AbcServlet.LOGIN_REQUIRED: return getTemplate("login.vm");
                case AbcServlet.USER_INSUFFICIENT_RIGHTS: {
                    addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx, null);
                    return getTemplate("add/poll.vm");
                }
                default: return actionAddStep2(request,response,ctx);
            }

        } else if ( action.equals(EditPoll.ACTION_EDIT) ) {
//            int rights = checkAccess(category,AbcServlet.METHOD_EDIT,ctx);
//            switch (rights) {
//                case AbcServlet.LOGIN_REQUIRED: return getTemplate("login.vm");
//            case AbcServlet.USER_INSUFFICIENT_RIGHTS: addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx);
//                default: return actionEditStep1(request,ctx);
//            }

        } else if ( action.equals(EditPoll.ACTION_EDIT2) ) {
//            int rights = checkAccess(category,AbcServlet.METHOD_EDIT,ctx);
//            switch (rights) {
//                case AbcServlet.LOGIN_REQUIRED: return getTemplate("login.vm");
//            case AbcServlet.USER_INSUFFICIENT_RIGHTS: addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx);
//                default: return actionEditStep2(request,response,ctx);
//            }

        }
        return getTemplate("add/poll.vm");
    }

    /**
     * Creates new poll
     */
    protected Template actionAddStep2(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        boolean error = false;

        int type = Poll.SURVEY;
        boolean multiChoice = false;
        String text = (String) params.get(EditPoll.PARAM_TEXT);
        List choices = (List) params.get(EditPoll.PARAM_CHOICES);
        Relation upperRelation = (Relation) ctx.get(EditPoll.VAR_RELATION);

        String tmp = (String) params.get(EditPoll.PARAM_TYPE);
        if ( "rating".equals(tmp) ) type = Poll.RATING;
        tmp = (String) params.get(EditPoll.PARAM_MULTICHOICE);
        if ( "yes".equals(tmp) ) multiChoice = true;

        if ( text==null || text.length()==0 ) {
            addError(EditPoll.PARAM_TEXT,"Nezadal jste otázku!",ctx, null);
            error = true;
        }

        for (Iterator iter = choices.iterator(); iter.hasNext();) {
            String choice = (String) iter.next();
            if ( choice==null || choice.length()==0 ) iter.remove();
        }

        if ( choices.size()<2 ) {
            addError(EditPoll.PARAM_CHOICES,"Vyplòte minimálnì dvì volby!",ctx, null);
            error = true;
        }

        if ( error ) return getTemplate("add/poll.vm");

        Poll poll = new Poll(0,type);
        poll.setText(text);
        poll.setMultiChoice(multiChoice);

        List pollChoices = new ArrayList(choices.size());
        for (Iterator iter = choices.iterator(); iter.hasNext();) {
            PollChoice choice = new PollChoice((String) iter.next());
            pollChoices.add(choice);
        }
        poll.setChoices(pollChoices);

        PersistanceFactory.getPersistance().create(poll);
        Relation relation = new Relation(upperRelation.getChild(),poll,upperRelation.getId());
        PersistanceFactory.getPersistance().create(relation);

        redirect("/ViewRelation?relationId="+upperRelation.getId(),response,ctx);
        return null;
    }
}
