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

            if ( version.match(line) ) writer.write("Stabilní øada: "+version.getParen(2));
            line = reader.readLine();
            if ( version.match(line) ) writer.write(" ("+version.getParen(2)+")<br>\n");
            line = reader.readLine();
            if ( version.match(line) ) writer.write("Vývojová øada: "+version.getParen(2));
            line = reader.readLine();
            if ( version.match(line) ) writer.write(" ("+version.getParen(2)+")<br>\n");
            line = reader.readLine();
            if ( version.match(line) ) writer.write("Produkèní øada: "+version.getParen(2)+"\n");

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
    public static void setFile(String name) {
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
