/*
 *  Copyright (C) 2006 Leos Literak
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
package cz.abclinuxu.utils;

import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Tag;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.utils.freemarker.Tools;
import java.util.ArrayList;
import java.util.HashMap;
import org.dom4j.Element;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Class that can select advertisement based on predefined criteria.
 * @author literakl
 * @since 26.12.2006
 */
public class Advertisement implements Configurable {
    static Logger log = Logger.getLogger(Advertisement.class);

    public static final String PREF_STANDARD_REGEXPS = "standard.regexps";
    static Map<String, Pattern> regexps;
    static Map<String, Integer> positions;
    public static Map<String, String> standardRegexps;
    static {
        regexps = new ConcurrentHashMap<String, Pattern>(50);
        positions = new ConcurrentHashMap<String, Integer>(50);

        Advertisement ad = new Advertisement();
        ConfigurationManager.getConfigurator().configureAndRememberMe(ad);
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        try {
            Preferences subprefs = prefs.node(PREF_STANDARD_REGEXPS);
            String[] keys = subprefs.keys();
            standardRegexps = new HashMap<String, String>(keys.length);

            for (int i = 0; i < keys.length; i++)
                standardRegexps.put(keys[i], subprefs.get(keys[i], null));
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Finds advertisement for given position on specified uri. The user may eventually
     * have paid not to see any advertisements. If the position is not defined, HTML comment
     * with info is returned.
     * @param id identifier of the position
     * @param env map with all request data model
     * @return advertisement or empty string
     */
    public static String getAdvertisement(String id, Map env) {
        String uri = (String) env.get(Constants.VAR_REQUEST_URI);
        User user = (User) env.get(Constants.VAR_USER);
        if (user != null && user.getId() == -1) // not implemented now
            return "";

        Item position = getPosition(id);

        if (position == null)
            return "<!-- the advertisement position '" + id + "' is not defined! -->";

        Element root = position.getData().getRootElement();
        if ("no".equals(Tools.xpath(position, "/data/active")))
            return "<!-- the advertisement position " + id + " is not active -->";

        Element selected = null;
        int selLen = -1;
        List<Element> codes = root.selectNodes("//code");

        for (Element code : codes) {
            String regexp = code.attributeValue("regexp");

            if (regexp == null)
                regexp = "";

            Matcher matcher = getPattern(regexp).matcher(uri);
            if (matcher.find() && regexp.length() > selLen) {
                selLen = regexp.length();
                selected = code;
            }
        }

        if (selected == null)
            return "<!-- error: no code available for position '" + id + "'! -->";

        List<Element> variants = selected.selectNodes("variants/variant");
        List<Element> availableVariants = new ArrayList<Element>(2);
        Element defaultVariant = null, selectedVariant;

        List<Tag> assignedTags = (List<Tag>) env.get("ASSIGNED_TAGS");

        for (Element variant : variants) {
            if ("no".equals(variant.attributeValue("active")))
                continue;

            String tags = variant.attributeValue("tags");
            if (tags != null && tags.trim().length() == 0)
                tags = null;

            if (tags == null) {
                defaultVariant = variant;
                continue;
            }

            if (assignedTags == null || assignedTags.size() == 0)
                continue;

            String[] tagList = tags.split(" ");

mainCycle:
            for (Tag tag : assignedTags) {
                for (int i = 0; i < tagList.length; i++) {
                    if (tagList[i].equalsIgnoreCase(tag.getId())) {
                        availableVariants.add(variant);
                        break mainCycle;
                    }
                }
            }

        }

        if (availableVariants.size() > 1) {
            int index = new Random().nextInt(availableVariants.size());
            selectedVariant = availableVariants.get(index);
        } else if (availableVariants.size() == 1)
            selectedVariant = availableVariants.get(0);
        else
            selectedVariant = defaultVariant;

        if (selectedVariant == null)
            return "<!-- error: no variant available for position '" + id + "', code '" + selected.attributeValue("regexp") + "' -->";

        String content = selectedVariant.getText();
        StringBuilder sb = new StringBuilder();
        sb.append("<!-- ad position '").append(id).append("' -->\n");
        if ("yes".equals(selectedVariant.attributeValue("dynamic")))
            try {
                sb.append(FMUtils.executeCode(content, env));
            } catch (Exception e) {
                log.warn("Position " + id + " threw an exception. Content: \n" + content + "\n", e);
                return "<!-- error: the code defined for position '" + id + "' failed! -->";
            }
        else
            sb.append(content);
        return sb.toString();
    }

    /**
     * @return Pattern for given regular expression, it uses a cache.
     */
    private static Pattern getPattern(String regexp) {
        Pattern pattern = regexps.get(regexp);
        if (pattern == null) {
            pattern = Pattern.compile(regexp);
            regexps.put(regexp, pattern);
        }
        return pattern;
    }

    /**
     * Returns the ad item with the specified identificator.
     * The function caches the data, so that a search by string id doesn't always
     * have to be performed.
     * @param id Position's identificator
     * @return A corresponding item, if any
     */
    private static Item getPosition(String id) {
        Integer objid = positions.get(id);

        if (objid != null) {
            try {
                Persistence persistence = PersistenceFactory.getPersistence();
                Item item = (Item) persistence.findById(new Item(objid));

                if (id.equals(item.getString1()))
                    return item;
            } catch (Exception e) {
                // The position has probably been erased
            }

            positions.remove(id);
        }

        Item item = SQLTool.getInstance().findAdvertisementByString(id);

        if (item != null)
            positions.put(id, item.getId());

        return item;
    }

    public static void clearCache() {
        positions.clear();
    }
}
