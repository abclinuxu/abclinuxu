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
package cz.abclinuxu.servlets.ws;

import cz.abclinuxu.exceptions.DuplicateKeyException;
import cz.abclinuxu.exceptions.InvalidInputException;
import cz.abclinuxu.exceptions.LdapException;
import cz.abclinuxu.exceptions.AccessDeniedException;
import cz.abclinuxu.persistence.ldap.LdapUserManager;
import cz.abclinuxu.data.User;
import cz.abclinuxu.servlets.html.edit.EditUser;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.AbcException;

import javax.jws.WebService;
import javax.jws.WebParam;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

/**
 * Implementation of UserAccountService interface.
 * @author literakl
 * @since 20.4.2008
 */

@WebService(endpointInterface = "cz.abclinuxu.servlets.ws.UserAccountService", serviceName = "users")
public class UserAccountServiceImpl implements UserAccountService, Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserAccountServiceImpl.class);

    public static final String PREF_KNOWN_CLIENTS = "known.clients";
    public static final String PREF_CLIENT_PREFIX = "client.";
    public static final String PREF_CLENT_ID_SUFFIX = ".id";
    public static final String PREF_CLENT_PASSWORD_SUFFIX = ".password";

    LdapUserManager mgr = LdapUserManager.getInstance();
    Map<String, ClientInfo> clients = new HashMap<String, ClientInfo>();

    public void registerUser(@WebParam(name = "login") String login, @WebParam(name = "password") String password,
                             @WebParam(name = "openId") String openId, @WebParam(name = "name") String name,
                             @WebParam(name = "portal") String portal, @WebParam(name = "portalPassword") String portalPassword)
            throws DuplicateKeyException, InvalidInputException, AccessDeniedException, LdapException {
        try {
            verifyAccess(portal, portalPassword);
            if (log.isDebugEnabled())
                log.debug("registerUser(" + login + ", ***, " + openId + ", " + name + ")");

            mgr.registerUser(login, password, openId, name, portal);
        } catch (AbcException e) {
            log.warn("Chyba webové služby", e);
            throw e;
        } catch (Exception e) {
            log.error("Chyba webové služby", e);
            throw new AbcException("Neočekávaná chyba " + e.getMessage(), e);
        }
    }

    public boolean login(@WebParam(name = "login") String login, @WebParam(name = "password") String password,
                         @WebParam(name = "portal") String portal, @WebParam(name = "portalPassword") String portalPassword)
            throws AccessDeniedException, LdapException {
        try {
            verifyAccess(portal, portalPassword);
            if (log.isDebugEnabled())
                log.debug("login(" + login + ")");

            return mgr.login(login, password, portal);
        } catch (AbcException e) {
            log.warn("Chyba webové služby", e);
            throw e;
        } catch (Exception e) {
            log.error("Chyba webové služby", e);
            throw new AbcException("Neočekávaná chyba " + e.getMessage(), e);
        }
    }

    public boolean loginWithPasswordHash(@WebParam(name = "login") String login, @WebParam(name = "passwordHash") int passwordHash,
                                         @WebParam(name = "portal") String portal, @WebParam(name = "portalPassword") String portalPassword)
            throws AccessDeniedException, LdapException {
        try {
            verifyAccess(portal, portalPassword);
            if (log.isDebugEnabled())
                log.debug("loginWithPasswordHash(" + login + ")");

            return mgr.loginWithPasswordHash(login, passwordHash, portal);
        } catch (AbcException e) {
            log.warn("Chyba webové služby", e);
            throw e;
        } catch (Exception e) {
            log.error("Chyba webové služby", e);
            throw new AbcException("Neočekávaná chyba " + e.getMessage(), e);
        }
    }

    public void changePassword(@WebParam(name = "login") String login, @WebParam(name = "password") String password,
                               @WebParam(name = "portal") String portal, @WebParam(name = "portalPassword") String portalPassword)
            throws AccessDeniedException, LdapException, InvalidInputException {
        try {
            verifyAccess(portal, portalPassword);
            if (log.isDebugEnabled())
                log.debug("changePassword(" + login + ")");

            mgr.changePassword(login, password);
        } catch (AbcException e) {
            log.warn("Chyba webové služby", e);
            throw e;
        } catch (Exception e) {
            log.error("Chyba webové služby", e);
            throw new AbcException("Neočekávaná chyba " + e.getMessage(), e);
        }
    }

    public void resetPassword(@WebParam(name = "login") String login, @WebParam(name = "forgottenPasswordToken") String forgottenPasswordToken,
                              @WebParam(name = "password") String password, @WebParam(name = "portal") String portal,
                              @WebParam(name = "portalPassword") String portalPassword)
            throws AccessDeniedException, LdapException, InvalidInputException {
        try {
            verifyAccess(portal, portalPassword);
            if (log.isDebugEnabled())
                log.debug("changePassword(" + login + ")");

            mgr.resetPassword(login, forgottenPasswordToken, password);
        } catch (AbcException e) {
            log.warn("Chyba webové služby", e);
            throw e;
        } catch (Exception e) {
            log.error("Chyba webové služby", e);
            throw new AbcException("Neočekávaná chyba " + e.getMessage(), e);
        }
    }

    public void updateUserMap(@WebParam(name = "login") String login, Map<String, String> values,
                           @WebParam(name = "portal") String portal, @WebParam(name = "portalPassword") String portalPassword)
            throws AccessDeniedException, LdapException, InvalidInputException {
        try {
            verifyAccess(portal, portalPassword);
            if (log.isDebugEnabled())
                log.debug("updateUser(" + login + ")" + values.toString());

            // todo odstranit volani EditUser, presunout kontroly primo sem, hlavne zadne HTML
            User user = new User();
            user.setData("<data/>");
            Map env = new HashMap();
            env.put(Constants.VAR_ERRORS, new HashMap());

            String tmp = values.get(LdapUserManager.ATTRIB_CITY);
            if (tmp != null) {
                if (!EditUser.setCity(Collections.singletonMap(EditUser.PARAM_CITY, tmp), user, env))
                    throw createException(EditUser.PARAM_CITY, env, LdapUserManager.ATTRIB_CITY);
            }
            tmp = values.get(LdapUserManager.ATTRIB_COUNTRY);
            if (tmp != null) {
                if (!EditUser.setCountry(Collections.singletonMap(EditUser.PARAM_COUNTRY, tmp), user, env))
                    throw createException(EditUser.PARAM_COUNTRY, env, LdapUserManager.ATTRIB_COUNTRY);
            }
            tmp = values.get(LdapUserManager.ATTRIB_EMAIL_ADRESS);
            if (tmp != null) {
                if (!EditUser.setEmail(Collections.singletonMap(EditUser.PARAM_EMAIL, tmp), user, env))
                    throw createException(EditUser.PARAM_EMAIL, env, LdapUserManager.ATTRIB_EMAIL_ADRESS);
            }
            tmp = values.get(LdapUserManager.ATTRIB_HOME_PAGE_URL);
            if (tmp != null) {
                if (!EditUser.setMyPage(Collections.singletonMap(EditUser.PARAM_HOME_PAGE, tmp), user, env))
                    throw createException(EditUser.PARAM_HOME_PAGE, env, LdapUserManager.ATTRIB_HOME_PAGE_URL);
            }
            tmp = values.get(LdapUserManager.ATTRIB_NAME);
            if (tmp != null) {
                if (!EditUser.setName(Collections.singletonMap(EditUser.PARAM_NAME, tmp), user, env))
                    throw createException(EditUser.PARAM_NAME, env, LdapUserManager.ATTRIB_NAME);
            }
            tmp = values.get(LdapUserManager.ATTRIB_OPEN_ID);
            if (tmp != null) {
                if (!EditUser.setOpenId(Collections.singletonMap(EditUser.PARAM_OPEN_ID, tmp), user, env))
                    throw createException(EditUser.PARAM_OPEN_ID, env, LdapUserManager.ATTRIB_OPEN_ID);
            }
            tmp = values.get(LdapUserManager.ATTRIB_SEX);
            if (tmp != null && tmp.length() > 0) {
                if (!EditUser.setSex(Collections.singletonMap(EditUser.PARAM_SEX, tmp), user, env))
                    throw createException(EditUser.PARAM_SEX, env, LdapUserManager.ATTRIB_SEX);
            }
            mgr.updateUser(login, values);
        } catch (AbcException e) {
            log.warn("Chyba webové služby", e);
            throw e;
        } catch (Exception e) {
            log.error("Chyba webové služby", e);
            throw new AbcException("Neočekávaná chyba " + e.getMessage(), e);
        }
    }

    public void updateUser(@WebParam(name = "acount") UserAccount account, @WebParam(name = "portal") String portal,
                           @WebParam(name = "portalPassword") String portalPassword)
            throws AccessDeniedException, LdapException, InvalidInputException {
        try {
            verifyAccess(portal, portalPassword);
            if (log.isDebugEnabled())
                log.debug("updateUser(" + account.getLogin() + ")" + account.toString());

            // todo odstranit volani EditUser, presunout kontroly primo sem, hlavne zadne HTML
            User user = new User();
            user.setData("<data/>");
            Map<String, String> userMap = new HashMap<String, String>();
            Map env = new HashMap();
            env.put(Constants.VAR_ERRORS, new HashMap());

            String  tmp;
            tmp = account.getCity();
            if (tmp != null) {
                if (! EditUser.setCity(Collections.singletonMap(EditUser.PARAM_CITY, tmp), user, env))
                    throw createException(EditUser.PARAM_CITY, env, LdapUserManager.ATTRIB_CITY);
                userMap.put(LdapUserManager.ATTRIB_CITY, tmp);
            }
            tmp = account.getCountry();
            if (tmp != null) {
                if (!EditUser.setCountry(Collections.singletonMap(EditUser.PARAM_COUNTRY, tmp), user, env))
                    throw createException(EditUser.PARAM_COUNTRY, env, LdapUserManager.ATTRIB_COUNTRY);
                userMap.put(LdapUserManager.ATTRIB_COUNTRY, tmp);
            }
            tmp = account.getDeliveryAddressCity();
            if (tmp != null) {
                userMap.put(LdapUserManager.ATTRIB_DELIVERY_ADDRESS_CITY, tmp);
            }
            tmp = account.getDeliveryAddressCountry();
            if (tmp != null) {
                userMap.put(LdapUserManager.ATTRIB_DELIVERY_ADDRESS_COUNTRY, tmp);
            }
            tmp = account.getDeliveryAddressName();
            if (tmp != null) {
                userMap.put(LdapUserManager.ATTRIB_DELIVERY_ADDRESS_NAME, tmp);
            }
            tmp = account.getDeliveryAddressStreet();
            if (tmp != null) {
                userMap.put(LdapUserManager.ATTRIB_DELIVERY_ADDRESS_STREET, tmp);
            }
            tmp = account.getDeliveryAddressZIP();
            if (tmp != null) {
                userMap.put(LdapUserManager.ATTRIB_DELIVERY_ADDRESS_ZIP, tmp);
            }
            tmp = account.getEmailAddress();
            if (tmp != null) {
                if (! EditUser.setEmail(Collections.singletonMap(EditUser.PARAM_EMAIL, tmp), user, env))
                    throw createException(EditUser.PARAM_EMAIL, env, LdapUserManager.ATTRIB_EMAIL_ADRESS);
                userMap.put(LdapUserManager.ATTRIB_EMAIL_ADRESS, tmp);
            }
            tmp = account.getEmailBlocked();
            if (tmp != null) {
                userMap.put(LdapUserManager.ATTRIB_EMAIL_BLOCKED, tmp);
            }
            tmp = account.getEmailVerified();
            if (tmp != null) {
                userMap.put(LdapUserManager.ATTRIB_EMAIL_VERIFIED, tmp);
            }
            tmp = account.getForgottenPasswordToken();
            if (tmp != null) {
                userMap.put(LdapUserManager.ATTRIB_FORGOTTEN_PASSWORD_TOKEN, tmp);
            }
            tmp = account.getHomepageURL();
            if (tmp != null) {
                if (! EditUser.setMyPage(Collections.singletonMap(EditUser.PARAM_HOME_PAGE, tmp), user, env))
                    throw createException(EditUser.PARAM_HOME_PAGE, env, LdapUserManager.ATTRIB_HOME_PAGE_URL);
                userMap.put(LdapUserManager.ATTRIB_HOME_PAGE_URL, tmp);
            }
            tmp = account.getInvoicingAddressCity();
            if (tmp != null) {
                userMap.put(LdapUserManager.ATTRIB_INVOICING_ADDRESS_CITY, tmp);
            }
            tmp = account.getInvoicingCompany();
            if (tmp != null) {
                userMap.put(LdapUserManager.ATTRIB_INVOICING_COMPANY, tmp);
            }
            tmp = account.getInvoicingAddressCountry();
            if (tmp != null) {
                userMap.put(LdapUserManager.ATTRIB_INVOICING_ADDRESS_COUNTRY, tmp);
            }
            tmp = account.getInvoicingCompanyDIC();
            if (tmp != null) {
                userMap.put(LdapUserManager.ATTRIB_INVOICING_COMPANY_DIC, tmp);
            }
            tmp = account.getInvoicingCompanyICO();
            if (tmp != null) {
                userMap.put(LdapUserManager.ATTRIB_INVOICING_COMPANY_ICO, tmp);
            }
            tmp = account.getInvoicingAddressName();
            if (tmp != null) {
                userMap.put(LdapUserManager.ATTRIB_INVOICING_ADDRESS_NAME, tmp);
            }
            tmp = account.getInvoicingAddressStreet();
            if (tmp != null) {
                userMap.put(LdapUserManager.ATTRIB_INVOICING_ADDRESS_STREET, tmp);
            }
            tmp = account.getInvoicingAddressZIP();
            if (tmp != null) {
                userMap.put(LdapUserManager.ATTRIB_INVOICING_ADDRESS_ZIP, tmp);
            }
            tmp = account.getName();
            if (tmp != null) {
                if (! EditUser.setName(Collections.singletonMap(EditUser.PARAM_NAME, tmp), user, env))
                    throw createException(EditUser.PARAM_NAME, env, LdapUserManager.ATTRIB_NAME);
                userMap.put(LdapUserManager.ATTRIB_NAME, tmp);
            }
            tmp = account.getOpenID();
            if (tmp != null) {
                if (! EditUser.setOpenId(Collections.singletonMap(EditUser.PARAM_OPEN_ID, tmp), user, env))
                    throw createException(EditUser.PARAM_OPEN_ID, env, LdapUserManager.ATTRIB_OPEN_ID);
                userMap.put(LdapUserManager.ATTRIB_OPEN_ID, tmp);
            }
            tmp = account.getPasswordAnswer();
            if (tmp != null) {
                userMap.put(LdapUserManager.ATTRIB_PASSWORD_ANSWEAR, tmp);
            }
            tmp = account.getPasswordQuestion();
            if (tmp != null) {
                userMap.put(LdapUserManager.ATTRIB_PASSWORD_QUESTION, tmp);
            }
            tmp = account.getPhone();
            if (tmp != null) {
                userMap.put(LdapUserManager.ATTRIB_PHONE, tmp);
            }
            tmp = account.getSex();
            if (tmp != null && tmp.length() > 0) {
                if (! EditUser.setSex(Collections.singletonMap(EditUser.PARAM_SEX, tmp), user, env))
                    throw createException(EditUser.PARAM_SEX, env, LdapUserManager.ATTRIB_SEX);
                userMap.put(LdapUserManager.ATTRIB_SEX, tmp);
            }
            mgr.updateUser(account.getLogin(), userMap);
        } catch (AbcException e) {
            log.warn("Chyba webové služby", e);
            throw e;
        } catch (Exception e) {
            log.error("Chyba webové služby", e);
            throw new AbcException("Neočekávaná chyba " + e.getMessage(), e);
        }
    }

    public UserAccount getUserInformation(@WebParam(name = "login") String login, @WebParam(name = "attributes") String[] attributes,
                                          @WebParam(name = "portal") String portal, @WebParam(name = "portalPassword") String portalPassword)
            throws AccessDeniedException, LdapException {
        try {
            verifyAccess(portal, portalPassword);
            if (log.isDebugEnabled())
                log.debug("getUserInformation(" + login + ")");

            Map<String, String> map = mgr.getUserInformation(login, attributes);
            UserAccount account = new UserAccount();
            account.setCity(map.get(LdapUserManager.ATTRIB_CITY));
            account.setLogin(map.get(LdapUserManager.ATTRIB_LOGIN));
            account.setCountry(map.get(LdapUserManager.ATTRIB_COUNTRY));
            account.setDeliveryAddressCity(map.get(LdapUserManager.ATTRIB_DELIVERY_ADDRESS_CITY));
            account.setDeliveryAddressCountry(map.get(LdapUserManager.ATTRIB_DELIVERY_ADDRESS_COUNTRY));
            account.setDeliveryAddressName(map.get(LdapUserManager.ATTRIB_DELIVERY_ADDRESS_NAME));
            account.setDeliveryAddressStreet(map.get(LdapUserManager.ATTRIB_DELIVERY_ADDRESS_STREET));
            account.setDeliveryAddressZIP(map.get(LdapUserManager.ATTRIB_DELIVERY_ADDRESS_ZIP));
            account.setEmailAddress(map.get(LdapUserManager.ATTRIB_EMAIL_ADRESS));
            account.setEmailBlocked(map.get(LdapUserManager.ATTRIB_EMAIL_BLOCKED));
            account.setEmailVerified(map.get(LdapUserManager.ATTRIB_EMAIL_VERIFIED));
            account.setForgottenPasswordToken(map.get(LdapUserManager.ATTRIB_FORGOTTEN_PASSWORD_TOKEN));
            account.setHomepageURL(map.get(LdapUserManager.ATTRIB_HOME_PAGE_URL));
            account.setInvoicingAddressCity(map.get(LdapUserManager.ATTRIB_INVOICING_ADDRESS_CITY));
            account.setInvoicingAddressCountry(map.get(LdapUserManager.ATTRIB_INVOICING_ADDRESS_COUNTRY));
            account.setInvoicingAddressName(map.get(LdapUserManager.ATTRIB_INVOICING_ADDRESS_NAME));
            account.setInvoicingAddressStreet(map.get(LdapUserManager.ATTRIB_INVOICING_ADDRESS_STREET));
            account.setInvoicingAddressZIP(map.get(LdapUserManager.ATTRIB_INVOICING_ADDRESS_ZIP));
            account.setInvoicingCompany(map.get(LdapUserManager.ATTRIB_INVOICING_COMPANY));
            account.setInvoicingCompanyDIC(map.get(LdapUserManager.ATTRIB_INVOICING_COMPANY_DIC));
            account.setInvoicingCompanyICO(map.get(LdapUserManager.ATTRIB_INVOICING_COMPANY_ICO));
            account.setLastLoginDate(map.get(LdapUserManager.ATTRIB_LAST_LOGIN_DATE));
            account.setName(map.get(LdapUserManager.ATTRIB_NAME));
            account.setOpenID(map.get(LdapUserManager.ATTRIB_OPEN_ID));
            account.setPasswordAnswer(map.get(LdapUserManager.ATTRIB_PASSWORD_ANSWEAR));
            account.setPasswordQuestion(map.get(LdapUserManager.ATTRIB_PASSWORD_QUESTION));
            account.setPhone(map.get(LdapUserManager.ATTRIB_PHONE));
            account.setRegistrationDate(map.get(LdapUserManager.ATTRIB_REGISTRATION_DATE));
            account.setSex(map.get(LdapUserManager.ATTRIB_SEX));
            return account;
        } catch (AbcException e) {
            log.warn("Chyba webové služby", e);
            throw e;
        } catch (Exception e) {
            log.error("Chyba webové služby", e);
            throw new AbcException("Neočekávaná chyba " + e.getMessage(), e);
        }
    }

    private void verifyAccess(String portalId, String portalPassword) throws AccessDeniedException {
        if (log.isDebugEnabled())
            log.debug("verify access for " + portalId);
        if (portalId == null)
            throw new AccessDeniedException("Přístup ke službě odepřen - chybí identifikace portálu!", false);
        ClientInfo client = clients.get(portalId.toLowerCase());
        if (client == null ||  ! client.password.equals(portalPassword))
            throw new AccessDeniedException("Přístup ke službě odepřen!", false);
        if (log.isDebugEnabled())
            log.debug("access verified for " + portalId);
    }

    private InvalidInputException createException(String param, Map env, String ldapAttribute) {
        Map errors = (Map) env.get(Constants.VAR_ERRORS);
        String message = (String) errors.get(param);
        return new InvalidInputException((message != null) ? message : "Nevalidní vstup pro atribut " + ldapAttribute);
    }

    public UserAccountServiceImpl() {
        ConfigurationManager.getConfigurator().configureAndRememberMe(this);
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        String ids = prefs.get(PREF_KNOWN_CLIENTS, "");
        StringTokenizer stk = new StringTokenizer(ids, ",");
        while (stk.hasMoreTokens()) {
            String portal = stk.nextToken();
            String id = prefs.get(PREF_CLIENT_PREFIX + portal + PREF_CLENT_ID_SUFFIX, null).toLowerCase();
            String password = prefs.get(PREF_CLIENT_PREFIX + portal + PREF_CLENT_PASSWORD_SUFFIX, null);
            clients.put(id, new ClientInfo(id, password));
        }
    }

    private static class ClientInfo {
        String id, password;

        public ClientInfo(String id, String password) {
            this.id = id;
            this.password = password;
        }
    }
}
