package cz.abclinuxu.data;

/**
 * This interface marks object to have ACL included.
 * Allows object to determine its owner
 * @author kapy
 * @since 22/05/2009
 *
 */
public interface AccessControllable {

    /**
     * Gets total permissions for object
     * @return Total permissions
     */
    public int getPermissions();
    
    /**
     * Sets total permissions for object
     * @param permissions New permissions
     */
    public void setPermissions(int permissions);
   
    /**
     * Gets owner of object 
     * @return Owner identification
     */
    public int getOwner();
    
    /**
     * Sets owner of object
     * @param owner New owner
     */
    public void setOwner(int owner);
         
    /**
     * Gets group in which object is stored
     * @return Group identification
     */
    public int getGroup();
    
    /**
     * Sets group of object
     * @param group New group
     */
    public void setGroup(int group);
    
    /**
     * Checks whether owner determined by id is owner of object.
     * This allows object to have different owner mechanisms
     * @param owner Identification of owner
     * @return {@code true} if it is owner, {@code false} otherwise
     */
    public boolean determineOwnership(int owner);
}
