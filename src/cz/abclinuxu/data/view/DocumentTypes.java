/*
 *  Copyright (C) 2008 Karel Piwko
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
package cz.abclinuxu.data.view;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.prefs.Preferences;

import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Map of available document types
 * @author kapy
 */
public class DocumentTypes implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NewsCategories.class);

    public enum Types { TAGS, SEARCH };

    public static final String PREF_FILE_TAGS = "types_tags.file";
    public static final String PREF_FILE_SEARCH = "types_search.file";

    static DocumentTypes tagsTypes, searchTypes;
    static {
        tagsTypes = new DocumentTypes();
        searchTypes = new DocumentTypes();
        ConfigurationManager.getConfigurator().configureAndRememberMe(tagsTypes);
    }

    private Map<String, DocumentType> map;

    /**
     * private constructor, inaccessible outside of this class.
     */
    private DocumentTypes() {
    }

    /**
     * @return singleton object of this class.
     */
    public static DocumentTypes getInstance(Types type) {
        if (type == Types.TAGS)
            return tagsTypes;
        else
            return searchTypes;
    }

    /**
     * Finds NewsCategory for given key. Case-insensitive match is performed.
     * @param key
     * @return found DocumentType or null
     */
    public DocumentType get(String key) {
        return map.get(key.toUpperCase());
    }

    /**
     * Gets map of all existing types. The order is preserved.
     * @return existing types
     */
    public Map<String, DocumentType> get() {
        return new LinkedHashMap<String, DocumentType>(map);
    }

    /**
     * Configures this instance.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        String filename = prefs.get(PREF_FILE_TAGS, null);
        loadDocumentTypes(tagsTypes, filename);
        filename = prefs.get(PREF_FILE_SEARCH, null);
        loadDocumentTypes(searchTypes, filename);
    }

    /**
     * Loads all document types from external file.
     */
    private void loadDocumentTypes(DocumentTypes types, String filename) {
        log.info("Loading list of document types from file '" + filename + "'");
        try {
            Document document = new SAXReader().read(filename);
            String key, label, typeString, subType;
            int type;
            Map<String, DocumentType> aMap = new LinkedHashMap<String, DocumentType>(15, 1.0f);
            List documentTypes = document.getRootElement().elements("document-type");
            for (Iterator iter = documentTypes.iterator(); iter.hasNext();) {
                Element element = (Element) iter.next();
                key = element.elementTextTrim("key");
                label = element.elementTextTrim("label");
                subType = element.elementTextTrim("subtype");

                typeString = element.elementTextTrim("type-id");
                if (typeString != null && !"".equals(typeString))
                    type = Integer.parseInt(typeString);
                else
                    type = DocumentType.TYPE_UNKNOWN;
                DocumentType dt = new DocumentType(key, label, type, subType);
                aMap.put(key, dt);
            }
            types.map = aMap;
            log.info("Loaded " + map.size() + " document types.");
        } catch (Exception e) {
            log.error("Cannot load list of document types!", e);
        }
    }
}
