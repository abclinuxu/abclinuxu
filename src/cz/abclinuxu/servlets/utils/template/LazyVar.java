/*
 * User: Leos Literak
 * Date: Jan 19, 2003
 * Time: 7:52:25 PM
 */
package cz.abclinuxu.servlets.utils.template;

/**
 * This class holds value, which shall be evaluated
 * at runtime.
 */
class LazyVar {
    String value;

    public LazyVar(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
