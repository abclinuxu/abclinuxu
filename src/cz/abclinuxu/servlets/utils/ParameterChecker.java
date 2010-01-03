/*
 *  Copyright (C) 2009 Leos Literak
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

package cz.abclinuxu.servlets.utils;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.parser.clean.HtmlChecker;
import cz.abclinuxu.utils.parser.clean.HtmlPurifier;
import cz.abclinuxu.utils.parser.clean.Rules;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;
import javax.mail.internet.InternetAddress;

/**
 *
 * @author lubos
 */
public class ParameterChecker {
    Map<String,Object> env;
    Map<String,Object> params;
    boolean failed;

    public ParameterChecker(Map<String,Object> env) {
        this.env = env;
        params = (Map<String,Object>) env.get(Constants.VAR_PARAMS);
    }

    /**
     * @return true if any of fields contains an invalid value
     */
    public boolean isFailed() {
        return failed;
    }

    /**
     * Adds an error message and sets the flag that an invalid value was encountered
     * @param field Which field contains an invalid value
     * @param message Error message
     */
    public void addError(String field, String message) {
        ServletUtils.addError(field, message, env, null);
        failed = true;
    }

    /**
     * Gets a String value from the form
     * @param field Field name
     * @return an empty string if no or empty string is found
     */
    public String getString(String field) {
        return getString(field, false);
    }

    public String getString(String field, boolean allowEmpty) {
        String value = (String) params.get(field);
        if (value != null)
            value = value.trim();
        
        if (value == null || value.isEmpty()) {
            if (!allowEmpty)
                addError(field, "Zadejte text!");
            return "";
        }

        return Misc.filterDangerousCharacters(value);
    }

    /**
     * Gets and checks a regular expression
     * @param field Field name
     * @param allowEmpty Whether or not consider an empty string as an error
     * @return A regular expression, never returns a null
     */
    public String getRegexp(String field, boolean allowEmpty) {
        String value = getString(field, true);
        if (value.isEmpty()) {
            if (!allowEmpty)
                addError(field, "Zadejte regulární výraz!");
            return value;
        }

        try {
            Pattern.compile(value);
        } catch (Exception e) {
            addError(field, "Regulární výraz je neplatný");
            return "";
        }
        return value;
    }

    /**
     * Gets an integer value from a form field
     * @param field Field name
     * @param defaultToZero Whether to default to zero if an empty String is found
     * @return An integer
     */
    public int getInteger(String field, boolean defaultToZero) {
        String value = getString(field, true);
        if (Misc.empty(value)) {
            if (defaultToZero)
                return 0;
            else {
                addError(field, "Zadejte číslo!");
                return 0;
            }
        }

        try {
            int ivalue = Integer.parseInt(value);
            return ivalue;
        } catch (Exception e) {
            addError(field, "Zadejte číslo!");
            return 0;
        }
    }

    /**
     * Gets an e-mail address from a form field
     * @param field Field name
     * @param allowEmpty Permit no e-mail address
     * @return A validated e-mail address or an empty string
     */
    public String getEmail(String field, boolean allowEmpty) {
        String value = getString(field, true);
        if (value.isEmpty()) {
            if (!allowEmpty)
                addError(field, "Zadejte platnou e-mailovou adresu!");
            
            return value;
        }

        try {
            InternetAddress.parse(value);
        } catch (Exception e) {
            addError(field, "Zadejte platnou e-mailovou adresu!");
            return "";
        }
        
        return value;
    }

    /**
     * Gets a valid Date from a form field in "isoFormat"
     * @param field Field name
     * @param allowEmpty Whether not fail on an empty field value
     * @return A Date or null
     */
    public Date getDate(String field, boolean allowEmpty) {
        String value = getString(field, true);
        if (value.isEmpty())
            return null;

        try {
            Date date;
            synchronized (Constants.isoFormat) {
                date = Constants.isoFormat.parse(value);
            }
            return date;
        } catch (Exception e) {
            addError(field, "Zadejte platné datum ve správném formátu!");
            return null;
        }
    }

    public String getHtml(String field, boolean allowEmpty, Rules rules) {
        String text = getString(field, allowEmpty);
        if (Misc.empty(text))
            return text;

        try {
            text = HtmlPurifier.clean(text);
            if (rules != null)
                HtmlChecker.check(rules, text);
            return text;
        } catch (Exception e) {
            addError(field, e.getMessage());
            return "";
        }
    }
}
