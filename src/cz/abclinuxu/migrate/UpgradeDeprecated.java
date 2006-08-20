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

import cz.abclinuxu.persistence.impl.MySqlPersistence;
import cz.abclinuxu.persistence.PersistenceFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.apache.regexp.StringCharacterIterator;

/**
 * Upgrades deprecated URLs to current format.
 */
public class UpgradeDeprecated {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UpgradeDeprecated.class);
    static RE reViewRelation, reProfile, reIndex;

    static {
        try {
            reIndex = new RE("(href=\"/)(Index[^\"]*)");
            reProfile = new RE("(href=\"/Profile\\?(userId|uid)=)([\\d]+)([^\"]*)", RE.MATCH_CASEINDEPENDENT);
            reViewRelation = new RE("((href|url)=\"[a-z/]+)(ViewRelation[^\"]+(relationId|rid)=)([\\d]+)([^#\"]*)", RE.MATCH_CASEINDEPENDENT);
        } catch (RESyntaxException e) {
            log.error("regexp syntax troubles", e);
        }
    }

    public static void main(String[] args) throws Exception {
        if ( args.length==0 ) {
            showHelp();
            System.exit(1);
        }

        List keys = findAllRecords();
        if ( "--find".equalsIgnoreCase(args[0]) )
            findAndPrint(keys);
        else if ( "--fix".equalsIgnoreCase(args[0]) )
            fixAndPrint(keys);
        else
            showHelp();
    }

    private static final void showHelp() {
        System.out.println("Usage:\t--find  prints absolute URLs");
        System.out.println("      \t--fix   converts absolute URLs to relative URLs");
    }

    /**
     * Finds all records, that contain deprecated URL and prints them to screen.
     */
    private static final void findAndPrint(List keys) throws Exception {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
        Connection con = null; Statement statement = null; ResultSet resultSet = null;
        int total = 0;
        try {
            con = persistance.getSQLConnection();
            statement = con.createStatement();
            String value = null;

            for ( Iterator iter = keys.iterator(); iter.hasNext(); ) {
                int key = ((Integer) iter.next()).intValue();
                resultSet = statement.executeQuery("select data from zaznam where cislo="+key);
                if (! resultSet.next() )
                    continue;
                value = resultSet.getString(1);

                StringCharacterIterator stringIter = new StringCharacterIterator(value);
                int position = 0;
                while (reViewRelation.match(stringIter,position)) {
                    position = reViewRelation.getParenEnd(0);
                    System.out.println("Record "+key+" contains old URL: "+reViewRelation.getParen(0));
                    total++;
                }

                stringIter = new StringCharacterIterator(value);
                position = 0;
                while (reProfile.match(stringIter,position)) {
                    position = reProfile.getParenEnd(0);
                    System.out.println("Record "+key+" contains old URL: "+reProfile.getParen(0));
                    total++;
                }

                stringIter = new StringCharacterIterator(value);
                position = 0;
                while (reIndex.match(stringIter,position)) {
                    position = reIndex.getParenEnd(0);
                    System.out.println("Record "+key+" contains old URL: "+reIndex.getParen(0));
                    total++;
                }

                resultSet.close(); resultSet = null;
            }
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
        System.out.println("Total number of deprecated URLS: "+total);
    }

    /**
     * Finds all records, that contain deprecated URL and upgrades them.
     */
    private static final void fixAndPrint(List keys) throws Exception {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
        Connection con = null; Statement statement = null; PreparedStatement prepared = null; ResultSet resultSet = null;
        int total = 0, skipped = 0;

        log.info("Upgrade deprecated URL invoked by user "+System.getProperty("user.name"));

        try {
            con = persistance.getSQLConnection();
            statement = con.createStatement();
            prepared = con.prepareStatement("update zaznam set data=?, zmeneno=zmeneno where cislo=?");
            int position = 0, start = 0, key = 0;
            String value = null;

            for ( Iterator iter = keys.iterator(); iter.hasNext(); ) {
                try {
                    key = ((Integer) iter.next()).intValue();
                    resultSet = statement.executeQuery("select data from zaznam where cislo="+key);
                    if (! resultSet.next() )
                        continue;
                    value = resultSet.getString(1);
                    boolean modified = false;

                    if ( reViewRelation.match(value) ) {
                        position = 0;
                        StringBuffer sb = new StringBuffer();
                        StringCharacterIterator stringIter = new StringCharacterIterator(value);

                        try {
                            do {
                                start = reViewRelation.getParenStart(0);
                                sb.append(stringIter.substring(position, start));
                                sb.append(reViewRelation.getParen(1));
                                sb.append("show/");
                                sb.append(reViewRelation.getParen(5));
                                position = reViewRelation.getParenEnd(0);
                            } while ( reViewRelation.match(stringIter, position) );
                            sb.append(stringIter.substring(position));

                            value = sb.toString();
                            modified = true;
                            total++;
                        } catch (Exception e) {
                            log.error("position="+position+", start="+start, e);
                            System.exit(1);
                        }
                    }

                    if ( reProfile.match(value) ) {
                        position = 0;
                        StringBuffer sb = new StringBuffer();
                        StringCharacterIterator stringIter = new StringCharacterIterator(value);

                        try {
                            do {
                                start = reProfile.getParenStart(0);
                                sb.append(stringIter.substring(position, start));
                                sb.append("href=\"/Profile/");
                                sb.append(reProfile.getParen(3));
                                position = reProfile.getParenEnd(0);
                            } while ( reProfile.match(stringIter, position) );
                            sb.append(stringIter.substring(position));

                            value = sb.toString();
                            modified = true;
                            total++;
                        } catch (Exception e) {
                            log.error("position="+position+", start="+start, e);
                            System.exit(1);
                        }
                    }

                    if ( reIndex.match(value) ) {
                        position = 0;
                        StringBuffer sb = new StringBuffer();
                        StringCharacterIterator stringIter = new StringCharacterIterator(value);

                        try {
                            do {
                                start = reIndex.getParenStart(0);
                                sb.append(stringIter.substring(position, start));
                                sb.append(reIndex.getParen(1));
                                position = reIndex.getParenEnd(0);
                            } while ( reIndex.match(stringIter, position) );
                            sb.append(stringIter.substring(position));

                            value = sb.toString();
                            modified = true;
                            total++;
                        } catch (Exception e) {
                            log.error("position="+position+", start="+start, e);
                            System.exit(1);
                        }
                    }

                    if (modified) {
                        prepared.setString(1, value);
                        prepared.setInt(2, key);
                        if ( prepared.executeUpdate()!=1 )
                            System.out.println("Failed to update Record "+key);
                        else
                            log.info("upgraded deprecated URLs in Record "+key);
                    }
                    else
                        skipped++;

                    resultSet.close(); resultSet = null;
                } catch (Exception e) {
                    log.error("Error on conversion of Record "+key, e);
                }
            }
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
        log.info("Total number of converted Records: "+total);
        System.out.println("Total number of converted Records: "+total);
        System.out.println("Total number of skipped Records: "+skipped);
    }

    /**
     * Finds all records.
     * @return list of Integers
     */
    private static final List findAllRecords() throws Exception {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
        Connection con = null; Statement statement = null; ResultSet resultSet = null;
        List result = new ArrayList();
        try {
            con = persistance.getSQLConnection();
            statement = con.createStatement();
            resultSet = statement.executeQuery("select * from zaznam");
            while ( resultSet.next() ) {
                Integer id = new Integer(resultSet.getInt(1));
                result.add(id);
            }
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
        return result;
    }
}
