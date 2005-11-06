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
package cz.abclinuxu.utils.email.forum;

/**
 * Value holder for one subscribed user.
 */
public class Subscription {
    Integer id;
    String email;

    public Subscription(Integer id, String email) {
        this.id = id;
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean equals(Object o) {
        if ( this==o ) return true;
        if ( !(o instanceof Subscription) ) return false;

        final Subscription subscription = (Subscription) o;

        if ( !email.equals(subscription.email) ) return false;
        if ( !id.equals(subscription.id) ) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = id.hashCode();
        result = 29*result+email.hashCode();
        return result;
    }
}
