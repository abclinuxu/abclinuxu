/*
 *  Copyright (C) 2008 Leos Literak
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
package cz.abclinuxu.utils.parser.clean;

import org.htmlparser.lexer.Lexer;
import org.htmlparser.Node;
import org.htmlparser.Remark;
import org.htmlparser.Text;
import org.htmlparser.util.ParserException;
import org.htmlparser.nodes.TagNode;

import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * This class is responsible for rule transformations (empty line separates paragraph)
 * and html cleaning. XSS and forbidden tags / elements must be checked separatelly
 * with SafeHtmlGuard implementations.
 * @author literakl
 * @since 23.9.2008
 */
public class HtmlPurifier {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HtmlPurifier.class);

    static Pattern reWinLines, reEmptyLine;
    static {
        reWinLines = Pattern.compile("\r\n", Pattern.MULTILINE);
        reEmptyLine = Pattern.compile("\n{2,}", Pattern.MULTILINE);
    }

    /**
     * Performs some transformations and cleaning of input text. Currently it transforms
     * simple text to HTML format (adds line breaks instead of empty lines to separate
     * paragraphs). It removes windows new line characters too.
     * This class will be merged with TagValidator in future. It could also automatically
     * fix ampersands in URLs, translate some HTML entities to UTF characters or escape
     * &gt; and &lt;.
     * @param input string containing HTML snippet or text to be transformed to valid HTML
     * @return hopefully valid HTML
     */
    public static String clean(String input/*, String htmlGuard*/) {
        Matcher matcher = reWinLines.matcher(input);
        input = matcher.replaceAll("\n");

        boolean inPre = false, paragraphDetected = false;
        List<Integer> emptyLines = new ArrayList<Integer>();
        StringBuilder sb = new StringBuilder();
        try {
            Lexer lexer = new Lexer(input);
            Node node;
            TagNode tag;
            while ((node = lexer.nextNode()) != null) {
                if ((node instanceof Remark)) {
                    sb.append(node.toHtml());
                    continue;
                }
                if ((node instanceof Text)) {
                    String text = node.getText();
                    if (! inPre) {
                        int offset = sb.length();
                        matcher = reEmptyLine.matcher(text);
                        while (matcher.find()) {
                            int start = matcher.start();
                            emptyLines.add(offset + start);
                        }
                    }
                    sb.append(text);
                    continue;
                }

                tag = (TagNode) node;
                String tagName = tag.getTagName();
                if (! paragraphDetected && ("P".equals(tagName) || "DIV".equals(tagName) ||"BR".equals(tagName)))
                    paragraphDetected = true;
                if ("PRE".equals(tagName))
                    inPre = ! tag.isEndTag();

                sb.append(node.toHtml());
            }

            if (! paragraphDetected && ! emptyLines.isEmpty()) {
                for (ListIterator<Integer> iter = emptyLines.listIterator(emptyLines.size()); iter.hasPrevious();) {
                    Integer position = iter.previous();
                    sb.insert(position + 1, "<br><br>");
                }
            }
        } catch (ParserException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
