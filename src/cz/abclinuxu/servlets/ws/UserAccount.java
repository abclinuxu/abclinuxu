/*
 *  Copyright (C) 2008 Leos Literak
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
package cz.abclinuxu.servlets.ws;

import java.io.Serializable;

/**
 * Wrapper for LDAP object holding info about user account. This class must be
 * synchronized with changes to LdapUserManager and file stickfish.schema.
 * @author literakl
 * @since 24.4.2008
 */
public class UserAccount implements Serializable {
    String city;
    String country;
    String deliveryAddressCity;
    String deliveryAddressCountry;
    String deliveryAddressName;
    String deliveryAddressStreet;
    String deliveryAddressZIP;
    String emailAddress;
    String emailBlocked;
    String emailVerified;
    String forgottenPasswordToken;
    String homepageURL;
    String invoicingAddressCity;
    String invoicingCompany;
    String invoicingAddressCountry;
    String invoicingCompanyDIC;
    String invoicingCompanyICO;
    String invoicingAddressName;
    String invoicingAddressStreet;
    String invoicingAddressZIP;
    String lastLoginDate;
    String login;
    String name;
    String openID;
    String userPassword;
    String passwordAnswer;
    String passwordHash;
    String passwordQuestion;
    String registrationDate;
    String sex;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDeliveryAddressCity() {
        return deliveryAddressCity;
    }

    public void setDeliveryAddressCity(String deliveryAddressCity) {
        this.deliveryAddressCity = deliveryAddressCity;
    }

    public String getDeliveryAddressCountry() {
        return deliveryAddressCountry;
    }

    public void setDeliveryAddressCountry(String deliveryAddressCountry) {
        this.deliveryAddressCountry = deliveryAddressCountry;
    }

    public String getDeliveryAddressName() {
        return deliveryAddressName;
    }

    public void setDeliveryAddressName(String deliveryAddressName) {
        this.deliveryAddressName = deliveryAddressName;
    }

    public String getDeliveryAddressStreet() {
        return deliveryAddressStreet;
    }

    public void setDeliveryAddressStreet(String deliveryAddressStreet) {
        this.deliveryAddressStreet = deliveryAddressStreet;
    }

    public String getDeliveryAddressZIP() {
        return deliveryAddressZIP;
    }

    public void setDeliveryAddressZIP(String deliveryAddressZIP) {
        this.deliveryAddressZIP = deliveryAddressZIP;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailBlocked() {
        return emailBlocked;
    }

    public void setEmailBlocked(String emailBlocked) {
        this.emailBlocked = emailBlocked;
    }

    public String getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(String emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getForgottenPasswordToken() {
        return forgottenPasswordToken;
    }

    public void setForgottenPasswordToken(String forgottenPasswordToken) {
        this.forgottenPasswordToken = forgottenPasswordToken;
    }

    public String getHomepageURL() {
        return homepageURL;
    }

    public void setHomepageURL(String homepageURL) {
        this.homepageURL = homepageURL;
    }

    public String getInvoicingAddressCity() {
        return invoicingAddressCity;
    }

    public void setInvoicingAddressCity(String invoicingAddressCity) {
        this.invoicingAddressCity = invoicingAddressCity;
    }

    public String getInvoicingCompany() {
        return invoicingCompany;
    }

    public void setInvoicingCompany(String invoicingCompany) {
        this.invoicingCompany = invoicingCompany;
    }

    public String getInvoicingAddressCountry() {
        return invoicingAddressCountry;
    }

    public void setInvoicingAddressCountry(String invoicingAddressCountry) {
        this.invoicingAddressCountry = invoicingAddressCountry;
    }

    public String getInvoicingCompanyDIC() {
        return invoicingCompanyDIC;
    }

    public void setInvoicingCompanyDIC(String invoicingCompanyDIC) {
        this.invoicingCompanyDIC = invoicingCompanyDIC;
    }

    public String getInvoicingCompanyICO() {
        return invoicingCompanyICO;
    }

    public void setInvoicingCompanyICO(String invoicingCompanyICO) {
        this.invoicingCompanyICO = invoicingCompanyICO;
    }

    public String getInvoicingAddressName() {
        return invoicingAddressName;
    }

    public void setInvoicingAddressName(String invoicingAddressName) {
        this.invoicingAddressName = invoicingAddressName;
    }

    public String getInvoicingAddressStreet() {
        return invoicingAddressStreet;
    }

    public void setInvoicingAddressStreet(String invoicingAddressStreet) {
        this.invoicingAddressStreet = invoicingAddressStreet;
    }

    public String getInvoicingAddressZIP() {
        return invoicingAddressZIP;
    }

    public void setInvoicingAddressZIP(String invoicingAddressZIP) {
        this.invoicingAddressZIP = invoicingAddressZIP;
    }

    public String getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(String lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOpenID() {
        return openID;
    }

    public void setOpenID(String openID) {
        this.openID = openID;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getPasswordAnswer() {
        return passwordAnswer;
    }

    public void setPasswordAnswer(String passwordAnswer) {
        this.passwordAnswer = passwordAnswer;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordQuestion() {
        return passwordQuestion;
    }

    public void setPasswordQuestion(String passwordQuestion) {
        this.passwordQuestion = passwordQuestion;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}