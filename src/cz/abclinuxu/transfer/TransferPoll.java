/*
 * User: literakl
 * Date: Jan 26, 2002
 * Time: 10:01:56 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.transfer;

import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;

import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * Transfers polls from php PHPPoll format to abc linuxu format.
 */
public class TransferPoll {

    static {
        try { Class.forName("org.gjt.mm.mysql.Driver"); } catch (Exception e) { e.printStackTrace(); }
    }

    public static void main(String[] args) throws Exception {
        Connection conHw = DriverManager.getConnection("jdbc:mysql://localhost/hardware?user=literakl&password=lkaretil");
        String url = "jdbc:mysql://localhost/abc?user=literakl&password=lkaretil&useUnicode=true&characterEncoding=ISO-8859-2";
        Persistance persistance = PersistanceFactory.getPersistance(url);
        Category pollsCategory = new Category(240);

        Statement statement = conHw.createStatement();
        PreparedStatement preparedStatement = conHw.prepareStatement("select optionText,optionCount from vbooth_data where pollId=? order by voteId asc");
        ResultSet polls = statement.executeQuery("select * from vbooth_desc order by pollId asc");
        while ( polls.next() ) {
            int id = polls.getInt(1);

            Poll poll = new Poll(id);
            poll.setText(polls.getString(2));
            long unixTimeStamp = polls.getInt(3);
            poll.setCreated(new java.util.Date(1000*unixTimeStamp));
            poll.setClosed(true);
            poll.setType(Poll.SURVEY);

            preparedStatement.setInt(1,id);
            ResultSet set = preparedStatement.executeQuery();

            int i = 0;
            List choices = new ArrayList();
            while ( set.next() ) {
                PollChoice choice = new PollChoice(set.getString(1));
                choice.setCount(set.getInt(2));
                choices.add(choice);
            }
            set.close();

            poll.setChoices(choices);
            persistance.create(poll);
            Relation relation = new Relation(pollsCategory,poll,250);
            persistance.create(relation);
        }
    }
}
