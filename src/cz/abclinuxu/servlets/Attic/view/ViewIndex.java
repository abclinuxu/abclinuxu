/*
 * User: literakl
 * Date: Jan 14, 2002
 * Time: 9:20:03 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Iterator;

/**
 * This servlet is used to create index page on this
 * site. It puts some variables to context and
 * renders velocity page.
 * <dl>
 * <dt><code>VAR_HARDWARE</code></dt>
 * <dd>List of Relations, where parent() is /Hardware/386 category. All children are initialized.</dd>
 * <dt><code>VAR_SOFTWARE</code></dt>
 * <dd>List of Relations, where parent() is /Software category. All children are initialized.</dd>
 * </dl>
 */
public class ViewIndex extends AbcServlet {
    public static final String VAR_HARDWARE = "HARDWARE";
    public static final String VAR_SOFTWARE = "SOFTWARE";

    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Persistance persistance = PersistanceFactory.getPersistance();
        Category hw = (Category) persistance.findById(new Category(4));
        List hwcontent = hw.getContent();
        for (Iterator iter = hwcontent.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            persistance.synchronize(relation.getChild());
        }
        ctx.put(ViewIndex.VAR_HARDWARE,hwcontent);

        Category sw = (Category) persistance.findById(new Category(3));
        List swcontent = sw.getContent();
        for (Iterator iter = hwcontent.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            persistance.synchronize(relation.getChild());
        }
        ctx.put(ViewIndex.VAR_SOFTWARE,swcontent);

        return getTemplate("index.vm");
    }
}
