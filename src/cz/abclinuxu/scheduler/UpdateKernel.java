/*
 * User: literakl
 * Date: Feb 12, 2002
 * Time: 8:32:33 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.scheduler;

import java.io.*;
import java.net.URL;
import java.util.prefs.Preferences;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.TimerTask;

import org.apache.regexp.*;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.FMUtils;

/**
 * This task is responsible for downloading
 * kernel versions from finger.kernel.org.
 */
public class UpdateKernel extends TimerTask implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UpdateKernel.class);

    static final String PREF_FILE = "file";
    static final String PREF_KERNEL_DIST_URI = "uri";
    static final String PREF_REGEXP_STABLE = "regexp.stable";
    static final String PREF_URL_STABLE = "url.stable";
    static final String PREF_REGEXP_STABLE_PRE = "regexp.stable.pre";
    static final String PREF_REGEXP_STABLE_MM = "regexp.stable.mm";
    static final String PREF_URL_STABLE_MM = "url.stable.mm";
    static final String PREF_REGEXP_DEVEL = "regexp.devel";
    static final String PREF_URL_DEVEL = "url.devel";
    static final String PREF_REGEXP_DEVEL_PRE = "regexp.devel.pre";
    static final String PREF_REGEXP_24 = "regexp.24";
    static final String PREF_URL_24 = "url.24";
    static final String PREF_REGEXP_24_PRE = "regexp.24.pre";
    static final String PREF_REGEXP_22 = "regexp.22";
    static final String PREF_URL_22 = "url.22";
    static final String PREF_REGEXP_22_PRE = "regexp.22.pre";
    static final String PREF_REGEXP_20 = "regexp.20";
    static final String PREF_URL_20 = "url.20";
    static final String PREF_REGEXP_20_PRE = "regexp.20.pre";
    static final String PREF_REGEXP_AC = "regexp.ac";
    static final String PREF_URL_AC = "url.ac";

    static UpdateKernel instance;
    static {
        instance = new UpdateKernel();
    }


    String fileName, uri;
    String stable, stablePre, stableMM, devel, develPre, old24, old24Pre, old22, old22Pre, old20, old20Pre, ac;
    String urlStable, urlDevel, url24, url22, url20, urlAC, urlStableMM;

    RE reStable,reStablepre,reDevel,reDevelpre,reStableMM;
    RE reOld24,reOld24pre,reOld22,reOld22pre,reOld20,reOld20pre,reAc;


    private UpdateKernel() {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureAndRememberMe(this);
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
     * @return singleton of this class
     */
    public static UpdateKernel getInstance() {
        return instance;
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

            Map kernels = new LinkedHashMap();
            if (urlStable!=null)
                kernels.put("v26", new KernelTree(urlStable, stable, stablePre));
            if ( urlDevel!=null )
                kernels.put("v27", new KernelTree(urlDevel,devel));
            if ( urlStableMM!=null )
                kernels.put("v26mm", new KernelTree(urlStableMM, stableMM));
            if ( url24!=null )
                kernels.put("v24", new KernelTree(url24,old24,old24Pre));
            if ( url22!=null )
                kernels.put("v22", new KernelTree(url22,old22,old22Pre));
            if ( url20!=null )
                kernels.put("v20", new KernelTree(url20,old20,old20Pre));
            if ( urlAC!=null )
                kernels.put("v26ac", new KernelTree(urlAC,ac));

            String file = AbcConfig.calculateDeployedPath(fileName);
            FMUtils.executeTemplate("/include/misc/generate_kernel.ftl", kernels, new File(file));
        } catch (IOException e) {
            log.error("Cannot parse current kernel information because of I/O problems! "+e.getMessage());
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

    /**
     * Data holder about kernel.
     */
    public static class KernelTree {
        String url, release, preRelease;

        public KernelTree(String url, String release) {
            this.url = url;
            this.release = release;
        }

        public KernelTree(String url, String release, String pre_prelease) {
            this.url = url;
            this.release = release;
            this.preRelease = pre_prelease;
        }

        public String getUrl() {
            return url;
        }

        public String getRelease() {
            return release;
        }

        public String getPreRelease() {
            return preRelease;
        }

        public String toString() {
            return release+","+preRelease+","+url;
        }
    }
}
