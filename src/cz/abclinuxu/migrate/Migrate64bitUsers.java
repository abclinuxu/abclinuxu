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
import java.util.List;
import java.util.StringTokenizer;
import java.sql.Date;
import java.sql.Timestamp;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;

/**
 * Migrates 64bit users to LDAP
 * @author literakl
 * @since 6.8.2008
 */
public class Migrate64bitUsers {
    public static final String SERVER_64BIT_CZ = "www.64bit.cz";
    public static final String SERVER_64BIT_SK = "www.64bit.sk";
    public static final String SERVER_64BIT_BIZ = "www.64bit.biz";

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: Migrate64bitUsers dumpFile ");
            System.exit(1);
        }

        Map<String, String> changes = new HashMap<String, String>();
        long start = System.currentTimeMillis();
        int created = 0, merged = 0, conflicts = 0;
        Map<String, String> ldapUser;

        LdapUserManager ldapMgr = LdapUserManager.getInstance();
        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        String line = reader.readLine(); // skip the first line

        while ((line = reader.readLine()) != null) {
            changes.clear();
            String id = null;
            String[] rs;
            boolean conflict = false;
            try {
                rs = parseLine(line, '\t', 22);
                id = rs[0];
                String server = rs[1], login = rs[2], passwd = rs[3], company = rs[4];
                String ic = rs[5], dic = rs[6], surname = rs[7], name = rs[8];
                String email = rs[9], phone = rs[10], street = rs[11], city = rs[12];
                String postcode = rs[13], country = rs[14], deliverCompany = rs[15];
                String deliverStreet = rs[16], deliverCity = rs[17], deliverPostcode = rs[18];
                String deliverCountry = rs[19], sRegistrationDate = rs[20], sTimestamp = rs[21];

                String registrationDate = sRegistrationDate + " 00:00";
                String lastLoginDate = sTimestamp.substring(0, 16);
                if (server.equals("3"))
                    server = SERVER_64BIT_BIZ;
                else if (server.equals("2"))
                    server = SERVER_64BIT_SK;
                else
                    server = SERVER_64BIT_CZ;

                ldapUser = ldapMgr.getUserInformation(login, new String[] {LdapUserManager.ATTRIB_EMAIL_ADRESS});
                if (ldapUser.size() == 1 && ! email.equals(ldapUser.values().iterator().next())) {
                    conflict = true;
                    conflicts++;
                    ldapUser.clear();
                    System.out.println("conflict " + login + ", email " + email);
                }
                if (ldapUser.size() == 0) {
                    // create
                    created++;
                    System.out.println("create " + login + ", email " + email);
                } else {
                    // merge
                    merged++;
                    System.out.println("merge " + login + ", email " + email);
                }

            } catch (Exception e) {
                System.err.println("Migration of user " + id + " failed. Reason: " + e.getMessage());
            }
        }
        System.out.println("Created " + created + " new users in LDAP, merged " + merged + " existing users, " + conflicts + " conflicts");
    }

    private static String[] parseLine(String line, char separator, int length) {
        String[] result = new String[length];
        StringTokenizer stk = new StringTokenizer(line, String.valueOf(separator), true);
        int i = 0;
        while (stk.hasMoreTokens()) {
            String s = stk.nextToken();
            if (s.length() == 1 && s.charAt(0) == separator)
                i++;
            else
                result[i] = s;
        }
        return result;
    }
/*
conversion

problemy:

a) ilegalni znaky: tspbrno@tycoint.
b) divne kodovani

mysql -e "set names latin2;select id,idserver,login,passwd,company,ic,dic,surname,name,email,phone,street,city,post
code,country,deliver_company,deliver_street,deliver_city,deliver_postcode,deliver_country,regist_date,last_login_date from ko_user where
id not in (153, 171,148,174,39,179,173,182,1,58,175,44,103,128,3,172,133,156,159,105,170,157,189) order by id" 64bitnew > 64bit_users.txt

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
