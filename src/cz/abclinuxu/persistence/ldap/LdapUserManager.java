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
package cz.abclinuxu.persistence.ldap;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.exceptions.DuplicateKeyException;
import cz.abclinuxu.exceptions.InvalidInputException;
import cz.abclinuxu.exceptions.LdapException;

import javax.naming.NamingException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NameAlreadyBoundException;
import javax.naming.AuthenticationException;
import javax.naming.InvalidNameException;
import javax.naming.directory.DirContext;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.prefs.Preferences;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

/**
 * Interface for communicating with LDAP user management.
 * @author literakl
 * @since 15.3.2008
 */
public class LdapUserManager implements Configurable {
    static Logger log = Logger.getLogger(LdapUserManager.class);

    public static final String PREF_SERVER_URI = "server.uri";
    public static final String PREF_ADMIN_USER = "admin.user";
    public static final String PREF_ADMIN_PASSWORD = "admin.password";
    public static final String PREF_SUPPORTED_PORTALS = "supported.portals";
    public static final String PREF_PARENT_CONTEXT = "parent.context";
    public static final String PREF_LDAP_CLASS = "user.ldap.class";
    public static final String PREF_INVALID_LOGIN_REGEXP = "regexp.invalid.login";
    public static final String PREF_CONNECTION_PROPERTIES = "admin.connection.properties.";
    public static final String PREF_PROPERTY_COUNT = "count";
    public static final String PREF_PROPERTY_KEY = "key";
    public static final String PREF_PROPERTY_VALUE = "value";

    // any change to attributes must be performed in stickfish.schema and UserAccount.java too

    public static final String ATTRIB_CITY = "city";
    public static final String ATTRIB_COUNTRY = "country";
    public static final String ATTRIB_DELIVERY_ADDRESS_CITY = "deliveryAddressCity";
    public static final String ATTRIB_DELIVERY_ADDRESS_COUNTRY = "deliveryAddressCountry";
    public static final String ATTRIB_DELIVERY_ADDRESS_NAME = "deliveryAddressName";
    public static final String ATTRIB_DELIVERY_ADDRESS_STREET = "deliveryAddressStreet";
    public static final String ATTRIB_DELIVERY_ADDRESS_ZIP = "deliveryAddressZIP";
    public static final String ATTRIB_EMAIL_ADRESS = "emailAddress";
    public static final String ATTRIB_EMAIL_BLOCKED = "emailBlocked";
    public static final String ATTRIB_EMAIL_VERIFICATION_TOKEN = "emailVerificationToken";
    public static final String ATTRIB_EMAIL_VERIFIED = "emailVerified";
    public static final String ATTRIB_FORGOTTEN_PASSWORD_TOKEN = "forgottenPasswordToken";
    public static final String ATTRIB_HOME_PAGE_URL = "homepageURL";
    public static final String ATTRIB_INVOICING_ADDRESS_CITY = "invoicingAddressCity";
    public static final String ATTRIB_INVOICING_COMPANY = "invoicingCompany";
    public static final String ATTRIB_INVOICING_ADDRESS_COUNTRY = "invoicingAddressCountry";
    public static final String ATTRIB_INVOICING_COMPANY_DIC = "invoicingCompanyDIC";
    public static final String ATTRIB_INVOICING_COMPANY_ICO = "invoicingCompanyICO";
    public static final String ATTRIB_INVOICING_ADDRESS_NAME = "invoicingAddressName";
    public static final String ATTRIB_INVOICING_ADDRESS_STREET = "invoicingAddressStreet";
    public static final String ATTRIB_INVOICING_ADDRESS_ZIP = "invoicingAddressZIP";
    public static final String ATTRIB_LAST_CHANGE_DATE = "lastChangeDate";
    public static final String ATTRIB_LAST_LOGIN_DATE = "lastLoginDate";
    public static final String ATTRIB_LOGIN = "cn";
    public static final String ATTRIB_NAME = "sn";
    public static final String ATTRIB_OPEN_ID = "openID";
    public static final String ATTRIB_PASSWORD = "userPassword";
    public static final String ATTRIB_PASSWORD_ANSWEAR = "passwordAnswer";
    public static final String ATTRIB_PASSWORD_HASHCODE = "passwordHash";
    public static final String ATTRIB_PASSWORD_QUESTION = "passwordQuestion";
    public static final String ATTRIB_PHONE = "telephoneNumber";
    public static final String ATTRIB_REGISTRATION_PORTAL = "registrationPortalID";
    public static final String ATTRIB_REGISTRATION_DATE = "registrationDate";
    public static final String ATTRIB_SALUTATION = "salutation";
    public static final String ATTRIB_SEX = "sex";
    public static final String ATTRIB_VISITED_PORTAL = "visitedPortalID";

    public static final String SERVER_ABCLINUXU = "www.abclinuxu.cz";
    public static final String SERVER_64BIT = "www.64bit.cz";

    public static final String SF_USER_ALL_ATTRIBUTES[] = {
        ATTRIB_CITY, ATTRIB_COUNTRY, ATTRIB_DELIVERY_ADDRESS_CITY, ATTRIB_DELIVERY_ADDRESS_COUNTRY,
        ATTRIB_DELIVERY_ADDRESS_NAME, ATTRIB_DELIVERY_ADDRESS_STREET, ATTRIB_DELIVERY_ADDRESS_ZIP,
        ATTRIB_EMAIL_ADRESS, ATTRIB_EMAIL_BLOCKED, ATTRIB_EMAIL_VERIFICATION_TOKEN, ATTRIB_EMAIL_VERIFIED,
        ATTRIB_FORGOTTEN_PASSWORD_TOKEN, ATTRIB_HOME_PAGE_URL, ATTRIB_INVOICING_ADDRESS_CITY, ATTRIB_INVOICING_COMPANY,
        ATTRIB_INVOICING_ADDRESS_COUNTRY, ATTRIB_INVOICING_COMPANY_DIC, ATTRIB_INVOICING_COMPANY_ICO,
        ATTRIB_INVOICING_ADDRESS_NAME, ATTRIB_INVOICING_ADDRESS_STREET, ATTRIB_INVOICING_ADDRESS_ZIP,
        ATTRIB_LAST_CHANGE_DATE, ATTRIB_LAST_LOGIN_DATE, ATTRIB_LOGIN, ATTRIB_NAME, ATTRIB_OPEN_ID, ATTRIB_PASSWORD_ANSWEAR,
        ATTRIB_PASSWORD_HASHCODE, ATTRIB_PASSWORD_QUESTION, ATTRIB_PHONE,
        ATTRIB_REGISTRATION_DATE, ATTRIB_REGISTRATION_PORTAL, ATTRIB_SALUTATION, ATTRIB_SEX, ATTRIB_VISITED_PORTAL
    };
    private static final String SF_USER_VISITED_PORTAL[] = new String[]{ATTRIB_VISITED_PORTAL};
    private static final String SF_USER_LOGIN[] = new String[]{ATTRIB_NAME, ATTRIB_LOGIN, ATTRIB_PASSWORD_HASHCODE};

    private static LdapUserManager instance = new LdapUserManager();

    private static final Set<String> MODIFIABLE_ATTRIBUTES = new HashSet<String>();

    static {
        for (String attr : SF_USER_ALL_ATTRIBUTES) {
            MODIFIABLE_ATTRIBUTES.add(attr);
        }
        MODIFIABLE_ATTRIBUTES.remove(ATTRIB_LOGIN);
//        MODIFIABLE_ATTRIBUTES.remove(ATTRIB_LAST_LOGIN_DATE);
//        MODIFIABLE_ATTRIBUTES.remove(ATTRIB_REGISTRATION_DATE);
//        MODIFIABLE_ATTRIBUTES.remove(ATTRIB_REGISTRATION_PORTAL);
//        MODIFIABLE_ATTRIBUTES.remove(ATTRIB_VISITED_PORTAL);
    }

    private static final String LDAP_PROVIDER = "com.sun.jndi.ldap.LdapCtxFactory";
    private static final String URL_CONTEXT_PREFIX = "com.sun.jndi.url";
    private static final String REFERRALS_IGNORE = "ignore";
    private static final String UPDATE_SECURITY_LEVEL = "simple";

    public static Pattern reLoginInvalid;

    static final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    String ldapServerUri;
    String adminUsername;
    String adminPassword;
    Set<String> portals = new HashSet<String>();
    String parentContext;
    String ldapClass;
    Map<String, String> adminConnectionEnv = new HashMap<String, String>(3, 1.0f);

    /**
     * Grants access to singleton of this object.
     * @return singleton
     */
    public static LdapUserManager getInstance() {
        return instance;
    }

    /**
     * Register new user in LDAP.
     * @param login required login. It must contain at least 3 characters and must consist of characters A-Z, digits, underscore, dash and dot.
     * @param password the password
     * @param openId optional openid key
     * @param name required user name
     * @param portal preconfigured portal id
     * @throws DuplicateKeyException either login or openid is already used
     * @throws LdapException LDAP connection error
     */
    public void registerUser(String login, String password, String openId, String name, String portal) throws DuplicateKeyException, InvalidInputException, LdapException {
        portal = checkPortal(portal);
        if (login == null || login.length() < 3)
            throw new InvalidInputException("Přihlašovací jméno musí mít nejméně tři znaky!");

        Matcher matcher = reLoginInvalid.matcher(login);
        if (matcher.find())
            throw new InvalidInputException("Přihlašovací jméno smí obsahovat pouze písmena A až Z, číslice, pomlčku, tečku a podtržítko!");

        if (password == null || password.length() < 3) // todo vratit zpet, zmena kvuli migraci
            throw new InvalidInputException("Přihlašovací heslo musí mít nejméně čtyři znaky!");

        if (name == null || name.length() < 3)
            throw new InvalidInputException("Jméno musí mít nejméně tři znaky!");

        DirContext ctx = null;
        try {
            ctx = connectLDAP(adminUsername, adminPassword, true);

            Attributes attrs = new BasicAttributes(true);
            Attribute objclass = new BasicAttribute("objectClass", ldapClass);
            objclass.add("person");
            attrs.put(objclass);

            Attribute attr = new BasicAttribute(ATTRIB_PASSWORD, password);
            attrs.put(attr);

            attr = new BasicAttribute(ATTRIB_PASSWORD_HASHCODE, Integer.toString(password.hashCode()));
            attrs.put(attr);

            if (openId != null) {
                checkDuplicateOpenId(openId, null, ctx);
                attr = new BasicAttribute(ATTRIB_OPEN_ID, openId);
                attrs.put(attr);
            }

            attr = new BasicAttribute(ATTRIB_NAME, name);
            attrs.put(attr);

            attr = new BasicAttribute(ATTRIB_REGISTRATION_PORTAL, portal);
            attrs.put(attr);

            String value;
            synchronized (isoFormat) {
                value = isoFormat.format(new Date());
            }
            attr = new BasicAttribute(ATTRIB_REGISTRATION_DATE, value);
            attrs.put(attr);

            ctx.createSubcontext(ATTRIB_LOGIN + "=" + login.toLowerCase() + "," + parentContext, attrs);
        } catch (NameAlreadyBoundException e) {
            String message = "Login " + login + " je již používán.";
            throw new DuplicateKeyException(message);
        } catch (NamingException e) {
            log.error("LDAP connection failed!", e);
            throw new LdapException("Spojení s LDAP serverem selhalo. Důvod: " + e.getMessage());
        } finally {
            if (ctx != null)
                try {
                    ctx.close();
                } catch (NamingException e) {
                    log.error("LDAP connection failed!", e);
                }
        }
    }

    /**
     * Updates LDAP information about the user.
     * @param login the login
     * @param values map, where keys are attributes. Login and some statistical attributes are forbidden to modify.
     * @throws LdapException LDAP connection error
     * @throws InvalidInputException unknown or unsupported attribute was supplied
     */
    public void updateUser(String login, Map<String, String> values) throws LdapException, InvalidInputException {
        DirContext ctx = null;
        try {
            ctx = connectLDAP(adminUsername, adminPassword, true);
            List<ModificationItem> modsList = new ArrayList<ModificationItem>();
            BasicAttribute attr;
            for (String key : values.keySet()) {
                if (!MODIFIABLE_ATTRIBUTES.contains(key))
                    throw new InvalidInputException("Atribut '" + key + "' je buď špatně zapsán, neexistuje nebo je zakázáno jej měnit!");
                if (ATTRIB_OPEN_ID.equals(key)) {
                    checkDuplicateOpenId(values.get(key), login, ctx);
                } else if (ATTRIB_EMAIL_BLOCKED.equals(key) || ATTRIB_EMAIL_VERIFIED.equals(key)) {
                    String value = values.get(key);
                    if ( value != null && ! ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)))
                        throw new InvalidInputException("Atribut '" + key + "' smí obsahovat jen hodnoty true a false!");
                }

                attr = new BasicAttribute(key, values.get(key));
                if (ATTRIB_VISITED_PORTAL.equals(key)) // TODO odstranit po migraci
                    modsList.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, attr));
                else
                    modsList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr));
            }

            if (modsList.isEmpty())
                return;

            ModificationItem[] mods = modsList.toArray(new ModificationItem[modsList.size()]);
            ctx.modifyAttributes(ATTRIB_LOGIN + "=" + login + "," + parentContext, mods);
        } catch (NamingException e) {
            log.error("LDAP connection failed!", e);
            throw new LdapException("Spojení s LDAP serverem selhalo. Důvod: " + e.getMessage());
        } finally {
            if (ctx != null)
                try {
                    ctx.close();
                } catch (NamingException e) {
                    log.error("LDAP connection failed!", e);
                }
        }
    }

    /**
     * Updates password and passwordHash.
     * @param login the login
     * @param password the password
     * @throws LdapException LDAP connection error
     * @throws InvalidInputException unknown or unsupported attribute was supplied
     */
    public void changePassword(String login, String password) throws LdapException, InvalidInputException {
        if (password == null || password.length() < 4)
            throw new InvalidInputException("Přihlašovací heslo musí mít nejméně čtyři znaky!");

        DirContext ctx = null;
        try {
            ctx = connectLDAP(adminUsername, adminPassword, true);
            List<ModificationItem> modsList = new ArrayList<ModificationItem>();
            setPassword(password, modsList);
            ModificationItem[] mods = modsList.toArray(new ModificationItem[modsList.size()]);
            ctx.modifyAttributes(ATTRIB_LOGIN + "=" + login + "," + parentContext, mods);
        } catch (NamingException e) {
            log.error("LDAP connection failed!", e);
            throw new LdapException("Spojení s LDAP serverem selhalo. Důvod: " + e.getMessage());
        } finally {
            if (ctx != null)
                try {
                    ctx.close();
                } catch (NamingException e) {
                    log.error("LDAP connection failed!", e);
                }
        }
    }

    private void setPassword(String password, List<ModificationItem> modsList) {
        BasicAttribute attr = new BasicAttribute(ATTRIB_PASSWORD, password);
        modsList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr));
        attr = new BasicAttribute(ATTRIB_PASSWORD_HASHCODE, Integer.toString(password.hashCode()));
        modsList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr));
    }

    /**
     * Updates password and passwordHash.
     * @param login the login
     * @param resetToken secret token
     * @param password the password
     * @throws LdapException LDAP connection error
     * @throws InvalidInputException unknown or unsupported attribute was supplied
     */
    public void resetPassword(String login, String resetToken, String password) throws LdapException, InvalidInputException {
        if (password == null || password.length() < 4)
            throw new InvalidInputException("Přihlašovací heslo musí mít nejméně čtyři znaky!");

        DirContext ctx = null;
        try {
            ctx = connectLDAP(adminUsername, adminPassword, true);
            Map<String, String> info = getUserInformation(login, new String[]{ATTRIB_FORGOTTEN_PASSWORD_TOKEN}, ctx);
            String realToken = info.get(ATTRIB_FORGOTTEN_PASSWORD_TOKEN);
            InvalidInputException e = null;

            List<ModificationItem> modsList = new ArrayList<ModificationItem>();
            // always remove the reset token
            BasicAttribute attr = new BasicAttribute(ATTRIB_FORGOTTEN_PASSWORD_TOKEN, null);
            modsList.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attr));

            if (realToken == null || ! realToken.equals(resetToken)) {
                e = new InvalidInputException("Token je neplatný, vygenerujte si nový!");
            } else {
                setPassword(password, modsList);
            }
            ModificationItem[] mods = modsList.toArray(new ModificationItem[modsList.size()]);

            ctx.modifyAttributes(ATTRIB_LOGIN + "=" + login + "," + parentContext, mods);

            if (e != null)
                throw e;
        } catch (NamingException e) {
            log.error("LDAP connection failed!", e);
            throw new LdapException("Spojení s LDAP serverem selhalo. Důvod: " + e.getMessage());
        } finally {
            if (ctx != null)
                try {
                    ctx.close();
                } catch (NamingException e) {
                    log.error("LDAP connection failed!", e);
                }
        }
    }

    /**
     * Verify user credentials and record that the user has successfully logged to given portal.
     * @param login user login
     * @param password the password
     * @param portal preconfigured portal id
     * @return true if login was successfull, false otherwise
     * @throws LdapException LDAP connection error
     */
    public boolean login(String login, String password, String portal) throws LdapException {
        portal = checkPortal(portal);
        DirContext ctx = null;

        try {
            try {
                ctx = connectLDAP(login, password, false); // verify password
            } catch (AuthenticationException e) {
                return false;
            } catch (InvalidNameException e) {
                return false;
            } finally {
                if (ctx != null)
                    ctx.close();
            }

            ctx = connectLDAP(adminUsername, adminPassword, true);
            recordLogin(login, portal, ctx);
            return true;
        } catch (NamingException e) {
            log.error("LDAP connection failed!", e);
            throw new LdapException("Spojení s LDAP serverem selhalo. Důvod: " + e.getMessage());
        } finally {
            if (ctx != null)
                try {
                    ctx.close();
                } catch (NamingException e) {
                    log.error("LDAP connection failed!", e);
                }
        }
    }

    /**
     * Verify user credentials (not password but only its hashcode) and record that the user has successfully logged to given portal.
     * @param login user login
     * @param passwordHash hashcode of the password
     * @param portal preconfigured portal id
     * @return true if login was successfull, false otherwise
     * @throws LdapException LDAP connection error
     */
    public boolean loginWithPasswordHash(String login, int passwordHash, String portal) throws LdapException {
        portal = checkPortal(portal);
        DirContext ctx = null;
        try {
            ctx = connectLDAP(adminUsername, adminPassword, true);

            Attributes searchAttrs = new BasicAttributes(true);
            searchAttrs.put(ATTRIB_LOGIN, login);
            searchAttrs.put(ATTRIB_PASSWORD_HASHCODE, Integer.toString(passwordHash));
            NamingEnumeration results = ctx.search(parentContext, searchAttrs, SF_USER_LOGIN);
            if (!results.hasMore())
                return false;

            SearchResult sr = (SearchResult) results.next();
            searchAttrs = sr.getAttributes();

            recordLogin(searchAttrs.get(ATTRIB_LOGIN).get(), portal, ctx);
            return true;
        } catch (NamingException e) {
            log.error("Login with '" + login + "' has failed!", e);
            throw new LdapException("Spojení s LDAP serverem selhalo. Důvod: " + e.getMessage());
        } finally {
            if (ctx != null)
                try {
                    ctx.close();
                } catch (NamingException e) {
                    log.error("LDAP connection failed!", e);
                }
        }
    }

    /**
     * Record that the user with specified openid has successfully logged to given portal.
     * @param openId openid of existing user
     * @param portal preconfigured portal id
     * @return true if openid was found
     * @throws LdapException LDAP connection error
     */
    public boolean loginWithOpenId(String openId, String portal) throws LdapException {
        portal = checkPortal(portal);
        DirContext ctx = null;
        try {
            ctx = connectLDAP(adminUsername, adminPassword, true);

            Attributes searchAttrs = new BasicAttributes(ATTRIB_OPEN_ID, openId);
            NamingEnumeration results = ctx.search(parentContext, searchAttrs, SF_USER_LOGIN);
            if (!results.hasMore())
                return false;

            SearchResult sr = (SearchResult) results.next();
            searchAttrs = sr.getAttributes();
            recordLogin(searchAttrs.get(ATTRIB_LOGIN).get(), portal, ctx);
            return true;
        } catch (NamingException e) {
            log.error("Login with '" + openId + "' has failed!", e);
            throw new LdapException("Spojení s LDAP serverem selhalo. Důvod: " + e.getMessage());
        } finally {
            if (ctx != null)
                try {
                    ctx.close();
                } catch (NamingException e) {
                    log.error("LDAP connection failed!", e);
                }
        }
    }

    /**
     * Finds user in LDAP store and returns associated information.
     * @param login
     * @param attributes
     * @return
     * @throws LdapException
     */
    public Map<String, String> getUserInformation(String login, String[] attributes) throws LdapException {
        DirContext ctx = null;
        try {
            ctx = connectLDAP(adminUsername, adminPassword, true);

            Map<String, String> result = getUserInformation(login, attributes, ctx);
            return result;
        } catch (NamingException e) {
            log.error("Get user information for '" + login + "' has failed!", e);
            throw new LdapException("Spojení s LDAP serverem selhalo. Důvod: " + e.getMessage());
        } finally {
            if (ctx != null)
                try {
                    ctx.close();
                } catch (NamingException e) {
                    log.error("LDAP connection failed!", e);
                }
        }
    }

    /**
     * Perform search using supplied query within parent context.
     * @param query LDAP query
     * @param attributes attributes to be fetched from LDAP
     * @return list of found LDAP records (as map, where key is attribute name and value is its value)
     */
    public List<Map<String, String>> search(String query, String[] attributes) throws LdapException, NamingException {
        DirContext ctx = null;
        try {
            ctx = connectLDAP(adminUsername, adminPassword, true);

            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            constraints.setReturningAttributes(attributes);
            NamingEnumeration found = ctx.search(parentContext, query, attributes, constraints);
            if (!found.hasMore())
                return Collections.emptyList();

            List<Map<String, String>> results = new ArrayList<Map<String, String>>();
            while (found.hasMore()) {
                SearchResult sr = (SearchResult) found.next();
                Attributes foundAttributes = sr.getAttributes();
                NamingEnumeration<? extends Attribute> all = foundAttributes.getAll();
                Map<String, String> item = new HashMap<String, String>();
                while (all.hasMoreElements()) {
                    Attribute attribute = all.nextElement();
                    item.put(attribute.getID(), (String) attribute.get());
                }
                if (! item.isEmpty())
                    results.add(item);
            }
            return results;
        } catch (NamingException e) {
            log.error("Search with '" + query + "' has failed!", e);
            throw new LdapException("Spojení s LDAP serverem selhalo. Důvod: " + e.getMessage());
        } finally {
            if (ctx != null)
                try {
                    ctx.close();
                } catch (NamingException e) {
                    log.error("LDAP connection failed!", e);
                }
        }
    }

    /**
     * Retrieves parent context
     * @return ldap parent context
     */
    public String getParentContext() {
        return parentContext;
    }

    /**
     * Retrieves LDAP class used to identify this user.
     * @return ldap user class
     */
    public String getLdapClass() {
        return ldapClass;
    }

    /**
     * Perform search using supplied context.
     */
    private Map<String, String> getUserInformation(String login, String[] attributes, DirContext ctx) throws LdapException, NamingException {
        Attributes searchAttrs = new BasicAttributes(ATTRIB_LOGIN, login);
        NamingEnumeration results = ctx.search(parentContext, searchAttrs, attributes);
        if (!results.hasMore())
            return Collections.emptyMap();

        SearchResult sr = (SearchResult) results.next();
        searchAttrs = sr.getAttributes();
        NamingEnumeration<? extends Attribute> all = searchAttrs.getAll();
        Map<String, String> result = new HashMap<String, String>();
        while (all.hasMoreElements()) {
            Attribute attribute = all.nextElement();
            result.put(attribute.getID(), (String) attribute.get());
        }
        return result;
    }

    /**
     * Stores information about user's log in.
     * @param login user login
     * @param portal registered portal key
     * @param ctx LDAP connection
     * @throws NamingException LDAP error
     */
    private void recordLogin(Object login, String portal, DirContext ctx) throws NamingException {
        Attributes attrs = new BasicAttributes(true);
        NamingEnumeration results;
        attrs.put(ATTRIB_LOGIN, login);
        attrs.put(ATTRIB_VISITED_PORTAL, portal);
        results = ctx.search(parentContext, attrs, SF_USER_VISITED_PORTAL);
        boolean portalAlreadyVisited = results.hasMore();

        ModificationItem[] mods = new ModificationItem[(portalAlreadyVisited) ? 1 : 2];
        String value;
        synchronized (isoFormat) {
            value = isoFormat.format(new Date());
        }

        BasicAttribute attr = new BasicAttribute(ATTRIB_LAST_LOGIN_DATE, value);
        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr);

        if (!portalAlreadyVisited) {
            attr = new BasicAttribute(ATTRIB_VISITED_PORTAL, portal);
            mods[1] = new ModificationItem(DirContext.ADD_ATTRIBUTE, attr);
        }

        ctx.modifyAttributes(ATTRIB_LOGIN + "=" + login + "," + parentContext, mods);
    }

    /**
     * Opens LDAP connection.
     * @param login cn attribute value that will be concatenated with parentContext
     * @param password the password
     * @param admin if true, login already contains full context and therefore it will be not concatenated with parentContext
     * @return LDAP connection
     * @throws NamingException LDAP error
     */
    private DirContext connectLDAP(String login, String password, boolean admin) throws NamingException {
        DirContext ctx;
        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, LDAP_PROVIDER);
        props.setProperty(Context.PROVIDER_URL, ldapServerUri);
        props.setProperty(Context.URL_PKG_PREFIXES, URL_CONTEXT_PREFIX);
        props.setProperty(Context.REFERRAL, REFERRALS_IGNORE);
        props.setProperty(Context.SECURITY_AUTHENTICATION, UPDATE_SECURITY_LEVEL);
        if (admin) {
            props.setProperty(Context.SECURITY_PRINCIPAL, login);
            // configurable connection pooling
            props.putAll(adminConnectionEnv);
        } else
            props.setProperty(Context.SECURITY_PRINCIPAL, ATTRIB_LOGIN + "=" + login + "," + parentContext);
        props.setProperty(Context.SECURITY_CREDENTIALS, password);
        ctx = new InitialDirContext(props);
        return ctx;
    }

    /**
     * Verifies that given openid key is not used by some other user. The reason is to protect agains duplicates.
     * @param openId searched openid
     * @param login optional login of modified user
     * @param ctx LDAP connection
     * @throws DuplicateKeyException some other user already has same openid key
     * @throws NamingException LDAP error
     */
    private void checkDuplicateOpenId(String openId, String login, DirContext ctx) throws DuplicateKeyException, NamingException {
        Attributes searchAttrs = new BasicAttributes(ATTRIB_OPEN_ID, openId);
        NamingEnumeration results = ctx.search(parentContext, searchAttrs, SF_USER_LOGIN);
        if (results.hasMore()) {
            SearchResult sr = (SearchResult) results.next();
            searchAttrs = sr.getAttributes();
            String existingUser = (String) searchAttrs.get(ATTRIB_LOGIN).get();
            if (login != null && login.equals(existingUser))
                return;

            String message = "Openid " + openId + " je již používán uživatelem " + existingUser;
            throw new DuplicateKeyException(message);
        }
    }

    /**
     * Checks given id against list of configured portal ids. The reason is to enforce uniform naming policy.
     * @param portal id
     * @return lower cased portal
     * @throws InvalidInputException when not registered or invalid id is supplied
     */
    private String checkPortal(String portal) throws InvalidInputException {
        if (portal == null || portal.length() == 0)
            throw new InvalidInputException("portal is mandatory");
        portal = portal.toLowerCase();
        if (!portals.contains(portal))
            throw new InvalidInputException("Portál '" + portal + "' není registrován v systému!");
        return portal;
    }

    private LdapUserManager() {
        ConfigurationManager.getConfigurator().configureAndRememberMe(this);
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        String tmp = prefs.get(PREF_INVALID_LOGIN_REGEXP, null);
        reLoginInvalid = Pattern.compile(tmp);
        ldapServerUri = prefs.get(PREF_SERVER_URI, "ldap://localhost:389");
        adminUsername = prefs.get(PREF_ADMIN_USER, "cn=Manager,dc=stickfish,dc=net");
        adminPassword = prefs.get(PREF_ADMIN_PASSWORD, "secret");
        parentContext = prefs.get(PREF_PARENT_CONTEXT, "ou=users,dc=stickfish,dc=net");
        ldapClass = prefs.get(PREF_LDAP_CLASS, "stickfishUser");

        String uris = prefs.get(PREF_SUPPORTED_PORTALS, "www.abclinuxu.cz");
        StringTokenizer stk = new StringTokenizer(uris, ",");
        while (stk.hasMoreTokens())
            portals.add(stk.nextToken().toLowerCase());

        String sCount = prefs.get(PREF_CONNECTION_PROPERTIES + PREF_PROPERTY_COUNT, "0");
        int count = Misc.parseInt(sCount, 0);
        for (int i = 1; i <= count; i++) {
            String key = prefs.get(PREF_CONNECTION_PROPERTIES + i + "." + PREF_PROPERTY_KEY, null);
            String value = prefs.get(PREF_CONNECTION_PROPERTIES + i + "." + PREF_PROPERTY_VALUE, null);
            if (key != null && value != null)
                adminConnectionEnv.put(key, value);
        }
    }
}
