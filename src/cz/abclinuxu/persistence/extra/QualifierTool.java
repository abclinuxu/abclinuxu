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

import cz.abclinuxu.utils.Misc;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Date;
import java.sql.Timestamp;

/**
 * Helper class to translate Qualifiers to SQL query.
 * 
 * @author literakl
 * @since 1.6.2008
 */
public class QualifierTool {

	/**
	 * Appends qualifiers to StringBuilder holding SQL command. DefaultTableNick
	 * is added before every column in custom conditions (in WHERE or ORDER BY
	 * clauses). if defaultTableNick cannot distinguish between two tables,
	 * fieldMapping can be used to assign exact tableNick to specific Field from
	 * qualifiers.
	 * 
	 * @param defaultTableNick
	 *            nick of table to distinguish columns. Default is null.
	 * @param qualifiers
	 *            list of query conditions and sort order and limit qualifiers.
	 *            The order is important.
	 * @param fieldMapping
	 *            key is PersistenceMapping.Table, value is tableNick to be
	 *            used.
	 */
	public static void appendQualifiers(StringBuilder sb,
			Qualifier[] qualifiers, List params, String defaultTableNick,
			Map<Field, String> fieldMapping) {
		if (qualifiers == null || qualifiers.length == 0)
			return;
		if (fieldMapping == null)
			fieldMapping = Collections.emptyMap();
		if (params == null)
			params = new ArrayList(3);

		Qualifier qualifier;
		for (int i = 0; i < qualifiers.length; i++) {
			qualifier = qualifiers[i];

			if (qualifier instanceof OrderByQualifier) {
				sb.append(" ORDER BY ");
				OrderByQualifier oq = (OrderByQualifier) qualifier;				
				appendField(oq.getField(), fieldMapping, defaultTableNick, sb);
			} else if (qualifier.equals(Qualifier.ORDER_ASCENDING)) {
				sb.append(" ASC");
			} else if (qualifier.equals(Qualifier.ORDER_DESCENDING)) {
				sb.append(" DESC");
			} else if (qualifier instanceof LimitQualifier) {
				sb.append(" LIMIT ?,?");
				LimitQualifier limitQualifier = (LimitQualifier) qualifier;
				params.add(limitQualifier.getOffset());
				params.add(limitQualifier.getCount());
			} else {
				int where = sb.indexOf("where ");
				if (where == -1)
					where = sb.indexOf("WHERE ");
				if (where == -1)
					sb.append(" WHERE ");
				else {
					if (where + 6 < sb.length())
						sb.append(" AND ");
				}
				if (qualifier instanceof CompareCondition) {
					appendCompareCondition(sb, (CompareCondition) qualifier,
							params, defaultTableNick, fieldMapping);
				} else if (qualifier instanceof NestedCondition) {
					appendNestedCondition(sb, (NestedCondition) qualifier,
							params, defaultTableNick, fieldMapping);
				}
			}
		}
	}

	/**
	 * Removes sort by, order by and limit qualifiers, which cannot be used in
	 * count statements.
	 * 
	 * @param qualifiers
	 *            qualifiers
	 * @return stripped version of qualifiers
	 */
	public static Qualifier[] removeOrderQualifiers(Qualifier[] qualifiers) {
		List<Qualifier> safeQualifiers = new ArrayList<Qualifier>(
				qualifiers.length);
		for (int i = 0; i < qualifiers.length; i++) {
			Qualifier qualifier = qualifiers[i];
			if (qualifier instanceof OrderByQualifier)
				continue;
			else if (qualifier.equals(Qualifier.ORDER_ASCENDING))
				continue;
			else if (qualifier.equals(Qualifier.ORDER_DESCENDING))
				continue;
			else if (qualifier instanceof LimitQualifier)
				continue;
			safeQualifiers.add(qualifier);
		}
		return safeQualifiers.toArray(new Qualifier[safeQualifiers.size()]);
	}

	/**
	 * Appends nested condition to StringBuilder
	 */
	private static void appendNestedCondition(StringBuilder sb,
			NestedCondition condition, List params, String defaultTableNick,
			Map<Field, String> fieldMapping) {
		sb.append('(');

		boolean first = true;
		for (Qualifier qualifier : condition.getQualifiers()) {
			if (!first) {
				if (condition.getOperation() == LogicalOperation.OR)
					sb.append(" OR ");
				else
					sb.append(" AND ");
			} else
				first = false;

			if (qualifier instanceof NestedCondition)
				appendNestedCondition(sb, (NestedCondition) qualifier, params,
						defaultTableNick, fieldMapping);
			else
				appendCompareCondition(sb, (CompareCondition) qualifier,
						params, defaultTableNick, fieldMapping);
		}

		sb.append(')');
	}

	/**
	 * Append comparation condition to StringBuilder.
	 */
	private static void appendCompareCondition(StringBuilder sb,
			CompareCondition condition, List params, String defaultTableNick,
			Map<Field, String> fieldMapping) {
		appendField(condition.getField(), fieldMapping, defaultTableNick, sb);

		if (condition.isNegated())
			sb.append(" NOT ");

		Operation operation = condition.getOperation();
		if (operation == Operation.GREATER)
			sb.append(">");
		else if (operation == Operation.GREATER_OR_EQUAL)
			sb.append(">=");
		else if (operation == Operation.SMALLER)
			sb.append("<");
		else if (operation == Operation.SMALLER_OR_EQUAL)
			sb.append("<=");
		else if (operation == Operation.EQUAL)
			sb.append("=");
		else if (operation == Operation.NOT_EQUAL)
			sb.append("!=");
		else if (operation == Operation.LIKE)
			sb.append(" LIKE ");
		else if (operation instanceof OperationIn)
			sb.append(" IN ").append(
					Misc.getInCondition(((OperationIn) operation).getCount()));
		else if (operation == Operation.IS_NULL) {
			sb.append(" IS NULL");
			return;
		}

		Object value = condition.getValue();
		if (value instanceof Field) {
			appendField((Field) condition.getValue(), fieldMapping,
					defaultTableNick, sb);
		} else if (value instanceof SpecialValue) {
			sb.append(value.toString());
		} else if (value instanceof Collection) {
			for (Iterator iter = ((Collection) value).iterator(); iter
					.hasNext();) {
				Object o = iter.next();
				addObjectToParams(o, params);
			}
		} else if (value instanceof Object[]) {
			for (int i = 0; i < ((Object[]) value).length; i++) {
				Object o = ((Object[]) value)[i];
				addObjectToParams(o, params);
			}
		} else {
			sb.append("?");
			addObjectToParams(value, params);
		}
	}

	private static void addObjectToParams(Object o, List params) {
		if (o instanceof Date)
			o = new Timestamp(((Date) o).getTime());
		params.add(o);
	}

	/**
	 * Appends table nick for specified field into StringBuilder. The priority
	 * is to search fieldMapping first, then use defaultTableNick from the field
	 * if defined, then argument defaultTableNick otherwise do nothing.
	 */
	private static void addTableNick(Field field,
			Map<Field, String> fieldMapping, String defaultTableNick,
			StringBuilder sb) {
		String tableNick = null;
		if (fieldMapping != null)
			tableNick = fieldMapping.get(field);
		if (tableNick == null)
			tableNick = field.getDefaultTableNick();
		if (tableNick == null)
			tableNick = defaultTableNick;
		if (tableNick != null) {
			sb.append(tableNick);
			sb.append(".");
		}
	}

	private static void appendField(Field field,
			Map<Field, String> fieldMapping, String defaultTableNick,
			StringBuilder sb) {
		addTableNick(field, fieldMapping, defaultTableNick, sb);

		switch (field.getId()) {
		case CHILD:
			sb.append("potomek");
			break;
		case CHILD_TYPE:
			sb.append("typ_potomka");
			break;
		case CREATED:
			sb.append("vytvoreno");
			break;
		case DATA:
			sb.append("data");
			break;
		case DATE1:
			sb.append("date1");
			break;
		case DATE2:
			sb.append("date2");
			break;
		case DAY:
			sb.append("den");
			break;
		case ID:
			sb.append("cislo");
			break;
		case LOGIN:
			sb.append("login");
			break;
		case OWNER:
			sb.append("pridal");
			break;
		case NUMERIC1:
			sb.append("numeric1");
			break;
		case NUMERIC2:
			sb.append("numeric2");
			break;
		case PARENT:
			sb.append("predek");
			break;
		case PARENT_TYPE:
			sb.append("typ_predka");
			break;
		case STRING1:
			sb.append("string1");
			break;
		case STRING2:
			sb.append("string2");
			break;
		case SUBTYPE:
			sb.append("podtyp");
			break;
		case TITLE:
			sb.append("jmeno");
			break;
		case TYPE:
			sb.append("typ");
			break;
		case UPDATED:
			sb.append("zmeneno");
			break;
		case UPPER:
			sb.append("predchozi");
			break;
		case WHEN:
			sb.append("kdy");
		}
	}
}
