/*
 * User: Leos Literak
 * Date: Jan 19, 2003
 * Time: 7:39:58 PM
 */
package cz.abclinuxu.servlets.utils.template;

import java.util.HashMap;
import java.util.List;

/**
 * Holder of Servlet and Action pair.
 */
public class ServletAction {
    String template;
    String content;
    String name;
    List variables;

    public ServletAction(String name) {
        this.name = name;
    }

    /**
     * Each action may have assigned special template, that
     * overrides default template selection mechanism of TemplateSelector.
     * @return forced template or null, if not set
     */
    public String getForcedTemplate() {
        return template;
    }

    /**
     * Set template, that overrides default TemplateSelector's choice.
     */
    public void setForcedTemplate(String template) {
        this.template = template;
    }

    public void setVariables(List variables) {
        this.variables = variables;
    }

    public List getVariables() {
        return variables;
    }

    /**
     * Gets content. Content is page to be rendered.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets content. Content is page to be rendered.
     */
    public void setContent(String content) {
        this.content = content;
    }

    public String toString() {
        return name + " -> " + content;
    }
}
