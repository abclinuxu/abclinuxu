/*
 * User: literakl
 * Date: 21.12.2003
 * Time: 14:01:32
 */
package cz.abclinuxu.persistance.extra;


/**
 * Sets limit of returned data.
 */
public class LimitQualifier extends Qualifier {

    private int offset, count;

    public LimitQualifier(int offset, int count) {
        super("LIMIT_QUALIFIER");
        this.offset = offset;
        this.count = count;
    }

    /**
     * @return Data shall be fetched from this position.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * @return Do not load more than this number of rows.
     */
    public int getCount() {
        return count;
    }
}
