/*
 * User: literakl
 * Date: Jan 15, 2002
 * Time: 9:59:02 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.PersistanceException;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.utils.Misc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Iterator;
import java.util.Map;

/**
 * Servlet for interactive selection of relation. When user chooses the relation, flow is forwarded
 * to <code>PARAM_URL</code> with all parameters propagated to new location plus
 * <code>SelectRelation.PARAM_SELECTED</code> set.<p>
 */
public class SelectRelation extends AbcFMServlet {
    public static final String PARAM_SELECTED = "selectedId";
    public static final String PARAM_CURRENT = "currentId";
    public static final String PARAM_ENTERED = "enteredId";
    public static final String PARAM_URL = "url";
    public static final String PARAM_FINISH = "finish";
    public static final String PARAM_CONFIRM = "confirm";
    public static final String VAR_SOFTWARE = "SOFTWARE";
    public static final String VAR_CLANKY = "CLANKY";
    public static final String VAR_386 = "H386";
    public static final String VAR_CURRENT = "CURRENT";

    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        String confirm = request.getParameter(PARAM_CONFIRM);
        String finish = request.getParameter(PARAM_FINISH);

        if ( finish!=null && finish.length()>0 ) {
            return actionFinish(request,response,env);
        }

        if ( confirm!=null && confirm.length()>0 ) {
            return actionConfirm(request,env);
        }
        return actionNext(request,env);
    }

    /**
     * Called, when we shall descend to another relation
     */
    protected String actionNext(HttpServletRequest request, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        String manual = request.getParameter(PARAM_ENTERED);
        String tmp = request.getParameter(PARAM_CURRENT);

        if ( tmp!=null ) {
            try {
                int currentId = Integer.parseInt( (Misc.empty(manual))? tmp:manual);
                Relation current = (Relation) persistance.findById(new Relation(currentId));
                env.put(VAR_CURRENT,current);
                return FMTemplateSelector.select("SelectRelation","step1",env,request);
            } catch (NumberFormatException e) {
                ServletUtils.addError(PARAM_ENTERED,"Èíslo vìt¹í ne¾ nula!",env, null);
            } catch (PersistanceException e) {
                ServletUtils.addError(Constants.ERROR_GENERIC,"Nebyla zvolena platná relace!",env, null);
            }
        }

        Category clanky = (Category) persistance.findById(new Category(Constants.CAT_ARTICLES));
        List content = clanky.getContent();
        env.put(VAR_CLANKY,content);

        Category sw = (Category) persistance.findById(new Category(Constants.CAT_SOFTWARE));
        content = sw.getContent();
        env.put(VAR_SOFTWARE,content);

        Category hw386 = (Category) persistance.findById(new Category(Constants.CAT_386));
        content = hw386.getContent();
        env.put(VAR_386,content);
        return FMTemplateSelector.select("SelectRelation","step1",env,request);
    }

    /**
     * Called, when user select relation.
     */
    protected String actionConfirm(HttpServletRequest request, Map env) throws Exception {
        int result = 0;
        String manual = request.getParameter(PARAM_ENTERED);
        String tmp = request.getParameter(PARAM_CURRENT);

        if ( manual!=null && manual.length()>0 ) {
            try {
                result = Integer.parseInt(manual);
            } catch (NumberFormatException e) {
                ServletUtils.addError(PARAM_ENTERED,"Císlo vìt¹í ne¾ nula!",env, null);
            }
        } else {
            try {
                result = Integer.parseInt(tmp);
            } catch (NumberFormatException e) {
                ServletUtils.addError(Constants.ERROR_GENERIC,"Nebyla zvolena platná relace!",env, null);
            }
        }

        Relation current = (Relation) PersistanceFactory.getPersistance().findById(new Relation(result));
        env.put(VAR_CURRENT,current);
        return FMTemplateSelector.select("SelectRelation","step2",env,request);
    }

    /**
     * Called, when user confirms his choice. It redirects flow to PARAM_URL and puts all parameters
     * to session map AbcVelocityServlet.ATTRIB_PARAMS. There will be also result under name PARAM_SELECTED.
     */
    protected String actionFinish(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        String choice = request.getParameter(PARAM_CURRENT);

        Map map = ServletUtils.putParamsToMap(request);
        map.put(PARAM_SELECTED,map.get(PARAM_CURRENT));
        String url = (String) map.remove(PARAM_URL);
        map.remove(PARAM_CURRENT);
        request.getSession().setAttribute(Constants.VAR_PARAMS,map);

        ((UrlUtils) env.get(Constants.VAR_URL_UTILS)).redirect(response, url);
        return null;
    }
}
