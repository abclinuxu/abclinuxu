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
package cz.abclinuxu.migrate;

import cz.abclinuxu.data.Poll;
import cz.abclinuxu.data.PollChoice;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.impl.MySqlPersistence;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tool that migrates poll to new format with single table
 * User: leos
 * Date: 11.11.2005
 */
public class UpgradePoll {
    MySqlPersistence persistance;

    public UpgradePoll() {
        persistance = (MySqlPersistence) PersistenceFactory.getPersistence(PersistenceFactory.directUrl);
    }

    void run() throws Exception {
        List polls = loadAllPolls();
        for (Iterator iter = polls.iterator(); iter.hasNext();) {
            Poll poll = (Poll) iter.next();
            persistance.create(poll);
        }
        // smaz puvodni data - rucne, drop table
    }

    List loadAllPolls() throws SQLException {
        Connection con = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.createStatement();
            resultSet = statement.executeQuery("select * from anketa");

            List result = new ArrayList();
            while ( resultSet.next() ) {
                Poll poll = new Poll(resultSet.getInt(1));
                poll.setOwner(1);
                poll.setText(resultSet.getString(3));
                poll.setMultiChoice(resultSet.getBoolean(4));
                poll.setCreated(new java.util.Date(resultSet.getTimestamp(5).getTime()));
                poll.setClosed(resultSet.getBoolean(6));
                result.add(poll);
            }

            for (Iterator iter = result.iterator(); iter.hasNext();) {
                Poll poll = (Poll) iter.next();
                resultSet = statement.executeQuery("select volba,pocet from data_ankety where anketa="+poll.getId()+" order by cislo asc");

                List choices = new ArrayList();
                int votes = 0;
                while ( resultSet.next() ) {
                    PollChoice choice = new PollChoice(resultSet.getString(1));
                    choice.setCount(resultSet.getInt(2));
                    votes += choice.getCount();
                    choice.setPoll(poll.getId());
                    choices.add(choice);
                }

                poll.setTotalVoters(votes);
                poll.setChoices(choices);
                poll.setInitialized(true);
            }

            return result;
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    public static void main(String[] args) throws Exception {
        UpgradePoll upgrade = new UpgradePoll();
        upgrade.run();
    }
}
