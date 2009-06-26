/*
 *  Copyright (C) 2005 Leos Literak
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
package cz.abclinuxu.utils.email;

import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.config.impl.LoggingConfig;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.servlets.Constants;

import java.util.*;
import java.util.prefs.Preferences;
import java.io.*;

import org.apache.log4j.*;

/**
 * Sends templated email to selected users.
 */
public class Mailer implements Configurable {
    static Logger log = Logger.getLogger(Mailer.class);

    static {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(new Mailer());
    }

    public static void main(String[] args) throws Exception {
        showHelp();

        if (args.length < 4) {
            System.out.println("Invalid number of parameters (" + args.length + ")!");
            System.exit(1);
        }

        String inputFile = args[0];
        String template = args[1];
        String sender = args[2];
        String subject = args[3];

        boolean error = false;
        if ( Misc.empty(template) ) error = true;
        if ( Misc.empty(sender) ) error = true;
        if ( Misc.empty(subject) ) error = true;
        List users = readIntegersFromFile(inputFile);
        if (error) {
            System.out.println("Invalid parameters! They cannot be empty!\n");
            System.exit(1);
        } else {
            System.out.println("Parameters:");
            System.out.println("template = " + template);
            System.out.println("subject = " + subject);
            System.out.println("sender = " + sender);
            System.out.println("recepients = " + users.size());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("This utility can be used only by administrators.");
        System.out.println("Send emails only to users, who subscribed them!");
        System.out.println("\n\n");

        System.out.println("Press enter to continue");
        reader.readLine();
        System.out.println("OK, about to send emails. If you are not sure, quit application now.");
        int random = new Random().nextInt(9999);
        System.out.println("To prevent random usage, enter following number: "+random);
        String tmp = reader.readLine();
        int answear = Misc.parseInt(tmp,Integer.MAX_VALUE);
        if ( random!=answear ) {
            System.out.println("Your answear differs, quitting now.");
            System.exit(1);
        }

        Map map = new HashMap(10);
        map.put(EmailSender.KEY_FROM,sender);
        map.put(EmailSender.KEY_SUBJECT,subject);
        map.put(EmailSender.KEY_TEMPLATE,template);
        map.put(EmailSender.KEY_STATS_KEY, Constants.EMAIL_SCRIPT);

        LoggingConfig.initialize();
        log.info("EmailSender called by "+System.getProperty("user.name")+", sender="+sender+
                 ", subject="+subject+", template="+template+", "+users.size()+" recepients.");

//        setupLog(); //redirect log to file in user's home directory

        System.out.println("\n\nLet's rock n' roll!");
        EmailSender.sendEmailToUsers(map,users);
        System.out.println("Finished.\n");
    }

    /**
     * Shows instructions, how to use this application.
     */
    private static void showHelp() {
        System.out.println("\n\n");
        System.out.println("Mailer is utility to send templated email to given list of users.\n");
        System.out.println("Usage: mailer.sh input template from subject");
        System.out.println("where");
        System.out.println("\t input \t\t input file with users, one number per line");
        System.out.println("\t template \t template with text, relative path to freemarker root");
        System.out.println("\t from \t\t email address of sender of the email");
        System.out.println("\t subject \t subject of the email");
        System.out.println("Example");
        System.out.println("mailer.sh ~/users.txt /mail/zpravodaj.ftl admin@abclinuxu.cz \"Zpravodaj 8/03\"");
        System.out.println("\n\n");
    }

    /**
     * Converts text file with single number on line to list of Integers.
     * Empty lines, texts, non-natural numbers are skipped.
     * @param file
     * @return
     * @throws Exception
     */
    private static List readIntegersFromFile(String file) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        List list = new ArrayList(500);
        String line = reader.readLine();
        int i = -1;
        while (line!=null) {
            i = Misc.parseInt(line,-1);
            if (i>0)
                list.add(new Integer(i));
            line = reader.readLine();
        }
        return list;
    }

    /**
     * Sets up logging.
     */
    private static void setupLog() {
        try {
            String home = System.getProperty("user.home");
            FileOutputStream os = new FileOutputStream(home+File.separatorChar+"mail_result.txt",true);
            WriterAppender appender = new WriterAppender(new PatternLayout(),os);
            BasicConfigurator.configure(appender);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * To force configuration of log4j
     * @param prefs
     * @throws ConfigurationException
     */
    public void configure(Preferences prefs) throws ConfigurationException {
    }
}
