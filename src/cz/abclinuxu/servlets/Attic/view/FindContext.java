/*
 * User: literakl
 * Date: Apr 21, 2002
 * Time: 12:20:45 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.data.*;
import cz.abclinuxu.utils.InstanceUtils;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;

/**
 * Simple servlet, which redirects browser to URL, which
 * contains correct prefix.
 */
public class FindContext extends AbcServlet {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(FindContext.class);

    public static final String PARAM_RELATION = "relationId";

    protected String process(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION,Relation.class,params);
        if ( relation==null ) throw new Exception("Chybí parametr relationId!");

        persistance.synchronize(relation);
        persistance.synchronize(relation.getChild());
        String context = chooseContext(relation);

        UrlUtils.redirect(context+request.getRequestURL(),response,ctx);// wrong!
        return null;
    }

    private String chooseContext(Relation relation) throws PersistanceException {
        GenericObject child = relation.getChild();

        if ( child instanceof Record ) {
            switch ( ((Record)child).getType() ) {
                case Record.SOFTWARE: return UrlUtils.PREFIX_SOFTWARE;
                case Record.ARTICLE: return UrlUtils.PREFIX_CLANKY;
                case Record.DISCUSSION: return UrlUtils.PREFIX_HARDWARE;
                default: return UrlUtils.PREFIX_HARDWARE;
            }
        }

        Persistance persistance = PersistanceFactory.getPersistance();
        List parents = persistance.findParents(relation);
        GenericObject first = ((Relation)parents.get(0)).getParent();

        if ( first instanceof Category && first.getId()==Constants.CAT_HARDWARE ) return UrlUtils.PREFIX_HARDWARE;
        if ( first instanceof Category && first.getId()==Constants.CAT_SOFTWARE ) return UrlUtils.PREFIX_SOFTWARE;
        if ( first instanceof Category && first.getId()==Constants.CAT_ARTICLES ) return UrlUtils.PREFIX_CLANKY;
        if ( first instanceof Category && first.getId()==Constants.CAT_ABC ) return UrlUtils.PREFIX_CLANKY;

        GenericObject second = ((Relation)parents.get(1)).getParent();
        if ( second instanceof Category && second.getId()==Constants.CAT_DRIVERS ) return UrlUtils.PREFIX_DRIVERS;

        log.warn("Uff, no prefix for "+relation);
        return UrlUtils.PREFIX_NONE; // error
    }
}
