/*
 *  Copyright (C) 2006 Leos Literak
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

import cz.abclinuxu.data.Category;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.url.URLManager;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.parser.clean.HtmlPurifier;
import cz.abclinuxu.utils.parser.clean.HtmlChecker;
import cz.abclinuxu.utils.parser.clean.Rules;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.versioning.Versioning;
import cz.abclinuxu.persistence.versioning.VersioningFactory;

import cz.abclinuxu.security.ActionCheck;
import cz.abclinuxu.servlets.AbcAutoAction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import java.util.Random;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Attribute;

/**
 * Editor for the trivia games.
 * @author literakl
 * @since 6.11.2006
 */
public class EditTrivia extends AbcAutoAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditTrivia.class);

    public static final String PARAM_TITLE = "title";
    public static final String PARAM_DESCRIPTION = "desc";
    public static final String PARAM_DIFFICULTY = "difficulty";
    public static final String PARAM_QUESTION = "question";
    public static final String PARAM_CORRECT_ANSWEAR = "answear";
    public static final String PARAM_WRONG_ANSWEAR1 = "bad1";
    public static final String PARAM_WRONG_ANSWEAR2 = "bad2";
    public static final String PARAM_RELATION = "rid";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_EDIT_STEP2 = "edit2";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        init(request, response, env);

        if (relation == null)
            relation = new Relation(Constants.REL_TRIVIA);

        return invokeAction();
    }

    @ActionCheck(requireCreateRight = true)
    public String actionAdd() {
        return FMTemplateSelector.select("EditTrivia", "add", env, request);
    }

    @ActionCheck(requireCreateRight = true, checkReferer = true, checkPost = true)
    public String actionAdd2() throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Relation upper = (Relation) persistence.findById(new Relation(Constants.REL_TRIVIA));

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("data");
        Element stats = root.addElement("stats");
        stats.addElement("sum").setText("0");
        stats.addElement("count").setText("0");
        Item item = new Item(0, Item.TRIVIA);
        item.setData(document);
        item.setOwner(user.getId());

		Category cat = (Category) upper.getChild();
		item.setGroup(cat.getGroup());
		item.setPermissions(cat.getPermissions());

        boolean canContinue = setName(params, item, env);
        canContinue &= setDescription(params, root, env);
        canContinue &= setDifficulty(params, root, env);
        canContinue &= setQuestions(params, root, env);

        if (!canContinue)
            return FMTemplateSelector.select("EditTrivia", "add", env, request);

        Versioning versioning = VersioningFactory.getVersioning();
        versioning.prepareObjectBeforeCommit(item, user.getId());
        persistence.create(item);
        versioning.commit(item, user.getId(), "Počáteční revize dokumentu");

        Relation newRelation = new Relation(upper.getChild(), item, upper.getId());
        String name = item.getTitle();
        String url = upper.getUrl() + "/" + URLManager.enforceRelativeURL(name);
        url = URLManager.protectFromDuplicates(url);
        if (url != null)
            newRelation.setUrl(url);

        persistence.create(newRelation);
        newRelation.getParent().addChildRelation(newRelation);

        EditDiscussion.createEmptyDiscussion(newRelation, user, persistence);

        
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, upper.getUrl());
        
        return null;
    }

    @ActionCheck(requireModifyRight = true, itemType = Item.TRIVIA)
    public String actionEdit() throws Exception {
        Item item = (Item) relation.getChild();
        Element root = item.getData().getRootElement();

        params.put(PARAM_TITLE, item.getTitle());
        Node node = root.element("description");
        if (node != null)
            params.put(PARAM_DESCRIPTION, node.getText());
        node = root.element("difficulty");
        if (node != null)
            params.put(PARAM_DIFFICULTY, node.getText());

        int i = 1;
        List questions = root.elements("question");
        for (Iterator iter = questions.iterator(); iter.hasNext();) {
            Element question = (Element) iter.next();
            String prefix = "q" + i;
            params.put(prefix+PARAM_QUESTION, question.elementText("content"));
            List answears = question.elements("answear");
            boolean firstWrongMatched = false;
            for (Iterator iterIn = answears.iterator(); iterIn.hasNext();) {
                Element answear = (Element) iterIn.next();
                if (Boolean.valueOf(answear.attributeValue("correct")))
                    params.put(prefix + PARAM_CORRECT_ANSWEAR, answear.getText());
                else {
                    if (! firstWrongMatched) {
                        params.put(prefix + PARAM_WRONG_ANSWEAR1, answear.getText());
                        firstWrongMatched = true;
                    } else
                        params.put(prefix + PARAM_WRONG_ANSWEAR2, answear.getText());
                }
            }
            i++;
        }

        return FMTemplateSelector.select("EditTrivia", "edit", env, request);
    }

    @ActionCheck(requireModifyRight = true, itemType = Item.TRIVIA, checkReferer = true, checkPost = true)
    public String actionEdit2() throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Relation upper = (Relation) persistence.findById(new Relation(Constants.REL_TRIVIA));

        Item item = (Item) relation.getChild().clone();
        item.setOwner(user.getId());
        Element root = item.getData().getRootElement();

        boolean canContinue = setName(params, item, env);
        canContinue &= setDescription(params, root, env);
        canContinue &= setDifficulty(params, root, env);
        canContinue &= setQuestions(params, root, env);
        String changesDescription = Misc.getRevisionString(params, env);
        canContinue &= !Constants.ERROR.equals(changesDescription);

        if (!canContinue)
            return FMTemplateSelector.select("EditTrivia", "edit", env, request);

        Versioning versioning = VersioningFactory.getVersioning();
        versioning.prepareObjectBeforeCommit(item, user.getId());
        persistence.update(item);
        versioning.commit(item, user.getId(), changesDescription);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, upper.getUrl());
        return null;
    }

    /* ******** setters ********* */

    /**
     * Updates name from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param item   item to be updated
     * @param env    environment
     * @return false, if there is a major error.
     */
    private boolean setName(Map params, Item item, Map env) {
        String tmp = (String) params.get(PARAM_TITLE);
        if (tmp != null && tmp.length() > 0) {
            if (tmp.indexOf('<') != -1) {
                ServletUtils.addError(PARAM_TITLE, "HTML zde není povoleno.", env, null);
                return false;
            }
            item.setTitle(tmp);
            return true;
        } else {
            ServletUtils.addError(PARAM_TITLE, "Zadejte název kvízu!", env, null);
            return false;
        }
    }

    /**
     * Updates description from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root   root element to be updated
     * @return false, if there is a major error.
     */
    private boolean setDescription(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_DESCRIPTION);
        if (tmp != null && tmp.length() > 0) {
            try {
                tmp = Misc.filterDangerousCharacters(tmp);
                tmp = HtmlPurifier.clean(tmp);
                HtmlChecker.check(Rules.DEFAULT, tmp);
            } catch (Exception e) {
                ServletUtils.addError(PARAM_DESCRIPTION, e.getMessage(), env, null);
                return false;
            }

            Element element = DocumentHelper.makeElement(root, "description");
            element.setText(tmp);
        } else {
            ServletUtils.addError(PARAM_DESCRIPTION, "Zadejte popis kvízu!", env, null);
            return false;
        }
        return true;
    }

    /**
     * Updates difficulty from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root   root element of item to be updated
     * @param env    environment
     * @return false, if there is a major error.
     */
    private boolean setDifficulty(Map params, Element root, Map env) {
        String tmp = (String) params.get(PARAM_DIFFICULTY);
        if (tmp != null && tmp.length() > 0) {
            DocumentHelper.makeElement(root, "difficulty").setText(tmp);
            return true;
        } else {
            ServletUtils.addError(PARAM_DIFFICULTY, "Zadejte úroveň kvízu!", env, null);
            return false;
        }
    }

    /**
     * Updates set of trivia questions from parameters. Changes are not synchronized with persistence.
     * @param params map holding request's parameters
     * @param root   root element of item to be updated
     * @param env    environment
     * @return false, if there is a major error.
     */
    private boolean setQuestions(Map params, Element root, Map env) {
        List questions = root.elements("question");
        if (questions.size() < 10) {
            // initialization
            Random rand = new Random(System.currentTimeMillis());
            for (int i = questions.size(); i < 10; i++) {
                int id1 = rand.nextInt(10);
                int id2 = rand.nextInt(10);
                while (id2 == id1)
                    id2 = rand.nextInt(10);
                int id3 = rand.nextInt(10);
                while (id3 == id1 || id3 == id2)
                    id3 = rand.nextInt(10);

                Element nodeQuestion = root.addElement("question");
                nodeQuestion.addElement("content");
                Element nodeAnswear = nodeQuestion.addElement("answear");
                nodeAnswear.addAttribute("id", Integer.toString(id1));
                nodeAnswear = nodeQuestion.addElement("answear");
                nodeAnswear.addAttribute("id", Integer.toString(id2));
                nodeAnswear = nodeQuestion.addElement("answear");
                nodeAnswear.addAttribute("id", Integer.toString(id3));
            }
            questions = root.elements("question");
        }

        for (int i = 0; i < 10; i++) {
            String prefix = "q" + (i + 1);
            String question = (String) params.get(prefix+PARAM_QUESTION);
            if (Misc.empty(question)) {
                ServletUtils.addError(prefix + PARAM_QUESTION, "Zadejte text otázky!", env, null);
                return false;
            }
            String answear = (String) params.get(prefix+PARAM_CORRECT_ANSWEAR);
            if (Misc.empty(answear)) {
                ServletUtils.addError(prefix + PARAM_CORRECT_ANSWEAR, "Zadejte odpověď!", env, null);
                return false;
            }
            String wrongAnswear1 = (String) params.get(prefix+PARAM_WRONG_ANSWEAR1);
            if (Misc.empty(wrongAnswear1)) {
                ServletUtils.addError(prefix + PARAM_WRONG_ANSWEAR1, "Zadejte odpověď!", env, null);
                return false;
            }
            String wrongAnswear2 = (String) params.get(prefix+PARAM_WRONG_ANSWEAR2);
            if (Misc.empty(wrongAnswear2)) {
                ServletUtils.addError(prefix + PARAM_WRONG_ANSWEAR2, "Zadejte odpověď!", env, null);
                return false;
            }

            Element nodeQuestion = (Element) questions.get(i);
            nodeQuestion.element("content").setText(question);

            List answears = nodeQuestion.elements("answear");
            Element nodeAnswear = (Element) answears.get(0);
            nodeAnswear.setText(answear);
            Attribute attribute = nodeAnswear.attribute("correct");
            if (attribute != null)
                attribute.setText("true");
            else
                nodeAnswear.addAttribute("correct", "true");

            nodeAnswear = (Element) answears.get(1);
            nodeAnswear.setText(wrongAnswear1);
            attribute = nodeAnswear.attribute("correct");
            if (attribute != null)
                attribute.detach();

            nodeAnswear = (Element) answears.get(2);
            nodeAnswear.setText(wrongAnswear2);
            attribute = nodeAnswear.attribute("correct");
            if (attribute != null)
                attribute.detach();
        }
        return true;
    }
}
