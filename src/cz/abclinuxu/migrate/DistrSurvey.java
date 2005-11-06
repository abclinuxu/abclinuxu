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
package cz.abclinuxu.migrate;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.*;

/**
 * Unifies distributon survey.
 */
public class DistrSurvey {
    static SAXReader saxReader = new SAXReader();
    static OutputFormat format = null;

    static {
        format = OutputFormat.createPrettyPrint();
        format.setEncoding("ISO-8859-2");
        format.setSuppressDeclaration(true);
    }

    static void fixFile(File file) throws IOException, DocumentException {
        Document document = saxReader.read(file);
        Element top = (Element) document.selectSingleNode("//screen");
        replace_jina(top, "jina1", file.getName());
        replace_jina(top, "jina2", file.getName());
        replace_jina(top, "jina3", file.getName());
        print("fixed/"+file.getName(), document);
    }

    static void replace_jina(Element top, String tagName, String fileName) {
        Element tag = top.element(tagName);
        if (tag==null)
            return;
        String name = tag.getTextTrim();
        if (name==null || name.length()==0)
            return;
        name = normalize(name, fileName);
        tag.setName("distribuce");
        tag.setText(name);

        tag = top.element(tagName+"_pocet");
        if ( tag!=null )
            tag.setName(name+"_pocet");
    }

    static String normalize(String name, String fileName) {
        StringBuffer sb = new StringBuffer(name);
        for ( int i = 0; i<sb.length(); i++ ) {
            char c = sb.charAt(i);
            if (c==' ') {
                sb.deleteCharAt(i);
                i--;
                continue;
            }
            if (c=='-') c = '_';
            c = Character.toLowerCase(c);
            if (c<'a'||c>'z')
                System.out.println(fileName+" : "+name);
            sb.setCharAt(i, c);
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        File dir = new File(".");
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });
        for ( int i = 0; i<files.length; i++ ) {
            File file = files[i];
            fixFile(file);
        }
    }

    static void print(String file, Document document) throws IOException {
        FileOutputStream os = new FileOutputStream(file);
        XMLWriter writer = new XMLWriter(os, format);
        writer.write(document);
        os.close();
    }
}
