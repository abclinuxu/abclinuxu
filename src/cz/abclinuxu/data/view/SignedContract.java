package cz.abclinuxu.data.view;

import java.util.Date;

/**
 * This bean contains information about contract signed by author.
 * User: literakl
 * Date: 6.12.2009
 */
public class SignedContract {
    // identification of contract
    private int id;
    // identification of relation to this contract
    private int relationId;
    // user id of author that signed this contract
    private int uid;
    // author that signed this contract, should be in sync with uid
    private Author author;
    // identifier of contract template
    private int template;
    // title of contract
    private String title;
    // content of the contract (template with parameters replaced by real values)
    private String content;
    // date when this contract has been signed
    private Date signed;
    // IP address of computer used during signing this contract
    private String ipAddress;
    // flag indicating that there newer contract template has been published
    private boolean obsolete;

    /**
     * @return id of this signed contract
     */
    public int getId() {
        return id;
    }

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
     * @return Author that signed this contract
     */
    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    /**
     * @return user id of author that signed this contract
     */
    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    /**
     * @return id of template from which this treaty was generated
     */
    public int getTemplate() {
        return template;
    }

    public void setTemplate(int template) {
        this.template = template;
    }

    /**
     * Title, dynamically loaded from contract template
     * @return title of this contract
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Text of the contract, it shall be set once during signing and then never changed
     * @return
     */
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Flag indicating whether associated contract template has been obsoleted new newer template.
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
     * @return date when this contract was signed
     */
    public Date getSigned() {
        return signed;
    }

    public void setSigned(Date signed) {
        this.signed = signed;
    }

    /**
     * @return IP address of computer used for signing this contract
     */
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
