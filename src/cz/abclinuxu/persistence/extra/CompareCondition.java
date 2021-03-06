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
package cz.abclinuxu.persistence.extra;

/**
 * Sets condition.
 */
public class CompareCondition extends Qualifier {
    Field field;
    Operation operation;
    Object value;
    boolean negated;

    /**
     * Creates new CompareCondition.
     * @param field field to be compared.
     * @param operation comparation operation.
     * @param value value to be compared.
     */
    public CompareCondition(Field field, Operation operation, Object value) {
        super("COMPARE_QUALIFIER");
        this.field = field;
        this.operation = operation;
        this.value = value;
    }

    /**
     * Creates new CompareCondition.
     * @param field field to be compared.
     * @param operation comparation operation.
     * @param value value to be compared.
     */
    public CompareCondition(Field field, Operation operation, Object value, boolean negated) {
        this(field, operation, value);
        this.negated = negated;
    }

    /**
     * @return field to be compared.
     */
    public Field getField() {
        return field;
    }

    /**
     * @return comparation operation.
     */
    public Operation getOperation() {
        return operation;
    }

    /**
     * @return value to be compared.
     */
    public Object getValue() {
        return value;
    }

    /**
     * @return when this operation shall be negated
     */
    public boolean isNegated() {
        return negated;
    }
}
