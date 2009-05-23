package cz.abclinuxu.data.view;

import java.util.Date;

import cz.abclinuxu.data.AccessControllable;
import cz.abclinuxu.data.ImageAssignable;

/**
 * Handles author for purposes of editors.
 * 
 * @author kapy
 * 
 */
public class Author implements ImageAssignable, AccessControllable {

	public static final int AUTHOR_PHOTO = 1;
	
	private int id;

	private Integer uid;

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
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
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
	 * @param uid
	 *            the uid to set
	 */
	public void setUid(Integer uid) {
		this.uid = uid;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active
	 *            the active to set
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
	 * @param name
	 *            the name to set
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
	 * @param surname
	 *            the surname to set
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
	 * @param login
	 *            the login to set
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
	 * @param nickname
	 *            the nickname to set
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
	 * @param birthNumber
	 *            the birthNumber to set
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
	 * @param accountNumber
	 *            the accountNumber to set
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
	 * @param address
	 *            the address to set
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
	 * @param email
	 *            the email to set
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
	 * @param phone
	 *            the phone to set
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
	 * @param photoUrl
	 *            the photoUrl to set
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
	 * @param about
	 *            the about to set
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
	 * @param articleCount
	 *            the articleCount to set
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
	 * @param lastArticleDate
	 *            the lastArticleDate to set
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
		StringBuilder sb = new StringBuilder();
		if (name != null)
			sb.append(name).append(" ");
		if (surname != null)
			sb.append(surname);

		return sb.toString();
	}

	public void assignImage(int imageNo, String imageUrl) {
		this.photoUrl = imageUrl;
	}

	public String detractImage(int imageNo) {
		String url = this.photoUrl;
		this.photoUrl = null;
		return url;
	}

	public String proposeImageUrl(int imageNo, String suffix) {
		StringBuilder sb = new StringBuilder();
		sb.append("images/authors/").append(name).append('-').append(surname);
		
		if(uid!=null)
			sb.append("-u-").append(uid);
		else if(id!=0)
			sb.append('-').append(id);
		
		return sb.append('.').append(suffix).toString();
	}

	public int getGroup() {
	    return this.group;
	}

	public int getOwner() {
	    return this.owner;
	}

	public int getPermissions() {
	    return this.permissions;
	}

	public void setGroup(int group) {
	    this.group = group;	    
	}

	public void setOwner(int owner) {
	    this.owner = owner;	    
	}

	public void setPermissions(int permissions) {
	    this.permissions = permissions;   
	}

	public boolean determineOwnership(int owner) {
	    return this.owner == owner || this.uid == owner;
	}

}
