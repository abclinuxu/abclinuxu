/*
 * Created by IntelliJ IDEA.
 * User: literakl
 * Date: Jan 6, 2002
 * Time: 9:53:11 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.servlets.utils.VelocityHelper;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.servlets.edit.EditCategory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.*;

/**
 * Servlet for interactive selection of icon. When user chooses the icon, flow is redirected
 * to <code>PARAM_URL</code> with all parameters propagated to new location plus
 * <code>PARAM_ICON</code> set.<p>
 * <u>Context variables introduced by AbcServlet</u>
 * <dl>
 * <dt><code>VAR_ICONS</code></dt>
 * <dd>Holds names of files in directory /ikony/$DIR.</dd>
 * <dt><code>VAR_DIR</code></dt>
 * <dd>Shortcut to <code>PARAM_DIR</code>.</dd>
 * <dt><code>VAR_DIRS</code></dt>
 * <dd>Contains list of directories.</dd>
 * </dl>
 * <u>Parameters used by SelectIcon</u>
 * <dl>
 * <dt><code>PARAM_URL</code></dt>
 * <dd>Where to redirect browser.</dd>
 * <dt><code>PARAM_DIR</code></dt>
 * <dd>Actual selected subdirectory of directory /ikony.</dd>
 * <dt><code>PARAM_ICON</code></dt>
 * <dd>When recieved by SelectIcons, holds name of selected icon in VAR_DIR diectory,
 * when sent to <code>PARAM_URL</code>, it holds complete path to icon.</dd>
 * <dt><code>PARAM_RELOAD</code></dt>
 * <dd>Indicates, whether user has changed directory.</dd>
 * </dl>
 */
public class SelectIcon extends AbcServlet {
    public static final String PARAM_URL = "url";
    public static final String PARAM_DIR = "dir";
    public static final String PARAM_ICON = "icon";
    public static final String PARAM_RELOAD = "reload";

    public static final String VAR_DIR = "DIR";
    public static final String VAR_DIRS = "DIRS";
    public static final String VAR_ICONS = "ICONS";


    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        String reload = request.getParameter(SelectIcon.PARAM_RELOAD);
        if ( "no".equals(reload) ) {
            return actionFinish(request,response,ctx);
        } else {
            return actionReload(request,ctx);
        }
    }

    /**
     * Called, when we shall display list of icons
     */
    protected Template actionReload(HttpServletRequest request, Context ctx) throws Exception {
        String path = getServletContext().getRealPath("/ikony");
        File ikony = new File(path);
        if ( path==null || !ikony.exists() ) throw new Exception("Nemohu nalezti adresar /ikony!");

        File[] iconsContent = ikony.listFiles();
        List dirs = new ArrayList(5);
        for ( int i=0; i<iconsContent.length; i++ ) {
            if ( iconsContent[i].isDirectory() ) {
                dirs.add(iconsContent[i].getName());
            }
        }
        ctx.put(SelectIcon.VAR_DIRS,dirs);

        String dir = request.getParameter(SelectIcon.PARAM_DIR);
        if ( dir==null || dir.length()==0 ) dir = (String) dirs.get(0);
        File file = new File(ikony,dir);
        if ( !file.exists() ) {
            dir = (String) dirs.get(0);
            file = new File(ikony,dir);
        }
        ctx.put(SelectIcon.VAR_DIR,dir);

        iconsContent = file.listFiles();
        List icons = new ArrayList(12);
        for ( int i=0; i<iconsContent.length; i++ ) {
            if ( iconsContent[i].isFile() ) {
                icons.add(iconsContent[i].getName());
            }
        }
        java.util.Collections.sort(icons);
        ctx.put(SelectIcon.VAR_ICONS,icons);
        return getTemplate("view/selectIcon.vm");
    }

    /**
     * Called, when we shall display list of icons
     */
    protected Template actionFinish(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        String url = request.getParameter(SelectIcon.PARAM_URL);
        String dir = "/ikony/"+request.getParameter(SelectIcon.PARAM_DIR)+"/";
        String icon = dir+request.getParameter(SelectIcon.PARAM_ICON);

        Map map = VelocityHelper.putParamsToMap(request,null);
        map.remove(SelectIcon.PARAM_DIR);
        map.remove(SelectIcon.PARAM_ICON);
        map.remove(SelectIcon.PARAM_URL);
        map.remove(SelectIcon.PARAM_RELOAD);
        map.remove(EditCategory.PARAM_CHOOSE_ICON);

        HttpSession session = request.getSession();
        session.setAttribute(AbcServlet.ATTRIB_PARAMS,map);

        String newUrl = url + "?icon="+icon;
        UrlUtils.redirect(newUrl,response,ctx);
        return null;
    }
}
