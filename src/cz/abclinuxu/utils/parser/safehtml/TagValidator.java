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

import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
    static void check(String s, Map allowedTags) throws TagNotAllowedException, TagNotClosedException, AttributeNotAllowedException, ParserException {
        Lexer lexer = new Lexer(s);
        Node node = null;
        TagNode tag = null;
        CheckedTag checkedTag = null;
        Vector attributes = null;
        HashMap openTags = new HashMap();

        while ((node=lexer.nextNode())!=null) {
            if (!(node instanceof TagNode))
                continue;
            tag = (TagNode) node;
            checkedTag = (CheckedTag) allowedTags.get(tag.getTagName());
            if (checkedTag==null)
                throw new TagNotAllowedException("Znaèka "+tag.getTagName()+" není povolena!");

            if (checkedTag.mustBeClosed) {
                if (tag.isEndTag()) {
                    openTags.remove(tag.getTagName());
                } else {
                    if ( openTags.get(tag.getTagName())!=null )
                        throw new TagNotClosedException("Znaèka "+tag.getTagName()+" musí být uzavøena!");
                    openTags.put(tag.getTagName(), Boolean.TRUE);
                }
            }

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
        if (openTags.size()>0)
            throw new TagNotClosedException("Znaèka "+openTags.keySet().iterator().next()+" musí být uzavøena!");
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
