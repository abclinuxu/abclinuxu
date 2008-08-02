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
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.data.User;

import java.util.prefs.Preferences;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.dom4j.Element;

/**
 * Component that displays WYSIWYG editor or textarea with/out javascript buttons.
 * @author literakl
 * @since 27.7.2008
 */
public class RichTextEditor {
    public static final String PREF_BROWSERS_TEXTAREA_JS_BUTTONS = "js.buttons.textarea.ua";
    public static final String PREF_BROWSERS_ONLY_TEXTAREA = "only.textarea.ua";

    public static final String SETTING_TEXTAREA = "textarea";
    public static final String SETTING_WYSIWYG = "wysiwyg";

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

        }
    }

    public void addInstance(EditorInstance rte) {
        if (instances == null)
            instances = new ArrayList<EditorInstance>();
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

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

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

        public String getInputMode() {
            return inputMode;
        }

        public void setInputMode(String inputMode) {
            this.inputMode = inputMode;
        }
    }

    static class Config implements Configurable {
        String onlyTextArea, jsButtonsTextArea;

        public void configure(Preferences prefs) throws ConfigurationException {
            onlyTextArea = prefs.get(PREF_BROWSERS_ONLY_TEXTAREA, null);
            jsButtonsTextArea = prefs.get(PREF_BROWSERS_TEXTAREA_JS_BUTTONS, null);
        }
    }
}
