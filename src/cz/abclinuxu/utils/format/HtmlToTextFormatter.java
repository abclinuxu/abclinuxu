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
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.AbcException;

import org.htmlparser.Node;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.Text;
import org.gjt.jedit.TextUtilities;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.prefs.Preferences;

/**
 * This class is able to format HTML code into text by skipping tags
 * and extracting links to the end.
 */
public class HtmlToTextFormatter implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HtmlToTextFormatter.class);
    static Pattern pattern = Pattern.compile("[\r\n]{2,}");

    private static final String PREF_EMAIL_LINE_LENGTH = "line.length";

    private static int lineLength = 80;

    static {
        ConfigurationManager.getConfigurator().configureMe(new HtmlToTextFormatter());
    }

    /**
     * Formats text from HTML to txt representation, limiting its line width
     * and putting URLs after the text.
     * @param input input text
     * @return formatted text
     * @throws AbcException some error
     */
    public static String format(String input) throws AbcException {
        StringBuilder sb = new StringBuilder();
        List<String> links = new ArrayList<String>();
        Node node;

        try {
            Lexer lexer = new Lexer(input);
            links.clear();
            while ((node = lexer.nextNode()) != null) {
                if (node instanceof TagNode) {
                    processTag((TagNode) node, sb, links);
                } else if (node instanceof Text) {
                    sb.append(((Text) node).getText());
                }
            }

            String formatted = TextUtilities.format(sb.toString(), lineLength, 4);
            Matcher matcher = pattern.matcher(formatted);
            formatted = matcher.replaceAll("\n\n");
            if (links.isEmpty())
                return formatted;

            sb.setLength(0);
            sb.append(formatted);
            sb.append("\n\n");

            int i = 1;
            for (String url : links) {
                sb.append(i++).append(". ").append(url).append('\n');
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Failed to format following text: \n'"+input+"'\n", e);
            return input;
        }
    }

    private static void processTag(TagNode tag, StringBuilder sb, List<String> links) {
        if ("A".equals(tag.getTagName()) && tag.isEndTag()) {
            String url = tag.getAttribute("href");
            if (url.indexOf("://") == -1)
                url = AbcConfig.getAbsoluteUrl() + url;
            links.add(url);
            sb.append(" [" + links.size() + "] ");
        } else if ("BR".equals(tag.getTagName())) {
            sb.append('\n');
        } else if (tag.breaksFlow()) {
            sb.append('\n').append('\n');
        }
    }

    public static void setLineLength(int length) {
        lineLength = length;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        String pref = prefs.get(PREF_EMAIL_LINE_LENGTH, "60");
        lineLength = Integer.parseInt(pref);
    }
}
