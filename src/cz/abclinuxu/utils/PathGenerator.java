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

import cz.abclinuxu.data.GenericObject;

import java.io.IOException;
import java.io.File;

/**
 * Interface for generating paths to store file for some object.
 */
public interface PathGenerator {

    /**
     * Creates complete path for specified attachment of given object.
     *
     * @param obj object which will be associated with the file
     * @param usage  usage of the file
     * @param prefix string to be added to filename, it can be empty
     * @param suffix extension of the file
     * @return path for the file
     * @throws java.io.IOException if file cannot be saved for any reason
     *                             (typically incorrect filesystem permissions)
     */
    public File getPath(GenericObject obj, Type usage, String prefix, String suffix) throws IOException;

    public class Type {
        private String value;
        public static final Type SCREENSHOT = new Type("screenshot");
        public static final Type ATTACHMENT = new Type("attachment");
		/** for articles, the file will be stored separately */
		public static final Type ARTICLE_ATTACHMENT = new Type("articleAttachment");

        private Type(String value) {
            this.value = value;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Type)) return false;

            final Type type = (Type) o;

            if (!value.equals(type.value)) return false;

            return true;
        }

        public int hashCode() {
            return value.hashCode();
        }
    }
}
