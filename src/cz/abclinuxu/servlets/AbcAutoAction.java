/*
 *  Copyright (C) 2009 Leos Literak
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

package cz.abclinuxu.servlets;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.security.ActionCheck;
import cz.abclinuxu.security.ActionProtector;
import cz.abclinuxu.security.Permissions;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author lubos
 */
public class AbcAutoAction implements AbcAction {
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String VAR_RELATION = "RELATION";

    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected Map<String,Object> env;
    protected Map<String,Object> params;
    protected Relation relation;
    protected User user;

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        init(request, response, env);
        return invokeAction();
    }

    protected void init(HttpServletRequest request, HttpServletResponse response, Map<String,Object> env) {
        this.request = request;
        this.response = response;
        this.env = env;

        params = (Map<String,Object>) env.get(Constants.VAR_PARAMS);
    }

    protected String invokeAction() throws Exception {
        if (getClass().getSimpleName().startsWith("Edit")) {
            if (ServletUtils.handleMaintainance(request, env)) {
                response.sendRedirect(response.encodeRedirectURL("/"));
                return null;
            }
        }

        String action = (String) params.get(PARAM_ACTION);

        if (Misc.empty(action)) {
            Field field;
            try {
                field = getClass().getField("DEFAULT_ACTION");
                action = (String) field.get(this);
            } catch (NoSuchFieldException e) {
                throw new MissingArgumentException("Chybí parametr action!");
            }
        }

        if (getClass().getName().toLowerCase().contains("edit")) {
            if (ServletUtils.handleMaintainance(request, env)) {
                response.sendRedirect(response.encodeRedirectURL("/"));
                return null;
            }
        }

        String methodName = "action" + Character.toUpperCase(action.charAt(0)) + action.substring(1);
        try {
            Method method = getClass().getDeclaredMethod(methodName);
            ActionCheck check = method.getAnnotation(ActionCheck.class);

            if (check != null) {
                boolean userRequired = check.userRequired();
                if (check.permittedRoles().length > 0)
                    userRequired = true;
                if (!Misc.empty(check.itemOwnerOrRole()))
                    userRequired = true;
                if (check.requireCreateRight() || check.requireDeleteRight() || check.requireModifyRight())
                    userRequired = true;

		user = (User) env.get(Constants.VAR_USER);
                if (userRequired) {
                    if (user == null)
                        return FMTemplateSelector.select("ViewUser", "login", env, request);
                }

                ActionProtector.ensureContract(request, getClass(), userRequired, check.checkReferer(), check.checkPost(), check.checkTicket());

                boolean relationRequired = check.relationRequired();
                if (check.itemType() != 0)
                    relationRequired = true;
                if (!Misc.empty(check.itemOwnerOrRole()))
                    relationRequired = true;

                if (relationRequired) {
                    relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);

                    if (relation == null)
                        throw new MissingArgumentException("Chybí parametr rid!");
                    
                    Tools.sync(relation);
                    env.put(VAR_RELATION, relation);
                } else
                    relation = null;

                String[] permittedRoles = check.permittedRoles();
                if (permittedRoles.length > 0) {
                    boolean ok = false;
                    for (String role : permittedRoles) {
                        if (user.hasRole(role)) {
                            ok = true;
                            break;
                        }
                    }

                    if (!ok) {
                        return returnForbidden();
                    }
                }

                if (check.itemType() != 0) {
                    Item child = (Item) relation.getChild();
                    if (child.getType() != check.itemType())
                        throw new SecurityException("Nepovolený typ položky!");
                }

                if (check.itemOwnerOrRole() != null && check.itemOwnerOrRole().length() > 0) {
                    Item child = (Item) relation.getChild();
                    String role = check.itemOwnerOrRole();

                    if (!user.hasRole(role)) {
                        if (child.getOwner() != user.getId())
                            return returnForbidden();
                    }
                }

                if (check.requireCreateRight() || check.requireDeleteRight() || check.requireModifyRight()) {
                    Permissions perm = Tools.permissionsFor(user, relation);
                    if (check.requireCreateRight() && !perm.canCreate())
                        return returnForbidden();
                    if (check.requireDeleteRight() && !perm.canDelete())
                        return returnForbidden();
                    if (check.requireModifyRight() && !perm.canModify())
                        return returnForbidden();
                }
            }

            return (String) method.invoke(this);
        } catch (NoSuchMethodException e) {
            throw new MissingArgumentException("Neplatna hodnota parametru action!");
        } catch (InvocationTargetException e) {
            throw (Exception) e.getTargetException();
        }
    }

    protected String returnForbidden() {
        return FMTemplateSelector.select("ViewUser", "forbidden", env, request);
    }
}
