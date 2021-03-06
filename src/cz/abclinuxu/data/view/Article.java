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
package cz.abclinuxu.data.view;

import cz.abclinuxu.data.Relation;

import java.util.Date;
import java.util.Set;
import java.util.HashSet;

/**
 * Wrapper arround article.
 */
public class Article {
    private String title, perex, url;
    private Set<Relation> authors = new HashSet<Relation>(2);
    private Date published;
    private int relationId, comments, reads;

    public Article(String title, Date published, String url) {
        this.title = title;
        this.published = published;
        this.url = url;
    }

    public void setRelationId(int relationId) {
        this.relationId = relationId;
    }

    public void setPerex(String perex) {
        this.perex = perex;
    }

    public void addAuthor(Relation author) {
        this.authors.add(author);
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public int getReads() {
        return reads;
    }

    public void setReads(int reads) {
        this.reads = reads;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getPerex() {
        return perex;
    }

    public Set<Relation> getAuthors() {
        return authors;
    }

    public Date getPublished() {
        return published;
    }

    public int getRelationId() {
        return relationId;
    }

    public String getUrl() {
        return url;
    }
}
