/*
 * User: literakl
 * Date: Feb 12, 2002
 * Time: 8:32:33 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.scheduler;

import java.io.*;
import java.net.URL;
import java.util.TimerTask;
import java.util.prefs.Preferences;

import org.apache.regexp.*;
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
    public static final String PREF_KERNEL_DIST_URI = "uri";
    public static final String PREF_REGEXP_STABLE = "regexp.stable";
    public static final String PREF_URL_STABLE = "url.stable";
    public static final String PREF_REGEXP_STABLE_PRE = "regexp.stable.pre";
    public static final String PREF_REGEXP_STABLE_MM = "regexp.stable.mm";
    public static final String PREF_URL_STABLE_MM = "url.stable.mm";
    public static final String PREF_REGEXP_DEVEL = "regexp.devel";
    public static final String PREF_URL_DEVEL = "url.devel";
    public static final String PREF_REGEXP_DEVEL_PRE = "regexp.devel.pre";
    public static final String PREF_REGEXP_24 = "regexp.24";
    public static final String PREF_URL_24 = "url.24";
    public static final String PREF_REGEXP_24_PRE = "regexp.24.pre";
    public static final String PREF_REGEXP_22 = "regexp.22";
    public static final String PREF_URL_22 = "url.22";
    public static final String PREF_REGEXP_22_PRE = "regexp.22.pre";
    public static final String PREF_REGEXP_20 = "regexp.20";
    public static final String PREF_URL_20 = "url.20";
    public static final String PREF_REGEXP_20_PRE = "regexp.20.pre";
    public static final String PREF_REGEXP_AC = "regexp.ac";
    public static final String PREF_URL_AC = "url.ac";

    String fileName, uri;
    String stable, stablePre, stableMM, devel, develPre, old24, old24Pre, old22, old22Pre, old20, old20Pre, ac;
    String urlStable, urlDevel, url24, url22, url20, urlAC, urlStableMM;

    RE reStable,reStablepre,reDevel,reDevelpre,reStableMM;
    RE reOld24,reOld24pre,reOld22,reOld22pre,reOld20,reOld20pre,reAc;

    public UpdateKernel() {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(this);
        try {
            reStable = new RE(stable);
            reStablepre = new RE(stablePre);
            reStableMM = new RE(stableMM);
            reDevel = new RE(devel);
            reDevelpre = new RE(develPre);
            reOld24 = new RE(old24);
            reOld24pre = new RE(old24Pre);
            reOld22 = new RE(old22);
            reOld22pre = new RE(old22Pre);
            reOld20 = new RE(old20);
            reOld20pre = new RE(old20Pre);
            reAc = new RE(ac);
        } catch (RESyntaxException e) {
            log.error("Cannot compile regexp!",e);
        }
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        uri = prefs.get(PREF_KERNEL_DIST_URI, null);
        fileName = prefs.get(PREF_FILE, null);
        stable = prefs.get(PREF_REGEXP_STABLE, null);
        urlStable = prefs.get(PREF_URL_STABLE,null);
        stablePre = prefs.get(PREF_REGEXP_STABLE_PRE, null);
        stableMM = prefs.get(PREF_REGEXP_STABLE_MM, null);
        urlStableMM = prefs.get(PREF_URL_STABLE_MM, null);
        devel = prefs.get(PREF_REGEXP_DEVEL, null);
        urlDevel = prefs.get(PREF_URL_DEVEL,null);
        develPre = prefs.get(PREF_REGEXP_DEVEL_PRE, null);
        old24 = prefs.get(PREF_REGEXP_24, null);
        url24 = prefs.get(PREF_URL_24,null);
        old24Pre = prefs.get(PREF_REGEXP_24_PRE, null);
        old22 = prefs.get(PREF_REGEXP_22, null);
        url22 = prefs.get(PREF_URL_22,null);
        old22Pre = prefs.get(PREF_REGEXP_22_PRE, null);
        old20 = prefs.get(PREF_REGEXP_20, null);
        url20 = prefs.get(PREF_URL_20,null);
        old20Pre = prefs.get(PREF_REGEXP_20_PRE, null);
        ac = prefs.get(PREF_REGEXP_AC, null);
        urlAC = prefs.get(PREF_URL_AC,null);
    }

    /**
     * Reads new kernel versions from finger.kernel.org using finger protocol
     * and write them to file.
     */
    public void run() {
        try {
            String line;
            line = stable = stablePre = devel = develPre = null;
            old24 = old24Pre = old22 = old22Pre = old20 = old20Pre = ac = null;

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
                if ( reOld24.match(line) ) {
                    old24 = reOld24.getParen(3);
                    continue;
                }
                if ( reOld24pre.match(line) ) {
                    old24Pre = reOld24pre.getParen(3);
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
                if ( reStableMM.match(line) ) {
                    stableMM = reStableMM.getParen(3);
                    continue;
                }
            }

            if ( stable==null ) // sometimes finger returns an empty file
                return;

            String file = AbcConfig.calculateDeployedPath(fileName);
            FileWriter writer = new FileWriter(file);
            writer.write("<table border=0>\n");

            writeTableRow(writer,"Stabilní:",urlStable,stable,stablePre);
            writeTableRow(writer,"Vývojová:",urlDevel,devel,null);
            writeTableRow(writer,"MM øada:", urlStableMM, stableMM, null);
            writeTableRow(writer,"Øada 2.4:",url24,old24,old24Pre);
            writeTableRow(writer,"Øada 2.2:",url22,old22,old22Pre);
            writeTableRow(writer,"Øada 2.0:",url20,old20,old20Pre);
            writeTableRow(writer,"AC øada:",urlAC,ac,null);

            writer.write("</table>");
            reader.close();
            writer.close();
        } catch (IOException e) {
            log.error("Cannot parse current kernel information because of I/O problems! "+e.getMessage());
        } catch (Exception e) {
            log.error("Cannot parse kernel headers!",e);
        }
    }

    private void writeTableRow(Writer writer, String desc, String url, String version, String preVersion) throws IOException {
        if (version==null && preVersion==null) return;
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
        UpdateKernel updateKernel = new UpdateKernel();
        updateKernel.run();
    }

    /**
     * get stream with kernel headers
     */
    private BufferedReader getStream() throws IOException {
        URL url = new URL(uri);
        Reader reader = new InputStreamReader(url.openStream());
        BufferedReader bufferedReader = new BufferedReader(reader);
        return bufferedReader;
    }
}
