/*
 * User: literakl
 * Date: Feb 18, 2002
 * Time: 9:03:52 AM
 */
package cz.abclinuxu.scheduler;

import cz.abclinuxu.utils.feeds.FeedGenerator;

import java.util.TimerTask;

public class GenerateLinks extends TimerTask {

    public void run() {
        FeedGenerator.updateArticles();
    }
}
