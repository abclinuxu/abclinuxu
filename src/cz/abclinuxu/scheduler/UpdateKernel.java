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

import org.apache.regexp.*;
import org.apache.log4j.BasicConfigurator;

/**
 * This task is responsible for downloading
 * kernel versions from finger.kernel.org.
 */
public class UpdateKernel extends TimerTask {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UpdateKernel.class);

    static String fileName = "kernel.txt";
    static String server = "finger.kernel.org";

    RE reStable,reStablepre,reDevel,reDevelpre,reOld22,reOld22pre,reOld20,reOld20pre,reAc,reDj;

    public UpdateKernel() {
        try {
            reStable = new RE("(The latest stable[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)");
            reStablepre = new RE("(The latest prepatch for the stable[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)");
            reDevel = new RE("(The latest beta[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)");
            reDevelpre = new RE("(The latest prepatch for the beta[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)");
            reOld22 = new RE("(The latest 2.2[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)");
            reOld22pre = new RE("(The latest prepatch for the 2.2[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)");
            reOld20 = new RE("(The latest 2.0[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)");
            reOld20pre = new RE("(The latest prepatch for the 2.0[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)");
            reAc = new RE("(The latest -ac[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)");
            reDj = new RE("(The latest -dj[^:]*)(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)");
        } catch (RESyntaxException e) {
            log.error("Cannot compile regexp!",e);
        }
    }

    /**
     * Reads new kernel versions from finger.kernel.org using finger protocol
     * and write them to file.
     */
    public void run() {
        try {
            String line, stable, stablepre, devel, develpre, old22, old22pre, old20, old20pre, ac, dj;
            line = stable = stablepre = devel = develpre = old22 = old22pre = old20 = old20pre = ac = dj = null;

            BufferedReader reader = getStream();
            while ((line = reader.readLine())!=null) {
                if ( reStable.match(line) ) { stable = reStable.getParen(3); continue; }
                if ( reStablepre.match(line) ) { stablepre = reStablepre.getParen(3); continue; }
                if ( reDevel.match(line) ) { devel = reDevel.getParen(3); continue; }
                if ( reDevelpre.match(line) ) { develpre = reDevelpre.getParen(3); continue; }
                if ( reOld22.match(line) ) { old22 = reOld22.getParen(3); continue; }
                if ( reOld22pre.match(line) ) { old22pre = reOld22pre.getParen(3); continue; }
                if ( reOld20.match(line) ) { old20 = reOld20.getParen(3); continue; }
                if ( reOld20pre.match(line) ) { old20pre = reOld20pre.getParen(3); continue; }
                if ( reAc.match(line) ) { ac = reAc.getParen(3); continue; }
                if ( reDj.match(line) ) { dj = reDj.getParen(3); continue; }
            }

            FileWriter writer = new FileWriter(fileName);
            writer.write("<table border=0>\n");

            writer.write("<tr><td class=\"jadro_h\"><a href=\"ftp://ftp.fi.muni.cz/pub/linux/kernel/v2.4\" class=\"ikona\">Stabilní:</a></td>\n");
            writer.write("<td>"+stable);
            if ( stablepre!=null ) writer.write(" "+stablepre);
            writer.write("</td></tr>\n");

            writer.write("<tr><td class=\"jadro_h\"><a href=\"ftp://ftp.fi.muni.cz/pub/linux/kernel/v2.5\" class=\"ikona\">Vývojové:</a></td>\n");
            writer.write("<td>"+devel);
//            if ( develpre!=null ) writer.write(" "+develpre);
            writer.write("</td></tr>\n");

            writer.write("<tr><td class=\"jadro_h\"><a href=\"ftp://ftp.fi.muni.cz/pub/linux/kernel/v2.2\" class=\"ikona\">Øada 2.2:</a></td>\n");
            writer.write("<td>"+old22);
            if ( old22pre!=null ) writer.write(" "+old22pre);
            writer.write("</td></tr>\n");

            writer.write("<tr><td class=\"jadro_h\"><a href=\"ftp://ftp.fi.muni.cz/pub/linux/kernel/v2.0\" class=\"ikona\">Øada 2.0:</a></td>\n");
            writer.write("<td>"+old20);
            if ( old20pre!=null ) writer.write(" "+old20pre);
            writer.write("</td></tr>\n");

            writer.write("<tr><td class=\"jadro_h\"><a href=\"http://www.kernel.org/pub/linux/kernel/people/alan/linux-2.4/\" class=\"ikona\">AC øada:</a></td>\n");
            writer.write("<td>"+ac+"</td>");
            writer.write("</tr>\n");

            writer.write("<tr><td class=\"jadro_h\"><a href=\"http://www.kernel.org/pub/linux/kernel/people/davej/patches/2.5/\" class=\"ikona\">DJ øada:</a></td>\n");
            writer.write("<td>"+dj+"</td>");
            writer.write("</tr>\n");

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

    /**
     * Sets default file name, where new kernel versions will be stored.
     */
    public static void setFileName(String name) {
        fileName = name;
    }

    /**
     * @return File, where new kernel versions are stored.
     */
    public static String getFileName() {
        return fileName;
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
