/*
 * User: literakl
 * Date: 21.12.2003
 * Time: 14:01:32
 */
package cz.abclinuxu.persistance.extra;

/**
 * Sets condition.
 */
public class CompareCondition extends Qualifier {
    Field field;
    Operation operation;
    Object value;

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
}
