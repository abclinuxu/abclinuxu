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
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Record;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
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
        Persistence persistence = PersistenceFactory.getPersistance();
        relation = (Relation) persistence.findById(relation);
        Item article = (Item) persistence.findById(relation.getChild());
        Document doc = article.getData();

        print("<?xml version='1.0' encoding='ISO-8859-2'?>", 0, true);
        print("<!DOCTYPE article PUBLIC \"-//OASIS//DTD DocBook XML V4.1.2//EN\" \"http://www.oasis-open.org/docbook/xml/4.0/docbookx.dtd\">\n", 0, true);
        print("<article>", 0, true);
        print("<title>"+Tools.xpath(doc, "/data/name")+"</title>", 0, true);

        print("<articleinfo>", 0, true);
        print("<date>"+Constants.czDayMonthYear.format(article.getCreated())+"</date>", 1, true);
        printAuthor(writer, doc, persistence);
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

    protected void printAuthor(Writer writer, Document document, Persistence persistence) {
        String id = document.selectSingleNode("/data/author").getText();
        User user = (User) persistence.findById(new User(Integer.parseInt(id)));
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
            else if ("H3".equals(tagName))
                visitH3Start();
            else if ( "P".equals(tagName) )
                visitPStart(tag);
            else if ( "DIV".equals(tagName) )
                visitDivStart(tag);
            else if ( "PRE".equals(tagName) )
                visitPreStart();
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
            else if ( "THEAD".equals(tagName) )
                visitTheadStart();
            else if ( "TR".equals(tagName) )
                visitTrStart();
            else if ( "TBODY".equals(tagName) )
                visitTbodyStart();
            else if ( "TD".equals(tagName) )
                visitTdStart();
            else if ( "TH".equals(tagName) )
                visitTdStart();
            else if ( "IMG".equals(tagName) )
                visitImg((ImageTag) tag);
            else if ( "BR".equals(tagName) )
                visitBr();
            else if ( "SPAN".equals(tagName) )
                visitSpanStart(tag);
            else if ( "I".equals(tagName) )
                visitIStart();
            else if ( "Q".equals(tagName) || "BLOCKQUOTE".equals(tagName) || "ABBR".equals(tagName) )
                visitIStart();
            else if ( "B".equals(tagName) )
                visitBStart();
            else if ( "EM".equals(tagName) )
                visitEmStart();
            else if ( "STRONG".equals(tagName) )
                visitStrongStart();
            else if ( "CODE".equals(tagName) )
                visitCodeStart();
            else if ( "SUB".equals(tagName) )
                visitSubStart();
            else if ( "SUP".equals(tagName) )
                visitSupStart();
            else if ( "HR".equals(tagName) )
                visitHr();
            else if ( "DL".equals(tagName) )
                visitDlStart();
            else if ( "DT".equals(tagName) )
                visitDtStart();
            else if ( "DD".equals(tagName) )
                visitDdStart();
            else
                print("&lt;"+tag.getTagName()+"&gt;", 2, true);
        }

        public void visitEndTag(Tag tag) {
            String tagName = tag.getTagName();
            if ( "H1".equals(tagName) )
                visitH1End();
            else if ( "H2".equals(tagName) )
                visitH2End();
            else if ( "H3".equals(tagName) )
                visitH3End();
            else if ( "P".equals(tagName) )
                visitPEnd();
            else if ( "DIV".equals(tagName) )
                visitDivEnd();
            else if ( "PRE".equals(tagName) )
                visitPreEnd();
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
            else if ( "THEAD".equals(tagName) )
                visitTheadEnd();
            else if ( "TBODY".equals(tagName) )
                visitTbodyEnd();
            else if ( "TR".equals(tagName) )
                visitTrEnd();
            else if ( "TD".equals(tagName) )
                visitTdEnd();
            else if ( "TH".equals(tagName) )
	           	visitTdEnd();
	       else if ( "SPAN".equals(tagName) )
                visitSpanEnd();
            else if ( "I".equals(tagName) )
                visitIEnd();
            else if ( "Q".equals(tagName) || "BLOCKQUOTE".equals(tagName) || "ABBR".equals(tagName) )
	        	visitIEnd();
	        else if ( "B".equals(tagName) )
                visitBEnd();
            else if ( "EM".equals(tagName) )
                visitEmEnd();
            else if ( "STRONG".equals(tagName) )
                visitStrongEnd();
            else if ( "CODE".equals(tagName) )
                visitCodeEnd();
            else if ( "SUB".equals(tagName) )
                visitSubEnd();
            else if ( "SUP".equals(tagName) )
                visitSupEnd();
            else if ( "DL".equals(tagName) )
                visitDlEnd();
            else if ( "DT".equals(tagName) )
                visitDtEnd();
            else if ( "DD".equals(tagName) )
                visitDdEnd();
            else
                print("&lt;/" + tag.getTagName() + "&gt;", 2, true);
        }

        public void finishedParsing() {
            if ( h2Open )
                print("</section>", 2, true);
            if ( h1Open )
                print("</section>", 1, true);
        }

        public void visitStringNode(StringNode stringNode) {
            String content = stringNode.getText();
            if (content.length()==0)
                return;
            content = escapeAmpersand(content);

            if (inline)
                print(content, 0, false);
            else
                print(content, 0, false);
        }

        private void visitH1Start() {
            if (h3Open)
                print("</section>", 3, true);
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
            if (h3Open)
                print("</section>", 3, true);
            if (h2Open)
                print("</section>", 2, true);
            println();
            print("<section>", 2, true);
            print("<title>", 3, false);
            h2Open = true;
            h3Open = false;
            inline = true;
        }

        private void visitH2End() {
            print("</title>", 0, true);
            inline = false;
        }

        private void visitH3Start() {
            if (h3Open)
                print("</section>", 3, true);
            println();
            print("<section>", 3, true);
            print("<title>", 4, false);
            h3Open = true;
            inline = true;
        }

        private void visitH3End() {
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

        private void visitPreStart() {
            print("<programlisting>", 2, false);
        }

        private void visitPreEnd() {
            print("</programlisting>", 2, true);
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
            String address = escapeAmpersand(tag.getLink());
            print("<ulink url=\""+address+"\">", 0, false);
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
            print("<listitem><para>", 3, true);
            inline = false;
        }

        private void visitLiEnd() {
            print("</para></listitem>", 3, true);
            inline = false;
        }

        private void visitTableStart() {
            print("<informaltable>", 2, true);
            print("<tgroup cols=\"XXX\">", 2, true);
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

        private void visitEmStart() {
            print("<emphasis>", 0, false);
            inline = true;
        }

        private void visitEmEnd() {
            print("</emphasis>", 0, false);
            inline = false;
        }

        private void visitStrongStart() {
            print("<emphasis>", 0, false);
            inline = true;
        }

        private void visitStrongEnd() {
            print("</emphasis>", 0, false);
            inline = false;
        }

        private void visitSubStart() {
            print("<subscript>", 0, false);
            inline = true;
        }

        private void visitSubEnd() {
            print("</subscript>", 0, false);
            inline = false;
        }

        private void visitSupStart() {
            print("<superscript>", 0, false);
            inline = true;
        }

        private void visitSupEnd() {
            print("</superscript>", 0, false);
            inline = false;
        }

        private void visitBr() {
            print("[BR]", 0, true);
        }

        private void visitDlStart() {
            print("<variablelist>", 2, true);
            inline = false;
        }

        private void visitDlEnd() {
            print("</variablelist>", 2, true);
            inline = false;
        }

        private void visitDtStart() {
            print("<varlistentry><term>", 3, true);
            inline = false;
        }

        private void visitDtEnd() {
            print("</term></varlistentry>", 3, true);
            inline = false;
        }

        private void visitDdStart() {
            print("<listitem><para>", 3, true);
            inline = false;
        }

        private void visitDdEnd() {
            print("</para></listitem>", 3, true);
            inline = false;
        }

        private void visitHr() {}
        private void visitTbodyStart() {}
        private void visitTbodyEnd() {}
        private void visitTheadStart() {}
        private void visitTheadEnd() {}

        private void visitCodeStart() {
            print("<command>", 0, false);
            inline = true;
        }

        private void visitCodeEnd() {
            print("</command>", 0, false);
            inline = false;
        }
    }

    private String escapeAmpersand(String input) {
        return input.replaceAll("&","&amp;");
    }

    public static void main(String[] args) throws Exception {
        Relation relation = new Relation(Misc.parseInt(args[0],0));
        ShowDocbook instance = new ShowDocbook();
        OutputStreamWriter writer = new OutputStreamWriter(System.out);
        instance.printDocbook(writer, relation);
        writer.flush();
    }
}
