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
    public static final String PREF_URL_STABLE = "url.stable";
    public static final String DEFAULT_REGEXP_STABLE = "(The latest stable[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)";
    public static final String PREF_REGEXP_STABLE_PRE = "regexp.stable.pre";
    public static final String DEFAULT_REGEXP_STABLE_PRE = "(The latest prepatch for the stable[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)";
    public static final String PREF_REGEXP_DEVEL = "regexp.devel";
    public static final String PREF_URL_DEVEL = "url.devel";
    public static final String DEFAULT_REGEXP_DEVEL = "(The latest beta[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)";
    public static final String PREF_REGEXP_DEVEL_PRE = "regexp.devel.pre";
    public static final String DEFAULT_REGEXP_DEVEL_PRE = "(The latest prepatch for the beta[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)";
    public static final String PREF_REGEXP_22 = "regexp.22";
    public static final String PREF_URL_22 = "url.22";
    public static final String DEFAULT_REGEXP_22 = "(The latest 2.2[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)";
    public static final String PREF_REGEXP_22_PRE = "regexp.22.pre";
    public static final String DEFAULT_REGEXP_22_PRE = "(The latest prepatch for the 2.2[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)";
    public static final String PREF_REGEXP_20 = "regexp.20";
    public static final String PREF_URL_20 = "url.20";
    public static final String DEFAULT_REGEXP_20 = "(The latest 2.0[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)";
    public static final String PREF_REGEXP_20_PRE = "regexp.20.pre";
    public static final String DEFAULT_REGEXP_20_PRE = "(The latest prepatch for the 2.0[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)";
    public static final String PREF_REGEXP_AC = "regexp.ac";
    public static final String PREF_URL_AC = "url.ac";
    public static final String DEFAULT_REGEXP_AC = "(The latest -ac[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)";
    public static final String PREF_REGEXP_DJ = "regexp.dj";
    public static final String PREF_URL_DJ = "url.dj";
    public static final String DEFAULT_REGEXP_DJ = "(The latest -dj[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)";

    String fileName, server;
    String stable, stablePre, devel, develPre, old22, old22Pre, old20, old20Pre, ac, dj;
    String urlStable, urlDevel, url22, url20, urlAC, urlDJ;

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
        fileName = prefs.get(PREF_FILE,DEFAULT_FILE);
        stable = prefs.get(PREF_REGEXP_STABLE,DEFAULT_REGEXP_STABLE);
        urlStable = prefs.get(PREF_URL_STABLE,null);
        stablePre = prefs.get(PREF_REGEXP_STABLE_PRE,DEFAULT_REGEXP_STABLE_PRE);
        devel = prefs.get(PREF_REGEXP_DEVEL,DEFAULT_REGEXP_DEVEL);
        urlDevel = prefs.get(PREF_URL_DEVEL,null);
        develPre = prefs.get(PREF_REGEXP_DEVEL_PRE,DEFAULT_REGEXP_DEVEL_PRE);
        old22 = prefs.get(PREF_REGEXP_22,DEFAULT_REGEXP_22);
        url22 = prefs.get(PREF_URL_22,null);
        old22Pre = prefs.get(PREF_REGEXP_22_PRE,DEFAULT_REGEXP_22_PRE);
        old20 = prefs.get(PREF_REGEXP_20,DEFAULT_REGEXP_20);
        url20 = prefs.get(PREF_URL_20,null);
        old20Pre = prefs.get(PREF_REGEXP_20_PRE,DEFAULT_REGEXP_20_PRE);
        ac = prefs.get(PREF_REGEXP_AC,DEFAULT_REGEXP_AC);
        urlAC = prefs.get(PREF_URL_AC,null);
        dj = prefs.get(PREF_REGEXP_DJ,DEFAULT_REGEXP_DJ);
        urlDJ = prefs.get(PREF_URL_DJ,null);
    }

    /**
     * Reads new kernel versions from finger.kernel.org using finger protocol
     * and write them to file.
     */
    public void run() {
        try {
            String line;
            line = stable = stablePre = devel = develPre = old22 = old22Pre = old20 = old20Pre = ac = dj = null;

            BufferedReader reader = getStream();
            while ((line = reader.readLine())!=null) {
                if ( reStable.match(line) ) {
                    stable = reStable.getParen(3);
                    continue;
                }
                if ( reStablepre.match(line) ) {
                    stablePre = reStablepre.getParen(3);
                    continue;
                }
                if ( reDevel.match(line) ) {
                    devel = reDevel.getParen(3);
                    continue;
                }
                if ( reDevelpre.match(line) ) {
                    develPre = reDevelpre.getParen(3);
                    continue;
                }
                if ( reOld22.match(line) ) {
                    old22 = reOld22.getParen(3);
                    continue;
                }
                if ( reOld22pre.match(line) ) {
                    old22Pre = reOld22pre.getParen(3);
                    continue;
                }
                if ( reOld20.match(line) ) {
                    old20 = reOld20.getParen(3);
                    continue;
                }
                if ( reOld20pre.match(line) ) {
                    old20Pre = reOld20pre.getParen(3);
                    continue;
                }
                if ( reAc.match(line) ) {
                    ac = reAc.getParen(3);
                    continue;
                }
                if ( reDj.match(line) ) {
                    dj = reDj.getParen(3);
                    continue;
                }
            }

            if ( stable==null ) // sometimes finger returns an empty file
                return;

            String file = AbcConfig.calculateDeployedPath(fileName);
            FileWriter writer = new FileWriter(file);
            writer.write("<table border=0>\n");

            writeTableRow(writer,"Stabilní:",urlStable,stable,stablePre);
            writeTableRow(writer,"Vývojové:",urlDevel,devel,null);
            writeTableRow(writer,"Øada 2.2:",url22,old22,old22Pre);
            writeTableRow(writer,"Øada 2.0:",url20,old20,old20Pre);
            writeTableRow(writer,"AC øada:",urlAC,ac,null);
            writeTableRow(writer,"DJ øada:",urlDJ,dj,null);

            writer.write("</table>");
            reader.close();
            writer.close();
        } catch (Exception e) {
            log.error("Cannot parse kernel headers!",e);
        }
    }

    private void writeTableRow(Writer writer, String desc, String url, String version, String preVersion) throws IOException {
        writer.write("<tr>\n<td class=\"jadro_h\">");
        if ( url!=null )
            writer.write("<a href=\""+url+"\" class=\"ikona\">"+desc+"</a>");
        else
            writer.write(desc);
        writer.write("</td>\n<td>");
        if ( version!=null )
            writer.write(version);
        if ( preVersion!=null )
            writer.write(" "+preVersion);
        writer.write(" </td>\n</tr>\n");
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
