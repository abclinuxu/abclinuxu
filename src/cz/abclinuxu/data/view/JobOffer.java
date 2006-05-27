/*
 *  Copyright (C) 2006 Leos Literak
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

import java.util.Date;

/**
 * Job offer from praceabc.cz.
 * @author literakl
 * @since 26.5.2006
 */
public class JobOffer {
    private int id;
    private String title;
    private Date lastModified;
    private boolean itJob, linuxJob;
    private String category;
    private String region;
    private String company;
    private String jobType;

    public JobOffer(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isItJob() {
        return itJob;
    }

    public void setItJob(boolean itJob) {
        this.itJob = itJob;
    }

    public boolean isLinuxJob() {
        return linuxJob;
    }

    public void setLinuxJob(boolean linuxJob) {
        this.linuxJob = linuxJob;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobOffer)) return false;

        final JobOffer jobOffer = (JobOffer) o;

        if (id != jobOffer.id) return false;

        return true;
    }

    public int hashCode() {
        return id;
    }
}
