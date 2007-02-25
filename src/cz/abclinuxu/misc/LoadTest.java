/*
 *  Copyright (C) 2007 Leos Literak
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

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.htmlcleaner.HtmlCleaner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Downloads selected list of pages to configured depth and outputs
 * time spent by this test.
 * @author literakl
 * @since 24.2.2007
 */
public class LoadTest {
    // load all articles sections, that have enough content to be paginated
    static Config articleSectionsConfig = new Config(new String[]{"/clanky/bezpecnost", "/clanky/hardware", "/clanky/system",
                                                            "/clanky/jaderne-noviny", "/clanky/multimedia", "/clanky/navody",
                                                            "/clanky/novinky", "/clanky/programovani", "/clanky/recenze",
                                                            "/clanky/rozhovory"}, 1);
    static Config miniConfig = new Config(new String[]{"/clanky/bezpecnost"}, 1);

    static String urlPrefix = "http://localhost:8080";
    static int downloaded;

    public static void main(String[] args) throws Exception {
        // initialize
        loadPage("/", false);
        downloaded = 0;

        Config config = articleSectionsConfig;
        List<String> urls = new ArrayList<String>(Arrays.asList(config.urls));

        long start = System.currentTimeMillis();
        crawl(urls, 0, config.depth);
        long end = System.currentTimeMillis();
        System.out.println();
        System.out.println("Test took " + (end - start)/1000 + " seconds, downloaded " + downloaded + " pages");
    }

/*
aktualni implementace Nursery, size: 750 objektu
Test took 705 seconds, downloaded 762 pages
Test took 714 seconds, downloaded 762 pages
aktualni implementace Nursery, size: 2500 objektu
Test took 689 seconds, downloaded 762 pages
Test took 681 seconds, downloaded 762 pages
stara Nursery, size: 750 objektu
Test took 727 seconds, downloaded 762 pages
Test took 710 seconds, downloaded 762 pages
stara Nursery, size: 2500 objektu
Test took 719 seconds, downloaded 762 pages
aktualni implementace Nursery, size: 750 objektu, whirly cache
Test took 684 seconds, downloaded 762 pages
Test took 684 seconds, downloaded 762 pages
*/

    static void crawl(List<String> pages, int currentDepth, int maximumDepth) throws Exception {
        List<String> next = new ArrayList<String>();
        for (String s : pages) {
            List<String> found = loadPage(s, maximumDepth > currentDepth);
            next.addAll(found);
        }
        if (currentDepth < maximumDepth)
            crawl(next, currentDepth + 1, maximumDepth);
    }

    /**
     * Loads given page and returns all relative urls in the content area
     * @param url relative url
     * @return relative urls found in content area
     * @throws Exception problem
     */
    static List<String> loadPage(String url, boolean analyzePage) throws Exception {
        List<String> childUrls = new ArrayList<String>();
        String page = readUrl(urlPrefix + url);
        downloaded++;
        System.out.print("#");
        if (downloaded % 60 == 0)
            System.out.println();
        if (! analyzePage)
            return Collections.emptyList();

        HtmlCleaner cleaner = new HtmlCleaner(page);
        cleaner.clean();
        String normalizedPage = cleaner.getCompactXmlAsString();

        Document document = DocumentHelper.parseText(normalizedPage);
        List links = document.selectNodes("//div[@id='st']//a");
        for (Iterator iter = links.iterator(); iter.hasNext();) {
            Element element = (Element) iter.next();
            String href = element.attributeValue("href");
            if (href == null)
                continue;
            if (href.startsWith("/"))
                childUrls.add(href);
        }

        return childUrls;
    }

    static String readUrl(String url) throws IOException {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);
        byte[] responseBody;
        try {
            method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
                return null;
            }


            responseBody = method.getResponseBody();
        } finally {
            method.releaseConnection();
        }
        return new String(responseBody);
    }

    static class Config {
        String[] urls;
        int depth;

        /**
         * Constructor
         * @param urls List of relative urls to be fetched during test
         * @param depth 0 load only given urls, 1 fetch relative links in content area of downloaded pages
         */
        public Config(String[] urls, int depth) {
            this.urls = urls;
            this.depth = depth;
        }
    }
}
