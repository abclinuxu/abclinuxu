/*
 * User: literakl
 * Date: Jan 14, 2002
 * Time: 9:20:03 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.data.*;
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
 * renders velocity page.<p>
 * <u>Context variables introduced by AbcServlet</u>
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
    public static final String VAR_CLANKY = "CLANKY";
    public static final String VAR_ABCLINUXU = "ABCLINUXU";
    public static final String VAR_ANKETA = "ANKETA";

    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Persistance persistance = PersistanceFactory.getPersistance();
        Category hw = (Category) persistance.findById(new Category(Constants.CAT_386));
        List content = hw.getContent();
        for (Iterator iter = content.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            persistance.synchronize(relation.getChild());
        }
        ctx.put(ViewIndex.VAR_HARDWARE,content);

        Category sw = (Category) persistance.findById(new Category(Constants.CAT_SOFTWARE));
        content = sw.getContent();
        for (Iterator iter = content.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            persistance.synchronize(relation.getChild());
        }
        ctx.put(ViewIndex.VAR_SOFTWARE,content);

        Category clanky = (Category) persistance.findById(new Category(Constants.CAT_ARTICLES));
        content = clanky.getContent();
        for (Iterator iter = content.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            persistance.synchronize(relation.getChild());
        }
        ctx.put(ViewIndex.VAR_CLANKY,content);

        Category abc = (Category) persistance.findById(new Category(Constants.CAT_ABC));
        content = abc.getContent();
        for (Iterator iter = content.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            persistance.synchronize(relation.getChild());
        }
        ctx.put(ViewIndex.VAR_ABCLINUXU,content);

        List list = persistance.findByCommand("select max(cislo) from anketa");
        Object[] objects = (Object[]) list.get(0);
        Poll poll = new Poll(((Integer)objects[0]).intValue());
        poll = (Poll) persistance.findById(poll);
        ctx.put(ViewIndex.VAR_ANKETA,poll);

        return getTemplate("index.vm");
    }
}
