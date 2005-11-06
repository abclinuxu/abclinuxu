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
package cz.abclinuxu.servlets.utils.template;

import java.util.List;

/**
 * Holder of Servlet and Action pair.
 */
public class ServletAction {
    String template;
    String content;
    String name;
    List variables;

    public ServletAction(String name) {
        this.name = name;
    }

    /**
     * Each action may have assigned special template, that
     * overrides default template selection mechanism of TemplateSelector.
     * @return forced template or null, if not set
     */
    public String getForcedTemplate() {
        return template;
    }

    /**
     * Set template, that overrides default TemplateSelector's choice.
     */
    public void setForcedTemplate(String template) {
        this.template = template;
    }

    public void setVariables(List variables) {
        this.variables = variables;
    }

    public List getVariables() {
        return variables;
    }

    /**
     * Gets content. Content is page to be rendered.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets content. Content is page to be rendered.
     */
    public void setContent(String content) {
        this.content = content;
    }

    public String toString() {
        return name + " -> " + content;
    }
}
