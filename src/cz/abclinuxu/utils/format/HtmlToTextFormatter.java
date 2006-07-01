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
package cz.abclinuxu.utils.format;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.AbcException;

import org.htmlparser.Node;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.lexer.nodes.TagNode;
import org.htmlparser.lexer.nodes.StringNode;
import org.gjt.jedit.TextUtilities;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.prefs.Preferences;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

/**
 * This class is able to format HTML code into text by skipping tags
 * and extracting links to the end.
 */
public class HtmlToTextFormatter implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HtmlToTextFormatter.class);

    private static final String PREF_EMAIL_LINE_LENGTH = "line.length";

    private List links = new ArrayList();
    private int lineLength = 80;

    public HtmlToTextFormatter() {
        ConfigurationManager.getConfigurator().configureMe(this);
    }

    public String format(String input) throws AbcException {
        StringBuffer sb = new StringBuffer();
        ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
        Lexer lexer = null;
        try {
            lexer = new Lexer(new Page(bais, "ISO-8859-2"));
        } catch (UnsupportedEncodingException e) {
            throw new AbcException(e.getMessage(), e);
        }
        Node node;

        try {
            links.clear();
            while ((node = lexer.nextNode()) != null) {
                if (node instanceof TagNode) {
                    processTag((TagNode) node, sb);
                } else if (node instanceof StringNode) {
                    sb.append(((StringNode) node).getText());
                }
            }

            String formatted = TextUtilities.format(sb.toString(), lineLength, 4);
            if (links.size() > 0) {
                sb.setLength(0);
                sb.append(formatted);
                sb.append("\n\n");

                int  i = 0;
                for (Iterator iter = links.iterator(); iter.hasNext(); i++) {
                    sb.append(i).append(". ").append((String) iter.next()).append('\n');
                }
                formatted = sb.toString();
            }

            return formatted;
        } catch (Exception e) {
            log.error("Failed to format following text: \n'"+input+"'\n", e);
            return input;
        }
    }

    private void processTag(TagNode tag, StringBuffer sb) {
        if ("A".equals(tag.getTagName()) && !tag.isEndTag()) {
            links.add(tag.getAttribute("href"));
            sb.append("[" + links.size() + "] ");
        } else if ("BR".equals(tag.getTagName())) {
            sb.append('\n').append('\n');
        } else if ("P".equals(tag.getTagName())) {
            sb.append('\n').append('\n');
        }
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        String pref = prefs.get(PREF_EMAIL_LINE_LENGTH, "60");
        lineLength = Integer.parseInt(pref);
    }

    public static void main(String[] args) {
        HtmlToTextFormatter f = new HtmlToTextFormatter();
        String text1 = "<p>Server <a href=\"http://none.net\">None</a> nic neohlasil...<br/>"
                        + "Tym    padom pokracuje vo svojej <i>strategii</i> <a href=\"http://none.net/strategy\">prilakat uzivatelov ich netrpezlivostou</a></p>";
        String text2 = "<i>Støíbrná</i>\n koèka\n <b>skákala</b>\n pøes rù¾ového konì <u>u ¹i¹atého</u> melounu.";
        System.out.println(f.format(text1));
        System.out.println(f.format(text2));
    }
}
