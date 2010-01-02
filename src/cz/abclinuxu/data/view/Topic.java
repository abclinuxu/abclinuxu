package cz.abclinuxu.data.view;

import cz.abclinuxu.data.Relation;

import java.util.Date;

/**
 * Bean for topic in redaction system.
 */
public class Topic {
	private int id;
    private int relationId;
	private String title;
    private String description;
    // date when topic should be delivered in article
	private Date deadline;
	// author assigned to this topic, null for public topics
	private Author author;
    // royalty offered, null for default royalty
    private Integer royalty;
    // initialized relation to article associated with this article
	private Relation article;
    private ArticleState articleState;

    /**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

    /**
     * @return id of relation to this topic
     */
    public int getRelationId() {
        return relationId;
    }

    public void setRelationId(int relationId) {
        this.relationId = relationId;
    }

    /**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the deadline
	 */
	public Date getDeadline() {
		return deadline;
	}

	/**
	 * @param deadline the deadline to set
	 */
	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}

	/**
	 * @return the author
	 */
	public Author getAuthor() {
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(Author author) {
		this.author = author;
	}

    /**
     * @return initialized relation to associated article
     */
    public Relation getArticle() {
        return article;
    }

    public void setArticle(Relation article) {
        this.article = article;
    }

    /**
	 * @return the royalty
	 */
	public Integer getRoyalty() {
		return royalty;
	}

	/**
	 * @param royalty the royalty to set
	 */
	public void setRoyalty(Integer royalty) {
		this.royalty = royalty;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

    /**
     * @return state of associated article
     */
    public ArticleState getArticleState() {
        return articleState;
    }

    public void setArticleState(ArticleState articleState) {
        this.articleState = articleState;
    }

    /**
	 * Checks availability of topic to authors 
	 * @return {@code true} if this topic can be accepted by any author,
	 * {@code false} otherwise
	 */
	public boolean isPublic() {
		return author == null;
	}
	
	/**
	 * Checks whether special royalty is assigned to the topic
	 * @return {@code true} if individual royalty is is assigned to the topic,
	 * {@code false} otherwise 
	 */
	public boolean hasRoyalty() {
		return royalty != null;
	}
	
	/**
	 * Checks whether this article is in delay
	 * @return {@code true} if the topic in in delay, {@code false} otherwise
	 */
	public boolean isDelayed() {
		return (new Date()).after(deadline);
	}

    /**
     * Constants for states in which an article can be
     */
    public enum ArticleState {
        /** there is no article */
        NONE,
        /** article is being written */
        DRAFT,
        /** author finished an article and submitted it to editor */
        SUBMITTED,
        /** editor is satisfied with article and accepts it */
        ACCEPTED,
        /** editor finished his changes and scheduled an article */
        READY,
        /** article was published, visitors can read it */
        PUBLISHED;

        /**
         * Finds constant for integer representation
         * @param state state, null or 0-4
         * @return enum or null for unsupported values
         */
        public static ArticleState get(Integer state) {
            if (state == null)
                return PUBLISHED; // legacy data
            switch (state) {
                case 0:
                    return PUBLISHED;
                case 1:
                    return DRAFT;
                case 2:
                    return READY;
                case 3:
                    return ACCEPTED;
                case 4:
                    return SUBMITTED;
                default:
                    return null;
            }
        }
    }
}
