/*
 * User: literakl
 * Date: Jan 14, 2002
 * Time: 9:20:03 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcVelocityServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.init.AbcInit;
import cz.abclinuxu.servlets.utils.VelocityHelper;
import cz.abclinuxu.servlets.utils.template.VelocityTemplateSelector;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.scheduler.VariableFetcher;
import cz.abclinuxu.utils.Tools;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * This servlet renders index page of AbcLinuxu.
 */
public class ViewIndex extends AbcFMServlet {
    public static final String VAR_HARDWARE = "HARDWARE";
    public static final String VAR_SOFTWARE = "SOFTWARE";
    public static final String VAR_FORUM = "FORUM";

    /**
     * Evaluate the request.
     */
    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        Tools tools = new Tools();

        Category hw = (Category) persistance.findById(new Category(Constants.CAT_386));
        env.put(ViewIndex.VAR_HARDWARE,hw.getContent());

        Category sw = (Category) persistance.findById(new Category(Constants.CAT_SOFTWARE));
        env.put(ViewIndex.VAR_SOFTWARE,sw.getContent());

        Category forum = (Category) persistance.findById(new Category(Constants.CAT_FORUM));
        tools.sync(forum.getContent());
        env.put(ViewIndex.VAR_FORUM,forum);

        return FMTemplateSelector.select("ViewIndex","show",env,request,null);
    }
}
