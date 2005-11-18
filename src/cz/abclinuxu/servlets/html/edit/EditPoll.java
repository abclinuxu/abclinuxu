/*
 *  Copyright (C) 2005 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
package cz.abclinuxu.servlets.html.edit;

import javax.servlet.http.*;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.security.AccessKeeper;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.exceptions.AccessDeniedException;
import cz.abclinuxu.AbcException;

import java.util.*;

/**
 * Servlet for manipulation with Polls.
 */
public class EditPoll implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditPoll.class);

    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_POLL = "pollId";
    public static final String PARAM_QUESTION = "question";
    public static final String PARAM_MULTICHOICE = "multichoice";
    public static final String PARAM_CLOSED = "closed";
    public static final String PARAM_CHOICES = "choices";
    public static final String PARAM_URL = "url";
    public static final String PARAM_VOTE_ID = "voteId";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT2 = "edit2";
    public static final String ACTION_VOTE = "vote";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_POLL = "POLL";


    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Chybí parametr rid!");
        relation = (Relation) persistance.findById(relation);
        env.put(VAR_RELATION, relation);

        if ( ACTION_VOTE.equals(action) )
            return actionVote(request, response, env);

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        boolean allowed = user.hasRole(Roles.POLL_ADMIN);
        if (!allowed) {
            GenericObject obj = relation.getChild();
            if (obj instanceof GenericDataObject)
                allowed = ((GenericDataObject)obj).getOwner() == user.getId();
            else if (obj instanceof Poll)
                allowed = ((Poll)obj).getOwner() == user.getId();
        }
        if ( !allowed )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if ( ACTION_ADD.equals(action) )
            return FMTemplateSelector.select("EditPoll", "add", env, request);

        if ( ACTION_ADD_STEP2.equals(action) )
            return actionAddStep2(request, response, env, true);

        Poll poll = (Poll) persistance.findById(relation.getChild());
        env.put(EditPoll.VAR_POLL, poll);

        if ( ACTION_EDIT.equals(action) )
            return FMTemplateSelector.select("EditPoll", "edit", env, request);

        if ( ACTION_EDIT2.equals(action) )
            return actionEditStep2(request, response, env);

        throw new MissingArgumentException("Chybí parametr action!");
    }

    // todo - kontrolovat validitu HTML

    /**
     * Creates new poll
     */
    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        boolean error = false;

        boolean multiChoice = false;
        User user = (User) env.get(Constants.VAR_USER);
        String text = (String) params.get(PARAM_QUESTION);
        List choices = (List) params.get(PARAM_CHOICES);
        Relation upperRelation = (Relation) env.get(VAR_RELATION);

        String tmp = (String) params.get(PARAM_MULTICHOICE);
        if ( "yes".equals(tmp) )
            multiChoice = true;

        if ( text==null || text.length()==0 ) {
            ServletUtils.addError(PARAM_QUESTION,"Nezadal jste otázku!",env, null);
            error = true;
        }

        for (Iterator iter = choices.iterator(); iter.hasNext();) {
            String choice = (String) iter.next();
            if ( choice==null || choice.length()==0 )
                iter.remove();
        }

        if ( choices.size()<1 ) {
            ServletUtils.addError(PARAM_CHOICES, "Vyplòte minimálnì jednu volbu!", env, null);
            error = true;
        }

        String url = (String) params.get(PARAM_URL);
        if (upperRelation.getId()==Constants.REL_POLLS && url!=null && url.length()>0) {
            try {
                url = UrlUtils.PREFIX_POLLS + "/" + URLManager.enforceLastURLPart(url);
                url = URLManager.protectFromDuplicates(url);
            } catch (AbcException e) {
                ServletUtils.addError(PARAM_URL, e.getMessage(), env, null);
                error = true;
            }
        } else if (upperRelation.getUrl()!=null) {
            url = upperRelation.getUrl() + "/anketa";
            url = URLManager.protectFromDuplicates(url);
        }

        if ( error )
            return FMTemplateSelector.select("EditPoll","add",env,request);

        Poll poll = new Poll(0);
        poll.setText(text);
        poll.setMultiChoice(multiChoice);
        poll.setClosed(false);
        poll.setOwner(user.getId());

        int i = 0;
        List pollChoices = new ArrayList(choices.size());
        for (Iterator iter = choices.iterator(); iter.hasNext();) {
            PollChoice choice = new PollChoice((String) iter.next());
            choice.setId(i++);
            pollChoices.add(choice);
        }
        poll.setChoices(pollChoices);
        persistance.create(poll);

        Relation relation = new Relation(upperRelation.getChild(),poll,upperRelation.getId());
        if (url != null && url.length() > 0)
            relation.setUrl(url);
        persistance.create(relation);
        relation.getParent().addChildRelation(relation);

        if (relation.getUpper()==Constants.REL_POLLS)
            FeedGenerator.updatePolls();

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        if (url==null)
            url = UrlUtils.PREFIX_POLLS + "/show/" + relation.getId();
        urlUtils.redirect(response, url);
        return null;
    }

    /**
     * Final step for editing of poll
     */
    protected String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        Relation relation = (Relation) env.get(VAR_RELATION);
        Poll poll = (Poll) env.get(VAR_POLL);

        String tmp = (String) params.get(PARAM_MULTICHOICE);
        poll.setMultiChoice( "yes".equals(tmp) );

        tmp = (String) params.get(PARAM_CLOSED);
        poll.setClosed( ("yes".equals(tmp)) );

        tmp = (String) params.get(PARAM_QUESTION);
        if ( tmp!=null && tmp.length()>0 ) {
            poll.setText(tmp);
        }

        List choices = (List) params.get(PARAM_CHOICES);
        List choicesList = Arrays.asList(poll.getChoices());

        for ( int i=0; i<10; i++ ) {
            tmp = (String) choices.get(i);
            if ( tmp==null || tmp.length()==0 )
                continue;
            PollChoice choice = (PollChoice) choicesList.get(i);
            if (choice==null) {
                choice = new PollChoice(tmp);
                choice.setPoll(poll.getId());
                choice.setId(i);
                choicesList.add(choice);
            }
            else
                choice.setText(tmp);
        }

        poll.setChoices(choicesList);
        Persistance persistance = PersistanceFactory.getPersistance();
        persistance.update(poll);

        if (relation.getUpper() == Constants.REL_POLLS)
            FeedGenerator.updatePolls();

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/show/"+relation.getId());
        return null;
    }

    /**
     * Voting
     */
    protected String actionVote(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);
        Relation relation = (Relation) env.get(VAR_RELATION);
        String url = (String) params.get(PARAM_URL);
        int max = 0;

        Poll poll = (Poll) persistance.findById(relation.getChild());
        env.put(EditPoll.VAR_POLL, poll);

        if ( poll.isClosed() ) {
            ServletUtils.addError(Constants.ERROR_GENERIC,"Litujeme, ale tato anketa je ji¾ uzavøena!",env,request.getSession());
            urlUtils.redirect(response, url);
            return null;
        }

        if ( url==null || url.length()==0 ) {
            log.error("U ankety "+poll.getId()+" chybí parametr url!");
            url = "/";
        }

        String[] values = request.getParameterValues(PARAM_VOTE_ID);
        if ( values==null ) {
            ServletUtils.addError(Constants.ERROR_GENERIC,"Nevybral jste ¾ádnou volbu!",env,request.getSession());
        } else {
            max = values.length;
            if ( ! poll.isMultiChoice() )
                max = 1;
        }

        if ( max>0 ) {
            try {
                AccessKeeper.checkAccess(relation, user, "vote", request, response);
                PollChoice[] choices = poll.getChoices();
                List votesFor = new ArrayList();
                for ( int i = 0; i<max; i++ ) {
                    String tmp = values[i];
                    if (tmp==null || tmp.length()==0)
                        continue;
                    int voteId = Integer.parseInt(tmp);
                    if (voteId<choices.length)
                        votesFor.add(choices[voteId]);
                }

                persistance.incrementPollChoicesCounter(votesFor);

                ServletUtils.addMessage("Vá¹ hlas do ankety byl pøijat.", env, request.getSession());
            } catch (AccessDeniedException e) {
                if (e.isIpAddressBlocked())
                    ServletUtils.addError(Constants.ERROR_GENERIC, "Z této IP adresy se u¾ volilo. Zkuste to pozdìji.", env, request.getSession());
                else
                    ServletUtils.addError(Constants.ERROR_GENERIC, "U¾ jste jednou volil!", env, request.getSession());
            } catch (Exception e) {
                log.error("Vote bug: ", e);
                ServletUtils.addError(Constants.ERROR_GENERIC, "Omlouváme se, ale nastala chyba.", env, request.getSession());
            }
        }

        urlUtils.redirect(response, url);
        return null;
    }
}
