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
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.scheduler.jobs.UpdateLinks;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * This servlet is used to display links to other servers.<p>
 * <u>Context variables introduced by AbcServlet</u>
 * <dl>
 * <dt><code>VAR_LINKS</code></dt>
 * <dd>Map, where key is Server and value is list of Links, where link.server==server.id && link.fixed==false.</dd>
 * </dl>
 */
public class ViewLinks extends AbcServlet {
    public static final String VAR_LINKS = "LINKS";

    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Persistance persistance = PersistanceFactory.getPersistance();
        Category category = (Category) persistance.findById(new Category(Constants.CAT_LINKS));
        Map links = UpdateLinks.groupLinks(category,persistance);
        ctx.put(ViewLinks.VAR_LINKS,links);

        return getTemplate("view/links.vm");
    }
}
