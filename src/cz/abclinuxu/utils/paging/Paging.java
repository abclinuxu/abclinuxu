/*
 * User: Leos Literak
 * Date: Aug 12, 2003
 * Time: 7:13:34 PM
 */
package cz.abclinuxu.utils.paging;

import java.util.List;

/**
 * Holds information about single page of listing
 * including its data and information about data as whole.
 */
public final class Paging {

    private Integer total;
    private Page currentPage;
    private int pageSize;
    private List data;

    /**
     * Creates new Paging instance.
     * @param row initial row of this page (first element has row equal to zero).
     * @param pageSize default page size.
     * @param data list of objects within the page. It must not be null and its size shall be not bigger than pageSize.
     */
    public Paging(List data, int row, int pageSize) {
        currentPage = new Page(row, data.size());
        this.pageSize = pageSize;
        this.data = data;
    }

    /**
     * Creates new Paging instance.
     * @param row initial row of this page (first element has row equal to zero).
     * @param pageSize default page size.
     * @param data list of objects within the page. It must not be null and its size shall be not bigger than pageSize.
     * @param total number of elements in whole set (e.g. sum of sizes of all Pages).
     */
    public Paging(List data, int row, int pageSize, int total) {
        currentPage = new Page(row, data.size());
        this.total = new Integer(total);
        this.pageSize = pageSize;
        this.data = data;
    }

    /**
     * Calculates next Page of this Paging. Next Page is defined as sum of current row and page size.
     * If next page's current row would break Paging limits (e.g. it will be bigger than total),
     * null is returned.
     * @return next Page or null, if it is not available.
     */
    public Page getNextPage() {
        if ( total==null ) {
            if ( currentPage.size<pageSize )
                return null;
            return new Page(currentPage.row+pageSize,0);
        }
        int row = currentPage.row+pageSize;
        if ( row<total.intValue() )
            return new Page(row,0);
        return null;
    }

    /**
     * Calculates previous Page of this Paging. Previous Page is defined as current row minus page size.
     * If we are at start of whole set (row is 0), null is returned.
     * @return previous Page or null
     */
    public Page getPrevPage() {
        if ( currentPage.row==0 )
            return null;
        int row = currentPage.row - pageSize;
        if ( row<0 )
            row = 0;
        return new Page(row,0);
    }

    /**
     * @return number of elements in whole set (e.g. sum of sizes of all Pages).
     */
    public Integer getTotal() {
        return total;
    }

    /**
     * @return information about current Page.
     */
    public Page getCurrentPage() {
        return currentPage;
    }

    /**
     * @return size of this Paging. E.g. size of window, through which you look at whole set.
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * @return data of this Paging. Their size is equal to currentPage.size.
     */
    public List getData() {
        return data;
    }
}
