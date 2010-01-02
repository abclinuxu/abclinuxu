package cz.abclinuxu.utils.forms;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.LogicalOperation;
import cz.abclinuxu.persistence.extra.NestedCondition;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.SpecialValue;
import cz.abclinuxu.utils.Misc;

/**
 * Allows form to apply different filtering conditions for result.
 * Allows multiple choice of objects
 *
 * @author kapy
 */
public class FormFilter {
    private static final Logger log = Logger.getLogger(FormFilter.class);

    public static final String PARAM_FILTER_AUTHORS_BY_NAME = "filterAuthorsByName";
    public static final String PARAM_FILTER_TOPICS_BY_ROYALTY = "filterTopicsByRoyalty";
    public static final String PARAM_FILTER_TOPICS_BY_STATE = "filterTopicsByState";
    public static final String PARAM_FILTER_TOPICS_BY_DEADLINE = "filterTopicsByDeadline";
    public static final String PARAM_FILTER_TOPICS_BY_AUTHOR = "filterTopicsByAuthor";
    public static final String PARAM_FILTER_TOPICS_BY_PUBLIC = "filterTopicsByPublic";
    public static final String PARAM_FILTER_TOPICS_BY_TITLE = "filterTopicsByTitle";
    public static final String PARAM_FILTER_AUTHORS_BY_RECENT = "filterAuthorsByRecent";
    public static final String PARAM_FILTER_AUTHORS_BY_ARTICLES = "filterAuthorsByArticles";
    public static final String PARAM_FILTER_AUTHORS_BY_ACTIVE = "filterAuthorsByActive";
    public static final String PARAM_FILTER_AUTHORS_BY_CONTRACT = "filterAuthorsByContract";
    public static final String PARAM_FILTER_AUTHORS_BY_SURNAME = "filterAuthorsBySurname";

    public static enum Filter {
        AUTHORS_BY_NAME(PARAM_FILTER_AUTHORS_BY_NAME) {
            @Override
            protected FilterQualifier construct(Object value) {
                return new FilterQualifier(new CompareCondition(new Field(Field.STRING1, "P"), Operation.LIKE, LIKE_PREFIX + value + LIKE_SUFFIX), value);
            }
        },

        AUTHORS_BY_SURNAME(PARAM_FILTER_AUTHORS_BY_SURNAME) {
            @Override
            protected FilterQualifier construct(Object value) {
                return new FilterQualifier(new CompareCondition(new Field(Field.STRING2, "P"), Operation.LIKE, LIKE_PREFIX + value + LIKE_SUFFIX), value);
            }
        },

        AUTHORS_BY_CONTRACT(PARAM_FILTER_AUTHORS_BY_CONTRACT) {
            @Override
            protected FilterQualifier construct(Object value) {
                int intValue = Misc.parseInt(value.toString(), 0);
                Qualifier qualifier;
                if (intValue == 1)
                    qualifier = new CompareCondition(new Field(Field.BOOLEAN2, "CT"), Operation.EQUAL, 1);
                else if (intValue == 0)
                    qualifier = new CompareCondition(new Field(Field.BOOLEAN2, "CT"), Operation.EQUAL, 0);
                else
                    qualifier = new CompareCondition(new Field(Field.BOOLEAN2, "CT"), Operation.IS_NULL, null);
                return new FilterQualifier(qualifier, value);
            }
        },

        AUTHORS_BY_ACTIVE(PARAM_FILTER_AUTHORS_BY_ACTIVE) {
            @Override
            protected FilterQualifier construct(Object value) {
                int intValue = Misc.parseInt(value.toString(), 0);
                return new FilterQualifier(nestedQualifer(new Field(Field.BOOLEAN1, "P"), intValue, Operation.EQUAL, 1, Operation.EQUAL, IsNullCheck.BEFORE_CAP), value);
            }
        },

        AUTHORS_BY_ARTICLES(PARAM_FILTER_AUTHORS_BY_ARTICLES) {
            @Override
            protected FilterQualifier construct(Object value) {
                Pair pair = new Pair(value.toString());
                // double-single value
                if (pair.single != null) {
                    return new FilterQualifier(nestedQualifer(Field.COUNTER, pair.single, Operation.SMALLER, 100, Operation.GREATER_OR_EQUAL, IsNullCheck.BEFORE_CAP), value);
                }
                // interval of value
                else if (pair.right != null) {
                    Qualifier[] list = {
                            new CompareCondition(Field.COUNTER, Operation.GREATER_OR_EQUAL, pair.left),
                            new CompareCondition(Field.COUNTER, Operation.SMALLER_OR_EQUAL, pair.right)
                    };
                    return new FilterQualifier(new NestedCondition(list, LogicalOperation.AND), value.toString());
                }
                log.warn("Unable to determine article count from value " + value);
                return new FilterQualifier(null, value);
            }
        },

        AUTHORS_BY_RECENT(PARAM_FILTER_AUTHORS_BY_RECENT) {
            @Override
            protected FilterQualifier construct(Object value) {
                Calendar cal = Calendar.getInstance();
                Calendar ref = (Calendar) cal.clone();
                ref.set(Calendar.MONTH, ref.get(Calendar.MONTH) - 24);
                java.sql.Date cap = new java.sql.Date(ref.getTimeInMillis());

                int intValue = Misc.parseInt(value.toString(), 0);
                java.sql.Date dateValue;
                if (intValue == 25) {
                    ref.set(Calendar.MILLISECOND, ref.get(Calendar.MILLISECOND) - 1);
                    dateValue = new java.sql.Date(ref.getTimeInMillis());
                } else {
                    cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - intValue);
                    dateValue = new java.sql.Date(cal.getTimeInMillis());
                }

                return new FilterQualifier(nestedQualifer(Field.WHEN, dateValue, Operation.SMALLER_OR_EQUAL, cap, Operation.GREATER, IsNullCheck.NO_CHECK), value);
            }
        },

        TOPICS_BY_TITLE(PARAM_FILTER_TOPICS_BY_TITLE) {
            @Override
            public FilterQualifier construct(Object value) {
                return new FilterQualifier(new CompareCondition(Field.TITLE, Operation.LIKE, LIKE_PREFIX + value + LIKE_SUFFIX), value);
            }
        },

        TOPICS_BY_PUBLIC(PARAM_FILTER_TOPICS_BY_PUBLIC) {
            @Override
            public FilterQualifier construct(Object value) {
                if (asBoolean(value))
                    return new FilterQualifier(new CompareCondition(new Field(Field.NUMERIC1, "P"), Operation.IS_NULL, null), value);
                else
                    return new FilterQualifier(new CompareCondition(new Field(Field.NUMERIC1, "P"), Operation.IS_NOT_NULL, null), value);
            }
        },

        TOPICS_BY_AUTHOR(PARAM_FILTER_TOPICS_BY_AUTHOR) {
            @Override
            public FilterQualifier construct(Object value) {
                int intValue = Misc.parseInt(value.toString(), 0);
                return new FilterQualifier(new CompareCondition(new Field(Field.NUMERIC1, "P"), Operation.EQUAL, intValue), value);
            }
        },

        TOPICS_BY_DEADLINE(PARAM_FILTER_TOPICS_BY_DEADLINE) {
            @Override
            public FilterQualifier construct(Object value) {
                Pair pair = new Pair(value.toString());
                if (pair.isSingleValue()) {
                    if (pair.single == 0) // delayed
                        return new FilterQualifier(new CompareCondition(new Field(Field.DATE1, "P"), Operation.SMALLER_OR_EQUAL, SpecialValue.NOW), value);
                    else if (pair.single == 1) // no deadline
                        return new FilterQualifier(new CompareCondition(new Field(Field.DATE1, "P"), Operation.IS_NULL, null), value);
                } else
                    if (pair.right != null) {
                        Calendar left = Calendar.getInstance();
                        Calendar right = (Calendar) left.clone();
                        left.set(Calendar.MONTH, left.get(Calendar.MONTH) + pair.left);
                        left = getMonthStart(left);
                        right.set(Calendar.MONTH, right.get(Calendar.MONTH) + pair.right);
                        right = getMonthStart(right);

                        Qualifier[] list = {
                                new CompareCondition(new Field(Field.DATE1, "P"), Operation.GREATER_OR_EQUAL, new java.sql.Date(left.getTimeInMillis())),
                                new CompareCondition(new Field(Field.DATE1, "P"), Operation.SMALLER_OR_EQUAL, new java.sql.Date(right.getTimeInMillis()))
                        };
                        return new FilterQualifier(new NestedCondition(list, LogicalOperation.AND), value);
                    }

                log.warn("Unable to determine topics deadline date from value " + value);
                return new FilterQualifier(null, value);
            }
        },

        TOPICS_BY_STATE(PARAM_FILTER_TOPICS_BY_STATE) {
            @Override
            public FilterQualifier construct(Object value) {
                int intValue = Misc.parseInt(value.toString(), 0);
                Qualifier qualifier;
                if (intValue == -1) {
                    qualifier = new NestedCondition(new Qualifier[] {
                                    new CompareCondition(new Field(Field.NUMERIC1, "A"), Operation.NOT_EQUAL, 0),
                                    new CompareCondition(new Field(Field.NUMERIC1, "A"), Operation.IS_NULL, null)
                    }, LogicalOperation.OR);
                } else
                    qualifier = new CompareCondition(new Field(Field.NUMERIC1, "A"), Operation.EQUAL, intValue);
                return new FilterQualifier(qualifier, value);
            }
        },

        TOPICS_BY_ROYALTY(PARAM_FILTER_TOPICS_BY_ROYALTY) {
            @Override
            public FilterQualifier construct(Object value) {
                if (asBoolean(value))
                    return new FilterQualifier(new CompareCondition(new Field(Field.NUMERIC2, "P"), Operation.IS_NOT_NULL, null), value);
                else
                    return new FilterQualifier(new CompareCondition(new Field(Field.NUMERIC2, "P"), Operation.IS_NULL, null), value);
            }
        };

        // name of filter
        private final String name;

        private Filter(String name) {
            this.name = name;
        }

        /**
         * Returns name of filter qualifier
         *
         * @return Name of qualifier
         */
        public String getName() {
            return name;
        }

        /**
         * Constructs qualifier available of use in form filter
         *
         * @param value Value passed in form
         * @return Constructed qualifier
         */
        protected abstract FilterQualifier construct(Object value);

        /**
         * Interprets passed value as boolean
         *
         * @param value Passed value
         * @return {@code true} if value is String with value "1", "true" or
         *         "ano", {@code false} otherwise
         */
        protected final boolean asBoolean(Object value) {
            String text = value.toString();
            return ("1".equals(text) || "true".equalsIgnoreCase(text) || "ano".equalsIgnoreCase(text));
        }

        protected Calendar getMonthStart(Calendar cal) {
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 1);
            return cal;
        }

        /**
         * Constructs either compare condition or nested condition with IS NULL
         * variant.
         * Value passed is compared against the cap and depending on this
         * comparison either
         * operation before or operation after is applied. Behavior of null
         * check is
         * dependent on passed value.
         *
         * @param <T>    Type of cap and value
         * @param field  Field to be compared
         * @param value  Value passed from form
         * @param before Operation applied to comparison before cap (excluded)
         *               is
         *               reached
         * @param cap    Value of cap
         * @param after  Operation applied to comparison after cap (included) is
         *               reached
         * @param inc    How to determine IS NULL presence
         * @return Qualifier for given field
         */
        protected final <T extends Comparable<? super T>> Qualifier nestedQualifer(Field field, T value, Operation before, T cap, Operation after, IsNullCheck inc) {
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
    }

    // This is string prefix for LIKE CompareConditions
    private static final String LIKE_PREFIX = "%";

    // This is string suffix for LIKE CompareConditions
    private static final String LIKE_SUFFIX = "%";

    // filters assigned to current form
    private Map<String, FilterQualifier> filters;

    /**
     * Creates form filter
     *
     * @param params  Parameters passed with HTTP header
     * @param filters Which filters are applied
     */
    public FormFilter(Map params, Filter... filters) {
        this.filters = new HashMap<String, FilterQualifier>();
        for (Filter filter : filters) {
            // skip parameter recognition if empty
            Object param = params.get(filter.name);
            if (param == null || param.toString().length() == 0)
                continue;
            this.filters.put(filter.name, filter.construct(param));
        }
    }

    /**
     * Allows qualifier to be added manually after filter is created
     *
     * @param paramName Name of parameter (or filter)
     * @param qualifer  Qualifier if condition matches
     * @param formValue Value passed from form
     * @return Modified instance of filter
     */
    public FormFilter appendFilterQualifier(String paramName, Qualifier qualifer, Object formValue) {
        filters.put(paramName, new FilterQualifier(qualifer, formValue));
        return this;
    }

    /**
     * Allows removing qualifier after filter is created
     *
     * @param paramName Name of parameter (or filter)
     * @return Modified instance of filter
     */
    public FormFilter removeFilterQualifer(String paramName) {
        filters.remove(paramName);
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
     * Tests whether there were specified some of filters in parameters.
     * @return true if no filters were passed
     */
    public boolean isEmpty() {
        return filters.isEmpty();
    }

    /**
     * Tests for presence of parameter for given filter
     * @param filter some filter
     * @return true if the filter parameter was passed in parameters
     */
    public boolean isSet(Filter filter) {
        return filters.containsKey(filter.name);
    }

    /**
     * Gets qualifiers stored in this filter as array
     *
     * @return Array of used qualifiers
     */
    public Qualifier[] getQualifiersAsArray() {
        List<Qualifier> qualifiers = getQualifiers();
        return qualifiers.toArray(new Qualifier[qualifiers.size()]);
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
                String key = entry.getKey();
                FilterQualifier fq = entry.getValue();
                if (fq.isSingleValue())
                    sb.append("&amp;").append(key).append('=').append(URLEncoder.encode(fq.formValue.toString(), "UTF-8"));
                else
                    for (Object o : (Collection<?>) fq.formValue)
                        sb.append("&amp;").append(key).append('=').append(URLEncoder.encode(o.toString(), "UTF-8"));

            } catch (UnsupportedEncodingException uee) {
                log.error("Couldn't not convert form parameters to UTF-8 URL encoded string in string: " + entry.getValue(), uee);
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
        if (fq == null)
            return false;
        if (fq.isSingleValue())
            return value.equals(fq.formValue.toString());
        else {
            Collection candidates = (Collection) fq.formValue;
            return candidates.contains(value);
        }
    }

    /**
     * Returns value of parameter as passed from form. Allows multiple values.
     *
     * @param param Name of parameter passed from form
     * @return Either single value passed from form or collection of values
     */
    public Object value(String param) {
        FilterQualifier fq = filters.get(param);
        if (fq == null)
            return "";

        return fq.formValue;
    }

    /**
     * Filter qualifier contains value passed from form and qualifier for
     * narrowing SQL query. This qualifier can be empty, if some different
     * approach to narrow query is needed. As value any object can be passed,
     * internally is casted to String
     *
     * @author kapy
     */
    private static class FilterQualifier {
        Qualifier qualifier;
        Object formValue;

        public FilterQualifier(Qualifier qualifer, Object formValue) {
            this.qualifier = qualifer;
            this.formValue = formValue;
        }

        public boolean isSingleValue() {
            return !isMultipleValue();
        }

        public boolean isMultipleValue() {
            return (formValue instanceof Collection<?>);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (isMultipleValue()) {
                sb.append("{");
                for (Object o : (Collection<?>) formValue)
                    sb.append(o.toString()).append(",");
                sb.replace(sb.length() - 1, sb.length(), "}");
            } else {
                sb.append(formValue);
            }
            sb.append("\n").append("with qualifer: ").append(qualifier);

            return sb.toString();
        }
    }

    /**
     * Parses string into pair. If single value is given, creates single value
     *
     * @author kapy
     */
    private static class Pair {
        Integer left;
        Integer right;
        Integer single;

        public Pair(String string) {
            try {
                StringTokenizer lexer = new StringTokenizer(string, "-");
                switch (lexer.countTokens()) {
                    case 1:
                        single = Integer.valueOf(lexer.nextToken());
                        break;
                    case 2:
                        left = Integer.valueOf(lexer.nextToken());
                        right = Integer.valueOf(lexer.nextToken());
                        break;
                    default:
                        break;
                }
            } catch (NumberFormatException e) {
                log.error("Failed to convert '" + string + "' to numbers", e);
            }
        }

        public boolean isSingleValue() {
            return single != null;
        }
    }

    /**
     * Determines whether IS NULL condition is added and when
     *
     * @author kapy
     */
    private enum IsNullCheck {
        /**
         * if value is before cap, add IS NULL condition
         */
        BEFORE_CAP,
        /**
         * if value is after cap, add IS NULL condition
         */
        AFTER_CAP,
        /**
         * do not add IS NULL condition
         */
        NO_CHECK,
        /**
         * add is null condition in both cases
         */
        ALL_CHECK
    }
}
