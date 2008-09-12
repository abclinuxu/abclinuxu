/*
 *  Copyright (C) 2008 Leos Literak
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

package cz.abclinuxu.servlets.ajax;

import cz.abclinuxu.data.User;
import cz.abclinuxu.exceptions.InvalidInputException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.scheduler.VariableFetcher;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.Misc;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 *
 * @author lubos
 */
public class Forum implements AbcAction {
    public static final String PARAM_RID = "rid";
    public static final String PARAM_QUESTIONS = "questions";
    
    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        String url = (String) env.get(Constants.VAR_REQUEST_URI);
        
        if ("/ajax/forum/questions".equals(url))
            return FMTemplateSelector.select("Forum", "questions", env, request);
        else if ("/ajax/forum/numquestions".equals(url)) {
            ActionProtector.ensureContract(request, Forum.class, true, false, false, true);
            return actionSetForumSize(request, response, env);
        }
        
        return null;
    }
    
    static String actionSetForumSize(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        int rid = Misc.parseInt((String) params.get(PARAM_RID), 0);
        int questions = Misc.parseInt((String) params.get(PARAM_QUESTIONS), 0);
        
        Map maxSizes = VariableFetcher.getInstance().getMaxSizes();
        int max = (Integer) maxSizes.get(VariableFetcher.KEY_QUESTION);
        
        try {
            if (user == null)
                throw new InvalidInputException("Nejste přihlášen!");
            if (rid == 0)
                throw new InvalidInputException("Chybí číslo relace!");
            if (questions > max || questions < 0)
                throw new InvalidInputException("Zadejte číslo v rozsahu 0 - " + max + "!");

            Map<Integer,Integer> mainForums = VariableFetcher.getInstance().getMainForums();
            Element forumsElem = DocumentHelper.makeElement(user.getData(), "/data/forums");
            
            Element elem = (Element) forumsElem.selectSingleNode("forum[text()='"+rid+"']");
            if (elem == null) {
                elem = forumsElem.addElement("forum");
                elem.setText(String.valueOf(rid));
            }

            if (questions == 0 && !mainForums.containsKey(rid))
                elem.detach();
            else
                elem.addAttribute("questions", String.valueOf(questions));
            
            persistence.update(user);
            response.getWriter().print("Nastaveno.");
        } catch (InvalidInputException e) {
            response.getWriter().print(e.getMessage());
        }
        
        return null;
    }
}
