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
import org.apache.regexp.RE;
import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Sends sms to all users, that allowed sending adds emails.
 */
public class Spammer {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Spammer.class);
    static RE re;

    Persistance persistance = PersistanceFactory.getPersistance();

    public static void main(String[] args) throws Exception {
        boolean test = true;
        if ( args!=null && args.length==1 && args[0].equals("send") ) test = false;

        re = new RE("^[A-Z]*$");
        setupLog();
        Velocity.init("/home/literakl/abc/deploy/WEB-INF/velocity.properties");

        Spammer mailer = new Spammer();
        mailer.doWork(test);
    }

    void doWork(boolean test) {
        int max = getLastUserId();
        Map data = new HashMap(10);

        if ( test ) {
            System.out.println("Running test");
        } else {
            System.out.println("Running REAL mode - will send mass mail!");
        }

        for ( int i=1; i<=max; i++ ) {
            addUsersEmail(i,data);
            if ( data.size()>9 ) {
                if ( test ) {
                    simulate(data);
                } else {
                    Email.sendBulkEmail("reklama@abclinuxu.cz","nabidka sluzeb",data);
                }
                data.clear();
            }
        }
        if ( test ) {
            simulate(data);
        } else {
            if ( data.size()>0 ) Email.sendBulkEmail("reklama@abclinuxu.cz","nabidka sluzeb",data);
        }
    }

    private void addUsersEmail(int id, Map map) {
        try {
            LogManager.getRootLogger().setLevel(Level.OFF);
            User user = (User) persistance.findById(new User(id));
            LogManager.getRootLogger().setLevel(Level.ALL);

            Document document = user.getData();
            String str = document.selectSingleNode("data/ads").getText();
            if ( ! Misc.same(str,"yes") ) {
                System.out.println("not including user "+user.getId()+", forbidden ads: "+str);
                return;
            }

            Node node = document.selectSingleNode("data/active");
            if ( node!=null ) {
                str = node.getText();
                if ( Misc.same(str,"no") ) {
                    System.out.println("not including user "+user.getId()+", not active");
                    return;
                }
            }

            str = user.getPassword();
            if ( str.length()==6 && re.match(str) ) {
                System.out.println("not including user "+user.getId()+", password is "+str);
                return;
            }

            VelocityContext tmpContext = new VelocityContext();
            tmpContext.put("USER",user);
            String message = VelocityHelper.mergeTemplate("mail/sro.vm",tmpContext);

            String email = user.getEmail();
            map.put(email,message);
        } catch (PersistanceException e) {
            LogManager.getRootLogger().setLevel(Level.ALL);
        }
    }

    /**
     * Run Mailer with this method first to verify impact!
     */
    private void simulate(Map map) {
        for (Iterator it = map.keySet().iterator(); it.hasNext();) {
            String s = (String) it.next();
            String d = (String) map.get(s);
            log.info(s+"\n"+d+"----\n");
        }
    }

    private static void setupLog() {
        try {
            FileOutputStream os = new FileOutputStream("/home/literakl/spam_result.txt",false);
            WriterAppender appender = new WriterAppender(new PatternLayout(),os);
            BasicConfigurator.configure(appender);
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * @return id of latest user
     */
    private int getLastUserId() {
        try {
            List list = persistance.findByCommand("select max(cislo) from uzivatel");
            Object[] objects = (Object[]) list.get(0);
            return ((Integer)objects[0]).intValue();
        } catch (Exception e) {
            log.error("Cannot get last user's id!", e);
            return 0;
        }
    }
}
