/*
 * User: Leos Literak
 * Date: Jan 19, 2003
 * Time: 7:53:41 PM
 */
package cz.abclinuxu.servlets.utils.template;

import java.util.List;

/**
 * This class holds attribute of one mapping. List variables holds
 * instances of Variable.
 */
class Mapping {
    String content;
    List variables;

    public Mapping(String content) {
        this.content = content;
    }

    public void setVariables(List variables) {
        this.variables = variables;
    }

    public String getContent() {
        return content;
    }

    public List getVariables() {
        return variables;
    }
}
