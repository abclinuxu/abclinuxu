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
 * <dt><code>PARAM_POLL</code></dt>
 * <dd>Poll to be edited.</dd>
 * <dt><code>PARAM_TEXT</code></dt>
 * <dd>Question of the poll</dd>
 * <dt><code>PARAM_TYPE</code></dt>
 * <dd>Constant defining type of the poll.</dd>
 * <dt><code>PARAM_URL</code></dt>
 * <dd>When user votes, redirect page to this URL.</dd>
 * <dt><code>PARAM_VOTE_ID</code></dt>
 * <dd>User's choice(s), when he votes.</dd>
 * </dl>
 */
public class EditPoll extends AbcServlet {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EditPoll.class);

    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_POLL = "pollId";
    public static final String PARAM_TEXT = "text";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_MULTICHOICE = "multichoice";
    public static final String PARAM_CLOSED = "closed";
    public static final String PARAM_CHOICES = "choices";
    public static final String PARAM_COUNTS = "counts";
    public static final String PARAM_URL = "url";
    public static final String PARAM_VOTE_ID = "voteId";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT2 = "edit2";
    public static final String ACTION_VOTE = "vote";

    public static final String VAR_RELATION = "relation";
    public static final String VAR_POLL = "POLL";

    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Relation relation = null;
        Poll poll = null;

        String tmp = (String) params.get(EditPoll.PARAM_RELATION);
        if ( tmp!=null && tmp.length()>0 ) {
            int id = Integer.parseInt(tmp);
            relation = (Relation) PersistanceFactory.getPersistance().findById(new Relation(id));
            ctx.put(EditPoll.VAR_RELATION,relation);
        }

        tmp = (String) params.get(EditPoll.PARAM_POLL);
        if ( tmp!=null && tmp.length()>0 ) {
            int id = Integer.parseInt(tmp);
            poll = (Poll) PersistanceFactory.getPersistance().findById(new Poll(id));
            ctx.put(EditPoll.VAR_POLL,poll);
        }

        String action = (String) params.get(AbcServlet.PARAM_ACTION);

        if ( action==null || action.equals(EditPoll.ACTION_ADD) ) {
            if ( relation==null ) throw new Exception("Chyb� parametr relationId!");
            int rights = checkAccess(relation.getChild(),AbcServlet.METHOD_ADD,ctx);
             switch (rights) {
                 case AbcServlet.LOGIN_REQUIRED: return getTemplate("login.vm");
                 case AbcServlet.USER_INSUFFICIENT_RIGHTS: addError(AbcServlet.GENERIC_ERROR,"Va�e pr�va nejsou dostate�n� pro tuto operaci!",ctx, null);
                 default: return getTemplate("add/poll.vm");
             }

        } else if ( action.equals(EditPoll.ACTION_VOTE) ) {
            if ( poll==null ) throw new Exception("Chyb� parametr pollId!");
            return actionVote(request,response,ctx);

        } else if ( action.equals(EditPoll.ACTION_ADD_STEP2) ) {
            if ( relation==null ) throw new Exception("Chyb� parametr relationId!");
            int rights = checkAccess(relation.getChild(),AbcServlet.METHOD_ADD,ctx);
            switch (rights) {
                case AbcServlet.LOGIN_REQUIRED: return getTemplate("login.vm");
                case AbcServlet.USER_INSUFFICIENT_RIGHTS: {
                    addError(AbcServlet.GENERIC_ERROR,"Va�e pr�va nejsou dostate�n� pro tuto operaci!",ctx, null);
                    return getTemplate("add/poll.vm");
                }
                default: return actionAddStep2(request,response,ctx);
            }

        } else if ( action.equals(EditPoll.ACTION_EDIT) ) {
            if ( relation==null ) throw new Exception("Chyb� parametr relationId!");
            if ( poll==null ) throw new Exception("Chyb� parametr pollId!");
            int rights = checkAccess(poll,AbcServlet.METHOD_EDIT,ctx);
            switch (rights) {
                case AbcServlet.LOGIN_REQUIRED: return getTemplate("login.vm");
                case AbcServlet.USER_INSUFFICIENT_RIGHTS: addError(AbcServlet.GENERIC_ERROR,"Va�e pr�va nejsou dostate�n� pro tuto operaci!",ctx,null);
                default: return getTemplate("edit/poll.vm");
            }

        } else if ( action.equals(EditPoll.ACTION_EDIT2) ) {
            if ( relation==null ) throw new Exception("Chyb� parametr relationId!");
            if ( poll==null ) throw new Exception("Chyb� parametr pollId!");
            int rights = checkAccess(poll,AbcServlet.METHOD_EDIT,ctx);
            switch (rights) {
                case AbcServlet.LOGIN_REQUIRED: return getTemplate("login.vm");
                case AbcServlet.USER_INSUFFICIENT_RIGHTS: addError(AbcServlet.GENERIC_ERROR,"Va�e pr�va nejsou dostate�n� pro tuto operaci!",ctx,null);
                default: return actionEditStep2(request,response,ctx);
            }

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
            addError(EditPoll.PARAM_TEXT,"Nezadal jste ot�zku!",ctx, null);
            error = true;
        }

        for (Iterator iter = choices.iterator(); iter.hasNext();) {
            String choice = (String) iter.next();
            if ( choice==null || choice.length()==0 ) iter.remove();
        }

        if ( choices.size()<2 ) {
            addError(EditPoll.PARAM_CHOICES,"Vypl�te minim�ln� dv� volby!",ctx, null);
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

    /**
     * Final step for editing of poll
     */
    protected Template actionEditStep2(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);

        int type = Poll.SURVEY;
        Relation upperRelation = (Relation) ctx.get(EditPoll.VAR_RELATION);
        Poll poll = (Poll) ctx.get(EditPoll.VAR_POLL);

        String tmp = (String) params.get(EditPoll.PARAM_TYPE);
        if ( "rating".equals(tmp) ) {
            poll.setType(Poll.RATING);
        } else {
            poll.setType(Poll.SURVEY);
        }

        tmp = (String) params.get(EditPoll.PARAM_MULTICHOICE);
        poll.setMultiChoice( "yes".equals(tmp) );

        tmp = (String) params.get(EditPoll.PARAM_CLOSED);
        poll.setClosed( ("yes".equals(tmp)) );

        tmp = (String) params.get(EditPoll.PARAM_TEXT);
        if ( tmp!=null && tmp.length()>0 ) {
            poll.setText(tmp);
        }

        List choices = (List) params.get(EditPoll.PARAM_CHOICES);
        List counts = (List) params.get(EditPoll.PARAM_COUNTS);
        PollChoice[] pollChoices = poll.getChoices();

        int max = choices.size();
        for ( int i=0; i<max; i++ ) {
            tmp = (String) choices.get(i);
            if ( tmp!=null && tmp.length()>0 ) pollChoices[i].setText(tmp);
            tmp = (String) counts.get(i);
            if ( tmp!=null && tmp.length()>0 ) {
                try { pollChoices[i].setCount(Integer.parseInt(tmp)); } catch (NumberFormatException e) {}
            }
        }
        PersistanceFactory.getPersistance().update(poll);

        redirect("/ViewRelation?relationId="+upperRelation.getId(),response,ctx);
        return null;
    }

    /**
     * Voting
     */
    protected Template actionVote(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Poll poll = (Poll) ctx.get(EditPoll.VAR_POLL);
        String url = (String) params.get(EditPoll.PARAM_URL);

        if ( url==null || url.length()==0 ) {
            addError(AbcServlet.GENERIC_ERROR,"Chyb� parametr url!",ctx,request.getSession());
        }

        try {
            String[] values = request.getParameterValues(EditPoll.PARAM_VOTE_ID);
            int max = 1;
            if ( poll.isMultiChoice() ) max = values.length;
            for (int i = 0; i < values.length; i++) {
                String tmp = values[i];
                int voteId = Integer.parseInt(tmp);
                PersistanceFactory.getPersistance().incrementCounter(poll.getChoices()[voteId]);
            }
        } catch (Exception e) {
            log.error("Vote bug: ",e);
            addError(AbcServlet.GENERIC_ERROR,"Nevybral jste ��dnou volbu!",ctx,request.getSession());
            redirect(url,response,ctx);
            return null;
        }

        redirect(url,response,ctx);
        return null;
    }
}
