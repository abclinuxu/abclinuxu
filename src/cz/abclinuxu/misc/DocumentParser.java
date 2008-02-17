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
package cz.abclinuxu.misc;

import cz.abclinuxu.data.view.ParsedDocument;
import cz.abclinuxu.data.view.DiscussionRecord;
import cz.abclinuxu.data.view.Comment;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Record;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Poll;
import cz.abclinuxu.data.PollChoice;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.exceptions.InternalException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Document;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.Set;
import java.util.Iterator;
import java.util.List;

/**
 * @author literakl
 * @since 15.2.2008
 */
public class DocumentParser {
    private static Logger log = Logger.getLogger(DocumentParser.class);
    static Persistence persistence = PersistenceFactory.getPersistence();

    /**
     * Extracts text from an Item instance.
     * @param item initialized item
     * @return ParsedDocument
     */
    public static ParsedDocument parse(Item item) {
        switch (item.getType()) {
            case Item.ARTICLE:
                return parseArticle(item);
            case Item.AUTHOR:
                return parseAuthor(item);
            case Item.BAZAAR:
                return parseBazaar(item);
            case Item.BLOG:
                return parseBlogStory(item);
            case Item.CONTENT:
                return parseWikiContent(item);
            case Item.DICTIONARY:
                return parseDictionary(item);
            case Item.DISCUSSION:
                return parseDiscussion(item);
            case Item.DRIVER:
                return parseDriver(item);
            case Item.FAQ:
                return parseFaq(item);
            case Item.HARDWARE:
                return parseHardware(item);
            case Item.NEWS:
                return parseNews(item);
            case Item.PERSONALITY:
                return parsePersonality(item);
            case Item.SCREENSHOT:
                return parseScreenshot(item);
            case Item.SERIES:
                return parseSeries(item);
            case Item.SOFTWARE:
                return parseSoftware(item);
        }

        throw new InternalException("Unsupported item type: " + item.getType());
    }

    /**
     * Extracts text from an Category instance.
     * @param category initialized category
     * @return ParsedDocument
     */
    public static ParsedDocument parse(Category category) {
        if (category.getType() == Category.BLOG)
            return parseBlogSection(category);
        else
            return parseSection(category);
    }

    /**
     * Extracts text from an Poll instance.
     * @param poll initialized poll
     * @return ParsedDocument
     */
    public static ParsedDocument parse(Poll poll) {
        StringBuffer sb = new StringBuffer();

        sb.append(poll.getText());
        for (int i = 0; i < poll.getChoices().length; i++) {
            PollChoice choice = poll.getChoices()[i];
            sb.append(" ").append(choice.getText());
        }

        return normalize(sb);
    }

    private static ParsedDocument parseBlogSection(Category category) {
        StringBuffer sb = new StringBuffer();

        Element data = (Element) category.getData().selectSingleNode("//custom");
        Node node = data.element("page_title");
        sb.append(node.getText());

        node = data.element("title");
        if (node != null)
            sb.append(node.getText());

        node = data.element("intro");
        if (node != null)
            sb.append(" ").append(node.getText());

        return normalize(sb);
    }

    private static ParsedDocument parseSection(Category category) {
        StringBuffer sb = new StringBuffer();

        Element data = category.getData().getRootElement();
        Node node = data.element("name");
        sb.append(node.getText());

        node = data.element("note");
        if (node != null)
            sb.append(" ").append(node.getText());

        return normalize(sb);
    }

    private static ParsedDocument parseHardware(Item item) {
        StringBuffer sb = new StringBuffer();

        Element data = item.getData().getRootElement();
        Node node = data.element("name");
        sb.append(node.getText());

        node = data.element("setup");
        if (node != null)
            sb.append(" ").append(node.getText());

        node = data.element("params");
        if (node != null)
            sb.append(" ").append(node.getText());

        node = data.element("identification");
        if (node != null)
            sb.append(" ").append(node.getText());

        node = data.element("note");
        if (node != null)
            sb.append(" ").append(node.getText());

        return normalize(sb);
    }

    private static ParsedDocument parseArticle(Item article) {
        StringBuffer sb = new StringBuffer();

        Set authors = article.getProperty(Constants.PROPERTY_AUTHOR);
        for (Iterator iter = authors.iterator(); iter.hasNext();) {
            int rid = Misc.parseInt((String) iter.next(), 0);
            storeAuthor(rid, sb);
        }

        Element data = article.getData().getRootElement();

        Node node = data.element("name");
        sb.append(node.getText());

        node = data.element("perex");
        if (node != null)
            sb.append(" ").append(node.getText());

        for (Iterator iter = article.getChildren().iterator(); iter.hasNext();) {
            Relation child = (Relation) iter.next();

            if (child.getChild() instanceof Record) {
                Record record = (Record) persistence.findById(child.getChild());
                if (record.getType() == Record.ARTICLE) {
                    List nodes = record.getData().selectNodes("/data/content");
                    if (nodes.size() == 1) {
                        sb.append(((Node) nodes.get(0)).getText());
                        sb.append(" ");
                    } else
                        for (Iterator iter2 = nodes.iterator(); iter2.hasNext();) {
                            node = (Element) iter2.next();
                            sb.append(node.getText());
                            sb.append(" ");
                            sb.append(((Element) node).attributeValue("title"));
                            sb.append(" ");
                        }
                }
            }
        }

        return normalize(sb);
    }

    private static ParsedDocument parseDiscussion(Item discussion) {
        StringBuffer sb = new StringBuffer();

        Document document = discussion.getData();
        Element data = document.getRootElement();
        Node node = data.element("title");
        if (node != null)
            sb.append(node.getText());
        node = data.element("text");
        if (node != null)
            sb.append(" ").append(node.getText());

        Record record = null;
        for (Relation childRel : discussion.getChildren()) {
            GenericObject obj = childRel.getChild();
            if ( !(obj instanceof Record))
                continue;
            record = (Record) obj;
            break;
        }

        if (record != null) {
            record = (Record) persistence.findById(record);
            DiscussionRecord dizRecord = (DiscussionRecord) record.getCustom();
            LinkedList stack = new LinkedList(dizRecord.getThreads());

            while (stack.size() > 0) {
                Comment comment = (Comment) stack.removeFirst();
                stack.addAll(comment.getChildren());
                String s = comment.getTitle();
                if (s != null) {
                    sb.append(" ");
                    sb.append(s);
                }

                node = comment.getData().getRootElement().element("text");
                if (node != null) {
                    sb.append(" ");
                    sb.append(node.getText());
                }

                s = comment.getAnonymName();
                if (s != null) {
                    sb.append(" ");
                    sb.append(s);
                } else {
                    Integer id = comment.getAuthor();
                    if (id != null)
                        storeUser(id, sb);
                }
            }
        }

        return normalize(sb);
    }

    private static ParsedDocument parseDriver(Item item) {
        StringBuffer sb = new StringBuffer();

        Element data = item.getData().getRootElement();
        Node node = data.element("name");
        sb.append(node.getText());

        node = data.element("note");
        if (node != null)
            sb.append(" ").append(node.getText());

        return normalize(sb);
    }

    private static ParsedDocument parseNews(Item news) {
        StringBuffer sb = new StringBuffer();

        Element data = news.getData().getRootElement();
        Node node = data.element("title");
        if (node == null) {
//            log.warn("Zpravicka nema titulek! " + news.getId()); // todo zkontrolovat, zda se to deje
        } else
            sb.append(node.getText());

        node = data.element("content");
        sb.append(" ").append(node.getText());

        storeUser(news.getOwner(), sb);
        return normalize(sb);
    }

    private static ParsedDocument parseDictionary(Item item) {
        StringBuffer sb = new StringBuffer();

        Element data = item.getData().getRootElement();
        Node node = data.element("name");
        sb.append(node.getText());
        node = data.element("description");
        sb.append(" ").append(node.getText());

        return normalize(sb);
    }

    private static ParsedDocument parseWikiContent(Item item) {
        StringBuffer sb = new StringBuffer();

        Element data = item.getData().getRootElement();
        Node node = data.element("name");
        sb.append(node.getText());
        node = data.element("content");
        sb.append(" ").append(node.getText());

        return normalize(sb);
    }

    private static ParsedDocument parseBlogStory(Item story) {
        StringBuffer sb = new StringBuffer();

        Element data = story.getData().getRootElement();
        Node node = data.element("name");
        sb.append(node.getText());

        node = data.element("perex");
        if (node != null)
            sb.append(" ").append(node.getText());

        node = data.element("content");
        sb.append(" ").append(node.getText());

        storeUser(story.getOwner(), sb);
        return normalize(sb);
    }

    private static ParsedDocument parseFaq(Item item) {
        StringBuffer sb = new StringBuffer();

        Element data = item.getData().getRootElement();
        Node node = data.element("title");
        sb.append(node.getText());
        node = data.element("text");
        sb.append(node.getText());

        return normalize(sb);
    }

    private static ParsedDocument parseSoftware(Item item) {
        StringBuffer sb = new StringBuffer();
        Element data = item.getData().getRootElement();

        Node node = data.element("name");
        sb.append(node.getText());

        node = data.element("description");
        if (node != null)
            sb.append(" ").append(node.getText());

        return normalize(sb);
    }

    private static ParsedDocument parseBazaar(Item item) {
        StringBuffer sb = new StringBuffer();

        Element data = item.getData().getRootElement();
        sb.append(data.elementText("title"));

        String content = data.element("text").getText();
        sb.append(" ").append(content);

        Node node = data.element("price");
        if (node != null)
            sb.append(" ").append(node.getText());

        node = data.element("contact");
        if (node != null)
            sb.append(" ").append(node.getText());

        storeUser(item.getOwner(), sb);
        return normalize(sb);
    }

    private static ParsedDocument parseAuthor(Item item) {
        StringBuffer sb = new StringBuffer();

        Element data = item.getData().getRootElement();
        Node node = data.element("firstname");
        if (node != null)
            sb.append(node.getText());

        node = data.element("surname");
        if (node != null)
            sb.append(" ").append(node.getText());

        node = data.element("nickname");
        if (node != null)
            sb.append(" ").append(node.getText());

        return normalize(sb);
    }

    private static ParsedDocument parseSeries(Item item) {
        StringBuffer sb = new StringBuffer();

        Element data = item.getData().getRootElement();
        Node node = data.element("name");
        sb.append(node.getText());

        node = data.element("description");
        if (node != null)
            sb.append(" ").append(node.getText());

        return normalize(sb);
    }

    private static ParsedDocument parsePersonality(Item item) {
        StringBuffer sb = new StringBuffer();

        Element data = item.getData().getRootElement();
        Node node = data.element("firstname");
        if (node != null)
            sb.append(node.getText());

        node = data.element("surname");
        if (node != null)
            sb.append(" ").append(node.getText());

        node = data.element("description");
        sb.append(" ").append(node.getText());

        return normalize(sb);
    }

    private static ParsedDocument parseScreenshot(Item item) {
        StringBuffer sb = new StringBuffer();

        Element data = item.getData().getRootElement();
        Node node = data.element("title");
        sb.append(node.getText());

        node = data.element("description");
        if (node != null)
            sb.append(" ").append(node.getText());

        return normalize(sb);
    }

    /**
     * Removes HTML tags and other garbage from given stringbuffer.
     * @param sb text that may contain html tags
     * @return text without html tags
     */
    private static ParsedDocument normalize(StringBuffer sb) {
        String s = Tools.removeTags(sb.toString());
        return new ParsedDocument(s);
    }

    /**
     * Appends user information into stringbuffer. If there is no such user,
     * error is ignored and this method does nothing.
     * @param rid author relation id
     * @param sb
     */
    private static void storeAuthor(int rid, StringBuffer sb) {
        try {
            sb.append(" ");
            Relation relation = (Relation) persistence.findById(new Relation(rid));
            Item author = (Item) persistence.findById(relation.getChild());
            sb.append(Tools.childName(author));
            sb.append(" ");
        } catch (NotFoundException e) {
            // user could be deleted
        }
    }

    /**
     * Appends user information into stringbuffer. If there is no such user,
     * error is ignored and this method does nothing.
     * @param id user id
     * @param sb
     */
    private static void storeUser(int id, StringBuffer sb) {
        try {
            sb.append(" ");
            User user = (User) persistence.findById(new User(id));
            String nick = user.getNick();
            if (nick != null) {
                sb.append(nick);
                sb.append(" ");
            }
            sb.append(user.getName());
            sb.append(" ");
        } catch (NotFoundException e) {
            // user could be deleted
        }
    }
}
