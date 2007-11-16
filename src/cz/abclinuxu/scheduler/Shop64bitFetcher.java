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

import cz.abclinuxu.data.view.HostingPrice;
import cz.abclinuxu.data.view.HostingServer;
import cz.abclinuxu.data.view.HostingService;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.Configurator;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;
import java.util.prefs.Preferences;

/**
 * Fetches content of 64bit.cz shop.
 * @author literakl
 * @since 27.9.2007
 */
public class Shop64bitFetcher extends TimerTask implements Configurable {
    static Logger log = Logger.getLogger(Shop64bitFetcher.class);

    static final String PREF_URI = "uri";

    String uri;

    public void run() {
        log.debug("Fetching 64bit.cz XML starts ..");
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(uri);
            Element productsElement = document.getRootElement().element("products");
            Element servicesElement = document.getRootElement().element("services");
            Element softwareElement = document.getRootElement().element("softwares");

            List<HostingServer> servers = new ArrayList<HostingServer>();
            for (Iterator iter = productsElement.elements().iterator(); iter.hasNext();) {
                Element element = (Element) iter.next();
//                servers.add(parse(element));
            }

            List<HostingService> services = new ArrayList<HostingService>();
            for (Iterator iter = servicesElement.elements().iterator(); iter.hasNext();) {
                Element element = (Element) iter.next();
                services.add(parse(element));
            }

            List<HostingService> software = new ArrayList<HostingService>();
            for (Iterator iter = softwareElement.elements().iterator(); iter.hasNext();) {
                Element element = (Element) iter.next();
                services.add(parse(element));
            }

            int random = new Random(System.currentTimeMillis()).nextInt(servers.size());
            VariableFetcher.getInstance().setHostingServer(servers.get(random));

            log.debug("AbcHost content generated");
        } catch (DocumentException e) {
            log.error("IO problems for " + uri + ": " + e.getMessage());
        } catch (Exception e) {
            log.error("Cannot parse links from " + uri, e);
        }
    }

    private HostingPrice parsePrice(Element element) {
        float price = Float.parseFloat(element.getText());
        float vat = Float.parseFloat(element.attributeValue("vat"));
        HostingPrice hostingPrice = new HostingPrice(price, vat);
        hostingPrice.setCurrency(element.attributeValue("currency"));
        return hostingPrice;
    }

    private HostingService parse(Element element) {
        HostingService service = new HostingService(element.elementText("name"), element.elementText("url"));
        if ("true".equalsIgnoreCase(element.attributeValue("action")))
            service.setAction(true);
//        if ("true".equalsIgnoreCase(element.attributeValue("new")))
//            server.setNewArrival(true);
        Element el = element.element("price");
        if (el != null)
            service.setPrice(parsePrice(el));
        return service;
    }

    public Shop64bitFetcher() {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(this);
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        uri = prefs.get(PREF_URI, null);
    }

    public static void main(String[] args) throws Exception {
        new Shop64bitFetcher().run();
    }
}
