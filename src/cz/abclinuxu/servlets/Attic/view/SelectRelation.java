/*
 * User: literakl
 * Date: Jan 15, 2002
 * Time: 9:59:02 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.VelocityHelper;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.PersistanceException;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.GenericObject;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

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
 * <u>Parameters used by SelectIcon</u>
 * <dl>
 * <dt><code>PARAM_URL</code></dt>
 * <dd>Where to redirect browser.</dd>
 * <dt><code>PARAM_SELECTED</code></dt>
 * <dd>Result of the search.</dd>
 * <dt><code>PARAM_CURRENT</code></dt>
 * <dd>Current relation, where user is selecting relation.</dd>
 * <dt><code>PARAM_ENTERED</code></dt>
 * <dd>Hand written relation id.</dd>
 * </dl>
 * <u>Context variables introduced by AbcServlet</u>
 * <dl>
 * <dt><code>VAR_HARDWARE</code></dt>
 * <dd>List of Relations, where parent() is /Hardware category.</dd>
 * <dt><code>VAR_SOFTWARE</code></dt>
 * <dd>List of Relations, where parent() is /Software category.</dd>
 * <dt><code>VAR_CLANKY</code></dt>
 * <dd>List of Relations, where parent() is /Clanky category.</dd>
 * <dt><code>VAR_H386</code></dt>
 * <dd>List of Relations, where parent() is /Hardware/386 category.</dd>
 * <dt><code>VAR_CURRENT</code></dt>
 * <dd>Actual Relation, equivalent of PARAM_CURRENT.</dd>
 * </dl>
 */
public class SelectRelation extends AbcServlet {
    public static final String PARAM_SELECTED = "selectedId";
    public static final String PARAM_CURRENT = "currentId";
    public static final String PARAM_ENTERED = "enteredId";
    public static final String PARAM_URL = "url";
    public static final String PARAM_FINISH = "finish";
    public static final String PARAM_CONFIRM = "confirm";

    public static final String VAR_HARDWARE = "HARDWARE";
    public static final String VAR_SOFTWARE = "SOFTWARE";
    public static final String VAR_CLANKY = "CLANKY";
    public static final String VAR_386 = "H386";
    public static final String VAR_CURRENT = "CURRENT";


    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        String confirm = request.getParameter(SelectRelation.PARAM_CONFIRM);
        String finish = request.getParameter(SelectRelation.PARAM_FINISH);

        if ( finish!=null && finish.length()>0 ) {
            return actionFinish(request,response,ctx);
        }

        if ( confirm!=null && confirm.length()>0 ) {
            return actionConfirm(request,ctx);
        }
        return actionNext(request,ctx);
    }

    /**
     * Called, when we shall descend to another relation
     */
    protected Template actionNext(HttpServletRequest request, Context ctx) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        String manual = request.getParameter(SelectRelation.PARAM_ENTERED);
        String tmp = request.getParameter(SelectRelation.PARAM_CURRENT);

        if ( tmp!=null ) {
            try {
                int currentId = Integer.parseInt( (manual!=null && manual.length()>0)? manual:tmp);
                Relation current = (Relation) persistance.findById(new Relation(currentId));
                ctx.put(SelectRelation.VAR_CURRENT,current);
                return getTemplate("view/selectRelation.vm");
            } catch (NumberFormatException e) {
                addError(SelectRelation.PARAM_ENTERED,"Cislo vetsi nez nula!",ctx, null);
            } catch (PersistanceException e) {
                addError(AbcServlet.GENERIC_ERROR,"Nebyla zvolena platna relace!",ctx, null);
            }
        }

        Category clanky = (Category) persistance.findById(new Category(Constants.CAT_ARTICLES));
        List content = clanky.getContent();
        ctx.put(SelectRelation.VAR_CLANKY,content);

        Category hw = (Category) persistance.findById(new Category(Constants.CAT_HARDWARE));
        content = hw.getContent();
        ctx.put(SelectRelation.VAR_HARDWARE,content);

        Category sw = (Category) persistance.findById(new Category(Constants.CAT_SOFTWARE));
        content = sw.getContent();
        ctx.put(SelectRelation.VAR_SOFTWARE,content);

        Category hw386 = (Category) persistance.findById(new Category(Constants.CAT_386));
        content = hw386.getContent();
        ctx.put(SelectRelation.VAR_386,content);
        return getTemplate("view/selectRelation.vm");
    }

    /**
     * Called, when user select relation.
     */
    protected Template actionConfirm(HttpServletRequest request, Context ctx) throws Exception {
        int result = 0;
        String manual = request.getParameter(SelectRelation.PARAM_ENTERED);
        String tmp = request.getParameter(SelectRelation.PARAM_CURRENT);

        if ( manual!=null && manual.length()>0 ) {
            try {
                result = Integer.parseInt(manual);
            } catch (NumberFormatException e) {
                addError(SelectRelation.PARAM_ENTERED,"Cislo vetsi nez nula!",ctx, null);
            }
        } else {
            try {
                result = Integer.parseInt(tmp);
            } catch (NumberFormatException e) {
                addError(AbcServlet.GENERIC_ERROR,"Nebyla zvolena platna relace!",ctx, null);
            }
        }

        Relation current = (Relation) PersistanceFactory.getPersistance().findById(new Relation(result));
        ctx.put(SelectRelation.VAR_CURRENT,current);
        return getTemplate("view/selectRelationConfirm.vm");
    }

    /**
     * Called, when user confirms his choice. It redirects flow to PARAM_URL and puts all parameters
     * to session map AbcServlet.ATTRIB_PARAMS. There will be also result under name PARAM_SELECTED.
     */
    protected Template actionFinish(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        String choice = request.getParameter(SelectRelation.PARAM_CURRENT);

        Map map = VelocityHelper.putParamsToMap(request,null);
        map.put(SelectRelation.PARAM_SELECTED,map.get(SelectRelation.PARAM_CURRENT));
        map.remove(SelectRelation.PARAM_CURRENT);
        map.remove(SelectRelation.PARAM_URL);

        HttpSession session = request.getSession();
        session.setAttribute(AbcServlet.ATTRIB_PARAMS,map);

        String url = request.getParameter(SelectRelation.PARAM_URL);
        redirect(url,response,ctx);
        return null;
    }
}
