/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.abclinuxu.scheduler;

import java.util.TimerTask;
import org.apache.log4j.Logger;

/**
 *
 * @author lubos
 */
public class UpdateTopStatistics extends TimerTask {
    static Logger log = Logger.getLogger(UpdateTopStatistics.class);
    
    public void run() {
        VariableFetcher.getInstance().refreshTopStatistics();
    }
}
