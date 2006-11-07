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
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.exceptions.InvalidInputException;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;
import org.dom4j.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Show games or play them.
 * @author literakl
 * @since 6.11.2006
 */
public class ViewGames implements AbcAction {
    public static final String PARAM_RELATION = "rid";
    public static final String PARAM_POSITION = "position";

    public static final String VAR_TRIVIA_GAMES = "TRIVIA_GAMES";
    public static final String VAR_RESULTS = "RESULTS";
    public static final String VAR_SCORE = "SCORE";
    public static final String VAR_POSITION = "POSITION";
    public static final String VAR_QUESTION = "QUESTION";
    public static final String VAR_CHOICES = "CHOICES";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION, Relation.class, params, request);
        if (relation == null)
            throw new NotFoundException("Stránka nebyla nalezena.");
        Tools.sync(relation);

        if (relation.getChild() instanceof Category)
            return processSection(request, relation, env);
        else
            return playTriviaGame(request, relation, env);
    }

    private String processSection(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance();
        SQLTool sqlTool = SQLTool.getInstance();

        List trivias = sqlTool.findItemRelationsWithType(Item.TRIVIA, null);
        Tools.syncList(trivias);
        env.put(VAR_TRIVIA_GAMES, trivias);

        Relation sectionGames = (Relation) persistence.findById(new Relation(Constants.REL_GAMES));
        env.put(ShowObject.VAR_PARENTS, Collections.singletonList(sectionGames));
        env.put(ShowObject.VAR_RELATION, relation);

        return FMTemplateSelector.select("ViewGames", "list", env, request);
    }

    private String playTriviaGame(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistance();

        Item game = (Item) relation.getChild();
        if (game.getType() != Item.TRIVIA)
            throw new InvalidInputException("Tato relace nepatøí kvízu!");
        Element root = game.getData().getRootElement();

        Relation sectionGames = (Relation) persistence.findById(new Relation(Constants.REL_GAMES));
        List parents = persistence.findParents(sectionGames);
        parents.add(relation);
        env.put(ShowObject.VAR_PARENTS, parents);
        env.put(ShowObject.VAR_RELATION, relation);

        int position = Misc.parseInt((String) params.get(PARAM_POSITION), -1);
        if (position == -1) {
            position = 1;
        } else {
            String lastChoice = (String) params.get("q"+position);
            if (Misc.empty(lastChoice))
                ServletUtils.addError(Constants.ERROR_GENERIC, "Prosím, vyberte jednu z nabízených mo¾ností.", env, null);
            else
                position++;
        }

        if (position > 10) {
            int correctAnswears = 0;
            List results = new ArrayList();
            List questions = root.elements("question");
            for (int i = 1; i <= 10; i++) {
                String reply = (String) params.get("q"+i);
                Element elementQuestion = (Element) questions.get(i - 1);
                String question = elementQuestion.elementText("content");
                Element elementReply = (Element) elementQuestion.selectSingleNode("answear[@id="+reply+"]");
                Element elementAnswear = (Element) elementQuestion.selectSingleNode("answear[@correct='true']");
                if (elementReply.equals(elementAnswear)) {
                    correctAnswears++;
                    results.add(new TriviaResult(question, elementAnswear.getText()));
                } else
                    results.add(new TriviaResult(question, elementReply.getText(), elementAnswear.getText()));
            }

            // refresh object again, let's minimize concurrency issue probability
            // todo use SQL table from bug #623
            game = (Item) persistence.findById(game).clone();
            root = game.getData().getRootElement();
            Element count = (Element) root.selectSingleNode("stats/count");
            count.setText(Integer.toString(Misc.parseInt(count.getText(), 0) + 1));
            Element sum = (Element) root.selectSingleNode("stats/sum");
            sum.setText(Integer.toString(Misc.parseInt(sum.getText(), 0) + correctAnswears));
            persistence.update(game);

            env.put(VAR_SCORE, correctAnswears);
            env.put(VAR_RESULTS, results);
            return FMTemplateSelector.select("ViewGames", "trivia_results", env, request);
        }

        Element elementQuestion = (Element) root.selectSingleNode("question["+position+"]");
        env.put(VAR_QUESTION, elementQuestion.elementText("content"));
        List answears = new ArrayList();
        for (Iterator iter = elementQuestion.elements("answear").iterator(); iter.hasNext();) {
            Element answear = (Element) iter.next();
            int id = Misc.parseInt(answear.attributeValue("id"), -1);
            answears.add(new TriviaChoice(answear.getText(), id));
        }
        Random rand = new Random(System.currentTimeMillis());
        Collections.shuffle(answears, rand);

        env.put(VAR_CHOICES, answears);
        env.put(VAR_POSITION, position);

        return FMTemplateSelector.select("ViewGames", "trivia_play", env, request);
    }

    public static class TriviaChoice {
        String text;
        int id;

        public TriviaChoice(String text, int id) {
            this.text = text;
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public int getId() {
            return id;
        }
    }

    public static class TriviaResult {
        String question;
        String reply;
        String correctAnswear;
        boolean correct;

        public TriviaResult(String question, String reply) {
            this.question = question;
            this.reply = reply;
            correct = true;
        }

        public TriviaResult(String question, String reply, String correctAnswear) {
            this.question = question;
            this.reply = reply;
            this.correctAnswear = correctAnswear;
            correct = false;
        }

        public String getQuestion() {
            return question;
        }

        public String getReply() {
            return reply;
        }

        public String getCorrectAnswear() {
            return correctAnswear;
        }

        public boolean isCorrect() {
            return correct;
        }
    }
}
