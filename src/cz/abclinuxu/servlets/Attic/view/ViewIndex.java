/*
 * User: literakl
 * Date: Jan 14, 2002
 * Time: 9:20:03 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.VelocityHelper;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.scheduler.jobs.UpdateLinks;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * This servlet is used to create index page on this
 * site. It puts some variables to context and
 * renders velocity page.<p>
 * <u>Context variables introduced by AbcServlet</u>
 * <dl>
 * <dt><code>VAR_HARDWARE</code></dt>
 * <dd>List of Relations, where parent() is /Hardware/386 category. All children are initialized.</dd>
 * <dt><code>VAR_SOFTWARE</code></dt>
 * <dd>List of Relations, where parent() is /Software category. All children are initialized.</dd>
 * <dt><code>VAR_LINKS</code></dt>
 * <dd>Map, where key is Server and value is list of Links, where link.server==server.id && link.fixed==false.</dd>
 * </dl>
 */
public class ViewIndex extends AbcServlet {
    public static final String VAR_HARDWARE = "HARDWARE";
    public static final String VAR_SOFTWARE = "SOFTWARE";
    public static final String VAR_CLANKY = "CLANKY";
    public static final String VAR_ABCLINUXU = "ABCLINUXU";
    public static final String VAR_ANKETA = "ANKETA";
    public static final String VAR_LINKS = "LINKS";

    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Persistance persistance = PersistanceFactory.getPersistance();
        VelocityHelper helper = new VelocityHelper();

        Category hw = (Category) persistance.findById(new Category(Constants.CAT_386));
        helper.sync(hw.getContent());
        ctx.put(ViewIndex.VAR_HARDWARE,hw.getContent());

        Category sw = (Category) persistance.findById(new Category(Constants.CAT_SOFTWARE));
        helper.sync(sw.getContent());
        ctx.put(ViewIndex.VAR_SOFTWARE,sw.getContent());

        Category clanky = (Category) persistance.findById(new Category(Constants.CAT_ARTICLES));
        helper.sync(clanky.getContent());
        ctx.put(ViewIndex.VAR_CLANKY,clanky.getContent());

        Category abc = (Category) persistance.findById(new Category(Constants.CAT_ABC));
        helper.sync(abc.getContent());
        ctx.put(ViewIndex.VAR_ABCLINUXU,abc.getContent());

        List list = persistance.findByCommand("select max(cislo) from anketa");
        Object[] objects = (Object[]) list.get(0);
        Poll poll = new Poll(((Integer)objects[0]).intValue());
        poll = (Poll) persistance.findById(poll);
        ctx.put(ViewIndex.VAR_ANKETA,poll);

        Category linksCategory = (Category) persistance.findById(new Category(Constants.CAT_LINKS));
        Map links = UpdateLinks.groupLinks(linksCategory,persistance);
        ctx.put(ViewLinks.VAR_LINKS,links);

        return getTemplate("index.vm");
    }
}
