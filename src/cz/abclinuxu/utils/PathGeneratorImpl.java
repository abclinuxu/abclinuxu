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

import java.io.File;
import java.io.IOException;

/**
 * Default implementation
 */
public class PathGeneratorImpl implements PathGenerator {
    /**
     * Creates complete path for specified attachment of given object.
     * @param obj object which will be associated with the file
     * @param usage usage of the file
     * @param filename name of uploaded file
     * @return path for the file
     * @throws java.io.IOException if file cannot be saved for any reason
     * (typically incorrect filesystem permissions)
     */
     public String getPath(GenericObject obj, Type usage, String filename) throws IOException {
        if (! Type.SCREENSHOT.equals(usage))
            throw new InternalException("Not implemented");

        StringBuffer sb = new StringBuffer("images/screenshots/");
        String id = String.valueOf(obj.getId());
	    sb.append(id.charAt(0)).append('/').append(id.charAt(1)).append('/');

        File dir = new File(AbcConfig.calculateDeployedPath(sb.toString()));
        if (!dir.exists())
            if (!dir.mkdirs())
                throw new IOException("Cannot create directory "+dir.getAbsolutePath()+"!");
        if (!dir.isDirectory())
            throw new IOException("Supposed path " + dir.getAbsolutePath() + " is not directory!");

        return sb.append(id+"_"+filename).toString();
     }
}
