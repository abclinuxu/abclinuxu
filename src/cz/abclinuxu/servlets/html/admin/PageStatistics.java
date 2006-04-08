/*
 *  Copyright (C) 2006 Leos Literak
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
package cz.abclinuxu.servlets.html.admin;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.data.User;
import cz.abclinuxu.persistance.extra.CompareCondition;
import cz.abclinuxu.persistance.extra.Field;
import cz.abclinuxu.persistance.extra.Operation;
import cz.abclinuxu.persistance.extra.Qualifier;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.utils.DateTool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.Calendar;

/**
 * Displays various graphs about page views.
 * @author literakl
 * @since 1.4.2006
 */
public class PageStatistics implements AbcAction {
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_PERIOD_START = "start";
    public static final String PARAM_PERIOD_STOP = "stop";
    public static final String PARAM_DAY = "day";

    public static final String TYPE_MONTHLY = "monthly";
    public static final String TYPE_PERIOD = "period";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        User user = (User) env.get(Constants.VAR_USER);
        if (user == null)
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        if (!(user.isMemberOf(Constants.GROUP_ADMINI) || user.isMemberOf(Constants.GROUP_STICKFISH)))
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        Map params = (Map) env.get(Constants.VAR_PARAMS);
        DateTool dateTool = new DateTool();
        Calendar calendar = Calendar.getInstance();
        Date startPeriod = null, endPeriod = null, day = null;

        String s = (String) params.get(PARAM_PERIOD_START);
        if (s == null) {
            calendar.add(Calendar.DAY_OF_YEAR, -90);
            startPeriod = calendar.getTime();
            params.put(PARAM_PERIOD_START, dateTool.show(startPeriod, DateTool.CZ_DAY_MONTH_YEAR, false));
        } else {
            startPeriod = Constants.czDayMonthYear.parse(s);
            calendar.setTime(startPeriod);
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            startPeriod = calendar.getTime();
        }

        s = (String) params.get(PARAM_PERIOD_STOP);
        if (s == null) {
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            endPeriod = calendar.getTime();
            params.put(PARAM_PERIOD_STOP, dateTool.show(endPeriod, DateTool.CZ_DAY_MONTH_YEAR, false));
        } else {
            endPeriod = Constants.czDayMonthYear.parse(s);
            calendar.setTime(endPeriod);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            endPeriod = calendar.getTime();
        }

        s = (String) params.get(PARAM_DAY);
        if (s == null) {
            day = new Date();
            params.put(PARAM_DAY, dateTool.show(day, DateTool.CZ_DAY_MONTH_YEAR, false));
        } else
            day = Constants.czDayMonthYear.parse(s);

        List found = null;
        SQLTool sqlTool = SQLTool.getInstance();
        String type = (String) params.get(PARAM_TYPE);
        if (TYPE_MONTHLY.equals(type)) {
            found = sqlTool.getStatisticsByMonth();
        } else if (TYPE_PERIOD.equals(type)) {
            CompareCondition startCondition = new CompareCondition(Field.DAY, Operation.GREATER, startPeriod);
            CompareCondition stopCondition = new CompareCondition(Field.DAY, Operation.SMALLER, endPeriod);
            found = sqlTool.getStatistics(new Qualifier[]{startCondition, stopCondition});
        } else {
            CompareCondition condition = new CompareCondition(Field.DAY, Operation.EQUAL, day);
            found = sqlTool.getStatistics(new Qualifier[]{condition});
        }

        env.put("DATA", found);
        return FMTemplateSelector.select("PageStatistics", "show", env, request);
    }
}
