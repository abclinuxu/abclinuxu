/*
 * User: Leos Literak
 * Date: Nov 26, 2002
 * Time: 9:16:28 AM
 */
package cz.abclinuxu.servlets.utils;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import cz.abclinuxu.utils.Misc;

/**
 * Super class for all template selectors.
 * Its responsibility is to read configuration.
 * Inherited classes will define contract for specific
 * template engine.
 */
public class TemplateSelector {

    /** if not overriden, this variant will be used */
    static String DEFAULT_VARIANT = "web";

    /** custom selection of variant */
    public static final String PARAM_VARIANTA = "varianta";

    /** this variable holds name of template to be included */
    public static final String VAR_CONTENT_TEMPLATE = "CONTENT";
    /** this variable holds type of browser, which is used by visitor */
    public static final String VAR_BROWSER = "BROWSER";

    /** lynx broswer */
    public static final String BROWSER_LYNX = "LYNX";
    /** mozilla browser */
    public static final String BROWSER_MOZILLA = "MOZILLA";
    /** Internet Explorer browser */
    public static final String BROWSER_IE = "IE";
    /** forbidden mirroring tool like wget or Custo */
    public static final String BROWSER_MIRROR = "MIRROR";
    /** plucker PDA browser */
    public static final String BROWSER_PLUCKER = "PLUCKER";
    /** other browser */
    public static final String BROWSER_OTHER = "OTHER";

    /**
     * Here we store mappings. key is concatenation of servlet name and action, value is map
     * where key is variant and value is templet name.
     */
    static HashMap mappings = new HashMap(75,0.8f);

    /**
     * Loads configuration and instantiates singleton of VelocityTemplateSelector.
     * @param filename name of configuration file
     * @return initialized instance of VelocityTemplateSelector
     */
    public static void initialize(String filename) throws Exception {
        Document document = new SAXReader().read(filename);
        List tagServlets = document.getRootElement().elements("servlet");

        for (Iterator servletIter = tagServlets.iterator(); servletIter.hasNext();) { // for each servlet
            Element tagServlet = (Element) servletIter.next();
            String servlet = tagServlet.attributeValue("name");
            List tagActions = tagServlet.elements("action");

            for (Iterator actionIter = tagActions.iterator(); actionIter.hasNext();) { // for each action
                Element tagAction = (Element) actionIter.next();
                String action = tagAction.attributeValue("name");
                List tagMappings = tagAction.elements("mapping");
                HashMap mappingsMap = new HashMap(tagMappings.size()+1,0.9999f);

                for (Iterator mappingIter = tagMappings.iterator(); mappingIter.hasNext();) { // for each mapping
                    Element tagMapping = (Element) mappingIter.next();
                    String variant = tagMapping.attributeValue("variant");
                    Mapping mapping = new Mapping(tagMapping.attributeValue("template"));

                    List tagVariables = tagMapping.elements("var");
                    if ( !Misc.empty(tagVariables) ) {
                        List vars = new ArrayList(tagVariables.size());
                        for (Iterator varIter = tagVariables.iterator(); varIter.hasNext();) { // for each var
                            Element tagVar = (Element) varIter.next();
                            Variable var = createVar(tagVar);
                            vars.add(var);
                        }
                        mapping.setVariables(vars);
                    }

                    mappingsMap.put(variant,mapping);
                }

                String name = servlet + action;
                mappings.put(name,mappingsMap);
            }
        }
    }

    /**
     * Creates Variable of tag Var.
     */
    static Variable createVar(Element tag) {
        String attribName = tag.attributeValue("name");
        String attribValue = tag.attributeValue("value");
        String attribType = tag.attributeValue("type");

        Object value = null;
        if ( Misc.same(attribType,"Boolean") ) {
            value = Boolean.valueOf(attribValue);
        } else if ( Misc.same(attribType,"Lazy") ) {
            value = new LazyVar(attribValue);
        } else {
            value = attribValue;
        }

        return new Variable(attribName,value);
    }

    /**
     * This class holds attribute of one mapping. List variables holds
     * instances of Variable.
     */
    static class Mapping {
        String template;
        List variables;

        public Mapping(String template) {
            this.template = template;
        }

        public void setVariables(List variables) {
            this.variables = variables;
        }

        public String getTemplate() {
            return template;
        }

        public List getVariables() {
            return variables;
        }
    }

    /**
     * This class holds one variable, which shall be passed to template engine.
     */
    static class Variable {
        String name;
        Object value;

        public Variable(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }
    }

    /**
     * This class holds value, which shall be evaluated
     * at runtime.
     */
    static class LazyVar {
        String value;

        public LazyVar(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
