/*
 * User: literakl
 * Date: 11.4.2004
 * Time: 17:37:33
 */
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Record;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;
import org.dom4j.Document;
import org.dom4j.Node;
import org.htmlparser.Parser;
import org.htmlparser.StringNode;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.lexer.nodes.Attribute;
import org.htmlparser.tags.Tag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.visitors.NodeVisitor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Returns article in docbook format.
 */
public class ShowDocbook implements AbcAction {
    public static final String PARAM_RELATION_ID = "relationId";
    public static final String PARAM_RELATION_ID_SHORT = "rid";

    Writer writer;

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        String uri = request.getRequestURI();
        String id = uri.substring(uri.lastIndexOf('/')+1,uri.lastIndexOf(".docb"));
        Relation relation = new Relation(Integer.parseInt(id));

        response.setContentType("text/html; charset=ISO-8859-2");
        PrintWriter writer = response.getWriter();
        printDocbook(writer, relation);
        writer.flush();
        return null;
    }

    protected synchronized void printDocbook(Writer writer, Relation relation) throws Exception {
        this.writer = writer;
        Persistance persistance = PersistanceFactory.getPersistance();
        relation = (Relation) persistance.findById(relation);
        Item article = (Item) persistance.findById(relation.getChild());
        Document doc = article.getData();

        print("<?xml version='1.0' encoding='ISO-8859-2'?>", 0, true);
        print("<!DOCTYPE article PUBLIC \"-//OASIS//DTD DocBook XML V4.1.2//EN\" \"http://www.oasis-open.org/docbook/xml/4.0/docbookx.dtd\">\n", 0, true);
        print("<article>", 0, true);
        print("<title>"+Tools.xpath(doc, "/data/name")+"</title>", 0, true);

        print("<articleinfo>", 0, true);
        print("<date>"+Constants.czDateOnly.format(article.getCreated())+"</date>", 1, true);
        printAuthor(writer, doc, persistance);
        print("</articleinfo>", 0, true);

        print("<para>", 0, true);
        print(doc.selectSingleNode("/data/perex").getText(), 1, true);
        print("</para>", 0, true);

        Map children = Tools.groupByType(article.getChildren());
        List list = (List) children.get(Constants.TYPE_RECORD);
        Record record = (Record) ((Relation) list.get(0)).getChild();
        doc = record.getData();

        List nodes = doc.selectNodes("/data/content");
        for ( Iterator iter = nodes.iterator(); iter.hasNext(); ) {
            Node node = (Node) iter.next();
            ByteArrayInputStream bais = new ByteArrayInputStream(node.getText().getBytes());
            Parser parser = new Parser(new Lexer(new Page(bais, "ISO-8859-2")));
            parser.setEncoding("ISO-8859-2");
            DocbookVisitor visitor = new DocbookVisitor();
            parser.visitAllNodesWith(visitor);
        }

        print("</article>", 0, true);
    }

    protected void printAuthor(Writer writer, Document document, Persistance persistance) {
        String id = document.selectSingleNode("/data/author").getText();
        User user = (User) persistance.findById(new User(Integer.parseInt(id)));
        String name = user.getName();
        int space = name.indexOf(" ");
        print("<author>", 1, true);
        if (space!=-1) {
            print("<firstname>"+name.substring(0,space)+"</firstname>", 2, true);
            print("<surname>"+name.substring(space+1)+"</surname>", 2, true);
        } else {
            print("<surname>"+name+"</surname>", 2, true);
        }
        print("</author>", 1, true);
    }

    protected void println() {
        try {
            writer.write('\n');
        } catch (IOException e) {
            // ignore
        }
    }

    protected void print(String message, int level, boolean newline) {
        try {
            for ( int i = 0; i<level; i++ )
                writer.write("\t");
            writer.write(message);
            if (newline)
                writer.write('\n');
        } catch (IOException e) {
            // ignore
        }
    }

    public class DocbookVisitor extends NodeVisitor {
        boolean h1Open, h2Open, h3Open;
        boolean inline, citeP, citeDiv, citeSpan;

        public void visitTag(Tag tag) {
            String tagName = tag.getTagName();
            if ("H1".equals(tagName))
                visitH1Start();
            else if ("H2".equals(tagName))
                visitH2Start();
            else if ( "P".equals(tagName) )
                visitPStart(tag);
            else if ( "DIV".equals(tagName) )
                visitDivStart(tag);
            else if ( "A".equals(tagName) )
                visitAStart((LinkTag)tag);
            else if ( "UL".equals(tagName) )
                visitUlStart();
            else if ( "OL".equals(tagName) )
                visitOlStart();
            else if ( "LI".equals(tagName) )
                visitLiStart();
            else if ( "TABLE".equals(tagName) )
                visitTableStart();
            else if ( "TR".equals(tagName) )
                visitTrStart();
            else if ( "TBODY".equals(tagName) )
                visitTbodyStart();
            else if ( "TD".equals(tagName) )
                visitTdStart();
            else if ( "IMG".equals(tagName) )
                visitImg((ImageTag) tag);
            else if ( "BR".equals(tagName) )
                visitBr();
            else if ( "SPAN".equals(tagName) )
                visitSpanStart(tag);
            else if ( "I".equals(tagName) )
                visitIStart();
            else if ( "B".equals(tagName) )
                visitBStart();
            else if ( "CODE".equals(tagName) )
                visitCodeStart();
            else
                print("start of unknown tag "+tag.getTagName(), 2, true);
        }

        public void visitEndTag(Tag tag) {
            String tagName = tag.getTagName();
            if ( "H1".equals(tagName) )
                visitH1End();
            else if ( "H2".equals(tagName) )
                visitH2End();
            else if ( "P".equals(tagName) )
                visitPEnd();
            else if ( "DIV".equals(tagName) )
                visitDivEnd();
            else if ( "A".equals(tagName) )
                visitAEnd();
            else if ( "UL".equals(tagName) )
                visitUlEnd();
            else if ( "OL".equals(tagName) )
                visitOlEnd();
            else if ( "LI".equals(tagName) )
                visitLiEnd();
            else if ( "TABLE".equals(tagName) )
                visitTableEnd();
            else if ( "TBODY".equals(tagName) )
                visitTbodyEnd();
            else if ( "TR".equals(tagName) )
                visitTrEnd();
            else if ( "TD".equals(tagName) )
                visitTdEnd();
            else if ( "SPAN".equals(tagName) )
                visitSpanEnd();
            else if ( "I".equals(tagName) )
                visitIEnd();
            else if ( "B".equals(tagName) )
                visitBEnd();
            else if ( "CODE".equals(tagName) )
                visitCodeEnd();
            else
                print("end of unknown tag "+tag.getTagName(), 2, true);
        }

        public void finishedParsing() {
            if ( h2Open )
                print("</section>", 2, true);
            if ( h1Open )
                print("</section>", 1, true);
        }

        public void visitStringNode(StringNode stringNode) {
            String content = stringNode.getText().trim();
            if (content.length()==0)
                return;
            if (inline)
                print(content, 0, false);
            else
                print(stringNode.getText(), 0, false);
        }

        private void visitH1Start() {
            if (h2Open)
                print("</section>", 2, true);
            if (h1Open)
                print("</section>", 1, true);
            println();
            print("<section>", 1, true);
            print("<title>", 2, false);
            h1Open = true;
            h2Open = false;
            h3Open = false;
            inline = true;
        }

        private void visitH1End() {
            print("</title>", 0, true);
            inline = false;
        }

        private void visitH2Start() {
            if (h2Open)
                print("</section>", 2, true);
            println();
            print("<section>", 2, true);
            print("<title>", 3, false);
            h2Open = true;
            inline = true;
        }

        private void visitH2End() {
            print("</title>", 0, true);
            inline = false;
        }

        private void visitPStart(Tag tag) {
            Attribute attribute = tag.getAttributeEx("class");
            if (attribute!=null && "kt_citace".equals(attribute.getValue())) {
                visitKtCitationStart();
                citeP = true;
                return;
            }
            print("<para>", 2, false);
        }

        private void visitPEnd() {
            if (citeP) {
                visitKtCitationEnd();
                citeP = false;
                return;
            }
            println();
            print("</para>", 2, true);
            inline = false;
        }

        private void visitDivStart(Tag tag) {
            Attribute attribute = tag.getAttributeEx("class");
            if ( attribute!=null && "kt_citace".equals(attribute.getValue()) ) {
                visitKtCitationStart();
                citeDiv = true;
                return;
            }
            print("<para>", 2, false);
        }

        private void visitDivEnd() {
            if (citeDiv) {
                visitKtCitationEnd();
                citeDiv = false;
                return;
            }
            println();
            print("</para>", 2, true);
            inline = false;
        }

        private void visitSpanStart(Tag tag) {
            Attribute attribute = tag.getAttributeEx("class");
            if ( attribute!=null && "kt_citace".equals(attribute.getValue()) ) {
                visitKtCitationStart();
                citeSpan = true;
                return;
            }
        }

        private void visitSpanEnd() {
            if (citeSpan) {
                visitKtCitationEnd();
                citeSpan = false;
                return;
            }
        }

        private void visitKtCitationStart() {
            print("<citation>", 0, false);
            inline = true;
        }

        private void visitKtCitationEnd() {
            print("</citation>", 0, true);
            inline = false;
        }

        private void visitAStart(LinkTag tag) {
            print("<ulink url=\""+tag.getLink()+"\">", 0, false);
            inline = true;
        }

        private void visitAEnd() {
            print("</ulink>", 0, false);
            inline = false;
        }

        public void visitImg(ImageTag tag) {
            print("<figure>", 2, true);
            print("<title>Titulek</title>", 3, true);
            print("<mediaobject>", 3, true);
            print("<imageobject>", 3, true);
            print("<imagedata fileref=\""+tag.getImageURL()+"\">", 3, false);
            print("</imagedata>", 0, true);
            print("</imageobject>", 3, true);
            print("</mediaobject>", 3, true);
            print("</figure>", 2, true);
        }

        private void visitUlStart() {
            print("<itemizedlist>", 2, true);
            inline = false;
        }

        private void visitUlEnd() {
            print("</itemizedlist>", 2, true);
            inline = false;
        }

        private void visitOlStart() {
            print("<orderedlist>", 2, true);
            inline = false;
        }

        private void visitOlEnd() {
            print("</orderedlist>", 2, true);
            inline = false;
        }

        private void visitLiStart() {
            print("<listitem>", 3, true);
            inline = false;
        }

        private void visitLiEnd() {
            print("</listitem>", 2, true);
            inline = false;
        }

        private void visitTableStart() {
            print("<informaltable>", 2, true);
            print("<tgroup>", 2, true);
            print("<tbody>", 2, true);
            inline = false;
        }

        private void visitTableEnd() {
            print("</tbody>", 2, true);
            print("</tgroup>", 2, true);
            print("</informaltable>", 2, true);
            inline = false;
        }

        private void visitTrStart() {
            print("<row>", 3, true);
        }

        private void visitTrEnd() {
            print("</row>", 3, true);
        }

        private void visitTdStart() {
            print("<entry>", 3, false);
        }

        private void visitTdEnd() {
            print("</entry>", 0, true);
        }

        private void visitIStart() {
            print("<emphasis>", 0, false);
            inline = true;
        }

        private void visitIEnd() {
            print("</emphasis>", 0, false);
            inline = false;
        }

        private void visitBStart() {
            print("<emphasis>", 0, false);
            inline = true;
        }

        private void visitBEnd() {
            print("</emphasis>", 0, false);
            inline = false;
        }

        private void visitBr() {}
        private void visitTbodyStart() {}
        private void visitTbodyEnd() {}

        private void visitCodeStart() {
            print("<command>", 0, false);
            inline = true;
        }

        private void visitCodeEnd() {
            print("</command>", 0, false);
            inline = false;
        }
    }

    public static void main(String[] args) throws Exception {
        Relation relation = new Relation(Misc.parseInt(args[0],0));
        ShowDocbook instance = new ShowDocbook();
        OutputStreamWriter writer = new OutputStreamWriter(System.out);
        instance.printDocbook(writer, relation);
        writer.flush();
    }
}
