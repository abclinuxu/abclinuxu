/*
 * User: literakl
 * Date: Jan 14, 2002
 * Time: 9:20:03 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcVelocityServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.init.AbcInit;
import cz.abclinuxu.servlets.utils.VelocityHelper;
import cz.abclinuxu.servlets.utils.VelocityTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.scheduler.VariableFetcher;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * This servlet is used to create index page on this
 * site. It puts some variables to context and
 * renders velocity page.<p>
 * <u>Context variables introduced by AbcVelocityServlet</u>
 * <dl>
 * <dt><code>VAR_HARDWARE</code></dt>
 * <dd>List of Relations, where parent() is /Hardware/386 category. All children are initialized.</dd>
 * <dt><code>VAR_SOFTWARE</code></dt>
 * <dd>List of Relations, where parent() is /Software category. All children are initialized.</dd>
 * </dl>
 */
public class ViewIndex extends AbcVelocityServlet {
    public static final String VAR_HARDWARE = "HARDWARE";
    public static final String VAR_SOFTWARE = "SOFTWARE";
    public static final String VAR_HW_NEW = "HW_NEW";
    public static final String VAR_SW_NEW = "SW_NEW";
    public static final String VAR_DRIVERS = "DRIVERS";
    public static final String VAR_ACTUAL = "NEW";
    public static final String VAR_FORUM = "FORUM";

    protected String process(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Persistance persistance = PersistanceFactory.getPersistance();
        VelocityHelper helper = new VelocityHelper();

        Category hw = (Category) persistance.findById(new Category(Constants.CAT_386));
        helper.sync(hw.getContent());
        ctx.put(ViewIndex.VAR_HARDWARE,hw.getContent());

        Category sw = (Category) persistance.findById(new Category(Constants.CAT_SOFTWARE));
        helper.sync(sw.getContent());
        ctx.put(ViewIndex.VAR_SOFTWARE,sw.getContent());

        Category actual = (Category) persistance.findById(new Category(Constants.CAT_ACTUAL_ARTICLES));
        helper.sync(actual.getContent());
        ctx.put(VAR_ACTUAL,actual);

        ctx.put(VAR_HW_NEW,AbcInit.getFetcher().getNewHardware());
        ctx.put(VAR_SW_NEW,AbcInit.getFetcher().getNewSoftware());
        ctx.put(VAR_DRIVERS,AbcInit.getFetcher().getNewDrivers());

        Category forum = (Category) persistance.findById(new Category(Constants.CAT_FORUM));
        helper.sync(forum.getContent());
        ctx.put(VAR_FORUM,forum);

        return VelocityTemplateSelector.selectTemplate(request,ctx,"ViewIndex","show");
    }
}
