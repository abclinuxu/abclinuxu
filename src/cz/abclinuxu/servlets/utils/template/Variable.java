/*
 * User: Leos Literak
 * Date: Jan 19, 2003
 * Time: 7:52:02 PM
 */
package cz.abclinuxu.servlets.utils.template;

/**
 * This class holds one variable, which shall be passed to template engine.
 */
class Variable {
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
