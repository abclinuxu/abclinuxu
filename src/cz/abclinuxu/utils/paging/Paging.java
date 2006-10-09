/*
 *  Copyright (C) 2005 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
package cz.abclinuxu.utils.paging;

import cz.abclinuxu.persistence.extra.Qualifier;

import java.util.List;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Holds information about single page of listing
 * including its data and information about data as whole.
 */
public final class Paging {
    private Integer total;
    private Page currentPage;
    private int pageSize;
    private List data;
    private List qualifiers;

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
        this.total = total;
        this.pageSize = pageSize;
        this.data = data;
    }

    /**
     * Creates new Paging instance.
     * @param row initial row of this page (first element has row equal to zero).
     * @param pageSize default page size.
     * @param data list of objects within the page. It must not be null and its size shall be not bigger than pageSize.
     * @param total number of elements in whole set (e.g. sum of sizes of all Pages).
     * @param qualifiers List of qualifiers, that affected data selection of this page.
     */
    public Paging(List data, int row, int pageSize, int total, Qualifier[] qualifiers) {
        currentPage = new Page(row, data.size());
        this.total = total;
        this.pageSize = pageSize;
        this.data = data;
        if (qualifiers!=null)
            this.qualifiers = Arrays.asList(qualifiers);
    }

    /**
     * Creates new Paging instance.
     * @param row initial row of this page (first element has row equal to zero).
     * @param pageSize default page size.
     * @param data list of objects within the page. It must not be null and its size shall be not bigger than pageSize.
     * @param total number of elements in whole set (e.g. sum of sizes of all Pages).
     * @param qualifiers List of qualifiers, that affected data selection of this page.
     */
    public Paging(List data, int row, int pageSize, int total, List qualifiers) {
        currentPage = new Page(row, data.size());
        this.total = total;
        this.pageSize = pageSize;
        this.data = data;
        this.qualifiers = qualifiers;
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
        if ( row < total)
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
     * @return information about current Page.
     */
    public Page getThisPage() {
        return currentPage;
    }

    /**
     * @return size of this Paging. E.g. size of window, through which you look at whole set.
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * @return number of pages within this result set. If total is not set, null is returned.
     */
    public Integer getPageCount() {
        if (total==null)
            return null;
        int count = total / pageSize;
        if (total % pageSize != 0)
            count++;
        return count;
    }

    /**
     * @return index of page within the result set.
     */
    public Integer getPageIndex() {
        int row = currentPage.getRow();
        return row / pageSize;
    }

    /**
     * @return data of this Paging. Their size is equal to currentPage.size.
     */
    public List getData() {
        return data;
    }

    /**
     * @return List of qualifiers, that affected data selection of this page.
     */
    public List getQualifiers() {
        return qualifiers;
    }

    /**
     * Test, whether qualifier of given name was used during data selection of this page.
     */
    public boolean isQualifierSet(String name) {
        if (qualifiers==null) return false;
        for ( Iterator iter = qualifiers.iterator(); iter.hasNext(); ) {
            if ( iter.next().toString().equals(name))
                return true;
        }
        return false;
    }
}
