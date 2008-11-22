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
package cz.abclinuxu.utils.parser.clean;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.parser.clean.exceptions.HtmlCheckException;
import cz.abclinuxu.utils.parser.clean.impl.Definition;
import cz.abclinuxu.utils.parser.clean.impl.Attribute;
import cz.abclinuxu.utils.parser.clean.impl.Checker;
import cz.abclinuxu.utils.parser.clean.impl.AttributeChecker;
import cz.abclinuxu.utils.parser.clean.impl.Tag;
import cz.abclinuxu.utils.parser.clean.impl.DefinitionChecker;

import java.util.prefs.Preferences;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;

import org.dom4j.Element;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

/**
 * This class is responsible for keeping HTML content safe. E.g. it will blocks malicious (or stupid) user's
 * input, that can harm portal's design or XSS. The rules are defined in file tag_checker.xml and it is possible
 * to reload its definition.
 * @author literakl
 * @since 22.11.2008
 */
public class HtmlChecker implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HtmlChecker.class);

    static EnumMap<Rules, Definition> ruleDefinitions;
    String configFileName;

    /**
     * Checks HTML snippet, that it contains valid HTML (no crossed tags, no missing closing tags),
     * only tags / attributes allowed by Rules are present and HTML is safe (no XSS).
     * @param rules identifier for rules to be used
     * @param htmlText HTML snippet
     * @throws HtmlCheckException When rules are broken.
     */
    public static void check(Rules rules, String htmlText) throws HtmlCheckException {
        if (htmlText == null || htmlText.length() == 0)
            return;
        if (ruleDefinitions == null)
            throw new HtmlCheckException("Třída nebyla inicializována!");

        Definition definition = ruleDefinitions.get(rules);
        if (definition == null) {
            log.error("Pravidlo " + rules + " pro kontrolu HTML kódu nebylo nalezeno!");
            throw new HtmlCheckException("Pravidlo " + rules + " pro kontrolu HTML kódu nebylo nalezeno!");
        } else
            definition.check(htmlText);
    }

    /**
     * Set absolute file name for configuration of this class.
     * @param configFileName absolute file name
     */
    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    /**
     * Configuration for this class. Configuration file name must be initialized first.
     * @param prefs preferences
     * @throws ConfigurationException
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        if (configFileName == null)
            throw new ConfigurationException("Není nastavena cesta ke konfiguračnímu souboru!");
        log.info("Načítám konfiguraci z " + configFileName);

        try {
            EnumMap<Rules, Definition> newDefinitions = new EnumMap<Rules, Definition>(Rules.class);
            Map<String, Attribute> predefinedAttributes = new HashMap<String, Attribute>();
            Map<String, Tag> predefinedTags = new HashMap<String, Tag>();

            Document document = new SAXReader().read(configFileName);
            Element elDefinitions = document.getRootElement().element("definitions");
            parseAttributeDefinitions(elDefinitions, predefinedAttributes);
            parseTagDefinitions(elDefinitions, predefinedTags, predefinedAttributes);
            List elRules = document.getRootElement().elements("rules");
            parseRules(elRules, predefinedTags, predefinedAttributes, newDefinitions);

            predefinedAttributes.clear();
            predefinedTags.clear();

            ruleDefinitions = newDefinitions;
            log.info("Hotovo");
        } catch (DocumentException e) {
            throw new ConfigurationException("Chyba při načítání konfiguračního souboru!", e);
        }
    }

    /**
     * Parse /config/definitions/attributes element and prepares default Attributes that can be referenced
     * from Tags.
     * @param elDefinitions element
     * @param predefinedAttributes map to store found Attributes
     */
    private void parseAttributeDefinitions(Element elDefinitions, Map<String, Attribute> predefinedAttributes) {
        for (Iterator iter = elDefinitions.element("attributes").elements().iterator(); iter.hasNext();) {
            Element elAttribute = (Element) iter.next();
            Attribute attribute = parseAttribute(elAttribute, predefinedAttributes);
            predefinedAttributes.put(attribute.getId(), attribute);
        }
    }

    /**
     * Parse /config/definitions/tags element and prepares default Tags that can be referenced from Definitions.
     * @param elDefinitions element
     * @param predefinedTags map to store found Tags
     * @param predefinedAttributes map from which to load referenced Attributes
     */
    private void parseTagDefinitions(Element elDefinitions, Map<String, Tag> predefinedTags, Map<String, Attribute> predefinedAttributes) {
        for (Iterator iter = elDefinitions.element("tags").elements().iterator(); iter.hasNext();) {
            Element elTag = (Element) iter.next();
            Tag tag = parseTag(elTag, predefinedTags, predefinedAttributes);
            predefinedTags.put(tag.getId(), tag);
        }
    }

    /**
     * Parse list of /config/rules elements and store them in definitions map.
     * @param elements elements
     * @param predefinedTags map from which to load referenced Tags
     * @param predefinedAttributes map from which to load referenced Attributes
     * @param definitions map to store found Definitions
     */
    private void parseRules(List elements, Map<String, Tag> predefinedTags, Map<String, Attribute> predefinedAttributes, EnumMap<Rules, Definition> definitions) {
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            Element elRules = (Element) iter.next();
            String id = elRules.attributeValue("id");
            Rules rules = Rules.getEnum(id);
            Definition definition = new Definition(rules);
            List<Tag> tags = new ArrayList<Tag>();

            String parentRulesId = elRules.attributeValue("extends");
            if (parentRulesId != null) {
                Definition parentDefinition = definitions.get(Rules.getEnum(parentRulesId));
                if (parentDefinition == null)
                    throw new ConfigurationException("Nenalezena odkazovaná pravidla s id " + parentRulesId);
                tags.addAll(parentDefinition.getTags().values());
            }

            String checkerClass = elRules.attributeValue("checker");
            if (checkerClass != null) {
                Checker checker;
                try {
                    Class aClass = Class.forName(checkerClass);
                    checker = (Checker) aClass.newInstance();
                } catch (Exception e) {
                    throw new ConfigurationException("Selhalo vytvoření instance třídy " + checkerClass + " pro pravidla " + id, e);
                }
                checker.configure(elRules);
                definition.setChecker((DefinitionChecker) checker);
            }

            Element elRemove = elRules.element("remove");
            if (elRemove != null) {
                for (Iterator iterIn = elRemove.elements("tag").iterator(); iterIn.hasNext();) {
                    Element elTag = (Element) iterIn.next();
                    Tag tag = parseTag(elTag, predefinedTags, predefinedAttributes);
                    tags.remove(tag);
                }
            }

            for (Iterator iterIn = elRules.elements("tag").iterator(); iterIn.hasNext();) {
                Element elTag = (Element) iterIn.next();
                Tag tag = parseTag(elTag, predefinedTags, predefinedAttributes);
                tags.add(tag);
            }

            definition.setTags(tags);
            definitions.put(rules, definition);
        }
    }

    private Tag parseTag(Element element, Map<String, Tag> predefinedTags, Map<String, Attribute> predefinedAttributes) {
        Tag tag;
        String rel = element.attributeValue("rel");
        if (rel != null) {
            rel = rel.toUpperCase();
            tag = predefinedTags.get(rel);
            if (tag == null)
                throw new ConfigurationException("Nenalezena odkazovaná značka s id " + rel);
            return tag;
        }

        String id = element.attributeValue("id").toUpperCase();
        boolean mustBeClosed = "true".equals(element.attributeValue("mustBeClosed"));
        tag = new Tag(id, mustBeClosed);

        List<Attribute> attributes = new ArrayList<Attribute>();
        for (Iterator iterIn = element.elements("attribute").iterator(); iterIn.hasNext();) {
            Element elAttribute = (Element) iterIn.next();
            Attribute attribute = parseAttribute(elAttribute, predefinedAttributes);
            attributes.add(attribute);
        }
        tag.setAttributes(attributes);

        return tag;
    }

    private Attribute parseAttribute(Element element, Map<String, Attribute> predefinedAttributes) {
        Attribute attribute;
        String rel = element.attributeValue("rel");
        if (rel != null) {
            rel = rel.toUpperCase();
            attribute = predefinedAttributes.get(rel);
            if (attribute == null)
                throw new ConfigurationException("Nenalezen odkazovaný atribut s id " + rel);
            return attribute;
        }

        String id = element.attributeValue("id").toUpperCase();
        attribute = new Attribute(id);

        String checkerClass = element.attributeValue("checker");
        if (checkerClass != null) {
            Checker checker;
            try {
                Class aClass = Class.forName(checkerClass);
                checker = (Checker) aClass.newInstance();
            } catch (Exception e) {
                throw new ConfigurationException("Selhalo vytvoření instance třídy " + checkerClass + " pro atribut " + id, e);
            }
            checker.configure(element);
            attribute.setChecker((AttributeChecker) checker);
        }

        Element elValues = element.element("values");
        if (elValues != null) {
            List<String> values = new ArrayList<String>();
            for (Iterator iterIn = elValues.elements().iterator(); iterIn.hasNext();) {
                Element elValue = (Element) iterIn.next();
                values.add(elValue.getTextTrim());
            }
            attribute.setValues(values);
        }
        return attribute;
    }

    public static void main(String[] args) throws Exception {
        HtmlChecker checker = new HtmlChecker();
        checker.setConfigFileName("conf/tag_checker.xml");
        ConfigurationManager.getConfigurator().configureAndRememberMe(checker);
        HtmlChecker.check(Rules.DEFAULT, "<p>");
    }
}
