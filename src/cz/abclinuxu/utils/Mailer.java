/*
 * User: literakl
 * Date: Feb 4, 2002
 * Time: 9:26:17 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.utils;

import cz.abclinuxu.persistance.*;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.servlets.utils.VelocityHelper;
import cz.abclinuxu.servlets.utils.Email;

import java.util.*;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

import org.apache.log4j.*;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * template for sending bulk sms
 */
public class Mailer {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Mailer.class);

    Persistance persistance = PersistanceFactory.getPersistance();

    public static void main(String[] args) throws Exception {
        boolean test = true;
        if ( args!=null && args.length==1 && args[0].equals("send") ) test = false;

        setupLog();
        Velocity.init("/home/literakl/tomcat/webapps/ROOT/WEB-INF/velocity.properties");

        Mailer mailer = new Mailer();
        mailer.doWork(test);
    }

    void doWork(boolean test) {
        int max = 1223;
        Map data = new HashMap(10);

        for ( int i=1; i<=max; i++ ) {
            addUsersEmail(i,data);
            if ( data.size()>9 ) {
                if ( test ) {
                    simulate(data);
                } else {
                    Email.sendBulkEmail("admin@AbcLinuxu.cz","Zprava pro uzivatele Linux Hardware",data);
                }
                data.clear();
            }
        }
        if ( test ) {
            simulate(data);
        } else {
            if ( data.size()>0 ) Email.sendBulkEmail("admin@AbcLinuxu.cz","Zprava pro uzivatele Linux Hardware",data);
        }
    }

    private void addUsersEmail(int id, Map map) {
        try {
            Category.getDefaultHierarchy().disableAll();
            User user = (User) persistance.findById(new User(id));
            Category.getDefaultHierarchy().enableAll();

            VelocityContext tmpContext = new VelocityContext();
            tmpContext.put("USER",user);
            String message = VelocityHelper.mergeTemplate("mail/first.vm",tmpContext);

            String email = user.getEmail();
            map.put(email,message);
        } catch (PersistanceException e) {
            Category.getDefaultHierarchy().enableAll();
        }
    }

    /**
     * Run Mailer with this method first to verify impact!
     */
    private void simulate(Map map) {
        for (Iterator it = map.keySet().iterator(); it.hasNext();) {
            String s = (String) it.next();
            String d = (String) map.get(s);
            log.info(s+"\n\n"+d);
        }
    }

    private static void setupLog() {
        try {
            FileOutputStream os = new FileOutputStream("/home/literakl/mail_result.txt",true);
            WriterAppender appender = new WriterAppender(new PatternLayout(),os);
            BasicConfigurator.configure(appender);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }
}
