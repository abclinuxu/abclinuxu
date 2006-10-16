/*
 *  Copyright (C) 2006 Yin, Leos Literak
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

import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.exceptions.InternalException;
import cz.finesoft.socd.analyzer.DiacriticRemover;

import java.io.File;
import java.io.IOException;

/**
 * Default implementation
 */
public class PathGeneratorImpl implements PathGenerator {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PathGeneratorImpl.class);

    /**
     * Creates complete path for specified attachment of given object.
     * @param obj object which will be associated with the file
     * @param usage usage of the file
     * @param prefix string to be added to filename, it can be empty
     * @param suffix extension of the file
     * @return path for the file
     * @throws java.io.IOException if file cannot be saved for any reason
     * (typically incorrect filesystem permissions)
     */
     public File getPath(GenericObject obj, Type usage, String prefix, String suffix) throws IOException {
        if (! Type.SCREENSHOT.equals(usage))
            throw new InternalException("Not implemented");
        if (prefix == null)
            prefix = "";
        if (suffix == null || suffix.length() == 0)
            throw new IllegalArgumentException("File suffix must be specified!");

        StringBuffer sb = new StringBuffer("images/screenshots/");
        String id = String.valueOf(obj.getId());
	    sb.append(id.charAt(id.length()-1)).append('/').append(id.charAt(id.length()-2)).append('/');

        File dir = new File(AbcConfig.calculateDeployedPath(sb.toString()));
        if (!dir.exists())
            if (!dir.mkdirs())
                throw new IOException("Cannot create directory "+dir.getAbsolutePath()+"!");
        if (!dir.isDirectory())
            throw new IOException("Supposed path " + dir.getAbsolutePath() + " is not directory!");

        prefix = DiacriticRemover.getInstance().removeDiacritics(prefix);
        String filePrefix = id + "-" + prefix + "-";
        File file = null;
        try {
            file = File.createTempFile(filePrefix, suffix, dir);
        } catch (IOException e) {
            log.error("Failed to create unique file! Prefix: "+filePrefix+", suffix: "+suffix);
            throw e;
        }
        return file;
     }
}
