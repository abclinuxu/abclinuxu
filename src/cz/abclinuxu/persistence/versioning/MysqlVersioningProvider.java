/*
 *  Copyright (C) 2005 Leos Literak
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
package cz.abclinuxu.persistence.versioning;

import cz.abclinuxu.exceptions.PersistenceException;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.PersistenceMapping;
import cz.abclinuxu.persistence.impl.MySqlPersistence;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.data.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.prefs.Preferences;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import org.dom4j.Attribute;


/**
 * This implementation of Versioning will store all versions
 * in Mysql table. Each version will be stored as separate
 * row. The version will be number, starting with 1
 * incremented by 1.
 * User: literakl
 * Date: 27.3.2005
 */
public class MysqlVersioningProvider implements Versioning, Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MysqlVersioningProvider.class);

    private static final String PREF_FIND_ALL_VERSIONS = "sql.find.all.versions";
    private static final String PREF_INSERT_VERSION = "sql.insert.version";
    private static final String PREF_FETCH_DOCUMENT = "sql.fetch.document";
    private static final String PREF_PURGE_DOCUMENT = "sql.purge.document";
    private static final String PREF_COMMITTERS_COUNT = "committers.count";

    private String allVersions, insertVersion, fetchDocument, purgeDocument;
    private int committersCount;

    static final String[] SKIPPED_ELEMENTS = {"rating"};

    public MysqlVersioningProvider() {
        ConfigurationManager.getConfigurator().configureAndRememberMe(this);
    }

    /**
     * Updates versioning element inside object's XML. This call is mandatory
     * before versioned object can be persisted and committed. It increments
     * latest version and list of last committers. It is neccessary to store
     * these changes to persistence, once this method is finished.
     * Typical use case is:
     * <ul>
     * <li>versioning.prepareObjectBeforeCommit(obj, user.getId());</li>
     * <li>persistence.create(obj); or persistence.update(obj);</li>
     * <li>versioning.commit(obj, user.getId(), commitMessage);</li>
     * </ul>
     * @param obj object that shall be updated
     * @param user identifier of the user who commited this version
     */
    public void prepareObjectBeforeCommit(GenericDataObject obj, int user) {
        String currentCommitter = Integer.toString(user);
        Element root = obj.getData().getRootElement();
        Element versioning = (Element) root.selectSingleNode("versioning/revisions");
        if (versioning == null) {
            versioning = DocumentHelper.makeElement(root, "versioning/revisions");
            versioning.addAttribute("last", "1");
            versioning.addElement("committers").addElement("creator").setText(currentCommitter);
        } else {
            Attribute attribute = versioning.attribute("last");
            int previousRevision = Integer.parseInt(attribute.getText());
            attribute.setText(Integer.toString(++previousRevision));

            Element commiters = versioning.element("committers");
            Element last = commiters.element("last");
            if (last == null) { // first change commit
                commiters.addElement("last").setText(currentCommitter);
                return;
            }

            String previousCommitter = last.getText();
            if (currentCommitter.equals(previousCommitter)) // same author commited annother revision
                return;

            List list = commiters.elements("committer");
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Element element = (Element) iter.next();
                if (previousCommitter.equals(element.getText()))
                    iter.remove();
            }

            last.setText(currentCommitter);
            Element addedCommitter = DocumentHelper.createElement("committer");
            addedCommitter.setText(previousCommitter);
            list.add(0, addedCommitter);

            if (list.size() >= committersCount)
                list.remove(list.size() - 1);
        }
    }

    /**
     * Stores latest version of document into versioning repository. Object's XML document
     * must contain valid versioning/info element. You must call prepareObjectBeforeCommit()
     * method prior to this call.
     * @param obj object that shall be stored
     * @param user identifier of the user who commited this version
     * @param descr description of commited changes
     */
    public void commit(GenericDataObject obj, int user, String descr) throws VersioningException {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        Element copy = obj.getData().getRootElement().createCopy();
        Element versioningInfo = (Element) copy.selectSingleNode("versioning/revisions");
        if (versioningInfo == null)
            throw new VersioningException("Chybí /versioning/revisions element. Byla zavolána metoda prepareObjectBeforeCommit()?");
        versioningInfo.detach();
        int revision = Misc.parseInt(versioningInfo.attributeValue("last"), -1);
        if (revision == -1)
            throw new VersioningException("Chybí atribut last. Byla zavolána metoda prepareObjectBeforeCommit()?");

        // we do not store some elements in the versioning
        for (String elname : SKIPPED_ELEMENTS) {
            Element elem = copy.element(elname);
            if (elem != null)
                elem.detach();
        }

        // we store object's properties in the revision within versioning/properties element
        Map<String, Set<String>> properties = obj.getProperties();
        if (properties != null) {
            Element propertiesElement = DocumentHelper.makeElement(copy, "/versioning/properties");
            for (String key : properties.keySet()) {
                Element propertyElement = propertiesElement.addElement("property");
                propertyElement.addElement("key").setText(key);
                for (String value : properties.get(key)) {
                    propertyElement.addElement("value").setText(value);
                }
            }
        }

        // store title
        DocumentHelper.makeElement(copy, "/versioning/title").setText(obj.getTitle());

        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(insertVersion);
            statement.setString(1, PersistenceMapping.getGenericObjectType(obj));
            statement.setInt(2, obj.getId());
            statement.setInt(3, revision);
            statement.setInt(4, user);
            statement.setTimestamp(5, new Timestamp(obj.getUpdated().getTime()));
            statement.setString(6, copy.asXML());
            statement.setString(7, descr);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("SQL error", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Loads given document in selected version from versioning.
     * @param obj object to be loaded and updated to contain same data like in specified revision. It must be initialized
     * @param version version to be fetched
     * @throws VersionNotFoundException Thrown when either document or specified version doesn't exist.
     */
    public void load(GenericDataObject obj, int version) throws VersionNotFoundException {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(fetchDocument);
            statement.setString(1, PersistenceMapping.getGenericObjectType(obj));
            statement.setInt(2, obj.getId());
            statement.setInt(3, version);
            resultSet = statement.executeQuery();
            if (!resultSet.next())
                throw new VersionNotFoundException("Cannot find document '"+obj+"' in version '"+version+"'!");

            Document document = obj.getData();
            Element versioningInfo = (Element) document.selectSingleNode("/data/versioning/revisions").clone();
            Element[] elems = new Element[SKIPPED_ELEMENTS.length];
            for (int i = 0; i < SKIPPED_ELEMENTS.length; i++)
                elems[i] = (Element) document.selectSingleNode("/data/" + SKIPPED_ELEMENTS[i]);

            obj.setOwner(resultSet.getInt(1));
            obj.setUpdated(new Date(resultSet.getTimestamp(2).getTime()));
            obj.setData(resultSet.getString(3));

            // we do not store some elements in the versioning, let's load it from persistence
            document = obj.getData();
            for (int i = 0; i < SKIPPED_ELEMENTS.length; i++) {
                if (elems[i] != null) {
                    Element copy = elems[i].createCopy();
                    Element orig = (Element) document.selectSingleNode("/data/" + SKIPPED_ELEMENTS[i]);
                    if (orig != null)
                        orig.detach();
                    document.getRootElement().add(copy);
                }
            }

            Element versioning = DocumentHelper.makeElement(document, "/data/versioning");
            versioning.add(versioningInfo);
            Element properties = versioning.element("properties");
            if (properties != null) {
                properties.detach();
                obj.clearProperties();

                List list = properties.elements("property");
                for (Iterator iter = list.iterator(); iter.hasNext();) {
                    Element propertyElement = (Element) iter.next();
                    String key = propertyElement.elementText("key");
                    for (Iterator iterIn = propertyElement.elements("value").iterator(); iterIn.hasNext();) {
                        Element value = (Element) iterIn.next();
                        obj.addProperty(key, value.getText());
                    }
                }
            }

            Element elementTitle = versioning.element("title");
            if (elementTitle != null) {
                obj.setTitle(elementTitle.getText());
                elementTitle.detach();
            } else {
                // todo docasne reseni, nez se zmigruji data v tabulce historie na novy format
                elementTitle = (Element) document.selectSingleNode("/data/name");
                if (elementTitle == null)
                    elementTitle = (Element) document.selectSingleNode("/data/title");
                if (elementTitle != null)
                    obj.setTitle(elementTitle.getText());
                else if (obj.getType() == Item.PERSONALITY) {
                    StringBuffer sb = new StringBuffer();
                    String name = document.getRootElement().elementTextTrim("firstname");
                    if (name != null)
                        sb.append(name).append(' ');
                    sb.append(document.getRootElement().elementTextTrim("surname"));
                    obj.setTitle(sb.toString());
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("SQL error", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Loads versioning history for selected document in descending order.
     * @param obj object, its type and id is used for identification
     * @return list of VersionInfo objects. When the list is empty, then there is no
     *         version of specified document.
     */
    public List<VersionInfo> getHistory(GenericDataObject obj) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(allVersions);
            statement.setString(1, PersistenceMapping.getGenericObjectType(obj));
            statement.setInt(2, obj.getId());
            resultSet = statement.executeQuery();

            List<VersionInfo> history = new ArrayList<VersionInfo>();
            while (resultSet.next()) {
                VersionInfo info = new VersionInfo();
                info.setVersion(resultSet.getInt(1));
                info.setUser(resultSet.getInt(2));
                info.setCommited(new Date(resultSet.getTimestamp(3).getTime()));
                info.setDescription(resultSet.getString(4));
                history.add(info);
            }

            return history;
        } catch (SQLException e) {
            throw new PersistenceException("SQL error", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Removes all information for given document from versioning repository.
     * @param obj object, its type and id is used for identification
     * @return true if there were some revisions for specified document
     */
    public boolean purge(GenericDataObject obj) {
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistence();
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(purgeDocument);
            statement.setString(1, PersistenceMapping.getGenericObjectType(obj));
            statement.setInt(2, obj.getId());
            int matched = statement.executeUpdate();
            return (matched > 0);
        } catch (SQLException e) {
            throw new PersistenceException("SQL error", e);
        } finally {
            PersistenceFactory.releaseSQLResources(con, statement, null);
        }
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        allVersions = getValue(PREF_FIND_ALL_VERSIONS, prefs);
        fetchDocument = getValue(PREF_FETCH_DOCUMENT, prefs);
        insertVersion = getValue(PREF_INSERT_VERSION, prefs);
        purgeDocument = getValue(PREF_PURGE_DOCUMENT, prefs);
        committersCount = prefs.getInt(PREF_COMMITTERS_COUNT, 2);
    }

    /**
     * Gets value from preferences. If value is not defined, it dumps info
     * into logs.
     */
    private String getValue(String name, Preferences prefs) {
        String sql = prefs.get(name, null);
        if (sql != null)
            return sql;

        log.fatal("Hodnota SQL prikazu " + name + " nebyla nastavena!");
        return null;
    }
}
