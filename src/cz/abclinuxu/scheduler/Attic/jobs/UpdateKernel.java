/*
 * User: literakl
 * Date: Feb 12, 2002
 * Time: 8:32:33 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.scheduler.jobs;

import cz.abclinuxu.scheduler.Task;

import java.io.*;
import java.net.Socket;

import org.apache.regexp.*;
import org.apache.log4j.BasicConfigurator;

/**
 * This task is responsible for downloading
 * kernel versions from finger.kernel.org.
 */
public class UpdateKernel implements Task {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(UpdateKernel.class);

    static String fileName = "kernel.txt";
    static String server = "finger.kernel.org";

    RE version;

    public UpdateKernel() {
        try {
            version = new RE("(:[ ]+)([:digit:].[:digit:].[a-z0-9-]+)");
        } catch (RESyntaxException e) {
            log.error("Cannot compile regexp!",e);
        }
    }

    /**
     * Reads new kernel versions from finger.kernel.org using finger protocol
     * and write them to file.
     */
    public void runJob() {
        try {
            BufferedReader reader = getStream();
            String line = reader.readLine();
            FileWriter writer = new FileWriter(fileName);

            writer.write("<table border=0>\n");
            if ( version.match(line) ) {
                String tmp = version.getParen(2);
                writer.write("<tr><td class=\"jadro_h\">Stabilní:</td>");
                writer.write("<td><a href=\"ftp://ftp.fi.muni.cz/pub/linux/kernel/v2.4/linux-"+tmp);
                writer.write(".tar.bz2\" class=\"ikona\">"+tmp+"</a></td>");
            }
            if ( version.match(reader.readLine()) ) writer.write("<td class=\"jadro_p\">("+version.getParen(2)+")</td>");
            writer.write("</tr>\n");

            if ( version.match(reader.readLine()) ) {
                String tmp = version.getParen(2);
                writer.write("<tr><td class=\"jadro_h\" colspan=\"2\">Vývojové:</td>");
                writer.write("<td><a href=\"ftp://ftp.fi.muni.cz/pub/linux/kernel/v2.5/linux-"+tmp);
                writer.write(".tar.bz2\" class=\"ikona\">"+tmp+"</a></td>");
            }
            reader.readLine();
            writer.write("</tr>\n");

            if ( version.match(reader.readLine()) ) {
                String tmp = version.getParen(2);
                writer.write("<tr><td class=\"jadro_h\">Øada 2.2:</td>");
                writer.write("<td><a href=\"ftp://ftp.fi.muni.cz/pub/linux/kernel/v2.2/linux-"+tmp);
                writer.write(".tar.bz2\" class=\"ikona\">"+tmp+"</a></td>");
            }
            if ( version.match(reader.readLine()) ) writer.write("<td class=\"jadro_p\">("+version.getParen(2)+")</td>");
            writer.write("</tr>\n");

            if ( version.match(reader.readLine()) ) {
                String tmp = version.getParen(2);
                writer.write("<tr><td class=\"jadro_h\">Øada 2.0:</td>");
                writer.write("<td><a href=\"ftp://ftp.fi.muni.cz/pub/linux/kernel/v2.0/linux-"+tmp);
                writer.write(".tar.bz2\" class=\"ikona\">"+tmp+"</a></td>");
            }
            if ( version.match(reader.readLine()) ) writer.write("<td class=\"jadro_p\">("+version.getParen(2)+")</td>");
            writer.write("</tr>\n</table>");

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
        updateKernel.runJob();
    }

    /**
     * get stream with kernel headers
     */
    private BufferedReader getStream() throws IOException {
//        BufferedReader reader = new BufferedReader(new FileReader("/home/literakl/penguin/obsahy/kernel.txt"));

        Socket socket = new Socket(server,79);
        socket.setSoTimeout(500);
        socket.getOutputStream().write("\015\012".getBytes());
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return reader;
    }
}
