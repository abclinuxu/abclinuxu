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
import org.dom4j.Document;

/**
 * Sends sms to all users, that allowed sending news emails.
 */
public class MailNews {
    static Logger log = Logger.getLogger(MailNews.class);

    Persistance persistance = PersistanceFactory.getPersistance();

    public static void main(String[] args) throws Exception {
        boolean test = true;
        if ( args!=null && args.length==1 && args[0].equals("send") ) test = false;

        setupLog();
        Velocity.init("/home/literakl/abc/deploy/WEB-INF/velocity.properties");

        MailNews mailer = new MailNews();
        mailer.doWork(test);
    }

    void doWork(boolean test) {
        int max = getLastUserId();
        Map data = new HashMap(10);

        for ( int i=1; i<=max; i++ ) {
            addUsersEmail(i,data);
            if ( data.size()>9 ) {
                if ( test ) {
                    simulate(data);
                } else {
                    Email.sendBulkEmail("admin@AbcLinuxu.cz","AbcLinuxu: casopis Abicko",data);
                }
                data.clear();
            }
        }
        if ( test ) {
            simulate(data);
        } else {
            if ( data.size()>0 ) Email.sendBulkEmail("admin@AbcLinuxu.cz","AbcLinuxu: casopis abicko",data);
        }
    }

    private void addUsersEmail(int id, Map map) {
        try {
//            Category.getDefaultHierarchy().disableAll();
            User user = (User) persistance.findById(new User(id));
//            Category.getDefaultHierarchy().enableAll();

            Document document = user.getData();
            String str = document.selectSingleNode("data/news").getText();
            if ( str==null ) return;
            if ( !str.equalsIgnoreCase("yes") ) return;

            VelocityContext tmpContext = new VelocityContext();
            tmpContext.put("USER",user);
            String message = VelocityHelper.mergeTemplate("mail/abicko.vm",tmpContext);

            String email = user.getEmail();
            map.put(email,message);
        } catch (PersistanceException e) {
//            Category.getDefaultHierarchy().enableAll();
        }
    }

    /**
     * Run Mailer with this method first to verify impact!
     */
    private void simulate(Map map) {
        for (Iterator it = map.keySet().iterator(); it.hasNext();) {
            String s = (String) it.next();
            String d = (String) map.get(s);
            log.info(s+"\n"+d+"----\n\n");
        }
    }

    private static void setupLog() {
        try {
            FileOutputStream os = new FileOutputStream("/home/literakl/mail_result.txt",false);
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
