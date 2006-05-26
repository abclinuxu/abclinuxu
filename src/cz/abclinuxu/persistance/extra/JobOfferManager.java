/*
 *  Copyright (C) 2006 Leos Literak
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
package cz.abclinuxu.persistance.extra;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.data.view.JobOffer;

import java.util.prefs.Preferences;
import java.util.TimerTask;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Tool for fetching fresh job offers from praceabc.
 * @author literakl
 * @since 26.5.2006
 */
public class JobOfferManager extends TimerTask implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JobOfferManager.class);

    public static final String PREF_JDBC_URL = "jdbc.url";
    public static final String PREF_SQL_QUERY = "sql.offers";

    private String jdbcUrl;
    // all offers must be sorted by last modified column in descending order
    private String sqlQuery;
    private static List offers = new ArrayList();

    /**
     * @return all offers (JobOffer instances)
     */
    public static List getOffers() {
        synchronized(offers) {
            return new ArrayList(offers);
        }
    }

    /**
     * @return all offers (JobOffer instances)
     */
    public static List getOffersAfter(Date limit) {
        synchronized(offers) {
            List result = new ArrayList(offers.size());
            for (Iterator iter = offers.iterator(); iter.hasNext();) {
                JobOffer offer = (JobOffer) iter.next();
                if (offer.getLastModified().before(limit))
                    break;
                result.add(offer);
            }
            return result;
        }
    }

    public void run() {
        try {
            List newOffers = new ArrayList(100);
            Connection connection = DriverManager.getConnection(jdbcUrl);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sqlQuery);
            while (resultSet.next()) {
                JobOffer offer = convert(resultSet);
                newOffers.add(offer);
            }

            synchronized(offers) {
                offers.clear();
                offers.addAll(newOffers);
            }
        } catch (Exception e) {
            log.error("JobOfferManager failed", e);
        }
    }

    private JobOffer convert(ResultSet rs) throws SQLException {
        JobOffer offer = new JobOffer(rs.getInt(1), rs.getString(2));
        offer.setLastModified(new Date(rs.getTimestamp(3).getTime()));
        offer.setItJob(rs.getInt(4)==1);
        offer.setLinuxJob(rs.getInt(5)==1);
        offer.setCategory(rs.getString(6));
        offer.setRegion(rs.getString(7));
        offer.setCompany(rs.getString(8));
        offer.setJobType(rs.getString(9));
        offer.setScope(rs.getString(10));
        return offer;
    }

    public JobOfferManager() {
        ConfigurationManager.getConfigurator().configureAndRememberMe(this);
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        jdbcUrl = prefs.get(PREF_JDBC_URL, null);
        if (jdbcUrl == null)
            throw new ConfigurationException("Configuration for "+PREF_JDBC_URL+" is missing!");
        sqlQuery = prefs.get(PREF_SQL_QUERY, null);
        if (sqlQuery == null)
            throw new ConfigurationException("Configuration for "+PREF_SQL_QUERY+" is missing!");
    }
}
