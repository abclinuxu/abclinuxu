package cz.abclinuxu.data.view;

import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Relation;

import java.util.Date;

/**
 * View bean for story in blog.
 * User: literakl
 * Date: 20.2.2009
 */
public class BlogStory {
    String url, title, perex, content, blogUrl, blogTitle;
    User author;
    Relation relation;
    BlogCategory category;
    Date created;
    DiscussionHeader discussion;
    boolean digest, displayLinkMore;
    int images, videos, polls;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBlogUrl() {
        return blogUrl;
    }

    public void setBlogUrl(String blogUrl) {
        this.blogUrl = blogUrl;
    }

    public String getBlogTitle() {
        return blogTitle;
    }

    public void setBlogTitle(String blogTitle) {
        this.blogTitle = blogTitle;
    }

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
    }

    public int getId() {
        return relation.getId();
    }

    public String getPerex() {
        return perex;
    }

    public void setPerex(String perex) {
        this.perex = perex;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public BlogCategory getCategory() {
        return category;
    }

    public void setCategory(BlogCategory category) {
        this.category = category;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public DiscussionHeader getDiscussion() {
        return discussion;
    }

    public void setDiscussion(DiscussionHeader discussion) {
        this.discussion = discussion;
    }

    public boolean isDigest() {
        return digest;
    }

    public void setDigest(boolean digest) {
        this.digest = digest;
    }

    public boolean isDisplayLinkMore() {
        return displayLinkMore;
    }

    public void setDisplayLinkMore(boolean displayLinkMore) {
        this.displayLinkMore = displayLinkMore;
    }

    public int getImages() {
        return images;
    }

    public void setImages(int images) {
        this.images = images;
    }

    public int getVideos() {
        return videos;
    }

    public void setVideos(int videos) {
        this.videos = videos;
    }

    public int getPolls() {
        return polls;
    }

    public void setPolls(int polls) {
        this.polls = polls;
    }
}
