/*
 * User: literakl
 * Date: Jan 24, 2002
 * Time: 12:33:39 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.edit;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import javax.servlet.http.*;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.security.Guard;

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
    public static final String PARAM_FROM = "from";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT2 = "edit2";
    public static final String ACTION_VOTE = "vote";

    public static final String VAR_RELATION = "relation";
    public static final String VAR_POLL = "POLL";

    /** this prefix will be used for marking user, that has already voted */
    static final String COOKIE_PREFIX = "P_";


    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Relation relation = null;
        Poll poll = null;

        relation = (Relation) instantiateParam(EditPoll.PARAM_RELATION,Relation.class,params);
        if ( relation!=null ) {
            relation = (Relation) PersistanceFactory.getPersistance().findById(relation);
            ctx.put(EditPoll.VAR_RELATION,relation);
        }

        poll = (Poll) instantiateParam(EditPoll.PARAM_POLL,Poll.class,params);
        if ( poll!=null ) {
            poll = (Poll) PersistanceFactory.getPersistance().findById(poll);
            ctx.put(EditPoll.VAR_POLL,poll);
        }

        String action = (String) params.get(AbcServlet.PARAM_ACTION);

        if ( action==null || action.equals(EditPoll.ACTION_ADD) ) {
            if ( relation==null ) throw new Exception("Chybí parametr relationId!");
            int rights = Guard.check((User)ctx.get(VAR_USER),relation.getChild(),Guard.OPERATION_ADD,Poll.class);
             switch (rights) {
                 case Guard.ACCESS_LOGIN: return getTemplate("view/login.vm");
                 case Guard.ACCESS_DENIED: addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx, null);
                 default: return getTemplate("add/poll.vm");
             }

        } else if ( action.equals(EditPoll.ACTION_VOTE) ) {
            if ( poll==null ) throw new Exception("Chybí parametr pollId!");
            return actionVote(request,response,ctx);

        } else if ( action.equals(EditPoll.ACTION_ADD_STEP2) ) {
            if ( relation==null ) throw new Exception("Chybí parametr relationId!");
            int rights = Guard.check((User)ctx.get(VAR_USER),relation.getChild(),Guard.OPERATION_ADD,Poll.class);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return getTemplate("view/login.vm");
                case Guard.ACCESS_DENIED: {
                    addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx, null);
                    return getTemplate("add/poll.vm");
                }
                default: return actionAddStep2(request,response,ctx);
            }

        } else if ( action.equals(EditPoll.ACTION_EDIT) ) {
            if ( relation==null ) throw new Exception("Chybí parametr relationId!");
            if ( poll==null ) throw new Exception("Chybí parametr pollId!");
            int rights = Guard.check((User)ctx.get(VAR_USER),poll,Guard.OPERATION_EDIT,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return getTemplate("view/login.vm");
                case Guard.ACCESS_DENIED: addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx,null);
                default: return getTemplate("edit/poll.vm");
            }

        } else if ( action.equals(EditPoll.ACTION_EDIT2) ) {
            if ( relation==null ) throw new Exception("Chybí parametr relationId!");
            if ( poll==null ) throw new Exception("Chybí parametr pollId!");
            int rights = Guard.check((User)ctx.get(VAR_USER),poll,Guard.OPERATION_EDIT,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return getTemplate("view/login.vm");
                case Guard.ACCESS_DENIED: addError(AbcServlet.GENERIC_ERROR,"Va¹e práva nejsou dostateèná pro tuto operaci!",ctx,null);
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

        StringBuffer sb = new StringBuffer("/ViewRelation?relationId=");
        sb.append(upperRelation.getId());
        tmp = request.getParameter(EditPoll.PARAM_FROM);
        if ( tmp!=null ) {
            sb.append("&from=");
            sb.append(tmp);
        }

        UrlUtils.redirect(sb.toString(),response,ctx);
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

        UrlUtils.redirect("/ViewRelation?relationId="+upperRelation.getId(),response,ctx);
        return null;
    }

    /**
     * Voting
     */
    protected Template actionVote(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Poll poll = (Poll) ctx.get(EditPoll.VAR_POLL);
        String url = (String) params.get(EditPoll.PARAM_URL);
        int max = 0;

        if ( url==null || url.length()==0 ) {
            addError(AbcServlet.GENERIC_ERROR,"Chybí parametr url!",ctx,request.getSession());
        }

        String[] values = request.getParameterValues(EditPoll.PARAM_VOTE_ID);
        if ( values==null ) {
            addError(AbcServlet.GENERIC_ERROR,"Nevybral jste ¾ádnou volbu!",ctx,request.getSession());
        } else {
            max = values.length;
            if ( ! poll.isMultiChoice() ) max = 1;
        }

        if ( hasAlreadyVoted(request,poll,ctx) ) {
            addError(AbcServlet.GENERIC_ERROR,"U¾ jste jednou volil!",ctx,request.getSession());
        } else if ( max>0 ) {
            try {
                for (int i = 0; i<max; i++) {
                    String tmp = values[i];
                    int voteId = Integer.parseInt(tmp);
                    PersistanceFactory.getPersistance().incrementCounter(poll.getChoices()[voteId]);
                }
                addMessage("Vá¹ hlas do ankety byl pøijat.",ctx,request.getSession());
                markAlreadyVoted(request,response,poll,ctx);
            } catch (Exception e) {
                log.error("Vote bug: ",e);
                addError(AbcServlet.GENERIC_ERROR,"Omlouváme se, ale nastala chyba.",ctx,request.getSession());
            }
        }

        UrlUtils.redirect(url,response,ctx);
        return null;
    }

    /**
     * Checks, whether this user has already voted for this poll.
     * @return true, if user is trying to vote again
     */
    boolean hasAlreadyVoted(HttpServletRequest request, Poll poll, Context context) {
        String searched = COOKIE_PREFIX+poll.getId();

        HttpSession session = request.getSession();
        if ( session.getAttribute(searched)!=null ) return true;

        Cookie[] cookies = request.getCookies();
        for (int i = 0; cookies!=null && i<cookies.length; i++) {
            Cookie cookie = cookies[i];
            if ( cookie.getName().equals(searched) ) return true;
        }

        // check IP address in future
        return false;
    }

    /**
     * Marks user, that he has already voted for this poll.
     */
    void markAlreadyVoted(HttpServletRequest request, HttpServletResponse response, Poll poll, Context context) {
        String searched = COOKIE_PREFIX+poll.getId();

        HttpSession session = request.getSession();
        session.setAttribute(searched,new Boolean(true));

        Cookie cookie = new Cookie(searched,""+poll.getId());
        cookie.setPath("/");
        cookie.setMaxAge(1*30*24*3600); // one month
        response.addCookie(cookie);
    }
}
