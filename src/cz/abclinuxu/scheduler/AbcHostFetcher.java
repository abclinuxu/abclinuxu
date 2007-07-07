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
import cz.abclinuxu.data.Item;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.prefs.Preferences;

/**
 * Fetches content from abchost.cz
 * @author literakl
 * @since 27.1.2007
 */
public class AbcHostFetcher extends TimerTask implements Configurable {
    static Logger log = Logger.getLogger(AbcHostFetcher.class);

    static final String PREF_URI = "uri";
    static final String PREF_ITEM = "item";

    String uri;
    int targetItemId;

    public void run() {
        log.debug("Fetching AbcHost XML starts ..");
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(uri);
            Element productsElement = document.getRootElement().element("products");
            Element servicesElement = document.getRootElement().element("services");

            List<HostingServer> servers = new ArrayList<HostingServer>();
            for (Iterator iter = productsElement.elements().iterator(); iter.hasNext();) {
                Element element = (Element) iter.next();
                servers.add(parseServer(element));
            }
            Collections.sort(servers, new Comparator<HostingServer>() {
                public int compare(HostingServer hostingServer, HostingServer hostingServer1) {
                    return (int) (hostingServer.getPrice().getPrice() - hostingServer1.getPrice().getPrice());
                }
            });

            List<HostingService> services = new ArrayList<HostingService>();
            for (Iterator iter = servicesElement.elements().iterator(); iter.hasNext();) {
                Element element = (Element) iter.next();
                services.add(parseService(element));
            }

            Map env = new HashMap();
            env.put("SERVERS", servers);
            env.put("SERVICES", services);
//            FMUtils.executeTemplate("/include/misc/generate_hosting.ftl", env, new File("/home/literakl/hosting.html"));
            String result = FMUtils.executeTemplate("/include/misc/generate_hosting.ftl", env);

            Persistence persistence = PersistenceFactory.getPersistence();
            Item hostingItem = (Item) persistence.findById(new Item(targetItemId));
            Element contentElement = hostingItem.getData().getRootElement().element("content");
            contentElement.setText(result);
            persistence.update(hostingItem);

            int random = new Random(System.currentTimeMillis()).nextInt(servers.size());
            VariableFetcher.getInstance().setHostingServer(servers.get(random));

            log.debug("AbcHost content generated");
        } catch (DocumentException e) {
            log.error("IO problems for " + uri + ": " + e.getMessage());
        } catch (Exception e) {
            log.error("Cannot parse links from " + uri, e);
        }
    }

    private HostingServer parseServer(Element element) {
        HostingServer server = new HostingServer(element.elementText("name"), element.elementText("url"));
        if ("true".equalsIgnoreCase(element.attributeValue("action")))
            server.setAction(true);
        if ("true".equalsIgnoreCase(element.attributeValue("new")))
            server.setNewArrival(true);

        Element el = element.element("image");
        if (el != null)
            server.setImageUrl(el.getText());
        el = element.element("description");
        if (el != null)
            server.setDescription(el.getText());
        el = element.element("availability");
        if (el != null)
            server.setAvailability(el.getText());
        el = element.element("bandwidth");
        if (el != null)
            server.setBandwidth(el.getText());
        el = element.element("transfers");
        if (el != null)
            server.setTransfers(el.getText());
        el = element.element("cpu");
        if (el != null)
            server.setCpu(getFirstLine(el.getText()));
        el = element.element("ram");
        if (el != null)
            server.setRam(getFirstLine(el.getText()));
        el = element.element("disc");
        if (el != null)
            server.setDisc(getFirstLine(el.getText()));
        el = element.element("network");
        if (el != null)
            server.setNetwork(getFirstLine(el.getText()));
        el = element.element("price");
        if (el != null)
            server.setPrice(parsePrice(el));
        el = element.element("fee");
        if (el != null)
            server.setSetupFee(parsePrice(el));

        return server;
    }

    private HostingPrice parsePrice(Element element) {
        float price = Float.parseFloat(element.getText());
        float vat = Float.parseFloat(element.attributeValue("vat"));
        HostingPrice hostingPrice = new HostingPrice(price, vat);
        hostingPrice.setCurrency(element.attributeValue("currency"));
        hostingPrice.setPaymentPeriod(element.attributeValue("period"));
        return hostingPrice;
    }

    private HostingService parseService(Element element) {
        HostingService service = new HostingService(element.elementText("name"), element.elementText("url"));
        if ("true".equalsIgnoreCase(element.attributeValue("action")))
            service.setAction(true);

        Element el = element.element("image");
        if (el != null)
            service.setImageUrl(el.getText());
        el = element.element("description");
        if (el != null)
            service.setDescription(el.getText());
        el = element.element("price");
        if (el != null)
            service.setPrice(parsePrice(el));

        return service;
    }

    /**
     * Extracts first line from string like: 128 MB SDRAM 100 MHz&lt;br&gt;maximálne 1 GB
     * @param s input
     * @return string up to first BR tag
     */
    private String getFirstLine(String s) {
        int position = s.indexOf("<br>");
        return (position == -1) ? s : s.substring(0, position);
    }

    public AbcHostFetcher() {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(this);
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        uri = prefs.get(PREF_URI, null);
        targetItemId = prefs.getInt(PREF_ITEM, 0);
    }

    public static void main(String[] args) throws Exception {
        new AbcHostFetcher().run();
    }
}
