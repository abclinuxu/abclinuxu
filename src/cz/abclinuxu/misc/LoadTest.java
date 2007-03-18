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
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Downloads selected list of pages to configured depth and outputs
 * time spent by this test.
 * // todo measure each execution time and perform analysis (min, max, avg, median)
 * @author literakl
 * @since 24.2.2007
 */
public class LoadTest {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LoadTest.class);

    // load all articles sections, that have enough content to be paginated
    private static Config articleSectionsConfig = new Config(new String[]{"/clanky/bezpecnost", "/clanky/hardware",
                        "/clanky/jaderne-noviny", "/clanky/multimedia", "/clanky/navody", "/clanky/novinky",
                        "/clanky/programovani", "/clanky/recenze", "/clanky/rozhovory", "/clanky/system"}, 1);
    private static Config richConfig = new Config(new String[]{"/clanky/bezpecnost", "/clanky/hardware",
                        "/clanky/jaderne-noviny", "/clanky/multimedia", "/clanky/navody", "/clanky/novinky",
                        "/clanky/programovani", "/clanky/recenze", "/clanky/rozhovory", "/clanky/system", "/faq/disky",
                        "/faq/souborove-systemy", "/zpravicky", "/hardware/pridavne-karty", "/software/hry",
                        "/blog", "/poradna", "/ankety", "/slovnik", "/ovladace", "/bazar", "/serialy"}, 1);
    // load single article section with its content
    private static Config miniConfig = new Config(new String[]{"/clanky/bezpecnost"}, 1);
    private static final int THREADS = 5;
    // number of milliseconds since last download, if exceeded, test is considered as finished
    private static final long FINISH_INTERVAL = 500;
    private static String urlPrefix = "http://localhost:8080";

    private static AtomicInteger downloaded = new AtomicInteger(0);
    private static ConcurrentLinkedQueue<Task> tasks;

    public static void main(String[] args) throws Exception {
        LogManager.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
        // initialize caches in server
        loadPage("/", false);

        Config config = richConfig;
        tasks = new ConcurrentLinkedQueue<Task>();
        for (int i = 0; i < config.urls.length; i++) {
            String url = config.urls[i];
            tasks.add(new Task(url, 0, config.depth));
        }

        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(THREADS);
        threadPool.prestartCoreThread();
        long sqlStart = getServedSqlQueries();
        long start = System.currentTimeMillis();
        int emptyResults = 0;
        while (true) {
            Task task = tasks.poll();
            if (task != null) {
                threadPool.submit(new Fetcher(task));
                emptyResults = 0;
            } else {
                if (threadPool.getActiveCount() > 0) {
                    try { Thread.sleep(10); } catch (InterruptedException e) {}
                    continue;
                }
                if (emptyResults > 3) {
                    threadPool.shutdown();
                    threadPool.awaitTermination(60, TimeUnit.SECONDS);
                    break;
                } else {
                    emptyResults++;
                    try { Thread.sleep(FINISH_INTERVAL); } catch (InterruptedException e) {}
                }
            }
        }

        long end = System.currentTimeMillis();
        long sqlEnd = getServedSqlQueries();
        System.out.println();
        System.out.println("Total time: " + (end - start)/1000 + " seconds, downloaded pages: " + downloaded.get()
                           + ", SQL queries: " + (sqlEnd - sqlStart));
    }

    /**
     * Loads given page and returns all relative urls in the content area
     * @param url relative url
     * @return relative urls found in content area
     * @throws IOException problem
     */
    static List<String> loadPage(String url, boolean analyzePage) throws IOException {
        String page = readUrl(urlPrefix + url);
        if (! analyzePage)
            return Collections.emptyList();

        // clean html, parse XML, find links: 27,17,5,5 ms (best)
        // clean html, parse XML, find links: 795,262,352,181 ms (worst)
        // substring search: 0 - 1 ms (all)
        int div = page.indexOf("id=\"st\"");
        if (div == -1)
            return Collections.emptyList();

        List<String> childUrls = new ArrayList<String>();
        int position = div + 6;
        while ((position = page.indexOf("href=\"/", position)) != -1) {
            int quote = page.indexOf('"', position + 6);
            String href = page.substring(position + 6, quote);
            position = quote + 1;
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

    static long getServedSqlQueries() throws IOException {
        String content = readUrl(urlPrefix + "/Admin?action=performCheck");
        int i = content.indexOf("SQL: ");
        if (i == -1)
            return 0;
        int j = content.indexOf("<br>", i + 1);
        if (j == -1)
            return 0;
        return Long.parseLong(content.substring(i + 5, j));
    }

    static class Fetcher implements Runnable {
        Task task;
        public Fetcher(Task task) {
            this.task = task;
        }

        public void run() {
            try {
                List<String> found = loadPage(task.url, task.targetDepth > task.currentDepth);
                int current = downloaded.incrementAndGet();
                System.out.print("#");
                if (current % 60 == 0)
                    System.out.println();

                if (! found.isEmpty()) {
                    List<Task> newTasks = new ArrayList<Task>(found.size());
                    for (Iterator<String> iter = found.iterator(); iter.hasNext();) {
                        String url = iter.next();
                        newTasks.add(new Task(url, task.currentDepth + 1, task.targetDepth));
                    }
                    tasks.addAll(newTasks);
                }
            } catch (IOException e) {
                log.warn("Failed to fetch " + task.url, e);
            }
        }
    }

    static class Task {
        String url;
        int currentDepth, targetDepth;

        public Task(String url, int currentDepth, int targetDepth) {
            this.url = url;
            this.currentDepth = currentDepth;
            this.targetDepth = targetDepth;
        }
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
