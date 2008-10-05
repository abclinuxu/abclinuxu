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
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.security.AccessKeeper;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.parser.safehtml.SafeHTMLGuard;
import cz.abclinuxu.utils.parser.clean.HtmlPurifier;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.exceptions.AccessDeniedException;
import cz.abclinuxu.AbcException;

import java.util.*;

import org.htmlparser.util.ParserException;

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
    public static final String PARAM_PREVIEW = "preview";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT2 = "edit2";
    public static final String ACTION_VOTE = "vote";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_POLL = "POLL";


    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        String action = (String) params.get(PARAM_ACTION);

        if (ServletUtils.handleMaintainance(request, env)) {
            response.sendRedirect(response.encodeRedirectURL("/"));
            return null;
        }

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Chybí parametr rid!");
        Tools.sync(relation);
        env.put(VAR_RELATION, relation);

        if ( ACTION_VOTE.equals(action) ) {
            ActionProtector.ensureContract(request, EditPoll.class, false, false, false, true);
            return actionVote(request, response, env);
        }

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

        if ( ACTION_ADD_STEP2.equals(action) ) {
            ActionProtector.ensureContract(request, EditPoll.class, true, true, true, false);
            return actionAddStep2(request, response, env, true);
        }

        Poll poll = (Poll) relation.getChild();
        env.put(EditPoll.VAR_POLL, poll);

        if ( ACTION_EDIT.equals(action) )
            return FMTemplateSelector.select("EditPoll", "edit", env, request);

        if ( ACTION_EDIT2.equals(action) ) {
            ActionProtector.ensureContract(request, EditPoll.class, true, true, true, false);
            return actionEditStep2(request, response, env);
        }

        throw new MissingArgumentException("Chybí parametr action!");
    }

    // todo - kontrolovat validitu HTML

    /**
     * Creates new poll
     */
    public String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env, boolean redirect) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) env.get(Constants.VAR_USER);
        Relation upperRelation = (Relation) env.get(VAR_RELATION);

        Poll poll = new Poll(0);
        poll.setClosed(false);
        poll.setOwner(user.getId());

        boolean canContinue = true;
        canContinue &= setQuestion(params, poll, env);
        canContinue &= setChoices(params, poll, env);
        canContinue &= setMultichoice(params, poll);

        String url = (String) params.get(PARAM_URL);
        boolean globalPoll = upperRelation.getId() == Constants.REL_POLLS;

        if (globalPoll && url != null && url.length() > 0) {
            try {
                url = UrlUtils.PREFIX_POLLS + "/" + URLManager.enforceRelativeURL(url);
                url = URLManager.protectFromDuplicates(url);
            } catch (AbcException e) {
                ServletUtils.addError(PARAM_URL, e.getMessage(), env, null);
                canContinue = false;
            }
        } else if (upperRelation.getUrl() != null) {
            url = upperRelation.getUrl() + "/anketa";
            url = URLManager.protectFromDuplicates(url);
        }

        if ( ! canContinue  || params.get(PARAM_PREVIEW) != null ) {
            if (! canContinue)
                params.remove(PARAM_PREVIEW);
            else
                env.put(VAR_POLL, new Relation(upperRelation.getChild(), poll, upperRelation.getId()));
            return FMTemplateSelector.select("EditPoll","add",env,request);
        }

        persistence.create(poll);

        Relation relation = new Relation(upperRelation.getChild(), poll, upperRelation.getId());
        if (url != null && url.length() > 0)
            relation.setUrl(url);
        persistence.create(relation);
        relation.getParent().addChildRelation(relation);

        if (globalPoll) {
            EditDiscussion.createEmptyDiscussion(relation, user, persistence);
            FeedGenerator.updatePolls();
        }

        if (redirect) {
            UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);

            if (globalPoll)
                urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
            else
                urlUtils.redirect(response, urlUtils.getRelationUrl(upperRelation));
        } else {
            env.put(VAR_RELATION, relation);
        }
        return null;
    }

    /**
     * Final step for editing of poll
     */
    protected String actionEditStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        Relation relation = (Relation) env.get(VAR_RELATION);
        Poll poll = (Poll) env.get(VAR_POLL);

        boolean canContinue = true;
        canContinue &= setQuestion(params, poll, env);
        canContinue &= setMultichoice(params, poll);
        canContinue &= setClosed(params, poll);

        if (! canContinue)
            return FMTemplateSelector.select("EditPoll", "edit", env, request);

        List choices = (List) params.get(PARAM_CHOICES);
        List choicesList = Arrays.asList(poll.getChoices());
        for ( int i = 0; i < 10 && i<choices.size(); i++ ) {
            String tmp = (String) choices.get(i);
            if (tmp == null || tmp.length() == 0)
                continue;

            PollChoice choice = (PollChoice) choicesList.get(i);
            if (choice == null) {
                choice = new PollChoice(tmp);
                choice.setPoll(poll.getId());
                choice.setId(i);
                choicesList.add(choice);
            }
            else
                choice.setText(tmp);
        }
        poll.setChoices(choicesList);

        Persistence persistence = PersistenceFactory.getPersistence();
        persistence.update(poll);

        if (relation.getUpper() == Constants.REL_POLLS)
            FeedGenerator.updatePolls();

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, urlUtils.getRelationUrl(relation));
        return null;
    }

    /**
     * Voting
     */
    protected String actionVote(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        User user = (User) env.get(Constants.VAR_USER);
        Relation relation = (Relation) env.get(VAR_RELATION);
        String url = (String) params.get(PARAM_URL);
        int max = 0;

        Poll poll = (Poll) persistence.findById(relation.getChild());
        env.put(EditPoll.VAR_POLL, poll);

        if ( poll.isClosed() ) {
            ServletUtils.addError(Constants.ERROR_GENERIC,"Litujeme, ale tato anketa je již uzavřena.",env,request.getSession());
            urlUtils.redirect(response, url);
            return null;
        }

        if ( url==null || url.length()==0 ) {
            log.error("U ankety "+poll.getId()+" chybí parametr url!");
            url = "/";
        }

        String[] values = request.getParameterValues(PARAM_VOTE_ID);
        if ( values==null ) {
            ServletUtils.addError(Constants.ERROR_GENERIC,"Nevybral jste žádnou volbu.",env,request.getSession());
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
                    int voteId = Misc.parseInt(tmp, -1);
                    if (voteId == -1)
                        continue;
                    if (voteId < choices.length)
                        votesFor.add(choices[voteId]);
                }

                if (votesFor.size() > 0)
                    persistence.incrementPollChoicesCounter(votesFor);

                ServletUtils.addMessage("Váš hlas do ankety byl přijat.", env, request.getSession());
            } catch (AccessDeniedException e) {
                if (e.isIpAddressBlocked())
                    ServletUtils.addError(Constants.ERROR_GENERIC, "Z této IP adresy se už volilo. Zkuste to později.", env, request.getSession());
                else
                    ServletUtils.addError(Constants.ERROR_GENERIC, "Už jste jednou volil.", env, request.getSession());
            } catch (Exception e) {
                log.error("Vote bug: ", e);
                ServletUtils.addError(Constants.ERROR_GENERIC, "Omlouváme se, ale nastala chyba.", env, request.getSession());
            }
        }

        urlUtils.redirect(response, url);
        return null;
    }


    /* ******** setters ********* */

    /**
     * Updates multichoice flag from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param poll poll to be updated
     * @return false, if there is a major error.
     */
    private boolean setMultichoice(Map params, Poll poll) {
        String tmp = (String) params.get(PARAM_MULTICHOICE);
        poll.setMultiChoice("yes".equals(tmp));
        return true;
    }

    /**
     * Updates closed flag from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param poll poll to be updated
     * @return false, if there is a major error.
     */
    private boolean setClosed(Map params, Poll poll) {
        String tmp = (String) params.get(PARAM_CLOSED);
        poll.setClosed("yes".equals(tmp));
        return true;
    }

    /**
     * Updates question from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param poll poll to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setQuestion(Map params, Poll poll, Map env) {
        String text = (String) params.get(PARAM_QUESTION);
        if (text == null || text.length() == 0) {
            ServletUtils.addError(PARAM_QUESTION, "Nezadal jste otázku!", env, null);
            return false;
        }

        try {
            text = HtmlPurifier.clean(text);
            SafeHTMLGuard.check(text);
        } catch (ParserException e) {
            log.error("ParseException on '" + text + "'", e);
            ServletUtils.addError(PARAM_QUESTION, e.getMessage(), env, null);
            return false;
        } catch (Exception e) {
            ServletUtils.addError(PARAM_QUESTION, e.getMessage(), env, null);
            return false;
        }
        poll.setText(text);
        return true;
    }

    /**
     * Updates choices from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param poll poll to be updated
     * @param env environment
     * @return false, if there is a major error.
     */
    private boolean setChoices(Map params, Poll poll, Map env) {
        List pollChoices = new ArrayList();
        List choices = Tools.asList(params.get(PARAM_CHOICES));
        int i = 0;
        for (Iterator iter = choices.iterator(); iter.hasNext();) {
            String choice = (String) iter.next();
            if (choice == null || choice.length() == 0)
                continue;

            try {
                choice = HtmlPurifier.clean(choice);
                SafeHTMLGuard.check(choice);
            } catch (ParserException e) {
                log.error("ParseException on '" + choice + "'", e);
                ServletUtils.addError(PARAM_QUESTION, e.getMessage(), env, null);
                return false;
            } catch (Exception e) {
                ServletUtils.addError(PARAM_QUESTION, e.getMessage(), env, null);
                return false;
            }

            PollChoice pollChoice = new PollChoice(choice);
            pollChoice.setId(i++);
            pollChoices.add(pollChoice);
        }
        poll.setChoices(pollChoices);

        if (pollChoices.size() < 2) {
            ServletUtils.addError(PARAM_CHOICES, "Vyplňte minimálně dvě volby!", env, null);
            return false;
        }
        return true;
    }
}
