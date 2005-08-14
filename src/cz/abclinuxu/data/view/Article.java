/*
 * User: literakl
 * Date: 28.8.2004
 * Time: 19:59:40
 */
package cz.abclinuxu.data.view;

import java.util.Date;

/**
 * Wrapper arround article.
 */
public class Article {
    private String title, perex, author, url;
    private Date published;
    private int relationId, comments, reads, authorId;

    public Article(String title, Date published, int relationId) {
        this.title = title;
        this.published = published;
        this.relationId = relationId;
    }

    public void setPerex(String perex) {
        this.perex = perex;
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

    public String getAuthor() {
        return author;
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
