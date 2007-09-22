/*
 *  Copyright (C) 2007 Leos Literak
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
package cz.abclinuxu.servlets.html.ajax;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.data.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;

/**
 * Server side component to respond to AJAX requests.
 * @author literakl
 * @since 22.9.2007
 */
public class ConflictingUsers implements AbcAction {
    public static final String PARAM_VALUE = "value";
    public static final String VAR_MESSAGE = "MESSAGE";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        env.put(VAR_MESSAGE, "");

        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String value = (String) params.get(PARAM_VALUE);
        if (value == null || value.trim().length() == 0)
            return null;

        SQLTool sqlTool = SQLTool.getInstance();
        User user = (User) env.get(Constants.VAR_USER);
        String url = (String) env.get(Constants.VAR_REQUEST_URI);
        if ("/ajax/checkLogin".equals(url)) {
            List<Integer> users = sqlTool.findUsersWithLogin(value, null);
            if (!(users.isEmpty() || (user != null && users.contains(user.getId()))))
                env.put(VAR_MESSAGE, "Toto přihlašovací jméno je již používáno.");
        } else {
            List<Integer> users = sqlTool.findUsersWithNick(value, null);
            if (!(users.isEmpty() || (user != null && users.contains(user.getId()))))
                env.put(VAR_MESSAGE, "Tato přezdívka je již používána.");
        }
        return "/print/ajax/user_conflicts.ftl";
    }
}
