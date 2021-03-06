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
package cz.abclinuxu.servlets.html.various;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.DiscussionHeader;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.utils.DateTool;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.data.view.NewsCategories;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Writer;
import java.util.*;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

/**
 * Displays last news in CSV format for internal use.
 * Restricted access.
 */
public class NewsCSV implements AbcAction, Configurable {
    static Logger log = Logger.getLogger(NewsCSV.class);

    public static final String PREF_NEWS_COUNT = "count";
    /** comma separated list of IP addresses, that are allowed to use this service */
    public static final String PREF_ALLOWED_IP_ADDRESSES = "allowed.ip.addresses";

    static {
        NewsCSV instance = new NewsCSV();
        ConfigurationManager.getConfigurator().configureAndRememberMe(instance);
    }

    static int count;
    static List allowedAddresses;

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        String remoteAddr = request.getRemoteAddr();
        if (!allowedAddresses.contains(remoteAddr)) {
            log.warn("Nekdo z adresy "+remoteAddr+" se pokusil pristoupit k /news/csv");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        Persistence persistence = PersistenceFactory.getPersistence();
        DateTool dateTool = new DateTool();
        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, count)};
        List news = SQLTool.getInstance().findNewsRelations(qualifiers);
        Tools.syncList(news);

        response.setContentType("text/plain; charset=UTF8");
        Writer writer = response.getWriter();
        for ( Iterator iterator = news.iterator(); iterator.hasNext(); ) {
            Relation relation = (Relation) iterator.next();
            Item item = (Item) relation.getChild();
            DiscussionHeader dizHeader = Tools.findComments(item);
            writer.write(dateTool.show(item.getCreated(), "CZ_SHORT"));
            writer.write("|");
            writer.write(Integer.toString(dizHeader.getResponseCount()));
            writer.write("|");
            writer.write(NewsCategories.get(item.getSubType()).getName());
            writer.write("|");
            User author = (User) persistence.findById(new User(item.getOwner()));
            writer.write("<a href=\"http://www.abclinuxu.cz/lide/"+author.getLogin()+"\">"+author.getName()+"</a>");
            writer.write("|");
            String text = Tools.xpath(item, "data/content");
            text = Tools.removeNewLines(text);
            text = Tools.fixAmpersand(text);
            text = Tools.prefixAbsoluteLinks(text, AbcConfig.getAbsoluteUrl());
            writer.write(text);
            writer.write("|");
            writer.write("http://www.abclinuxu.cz"+relation.getUrl());
            writer.write("\n");
        }
        writer.flush();
        writer.close();

        return null;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        count = prefs.getInt(PREF_NEWS_COUNT,10);
        String ips = prefs.get(PREF_ALLOWED_IP_ADDRESSES, "");
        StringTokenizer stk = new StringTokenizer(ips,",");
        allowedAddresses = new ArrayList(stk.countTokens());
        while(stk.hasMoreTokens()) {
            allowedAddresses.add(stk.nextToken());
        }
    }
}
