/*
 * User: literakl
 * Date: 4.4.2004
 * Time: 16:35:01
 */
package cz.abclinuxu.utils.parser.safehtml;

import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.nodes.TagNode;
import org.htmlparser.lexer.nodes.Attribute;
import org.htmlparser.Node;
import org.htmlparser.util.ParserException;

import java.util.*;

/**
 * This class is responsible for keeping HTML content
 * to be safe. E.g. it will blocks malicious (or stupid) user's
 * input, that can harm portal's UI or XSS.
 * <p>
 * These tags are allowed:
 * p, br, li (no attributes)
 * ul, ol, b, i, code, pre, div, h1, h2, h3 (no attributes, must be closed)
 * a (attribute href, must be closed)
 */
public class SafeHTMLGuard {
    static final CheckedTag TAG_B = new CheckedTag("B", true, null);
    static final CheckedTag TAG_I = new CheckedTag("I", true, null);
    static final CheckedTag TAG_P = new CheckedTag("P", false, null);
    static final CheckedTag TAG_BR = new CheckedTag("BR", false, null);
    static final CheckedTag TAG_A = new CheckedTag("A", true, new String[]{"HREF"});
    static final CheckedTag TAG_PRE = new CheckedTag("PRE", true, null);
    static final CheckedTag TAG_LI = new CheckedTag("LI", false, null);
    static final CheckedTag TAG_UL = new CheckedTag("UL", true, null);
    static final CheckedTag TAG_OL = new CheckedTag("OL", true, null);
    static final CheckedTag TAG_CODE = new CheckedTag("CODE", true, null);
    static final CheckedTag TAG_DIV = new CheckedTag("DIV", true, null);
    static final CheckedTag TAG_H1 = new CheckedTag("H1", true, null);
    static final CheckedTag TAG_H2 = new CheckedTag("H2", true, null);
    static final CheckedTag TAG_H3 = new CheckedTag("H3", true, null);

    static final Map TAGS = new HashMap();
    static {
        TAGS.put(TAG_B.name, TAG_B);
        TAGS.put(TAG_I.name, TAG_I);
        TAGS.put(TAG_P.name, TAG_P);
        TAGS.put(TAG_BR.name, TAG_BR);
        TAGS.put(TAG_A.name, TAG_A);
        TAGS.put(TAG_PRE.name, TAG_PRE);
        TAGS.put(TAG_LI.name, TAG_LI);
        TAGS.put(TAG_UL.name, TAG_UL);
        TAGS.put(TAG_OL.name, TAG_OL);
        TAGS.put(TAG_CODE.name, TAG_CODE);
        TAGS.put(TAG_DIV.name, TAG_DIV);
        TAGS.put(TAG_H1.name, TAG_H1);
        TAGS.put(TAG_H2.name, TAG_H2);
        TAGS.put(TAG_H3.name, TAG_H3);
    }

    /**
     * Performs check of html string.
     * @param s html to be checked.
     * @throws TagNotAllowedException If tag is not allowed or recognized.
     * @throws TagNotClosedException If tag is not closed.
     * @throws AttributeNotAllowedException If attribute is not allowed.
     */
    public static void check(String s) throws TagNotAllowedException, TagNotClosedException, AttributeNotAllowedException, ParserException {
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
            checkedTag = (CheckedTag) TAGS.get(tag.getTagName());
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
            removeTagAttribute(attributes, tag.getTagName());
            if (attributes!=null && attributes.size()>0) {
                if ( checkedTag.attributes==null )
                    throw new AttributeNotAllowedException("Znaèka "+tag.getTagName()+" nesmí obsahovat ¾ádné atributy!");
                for ( Iterator iter = attributes.iterator(); iter.hasNext(); ) {
                    boolean found = false;
                    Attribute attribute = (Attribute) iter.next();
                    String name = attribute.getName();
                    if ( name==null ) continue;
                    name = name.toUpperCase();
                    for ( int i = 0; i<checkedTag.attributes.length; i++ ) {
                        String allowedAttribute = checkedTag.attributes[i];
                        if (name.equals(allowedAttribute)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        throw new AttributeNotAllowedException("Znaèka "+tag.getTagName()+" nesmí obsahovat atribut "+name+"!");
                }
            }
        }
        if (openTags.size()>0)
            throw new TagNotClosedException("Znaèka "+openTags.keySet().iterator().next()+" musí být uzavøena!");
    }

    /**
     * getAttributesEx() returns name of tag as attribute. Remove it.
     */
    private static void removeTagAttribute(Vector attributes, String tag) {
        if (attributes==null)
            return;
        attributes.remove(0); //seems, that first attribute is always tag name
//        for ( Iterator iter = attributes.iterator(); iter.hasNext(); ) {
//            Attribute attribute = (Attribute) iter.next();
//            if (attribute.isStandAlone() && attribute.getName().toUpperCase().equals(tag)) {
//                iter.remove();
//                return;
//            }
//        }
    }

    private static class CheckedTag {
        /** uper case tag name */
        final String name;
        /** whether this tag must be closed */
        final boolean mustBeClosed;
        /** array of allowed attributes */
        final String[] attributes;

        public CheckedTag(String name, boolean mustBeClosed, String[] attributes) {
            this.name = name;
            this.mustBeClosed = mustBeClosed;
            this.attributes = attributes;
        }
    }
}
