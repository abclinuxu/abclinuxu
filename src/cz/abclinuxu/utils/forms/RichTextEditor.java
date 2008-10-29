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

    public static final String SETTING_TEXTAREA = "textarea";
    public static final String SETTING_WYSIWYG = "wysiwyg";

    static Config config = Config.getConfig();

    List<EditorInstance> instances;
    boolean wysiwygMode = true;
    boolean displayJavascriptButtons = true;

    public RichTextEditor(Map env) {
        User user = (User) env.get(Constants.VAR_USER);
        if (user != null) {
            Element rteSetting = (Element) user.getData().selectSingleNode("/data/settings/rte']");
            if (rteSetting != null && ! SETTING_WYSIWYG.equalsIgnoreCase(rteSetting.getText()))
                wysiwygMode = false;
        }

        String ua = (String) env.get(Constants.VAR_USER_AGENT);
        if (ua != null) {
            boolean jsDisabled = false;
            if (config.reNoJavascriptBrowsers != null) {
                Matcher matcher = config.reNoJavascriptBrowsers.matcher(ua);
                if (matcher.find()) {
                    wysiwygMode = false;
                    displayJavascriptButtons = false;
                    jsDisabled = true;
                }
            }

            if (! jsDisabled && config.reOnlyJsButtonsBrowsers != null) {
                Matcher matcher = config.reOnlyJsButtonsBrowsers.matcher(ua);
                if (matcher.find())
                    wysiwygMode = false;
            }
        }

        if (wysiwygMode && config.disabled)
            wysiwygMode = false;
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
     * True when rich text editor with WYSIWYG display shall be presented instead of text area. This is true when
     * current user agent is not matched by any regexp from configuration. For example Konqueror is not supported
     * by current rich text editor (FCK). The user can configure to switch off rich text editor.
     * @return whether WYSIWYG editor can be displayed
     */
    public boolean isWysiwygMode() {
        return wysiwygMode;
    }

    /**
     * When WYSIWYG support is disabled for current user agent, then this property influences, whether javascript
     * formatting buttons shall be rendered. For example lynx does not support javascript.
     * @return true when javascript buttons can be rendered
     */
    public boolean isDisplayJavascriptButtons() {
        return displayJavascriptButtons;
    }

    /**
     * Holder for one editor instance data.
     */
    public static class EditorInstance {
        String id, form; // form identifier and text area id
        String commentedContent;
        String inputMode; // constant for allowed HTML tags

        public EditorInstance(String id, String formId, String inputMode) {
            this.id = id;
            this.form = formId;
            this.inputMode = inputMode;
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
         * @return requested toolbar (list of tags)
         */
        public String getInputMode() {
            return inputMode;
        }

        public void setInputMode(String inputMode) {
            this.inputMode = inputMode;
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
