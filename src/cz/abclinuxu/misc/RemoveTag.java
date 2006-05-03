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
package cz.abclinuxu.misc;

import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;
import org.dom4j.Document;
import org.dom4j.Node;

import java.io.File;
import java.io.FileWriter;

/**
 * Processes all files in current directory (must be XML) and
 * removes xpath specified tag.
 * @author literakl
 * @since 3.5.2006
 */
public class RemoveTag {
    public static void main(String[] args) throws Exception {
        String path = args[0];
        String xpath = args[1];

        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("ISO-8859-2");
        format.setSuppressDeclaration(false);

        File dir = new File(path);
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            Document document = new SAXReader().read(file);
            Node node = document.selectSingleNode(xpath);
            if (node != null) {
                node.detach();
                XMLWriter writer = new XMLWriter(new FileWriter(file), format);
                writer.write(document);
                writer.close();
            }
        }
    }
}
