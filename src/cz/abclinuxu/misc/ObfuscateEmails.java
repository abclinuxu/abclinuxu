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
package cz.abclinuxu.misc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * Reads file wityh email adresses and obfuscates them,
 * so they are anonymous.
 * @author literakl
 * @since 3.5.2006
 */
public class ObfuscateEmails {

    public static void main(String[] args) throws Exception {
        String name = args[0];
        BufferedReader in = new BufferedReader(new FileReader(name));
        BufferedWriter out = new BufferedWriter(new FileWriter(name+".obfuscated"));
        String line;
        while ((line=in.readLine()) != null) {
            line = obfuscate(line);
            out.write(line);
            out.write('\n');
        }
        in.close();
        out.close();
    }

    private static String obfuscate(String email) throws Exception {
        try {
            int positionAt = email.indexOf('@');
            int positionLastDot = email.lastIndexOf('.');
            StringBuffer sb = new StringBuffer();
            sb.append(email.charAt(0));
            for (int i=1; i < positionAt; i++)
                sb.append('*');
            sb.append('@');
            sb.append(email.charAt(positionAt+1));
            for (int i = positionAt+2; i < positionLastDot-1; i++)
                sb.append('*');
            for (int i = positionLastDot-1; i < email.length(); i++)
                sb.append(email.charAt(i));
            if (email.equals(sb.toString()))
                System.err.println("zadna obfuscace pro "+email);
            return sb.toString();
        } catch (Exception e) {
            System.err.println("chyba pro "+email);
            throw e;
        }
    }
}
