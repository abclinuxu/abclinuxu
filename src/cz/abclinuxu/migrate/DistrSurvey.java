/*
 * User: literakl
 * Date: 21.3.2004
 * Time: 20:52:22
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
        replace_jina(top, "jina1");
        replace_jina(top, "jina2");
        replace_jina(top, "jina3");
        print("fixed/"+file.getName(), document);
    }

    static void replace_jina(Element top, String tagName) {
        Element tag = top.element(tagName);
        if (tag==null)
            return;
        String name = tag.getTextTrim();
        if (name==null || name.length()==0)
            return;
        name = name.toLowerCase();
        tag.setName("distribuce");
        tag.setText(name);

        tag = top.element(tagName+"_pocet");
        if ( tag!=null )
            tag.setName(name+"_pocet");
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
