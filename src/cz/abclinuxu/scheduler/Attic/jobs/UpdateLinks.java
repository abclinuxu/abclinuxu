/*
 * User: literakl
 * Date: Jan 28, 2002
 * Time: 1:38:39 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.scheduler.jobs;

import cz.abclinuxu.scheduler.Task;
import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.Sorters;

import java.util.*;
import java.net.URL;
import java.io.*;

import org.apache.log4j.xml.DOMConfigurator;

/**
 * This class is responsible for downloading of
 * freshest links from selected servers and updating
 * local copies.
 */
public class UpdateLinks implements Task {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(UpdateLinks.class);

    /** how many links we have per server */
    static final int COUNT = 5;
    static final int TEXT_LENGTH = 80;
    /** ids of servers */
    public static final int ROOT = 1;
    public static final int LW = 2;
    public static final int SW = 3;
    public static final int UG = 4;
    public static final int PENGUIN = 5;
    public static final int WS = 6;
    public static final int KECZY = 7;
    public static final int REBOOT = 8;
    /** id of last server, maximum id */
    public static final int MAX = REBOOT;

    Persistance persistance;
    Category category = new Category(Constants.CAT_LINKS);
    Map mapLinks;

    /**
     * Default constructor, used by Scheduler
     */
    public UpdateLinks() {
        persistance = PersistanceFactory.getPersistance();
        mapLinks = new HashMap();

        mapLinks.put(new Server(ROOT),"http://www.root.cz/share/ttitles.txt");
        mapLinks.put(new Server(LW),"http://www.linuxworld.cz/lw.dat");
        mapLinks.put(new Server(SW),"http://www.scienceworld.cz/sw.dat");
        mapLinks.put(new Server(UG),"http://underground.cz/backend/czech.txt");
        mapLinks.put(new Server(PENGUIN),"http://www.penguin.cz/trafika.php3");
        mapLinks.put(new Server(WS),"http://www.awdesign.cz/ws/ws.dat");
        mapLinks.put(new Server(KECZY),"http://www.keczy.cz/latin2/headline.php3");
        mapLinks.put(new Server(REBOOT),"http://www.reboot.cz/reboot_lh.phtml");

//        mapLinks.put(new Server(ROOT),"file:///home/literakl/obsahy/ttitles.txt");
//        mapLinks.put(new Server(LW),"file:///home/literakl/obsahy/lw.dat");
//        mapLinks.put(new Server(SW),"file:///home/literakl/obsahy/sw.dat");
//        mapLinks.put(new Server(UG),"file:///home/literakl/obsahy/czech.txt");
//        mapLinks.put(new Server(PENGUIN),"file:///home/literakl/obsahy/trafika.php3");
//        mapLinks.put(new Server(WS),"file:///home/literakl/obsahy/ws.dat");
//        mapLinks.put(new Server(KECZY),"file:///home/literakl/obsahy/headline.php3");
//        mapLinks.put(new Server(REBOOT),"file:///home/literakl/obsahy/reboot_lh.phtml");
    }

    /**
     * It is time to dowload links and update DB
     * @todo time out for opening connection!!!!!!!!!!!!
     */
    public void runJob() {
        InputStream is = null;
        BufferedReader in = null;

        try {
            category = (Category) persistance.findById(category);
            Map servers = groupLinks(category,persistance);

            for (Iterator iter = mapLinks.keySet().iterator(); iter.hasNext();) {
                Server server = (Server) iter.next();
                List links = (List) servers.get(server);
                Sorters.sortByDate(links,true);

                URL url = new URL((String) mapLinks.get(server));
                try {
                    is = url.openStream();
                    switch ( server.getId() ) {
                        case SW:
                        case LW: in = new BufferedReader(new InputStreamReader(is,"Windows-1250"));break;
                        default: in = new BufferedReader(new InputStreamReader(is));
                    }
                } catch (Exception e) {
                    log.warn("Cannot open URL "+mapLinks.get(server),e);
                    continue;
                }
                updateLinks(in,links,server,category);
            }
        } catch (Exception e) {
            log.error("Cannot update links!",e);
        }
    }

    public String getJobName() {
        return "UpdateLinks";
    }

    /**
     * Groups links by server. Each server will be key in map and value will be
     * list of links. Fixed links will be discarded.
     */
    public static Map groupLinks(Category category, Persistance persistance) {
        List[] links = new List[MAX+1];
        for ( int i=0; i<9; i++ ) links[i] = new ArrayList();

        for (Iterator iter = category.getContent().iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            try {
                Link link = (Link) persistance.findById(relation.getChild());
                if ( !link.isFixed()) links[link.getServer()].add(link);
            } catch (PersistanceException e) {
                log.error("Cannot find child Link in "+relation,e);
            }
        }

        Map result = new HashMap();
        for ( int i=1; i<9; i++ ) result.put(new Server(i),links[i]);
        return result;
    }

    /**
     * Reads one input file for selected server and updates links. It tries to keep
     * number of links on value of <code>COUNT</code>.
     */
    private void updateLinks(BufferedReader in, List links, Server server, Category category) throws PersistanceException {
        List urls = new ArrayList(COUNT);
        List titles = new ArrayList(COUNT);
        int count = 0;

        try {
            String line = in.readLine();
            while ( (line = in.readLine())!=null && count<COUNT ) {
                int where = line.indexOf("|\\");
                if ( where==-1 ) break;
                String url = line.substring(0,where);
                String title = line.substring(where+2);
                if ( title.length()>TEXT_LENGTH ) title = title.substring(0,TEXT_LENGTH);

                if ( ! removeTitle(links,title) ) {
                    urls.add(url);
                    titles.add(title);
                }
                count++;
            }
        } catch (IOException e) {
            log.warn("Cannot update links from server "+server,e);
            return;
        }

        int replacable = links.size(), i=0;
        for (Iterator iter = titles.iterator(); iter.hasNext();count++) {
            String title = (String) iter.next();
            String url = (String) urls.get(i++);

            if ( links.size()>0 ) {
                Link link = (Link) links.remove(0);
                link.setText(title);
                link.setUrl(url);
                persistance.update(link);
            } else {
                Link link = new Link();
                link.setOwner(1);
                link.setServer(server.getId());
                link.setFixed(false);
                link.setText(title);
                link.setUrl(url);

                persistance.create(link);
                Relation relation = new Relation(category,link,0);
                persistance.create(relation);
            }
        }
    }

    /**
     * Searches list of Links for title. If it finds it, it deletes it and returns true. Otherwise
     * does nothing else than returning false.
     */
    private boolean removeTitle(List list, String title) {
        String tmp;
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            tmp = ((Link) iter.next()).getText();
            if ( title.equals(tmp) ) {
                iter.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * @return true, if a is older than b.
     */
    private boolean isOlder(Link a, Link b) {
        Date ad = a.getUpdated();
        if ( ad==null ) return false;
        Date bd = b.getUpdated();
        if ( bd==null ) return true;
        return ad.before(bd);
    }

    public static void main(String[] args) {
        org.apache.log4j.BasicConfigurator.configure();
        new UpdateLinks().runJob();
    }
}
