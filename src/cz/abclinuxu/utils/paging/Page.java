/*
 * User: Leos Literak
 * Date: Aug 12, 2003
 * Time: 7:13:34 PM
 */
package cz.abclinuxu.utils.paging;

/**
 * This class encapsulates information about single page of listing.
 */
public final class Page {

    int row, size;

    /**
     * Can be instantiated only within this package.
     * @param row index of first element within whole set, starts at 0.
     * @param size number of elements within this page.
     */
    Page(int row, int size) {
        this.row = row;
        this.size = size;
    }

    /**
     * Gets initial row of this page.
     * @return  row, where page starts.
     */
    public int getRow() {
        return row;
    }

    /**
     * Gets size of this page.
     * @return number of rows, which are contained by this page.
     */
    public int getSize() {
        return size;
    }
}
