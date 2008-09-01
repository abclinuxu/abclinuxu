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

package cz.abclinuxu.migrate;

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Server;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.freemarker.Tools;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

/**
 *
 * @author lubos
 */
public class MigrateRssServers {
    static Map<Integer,String> serverUrls;
    
    public static void main (String[] args) {
        Persistence persistence = PersistenceFactory.getPersistence();
        
        // alter table server add `rss` varchar(255);
        // alter table server modify cislo int(3) NOT NULL auto_increment;
        
        loadUrls();
        
        Category parent = new Category(Constants.CAT_LINKS);
        Tools.sync(parent);
        
        for (Map.Entry<Integer,String> serverEntry : serverUrls.entrySet()) {
            Server server = new Server(serverEntry.getKey());
            persistence.synchronize(server);
            
            server.setRssUrl(serverEntry.getValue());
            persistence.update(server);
            
            Relation relation = new Relation(parent, server, Constants.REL_LINKS);
            persistence.create(relation);
        }
        
        System.out.println("Migrated "+serverUrls.size()+" servers.");
    }
    
    static void loadUrls() {
        Preferences preferences = Preferences.systemRoot();
        Preferences links = preferences.node("cz/abclinuxu/scheduler/UpdateLinks");
        
        String feeds = links.get("feeds", null);
        StringTokenizer stk = new StringTokenizer(feeds, ",");
        
        serverUrls = new HashMap(stk.countTokens());
        
        while (stk.hasMoreElements()) {
            String sid = stk.nextToken();
            int id = Integer.parseInt(sid);
            String url = links.get("feedUri"+id, null);
            
            serverUrls.put(id, url);
        }
    }
}
