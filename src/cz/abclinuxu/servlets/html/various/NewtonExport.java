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
package cz.abclinuxu.servlets.html.various;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.AbcException;
import cz.abclinuxu.utils.freemarker.Tools;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Export for newton IT monitoring service.
 * Date: 16.2.2007
 */
public class NewtonExport implements AbcAction {
    public static final String PARAM_FROM = "FromDate";
    public static final String PARAM_TO = "ToDate";

    public static final String VAR_ARTICLES = "ARTICLES";
    public static final String VAR_NEWS = "NEWS";

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HHmmss");

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Date from = parseDate(PARAM_FROM, params);
        Date to = parseDate(PARAM_TO, params);

        SQLTool sqlTool = SQLTool.getInstance();
        CompareCondition conditionFrom = new CompareCondition(Field.CREATED, Operation.GREATER_OR_EQUAL, from);
        CompareCondition conditionTo = new CompareCondition(Field.CREATED, Operation.SMALLER, to);
        Qualifier[] qualifiers = new Qualifier[] {conditionFrom, conditionTo};
        List<Relation> articles = sqlTool.findArticleRelations(qualifiers, 0);

        Tools.syncList(articles);
        List sections = new ArrayList(articles.size());
        for (Relation relation : articles) {
            sections.add(relation.getParent());
        }
        Tools.syncList(sections);
        env.put(VAR_ARTICLES, articles);

        List<Relation> news = sqlTool.findNewsRelations(qualifiers);
        Tools.syncList(news);
        env.put(VAR_NEWS, news);

        env.put(Constants.VAR_CONTENT_TYPE, "application/xml; charset=UTF-8");
        return "/include/misc/generate_newton.ftl";
    }

    private Date parseDate(String param, Map params) {
        String value = (String) params.get(param);
        if (value == null)
            throw new AbcException("Parameter " + param + " is missing!");
        try {
            return format.parse(value);
        } catch (ParseException e) {
            throw new AbcException("Parameter " + param + " has unparsable value '" + value + '!');
        }
    }
}
