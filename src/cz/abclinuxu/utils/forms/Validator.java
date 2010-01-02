/*
 * Copyright (C) 2009 Karel Piwko
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; see the file COPYING. If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */
package cz.abclinuxu.utils.forms;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import cz.abclinuxu.data.User;
import cz.abclinuxu.data.view.Author;
import cz.abclinuxu.exceptions.InternalException;
import cz.abclinuxu.exceptions.InvalidDataException;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.utils.Misc;

/**
 * Validates output from form and updates validation candidate accordingly
 * to passed values
 * 
 * @author kapy
 * 
 * @param <T> Type of validation candidate
 */
public abstract class Validator<T> {

	protected static final Logger log = Logger.getLogger(Validator.class);

	/**
	 * Object which is modified during validation
	 */
	protected T validee;
	/**
	 * Access to execution context
	 */
	protected Map env;
	/**
	 * Parameters passed in HTTP request
	 */
	protected Map params;
	/**
	 * HTTP session to make scope larger
	 */
	protected HttpSession session;
	/**
	 * Mapping between fields of validation candidate and corresponding objects
	 * passed in HTTP request
	 */
	protected Map<String, Field> beanMapping;

	/**
	 * Creates validator object to validate content passed in HTTP request
	 * 
	 * @param validee Object whose attributes are set during validation
	 * @param beanMapping Mapping between object attribute fields and names of
	 *            parameters passed in HTTP request
	 * @param env Execution context
	 * @param session HTTP session
	 */
	public Validator(T validee, Map<String, Field> beanMapping, Map env, HttpSession session) {
		this.validee = validee;
		this.env = env;
		this.beanMapping = beanMapping;
		this.params = (Map) env.get(Constants.VAR_PARAMS);
		this.session = session;
	}

	public abstract boolean setAndValidate();

	/**
	 * Validates that field is not empty and set value of appropriate field on
	 * validation candidate. Field value is always set.
	 * 
	 * @param <C> Type of class
	 * @param paramType Type of class to be instantiated from paramType from
	 *            argument retrieved under paramName key. This class must not be
	 *            of primitive type unless input is always correct
	 * @param paramName Name of parameter to be retrieved
	 * @param errorMessage Message added to paramName if anything goes wrong
	 * @return {@code true} if validation succeeded
	 */
	protected <C> boolean validateNotEmptyAndSet(Class<C> paramType, String paramName, String errorMessage) {
		String value = (String) params.get(paramName);
		if (Misc.empty(value)) {
			ServletUtils.addError(paramName, errorMessage, env, session);
			return false;
		}
		return setBeanField(paramType, paramName, value, errorMessage);
	}

	/**
	 * Sets field value on validated candidate. If value set is null, returns
	 * {@code false}
	 * 
	 * @param paramType Type of class to be instantiated from paramType from
	 *            argument retrieved under paramName key. This class must not be
	 *            of primitive type unless input is always correct
	 * @param paramName Name of parameter to be retrieved
	 * @param errorMessage Message added to paramName if anything goes wrong
	 * @return {@code true} if field was set to not {@code null} value, {@code
	 *         false} otherwise
	 */
	protected <C> boolean setBeanField(Class<C> paramType, String paramName, String value, String errorMessage) {
		try {
			Field field = beanMapping.get(paramName);
			field.setAccessible(true);

			// set field value 
			C fieldValue = transform(paramType, paramName, value);
			field.set(validee, fieldValue);
			return fieldValue != null;
		} catch (IllegalArgumentException e) {
			log.warn("Trying to validate returned primitive object, assignment failed", e);
		} catch (Exception e) {
			throw new InternalException("Unable to set a field during validation of object", e);
		}
		return false;
	}

	/**
	 * Transforms value into instance of given class.
	 * @param clazz Type of class to be returned
	 * @param paramName Name of parameter in context
	 * @param value String value of parameter
	 * @return Transformed value in case of success, {@code null} if anything
	 *         goes wrong
	 */
	protected <C> C transform(Class<C> clazz, String paramName, String value) {
		if (value == null)
            return null;

		if (String.class.equals(clazz)) {
			return clazz.cast(value);
		} else if (Boolean.class.equals(clazz)) {
			if ("1".equals(value) || "true".equalsIgnoreCase(value) || "ano".equalsIgnoreCase(value))
				return clazz.cast(Boolean.TRUE);
			else if ("0".equals(value) || "false".equalsIgnoreCase(value) || "ne".equalsIgnoreCase(value))
				return clazz.cast(Boolean.FALSE);
			else {
				ServletUtils.addError(paramName, "Chybný formát hodnoty boolean", env, session);
				return null;
			}
		} else if (Double.class.equals(clazz)) {
			try {
				double val = Double.valueOf(value);
				return clazz.cast(val);
			} catch (NumberFormatException e) {
				ServletUtils.addError(paramName, "Číslo je ve špatném formátu", env, session);
				return null;
			}
		} else if (Integer.class.equals(clazz)) {
			try {
				int val = Integer.valueOf(value);
				return clazz.cast(val);
			} catch (NumberFormatException e) {
				ServletUtils.addError(paramName, "Číslo je ve špatném formátu", env, session);
				return null;
			}
		} else if (Date.class.equals(clazz)) {
			try {
                // date in iso format
                synchronized (Constants.isoFormatShort) {
					return clazz.cast(Constants.isoFormatShort.parse(value));
				}
			} catch (ParseException e) {
				ServletUtils.addError(paramName, "Chybný formát datumu!", env, session);
				return null;
			}
		} else if (Author.class.equals(clazz)) {
			try {
				int intVal = Integer.valueOf(value);
				Author tmp = new Author();
				tmp.setRelationId(intVal);
				return clazz.cast(tmp);
			} catch (NumberFormatException e) {
				ServletUtils.addError(paramName, "Chybný formát identifikace autora", env, session);
				return null;
			}
		} else if (User.class.equals(clazz)) {
			try {
				int intVal = Integer.valueOf(value);
				User tmp = new User(intVal);
				return clazz.cast(tmp);
			} catch (NumberFormatException e) {
				ServletUtils.addError(paramName, "Chybný formát identifikace uživatele", env, session);
				return null;
			}
		} else
			throw new InvalidDataException("Unable to convert string value " + value + " to class of " + clazz.getCanonicalName());
	}
}
