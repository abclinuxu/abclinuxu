package cz.abclinuxu.data.view;

import cz.abclinuxu.data.AccessControllable;
import cz.abclinuxu.data.ImageAssignable;
import cz.abclinuxu.utils.freemarker.Tools;

import java.util.Date;

/**
 * Handles author for purposes of editors.
 * 
 * @author kapy
 */
public class Author implements Cloneable, ImageAssignable<Author.AuthorImage>, AccessControllable {
	public enum AuthorImage implements ImageAssignable.AssignedImage {
		PHOTO
	}

    public enum ContractStatus {
        UNSIGNED, CURRENT, OBSOLETE
    }

	private int id;

	private Integer uid;
    // identification of relation to this author
    private int relationId;
    private Integer contractId;
    private ContractStatus contractStatus;

    private boolean active;

	private String name, surname, login, nickname;

	private String birthNumber, accountNumber;

	private String address;

	private String email, phone;

	private String photoUrl;

	private String about;

	private int articleCount;

	private Date lastArticleDate;

	private int permissions, group, owner;

	/**
	 * Creates empty author
	 */
	public Author() {
	}

	/**
	 * Creates author with given id
	 * 
	 * @param id Identification of author
	 */
	public Author(int id) {
		this.id = id;
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
	 * @return the uid
	 */
	public Integer getUid() {
		return uid;
	}

	/**
	 * @param uid the uid to set
	 */
	public void setUid(Integer uid) {
		this.uid = uid;
	}

    /**
     * @return id of relation to this author
     */
    public int getRelationId() {
        return relationId;
    }

    public void setRelationId(int relationId) {
        this.relationId = relationId;
    }

    /**
     * @return id of item containing last contract template signed by this author
     */
    public Integer getContractId() {
        return contractId;
    }

    public void setContractId(Integer contractId) {
        this.contractId = contractId;
    }

    /**
     * @return status of signed contract
     */
    public ContractStatus getContractStatus() {
        return contractStatus;
    }

    public void setContractStatus(ContractStatus contractStatus) {
        this.contractStatus = contractStatus;
    }

    /**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the surname
	 */
	public String getSurname() {
		return surname;
	}

	/**
	 * @param surname the surname to set
	 */
	public void setSurname(String surname) {
		this.surname = surname;
	}

	/**
	 * @return the login
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * @param login the login to set
	 */
	public void setLogin(String login) {
		this.login = login;
	}

	/**
	 * @return the nickname
	 */
	public String getNickname() {
		return nickname;
	}

	/**
	 * @param nickname the nickname to set
	 */
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	/**
	 * @return the birthNumber
	 */
	public String getBirthNumber() {
		return birthNumber;
	}

	/**
	 * @param birthNumber the birthNumber to set
	 */
	public void setBirthNumber(String birthNumber) {
		this.birthNumber = birthNumber;
	}

	/**
	 * @return the accountNumber
	 */
	public String getAccountNumber() {
		return accountNumber;
	}

	/**
	 * @param accountNumber the accountNumber to set
	 */
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the phone
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * @param phone the phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * @return the photoUrl
	 */
	public String getPhotoUrl() {
		return photoUrl;
	}

	/**
	 * @param photoUrl the photoUrl to set
	 */
	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	/**
	 * @return the about
	 */
	public String getAbout() {
		return about;
	}

	/**
	 * @param about the about to set
	 */
	public void setAbout(String about) {
		this.about = about;
	}

	/**
	 * @return the articleCount
	 */
	public int getArticleCount() {
		return articleCount;
	}

	/**
	 * @param articleCount the articleCount to set
	 */
	public void setArticleCount(int articleCount) {
		this.articleCount = articleCount;
	}

	/**
	 * @return the lastArticleDate
	 */
	public Date getLastArticleDate() {
		return lastArticleDate;
	}

	/**
	 * @param lastArticleDate the lastArticleDate to set
	 */
	public void setLastArticleDate(Date lastArticleDate) {
		this.lastArticleDate = lastArticleDate;
	}

	/**
	 * Constructs title (name with surname) for author
	 * 
	 * @return Full name representation of author
	 */
	public String getTitle() {
		return Tools.getPersonName(this);
	}

	@Override
	public void assignImage(AuthorImage imageId, String imageUrl) {
		this.photoUrl = imageUrl;
	}

	@Override
	public String detractImage(AuthorImage imageId) {
		String url = this.photoUrl;
		this.photoUrl = null;
		return url;
	}

	@Override
	public String proposeImageUrl(AuthorImage imageId, String suffix) {
		StringBuilder sb = new StringBuilder();
		sb.append("images/authors/").append(name).append('-').append(surname);

		if (uid != null)
			sb.append("-u-").append(uid);
		else if (id != 0)
		    sb.append('-').append(id);

		return sb.append('.').append(suffix).toString();
	}

	@Override
	public int getGroup() {
		return this.group;
	}

	@Override
	public int getOwner() {
		return this.owner;
	}

	@Override
	public int getPermissions() {
		return this.permissions;
	}

	@Override
	public void setGroup(int group) {
		this.group = group;
	}

	@Override
	public void setOwner(int owner) {
		this.owner = owner;
	}

	@Override
	public void setPermissions(int permissions) {
		this.permissions = permissions;
	}

	@Override
	public boolean determineOwnership(int owner) {
		return this.owner == owner || this.uid == owner;
	}

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
	 * Returns a brief description of author, in general his id in system, name
	 * and surname
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Author: ").append(id).append(", ").append(getTitle())
		        .append(active ? "active" : "");
		return sb.toString();
	}
}
