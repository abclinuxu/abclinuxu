/*
 * User: literakl
 * Date: 26.2.2003
 * Time: 12:43:06
 */
package cz.abclinuxu.utils.offline;

import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.cache.EmptyCache;
import cz.abclinuxu.persistance.extra.LimitQualifier;
import cz.abclinuxu.persistance.extra.Qualifier;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.view.ShowObject;
import cz.abclinuxu.servlets.html.view.ViewCategory;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.template.TemplateSelector;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.DateTool;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.paging.Paging;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

import java.io.File;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.*;
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
    static final String VAR_ONLINE_URL = "ONLINE";
    static final String VAR_DATA = "RESULT";

    static final String PORTAL_URL = "http://www.abclinuxu.cz";

    Persistance persistance;
    SQLTool sqlTool;
    DecimalFormat df;
    Configuration config;
    Map indexed = new HashMap(100000);

    public static void main(String[] args) throws Exception {
        Dump dumper = new Dump();
        dumper.execute();
    }

    public Dump() throws Exception {
        ConfigurationManager.getConfigurator().configureMe(this);
        persistance = PersistanceFactory.getPersistance(EmptyCache.class);
        sqlTool = SQLTool.getInstance();
        String templateURI = AbcConfig.calculateDeployedPath("WEB-INF/conf/templates.xml");
        TemplateSelector.initialize(templateURI);

        df = new DecimalFormat("#####");
        df.setDecimalSeparatorAlwaysShown(false);
        df.setMinimumIntegerDigits(6);
        df.setMaximumIntegerDigits(6);

        config = FMUtils.getConfiguration();
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        config.setTemplateUpdateDelay(10000);
        config.setSharedVariable(Constants.VAR_TOOL,new Tools());
        config.setSharedVariable(Constants.VAR_DATE_TOOL,new DateTool());
        config.setSharedVariable(Constants.VAR_SORTER,new Sorters2());
        config.setSharedVariable("DUMP",this);
    }

    void execute() throws Exception {
        File dirRoot = new File("objects");

        Relation hardware = (Relation) persistance.findById(new Relation(Constants.REL_HARDWARE));
        Relation drivers = (Relation) persistance.findById(new Relation(Constants.REL_DRIVERS));

        long start = System.currentTimeMillis();
        dumpTree(drivers, dirRoot, UrlUtils.PREFIX_DRIVERS);
        dumpTree(hardware, dirRoot, UrlUtils.PREFIX_HARDWARE);
//        dumpArticles(dirRoot);
        long end = System.currentTimeMillis();
        System.out.println("Dumping of "+indexed.size()+" documents took "+(end-start)/1000+" seconds.");
    }

    /**
     * Recursively dumps relation and all its objects.
     */
    void dumpTree(Relation relation, File currentDir, String prefix) throws Exception {
        Tools.sync(relation);
        GenericObject obj = relation.getChild();

        File file = getFileName(relation,currentDir);
        if ( obj instanceof Item ) {
            dumpItem(relation, (Item) obj, file, null, prefix);
        } else if ( obj instanceof Category ) {
            dumpCategory(relation, (Category) obj, file, prefix);
        }

        for (Iterator iter = obj.getChildren().iterator(); iter.hasNext();) {
            dumpTree( (Relation)iter.next(), currentDir, prefix);
        }
    }

    /**
     * dumps article into html file.
     */
    void dumpItem(Relation relation, Item item, File file, List parents, String prefix) throws Exception {
        if (hasBeenIndexed(relation))
            return;

        Map env = new HashMap();
        env.put(ShowObject.VAR_RELATION,relation);
        env.put(VAR_ONLINE_URL, PORTAL_URL+prefix+"/show/"+relation.getId());

        if (parents==null) {
            parents = persistance.findParents(relation);
//            parents.add(relation);
        } else {
//            parents = new ArrayList(parents);
//            parents.add(relation);
        }
        env.put(ShowObject.VAR_PARENTS,parents);

        Tools.sync(item);
        env.put(ShowObject.VAR_ITEM,item);
        env.put(ShowObject.VAR_UPPER,relation);
        String name = null;

        if ( item.getType()==Item.DISCUSSION ) {
            name = FMTemplateSelector.select("ShowObject","discussion", env, "offline");
            FMUtils.executeTemplate(name,env,file);
            return;
        }
        if ( item.getType()==Item.DRIVER ) {
            name = FMTemplateSelector.select("ShowObject","driver", env, "offline");
            FMUtils.executeTemplate(name,env,file);
            return;
        }

        Map children = Tools.groupByType(item.getChildren());
        env.put(ShowObject.VAR_CHILDREN_MAP,children);

        if ( item.getType()==Item.ARTICLE ) {
            name = FMTemplateSelector.select("ShowObject","article", env, "offline");
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
                name = FMTemplateSelector.select("ShowObject","hardware", env, "offline");
            else
                return;

            FMUtils.executeTemplate(name,env,file);
        }
    }

    /**
     * dumps category into html file.
     */
    void dumpCategory(Relation relation, Category category, File file, String prefix) throws Exception {
        if (hasBeenIndexed(relation))
            return;

        Map env = new HashMap();
        env.put(ShowObject.VAR_RELATION,relation);
        env.put(VAR_ONLINE_URL, PORTAL_URL+prefix+"/dir/"+relation.getId());

        List parents = persistance.findParents(relation);
        parents.add(relation);
        env.put(ShowObject.VAR_PARENTS,parents);

        Tools.sync(category);
        env.put(ViewCategory.VAR_CATEGORY,category);
        Map children = Tools.groupByType(category.getChildren());
        env.put(ShowObject.VAR_CHILDREN_MAP, children);

        String name = FMTemplateSelector.select("ViewCategory","sekce", env, "offline");
        FMUtils.executeTemplate(name,env,file);
    }

    /**
     * Dumps articles.
     */
    void dumpArticles(File currentDir) throws Exception {
        Relation articles = (Relation) persistance.findById(new Relation(Constants.REL_ARTICLES));
        List sections = articles.getChild().getChildren();
        int total, i, position = 0;
        Runtime runtime = Runtime.getRuntime();

        for (Iterator iter = sections.iterator(); iter.hasNext();) {
            Relation sectionRelation = (Relation) iter.next();
            if (hasBeenIndexed(sectionRelation))
                continue;

            Map env = new HashMap();
            env.put(ShowObject.VAR_RELATION, sectionRelation);
            env.put(VAR_ONLINE_URL, PORTAL_URL + "/clanky/dir/" + sectionRelation.getId());
            env.put(ViewCategory.VAR_CATEGORY, sectionRelation.getChild());

            List parents = persistance.findParents(sectionRelation);
            parents.add(sectionRelation);
            env.put(ShowObject.VAR_PARENTS, parents);

            int sectionId = sectionRelation.getChild().getId();
            total = sqlTool.countArticleRelations(sectionId);
            int count = 30;

            for (i = 0; i < total;) {
                Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(i, count)};
                List data = sqlTool.findArticleRelations(qualifiers, sectionId);
                Tools.syncList(data);
                Paging paging = new Paging(data, i, count, total);
                env.put(VAR_DATA, paging);
                i += data.size();

                String template = FMTemplateSelector.select("ViewCategory", "rubrika", env, "offline");
                File file = getFileName(sectionRelation, currentDir, paging.getPageIndex().intValue());
                FMUtils.executeTemplate(template, env, file);

                for (Iterator iter2 = data.iterator(); iter2.hasNext();) {
                    Relation article = (Relation) iter2.next();
                    file = getFileName(article, currentDir);
                    dumpItem(article, (Item) article.getChild(), file, parents, UrlUtils.PREFIX_CLANKY);
                    System.out.println(position++ + ". " + runtime.freeMemory());
                }
            }
        }
    }

    /**
     * Calculates file name including directories for relation.
     * File name is equal to current directory plus computed file name.
     */
    File getFileName(Relation relation, File currentDir) {
        return getFileName(relation, currentDir, 0);
    }

    File getFileName(Relation relation, File currentDir, int page) {
        int id = relation.getId();
        StringBuffer sb = new StringBuffer();
        insertDirectory(sb, id);
        File dir = new File(currentDir, sb.toString());
        dir.mkdirs();

        sb.setLength(0);
        insertFileName(sb, id, page);
        File file = new File(dir, sb.toString());
        return file;
    }

    /**
     * Calculates file name including directories for relation.
     */
    public String getFile(int relationId, Number page) {
        StringBuffer sb = new StringBuffer();
        insertDirectory(sb, relationId);
        sb.append(File.separatorChar);
        int pageIndex = 0;
        if (page!=null)
            pageIndex = page.intValue();
        insertFileName(sb, relationId, pageIndex);
        return sb.toString();
    }

    /**
     * Calculates file name including directories for relation.
     */
    public String getFile(int relationId) {
        return getFile(relationId, new Integer(0));
    }

    private void insertDirectory(StringBuffer sb, int id) {
        sb.append((char) ('a' + id % 23));
        sb.append('/');
        sb.append((char) ('a' + id % 26));
    }

    private void insertFileName(StringBuffer sb, int id, int page) {
        df.format(id, sb, new FieldPosition(0));
        if (page>0)
            sb.append("_"+page);
        sb.append(".html");
    }

    /**
     * Tests, whether child has been already indexed. If it has not been,
     * its empty clone is stored to mark child as indexed.
     */
    boolean hasBeenIndexed(Relation relation) throws Exception {
        Integer id = new Integer(relation.getId());
        if (indexed.containsKey(id))
            return true;
        indexed.put(id, Boolean.TRUE);
        return false;
    }

    /**
     * Force initialization of subsystems.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
    }
}
