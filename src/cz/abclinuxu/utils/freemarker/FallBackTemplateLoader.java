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
package cz.abclinuxu.utils.freemarker;

import freemarker.cache.TemplateLoader;
import freemarker.cache.FileTemplateLoader;

import java.io.IOException;
import java.io.Reader;

/**
 * TemplateLoader implementation that assumes some hierarchy structure
 * and if the template is not found, it replaces top level directory
 * with default one and it tries to load template from thid default
 * directory. E.g. default directory is 'default' and template name
 * is 'other/template.ftl'. If the file 'other/template.ftl' is not
 * found, then it will try to use 'default/template.ftl' instead.
 * The purpose of this TemplateLoader is in environment where you have
 * multiple variants of same content (a.k.a. skins, or print version).
 * Date: 4.10.2006
 */
public class FallBackTemplateLoader implements TemplateLoader {
    FileTemplateLoader loader;
    String defaultDir;

    /**
     * Creates new TemplateLoader backed up by given FileTemplateLoader
     * that will try to locate unfound templates in specified defaultDir.
     * @param loader initialized FileTemplateLoader
     * @param defaultDir directory where not found templates shall be searched
     */
    public FallBackTemplateLoader(FileTemplateLoader loader, String defaultDir) {
        this.loader = loader;
        if (defaultDir == null)
            throw new IllegalArgumentException("defaultDir must not be equal to null!");
        this.defaultDir = defaultDir;
    }

    public Object findTemplateSource(String name) throws IOException {
        Object templateSource = loader.findTemplateSource(name);
        if (templateSource != null)
            return templateSource;

        if (name.startsWith(defaultDir))
            return null;

        int index = name.indexOf('/');
        if (index == -1)
            return null;

        name = defaultDir + name.substring(index);
        return loader.findTemplateSource(name);
    }

    public long getLastModified(Object templateSource) {
        return loader.getLastModified(templateSource);
    }

    public Reader getReader(Object templateSource, String encoding) throws IOException {
        return loader.getReader(templateSource, encoding);
    }

    public void closeTemplateSource(Object templateSource) throws IOException {
        loader.closeTemplateSource(templateSource);
    }
}
