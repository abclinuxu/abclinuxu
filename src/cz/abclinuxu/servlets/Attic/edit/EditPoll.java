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

import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.security.Guard;
import cz.abclinuxu.utils.InstanceUtils;

import java.util.*;

/**
 * Servlet for manipulation with Polls.
 * @todo create rating of articles, records and replies
 */
public class EditPoll extends AbcFMServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditPoll.class);

    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_POLL = "pollId";
    public static final String PARAM_QUESTION = "question";
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

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_POLL = "POLL";

    /** this prefix will be used for marking user, which has already voted */
    static final String COOKIE_PREFIX = "P_";


    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION,Relation.class,params);
        if ( relation!=null ) {
            relation = (Relation) persistance.findById(relation);
            env.put(VAR_RELATION,relation);
        }

        Poll poll = (Poll) InstanceUtils.instantiateParam(PARAM_POLL,Poll.class,params);
        if ( poll!=null ) {
            poll = (Poll) persistance.findById(poll);
            env.put(EditPoll.VAR_POLL,poll);
        }

        if ( action==null || action.equals(ACTION_ADD) ) {
            if ( relation==null ) throw new Exception("Chybí parametr relationId!");
            int rights = Guard.check(user,relation.getChild(),Guard.OPERATION_ADD,Poll.class);
             switch (rights) {
                 case Guard.ACCESS_LOGIN: return FMTemplateSelector.select("ViewUser","login",env,request);
                 case Guard.ACCESS_DENIED: return FMTemplateSelector.select("ViewUser","forbidden",env,request);
                 default: return FMTemplateSelector.select("EditPoll","add",env,request);
             }

        } else if ( action.equals(ACTION_VOTE) ) {
            if ( poll==null ) throw new Exception("Chybí parametr pollId!");
            return actionVote(request,response,env);

        } else if ( action.equals(ACTION_ADD_STEP2) ) {
            if ( relation==null ) throw new Exception("Chybí parametr relationId!");
            int rights = Guard.check(user,relation.getChild(),Guard.OPERATION_ADD,Poll.class);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return FMTemplateSelector.select("ViewUser","login",env,request);
                case Guard.ACCESS_DENIED: return FMTemplateSelector.select("ViewUser","forbidden",env,request);
                default: return actionAddStep2(request,response,env);
            }

        } else if ( action.equals(ACTION_EDIT) ) {
            if ( relation==null ) throw new Exception("Chybí parametr relationId!");
            if ( poll==null ) throw new Exception("Chybí parametr pollId!");
            int rights = Guard.check(user,poll,Guard.OPERATION_EDIT,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return FMTemplateSelector.select("ViewUser","login",env,request);
                case Guard.ACCESS_DENIED: return FMTemplateSelector.select("ViewUser","forbidden",env,request);
                default: return FMTemplateSelector.select("EditPoll","edit",env,request);
            }

        } else if ( action.equals(ACTION_EDIT2) ) {
            if ( relation==null ) throw new Exception("Chybí parametr relationId!");
            if ( poll==null ) throw new Exception("Chybí parametr pollId!");
            int rights = Guard.check(user,poll,Guard.OPERATION_EDIT,null);
            switch (rights) {
                case Guard.ACCESS_LOGIN: return FMTemplateSelector.select("ViewUser","login",env,request);
                case Guard.ACCESS_DENIED: return FMTemplateSelector.select("ViewUser","forbidden",env,request);
                default: return actionEditStep2(request,response,env);
            }

        }
        return FMTemplateSelector.select("EditPoll","add",env,request);
    }

    /**
     * Creates new poll
     */
    protected String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        boolean error = false;

        int type = Poll.SURVEY;
        boolean multiChoice = false;
        String text = (String) params.get(PARAM_QUESTION);
        List choices = (List) params.get(PARAM_CHOICES);
        Relation upperRelation = (Relation) env.get(VAR_RELATION);

        String tmp = (String) params.get(PARAM_TYPE);
        if ( "rating".equals(tmp) ) type = Poll.RATING;
        tmp = (String) params.get(PARAM_MULTICHOICE);
        if ( "yes".equals(tmp) ) multiChoice = true;

        if ( text==null || text.length()==0 ) {
            ServletUtils.addError(PARAM_QUESTION,"Nezadal jste otázku!",env, null);
            error = true;
        }

        for (Iterator iter = choices.iterator(); iter.hasNext();) {
            String choice = (String) iter.next();
            if ( choice==null || choice.length()==0 ) iter.remove();
        }

        if ( choices.size()<2 ) {
            ServletUtils.addError(PARAM_CHOICES,"Vyplòte minimálnì dvì volby!",env, null);
            error = true;
        }

        if ( error )
            return FMTemplateSelector.select("EditPoll","add",env,request);

        Poll poll = new Poll(0,type);
        poll.setText(text);
        poll.setMultiChoice(multiChoice);
        poll.setClosed(false);

        List pollChoices = new ArrayList(choices.size());
        for (Iterator iter = choices.iterator(); iter.hasNext();) {
            PollChoice choice = new PollChoice((String) iter.next());
            pollChoices.add(choice);
        }
        poll.setChoices(pollChoices);

        persistance.create(poll);
        Relation relation = new Relation(upperRelation.getChild(),poll,upperRelation.getId());
        persistance.create(relation);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/ViewRelation?relationId="+upperRelation.getId());
        return null;
    }

    /**
     * Final step for editing of poll
     */
    protected String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        int type = Poll.SURVEY;
        Relation upperRelation = (Relation) env.get(VAR_RELATION);
        Poll poll = (Poll) env.get(VAR_POLL);

        String tmp = (String) params.get(PARAM_TYPE);
        if ( "rating".equals(tmp) ) {
            poll.setType(Poll.RATING);
        } else {
            poll.setType(Poll.SURVEY);
        }

        tmp = (String) params.get(PARAM_MULTICHOICE);
        poll.setMultiChoice( "yes".equals(tmp) );

        tmp = (String) params.get(PARAM_CLOSED);
        poll.setClosed( ("yes".equals(tmp)) );

        tmp = (String) params.get(PARAM_QUESTION);
        if ( tmp!=null && tmp.length()>0 ) {
            poll.setText(tmp);
        }

        List choices = (List) params.get(PARAM_CHOICES);
        List counts = (List) params.get(PARAM_COUNTS);
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

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/ViewRelation?relationId="+upperRelation.getId());
        return null;
    }

    /**
     * Voting
     */
    protected String actionVote(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);

        Poll poll = (Poll) env.get(VAR_POLL);
        String url = (String) params.get(PARAM_URL);
        int max = 0;

        if ( poll.isClosed() ) {
            ServletUtils.addError(Constants.ERROR_GENERIC,"Litujeme, ale tato anketa je ji¾ uzavøena!",env,request.getSession());
            urlUtils.redirect(response, url);
            return null;
        }

        if ( url==null || url.length()==0 ) {
            log.error("U ankety "+poll.getId()+" chybí parametr url!");
            url = "/Index";
        }

        String[] values = request.getParameterValues(PARAM_VOTE_ID);
        if ( values==null ) {
            ServletUtils.addError(Constants.ERROR_GENERIC,"Nevybral jste ¾ádnou volbu!",env,request.getSession());
        } else {
            max = values.length;
            if ( ! poll.isMultiChoice() ) max = 1;
        }

        if ( hasAlreadyVoted(request,poll,env) ) {
            ServletUtils.addError(Constants.ERROR_GENERIC,"U¾ jste jednou volil!",env,request.getSession());
        } else if ( max>0 ) {
            try {
                for (int i = 0; i<max; i++) {
                    String tmp = values[i];
                    int voteId = Integer.parseInt(tmp);
                    PersistanceFactory.getPersistance().incrementCounter(poll.getChoices()[voteId]);
                }
                ServletUtils.addMessage("Vá¹ hlas do ankety byl pøijat.",env,request.getSession());
                markAlreadyVoted(request,response,poll,env);
            } catch (Exception e) {
                log.error("Vote bug: ",e);
                ServletUtils.addError(Constants.ERROR_GENERIC,"Omlouváme se, ale nastala chyba.",env,request.getSession());
            }
        }

        urlUtils.redirect(response, url);
        return null;
    }

    /**
     * Checks, whether this user has already voted for this poll.
     * @return true, if user is trying to vote again
     */
    boolean hasAlreadyVoted(HttpServletRequest request, Poll poll, Map env) {
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
    void markAlreadyVoted(HttpServletRequest request, HttpServletResponse response, Poll poll, Map env) {
        String searched = COOKIE_PREFIX+poll.getId();

        HttpSession session = request.getSession();
        session.setAttribute(searched,new Boolean(true));

        Cookie cookie = new Cookie(searched,""+poll.getId());
        cookie.setPath("/");
        cookie.setMaxAge(1*30*24*3600); // one month
        response.addCookie(cookie);
    }
}
