/*
 *  Copyright (C) 2005 Leos Literak
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
package cz.abclinuxu.utils.news;

/**
 * Holder of category for news.
 */
public class NewsCategory {
    String key, name, desc;

    /**
     * Constructs new NewsCategory.
     * @param key identifier of NewsCategory. This value shall be not changed!
     * @param name locale specific short name.
     * @param desc locale specific description.
     */
    public NewsCategory(String key, String name, String desc) {
        this.key = key;
        this.name = name;
        this.desc = desc;
    }

    /**
     * @return identifier of NewsCategory
     */
    public String getKey() {
        return key;
    }

    /**
     * @return locale specific short name
     */
    public String getName() {
        return name;
    }

    /**
     * @return locale specific description
     */
    public String getDesc() {
        return desc;
    }
}
