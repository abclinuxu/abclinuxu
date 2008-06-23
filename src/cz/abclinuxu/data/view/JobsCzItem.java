/*
 *  Copyright (C) 2008 Karel Piwko
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

package cz.abclinuxu.data.view;

/**
 * Holder of parsed item from XML source of jobs.cz
 * @author kapy
 */
public class JobsCzItem {
    
    private String url, positionName, companyName, createDate;
    private String[] skills, localities;

    /**
     * Gets URL address of current item
     * @return URL of item
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets URL address of item
     * @param url URL of item
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets name of position
     * @return Name of position
     */
    public String getPositionName() {
        return positionName;
    }

    /**
     * Sets name of position
     * @param positionName New position name
     */
    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    /**
     * Gets name of company which provides the job
     * @return Name of company
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Sets name of company
     * @param companyName
     */
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /**
     * Gets skills required to take position
     * @return Required skills
     */
    public String[] getSkills() {
        return skills;
    }

    /**
     * Sets skills required to take position
     * @param skills Required skills
     */
    public void setSkills(String[] skills) {
        this.skills = skills;
    }

    /**
     * Gets localities of job
     * @return Job's localities
     */
    public String[] getLocalities() {
        return localities;
    }

    /**
     * Sets localities of job
     * @param localities Job's localities
     */
    public void setLocalities(String[] localities) {
        this.localities = localities;
    }

    /**
     * Gets date when job place offert was created
     * @return Created date
     */
    public String getCreateDate() {
        return createDate;
    }

    /**
     * Sets date when job place offert was created
     * @param createDate Day when job was created
     */
    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
    
}
