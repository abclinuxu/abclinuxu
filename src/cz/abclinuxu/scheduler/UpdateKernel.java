/*
 * User: literakl
 * Date: Feb 12, 2002
 * Time: 8:32:33 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.scheduler;

import java.io.*;
import java.net.Socket;
import java.util.TimerTask;
import java.util.prefs.Preferences;

import org.apache.regexp.*;
import org.apache.log4j.BasicConfigurator;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.impl.AbcConfig;

/**
 * This task is responsible for downloading
 * kernel versions from finger.kernel.org.
 */
public class UpdateKernel extends TimerTask implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UpdateKernel.class);

    public static final String PREF_FILE = "file";
    public static final String DEFAULT_FILE = "kernel.txt";
    public static final String PREF_SERVER = "server";
    public static final String DEFAULT_SERVER = "finger.kernel.org";
    public static final String PREF_REGEXP_STABLE = "regexp.stable";
    public static final String DEFAULT_REGEXP_STABLE = "(The latest stable[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)";
    public static final String PREF_REGEXP_STABLE_PRE = "regexp.stable.pre";
    public static final String DEFAULT_REGEXP_STABLE_PRE = "(The latest prepatch for the stable[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)";
    public static final String PREF_REGEXP_DEVEL = "regexp.devel";
    public static final String DEFAULT_REGEXP_DEVEL = "(The latest beta[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)";
    public static final String PREF_REGEXP_DEVEL_PRE = "regexp.devel.pre";
    public static final String DEFAULT_REGEXP_DEVEL_PRE = "(The latest prepatch for the beta[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)";
    public static final String PREF_REGEXP_22 = "regexp.22";
    public static final String DEFAULT_REGEXP_22 = "(The latest 2.2[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)";
    public static final String PREF_REGEXP_22_PRE = "regexp.22.pre";
    public static final String DEFAULT_REGEXP_22_PRE = "(The latest prepatch for the 2.2[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)";
    public static final String PREF_REGEXP_20 = "regexp.20";
    public static final String DEFAULT_REGEXP_20 = "(The latest 2.0[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)";
    public static final String PREF_REGEXP_20_PRE = "regexp.20.pre";
    public static final String DEFAULT_REGEXP_20_PRE = "(The latest prepatch for the 2.0[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)";
    public static final String PREF_REGEXP_AC = "regexp.ac";
    public static final String DEFAULT_REGEXP_AC = "(The latest -ac[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)";
    public static final String PREF_REGEXP_DJ = "regexp.dj";
    public static final String DEFAULT_REGEXP_DJ = "(The latest -dj[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)";

    String fileName, server;
    String stable, stablePre, devel, develPre, old22, old22Pre, old20, old20Pre, ac, dj;

    RE reStable,reStablepre,reDevel,reDevelpre,reOld22,reOld22pre,reOld20,reOld20pre,reAc,reDj;

    public UpdateKernel() {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(this);
        try {
            reStable = new RE(stable);
            reStablepre = new RE(stablePre);
            reDevel = new RE(devel);
            reDevelpre = new RE(develPre);
            reOld22 = new RE(old22);
            reOld22pre = new RE(old22Pre);
            reOld20 = new RE(old20);
            reOld20pre = new RE(old20Pre);
            reAc = new RE(ac);
            reDj = new RE(dj);
        } catch (RESyntaxException e) {
            log.error("Cannot compile regexp!",e);
        }
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        server = prefs.get(PREF_SERVER,DEFAULT_SERVER);
        stable = prefs.get(PREF_REGEXP_STABLE,DEFAULT_REGEXP_STABLE);
        stablePre = prefs.get(PREF_REGEXP_STABLE_PRE,DEFAULT_REGEXP_STABLE_PRE);
        devel = prefs.get(PREF_REGEXP_DEVEL,DEFAULT_REGEXP_DEVEL);
        develPre = prefs.get(PREF_REGEXP_DEVEL_PRE,DEFAULT_REGEXP_DEVEL_PRE);
        old22 = prefs.get(PREF_REGEXP_22,DEFAULT_REGEXP_22);
        old22Pre = prefs.get(PREF_REGEXP_22_PRE,DEFAULT_REGEXP_22_PRE);
        old20 = prefs.get(PREF_REGEXP_20,DEFAULT_REGEXP_20);
        old20Pre = prefs.get(PREF_REGEXP_20_PRE,DEFAULT_REGEXP_20_PRE);
        ac = prefs.get(PREF_REGEXP_AC,DEFAULT_REGEXP_AC);
        dj = prefs.get(PREF_REGEXP_DJ,DEFAULT_REGEXP_DJ);
    }

    /**
     * Reads new kernel versions from finger.kernel.org using finger protocol
     * and write them to file.
     */
    public void run() {
        try {
            String line, stable, stablepre, devel, develpre, old22, old22pre, old20, old20pre, ac, dj;
            line = stable = stablepre = devel = develpre = old22 = old22pre = old20 = old20pre = ac = dj = null;

            boolean readSuccess = false;
            BufferedReader reader = getStream();
            while ((line = reader.readLine())!=null) {
                if ( reStable.match(line) ) {
                    stable = reStable.getParen(3);
                    readSuccess = true;
                    continue;
                }
                if ( reStablepre.match(line) ) {
                    stablepre = reStablepre.getParen(3);
                    readSuccess = true;
                    continue;
                }
                if ( reDevel.match(line) ) {
                    devel = reDevel.getParen(3);
                    readSuccess = true;
                    continue;
                }
                if ( reDevelpre.match(line) ) {
                    develpre = reDevelpre.getParen(3);
                    readSuccess = true;
                    continue;
                }
                if ( reOld22.match(line) ) {
                    old22 = reOld22.getParen(3);
                    readSuccess = true;
                    continue;
                }
                if ( reOld22pre.match(line) ) {
                    old22pre = reOld22pre.getParen(3);
                    readSuccess = true;
                    continue;
                }
                if ( reOld20.match(line) ) {
                    old20 = reOld20.getParen(3);
                    readSuccess = true;
                    continue;
                }
                if ( reOld20pre.match(line) ) {
                    old20pre = reOld20pre.getParen(3);
                    readSuccess = true;
                    continue;
                }
                if ( reAc.match(line) ) {
                    ac = reAc.getParen(3);
                    readSuccess = true;
                    continue;
                }
                if ( reDj.match(line) ) {
                    dj = reDj.getParen(3);
                    readSuccess = true;
                    continue;
                }
            }

            if ( !readSuccess ) // sometimes finger returns empty file
                return;

            String file = AbcConfig.calculateDeployedPath(fileName);
            FileWriter writer = new FileWriter(file);
            writer.write("<table border=0>\n");

            writer.write("<tr><td class=\"jadro_h\"><a href=\"ftp://ftp.fi.muni.cz/pub/linux/kernel/v2.4\" class=\"ikona\">Stabilní:</a></td>\n");
            writer.write("<td>");
            if ( stable!=null ) writer.write(stable);
            if ( stablepre!=null ) writer.write(" "+stablepre);
            writer.write("</td></tr>\n");

            writer.write("<tr><td class=\"jadro_h\"><a href=\"ftp://ftp.fi.muni.cz/pub/linux/kernel/v2.5\" class=\"ikona\">Vývojové:</a></td>\n");
            writer.write("<td>");
            if ( devel!=null ) writer.write(devel);
//            if ( develpre!=null ) writer.write(" "+develpre);
            writer.write("</td></tr>\n");

            writer.write("<tr><td class=\"jadro_h\"><a href=\"ftp://ftp.fi.muni.cz/pub/linux/kernel/v2.2\" class=\"ikona\">Øada 2.2:</a></td>\n");
            writer.write("<td>");
            if ( old22!=null ) writer.write(old22);
            if ( old22pre!=null ) writer.write(" "+old22pre);
            writer.write("</td></tr>\n");

            writer.write("<tr><td class=\"jadro_h\"><a href=\"ftp://ftp.fi.muni.cz/pub/linux/kernel/v2.0\" class=\"ikona\">Øada 2.0:</a></td>\n");
            writer.write("<td>");
            if ( old20!=null ) writer.write(old20);
            if ( old20pre!=null ) writer.write(" "+old20pre);
            writer.write("</td></tr>\n");

            writer.write("<tr><td class=\"jadro_h\"><a href=\"http://www.kernel.org/pub/linux/kernel/people/alan/linux-2.4/\" class=\"ikona\">AC øada:</a></td>\n");
            writer.write("<td>");
            if ( ac!=null ) writer.write(ac);
            writer.write("</td></tr>\n");

            writer.write("<tr><td class=\"jadro_h\"><a href=\"http://www.kernel.org/pub/linux/kernel/people/davej/patches/2.5/\" class=\"ikona\">DJ øada:</a></td>\n");
            writer.write("<td>");
            if ( dj!=null ) writer.write(dj);
            writer.write("</td></tr>\n");

            writer.write("</table>");
            reader.close();
            writer.close();
        } catch (Exception e) {
            log.error("Cannot parse kernel headers!",e);
        }
    }

    /**
     * @return Task name, for logging purposes.
     */
    public String getJobName() {
        return "UpdateKernel";
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        UpdateKernel updateKernel = new UpdateKernel();
        updateKernel.run();
    }

    /**
     * get stream with kernel headers
     */
    private BufferedReader getStream() throws IOException {
//        BufferedReader reader = new BufferedReader(new FileReader("/home/literakl/penguin/obsahy/kernel.txt"));
//        BufferedReader reader = new BufferedReader(new FileReader("/home/literakl/finger.txt"));

        Socket socket = new Socket(server,79);
        socket.setSoTimeout(500);
        socket.getOutputStream().write("\015\012".getBytes());
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return reader;
    }
}
