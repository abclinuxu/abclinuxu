package cz.abclinuxu.utils.forms;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.LogicalOperation;
import cz.abclinuxu.persistence.extra.NestedCondition;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.utils.Misc;

/**
 * Allows form to apply different filtering conditions for result.
 * 
 * @author kapy
 * 
 */
public class FormFilter {
    private static final Logger log = Logger.getLogger(FormFilter.class);

    public static final String AUTHORS_BY_NAME = "filterAuthorsByName";
    public static final String AUTHORS_BY_SURNAME = "filterAuthorsBySurname";
    public static final String AUTHORS_BY_CONTRACT = "filterAuthorsByContract";
    public static final String AUTHORS_BY_ACTIVE = "filterAuthorsByActive";
    public static final String AUTHORS_BY_ARTICLES = "filterAuthorsByArticles";
    public static final String AUTHORS_BY_RECENT = "filterAuthorsByRecent";

    /** This is string prefix for LIKE CompareConditions */
    private static final String LIKE_PREFIX = "";
    /** This is strig suffix for LIKE CompareConditions */
    private static final String LIKE_SUFFIX = "%";

    private Map<String, FilterQualifier> filters;

    /**
     * Creates form filter
     * 
     * @param params Parameters passed with HTTP header
     * @param filterNames Which filters are applied
     */
    public FormFilter(Map<? super Object, ? super Object> params, String... filterNames) {

	this.filters = new HashMap<String, FilterQualifier>();

	for (String paramName : filterNames) {
	    // skip parameter recognition if empty
	    Object param = params.get(paramName);
	    if (param == null || param.toString().length() == 0)
		continue;
	    initFilter(param, paramName);
	}
    }

    /**
     * Allows qualifier to be added manually after filter is created
     * 
     * @param paramName Name of parameter (or filter)
     * @param qualifer Qualifier if condition matches
     * @param formValue Value passed from form
     * @return Modified instance of filter
     */
    public FormFilter appendFilterQualifier(String paramName, Qualifier qualifer, String formValue) {
	filters.put(paramName, new FilterQualifier(qualifer, formValue));
	return this;
    }

    /**
     * Gets qualifiers stored in this filter
     * 
     * @return List of used qualifiers
     */
    public List<Qualifier> getQualifiers() {
	List<Qualifier> qualifiers = new ArrayList<Qualifier>();
	for (FilterQualifier fq : filters.values()) {
	    if (fq.qualifier != null)
		qualifiers.add(fq.qualifier);
	}
	return qualifiers;
    }

    /**
     * Gets qualifiers stored in this filter as array
     * 
     * @return Array of used qualifiers
     */
    public Qualifier[] getQualifiersAsArray() {
	return getQualifiers().toArray(Qualifier.ARRAY_TYPE);
    }

    /**
     * Returns data in filter as part of url GET string. This string is encoded
     * according to UTF-8 {@code application/x-www-form-urlencoded} MIME format.
     * String begins with &amp; character
     * 
     * @return Encoded content of filter or {@code null} if error occurred
     */
    public String encodeAsURL() {
	StringBuilder sb = new StringBuilder();

	for (Map.Entry<String, FilterQualifier> entry : filters.entrySet()) {
	    try {
		sb.append("&amp;")
		    .append(entry.getKey())
		    .append('=')
		    .append(URLEncoder.encode(entry.getValue().formValue, "UTF-8"));
	    } catch (UnsupportedEncodingException uee) {
		log.error("Couldn't not convert form parameters to UTF-8 URL encoded string in string: " + entry.getValue().formValue, uee);
		return null;
	    }
	}
	return sb.toString();
    }

    /**
     * Checks whether for given parameter value was passed
     * 
     * @param param Name of parameter
     * @param value Value of parameter
     * @return {@code true} if matched, {@code false} otherwise
     */
    public boolean checked(String param, String value) {

	if (log.isDebugEnabled()) {
	    log.debug("paramName: " + param + " value: " + value);
	    log.debug("found: " + filters.get(param));
	}

	FilterQualifier fq = filters.get(param);
	return fq != null && value.equals(fq.formValue);
    }

    /**
     * Returns value of parameter, as passed from form
     * 
     * @param param Value passed from form
     * @return Value of parameter passed od {@code null}
     */
    public String value(String param) {
	FilterQualifier fq = filters.get(param);
	return (fq != null) ? fq.formValue : "";
    }

    /**
     * Adds filter for given name and given object
     * 
     * @param param Object found for given filter
     * @param filterName Name of filter
     */
    private void initFilter(Object param, String filterName) {
	// TODO Do we have to sanify somehow this input?
	String formValue = param.toString();

	// select according to type of condition

	// author name has append to be regexp
	if (AUTHORS_BY_NAME.equals(filterName)) {
	    filters.put(filterName, new FilterQualifier(new CompareCondition(Field.STRING1, Operation.LIKE, LIKE_PREFIX + param
		+ LIKE_SUFFIX), formValue));
	} else if (AUTHORS_BY_SURNAME.equals(filterName)) {
	    filters.put(filterName, new FilterQualifier(new CompareCondition(Field.STRING2, Operation.LIKE, LIKE_PREFIX + param
		+ LIKE_SUFFIX), formValue));
	} else if (AUTHORS_BY_ACTIVE.equals(filterName)) {
	    int value = Misc.parseInt(formValue, 0);
	    filters.put(filterName, new FilterQualifier(construct(Field.NUMERIC2, value, Operation.EQUAL, 1, Operation.EQUAL, IsNullCheck.BEFORE_CAP), formValue));
	} else if (AUTHORS_BY_ARTICLES.equals(filterName)) {
	    int value = Misc.parsePossiblyWrongInt(formValue);
	    filters.put(filterName, new FilterQualifier(construct(Field.COUNTER, value, Operation.SMALLER, 101, Operation.GREATER_OR_EQUAL, IsNullCheck.BEFORE_CAP), formValue));
	} else if (AUTHORS_BY_RECENT.equals(filterName)) {
	    Calendar cal = Calendar.getInstance();
	    Calendar ref = (Calendar) cal.clone();
	    ref.set(Calendar.MONTH, ref.get(Calendar.MONTH) - 24);
	    java.sql.Date cap = new java.sql.Date(ref.getTimeInMillis());

	    int intValue = Misc.parsePossiblyWrongInt(formValue);
	    java.sql.Date value = null;
	    if (intValue == 25) {
		ref.set(Calendar.MILLISECOND, ref.get(Calendar.MILLISECOND) - 1);
		value = new java.sql.Date(ref.getTimeInMillis());
	    } else {
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - intValue);
		value = new java.sql.Date(cal.getTimeInMillis());
	    }

	    filters.put(filterName, new FilterQualifier(construct(Field.WHEN, value, Operation.SMALLER_OR_EQUAL, cap, Operation.GREATER, IsNullCheck.NO_CHECK), formValue));
	} else if (AUTHORS_BY_CONTRACT.equals(filterName)) {
	    filters.put(filterName, new FilterQualifier(null, formValue));
	}

    }

    /**
     * Constructs Compare condition or nested condition with IS NULL variant
     * 
     * @param <T> Type of cap and value
     * @param field Field to be compared
     * @param value Value passed from form
     * @param before Operation applied to comparison before cap (excluded) is
     *            reached
     * @param cap Value of cap
     * @param after Operation applied to comparison after cap (included) is
     *            reached
     * @param inc How to determine IS NULL presence
     * @return Qualifier for given field
     */
    private <T extends Comparable<? super T>> Qualifier construct(Field field, T value, Operation before,
	T cap, Operation after, IsNullCheck inc) {

	boolean beforeCap = cap.compareTo(value) > 0;
	Operation op = (beforeCap) ? before : after;

	List<Qualifier> list = new ArrayList<Qualifier>();

	switch (inc) {
	case BEFORE_CAP:
	    if (beforeCap)
		list.add(new CompareCondition(field, Operation.IS_NULL, 0));
	    list.add(new CompareCondition(field, op, value));
	    break;
	case AFTER_CAP:
	    if (!beforeCap)
		list.add(new CompareCondition(field, Operation.IS_NULL, 0));
	    list.add(new CompareCondition(field, op, value));
	    break;
	case ALL_CHECK:
	    list.add(new CompareCondition(field, Operation.IS_NULL, 0));
	case NO_CHECK:
	    list.add(new CompareCondition(field, op, value));
	    break;
	}

	if (list.size() == 1)
	    return list.get(0);

	return new NestedCondition(list, LogicalOperation.OR);

    }

    /**
     * Filter qualifier contains value passed from form and qualifier for
     * narrowing SQL query. This qualifier can be empty, if some different
     * approach to narrow query is needed.
     * 
     * @author kapy
     * 
     */
    private static class FilterQualifier {
	Qualifier qualifier;
	String formValue;

	public FilterQualifier(Qualifier qualifer, String formValue) {
	    this.qualifier = qualifer;
	    this.formValue = formValue;
	}

	@Override
	public String toString() {
	    return "FQ:" + formValue;
	}
    }

    /**
     * Determines whether IS NULL condition is added and when
     * 
     * @author kapy
     * 
     */
    private enum IsNullCheck {
	/** if value is before cap, add IS NULL condition */
	BEFORE_CAP,
	/** if value is after cap, add IS NULL condition */
	AFTER_CAP,
	/** do not add IS NULL condition */
	NO_CHECK,
	/** add is null condition in both cases */
	ALL_CHECK
    };

}
