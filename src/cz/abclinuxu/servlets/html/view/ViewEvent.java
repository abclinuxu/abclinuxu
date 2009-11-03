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

package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Link;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.LogicalOperation;
import cz.abclinuxu.persistence.extra.NestedCondition;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.InstanceUtils;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.ReadRecorder;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.paging.Paging;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author lubos
 */
public class ViewEvent implements AbcAction, Configurable {
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_FROM = "from";
    
    public static final String PARAM_YEAR = "year";
    public static final String PARAM_MONTH = "month";
    public static final String PARAM_DAY = "day";
    public static final String PARAM_MODE = "mode";
    /** events types like community, educational, company... */
    public static final String PARAM_SUBTYPE = "subtype";
    
    public static final String MODE_OLD = "old";
    public static final String MODE_UPCOMING = "upcoming";
    public static final String MODE_UNPUBLISHED = "unpublished";
    public static final String MODE_EVERYTHING = "everything";
    
    public static final String ACTION_PARTICIPANTS = "participants";
    
    public static final String VAR_ITEMS = "ITEMS";
    public static final String VAR_ITEM = "ITEM";
    /** A hashmap containing data for the calendar */
    public static final String VAR_CALENDAR = "CALENDAR";
    public static final String VAR_REGISTRATIONS = "REGISTRATIONS";
    public static final String VAR_GOOGLE_MAPS_KEY = "GOOGLE_MAPS_KEY";
    
    static String googleMapsKey;
    
    static {
        ViewEvent servlet = new ViewEvent();
        ConfigurationManager.getConfigurator().configureAndRememberMe(servlet);
    }
    
    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Persistence persistence = PersistenceFactory.getPersistence();
        
        
        Relation relation = (Relation) InstanceUtils.instantiateParam(PARAM_RELATION_SHORT, Relation.class, params, request);
        if (relation == null)
            throw new MissingArgumentException("Parametr relationId je prázdný!");
        relation = (Relation) persistence.findById(relation);
        env.put(ShowObject.VAR_RELATION,relation);
        
        List parents = persistence.findParents(relation);
        env.put(ShowObject.VAR_PARENTS, parents);
        
        Relation subportal = Tools.getParentSubportal(parents);
        if (subportal != null) {
            env.put(ShowObject.VAR_SUBPORTAL, subportal);
            ReadRecorder.log(subportal.getChild(), Constants.COUNTER_READ, env);
        }
        
        if (relation.getId() == Constants.REL_EVENTS)
            return processSection(request, response, relation, env);
        else
            return processItem(request, relation, env);
        
    }
    
    public static String processSection(HttpServletRequest request, HttpServletResponse response, Relation relation, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String mode = (String) params.get(PARAM_MODE);
        
        if (MODE_UNPUBLISHED.equals(mode))
            return processWaitingSection(request, response, relation, env);
        else
            return processStandardSection(request, response, relation, env);
    }
        
    
    public static String processWaitingSection(HttpServletRequest request, HttpServletResponse response, Relation relation, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int count = Misc.getDefaultPageSize(env);
        SQLTool sqlTool = SQLTool.getInstance();
        
        List<Qualifier> qualifiers = new ArrayList(3);
        Qualifier[] qa;
        int total;
        
        qualifiers.add(new CompareCondition(Field.UPPER, Operation.EQUAL, relation.getId()));
        qualifiers.add(Qualifier.SORT_BY_CREATED);
        
        qa = new Qualifier[qualifiers.size()];
        total = sqlTool.countItemRelationsWithType(Item.UNPUBLISHED_EVENT, qualifiers.toArray(qa));
        
        qualifiers.add(new LimitQualifier(from, count));
        qa = new Qualifier[qualifiers.size()];

        List list = sqlTool.findItemRelationsWithType(Item.UNPUBLISHED_EVENT, qualifiers.toArray(qa));
        Tools.syncList(list);

        Paging paging = new Paging(list, from, count, total);
        env.put(VAR_ITEMS, paging);
        
        List parents = (List) env.get(ShowObject.VAR_PARENTS);
        Link link = new Link("Čekající akce", relation.getUrl()+"?mode="+MODE_UNPUBLISHED, null);
        parents.add(link);
        
        return FMTemplateSelector.select("ViewEvent", "list", env, request);
    }
    
    public static String processStandardSection(HttpServletRequest request, HttpServletResponse response, Relation relation, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        SQLTool sqlTool = SQLTool.getInstance();
        String mode = (String) params.get(PARAM_MODE);
        String subtype = (String) params.get(PARAM_SUBTYPE);
        
        Calendar dateFrom = null, dateTo = null;
        Qualifier order = Qualifier.ORDER_ASCENDING;
        
        if (MODE_OLD.equals(mode)) {
            order = Qualifier.ORDER_DESCENDING;
            dateTo = Calendar.getInstance();
        } else if (!MODE_EVERYTHING.equals(mode)) { // compute the date range
            int year, month, day;
            year = Misc.parseInt((String) params.get(PARAM_YEAR), 0);
            month = Misc.parseInt((String) params.get(PARAM_MONTH), 0);
            day = Misc.parseInt((String) params.get(PARAM_DAY), 0);
            
            dateFrom = Calendar.getInstance();
            dateFrom.set(Calendar.HOUR_OF_DAY, 0);
            dateFrom.set(Calendar.MINUTE, 0);
            
            // If the year was specified, we have to create a date range
            if (year != 0) {
                dateFrom.set(Calendar.YEAR, year);
                
                if (month != 0)
                    dateFrom.set(Calendar.MONTH, month-1);
                else {
                    dateFrom.set(Calendar.MONTH, 0);
                    day = 0; // disregard the day
                }
                
                if (day != 0)
                    dateFrom.set(Calendar.DAY_OF_MONTH, day);
                else
                    dateFrom.set(Calendar.DAY_OF_MONTH, 1);
                
                dateTo = (Calendar) dateFrom.clone();
                dateTo.set(Calendar.HOUR_OF_DAY, 23);
                dateTo.set(Calendar.MINUTE, 59);
                
                if (month == 0) {
                    // user wants to browse the whole year
                    dateTo.add(Calendar.YEAR, 1);
                    dateTo.add(Calendar.DAY_OF_MONTH, -1);
                } else if (day == 0) {
                    // user wants to browse the whole month
                    dateTo.add(Calendar.MONTH, 1);
                    dateTo.add(Calendar.DAY_OF_MONTH, -1);
                }
                
                params.remove(PARAM_MODE);
            } else
                params.put(PARAM_MODE, MODE_UPCOMING);
        }
        
        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int count = Misc.getDefaultPageSize(env);
        List<Qualifier> qualifiers = new ArrayList<Qualifier>(5);
        List<Qualifier> qualifiersCal = new ArrayList<Qualifier>(3);
        
        if (dateFrom != null) {
            String date = Constants.isoFormat.format(dateFrom.getTime());
            CompareCondition left = new CompareCondition(Field.CREATED, Operation.GREATER_OR_EQUAL, date);
            CompareCondition right = new CompareCondition(Field.DATE1, Operation.GREATER_OR_EQUAL, date);
            
            qualifiers.add(new NestedCondition(new Qualifier[] {left, right}, LogicalOperation.OR));
        }
        if (dateTo != null) {
            String date = Constants.isoFormat.format(dateTo.getTime());
            CompareCondition left = new CompareCondition(Field.CREATED, Operation.SMALLER_OR_EQUAL, date);
            CompareCondition right = new CompareCondition(Field.DATE1, Operation.SMALLER_OR_EQUAL, date);
            
            qualifiers.add(new NestedCondition(new Qualifier[] {left, right}, LogicalOperation.OR));
        }
        if (!Misc.empty(subtype))
            qualifiers.add(new CompareCondition(Field.SUBTYPE, Operation.EQUAL, subtype));
        
        // The REL_EVENTS section should display all events in the system
        // So if we're not processing that section, we should limit the search to the current section
        if (relation.getId() != Constants.REL_EVENTS) {
            Qualifier q = new CompareCondition(Field.UPPER, Operation.EQUAL, relation.getId());
            qualifiers.add(q);
            qualifiersCal.add(q);
        }

        Qualifier[] qa = new Qualifier[qualifiers.size()];
        int total = sqlTool.countItemRelationsWithType(Item.EVENT, qualifiers.toArray(qa));
        
        qualifiers.add(Qualifier.SORT_BY_CREATED);
        qualifiers.add(order);
        qualifiers.add(new LimitQualifier(from, count));
        qa = new Qualifier[qualifiers.size()];

        List<Relation> list = sqlTool.findItemRelationsWithType(Item.EVENT, qualifiers.toArray(qa));
        Tools.syncList(list);

        Paging paging = new Paging(list, from, count, total);
        env.put(VAR_ITEMS, paging);

        env.put(Constants.VAR_READ_COUNTERS, Tools.getRelationCountersValue(list, Constants.COUNTER_READ));

        // Process the month for the calendar view
        Map map = new HashMap(10);
        Calendar cal;
        
        if (dateFrom != null)
            cal = dateFrom;
        else if (dateTo != null)
            cal = dateTo;
        else
            cal = Calendar.getInstance();
                
        int month = cal.get(Calendar.MONTH)+1; // the selected month
        map.put("month", month);
        map.put("year", cal.get(Calendar.YEAR));
        map.put("days", cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        
        // find all events in that month
        String date;
        
        CompareCondition condLeft1, condRight1, condLeft2, condRight2;
        cal.set(Calendar.DAY_OF_MONTH, 1);
        date = Constants.isoFormat.format(cal.getTime());
        condLeft1 = new CompareCondition(Field.CREATED, Operation.GREATER_OR_EQUAL, date);
        condLeft2 = new CompareCondition(Field.DATE1, Operation.GREATER_OR_EQUAL, date);
        qualifiersCal.add(new NestedCondition(new Qualifier[] { condLeft1, condLeft2 }, LogicalOperation.OR));
        
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, 1);
        date = Constants.isoFormat.format(cal.getTime());
        condRight1 = new CompareCondition(Field.CREATED, Operation.SMALLER, date);
        condRight2 = new CompareCondition(Field.DATE1, Operation.SMALLER, date);
        qualifiersCal.add(new NestedCondition(new Qualifier[] { condRight1, condRight2 }, LogicalOperation.OR));
        
        qualifiersCal.add(Qualifier.SORT_BY_CREATED);
        qualifiersCal.add(Qualifier.ORDER_ASCENDING);
        
        qa = new Qualifier[qualifiersCal.size()];
        list = sqlTool.findItemRelationsWithType(Item.EVENT, qualifiersCal.toArray(qa));
        Tools.syncList(list);
        
        // mark those days in the calendar
        int days = (Integer) map.get("days");
        boolean[] eventDays = new boolean[days];
        
        for (Relation r : list) {
            Item item = (Item) r.getChild();
            Calendar cItemFrom = Calendar.getInstance();
            Calendar cItemTo;
            int fromIndex, toIndex = -1;
            
            cItemFrom.setTime(item.getCreated());
            fromIndex = cItemFrom.get(Calendar.DAY_OF_MONTH)-1;
            
            if (item.getDate1() != null) {
                cItemTo = Calendar.getInstance();
                cItemTo.setTime(item.getDate1());
                
                if (cItemTo.get(Calendar.YEAR) > cItemFrom.get(Calendar.YEAR) ||
                        cItemTo.get(Calendar.MONTH) > cItemFrom.get(Calendar.MONTH)) {
                    toIndex = days - 1;
                } else
                    toIndex = cItemTo.get(Calendar.DAY_OF_MONTH)-1;
            }
            
            
            eventDays[fromIndex] = true;
            
            if (toIndex != -1) {
                for (int i = fromIndex; i <= toIndex; i++)
                    eventDays[i] = true;
            }
        }
        
        map.put("eventDays", eventDays);
        
        // Set the info about the previous and next months
        // so that it doesn't have to be computed in Freemarker
        if (month < 12) {
            map.put("nextMonth", month+1);
            map.put("nextYear", map.get("year"));
        } else {
            map.put("nextMonth", 1);
            map.put("nextYear", ((Integer) map.get("year")) + 1);
        }
        
        if (month > 1) {
            map.put("prevMonth", month-1);
            map.put("prevYear", map.get("year"));
        } else {
            map.put("prevMonth", 12);
            map.put("prevYear", ((Integer) map.get("year")) - 1);
        }
        
        map.put("week", cal.get(Calendar.WEEK_OF_YEAR)+1);
        
        // The number of empty day fields in the first week of the month in question
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        int firstDay = cal.get(Calendar.DAY_OF_WEEK);
        if (firstDay == Calendar.SUNDAY)
            map.put("emptyDays", 6);
        else
            map.put("emptyDays", firstDay-2);
        
        // If the month shown is the current month,
        // emphasize the current day
        Calendar today = Calendar.getInstance();
        if (cal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
            
            map.put("today", today.get(Calendar.DAY_OF_MONTH));
        }
        
        env.put(VAR_CALENDAR, map);
        
        return FMTemplateSelector.select("ViewEvent", "list", env, request);
    }
    
    public static String processItem(HttpServletRequest request, Relation relation, Map env) throws Exception {
        Item item = (Item) relation.getChild();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);
        
        Tools.sync(item);
        env.put(VAR_ITEM, item);
        
        Map children = (Map) env.get(ShowObject.VAR_CHILDREN_MAP);
        if (children==null) {
            children = Tools.groupByType(item.getChildren());
            env.put(ShowObject.VAR_CHILDREN_MAP, children);
        }
        
        if (ACTION_PARTICIPANTS.equals(action)) {
            List parents = (List) env.get(ShowObject.VAR_PARENTS);
            Link link = new Link("Účastníci", relation.getUrl()+"?action="+ACTION_PARTICIPANTS, null);
            parents.add(link);
        
            return FMTemplateSelector.select("ViewEvent", "participants", env, request);
        } else {
            User user = (User) env.get(Constants.VAR_USER);
            if ( user == null || user.getId() != item.getOwner() )
                ReadRecorder.log(item, Constants.COUNTER_READ, env);
            
            env.put(VAR_GOOGLE_MAPS_KEY, googleMapsKey);

            return FMTemplateSelector.select("ViewEvent", "view", env, request);
        }
    }
    
    public void configure(Preferences prefs) throws ConfigurationException {
        googleMapsKey = prefs.get("google.maps.key", null);
    }
}
