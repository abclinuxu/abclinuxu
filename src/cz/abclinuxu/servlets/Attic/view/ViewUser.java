/*
 * User: literakl
 * Date: Feb 3, 2002
 * Time: 8:22:28 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.servlets.utils.VelocityHelper;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.data.*;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Profile of the user
 */
public class ViewUser extends AbcServlet {
    public static final String PARAM_USER = "userId";

    public static final String VAR_PROFILE = "PROFILE";
    public static final String VAR_SW_RECORDS = "SW";
    public static final String VAR_HW_RECORDS = "HW";
    public static final String VAR_ARTICLES = "ARTICLE";

    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();
        VelocityHelper helper = new VelocityHelper();

        User user = (User) instantiateParam(PARAM_USER,User.class,params);
        persistance.synchronize(user);
        ctx.put(VAR_PROFILE,user);

        Record record = new Record();
        record.setType(Record.HARDWARE);
        record.setOwner(user.getId());

        List list = new ArrayList(1);
        list.add(record);
        List found = persistance.findByExample(list,null);

        List hw = new ArrayList(found.size());
        Relation rel = new Relation();
        for (Iterator iter = found.iterator(); iter.hasNext();) {
            rel.setChild((GenericObject) iter.next());
            hw.add(persistance.findByExample(rel)[0]);
        }
        helper.sync(hw);
        ctx.put(VAR_HW_RECORDS,hw);

        record.setType(Record.SOFTWARE);
        found = persistance.findByExample(list,null);

        List sw = new ArrayList(found.size());
        for (Iterator iter = found.iterator(); iter.hasNext();) {
            rel.setChild((GenericObject) iter.next());
            sw.add(persistance.findByExample(rel)[0]);
        }
        helper.sync(sw);
        ctx.put(VAR_SW_RECORDS,sw);

        return getTemplate("view/profile.vm");
    }
}
