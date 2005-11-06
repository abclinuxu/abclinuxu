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
package cz.abclinuxu.utils.offline;

import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.data.*;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.Constants;

import java.util.*;
import java.io.*;

import org.apache.regexp.RE;
import org.apache.regexp.StringCharacterIterator;

/**
 * Fixes URL in dumped offline HTML pages.
 * All local absolute URLs are processed
 * and their object type is detected. If it
 * was serialized, it will be converted
 * to correct path in filesystem. If the object
 * was not dumped, the link will be updated to
 * contain server.
 */
public class FixUrls {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FixUrls.class);
    static final String TYPE_ARTICLE = "article";
    static final String TYPE_NEWS = "news";
    static final String TYPE_HARDWARE = "hardware";
    static final String TYPE_SOFTWARE = "software";
    static final String TYPE_DRIVER = "driver";
    static final String TYPE_FAQ = "faq";
    static final String TYPE_DISCUSSION = "discussion";
    static final String TYPE_DICTIONARY = "dictionary";
    static final String TYPE_BLOG = "blog";
    static final String TYPE_SECTION = "section";
    static final String TYPE_POLL = "poll";
    static final String TYPE_SURVEY = "survey";
    static final String TYPE_CONTENT = "content";

    // configuration of which objects has been dumped and which shall have online URL
    boolean onlineArticles = false;
    boolean onlineNews = true;
    boolean onlineHardware = false;
    boolean onlineSoftware = true;
    boolean onlineDriver = false;
    boolean onlineFaq = false;
    boolean onlineDiscusion = false;
    boolean onlineDictionary = false;
    boolean onlineBlog = true;
    boolean onlineSection = false;
    boolean onlinePoll = true;
    boolean onlineSurvey = true;
    boolean onlineContent = true;

    RE reHref;
    Map textUrls = new HashMap(30000); // URL -> Integer (relationId)
    Map knownObjects = new HashMap(50000); // Integer (relationId) -> String
    Persistance persistance;
    Dump dump;
    final int DICTIONARY_PREFIX_LENGTH = UrlUtils.PREFIX_DICTIONARY.length()+1;

    public static void main(String[] args) throws Exception {
        if ( args.length==0 ) {
            System.out.println("Parameter directory is missing!");
            System.exit(1);
        }

        FixUrls fix = new FixUrls();
        long start = System.currentTimeMillis();
        fix.fixUrlsInFiles(args[0]);
        long end = System.currentTimeMillis();
        System.out.println("Total time: "+(end-start)/1000+" seconds");
    }

    public FixUrls() throws Exception {
        dump = new Dump();
        persistance = PersistanceFactory.getPersistance();
        reHref = new RE("(HREF|SRC)(=\")(/[^\"]+)(\")", RE.MATCH_CASEINDEPENDENT);
    }

    /**
     * Gets URL according to rules. It assumes that URL starts with /. If not,
     * currentUrl is returned unmodified. Then it is looked up in textUrls cache
     * and database, so we know relation id. If nothing is found and it does not
     * ends with number, currentURL is returned unmodified. Then we know relation id.
     * We seeks knownObjects and database to find object type. Then according to
     * configuration either local URL is generated or server's URL is used.
     * @param currentURL
     * @return URL
     */
    String getUrl(String currentURL) {
        if (currentURL==null)
            return "";
        if (!currentURL.startsWith("/"))
            return currentURL;
        if (currentURL.startsWith("/images"))
            return Dump.IMAGES_LOCAL_PATH + currentURL;
        if (currentURL.startsWith("/download"))
            return Dump.PORTAL_URL + currentURL;

        int targetPos = currentURL.indexOf('#');
        String target = null;
        if (targetPos > -1) {
            currentURL = currentURL.substring(0, targetPos);
            target = currentURL.substring(targetPos);
        }

        Integer rid = getTextUrlRelationId(currentURL);
        if (rid==null)
            rid = getRelationIdFromUrl(currentURL);
        if (rid==null)
            return currentURL;

        String object = detectType(rid, currentURL);
        if (TYPE_ARTICLE.equals(object))
            if (onlineArticles)
                return getOnlineUrl(currentURL, target);
            else
                return getLocalUrl(rid, target);
        if (TYPE_BLOG.equals(object))
            if (onlineBlog)
                return getOnlineUrl(currentURL, target);
            else
                return getLocalUrl(rid, target);
        if (TYPE_CONTENT.equals(object))
            if (onlineContent)
                return getOnlineUrl(currentURL, target);
            else
                return getLocalUrl(rid, target);
        if (TYPE_DICTIONARY.equals(object))
            if (onlineDictionary)
                return getOnlineUrl(currentURL, target);
            else
                return getLocalUrl(rid, target);
        if (TYPE_DISCUSSION.equals(object))
            if (onlineDiscusion)
                return getOnlineUrl(currentURL, target);
            else
                return getLocalUrl(rid, target);
        if (TYPE_DRIVER.equals(object))
            if (onlineDriver)
                return getOnlineUrl(currentURL, target);
            else
                return getLocalUrl(rid, target);
        if (TYPE_FAQ.equals(object))
            if (onlineFaq)
                return getOnlineUrl(currentURL, target);
            else
                return getLocalUrl(rid, target);
        if (TYPE_HARDWARE.equals(object))
            if (onlineHardware)
                return getOnlineUrl(currentURL, target);
            else
                return getLocalUrl(rid, target);
        if (TYPE_NEWS.equals(object))
            if (onlineNews)
                return getOnlineUrl(currentURL, target);
            else
                return getLocalUrl(rid, target);
        if (TYPE_POLL.equals(object))
            if (onlinePoll)
                return getOnlineUrl(currentURL, target);
            else
                return getLocalUrl(rid, target);
        if (TYPE_SECTION.equals(object))
            if (onlineSection)
                return getOnlineUrl(currentURL, target);
            else
                return getLocalUrl(rid, target);
        if (TYPE_SOFTWARE.equals(object))
            if (onlineSoftware)
                return getOnlineUrl(currentURL, target);
            else
                return getLocalUrl(rid, target);
        if (TYPE_SURVEY.equals(object))
            if (onlineSurvey)
                return getOnlineUrl(currentURL, target);
            else
                return getLocalUrl(rid, target);

        return getOnlineUrl(currentURL, target);
    }

    private String getOnlineUrl(String currentUrl, String target) {
        if (target==null)
            return Dump.PORTAL_URL + currentUrl;
        else
            return Dump.PORTAL_URL + currentUrl + target;
    }

    private String getLocalUrl(Integer rid, String target) {
        if (target == null)
            return Dump.LOCAL_PATH + "/" + dump.getFile(rid.intValue());
        else
            return Dump.LOCAL_PATH + "/" + dump.getFile(rid.intValue()) + target;
    }

    /**
     * Finds relation id for text url or return null.
     * @return relation id or null
     */
    Integer getTextUrlRelationId(String url) {
        Integer id = (Integer) textUrls.get(url);
        if (id!=null)
            return id;
        if (url.startsWith(UrlUtils.PREFIX_DICTIONARY)) {
            if (url.length()<=DICTIONARY_PREFIX_LENGTH) {
                id = new Integer(Constants.REL_DICTIONARY);
                textUrls.put(url, id);
                return id;
            }

            String name = url.substring(DICTIONARY_PREFIX_LENGTH);
            Relation relation = SQLTool.getInstance().findDictionaryByURLName(name);
            if (relation != null) {
                id = new Integer(relation.getId());
                textUrls.put(url, id);
                return id;
            }
        }
        Relation relation = SQLTool.getInstance().findRelationByURL(url);
        if (relation!=null) {
            id = new Integer(relation.getId());
            textUrls.put(url, id);
        }
        return id;
    }

    /**
     * Extracts relation id from url (numbers after last slash) or return null
     */
    Integer getRelationIdFromUrl(String url) {
        int position = url.lastIndexOf('/');
        if (position >= 0 && (position + 1) < url.length()) {
            String lastPart = url.substring(position + 1).trim();
            int length = lastPart.length();
            char c;
            boolean found = false;
            for (int i = 0; i < length; i++) {
                c = lastPart.charAt(i);
                if (c < '0' || c > '9') {
                    found = true;
                    break;
                }
            }
            if (found)
                return null;

            try {
                Integer id = Integer.valueOf(lastPart);
                return id;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Detects type of child object in relation.
     * @param rid
     * @return one of constants
     */
    String detectType(Integer rid, String url) {
        Relation relation = null;
        try {
            relation = (Relation) Tools.sync(new Relation(rid.intValue()));
        } catch (Exception e) {
            return null;
        }
        GenericObject child = relation.getChild();
        if (child instanceof Category) {
            knownObjects.put(rid, TYPE_SECTION);
            return TYPE_SECTION;
        }
        if (child instanceof Poll) {
            knownObjects.put(rid, TYPE_POLL);
            return TYPE_POLL;
        }
        if (child instanceof Item) {
            switch (((Item)child).getType()) {
                case Item.ARTICLE: {
                    knownObjects.put(rid, TYPE_ARTICLE);
                    return TYPE_ARTICLE;
                }
                case Item.BLOG: {
                    knownObjects.put(rid, TYPE_BLOG);
                    return TYPE_BLOG;
                }
                case Item.CONTENT: {
                    knownObjects.put(rid, TYPE_CONTENT);
                    return TYPE_CONTENT;
                }
                case Item.DICTIONARY: {
                    knownObjects.put(rid, TYPE_DICTIONARY);
                    return TYPE_DICTIONARY;
                }
                case Item.DISCUSSION: {
                    knownObjects.put(rid, TYPE_DISCUSSION);
                    return TYPE_DISCUSSION;
                }
                case Item.DRIVER: {
                    knownObjects.put(rid, TYPE_DRIVER);
                    return TYPE_DRIVER;
                }
                case Item.FAQ: {
                    knownObjects.put(rid, TYPE_FAQ);
                    return TYPE_FAQ;
                }
                case Item.NEWS: {
                    knownObjects.put(rid, TYPE_NEWS);
                    return TYPE_NEWS;
                }
                case Item.SURVEY: {
                    knownObjects.put(rid, TYPE_SURVEY);
                    return TYPE_SURVEY;
                }
                case Item.MAKE: {
                    String type = (url.startsWith("/hardware"))? TYPE_HARDWARE : TYPE_SOFTWARE;
                    knownObjects.put(rid, type);
                    return type;
                }
            }
        }
        return null;
    }

    private String readFile(File file) throws IOException {
        StringBuffer sb = new StringBuffer((int)(1.1*file.length()));
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String s = reader.readLine();
        while (s!=null) {
            sb.append(s);
            sb.append('\n');
            s = reader.readLine();
        }
        return sb.toString();
    }

    /**
     * Finds all files and fix URLs referenced in HREF and SRC attributes.
     */
    private void fixUrlsInFiles(String dirName) throws Exception {
        int total = 0, modified = 0, position = 0, start = 0;
        String content, url, modifiedUrl;
        boolean isModified;
        List stack = new ArrayList();
        stack.add(new File(dirName));

        while (stack.size() > 0) {
            File file = (File) stack.remove(0);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; i++)
                    stack.add(files[i]);
                continue;
            }

            content = readFile(file);
            isModified = false;
            if (reHref.match(content)) {
                position = 0;
                StringBuffer sb = new StringBuffer();
                StringCharacterIterator stringIter = new StringCharacterIterator(content);

                try {
                    do {
                        start = reHref.getParenStart(3);
                        sb.append(stringIter.substring(position, start));
                        url = reHref.getParen(3);
                        modifiedUrl = getUrl(url);
                        if (! url.equals(modifiedUrl))
                            isModified = true;
                        sb.append(modifiedUrl);
                        position = reHref.getParenEnd(3);
                    } while (reHref.match(stringIter, position));
                    sb.append(stringIter.substring(position));
                    total++;

                    if (isModified) {
                        modified++;
                        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                        writer.write(sb.toString());
                        writer.close();
                    }
                } catch (Exception e) {
                    log.error(file + ", position=" + position + ", start=" + start, e);
                    System.exit(1);
                }
            }
        }

        System.out.println("Files modified/processed: "+modified+"/"+total);
    }
}
