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
package cz.abclinuxu.persistence;

import junit.framework.TestCase;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.NestedCondition;
import cz.abclinuxu.persistence.extra.LogicalOperation;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.OperationIn;
import cz.abclinuxu.persistence.extra.QualifierTool;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.servlets.Constants;

import java.util.List;
import java.util.ArrayList;

/**
 * @author literakl
 * @since 1.6.2008
 */
public class TestQualifiers extends TestCase {

    public void testNestedCondition() throws Exception {
        CompareCondition typeDiscussion = new CompareCondition(Field.TYPE, Operation.EQUAL, Item.DISCUSSION);
        CompareCondition subtypeQuestion = new CompareCondition(Field.SUBTYPE, Operation.EQUAL, Constants.SUBTYPE_QUESTION);
        CompareCondition subtypeIsNull = new CompareCondition(Field.SUBTYPE, Operation.IS_NULL, null);
        NestedCondition questions = new NestedCondition(new Qualifier[] {typeDiscussion, subtypeQuestion}, LogicalOperation.AND);
        NestedCondition discussions = new NestedCondition(new Qualifier[] {typeDiscussion, subtypeIsNull}, LogicalOperation.AND);
        Object[] types = new Object[] {Item.ARTICLE, Item.NEWS};
        CompareCondition inCondition = new CompareCondition(Field.TYPE, new OperationIn(2), types);
        NestedCondition nested = new NestedCondition(new Qualifier[] {inCondition, questions, discussions}, LogicalOperation.OR);
        Qualifier[] qualifiers = new Qualifier[] {nested, Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING};
        StringBuilder sb = new StringBuilder("SELECT * FROM polozka p, spolecne s");
        List params = new ArrayList();
        QualifierTool.appendQualifiers(sb, qualifiers, params, "P", null);
        assertEquals("select * from polozka p, spolecne s where (p.typ in (?,?) or (p.typ=? and p.podtyp=?) or (p.typ=? and p.podtyp is null)) order by s.zmeneno desc", sb.toString().toLowerCase());
        assertEquals(5, params.size());
        assertEquals(2, params.get(0));
        assertEquals(7, params.get(1));
        assertEquals(3, params.get(2));
        assertEquals("question", params.get(3));
        assertEquals(3, params.get(4));
    }
}
