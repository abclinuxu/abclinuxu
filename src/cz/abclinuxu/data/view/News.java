/*
 * User: literakl
 * Date: 28.8.2004
 * Time: 19:59:21
 */
package cz.abclinuxu.data.view;

import java.util.Date;

/**
 * Warpper arround news.
 */
public class News {
    String content, author;
    Date published;
    int relationId, comments, authorId;

    public News(String content, Date published, int relationId) {
        this.content = content;
        this.published = published;
        this.relationId = relationId;
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

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public Date getPublished() {
        return published;
    }

    public int getRelationId() {
        return relationId;
    }

    public int getComments() {
        return comments;
    }
}
