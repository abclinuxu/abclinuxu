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
    HashMap mappings;
    String template;

    public ServletAction() {
        this(3);
    }

    public ServletAction(int size) {
        mappings = new HashMap(size);
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

    /**
     * Add mapping.
     * @param template name of template
     * @param content name of content
     * @param variables optional list of Variables
     */
    public void addMapping(String template, String content, List variables) {
        Mapping mapping = new Mapping(content);
        mapping.setVariables(variables);
        mappings.put(template, mapping);
    }

    /**
     * Finds mapping for selected template. If mapping is not found for given
     * template, default template's mapping is used.
     * @return found mapping
     */
    public Mapping getMapping(String template) {
        Mapping mapping = (Mapping) mappings.get(template);
        if ( mapping==null )
            mapping = (Mapping) mappings.get(TemplateSelector.DEFAULT_TEMPLATE);
        return mapping;
    }
}
