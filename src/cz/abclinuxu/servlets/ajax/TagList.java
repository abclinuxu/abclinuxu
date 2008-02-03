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
package cz.abclinuxu.servlets.ajax;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.html.edit.EditTag;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.utils.TagTool;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.comparator.TagTitleComparator;
import cz.abclinuxu.data.Tag;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.data.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * Lists of tags according to criteria from the request.
 * @author literakl
 * @since 2.2.2008
 */
public class TagList implements AbcAction {
    private static Logger log = Logger.getLogger(TagList.class);

    public static final String PARAM_FILTER = "filter";
    public static final String PARAM_RELATION = "rid";
    public static final String PARAM_TAG_ID = "tagID";

    public static final String VAR_TAGS = "TAGS";
    public static final String VAR_CREATE_POSSIBLE = "CREATE_POSSIBLE";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        List<Tag> tags = Collections.emptyList();
        boolean canBeCreated = false;

        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String url = (String) env.get(Constants.VAR_REQUEST_URI);
        if ("/ajax/tags/list".equals(url)) {
            tags = TagTool.list(0, -1, TagTool.ListOrder.BY_TITLE, true);
            String filter = (String) params.get(PARAM_FILTER);
            filter =  (filter != null) ? filter.trim() : "";
            if (filter.length() > 0) {
                List<Tag> tmp = new ArrayList<Tag>(tags.size() / 26);
                for (Tag tag : tags) {
                    if (tag.getTitle().toLowerCase().startsWith(filter.toLowerCase()))
                        tmp.add(tag);
                }
                tags = tmp;

                if (filter.length() > 2) {
                    String id = TagTool.getNormalizedId(filter);
                    Tag existingTag = TagTool.getById(id);
                    if (existingTag == null)
                        canBeCreated = true;
                }
            }

        } else if ("/ajax/tags/favourite".equals(url)) {
            tags = TagTool.list(0, 30, TagTool.ListOrder.BY_USAGE, false);
            Collections.sort(tags, new TagTitleComparator(true));

        } else {
            int rid = Misc.parseInt((String) params.get(PARAM_RELATION), -1);
            if (rid == -1) {
                log.debug("Relation parameter is empty in AJAX request: " + url);
                return null;
            }

            Relation relation = new Relation(rid);
            try {
                Tools.sync(relation);
            } catch (Exception e) {
                log.debug("Cannot load relation " + rid + " in AJAX request: " + url, e);
                return null;
            }

            if ( ! (relation.getChild() instanceof GenericDataObject)) {
                log.debug("The relation " + rid + " does not contain GenericDataObject as child! " + relation.getChild());
                return null;
            }

            String ipAddress = ServletUtils.getClientIPAddress(request);
            User user = (User) env.get(Constants.VAR_USER);

            GenericDataObject object = (GenericDataObject) relation.getChild();
            if ("/ajax/tags/assigned".equals(url)) {
                // no action needed at this moment, it will be performed after the if statements

            } else if ("/ajax/tags/assign".equals(url)) {
                String tag = (String) params.get(PARAM_TAG_ID);
                TagTool.assignTags(object, Collections.singletonList(tag), user, ipAddress);

            } else if ("/ajax/tags/unassign".equals(url)) {
                String tag = (String) params.get(PARAM_TAG_ID);
                TagTool.unassignTags(object, Collections.singletonList(tag), user, ipAddress);

            } else if ("/ajax/tags/create".equals(url)) {
                Tag tag = new Tag();
                EditTag.setTitleAndId(params, tag, false, env);
                TagTool.create(tag, user, ipAddress);
                TagTool.assignTags(object, Collections.singletonList(tag.getId()), user, ipAddress);
            }

            tags = TagTool.getAssignedTags(object);
        }

        env.put(VAR_TAGS, tags);
        env.put(VAR_CREATE_POSSIBLE, canBeCreated);
        env.put(Constants.VAR_CONTENT_TYPE, "text/xml; charset=UTF-8");
        return "/print/ajax/tags.ftl";
    }
}
