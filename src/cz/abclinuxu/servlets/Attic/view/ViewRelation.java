/*
 * Created by IntelliJ IDEA.
 * User: literakl
 * Date: Jan 3, 2002
 * Time: 8:14:00 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcVelocityServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Record;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Tools;
import cz.abclinuxu.AbcException;
import cz.abclinuxu.exceptions.MissingArgumentException;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import java.util.List;
import java.util.Map;

/**
 * Servlet, which loads Relation specified by parameter <code>relationId</code>
 * and redirects execution to servlet handling one of relation's GenericObjects.<p>
 * <u>Context variables introduced by AbcVelocityServlet</u>
 * <dl>
 * <dt><code>VAR_RELATION</code></dt>
 * <dd>instance of Relation.</dd>
 * <dt><code>VAR_PARENTS</code></dt>
 * <dd>List of parental relations. Last element is current relation.</dd>
 * </dl>
 * <u>Parameters used by ViewRelation</u>
 * <dl>
 * <dt>relationId</dt>
 * <dd>PK of asked relation, number.</dd>
 * </dl>
 */
public class ViewRelation extends AbcFMServlet {
    public static final String PARAM_RELATION_ID = "relationId";
    public static final String VAR_RELATION = "RELATION";
    public static final String VAR_PARENTS = "PARENTS";
    public static final String VAR_ITEM = "ITEM";
    /** Relation upper to selected relation Item-Record */
    public static final String VAR_UPPER = "REL_ITEM";
    /** children relation of Item, grouped by their type */
    public static final String VAR_CHILDREN_MAP = "CHILDREN";

    Persistance persistance = PersistanceFactory.getPersistance();

    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_ID,Relation.class,params);
        if ( relation==null ) {
            throw new MissingArgumentException("Parametr relationId je prázdný!");
        }

        Tools.sync(relation);
        env.put(VAR_RELATION,relation);
        List parents = persistance.findParents(relation);
        env.put(VAR_PARENTS,parents);

        if ( relation.getParent() instanceof Item )
            return processItem(request,env,relation,parents);
        else if ( relation.getParent() instanceof Category ) {
            if ( relation.getChild() instanceof Item )
                return processItem(request,env,relation,parents);
            else
                return ViewCategory.processCategory(request,env,relation,parents);
        }
        return null;
    }

    /**
     * Processes item - like article, discussion, driver etc.
     * @return template to be rendered
     */
    String processItem(HttpServletRequest request, Map env, Relation relation, List parents) throws Exception {
        Item item = null;
        Record record = null;
        Relation upper = null;

        if ( relation.getChild() instanceof Item ) {
            item = (Item) relation.getChild();
            upper = relation;
            if ( parents!=null ) parents.add(upper);
        } else if ( relation.getParent() instanceof Item ) {
            item = (Item) relation.getParent();
            upper = new Relation(relation.getUpper());
            record = (Record) relation.getChild();
        }

        Tools.sync(item); env.put(VAR_ITEM,item);
        Tools.sync(upper); env.put(VAR_UPPER,upper);

        if ( item.getType()==Item.DISCUSSION ) {
            return FMTemplateSelector.select("ViewRelation","discussion",env,request,null);
        }
        if ( item.getType()==Item.DRIVER )
            return FMTemplateSelector.select("ViewRelation","driver",env,request,null);

        Map children = Tools.groupByType(item.getContent());
        env.put(VAR_CHILDREN_MAP,children);

        if ( item.getType()==Item.ARTICLE )
            return FMTemplateSelector.select("ViewRelation","article",env,request,null);

        if ( record==null ) {
            List records = (List) children.get(Constants.TYPE_RECORD);
            if ( records!=null && records.size()>0 )
                record = (Record) ((Relation)records.get(0)).getChild();
        }

        if ( item.getType()==Item.MAKE && record!=null ) {
            if ( ! record.isInitialized() )
                persistance.synchronize(record);
            switch ( record.getType() ) {
                case Record.HARDWARE: return FMTemplateSelector.select("ViewRelation","hardware",env,request,null);
                case Record.SOFTWARE: return FMTemplateSelector.select("ViewRelation","software",env,request,null);
            }
        }
        return null;
    }
}
