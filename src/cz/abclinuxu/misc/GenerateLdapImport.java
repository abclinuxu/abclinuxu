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
package cz.abclinuxu.misc;

import cz.abclinuxu.persistence.impl.MySqlPersistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.ldap.LdapUserManager;

import java.io.File;
import java.io.Writer;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Generate import file for OpenLDAP from database. Emails are imagined,
 * password is set to 'secret'.
 * @author literakl
 * @since 4.4.2008
 */
public class GenerateLdapImport {
    static int column = 0;

    public static void main(String[] args) throws Exception {
        if (args.length != 2)
            showHelpAndExit();

        boolean defaultSchema = false;
        if ("default".equalsIgnoreCase(args[0]))
            defaultSchema = true;
        else if ("devel".equalsIgnoreCase(args[0]))
            defaultSchema = false;
        else
            showHelpAndExit();

        File file = new File(args[1]);
        if (file.exists() && ( ! file.canWrite() || file.isDirectory())) {
            System.err.println("Error: file '"+file.getName()+"' must not exist or it must be regular writable file!");
            showHelpAndExit();
        }
        Writer writer = new FileWriter(file);

        String dbUrl = (defaultSchema) ? PersistenceFactory.defaultUrl : PersistenceFactory.defaultDevelUrl;
        MySqlPersistence persistence = (MySqlPersistence) PersistenceFactory.getPersistence(dbUrl);
        Connection con = persistence.getSQLConnection();
        PreparedStatement statement;
        ResultSet resultSet;

//        statement = con.prepareStatement("select login,jmeno,data from uzivatel order by cislo limit ?,50");
        statement = con.prepareStatement("select login,jmeno from uzivatel order by cislo limit ?,50");
        int position = 0;
        int passwordHash = "secret".hashCode();
        while (true) {
            statement.setInt(1, position);
            resultSet = statement.executeQuery();
            boolean found = false;
            while (resultSet.next()) {
                found = true;
                String login = resultSet.getString(1);
                String name = resultSet.getString(2);
                writer.write("dn: " + LdapUserManager.ATTRIB_LOGIN + "=" + login.toLowerCase() + "," +
                             LdapUserManager.getInstance().getParentContext() + "\n");
                writer.write("objectClass: " + LdapUserManager.getInstance().getLdapClass() + "\n");
                writer.write("objectClass: person" + "\n");
                writer.write(LdapUserManager.ATTRIB_LOGIN + ": " + login.toLowerCase() + "\n");
                writer.write(LdapUserManager.ATTRIB_NAME + ": " + name + "\n");
                writer.write(LdapUserManager.ATTRIB_PASSWORD + ": secret\n");
                writer.write(LdapUserManager.ATTRIB_PASSWORD_HASHCODE + ": " + passwordHash + "\n");
                writer.write(LdapUserManager.ATTRIB_EMAIL_ADRESS + ": root@localhost\n");
                writer.write(LdapUserManager.ATTRIB_REGISTRATION_DATE + ": 2008-04-04 12:23\n");
                writer.write(LdapUserManager.ATTRIB_REGISTRATION_PORTAL + ": " + LdapUserManager.SERVER_ABCLINUXU + "\n");
                writer.write("\n");
                hash();
            }
            resultSet.close();
            if (!found)
                break;
            position += 50;
        }
        statement.close();
        con.close();
        writer.close();
    }

    static void hash() {
        if (column == 40) {
            column = 0;
            System.out.print('\n');
            System.out.flush();
        }
        System.out.print('#');
        column++;
    }

    private static void showHelpAndExit() {
        System.out.println("Usage: GenerateLdapImport default|devel file");
        System.out.println("creates ldif file from users in specified database schema");
        System.exit(1);
    }
}
