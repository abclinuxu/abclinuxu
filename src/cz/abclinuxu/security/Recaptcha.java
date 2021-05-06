/*
 *  Copyright (C) 2018 Lubos Dolezel
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
package cz.abclinuxu.security;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author lubos
 */
public class Recaptcha implements Configurable {
    public static final String PREF_RECAPTCHA_KEY = "recaptcha.key";
    public static final String PREF_RECAPTCHA_SECRET = "recaptcha.secret";
    
    private static final String RECAPTCHA_URL = "https://www.google.com/recaptcha/api/siteverify";
    private static final org.apache.log4j.Logger securityLog = org.apache.log4j.Logger.getLogger("security");
    
    private static final Recaptcha instance = new Recaptcha();
    private String recaptchaKey, recaptchaSecret;
    private final JSONParser jsonParser = new JSONParser();
    
    static {
        ConfigurationManager.getConfigurator().configureAndRememberMe(instance);
    }

    @Override
    public void configure(Preferences prefs) throws ConfigurationException {
        recaptchaKey = prefs.get(PREF_RECAPTCHA_KEY, "");
        recaptchaSecret = prefs.get(PREF_RECAPTCHA_SECRET, "");
    }
    
    public static Recaptcha getInstance() {
        return instance;
    }
    
    public void verifyAccess(HttpServletRequest request) throws SecurityException {
        try {
            String remoteAddress = ServletUtils.getClientIPAddress(request);
            Map<String,?> env = (Map) request.getAttribute(Constants.VAR_ENVIRONMENT);
            Map<String,?> params = (Map) env.get(Constants.VAR_PARAMS);
            
            String response = (String) params.get("g-recaptcha-response");
            if (Misc.empty(response))
            {
                securityLog.warn("Chybejici hodnota recaptcha 'g-recaptcha-response'");
                throw new SecurityException("Chybějící odpověď recaptcha");
            }
            
            URL obj = new URL(RECAPTCHA_URL);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
            
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            StringBuilder sb = new StringBuilder();
            
            sb.append("secret=").append(recaptchaSecret);
            sb.append("&response=").append(URLEncoder.encode(response, "UTF-8"));
            sb.append("&remoteip=").append(remoteAddress);
            
            wr.writeBytes(sb.toString());
            wr.flush();
            wr.close();
            
            int responseCode = con.getResponseCode();
            if (responseCode != 200) {
                securityLog.error("Unexpected reCAPTCHA response code: " + responseCode);
                return;
            }
            
            byte[] data = IOUtils.toByteArray(con.getInputStream());
            JSONObject result;
            
            synchronized (jsonParser) {
                result = (JSONObject) jsonParser.parse(new String(data));
            }
            
            Boolean success = (Boolean) result.get("success");
            if (success != null && !success) {
                securityLog.warn("Nespravne reseni reCAPTCHA");
                throw new SecurityException("Nesprávné řešení reCAPTCHA");
            }
            
        } catch (IOException ex) {
            securityLog.error("Failed to process reCAPTCHA", ex);
        } catch (ParseException ex) {
            securityLog.error("Failed to process reCAPTCHA", ex);
	}
    }
    
    public String getKey() {
        return recaptchaKey;
    }
    
}
