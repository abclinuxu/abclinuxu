/*
 * User: literakl
 * Date: 5.4.2004
 * Time: 19:48:03
 */
package cz.abclinuxu.utils.parser.safehtml;

import org.htmlparser.util.ParserException;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.nodes.TagNode;
import org.htmlparser.lexer.nodes.Attribute;
import org.htmlparser.lexer.nodes.PageAttribute;
import org.htmlparser.Node;

import java.util.*;

/**
 * Class, that can check validity and conformance to policy of HTML text.
 */
public class TagValidator {

    /**
     * Performs check of html string.
     * @param s html to be checked.
     * @param allowedTags list of allowed tags and their policy.
     * @throws cz.abclinuxu.utils.parser.safehtml.TagNotAllowedException If tag is not allowed or recognized.
     * @throws cz.abclinuxu.utils.parser.safehtml.TagNotClosedException If tag is not closed.
     * @throws cz.abclinuxu.utils.parser.safehtml.AttributeNotAllowedException If attribute is not allowed.
     */
    static void check(String s, Map allowedTags) throws HtmlCheckException, ParserException {
        Lexer lexer = new Lexer(s);
        Node node = null;
        TagNode tag = null;
        CheckedTag checkedTag = null, lastTag = null;
        String currentTagName = null;
        Vector attributes = null;
        List tagStack = new ArrayList();

        while ((node=lexer.nextNode())!=null) {
            if (!(node instanceof TagNode))
                continue;

            tag = (TagNode) node;
            currentTagName = tag.getTagName();
            checkedTag = (CheckedTag) allowedTags.get(currentTagName);
            if (checkedTag==null)
                throw new TagNotAllowedException("Znaèka "+currentTagName+" není povolena!");

            if (tag.isEndTag()) {
                do {
                    if (tagStack.size() == 0)
                        throw new TagNotClosedException("Nenaletena otevírací znaèka " + currentTagName + "! Nejsou znaèky pøekøí¾eny?");
                    lastTag = (CheckedTag) tagStack.remove(tagStack.size() - 1);
                } while(!lastTag.mustBeClosed && !lastTag.name.equals(currentTagName));

                if (checkedTag.mustBeClosed && !lastTag.name.equals(currentTagName))
                    throw new CrossedTagException("Znaèky " + lastTag.name + " a " + currentTagName + " jsou pøekøí¾eny!");
            } else
                tagStack.add(checkedTag);

            attributes = tag.getAttributesEx();
            removeTagAttribute(attributes);

            if ( checkedTag.attributes==null && attributes.size()>0 )
                throw new AttributeNotAllowedException("Znaèka "+checkedTag.name+" nesmí obsahovat ¾ádné atributy!");

            for ( Iterator iter = attributes.iterator(); iter.hasNext(); ) {
                boolean found = false;
                Attribute attribute = (Attribute) iter.next();
                String name = attribute.getName();
                if ( name==null ) continue;
                name = name.toUpperCase();
                for ( int i = 0; i<checkedTag.attributes.length; i++ ) {
                    String allowedAttribute = checkedTag.attributes[i];
                    if ( name.equals(allowedAttribute) ) {
                        found = true;
                        break;
                    }
                }
                if ( !found )
                    throw new AttributeNotAllowedException("Znaèka "+checkedTag.name+" nesmí obsahovat atribut "+name+"!");
            }
        }

        for (Iterator iter = tagStack.iterator(); iter.hasNext();) {
            checkedTag = (CheckedTag) iter.next();
            if (checkedTag.mustBeClosed)
                throw new TagNotClosedException("Znaèka " + checkedTag.name + " musí být uzavøena!");
        }
    }

    /**
     * getAttributesEx() returns name of tag as attribute. Remove it.
     */
    private static void removeTagAttribute(Vector attributes) {
        if (attributes==null)
            return;
        attributes.remove(0);
        for ( Iterator iter = attributes.iterator(); iter.hasNext(); ) {
            PageAttribute attribute = (PageAttribute) iter.next();
            if (attribute.isWhitespace()) {
                iter.remove();
                continue;
            }
            if ("/".equals(attribute.getName())) {
                iter.remove();
                continue;
            }
        }
    }
}
