/*
 * User: literakl
 * Date: Feb 6, 2002
 * Time: 6:32:36 PM
 */
package cz.abclinuxu.servlets.edit;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.*;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.exceptions.PersistanceException;
import cz.abclinuxu.exceptions.MissingArgumentException;

import org.dom4j.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * todo archive last three versions of driver
 */
public class EditDriver extends AbcFMServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditDriver.class);

    public static final String PARAM_NAME = "name";
    public static final String PARAM_VERSION = "version";
    public static final String PARAM_URL = "url";
    public static final String PARAM_NOTE = "note";
    public static final String PARAM_RELATION = "relationId";
    public static final String PARAM_DRIVER = "driverId";

    public static final String VAR_DRIVER = "DRIVER";
    public static final String VAR_RELATION = "RELATION";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADD_STEP2 = "add2";


    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        Persistance persistance = PersistanceFactory.getPersistance();
        String action = (String) params.get(PARAM_ACTION);

        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION,Relation.class,params);
        if ( relation!=null ) {
            persistance.synchronize(relation);
            persistance.synchronize(relation.getChild());
            env.put(VAR_RELATION,relation);
        } else throw new Exception("Chybí parametr relationId!");

        Item driver = (Item) InstanceUtils.instantiateParam(PARAM_DRIVER, Item.class, params);
        if ( driver!=null ) {
            persistance.synchronize(driver);
            env.put(VAR_DRIVER,driver);
        }

        // check permissions
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        if ( driver!=null && user.getId()!=driver.getOwner() && !user.hasRole(Roles.ROOT) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if ( ACTION_ADD.equals(action) )
            return actionAddStep(request, env);

        if ( ACTION_ADD_STEP2.equals(action) )
            return actionAddStep2(request, response, env);

        throw new MissingArgumentException("Chybí parametr action!");
    }

    protected String actionAddStep(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Item driver = (Item) env.get(VAR_DRIVER);

        if ( driver!=null ) {
            Document document = driver.getData();
            Node node = document.selectSingleNode("data/name");
            if ( node!=null ) params.put(PARAM_NAME,node.getText());
            node = document.selectSingleNode("data/version");
            if ( node!=null ) params.put(PARAM_VERSION,node.getText());
            node = document.selectSingleNode("data/url");
            if ( node!=null ) params.put(PARAM_URL,node.getText());
            node = document.selectSingleNode("data/note");
            if ( node!=null ) params.put(PARAM_NOTE,node.getText());
        }

        return FMTemplateSelector.select("EditDriver","add",env,request);
    }

    /**
     * add: if driver exists, its content is replaced by newer version. otherwise it is created.
     */
    protected String actionAddStep2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistance persistance = PersistanceFactory.getPersistance();

        Relation upper = (Relation) env.get(VAR_RELATION);
        User user = (User) env.get(Constants.VAR_USER);

        boolean error = false;
        String name = (String) params.get(PARAM_NAME);
        if ( name==null || name.length()==0 ) {
            ServletUtils.addError(PARAM_NAME,"Zadejte název ovladaèe!",env,null);
            error = true;
        }
        String version = (String) params.get(PARAM_VERSION);
        if ( version==null || version.length()==0 ) {
            ServletUtils.addError(PARAM_VERSION,"Zadejte verzi ovladaèe!",env,null);
            error = true;
        }
        String url = (String) params.get(PARAM_URL);
        if ( url==null || url.length()==0 ) {
            ServletUtils.addError(PARAM_URL,"Zadejte adresu ovladaèe!",env,null);
            error = true;
        } else if ( url.indexOf("tp://")==-1 || url.length()<12 ) {
            ServletUtils.addError(PARAM_URL,"Neplatná adresa ovladaèe!",env,null);
            error = true;
        }
        String note = (String) params.get(PARAM_NOTE);

        if ( error ) {
            return FMTemplateSelector.select("EditDriver","add",env,request);
        }

        boolean created = true;
        Item driver = (Item) InstanceUtils.instantiateParam(PARAM_DRIVER,Item.class,params);
        Document document = null;

        if ( driver!=null ) {
            try {
                persistance.synchronize(driver);
                document = driver.getData();
                created = false;
            } catch (PersistanceException e) {
                log.warn("Driver doesn't exist, creating new one.",e);
                driver = null;
            }
        }

        if ( driver==null ) {
            driver = new Item(0,Item.DRIVER);
            document = DocumentHelper.createDocument();
        }

        DocumentHelper.makeElement(document,"data/name").setText(name);
        DocumentHelper.makeElement(document,"data/version").setText(version);
        DocumentHelper.makeElement(document,"data/url").setText(url);
        if ( note!=null && note.length()>0 ) {
            DocumentHelper.makeElement(document,"data/note").setText(note);
        }

        driver.setData(document);
        driver.setOwner(user.getId());

        try {
            if ( created ) {
                persistance.create(driver);
                Relation relation = new Relation(upper.getChild(),driver,upper.getId());
                persistance.create(relation);
            } else {
                persistance.update(driver);
            }
        } catch (PersistanceException e) {
            ServletUtils.addError(Constants.ERROR_GENERIC,e.getMessage(),env, null);
            return FMTemplateSelector.select("EditDriver","add",env,request);
        }

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/ViewRelation?relationId="+Constants.REL_DRIVERS);
        return null;
    }
}
