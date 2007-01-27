/*
 *  Copyright (C) 2007 Leos Literak
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
 * Server connected to internet for rent.
 * @author literakl
 * @since 27.1.2007
 */
public class HostingServer {
    boolean action, newArrival;
    String name, description, url;
    String availability, bandwidth, transfers;
    String cpu, ram, disc, network;
    String imageUrl;
    HostingPrice price, setupFee;

    public HostingServer(String name, String url) {
        this.name = name;
        this.url = url;
    }

    /**
     * @return true if this server is in action
     */
    public boolean isAction() {
        return action;
    }

    public void setAction(boolean action) {
        this.action = action;
    }

    /**
     * @return true if this server is new in the offering
     */
    public boolean isNewArrival() {
        return newArrival;
    }

    public void setNewArrival(boolean newArrival) {
        this.newArrival = newArrival;
    }

    /**
     * @return name of the server
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return short description
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return url of the detail
     */
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return string label of server availability
     */
    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    /**
     * @return description of available bandwidth
     */
    public String getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(String bandwidth) {
        this.bandwidth = bandwidth;
    }

    /**
     * @return description of transfer terms
     */
    public String getTransfers() {
        return transfers;
    }

    public void setTransfers(String transfers) {
        this.transfers = transfers;
    }

    /**
     * @return cpu description
     */
    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    /**
     * @return RAM description
     */
    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    /**
     * @return hard disc(s) info
     */
    public String getDisc() {
        return disc;
    }

    public void setDisc(String disc) {
        this.disc = disc;
    }

    /**
     * @return networking card(s) description
     */
    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    /**
     * @return url of image
     */
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * @return price for renting this server
     */
    public HostingPrice getPrice() {
        return price;
    }

    public void setPrice(HostingPrice price) {
        this.price = price;
    }

    /**
     * @return optional setup fee
     */
    public HostingPrice getSetupFee() {
        return setupFee;
    }

    public void setSetupFee(HostingPrice setupFee) {
        this.setupFee = setupFee;
    }
}
