/*
 *  Copyright (C) 2008 Leos Literak
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

import cz.abclinuxu.persistence.ldap.LdapUserManager;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * Migrates 64bit users to LDAP
 * @author literakl
 * @since 6.8.2008
 */
public class Migrate64bitUsers {
    static Set ignored;
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int[] ids = {153, 171,148,174,39,179,173,182,1,58,175,44,103,128,3,172,133,156,159,105,170,157};
        ignored = new HashSet();
        for (int i = 0; i < ids.length; i++) {
            ignored.add(ids[i]);
        }
    }

    public static void main(String[] args) throws SQLException {
        if (args.length != 4) {
            System.out.println("Usage: Migrate64bitUsers dbhost schema user password ");
            System.exit(1);
        }
        String dbhost = args[0], dbSchema = args[1], dbUser = args[2], dbPassword = args[3];
        Map<String, String> changes = new HashMap<String, String>();
        long start = System.currentTimeMillis();
        int created = 0, merged = 0, conflicts = 0;
        LdapUserManager ldapMgr = LdapUserManager.getInstance();

//        String dbUrl = "jdbc:mysql://"+dbhost+"/"+dbSchema+"?user="+dbUser+"&password="+dbPassword+"&useUnicode=true&characterEncoding=ISO-8859-2";
//        String dbUrl = "jdbc:mysql://"+dbhost+"/"+dbSchema+"?user="+dbUser+"&password="+dbPassword+"&useUnicode=true";
//        String dbUrl = "jdbc:mysql://"+dbhost+"/"+dbSchema+"?user="+dbUser+"&password="+dbPassword+"&mysqlEncoding=latin2";
        String dbUrl = "jdbc:mysql://"+dbhost+"/"+dbSchema+"?user="+dbUser+"&password="+dbPassword+"&characterEncoding=ISO-8859-2&mysqlEncoding=latin2";
        Connection con = DriverManager.getConnection(dbUrl);
        Statement statement = con.createStatement();
//        statement.execute("set names latin2");
        String sql = "select id,idserver,login,passwd,company,ic,dic,surname,name,email,phone,street,city,postcode,country,"+
                "deliver_company,deliver_street,deliver_city,deliver_postcode,deliver_country,regist_date,last_login_date from ko_user";
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            changes.clear();
            int id = -1, server;
            try {
                id = rs.getInt(1);
                server = rs.getInt(2);
                String login = rs.getString(3), passwd = rs.getString(4), company = rs.getString(5);
                String ic = rs.getString(6), dic = rs.getString(7), surname = rs.getString(8), name = rs.getString(9);
                String email = rs.getString(10), phone = rs.getString(11), street = rs.getString(12), city = rs.getString(13);
                String postcode = rs.getString(14), country = rs.getString(15), deliverCompany = rs.getString(16);
                String deliverStreet = rs.getString(17), deliverCity = rs.getString(18), deliverPostcode = rs.getString(19);
                String deliverCountry = rs.getString(20);
                Date registrationDate = rs.getDate(21);
                Timestamp timestamp = rs.getTimestamp(22);
                Date lastLoginDate = (timestamp == null) ? null : new Date(timestamp.getTime());

                System.out.println("surname = " + surname);

//                ldapMgr.registerUser(user.getLogin(), user.getPassword(), null, user.getName(), "www.abclinuxu.cz");
//
//                changes.put(LdapUserManager.ATTRIB_REGISTRATION_DATE, tmp);
//                changes.put(LdapUserManager.ATTRIB_REGISTRATION_PORTAL, "www.abclinuxu.cz");
//                changes.put(LdapUserManager.ATTRIB_VISITED_PORTAL, "www.abclinuxu.cz");
//                if ("no".equals(tmp))
//                    changes.put(LdapUserManager.ATTRIB_EMAIL_BLOCKED, "true");
//                else
//                    changes.put(LdapUserManager.ATTRIB_EMAIL_BLOCKED, "false");
//                changes.put(LdapUserManager.ATTRIB_EMAIL_VERIFIED, "true");
//                changes.put(LdapUserManager.ATTRIB_SEX, tmp);
//
//                ldapMgr.updateUser(user.getLogin(), changes);
            } catch (Exception e) {
                System.err.println("Migration of user " + id + " failed. Reason: " + e.getMessage());
            }
        }
        long end = System.currentTimeMillis();
    }
/*
conversion

problemy:

a) ilegalni znaky: tspbrno@tycoint.
b) divne kodovani


| id               | int(10)     | -
| idserver         | int(11)     | -
| idgroup          | int(10)     | -
| login            | varchar(16) | ATTRIB_LOGIN
| passwd           | varchar(16) | ATTRIB_PASSWORD
| company          | varchar(64) | ATTRIB_INVOICING_COMPANY
| ic               | varchar(16) | ATTRIB_INVOICING_COMPANY_ICO
| dic              | varchar(32) | ATTRIB_INVOICING_COMPANY_DIC
| surname          | varchar(250)| ATTRIB_NAME
| name             | varchar(32) | ATTRIB_NAME
| email            | varchar(64) | ATTRIB_EMAIL_ADRESS, ATTRIB_EMAIL_BLOCKED, ATTRIB_EMAIL_VERIFIED
| phone            | varchar(16) | ATTRIB_PHONE
| street           | varchar(128)| ATTRIB_INVOICING_ADDRESS_STREET
| city             | varchar(64) | ATTRIB_INVOICING_ADDRESS_CITY
| postcode         | varchar(16) | ATTRIB_INVOICING_ADDRESS_ZIP
| country          | varchar(50) | ATTRIB_COUNTRY
| deliver_company  | varchar(64) | ATTRIB_DELIVERY_ADDRESS_NAME
| deliver_street   | varchar(128)| ATTRIB_DELIVERY_ADDRESS_STREET
| deliver_city     | varchar(64) | ATTRIB_DELIVERY_ADDRESS_CITY
| deliver_postcode | varchar(16) | ATTRIB_DELIVERY_ADDRESS_ZIP
| deliver_country  | varchar(50) | ATTRIB_DELIVERY_ADDRESS_COUNTRY
| from_where       | varchar(128)| -
| discount         | tinyint(4)  | -
| note             | text        | -
| blocked          | tinyint(1)  | -
| regist_date      | date        | ATTRIB_REGISTRATION_DATE, ATTRIB_REGISTRATION_PORTAL, ATTRIB_VISITED_PORTAL
| last_login_date  | datetime    | ATTRIB_LAST_LOGIN_DATE
| last_login_ip    | varchar(40) | -
| last_access_date | datetime    | -

*/
}
