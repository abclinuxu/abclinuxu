/*
 * User: literakl
 * Date: 26.2.2003
 * Time: 12:43:06
 */
package cz.abclinuxu.utils.offline;

import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.template.TemplateSelector;
import cz.abclinuxu.servlets.html.view.ViewCategory;
import cz.abclinuxu.servlets.html.view.ShowObject;
import cz.abclinuxu.utils.DateTool;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.FMUtils;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

import java.io.File;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * This class is responsible for dumping all
 * objects to hard disc. Objects are stored
 * in files, where directory is computed as
 * relationId modulo 26 represented as ascii
 * character (27%26=1 => 'a') a filename
 * consists of string "relace" and relationId
 * padded to 5 digits.
 */
public class Dump implements Configurable {
    public static final String VAR_ONLINE_URL = "ONLINE";
    static final String PORTAL_URL = "http://www.abclinuxu.cz";

    Persistance persistance;
    DecimalFormat df;
    Configuration config;
    Map indexed = new HashMap(50000);

    public static void main(String[] args) throws Exception {
        Dump dumper = new Dump();
        dumper.execute();
    }

    public Dump() throws Exception {
        ConfigurationManager.getConfigurator().configureMe(this);
        persistance = PersistanceFactory.getPersistance();
        FMUtils fmUtils = new FMUtils();
        String templateURI = AbcConfig.calculateDeployedPath("WEB-INF/conf/templates.xml");
        TemplateSelector.initialize(templateURI);

        df = new DecimalFormat("#####");
        df.setDecimalSeparatorAlwaysShown(false);
        df.setMinimumIntegerDigits(5);
        df.setMaximumIntegerDigits(5);

        config = fmUtils.getConfiguration();
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        config.setTemplateUpdateDelay(100);
        config.setSharedVariable(Constants.VAR_TOOL,new Tools());
        config.setSharedVariable(Constants.VAR_DATE_TOOL,new DateTool());
        config.setSharedVariable(Constants.VAR_SORTER,new Sorters2());
        config.setSharedVariable("DUMP",this);
    }

    void execute() throws Exception {
        File dirRoot = new File("objects");

        Relation articles = (Relation) persistance.findById(new Relation(Constants.REL_ARTICLES));
        Relation hardware = (Relation) persistance.findById(new Relation(Constants.REL_HARDWARE));
        Relation software = (Relation) persistance.findById(new Relation(Constants.REL_SOFTWARE));
        Relation drivers = (Relation) persistance.findById(new Relation(Constants.REL_DRIVERS));

        long start = System.currentTimeMillis();
        dumpTree(articles,dirRoot, "/clanky");
        dumpTree(drivers,dirRoot, "/drivers");
        dumpTree(software,dirRoot, "/software");
        dumpTree(hardware,dirRoot, "/hardware");
        long end = System.currentTimeMillis();
        System.out.println("Dumping of "+indexed.size()+" documents took "+(end-start)/1000+" seconds.");
    }

    /**
     * Recursively dumps relation and all its objects.
     */
    void dumpTree(Relation relation, File currentDir, String prefix) throws Exception {
        Integer id = new Integer(relation.getId());
        if ( indexed.containsKey(id) ) return;
        indexed.put(id,id);

        Tools.sync(relation);
        GenericObject obj = relation.getChild();
        Tools.sync(obj);

        File file = getFileName(relation,currentDir);
        if ( obj instanceof Item ) {
            dumpItem(relation, (Item) obj, file, prefix);
        } else if ( obj instanceof Category ) {
            dumpCategory(relation, (Category) obj, file, prefix);
        }

        for (Iterator iter = obj.getChildren().iterator(); iter.hasNext();) {
            dumpTree( (Relation)iter.next(), currentDir, prefix);
        }
    }

    /**
     * Calculates filename and directory for relation.
     * Algorithm is: directory is computed as
     * relationId modulo 26 represented as ascii
     * character (27%26=1 => 'a') a filename
     * consists of string "relace" and relationId
     * padded to 5 digits.
     */
    File getFileName(Relation relation, File currentDir) {
        int id = relation.getId();
        StringBuffer sb = new StringBuffer();
        sb.append((char)('a'+id%6));
        sb.append((char)('a'+id%26));
        File dir = new File(currentDir,sb.toString());
        dir.mkdirs();

        sb = new StringBuffer();
        sb.append("relace");
        df.format(id,sb,new FieldPosition(0));
        sb.append(".html");

        String filename = sb.toString();
        File file = new File(dir,filename);
        return file;
    }

    /**
     * Calculates filename and directory for relation.
     * Algorithm is: directory is computed as
     * relationId modulo 26 represented as ascii
     * character (27%26=1 => 'a') a filename
     * consists of string "relace" and relationId
     * padded to 5 digits.
     */
    public String getFile(int relationId) {
        StringBuffer sb = new StringBuffer();
        sb.append((char)('a'+relationId%6));
        sb.append((char)('a'+relationId%26));
        sb.append(File.separatorChar);
        sb.append("relace");
        df.format(relationId,sb,new FieldPosition(0));
        sb.append(".html");

        String filename = sb.toString();
        return filename;
    }

    /**
     * dumps article into html file.
     */
    void dumpItem(Relation relation, Item item, File file, String prefix) throws Exception {
        Map env = new HashMap();

        env.put(ShowObject.VAR_RELATION,relation);
        env.put(VAR_ONLINE_URL, PORTAL_URL+prefix+"/ViewRelation?relationId="+relation.getId());
        List parents = persistance.findParents(relation);
        parents.add(relation);
        env.put(ShowObject.VAR_PARENTS,parents);
        Tools.sync(item); env.put(ShowObject.VAR_ITEM,item);
        env.put(ShowObject.VAR_UPPER,relation);
        String name = null;

        if ( item.getType()==Item.DISCUSSION ) {
            name = FMTemplateSelector.select("ViewRelation","discussion", env, "offline");
            FMUtils.executeTemplate(name,env,file);
            return;
        }
        if ( item.getType()==Item.DRIVER ) {
            name = FMTemplateSelector.select("ViewRelation","driver", env, "offline");
            FMUtils.executeTemplate(name,env,file);
            return;
        }

        Map children = Tools.groupByType(item.getChildren());
        env.put(ShowObject.VAR_CHILDREN_MAP,children);

        if ( item.getType()==Item.ARTICLE ) {
            name = FMTemplateSelector.select("ViewRelation","article", env, "offline");
            FMUtils.executeTemplate(name,env,file);
            return;
        }

        if ( item.getType()==Item.MAKE ) {
            List records = (List) children.get(Constants.TYPE_RECORD);
            Record record = null;
            if ( records!=null && records.size()>0 )
                record = (Record) ((Relation)records.get(0)).getChild();
            else
                return;
            if ( ! record.isInitialized() )
                persistance.synchronize(record);

            if ( record.getType()== Record.HARDWARE )
                name = FMTemplateSelector.select("ViewRelation","hardware", env, "offline");
            else if ( record.getType()== Record.SOFTWARE )
                name = FMTemplateSelector.select("ViewRelation","software", env, "offline");

            FMUtils.executeTemplate(name,env,file);
        }
    }

    /**
     * dumps category into html file.
     */
    void dumpCategory(Relation relation, Category category, File file, String prefix) throws Exception {
        Map env = new HashMap();

        env.put(ShowObject.VAR_RELATION,relation);
        env.put(VAR_ONLINE_URL, PORTAL_URL+prefix+"/ViewCategory?relationId="+relation.getId());
        List parents = persistance.findParents(relation);
        parents.add(relation);
        env.put(ShowObject.VAR_PARENTS,parents);
        env.put(ViewCategory.VAR_CATEGORY,category);

        Tools.sync(category);
        Tools.syncList(category.getChildren());

        String name = FMTemplateSelector.select("ViewCategory","sekce", env, "offline");
        FMUtils.executeTemplate(name,env,file);
    }

    /**
     * Force initialization of subsystems.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
    }
}
