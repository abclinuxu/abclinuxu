/*
 * User: literakl
 * Date: Jan 31, 2002
 * Time: 9:10:54 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.servlets.utils.VelocityHelper;
import cz.abclinuxu.servlets.utils.VariantTool;
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
 * Servlet for viewing items. Can decide, what kind of item
 * it is and how to display it on fly.
 */
public class ViewItem extends AbcServlet {
    public static final String VAR_ITEM = "ITEM";
    public static final String VAR_RECORD = "RECORD";
    /** Relation upper to selected relation Item-Record */
    public static final String VAR_UPPER = "UPPER";

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
        if ( item==null ) throw new Exception("Chyb� parametry!");

        persistance.synchronize(item); ctx.put(VAR_ITEM,item);
        persistance.synchronize(upper); ctx.put(VAR_UPPER,upper);

        VelocityHelper helper = new VelocityHelper();
        helper.sortByDateDescending(item.getContent());
        if ( record==null && item.getType()!=Item.DRIVER ) {
            for (Iterator iter = item.getContent().iterator(); iter.hasNext();) {
                Relation rel = (Relation) iter.next();
                if ( rel.getChild() instanceof Record ) {
                    record = (Record) rel.getChild();
                    break;
                }
            }
        }

        if ( record!=null ) ctx.put(VAR_RECORD,record);

        if ( item.getType()==Item.MAKE ) {
            switch ( record.getType() ) {
                case Record.HARDWARE: return VariantTool.selectTemplate(request,ctx,"ViewItem","hardware");
                case Record.SOFTWARE: return VariantTool.selectTemplate(request,ctx,"ViewItem","software");
            }
        }
        if ( item.getType()==Item.DISCUSSION ) return VariantTool.selectTemplate(request,ctx,"ViewItem","discussion");
        if ( item.getType()==Item.ARTICLE ) return VariantTool.selectTemplate(request,ctx,"ViewItem","article");
        if ( item.getType()==Item.DRIVER ) return VariantTool.selectTemplate(request,ctx,"ViewItem","driver");
        return null;
    }
}
