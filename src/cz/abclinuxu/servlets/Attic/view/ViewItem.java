/*
 * User: literakl
 * Date: Jan 31, 2002
 * Time: 9:10:54 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.VelocityHelper;
import cz.abclinuxu.servlets.utils.VariantTool;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Servlet for viewing items. Can decide, what kind of item
 * it is and how to display it on fly.
 */
public class ViewItem extends AbcServlet {
    public static final String VAR_ITEM = "ITEM";
    /** Relation upper to selected relation Item-Record */
    public static final String VAR_UPPER = "REL_ITEM";
    /** children relation of Item, grouped by their type */
    public static final String VAR_CHILDREN_MAP = "CHILDREN";

    protected String process(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Persistance persistance = PersistanceFactory.getPersistance();
        Relation relation = (Relation) ctx.get(ViewRelation.VAR_RELATION);

        Item item = null;
        Record record = null;
        Relation upper = null;

        if ( relation.getChild() instanceof Item ) {
            item = (Item) relation.getChild();
            upper = relation;

            List parents = (List) ctx.get(ViewRelation.VAR_PARENTS);
            if ( parents!=null ) parents.add(upper);
        } else if ( relation.getParent() instanceof Item ) {
            item = (Item) relation.getParent();
            record = (Record) relation.getChild();
            upper = new Relation(relation.getUpper());
        }
        if ( item==null ) throw new Exception("Chybí parametry!");

        persistance.synchronize(item); ctx.put(VAR_ITEM,item);
        persistance.synchronize(upper); ctx.put(VAR_UPPER,upper);

        if ( item.getType()==Item.DRIVER )
            return VariantTool.selectTemplate(request,ctx,"ViewItem","driver");
        if ( item.getType()==Item.DISCUSSION )
            return VariantTool.selectTemplate(request,ctx,"ViewItem","discussion");

        VelocityHelper helper = new VelocityHelper();
        Map children = helper.groupByType(item.getContent());
        ctx.put(VAR_CHILDREN_MAP,children);

        if ( item.getType()==Item.ARTICLE )
            return VariantTool.selectTemplate(request,ctx,"ViewItem","article");

        if ( record==null ) {
            List records = (List) children.get(Constants.TYPE_RECORD);
            if ( records!=null && records.size()>0 )
                record = (Record) ((Relation)records.get(0)).getChild();
        }

        if ( item.getType()==Item.MAKE && record!=null ) {
            switch ( record.getType() ) {
                case Record.HARDWARE: return VariantTool.selectTemplate(request,ctx,"ViewItem","hardware");
                case Record.SOFTWARE: return VariantTool.selectTemplate(request,ctx,"ViewItem","software");
            }
        }
        return null;
    }
}
