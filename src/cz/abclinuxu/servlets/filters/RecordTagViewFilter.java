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
package cz.abclinuxu.servlets.filters;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.data.Tag;
import cz.abclinuxu.scheduler.UpdateStatistics;

import javax.servlet.*;
import java.io.IOException;
import java.util.Map;
import java.util.List;

/**
 * This filter is responsible for recording page views of tags. It depends on map passed as request attribute
 * with key Constants.VAR_ENVIRONMENT, which shall contain list of tags with key Constants.VAR_ASSIGNED_TAGS.
 */
public class RecordTagViewFilter implements Filter {
    UpdateStatistics statistics = UpdateStatistics.getInstance();

    public void init(FilterConfig config) throws ServletException {}

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (chain != null)
            chain.doFilter(request,response);

        Map env = (Map) request.getAttribute(Constants.VAR_ENVIRONMENT);
        if (env != null) {
            List<Tag> tags = (List<Tag>) env.get(Constants.VAR_ASSIGNED_TAGS);
            if (tags != null && ! tags.isEmpty()) {
                for (Tag tag : tags) {
                    statistics.recordView(Constants.TAG_PREFIX + tag.getId(), 1);
                }
            }
        }
    }

    public void destroy() {
    }
}
