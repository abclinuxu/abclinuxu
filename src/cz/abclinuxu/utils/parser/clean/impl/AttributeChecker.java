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
package cz.abclinuxu.utils.parser.clean.impl;

import cz.abclinuxu.utils.parser.clean.exceptions.AttributeValueNotAllowedException;

/**
 * Extensible way how to peform some additional or non-standard checks on attributes.
 * @author literakl
 * @since 22.11.2008
 */
public interface AttributeChecker extends Checker {

    /**
     * Performs some check on passed text and throws exception, if its contract is not fullfilled.
     * @param attribute value to be checked, it is never empty or null
     * @param tag tag name
     * @param attribute attribute name
     * @throws AttributeValueNotAllowedException value is forbidden
     */
    public void check(String text, String tag, String attribute) throws AttributeValueNotAllowedException;
}
