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
package cz.abclinuxu.utils.forms;

/**
 * Allows object implementing this interface to be selected, for example
 * in HTML form
 * @author kapy
 */
public interface Selectable {

    /**
     * Checks whether object is selected
     * @return <code>true</code> if object is selected, <code>false</code> otherwise
     */
    public boolean isSet();

    /**
     * Sets value of selection flag
     * @param set New value of selection flag
     */
    public void setSet(boolean set);
}
