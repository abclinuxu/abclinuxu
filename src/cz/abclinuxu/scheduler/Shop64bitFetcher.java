/*
 *  Copyright (C) 2007 Leos Literak
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
package cz.abclinuxu.scheduler;

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.view.ShopProduct;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import org.apache.log4j.Logger;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;
import java.util.Map;
import java.util.HashMap;
import java.util.prefs.Preferences;

/**
 * Fetches content of 64bit.cz shop.
 * @author literakl
 * @since 27.9.2007
 */
public class Shop64bitFetcher extends TimerTask implements Configurable {
    static Logger log = Logger.getLogger(Shop64bitFetcher.class);

    private static Shop64bitFetcher instance ;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e) {
            log.fatal("Nemohu vytvořit instanci JDBC driveru, zkontroluj CLASSPATH!", e);
        }

        instance = new Shop64bitFetcher();
        ConfigurationManager.getConfigurator().configureAndRememberMe(instance);
    }

    public static final String PREF_JDBC_URL = "jdbc.url";
    public static final String PREF_SQL_QUERY = "sql.products";


    private String jdbcUrl;
    private String sqlQuery;


    public void run() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            Persistence persistence = PersistenceFactory.getPersistence();
            Category importCategory = (Category) persistence.findById(new Category(Constants.CAT_SHOP_64BIT_CZ)).clone();

            List<Integer> ids = new ArrayList<Integer>();
            Element rootElement = importCategory.getData().getRootElement();
            List elements = rootElement.elements("item");
            for (Iterator iter = elements.iterator(); iter.hasNext();) {
                Element element = (Element) iter.next();
                String id = element.attributeValue("id");
                ids.add(Integer.parseInt(id));
            }

            connection = DriverManager.getConnection(jdbcUrl);
            statement = connection.prepareStatement(sqlQuery + Misc.getInCondition(ids.size()));
            for (int i = 0; i < ids.size(); i++)
                statement.setInt(i + 1, ids.get(i));

            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Element element = (Element) rootElement.selectSingleNode("item[@id=" + resultSet.getInt(1) + "]");
                if (element == null) {
                    log.warn("Nenalezen produkt s id " + resultSet.getInt(1));
                    continue;
                }

                double price = resultSet.getDouble(2), discount = 1 - resultSet.getDouble(3) / 100.0;
                double endPrice = Math.ceil(price * discount);
                element.addAttribute("price", Double.toString(endPrice));
                DocumentHelper.makeElement(element, "name").setText(resultSet.getString(4));
            }
            persistence.update(importCategory);
            updateVariableFetcher();

        } catch (Exception e) {
            log.error("Shop64bitFetcher failed", e);
        } finally {
            PersistenceFactory.releaseSQLResources(connection, statement, resultSet);
        }
    }

    private void updateVariableFetcher() {
        List<ShopProduct> products = new ArrayList<ShopProduct>();
        Persistence persistence = PersistenceFactory.getPersistence();
        Category importCategory = (Category) persistence.findById(new Category(Constants.CAT_SHOP_64BIT_CZ));
        Element rootElement = importCategory.getData().getRootElement();
        List elements = rootElement.elements("item");
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            Element element = (Element) iter.next();
            String name = element.elementText("name");
            String id = element.attributeValue("id");
            String price = element.attributeValue("price");
            String url = element.elementText("url");
            products.add(new ShopProduct(id, name, url, Float.parseFloat(price)));
        }
        VariableFetcher.getInstance().setShopProducts("64bit", products);
    }

    public static Shop64bitFetcher getInstance() {
        return instance;
    }

    private Shop64bitFetcher() {
        updateVariableFetcher();
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        jdbcUrl = prefs.get(PREF_JDBC_URL, null);
        if (jdbcUrl == null)
            throw new ConfigurationException("Configuration for " + PREF_JDBC_URL + " is missing!");
        sqlQuery = prefs.get(PREF_SQL_QUERY, null);
        if (sqlQuery == null)
            throw new ConfigurationException("Configuration for " + PREF_SQL_QUERY + " is missing!");
    }

    public static void main(String[] args) throws Exception {
        new Shop64bitFetcher().run();
    }
}
