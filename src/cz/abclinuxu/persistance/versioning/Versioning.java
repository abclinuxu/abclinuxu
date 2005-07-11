package cz.abclinuxu.persistance.versioning;

import java.util.List;

/**
 * Interface to access versioning repository. It supports to
 * store (latest) version of document, load selected
 * document in given version and load information about all
 * versions of selected document. The document can hold any data,
 * for example serialized xml.
 * User: literakl
 * Date: 27.3.2005
 */
public interface Versioning {

    /**
     * Stores latest version of document into versioning repository.
     * @param document document to be stored
     * @param path path that uniquely identifies the document. Maximum length is 255 characters.
     * @param user identifier of the user who commited this version. Maximum length is 50 characters.
     * @return information about this version
     */
    public VersionInfo commit(String document, String path, String user);

    /**
     * Loads document identified by path in selected version.
     * @param path unique identifier of the document
     * @param version version to be fetched
     * @return document with versioning metadata
     * @throws VersionNotFoundException Thrown when either document or specified version doesn't exist.
     */
    public VersionedDocument load(String path, String version) throws VersionNotFoundException;

    /**
     * Loads versioning history for selected document in descending order.
     * @param path unique identifier of the document
     * @return list of VersionInfo objects. When the list is empty, then there is no
     * version of specified document.
     */
    public List getHistory(String path);
}
