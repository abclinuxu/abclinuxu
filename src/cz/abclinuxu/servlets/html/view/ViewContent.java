/*
 * User: literakl
 * Date: 21.9.2004
 * Time: 19:10:00
 */
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Displays content (Item.type==11)
 */
public class ViewContent {
    public static final String VAR_CONTENT = ShowObject.VAR_ITEM;

    /**
     * Env must contain VAR_CONTENT already.
     */
    public static String show(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        return FMTemplateSelector.select("ViewContent", "view", env, request);
    }
}
