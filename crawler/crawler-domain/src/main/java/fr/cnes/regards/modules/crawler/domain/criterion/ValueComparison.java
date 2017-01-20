package fr.cnes.regards.modules.crawler.domain.criterion;

/**
 * Pair of comparison operator and value
 * @param <T> value type
 * @author oroussel
 */
public class ValueComparison<T> {

    /**
     * Operator
     */
    private ComparisonOperator operator;

    /**
     * Value to compare
     */
    private T value;

    public ValueComparison(ComparisonOperator pOperator, T pValue) {
        super();
        operator = pOperator;
        value = pValue;
    }

    public ComparisonOperator getOperator() {
        return operator;
    }

    public void setOperator(ComparisonOperator pOperator) {
        operator = pOperator;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T pValue) {
        value = pValue;
    }

    // hashCode() and equals() use only operator because ValueComparison is used
    // into a set and must not be encountered twice with same operator
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((operator == null) ? 0 : operator.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ValueComparison<?> other = (ValueComparison<?>) obj;
        if (operator != other.operator) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ValueComparison [operator=" + operator + ", value=" + value + "]";
    }

}
