/*
 * User: literakl
 * Date: 28.9.2003
 * Time: 19:01:54
 */
package cz.abclinuxu.utils.news;

/**
 * Holder of category for news.
 */
public final class NewsCategory {
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
