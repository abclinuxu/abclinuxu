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
 * Price bean
 * @author literakl
 * @since 27.1.2007
 */
public class HostingPrice {
    float price, vat;
    String paymentPeriod, currency;

    /**
     * Constructs new price bean
     * @param price price without value added tax
     * @param vat value added tax (e.g. 0.19 for 19% vat)
     */
    public HostingPrice(float price, float vat) {
        this.price = price;
        this.vat = vat;
    }

    public void setPaymentPeriod(String paymentPeriod) {
        this.paymentPeriod = paymentPeriod;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * @return price without VAT
     */
    public float getPrice() {
        return price;
    }

    /**
     * @return price with VAT
     */
    public float getPriceWithVat() {
        return price * (1.0f + vat);
    }

    public float getVat() {
        return vat;
    }

    /**
     * @return label for payment period
     */
    public String getPaymentPeriod() {
        return paymentPeriod;
    }

    /**
     * @return short code for currency
     */
    public String getCurrency() {
        return currency;
    }
}
