package cz.abclinuxu.persistance.versioning;

import java.util.Date;

/**
 * This class holds one version of the document with version metadata.
 * To make it as generic as possible, both version and
 * user are stored as String.
 * Date: 28.3.2005
 */
public class VersionedDocument {
    private String version;
    private String user;
    private Date commited;
    private String document;

    /**
     * @return document
     */
    public String getDocument() {
        return document;
    }

    /**
     * Sets document.
     * @param document
     */
    public void setDocument(String document) {
        this.document = document;
    }

    /**
     * @return version string
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets version. Maximum length of version is 25.
     * @param version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return identificator of the user, who commited this version
     */
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return time, when this version has been commited
     */
    public Date getCommited() {
        return commited;
    }

    public void setCommited(Date commited) {
        this.commited = commited;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VersionedDocument)) return false;

        final VersionedDocument versionedDocument = (VersionedDocument) o;

//        if (!commited.equals(versionedDocument.commited)) return false;
//        if (!document.equals(versionedDocument.document)) return false;
        if (!user.equals(versionedDocument.user)) return false;
        if (!version.equals(versionedDocument.version)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = version.hashCode();
        result = 29 * result + user.hashCode();
        result = 29 * result + commited.hashCode();
        result = 29 * result + document.hashCode();
        return result;
    }
}
