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

import cz.abclinuxu.utils.format.HtmlToTextFormatter;

import java.util.Date;

/**
 * Warpper arround news.
 */
public class News {
    String title, content, author, url;
    Date published;
    int relationId, comments, authorId;

    public News(String title, String content, Date published, String url) {
        this.title = title;
        this.content = content;
        this.published = published;
        this.url = url;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public String getPlainTextContent() {
        return HtmlToTextFormatter.format(this.content);
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public Date getPublished() {
        return published;
    }

    public void setRelationId(int relationId) {
        this.relationId = relationId;
    }

    public int getRelationId() {
        return relationId;
    }

    public int getComments() {
        return comments;
    }

    public String getUrl() {
        return url;
    }
}
