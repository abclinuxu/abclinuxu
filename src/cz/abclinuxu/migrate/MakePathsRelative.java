/*
 * User: literakl
 * Date: 23.8.2003
 * Time: 8:13:30
 */
package cz.abclinuxu.migrate;

import cz.abclinuxu.persistance.impl.MySqlPersistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.cache.EmptyCache;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.apache.regexp.StringCharacterIterator;

/**
 *
 */
public class MakePathsRelative {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MakePathsRelative.class);
    static RE reAbsoluteURL;
    static MySqlPersistance persistance;

    static {
        try {
            reAbsoluteURL = new RE("(HREF|SRC)(=\")(http://(www.)?abclinuxu.cz)(/[^\"]+)", RE.MATCH_CASEINDEPENDENT);
//            reAbsoluteURL = new RE("(HREF|SRC)(=\"http://(www.)?abclinuxu.cz)([^\"]+)(\")", RE.MATCH_CASEINDEPENDENT+RE.MATCH_SINGLELINE);
            persistance = (MySqlPersistance) PersistanceFactory.getPersistance("jdbc:mysql://localhost/abc?user=literakl&password=lkaretil&useUnicode=true&characterEncoding=ISO-8859-2", EmptyCache.class);
        } catch (RESyntaxException e) {
            log.error("regexp syntax troubles", e);
        }
    }

    public static void main(String[] args) throws Exception {
        if ( args.length==0 ) {
            showHelp();
            System.exit(1);
        }

        long start = System.currentTimeMillis();
        if ( "--find".equalsIgnoreCase(args[0]) ) {
            List keys = findMatchingRows("zaznam");
            int totalRecords = findAndPrintAbsoluteURLs("zaznam", keys);
            keys = findMatchingRows("polozka");
            int totalItems = findAndPrintAbsoluteURLs("polozka", keys);
            keys = findMatchingRows("kategorie");
            int totalCategories = findAndPrintAbsoluteURLs("kategorie", keys);
            System.out.println("Total number of matching records: " + totalRecords);
            System.out.println("Total number of matching items: " + totalItems);
            System.out.println("Total number of matching categories: " + totalCategories);
        } else if ( "--fix".equalsIgnoreCase(args[0]) ) {
            List keys = findMatchingRows("zaznam");
            int totalRecords = fixAbsoluteURLs("zaznam", keys);
            keys = findMatchingRows("polozka");
            int totalItems = fixAbsoluteURLs("polozka", keys);
            keys = findMatchingRows("kategorie");
            int totalCategories = fixAbsoluteURLs("kategorie", keys);
            System.out.println("Total number of converted URLs in records: " + totalRecords);
            System.out.println("Total number of converted URLs in items: " + totalItems);
            System.out.println("Total number of converted URLs in categories: " + totalCategories);
        } else
            showHelp();
        long end = System.currentTimeMillis();
        System.out.println("Total time: "+(end-start)/1000+" seconds");
    }

    private static final void showHelp() {
        System.out.println("Usage:\t--find  prints absolute URLs");
        System.out.println("      \t--fix   converts absolute URLs to relative URLs");
    }

    /**
     * Finds all rows, that contain absolute URL and prints them to screen.
     * @return number of matching rows
     */
    private static final int findAndPrintAbsoluteURLs(String table, List keys) throws Exception {
        Connection con = null; Statement statement = null; ResultSet resultSet = null;
        int total = 0;
        try {
            con = persistance.getSQLConnection();
            statement = con.createStatement();
            String value = null;

            for ( Iterator iter = keys.iterator(); iter.hasNext(); ) {
                int key = ((Integer) iter.next()).intValue();
                resultSet = statement.executeQuery("select data from "+table+" where cislo="+key);
                if (! resultSet.next() )
                    continue;
                value = resultSet.getString(1);

                StringCharacterIterator stringIter = new StringCharacterIterator(value);
                int position = 0;
                while (reAbsoluteURL.match(stringIter,position)) {
                    position = reAbsoluteURL.getParenEnd(0);
                    System.out.println(table+" "+key+": "+reAbsoluteURL.getParen(0));
                    total++;
                }

                resultSet.close(); resultSet = null;
            }
            return total;
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds all rows, that contain absolute URL and makes them relative.
     * @return number of fixed rows
     */
    private static final int fixAbsoluteURLs(String table, List keys) throws Exception {
        Connection con = null; Statement statement = null; PreparedStatement prepared = null; ResultSet resultSet = null;
        int total = 0, position = 0, start = 0, key = 0;

        log.info("Fix absolute paths invoked by user "+System.getProperty("user.name"));

        try {
            con = persistance.getSQLConnection();
            statement = con.createStatement();
            prepared = con.prepareStatement("update "+table+" set data=?, zmeneno=zmeneno where cislo=?");
            String value = null;

            for ( Iterator iter = keys.iterator(); iter.hasNext(); ) {
                try {
                    key = ((Integer) iter.next()).intValue();
                    resultSet = statement.executeQuery("select data from " + table + " where cislo=" + key);
                    if (! resultSet.next() )
                        continue;
                    value = resultSet.getString(1);

                    if ( reAbsoluteURL.match(value) ) {
                        position = 0;
                        StringBuffer sb = new StringBuffer();
                        StringCharacterIterator stringIter = new StringCharacterIterator(value);

                        try {
                            do {
                                start = reAbsoluteURL.getParenStart(3);
                                sb.append(stringIter.substring(position, start));
                                position = reAbsoluteURL.getParenEnd(3);
                            } while ( reAbsoluteURL.match(stringIter, position) );
                            sb.append(stringIter.substring(position));
                            total++;
                        } catch (Exception e) {
                            log.error(table + " " + key + ", position="+position+", start="+start, e);
                            System.exit(1);
                        }

                        prepared.setString(1,sb.toString());
                        prepared.setInt(2,key);
                        if ( prepared.executeUpdate()!=1 )
                            System.out.println("Failed to update "+table + " " + key);
                        else
                            log.info(table+" "+key+" fixed absolute url");
                    }

                    resultSet.close(); resultSet = null;
                } catch (Exception e) {
                    log.error("Error on convertion of " + table + " " + key, e);
                }
            }
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
        log.info("Total number of converted "+table+"s: "+total);
        return total;
    }

    /**
     * Finds all records.
     * @return list of Integers
     */
    private static final List findMatchingRows(String table) throws Exception {
        Connection con = null; Statement statement = null; ResultSet resultSet = null;
        List result = new ArrayList();
        try {
            con = persistance.getSQLConnection();
            statement = con.createStatement();
            resultSet = statement.executeQuery("select cislo from "+table+" where data like '%=\"http://www.abclinuxu.cz%' or data like '%=\"http://abclinuxu.cz%'");
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
