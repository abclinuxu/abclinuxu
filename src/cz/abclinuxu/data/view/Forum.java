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
package cz.abclinuxu.data.view;

/**
 * View for summary information about some forum
 * User: literakl
 * Date: 18.2.2007
 */
public class Forum {
    private SectionNode section;
    private DiscussionHeader discussion;

    public Forum(SectionNode section) {
        this.section = section;
    }

    public String getName() {
        return section.getName();
    }

    public String getUrl() {
        return section.getUrl();
    }

    public int getSize() {
        return section.getSize();
    }

    public DiscussionHeader getLastQuestion() {
        return discussion;
    }

    public SectionNode getSection() {
        return section;
    }

    public void setSection(SectionNode section) {
        this.section = section;
    }

    public void setDiscussion(DiscussionHeader discussion) {
        this.discussion = discussion;
    }
}
