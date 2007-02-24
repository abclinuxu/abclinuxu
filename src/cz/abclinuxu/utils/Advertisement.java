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
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.utils.freemarker.FMUtils;
import org.dom4j.Element;

import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Class that can select advertisement based on predefined criteria.
 * @author literakl
 * @since 26.12.2006
 */
public class Advertisement {
    static Map<String, Pattern> regexps;
    static {
        regexps = Collections.synchronizedMap(new HashMap<String, Pattern>());
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

        Persistence persistence = PersistenceFactory.getPersistance();
        Item item = (Item) persistence.findById(new Item(Constants.ITEM_DYNAMIC_CONFIGURATION));
        Element position = (Element) item.getData().selectSingleNode("//advertisement/position[@id='" + id + "']");
        if (position == null)
            return "<!-- advertisement position '" + id + "' is not defined! -->";
        if (position.attributeValue("active").equals("no"))
            return "<!-- advertisement position " + id + " is not active -->";

        Element selected = null, defaultCode = null, code;
        List codes = position.elements("code");
        for (Iterator iter = codes.iterator(); iter.hasNext();) {
            code = (Element) iter.next();
            String regexp = code.attributeValue("regexp");
            if (regexp == null) {
                defaultCode = code;
                continue;
            }

            Matcher matcher = getPattern(regexp).matcher(uri);
            if (matcher.find()) {
                selected = code;
                break;
            }
        }
        if (selected == null)
            selected = defaultCode;
        if (selected == null)
            return "<!-- error: no default code defined for position '" + id + "'! -->";

        String content = selected.getText();
        if ("yes".equals(selected.attributeValue("dynamic")))
            try {
                return FMUtils.executeCode(content, env);
            } catch (Exception e) {
                return "<!-- error: code defined for position '" + id + "' threw an error: " + e.getMessage() + "! -->";
            }
        else
            return content;
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
}