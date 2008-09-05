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

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Migrates 64bit users to LDAP
 * @author literakl
 * @since 6.8.2008
 */
public class Migrate64bitUsers {
    public static final String SERVER_64BIT = "www.64bit.cz";

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: Migrate64bitUsers dumpFile ");
            System.exit(1);
        }

        Map<String, String> changes = new HashMap<String, String>();
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
                if ("189".equals(id)) // invalidni login duplicitniho uzivatele
                    continue;

                String server = rs[1], login = rs[2], password = rs[3], company = rs[4];
                String ic = rs[5], dic = rs[6], surname = rs[7], name = rs[8];
                String email = rs[9], phone = rs[10], street = rs[11], city = rs[12];
                String postcode = rs[13], country = rs[14], deliverCompany = rs[15];
                String deliverStreet = rs[16], deliverCity = rs[17], deliverPostcode = rs[18];
                String deliverCountry = rs[19], sRegistrationDate = rs[20], sTimestamp = rs[21];

                name = name + " " + surname;
                String registrationDate = sRegistrationDate + " 00:00";
                String lastLoginDate = sTimestamp.substring(0, 16);

                ldapUser = ldapMgr.getUserInformation(login, new String[] {LdapUserManager.ATTRIB_EMAIL_ADRESS, LdapUserManager.ATTRIB_LAST_LOGIN_DATE});
                String sfEmail = ldapUser.get(LdapUserManager.ATTRIB_EMAIL_ADRESS);
                if (sfEmail == null)
                    sfEmail = ldapUser.get("email");

                if (ldapUser.size() == 1 && ! email.equals(sfEmail)) {
                    conflict = true;
                    conflicts++;
                    ldapUser.clear();
                    System.out.println("conflict " + login + ", email " + email);
                }
                if (ldapUser.size() == 0) {
                    // create
                    created++;
                    System.out.println("create " + login + ", email " + email);

                    if (conflict)
                        login = "64bit" + login;

                    ldapMgr.registerUser(login, password, null, name, SERVER_64BIT);
                    changes.put(LdapUserManager.ATTRIB_REGISTRATION_DATE, registrationDate);
                    changes.put(LdapUserManager.ATTRIB_REGISTRATION_PORTAL, SERVER_64BIT);
                    changes.put(LdapUserManager.ATTRIB_VISITED_PORTAL, SERVER_64BIT);
                    changes.put(LdapUserManager.ATTRIB_EMAIL_ADRESS, email);
                    changes.put(LdapUserManager.ATTRIB_EMAIL_BLOCKED, "false");
                    changes.put(LdapUserManager.ATTRIB_EMAIL_VERIFIED, "true");
                    changes.put(LdapUserManager.ATTRIB_LAST_LOGIN_DATE, lastLoginDate);
                    setValues(phone, changes, company, ic, dic, street, city, postcode, country,
                            deliverCompany, deliverStreet, deliverCity, deliverPostcode, deliverCountry);

                    ldapMgr.updateUser(login, changes);

                } else {
                    // merge
                    merged++;
                    System.out.println("merge " + login + ", email " + email);

                    changes.put(LdapUserManager.ATTRIB_VISITED_PORTAL, SERVER_64BIT);
                    setValues(phone, changes, company, ic, dic, street, city, postcode, country,
                            deliverCompany, deliverStreet, deliverCity, deliverPostcode, deliverCountry);

                    String lastAbcLogin = ldapUser.get(LdapUserManager.ATTRIB_LAST_LOGIN_DATE);
                    if (lastAbcLogin != null && lastAbcLogin.compareTo(lastLoginDate) < 0) {
                        System.out.println("Abc ma starsi login: " + lastAbcLogin + ", " + lastLoginDate);
                        changes.put(LdapUserManager.ATTRIB_LAST_LOGIN_DATE, lastLoginDate);
                    }

                    ldapMgr.updateUser(login, changes);
                }

            } catch (Exception e) {
                System.err.println("Migration of user " + id + " failed. Reason: " + e.getMessage());
            }
        }
        System.out.println("Created " + created + " new users in LDAP, merged " + merged + " existing users, " + conflicts + " conflicts");
    }

    private static void setValues(String phone, Map<String, String> changes, String company, String ic, String dic, String street, String city, String postcode, String country, String deliverCompany, String deliverStreet, String deliverCity, String deliverPostcode, String deliverCountry) {
        if (phone != null)
            changes.put(LdapUserManager.ATTRIB_PHONE, phone);
        if (company != null)
            changes.put(LdapUserManager.ATTRIB_INVOICING_COMPANY, company);
        if (ic != null)
            changes.put(LdapUserManager.ATTRIB_INVOICING_COMPANY_ICO, ic);
        if (dic != null)
            changes.put(LdapUserManager.ATTRIB_INVOICING_COMPANY_ICO, dic);
        if (street != null)
            changes.put(LdapUserManager.ATTRIB_INVOICING_ADDRESS_STREET, street);
        if (city != null)
            changes.put(LdapUserManager.ATTRIB_INVOICING_ADDRESS_CITY, city);
        if (postcode != null)
            changes.put(LdapUserManager.ATTRIB_INVOICING_ADDRESS_ZIP, postcode);
        if (country != null)
            changes.put(LdapUserManager.ATTRIB_INVOICING_ADDRESS_ZIP, postcode);
        if (deliverCompany != null)
            changes.put(LdapUserManager.ATTRIB_DELIVERY_ADDRESS_NAME, company);
        if (deliverStreet != null)
            changes.put(LdapUserManager.ATTRIB_DELIVERY_ADDRESS_STREET, street);
        if (deliverCity != null)
            changes.put(LdapUserManager.ATTRIB_DELIVERY_ADDRESS_CITY, city);
        if (deliverPostcode != null)
            changes.put(LdapUserManager.ATTRIB_DELIVERY_ADDRESS_ZIP, postcode);
        if (deliverCountry != null)
            changes.put(LdapUserManager.ATTRIB_DELIVERY_ADDRESS_ZIP, postcode);
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

mysql -e "set names latin2;select id,idserver,login,passwd,company,ic,dic,surname,name,email,phone,street,city,post
code,country,deliver_company,deliver_street,deliver_city,deliver_postcode,deliver_country,regist_date,last_login_date from ko_user where
id not in (153, 171,148,174,39,179,173,182,1,58,175,44,103,128,3,172,133,156,159,105,170,157,189) order by id" 64bitnew > 64bit_users.txt

update ko_user set name=concat(name," ",surname);

alter table ko_user drop column passwd;
alter table ko_user drop column regist_date;
alter table ko_user drop column last_login_date;
alter table ko_user drop column company;
alter table ko_user drop column ic;
alter table ko_user drop column dic;
alter table ko_user drop column surname;
alter table ko_user drop column phone;
alter table ko_user drop column street;
alter table ko_user drop column city;
alter table ko_user drop column postcode;
alter table ko_user drop column country;
alter table ko_user drop column deliver_company;
alter table ko_user drop column deliver_street;
alter table ko_user drop column deliver_city;
alter table ko_user drop column deliver_postcode;
alter table ko_user drop column deliver_country;

*/
}
