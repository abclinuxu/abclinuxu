/*
 *  Copyright (C) 2005 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
package cz.abclinuxu.servlets.html.select;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.html.edit.EditCategory;
import cz.abclinuxu.utils.config.impl.AbcConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;

/**
 * Servlet for interactive selection of icon. When user chooses the icon, flow is redirected
 * to <code>PARAM_URL</code> with all parameters propagated to new location plus
 * <code>PARAM_ICON</code> set.<p>
 * <u>Context variables introduced by AbcVelocityServlet</u>
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
public class SelectIcon implements AbcAction {
    public static final String PARAM_URL = "url";
    public static final String PARAM_DIR = "dir";
    public static final String PARAM_ICON = "icon";
    public static final String PARAM_RELOAD = "reload";

    public static final String VAR_DIR = "DIR";
    public static final String VAR_DIRS = "DIRS";
    public static final String VAR_ICONS = "ICONS";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        String reload = request.getParameter(SelectIcon.PARAM_RELOAD);
        if ( "no".equals(reload) ) {
            return actionFinish(request,response,env);
        } else {
            return actionReload(request,env);
        }
    }

    /**
     * Called, when we shall display list of icons
     */
    protected String actionReload(HttpServletRequest request, Map env) throws Exception {
        String path = AbcConfig.calculateDeployedPath("ikony");
        File ikony = new File(path);
        if ( path==null || !ikony.exists() ) throw new Exception("Nemohu nalezt adresar /ikony!");

        File[] iconsContent = ikony.listFiles();
        List dirs = new ArrayList(5);
        for ( int i=0; i<iconsContent.length; i++ ) {
            if ( iconsContent[i].isDirectory() ) {
                dirs.add(iconsContent[i].getName());
            }
        }
        env.put(VAR_DIRS,dirs);

        String dir = request.getParameter(PARAM_DIR);
        if ( dir==null || dir.length()==0 )
            dir = (String) dirs.get(0);
        File file = new File(ikony,dir);
        if ( !file.exists() ) {
            dir = (String) dirs.get(0);
            file = new File(ikony,dir);
        }
        env.put(SelectIcon.VAR_DIR,dir);

        iconsContent = file.listFiles();
        List icons = new ArrayList(12);
        for ( int i=0; i<iconsContent.length; i++ ) {
            if ( iconsContent[i].isFile() ) {
                icons.add(iconsContent[i].getName());
            }
        }
        java.util.Collections.sort(icons);
        env.put(VAR_ICONS,icons);
        return FMTemplateSelector.select("SelectIcon","show",env,request);
    }

    /**
     * Called, when we shall display list of icons.
     * todo replace usage of ServletUtils with env.
     */
    protected String actionFinish(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        String url = request.getParameter(PARAM_URL);
        String dir = "/ikony/"+request.getParameter(PARAM_DIR)+"/";
        String newIcon = dir+request.getParameter(PARAM_ICON);

        Map map = ServletUtils.putParamsToMap(request);
        map.put(PARAM_ICON,newIcon);
        map.remove(PARAM_DIR);
        map.remove(PARAM_URL);
        map.remove(PARAM_RELOAD);
        map.remove(EditCategory.PARAM_CHOOSE_ICON);
        request.getSession().setAttribute(Constants.VAR_PARAMS,map);

        ((UrlUtils) env.get(Constants.VAR_URL_UTILS)).redirect(response, url);
        return null;
    }
}
