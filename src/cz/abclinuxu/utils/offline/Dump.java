/*
 * User: literakl
 * Date: 26.2.2003
 * Time: 12:43:06
 */
package cz.abclinuxu.utils.offline;

import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.EmptyCache;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.view.ViewRelation;
import cz.abclinuxu.servlets.view.ViewCategory;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.template.TemplateSelector;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.utils.Tools;
import cz.abclinuxu.utils.DateTool;
import cz.abclinuxu.utils.Sorters2;

import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Template;
import freemarker.ext.beans.BeansWrapper;

/**
 * This class is responsible for dumping all
 * objects to hard disc. Objects are stored
 * in files, where directory is computed as
 * relationId modulo 26 represented as ascii
 * character (27%26=1 => 'a') a filename
 * consists of string "relace" and relationId
 * padded to 5 digits.
 */
public class Dump {
    Persistance persistance;
    DecimalFormat df;
    Configuration config;
    Map indexed = new HashMap(30000);

    public static void main(String[] args) throws Exception {
        Dump dumper = new Dump();
        dumper.execute();
    }

    public Dump() throws Exception {
        persistance = PersistanceFactory.getPersistance();
        TemplateSelector.initialize("/home/literakl/abc/deploy/WEB-INF/conf/templates.xml");

        df = new DecimalFormat("#####");
        df.setDecimalSeparatorAlwaysShown(false);
        df.setMinimumIntegerDigits(5);
        df.setMaximumIntegerDigits(5);

        config = Configuration.getDefaultConfiguration();
        config.setDefaultEncoding("ISO-8859-2");
        config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        config.setObjectWrapper(BeansWrapper.getDefaultInstance());
        config.setStrictSyntaxMode(true);
        config.setTemplateUpdateDelay(1);
        config.setDirectoryForTemplateLoading(new File("/home/literakl/abc/deploy/WEB-INF/freemarker"));
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
        dumpTree(articles,dirRoot);
        dumpTree(drivers,dirRoot);
        dumpTree(software,dirRoot);
        dumpTree(hardware,dirRoot);
        long end = System.currentTimeMillis();
        System.out.println("Dumping of "+indexed.size()+" documents took "+(end-start)/1000+" seconds.");
    }

    /**
     * Recursively dumps relation and all its objects.
     */
    void dumpTree(Relation relation, File currentDir) throws Exception {
        Integer id = new Integer(relation.getId());
        if ( indexed.containsKey(id) ) return;
        indexed.put(id,id);

        Tools.sync(relation);
        GenericObject obj = relation.getChild();
        Tools.sync(obj);

        File file = getFileName(relation,currentDir);
        dumpObject(relation,obj,file);

        for (Iterator iter = obj.getContent().iterator(); iter.hasNext();) {
            dumpTree( (Relation)iter.next(),currentDir );
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
        sb.append((char)('a'+id%3));
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
        sb.append((char)('a'+relationId%3));
        sb.append((char)('a'+relationId%26));
        sb.append(File.separatorChar);
        sb.append("relace");
        df.format(relationId,sb,new FieldPosition(0));
        sb.append(".html");

        String filename = sb.toString();
        return filename;
    }

    /**
     * Dumps html file with formatted object obj into
     * file file.
     */
    void dumpObject(Relation relation, GenericObject obj, File file) throws Exception {
        if ( obj instanceof Item ) {
            dumpItem(relation,(Item)obj,file);
            return;
        }
        if ( obj instanceof Category ) {
            dumpCategory(relation,(Category)obj,file);
            return;
        }
    }

    /**
     * dumps article into html file.
     */
    void dumpItem(Relation relation, Item item, File file) throws Exception {
        Map env = new HashMap();

        env.put(ViewRelation.VAR_RELATION,relation);
        List parents = persistance.findParents(relation);
        parents.add(relation);
        env.put(ViewRelation.VAR_PARENTS,parents);
        Tools.sync(item); env.put(ViewRelation.VAR_ITEM,item);
        env.put(ViewRelation.VAR_UPPER,relation);
        String name = null;

        if ( item.getType()==Item.DISCUSSION ) {
            name = FMTemplateSelector.select("ViewRelation","discussion","offline",env);
            processTemplate(name,env,file);
            return;
        }
        if ( item.getType()==Item.DRIVER ) {
            name = FMTemplateSelector.select("ViewRelation","driver","offline",env);
            processTemplate(name,env,file);
            return;
        }

        Map children = Tools.groupByType(item.getContent());
        env.put(ViewRelation.VAR_CHILDREN_MAP,children);

        if ( item.getType()==Item.ARTICLE ) {
            name = FMTemplateSelector.select("ViewRelation","article","offline",env);
            processTemplate(name,env,file);
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
                name = FMTemplateSelector.select("ViewRelation","hardware","offline",env);
            else if ( record.getType()== Record.SOFTWARE )
                name = FMTemplateSelector.select("ViewRelation","software","offline",env);

            processTemplate(name,env,file);
        }
    }

    /**
     * dumps category into html file.
     */
    void dumpCategory(Relation relation, Category category, File file) throws Exception {
        Map env = new HashMap();

        env.put(ViewRelation.VAR_RELATION,relation);
        List parents = persistance.findParents(relation);
        parents.add(relation);
        env.put(ViewRelation.VAR_PARENTS,parents);
        env.put(ViewCategory.VAR_CATEGORY,category);

        Tools.sync(category);
        Tools.sync(category.getContent());

        String name = FMTemplateSelector.select("ViewCategory","sekce","offline",env);
        processTemplate(name,env,file);
    }

    /**
     * processes given template and creates file.
     */
    void processTemplate(String template, Map env, File file) throws Exception {
        Template tpl = config.getTemplate(template);
        FileWriter writer = new FileWriter(file);
        tpl.process(env,writer);
        writer.close();
    }
}
