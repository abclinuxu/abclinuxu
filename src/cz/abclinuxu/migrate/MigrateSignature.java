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
package cz.abclinuxu.migrate;

import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.data.User;
import cz.abclinuxu.utils.freemarker.Tools;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import org.dom4j.Element;
import org.htmlparser.util.ParserException;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.Node;
import org.htmlparser.nodes.TagNode;

/**
 * Fixes illegal HTML tags in user's signature.
 * @author literakl
 * @since 18.2.2008
 */
public class MigrateSignature {

    public static void main(String[] args) throws Exception {
        Set allowedTags = new HashSet();
        allowedTags.add("A");
        allowedTags.add("ABBR");
        allowedTags.add("ACRONYM");
        allowedTags.add("CITE");
        allowedTags.add("CODE");

        Persistence persistence = PersistenceFactory.getPersistence();
        SQLTool sqlTool = SQLTool.getInstance();
        System.out.println("Starting to search for users ..");
        int max = sqlTool.getMaximumUserId();
        for (int i = 0; i < max; i += 50) {
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_ID, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, 50)};
            List<Integer> userIds = sqlTool.findUsers(qualifiers);
            List<User> users = new ArrayList<User>(50);
            for (Integer id : userIds) {
                users.add(new User(id));
            }
            Tools.syncList(users);

            for (User user : users) {
                Element element = (Element) user.getData().selectSingleNode("/data/personal/signature");
                if (element == null)
                    continue;

                String before = element.getText();
                String after = removeNotAllowedTags(before, allowedTags);
                if (before.equals(after))
                    continue;

                element.setText(after);
                persistence.update(user);
                System.out.print("#");
                if (i % 50 == 0)
                    System.out.println();
            }
        }
        System.out.println();
        System.out.println("done");
    }

    static String removeNotAllowedTags(String s, Set allowedTags) throws ParserException {
        Lexer lexer = new Lexer(s);
        Node node = null;
        TagNode tag = null;
        String currentTagName = null;
        StringBuffer sb = new StringBuffer();

        while ((node = lexer.nextNode()) != null) {
            if (!(node instanceof TagNode)) {
                sb.append(node.getText());
                continue;
            }

            tag = (TagNode) node;
            currentTagName = tag.getTagName().toUpperCase();
            if ( ! allowedTags.contains(currentTagName))
                continue;

            if (currentTagName.equals("A") && ! tag.isEndTag())
                tag.setAttribute("rel", "\"nofollow\"");
            sb.append('<').append(node.getText()).append('>');
        }
        return sb.toString();
    }
}
