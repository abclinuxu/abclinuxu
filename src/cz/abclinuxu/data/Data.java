/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

/**
 * Class for storage various binary objects like images,
 * sounds or video
 */
public class Data extends GenericObject {
    /** owner of this object */
    protected int owner;
    /** data of this object */
    protected byte[] data;
    /** MIME type or "URL", if <code>data</code> contains URL */
    protected String format;


    public Data(int id) {
        super(id);
    }

    /**
     * @return owner's id
     */
    public int getOwner() {
        return owner;
    }

    /**
     * sets owner's id
     */
    public void setOwner(int owner) {
        this.owner = owner;
    }

    /**
     * @return binary data or URL
     */
    public byte[] getData() {
        return data;
    }

    /**
     * sets binary data or URL
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * @return MIME type or "URL", if <code>data</code> contains URL
     */
    public String getFormat() {
        return format;
    }

    /**
     * sets MIME type. Use "URL", if <code>data</code> contains URL
     */
    public void setFormat(String format) {
        this.format = format;
    }
}
