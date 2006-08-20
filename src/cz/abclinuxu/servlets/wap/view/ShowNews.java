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
package cz.abclinuxu.servlets.wap.view;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.utils.paging.Paging;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Shows (actual|selected) news.
 */
public class ShowNews implements AbcAction {
    /** n-th oldest object, where display from */
    public static final String PARAM_FROM = "from";
    /** list of found relations, that match the conditions */
    public static final String VAR_FOUND = "FOUND";

    static final Qualifier[] QUALIFIERS_ARRAY = new Qualifier[]{};

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        SQLTool sqlTool = SQLTool.getInstance();

        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int count = 5;
        Qualifier[] qualifiers = getQualifiers(Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, from, count);
        List data = sqlTool.findNewsRelations(qualifiers);
        Tools.syncList(data);
        int total = sqlTool.countNewsRelations();

        Paging found = new Paging(data, from, count, total, qualifiers);
        env.put(VAR_FOUND, found);
        return "/wap/show/zpravicky.ftl";
    }

    /**
     * Gets qualifiers, which user might overwrote in params.
     * @param sortBy Optional sortBy Qualifier.
     * @param sortDir Optional sort direction Qualifier.
     * @param fromRow Optional first row of data to be fetched.
     * @param rowCount 0 means do not set LimiQualifier. Otherwise it sets size of page to be fetched.
     * @return Qualifiers.
     */
    public static Qualifier[] getQualifiers(Qualifier sortBy, Qualifier sortDir, int fromRow, int rowCount) {
        List qualifiers = new ArrayList(3);

        if ( sortBy!=null )
            qualifiers.add(sortBy);
        if ( sortDir!=null )
            qualifiers.add(sortDir);
        if ( rowCount>0 )
            qualifiers.add(new LimitQualifier(fromRow, rowCount));

        return (Qualifier[]) qualifiers.toArray(QUALIFIERS_ARRAY);
    }
}
