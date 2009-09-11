package cz.abclinuxu.data.view;

import java.util.Date;

public class Topic {

	// identification of topic
	private int id;

	// title of topic
	private String title;
	// date when topic should be done
	private Date deadline;

	// author which is assigned to this topic 
	private Author author;
	// flag to mark topic as accepted
	private boolean accepted;
	// flag to mark topic's work as published
	private boolean published;

	// royalty offered, if empty standard royalty
	private Double royalty;

	// description of topic
	private String description;

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
	 * @return the accepted
	 */
	public boolean isAccepted() {
		return accepted;
	}

	/**
	 * @param accepted the accepted to set
	 */
	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	/**
	 * @return the published
	 */
	public boolean isPublished() {
		return published;
	}

	/**
	 * @param published the published to set
	 */
	public void setPublished(boolean published) {
		this.published = published;
	}

	/**
	 * @return the royalty
	 */
	public Double getRoyalty() {
		return royalty;
	}

	/**
	 * @param royalty the royalty to set
	 */
	public void setRoyalty(Double royalty) {
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
	public boolean isInDelay() {
		return (new Date()).after(deadline);
	}
}
