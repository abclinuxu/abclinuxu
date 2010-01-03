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
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.exceptions.InternalException;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.servlets.html.edit.EditArticle;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Matcher;
import org.dom4j.Element;

/**
 * Default implementation
 */
public class PathGeneratorImpl implements PathGenerator {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PathGeneratorImpl.class);
    static public Pattern reInvalidChars;
    static {
        try {
            reInvalidChars = Pattern.compile("[^a-z0-9_-]");
        } catch (PatternSyntaxException e) {
            log.error("Regexp cannot be compiled!", e);
        }
    }

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
        if (prefix == null)
            prefix = "";
        else {
            prefix = Misc.removeDiacritics(prefix);
            prefix = prefix.toLowerCase();
            Matcher matcher = reInvalidChars.matcher(prefix);
            prefix = matcher.replaceAll("-");
            prefix = prefix.replaceAll("\\-{2,}", "-"); // eliminate multiple dashes
            // TODO more checks like in URLManager.normalizeCharacters()
        }
        if (suffix == null || suffix.length() == 0)
            throw new IllegalArgumentException("File suffix must be specified!");

		String id = String.valueOf(obj.getId());
        StringBuffer sb = new StringBuffer();
		
        if (Type.SCREENSHOT.equals(usage))
            sb.append("images/screenshots/");
        else if (Type.ATTACHMENT.equals(usage))
            sb.append("data/prilohy/");
		else if (Type.ARTICLE_ATTACHMENT.equals(usage)) {
			sb.append("data");
			
			Relation rel = (Relation) obj;
			String url = rel.getUrl();
			
			if(url == null) {
				Item item = (Item) rel.getChild();
				Element element = (Element) item.getData().selectSingleNode("/data/section_rid");
				if (element == null)
					return null;
				int section_rid = Misc.parseInt(element.getText(), 0);
				if (section_rid == 0)
					return null;
				
				url = EditArticle.getUrl(item, section_rid, PersistenceFactory.getPersistence());
			}
			
			sb.append(url);
		}
        else
            throw new InternalException("Type " + usage + "not implemented!");

		if (!Type.ARTICLE_ATTACHMENT.equals(usage))
			sb.append(id.charAt(id.length()-1)).append('/').append(id.charAt(id.length()-2)).append('/');

        File dir = new File(AbcConfig.calculateDeployedPath(sb.toString()));
        if (!dir.exists())
            if (!dir.mkdirs())
                throw new IOException("Cannot create directory "+dir.getAbsolutePath()+"!");
        if (!dir.isDirectory())
            throw new IOException("Supposed path " + dir.getAbsolutePath() + " is not directory!");

        String filePrefix = prefix;
        File file;
        try {
			if (!Type.ARTICLE_ATTACHMENT.equals(usage)) {
				filePrefix = id + "-" + prefix + "-";
				file = File.createTempFile(filePrefix, suffix, dir);
			} else
				file = new File(dir + "/" + filePrefix + suffix);
        } catch (IOException e) {
            log.error("Failed to create unique file! Prefix: "+filePrefix+", suffix: "+suffix);
            throw e;
        }
        return file;
     }
}
