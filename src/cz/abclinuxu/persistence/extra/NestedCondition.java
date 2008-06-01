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
package cz.abclinuxu.persistence.extra;

import java.util.List;
import java.util.Arrays;

/**
 * Holds single condition created by logical operation between qualifiers in the list. This object will be rendered
 * to SQL in this way "(first or second or third)".
 * @author literakl
 * @since 1.6.2008
 */
public class NestedCondition extends Qualifier {
    List<Qualifier> qualifiers;
    LogicalOperation operation;

    public NestedCondition(Qualifier[] qualifiers, LogicalOperation operation) {
        this(Arrays.asList(qualifiers), operation);
    }

    public NestedCondition(List<Qualifier> qualifiers, LogicalOperation operation) {
        super("NESTED_QUALIFIER");
        this.qualifiers = qualifiers;
        this.operation = operation;
    }

    public List<Qualifier> getQualifiers() {
        return qualifiers;
    }

    public LogicalOperation getOperation() {
        return operation;
    }
}
