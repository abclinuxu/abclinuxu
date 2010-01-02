package cz.abclinuxu.persistence.extra;

/**
 * Encapsulates qualifiers which are supposed to order results of SQL query
 * 
 * @author kapy
 * 
 */
public class OrderByQualifier extends Qualifier {

	// field on which we are ordering items
	private Field field; 
	
	/**
	 * Creates an ORDER BY SQL qualifier
	 * @param field Field used for comparison
	 * @param fieldName Name of field in table
	 */
	protected OrderByQualifier(String name, Field field) {
		super("ORDER_BY_" + name);
		this.field = field;
	}

    /**
     * Public constructor to override table nick mapping for selected Field.
     * @param qualifier existing OrderByQualifier
     * @param tableNick new table nick
     */
    public OrderByQualifier(Qualifier qualifier, String tableNick) {
        this(qualifier.name, new Field(((OrderByQualifier) qualifier).getField(), tableNick));
    }
	
	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}
	
	@Override
    public String toString() {
        return name;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		OrderByQualifier other = (OrderByQualifier) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		return true;
	}
		
}
