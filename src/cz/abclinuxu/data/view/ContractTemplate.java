package cz.abclinuxu.data.view;

import cz.abclinuxu.data.ImageAssignable;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.utils.PathGenerator;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.config.impl.AbcConfig;

import java.util.Date;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * This class represents template for authors' contracts.
 * 
 * @author kapy & Leos
 */
public class ContractTemplate implements ImageAssignable {
    private final Logger log = Logger.getLogger("cz.abclinuxu.data.view.ContractTemplate");

	// identification of contract
	private int id;
	// identification of relation to this contract
	private int relationId;
    // title of contract
    private String title;
    // description of contract
	private String description;
	// whole content
	private String content;
    // flag indicating that there newer contract template has been published
    private boolean obsolete;
    // flag indicating whether this contract template has been published or not
    private boolean draft;
    // if this template has been published, then this value contains date, when this action was performed
    private Date published;
    // if this template has been published, then it contains number of signatures
    private int signedContracts;

    public ContractTemplate() {
	}

	public ContractTemplate(ContractTemplate clone) {
        this.id = clone.id;
        this.relationId = clone.relationId;
        this.title = clone.title;
		this.description = clone.description;
        this.content = clone.content;
        this.obsolete = clone.obsolete;
        this.draft = clone.draft;
        this.published = clone.published;
        this.signedContracts = clone.signedContracts;
	}

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
     * @return id of relation to this contract template
     */
    public int getRelationId() {
        return relationId;
    }

    public void setRelationId(int relationId) {
        this.relationId = relationId;
    }

    /**
	 * @return set if this contract is obsolete
	 */
	public boolean isObsolete() {
		return obsolete;
	}

	/**
	 * @param obsolete the obsolete to set
	 */
	public void setObsolete(boolean obsolete) {
		this.obsolete = obsolete;
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
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

    /**
     * @return true, if this template has not been published yet
     */
    public boolean isDraft() {
        return draft;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    /**
     * @return date when this template has been published
     */
    public Date getPublished() {
        return published;
    }

    public void setPublished(Date published) {
        this.published = published;
    }

    /**
     * @return number of authors that signed this contract
     */
    public int getSignedContracts() {
        return signedContracts;
    }

    public void setSignedContracts(int signedContracts) {
        this.signedContracts = signedContracts;
    }

    @Override
    public void assignImage(Enum imageId, String imageUrl) {
        content = content.concat("<img src=\"" + imageUrl + "\">");
    }

    @Override
    /**
     * Delete operation is not implemented.
     */
    public String detractImage(Enum imageId) {
        return null;
    }

    @Override
    public String proposeImageUrl(Enum imageId, String suffix) {
        try {
            PathGenerator pathGenerator = AbcConfig.getPathGenerator();
            File imageFile = pathGenerator.getPath(new Item(id), PathGenerator.Type.SCREENSHOT, "cntrct", "." + suffix);
            return Misc.getWebPath(imageFile.getAbsolutePath()).substring(1);
        } catch (IOException e) {
            log.error("Cannot generate path to store picture!", e);
            return "image" + id + "." + suffix;
        }
    }

    public enum Image implements ImageAssignable.AssignedImage {
        PICTURE
    }
}
