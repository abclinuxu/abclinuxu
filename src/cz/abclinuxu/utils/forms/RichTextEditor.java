/*
 *  Copyright (C) 2008 Leos Literak
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
package cz.abclinuxu.utils.forms;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.data.User;

import java.util.prefs.Preferences;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.dom4j.Element;

/**
 * Component that displays WYSIWYG editor or textarea with/out javascript buttons.
 * @author literakl
 * @since 27.7.2008
 */
public class RichTextEditor {
    public static final String PREF_BROWSERS_TEXTAREA_JS_BUTTONS = "regexp.js.buttons.textarea.ua";
    public static final String PREF_BROWSERS_ONLY_TEXTAREA = "regexp.only.textarea.ua";
    public static final String PREF_DISABLED = "disabled";

    public static final String PARAM_PREFIX = "rte_";

    static Config config = Config.getConfig();

    List<EditorInstance> instances;
    Map<String, String> params = new HashMap<String, String>(2, 1.0f);
    boolean displayControls = true;
    String menu;
    String mode = Constants.RTE_SETTING_ON_REQUEST;

    public RichTextEditor(Map env) {
        Map<String,?> params = (Map) env.get(Constants.VAR_PARAMS);
        for (String key : params.keySet()) {
            if (key.startsWith(PARAM_PREFIX)) {
                this.params.put(key.substring(PARAM_PREFIX.length()), (String) params.get(key));
            }
        }

        User user = (User) env.get(Constants.VAR_USER);
        if (user != null) {
            Element rteSetting = (Element) user.getData().selectSingleNode("/data/settings/rte']");
            if (rteSetting != null) {
                String value = rteSetting.getText();
                if (! Constants.RTE_SETTING_ALWAYS.equalsIgnoreCase(value) && ! Constants.RTE_SETTING_NEVER.equalsIgnoreCase(value))
                    mode = Constants.RTE_SETTING_ON_REQUEST;
                else
                    mode = value.toLowerCase();
            }
        }

        String ua = (String) env.get(Constants.VAR_USER_AGENT);
        if (ua != null) {
            boolean jsDisabled = false;
            if (config.reNoJavascriptBrowsers != null) {
                Matcher matcher = config.reNoJavascriptBrowsers.matcher(ua);
                if (matcher.find()) {
                    mode = Constants.RTE_SETTING_NEVER;
                    displayControls = false;
                    jsDisabled = true;
                }
            }

            if (! jsDisabled && config.reOnlyJsButtonsBrowsers != null) {
                Matcher matcher = config.reOnlyJsButtonsBrowsers.matcher(ua);
                if (matcher.find())
                    mode = Constants.RTE_SETTING_NEVER;
            }
        }

        if (config.disabled)
            mode = Constants.RTE_SETTING_NEVER;
    }

    public void addInstance(EditorInstance rte) {
        if (instances == null)
            instances = new ArrayList<EditorInstance>(1);
        instances.add(rte);
    }

    public List<EditorInstance> getInstances() {
        if (instances == null)
            return Collections.emptyList();
        else
            return instances;
    }

    /**
     * Finds editor instance with given id
     * @param key text area id
     * @return instance or null
     */
    public EditorInstance get(String key) {
        if (instances == null)
            return null;
        for (EditorInstance instance : instances) {
            if (instance.id.equalsIgnoreCase(key))
                return instance;
        }
        return null;
    }

    /**
     * Returns mode for rich text editor. It can be always (displayed automatically), request (displayed
     * upon user's action) or never (unavailabe). The value is taken from current browser (JS support, compatibility),
     * user's preference or last selection (hidden form field).
     * @return on of the constant
     */
    public String getMode() {
        return mode;
    }

    /**
     * Returns true, when javascript controls shall be displayed. This is false for browsers without javascript like lynx.
     * @return true when javascript buttons shall be rendered
     */
    public boolean isDisplayControls() {
        return displayControls;
    }

    /**
     * @return requested toolbar (list of tags)
     */
    public String getMenu() {
        return menu;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }

    public EditorInstance addInstance(String textAreaId, String formId, String menu) {
        EditorInstance editor = new EditorInstance(textAreaId, formId);
        setMenu(menu);
        addInstance(editor);
        return editor;
    }

    /**
     * Holder for one editor instance data.
     */
    public class EditorInstance {
        String id, form; // form identifier and text area id
        String commentedContent;

        public EditorInstance(String id, String formId) {
            this.id = id;
            this.form = formId;
        }

        /**
         * @return text area id
         */
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        /**
         * @return form id
         */
        public String getForm() {
            return form;
        }

        public void setForm(String form) {
            this.form = form;
        }

        public String getCommentedContent() {
            return commentedContent;
        }

        public void setCommentedContent(String commentedContent) {
            this.commentedContent = commentedContent;
        }

        /**
         * If there is special form parameter for this textarea and its value is true, then
         * user was using RTE on previous page and let redisplay it on current page. Otherwise
         * use default value computed for all editors.
         * @return mode constant
         */
        public String getMode() {
            String value = params.get(id);
            if (value == null)
                return mode;

            if ("true".equals(value))
                return Constants.RTE_SETTING_ALWAYS;
            else
                return Constants.RTE_SETTING_ON_REQUEST;
        }
    }

    /**
     * Configuration for RichTextEditor, intentionally separated from main class
     * so it is not reloaded too often or not reconfigured.
     */
    static class Config implements Configurable {
        static Config instance;
        static {
            instance = new Config();
            ConfigurationManager.getConfigurator().configureAndRememberMe(instance);
        }

        Pattern reOnlyJsButtonsBrowsers, reNoJavascriptBrowsers;
        boolean disabled;

        public void configure(Preferences prefs) throws ConfigurationException {
            disabled = prefs.getBoolean(PREF_DISABLED, false);
            String pattern = prefs.get(PREF_BROWSERS_TEXTAREA_JS_BUTTONS, null);
            if (pattern != null)
                reOnlyJsButtonsBrowsers = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            pattern = prefs.get(PREF_BROWSERS_ONLY_TEXTAREA, null);
            if (pattern != null)
                reNoJavascriptBrowsers = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        }

        private Config() {
        }

        public static Config getConfig() {
            return instance;
        }
    }
}
