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
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Poll;
import cz.abclinuxu.data.Record;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.view.Comment;
import cz.abclinuxu.data.view.DiscussionRecord;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.data.view.NewsCategories;
import cz.abclinuxu.data.view.RevisionInfo;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.versioning.VersionedDocument;
import cz.abclinuxu.persistence.versioning.Versioning;
import cz.abclinuxu.persistence.versioning.VersioningFactory;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.edit.EditNews;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.ReadRecorder;
import cz.abclinuxu.utils.TagTool;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.freemarker.Tools;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servlet, which loads Relation specified by parameter <code>relationId</code>
 * and redirects execution to servlet handling one of relation's GenericObjects.<p>
 * <u>Context variables introduced by AbcVelocityServlet</u>
 * <dl>
 * <dt><code>VAR_RELATION</code></dt>
 * <dd>instance of Relation.</dd>
 * <dt><code>VAR_PARENTS</code></dt>
 * <dd>List of parental relations. Last element is current relation.</dd>
 * </dl>
 * <u>Parameters used by ShowObject</u>
 * <dl>
 * <dt>relationId</dt>
 * <dd>PK of asked relation, number.</dd>
 * </dl>
 */
public class ShowObject implements AbcAction {
    public static final Logger log = Logger.getLogger(ShowObject.class);

    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_DISCUSSION = "dizId";
    public static final String PARAM_THREAD = "threadId";

    public static final String ACTION_SHOW_CENSORED = "censored";

    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_PARENTS = Constants.VAR_PARENTS;
    public static final String VAR_ITEM = "ITEM";
    public static final String VAR_DRIVER_VERSIONS = "DRIVER_VERSIONS";
    /** children relation of Item, grouped by their type */
    public static final String VAR_CHILDREN_MAP = "CHILDREN";
    public static final String VAR_THREAD = "THREAD";
    public static final String VAR_SUBPORTAL = "SUBPORTAL";

    Persistence persistence = PersistenceFactory.getPersistence();

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);

        if ( ACTION_SHOW_CENSORED.equals(action))
            return processCensored(request,env);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Parametr relationId je prázdný!");
        relation = (Relation) persistence.findById(relation);
        if (relation.getChild() instanceof Record)
            relation = (Relation) persistence.findById(new Relation(relation.getUpper()));
        env.put(VAR_RELATION,relation);

        List parents = persistence.findParents(relation);
        env.put(Constants.VAR_PARENTS, parents);

        Relation subportal = Tools.getParentSubportal(parents);
        if (subportal != null) {
            env.put(VAR_SUBPORTAL, subportal);
            ReadRecorder.log(subportal.getChild(), Constants.COUNTER_READ, env);
        }

        if (relation.getChild() instanceof Poll)
            return ViewPolls.processPoll(env, relation, request);
        if (relation.getChild() instanceof Item)
            return processItem(request, env, relation);
        if ( relation.getParent() instanceof Category ) {
            if (relation.getId()==Constants.REL_POLLS)
                return ViewPolls.processPolls(env, request);
            else
                return ViewCategory.processCategory(request,response,env,relation);
        }
        return FMTemplateSelector.select("ShowObject", "notfound", env, request);
    }

    /**
     * Processes item - like article, discussion, driver etc.
     * @return template to be rendered
     */
    public static String processItem(HttpServletRequest request, Map env, Relation relation) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Item item = (Item) relation.getChild();
        Relation upper = relation;

        Tools.sync(item);
        env.put(VAR_ITEM, item);
        Map children = Tools.groupByType(item.getChildren());
        env.put(VAR_CHILDREN_MAP, children);
        Tools.sync(upper);

        // all these documents supports tags
        env.put(Constants.VAR_ASSIGNED_TAGS, TagTool.getAssignedTags(item));

        int revision = Misc.parseInt((String) params.get(ShowRevisions.PARAM_REVISION), -1);
        if (revision != -1) {
            Versioning versioning = VersioningFactory.getVersioning();
            versioning.load(item, revision);

            List parents = (List) env.get(Constants.VAR_PARENTS);
            Link link = new Link("Revize "+revision, relation.getUrl()+"?revize="+revision, null);
            parents.add(link);
        }

        switch (item.getType()) {
            case Item.ARTICLE:
                return ShowArticle.show(env, item, request);
            case Item.NEWS: {
                env.put(Constants.VAR_RSS, FeedGenerator.getNewsFeedUrl());
                env.put(EditNews.VAR_CATEGORY, NewsCategories.get(item.getSubType()));
                return FMTemplateSelector.select("ShowObject", "news", env, request);
            }
            case Item.DISCUSSION: {
                if (Tools.isQuestion(relation)) {
                    ReadRecorder.log(item, Constants.COUNTER_READ, env);
                    env.put(Constants.VAR_RSS, FeedGenerator.getForumFeedUrl(upper.getId()));
                }
                return FMTemplateSelector.select("ShowObject", "discussion", env, request);
            }
            case Item.HARDWARE: {
                RevisionInfo revisionInfo = Tools.getRevisionInfo(item);
                env.put(Constants.VAR_REVISIONS, revisionInfo);
                Misc.recordReadByNonCommitter(item, revisionInfo, env);
                env.put(Constants.VAR_RSS, FeedGenerator.getHardwareFeedUrl());
                return FMTemplateSelector.select("ShowObject", "hardware", env, request);
            }
            case Item.DRIVER: {
                RevisionInfo revisionInfo = Tools.getRevisionInfo(item);
                env.put(Constants.VAR_REVISIONS, revisionInfo);
                Misc.recordReadByNonCommitter(item, revisionInfo, env);

                SQLTool sqlTool = SQLTool.getInstance();
                List<VersionedDocument> history = sqlTool.getLastRevisions(item, 6);
                String revisionParam = "?" + ShowRevisions.PARAM_REVISION + "=";
                env.put(ShowRevisions.VAR_HISTORY, history);
                env.put(ShowRevisions.VAR_REVISION_PARAM, revisionParam);

                List versions = new ArrayList();
                for ( VersionedDocument version : history ) {
                    Document doc = DocumentHelper.parseText(version.getDocument());
                    Element e = (Element) doc.selectSingleNode("data/version");
                    versions.add(e.getText());
                }
                env.put(VAR_DRIVER_VERSIONS, versions);

                env.put(Constants.VAR_RSS, FeedGenerator.getDriversFeedUrl());
                return FMTemplateSelector.select("ShowObject", "driver", env, request);
            }
            case Item.DICTIONARY: {
                env.put(Constants.VAR_RSS, FeedGenerator.getDictionariesFeedUrl());
                return ShowDictionary.processDefinition(request, relation, env);
            }
			case Item.UNPUBLISHED_BLOG:
            case Item.BLOG:
                return ViewBlog.processStory(request, relation, env);
            case Item.FAQ:
                return ViewFaq.processQuestion(request, relation, env);
            case Item.SOFTWARE:
                return ViewSoftware.processItem(request, relation, env);
            case Item.AUTHOR:
                return ViewAuthor.processAuthor(request, relation, env);
            case Item.TRIVIA:
                return ViewGames.playTriviaGame(request, relation, env);
            case Item.BAZAAR:
                return ViewBazaar.processAd(request, relation, env);
            case Item.CONTENT: {
                env.remove(Constants.VAR_ASSIGNED_TAGS);
                return ViewContent.show(request, env);
            }
            case Item.TOC:
                return ViewTOC.show(request, env);
            case Item.PERSONALITY:
                return ViewPersonality.processPersonality(request, relation, env);
            case Item.DESKTOP:
                return ViewDesktop.processItem(request, relation, env);
			case Item.EVENT:
            case Item.UNPUBLISHED_EVENT:
				return ViewEvent.processItem(request, relation, env);
            case Item.VIDEO:
                return ViewVideo.processItem(request, relation, env);
        }

        return null;
    }

    /**
     * Displays thread in discussion, that was marked as censored.
     */
    String processCensored(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();

        Item diz = (Item) InstanceUtils.instantiateParam(PARAM_DISCUSSION, Item.class, params, request);
        if (diz==null)
            throw new MissingArgumentException("Chybí parametr "+PARAM_DISCUSSION+"!");
        diz = (Item) persistence.findById(diz);

        List children = diz.getChildren();
        Record record = (Record) ((Relation)children.get(0)).getChild();
        persistence.synchronize(record);

        int id = Misc.parseInt((String) params.get(PARAM_THREAD), -1);
        if (id == -1)
            throw new MissingArgumentException("Chybí parametr " + PARAM_THREAD + "!");

        DiscussionRecord dizRecord = (DiscussionRecord) record.getCustom();
        Comment comment = dizRecord.getComment(id);
        env.put(VAR_THREAD, comment);

        return FMTemplateSelector.select("ShowObject", "censored", env, request);
    }
}
